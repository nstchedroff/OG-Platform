/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.BasicBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.master.AbstractHistoryResult;
import com.opengamma.util.PublicSPI;

/**
 * Result providing the history of a region.
 * <p>
 * The returned documents may be a mixture of versions and corrections.
 * The document instant fields are used to identify which are which.
 * See {@link RegionHistoryRequest} for more details.
 */
@PublicSPI
@BeanDefinition
public class RegionHistoryResult extends AbstractHistoryResult<RegionDocument> {

  /**
   * Creates an instance.
   */
  public RegionHistoryResult() {
  }

  /**
   * Creates an instance.
   * 
   * @param coll  the collection of documents to add, not null
   */
  public RegionHistoryResult(Collection<RegionDocument> coll) {
    super(coll);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the returned regions from within the documents.
   * 
   * @return the regions, not null
   */
  public List<ManageableRegion> getRegions() {
    List<ManageableRegion> result = new ArrayList<ManageableRegion>();
    if (getDocuments() != null) {
      for (RegionDocument doc : getDocuments()) {
        result.add(doc.getRegion());
      }
    }
    return result;
  }

  /**
   * Gets the first region, or null if no documents.
   * 
   * @return the first region, null if none
   */
  public ManageableRegion getFirstRegion() {
    return getDocuments().size() > 0 ? getDocuments().get(0).getRegion() : null;
  }

  /**
   * Gets the single result expected from a query.
   * <p>
   * This throws an exception if more than 1 result is actually available.
   * Thus, this method implies an assumption about uniqueness of the queried region.
   * 
   * @return the matching region, not null
   * @throws IllegalStateException if no region was found
   */
  public ManageableRegion getSingleRegion() {
    if (getDocuments().size() != 1) {
      throw new OpenGammaRuntimeException("Expecting zero or single resulting match, and was " + getDocuments().size());
    } else {
      return getDocuments().get(0).getRegion();
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code RegionHistoryResult}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static RegionHistoryResult.Meta meta() {
    return RegionHistoryResult.Meta.INSTANCE;
  }

  @Override
  public RegionHistoryResult.Meta metaBean() {
    return RegionHistoryResult.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    return super.propertyGet(propertyName);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue) {
    super.propertySet(propertyName, newValue);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RegionHistoryResult}.
   */
  public static class Meta extends AbstractHistoryResult.Meta<RegionDocument> {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap());

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    public BeanBuilder<? extends RegionHistoryResult> builder() {
      return new BasicBeanBuilder<RegionHistoryResult>(new RegionHistoryResult());
    }

    @Override
    public Class<? extends RegionHistoryResult> beanType() {
      return RegionHistoryResult.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
