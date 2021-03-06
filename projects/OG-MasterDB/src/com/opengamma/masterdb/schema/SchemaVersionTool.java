/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.schema;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.opengamma.util.test.DbTool;

/**
 * Ant task which outputs files containing schema version information.
 */
public class SchemaVersionTool extends Task {

  private String _dbScriptDir;
  private String _outputDir;
  
  public String getDbScriptDir() {
    return _dbScriptDir;
  }

  public void setDbScriptDir(String dbScriptDir) {
    _dbScriptDir = dbScriptDir;
  }
  
  public String getOutputDir() {
    return _outputDir;
  }

  public void setOutputDir(String outputDir) {
    _outputDir = outputDir;
  }

  @Override
  public void execute() throws BuildException {
    if (getDbScriptDir() == null) {
      throw new BuildException("dbScriptDir must be specified");
    }
    System.out.println("dbScriptDir: " + getDbScriptDir());
    if (getOutputDir() == null) {
      throw new BuildException("outputDir must be specified");
    }
    System.out.println("outputDir: " + getOutputDir());
    final File outputDir = SchemaVersionUtils.getSchemaVersionDir(getOutputDir());
    if (outputDir.isFile()) {
      throw new BuildException("outputDir must be a directory");
    }
    if (!outputDir.exists()) {
      outputDir.mkdir();
    }
    
    final DbTool tool = new DbTool();
    tool.addDbScriptDirectory(getDbScriptDir());
    final Map<String, Integer> latestVersions = tool.getLatestVersions();
    
    System.out.println("Found latest versions: " + latestVersions);
    
    for (final Map.Entry<String, Integer> schemaVersion : latestVersions.entrySet()) {
      final String schemaName = schemaVersion.getKey();
      final int latestVersion = schemaVersion.getValue();
      try {
        final File f = new File(outputDir, schemaName);
        if (f.exists()) {
          f.delete();
        }
        f.createNewFile();
        final FileOutputStream fos = new FileOutputStream(f);
        final OutputStreamWriter out = new OutputStreamWriter(fos);
        try {
          out.write(Integer.toString(latestVersion));
        } finally {
          out.close();
        }
        System.out.println("Written version " + latestVersion + " to " + f.getAbsolutePath());
      } catch (Exception e) {
        throw new BuildException("Error writing file for schema " + schemaName, e);
      }
    }
  }
}
