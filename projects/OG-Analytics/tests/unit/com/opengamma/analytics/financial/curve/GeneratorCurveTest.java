/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.curve.generator.GeneratorCurve;
import com.opengamma.analytics.financial.curve.generator.GeneratorCurveAddYield;
import com.opengamma.analytics.financial.curve.generator.GeneratorCurveAddYieldExisiting;
import com.opengamma.analytics.financial.curve.generator.GeneratorCurveYieldConstant;
import com.opengamma.analytics.financial.curve.generator.GeneratorCurveYieldInterpolatedAnchorNode;
import com.opengamma.analytics.financial.curve.generator.GeneratorCurveYieldInterpolatedNode;
import com.opengamma.analytics.financial.curve.generator.GeneratorCurveYieldNelsonSiegel;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroSpreadCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.DoublesCurveNelsonSiegel;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;

/**
 * Tests the different curve generators types.
 */
public class GeneratorCurveTest {

  private static final String CURVE_NAME_1 = "EUR Discounting";
  private static final String CURVE_NAME_2 = "EUR Discounting-Spread";

  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final double[] NODES = new double[] {0.01, 0.50, 1.00, 2.00, 5.05, 10.0};
  private static final double[] YIELD = new double[] {0.02, 0.02, 0.03, 0.01, 0.02, 0.01};
  private static final double ANCHOR = 1.5;
  private static final GeneratorCurveYieldInterpolatedNode GENERATOR_YIELD_INTERPOLATED_NODE = new GeneratorCurveYieldInterpolatedNode(NODES, LINEAR_FLAT);
  private static final GeneratorCurveYieldInterpolatedAnchorNode GENERATOR_YIELD_INTERPOLATED_ANCHOR_NODE = new GeneratorCurveYieldInterpolatedAnchorNode(NODES, ANCHOR, LINEAR_FLAT);

  private static final double CST = 0.0050;
  private static final GeneratorCurveYieldConstant GENERATOR_YIELD_CONSTANT = new GeneratorCurveYieldConstant();

  private static final GeneratorCurveAddYield GENERATOR_SPREAD = new GeneratorCurveAddYield(new GeneratorCurve[] {GENERATOR_YIELD_INTERPOLATED_NODE, GENERATOR_YIELD_CONSTANT}, false);

  private static final GeneratorCurveAddYieldExisiting GENERATOR_EXISTING = new GeneratorCurveAddYieldExisiting(GENERATOR_YIELD_CONSTANT, true, CURVE_NAME_1);

  private static final double BETA0 = 0.03;
  private static final double BETA1 = -0.02;
  private static final double BETA2 = 0.06;
  private static final double LAMBDA = 2.0;
  private static final double[] PARAMETERS_NS = new double[] {BETA0, BETA1, BETA2, LAMBDA};
  private static final GeneratorCurveYieldNelsonSiegel GENERATOR_NS = new GeneratorCurveYieldNelsonSiegel();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullYieldNodes() {
    new GeneratorCurveYieldInterpolatedNode(null, LINEAR_FLAT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullYieldInterpolator() {
    new GeneratorCurveYieldInterpolatedNode(NODES, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullYieldInterpolatorZero() {
    new GeneratorCurveYieldInterpolatedAnchorNode(NODES, ANCHOR, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSpreadGen() {
    new GeneratorCurveAddYield(null, false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSpreadExGen() {
    new GeneratorCurveAddYieldExisiting(null, true, CURVE_NAME_1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSpreadExName() {
    new GeneratorCurveAddYieldExisiting(GENERATOR_YIELD_CONSTANT, true, null);
  }

  @Test
  public void getterYieldInterpolated() {
    assertEquals(NODES.length, GENERATOR_YIELD_INTERPOLATED_NODE.getNumberOfParameter());
  }

  @Test
  public void getterYieldInterpolatedZero() {
    assertEquals(NODES.length, GENERATOR_YIELD_INTERPOLATED_ANCHOR_NODE.getNumberOfParameter());
  }

  @Test
  public void generateCurveYieldInterpolated() {
    YieldAndDiscountCurve curveGenerated = GENERATOR_YIELD_INTERPOLATED_NODE.generateCurve(CURVE_NAME_1, YIELD);
    YieldAndDiscountCurve curveExpected = new YieldCurve(CURVE_NAME_1, new InterpolatedDoublesCurve(NODES, YIELD, LINEAR_FLAT, true, CURVE_NAME_1));
    assertEquals("GeneratorCurveYieldInterpolated: generate curve", curveExpected, curveGenerated);
  }

  @Test
  public void generateCurveYieldInterpolatedAnchor() {
    YieldAndDiscountCurve curveGenerated = GENERATOR_YIELD_INTERPOLATED_ANCHOR_NODE.generateCurve(CURVE_NAME_1, YIELD);
    double[] nodex = new double[NODES.length + 1];
    System.arraycopy(NODES, 0, nodex, 0, 3);
    nodex[3] = ANCHOR;
    System.arraycopy(NODES, 3, nodex, 4, 3);
    double[] yieldx = new double[NODES.length + 1];
    System.arraycopy(YIELD, 0, yieldx, 0, 3);
    yieldx[3] = 0.0;
    System.arraycopy(YIELD, 3, yieldx, 4, 3);
    YieldAndDiscountCurve curveExpected = new YieldCurve(CURVE_NAME_1, new InterpolatedDoublesCurve(nodex, yieldx, LINEAR_FLAT, true, CURVE_NAME_1));
    assertEquals("GeneratorCurveYieldInterpolated: generate curve", curveExpected.getNumberOfParameters() - 1, curveGenerated.getNumberOfParameters());
    assertArrayEquals("GeneratorCurveYieldInterpolated: generate curve", ((YieldCurve) curveExpected).getCurve().getXData(), ((YieldCurve) curveGenerated).getCurve().getXData());
    assertArrayEquals("GeneratorCurveYieldInterpolated: generate curve", ((YieldCurve) curveExpected).getCurve().getYData(), ((YieldCurve) curveGenerated).getCurve().getYData());
  }

  @Test
  public void generateCurveYieldConstant() {
    YieldAndDiscountCurve curveGenerated = GENERATOR_YIELD_CONSTANT.generateCurve(CURVE_NAME_1, new double[] {CST});
    YieldAndDiscountCurve curveExpected = new YieldCurve(CURVE_NAME_1, new ConstantDoublesCurve(CST, CURVE_NAME_1));
    assertEquals("GeneratorCurveYieldConstant: generate curve", curveExpected, curveGenerated);
  }

  @Test
  public void generateCurveYieldSpread1() {
    double[] x = new double[YIELD.length + 1];
    System.arraycopy(YIELD, 0, x, 0, YIELD.length);
    x[YIELD.length] = CST;
    YieldAndDiscountCurve curveGenerated = GENERATOR_SPREAD.generateCurve(CURVE_NAME_1, x);
    YieldAndDiscountCurve curveExpected0 = new YieldCurve(CURVE_NAME_1 + "-0", new InterpolatedDoublesCurve(NODES, YIELD, LINEAR_FLAT, true, CURVE_NAME_1 + "-0"));
    YieldAndDiscountCurve curveExpected1 = new YieldCurve(CURVE_NAME_1 + "-1", new ConstantDoublesCurve(CST, CURVE_NAME_1 + "-1"));
    YieldAndDiscountCurve curveExpected = new YieldAndDiscountAddZeroSpreadCurve(CURVE_NAME_1, false, curveExpected0, curveExpected1);
    assertEquals("GeneratorCurveYieldConstant: generate curve", curveExpected, curveGenerated);
  }

  @Test
  /**
   * The the curve generator with yield spread with two levels of spread.
   */
  public void generateCurveYieldSpread2() {
    GeneratorCurveAddYield generatorSpread1 = new GeneratorCurveAddYield(new GeneratorCurve[] {GENERATOR_YIELD_INTERPOLATED_NODE, GENERATOR_YIELD_CONSTANT}, false);
    GeneratorCurveAddYield generatorSpread2 = new GeneratorCurveAddYield(new GeneratorCurve[] {generatorSpread1, GENERATOR_YIELD_CONSTANT}, false);
    double[] x = new double[YIELD.length + 2];
    System.arraycopy(YIELD, 0, x, 0, YIELD.length);
    x[YIELD.length] = CST;
    x[YIELD.length + 1] = CST;
    YieldAndDiscountCurve curveGenerated = generatorSpread2.generateCurve(CURVE_NAME_1, x);
    YieldAndDiscountCurve curveExpected00 = new YieldCurve(CURVE_NAME_1 + "-0-0", new InterpolatedDoublesCurve(NODES, YIELD, LINEAR_FLAT, true, CURVE_NAME_1 + "-0-0"));
    YieldAndDiscountCurve curveExpected01 = new YieldCurve(CURVE_NAME_1 + "-0-1", new ConstantDoublesCurve(CST, CURVE_NAME_1 + "-0-1"));
    YieldAndDiscountCurve curveExpected0 = new YieldAndDiscountAddZeroSpreadCurve(CURVE_NAME_1 + "-0", false, curveExpected00, curveExpected01);
    YieldAndDiscountCurve curveExpected1 = new YieldCurve(CURVE_NAME_1 + "-1", new ConstantDoublesCurve(CST, CURVE_NAME_1 + "-1"));
    YieldAndDiscountCurve curveExpected = new YieldAndDiscountAddZeroSpreadCurve(CURVE_NAME_1, false, curveExpected0, curveExpected1);
    assertEquals("GeneratorCurveYieldConstant: generate curve", curveExpected, curveGenerated);
  }

  @Test
  public void generateCurveYieldSpread3() {
    double[] x = new double[2 * YIELD.length];
    System.arraycopy(YIELD, 0, x, 0, YIELD.length);
    System.arraycopy(YIELD, 0, x, YIELD.length, YIELD.length);
    GeneratorCurveAddYield generatorIntMinusInt = new GeneratorCurveAddYield(new GeneratorCurve[] {GENERATOR_YIELD_INTERPOLATED_NODE, GENERATOR_YIELD_INTERPOLATED_NODE}, true);
    YieldAndDiscountCurve curveGenerated = generatorIntMinusInt.generateCurve(CURVE_NAME_1, x);
    YieldAndDiscountCurve curveExpected0 = new YieldCurve(CURVE_NAME_1 + "-0", new InterpolatedDoublesCurve(NODES, YIELD, LINEAR_FLAT, true, CURVE_NAME_1 + "-0"));
    YieldAndDiscountCurve curveExpected1 = new YieldCurve(CURVE_NAME_1 + "-1", new InterpolatedDoublesCurve(NODES, YIELD, LINEAR_FLAT, true, CURVE_NAME_1 + "-1"));
    YieldAndDiscountCurve curveExpected = new YieldAndDiscountAddZeroSpreadCurve(CURVE_NAME_1, true, curveExpected0, curveExpected1);
    assertEquals("GeneratorCurveYieldConstant: generate curve", curveExpected, curveGenerated);
  }

  @Test
  public void generateCurveYieldSpreadExisting() {
    YieldAndDiscountCurve curveExisting = new YieldCurve(CURVE_NAME_1, new InterpolatedDoublesCurve(NODES, YIELD, LINEAR_FLAT, true, CURVE_NAME_1));
    YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve(CURVE_NAME_1, curveExisting);
    YieldAndDiscountCurve curveGenerated = GENERATOR_EXISTING.generateCurve(CURVE_NAME_2, bundle, new double[] {CST});
    YieldAndDiscountCurve curveExpected0 = new YieldCurve(CURVE_NAME_2 + "-0", new ConstantDoublesCurve(CST, CURVE_NAME_2 + "-0"));
    YieldAndDiscountCurve curveExpected = new YieldAndDiscountAddZeroSpreadCurve(CURVE_NAME_2, true, curveExisting, curveExpected0);
    assertEquals("GeneratorCurveYieldConstant: generate curve", curveExpected, curveGenerated);
  }

  @Test
  public void generateCurveYieldNelsonSiegel() {
    YieldAndDiscountCurve curveGenerated = GENERATOR_NS.generateCurve(CURVE_NAME_1, PARAMETERS_NS);
    YieldAndDiscountCurve curveExpected = new YieldCurve(CURVE_NAME_1, new DoublesCurveNelsonSiegel(CURVE_NAME_1, PARAMETERS_NS));
    assertEquals("GeneratorCurveYieldNelsonSiegel: generate curve", curveExpected, curveGenerated);

  }

}
