package com.opengamma.analytics.financial.credit.cds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.FollowingBusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.ActualActualISDA;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;

public class CDSApproxISDAMethodTest {

  
  /**
   * Test with the same data as the simple CDS method for comparison of the results
   */
  @Test
  public void testComparisonWithSimpleCalculation() {

    final ZonedDateTime pricingDate = ZonedDateTime.of(2010, 12, 31, 0, 0, 0, 0, TimeZone.UTC);

    double notional = 1.0;
    double recoveryRate = 0.6;
    double spread = 0.0025;
    Currency currency = Currency.GBP;
    ZonedDateTime protectionStartDate = ZonedDateTime.of(2010, 12, 20, 0, 0, 0, 0, TimeZone.UTC);
    ZonedDateTime maturity = ZonedDateTime.of(2020, 12, 20, 0, 0, 0, 0, TimeZone.UTC);
    Frequency premiumFrequency = SimpleFrequency.QUARTERLY;

    ZonedDateTime bondMaturity = ZonedDateTime.of(2016, 6, 20, 0, 0, 0, 0, TimeZone.UTC);
    PeriodFrequency bondPremiumFrequency = PeriodFrequency.ANNUAL;

    final double[] timePoints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    final double[] cdsCcyPoints = {
      0.00673222222222214000,
      0.00673222222222214000,
      0.01429905379554900000,
      0.02086135487690320000,
      0.02630494452289900000,
      0.03072389009716760000,
      0.03406975577292020000,
      0.03670379007549540000,
      0.03859767722179330000,
      0.04029139055351690000,
      0.04171488783608950000,
      0.04171488783608950000
    };
    final double[] bondCcyPoints = {
      0.00673222222222214000,
      0.00673222222222214000,
      0.01429905379554900000,
      0.02086135487690320000,
      0.02630494452289900000,
      0.03072389009716760000,
      0.03406975577292020000,
      0.03670379007549540000,
      0.03859767722179330000,
      0.04029139055351690000,
      0.04171488783608950000,
      0.04171488783608950000
    };
    final double[] spreadPoints = {
      0.008094573225337830000000000000,
      0.008094573225337830000000000000,
      0.008472028609360500000000000000,
      0.008833186263998250000000000000,
      0.009178825884456880000000000000,
      0.009509688657093270000000000000,
      0.009826479094981490000000000000,
      0.010129866801184300000000000000,
      0.010420488160288400000000000000,
      0.010698947959110100000000000000,
      0.010965820937831700000000000000,
      0.010965820937831700000000000000
    };

    final YieldCurve cdsCcyYieldCurve = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(timePoints, cdsCcyPoints, new LinearInterpolator1D()));
    final YieldCurve bondCcyYieldCurve = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(timePoints, bondCcyPoints, new LinearInterpolator1D()));
    final YieldCurve spreadCurve = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(timePoints, spreadPoints, new LinearInterpolator1D()));

    YieldCurveBundle curveBundle = new YieldCurveBundle();
    curveBundle.setCurve("CDS_CCY", cdsCcyYieldCurve);
    curveBundle.setCurve("BOND_CCY", bondCcyYieldCurve);
    curveBundle.setCurve("SPREAD", spreadCurve);

    Calendar calendar = new MondayToFridayCalendar("TestCalendar");
    DayCount dayCount = new ActualThreeSixtyFive();
    BusinessDayConvention convention = new FollowingBusinessDayConvention();
    boolean isEOM = false;

    final AnnuityCouponFixedDefinition premiumDefinition = AnnuityCouponFixedDefinition.from(currency, protectionStartDate, maturity, premiumFrequency, calendar, dayCount, convention, isEOM,
      notional, spread, false);
    final AnnuityCouponFixed premiums = premiumDefinition.toDerivative(pricingDate, "CDS_CCY");

    List<ZonedDateTime> possibleDefaultDates = scheduleDatesInRange(bondMaturity, bondPremiumFrequency.getPeriod(), pricingDate, maturity, calendar, convention);
    if (maturity.isAfter(bondMaturity)) {
      possibleDefaultDates.add(convention.adjustDate(calendar, maturity));
    }

    PaymentFixedDefinition[] defaultPayments = new PaymentFixedDefinition[possibleDefaultDates.size()];

    for (int i = 0; i < defaultPayments.length; ++i) {
      defaultPayments[i] = new PaymentFixedDefinition(currency, possibleDefaultDates.get(i), notional * (1.0 - recoveryRate));
    }

    final AnnuityPaymentFixed payouts = (new AnnuityPaymentFixedDefinition(defaultPayments)).toDerivative(pricingDate, "CDS_CCY");

    final CDSDerivative cds = new CDSDerivative(
      "CDS_CCY", "BOND_CCY", "SPREAD",
      premiums, payouts,
      TimeCalculator.getTimeBetween(pricingDate, protectionStartDate), TimeCalculator.getTimeBetween(pricingDate, maturity),
      notional, spread, recoveryRate);

    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();
    final CurrencyAmount result = method.presentValue(cds, curveBundle);


    double differenceToreferenceModel = Math.abs(result.getAmount() - 0.05973453601495030000);
    double tolerance = 0.01;
    Assert.assertTrue(differenceToreferenceModel < tolerance, "Difference between results was " + differenceToreferenceModel + " which is more than the tolerance of " + tolerance);
  }
  
  /**
   * Test with the same data as the simple CDS method for comparison of the results
   */
  @Test
  public void testPresentValue() {

    final ZonedDateTime pricingDate = ZonedDateTime.of(2008, 2, 1, 0, 0, 0, 0, TimeZone.UTC);

    double notional = 1.0e7;
    double recoveryRate = 0.4;
    double spread = 3600.0/10000.0;
    Currency currency = Currency.GBP;
    ZonedDateTime protectionStartDate = ZonedDateTime.of(2008, 2, 1, 0, 0, 0, 0, TimeZone.UTC);
    ZonedDateTime maturity = ZonedDateTime.of(2008, 2, 12, 0, 0, 0, 0, TimeZone.UTC);
    Frequency premiumFrequency = SimpleFrequency.CONTINUOUS; // not used

    ZonedDateTime bondMaturity = ZonedDateTime.of(2016, 6, 20, 0, 0, 0, 0, TimeZone.UTC);
    PeriodFrequency bondPremiumFrequency = PeriodFrequency.CONTINUOUS;

    double[] discountCurveTimePoints = {
      0.0,
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2008, 2, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2008, 3, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2008, 4, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2008, 7, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2008, 10, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2009, 1, 5, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2009, 7, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2010, 1, 4, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2010, 7, 5, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2011, 1, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2011, 7, 4, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2012, 1, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2012, 7, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2013, 1, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2013, 7, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2014, 1, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2014, 7, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2015, 1, 5, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2015, 7, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2016, 1, 4, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2016, 7, 4, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2017, 1, 3, 0, 0, 0, 0, TimeZone.UTC))
    };

    double[] discountCurveRates = {
      0.000000001013888972778431700000, /* copied first value for zero point */
      0.000000001013888972778431700000,
      0.000000001013888972778431700000,
      0.000000001013888972778431700000,
      0.000000001013888972778431700000,
      0.000000001013888972778431700000,
      0.000000000997358196030972980000,
      0.000000000995057147790134880000,
      0.000000000993861384526237670000,
      0.000000000983571668555782710000,
      0.000000000976699218749570080000,
      0.000000000989587300992411660000,
      0.000000000999308693520927250000,
      0.000000000999070826068759740000,
      0.000000000998878776248821030000,
      0.000000000998947369268421430000,
      0.000000000999005352108518340000,
      0.000000000999007765400961030000,
      0.000000000999009754006644690000,
      0.000000000998901406035201940000,
      0.000000000998803566131306970000,
      0.000000000998365168314307990000,
      0.000000000997973248135311370000
    };
    
    double[] flatSpreadCurveTimePoints = {
      0.0,
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2008, 2, 12, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2009, 2, 12, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2010, 2, 12, 0, 0, 0, 0, TimeZone.UTC))
    };
    double[] flatSpreadCurveRates = {
      0.815827074252063690000000000000,
      0.815827074252063690000000000000,
      0.815827074252063690000000000000,
      0.815827074252063690000000000000
    };
    double flatSpreadCurveRate = 0.815827074252063690000000000000;

    final YieldCurve cdsCcyYieldCurve = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(discountCurveTimePoints, discountCurveRates, new LinearInterpolator1D()));
    final YieldCurve spreadCurve = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(flatSpreadCurveTimePoints, flatSpreadCurveRates, new LinearInterpolator1D()));

    YieldCurveBundle curveBundle = new YieldCurveBundle();
    curveBundle.setCurve("CDS_CCY", cdsCcyYieldCurve);
    curveBundle.setCurve("SPREAD", spreadCurve);

    Calendar calendar = new MondayToFridayCalendar("TestCalendar");
    DayCount dayCount = new ActualThreeSixtyFive();
    BusinessDayConvention convention = new FollowingBusinessDayConvention();
    boolean isEOM = false;

//    final AnnuityCouponFixedDefinition premiumDefinition = AnnuityCouponFixedDefinition.from(currency, protectionStartDate, maturity, premiumFrequency, calendar, dayCount, convention, isEOM,
//      notional, spread, false);
//    final AnnuityCouponFixed premiums = premiumDefinition.toDerivative(pricingDate, "CDS_CCY");

    final AnnuityCouponFixed premiums = new AnnuityCouponFixed(Currency.GBP, discountCurveTimePoints /*should be coupon time points, but not ultimately used */, spread, "CDS_CCY", false);
    
//    List<ZonedDateTime> possibleDefaultDates = scheduleDatesInRange(bondMaturity, bondPremiumFrequency.getPeriod(), pricingDate, maturity, calendar, convention);
//    if (maturity.isAfter(bondMaturity)) {
//      possibleDefaultDates.add(convention.adjustDate(calendar, maturity));
//    }
//
//    PaymentFixedDefinition[] defaultPayments = new PaymentFixedDefinition[possibleDefaultDates.size()];
//
//    for (int i = 0; i < defaultPayments.length; ++i) {
//      defaultPayments[i] = new PaymentFixedDefinition(currency, possibleDefaultDates.get(i), notional * (1.0 - recoveryRate));
//    }
//
//    final AnnuityPaymentFixed payouts = (new AnnuityPaymentFixedDefinition(defaultPayments)).toDerivative(pricingDate, "CDS_CCY");

    final AnnuityPaymentFixed payouts = null;
    
    final CDSDerivative cds = new CDSDerivative(
      "CDS_CCY", "BOND_CCY", "SPREAD",
      premiums, payouts,
      TimeCalculator.getTimeBetween(pricingDate, protectionStartDate), TimeCalculator.getTimeBetween(pricingDate, maturity),
      notional, spread, recoveryRate);

    final CDSApproxISDAMethod method = new CDSApproxISDAMethod();
    final CurrencyAmount result = method.presentValue(cds, curveBundle);

    System.out.println( result );
  }
  
  @Test
  public void testRealTimePointsForCurves() {
    
    final ZonedDateTime pricingDate = ZonedDateTime.of(2008, 2, 1, 0, 0, 0, 0, TimeZone.UTC);
    
    double notional = 1.0e7;
    double recoveryRate = 0.4;
    double spread = 3600.0/10000.0;
    Currency currency = Currency.GBP;
    ZonedDateTime protectionStartDate = ZonedDateTime.of(2008, 2, 1, 0, 0, 0, 0, TimeZone.UTC);
    ZonedDateTime maturity = ZonedDateTime.of(2008, 2, 12, 0, 0, 0, 0, TimeZone.UTC);
    Frequency premiumFrequency = SimpleFrequency.CONTINUOUS; // not used
    
    double[] discountCurveTimePoints = {
      0.0,
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2008, 2, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2008, 3, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2008, 4, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2008, 7, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2008, 10, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2009, 1, 5, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2009, 7, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2010, 1, 4, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2010, 7, 5, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2011, 1, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2011, 7, 4, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2012, 1, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2012, 7, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2013, 1, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2013, 7, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2014, 1, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2014, 7, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2015, 1, 5, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2015, 7, 3, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2016, 1, 4, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2016, 7, 4, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2017, 1, 3, 0, 0, 0, 0, TimeZone.UTC))
    };

    double[] discountCurveRates = {
      0.000000001013888972778431700000, /* copied first value for zero point */
      0.000000001013888972778431700000,
      0.000000001013888972778431700000,
      0.000000001013888972778431700000,
      0.000000001013888972778431700000,
      0.000000001013888972778431700000,
      0.000000000997358196030972980000,
      0.000000000995057147790134880000,
      0.000000000993861384526237670000,
      0.000000000983571668555782710000,
      0.000000000976699218749570080000,
      0.000000000989587300992411660000,
      0.000000000999308693520927250000,
      0.000000000999070826068759740000,
      0.000000000998878776248821030000,
      0.000000000998947369268421430000,
      0.000000000999005352108518340000,
      0.000000000999007765400961030000,
      0.000000000999009754006644690000,
      0.000000000998901406035201940000,
      0.000000000998803566131306970000,
      0.000000000998365168314307990000,
      0.000000000997973248135311370000
    };
    
    double[] flatSpreadCurveTimePoints = {
      0.0,
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2008, 2, 12, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2009, 2, 12, 0, 0, 0, 0, TimeZone.UTC)),
      TimeCalculator.getTimeBetween(pricingDate, ZonedDateTime.of(2010, 2, 12, 0, 0, 0, 0, TimeZone.UTC))
    };
    double[] flatSpreadCurveRates = {
      0.815827074252063690000000000000,
      0.815827074252063690000000000000,
      0.815827074252063690000000000000,
      0.815827074252063690000000000000
    };
    
    final YieldCurve cdsCcyYieldCurve = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(discountCurveTimePoints, discountCurveRates, new LinearInterpolator1D()));
    final YieldCurve spreadCurve = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(flatSpreadCurveTimePoints, flatSpreadCurveRates, new LinearInterpolator1D()));

    final CDSDerivative cds = new CDSDerivative(
      "CDS_CCY", "BOND_CCY", "SPREAD",
      null, null,
      TimeCalculator.getTimeBetween(pricingDate, protectionStartDate), TimeCalculator.getTimeBetween(pricingDate, maturity),
      notional, spread, recoveryRate);
    
    Double [] timePoints = CDSApproxISDAMethod.realTimePointsForCurves(cds, cdsCcyYieldCurve, spreadCurve);
    
    // Test data is during a leap year
    // If TimeCalculator is parameterised to use different DayCount classes, these numbers may need to be updated/computed accordingly
    Double [] expectedTimePoints = {
      0.0 / 366.0,
      2.0 / 366.0,
      11.0 / 366.0 
    };
    
    Assert.assertEquals(timePoints, expectedTimePoints);
  }

  private static List<ZonedDateTime> scheduleDatesInRange(ZonedDateTime maturity, Period schedulePeriod, ZonedDateTime earliest, ZonedDateTime latest,
    Calendar calendar, BusinessDayConvention convention) {

    List<ZonedDateTime> datesInRange = new ArrayList<ZonedDateTime>();
    ZonedDateTime scheduleDate = maturity;
    int periods = 0;

    while (scheduleDate.isAfter(latest)) {
      scheduleDate = convention.adjustDate(calendar, maturity.minus(schedulePeriod.multipliedBy(++periods)));
    }

    while (!scheduleDate.isBefore(earliest)) {
      datesInRange.add(scheduleDate);
      scheduleDate = convention.adjustDate(calendar, maturity.minus(schedulePeriod.multipliedBy(++periods)));
    }

    Collections.reverse(datesInRange);
    return datesInRange;
  }
}