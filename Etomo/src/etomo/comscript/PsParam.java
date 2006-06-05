package etomo.comscript;

import java.util.ArrayList;

import etomo.type.Time;

/**
 * <p>Description:
 * Class which represents the ps command.  Can run build a ps command string and
 * parse ps command output.</p>
 * 
 * <p>Copyright: Copyright 2006</p>
 *
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 */
public final class PsParam {
  public static final String rcsid = "$Id$";

  private static final String PID_HEADER = "PID";
  private static final String PARENT_PID_HEADER = "PPID";
  private static final String GROUP_PID_HEADER = "PGID";
  private static final String START_TIME_HEADER = "STARTED";
  private static final String PWD_TAG = "PWD=";

  private final ArrayList command = new ArrayList();
  private final ArrayList valuesArray = new ArrayList();

  private String[] output = null;

  private int pidStartIndex = -1;
  private int pidEndIndex = -1;
  private int groupPidStartIndex = -1;
  private int groupPidEndIndex = -1;
  private int startTimeStartIndex = -1;
  private int startTimeEndIndex = -1;

  /**
   * Builds the ps commmand.  Uses the pid to limit the output to one process.
   * @param pid
   */
  public PsParam(String pid) {
    command.add("ps");
    command.add("-p");
    command.add(pid);
    command.add("-o");
    command.add("pid,pgid,lstart");
  }
  
  public Row getRow() {
    return new Row(this);
  }

  public ArrayList getCommandArray() {
    return command;
  }

  /**
   * Sets the output of the ps command.  Sets the indices of fields that can be
   * parsed.
   * @param output
   */
  public void setOutput(String[] output) {
    this.output = output;
    valuesArray.clear();
    if (output == null || output.length < 2) {
      return;
    }
    //set the indexes based on the header
    pidStartIndex = 0;
    pidEndIndex = output[0].indexOf(PID_HEADER) + PID_HEADER.length();
    groupPidStartIndex = pidEndIndex + 1;
    groupPidEndIndex = output[0].indexOf(GROUP_PID_HEADER)
        + GROUP_PID_HEADER.length();
    startTimeStartIndex = groupPidEndIndex + 1;
    startTimeEndIndex = output[0].indexOf(START_TIME_HEADER)
        + START_TIME_HEADER.length();
    loadValues();
  }

  /**
   * Loads the output of the ps command into valuesArray.
   */
  private void loadValues() {
    if (valuesArray.size() > 0 || output == null) {
      return;
    }
    for (int i = 1; i < output.length; i++) {
      //System.out.println(output[i]);
      if (output[i] != null)
        if (output[i] != null && output[i].length() >= startTimeEndIndex) {
          valuesArray.add(new Values(output[i]));
        }
    }
  }

  /**
   * @return the index of the row in the ps output where the pid is found.
   * @param pid
   */
  int findRow(String pid) {
    for (int i = 0; i < valuesArray.size(); i++) {
      Values values = (Values) valuesArray.get(i);
      if (values.getPid().equals(pid)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * @return true if the pid, groupPid, and startTime are found on a row.
   * @param pid
   * @param groupPid
   * @param startTime
   */
  public boolean findRow(String pid, String groupPid, Time startTime) {
    for (int i = 0; i < valuesArray.size(); i++) {
      Values values = (Values) valuesArray.get(i);
      if (values.getPid().equals(pid) && values.getGroupPid().equals(groupPid)
          && values.getStartTime().almostEquals(startTime)) {
        return true;
      }
    }
    return false;
  }
  
  String getGroupPid(int rowIndex) {
    if (rowIndex == -1||valuesArray.size() <= rowIndex) {
      return null;
    }
    return ((Values) valuesArray.get(rowIndex)).getGroupPid();
  }

  Time getStartTime(int rowIndex) {
    if (rowIndex == -1||valuesArray.size() <= rowIndex) {
      return null;
    }
    return ((Values) valuesArray.get(rowIndex)).getStartTime();
  }

  int getPidStartIndex() {
    return pidStartIndex;
  }

  int getPidEndIndex() {
    return pidEndIndex;
  }

  int getGroupPidStartIndex() {
    return groupPidStartIndex;
  }

  int getGroupPidEndIndex() {
    return groupPidEndIndex;
  }

  int getStartTimeStartIndex() {
    return startTimeStartIndex;
  }

  int getStartTimeEndIndex() {
    return startTimeEndIndex;
  }

  private final class Values {
    private final String row;

    private String pid = null;
    private String groupPid = null;
    private Time startTime = null;

    private Values(String row) {
      this.row = row;
    }

    String getPid() {
      if (pid != null) {
        return pid;
      }
      pid = row.substring(getPidStartIndex(), getPidEndIndex()).trim();
      return pid;
    }

    String getGroupPid() {
      if (groupPid != null) {
        return groupPid;
      }
      groupPid = row.substring(getGroupPidStartIndex(), getGroupPidEndIndex())
          .trim();
      return groupPid;
    }

    Time getStartTime() {
      if (startTime != null) {
        return startTime;
      }
      startTime = new Time(row.substring(getStartTimeStartIndex(),
          getStartTimeEndIndex()).trim());
      return startTime;
    }
  }

  public class Row {
    private final PsParam psParam;
    
    private int index = -1;
    
    Row(PsParam psParam) {
      this.psParam = psParam;
    }
    
    /**
     * Find the process which matches the pid.
     * @param command
     * @param pid
     * @return
     */
    public boolean find(String pid) {
      index = psParam.findRow(pid);
      if (index != -1) {
        return true;
      }
      return false;
    }
    
    public String getGroupPid() {
      return psParam.getGroupPid(index);
    }

    public Time getStartTime() {
      return psParam.getStartTime(index);
    }
  }
}
/**
 * <p> $Log$ </p>
 */
