/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
public class DependencyGraphGridStructure extends AnalyticsGridStructure {

  // TODO maybe this should move to DependencyGraphViewport, need to consult the columns to build the results
  public static final List<AnalyticsColumnGroup> COLUMN_GROUPS = ImmutableList.of(
      new AnalyticsColumnGroup("", ImmutableList.<AnalyticsColumn>of(
          column("Target"),
          column("Type"),
          column("Value Name"),
          column("Value"),
          column("Function"),
          column("Properties"))));
  /** {@link ValueSpecification}s for all rows in the grid in row index order. */
  private final List<ValueSpecification> _gridValueSpecs;

  DependencyGraphGridStructure(AnalyticsNode root,
                               List<AnalyticsColumnGroup> columnGroups,
                               List<ValueSpecification> gridValueSpecs) {
    super(root, columnGroups);
    _gridValueSpecs = gridValueSpecs;
  }

  /* package */ ValueSpecification getTargetForRow(Integer rowIndex) {
    return _gridValueSpecs.get(rowIndex);
  }

  /* package */ List<Object> createResultsForRow(SortedSet<Integer> cols, ValueSpecification valueSpec, Object value) {
    List<Object> rowResults = Lists.newArrayListWithCapacity(cols.size());
    for (Integer colIndex : cols) {
      rowResults.add(getValueForColumn(colIndex, valueSpec, value));
    }
    return rowResults;
  }

  // TODO should the fn name and target name be built into the grid structure? mapper?
  /* package */ static Object getValueForColumn(int colIndex, ValueSpecification valueSpec, Object value) {
    switch (colIndex) {
      case 0: // target
        return "TODO need a ComputationTargetResolver";
      case 1: // target type
        return getTargetTypeName(valueSpec.getTargetSpecification().getType());
      case 2: // value name
        return valueSpec.getValueName();
      case 3: // value
        return value; // TODO formatting
      case 4: // function name
        return "TODO"; // TODO this comes from the node
      case 5: // properties
        return getValuePropertiesForDisplay(valueSpec.getProperties());
      default: // should never happen
        return null;
    }
  }

  /* package */ static String getTargetTypeName(ComputationTargetType targetType) {
    switch (targetType) {
      case PORTFOLIO_NODE:
        return "Agg";
      case POSITION:
        return "Pos";
      case SECURITY:
        return "Sec";
      case PRIMITIVE:
        return "Prim";
      case TRADE:
        return "Trade";
      default:
        return null;
    }
  }

  /* package */ static String getValuePropertiesForDisplay(ValueProperties properties) {
    StringBuilder sb = new StringBuilder();
    boolean isFirst = true;
    for (String property : properties.getProperties()) {
      if (ValuePropertyNames.FUNCTION.equals(property)) {
        continue;
      }
      if (isFirst) {
        isFirst = false;
      } else {
        sb.append("; ");
      }
      sb.append(property).append("=");
      Set<String> propertyValues = properties.getValues(property);
      if (propertyValues.isEmpty()) {
        sb.append("*");
      } else {
        boolean isFirstValue = true;
        for (String value : propertyValues) {
          if (isFirstValue) {
            isFirstValue = false;
          } else {
            sb.append(", ");
          }
          sb.append(value);
        }
      }
    }
    return sb.length() == 0 ? null : sb.toString();
  }

  private static AnalyticsColumn column(String header) {
    return new AnalyticsColumn(header, header);
  }
}