package etomo.process;

import etomo.JoinManager;
import etomo.comscript.FlipyzParam;
import etomo.comscript.MakejoincomParam;
import etomo.type.AxisID;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright (c) 2002, 2003, 2004</p>
*
*<p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
* University of Colorado</p>
* 
* @author $Author$
* 
* @version $Revision$
* 
* <p> $Log$
* <p> Revision 1.1.2.2  2004/10/06 01:40:27  sueh
* <p> bug# 520 Added flipyz().  Added backgroundPostProcess() to handle non-
* <p> generic processing after BackgroundProcess is done.
* <p>
* <p> Revision 1.1.2.1  2004/09/29 17:54:52  sueh
* <p> bug# 520 Process manager for serial sections.
* <p> </p>
*/
public class JoinProcessManager extends BaseProcessManager {
  public static final String rcsid = "$Id$";

  public JoinProcessManager(JoinManager joinMgr) {
    super(joinMgr);
  }
  
  /**
   * Run makejoincom
   */
  public String makejoincom(MakejoincomParam makejoincomParam)
      throws SystemProcessException {
    BackgroundProcess backgroundProcess = startBackgroundProcess(
        makejoincomParam.getCommandArray(), AxisID.ONLY);
    return backgroundProcess.getName();
  }
  
  /**
   * Run flip
   */
  public String flipyz(FlipyzParam flipyzParam)
    throws SystemProcessException {
    BackgroundProcess backgroundProcess = startBackgroundProcess(flipyzParam, AxisID.ONLY);
    return backgroundProcess.getName();
  }
  
  /**
   * Run the startjoin com file
   */
  public String startjoin() throws SystemProcessException {
    String command = "startjoin.com";
    ComScriptProcess comScriptProcess = startComScript(command,
      null, AxisID.ONLY);
    return comScriptProcess.getName();
  }
  
  protected void comScriptPostProcess(ComScriptProcess script, int exitValue) {
  }
  
  protected void backgroundPostProcess(BackgroundProcess process) {
    String commandName = process.getCommandName();
    if (commandName != null && commandName.equals(FlipyzParam.getName())) {
      ((JoinManager) manager).addSection(process.getOutputFile());
    }
  }
}
