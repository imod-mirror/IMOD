package etomo.storage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import etomo.process.AlignLogGenerator;
import etomo.type.AxisID;
import etomo.type.ProcessName;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright 2008</p>
 *
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 * 
 * <p> $Log$ </p>
 */
public final class TaErrorLog implements Loggable {
  public static final String rcsid = "$Id$";

  private static TaErrorLog INSTANCE_A = null;
  private static TaErrorLog INSTANCE_B = null;

  private final List lineList = new ArrayList();

  private final String userDir;
  private final AxisID axisID;

  private TaErrorLog(String userDir, AxisID axisID) {
    this.userDir = userDir;
    this.axisID = axisID;
  }

  /**
   * Gets either INSTANCE_A or INSTANCE_B depending on the axisID.
   * @param userDir
   * @param axisID
   * @param processName
   * @return
   */
  public static TaErrorLog getInstance(String userDir, AxisID axisID) {
    if (axisID == AxisID.SECOND) {
      if (INSTANCE_B == null) {
        INSTANCE_B = new TaErrorLog(userDir, axisID);
      }
      return INSTANCE_B;
    }
    if (INSTANCE_A == null) {
      INSTANCE_A = new TaErrorLog(userDir, axisID);
    }
    return INSTANCE_A;
  }

  public String getName() {
    return ProcessName.ALIGN.toString();
  }

  /**
   * Get a message to be logged in the LogPanel.
   */
  public List getLogMessage() throws LogFile.LockException,
      FileNotFoundException, IOException {
    lineList.clear();
    //refresh the log file
    LogFile taErrorLog = LogFile.getInstance(userDir, axisID,
        AlignLogGenerator.ERROR_LOG_NAME);
    if (taErrorLog.exists()) {
      LogFile.ReaderId readerId = taErrorLog.openReader();
      if (readerId != null && !readerId.isEmpty()) {
        String line = taErrorLog.readLine(readerId);
        while (line != null) {
          if (line.trim().startsWith("Residual error")) {
            lineList.add(line);
          }
          line = taErrorLog.readLine(readerId);
        }
        taErrorLog.closeReader(readerId);
      }
    }
    return lineList;
  }
}
