/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.BasicMetaBean;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaProperty;

import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A portfolio tree representing an arbitrary structure within an organization
 * for the management of positions.
 * <p>
 * The portfolio tree contains the portfolio and the node hierarchy.
 * The position details are not held in the tree and must be retrieved separately
 * via the position master.
 */
@PublicSPI
@BeanDefinition
public class ManageablePortfolio extends DirectBean implements MutableUniqueIdentifiable {

  /**
   * The portfolio tree unique identifier.
   * This must be null when adding to a master and not null when retrieved from a master.
   */
  @PropertyDefinition
  private UniqueIdentifier _uniqueId;
  /**
   * The portfolio tree name.
   * This field must not be null for the object to be valid.
   */
  @PropertyDefinition
  private String _name = "";
  /**
   * The root node of the tree.
   * This field must not be null for the object to be valid.
   */
  @PropertyDefinition
  private ManageablePortfolioNode _rootNode = new ManageablePortfolioNode();

  /**
   * Creates a portfolio.
   */
  public ManageablePortfolio() {
  }

  /**
   * Creates a portfolio specifying the name.
   * 
   * @param name  the name, not null
   */
  public ManageablePortfolio(final String name) {
    ArgumentChecker.notNull(name, "name");
    setName(name);
  }

  /**
   * Creates a portfolio specifying the name and root node.
   * 
   * @param name  the name, not null
   * @param rootNode  the root node, not null
   */
  public ManageablePortfolio(final String name, final ManageablePortfolioNode rootNode) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(rootNode, "rootNode");
    setName(name);
    setRootNode(rootNode);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ManageablePortfolio}.
   * @return the meta-bean, not null
   */
  public static ManageablePortfolio.Meta meta() {
    return ManageablePortfolio.Meta.INSTANCE;
  }

  @Override
  public ManageablePortfolio.Meta metaBean() {
    return ManageablePortfolio.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        return getUniqueId();
      case 3373707:  // name
        return getName();
      case -167026172:  // rootNode
        return getRootNode();
    }
    return super.propertyGet(propertyName);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        setUniqueId((UniqueIdentifier) newValue);
        return;
      case 3373707:  // name
        setName((String) newValue);
        return;
      case -167026172:  // rootNode
        setRootNode((ManageablePortfolioNode) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio tree unique identifier.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the value of the property
   */
  public UniqueIdentifier getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the portfolio tree unique identifier.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueIdentifier uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio tree name.
   * This field must not be null for the object to be valid.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the portfolio tree name.
   * This field must not be null for the object to be valid.
   * @param name  the new value of the property
   */
  public void setName(String name) {
    this._name = name;
  }

  /**
   * Gets the the {@code name} property.
   * This field must not be null for the object to be valid.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the root node of the tree.
   * This field must not be null for the object to be valid.
   * @return the value of the property
   */
  public ManageablePortfolioNode getRootNode() {
    return _rootNode;
  }

  /**
   * Sets the root node of the tree.
   * This field must not be null for the object to be valid.
   * @param rootNode  the new value of the property
   */
  public void setRootNode(ManageablePortfolioNode rootNode) {
    this._rootNode = rootNode;
  }

  /**
   * Gets the the {@code rootNode} property.
   * This field must not be null for the object to be valid.
   * @return the property, not null
   */
  public final Property<ManageablePortfolioNode> rootNode() {
    return metaBean().rootNode().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ManageablePortfolio}.
   */
  public static class Meta extends BasicMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueIdentifier> _uniqueId = DirectMetaProperty.ofReadWrite(this, "uniqueId", UniqueIdentifier.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(this, "name", String.class);
    /**
     * The meta-property for the {@code rootNode} property.
     */
    private final MetaProperty<ManageablePortfolioNode> _rootNode = DirectMetaProperty.ofReadWrite(this, "rootNode", ManageablePortfolioNode.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap();
      temp.put("uniqueId", _uniqueId);
      temp.put("name", _name);
      temp.put("rootNode", _rootNode);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public ManageablePortfolio createBean() {
      return new ManageablePortfolio();
    }

    @Override
    public Class<? extends ManageablePortfolio> beanType() {
      return ManageablePortfolio.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueIdentifier> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code rootNode} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ManageablePortfolioNode> rootNode() {
      return _rootNode;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
