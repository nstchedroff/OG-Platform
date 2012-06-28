/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.copiernew.exchange;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.*;
import com.opengamma.integration.copiernew.JodaBeanRowUtils;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.master.exchange.ManageableExchangeDetail;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.ArgumentChecker;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.beans.impl.direct.DirectBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

/**
 * A generic row parser for Joda beans that automatically identifies fields to be persisted to rows/populated from rows
 */
public class ExchangeRowUtils extends JodaBeanRowUtils {

  private static final Logger s_logger = LoggerFactory.getLogger(ExchangeRowUtils.class);

  /**
   * Security properties to ignore when scanning
   */
  @Override
  protected String[] getIgnoreMetaproperties() {
    return new String[] {
      "uniqueid",
//      "detail"
    };
  };

  @Override
  protected String[] getClassPackages() {
    return new String[] {

    };
  }

  static {
    // Register the automatic string converters with Joda Beans
    JodaBeanConverters.getInstance();

    // Force registration of various meta beans that might not have been loaded yet
    ManageableExchange.meta();
    ManageableExchangeDetail.meta();
  }

  public ExchangeRowUtils() {
    _columns = recursiveGetColumnMap(ManageableExchange.class, "");
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Import routines: construct exchange from row
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public ManageableExchange constructExchange(Map<String, String> row) {
    ArgumentChecker.notNull(row, "row");
    ManageableExchange exchange = (ManageableExchange) recursiveConstructBean(row, ManageableExchange.class, "");
    return exchange;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Export routines: construct row from exchange
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   
  public Map<String, String> constructRow(ManageableExchange exchange) {
    ArgumentChecker.notNull(exchange, "exchange");
    Map<String, String> result = recursiveConstructRow(exchange, "");
    return result;
  }

  /**
   * Given a bean class, find its subclasses; this is current hard coded as Java can neither identify the 
   * classes within a package, nor identify a class's subclasses.
   * @param beanClass
   * @return
   */
  @Override
  protected Collection<Class<?>> getSubClasses(Class<?> beanClass) {
    return new ArrayList<Class<?>>();
  }

}
