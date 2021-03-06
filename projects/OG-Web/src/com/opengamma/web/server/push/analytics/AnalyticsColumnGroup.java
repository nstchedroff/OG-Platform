/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.util.ArgumentChecker;

/**
 * Named group of columns in a grid displaying analytics data.
 */
/* package */ class AnalyticsColumnGroup {

  private final String _name;
  private final List<AnalyticsColumn> _columns;

  /**
   * @param name The name of the group
   * @param columns The columns in the group
   */
  /* package */ AnalyticsColumnGroup(String name, List<AnalyticsColumn> columns) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(columns, "cols");
    _name = name;
    _columns = ImmutableList.copyOf(columns);
  }

  /**
   * @return The name of the group
   */
  /* package */ String getName() {
    return _name;
  }

  /**
   * @return The columns in the group
   */
  /* package */ List<AnalyticsColumn> getColumns() {
    return _columns;
  }

  @Override
  public String toString() {
    return "AnalyticsColumnGroup [_name='" + _name + '\'' + ", _columns=" + _columns + "]";
  }
}

