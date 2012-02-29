/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.loader.portfolio.DummyPortfolioWriter;
import com.opengamma.financial.loader.portfolio.MasterPortfolioReader;
import com.opengamma.financial.loader.portfolio.PortfolioReader;
import com.opengamma.financial.loader.portfolio.PortfolioWriter;
import com.opengamma.financial.loader.portfolio.SingleSheetPortfolioWriter;
import com.opengamma.financial.loader.rowparser.EquityFutureParser;
import com.opengamma.financial.loader.rowparser.InterestRateFutureParser;
import com.opengamma.financial.loader.rowparser.RowParser;
import com.opengamma.financial.loader.rowparser.SwapParser;
import com.opengamma.financial.tool.ToolContext;

/**
 * Provides standard portfolio loader functionality
 */
public class PortfolioSaverTool {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioLoaderTool.class);

  /** Tool name */
  private static final String TOOL_NAME = "OpenGamma Portfolio Exporter";
  /** File name option flag */
  private static final String FILE_NAME_OPT = "f";
  /** Portfolio name option flag*/
  private static final String PORTFOLIO_NAME_OPT = "n";
  /** Write option flag */
  private static final String WRITE_OPT = "w";

  
  private static RowParser[] s_rowParsers;
  
  /**
   * ENTRY POINT FOR COMMAND LINE TOOL
   * @param args  Command line args
   * @param toolContext  the loader context
   */
  public void run(String[] args, ToolContext toolContext) {
    s_logger.info(TOOL_NAME + " is initialising...");
    s_logger.info("Current working directory is " + System.getProperty("user.dir"));
    
    // Parse command line arguments
    CommandLine cmdLine = getCmdLine(args, true);
    
    run(cmdLine, toolContext);
  }

  private void run(CommandLine cmdLine, ToolContext toolContext) {
    
    // Set up list of available parsers
    s_rowParsers = new RowParser[] {
//      new CashParser(toolContext)
      new EquityFutureParser(toolContext),
//      new FRAParser(toolContext),
//      new FXDigitalOptionParser(toolContext),
//      new FXForwardParser(toolContext),
//      new FXVanillaOptionParser(toolContext),
//      new IRFutureOptionParser(toolContext),
      new InterestRateFutureParser(toolContext),
      new SwapParser(toolContext)
//      new SwaptionParser(toolContext)
    };
    
    // Set up writer
    PortfolioWriter portfolioWriter = constructPortfolioWriter(
        cmdLine.getOptionValue(FILE_NAME_OPT), 
        cmdLine.hasOption(WRITE_OPT),
        toolContext);
    
     // Set up reader
    PortfolioReader portfolioReader = constructPortfolioReader(
        cmdLine.getOptionValue(PORTFOLIO_NAME_OPT), 
        toolContext);
    
    // Load in and write the securities, positions and trades
    portfolioReader.writeTo(portfolioWriter);
    
    // Flush changes to portfolio master & close
    portfolioWriter.flush();
    portfolioWriter.close();
    
    s_logger.info(TOOL_NAME + " is finished.");
  }
  
  
  private static CommandLine getCmdLine(String[] args, boolean contextProvided) {
    final Options options = getOptions(contextProvided);
    try {
      return new PosixParser().parse(options, args);
    } catch (ParseException e) {
      s_logger.warn(e.getMessage());
      (new HelpFormatter()).printHelp(" ", options);
      throw new OpenGammaRuntimeException("Could not parse the command line");
    }        
  }

  private static Options getOptions(boolean contextProvided) {
    Options options = new Options();
    Option filenameOption = new Option(
        FILE_NAME_OPT, "filename", true, "The path to the file to create and export to (CSV or ZIP)");
    filenameOption.setRequired(true);
    options.addOption(filenameOption);
    
    Option portfolioNameOption = new Option(
        PORTFOLIO_NAME_OPT, "name", true, "The name of the source OpenGamma portfolio");
    options.addOption(portfolioNameOption);
    
    Option writeOption = new Option(
        WRITE_OPT, "write", false, 
        "Actually persists the portfolio to the file if specified, otherwise pretty-prints without persisting");
    options.addOption(writeOption);
       
    return options;
  }

  private static PortfolioWriter constructPortfolioWriter(String filename, boolean write, ToolContext toolContext) {
    if (write) {  
      // Check that the file name was specified on the command line
      if (filename == null) {
        throw new OpenGammaRuntimeException("File name omitted, cannot export to file");
      }
      
      s_logger.info("Write option specified, will persist to file '" + filename + "'");
      
      // Create a portfolio writer to persist imported positions, trades and securities to the OG masters
      return new SingleSheetPortfolioWriter(filename, s_rowParsers);
      
    } else {
      s_logger.info("Write option omitted, will pretty-print instead of persisting to file");
      
      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new DummyPortfolioWriter();
    }

  }

  private static PortfolioReader constructPortfolioReader(String portfolioName, ToolContext toolContext) {
    return new MasterPortfolioReader(portfolioName, toolContext);
  }

}
