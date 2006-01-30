package etomo.comscript;

import java.util.ArrayList;

import etomo.BaseManager;
import etomo.type.AxisID;
import etomo.type.ProcessName;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright (c) 2005</p>
*
* <p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
* University of Colorado</p>
* 
* @author $Author$
* 
* @version $Revision$
* 
*/
public final class TomosnapshotParam implements Command {
  public static  final String  rcsid =  "$Id$";
  
  public static final String OUTPUT_LINE = "Snapshot done";
  private static final String COMMAND_NAME = ProcessName.TOMOSNAPSHOT.toString();

  private final AxisID axisID;
  private final BaseManager manager;
  
  private String[] commandArray = null;
  
  public TomosnapshotParam(BaseManager manager, AxisID axisID) {
    this.axisID = axisID;
    this.manager = manager;
  }
  
  public final String[] getCommand() {
    if (commandArray == null) {
      buildCommand();
    }
    return commandArray;
  }
  
  private final void buildCommand() {
    ArrayList command = new ArrayList();
    command.add(COMMAND_NAME);
    command.add("-e");
    command.add(manager.getBaseMetaData().getMetaDataFileName());
    int commandSize = command.size();
    commandArray = new String[commandSize];
    for (int i = 0; i < commandSize; i++) {
      commandArray[i] = (String) command.get(i);
    }
  }
  
  public final String getCommandName() {
    return COMMAND_NAME;
  }
  
  public final String getCommandLine() {
    getCommand();
    if (commandArray == null) {
      return null;
    }
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < commandArray.length; i++) {
      buffer.append(commandArray[i] + ' ');
    }
    return buffer.toString();
  }
  
  public String[] getCommandArray() {
    return getCommand();
  }
}
/**
* <p> $Log$ </p>
*/