/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.copiernew.security;

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
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.ArgumentChecker;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.PropertyReadWrite;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

/**
 * A generic row parser for Joda beans that automatically identifies fields to be persisted to rows/populated from rows
 */
public class SecurityRowUtils extends JodaBeanRowUtils {

  private static final Logger s_logger = LoggerFactory.getLogger(SecurityRowUtils.class);

  /**
   * Types of swap leg that might be encountered, and for which additional fields are generated
   */
  private final static Class<?>[] SWAP_LEG_CLASSES = {
      SwapLeg.class,
      InterestRateLeg.class,
      FixedInterestRateLeg.class,
      FloatingInterestRateLeg.class,
      FloatingGearingIRLeg.class,
      FloatingSpreadIRLeg.class,
      VarianceSwapLeg.class,
      FixedVarianceSwapLeg.class,
      FloatingVarianceSwapLeg.class
    };



  /**
   * The packages where security classes are to be found
   */
  @Override
  protected String[] getClassPackages() {
    return new String[] {
      "com.opengamma.financial.security.bond",
      "com.opengamma.financial.security.capfloor",
      "com.opengamma.financial.security.cash",
      "com.opengamma.financial.security.equity",
      "com.opengamma.financial.security.fra",
      "com.opengamma.financial.security.future",
      "com.opengamma.financial.security.fx",
      "com.opengamma.financial.security.option",
      "com.opengamma.financial.security.swap"
    };
  }

  /**
   * Security properties to ignore when scanning
   */
  @Override
  protected String[] getIgnoreMetaproperties() {
    return new String[] {
      "attributes",
      "uniqueid",
      "objectid",
      "securitylink",
      "trades",
      "attributes",
      "gicscode",
      "parentpositionid",
      "providerid",
      "deal"
    };
  }

  /**
   * Column prefixes
   */
  private static final String POSITION_PREFIX = "position";
  private static final String TRADE_PREFIX = "trade";
  private static final String UNDERLYING_PREFIX = "underlying";

  /**
   * Every security class name ends with this
   */
  private static final String CLASS_POSTFIX = "Security";

  /**
   * The security class that this parser is adapted to
   */
  private Class<DirectBean> _securityClass;

  /**
   * The underlying security class(es) for the security class above
   */
  private List<Class<?>> _underlyingSecurityClasses;

  static {
    // Register the automatic string converters with Joda Beans
    JodaBeanConverters.getInstance();

    // Force registration of various meta beans that might not have been loaded yet
    ManageablePosition.meta();
    ManageableTrade.meta();
    Notional.meta();
    SwapLeg.meta();
    InterestRateLeg.meta();
    FixedInterestRateLeg.meta();
    FloatingInterestRateLeg.meta();
    FloatingGearingIRLeg.meta();
    FloatingSpreadIRLeg.meta();
    VarianceSwapLeg.meta();
    FixedVarianceSwapLeg.meta();
    FloatingVarianceSwapLeg.meta();
    EquitySecurity.meta();
    SwapSecurity.meta();
  }

  protected SecurityRowUtils(String securityName) throws OpenGammaRuntimeException {
    
    ArgumentChecker.notEmpty(securityName, "securityName");

    // Find the corresponding security class
    _securityClass = getClass(securityName + CLASS_POSTFIX);

    // Find the underlying(s)
    _underlyingSecurityClasses = getUnderlyingSecurityClasses(_securityClass);
    
    // Set column map
    _columns = recursiveGetColumnMap(_securityClass, "");
    for (Class<?> securityClass : _underlyingSecurityClasses) {
      _columns.putAll(recursiveGetColumnMap(securityClass, UNDERLYING_PREFIX + securityClass.getSimpleName() + ":"));
    }
  }

  private List<Class<?>> getUnderlyingSecurityClasses(Class<DirectBean> securityClass) {
    
    List<Class<?>> result = new ArrayList<Class<?>>();
          
    // Futures
    if (EquityFutureSecurity.class.isAssignableFrom(securityClass)) {
      result.add(EquitySecurity.class);
      
    // Options
    } else if (EquityBarrierOptionSecurity.class.isAssignableFrom(securityClass)) {
      result.add(EquitySecurity.class);
    } else if (EquityOptionSecurity.class.isAssignableFrom(securityClass)) {
      result.add(EquitySecurity.class);      
    } else if (IRFutureOptionSecurity.class.isAssignableFrom(securityClass)) {
      result.add(InterestRateFutureSecurity.class);
    } else if (SwaptionSecurity.class.isAssignableFrom(securityClass)) {
      result.add(SwapSecurity.class);
    } 

    return result;
  }

  /**
   * Creates a new row parser for the specified security type and tool context
   * @param securityName  the type of the security for which a row parser is to be created
   * @return              the RowReader class for the specified security type, or null if unable to identify a suitable parser
   */
  public static SecurityRowUtils newSecurityRowUtils(String securityName) {
    // Now using the JodaBean parser

    ArgumentChecker.notEmpty(securityName, "securityName");

    try {
      return new SecurityRowUtils(securityName);
    } catch (Throwable e) {
      return null;
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Import routines: construct security(ies), position, trade
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public ManageableSecurity[] constructSecurity(Map<String, String> row) {
    
    ArgumentChecker.notNull(row, "row");
    
    ManageableSecurity security = (ManageableSecurity) recursiveConstructBean(row, _securityClass, "");
    if (security != null) {
      ArrayList<ManageableSecurity> securities = new ArrayList<ManageableSecurity>();
      securities.add(security);
      for (Class<?> underlyingClass : _underlyingSecurityClasses) {
        ManageableSecurity underlying = (ManageableSecurity) recursiveConstructBean(row, underlyingClass, UNDERLYING_PREFIX + underlyingClass.getSimpleName().toLowerCase() + ":");
        if (underlying != null) {
          securities.add(underlying);
        } else {
          s_logger.warn("Could not populate underlying security of type " + underlyingClass);
        }
      }
      return securities.toArray(new ManageableSecurity[securities.size()]);
    } else {
      return null;
    }
  }
  
  public ManageablePosition constructPosition(Map<String, String> row, ManageableSecurity security) {
    
    ArgumentChecker.notNull(row, "row");
    ArgumentChecker.notNull(security, "security");
    
    ManageablePosition result = (ManageablePosition) recursiveConstructBean(row, ManageablePosition.class, "position:");
    if (result != null) {
      result.setSecurityLink(new ManageableSecurityLink(security.getExternalIdBundle()));
    }
    return result;
  }

  public ManageableTrade constructTrade(Map<String, String> row, ManageableSecurity security, ManageablePosition position) {

    ArgumentChecker.notNull(row, "row");
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(position, "position");
    
    ManageableTrade result = (ManageableTrade) recursiveConstructBean(row, ManageableTrade.class, "trade:");
    if (result != null) {
      if (result.getTradeDate() == null) {
        return null;
      }
      result.setSecurityLink(new ManageableSecurityLink(security.getExternalIdBundle()));
    }
    return result;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Export routines: construct row from security, position, trade
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   
  public Map<String, String> constructRow(ManageableSecurity[] securities) {
    ArgumentChecker.notNull(securities, "securities");
    Map<String, String> result = recursiveConstructRow(securities[0], "");
    
    for (int i = 1; i < securities.length; i++) {
      result.putAll(recursiveConstructRow(securities[i], UNDERLYING_PREFIX + securities[i].getClass().getSimpleName() + ":"));
    }
    return result;
  }
  
  public Map<String, String> constructRow(ManageablePosition position) {
    ArgumentChecker.notNull(position, "position");
    return recursiveConstructRow(position, "position:");
  }

  public Map<String, String> constructRow(ManageableTrade trade) {
    ArgumentChecker.notNull(trade, "trade");
    return recursiveConstructRow(trade, "trade:");
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Given a bean class, find its subclasses; this is current hard coded as Java can neither identify the 
   * classes within a package, nor identify a class's subclasses. Currently identifies swap legs.
   * @param beanClass
   * @return
   */
  @Override
  protected Collection<Class<?>> getSubClasses(Class<?> beanClass) {
    Collection<Class<?>> subClasses = new ArrayList<Class<?>>();
    
    // This has to be hard-coded since Java can neither identify the classes within a package, nor identify a class's subclasses
    if (SwapLeg.class.isAssignableFrom(beanClass)) {
      for (Class<?> c : SWAP_LEG_CLASSES) {
        subClasses.add(c);
      }
    }  
    return (Collection<Class<?>>) subClasses;
  }

}
