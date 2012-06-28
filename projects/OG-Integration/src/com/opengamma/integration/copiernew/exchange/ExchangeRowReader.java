/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copiernew.exchange;

import com.opengamma.integration.copiernew.sheet.RowReader;
import com.opengamma.master.exchange.ManageableExchange;

import java.util.Map;

public class ExchangeRowReader implements RowReader<ManageableExchange> {

  @Override
  public ManageableExchange readRow(Map<String, String> row) {
    ManageableExchange exchange = new ManageableExchange();

    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String[] getColumns() {
    return null;
  }
}
