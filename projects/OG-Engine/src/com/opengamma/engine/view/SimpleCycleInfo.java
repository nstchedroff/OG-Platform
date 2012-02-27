/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import org.joda.beans.BeanDefinition;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;

import javax.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * Class encapsulationg information available on cycle initialisation
 */
@BeanDefinition
public class SimpleCycleInfo extends DirectBean implements CycleInfo {

  @PropertyDefinition
  UniqueId _marketDataSnapshotUniqueId;

  @PropertyDefinition
  UniqueId _viewDefinitionUid;

  @PropertyDefinition
  VersionCorrection _versionCorrection;

  @PropertyDefinition
  Instant _valuationTime;

  @PropertyDefinition
  Collection<String> _allCalculationConfigurationNames;

  @PropertyDefinition
  Map<String, Collection<ComputationTarget>> _computationTargetsByConfigName;

  @PropertyDefinition
  Map<String, Map<ValueSpecification, Set<ValueRequirement>>> _terminalOutputsByConfigName;


  public SimpleCycleInfo() {
  }

  public SimpleCycleInfo(UniqueId marketDataSnapshotUniqueId, UniqueId viewDefinitionUid, VersionCorrection versionCorrection, Instant valuationTime,
                         Collection<String> allCalculationConfigurationNames,
                         Map<String, Collection<ComputationTarget>> computationTargetsByConfigName,
                         Map<String, Map<ValueSpecification, Set<ValueRequirement>>> terminalOutputsByConfigName
  ) {
    _marketDataSnapshotUniqueId = marketDataSnapshotUniqueId;
    _viewDefinitionUid = viewDefinitionUid;
    _versionCorrection = versionCorrection;
    _valuationTime = valuationTime;

    _allCalculationConfigurationNames = allCalculationConfigurationNames;
    _computationTargetsByConfigName = computationTargetsByConfigName;
    _terminalOutputsByConfigName = terminalOutputsByConfigName;
  }

  @Override
  public Collection<ComputationTarget> getComputationTargetsByConfigName(String calcConfName) {
    return getComputationTargetsByConfigName().get(calcConfName);
  }

  @Override
  public Map<ValueSpecification, Set<ValueRequirement>> getTerminalOutputsByConfigName(String calcConfName) {
    return getTerminalOutputsByConfigName().get(calcConfName);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SimpleCycleInfo}.
   * @return the meta-bean, not null
   */
  public static SimpleCycleInfo.Meta meta() {
    return SimpleCycleInfo.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(SimpleCycleInfo.Meta.INSTANCE);
  }

  @Override
  public SimpleCycleInfo.Meta metaBean() {
    return SimpleCycleInfo.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -1507586858:  // marketDataSnapshotUniqueId
        return getMarketDataSnapshotUniqueId();
      case 276749144:  // viewDefinitionUid
        return getViewDefinitionUid();
      case -2031293866:  // versionCorrection
        return getVersionCorrection();
      case 113591406:  // valuationTime
        return getValuationTime();
      case 197622906:  // allCalculationConfigurationNames
        return getAllCalculationConfigurationNames();
      case 1720880511:  // computationTargetsByConfigName
        return getComputationTargetsByConfigName();
      case -1247804102:  // terminalOutputsByConfigName
        return getTerminalOutputsByConfigName();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -1507586858:  // marketDataSnapshotUniqueId
        setMarketDataSnapshotUniqueId((UniqueId) newValue);
        return;
      case 276749144:  // viewDefinitionUid
        setViewDefinitionUid((UniqueId) newValue);
        return;
      case -2031293866:  // versionCorrection
        setVersionCorrection((VersionCorrection) newValue);
        return;
      case 113591406:  // valuationTime
        setValuationTime((Instant) newValue);
        return;
      case 197622906:  // allCalculationConfigurationNames
        setAllCalculationConfigurationNames((Collection<String>) newValue);
        return;
      case 1720880511:  // computationTargetsByConfigName
        setComputationTargetsByConfigName((Map<String, Collection<ComputationTarget>>) newValue);
        return;
      case -1247804102:  // terminalOutputsByConfigName
        setTerminalOutputsByConfigName((Map<String, Map<ValueSpecification, Set<ValueRequirement>>>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SimpleCycleInfo other = (SimpleCycleInfo) obj;
      return JodaBeanUtils.equal(getMarketDataSnapshotUniqueId(), other.getMarketDataSnapshotUniqueId()) &&
          JodaBeanUtils.equal(getViewDefinitionUid(), other.getViewDefinitionUid()) &&
          JodaBeanUtils.equal(getVersionCorrection(), other.getVersionCorrection()) &&
          JodaBeanUtils.equal(getValuationTime(), other.getValuationTime()) &&
          JodaBeanUtils.equal(getAllCalculationConfigurationNames(), other.getAllCalculationConfigurationNames()) &&
          JodaBeanUtils.equal(getComputationTargetsByConfigName(), other.getComputationTargetsByConfigName()) &&
          JodaBeanUtils.equal(getTerminalOutputsByConfigName(), other.getTerminalOutputsByConfigName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarketDataSnapshotUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getViewDefinitionUid());
    hash += hash * 31 + JodaBeanUtils.hashCode(getVersionCorrection());
    hash += hash * 31 + JodaBeanUtils.hashCode(getValuationTime());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAllCalculationConfigurationNames());
    hash += hash * 31 + JodaBeanUtils.hashCode(getComputationTargetsByConfigName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTerminalOutputsByConfigName());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the marketDataSnapshotUniqueId.
   * @return the value of the property
   */
  public UniqueId getMarketDataSnapshotUniqueId() {
    return _marketDataSnapshotUniqueId;
  }

  /**
   * Sets the marketDataSnapshotUniqueId.
   * @param marketDataSnapshotUniqueId  the new value of the property
   */
  public void setMarketDataSnapshotUniqueId(UniqueId marketDataSnapshotUniqueId) {
    this._marketDataSnapshotUniqueId = marketDataSnapshotUniqueId;
  }

  /**
   * Gets the the {@code marketDataSnapshotUniqueId} property.
   * @return the property, not null
   */
  public final Property<UniqueId> marketDataSnapshotUniqueId() {
    return metaBean().marketDataSnapshotUniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the viewDefinitionUid.
   * @return the value of the property
   */
  public UniqueId getViewDefinitionUid() {
    return _viewDefinitionUid;
  }

  /**
   * Sets the viewDefinitionUid.
   * @param viewDefinitionUid  the new value of the property
   */
  public void setViewDefinitionUid(UniqueId viewDefinitionUid) {
    this._viewDefinitionUid = viewDefinitionUid;
  }

  /**
   * Gets the the {@code viewDefinitionUid} property.
   * @return the property, not null
   */
  public final Property<UniqueId> viewDefinitionUid() {
    return metaBean().viewDefinitionUid().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the versionCorrection.
   * @return the value of the property
   */
  public VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

  /**
   * Sets the versionCorrection.
   * @param versionCorrection  the new value of the property
   */
  public void setVersionCorrection(VersionCorrection versionCorrection) {
    this._versionCorrection = versionCorrection;
  }

  /**
   * Gets the the {@code versionCorrection} property.
   * @return the property, not null
   */
  public final Property<VersionCorrection> versionCorrection() {
    return metaBean().versionCorrection().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the valuationTime.
   * @return the value of the property
   */
  public Instant getValuationTime() {
    return _valuationTime;
  }

  /**
   * Sets the valuationTime.
   * @param valuationTime  the new value of the property
   */
  public void setValuationTime(Instant valuationTime) {
    this._valuationTime = valuationTime;
  }

  /**
   * Gets the the {@code valuationTime} property.
   * @return the property, not null
   */
  public final Property<Instant> valuationTime() {
    return metaBean().valuationTime().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the allCalculationConfigurationNames.
   * @return the value of the property
   */
  public Collection<String> getAllCalculationConfigurationNames() {
    return _allCalculationConfigurationNames;
  }

  /**
   * Sets the allCalculationConfigurationNames.
   * @param allCalculationConfigurationNames  the new value of the property
   */
  public void setAllCalculationConfigurationNames(Collection<String> allCalculationConfigurationNames) {
    this._allCalculationConfigurationNames = allCalculationConfigurationNames;
  }

  /**
   * Gets the the {@code allCalculationConfigurationNames} property.
   * @return the property, not null
   */
  public final Property<Collection<String>> allCalculationConfigurationNames() {
    return metaBean().allCalculationConfigurationNames().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the computationTargetsByConfigName.
   * @return the value of the property
   */
  public Map<String, Collection<ComputationTarget>> getComputationTargetsByConfigName() {
    return _computationTargetsByConfigName;
  }

  /**
   * Sets the computationTargetsByConfigName.
   * @param computationTargetsByConfigName  the new value of the property
   */
  public void setComputationTargetsByConfigName(Map<String, Collection<ComputationTarget>> computationTargetsByConfigName) {
    this._computationTargetsByConfigName = computationTargetsByConfigName;
  }

  /**
   * Gets the the {@code computationTargetsByConfigName} property.
   * @return the property, not null
   */
  public final Property<Map<String, Collection<ComputationTarget>>> computationTargetsByConfigName() {
    return metaBean().computationTargetsByConfigName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the terminalOutputsByConfigName.
   * @return the value of the property
   */
  public Map<String, Map<ValueSpecification, Set<ValueRequirement>>> getTerminalOutputsByConfigName() {
    return _terminalOutputsByConfigName;
  }

  /**
   * Sets the terminalOutputsByConfigName.
   * @param terminalOutputsByConfigName  the new value of the property
   */
  public void setTerminalOutputsByConfigName(Map<String, Map<ValueSpecification, Set<ValueRequirement>>> terminalOutputsByConfigName) {
    this._terminalOutputsByConfigName = terminalOutputsByConfigName;
  }

  /**
   * Gets the the {@code terminalOutputsByConfigName} property.
   * @return the property, not null
   */
  public final Property<Map<String, Map<ValueSpecification, Set<ValueRequirement>>>> terminalOutputsByConfigName() {
    return metaBean().terminalOutputsByConfigName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SimpleCycleInfo}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code marketDataSnapshotUniqueId} property.
     */
    private final MetaProperty<UniqueId> _marketDataSnapshotUniqueId = DirectMetaProperty.ofReadWrite(
        this, "marketDataSnapshotUniqueId", SimpleCycleInfo.class, UniqueId.class);
    /**
     * The meta-property for the {@code viewDefinitionUid} property.
     */
    private final MetaProperty<UniqueId> _viewDefinitionUid = DirectMetaProperty.ofReadWrite(
        this, "viewDefinitionUid", SimpleCycleInfo.class, UniqueId.class);
    /**
     * The meta-property for the {@code versionCorrection} property.
     */
    private final MetaProperty<VersionCorrection> _versionCorrection = DirectMetaProperty.ofReadWrite(
        this, "versionCorrection", SimpleCycleInfo.class, VersionCorrection.class);
    /**
     * The meta-property for the {@code valuationTime} property.
     */
    private final MetaProperty<Instant> _valuationTime = DirectMetaProperty.ofReadWrite(
        this, "valuationTime", SimpleCycleInfo.class, Instant.class);
    /**
     * The meta-property for the {@code allCalculationConfigurationNames} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Collection<String>> _allCalculationConfigurationNames = DirectMetaProperty.ofReadWrite(
        this, "allCalculationConfigurationNames", SimpleCycleInfo.class, (Class) Collection.class);
    /**
     * The meta-property for the {@code computationTargetsByConfigName} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, Collection<ComputationTarget>>> _computationTargetsByConfigName = DirectMetaProperty.ofReadWrite(
        this, "computationTargetsByConfigName", SimpleCycleInfo.class, (Class) Map.class);
    /**
     * The meta-property for the {@code terminalOutputsByConfigName} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, Map<ValueSpecification, Set<ValueRequirement>>>> _terminalOutputsByConfigName = DirectMetaProperty.ofReadWrite(
        this, "terminalOutputsByConfigName", SimpleCycleInfo.class, (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "marketDataSnapshotUniqueId",
        "viewDefinitionUid",
        "versionCorrection",
        "valuationTime",
        "allCalculationConfigurationNames",
        "computationTargetsByConfigName",
        "terminalOutputsByConfigName");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1507586858:  // marketDataSnapshotUniqueId
          return _marketDataSnapshotUniqueId;
        case 276749144:  // viewDefinitionUid
          return _viewDefinitionUid;
        case -2031293866:  // versionCorrection
          return _versionCorrection;
        case 113591406:  // valuationTime
          return _valuationTime;
        case 197622906:  // allCalculationConfigurationNames
          return _allCalculationConfigurationNames;
        case 1720880511:  // computationTargetsByConfigName
          return _computationTargetsByConfigName;
        case -1247804102:  // terminalOutputsByConfigName
          return _terminalOutputsByConfigName;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SimpleCycleInfo> builder() {
      return new DirectBeanBuilder<SimpleCycleInfo>(new SimpleCycleInfo());
    }

    @Override
    public Class<? extends SimpleCycleInfo> beanType() {
      return SimpleCycleInfo.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code marketDataSnapshotUniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> marketDataSnapshotUniqueId() {
      return _marketDataSnapshotUniqueId;
    }

    /**
     * The meta-property for the {@code viewDefinitionUid} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> viewDefinitionUid() {
      return _viewDefinitionUid;
    }

    /**
     * The meta-property for the {@code versionCorrection} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<VersionCorrection> versionCorrection() {
      return _versionCorrection;
    }

    /**
     * The meta-property for the {@code valuationTime} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> valuationTime() {
      return _valuationTime;
    }

    /**
     * The meta-property for the {@code allCalculationConfigurationNames} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Collection<String>> allCalculationConfigurationNames() {
      return _allCalculationConfigurationNames;
    }

    /**
     * The meta-property for the {@code computationTargetsByConfigName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<String, Collection<ComputationTarget>>> computationTargetsByConfigName() {
      return _computationTargetsByConfigName;
    }

    /**
     * The meta-property for the {@code terminalOutputsByConfigName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<String, Map<ValueSpecification, Set<ValueRequirement>>>> terminalOutputsByConfigName() {
      return _terminalOutputsByConfigName;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}