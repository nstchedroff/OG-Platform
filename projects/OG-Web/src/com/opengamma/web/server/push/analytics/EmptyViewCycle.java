/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collections;

import javax.time.Duration;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.calc.ComputationCacheQuery;
import com.opengamma.engine.view.calc.ComputationCacheResponse;
import com.opengamma.engine.view.calc.EngineResourceReference;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.calc.ViewCycleState;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;

/**
 * {@link ViewCycle} implementation that acts as a placeholder when a calculation cycle hasn't completed and there
 * isn't a cycle available. This is cleaner than using a null cycle reference and being forced to do a null check
 * everywhere it's used. Only a sigle instance of this class should ever exist.
 */
/* package */ class EmptyViewCycle implements ViewCycle {

  /** Reference to the empty cycle. */
  /* package */ static final EngineResourceReference<ViewCycle> REFERENCE = new EmptyViewCycleReference();
  /** Single empty cycle instance. */
  /* package */ static final ViewCycle INSTANCE = new EmptyViewCycle();

  /** Empty set of analytics results. */
  private static final InMemoryViewComputationResultModel EMPTY_RESULTS = new InMemoryViewComputationResultModel();
  /** Empty response for a cache lookup. */
  private static final ComputationCacheResponse EMPTY_RESPONSE;

  static {
    EMPTY_RESPONSE = new ComputationCacheResponse();
    EMPTY_RESPONSE.setResults(Collections.<Pair<ValueSpecification, Object>>emptyList());
  }

  private EmptyViewCycle() {
  }

  @Override
  public UniqueId getUniqueId() {
    throw new UnsupportedOperationException("getUniqueId not supported");
  }

  @Override
  public UniqueId getViewProcessId() {
    throw new UnsupportedOperationException("getViewProcessId not supported");
  }

  @Override
  public ViewCycleState getState() {
    throw new UnsupportedOperationException("getState not supported");
  }

  @Override
  public Duration getDuration() {
    return Duration.ZERO;
  }

  @Override
  public CompiledViewDefinitionWithGraphs getCompiledViewDefinition() {
    throw new UnsupportedOperationException("getCompiledViewDefinition not supported");
  }

  @Override
  public ViewComputationResultModel getResultModel() {
    return EMPTY_RESULTS;
  }

  @Override
  public ComputationCacheResponse queryComputationCaches(ComputationCacheQuery computationCacheQuery) {
    return EMPTY_RESPONSE;
  }

  /* package */ static class EmptyViewCycleReference implements EngineResourceReference<ViewCycle> {

    private EmptyViewCycleReference() {
    }

    @Override
    public ViewCycle get() {
      return EmptyViewCycle.INSTANCE;
    }

    @Override
    public void release() {
      // do nothing
    }
  }
}
