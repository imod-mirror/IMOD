
package etomo.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import etomo.ApplicationManager;
import etomo.type.AxisID;
import etomo.util.Utilities;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright (c) 2002, 2003</p>
 * 
 * <p>Organization: Boulder Laboratory for 3D Electron Microscopy (BL3DEM),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 * 
 * <p> $Log$
 * <p> Revision 3.7  2004/04/23 20:04:09  sueh
 * <p> bug# 83 using initializeProgressBar() to initialize progress bar
 * <p> before running waitForLogFile()
 * <p>
 * <p> Revision 3.6  2004/04/23 19:36:56  sueh
 * <p> bug# 83 adding a "starting" comment when starting the process bar
 * <p>
 * <p> Revision 3.5  2004/04/08 17:33:59  rickg
 * <p> Use Utilities.milliesToMinAndSecs to get time string
 * <p>
 * <p> Revision 3.4  2004/03/22 23:44:09  sueh
 * <p> bug# 83 allowed findNSections() to be overridden, allowed for a 
 * <p> non-standard log file name
 * <p>
 * <p> Revision 3.3  2004/03/16 21:52:26  sueh
 * <p> bug# 413 reset process bar after waiting for exit if exit signal doesn't
 * <p> come
 * <p>
 * <p> Revision 3.2  2004/03/13 01:55:29  sueh
 * <p> bug# 413 possible solution infinite run() loop in comments
 * <p>
 * <p> Revision 3.1  2003/11/27 00:01:27  rickg
 * <p> logFile is now a member object
 * <p> made sure the the logFile is closed when the monitor is done
 * <p>
 * <p> Revision 3.0  2003/11/07 23:19:00  rickg
 * <p> Version 1.0.0
 * <p>
 * <p> Revision 1.3  2003/09/08 22:23:37  rickg
 * <p> Limit percentage done to between 0 and 99
 * <p>
 * <p> Revision 1.2  2003/08/05 21:17:23  rickg
 * <p> Initial revision (really)
 * <p>
 * <p> Revision 1.1  2003/08/04 22:23:16  rickg
 * <p> Initial revision
 * <p> </p>
 */

public abstract class LogFileProcessMonitor implements Runnable {
  public static final String rcsid = "$Id$";
  protected ApplicationManager applicationManager;
  protected AxisID axisID;
  protected long processStartTime;
  protected BufferedReader logFileReader;
  protected int nSections = Integer.MIN_VALUE;
  protected int currentSection;
  protected int remainingTime;
  protected int waitingForExit = 0;

  protected int updatePeriod = 500;
  protected int stopWaiting = 20;

  boolean standardLogFileName = true;

  //  This needs to be set in the concrete class constructor
  protected String logFileBasename;
  protected File logFile;

  protected abstract void initializeProgressBar();

  protected abstract void getCurrentSection() throws NumberFormatException,
      IOException;

  /**
   * Default constructor
   * @param appMgr  The application manager object
   * @param id  The axis ID to be monitored
   */
  public LogFileProcessMonitor(ApplicationManager appMgr, AxisID id) {
    applicationManager = appMgr;
    axisID = id;
  }

  public void run() {
    initializeProgressBar();
    //  Instantiate the logFile object
    String logFileName;
    if (standardLogFileName) {
      logFileName = logFileBasename + axisID.getExtension() + ".log";
    }
    else {
      logFileName = logFileBasename;
    }
    logFile = new File(System.getProperty("user.dir"), logFileName);

    boolean processRunning = true;
    try {
      //  Wait for the log file to exist
      waitForLogFile();
      findNSections();
      initializeProgressBar();

      while (processRunning && waitingForExit < stopWaiting) {
        Thread.sleep(updatePeriod);
        getCurrentSection();
        calcRemainingTime();
        updateProgressBar();
      }
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    catch (InterruptedException e) {
      processRunning = false;
    }
    catch (NumberFormatException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    //  Close the log file reader
    try {
      Utilities
        .debugPrint("LogFileProcessMonitor: Closing the log file reader for "
            + logFile.getAbsolutePath());
      if (logFileReader != null) {
        logFileReader.close();
      }
    }
    catch (IOException e1) {
      e1.printStackTrace();
    }
    applicationManager.progressBarDone(axisID);
  }

  /**
   * Wait for the process to start and the appropriate log file to be created 
   * @return a buffered reader of the log file
   */
  private void waitForLogFile() throws InterruptedException,
      FileNotFoundException {

    processStartTime = System.currentTimeMillis();

    boolean newLogFile = false;
    while (!newLogFile) {
      // Check to see if the log file exists that signifies that the process
      // has started
      if (logFile.exists()) {
        newLogFile = true;
      }
      else {
        Thread.sleep(updatePeriod);
      }
    }
    //  Open the log file
    logFileReader = new BufferedReader(new FileReader(logFile));
  }

  /**
   * Search the log file for the header section and extract the number of
   * sections
   */
  protected void findNSections() throws InterruptedException,
      NumberFormatException, IOException {

    //  Search for the number of sections, we should see a header ouput first
    boolean foundNSections = false;

    nSections = -1;
    while (!foundNSections) {
      Thread.sleep(updatePeriod);
      String line;
      while ((line = logFileReader.readLine()) != null) {
        if (line.startsWith(" Number of columns, rows, sections")) {
          String[] fields = line.split("\\s+");
          if (fields.length > 9) {
            nSections = Integer.parseInt(fields[9]);
            foundNSections = true;
            break;
          }
          else {
            throw new NumberFormatException("Incomplete size line in header");
          }
        }
      }
    }
  }

  /**
   * Update the progress bar with percentage done and estimated time to
   * completion message. 
   * @param percentage
   * @param remainingTime
   */
  private void updateProgressBar() {

    //  Calculate the percetage done
    double fractionDone = (double) currentSection / nSections;
    int percentage = (int) Math.round(fractionDone * 100);
    if (percentage < 0) {
      percentage = 0;
    }
    if (percentage > 99) {
      percentage = 99;
    }

    // Convert the remainingTime to minutes and seconds
    int minutes = (int) Math.floor(remainingTime / 60000);
    int seconds = (int) Math.floor((remainingTime - minutes * 60000) / 1000.0);

    // Format the progress bar string
    String message = String.valueOf(percentage) + "%   ETC: "
        + Utilities.millisToMinAndSecs(remainingTime);

    applicationManager.setProgressBarValue(currentSection, message, axisID);
  }

  /**
   * Calculate the amount of time remaining, this can be overridden if the
   * derived class has a better way to calculate.
   *
   */
  private void calcRemainingTime() {
    double fractionDone = (double) currentSection / nSections;
    long elapsedTime = System.currentTimeMillis() - processStartTime;
    remainingTime = (int) (elapsedTime / fractionDone - elapsedTime);
  }
}