/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copiernew.security;

import com.opengamma.integration.copiernew.row.RowReader;
import com.opengamma.master.security.ManageableSecurity;

import java.util.Map;

public class SecurityRowReader<E extends ManageableSecurity> implements RowReader<E> {

  @Override
  public E readRow(Map<String, String> row) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String[] getColumns() {
    return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
  }
}
