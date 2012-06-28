/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copiernew;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.integration.copiernew.exchange.ExchangeMasterReader;
import com.opengamma.integration.copiernew.exchange.ExchangeRowWriter;
import com.opengamma.integration.copiernew.sheet.CsvRawSheetWriter;
import com.opengamma.integration.copiernew.sheet.RowWriter;
import com.opengamma.integration.copiernew.security.SecurityMasterReader;
import com.opengamma.integration.copiernew.security.SecurityRowWriter;
import com.opengamma.integration.copiernew.sheet.SheetWriter;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.master.security.SecuritySearchRequest;

public class TestCopierTool extends AbstractTool {

  /**
   * Main method to run the tool.
   *
   * @param args  the arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new TestCopierTool().initAndRun(args);
    System.exit(0);
  }

  @Override
  protected void doRun() throws Exception {
/*
    SecuritySearchRequest searchRequest = new SecuritySearchRequest();
    searchRequest.setSecurityType("EQUITY");

    Iterable<EquitySecurity> securityMasterReader =
        new SecurityMasterReader<EquitySecurity>(getToolContext().getSecurityMaster(), searchRequest);
    RowWriter<EquitySecurity> securityRowWriter =
        new SecurityRowWriter<EquitySecurity>("Equity");
    Writeable<EquitySecurity> securitySheetWriter =
        new SheetWriter<EquitySecurity>(new CsvRawSheetWriter("test.csv", securityRowWriter.getColumns()), securityRowWriter);

    new Copier<EquitySecurity>().copy(securityMasterReader, securitySheetWriter);
*/

    ExchangeSearchRequest searchRequest = new ExchangeSearchRequest();
    //searchRequest.setSecurityType("EQUITY");

    Iterable<ManageableExchange> masterReader =
        new ExchangeMasterReader(getToolContext().getExchangeMaster(), searchRequest);
    RowWriter<ManageableExchange> rowWriter =
        new ExchangeRowWriter();
    Writeable<ManageableExchange> sheetWriter =
        new SheetWriter<ManageableExchange>(new CsvRawSheetWriter("test.csv", rowWriter.getColumns()), rowWriter);

    new Copier<ManageableExchange>().copy(masterReader, sheetWriter);

  }

}
