/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.core.user.AuthenticationUtils;
import com.opengamma.core.user.OGUser;
import com.opengamma.core.user.UserSource;
import com.opengamma.core.user.impl.SimpleOGUser;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.server.LastKnownValueStore;
import com.opengamma.livedata.server.LastKnownValueStoreProvider;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionReceiver;
import com.opengamma.transport.socket.ServerSocketFudgeConnectionReceiver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * The base server process for any Cogda Live Data Server.
 * <p/>
 * By default, it will operate in a completely unauthenticated mode. User names and passwords
 * in connection requests will be ignored, and all fields will be accessible by any connection.
 * However, in combination with an injected {@link UserSource} (see {@link #setUserSource(UserSource)}),
 * the server will authenticate users and authorize access.
 * If the server has a {@link UserSource} provided, but the {@code checkPassword} parameter
 * is set to false (see {@link #setCheckPassword(boolean)}) then only authorization will be provided and it is assumed
 * that authentication is handled elsewhere in the overall application and so user credentials
 * can be assumed to be valid without a password being provided.
 * <p/>
 * Because the {@link UserSource} will be hit for every authorization question, it is <strong>critical</strong>
 * that the source caches requests in some form.
 */
public class CogdaLiveDataServer implements FudgeConnectionReceiver, Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(CogdaLiveDataServer.class);
  /**
   * The default port on which the server will listen for inbound connections.
   */
  public static final int DEFAULT_LISTEN_PORT = 11876;
  private int _portNumber = DEFAULT_LISTEN_PORT;
  
  private final ServerSocketFudgeConnectionReceiver _connectionReceiver;
  private final LastKnownValueStoreProvider _lastKnownValueStoreProvider;
  private final ConcurrentMap<LiveDataSpecification, LastKnownValueStore> _lastKnownValueStores =
      new ConcurrentHashMap<LiveDataSpecification, LastKnownValueStore>();
  
  private final Set<CogdaClientConnection> _clients = Collections.synchronizedSet(new HashSet<CogdaClientConnection>());
  // TODO kirk 2012-07-23 -- This is absolutely the wrong executor here.
  private final Executor _valueUpdateSendingExecutor = Executors.newFixedThreadPool(5);
  private final AtomicLong _ticksReceived = new AtomicLong(0L);
  private UserSource _userSource;
  private boolean _checkPassword = true;
  
  public CogdaLiveDataServer(LastKnownValueStoreProvider lkvStoreProvider) {
    this(lkvStoreProvider, OpenGammaFudgeContext.getInstance());
  }
  
  public CogdaLiveDataServer(LastKnownValueStoreProvider lkvStoreProvider, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(lkvStoreProvider, "lkvStoreProvider");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _lastKnownValueStoreProvider = lkvStoreProvider;
    _connectionReceiver = new ServerSocketFudgeConnectionReceiver(fudgeContext, this);
    _connectionReceiver.setLazyFudgeMsgReads(false);
  }

  /**
   * Gets the portNumber.
   * @return the portNumber
   */
  public int getPortNumber() {
    return _portNumber;
  }

  /**
   * Sets the portNumber. Defaults to {@link #DEFAULT_LISTEN_PORT}.
   * This <b>must</b> be set <b>before</b> {@link #start()} is called.
   * @param portNumber  the portNumber
   */
  public void setPortNumber(int portNumber) {
    _portNumber = portNumber;
  }

  /**
   * Gets the lastKnownValueStoreProvider.
   * @return the lastKnownValueStoreProvider
   */
  public LastKnownValueStoreProvider getLastKnownValueStoreProvider() {
    return _lastKnownValueStoreProvider;
  }

  /**
   * Gets the userSource.
   * @return the userSource
   */
  public UserSource getUserSource() {
    return _userSource;
  }

  /**
   * Sets the userSource.
   * @param userSource  the userSource
   */
  public void setUserSource(UserSource userSource) {
    _userSource = userSource;
  }

  /**
   * Whether passwords will be checked.
   * @return true if passwords will be checked.
   */
  public boolean isCheckPassword() {
    return _checkPassword;
  }

  /**
   * Sets whether passwords will be checked.
   * Setting to false means that only authorization will be performed
   * rather than authentication.
   * @param checkPassword  false to turn off password checking on connections.
   */
  public void setCheckPassword(boolean checkPassword) {
    _checkPassword = checkPassword;
  }

  @Override
  public void connectionReceived(FudgeContext fudgeContext, FudgeMsgEnvelope message, FudgeConnection connection) {
    CogdaClientConnection clientConnection = new CogdaClientConnection(fudgeContext, this, connection);
    // We're blocked on connection acceptance. Therefore it's entirely fine
    // to do the handshake here as we won't get any more messages until
    // it's done.
    clientConnection.handshakeMessage(fudgeContext, message);
    _clients.add(clientConnection);
  }

  @Override
  
  public void start() {
    _connectionReceiver.setPortNumber(getPortNumber());
    _connectionReceiver.start();
  }

  @Override
  public void stop() {
    _connectionReceiver.stop();
  }

  @Override
  public boolean isRunning() {
    return _connectionReceiver.isRunning();
  }
  
  public void liveDataReceived(LiveDataValueUpdate valueUpdate) {
    _ticksReceived.incrementAndGet();
    // This could probably be much much faster, but we're designed initially for low-frequency
    // updates. Someone smarter should optimize the data structures here.
    List<CogdaClientConnection> connections = new LinkedList<CogdaClientConnection>(_clients);
    for (CogdaClientConnection connection : connections) {
      boolean needsPump = connection.liveDataReceived(valueUpdate);
      if (needsPump) {
        final CogdaClientConnection finalConnection = connection;
        _valueUpdateSendingExecutor.execute(new Runnable() {
          @Override
          public void run() {
            finalConnection.sendAllUpdates();
          }
        });
      }
    }
  }
  
  protected OGUser getOGUser(String userId) {
    if (getUserSource() == null) {
      // Nothing will work without an OG User. So we return a mock one.
      SimpleOGUser simpleUser = new SimpleOGUser(userId);
      simpleUser.getEntitlements().add("*");
      return simpleUser;
    }
    try {
      return getUserSource().getUser(userId, VersionCorrection.LATEST);
      
    } catch (RuntimeException ex) {
      s_logger.warn("Authentication could not find user {}", userId);
      return null;
    }
  }
  
  // Callbacks from the client.
  public UserPrincipal authenticate(String userId, String password) {
    if (getUserSource() == null) {
      // No user source. Allow all connections.
      return UserPrincipal.getLocalUser(userId);
    }
    
    OGUser ogUser = getOGUser(userId);
    if (ogUser == null) {
      s_logger.info("Not allowing login for {} because no user in UserSource", userId);
      return null;
    }
    
    if (isCheckPassword() && !AuthenticationUtils.passwordsMatch(ogUser, password)) {
      s_logger.info("Not allowing login for {} because passwords don't match", userId);
      return null;
    }
    return UserPrincipal.getLocalUser(userId);
  }
  
  public List<String> getAvailableServers() {
    return Collections.emptyList();
  }
  
  public FudgeMsg getCapabilities() {
    return OpenGammaFudgeContext.getInstance().newMessage();
  }
  
  public boolean isValidLiveData(ExternalId subscriptionId, String normalizationScheme) {
    return getLastKnownValueStoreProvider().isAvailable(subscriptionId, normalizationScheme);
  }
  
  public LastKnownValueStore getLastKnownValueStore(ExternalId subscriptionId, String normalizationScheme) {
    LiveDataSpecification ldspec = new LiveDataSpecification(normalizationScheme, subscriptionId);
    // TODO kirk 2012-07-23 -- Check to see if valid.
    
    LastKnownValueStore store = _lastKnownValueStores.get(ldspec);
    if (store == null) {
      LastKnownValueStore fresh = getLastKnownValueStoreProvider().newInstance(subscriptionId, normalizationScheme);
      LastKnownValueStore fromMap = _lastKnownValueStores.putIfAbsent(ldspec, fresh);
      if (fromMap == null) {
        store = fresh;
      } else {
        store = fromMap;
      }
    }
    return store;
  }
  
  public void removeClient(CogdaClientConnection connection) {
    _clients.remove(connection);
  }
  

  public int getNumClients() {
    return _clients.size();
  }
  
  public long getNumTicksReceived() {
    return _ticksReceived.get();
  }
  
  public Set<String> getActiveUsers() {
    Set<String> result = new TreeSet<String>();
    synchronized (_clients) {
      for (CogdaClientConnection connection : _clients) {
        result.add(connection.getUserPrincipal().toString());
      }
    }
    return result;
  }
}
