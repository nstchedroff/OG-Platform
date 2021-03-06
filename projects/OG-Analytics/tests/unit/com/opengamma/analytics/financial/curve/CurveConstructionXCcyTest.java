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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.calculator.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.calculator.PresentValueConvertedCalculator;
import com.opengamma.analytics.financial.calculator.PresentValueCurveSensitivityConvertedCalculator;
import com.opengamma.analytics.financial.calculator.PresentValueCurveSensitivityMCSCalculator;
import com.opengamma.analytics.financial.calculator.PresentValueMCACalculator;
import com.opengamma.analytics.financial.curve.building.CurveBuildingBlock;
import com.opengamma.analytics.financial.curve.building.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.curve.building.MultipleYieldCurveFinderGeneratorDataBundle;
import com.opengamma.analytics.financial.curve.building.MultipleYieldCurveFinderGeneratorFunction;
import com.opengamma.analytics.financial.curve.building.MultipleYieldCurveFinderGeneratorJacobian;
import com.opengamma.analytics.financial.curve.generator.GeneratorCurve;
import com.opengamma.analytics.financial.curve.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.sensitivity.ParameterSensitivity;
import com.opengamma.analytics.financial.curve.sensitivity.ParameterSensitivityBlockCalculator;
import com.opengamma.analytics.financial.curve.sensitivity.ParameterSensitivityCalculator;
import com.opengamma.analytics.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorForexSwap;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapXCcyIborIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.generator.GeneratorSwapTestsMaster;
import com.opengamma.analytics.financial.instrument.index.iborindex.IndexIborTestsMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapXCcyIborIborDefinition;
import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.LastTimeCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCalculator;
import com.opengamma.analytics.financial.interestrate.ParSpreadMarketQuoteCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Build of curve in several currencies in several blocks with relevant Jacobian matrices.
 * Currencies: USD (2 curves), EUR (2 curves), JPY (3 curves)
 */
public class CurveConstructionXCcyTest {

  //  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
  //      Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final MatrixAlgebra MATRIX_ALGEBRA = new CommonsMatrixAlgebra();

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final BroydenVectorRootFinder ROOT_FINDER = new BroydenVectorRootFinder(TOLERANCE_ROOT, TOLERANCE_ROOT, 10000,
      DecompositionFactory.getDecomposition(DecompositionFactory.SV_COLT_NAME));
  private static final Currency CCY_USD = Currency.USD;
  private static final Currency CCY_EUR = Currency.EUR;
  private static final Currency CCY_JPY = Currency.JPY;
  private static final double FX_EURUSD = 1.40;
  private static final double FX_USDJPY = 80.0;
  private static final FXMatrix FX_MATRIX = new FXMatrix(CCY_USD);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("CAL");
  private static final int SPOT_LAG = 2;
  private static final DayCount DAY_COUNT_CASH = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final DayCount DAY_COUNT_CASH_3 = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  private static final double NOTIONAL = 1.0;
  private static final BusinessDayConvention BDC = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final IndexON INDEX_ON_1 = new IndexON("Fed Fund", CCY_USD, DAY_COUNT_CASH, 1, CALENDAR);
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_1 = new GeneratorDepositON("USD Deposit ON", CCY_USD, CALENDAR, DAY_COUNT_CASH);
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_2 = new GeneratorDepositON("EUR Deposit ON", CCY_EUR, CALENDAR, DAY_COUNT_CASH);
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_3 = new GeneratorDepositON("JPY Deposit ON", CCY_JPY, CALENDAR, DAY_COUNT_CASH_3);
  private static final GeneratorFixedON GENERATOR_OIS_1 = new GeneratorFixedON("USD1YFEDFUND", INDEX_ON_1, Period.ofMonths(12), DAY_COUNT_CASH, BDC, true, SPOT_LAG, SPOT_LAG);
  private static final GeneratorForexSwap GENERATOR_FX_EURUSD = new GeneratorForexSwap("EURUSD", CCY_EUR, CCY_USD, CALENDAR, SPOT_LAG, BDC, true);
  private static final GeneratorForexSwap GENERATOR_FX_USDJPY = new GeneratorForexSwap("USDJPY", CCY_USD, CCY_JPY, CALENDAR, SPOT_LAG, BDC, true);
  private static final GeneratorDeposit GENERATOR_DEPOSIT_USD = new GeneratorDeposit("USD Deposit", CCY_USD, CALENDAR, SPOT_LAG, DAY_COUNT_CASH, BDC, true);
  private static final GeneratorDeposit GENERATOR_DEPOSIT_EUR = new GeneratorDeposit("EUR Deposit", CCY_EUR, CALENDAR, SPOT_LAG, DAY_COUNT_CASH, BDC, true);
  private static final GeneratorDeposit GENERATOR_DEPOSIT_JPY = new GeneratorDeposit("JPY Deposit", CCY_JPY, CALENDAR, SPOT_LAG, DAY_COUNT_CASH, BDC, true);
  private static final GeneratorSwapTestsMaster GENERATOR_SWAP_MASTER = GeneratorSwapTestsMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", CALENDAR);
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR3M", CALENDAR);
  private static final GeneratorSwapFixedIbor JPY6MLIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("JPY6MLIBOR6M", CALENDAR);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final IborIndex EURIBOR3M = EUR1YEURIBOR3M.getIborIndex();
  private static final IborIndex JPYLIBOR6M = JPY6MLIBOR6M.getIborIndex();
  private static final IborIndex JPYLIBOR3M = IndexIborTestsMaster.getInstance().getIndex("JPYLIBOR3M", CALENDAR);
  private static final GeneratorSwapXCcyIborIbor EURIBOR3MUSDLIBOR3M = new GeneratorSwapXCcyIborIbor("EURIBOR3MUSDLIBOR3M", EURIBOR3M, USDLIBOR3M); // Spread on EUR leg
  private static final GeneratorSwapXCcyIborIbor JPYLIBOR3MUSDLIBOR3M = new GeneratorSwapXCcyIborIbor("JPYLIBOR3MUSDLIBOR3M", JPYLIBOR3M, USDLIBOR3M); // Spread on JPY leg
  private static final GeneratorSwapXCcyIborIbor JPYLIBOR3MEURIBOR3M = new GeneratorSwapXCcyIborIbor("JPYLIBOR3MEURIBOR3M", JPYLIBOR3M, EURIBOR3M); // Spread on JPY leg
  private static final GeneratorSwapIborIbor JPYLIBOR6MLIBOR3M = new GeneratorSwapIborIbor("JPYLIBOR6MLIBOR3M", JPYLIBOR3M, JPYLIBOR6M);

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

  private static final ArrayZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITH_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
      DateUtils.getUTCDate(2011, 9, 28)}, new double[] {0.0060, 0.0061});
  private static final ArrayZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITHOUT_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27)},
      new double[] {0.0060});

  private static final ArrayZonedDateTimeDoubleTimeSeries TS_IBOR_JPY3M_WITH_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
      DateUtils.getUTCDate(2011, 9, 28)}, new double[] {0.0060, 0.0061});
  private static final ArrayZonedDateTimeDoubleTimeSeries TS_IBOR_JPY3M_WITHOUT_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27)},
      new double[] {0.0060});
  private static final ArrayZonedDateTimeDoubleTimeSeries TS_IBOR_JPY6M_WITH_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
      DateUtils.getUTCDate(2011, 9, 28)}, new double[] {0.0060, 0.0061});
  private static final ArrayZonedDateTimeDoubleTimeSeries TS_IBOR_JPY6M_WITHOUT_TODAY = new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27)},
      new double[] {0.0060});
  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITH_TODAY = new DoubleTimeSeries[] {TS_IBOR_USD3M_WITH_TODAY};
  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_IBOR_USD3M_WITHOUT_TODAY = new DoubleTimeSeries[] {TS_IBOR_USD3M_WITHOUT_TODAY};
  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_IBOR_EUR3M_WITH_TODAY = new DoubleTimeSeries[] {TS_IBOR_EUR3M_WITH_TODAY};
  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_IBOR_EUR3M_WITHOUT_TODAY = new DoubleTimeSeries[] {TS_IBOR_EUR3M_WITHOUT_TODAY};
  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_IBOR_EURUSD3M_WITH_TODAY = new DoubleTimeSeries[] {TS_IBOR_EUR3M_WITH_TODAY, TS_IBOR_USD3M_WITH_TODAY};
  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_IBOR_EURUSD3M_WITHOUT_TODAY = new DoubleTimeSeries[] {TS_IBOR_EUR3M_WITHOUT_TODAY, TS_IBOR_USD3M_WITHOUT_TODAY};
  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_IBOR_JPY3MJPY6M_WITH_TODAY = new DoubleTimeSeries[] {TS_IBOR_JPY3M_WITH_TODAY, TS_IBOR_JPY6M_WITH_TODAY};
  @SuppressWarnings("rawtypes")
  private static final DoubleTimeSeries[] TS_FIXED_IBOR_JPY3MJPY6M_WITHOUT_TODAY = new DoubleTimeSeries[] {TS_IBOR_JPY3M_WITHOUT_TODAY, TS_IBOR_JPY6M_WITHOUT_TODAY};

  private static final String CURVE_NAME_DSC_USD = "USD Dsc";
  private static final String CURVE_NAME_FWD3_USD = "USD Fwd 3M";
  private static final String CURVE_NAME_DSC_EUR = "EUR Dsc";
  private static final String CURVE_NAME_FWD3_EUR = "EUR Fwd 3M";
  private static final String CURVE_NAME_DSC_JPY = "JPY Dsc";
  private static final String CURVE_NAME_FWD3_JPY = "JPY Fwd 3M";
  private static final String CURVE_NAME_FWD6_JPY = "JPY Fwd 6M";
  private static final HashMap<String, Currency> CCY_MAP = new HashMap<String, Currency>();
  static {
    CCY_MAP.put(CURVE_NAME_DSC_USD, CCY_USD);
    CCY_MAP.put(CURVE_NAME_FWD3_USD, CCY_USD);
    CCY_MAP.put(CURVE_NAME_DSC_EUR, CCY_EUR);
    CCY_MAP.put(CURVE_NAME_FWD3_EUR, CCY_EUR);
    CCY_MAP.put(CURVE_NAME_DSC_JPY, CCY_JPY);
    CCY_MAP.put(CURVE_NAME_FWD3_JPY, CCY_JPY);
    CCY_MAP.put(CURVE_NAME_FWD6_JPY, CCY_JPY);
    FX_MATRIX.addCurrency(CCY_EUR, CCY_USD, FX_EURUSD);
    FX_MATRIX.addCurrency(CCY_JPY, CCY_USD, 1 / FX_USDJPY);
  }

  /** Market values for the dsc USD curve */
  public static final double[] DSC_1_MARKET_QUOTES = new double[] {0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0010, 0.0015, 0.0020, 0.0035, 0.0050, 0.0130};
  /** Generators for the dsc USD curve */
  public static final GeneratorInstrument[] DSC_1_GENERATORS = new GeneratorInstrument[] {GENERATOR_DEPOSIT_ON_1, GENERATOR_DEPOSIT_ON_1, GENERATOR_OIS_1, GENERATOR_OIS_1, GENERATOR_OIS_1, GENERATOR_OIS_1,
      GENERATOR_OIS_1, GENERATOR_OIS_1, GENERATOR_OIS_1, GENERATOR_OIS_1, GENERATOR_OIS_1, GENERATOR_OIS_1, GENERATOR_OIS_1};
  /** Tenors for the dsc USD curve */
  public static final Period[] DSC_1_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
      Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10)};

  /** Market values for the Fwd 3M USD curve */
  public static final double[] FWD_1_MARKET_QUOTES = new double[] {0.0045, 0.0045, 0.0045, 0.0045, 0.0060, 0.0070, 0.0080, 0.0160};
  /** Generators for the Fwd 3M USD curve */
  public static final GeneratorInstrument[] FWD_1_GENERATORS = new GeneratorInstrument[] {GENERATOR_DEPOSIT_USD, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M};
  /** Tenors for the Fwd 3M USD curve */
  public static final Period[] FWD_1_TENOR = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
      Period.ofYears(10)};

  /** Market values for the dsc EUR curve */
  public static final double[] DSC_EUR_MARKET_QUOTES = new double[] {0.0010, 0.0010, 0.0004, 0.0009, 0.0015, 0.0035, 0.0050, 0.0060, -0.0050, -0.0050, -0.0050, -0.0045, -0.0040};
  //  public static final double[] DSC_EUR_MARKET_QUOTES = new double[] {0.0010, 0.0010, 0.0004 * FX_EURUSD, 0.0009 * FX_EURUSD, 0.0015 * FX_EURUSD, 0.0035 * FX_EURUSD, 0.0050 * FX_EURUSD,
  //    0.0060 * FX_EURUSD, -0.0050, -0.0050, -0.0050, -0.0045, -0.0040};
  /** Generators for the dsc EUR curve */
  public static final GeneratorInstrument[] DSC_EUR_GENERATORS = new GeneratorInstrument[] {GENERATOR_DEPOSIT_ON_2, GENERATOR_DEPOSIT_ON_2, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD,
      GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M};
  /** Tenors for the dsc EUR curve */
  public static final Period[] DSC_EUR_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
      Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10)};
  //  /** FX rqtes for the dsc EUR curve */
  //  public static final Double[] DSC_EUR_FX_RATE = new Double[] {FX_EURUSD, FX_EURUSD, FX_EURUSD, FX_EURUSD, FX_EURUSD, FX_EURUSD, FX_EURUSD, FX_EURUSD, FX_EURUSD, FX_EURUSD, FX_EURUSD, FX_EURUSD,
  //      FX_EURUSD};

  /** Market values for the Fwd 3M EUR curve */
  public static final double[] FWD_EUR_MARKET_QUOTES = new double[] {0.0045, 0.0045, 0.0045, 0.0045, 0.0050, 0.0060, 0.0085, 0.0160};
  /** Generators for the Fwd 3M USD curve */
  public static final GeneratorInstrument[] FWD_EUR_GENERATORS = new GeneratorInstrument[] {GENERATOR_DEPOSIT_EUR, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M,
      EUR1YEURIBOR3M};
  /** Tenors for the Fwd 3M USD curve */
  public static final Period[] FWD_EUR_TENOR = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
      Period.ofYears(10)};

  /** Market values for the dsc JPY curve */
  public static final double[] DSC_JPY_MARKET_QUOTES = new double[] {0.0005, 0.0005, -0.0004, -0.0008, -0.0012, -0.0024, -0.0036, -0.0048, -0.0030, -0.0040, -0.0040, -0.0045, -0.0050};
  /** Generators for the dsc EUR curve */
  public static final GeneratorInstrument[] DSC_JPY_GENERATORS = new GeneratorInstrument[] {GENERATOR_DEPOSIT_ON_3, GENERATOR_DEPOSIT_ON_3, GENERATOR_FX_USDJPY, GENERATOR_FX_USDJPY, GENERATOR_FX_USDJPY,
      GENERATOR_FX_USDJPY, GENERATOR_FX_USDJPY, GENERATOR_FX_USDJPY, JPYLIBOR3MUSDLIBOR3M, JPYLIBOR3MUSDLIBOR3M, JPYLIBOR3MUSDLIBOR3M, JPYLIBOR3MUSDLIBOR3M, JPYLIBOR3MUSDLIBOR3M};
  /** Tenors for the dsc EUR curve */
  public static final Period[] DSC_JPY_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
      Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10)};
  //  /** FX rqtes for the dsc EUR curve */
  //  public static final Double[] DSC_JPY_FX_RATE = new Double[] {FX_USDJPY, FX_USDJPY, FX_USDJPY, FX_USDJPY, FX_USDJPY, FX_USDJPY, FX_USDJPY, FX_USDJPY, FX_USDJPY, FX_USDJPY, FX_USDJPY, FX_USDJPY,
  //      FX_USDJPY};

  /** Market values for the Fwd 3M JPY curve */
  public static final double[] FWD3_JPY_MARKET_QUOTES = new double[] {0.0020, 0.0010, 0.0010, 0.0010, 0.0010, 0.0015, 0.0015, 0.0015};
  /** Generators for the Fwd 3M JPY curve */
  public static final GeneratorInstrument[] FWD3_JPY_GENERATORS = new GeneratorInstrument[] {GENERATOR_DEPOSIT_JPY, JPYLIBOR6MLIBOR3M, JPYLIBOR6MLIBOR3M, JPYLIBOR6MLIBOR3M, JPYLIBOR6MLIBOR3M, JPYLIBOR6MLIBOR3M,
      JPYLIBOR6MLIBOR3M, JPYLIBOR6MLIBOR3M};
  /** Tenors for the Fwd 3M JPY curve */
  public static final Period[] FWD3_JPY_TENOR = new Period[] {Period.ofMonths(3), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
      Period.ofYears(10)};

  /** Market values for the Fwd 6M JPY curve */
  public static final double[] FWD6_JPY_MARKET_QUOTES = new double[] {0.0035, 0.0035, 0.0035, 0.0040, 0.0040, 0.0040, 0.0075};
  /** Generators for the Fwd 6M JPY curve */
  public static final GeneratorInstrument[] FWD6_JPY_GENERATORS = new GeneratorInstrument[] {GENERATOR_DEPOSIT_JPY, JPY6MLIBOR6M, JPY6MLIBOR6M, JPY6MLIBOR6M, JPY6MLIBOR6M, JPY6MLIBOR6M, JPY6MLIBOR6M};
  /** Tenors for the Fwd 6M JPY curve */
  public static final Period[] FWD6_JPY_TENOR = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10)};

  /** Standard USD discounting curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_DSC_USD;
  /** Standard USD Forward 3M curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_USD;
  /** Standard EUR discounting curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_DSC_EUR;
  /** Standard EUR Forward 3M curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_EUR;
  /** Standard JPY discounting curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_DSC_JPY;
  /** Standard JPY Forward 3M curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_JPY;
  /** Standard JPY Forward 6M curve instrument definitions */
  public static final InstrumentDefinition<?>[] DEFINITIONS_FWD6_JPY;
  /** Units of curves */
  public static final int[] NB_UNITS = new int[] {3, 3, 1};
  public static final int NB_BLOCKS = NB_UNITS.length;
  public static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  public static final GeneratorCurve[][][] GENERATORS_UNITS = new GeneratorCurve[NB_BLOCKS][][];
  public static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];
  public static final YieldCurveBundle KNOWN_DATA = new YieldCurveBundle(FX_MATRIX, CCY_MAP);

  static {
    DEFINITIONS_DSC_USD = getDefinitions(DSC_1_MARKET_QUOTES, DSC_1_GENERATORS, DSC_1_TENOR);
    DEFINITIONS_FWD3_USD = getDefinitions(FWD_1_MARKET_QUOTES, FWD_1_GENERATORS, FWD_1_TENOR);
    DEFINITIONS_DSC_EUR = getDefinitions(DSC_EUR_MARKET_QUOTES, DSC_EUR_GENERATORS, DSC_EUR_TENOR);
    DEFINITIONS_FWD3_EUR = getDefinitions(FWD_EUR_MARKET_QUOTES, FWD_EUR_GENERATORS, FWD_EUR_TENOR);
    DEFINITIONS_DSC_JPY = getDefinitions(DSC_JPY_MARKET_QUOTES, DSC_JPY_GENERATORS, DSC_JPY_TENOR);
    DEFINITIONS_FWD3_JPY = getDefinitions(FWD3_JPY_MARKET_QUOTES, FWD3_JPY_GENERATORS, FWD3_JPY_TENOR);
    DEFINITIONS_FWD6_JPY = getDefinitions(FWD6_JPY_MARKET_QUOTES, FWD6_JPY_GENERATORS, FWD6_JPY_TENOR);
    DEFINITIONS_UNITS[0] = new InstrumentDefinition<?>[NB_UNITS[0]][][];
    DEFINITIONS_UNITS[1] = new InstrumentDefinition<?>[NB_UNITS[1]][][];
    DEFINITIONS_UNITS[2] = new InstrumentDefinition<?>[NB_UNITS[2]][][];
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD};
    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD};
    DEFINITIONS_UNITS[0][2] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_EUR, DEFINITIONS_FWD3_EUR};
    DEFINITIONS_UNITS[1][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD};
    DEFINITIONS_UNITS[1][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD};
    DEFINITIONS_UNITS[1][2] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_JPY, DEFINITIONS_FWD3_JPY, DEFINITIONS_FWD6_JPY};
    DEFINITIONS_UNITS[2][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD, DEFINITIONS_FWD3_USD, DEFINITIONS_DSC_JPY, DEFINITIONS_FWD3_JPY, DEFINITIONS_FWD6_JPY};
    GeneratorCurve genInt = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR);
    GENERATORS_UNITS[0] = new GeneratorCurve[NB_UNITS[0]][];
    GENERATORS_UNITS[1] = new GeneratorCurve[NB_UNITS[1]][];
    GENERATORS_UNITS[2] = new GeneratorCurve[NB_UNITS[2]][];
    GENERATORS_UNITS[0][0] = new GeneratorCurve[] {genInt};
    GENERATORS_UNITS[0][1] = new GeneratorCurve[] {genInt};
    GENERATORS_UNITS[0][2] = new GeneratorCurve[] {genInt, genInt};
    GENERATORS_UNITS[1][0] = new GeneratorCurve[] {genInt};
    GENERATORS_UNITS[1][1] = new GeneratorCurve[] {genInt};
    GENERATORS_UNITS[1][2] = new GeneratorCurve[] {genInt, genInt, genInt};
    GENERATORS_UNITS[2][0] = new GeneratorCurve[] {genInt, genInt, genInt, genInt, genInt};
    NAMES_UNITS[0] = new String[NB_UNITS[0]][];
    NAMES_UNITS[1] = new String[NB_UNITS[1]][];
    NAMES_UNITS[2] = new String[NB_UNITS[2]][];
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_USD};
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD3_USD};
    NAMES_UNITS[0][2] = new String[] {CURVE_NAME_DSC_EUR, CURVE_NAME_FWD3_EUR};
    NAMES_UNITS[1][0] = new String[] {CURVE_NAME_DSC_USD};
    NAMES_UNITS[1][1] = new String[] {CURVE_NAME_FWD3_USD};
    NAMES_UNITS[1][2] = new String[] {CURVE_NAME_DSC_JPY, CURVE_NAME_FWD3_JPY, CURVE_NAME_FWD6_JPY};
    NAMES_UNITS[2][0] = new String[] {CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD, CURVE_NAME_DSC_JPY, CURVE_NAME_FWD3_JPY, CURVE_NAME_FWD6_JPY};
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
  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> CURVES_PAR_SPREAD_MQ_WITH_TODAY_BLOCK0;
  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> CURVES_PRESENT_VALUE_WITHOUT_TODAY_BLOCK0;
  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK0;
  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK1;
  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK2;
  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_TOTAL;

  private static final double TOLERANCE_PV = 1.0E-10;

  @BeforeSuite
  static void initClass() {
    CURVES_PRESENT_VALUE_WITH_TODAY_BLOCK0 = makeCurves(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PV_CONVERTED_CALCULATOR, PVCS_CONVERTED_CALCULATOR, true, 0);
    CURVES_PAR_SPREAD_MQ_WITH_TODAY_BLOCK0 = makeCurves(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, true, 0);
    CURVES_PRESENT_VALUE_WITHOUT_TODAY_BLOCK0 = makeCurves(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PV_CONVERTED_CALCULATOR, PVCS_CONVERTED_CALCULATOR, false, 0);
    CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK0 = makeCurves(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, false, 0);
    CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK1 = makeCurves(DEFINITIONS_UNITS[1], GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, false, 1);
    CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK2 = makeCurves(DEFINITIONS_UNITS[2], GENERATORS_UNITS[2], NAMES_UNITS[2], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, false, 2);
    YieldCurveBundle ycbTotal = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK0.getFirst().copy();
    ycbTotal.addAll(CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK1.getFirst());
    CurveBuildingBlockBundle cubTotal = new CurveBuildingBlockBundle();
    cubTotal.addAll(CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK0.getSecond());
    cubTotal.addAll(CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK1.getSecond());
    CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_TOTAL = new ObjectsPair<YieldCurveBundle, CurveBuildingBlockBundle>(ycbTotal, cubTotal);
  }

  @Test
  public void curveConstructionGeneratorBlock0() {
    // Curve constructed with present value and today fixing
    curveConstructionTest(NAMES_UNITS[0], DEFINITIONS_UNITS[0], CURVES_PRESENT_VALUE_WITH_TODAY_BLOCK0.getFirst(), true, 0);
    // Curve constructed with par spread (market quote) and  today fixing
    curveConstructionTest(NAMES_UNITS[0], DEFINITIONS_UNITS[0], CURVES_PAR_SPREAD_MQ_WITH_TODAY_BLOCK0.getFirst(), true, 0);
    // Curve constructed with present value and no today fixing
    curveConstructionTest(NAMES_UNITS[0], DEFINITIONS_UNITS[0], CURVES_PRESENT_VALUE_WITHOUT_TODAY_BLOCK0.getFirst(), false, 0);
    // Curve constructed with par spread (market quote) and no today fixing
    curveConstructionTest(NAMES_UNITS[0], DEFINITIONS_UNITS[0], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK0.getFirst(), false, 0);
  }

  @Test
  public void curveConstructionGeneratorAllBlocks() {
    // Curve constructed with par spread (market quote) and  today fixing
    curveConstructionTest(NAMES_UNITS[0], DEFINITIONS_UNITS[0], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_TOTAL.getFirst(), false, 0);
    curveConstructionTest(NAMES_UNITS[1], DEFINITIONS_UNITS[1], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_TOTAL.getFirst(), false, 1);
    curveConstructionTest(NAMES_UNITS[2], DEFINITIONS_UNITS[2], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK2.getFirst(), false, 2);
  }

  @Test(enabled = true)
  public void curveSensitivity() {
    ZonedDateTime settleDate = ScheduleCalculator.getAdjustedDate(NOW, Period.ofMonths(6), GENERATOR_DEPOSIT_JPY);
    SwapXCcyIborIborDefinition swapJpyEurDefinition = SwapXCcyIborIborDefinition.from(settleDate, Period.ofYears(5), JPYLIBOR3MEURIBOR3M, 1000000000, 10000000, -0.0020, true);
    Swap<Payment, Payment> swapJpyEur = swapJpyEurDefinition.toDerivative(NOW, new String[] {CURVE_NAME_DSC_JPY, CURVE_NAME_FWD3_JPY, CURVE_NAME_DSC_EUR, CURVE_NAME_FWD3_EUR});
    ParameterSensitivityBlockCalculator psc = new ParameterSensitivityBlockCalculator(PVCS_CALCULATOR);
    @SuppressWarnings("unused")
    ParameterSensitivity ps = psc.calculateSensitivity(swapJpyEur, new HashSet<String>(), CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_TOTAL.getFirst());
    MarketQuoteSensitivityBlockCalculator mqsc = new MarketQuoteSensitivityBlockCalculator(psc);
    @SuppressWarnings("unused")
    ParameterSensitivity mqs = mqsc.fromInstrument(swapJpyEur, new HashSet<String>(), CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_TOTAL.getFirst(), CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_TOTAL.getSecond());
    int t = 0;
    t++;
  }

  public void curveConstructionTest(String[][] curveNames, final InstrumentDefinition<?>[][][] definitions, final YieldCurveBundle curves, final boolean withToday, int block) {
    int nbBlocks = definitions.length;
    for (int loopblock = 0; loopblock < nbBlocks; loopblock++) {
      InstrumentDerivative[] instruments = convert(curveNames, definitions[loopblock], loopblock, withToday, block);
      double[] pv = new double[instruments.length];
      for (int loopins = 0; loopins < instruments.length; loopins++) {
        pv[loopins] = curves.getFxRates().convert(PV_CALCULATOR.visit(instruments[loopins], curves), CCY_USD).getAmount();
        assertEquals("Curve construction: node block " + loopblock + " - instrument " + loopins, 0, pv[loopins], TOLERANCE_PV);
      }
    }
  }

  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 10;
    @SuppressWarnings("unused")
    Pair<YieldCurveBundle, CurveBuildingBlockBundle> curvePresentValue;
    Pair<YieldCurveBundle, CurveBuildingBlockBundle> curveParSpreadMQ0;
    Pair<YieldCurveBundle, CurveBuildingBlockBundle> curveParSpreadMQ1;
    @SuppressWarnings("unused")
    Pair<YieldCurveBundle, CurveBuildingBlockBundle> curveParSpreadMQ2;
    @SuppressWarnings("unused")
    Pair<YieldCurveBundle, CurveBuildingBlockBundle> curveParSpreadMQT;
    int nbIns0 = DEFINITIONS_DSC_USD.length + DEFINITIONS_FWD3_USD.length + DEFINITIONS_DSC_EUR.length + DEFINITIONS_FWD3_EUR.length;
    int nbIns1 = DEFINITIONS_DSC_USD.length + DEFINITIONS_FWD3_USD.length + DEFINITIONS_DSC_JPY.length + DEFINITIONS_FWD3_JPY.length + DEFINITIONS_FWD6_JPY.length;
    int nbInsT = DEFINITIONS_DSC_USD.length + DEFINITIONS_FWD3_USD.length + DEFINITIONS_DSC_EUR.length + DEFINITIONS_FWD3_EUR.length + DEFINITIONS_DSC_JPY.length + DEFINITIONS_FWD3_JPY.length
        + DEFINITIONS_FWD6_JPY.length;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      curvePresentValue = makeCurves(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PV_CONVERTED_CALCULATOR, PVCS_CONVERTED_CALCULATOR, true, 0);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction Full (4 curves, 2 ccy and " + nbIns0 + " instruments in 3 units) - with present value: " + (endTime - startTime) + " ms");
    // Performance note: curve construction (with present value, 4 curves - 2 ccy and 42 instruments by units): 23-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: xx ms for 100 bundles.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      curveParSpreadMQ0 = makeCurves(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, true, 0);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction Full (4 curves, 2 ccy and " + nbIns0 + " instruments in 3 units)- with par spread-market quote: " + (endTime - startTime) + " ms");
    // Performance note: curve construction (with par spread/market quote), 4 curves - 2 ccy and 49 instruments by units): 23-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: x ms for 100 bundles.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      curveParSpreadMQ1 = makeCurves(DEFINITIONS_UNITS[1], GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, true, 1);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction Full (5 curves, 2 ccy and " + nbIns1 + " instruments in 3 units) - with par spread-market quote: " + (endTime - startTime) + " ms");
    // Performance note: curve construction (with par spread/market quote), 4 curves - 2 ccy and 49 instruments by units): 23-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: x ms for 100 bundles.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      curveParSpreadMQ2 = makeCurves(DEFINITIONS_UNITS[2], GENERATORS_UNITS[2], NAMES_UNITS[2], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, true, 2);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction Full (5 curves, 2 ccy and " + nbIns1 + " instruments in 1 units) - with par spread-market quote: " + (endTime - startTime) + " ms");
    // Performance note: curve construction (with par spread/market quote), 4 curves - 2 ccy and 49 instruments by units): 23-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: x ms for 100 bundles.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      curveParSpreadMQ0 = makeCurves(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, true, 0);
      curveParSpreadMQ1 = makeCurves(DEFINITIONS_UNITS[1], GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, true, 1);
      YieldCurveBundle ycbTotal = curveParSpreadMQ0.getFirst().copy();
      ycbTotal.addAll(curveParSpreadMQ0.getFirst());
      CurveBuildingBlockBundle cubTotal = new CurveBuildingBlockBundle();
      cubTotal.addAll(curveParSpreadMQ1.getSecond());
      cubTotal.addAll(curveParSpreadMQ1.getSecond());
      curveParSpreadMQT = new ObjectsPair<YieldCurveBundle, CurveBuildingBlockBundle>(ycbTotal, cubTotal);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction Full (7 curves, 3 ccy and " + nbInsT + " instruments in 4 units) - with par spread-market quote: " + (endTime - startTime) + " ms");
    // Performance note: curve construction (with par spread/market quote), 7 curves - 3 ccy and 70 instruments by units): 23-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: x ms for 100 bundles.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      curvePresentValue = makeCurves(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PV_CONVERTED_CALCULATOR, PVCS_CONVERTED_CALCULATOR, true, 0);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction Full (4 curves, 2 ccy and " + nbIns0 + " instruments in 3 units) - with present value: " + (endTime - startTime) + " ms");
    // Performance note: curve construction (with present value, 4 curves - 2 ccy and 42 instruments by units): 23-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: xx ms for 100 bundles.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      curveParSpreadMQ0 = makeCurves(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, true, 0);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction Full (4 curves, 2 ccy and " + nbIns0 + " instruments in 3 units)- with par spread-market quote: " + (endTime - startTime) + " ms");
    // Performance note: curve construction (with par spread/market quote), 4 curves - 2 ccy and 49 instruments by units): 23-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: x ms for 100 bundles.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      curveParSpreadMQ1 = makeCurves(DEFINITIONS_UNITS[1], GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, true, 1);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction Full (5 curves, 2 ccy and " + nbIns1 + " instruments in 3 units) - with par spread-market quote: " + (endTime - startTime) + " ms");
    // Performance note: curve construction (with par spread/market quote), 4 curves - 2 ccy and 49 instruments by units): 23-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: x ms for 100 bundles.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      curveParSpreadMQ2 = makeCurves(DEFINITIONS_UNITS[2], GENERATORS_UNITS[2], NAMES_UNITS[2], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, true, 2);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction Full (5 curves, 2 ccy and " + nbIns1 + " instruments in 1 units) - with par spread-market quote: " + (endTime - startTime) + " ms");
    // Performance note: curve construction (with par spread/market quote), 4 curves - 2 ccy and 49 instruments by units): 23-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: x ms for 100 bundles.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      curveParSpreadMQ0 = makeCurves(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, true, 0);
      curveParSpreadMQ1 = makeCurves(DEFINITIONS_UNITS[1], GENERATORS_UNITS[1], NAMES_UNITS[1], KNOWN_DATA, PSMQ_CALCULATOR, PSMQCS_CALCULATOR, true, 1);
      YieldCurveBundle ycbTotal = curveParSpreadMQ0.getFirst().copy();
      ycbTotal.addAll(curveParSpreadMQ0.getFirst());
      CurveBuildingBlockBundle cubTotal = new CurveBuildingBlockBundle();
      cubTotal.addAll(curveParSpreadMQ1.getSecond());
      cubTotal.addAll(curveParSpreadMQ1.getSecond());
      curveParSpreadMQT = new ObjectsPair<YieldCurveBundle, CurveBuildingBlockBundle>(ycbTotal, cubTotal);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction Full (7 curves, 3 ccy and " + nbIns1 + " instruments in 4 units) - with par spread-market quote: " + (endTime - startTime) + " ms");
    // Performance note: curve construction (with par spread/market quote), 7 curves - 3 ccy and 70 instruments by units): 23-Jul-2012: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: x ms for 100 bundles.

  }

  public static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, final GeneratorInstrument[] generators, final Period[] tenors) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int loopmv = 0; loopmv < marketQuotes.length; loopmv++) {
      definitions[loopmv] = generators[loopmv].generateInstrument(NOW, tenors[loopmv], marketQuotes[loopmv], NOTIONAL, FX_MATRIX);
    }
    return definitions;
  }

  private static Pair<YieldCurveBundle, Double[]> makeUnit(InstrumentDerivative[] instruments, double[] initGuess, LinkedHashMap<String, GeneratorCurve> curveGenerators, YieldCurveBundle knownData,
      final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator, final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> sensitivityCalculator) {
    final MultipleYieldCurveFinderGeneratorDataBundle data = new MultipleYieldCurveFinderGeneratorDataBundle(instruments, knownData, curveGenerators);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MultipleYieldCurveFinderGeneratorFunction(calculator, data);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderGeneratorJacobian(new ParameterSensitivityCalculator(sensitivityCalculator), data);
    final double[] parameters = ROOT_FINDER.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initGuess)).getData();
    final YieldCurveBundle newCurves = data.getBuildingFunction().evaluate(new DoubleMatrix1D(parameters));
    return new ObjectsPair<YieldCurveBundle, Double[]>(newCurves, ArrayUtils.toObject(parameters));
  }

  private static DoubleMatrix2D[] makeCurveMatrix(InstrumentDerivative[] instrumentsTotal, LinkedHashMap<String, GeneratorCurve> curveGeneratorsTotal, int startBlock, int[] nbParameters,
      Double[] parametersTotal, YieldCurveBundle knownData, final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> sensitivityCalculator) {
    final MultipleYieldCurveFinderGeneratorDataBundle data = new MultipleYieldCurveFinderGeneratorDataBundle(instrumentsTotal, knownData, curveGeneratorsTotal);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MultipleYieldCurveFinderGeneratorJacobian(new ParameterSensitivityCalculator(sensitivityCalculator), data);
    final DoubleMatrix2D jacobian = jacobianCalculator.evaluate(new DoubleMatrix1D(parametersTotal));
    final DoubleMatrix2D inverseJacobian = MATRIX_ALGEBRA.getInverse(jacobian);
    double[][] matrixTotal = inverseJacobian.getData();
    DoubleMatrix2D[] result = new DoubleMatrix2D[nbParameters.length];
    int startCurve = 0;
    for (int loopmat = 0; loopmat < nbParameters.length; loopmat++) {
      double[][] matrixCurve = new double[nbParameters[loopmat]][matrixTotal.length];
      for (int loopparam = 0; loopparam < nbParameters[loopmat]; loopparam++) {
        matrixCurve[loopparam] = matrixTotal[startBlock + startCurve + loopparam].clone();
      }
      result[loopmat] = new DoubleMatrix2D(matrixCurve);
      startCurve += nbParameters[loopmat];
    }
    return result;
  }

  private static Pair<YieldCurveBundle, CurveBuildingBlockBundle> makeCurves(final InstrumentDefinition<?>[][][] definitions, GeneratorCurve[][] curveGenerators, String[][] curveNames,
      YieldCurveBundle knownData, final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator,
      final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> sensitivityCalculator, boolean withToday, int block) {
    int nbBlocks = curveGenerators.length;
    YieldCurveBundle knownSoFarData = knownData.copy();
    List<InstrumentDerivative> instrumentsSoFar = new ArrayList<InstrumentDerivative>();
    LinkedHashMap<String, GeneratorCurve> generatorsSoFar = new LinkedHashMap<String, GeneratorCurve>();
    LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> unitBundleSoFar = new LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>>();
    List<Double> parametersSoFar = new ArrayList<Double>();
    LinkedHashMap<String, Pair<Integer, Integer>> unitMap = new LinkedHashMap<String, Pair<Integer, Integer>>();
    int start = 0;
    for (int loopunit = 0; loopunit < nbBlocks; loopunit++) {
      int startBlock = 0;
      InstrumentDerivative[] instruments = convert(curveNames, definitions[loopunit], loopunit, withToday, block);
      instrumentsSoFar.addAll(Arrays.asList(instruments));
      InstrumentDerivative[] instrumentsSoFarArray = instrumentsSoFar.toArray(new InstrumentDerivative[0]);
      double[] initGuess = initialGuess(definitions[loopunit]);
      LinkedHashMap<String, GeneratorCurve> gen = new LinkedHashMap<String, GeneratorCurve>();
      int[] nbIns = new int[curveGenerators[loopunit].length];
      for (int loopcurve = 0; loopcurve < curveGenerators[loopunit].length; loopcurve++) {
        nbIns[loopcurve] = definitions[loopunit][loopcurve].length;
        InstrumentDerivative[] insCurve = new InstrumentDerivative[nbIns[loopcurve]];
        System.arraycopy(instruments, startBlock, insCurve, 0, nbIns[loopcurve]);
        GeneratorCurve tmp = curveGenerators[loopunit][loopcurve].finalGenerator(insCurve);
        gen.put(curveNames[loopunit][loopcurve], tmp);
        generatorsSoFar.put(curveNames[loopunit][loopcurve], tmp);
        unitMap.put(curveNames[loopunit][loopcurve], new ObjectsPair<Integer, Integer>(start + startBlock, nbIns[loopcurve]));
        startBlock += nbIns[loopcurve];
      }
      Pair<YieldCurveBundle, Double[]> unitCal = makeUnit(instruments, initGuess, gen, knownSoFarData, calculator, sensitivityCalculator);
      parametersSoFar.addAll(Arrays.asList(unitCal.getSecond()));
      DoubleMatrix2D[] mat = makeCurveMatrix(instrumentsSoFarArray, generatorsSoFar, start, nbIns, parametersSoFar.toArray(new Double[0]), knownData, sensitivityCalculator);
      for (int loopcurve = 0; loopcurve < curveGenerators[loopunit].length; loopcurve++) {
        unitBundleSoFar.put(curveNames[loopunit][loopcurve], new ObjectsPair<CurveBuildingBlock, DoubleMatrix2D>(new CurveBuildingBlock(unitMap), mat[loopcurve]));
      }
      knownSoFarData.addAll(unitCal.getFirst());
      start = start + startBlock;
    }
    return new ObjectsPair<YieldCurveBundle, CurveBuildingBlockBundle>(knownSoFarData, new CurveBuildingBlockBundle(unitBundleSoFar));
  }

  @SuppressWarnings("unchecked")
  private static InstrumentDerivative[] convert(String[][] curveNames, InstrumentDefinition<?>[][] definitions, int unit, boolean withToday, int block) {
    int nbDef = 0;
    for (int loopdef1 = 0; loopdef1 < definitions.length; loopdef1++) {
      nbDef += definitions[loopdef1].length;
    }
    final InstrumentDerivative[] instruments = new InstrumentDerivative[nbDef];
    int loopins = 0;
    for (int loopcurve = 0; loopcurve < definitions.length; loopcurve++) {
      for (final InstrumentDefinition<?> instrument : definitions[loopcurve]) {
        InstrumentDerivative ird;
        if (instrument instanceof SwapFixedONDefinition) {
          final String[] names = getCurvesNameSwapFixedON(curveNames, unit);
          ird = ((SwapFixedONDefinition) instrument).toDerivative(NOW, getTSSwapFixedON(withToday, unit), names);
        } else {
          if (instrument instanceof SwapFixedIborDefinition) {
            final String[] names = getCurvesNameSwapFixedIbor(curveNames, unit, loopcurve, block);
            ird = ((SwapFixedIborDefinition) instrument).toDerivative(NOW, getTSSwapFixedIbor(withToday, unit), names);
          } else {
            if (instrument instanceof SwapXCcyIborIborDefinition) {
              final String[] names = getCurvesNameSwapXCcyIborIbor(curveNames, block);
              ird = ((SwapXCcyIborIborDefinition) instrument).toDerivative(NOW, getTSSwapXCcyIborIbor(withToday, unit), names);
            } else {
              if (instrument instanceof SwapIborIborDefinition) {
                final String[] names = getCurvesNameSwapIborIbor(curveNames, unit, block);
                ird = ((SwapIborIborDefinition) instrument).toDerivative(NOW, getTSSwapIborIbor(withToday, unit), names);
              } else {
                final String[] names;
                if (instrument instanceof ForexSwapDefinition) {
                  names = getCurvesNameFXSwap(curveNames, block);
                } else {
                  // Cash
                  names = getCurvesNameCash(curveNames, unit, loopcurve, block);
                }
                ird = instrument.toDerivative(NOW, names);
              }
            }
          }
        }
        instruments[loopins++] = ird;
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

  private static double[] initialGuess(InstrumentDefinition<?>[][] definitions) {
    int nbDef = 0;
    for (int loopdef1 = 0; loopdef1 < definitions.length; loopdef1++) {
      nbDef += definitions[loopdef1].length;
    }
    double[] result = new double[nbDef];
    int loopr = 0;
    for (int loopcurve = 0; loopcurve < definitions.length; loopcurve++) {
      for (int loopins = 0; loopins < definitions[loopcurve].length; loopins++) {
        result[loopr++] = initialGuess(definitions[loopcurve][loopins]);
      }
    }
    return result;
  }

  private static String[] getCurvesNameSwapFixedON(String[][] curveNames, Integer unit) {
    switch (unit) {
      case 0:
        return new String[] {curveNames[0][0], curveNames[0][0]};
      case 2:
        return new String[] {curveNames[2][0], curveNames[2][0]};
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

  private static String[] getCurvesNameSwapFixedIbor(String[][] curveNames, Integer unit, Integer curve, int block) {
    if (block != 2) {
      switch (unit) {
        case 1:
          return new String[] {curveNames[0][0], curveNames[1][0]};
        case 2:
          return new String[] {curveNames[2][0], curveNames[2][1]};
        default:
          throw new IllegalArgumentException(unit.toString());
      }
    }
    switch (curve) {
      case 1:
        return new String[] {curveNames[0][0], curveNames[0][1]};
      case 4:
        return new String[] {curveNames[0][2], curveNames[0][4]};
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

  private static String[] getCurvesNameSwapXCcyIborIbor(String[][] curveNames, int block) {
    switch (block) {
      case 2:
        return new String[] {curveNames[0][2], curveNames[0][3], curveNames[0][0], curveNames[0][1]};
      default:
        return new String[] {curveNames[2][0], curveNames[2][1], curveNames[0][0], curveNames[1][0]};
    }
  }

  private static String[] getCurvesNameSwapIborIbor(String[][] curveNames, Integer unit, int block) {
    if (block != 2) {
      switch (unit) {
        case 2:
          return new String[] {curveNames[2][0], curveNames[2][1], curveNames[2][2]};
        default:
          throw new IllegalArgumentException(unit.toString());
      }
    }
    return new String[] {curveNames[0][2], curveNames[0][3], curveNames[0][4]};
  }

  private static String[] getCurvesNameCash(String[][] curveNames, Integer unit, Integer curve, int block) {
    if (block != 2) {
      switch (unit) {
        case 0:
          return new String[] {curveNames[0][0]};
        case 1:
          return new String[] {curveNames[1][0]};
        case 2:
          return new String[] {curveNames[2][curve]};
        default:
          throw new IllegalArgumentException(unit.toString());
      }
    }
    return new String[] {curveNames[0][curve]};

  }

  private static String[] getCurvesNameFXSwap(String[][] curveNames, int block) {
    switch (block) {
      case 2:
        return new String[] {curveNames[0][2], curveNames[0][0]};
      default:
        return new String[] {curveNames[2][0], curveNames[0][0]};
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
        return TS_FIXED_IBOR_USD3M_WITHOUT_TODAY;
      case 1:
        return withToday ? TS_FIXED_IBOR_USD3M_WITH_TODAY : TS_FIXED_IBOR_USD3M_WITHOUT_TODAY;
      case 2:
        return withToday ? TS_FIXED_IBOR_EUR3M_WITH_TODAY : TS_FIXED_IBOR_EUR3M_WITHOUT_TODAY;
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

  @SuppressWarnings("rawtypes")
  private static DoubleTimeSeries[] getTSSwapXCcyIborIbor(Boolean withToday, Integer unit) {
    switch (unit) {
      case 0:
        return TS_FIXED_IBOR_EURUSD3M_WITHOUT_TODAY;
      case 2:
        return withToday ? TS_FIXED_IBOR_EURUSD3M_WITH_TODAY : TS_FIXED_IBOR_EURUSD3M_WITHOUT_TODAY;
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

  @SuppressWarnings("rawtypes")
  private static DoubleTimeSeries[] getTSSwapIborIbor(Boolean withToday, Integer unit) {
    switch (unit) {
      case 0:
        return TS_FIXED_IBOR_JPY3MJPY6M_WITHOUT_TODAY;
      case 2:
        return withToday ? TS_FIXED_IBOR_JPY3MJPY6M_WITH_TODAY : TS_FIXED_IBOR_JPY3MJPY6M_WITHOUT_TODAY;
      default:
        throw new IllegalArgumentException(unit.toString());
    }
  }

}
