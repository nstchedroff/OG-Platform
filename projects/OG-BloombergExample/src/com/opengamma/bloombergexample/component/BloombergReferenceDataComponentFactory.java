/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.component;

import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.ehcache.CacheManager;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.bloomberglp.blpapi.SessionOptions;
import com.opengamma.bbg.BloombergReferenceDataProvider;
import com.opengamma.bbg.EHCachingReferenceDataProvider;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;

/**
 * Component factory for the bloomberg reference data provider
 */
@BeanDefinition
public class BloombergReferenceDataComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier under which to publish.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The Bloomberg server host.
   */
  @PropertyDefinition(validate = "notNull")
  private String _serverHost;
  /**
   * The Bloomberg server port.
   */
  @PropertyDefinition(validate = "notNull")
  private int _serverPort;
  /**
   * The optional cache manager.
   */
  @PropertyDefinition
  private CacheManager _cacheManager;


  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    initReferenceDataProvider(repo);
  }

  protected void initReferenceDataProvider(ComponentRepository repo) {
    final SessionOptions bbgSessionOptions = new SessionOptions();
    bbgSessionOptions.setServerHost(getServerHost());
    bbgSessionOptions.setServerPort(getServerPort());
    
    BloombergReferenceDataProvider bbgRdp = new BloombergReferenceDataProvider(bbgSessionOptions);
    repo.registerLifecycle(bbgRdp);
    
    ReferenceDataProvider rdp;
    if (getCacheManager() != null) {
      rdp = new EHCachingReferenceDataProvider(bbgRdp, getCacheManager());
    } else {
      rdp = bbgRdp;
    }
    
    final ComponentInfo info = new ComponentInfo(ReferenceDataProvider.class, getClassifier());
    repo.registerComponent(info, rdp);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code BloombergReferenceDataComponentFactory}.
   * @return the meta-bean, not null
   */
  public static BloombergReferenceDataComponentFactory.Meta meta() {
    return BloombergReferenceDataComponentFactory.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(BloombergReferenceDataComponentFactory.Meta.INSTANCE);
  }

  @Override
  public BloombergReferenceDataComponentFactory.Meta metaBean() {
    return BloombergReferenceDataComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        return getClassifier();
      case -1826275445:  // serverHost
        return getServerHost();
      case -1826037148:  // serverPort
        return getServerPort();
      case -1452875317:  // cacheManager
        return getCacheManager();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        setClassifier((String) newValue);
        return;
      case -1826275445:  // serverHost
        setServerHost((String) newValue);
        return;
      case -1826037148:  // serverPort
        setServerPort((Integer) newValue);
        return;
      case -1452875317:  // cacheManager
        setCacheManager((CacheManager) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_classifier, "classifier");
    JodaBeanUtils.notNull(_serverHost, "serverHost");
    JodaBeanUtils.notNull(_serverPort, "serverPort");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      BloombergReferenceDataComponentFactory other = (BloombergReferenceDataComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getServerHost(), other.getServerHost()) &&
          JodaBeanUtils.equal(getServerPort(), other.getServerPort()) &&
          JodaBeanUtils.equal(getCacheManager(), other.getCacheManager()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(getServerHost());
    hash += hash * 31 + JodaBeanUtils.hashCode(getServerPort());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCacheManager());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier under which to publish.
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier under which to publish.
   * @param classifier  the new value of the property, not null
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notNull(classifier, "classifier");
    this._classifier = classifier;
  }

  /**
   * Gets the the {@code classifier} property.
   * @return the property, not null
   */
  public final Property<String> classifier() {
    return metaBean().classifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Bloomberg server host.
   * @return the value of the property, not null
   */
  public String getServerHost() {
    return _serverHost;
  }

  /**
   * Sets the Bloomberg server host.
   * @param serverHost  the new value of the property, not null
   */
  public void setServerHost(String serverHost) {
    JodaBeanUtils.notNull(serverHost, "serverHost");
    this._serverHost = serverHost;
  }

  /**
   * Gets the the {@code serverHost} property.
   * @return the property, not null
   */
  public final Property<String> serverHost() {
    return metaBean().serverHost().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Bloomberg server port.
   * @return the value of the property, not null
   */
  public int getServerPort() {
    return _serverPort;
  }

  /**
   * Sets the Bloomberg server port.
   * @param serverPort  the new value of the property, not null
   */
  public void setServerPort(int serverPort) {
    JodaBeanUtils.notNull(serverPort, "serverPort");
    this._serverPort = serverPort;
  }

  /**
   * Gets the the {@code serverPort} property.
   * @return the property, not null
   */
  public final Property<Integer> serverPort() {
    return metaBean().serverPort().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the optional cache manager.
   * @return the value of the property
   */
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * Sets the optional cache manager.
   * @param cacheManager  the new value of the property
   */
  public void setCacheManager(CacheManager cacheManager) {
    this._cacheManager = cacheManager;
  }

  /**
   * Gets the the {@code cacheManager} property.
   * @return the property, not null
   */
  public final Property<CacheManager> cacheManager() {
    return metaBean().cacheManager().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code BloombergReferenceDataComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", BloombergReferenceDataComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code serverHost} property.
     */
    private final MetaProperty<String> _serverHost = DirectMetaProperty.ofReadWrite(
        this, "serverHost", BloombergReferenceDataComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code serverPort} property.
     */
    private final MetaProperty<Integer> _serverPort = DirectMetaProperty.ofReadWrite(
        this, "serverPort", BloombergReferenceDataComponentFactory.class, Integer.TYPE);
    /**
     * The meta-property for the {@code cacheManager} property.
     */
    private final MetaProperty<CacheManager> _cacheManager = DirectMetaProperty.ofReadWrite(
        this, "cacheManager", BloombergReferenceDataComponentFactory.class, CacheManager.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "serverHost",
        "serverPort",
        "cacheManager");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return _classifier;
        case -1826275445:  // serverHost
          return _serverHost;
        case -1826037148:  // serverPort
          return _serverPort;
        case -1452875317:  // cacheManager
          return _cacheManager;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends BloombergReferenceDataComponentFactory> builder() {
      return new DirectBeanBuilder<BloombergReferenceDataComponentFactory>(new BloombergReferenceDataComponentFactory());
    }

    @Override
    public Class<? extends BloombergReferenceDataComponentFactory> beanType() {
      return BloombergReferenceDataComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code classifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> classifier() {
      return _classifier;
    }

    /**
     * The meta-property for the {@code serverHost} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> serverHost() {
      return _serverHost;
    }

    /**
     * The meta-property for the {@code serverPort} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> serverPort() {
      return _serverPort;
    }

    /**
     * The meta-property for the {@code cacheManager} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CacheManager> cacheManager() {
      return _cacheManager;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}