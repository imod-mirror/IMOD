package etomo.storage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import etomo.ApplicationManager;
import etomo.process.AlignLogGenerator;
import etomo.type.AxisID;
import etomo.type.FileType;
import etomo.type.ProcessName;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright 2008 - 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 * 
 * <p> $Log$
 * <p> Revision 1.6  2010/03/19 21:59:44  sueh
 * <p> bug# 1335 Class can't be a n'ton because the dataset tabs.
 * <p>
 * <p> Revision 1.5  2010/02/17 04:49:31  sueh
 * <p> bug# 1301 Using the manager instead of the manager key do pop up
 * <p> messages.
 * <p>
 * <p> Revision 1.4  2009/10/19 15:24:05  sueh
 * <p> bug# 1247 In getLogMessage added the global ratio.
 * <p>
 * <p> Revision 1.3  2009/03/17 00:45:24  sueh
 * <p> bug# 1186 Pass managerKey to everything that pops up a dialog.
 * <p>
 * <p> Revision 1.2  2009/02/19 18:20:23  sueh
 * <p> bug# 1179 Excluding local area output lines.
 * <p>
 * <p> Revision 1.1  2009/02/04 23:28:52  sueh
 * <p> bug# 1158 Class representing the taError.log file.  Used to send entries to
 * <p> LogPanel.
 * <p> </p>
 */
public final class TaErrorLog implements Loggable {
  public static final String rcsid = "$Id$";

  private final ArrayList<String> lineList = new ArrayList<String>();

  private final String userDir;
  private final ApplicationManager manager;
  private final AxisID axisID;

  private TaErrorLog(String userDir, final ApplicationManager manager, AxisID axisID) {
    this.userDir = userDir;
    this.manager = manager;
    this.axisID = axisID;
  }

  /**
   * @param userDir
   * @param axisID
   * @param processName
   * @return
   */
  public static TaErrorLog getInstance(String userDir, final ApplicationManager manager,
    AxisID axisID) {
    TaErrorLog instance = new TaErrorLog(userDir, manager, axisID);
    instance.createLog();
    return instance;
  }

  private void createLog() {
    FileType taLog = FileType.ALIGN_ERROR_LOG;
    if (!taLog.exists(manager, axisID)
      || taLog.lastModified(manager, axisID) < FileType.TILT_ALIGN_LOG.lastModified(
        manager, axisID)) {
      manager.generateAlignLogs(axisID);
    }
  }

  public String getName() {
    return ProcessName.ALIGN.toString();
  }

  /**
   * Get a message to be logged in the LogPanel.
   */
  public ArrayList<String> getLogMessage() throws LogFile.LockException, FileNotFoundException,
    IOException {
    lineList.clear();
    // refresh the log file
    LogFile taErrorLog =
      LogFile.getInstance(userDir, axisID, AlignLogGenerator.ERROR_LOG_NAME);
    if (taErrorLog.exists()) {
      LogFile.ReaderId readerId = taErrorLog.openReader();
      if (readerId != null && !readerId.isEmpty()) {
        String line = taErrorLog.readLine(readerId);
        boolean globalRatioFound = false;
        while (line != null) {
          if (!globalRatioFound
            && line.trim().startsWith("Ratio of total measured values to all unknowns")) {
            globalRatioFound = true;
            lineList.add(line);
          }
          else if (line.trim().startsWith("Residual error mean and sd")
            && line.indexOf("Local area") == -1) {
            lineList.add(line);
          }
          else if (line.trim().startsWith("Residual error local mean")) {
            lineList.add(line);
          }
          line = taErrorLog.readLine(readerId);
        }
        taErrorLog.closeRead(readerId);
      }
    }
    return lineList;
  }
}
