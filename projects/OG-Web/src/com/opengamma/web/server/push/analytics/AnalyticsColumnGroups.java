/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.util.ArgumentChecker;

/**
 * Collection of {@link AnalyticsColumnGroup}s that make up the columns in a grid.
 */
public class AnalyticsColumnGroups {

  private final List<AnalyticsColumn> _columns = Lists.newArrayList();
  private final List<AnalyticsColumnGroup> _columnGroups;

  /* package */ AnalyticsColumnGroups(List<AnalyticsColumnGroup> columnGroups) {
    ArgumentChecker.notNull(columnGroups, "columnGroups");
    ArgumentChecker.notNull(columnGroups, "columnGroups");
    for (AnalyticsColumnGroup group : columnGroups) {
      _columns.addAll(group.getColumns());
    }
    _columnGroups = ImmutableList.copyOf(columnGroups);
  }

  /**
   * @return A instance containing no column groups
   */
  /* package */ static AnalyticsColumnGroups empty() {
    return new AnalyticsColumnGroups(Collections.<AnalyticsColumnGroup>emptyList());
  }

  /**
   * @return Total number of columns in all column groups
   */
  /* package */ int getColumnCount() {
    return _columns.size();
  }

  /**
   * Returns the column at an index
   * @param index The column index, zero based
   * @return The column at the specified index
   */
  public AnalyticsColumn getColumn(int index) {
    return _columns.get(index);
  }

  /**
   * @return The column groups in the order they should be displayed
   */
  public List<AnalyticsColumnGroup> getGroups() {
    return _columnGroups;
  }
}
