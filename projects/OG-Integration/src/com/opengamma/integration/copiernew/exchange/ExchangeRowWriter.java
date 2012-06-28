/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copiernew.exchange;

import com.opengamma.integration.copiernew.security.SecurityRowUtils;
import com.opengamma.integration.copiernew.sheet.RowWriter;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.master.security.ManageableSecurity;

import java.util.Map;

public class ExchangeRowWriter implements RowWriter<ManageableExchange> {

  private ExchangeRowUtils _utils;

  public ExchangeRowWriter() {
    _utils = new ExchangeRowUtils();
  }

  @Override
  public Map<String, String> writeRow(ManageableExchange exchange) {
    return _utils.constructRow(exchange);
  }

  @Override
  public String[] getColumns() {
    return _utils.getColumns();
  }

}
