package etomo.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import etomo.comscript.ComscriptState;
import etomo.type.AxisID;
import etomo.util.Utilities;

/**
 * <p>
 * Description: Provides a threadable class to execute IMOD com scripts in the
 * background.
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2004</p>
 * 
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
 * University of Colorado</p>
 * 
 * @author $$Author$$
 * 
 * @version $$Revision$$
 * 
 * <p> $Log$
 * <p> Revision 1.3  2004/08/20 21:39:45  sueh
 * <p> bug# 508 added parseWarning()
 * <p>
 * <p> $Revision 1.2  2004/08/19 01:45:03  sueh
 * <p> $bug# 508 Removed the -e when running combine.csh, because it
 * <p> $wasn't returning errors.  Added file renaming based the state of
 * <p> $combine.com.  Added parsed errors from combine.log and child .log
 * <p> $files based on the state of combine.com.  Added a function to kill the
 * <p> $process monitor.
 * <p> $Added:
 * <p> $BackgroundProcessMonitor backgroundProcessMonitor
 * <p> $ComscriptState comscriptState
 * <p> $BackgroundComScriptProcess(String comScript,
 * <p> $    ProcessManager processManager, AxisID axisID,
 * <p> $    String watchedFileName,
 * <p> $    BackgroundProcessMonitor backgroundProcessMonitor,
 * <p> $    ComscriptState comscriptState)
 * <p> $parseError()
 * <p> $renameFiles()
 * <p> $setKilled(boolean killed)
 * <p> $Changed:
 * <p> $execCsh(String[] commands)
 * <p> $makeRunCshFile(File runCshFile, String cshFileName,
 * <p> $    String outFileName)
 * <p> $Deleted:
 * <p> $BackgroundComScriptProcess(
 * <p> $    String comScript,
 * <p> $    ProcessManager processManager,
 * <p> $    AxisID axisID,
 * <p> $    String watchedFileName)
 * <p> $
 * <p> $Revision 1.1  2004/08/06 22:58:19  sueh
 * <p> $bug# 508 Runs comscripts in the background by placing
 * <p> $vmstocsh output in  .csh file, and running the .csh file with an
 * <p> $"&".  Also need to send output to a file.  In order to run the
 * <p> $.csh file with an "&", runing it from another .csh file, which is
 * <p> $generated by this object.
 * <p> </p>
 */
public class BackgroundComScriptProcess extends ComScriptProcess {
  public static final String rcsid = "$$Id$$";
  
  private BackgroundProcessMonitor backgroundProcessMonitor = null;
  private ComscriptState comscriptState;

  /**
   * @param comScript
   * @param processManager
   * @param axisID
   * @param watchedFileName
   */
  public BackgroundComScriptProcess(String comScript,
    ProcessManager processManager, AxisID axisID, String watchedFileName,
    BackgroundProcessMonitor backgroundProcessMonitor, 
    ComscriptState comscriptState) {
    super(comScript, processManager, axisID, watchedFileName);
    this.backgroundProcessMonitor = backgroundProcessMonitor;
    this.comscriptState = comscriptState;
  }
  
  protected void renameFiles() {
    super.renameFiles();
    int startCommand = comscriptState.getStartCommand();
    int endCommand = comscriptState.getEndCommand();
    int index = startCommand;
    while (index <= endCommand) {
      renameFiles(comscriptState.getCommand(index) + ".com", 
        comscriptState.getWatchedFile(index), workingDirectory);
      index++;
    }
  }

  /**
   * Places commmands in the .csh file.  Creates and runs a file containing
   * commands to execute the .csh file in the background.  
   */
  protected void execCsh(String[] commands) throws IOException,
      SystemProcessException {
    String runName = parseBaseName(name, ".com");
    String cshFileName = runName + ".csh";
    File cshFile = new File(workingDirectory, cshFileName);
    
    String runCshFileName = "run" + runName + ".csh";
    File runCshFile = new File(workingDirectory, runCshFileName);
    
    String outFileName = runName + ".out";
    File outFile = new File(workingDirectory, outFileName);
    
    Utilities.writeFile(cshFile, commands, true);
    makeRunCshFile(runCshFile, cshFileName, outFileName);
    
    // Do not use the -e flag for tcsh since David's scripts handle the failure 
    // of commands and then report appropriately.  The exception to this is the
    // com scripts which require the -e flag.  RJG: 2003-11-06 
    csh = 
      new BackgroundSystemProgram("tcsh -f " + runCshFile.getAbsolutePath(),
      backgroundProcessMonitor);
    csh.setWorkingDirectory(workingDirectory);
    csh.setDebug(debug);
    
    ParseBackgroundPID parsePID = 
        new ParseBackgroundPID(csh, cshProcessID, outFile);
    Thread parsePIDThread = new Thread(parsePID);
    parsePIDThread.start();
    
    csh.run();

    // Check the exit value, if it is non zero, parse the warnings and errors
    // from the log file.
    if (csh.getExitValue() != 0) {
      throw new SystemProcessException("");
    }
  }
  
  /**
   * create a csh file to run commandname.csh (created from commandname.com).
   * To avoid hangups when quitting Etomo or logging out, put nohup on the first
   * line and send the output to a file.
   * @param runCshFile
   * @param cshFileName
   * @param runName
   * @throws IOException
   */
  private void makeRunCshFile(File runCshFile, String cshFileName, String outFileName)
    throws IOException {
    if (runCshFile == null) {
      throw new IOException("unable to create " + runCshFile.getAbsolutePath());
    }
    if (runCshFile.exists()) {
      return;
    }
    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(runCshFile));
    if (bufferedWriter == null) {
      throw new IOException("unable to write to " + runCshFile.getAbsolutePath());
    }
    bufferedWriter.write("nohup");
    bufferedWriter.newLine();
    bufferedWriter.write("tcsh -f " + cshFileName + ">&" + outFileName + "&");
    bufferedWriter.newLine();
    bufferedWriter.close();
  }
  
  /**
   * set killed in the process monitor
   * @param killed
   */
  public void kill() {
    if (backgroundProcessMonitor != null) {
      backgroundProcessMonitor.kill();
    }
  }
  
  /**
   * Parses errors from log files.
   * Parses errors from the comscript and all child comscripts found in
   * comscriptState that may have been executed.
   */
  protected String[] parseError() throws IOException {
    ArrayList errors = parseError(name, true);
    int startCommand = comscriptState.getStartCommand();
    int endCommand = comscriptState.getEndCommand();
    int index = startCommand;
    while (index <= endCommand) {
      errors.addAll(
          parseError(comscriptState.getCommand(index) + ".com", false));
      index++;
    }
    return (String[]) errors.toArray(new String[errors.size()]);
  }
 
  /**
   * Parses warnings from log files.
   * Parses warnings from the comscript and all child comscripts found in
   * comscriptState that may have been executed.
   */
  protected String[] parseWarning() throws IOException {
    ArrayList errors = parseWarning(name, true);
    int startCommand = comscriptState.getStartCommand();
    int endCommand = comscriptState.getEndCommand();
    int index = startCommand;
    while (index <= endCommand) {
      errors.addAll(
          parseWarning(comscriptState.getCommand(index) + ".com", false));
      index++;
    }
    return (String[]) errors.toArray(new String[errors.size()]);
  }
 
}
