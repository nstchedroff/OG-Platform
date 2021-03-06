/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calc.ComputationCacheQuery;
import com.opengamma.engine.view.calc.ComputationCacheResponse;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Viewport on a grid displaying the dependency graph showing how a value is calculated. This class isn't thread safe.
 */
public class DependencyGraphViewport extends AnalyticsViewport {

  /** The calculation configuration used when calculating the value and its ancestor values. */
  private final String _calcConfigName;
  /** The row and column structure of the underlying grid. */
  private final DependencyGraphGridStructure _gridStructure;
  /** {@link ValueSpecification}s for all rows visible in the viewport. */
  private List<ValueSpecification> _viewportValueSpecs = Collections.emptyList();

  /**
   * @param viewportSpec Definition of the viewport
   * @param calcConfigName Calculation configuration used to calculate the dependency graph
   * @param gridStructure Row and column structure of the grid
   * @param cycle Calculation cycle that produced the most recent results
   * @param cache Cache of calculation results used to populate the viewport's data
   * @param callbackId ID that's passed to listeners when the viewport's data changes
   */
  /* package */ DependencyGraphViewport(ViewportSpecification viewportSpec,
                                        String calcConfigName,
                                        DependencyGraphGridStructure gridStructure,
                                        ViewCycle cycle,
                                        ResultsCache cache,
                                        String callbackId) {
    super(callbackId);
    _calcConfigName = calcConfigName;
    _gridStructure = gridStructure;
    update(viewportSpec, cycle, cache);
  }

  /**
   * Updates the viewport, e.g. in response to the user scrolling the grid.
   * @param viewportSpec The definition of the updated viewport
   * @param cycle The cycle used to calculate the latest set of results
   * @param cache Cache of results for the grid
   * @return Version number of the viewport, allows the client to confirm a set of results corresponds to the
   * current state of the viewport
   */
  /* package */ long update(ViewportSpecification viewportSpec, ViewCycle cycle, ResultsCache cache) {
    ArgumentChecker.notNull(viewportSpec, "viewportSpec");
    ArgumentChecker.notNull(cycle, "cycle");
    ArgumentChecker.notNull(cache, "cache");
    if (!viewportSpec.isValidFor(_gridStructure)) {
      throw new IllegalArgumentException("Viewport contains cells outside the bounds of the grid. Viewport: " +
                                             viewportSpec + ", grid: " + _gridStructure);
    }
    _viewportSpec = viewportSpec;
    _viewportValueSpecs = _gridStructure.getValueSpecificationsForRows(_viewportSpec.getRows());
    ++_version;
    updateResults(cycle, cache);
    return _version;
  }

  /**
   * Updates the data in the viewport when a new set of results arrives from the calculation engine.
   * @param cycle Calculation cycle that calculated the results
   * @param cache Cache of results
   * @return ID of the viewport's data which is passed to listeners to notify them the data has changed
   */
  /* package */ String updateResults(ViewCycle cycle, ResultsCache cache) {
    ComputationCacheQuery query = new ComputationCacheQuery();
    Map<ValueSpecification, Object> resultsMap = Maps.newHashMap();
    query.setCalculationConfigurationName(_calcConfigName);
    query.setValueSpecifications(_viewportValueSpecs);
    ComputationCacheResponse cacheResponse = cycle.queryComputationCaches(query);
    List<Pair<ValueSpecification, Object>> results = cacheResponse.getResults();
    for (Pair<ValueSpecification, Object> result : results) {
      ValueSpecification valueSpec = result.getFirst();
      Object value = result.getSecond();
      resultsMap.put(valueSpec, value);
    }
    List<List<ViewportResults.Cell>> gridResults =
    _gridStructure.createResultsForViewport(_viewportSpec, resultsMap, cache, _calcConfigName);
    _latestResults = new ViewportResults(gridResults, _viewportSpec, _gridStructure.getColumnStructure(), _version);
    // TODO return null if nothing was updated
    return _callbackId;
  }
}
