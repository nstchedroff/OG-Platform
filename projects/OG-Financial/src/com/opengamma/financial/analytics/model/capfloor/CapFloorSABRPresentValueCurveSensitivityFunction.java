/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.capfloor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivitySABRCalculator;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivitySABRExtrapolationCalculator;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class CapFloorSABRPresentValueCurveSensitivityFunction extends CapFloorSABRFunction {
  private final PresentValueCurveSensitivityCalculator _calculator;

  public CapFloorSABRPresentValueCurveSensitivityFunction(final String currency, final String definitionName, final String useSABRExtrapolation, String forwardCurveName, String fundingCurveName) {
    this(Currency.of(currency), definitionName, Boolean.parseBoolean(useSABRExtrapolation), forwardCurveName, fundingCurveName);
  }

  public CapFloorSABRPresentValueCurveSensitivityFunction(final Currency currency, final String definitionName, final boolean useSABRExtrapolation, String forwardCurveName, String fundingCurveName) {
    super(currency, definitionName, useSABRExtrapolation, forwardCurveName, fundingCurveName);
    _calculator = useSABRExtrapolation ? PresentValueCurveSensitivitySABRExtrapolationCalculator.getInstance() : PresentValueCurveSensitivitySABRCalculator.getInstance();
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final CapFloorSecurity capFloorSecurity = (CapFloorSecurity) target.getSecurity();
    final FixedIncomeInstrumentDefinition<?> capFloorDefinition = capFloorSecurity.accept(getVisitor());
    final SABRInterestRateDataBundle data = new SABRInterestRateDataBundle(getModelParameters(target, inputs), getYieldCurves(target, inputs));
    final InterestRateDerivative capFloor = capFloorDefinition.toDerivative(now, getFundingCurveName(), getForwardCurveName());
    final Map<String, List<DoublesPair>> presentValueCurveSensitivity = _calculator.visit(capFloor, data);
    final InterestRateCurveSensitivity result = new InterestRateCurveSensitivity(presentValueCurveSensitivity);
    return Collections.singleton(new ComputedValue(getSpecification(target), result));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(getSpecification(target));
  }

  private ValueSpecification getSpecification(final ComputationTarget target) {
    return new ValueSpecification(ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY, target.toSpecification(),
        createValueProperties()
            .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode())
            .with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, getForwardCurveName())
            .with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, getFundingCurveName())
            .with(ValuePropertyNames.CUBE, getHelper().getDefinitionName()).get());
  }
}
