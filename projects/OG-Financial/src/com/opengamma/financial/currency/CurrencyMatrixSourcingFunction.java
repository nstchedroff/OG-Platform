/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixCross;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixFixed;
import com.opengamma.financial.currency.CurrencyMatrixValue.CurrencyMatrixValueRequirement;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Injects a {@link CurrencyMatrix} into a dependency graph to satisfy the currency requirements generated by {@link CurrencyConversionFunction}.
 * The generic {@link CurrencyCrossRateFunction}, {@link CurrencyInversionFunction}, and any other functions that source rates from live data
 * providers should not be used if this is used - or set to a very low priority so that the matrix will override them.
 */
public class CurrencyMatrixSourcingFunction extends AbstractFunction.NonCompiledInvoker {

  private static final Logger s_logger = LoggerFactory.getLogger(CurrencyMatrixSourcingFunction.class);

  private static final Pattern s_validate = Pattern.compile("[A-Z]{3}_[A-Z]{3}");

  private String _rateLookupIdentifierScheme = CurrencyConversionFunction.DEFAULT_LOOKUP_IDENTIFIER_SCHEME;
  private String _rateLookupValueName = CurrencyConversionFunction.DEFAULT_LOOKUP_VALUE_NAME;

  private final String _currencyMatrixName;
  private CurrencyMatrix _currencyMatrix;

  public CurrencyMatrixSourcingFunction(final String currencyMatrixName) {
    _currencyMatrixName = currencyMatrixName;
  }

  private static Pair<Currency, Currency> parse(final UniqueId uniqueId) {
    final int underscore = uniqueId.getValue().indexOf('_');
    final Currency source = Currency.of(uniqueId.getValue().substring(0, underscore));
    final Currency target = Currency.of(uniqueId.getValue().substring(underscore + 1));
    return Pair.of(source, target);
  }

  public void setRateLookupIdentifierScheme(final String rateLookupIdentifierScheme) {
    _rateLookupIdentifierScheme = rateLookupIdentifierScheme;
  }

  public String getRateLookupIdentifierScheme() {
    return _rateLookupIdentifierScheme;
  }

  public void setRateLookupValueName(final String rateLookupValueName) {
    _rateLookupValueName = rateLookupValueName;
  }

  public String getRateLookupValueName() {
    return _rateLookupValueName;
  }

  protected CurrencyMatrix getCurrencyMatrix() {
    return _currencyMatrix;
  }

  protected void setCurrencyMatrix(final CurrencyMatrix currencyMatrix) {
    _currencyMatrix = currencyMatrix;
  }

  protected String getCurrencyMatrixName() {
    return _currencyMatrixName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final CurrencyMatrix matrix = OpenGammaCompilationContext.getCurrencyMatrixSource(context).getCurrencyMatrix(getCurrencyMatrixName());
    setCurrencyMatrix(matrix);
    if (matrix != null) {
      if (matrix.getUniqueId() != null) {
        context.getFunctionReinitializer().reinitializeFunction(this, matrix.getUniqueId());
      }
    }
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Set<ComputedValue> rates = Sets.newHashSetWithExpectedSize(desiredValues.size());
    for (ValueRequirement desiredValue : desiredValues) {
      final Pair<Currency, Currency> currencies = parse(desiredValue.getTargetSpecification().getUniqueId());
      rates.add(new ComputedValue(createValueSpecification(desiredValue.getTargetSpecification()), getConversionRate(inputs, currencies.getFirst(), currencies.getSecond())));
    }
    return rates;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (getCurrencyMatrix() == null) {
      return false;
    }
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    if (!getRateLookupIdentifierScheme().equals(target.getUniqueId().getScheme())) {
      return false;
    }
    if (!s_validate.matcher(target.getUniqueId().getValue()).matches()) {
      return false;
    }
    return getConversionRequirements(parse(target.getUniqueId())) != null;
  }

  private boolean getConversionRequirements(final Set<ValueRequirement> requirements, final Set<Pair<Currency, Currency>> visited, final Pair<Currency, Currency> currencies) {
    if (!visited.add(currencies)) {
      // Gone round in a loop if we've already seen this pair
      throw new IllegalStateException();
    }
    final CurrencyMatrixValue value = getCurrencyMatrix().getConversion(currencies.getFirst(), currencies.getSecond());
    if (value != null) {
      return value.accept(new CurrencyMatrixValueVisitor<Boolean>() {

        @Override
        public Boolean visitCross(CurrencyMatrixCross cross) {
          return getConversionRequirements(requirements, visited, Pair.of(currencies.getFirst(), cross.getCrossCurrency()))
              && getConversionRequirements(requirements, visited, Pair.of(cross.getCrossCurrency(), currencies.getSecond()));
        }

        @Override
        public Boolean visitFixed(CurrencyMatrixFixed fixedValue) {
          // Literal value - nothing required
          return Boolean.TRUE;
        }

        @Override
        public Boolean visitValueRequirement(final CurrencyMatrixValueRequirement valueRequirement) {
          requirements.add(valueRequirement.getValueRequirement());
          return Boolean.TRUE;
        }

      });
    } else {
      return false;
    }
  }

  private Set<ValueRequirement> getConversionRequirements(final Pair<Currency, Currency> currencies) {
    if (getCurrencyMatrix() == null) {
      return null;
    }
    final HashSet<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    if (!getConversionRequirements(requirements, new HashSet<Pair<Currency, Currency>>(), currencies)) {
      return null;
    }
    return requirements;
  }

  private double getConversionRate(final FunctionInputs inputs, final Currency source, final Currency target) {
    final CurrencyMatrixValue value = getCurrencyMatrix().getConversion(source, target);
    Double rate = value.accept(new CurrencyMatrixValueVisitor<Double>() {

      @Override
      public Double visitCross(CurrencyMatrixCross cross) {
        return getConversionRate(inputs, source, cross.getCrossCurrency()) * getConversionRate(inputs, cross.getCrossCurrency(), target);
      }

      @Override
      public Double visitFixed(CurrencyMatrixFixed fixedValue) {
        return fixedValue.getFixedValue();
      }

      @Override
      public Double visitValueRequirement(CurrencyMatrixValueRequirement valueRequirement) {
        Object marketValue = inputs.getValue(valueRequirement.getValueRequirement());
        if (!(marketValue instanceof Number)) {
          throw new IllegalArgumentException(valueRequirement.toString());
        }
        double rate = ((Number) marketValue).doubleValue();
        if (valueRequirement.isReciprocal()) {
          rate = 1.0 / rate;
        }
        return rate;
      }

    });
    s_logger.debug("{} to {} = {}", new Object[] {source, target, rate});
    return rate;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Pair<Currency, Currency> currencies = parse(desiredValue.getTargetSpecification().getUniqueId());
    return getConversionRequirements(currencies);
  }

  private ValueSpecification createValueSpecification(final ComputationTargetSpecification spec) {
    return new ValueSpecification(getRateLookupValueName(), spec, createValueProperties().get());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(createValueSpecification(target.toSpecification()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

}
