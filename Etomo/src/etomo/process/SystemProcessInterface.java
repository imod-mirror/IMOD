package etomo.process;

import etomo.type.ProcessEndState;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright (c) 2002 - 2005</p>
*
* <p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
* University of Colorado</p>
* 
* @author $Author$
* 
* @version $Revision$
*/

public interface SystemProcessInterface {
  public static  final String  rcsid =  "$Id$";
  
  public String[] getStdOutput();
  /**
   * Get standard output while the process is running
   * @return
   */
  public String[] getCurrentStdOutput();
  public String[] getStdError();
  public boolean isStarted();
  public boolean isDone();
  public String getShellProcessID();
  public void notifyKilled();
  public void setProcessEndState(ProcessEndState endState);
}
/**
* <p> $Log$
* <p> Revision 3.2  2005/07/26 21:46:49  sueh
* <p> bug# 701 Changed notifyKill() to notifyKilled().  Added
* <p> setProcessendState().
* <p> </p>
*/