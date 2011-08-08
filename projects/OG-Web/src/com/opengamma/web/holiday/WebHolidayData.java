/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.holiday;

import java.util.Map;

import javax.ws.rs.core.UriInfo;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.id.UniqueId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;

/**
 * Data class for web-based holidays.
 */
@BeanDefinition
public class WebHolidayData extends DirectBean {

  /**
   * The holiday master.
   */
  @PropertyDefinition
  private HolidayMaster _holidayMaster;
  /**
   * The JSR-311 URI information.
   */
  @PropertyDefinition
  private UriInfo _uriInfo;
  /**
   * The holiday id from the input URI.
   */
  @PropertyDefinition
  private String _uriHolidayId;
  /**
   * The version id from the URI.
   */
  @PropertyDefinition
  private String _uriVersionId;
  /**
   * The holiday.
   */
  @PropertyDefinition
  private HolidayDocument _holiday;
  /**
   * The versioned holiday.
   */
  @PropertyDefinition
  private HolidayDocument _versioned;

  /**
   * Creates an instance.
   */
  public WebHolidayData() {
  }

  /**
   * Creates an instance.
   * @param uriInfo  the URI information
   */
  public WebHolidayData(final UriInfo uriInfo) {
    setUriInfo(uriInfo);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the best available holiday id.
   * @param overrideId  the override id, null derives the result from the data
   * @return the id, may be null
   */
  public String getBestHolidayUriId(final UniqueId overrideId) {
    if (overrideId != null) {
      return overrideId.toLatest().toString();
    }
    return getHoliday() != null ? getHoliday().getUniqueId().toLatest().toString() : getUriHolidayId();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code WebHolidayData}.
   * @return the meta-bean, not null
   */
  public static WebHolidayData.Meta meta() {
    return WebHolidayData.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(WebHolidayData.Meta.INSTANCE);
  }

  @Override
  public WebHolidayData.Meta metaBean() {
    return WebHolidayData.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 246258906:  // holidayMaster
        return getHolidayMaster();
      case -173275078:  // uriInfo
        return getUriInfo();
      case -872009849:  // uriHolidayId
        return getUriHolidayId();
      case 666567687:  // uriVersionId
        return getUriVersionId();
      case 1091905624:  // holiday
        return getHoliday();
      case -1407102089:  // versioned
        return getVersioned();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 246258906:  // holidayMaster
        setHolidayMaster((HolidayMaster) newValue);
        return;
      case -173275078:  // uriInfo
        setUriInfo((UriInfo) newValue);
        return;
      case -872009849:  // uriHolidayId
        setUriHolidayId((String) newValue);
        return;
      case 666567687:  // uriVersionId
        setUriVersionId((String) newValue);
        return;
      case 1091905624:  // holiday
        setHoliday((HolidayDocument) newValue);
        return;
      case -1407102089:  // versioned
        setVersioned((HolidayDocument) newValue);
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
      WebHolidayData other = (WebHolidayData) obj;
      return JodaBeanUtils.equal(getHolidayMaster(), other.getHolidayMaster()) &&
          JodaBeanUtils.equal(getUriInfo(), other.getUriInfo()) &&
          JodaBeanUtils.equal(getUriHolidayId(), other.getUriHolidayId()) &&
          JodaBeanUtils.equal(getUriVersionId(), other.getUriVersionId()) &&
          JodaBeanUtils.equal(getHoliday(), other.getHoliday()) &&
          JodaBeanUtils.equal(getVersioned(), other.getVersioned());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getHolidayMaster());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUriInfo());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUriHolidayId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getUriVersionId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHoliday());
    hash += hash * 31 + JodaBeanUtils.hashCode(getVersioned());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the holiday master.
   * @return the value of the property
   */
  public HolidayMaster getHolidayMaster() {
    return _holidayMaster;
  }

  /**
   * Sets the holiday master.
   * @param holidayMaster  the new value of the property
   */
  public void setHolidayMaster(HolidayMaster holidayMaster) {
    this._holidayMaster = holidayMaster;
  }

  /**
   * Gets the the {@code holidayMaster} property.
   * @return the property, not null
   */
  public final Property<HolidayMaster> holidayMaster() {
    return metaBean().holidayMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the JSR-311 URI information.
   * @return the value of the property
   */
  public UriInfo getUriInfo() {
    return _uriInfo;
  }

  /**
   * Sets the JSR-311 URI information.
   * @param uriInfo  the new value of the property
   */
  public void setUriInfo(UriInfo uriInfo) {
    this._uriInfo = uriInfo;
  }

  /**
   * Gets the the {@code uriInfo} property.
   * @return the property, not null
   */
  public final Property<UriInfo> uriInfo() {
    return metaBean().uriInfo().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the holiday id from the input URI.
   * @return the value of the property
   */
  public String getUriHolidayId() {
    return _uriHolidayId;
  }

  /**
   * Sets the holiday id from the input URI.
   * @param uriHolidayId  the new value of the property
   */
  public void setUriHolidayId(String uriHolidayId) {
    this._uriHolidayId = uriHolidayId;
  }

  /**
   * Gets the the {@code uriHolidayId} property.
   * @return the property, not null
   */
  public final Property<String> uriHolidayId() {
    return metaBean().uriHolidayId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the version id from the URI.
   * @return the value of the property
   */
  public String getUriVersionId() {
    return _uriVersionId;
  }

  /**
   * Sets the version id from the URI.
   * @param uriVersionId  the new value of the property
   */
  public void setUriVersionId(String uriVersionId) {
    this._uriVersionId = uriVersionId;
  }

  /**
   * Gets the the {@code uriVersionId} property.
   * @return the property, not null
   */
  public final Property<String> uriVersionId() {
    return metaBean().uriVersionId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the holiday.
   * @return the value of the property
   */
  public HolidayDocument getHoliday() {
    return _holiday;
  }

  /**
   * Sets the holiday.
   * @param holiday  the new value of the property
   */
  public void setHoliday(HolidayDocument holiday) {
    this._holiday = holiday;
  }

  /**
   * Gets the the {@code holiday} property.
   * @return the property, not null
   */
  public final Property<HolidayDocument> holiday() {
    return metaBean().holiday().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the versioned holiday.
   * @return the value of the property
   */
  public HolidayDocument getVersioned() {
    return _versioned;
  }

  /**
   * Sets the versioned holiday.
   * @param versioned  the new value of the property
   */
  public void setVersioned(HolidayDocument versioned) {
    this._versioned = versioned;
  }

  /**
   * Gets the the {@code versioned} property.
   * @return the property, not null
   */
  public final Property<HolidayDocument> versioned() {
    return metaBean().versioned().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code WebHolidayData}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code holidayMaster} property.
     */
    private final MetaProperty<HolidayMaster> _holidayMaster = DirectMetaProperty.ofReadWrite(
        this, "holidayMaster", WebHolidayData.class, HolidayMaster.class);
    /**
     * The meta-property for the {@code uriInfo} property.
     */
    private final MetaProperty<UriInfo> _uriInfo = DirectMetaProperty.ofReadWrite(
        this, "uriInfo", WebHolidayData.class, UriInfo.class);
    /**
     * The meta-property for the {@code uriHolidayId} property.
     */
    private final MetaProperty<String> _uriHolidayId = DirectMetaProperty.ofReadWrite(
        this, "uriHolidayId", WebHolidayData.class, String.class);
    /**
     * The meta-property for the {@code uriVersionId} property.
     */
    private final MetaProperty<String> _uriVersionId = DirectMetaProperty.ofReadWrite(
        this, "uriVersionId", WebHolidayData.class, String.class);
    /**
     * The meta-property for the {@code holiday} property.
     */
    private final MetaProperty<HolidayDocument> _holiday = DirectMetaProperty.ofReadWrite(
        this, "holiday", WebHolidayData.class, HolidayDocument.class);
    /**
     * The meta-property for the {@code versioned} property.
     */
    private final MetaProperty<HolidayDocument> _versioned = DirectMetaProperty.ofReadWrite(
        this, "versioned", WebHolidayData.class, HolidayDocument.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "holidayMaster",
        "uriInfo",
        "uriHolidayId",
        "uriVersionId",
        "holiday",
        "versioned");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 246258906:  // holidayMaster
          return _holidayMaster;
        case -173275078:  // uriInfo
          return _uriInfo;
        case -872009849:  // uriHolidayId
          return _uriHolidayId;
        case 666567687:  // uriVersionId
          return _uriVersionId;
        case 1091905624:  // holiday
          return _holiday;
        case -1407102089:  // versioned
          return _versioned;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends WebHolidayData> builder() {
      return new DirectBeanBuilder<WebHolidayData>(new WebHolidayData());
    }

    @Override
    public Class<? extends WebHolidayData> beanType() {
      return WebHolidayData.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code holidayMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HolidayMaster> holidayMaster() {
      return _holidayMaster;
    }

    /**
     * The meta-property for the {@code uriInfo} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UriInfo> uriInfo() {
      return _uriInfo;
    }

    /**
     * The meta-property for the {@code uriHolidayId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> uriHolidayId() {
      return _uriHolidayId;
    }

    /**
     * The meta-property for the {@code uriVersionId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> uriVersionId() {
      return _uriVersionId;
    }

    /**
     * The meta-property for the {@code holiday} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HolidayDocument> holiday() {
      return _holiday;
    }

    /**
     * The meta-property for the {@code versioned} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HolidayDocument> versioned() {
      return _versioned;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
