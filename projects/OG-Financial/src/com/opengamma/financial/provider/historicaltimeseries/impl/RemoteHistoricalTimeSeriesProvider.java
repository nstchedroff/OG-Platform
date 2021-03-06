/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.provider.historicaltimeseries.impl;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import com.opengamma.financial.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.financial.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.financial.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;
import com.opengamma.util.time.LocalDateRange;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * Provides access to a remote {@link HistoricalTimeSeriesProvider}.
 */
public class RemoteHistoricalTimeSeriesProvider extends AbstractRemoteClient implements HistoricalTimeSeriesProvider {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteHistoricalTimeSeriesProvider(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  // delegate convenience methods to request/result method
  // code copied from AbstractHistoricalTimeSeriesProvider due to lack of multiple inheritance
  @Override
  public LocalDateDoubleTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle externalIdBundle, String dataSource, String dataProvider, String dataField, LocalDateRange dateRange) {
    
    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGet(externalIdBundle, dataSource, dataProvider, dataField, dateRange);
    HistoricalTimeSeriesProviderGetResult result = getHistoricalTimeSeries(request);
    return result.getTimeSeries().get(externalIdBundle);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle externalIdBundle, String dataSource, String dataProvider, String dataField, LocalDateRange dateRange) {
    
    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGetLatest(externalIdBundle, dataSource, dataProvider, dataField, dateRange);
    HistoricalTimeSeriesProviderGetResult result = getHistoricalTimeSeries(request);
    LocalDateDoubleTimeSeries series = result.getTimeSeries().get(externalIdBundle);
    if (series == null || series.isEmpty()) {
      return null;
    }
    return Pair.of(series.getLatestTime(), series.getLatestValue());
  }

  @Override
  public Map<ExternalIdBundle, LocalDateDoubleTimeSeries> getHistoricalTimeSeries(
      Set<ExternalIdBundle> externalIdBundleSet, String dataSource, String dataProvider, String dataField, LocalDateRange dateRange) {
    
    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGetBulk(externalIdBundleSet, dataSource, dataProvider, dataField, dateRange);
    HistoricalTimeSeriesProviderGetResult result = getHistoricalTimeSeries(request);
    return result.getTimeSeries();
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesProviderGetResult getHistoricalTimeSeries(HistoricalTimeSeriesProviderGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    URI uri = DataHistoricalTimeSeriesProviderResource.uriGet(getBaseUri());
    return accessRemote(uri).post(HistoricalTimeSeriesProviderGetResult.class, request);
  }

}
