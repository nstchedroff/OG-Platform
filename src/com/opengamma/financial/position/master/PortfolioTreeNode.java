/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.PropertyReadWrite;
import org.joda.beans.impl.BasicMetaBean;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaProperty;

import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A node in a portfolio tree allowing an arbitrary structure within an organization
 * for the management of positions.
 * <p>
 * Positions are logically attached to nodes such as this, however they are
 * stored and returned separately from the position master.
 */
@BeanDefinition
public class PortfolioTreeNode extends DirectBean implements MutableUniqueIdentifiable {

  /**
   * The portfolio tree node unique identifier.
   */
  @PropertyDefinition
  private UniqueIdentifier _uniqueIdentifier;
  /**
   * The portfolio tree node name.
   */
  @PropertyDefinition
  private String _name = "";
  /**
   * The root node of the tree.
   */
  @PropertyDefinition(readWrite = PropertyReadWrite.READ_ONLY)
  private List<PortfolioTreeNode> _childNodes = new ArrayList<PortfolioTreeNode>();

  /**
   * Creates a node.
   */
  public PortfolioTreeNode() {
  }

  /**
   * Creates a node specifying the name.
   * @param name  the name, not null
   */
  public PortfolioTreeNode(final String name) {
    ArgumentChecker.notNull(name, "name");
    setName(name);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a child node to this node.
   * @param childNode  the child node, not null
   */
  public void addChildNode(final PortfolioTreeNode childNode) {
    ArgumentChecker.notNull(childNode, "childNode");
    getChildNodes().add(childNode);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a node from the tree below this node by identifier.
   * This performs a recursive scan of the child nodes.
   * @param nodeOid  the node object identifier, not null
   * @return the node with the identifier, null if not found
   */
  public PortfolioTreeNode getNode(final UniqueIdentifier nodeOid) {
    ArgumentChecker.notNull(nodeOid, "nodeOid");
    for (Iterator<PortfolioTreeNode> it = _childNodes.iterator(); it.hasNext(); ) {
      final PortfolioTreeNode child = it.next();
      if (child.getUniqueIdentifier().getScheme().equals(nodeOid.getScheme()) &&
          child.getUniqueIdentifier().getValue().equals(nodeOid.getValue())) {
        return child;
      }
      PortfolioTreeNode found = child.getNode(nodeOid);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  /**
   * Removes a node from the tree below this node by identifier.
   * This performs a recursive scan of the child nodes.
   * @param nodeOid  the node object identifier, not null
   * @return true if a node was removed
   */
  public boolean removeNode(final UniqueIdentifier nodeOid) {
    ArgumentChecker.notNull(nodeOid, "nodeOid");
    for (Iterator<PortfolioTreeNode> it = _childNodes.iterator(); it.hasNext(); ) {
      final PortfolioTreeNode child = it.next();
      if (child.getUniqueIdentifier().getScheme().equals(nodeOid.getScheme()) &&
          child.getUniqueIdentifier().getValue().equals(nodeOid.getValue())) {
        it.remove();
        return true;
      }
      if (child.removeNode(nodeOid)) {
        return true;
      }
    }
    return false;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts to a Fudge message.
   * @param fudgeContext  the Fudge context, not null
   * @return the message, not null
   */
  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext fudgeContext) {
    final MutableFudgeFieldContainer msg = fudgeContext.newMessage();
    if (getUniqueIdentifier() != null) {
      msg.add(meta().uniqueIdentifier().name(), getUniqueIdentifier().toFudgeMsg(fudgeContext));
    }
    if (getName() != null) {
      msg.add(meta().name().name(), getName());
    }
    for (PortfolioTreeNode node : getChildNodes()) {
      msg.add(meta().childNodes().name(), fudgeContext.objectToFudgeMsg(node));
    }
    return msg;
  }

  /**
   * Converts from a Fudge message.
   * @param fudgeContext  the Fudge context, not null
   * @param fudgeMsg  the Fudge message, not null
   * @return the node, not null
   */
  public static PortfolioTreeNode fromFudgeMsg(final FudgeDeserializationContext fudgeContext, final FudgeFieldContainer fudgeMsg) {
    final List<FudgeField> types = fudgeMsg.getAllByOrdinal(0);
    for (FudgeField field : types) {
      final String className = (String) field.getValue();
      if (PortfolioTreeNode.class.getName().equals(className)) {
        break;
      }
      try {
        return (PortfolioTreeNode) Class.forName(className).getDeclaredMethod("fromFudgeMsg", FudgeFieldContainer.class).invoke(null, fudgeMsg);
      } catch (Throwable ignored) {
        // no-action
      }
    }
    PortfolioTreeNode result = new PortfolioTreeNode();
    FudgeField fudgeUniqueIdentifier = fudgeMsg.getByName(meta().uniqueIdentifier().name());
    if (fudgeUniqueIdentifier != null) {
      result.setUniqueIdentifier(fudgeContext.fieldValueToObject(UniqueIdentifier.class, fudgeUniqueIdentifier));
    }
    FudgeField fudgeName = fudgeMsg.getByName(meta().name().name());
    if (fudgeName != null) {
      result.setName(fudgeName.getValue().toString());
    }
    List<FudgeField> fudgeChildNodes = fudgeMsg.getAllByName(meta().childNodes().name());
    for (FudgeField fudgeChildNode : fudgeChildNodes) {
      result.getChildNodes().add(fudgeContext.fieldValueToObject(PortfolioTreeNode.class, fudgeChildNode));
    }
    return result;
  }

  //------------------------- AUTOGENERATED START -------------------------
  /**
   * The meta-bean for {@code PortfolioTreeNode}.
   * @return the meta-bean, not null
   */
  public static PortfolioTreeNode.Meta meta() {
    return PortfolioTreeNode.Meta.INSTANCE;
  }

  @Override
  public PortfolioTreeNode.Meta metaBean() {
    return PortfolioTreeNode.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -125484198:  // uniqueIdentifier
        return getUniqueIdentifier();
      case 3373707:  // name
        return getName();
      case 1339293429:  // childNodes
        return getChildNodes();
    }
    return super.propertyGet(propertyName);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -125484198:  // uniqueIdentifier
        setUniqueIdentifier((UniqueIdentifier) newValue);
        return;
      case 3373707:  // name
        setName((String) newValue);
        return;
      case 1339293429:  // childNodes
        throw new UnsupportedOperationException("Property cannot be written: childNodes");
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio tree node unique identifier.
   * @return the value of the property
   */
  public UniqueIdentifier getUniqueIdentifier() {
    return _uniqueIdentifier;
  }

  /**
   * Sets the portfolio tree node unique identifier.
   * @param uniqueIdentifier  the new value of the property
   */
  public void setUniqueIdentifier(UniqueIdentifier uniqueIdentifier) {
    this._uniqueIdentifier = uniqueIdentifier;
  }

  /**
   * Gets the the {@code uniqueIdentifier} property.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> uniqueIdentifier() {
    return metaBean().uniqueIdentifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the portfolio tree node name.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the portfolio tree node name.
   * @param name  the new value of the property
   */
  public void setName(String name) {
    this._name = name;
  }

  /**
   * Gets the the {@code name} property.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the root node of the tree.
   * @return the value of the property
   */
  public List<PortfolioTreeNode> getChildNodes() {
    return _childNodes;
  }

  /**
   * Gets the the {@code childNodes} property.
   * @return the property, not null
   */
  public final Property<List<PortfolioTreeNode>> childNodes() {
    return metaBean().childNodes().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code PortfolioTreeNode}.
   */
  public static class Meta extends BasicMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueIdentifier} property.
     */
    private final MetaProperty<UniqueIdentifier> _uniqueIdentifier = DirectMetaProperty.ofReadWrite(this, "uniqueIdentifier", UniqueIdentifier.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(this, "name", String.class);
    /**
     * The meta-property for the {@code childNodes} property.
     */
    @SuppressWarnings("unchecked")
    private final MetaProperty<List<PortfolioTreeNode>> _childNodes = DirectMetaProperty.ofReadOnly(this, "childNodes", (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings("unchecked")
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap();
      temp.put("uniqueIdentifier", _uniqueIdentifier);
      temp.put("name", _name);
      temp.put("childNodes", _childNodes);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public PortfolioTreeNode createBean() {
      return new PortfolioTreeNode();
    }

    @Override
    public Class<? extends PortfolioTreeNode> beanType() {
      return PortfolioTreeNode.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueIdentifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueIdentifier> uniqueIdentifier() {
      return _uniqueIdentifier;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code childNodes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<PortfolioTreeNode>> childNodes() {
      return _childNodes;
    }

  }

  //-------------------------- AUTOGENERATED END --------------------------

}
