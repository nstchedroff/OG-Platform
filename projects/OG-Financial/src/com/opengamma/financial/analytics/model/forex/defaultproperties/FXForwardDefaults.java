/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.defaultproperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.forex.forward.FXForwardFunction;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class FXForwardDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardDefaults.class);
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.FX_PRESENT_VALUE,
    ValueRequirementNames.FX_CURRENCY_EXPOSURE,
    ValueRequirementNames.FX_CURVE_SENSITIVITIES,
    ValueRequirementNames.PV01,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.VALUE_THETA
  };
  private final PriorityClass _priority;
  private final Map<String, Pair<String, String>> _currencyCurveConfigAndDiscountingCurveNames;

  public FXForwardDefaults(final String priority, final String... currencyCurveConfigAndDiscountingCurveNames) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(currencyCurveConfigAndDiscountingCurveNames, "currency and curve config names");
    final int nPairs = currencyCurveConfigAndDiscountingCurveNames.length;
    ArgumentChecker.isTrue(nPairs % 3 == 0, "Must have one curve config and discounting curve name per currency");
    _priority = PriorityClass.valueOf(priority);
    _currencyCurveConfigAndDiscountingCurveNames = new HashMap<String, Pair<String, String>>();
    for (int i = 0; i < currencyCurveConfigAndDiscountingCurveNames.length; i += 3) {
      final Pair<String, String> pair = Pair.of(currencyCurveConfigAndDiscountingCurveNames[i + 1], currencyCurveConfigAndDiscountingCurveNames[i + 2]);
      _currencyCurveConfigAndDiscountingCurveNames.put(currencyCurveConfigAndDiscountingCurveNames[i], pair);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security security = target.getSecurity();
    if (!(security instanceof FinancialSecurity)) {
      return false;
    }
    if (!(security instanceof FXForwardSecurity || security instanceof NonDeliverableFXForwardSecurity)) {
      return false;
    }
    final FinancialSecurity fxSecurity = (FinancialSecurity) security;
    final String payCurrency = fxSecurity.accept(ForexVisitors.getPayCurrencyVisitor()).getCode();
    final String receiveCurrency = fxSecurity.accept(ForexVisitors.getReceiveCurrencyVisitor()).getCode();
    return _currencyCurveConfigAndDiscountingCurveNames.containsKey(payCurrency) && _currencyCurveConfigAndDiscountingCurveNames.containsKey(receiveCurrency);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.PAY_CURVE);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.RECEIVE_CURVE);
      defaults.addValuePropertyName(valueRequirement, FXForwardFunction.PAY_CURVE_CALC_CONFIG);
      defaults.addValuePropertyName(valueRequirement, FXForwardFunction.RECEIVE_CURVE_CALC_CONFIG);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final String payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor()).getCode();
    final String receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor()).getCode();
    if (!_currencyCurveConfigAndDiscountingCurveNames.containsKey(payCurrency)) {
      s_logger.error("Could not get config for pay currency " + payCurrency + "; should never happen");
      return null;
    }
    if (!_currencyCurveConfigAndDiscountingCurveNames.containsKey(receiveCurrency)) {
      s_logger.error("Could not get config for receive currency " + receiveCurrency + "; should never happen");
      return null;
    }
    final Pair<String, String> payPair = _currencyCurveConfigAndDiscountingCurveNames.get(payCurrency);
    final Pair<String, String> receivePair = _currencyCurveConfigAndDiscountingCurveNames.get(receiveCurrency);
    if (ValuePropertyNames.PAY_CURVE.equals(propertyName)) {
      return Collections.singleton(payPair.getSecond());
    }
    if (ValuePropertyNames.RECEIVE_CURVE.equals(propertyName)) {
      return Collections.singleton(receivePair.getSecond());
    }
    if (FXForwardFunction.PAY_CURVE_CALC_CONFIG.equals(propertyName)) {
      return Collections.singleton(payPair.getFirst());
    }
    if (FXForwardFunction.RECEIVE_CURVE_CALC_CONFIG.equals(propertyName)) {
      return Collections.singleton(receivePair.getFirst());
    }
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.FX_FORWARD_DEFAULTS;
  }
}
