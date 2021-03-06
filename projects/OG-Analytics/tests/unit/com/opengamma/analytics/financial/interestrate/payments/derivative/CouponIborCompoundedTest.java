/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.iborindex.IndexIborTestsMaster;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the building of compounded Ibor coupons.
 */
public class CouponIborCompoundedTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexIborTestsMaster MASTER_IBOR = IndexIborTestsMaster.getInstance();
  private static final IborIndex USDLIBOR1M = MASTER_IBOR.getIndex("USDLIBOR1M", NYC);

  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2012, 8, 24);
  private static final double NOTIONAL = 123454321;

  private static final ZonedDateTime[] ACCRUAL_END_DATES = ScheduleCalculator.getAdjustedDateSchedule(START_DATE, TENOR_3M, true, false, USDLIBOR1M);
  private static final int NB_SUB_PERIOD = ACCRUAL_END_DATES.length;
  private static final ZonedDateTime[] ACCRUAL_START_DATES = new ZonedDateTime[NB_SUB_PERIOD];
  private static final double[] PAYMENT_ACCRUAL_FACTORS = new double[NB_SUB_PERIOD];
  private static final double PAYMENT_ACCRUAL_FACTOR;

  private static final double TOLERANCE = 1.0E-10;

  static {
    ACCRUAL_START_DATES[0] = START_DATE;
    for (int loopsub = 1; loopsub < NB_SUB_PERIOD; loopsub++) {
      ACCRUAL_START_DATES[loopsub] = ACCRUAL_END_DATES[loopsub - 1];
    }
    double af = 0.0;
    for (int loopsub = 0; loopsub < NB_SUB_PERIOD; loopsub++) {
      PAYMENT_ACCRUAL_FACTORS[loopsub] = USDLIBOR1M.getDayCount().getDayCountFraction(ACCRUAL_START_DATES[loopsub], ACCRUAL_END_DATES[loopsub]);
      af += PAYMENT_ACCRUAL_FACTORS[loopsub];
    }
    PAYMENT_ACCRUAL_FACTOR = af;
  }
  private static final ZonedDateTime[] FIXING_DATES = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATES, -USDLIBOR1M.getSpotLag(), NYC);
  private static final ZonedDateTime[] FIXING_PERIOD_END_DATES = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATES, USDLIBOR1M);
  private static final double[] FIXING_ACCRUAL_FACTORS = new double[NB_SUB_PERIOD];
  static {
    for (int loopsub = 0; loopsub < NB_SUB_PERIOD; loopsub++) {
      FIXING_ACCRUAL_FACTORS[loopsub] = USDLIBOR1M.getDayCount().getDayCountFraction(ACCRUAL_START_DATES[loopsub], FIXING_PERIOD_END_DATES[loopsub]);
    }
  }
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 8, 17);
  private static final double[] FIXING_TIMES = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATES);
  private static final double[] FIXING_PERIOD_END_TIMES = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_PERIOD_END_DATES);
  private static final double[] ACCRUAL_START_TIMES = TimeCalculator.getTimeBetween(REFERENCE_DATE, ACCRUAL_START_DATES);
  private static final double[] ACCRUAL_END_TIMES = TimeCalculator.getTimeBetween(REFERENCE_DATE, ACCRUAL_END_DATES);
  private static final double PAYMENT_TIME = ACCRUAL_END_TIMES[NB_SUB_PERIOD - 1];
  private static final String DSC_NAME = "Dsc_USD";
  private static final String FWD_NAME = "Forward1M_USD";
  private static final CouponIborCompounded CPN = new CouponIborCompounded(USDLIBOR1M.getCurrency(), PAYMENT_TIME, DSC_NAME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, NOTIONAL * 1.01, USDLIBOR1M,
      PAYMENT_ACCRUAL_FACTORS, FIXING_TIMES, ACCRUAL_START_TIMES, FIXING_PERIOD_END_TIMES, FIXING_ACCRUAL_FACTORS, FWD_NAME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurrency() {
    new CouponIborCompounded(null, PAYMENT_TIME, DSC_NAME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, NOTIONAL, USDLIBOR1M,
        PAYMENT_ACCRUAL_FACTORS, FIXING_TIMES, ACCRUAL_START_TIMES, FIXING_PERIOD_END_TIMES, FIXING_ACCRUAL_FACTORS, FWD_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDsc() {
    new CouponIborCompounded(USDLIBOR1M.getCurrency(), PAYMENT_TIME, null, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, NOTIONAL, USDLIBOR1M,
        PAYMENT_ACCRUAL_FACTORS, FIXING_TIMES, ACCRUAL_START_TIMES, FIXING_PERIOD_END_TIMES, FIXING_ACCRUAL_FACTORS, FWD_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullIndex() {
    new CouponIborCompounded(USDLIBOR1M.getCurrency(), PAYMENT_TIME, DSC_NAME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, NOTIONAL, null,
        PAYMENT_ACCRUAL_FACTORS, FIXING_TIMES, ACCRUAL_START_TIMES, FIXING_PERIOD_END_TIMES, FIXING_ACCRUAL_FACTORS, FWD_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFwd() {
    new CouponIborCompounded(USDLIBOR1M.getCurrency(), PAYMENT_TIME, DSC_NAME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, NOTIONAL, USDLIBOR1M,
        PAYMENT_ACCRUAL_FACTORS, FIXING_TIMES, ACCRUAL_START_TIMES, FIXING_PERIOD_END_TIMES, FIXING_ACCRUAL_FACTORS, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void sizePayFactors() {
    new CouponIborCompounded(USDLIBOR1M.getCurrency(), PAYMENT_TIME, DSC_NAME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, NOTIONAL, USDLIBOR1M,
        new double[1], FIXING_TIMES, ACCRUAL_START_TIMES, FIXING_PERIOD_END_TIMES, FIXING_ACCRUAL_FACTORS, FWD_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void sizeFixing() {
    new CouponIborCompounded(USDLIBOR1M.getCurrency(), PAYMENT_TIME, DSC_NAME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, NOTIONAL, USDLIBOR1M,
        PAYMENT_ACCRUAL_FACTORS, new double[1], ACCRUAL_START_TIMES, FIXING_PERIOD_END_TIMES, FIXING_ACCRUAL_FACTORS, FWD_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void sizeFixStart() {
    new CouponIborCompounded(USDLIBOR1M.getCurrency(), PAYMENT_TIME, DSC_NAME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, NOTIONAL, USDLIBOR1M,
        PAYMENT_ACCRUAL_FACTORS, FIXING_TIMES, new double[1], FIXING_PERIOD_END_TIMES, FIXING_ACCRUAL_FACTORS, FWD_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void sizeFixEnd() {
    new CouponIborCompounded(USDLIBOR1M.getCurrency(), PAYMENT_TIME, DSC_NAME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, NOTIONAL, USDLIBOR1M,
        PAYMENT_ACCRUAL_FACTORS, FIXING_TIMES, ACCRUAL_START_TIMES, new double[1], FIXING_ACCRUAL_FACTORS, FWD_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void sizeFixFactors() {
    new CouponIborCompounded(USDLIBOR1M.getCurrency(), PAYMENT_TIME, DSC_NAME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, NOTIONAL, USDLIBOR1M,
        PAYMENT_ACCRUAL_FACTORS, FIXING_TIMES, ACCRUAL_START_TIMES, FIXING_PERIOD_END_TIMES, new double[1], FWD_NAME);
  }

  @Test
  public void getter() {
    assertEquals("CouponIborCompounded: getter", USDLIBOR1M.getCurrency(), CPN.getCurrency());
    assertEquals("CouponIborCompounded: getter", PAYMENT_TIME, CPN.getPaymentTime());
    assertEquals("CouponIborCompounded: getter", DSC_NAME, CPN.getFundingCurveName());
    assertEquals("CouponIborCompounded: getter", PAYMENT_ACCRUAL_FACTOR, CPN.getPaymentYearFraction());
    assertEquals("CouponIborCompounded: getter", NOTIONAL, CPN.getNotional());
    assertEquals("CouponIborCompounded: getter", NOTIONAL * 1.01, CPN.getNotionalAccrued());
    assertEquals("CouponIborCompounded: getter", USDLIBOR1M, CPN.getIndex());
    assertArrayEquals("CouponIborCompounded: getter", PAYMENT_ACCRUAL_FACTORS, CPN.getPaymentAccrualFactors(), TOLERANCE);
    assertArrayEquals("CouponIborCompounded: getter", FIXING_TIMES, CPN.getFixingTimes(), TOLERANCE);
    assertArrayEquals("CouponIborCompounded: getter", ACCRUAL_START_TIMES, CPN.getFixingPeriodStartTimes(), TOLERANCE);
    assertArrayEquals("CouponIborCompounded: getter", FIXING_PERIOD_END_TIMES, CPN.getFixingPeriodEndTimes(), TOLERANCE);
    assertArrayEquals("CouponIborCompounded: getter", FIXING_ACCRUAL_FACTORS, CPN.getFixingPeriodAccrualFactors(), TOLERANCE);
    assertEquals("CouponIborCompounded: getter", FWD_NAME, CPN.getForwardCurveName());
  }

}
