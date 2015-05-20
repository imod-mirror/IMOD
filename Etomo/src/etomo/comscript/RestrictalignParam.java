package etomo.comscript;

import java.io.File;

import etomo.BaseManager;
import etomo.type.AxisID;
import etomo.type.EtomoNumber;
import etomo.type.FileType;
import etomo.type.ProcessName;

/**
 * <p>Description: Contains parameters and command string for restrictalign. </p>
 * <p/>
 * <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class RestrictalignParam implements Command {
  private static final ProcessName PROCESS_NAME = ProcessName.RESTRICTALIGN;

  private EtomoNumber targetMeasurementRatio = new EtomoNumber(EtomoNumber.Type.DOUBLE,
    "TargetMeasurementRatio");
  private EtomoNumber minMeasurementRatio = new EtomoNumber(EtomoNumber.Type.DOUBLE,
    "MinMeasurementRatio");

  private final BaseManager manager;
  private final AxisID axisID;
  private final File alignComscriptFile;

  private String[] commandArray = null;

  public RestrictalignParam(final BaseManager manager, final AxisID axisID) {
    this.manager = manager;
    this.axisID = axisID;
    alignComscriptFile = FileType.ALIGN_COMSCRIPT.getFile(manager, axisID);
  }

  public String[] getCommandArray() {
    if (commandArray != null) {
      return commandArray;
    }
    commandArray =
      new String[] { "python", "-u", BaseManager.getIMODBinPath() + PROCESS_NAME,
        "-AlignCommandFile", alignComscriptFile.getName(),
        "-" + targetMeasurementRatio.getName(), targetMeasurementRatio.toString(),
        "-" + minMeasurementRatio.getName(), minMeasurementRatio.toString() };
    return commandArray;
  }

  public void setTargetMeasurementRatio(final String input) {
    targetMeasurementRatio.set(input);
  }

  public void setMinMeasurementRatio(final String input) {
    minMeasurementRatio.set(input);
  }

  public AxisID getAxisID() {
    return AxisID.ONLY;
  }

  public String getCommand() {
    return PROCESS_NAME.toString();
  }

  public File getCommandInputFile() {
    return new File(alignComscriptFile.getAbsolutePath());
  }

  /**
   * Not for running the command.  Use getCommandArray.
   */
  public String getCommandLine() {
    if (commandArray == null) {
      return PROCESS_NAME.toString();
    }
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < commandArray.length; i++) {
      buffer.append(commandArray[i] + " ");
    }
    return buffer.toString();
  }

  public CommandMode getCommandMode() {
    return null;
  }

  public String getCommandName() {
    return PROCESS_NAME.toString();
  }

  public File getCommandOutputFile() {
    return new File(alignComscriptFile.getAbsolutePath());
  }

  public FileType getOutputImageFileType() {
    return null;
  }

  public FileType getOutputImageFileType2() {
    return null;
  }

  public ProcessName getProcessName() {
    return PROCESS_NAME;
  }

  public CommandDetails getSubcommandDetails() {
    return null;
  }

  public String getSubcommandProcessName() {
    return null;
  }

  public boolean isMessageReporter() {
    return false;
  }
}
