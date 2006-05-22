package etomo.comscript;

import java.io.File;

import etomo.BaseManager;
import etomo.type.AxisID;
import etomo.util.Utilities;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright (c) 2005 - 2006</p>
*
*<p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
* University of Colorado</p>
* 
* @author $Author$
* 
* @version $Revision$
*/
public class ArchiveorigParam implements Command {
  public static  final String  rcsid =  "$Id$";
  
  public static final String COMMAND_NAME = "archiveorig";
  public static final int AXIS_A_MODE = -1;
  public static final int AXIS_B_MODE = -2;
  public static final int AXIS_ONLY_MODE = -3;
  
  private String[] commandArray;
  private int mode = AXIS_ONLY_MODE;
  private File outputFile;
  private final BaseManager manager;
  
  public ArchiveorigParam(BaseManager manager, AxisID axisID) {
    this.manager = manager;
    if (axisID == AxisID.FIRST) {
      mode = AXIS_A_MODE;
    }
    else if (axisID == AxisID.SECOND) {
      mode = AXIS_B_MODE;
    }
    File stack = Utilities.getFile(manager, false, axisID, ".st", "");
    commandArray = new String[] { COMMAND_NAME, "-P", stack.getName() };
    outputFile = Utilities.getFile(manager, false, axisID, "_xray.st.gz", "");
  }
  
  public String[] getCommandArray() {
    return commandArray;
  }
  
  public String getCommandName() {
    return COMMAND_NAME;
  }
  
  public String getCommand() {
    return COMMAND_NAME;
  }
  
  public String getCommandLine() {
    if (commandArray == null) {
      return "";
    }
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < commandArray.length; i++) {
      buffer.append(commandArray[i] + " ");
    }
    return buffer.toString();
  }
  
  public int getCommandMode() {
    return mode;
  }
  
  public File getCommandOutputFile() {
    return outputFile;
  }
  
  public AxisID getAxisID() {
    return AxisID.ONLY;
  }
}
/**
* <p> $Log$
* <p> Revision 1.7  2006/05/11 19:33:59  sueh
* <p> bug# 838 Implement Command instead of CommandDetails
* <p>
* <p> Revision 1.6  2006/04/06 18:48:12  sueh
* <p> bug# 808 Implementing ProcessDetails.
* <p>
* <p> Revision 1.5  2006/01/20 20:45:00  sueh
* <p> updated copyright year
* <p>
* <p> Revision 1.4  2005/11/19 01:45:53  sueh
* <p> bug# 744 Moved functions only used by process manager post
* <p> processing and error processing from Commands to ProcessDetails.
* <p> This allows ProcesschunksParam to be passed to DetackedProcess
* <p> without having to add unnecessary functions to it.
* <p>
* <p> Revision 1.3  2005/07/29 00:42:44  sueh
* <p> bug# 709 Adding a EtomoDirector test harness so that unit test functions
* <p> can use package level EtomoDirector functions getCurrentManager and
* <p> setCurrentPropertyUserDir.  As long as the unit test doesn't open multiple
* <p> windows and switch to another tab, it is OK for it to get the current
* <p> manager from EtomoDirector.
* <p>
* <p> Revision 1.2  2005/07/26 17:08:27  sueh
* <p> bug# 701 Get the PID from archiveorig
* <p>
* <p> Revision 1.1  2005/05/18 22:31:38  sueh
* <p> bug# 662 A param object for archiveorig.
* <p> </p>
*/