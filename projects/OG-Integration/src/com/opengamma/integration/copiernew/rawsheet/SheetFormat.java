/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copiernew.rawsheet;

/**
 * Known rawsheet formats
 */
public enum SheetFormat {

  /** CSV rawsheet */
  CSV, 

  /** XLS rawsheet */
  XLS, 

  /** XLSX rawsheet */
  XLSX,
  
  /** ZIP rawsheet */
  ZIP,

  /** Unknown rawsheet */
  UNKNOWN;
 
  public static SheetFormat of(String filename) {
    if (filename.lastIndexOf('.') < 0) {
      return SheetFormat.UNKNOWN;
    }
    String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase().trim();
    if (extension.equals(".csv")) {
      return SheetFormat.CSV;
    } else if (extension.equals(".xls")) {
      return SheetFormat.XLS;
    } else if (extension.equals(".xlsx")) {
      return SheetFormat.XLSX;
    } else if (extension.equals(".zip")) {
      return SheetFormat.ZIP;
    } else {
      return SheetFormat.UNKNOWN;
    }
  }

}
