/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

import java.util.Set;

import com.opengamma.analytics.financial.equity.EquityOptionDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionBlackMethod;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class EquityIndexVanillaBarrierOptionVegaFunction extends EquityIndexVanillaBarrierOptionFunction {

  public EquityIndexVanillaBarrierOptionVegaFunction() {
    super(ValueRequirementNames.VALUE_VEGA);
  }

  @Override
  protected Object computeValues(Set<EquityIndexOption> vanillaOptions, EquityOptionDataBundle market, Currency currency) {
    EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
    double sum = 0.0;
    for (EquityIndexOption derivative : vanillaOptions) {
      sum += model.vega(derivative, market);
    }
    return sum;
  }

}