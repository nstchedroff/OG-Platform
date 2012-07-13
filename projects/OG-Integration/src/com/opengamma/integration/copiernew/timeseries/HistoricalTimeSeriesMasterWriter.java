package com.opengamma.integration.copiernew.timeseries;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.copiernew.Writeable;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.beancompare.BeanCompare;
import com.opengamma.util.beancompare.BeanDifference;
import com.opengamma.util.tuple.ObjectsPair;

import javax.time.calendar.ZonedDateTime;
import java.io.IOException;
import java.util.List;

public class HistoricalTimeSeriesMasterWriter
    implements Writeable<ObjectsPair<ManageableHistoricalTimeSeriesInfo, ManageableHistoricalTimeSeries>> {

  HistoricalTimeSeriesMaster _historicalTimeSeriesMaster;
  private BeanCompare _beanCompare;

  public HistoricalTimeSeriesMasterWriter(HistoricalTimeSeriesMaster historicalTimeSeriesMaster) {
    ArgumentChecker.notNull(historicalTimeSeriesMaster, "historicalTimeSeriesMaster");
    _historicalTimeSeriesMaster = historicalTimeSeriesMaster;
    _beanCompare = new BeanCompare();
  }

  @Override
  public ObjectsPair<ManageableHistoricalTimeSeriesInfo, ManageableHistoricalTimeSeries> addOrUpdate(ObjectsPair<ManageableHistoricalTimeSeriesInfo, ManageableHistoricalTimeSeries> pair) {
    ArgumentChecker.notNull(pair, "historicalTimeSeriesInfo, historicalTimeSeries");

    ManageableHistoricalTimeSeriesInfo info = pair.getFirst();
    ManageableHistoricalTimeSeries timeSeries = pair.getSecond();

    return null; // TODO

//    HistoricalTimeSeriesInfoSearchRequest searchReq = new HistoricalTimeSeriesInfoSearchRequest();
//    ExternalIdSearch idSearch = new ExternalIdSearch(info.getExternalIdBundle().toBundle());  // match any one of the IDs
//    searchReq.setVersionCorrection(VersionCorrection.ofVersionAsOf(ZonedDateTime.now())); // valid now
//    searchReq.setExternalIdSearch(idSearch);
//    HistoricalTimeSeriesInfoSearchResult searchResult = _historicalTimeSeriesMaster.search(searchReq);
//    ManageableHistoricalTimeSeriesInfo foundHistoricalTimeSeriesInfo = searchResult.getFirstInfo();
//    if (foundHistoricalTimeSeriesInfo != null) {
//      List<BeanDifference<?>> differences;
//      try {
//        differences = _beanCompare.compare(foundHistoricalTimeSeriesInfo, info);
//      } catch (Exception e) {
//        throw new OpenGammaRuntimeException("Error comparing historicalTimeSeriesInfos with ID bundle " + info.getExternalIdBundle(), e);
//      }
//      if (differences.size() == 1 && differences.get(0).getProperty().propertyType() == UniqueId.class) {
//        // It's already there, don't update or add it
//        return foundHistoricalTimeSeriesInfo;
//      } else {
//        HistoricalTimeSeriesInfoDocument updateDoc = new HistoricalTimeSeriesInfoDocument(info);
//        updateDoc.setUniqueId(foundHistoricalTimeSeriesInfo.getUniqueId());
//        HistoricalTimeSeriesInfoDocument result = _historicalTimeSeriesMaster.update(updateDoc);
//        return result.getInfo();
//      }
//    } else {
//      // Not found, so add it
//      HistoricalTimeSeriesInfoDocument addDoc = new HistoricalTimeSeriesInfoDocument(info);
//      HistoricalTimeSeriesInfoDocument resultInfo = _historicalTimeSeriesMaster.add(addDoc);
//      UniqueId resultUniqueId = _historicalTimeSeriesMaster.updateTimeSeriesDataPoints(resultInfo.getObjectId(),
//          pair.getSecond().getTimeSeries());
//      ManageableHistoricalTimeSeries resultTimeSeries = _historicalTimeSeriesMaster.getTimeSeries(resultUniqueId);
//      return new ObjectsPair<ManageableHistoricalTimeSeriesInfo, ManageableHistoricalTimeSeries>(resultInfo.getInfo(),
//          resultTimeSeries);
//    }
  }

  @Override
  public void flush() throws IOException {
    // No action
  }
}
