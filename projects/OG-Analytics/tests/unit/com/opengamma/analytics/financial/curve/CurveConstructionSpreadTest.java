/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.calculator.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.calculator.PresentValueConvertedCalculator;
import com.opengamma.analytics.financial.calculator.PresentValueCurveSensitivityConvertedCalculator;
import com.opengamma.analytics.financial.calculator.PresentValueCurveSensitivityMCSCalculator;
import com.opengamma.analytics.financial.calculator.PresentValueMCACalculator;
import com.opengamma.analytics.financial.curve.building.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.curve.building.CurveBuildingFunction;
import com.opengamma.analytics.financial.curve.generator.GeneratorCurve;
import com.opengamma.analytics.financial.curve.generator.GeneratorCurveAddYield;
import com.opengamma.analytics.financial.curve.generator.GeneratorCurveAddYieldExisiting;
import com.opengamma.analytics.financial.curve.generator.GeneratorCurveDiscountFactorInterpolated;
import com.opengamma.analytics.financial.curve.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.generator.GeneratorCurveYieldInterpolatedAnchor;
import com.opengamma.analytics.financial.curve.generator.GeneratorCurveYieldNelsonSiegel;
import com.opengamma.analytics.financial.curve.generator.GeneratorCurveYieldPeriodicInterpolated;
import com.opengamma.analytics.financial.curve.sensitivity.ParameterSensitivity;
import com.opengamma.analytics.financial.curve.sensitivity.ParameterUnderlyingSensitivityBlockCalculator;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.generator.GeneratorSwapTestsMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.LastTimeCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroSpreadCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.DoublesCurveNelsonSiegel;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Build of curve in several blocks with relevant Jacobian matrices.
 * Curves are using different types of interpolations (on cc rate, periodic rates and discount factors) and spread curves.
 */
public class CurveConstructionSpreadTest {

  //TODO: add test on futures.
  private static final Interpolator1D INTERPOLATOR_DQ = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  //  private static final Interpolator1D INTERPOLATOR_CS = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
  //      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D INTERPOLATOR_LL = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LOG_LINEAR, Interpolator1DFactory.EXPONENTIAL_EXTRAPOLATOR,
      Interpolator1DFactory.EXPONENTIAL_EXTRAPOLATOR);
  //  private static final MatrixAlgebra MATRIX_ALGEBRA = new CommonsMatrixAlgebra();

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;
  private static final CurveBuildingFunction CURVE_BUILDING_FUNCTION = new CurveBuildingFunction(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);
  private static final Currency CCY_USD = Currency.USD;
  private static final FXMatrix FX_MATRIX = new FXMatrix(CCY_USD);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("CAL");
  private static final int SPOT_LAG = 2;
  private static final DayCount DAY_COUNT_CASH = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final double NOTIONAL = 1.0;
  private static final BusinessDayConvention BDC = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final IndexON INDEX_ON_1 = new IndexON("Fed Fund", CCY_USD, DAY_COUNT_CASH, 1, CALENDAR);
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", CCY_USD, CALENDAR, DAY_COUNT_CASH);
  private static final GeneratorFixedON GENERATOR_OIS_USD = new GeneratorFixedON("USD1YFEDFUND", INDEX_ON_1, Period.ofMonths(12), DAY_COUNT_CASH, BDC, true, SPOT_LAG, SPOT_LAG);
  private static final GeneratorDeposit GENERATOR_DEPOSIT_USD = new GeneratorDeposit("USD Deposit", CCY_USD, CALENDAR, SPOT_LAG, DAY_COUNT_CASH, BDC, true);
  private static final GeneratorSwapTestsMaster GENERATOR_SWAP_MASTER = GeneratorSwapTestsMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", CALENDAR);
  private static final GeneratorSwapFixedIbor USD6MLIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR6M", CALENDAR);

  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 28);

  private static final ArrayZonedDateTimeDoubleTimeSeries TS_EMPTY = new ArrayZonedDateTimeDoubleTimeSeries();
  private static final ArrayZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
      DateUtils.getUTCDate(2011, 9, 28)}, new double[] {0.07, 0.08});
  private static final ArrayZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
      DateUtils.getUTCDate(2011, 9, 28)}, new double[] {0.07, 0.08});
  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_OIS_USD_WITH_TODAY = new DoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITH_TODAY};
  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_OIS_USD_WITHOUT_TODAY = new DoubleTimeSeries[] {TS_EMPTY, TS_ON_USD_WITHOUT_TODAY};

  private static final ArrayZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITH_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
      DateUtils.getUTCDate(2011, 9, 28)}, new double[] {0.0035, 0.0036});
  private static final ArrayZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITHOUT_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27)},
      new double[] {0.0035});

  private static final ArrayZonedDateTimeDoubleTimeSeries TS_IBOR_USD6M_WITH_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
      DateUtils.getUTCDate(2011, 9, 28)}, new double[] {0.0045, 0.0046});
  private static final ArrayZonedDateTimeDoubleTimeSeries TS_IBOR_USD6M_WITHOUT_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27)},
      new double[] {0.0045});

  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITH_TODAY = new DoubleTimeSeries[] {TS_IBOR_USD3M_WITH_TODAY};
  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITHOUT_TODAY = new DoubleTimeSeries[] {TS_IBOR_USD3M_WITHOUT_TODAY};
  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_IBOR_USD6M_WITH_TODAY = new DoubleTimeSeries[] {TS_IBOR_USD6M_WITH_TODAY};
  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_IBOR_USD6M_WITHOUT_TODAY = new DoubleTimeSeries[] {TS_IBOR_USD6M_WITHOUT_TODAY};

  private static final String CURVE_NAME_DSC_USD = "USD Dsc";
  private static final String CURVE_NAME_FWD3_USD = "USD Fwd 3M";
  private static final String CURVE_NAME_FWD6_USD = "USD Fwd 6M";
  private static final HashMap<String, Currency> CCY_MAP = new HashMap<String, Currency>();
  static {
    CCY_MAP.put(CURVE_NAME_DSC_USD, CCY_USD);
    CCY_MAP.put(CURVE_NAME_FWD3_USD, CCY_USD);
    CCY_MAP.put(CURVE_NAME_FWD6_USD, CCY_USD);
  }

  /** Market values for the dsc USD curve */
  public static final double[] DSC_USD_MARKET_QUOTES = new double[] {0.0010, 0.0010, 0.0015, 0.0008, 0.0010, 0.0010, 0.0010, 0.0010, 0.0020, 0.0030, 0.0040, 0.0050, 0.0130};
  /** Generators for the dsc USD curve */
  public static final GeneratorInstrument[] DSC_USD_GENERATORS = new GeneratorInstrument[] {GENERATOR_DEPOSIT_ON_USD, GENERATOR_DEPOSIT_ON_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
      GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD};
  /** Tenors for the dsc USD curve */
  public static final Period[] DSC_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
      Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10)};

  /** Market values for the Fwd 3M USD curve */
  public static final double[] FWD3_USD_MARKET_QUOTES = new double[] {0.0045, 0.0045, 0.0045, 0.0045, 0.0060, 0.0070, 0.0080, 0.0160};
  /** Generators for the Fwd 3M USD curve */
  public static final GeneratorInstrument[] FWD3_USD_GENERATORS = new GeneratorInstrument[] {USD6MLIBOR3M, GENERATOR_DEPOSIT_USD, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M,
      USD6MLIBOR3M};
  /** Tenors for the Fwd 3M USD curve */
  public static final Period[] FWD3_USD_TENOR = new Period[] {Period.ofYears(1), Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
      Period.ofYears(10)};

  /** Market values for the Fwd 3M USD curve */
  public static final double[] FWD3_USD_MARKET_QUOTES_2 = new double[] {0.0045, 0.0045, 0.0045, 0.0050, 0.0060, 0.0080, 0.0075, 0.0090, 0.0160};
  /** Generators for the Fwd 3M USD curve */
  public static final GeneratorInstrument[] FWD3_USD_GENERATORS_2 = new GeneratorInstrument[] {GENERATOR_DEPOSIT_USD, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M,
      USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M};
  /** Tenors for the Fwd 3M USD curve */
  public static final Period[] FWD3_USD_TENOR_2 = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
      Period.ofYears(7), Period.ofYears(10)};

  /** Market values for the Fwd 3M USD curve */
  public static final double[] FWD3_USD_MARKET_QUOTES_3 = new double[] {0.0045, 0.0045, 0.0045, 0.0050, 0.0060, 0.0080, 0.0075, 0.0090, 0.0160, 0.0200, 0.0180};
  /** Generators for the Fwd 3M USD curve */
  public static final GeneratorInstrument[] FWD3_USD_GENERATORS_3 = new GeneratorInstrument[] {GENERATOR_DEPOSIT_USD, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M,
      USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M};
  /** Tenors for the Fwd 3M USD curve */
  public static final Period[] FWD3_USD_TENOR_3 = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
      Period.ofYears(7), Period.ofYears(10), Period.ofYears(15), Period.ofYears(20)};

  /** Market values for the Fwd 3M USD curve */
  public static final double[] FWD3_USD_MARKET_QUOTES_4 = new double[] {0.0100, 0.0125, 0.0150, 0.0140, 0.0113, 0.0131, 0.0136, 0.0142, 0.0146, 0.0135};
  /** Generators for the Fwd 3M USD curve */
  public static final GeneratorInstrument[] FWD3_USD_GENERATORS_4 = new GeneratorInstrument[] {GENERATOR_DEPOSIT_USD, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M,
      USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M};
  /** Tenors for the Fwd 3M USD curve */
  public static final Period[] FWD3_USD_TENOR_4 = new Period[] {Period.ofMonths(3), Period.ofYears(1), Period.ofYears(5), Period.ofYears(10), Period.ofMonths(6), Period.ofYears(2), Period.ofYears(3),
      Period.ofYears(4), Period.ofYears(7), Period.ofYears(15)};

  /** Market values for the Fwd 6M USD curve */
  public static final double[] FWD6_USD_MARKET_QUOTES = new double[] {0.0065, 0.0055, 0.0080, 0.0170};
  /** Generators for the Fwd 6M USD curve */
  public static final GeneratorInstrument[] FWD6_USD_GENERATORS = new GeneratorInstrument[] {GENERATOR_DEPOSIT_USD, USD6MLIBOR6M, USD6MLIBOR6M, USD6MLIBOR6M};
  /** Tenors for the Fwd 6M USD curve */
  public static final Period[] FWD6_USD_TENOR = new Period[] {Period.ofMonths(6), Period.ofYears(2), Period.ofYears(5), Period.ofYears(10)};

  /** Standard USD discounting curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_DSC_USD;
  /** Standard USD Forward 3M curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_USD;
  /** Standard USD Forward 3M curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_USD_2;
  /** Standard USD Forward 3M curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_USD_3;
  /** Standard USD Forward 3M curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_USD_4;
  /** Standard USD Forward 6M curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_FWD6_USD;
  /** Units of curves */
  public static final int[] NB_UNITS = new int[] {2, 2, 2, 3, 3, 1, 1, 2};
  public static final int NB_BLOCKS = NB_UNITS.length;
  public static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  public static final GeneratorCurve[][][] GENERATORS_UNITS = new GeneratorCurve[NB_BLOCKS][][];
  public static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  public static final YieldCurveBundle KNOWN_DATA = new YieldCurveBundle(FX_MATRIX, CCY_MAP);

  static {
    DEFINITIONS_DSC_USD = getDefinitions(DSC_USD_MARKET_QUOTES, DSC_USD_GENERATORS, DSC_USD_TENOR, new Double[DSC_USD_MARKET_QUOTES.length]);
    DEFINITIONS_FWD3_USD = getDefinitions(FWD3_USD_MARKET_QUOTES, FWD3_USD_GENERATORS, FWD3_USD_TENOR, new Double[FWD3_USD_MARKET_QUOTES.length]);
    DEFINITIONS_FWD3_USD_2 = getDefinitions(FWD3_USD_MARKET_QUOTES_2, FWD3_USD_GENERATORS_2, FWD3_USD_TENOR_2, new Double[FWD3_USD_MARKET_QUOTES_2.length]);
    DEFINITIONS_FWD3_USD_3 = getDefinitions(FWD3_USD_MARKET_QUOTES_3, FWD3_USD_GENERATORS_3, FWD3_USD_TENOR_3, new Double[FWD3_USD_MARKET_QUOTES_3.length]);
    DEFINITIONS_FWD3_USD_4 = getDefinitions(FWD3_USD_MARKET_QUOTES_4, FWD3_USD_GENERATORS_4, FWD3_USD_TENOR_4, new Double[FWD3_USD_MARKET_QUOTES_4.length]);
    DEFINITIONS_FWD6_USD = getDefinitions(FWD6_USD_MARKET_QUOTES, FWD6_USD_GENERATORS, FWD6_USD_TENOR, new Double[FWD6_USD_MARKET_QUOTES.length]);
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      DEFINITIONS_UNITS[loopblock] = new InstrumentDefinition<?>[NB_UNITS[loopblock]][][];
      GENERATORS_UNITS[loopblock] = new GeneratorCurve[NB_UNITS[loopblock]][];
      NAMES_UNITS[loopblock] = new String[NB_UNITS[loopblock]][];
    }
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD};
    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD_2};
    DEFINITIONS_UNITS[1][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD};
    DEFINITIONS_UNITS[1][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD_2};
    DEFINITIONS_UNITS[2][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD};
    DEFINITIONS_UNITS[2][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD_2};
    DEFINITIONS_UNITS[3][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD};
    DEFINITIONS_UNITS[3][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD_2};
    DEFINITIONS_UNITS[3][2] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD6_USD};
    DEFINITIONS_UNITS[4][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD};
    DEFINITIONS_UNITS[4][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD_2};
    DEFINITIONS_UNITS[4][2] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD6_USD};
    DEFINITIONS_UNITS[5][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD_3, DEFINITIONS_DSC_USD};
    DEFINITIONS_UNITS[6][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD_3, DEFINITIONS_DSC_USD};
    DEFINITIONS_UNITS[7][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD};
    DEFINITIONS_UNITS[7][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD_4};
    GeneratorCurve genIntDQ = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_DQ);
    //    GeneratorCurve genIntCS = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_CS);
    GeneratorCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
    int compoundingRate = 1;
    GeneratorCurve genIntRPLin = new GeneratorCurveYieldPeriodicInterpolated(MATURITY_CALCULATOR, compoundingRate, INTERPOLATOR_LINEAR);
    GeneratorCurve genIntDFLL = new GeneratorCurveDiscountFactorInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LL);
    GeneratorCurve genNS = new GeneratorCurveYieldNelsonSiegel();
    GeneratorCurve genInt0 = new GeneratorCurveYieldInterpolatedAnchor(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
    GeneratorCurve genAddExistFwd3 = new GeneratorCurveAddYieldExisiting(genIntDQ, false, CURVE_NAME_FWD3_USD);
    //    GeneratorCurve genCst = new GeneratorCurveYieldConstant();
    // Interpolated (cubic spline on yield cc) and Cst+interpolated / interpolated (linear on periodic yield) / interpolated (exponential on discount factor)
    GENERATORS_UNITS[0][0] = new GeneratorCurve[] {genIntLin};
    GENERATORS_UNITS[0][1] = new GeneratorCurve[] {genIntLin};
    //    GENERATORS_UNITS[0][1] = new GeneratorCurve[] {new GeneratorCurveAddYield(new GeneratorCurve[] {genCst, genInt0}, false)};
    GENERATORS_UNITS[1][0] = new GeneratorCurve[] {genIntRPLin};
    GENERATORS_UNITS[1][1] = new GeneratorCurve[] {genIntRPLin};
    GENERATORS_UNITS[2][0] = new GeneratorCurve[] {genIntDFLL};
    GENERATORS_UNITS[2][1] = new GeneratorCurve[] {genIntDFLL};
    // 3xinterpolated / 2xinterpolated + spread over existing
    GENERATORS_UNITS[3][0] = new GeneratorCurve[] {genIntDQ};
    GENERATORS_UNITS[3][1] = new GeneratorCurve[] {genIntDQ};
    GENERATORS_UNITS[3][2] = new GeneratorCurve[] {genIntDQ};
    GENERATORS_UNITS[4][0] = new GeneratorCurve[] {genIntDQ};
    GENERATORS_UNITS[4][1] = new GeneratorCurve[] {genIntDQ};
    GENERATORS_UNITS[4][2] = new GeneratorCurve[] {genAddExistFwd3};
    // 2xinterpolated / interpolated + spread over existing
    GENERATORS_UNITS[5][0] = new GeneratorCurve[] {genIntDQ, genIntDQ};
    GENERATORS_UNITS[6][0] = new GeneratorCurve[] {genIntDQ, genAddExistFwd3};
    // interpolated + functional+interpolated
    GENERATORS_UNITS[7][0] = new GeneratorCurve[] {genIntDQ};
    GENERATORS_UNITS[7][1] = new GeneratorCurve[] {new GeneratorCurveAddYield(new GeneratorCurve[] {genNS, genInt0}, false)};
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_USD};
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD3_USD};
    NAMES_UNITS[1][0] = new String[] {CURVE_NAME_DSC_USD};
    NAMES_UNITS[1][1] = new String[] {CURVE_NAME_FWD3_USD};
    NAMES_UNITS[2][0] = new String[] {CURVE_NAME_DSC_USD};
    NAMES_UNITS[2][1] = new String[] {CURVE_NAME_FWD3_USD};
    NAMES_UNITS[3][0] = new String[] {CURVE_NAME_DSC_USD};
    NAMES_UNITS[3][1] = new String[] {CURVE_NAME_FWD3_USD};
    NAMES_UNITS[3][2] = new String[] {CURVE_NAME_FWD6_USD};
    NAMES_UNITS[4][0] = new String[] {CURVE_NAME_DSC_USD};
    NAMES_UNITS[4][1] = new String[] {CURVE_NAME_FWD3_USD};
    NAMES_UNITS[4][2] = new String[] {CURVE_NAME_FWD6_USD};
    NAMES_UNITS[5][0] = new String[] {CURVE_NAME_FWD3_USD, CURVE_NAME_DSC_USD};
    NAMES_UNITS[6][0] = new String[] {CURVE_NAME_FWD3_USD, CURVE_NAME_DSC_USD};
    NAMES_UNITS[7][0] = new String[] {CURVE_NAME_DSC_USD};
    NAMES_UNITS[7][1] = new String[] {CURVE_NAME_FWD3_USD};
  }

  // Present Value
  private static final PresentValueMCACalculator PV_CALCULATOR = PresentValueMCACalculator.getInstance();
  private static final PresentValueCurveSensitivityMCSCalculator PVCS_CALCULATOR = PresentValueCurveSensitivityMCSCalculator.getInstance();
  private static final Currency CCY_PV = CCY_USD;
  private static final PresentValueConvertedCalculator PV_CONVERTED_CALCULATOR = new PresentValueConvertedCalculator(CCY_PV, PV_CALCULATOR);
  private static final PresentValueCurveSensitivityConvertedCalculator PVCS_CONVERTED_CALCULATOR = new PresentValueCurveSensitivityConvertedCalculator(CCY_PV, PVCS_CALCULATOR);
  // Par spread market quote
  private static final ParSpreadMarketQuoteCalculator PSMQ_CALCULATOR = ParSpreadMarketQuoteCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityCalculator PSMQCS_CALCULATOR = ParSpreadMarketQuoteCurveSensitivityCalculator.getInstance();

  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> CURVES_PRESENT_VALUE_WITH_TODAY_BLOCK0;
  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> CURVES_PRESENT_VALUE_WITHOUT_TODAY_BLOCK0;
  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK0;
  private static List<Pair<YieldCurveBundle, CurveBuildingBlockBundle>> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK = new ArrayList<Pair<YieldCurveBundle, CurveBuildingBlockBundle>>();

  // Instrument used for sensitivity tests
  private static final Period SWAP_START = Period.ofMonths(6);
  private static final ZonedDateTime SWAP_SETTLE = ScheduleCalculator.getAdjustedDate(ScheduleCalculator.getAdjustedDate(NOW, SPOT_LAG, CALENDAR), SWAP_START, GENERATOR_DEPOSIT_USD);
  private static final Period SWAP_TENOR = Period.ofYears(5);
  private static final SwapFixedIborDefinition SWAP_DEFINITION = SwapFixedIborDefinition.from(SWAP_SETTLE, SWAP_TENOR, USD6MLIBOR3M, 1000000, 0.02, true);
  private static final SwapFixedCoupon<Coupon> SWAP = SWAP_DEFINITION.toDerivative(NOW, new String[] {CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD});

  private static final double TOLERANCE_PV = 1.0E-10;
  private static final double TOLERANCE_PNL = 1.0E+0;

  @BeforeSuite
  static void initClass() {
    CURVES_PRESENT_VALUE_WITH_TODAY_BLOCK0 = makeCurvesFromDefinitions(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PV_CONVERTED_CALCULATOR, PVCS_CONVERTED_CALCULATOR, true,
        0);
    CURVES_PRESENT_VALUE_WITHOUT_TODAY_BLOCK0 = makeCurvesFromDefinitions(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PV_CONVERTED_CALCULATOR, PVCS_CONVERTED_CALCULATOR,
        false, 0);
    CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK0 = makeCurvesFromDefinitions(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, false, 0);
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[loopblock], GENERATORS_UNITS[loopblock], NAMES_UNITS[loopblock], KNOWN_DATA, PSMQ_CALCULATOR,
          PSMQCS_CALCULATOR, false, loopblock));
    }
  }

  @Test
  public void curveConstructionGeneratorBlock0() {
    // Curve constructed with present value and today fixing
    curveConstructionTest(NAMES_UNITS[0], DEFINITIONS_UNITS[0], CURVES_PRESENT_VALUE_WITH_TODAY_BLOCK0.getFirst(), true, 0);
    // Curve constructed with present value and no today fixing
    curveConstructionTest(NAMES_UNITS[0], DEFINITIONS_UNITS[0], CURVES_PRESENT_VALUE_WITHOUT_TODAY_BLOCK0.getFirst(), false, 0);
    // Curve constructed with par spread (market quote) and no today fixing
    curveConstructionTest(NAMES_UNITS[0], DEFINITIONS_UNITS[0], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK0.getFirst(), false, 0);
    // Curve constructed with par spread (market quote) and  today fixing
    curveConstructionTest(NAMES_UNITS[0], DEFINITIONS_UNITS[0], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getFirst(), false, 0);
  }

  @Test
  public void curveConstructionGeneratorOtherBlocks() {
    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      curveConstructionTest(NAMES_UNITS[loopblock], DEFINITIONS_UNITS[loopblock], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst(), false, loopblock);
    }
    int t = 0;
    t++;
  }

  public void curveConstructionTest(String[][] curveNames, final InstrumentDefinition<?>[][][] definitions, final YieldCurveBundle curves, final boolean withToday, int block) {
    int nbBlocks = definitions.length;
    for (int loopblock = 0; loopblock < nbBlocks; loopblock++) {
      InstrumentDerivative[][] instruments = convert(curveNames, definitions[loopblock], loopblock, withToday, block);
      double[][] pv = new double[instruments.length][];
      for (int loopcurve = 0; loopcurve < instruments.length; loopcurve++) {
        pv[loopcurve] = new double[instruments[loopcurve].length];
        for (int loopins = 0; loopins < instruments[loopcurve].length; loopins++) {
          pv[loopcurve][loopins] = curves.getFxRates().convert(PV_CALCULATOR.visit(instruments[loopcurve][loopins], curves), CCY_USD).getAmount();
          assertEquals("Curve construction: node block " + loopblock + " - instrument " + loopins, 0, pv[loopcurve][loopins], TOLERANCE_PV);
        }
      }
    }
  }

  @Test
  /**
   * Test the market quote sensitivity by comparison to a finite difference (bump and recompute)
   */
  public void sensiParSpreadMQ() {
    ParameterUnderlyingSensitivityBlockCalculator pusbc = new ParameterUnderlyingSensitivityBlockCalculator(PVCS_CALCULATOR);
    final MarketQuoteSensitivityBlockCalculator mqsc = new MarketQuoteSensitivityBlockCalculator(pusbc);
    MultipleCurrencyInterestRateCurveSensitivity[] pvcs = new MultipleCurrencyInterestRateCurveSensitivity[NB_BLOCKS];
    MultipleCurrencyAmount[] pv = new MultipleCurrencyAmount[NB_BLOCKS];
    ParameterSensitivity[] ps = new ParameterSensitivity[NB_BLOCKS];
    ParameterSensitivity[] mqs = new ParameterSensitivity[NB_BLOCKS];
    Set<String> fixedCurves = new java.util.HashSet<String>();
    for (int loopblock = 1; loopblock < NB_BLOCKS; loopblock++) {
      pv[loopblock] = PV_CALCULATOR.visit(SWAP, CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst());
      pvcs[loopblock] = PVCS_CALCULATOR.visit(SWAP, CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst());
      ps[loopblock] = pusbc.calculateSensitivity(SWAP, fixedCurves, CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst());
      mqs[loopblock] = mqsc.fromInstrument(SWAP, fixedCurves, CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst(), CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getSecond());
    }
    for (int loopblock = 5; loopblock < 7; loopblock++) { // Test is underlying specific. Only 2 curves tested.
      testPnLNodeSensitivity(mqs[loopblock], loopblock);
    }
    int t = 0;
    t++;
  }

  public void testPnLNodeSensitivity(ParameterSensitivity mqSensitivities, int block) {
    double bp1 = 1.0E-4; // 1 bp
    final double eps = 0.1 * bp1;
    double pv = PV_CALCULATOR.visit(SWAP, CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(block).getFirst()).getAmount(CCY_USD);
    int nbFwd = DEFINITIONS_UNITS[block][0][0].length;
    double[] pvFwd = new double[nbFwd];
    double[] pnlFwd = new double[nbFwd];
    double[] pvcsFwdApprox = new double[nbFwd];
    for (int loopnode = 0; loopnode < nbFwd; loopnode++) {
      final double[] bumpedMarketValues = getBumpedMarketValues(FWD3_USD_MARKET_QUOTES_3, loopnode, eps);
      InstrumentDefinition<?>[] bumpedFwdDefinitions = getDefinitions(bumpedMarketValues, FWD3_USD_GENERATORS_3, FWD3_USD_TENOR_3, new Double[FWD3_USD_MARKET_QUOTES_3.length]);
      final Pair<YieldCurveBundle, CurveBuildingBlockBundle> bumpedResult = makeCurvesFromDefinitions(new InstrumentDefinition<?>[][][] {{bumpedFwdDefinitions, DEFINITIONS_DSC_USD}},
          GENERATORS_UNITS[block], NAMES_UNITS[block], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, false, block);
      pvFwd[loopnode] = PV_CONVERTED_CALCULATOR.visit(SWAP, bumpedResult.getFirst());
      pnlFwd[loopnode] = pvFwd[loopnode] - pv;
      pvcsFwdApprox[loopnode] = pnlFwd[loopnode] / eps;
      assertEquals("PL explain: Block " + block + " -  fwd node " + loopnode,
          mqSensitivities.getSensitivity(new ObjectsPair<String, Currency>(CURVE_NAME_FWD3_USD, CCY_USD)).getData()[loopnode] * bp1, pvcsFwdApprox[loopnode] * bp1, TOLERANCE_PNL);
    }
    int nbDsc = DEFINITIONS_UNITS[block][0][1].length;
    double[] pvDsc = new double[nbDsc];
    double[] pnlDsc = new double[nbDsc];
    double[] pvcsDscApprox = new double[nbDsc];
    for (int loopnode = 0; loopnode < nbDsc; loopnode++) {
      final double[] bumpedMarketValues = getBumpedMarketValues(DSC_USD_MARKET_QUOTES, loopnode, eps);
      InstrumentDefinition<?>[] bumpedDscDefinitions = getDefinitions(bumpedMarketValues, DSC_USD_GENERATORS, DSC_USD_TENOR, new Double[DSC_USD_MARKET_QUOTES.length]);
      final Pair<YieldCurveBundle, CurveBuildingBlockBundle> bumpedResult = makeCurvesFromDefinitions(new InstrumentDefinition<?>[][][] {{DEFINITIONS_FWD3_USD_3, bumpedDscDefinitions}},
          GENERATORS_UNITS[block], NAMES_UNITS[block], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, false, block);
      pvDsc[loopnode] = PV_CONVERTED_CALCULATOR.visit(SWAP, bumpedResult.getFirst());
      pnlDsc[loopnode] = pvDsc[loopnode] - pv;
      pvcsDscApprox[loopnode] = pnlDsc[loopnode] / eps;
      assertEquals("PL explain: Block " + block + " -  Dsc node " + loopnode, mqSensitivities.getSensitivity(new ObjectsPair<String, Currency>(CURVE_NAME_DSC_USD, CCY_USD)).getData()[loopnode] * bp1,
          pvcsDscApprox[loopnode] * bp1, TOLERANCE_PNL);
    }
    int t = 0;
    t++;
  }

  private static double[] getBumpedMarketValues(final double[] marketValues, final int n, final double eps) {
    final double[] bumped = Arrays.copyOf(marketValues, marketValues.length);
    bumped[n] += eps;
    return bumped;
  }

  @Test(enabled = false)
  /**
   * Code used to graph the curves
   */
  public void analysis() {
    int nbPoints = 210;
    double endTime = 21.0;
    double[] x = new double[nbPoints + 1];
    for (int looppt = 0; looppt <= nbPoints; looppt++) {
      x[looppt] = looppt * endTime / nbPoints;
    }
    int nbAnalysis = NB_BLOCKS;
    YieldCurveBundle[] bundle = new YieldCurveBundle[nbAnalysis];
    double[][][] rate = new double[nbAnalysis][][];
    for (int loopblock = 0; loopblock < nbAnalysis; loopblock++) {
      bundle[loopblock] = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(loopblock).getFirst();
    }
    for (int loopbundle = 0; loopbundle < nbAnalysis; loopbundle++) {
      Set<String> curveNames = bundle[loopbundle].getAllNames();
      int nbCurve = curveNames.size();
      int loopc = 0;
      rate[loopbundle] = new double[nbCurve][nbPoints + 1];
      for (String name : curveNames) {
        for (int looppt = 0; looppt <= nbPoints; looppt++) {
          rate[loopbundle][loopc][looppt] = bundle[loopbundle].getCurve(name).getInterestRate(x[looppt]);
        }
        loopc++;
      }
    }
    double[] rateNS = new double[nbPoints + 1];
    YieldAndDiscountAddZeroSpreadCurve curve = (YieldAndDiscountAddZeroSpreadCurve) bundle[7].getCurve(CURVE_NAME_FWD3_USD);
    DoublesCurveNelsonSiegel curveNS = (DoublesCurveNelsonSiegel) (((YieldCurve) curve.getCurves()[0]).getCurve());
    for (int looppt = 0; looppt <= nbPoints; looppt++) {
      rateNS[looppt] = curveNS.getYValue(x[looppt]);
    }
    int t = 0;
    t++;
  }

  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;
    @SuppressWarnings("unused")
    Pair<YieldCurveBundle, CurveBuildingBlockBundle> curvePresentValue;
    @SuppressWarnings("unused")
    Pair<YieldCurveBundle, CurveBuildingBlockBundle> curveParSpreadMQ;
    int[] nbIns = new int[NB_BLOCKS];
    nbIns[0] = DEFINITIONS_DSC_USD.length + DEFINITIONS_FWD3_USD_2.length;
    nbIns[1] = DEFINITIONS_DSC_USD.length + DEFINITIONS_FWD3_USD_2.length;
    nbIns[2] = DEFINITIONS_DSC_USD.length + DEFINITIONS_FWD3_USD_2.length;
    nbIns[3] = DEFINITIONS_DSC_USD.length + DEFINITIONS_FWD3_USD_2.length + DEFINITIONS_FWD6_USD.length;
    nbIns[4] = DEFINITIONS_DSC_USD.length + DEFINITIONS_FWD3_USD_2.length + DEFINITIONS_FWD6_USD.length;
    nbIns[5] = DEFINITIONS_DSC_USD.length + DEFINITIONS_FWD3_USD_3.length;
    nbIns[6] = DEFINITIONS_DSC_USD.length + DEFINITIONS_FWD3_USD_3.length;
    nbIns[7] = DEFINITIONS_DSC_USD.length + DEFINITIONS_FWD3_USD_4.length;
    int[] nbCurve = new int[] {2, 2, 2, 3, 3, 2, 2, 2};

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      curvePresentValue = makeCurvesFromDefinitions(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PV_CONVERTED_CALCULATOR, PVCS_CONVERTED_CALCULATOR, true, 0);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction Full 2 curves (Int / Cst+Int) and " + nbIns[0] + " instruments (with present value): " + (endTime - startTime) + " ms");
    // Performance note: curve construction (with present value, 2 curves (Int / Cst+Int) and 21 instruments by units): 23-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: xx ms for 100 bundles.

    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      startTime = System.currentTimeMillis();
      for (int looptest = 0; looptest < nbTest; looptest++) {
        curveParSpreadMQ = makeCurvesFromDefinitions(DEFINITIONS_UNITS[loopblock], GENERATORS_UNITS[loopblock], NAMES_UNITS[loopblock], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, false,
            loopblock);
      }
      endTime = System.currentTimeMillis();
      System.out.println(nbTest + " curve construction Full " + nbCurve[loopblock] + " curves and " + nbIns[loopblock] + " instruments (with par spread-market quote): " + (endTime - startTime)
          + " ms");
    }
    // Performance note: curve construction (with par spread/market quote), 2 curves (Int / Cst+Int) and 21 instruments by units): 23-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: xx ms for 100 bundles.
    //    100 curve construction Full 2 curves and 22 instruments (with par spread-market quote): 391 ms
    //    100 curve construction Full 2 curves and 22 instruments (with par spread-market quote): 472 ms
    //    100 curve construction Full 2 curves and 22 instruments (with par spread-market quote): 631 ms
    //    100 curve construction Full 3 curves and 26 instruments (with par spread-market quote): 545 ms
    //    100 curve construction Full 3 curves and 26 instruments (with par spread-market quote): 549 ms
    //    100 curve construction Full 2 curves and 24 instruments (with par spread-market quote): 605 ms
    //    100 curve construction Full 2 curves and 24 instruments (with par spread-market quote): 847 ms
    //    100 curve construction Full 2 curves and 23 instruments (with par spread-market quote): 564 ms

    for (int loopblock = 0; loopblock < NB_BLOCKS; loopblock++) {
      startTime = System.currentTimeMillis();
      for (int looptest = 0; looptest < nbTest; looptest++) {
        curveParSpreadMQ = makeCurvesFromDefinitions(DEFINITIONS_UNITS[loopblock], GENERATORS_UNITS[loopblock], NAMES_UNITS[loopblock], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, false,
            loopblock);
      }
      endTime = System.currentTimeMillis();
      System.out.println(nbTest + " curve construction Full " + nbCurve[loopblock] + " curves and " + nbIns[loopblock] + " instruments (with par spread-market quote): " + (endTime - startTime)
          + " ms");
    }
    // Performance note: curve construction (with par spread/market quote), 2 curves (Int / Cst+Int) and 21 instruments by units): 23-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: xx ms for 100 bundles.

  }

  public static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, final GeneratorInstrument[] generators, final Period[] tenors, final Double[] other) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(NOW, tenors[loopmv], marketQuotes[loopmv], NOTIONAL, other[loopmv]);
    }
    return definitions;
  }

  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> makeCurvesFromDefinitions(final InstrumentDefinition<?>[][][] definitions, GeneratorCurve[][] curveGenerators, String[][] curveNames,
      YieldCurveBundle knownData, final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator,
      final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> sensitivityCalculator, boolean withToday, int block) {
    int nbUnits = curveGenerators.length;
    double[][] parametersGuess = new double[nbUnits][];
    GeneratorCurve[][] generatorFinal = new GeneratorCurve[nbUnits][];
    InstrumentDerivative[][][] instruments = new InstrumentDerivative[nbUnits][][];
    for (int loopunit = 0; loopunit < nbUnits; loopunit++) {
      generatorFinal[loopunit] = new GeneratorCurve[curveGenerators[loopunit].length];
      int nbInsUnit = 0;
      for (int loopcurve = 0; loopcurve < curveGenerators[loopunit].length; loopcurve++) {
        nbInsUnit += definitions[loopunit][loopcurve].length;
      }
      parametersGuess[loopunit] = new double[nbInsUnit];
      int startCurve = 0; // First parameter index of the curve in the unit. 
      instruments[loopunit] = convert(curveNames, definitions[loopunit], loopunit, withToday, block);
      for (int loopcurve = 0; loopcurve < curveGenerators[loopunit].length; loopcurve++) {
        generatorFinal[loopunit][loopcurve] = curveGenerators[loopunit][loopcurve].finalGenerator(instruments[loopunit][loopcurve]);
        double[] guessCurve = generatorFinal[loopunit][loopcurve].initialGuess(initialGuess(definitions[loopunit][loopcurve]));
        System.arraycopy(guessCurve, 0, parametersGuess[loopunit], startCurve, instruments[loopunit][loopcurve].length);
        startCurve += instruments[loopunit][loopcurve].length;
      }
      if ((block == 7) && (loopunit == 1)) {
        parametersGuess[loopunit] = new double[] {0.012, -0.003, 0.018, 1.60, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
      }
    }
    return CURVE_BUILDING_FUNCTION.makeCurvesFromDerivatives(instruments, generatorFinal, curveNames, parametersGuess, knownData, calculator, sensitivityCalculator);
  }

  @SuppressWarnings("unchecked")
  private static InstrumentDerivative[][] convert(String[][] curveNames, InstrumentDefinition<?>[][] definitions, int unit, boolean withToday, int block) {
    int nbDef = 0;
    for (int loopdef1 = 0; loopdef1 < definitions.length; loopdef1++) {
      nbDef += definitions[loopdef1].length;
    }
    final InstrumentDerivative[][] instruments = new InstrumentDerivative[definitions.length][];
    for (int loopcurve = 0; loopcurve < definitions.length; loopcurve++) {
      instruments[loopcurve] = new InstrumentDerivative[definitions[loopcurve].length];
      int loopins = 0;
      for (final InstrumentDefinition<?> instrument : definitions[loopcurve]) {
        InstrumentDerivative ird;
        if (instrument instanceof SwapFixedONDefinition) {
          final String[] names = getCurvesNameSwapFixedON(curveNames, block);
          ird = ((SwapFixedONDefinition) instrument).toDerivative(NOW, getTSSwapFixedON(withToday, unit), names);
        } else {
          if (instrument instanceof SwapFixedIborDefinition) {
            final String[] names = getCurvesNameSwapFixedIbor(curveNames, unit, block);
            ird = ((SwapFixedIborDefinition) instrument).toDerivative(NOW, getTSSwapFixedIbor(withToday, unit), names);
          } else {
            final String[] names;
            // Cash
            names = getCurvesNameCash(curveNames, loopcurve, unit, block);
            ird = instrument.toDerivative(NOW, names);
          }
        }
        instruments[loopcurve][loopins++] = ird;
      }
    }
    return instruments;
  }

  private static double initialGuess(InstrumentDefinition<?> instrument) {
    if (instrument instanceof SwapFixedONDefinition) {
      return ((SwapFixedONDefinition) instrument).getFixedLeg().getNthPayment(0).getRate();
    }
    if (instrument instanceof SwapFixedIborDefinition) {
      return ((SwapFixedIborDefinition) instrument).getFixedLeg().getNthPayment(0).getRate();
    }
    if (instrument instanceof ForwardRateAgreementDefinition) {
      return ((ForwardRateAgreementDefinition) instrument).getRate();
    }
    if (instrument instanceof CashDefinition) {
      return ((CashDefinition) instrument).getRate();
    }
    return 0.01;
  }

  private static double[] initialGuess(InstrumentDefinition<?>[] definitions) {
    double[] result = new double[definitions.length];
    int loopr = 0;
    for (int loopcurve = 0; loopcurve < definitions.length; loopcurve++) {
      result[loopr++] = initialGuess(definitions[loopcurve]);
    }
    return result;
  }

  private static String[] getCurvesNameSwapFixedON(String[][] curveNames, Integer block) {
    switch (block) {
      case 0:
      case 1:
      case 2:
      case 3:
      case 4:
      case 7:
        return new String[] {curveNames[0][0], curveNames[0][0]};
      case 5:
      case 6:
        return new String[] {curveNames[0][1], curveNames[0][1]};
      default:
        throw new IllegalArgumentException(block.toString());
    }
  }

  private static String[] getCurvesNameSwapFixedIbor(String[][] curveNames, Integer unit, Integer block) {
    switch (unit) {
      case 0:
        switch (block) {
          case 0:
          case 1:
          case 2:
          case 3:
          case 4:
          case 7:
            return new String[] {curveNames[0][0], curveNames[1][0]};
          case 5:
          case 6:
            return new String[] {curveNames[0][1], curveNames[0][0]};
          default:
            throw new IllegalArgumentException(block.toString());
        }
      case 1:
        return new String[] {curveNames[0][0], curveNames[1][0]};
      case 2:
        return new String[] {curveNames[0][0], curveNames[2][0]};
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

  private static String[] getCurvesNameCash(String[][] curveNames, Integer loopcurve, Integer unit, Integer block) {
    switch (unit) {
      case 0:
        switch (block) {
          case 0:
          case 1:
          case 2:
          case 3:
          case 4:
          case 7:
            return new String[] {curveNames[0][0]};
          case 5:
          case 6:
            return new String[] {curveNames[0][loopcurve]};
          default:
            throw new IllegalArgumentException(block.toString());
        }
      case 1:
        return new String[] {curveNames[1][0]};
      case 2:
        return new String[] {curveNames[2][0]};
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

  @SuppressWarnings("rawtypes")
  private static DoubleTimeSeries[] getTSSwapFixedON(Boolean withToday, Integer unit) {
    switch (unit) {
      case 0:
        return withToday ? TS_FIXED_OIS_USD_WITH_TODAY : TS_FIXED_OIS_USD_WITHOUT_TODAY;
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

  @SuppressWarnings("rawtypes")
  private static DoubleTimeSeries[] getTSSwapFixedIbor(Boolean withToday, Integer unit) {
    switch (unit) {
      case 0:
        return withToday ? TS_FIXED_IBOR_USD3M_WITH_TODAY : TS_FIXED_IBOR_USD3M_WITHOUT_TODAY;
      case 1:
        return withToday ? TS_FIXED_IBOR_USD3M_WITH_TODAY : TS_FIXED_IBOR_USD3M_WITHOUT_TODAY;
      case 2:
        return withToday ? TS_FIXED_IBOR_USD6M_WITH_TODAY : TS_FIXED_IBOR_USD6M_WITHOUT_TODAY;
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

}
