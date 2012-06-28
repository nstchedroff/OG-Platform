/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.copiernew.rawsheet.writer;

import java.io.*;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.integration.copiernew.Writeable;
import com.opengamma.integration.copiernew.rawsheet.SheetFormat;
import com.opengamma.util.ArgumentChecker;

/**
 * This abstract class represents a rawsheet writer that, given a map from column names to data, writes out a row containing that
 * data under the matching columns.
 */
public abstract class RawSheetWriter implements Writeable<Map<String, String>>, Closeable, Flushable {

  private String[] _columns; // The column names and order
  
  public static RawSheetWriter newSheetWriter(String filename, String[] columns) {
    ArgumentChecker.notEmpty(filename, "filename");
    OutputStream outputStream = openFile(filename);
    return newSheetWriter(SheetFormat.of(filename), outputStream, columns); 
  }

  public static RawSheetWriter newSheetWriter(SheetFormat sheetFormat, OutputStream outputStream, String[] columns) {
    
    ArgumentChecker.notNull(sheetFormat, "sheetFormat");
    ArgumentChecker.notNull(outputStream, "outputStream");
    ArgumentChecker.notNull(columns, "columns");
    
    switch (sheetFormat) {
      case CSV:
        return new CsvRawSheetWriter(outputStream, columns);
      default:
        throw new OpenGammaRuntimeException("Could not create a writer for the rawsheet output format " + sheetFormat.toString());
    }
  }

  protected String[] getColumns() {
    return _columns;
  }

  protected void setColumns(String[] columns) {
    _columns = columns;
  }
  
  protected static OutputStream openFile(String filename) {
    // Open input file for writing
    FileOutputStream fileOutputStream;
    try {
      fileOutputStream = new FileOutputStream(filename);
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Could not open file " + filename + " for writing.");
    }

    return fileOutputStream;
  }

}
