/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.PresentValueCurveSensitivityBlackSmileForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueCurveSensitivityBlackTermStructureForexCalculator;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackForexTermStructureBundle;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class FXOneLookBarrierOptionBlackCurveSensitivityFunction extends FXOneLookBarrierOptionBlackFunction {

  public FXOneLookBarrierOptionBlackCurveSensitivityFunction() {
    super(ValueRequirementNames.FX_CURVE_SENSITIVITIES);
  }

  private static final PresentValueCurveSensitivityBlackSmileForexCalculator SMILE_CALCULATOR = PresentValueCurveSensitivityBlackSmileForexCalculator.getInstance();
  private static final PresentValueCurveSensitivityBlackTermStructureForexCalculator FLAT_CALCULATOR = PresentValueCurveSensitivityBlackTermStructureForexCalculator.getInstance();

  @Override
  protected Object computeValues(Set<ForexOptionVanilla> vanillaOptions, ForexOptionDataBundle<?> market) {
    MultipleCurrencyInterestRateCurveSensitivity sum = new MultipleCurrencyInterestRateCurveSensitivity();
    for (ForexOptionVanilla derivative : vanillaOptions) {
      if (market instanceof YieldCurveWithBlackForexTermStructureBundle) {
        final MultipleCurrencyInterestRateCurveSensitivity result = FLAT_CALCULATOR.visit(derivative, market);
        ArgumentChecker.isTrue(result.getCurrencies().size() == 1, "Only one currency");
        sum.plus(result);
      }
      final MultipleCurrencyInterestRateCurveSensitivity result = SMILE_CALCULATOR.visit(derivative, market);
      ArgumentChecker.isTrue(result.getCurrencies().size() == 1, "Only one currency");
      sum.plus(result);
    }
    return sum;
  }
}

