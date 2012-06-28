/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copiernew.security;

import com.opengamma.integration.copiernew.row.RowWriter;
import com.opengamma.master.security.ManageableSecurity;

import java.util.Map;

public class SecurityRowWriter<E extends ManageableSecurity> implements RowWriter<E> {

  private SecurityRowUtils _utils;

  public SecurityRowWriter(String secType) {
    _utils = SecurityRowUtils.newSecurityRowUtils(secType);
  }

  @Override
  public Map<String, String> writeRow(E security) {
    return _utils.constructRow(new ManageableSecurity[] {security});
  }

  @Override
  public String[] getColumns() {
    return _utils.getColumns();
  }
}
