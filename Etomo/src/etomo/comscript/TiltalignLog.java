package etomo.comscript;

import java.io.FileNotFoundException;
import java.io.IOException;

import etomo.BaseManager;
import etomo.storage.LogFile;
import etomo.type.AxisID;
import etomo.type.FileType;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright 2012</p>
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
public final class TiltalignLog {
  public static final String rcsid = "$Id:$";

  private LogFile log = null;

  private TiltalignLog() {
  }

  public static TiltalignLog getInstance(final BaseManager manager, final AxisID axisID) {
    TiltalignLog instance = new TiltalignLog();
    instance.init(manager, axisID);
    return instance;
  }

  /**
   * Initialize the log variable.  Set it to null if the initialization fails.
   */
  private void init(final BaseManager manager, final AxisID axisID) {
    if (log != null) {
      return;
    }
    try {
      log = LogFile.getInstance(FileType.TILT_ALIGN_LOG.getFile(manager, axisID));
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
      log = null;
    }
  }

  /**
   * Return true if the file exists
   * @return
   */
  public boolean exists() {
    if (log == null) {
      return false;
    }
    return log.exists();
  }

  /**
   * Return true if the .log files exists and ends with the sucess tag.
   * @return
   */
  public boolean isSuccess() {
    if (log == null) {
      return false;
    }
    LogFile.BigBufferReaderId id = null;
    try {
      id = log.openFileChannelReader();
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
      return false;
    }
    catch (LogFile.LockException e) {
      return false;
    }
    if (id == null || id.isEmpty()) {
      return false;
    }
    try {
      return log.searchForLastLine(id, "SUCCESSFULLY COMPLETED");
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Searches the log file for the exclude list parameter and returns it, or null if it is
   * not found.
   * @return
   */
  String getExcludeList() {
    if (log == null) {
      return null;
    }
    LogFile.ReaderId id;
    try {
      id = log.openReader();
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
      return null;
    }
    if (id == null || id.isEmpty()) {
      return null;
    }
    boolean foundParamList = false;
    try {
      String line = log.readLine(id).trim();
      while (line != null) {
        if (line.equals("*** End of entries ***")) {
          return null;
        }
        if (foundParamList) {
          if (line.indexOf(ConstTiltalignParam.EXCLUDE_LIST_KEY) != -1) {
            // May be the right parameter - make sure by matching it exactly
            String[] pair = line.split("\\s*=\\s*");
            if (pair != null && pair.length > 0
                && pair[0].equals(ConstTiltalignParam.EXCLUDE_LIST_KEY)) {
              if (pair.length > 1) {
                return pair[1];
              }
              else {
                return null;
              }
            }
          }
        }
        else if (line.equals("*** Entries to program tiltalign ***")) {
          foundParamList = true;
        }
        line = log.readLine(id).trim();
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
    }
    return null;
  }
}