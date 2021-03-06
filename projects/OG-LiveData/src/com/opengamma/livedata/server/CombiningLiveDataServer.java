/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.sf.ehcache.CacheManager;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.LiveDataSubscriptionRequest;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponseMsg;
import com.opengamma.livedata.server.distribution.MarketDataDistributor;
import com.opengamma.util.tuple.Pair;

/**
 * A {@link AbstractLiveDataServer} which delegates all the work to a set of {@link AbstractLiveDataServer}  
 */
public abstract class CombiningLiveDataServer extends AbstractLiveDataServer {
  
  private static final Logger s_logger = LoggerFactory.getLogger(CombiningLiveDataServer.class);

  private final ExecutorService _subscriptionExecutor = Executors.newCachedThreadPool();
  private final Set<AbstractLiveDataServer> _underlyings;

  public CombiningLiveDataServer(CacheManager cacheManager, AbstractLiveDataServer... otherUnderlyings) {
    this(Arrays.asList(otherUnderlyings), cacheManager);
  }

  public CombiningLiveDataServer(Collection<? extends AbstractLiveDataServer> otherUnderlyings, CacheManager cacheManager) {
    super(cacheManager);
    _underlyings = Sets.newHashSet();
    _underlyings.addAll(otherUnderlyings);
  }

  @Override
  public Collection<LiveDataSubscriptionResponse> subscribe(Collection<LiveDataSpecification> liveDataSpecificationsFromClient, final boolean persistent) {
    return subscribeByServer(
        liveDataSpecificationsFromClient,
        new SubscribeAction() {

          @Override
          public Collection<LiveDataSubscriptionResponse> subscribe(AbstractLiveDataServer server, Collection<LiveDataSpecification> specifications) {
            return  server.subscribe(specifications, persistent);
          }
          
          @Override
          public String getName() {
            return "Subscribe";
          }
        });
  }

  @Override
  public LiveDataSubscriptionResponseMsg subscriptionRequestMadeImpl(final LiveDataSubscriptionRequest subscriptionRequest) {
    //Need to override here as well in order to catch the resolution/entitlement checking
    
    Collection<LiveDataSubscriptionResponse> responses = subscribeByServer(
        subscriptionRequest.getSpecifications(),
        new SubscribeAction() {
          @Override
          public Collection<LiveDataSubscriptionResponse> subscribe(AbstractLiveDataServer server, Collection<LiveDataSpecification> specifications) {
            LiveDataSubscriptionRequest liveDataSubscriptionRequest = buildSubRequest(subscriptionRequest, specifications);
            //NOTE: we call up to subscriptionRequestMade to get the exception catching
            LiveDataSubscriptionResponseMsg response = server.subscriptionRequestMade(liveDataSubscriptionRequest);

            //Check that we know how to combine these responses
            if (response.getRequestingUser() != subscriptionRequest.getUser()) {
              throw new OpenGammaRuntimeException("Unexpected user in response " + response.getRequestingUser());
            }
            return response.getResponses();
          }

          @Override
          public String getName() {
            return "SubscriptionRequestMade";
          }
        });
    return new LiveDataSubscriptionResponseMsg(subscriptionRequest.getUser(), responses);
  }
  
  private LiveDataSubscriptionRequest buildSubRequest(final LiveDataSubscriptionRequest subscriptionRequest, Collection<LiveDataSpecification> specifications) {
    LiveDataSubscriptionRequest liveDataSubscriptionRequest = new LiveDataSubscriptionRequest(subscriptionRequest.getUser(), subscriptionRequest.getType(), specifications);
    return liveDataSubscriptionRequest;
  }
  
  private interface SubscribeAction {
    Collection<LiveDataSubscriptionResponse> subscribe(AbstractLiveDataServer server, Collection<LiveDataSpecification> specifications);
    String getName();
  }
  private Collection<LiveDataSubscriptionResponse> subscribeByServer(Collection<LiveDataSpecification> specifications, final SubscribeAction action)
  {
    return forEachServer(specifications, new Function<Pair<AbstractLiveDataServer, Collection<LiveDataSpecification>>, Collection<LiveDataSubscriptionResponse>>() {
      @Override
      public Collection<LiveDataSubscriptionResponse> apply(Pair<AbstractLiveDataServer, Collection<LiveDataSpecification>> input) {
        AbstractLiveDataServer specs = input.getFirst();
        Collection<LiveDataSpecification> server = input.getSecond();
        s_logger.debug("Sending subscription ({}) for {} to underlying server {}", new Object[] {action.getName(), specs, server});
        return action.subscribe(specs, server);
      }
    });
  }
  private <T> Collection<T> forEachServer(Collection<LiveDataSpecification> specifications, final Function<Pair<AbstractLiveDataServer, Collection<LiveDataSpecification>>, Collection<T>> operation)
  {
    Map<AbstractLiveDataServer, Collection<LiveDataSpecification>> mapped = groupByServer(specifications);

    Collection<Future<Collection<T>>> futures = new ArrayList<Future<Collection<T>>>(mapped.size());
    for (final Entry<AbstractLiveDataServer, Collection<LiveDataSpecification>> entry : mapped.entrySet()) {
      if (entry.getValue().isEmpty()) {
        continue;
      }
      Future<Collection<T>> future = _subscriptionExecutor.submit(new Callable<Collection<T>>() {

        @Override
        public Collection<T> call() throws Exception {
          return operation.apply(Pair.of(entry.getKey(), entry.getValue()));
        }
      });
      
      futures.add(future);
    }
    List<T> responses = new ArrayList<T>(specifications.size());
    for (Future<Collection<T>> future : futures) {
      try {
        responses.addAll(future.get());
      } catch (InterruptedException ex) {
        //Should be rare, since the subscription methods should bundle everything into the response
        s_logger.error("Unexpected exception when delegating subscription", ex);
        throw new OpenGammaRuntimeException(ex.getMessage(), ex);
      } catch (ExecutionException ex) {
        //Should be rare, since the subscription methods should bundle everything into the response
        s_logger.error("Unexpected exception when delegating subscription", ex);
        throw new OpenGammaRuntimeException(ex.getMessage(), ex);
      }
    }
    return responses;
  }

  protected abstract Map<AbstractLiveDataServer, Collection<LiveDataSpecification>> groupByServer(
      Collection<LiveDataSpecification> specs);

  private AbstractLiveDataServer getServer(LiveDataSpecification spec) {
    Map<AbstractLiveDataServer, Collection<LiveDataSpecification>> grouped = groupByServer(Sets.newHashSet(spec));
    for (Entry<AbstractLiveDataServer, Collection<LiveDataSpecification>> entry : grouped.entrySet()) {
      if (entry.getValue().size() > 0) {
        return entry.getKey();
      }
    }
    throw new OpenGammaRuntimeException("Couldn't find server for " + spec);
  }
  
  // For expiration manager
  @Override
  public void addSubscriptionListener(SubscriptionListener subscriptionListener) {
    for (AbstractLiveDataServer server : _underlyings) {
      server.addSubscriptionListener(subscriptionListener);
    }
  }

  
  @Override
  public Set<Subscription> getSubscriptions() {
    Set<Subscription> ret = new HashSet<Subscription>();
    for (AbstractLiveDataServer server : _underlyings) {
      Set<Subscription> serversSubscriptions = server.getSubscriptions();
      s_logger.debug("Server {} has {} subscriptions", server, serversSubscriptions.size());
      ret.addAll(serversSubscriptions);
    }
    return ret;
  }

  
  @Override
  public Subscription getSubscription(LiveDataSpecification fullyQualifiedSpec) {
    return getServer(fullyQualifiedSpec).getSubscription(fullyQualifiedSpec);
  }

  @Override
  public MarketDataDistributor getMarketDataDistributor(LiveDataSpecification fullyQualifiedSpec) {
    return getServer(fullyQualifiedSpec).getMarketDataDistributor(fullyQualifiedSpec);
  }
  
  
  @Override
  public Map<LiveDataSpecification, MarketDataDistributor> getMarketDataDistributors(Collection<LiveDataSpecification> fullyQualifiedSpecs) {
    Map<AbstractLiveDataServer, Collection<LiveDataSpecification>> grouped = groupByServer(fullyQualifiedSpecs);
    HashMap<LiveDataSpecification, MarketDataDistributor> ret = new HashMap<LiveDataSpecification, MarketDataDistributor>();
    for (Entry<AbstractLiveDataServer, Collection<LiveDataSpecification>> entry : grouped.entrySet()) {
      Map<LiveDataSpecification, MarketDataDistributor> entries = entry.getKey().getMarketDataDistributors(entry.getValue());
      ret.putAll(entries);
    }
    return ret;
  }

  @Override
  public boolean stopDistributor(MarketDataDistributor distributor) {
    return getServer(distributor.getFullyQualifiedLiveDataSpecification()).stopDistributor(distributor);    
  }

  @Override
  protected void doConnect() {
    for (AbstractLiveDataServer server : _underlyings) {
      server.start();
    }
  }

  @Override
  protected void doDisconnect() {
    for (AbstractLiveDataServer server : _underlyings) {
      server.stop();
    }
  }

  //----- Shouldn't happen -----
  @Override
  protected Map<String, Object> doSubscribe(Collection<String> uniqueIds) {
    throw new IllegalArgumentException();
  }

  @Override
  protected void doUnsubscribe(Collection<Object> subscriptionHandles) {
    throw new IllegalArgumentException();
  }

  @Override
  protected Map<String, FudgeMsg> doSnapshot(Collection<String> uniqueIds) {
    throw new IllegalArgumentException();
  }

  @Override
  protected ExternalScheme getUniqueIdDomain() {
    throw new IllegalArgumentException();
  }

  @Override
  protected boolean snapshotOnSubscriptionStartRequired(Subscription subscription) {
    throw new IllegalArgumentException();
  }
}
