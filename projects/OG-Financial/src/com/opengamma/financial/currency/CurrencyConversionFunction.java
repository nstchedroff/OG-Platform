/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Converts a value from one currency to another, preserving all other properties.
 */
public class CurrencyConversionFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * Default value for {@code _rateLookupValueName}.
   */
  public static final String DEFAULT_LOOKUP_VALUE_NAME = "CurrencyConversion";

  /**
   * Default value for {@code _rateLookupIdentifierScheme}.
   */
  public static final String DEFAULT_LOOKUP_IDENTIFIER_SCHEME = "CurrencyPair";

  private static final String DEFAULT_CURRENCY_INJECTION = ValuePropertyNames.OUTPUT_RESERVED_PREFIX + "Currency";

  private static final Logger s_logger = LoggerFactory.getLogger(CurrencyConversionFunction.class);

  private final ComputationTargetType _targetType;
  private final Set<String> _valueNames;
  private boolean _allowViewDefaultCurrency; // = false;
  private String _rateLookupValueName = DEFAULT_LOOKUP_VALUE_NAME;
  private String _rateLookupIdentifierScheme = DEFAULT_LOOKUP_IDENTIFIER_SCHEME;

  public CurrencyConversionFunction(final ComputationTargetType targetType, final String valueName) {
    ArgumentChecker.notNull(targetType, "targetType");
    ArgumentChecker.notNull(valueName, "valueName");
    _targetType = targetType;
    _valueNames = Collections.singleton(valueName);
  }

  public CurrencyConversionFunction(final ComputationTargetType targetType, final String... valueNames) {
    ArgumentChecker.notNull(targetType, "targetType");
    ArgumentChecker.notEmpty(valueNames, "valueNames");
    _targetType = targetType;
    _valueNames = new HashSet<String>(Arrays.asList(valueNames));
  }

  public void setRateLookupValueName(final String rateLookupValueName) {
    ArgumentChecker.notNull(rateLookupValueName, "rateLookupValueName");
    _rateLookupValueName = rateLookupValueName;
  }

  public String getRateLookupValueName() {
    return _rateLookupValueName;
  }

  public void setRateLookupIdentifierScheme(final String rateLookupIdentifierScheme) {
    ArgumentChecker.notNull(rateLookupIdentifierScheme, "rateLookupIdentifierScheme");
    _rateLookupIdentifierScheme = rateLookupIdentifierScheme;
  }

  public String getRateLookupIdentifierScheme() {
    return _rateLookupIdentifierScheme;
  }

  protected Set<String> getValueNames() {
    return _valueNames;
  }

  public void setAllowViewDefaultCurrency(final boolean allowViewDefaultCurrency) {
    _allowViewDefaultCurrency = allowViewDefaultCurrency;
  }

  public boolean isAllowViewDefaultCurrency() {
    return _allowViewDefaultCurrency;
  }

  private ValueRequirement getInputValueRequirement(final ValueRequirement desiredValue) {
    return new ValueRequirement(desiredValue.getValueName(), desiredValue.getTargetSpecification(), desiredValue.getConstraints().copy().withoutAny(DEFAULT_CURRENCY_INJECTION)
        .withAny(ValuePropertyNames.CURRENCY).get());
  }

  private ValueRequirement getInputValueRequirement(final ValueRequirement desiredValue, final String forceCurrency) {
    return new ValueRequirement(desiredValue.getValueName(), desiredValue.getTargetSpecification(), desiredValue.getConstraints().copy().withoutAny(ValuePropertyNames.CURRENCY).with(
        ValuePropertyNames.CURRENCY, forceCurrency).withOptional(DEFAULT_CURRENCY_INJECTION).get());
  }

  /**
   * Divides the value by the conversion rate. Override this in a subclass for anything more elaborate - e.g. if
   * the value is in "somethings per currency unit foo" so needs multiplying by the rate instead.
   * 
   * @param value input value to convert
   * @param conversionRate conversion rate to use
   * @return the converted value
   */
  protected double convertDouble(final double value, final double conversionRate) {
    return value / conversionRate;
  }

  protected double[] convertDoubleArray(final double[] values, final double conversionRate) {
    final double[] newValues = new double[values.length];
    for (int i = 0; i < values.length; i++) {
      newValues[i] = convertDouble(values[i], conversionRate);
    }
    return newValues;
  }

  protected DoubleLabelledMatrix1D convertDoubleLabelledMatrix1D(final DoubleLabelledMatrix1D value, final double conversionRate) {
    return new DoubleLabelledMatrix1D(value.getKeys(), value.getLabels(), convertDoubleArray(value.getValues(), conversionRate));
  }

  /**
   * Delegates off to the other convert methods depending on the type of value.
   * 
   * @param inputValue input value to convert
   * @param desiredValue requested value requirement
   * @param conversionRate conversion rate to use
   * @return the converted value
   */
  protected Object convertValue(final ComputedValue inputValue, final ValueRequirement desiredValue, final double conversionRate) {
    final Object value = inputValue.getValue();
    if (value instanceof Double) {
      return convertDouble((Double) value, conversionRate);
    } else if (value instanceof DoubleLabelledMatrix1D) {
      return convertDoubleLabelledMatrix1D((DoubleLabelledMatrix1D) value, conversionRate);
    } else {
      s_logger.error("Can't convert object with type {} to {}", inputValue.getValue().getClass(), desiredValue);
      return null;
    }
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    ComputedValue inputValue = null;
    double exchangeRate = 0;
    for (final ComputedValue input : inputs.getAllValues()) {
      if (getRateLookupValueName().equals(input.getSpecification().getValueName())) {
        if (input.getValue() instanceof Double) {
          exchangeRate = (Double) input.getValue();
        } else {
          return null;
        }
      } else {
        inputValue = input;
      }
    }
    if (inputValue == null) {
      return null;
    }
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String outputCurrency = desiredValue.getConstraint(ValuePropertyNames.CURRENCY);
    final String inputCurrency = inputValue.getSpecification().getProperty(ValuePropertyNames.CURRENCY);
    if (outputCurrency.equals(inputCurrency)) {
      // Don't think this should happen
      return Collections.singleton(inputValue);
    }
    s_logger.debug("Converting from {} to {}", inputCurrency, outputCurrency);
    final Object converted = convertValue(inputValue, desiredValue, exchangeRate);
    if (converted != null) {
      return Collections.singleton(new ComputedValue(new ValueSpecification(desiredValue, desiredValue.getConstraints()), converted));
    }
    return null;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return true;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> possibleCurrencies = desiredValue.getConstraints().getValues(ValuePropertyNames.CURRENCY);
    if (possibleCurrencies == null) {
      s_logger.debug("Must specify a currency constraint; use DefaultCurrencyFunction instead");
      return null;
    } else if (possibleCurrencies.isEmpty()) {
      if (isAllowViewDefaultCurrency()) {
        // The original function may not have delivered a result because it had heterogeneous input currencies, so try forcing the view default
        final String defaultCurrencyISO = DefaultCurrencyFunction.getViewDefaultCurrencyISO(context);
        if (defaultCurrencyISO == null) {
          s_logger.debug("No default currency from the view to inject");
          return null;
        }
        s_logger.debug("Injecting view default currency {}", defaultCurrencyISO);
        return Collections.singleton(getInputValueRequirement(desiredValue, defaultCurrencyISO));
      }
      s_logger.debug("Cannot satisfy a wildcard currency constraint");
      return null;
    } else {
      // Actual input requirement is desired requirement with the currency wild-carded
      return Collections.singleton(getInputValueRequirement(desiredValue));
    }
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    // Maximal set of outputs is the valueNames with the infinite property set
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    if (getValueNames().size() == 1) {
      return Collections.singleton(new ValueSpecification(getValueNames().iterator().next(), targetSpec, ValueProperties.all()));
    }
    final Set<ValueSpecification> result = new HashSet<ValueSpecification>();
    for (final String valueName : getValueNames()) {
      result.add(new ValueSpecification(valueName, targetSpec, ValueProperties.all()));
    }
    return result;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final Map.Entry<ValueSpecification, ValueRequirement> input = inputs.entrySet().iterator().next();
    if (input.getValue().getConstraints().getValues(DEFAULT_CURRENCY_INJECTION) == null) {
      // Resolved output is the input with the currency wild-carded, and the function ID the same (this is so that after composition the node might
      // be removed from the graph)
      final ValueSpecification value = input.getKey();
      return Collections.singleton(new ValueSpecification(value.getValueName(), value.getTargetSpecification(), value.getProperties().copy().withAny(ValuePropertyNames.CURRENCY).get()));
    }
    // The input was requested with the converted currency, so return the same specification to remove this node from the graph
    return Collections.singleton(input.getKey());
  }

  private String getCurrency(final Collection<ValueSpecification> specifications) {
    final ValueSpecification specification = specifications.iterator().next();
    final Set<String> currencies = specification.getProperties().getValues(ValuePropertyNames.CURRENCY);
    if ((currencies == null) || (currencies.size() != 1)) {
      return null;
    }
    return currencies.iterator().next();
  }

  private ValueRequirement getCurrencyConversion(final String fromCurrency, final String toCurrency) {
    return new ValueRequirement(getRateLookupValueName(), new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of(getRateLookupIdentifierScheme(), fromCurrency + "_"
        + toCurrency)));
  }

  @Override
  public Set<ValueRequirement> getAdditionalRequirements(final FunctionCompilationContext context, final ComputationTarget target, final Set<ValueSpecification> inputs,
      final Set<ValueSpecification> outputs) {
    s_logger.debug("FX requirements for {} -> {}", inputs, outputs);
    final String inputCurrency = getCurrency(inputs);
    if (inputCurrency == null) {
      return null;
    }
    final String outputCurrency = getCurrency(outputs);
    if (outputCurrency == null) {
      return null;
    }
    if (inputCurrency.equals(outputCurrency)) {
      return Collections.emptySet();
    }
    return Collections.singleton(getCurrencyConversion(inputCurrency, outputCurrency));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return _targetType;
  }

}
