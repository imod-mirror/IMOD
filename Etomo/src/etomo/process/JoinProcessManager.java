package etomo.process;

import java.io.File;
import java.io.IOException;

import etomo.BaseManager;
import etomo.EtomoDirector;
import etomo.JoinManager;
import etomo.comscript.FinishjoinParam;
import etomo.comscript.FlipyzParam;
import etomo.comscript.MakejoincomParam;
import etomo.comscript.MidasParam;
import etomo.comscript.XfalignParam;
import etomo.type.AxisID;
import etomo.type.ConstEtomoLong;
import etomo.util.Utilities;

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
* <p> Revision 1.1.2.6  2004/10/21 02:44:29  sueh
* <p> bug# 520 Added finishJoin and midasSample.  Added post processing for
* <p> midas and xfalign.
* <p>
* <p> Revision 1.1.2.5  2004/10/18 19:08:50  sueh
* <p> bug# 520 Added misdasSample.  Added getManager().
* <p>
* <p> Revision 1.1.2.4  2004/10/18 17:58:26  sueh
* <p> bug# 520 Added xfalign.
* <p>
* <p> Revision 1.1.2.3  2004/10/08 15:59:59  sueh
* <p> bug# 520 Fixed makejoincom() to that it used BackgroundProcess.
* <p> Added startjoin.
* <p>
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

  JoinManager joinManager;
  
  public JoinProcessManager(JoinManager joinMgr) {
    super();
    joinManager = joinMgr;
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
   * Run finishjoin
   */
  public String finishjoin(FinishjoinParam finishjoinParam)
      throws SystemProcessException {
    BackgroundProcess backgroundProcess = startBackgroundProcess(
        finishjoinParam.getCommandArray(), AxisID.ONLY);
    return backgroundProcess.getName();
  }
  
  /**
   * Run xfalign
   */
  public String xfalign(XfalignParam xfalignParam)
      throws SystemProcessException {
    BackgroundProcess backgroundProcess = startBackgroundProcess(xfalignParam,
        AxisID.ONLY);
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
  
  /**
   * Run midas on the sample file.
   */
  public String midasSample(MidasParam midasParam) throws SystemProcessException {
    InteractiveSystemProgram program = startInteractiveSystemProgram(midasParam);
    return program.getName();
  }

  
  protected void comScriptPostProcess(ComScriptProcess script, int exitValue) {
  }
  
  protected void backgroundPostProcess(BackgroundProcess process) {
    String commandName = process.getCommandName();
    if (commandName == null) {
      return;
    }
    if (commandName.equals(FlipyzParam.getName())) {
      joinManager.addSection(process.getOutputFile());
    }
    if (commandName.equals(XfalignParam.getName())) {
      joinManager.enableMidas();
    }
  }
  
  protected void backgroundErrorProcess(BackgroundProcess process) {
    String commandName = process.getCommandName();
    if (commandName == null) {
      return;
    }
    if (commandName.equals(XfalignParam.getName())) {
      joinManager.enableMidas();
    }
  }

  protected void interactiveSystemProgramPostProcess(
      InteractiveSystemProgram program) {
    String commandName = program.getCommandName();
    if (commandName == null) {
      return;
    }
    if (commandName.equals(MidasParam.getName())) {
      File outputFile = program.getOutputFile();
      if (outputFile != null) {
        ConstEtomoLong oldOutputFileTime = program.getOldOutputFileTime();
        if (oldOutputFileTime.isSet()
            && !oldOutputFileTime.equals(outputFile.lastModified())) {
          try {
            Utilities.copyFile(outputFile, new File(outputFile.getAbsolutePath()
                + ".bak"));
          }
          catch (IOException e) {
            e.printStackTrace();
            String outputFileName = outputFile.getName();
            String[] message = {
                "Unable to backup " + outputFile.getAbsolutePath() + ".",
                "Copy " + outputFileName + " to " + outputFileName + ".bak",
                "before using Automatic Alignment or Revert." };
            EtomoDirector.getInstance().getMainFrame().openMessageDialog(
                message, "WARNING!  Unable to Backup");
          }
        }
      }
    }
  }
  
  protected BaseManager getManager() {
    return joinManager;
  }
}
