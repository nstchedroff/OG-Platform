/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.future;

import javax.time.calendar.LocalDate;

import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.convention.BondFutureOptionExpiryCalculator;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 *
 */
public class BondFuturePriceCurveFunction extends FuturePriceCurveFunction {

  @Override
  protected String getInstrumentType() {
    return InstrumentTypeProperties.BOND_FUTURE_PRICE;
  }

  @Override
  protected Double getTimeToMaturity(final int n, final LocalDate date, final Calendar calendar) {
    return TimeCalculator.getTimeBetween(date, BondFutureOptionExpiryCalculator.getInstance().getExpiryDate(n, date, calendar));
  }

}
