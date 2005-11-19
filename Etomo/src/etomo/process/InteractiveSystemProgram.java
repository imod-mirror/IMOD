package etomo.process;
import java.io.*;

import etomo.BaseManager;
import etomo.comscript.ProcessDetails;
import etomo.type.AxisID;
import etomo.type.ConstEtomoNumber;
import etomo.type.EtomoNumber;

/*
 * <p>Description: InteractiveSystemProgram implements a Runable class that can
 * execute an arbitrary program under the host operating system.</p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Organization: Boulder Laboratory for 3D Fine Structure,
 * University of Colorado</p>
 *
 * @author $Author$
 *
 * @version $Revision$
 *
 * <p> $Log$
 * <p> Revision 3.5  2005/07/29 00:52:00  sueh
 * <p> bug# 709 Going to EtomoDirector to get the current manager is unreliable
 * <p> because the current manager changes when the user changes the tab.
 * <p> Passing the manager where its needed.
 * <p>
 * <p> Revision 3.4  2005/04/25 20:47:39  sueh
 * <p> bug# 615 Passing the axis where a command originates to the message
 * <p> functions so that the message will be popped up in the correct window.
 * <p> This requires adding AxisID to many objects.
 * <p>
 * <p> Revision 3.3  2005/01/08 01:48:02  sueh
 * <p> bug# 578 Command interface has changed - update calls.
 * <p>
 * <p> Revision 3.2  2004/12/04 00:58:22  sueh
 * <p> bug# 569 Handling directory paths with spaces:  converting from a
 * <p> command line to a command array to prevent the command line from
 * <p> being split on white space.
 * <p>
 * <p> Revision 3.1  2004/11/19 23:22:01  sueh
 * <p> bug# 520 merging Etomo_3-4-6_JOIN branch to head.
 * <p>
 * <p> Revision 3.0.6.5  2004/11/16 02:21:59  sueh
 * <p> bug# 520 Replacing EtomoInteger, EtomoDouble, EtomoFloat, and
 * <p> EtomoLong with EtomoNumber.
 * <p>
 * <p> Revision 3.0.6.4  2004/11/12 22:53:53  sueh
 * <p> bug# 520 Removed pass-through commands to Command.  Added
 * <p> Command getCommand().
 * <p>
 * <p> Revision 3.0.6.3  2004/10/28 17:05:51  sueh
 * <p> bug# 520 Rename oldOutputFileTime to outputFileLastModified.
 * <p>
 * <p> Revision 3.0.6.2  2004/10/21 02:43:00  sueh
 * <p> bug# 520 Created a constructor which takes a Command interface.  This
 * <p> lets ProcessManager and JoinProcessManager do non-generic post
 * <p> processing for this program type.  Added functions to be used during post
 * <p> processing.
 * <p>
 * <p> Revision 3.0.6.1  2004/10/11 02:03:07  sueh
 * <p> bug# 520 Using a variable called propertyUserDir instead of the "user.dir"
 * <p> property.  This property would need a different value for each manager.
 * <p> This variable can be retrieved from the manager if the object knows its
 * <p> manager.  Otherwise it can retrieve it from the current manager using the
 * <p> EtomoDirector singleton.  If there is no current manager, EtomoDirector
 * <p> gets the value from the "user.dir" property.
 * <p>
 * <p> Revision 3.0  2003/11/07 23:19:00  rickg
 * <p> Version 1.0.0
 * <p>
 * <p> Revision 2.2  2003/03/20 17:26:48  rickg
 * <p> Comment update
 * <p>
 * <p> Revision 2.1  2003/01/30 04:54:24  rickg
 * <p> Comments
 * <p>
 * <p> Revision 2.0  2003/01/24 20:30:31  rickg
 * <p> Single window merge to main branch
 * <p>
 * <p> Revision 1.2  2002/10/07 22:25:48  rickg
 * <p> removed unused imports
 * <p> reformat after emacs messed it up
 * <p>
 * <p> Revision 1.1  2002/09/17 23:22:58  rickg
 * <p> Complete basic operation
 * <p>
 * <p> </p>
 */
public class InteractiveSystemProgram implements Runnable {
  public static final String rcsid =
    "$Id$";

  private BaseProcessManager processManager = null;
  private String threadName = null;
  private EtomoNumber outputFileLastModified = new EtomoNumber(EtomoNumber.LONG_TYPE, "");
  private final BaseManager manager;
  
  /**
   * The exit value of the command
   */
  private int exitValue = Integer.MIN_VALUE;
  private AxisID axisID;

  /**
   * The command to run
   */
  private String command = null;
  private String[] commandArray = null;
  private ProcessDetails processDetails = null;

  /**
   * The buffered IO streams connecting to the commmand.
   */
  BufferedWriter inputBuffer;
  BufferedReader outputBuffer;
  BufferedReader errorBuffer;

  private File workingDirectory = null;

  private String exceptionMessage = "";

  /**
   * Creates a SystemProgram object to execute the program specified by the
   * argument <i>command</i>
   * @param command The string containng the command to run
   */
  public InteractiveSystemProgram(BaseManager manager, String command, AxisID axisID) {
    this.manager = manager;
    this.axisID = axisID;
    this.command = command;
  }
  
  /**
   * Creates a SystemProgram object to execute the program specified by the
   * argument <i>command</i>
   * @param command The string containng the command to run
   */
  public InteractiveSystemProgram(BaseManager manager, String[] commandArray, AxisID axisID) {
    this.manager = manager;
    this.axisID = axisID;
    this.commandArray = commandArray;
  }
  
  public InteractiveSystemProgram(BaseManager manager, ProcessDetails processDetails,
      BaseProcessManager processManager, AxisID axisID) {
    this.manager = manager;
    this.axisID = axisID;
    this.processManager = processManager;
    this.processDetails = processDetails;
    
    if (processDetails == null) {
      throw new IllegalArgumentException("CommandParam is null.");
    }
    commandArray = processDetails.getCommandArray();
    command = processDetails.getCommandLine();
  }
  
  public AxisID getAxisID() {
    return axisID;
  }
  
  public void setName(String threadName) {
    this.threadName = threadName;
  }

  /**
   * Specify the directory in which the program should be run.  If the working
   * directory is not specified then current user.dir is the default.
   * @param workingDirectory a File object specifying the working directory
   */
  public void setWorkingDirectory(File workingDirectory) {
    this.workingDirectory = workingDirectory;
  }
  
  public String getCommandLine() {
    return command;
  }
  
  public String getCommandName() {
    if (processDetails == null) {
      return null;
    }
    return processDetails.getCommandName();
  }

  /**
   * Execute the command.
   */
  public void run() {

    //  Setup the Process object and run the command
    Process process = null;
    File outputFile = null;
    if (processDetails != null) {
      outputFile = processDetails.getCommandOutputFile();
      outputFileLastModified.set(outputFile.lastModified());
    }
    try {
      if (workingDirectory == null) {
        File currentUserDirectory = new File(manager
            .getPropertyUserDir());
        if (commandArray != null) {
          process = Runtime.getRuntime().exec(commandArray, null,
              currentUserDirectory);
        }
        else {
          process = Runtime.getRuntime().exec(command, null,
              currentUserDirectory);
        }
      }
      else {
        if (commandArray != null) {
          process = Runtime.getRuntime().exec(commandArray, null,
              workingDirectory);
        }
        else {
          process = Runtime.getRuntime().exec(command, null, workingDirectory);
        }
      }

      //  Create a buffered writer to handle the stdin, stdout and stderr
      //  streams of the process
      OutputStream cmdIn = process.getOutputStream();
      OutputStreamWriter cmdInWriter = new OutputStreamWriter(cmdIn);
      inputBuffer = new BufferedWriter(cmdInWriter);

      InputStream cmdOut = process.getInputStream();
      InputStreamReader cmdOutReader = new InputStreamReader(cmdOut);
      outputBuffer = new BufferedReader(cmdOutReader);

      InputStream cmdErr = process.getErrorStream();
      InputStreamReader cmdErrReader = new InputStreamReader(cmdErr);
      errorBuffer = new BufferedReader(cmdErrReader);

    }

    // TOD need better error handling, what should be the state if an
    // exception is thrown i.e. the exitValue
    // we can't rethrow the exception because it is a runnable implementation
    catch (IOException except) {
      except.printStackTrace();
      exceptionMessage = except.getMessage();
    }

    try {
      process.waitFor();
      exitValue = process.exitValue();
    }
    catch (InterruptedException except) {
      except.printStackTrace();
      exceptionMessage = except.getMessage();
    }

    if (processManager != null) {
      processManager.msgInteractiveSystemProgramDone(this, exitValue);
    }
  }

  /**
   * Send text to the program's standard input
   * @param line the string to send to the program's standard input
   */
  public void writeStdin(String line) {
    try {
      if (inputBuffer != null) {
        inputBuffer.write(line);
        inputBuffer.newLine();
        inputBuffer.flush();
      }
    }
    catch (IOException except) {
      except.printStackTrace();
      exceptionMessage = except.getMessage();
    }
  }

  /**
   * Read one line from the stdout buffer if available, if one isn't available
   * then null is returned
   */
  public String readStdout() {
    String line = null;
    try {
      if (outputBuffer != null && outputBuffer.ready()) {
        line = outputBuffer.readLine();
      }
    }
    catch (IOException except) {
      except.printStackTrace();
      exceptionMessage = except.getMessage();
    }
    return line;
  }
  
  public ProcessDetails getProcessDetails() {
    return processDetails;
  }
  
  public ConstEtomoNumber getOutputFileLastModified() {
    return outputFileLastModified;
  }

  /**
   * Read one line from the stderr buffer if available, if one isn't available
   * then null is returned
   */
  public String readStderr() {
    String line = null;
    try {
      if (errorBuffer != null && errorBuffer.ready()) {
        line = errorBuffer.readLine();
      }
    }
    catch (IOException except) {
      except.printStackTrace();
      exceptionMessage = except.getMessage();
    }
    return line;
  }

  public int getExitValue() {
    return exitValue;
  }
  
  public String getName() {
    return threadName;
  }

  public String getExceptionMessage() {
    return exceptionMessage;
  }
}
