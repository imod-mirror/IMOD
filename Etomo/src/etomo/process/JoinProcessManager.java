package etomo.process;

import java.io.IOException;

import etomo.JoinManager;
import etomo.comscript.BadComScriptException;
import etomo.comscript.FlipyzParam;
import etomo.comscript.Makejoincom;
import etomo.type.AxisID;
import etomo.type.ConstJoinMetaData;

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
* <p> Revision 1.1.2.1  2004/09/29 17:54:52  sueh
* <p> bug# 520 Process manager for serial sections.
* <p> </p>
*/
public class JoinProcessManager extends BaseProcessManager {
  public static final String rcsid = "$Id$";

  public JoinProcessManager(JoinManager joinMgr) {
    super(joinMgr);
  }
  
  public String startJoin(ConstJoinMetaData metaData)
      throws BadComScriptException, IOException, SystemProcessException {
    makejoincom(metaData);
    return startjoin();
  }
  
  private void makejoincom(ConstJoinMetaData metaData)
      throws BadComScriptException, IOException {

    Makejoincom makejoincom = new Makejoincom(metaData);

    int exitValue = makejoincom.run();

    if (exitValue != 0) {
      System.err.println("Exit value: " + String.valueOf(exitValue));

      //  Compile the exception message from the stderr stream
      String[] stdError = makejoincom.getStdError();
      if (stdError.length < 1) {
        stdError = new String[1];
        stdError[0] = "Get David to add some std error reporting to makejoincom";
      }
      StringBuffer buffer = new StringBuffer();
      buffer.append("makejoincom Error\n");
      buffer.append("Standard error output:\n");
      for (int i = 0; i < stdError.length; i++) {
        buffer.append(stdError[i]);
        buffer.append("\n");
      }

      throw (new BadComScriptException(buffer.toString()));
    }
  }
  
  /**
   * Run flip
   */
  public String flipyz(FlipyzParam flipyzParam)
    throws SystemProcessException {
    BackgroundProcess backgroundProcess = startBackgroundProcess(flipyzParam, AxisID.ONLY);
    return backgroundProcess.getName();
  }
  
  public String startjoin() throws SystemProcessException {
    String command = "startjoin.com";
    ComScriptProcess comScriptProcess = startComScript(command, null, AxisID.ONLY);
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
