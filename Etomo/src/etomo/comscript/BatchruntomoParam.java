package etomo.comscript;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import etomo.BaseManager;
import etomo.process.ProcessMessages;
import etomo.process.SystemProgram;
import etomo.storage.DirectiveFile;
import etomo.type.AxisID;
import etomo.type.EtomoNumber;
import etomo.type.ProcessName;
import etomo.type.StringParameter;
import etomo.ui.swing.UIHarness;
import etomo.util.RemotePath;
import etomo.util.RemotePath.InvalidMountRuleException;

/**
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright 2012 - 2014 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public class BatchruntomoParam implements CommandParam {
  private static final int VALIDATION_TYPE_BATCH_DIRECTIVE = 1;
  private static final int VALIDATION_TYPE_TEMPLATE = 2;
  private static final String CPU_MACHINE_LIST_TAG = "CPUMachineList";
  private static final String GPU_MACHINE_LIST_TAG = "GPUMachineList";
  public static final String MACHINE_LIST_LOCAL_VALUE = "1";
  private static final String CPU_MACHINE_LIST_DIVIDER = "#";
  private static final String GPU_MACHINE_LIST_DIVIDER = ":";

  private final List<String> command = new ArrayList<String>();
  private final EtomoNumber validationType = new EtomoNumber();

  private final StringParameter deliverToDirectory =
      new StringParameter("DeliverToDirectory");
  private final StringParameter niceValue = new StringParameter("NiceValue");
  private final StringParameter emailAddress = new StringParameter("EmailAddress");

  private final BaseManager manager;
  private final AxisID axisID;
  private final CommandMode mode;
  private final boolean doValidation;
  private final ArrayList[] interleavedParameters;

  private StringBuffer commandLine = null;
  private SystemProgram batchruntomo = null;
  private int exitValue = -1;
  private boolean valid = true;
  private StringBuilder cpuMachineList = null;
  private StringBuilder gpuMachineList = null;
  private Set<String> currentLocationValidationSet = null;
  private Set<String> rootNameValidationSet = null;
  private Set<String> requiredFilesValidationSet = null;

  private BatchruntomoParam(final BaseManager manager, final AxisID axisID,
      final CommandMode mode, final boolean doValidation) {
    this.manager = manager;
    this.axisID = axisID;
    this.mode = mode;
    this.doValidation = doValidation;
    if (mode == Mode.BATCH) {
      interleavedParameters = new ArrayList[InterleavedIndex.BATCH_LENGTH];
      interleavedParameters[InterleavedIndex.ROOT_NAME.index] = new ArrayList<String>();
      interleavedParameters[InterleavedIndex.CURRENT_LOCATION.index] =
          new ArrayList<String>();
    }
    else {
      interleavedParameters = new ArrayList[InterleavedIndex.VALIDATION_LENGTH];
    }
    interleavedParameters[InterleavedIndex.DIRECTIVE_FILE.index] =
        new ArrayList<String>();
  }

  public static BatchruntomoParam getInstance(final BaseManager manager,
      final AxisID axisID, final boolean doValidation) {
    return new BatchruntomoParam(manager, axisID, Mode.BATCH, doValidation);
  }

  public static BatchruntomoParam getValidationInstance(final BaseManager manager,
      final AxisID axisID) {
    return new BatchruntomoParam(manager, axisID, Mode.VALIDATION, false);
  }

  public void parseComScriptCommand(final ComScriptCommand scriptCommand)
      throws BadComScriptException, InvalidParameterException,
      FortranInputSyntaxException {
    // reset
    for (int i = 0; i < interleavedParameters.length; i++) {
      interleavedParameters[i].clear();
    }
    deliverToDirectory.reset();
    cpuMachineList = null;
    gpuMachineList = null;
    niceValue.reset();
    emailAddress.reset();
    // parse
    // rootName: based on .ebt file properties
    // currentLocation: based on .ebt file properties
    // directiveFile: based on .ebt file properties
    deliverToDirectory.parse(scriptCommand);
    cpuMachineList = new StringBuilder();
    cpuMachineList.append(scriptCommand.getValue(CPU_MACHINE_LIST_TAG));
    gpuMachineList = new StringBuilder();
    gpuMachineList.append(scriptCommand.getValue(GPU_MACHINE_LIST_TAG));
    niceValue.parse(scriptCommand);
    emailAddress.parse(scriptCommand);
  }

  public void updateComScriptCommand(final ComScriptCommand scriptCommand)
      throws BadComScriptException {
    scriptCommand.useKeywordValue();
    scriptCommand
        .setValuesInterleaved(InterleavedIndex.getTags(mode), interleavedParameters);
    deliverToDirectory.updateComScript(scriptCommand);
    if (cpuMachineList != null && cpuMachineList.length() > 0) {
      scriptCommand.setValue(CPU_MACHINE_LIST_TAG, cpuMachineList.toString());
    }
    if (gpuMachineList != null && gpuMachineList.length() > 0) {
      scriptCommand.setValue(GPU_MACHINE_LIST_TAG, gpuMachineList.toString());
    }
    niceValue.updateComScript(scriptCommand);
    emailAddress.updateComScript(scriptCommand);
    String remoteDirectory = null;
    try {
      remoteDirectory = RemotePath.INSTANCE
          .getRemotePath(manager, manager.getPropertyUserDir(), axisID);
    }
    catch (InvalidMountRuleException e) {
      UIHarness.INSTANCE.openMessageDialog(manager,
          "ERROR:  Remote path error.  " + "Unabled to run batchruntomo" + ".\n\n" +
              e.getMessage(), "Batchruntomo Error", axisID);
      valid = false;
    }
    if (remoteDirectory != null) {
      scriptCommand.setValue("", remoteDirectory);
    }
  }

  public void initializeDefaults() {
  }

  /**
   * @param directiveFile
   */
  public void addDirectiveFile(final File directiveFile) {
    if (directiveFile != null) {
      interleavedParameters[InterleavedIndex.DIRECTIVE_FILE.index]
          .add(directiveFile.getAbsolutePath());
    }
  }

  public void resetCPUMachineList() {
    cpuMachineList = null;
  }

  public void addCPUMachine(final String machine, final int number) {
    if (machine != null && !machine.matches("\\s*") && number > 0) {
      boolean first = false;
      if (cpuMachineList == null) {
        cpuMachineList = new StringBuilder();
        first = true;
      }
      if (!first) {
        cpuMachineList.append(",");
      }
      cpuMachineList.append(machine + CPU_MACHINE_LIST_DIVIDER + number);
    }
  }

  public Map<String, String> getCPUMachineMap() {
    if (cpuMachineList != null) {
      return convertToMachineMap(cpuMachineList, false);
    }
    return null;
  }

  public Map<String, String> getGPUMachineMap() {
    if (gpuMachineList != null) {
      return convertToMachineMap(gpuMachineList, true);
    }
    return null;
  }

  /**
   * Convert a CPU or GPU machine list string to a map containing computer and # of PUs.
   * Returns null if this is not a parallel processing list.
   *
   * @param machineList
   * @param gpu
   * @return
   */
  private static Map<String, String> convertToMachineMap(final StringBuilder machineList,
      final boolean gpu) {
    if (machineList != null && machineList.length() > 0) {
      String list = machineList.toString();
      if (!list.equals(MACHINE_LIST_LOCAL_VALUE)) {
        String[] machineArray = machineList.toString().split(",");
        if (machineArray != null && machineArray.length > 0) {
          Map<String, String> machineMap = new HashMap<String, String>();
          String divider = null;
          for (int i = 0; i < machineArray.length; i++) {
            String[] machine = null;
            //Try to set the divider.  Each element will use the same, or no divider.
            if (divider != null) {
              machine = machineArray[i].split(divider);
            }
            else if (machineArray[i].indexOf(CPU_MACHINE_LIST_DIVIDER) != -1) {
              divider = CPU_MACHINE_LIST_DIVIDER;
              machine = machineArray[i].split(divider);
            }
            else if (machineArray[i].indexOf(GPU_MACHINE_LIST_DIVIDER) != -1) {
              divider = GPU_MACHINE_LIST_DIVIDER;
              machine = machineArray[i].split(divider);
            }
            if (machine == null) {
              machineMap.put(machineArray[i], "1");
            }
            else {
              if (machine != null && machine.length > 1) {
                if (gpu) {
                  //for gpu put in the number of gpu ids.
                  //frodo:2,sam:1:2
                  machineMap.put(machine[0], Integer.toString(machine.length - 1));
                }
                else {
                  //for cpu, there should be only one number - the number of CPUs.
                  //frodo#4,sam#4
                  machineMap.put(machine[0], machine[1]);
                }
              }
              else {
                machineMap.put(machine[i], "1");
              }
            }
          }
          return machineMap;
        }
      }
    }
    return null;
  }

  public void resetGPUMachineList() {
    gpuMachineList = null;
  }

  public void addGPUMachine(final String machine, final int number,
      final String[] deviceArray) {
    if (machine != null && !machine.matches("\\s*") && number > 0) {
      boolean first = false;
      if (gpuMachineList == null) {
        gpuMachineList = new StringBuilder();
        first = true;
      }
      if (!first) {
        gpuMachineList.append(",");
      }
      gpuMachineList.append(machine);
      if (deviceArray != null) {
        for (int i = 0; i < number; i++) {
          gpuMachineList.append(GPU_MACHINE_LIST_DIVIDER + deviceArray[i]);
        }
      }
    }
  }

  public void setNiceValue(final Number input) {
    if (input == null) {
      niceValue.reset();
    }
    else {
      niceValue.set(input.toString());
    }
  }

  public String getNiceValue() {
    return niceValue.toString();
  }

  public boolean isDeliverToDirectoryNull() {
    return deliverToDirectory.isEmpty();
  }

  public String getDeliverToDirectory() {
    return deliverToDirectory.toString();
  }

  public void setDeliverToDirectory(final File input) {
    if (input != null) {
      deliverToDirectory.set(input.getAbsolutePath());
    }
  }

  public void resetDeliverToDirectory() {
    deliverToDirectory.reset();
  }

  public void errorCheck() {
    if (rootNameValidationSet != null) {
      rootNameValidationSet.clear();
    }
    if (requiredFilesValidationSet != null) {
      requiredFilesValidationSet.clear();
    }
  }

  public boolean addRootName(final String input, final boolean enforceUniqueness,
      final boolean dual, final StringBuilder errMsg) {
    if (mode != Mode.BATCH) {
      System.err.println(
          "Warning: attempting to set a root name in a non-batch mode batchruntomo command");
      return false;
    }
    boolean retval = true;
    if (doValidation && enforceUniqueness) {
      if (rootNameValidationSet == null) {
        rootNameValidationSet = new HashSet<String>();
      }
      if (rootNameValidationSet.contains(input)) {
        valid = false;
        retval = false;
        if (errMsg != null) {
          errMsg.append("Dataset root name must be unique: " + input + ".  ");
        }
      }
      else {
        rootNameValidationSet.add(input);
      }
      //Make sure that file(s) denoted by the root name are not used more then once.
      //Keep retval off if it has been turned off.
      if (!dual) {
        retval = validateRequiredFile(input, errMsg) && retval;
      }
      else {
        retval =
            validateRequiredFile(input + AxisID.FIRST.getExtension(), errMsg) && retval;
        retval =
            validateRequiredFile(input + AxisID.SECOND.getExtension(), errMsg) && retval;
      }
    }
    interleavedParameters[InterleavedIndex.ROOT_NAME.index].add(input);
    return retval;
  }

  private boolean validateRequiredFile(final String input, final StringBuilder errMsg) {
    if (requiredFilesValidationSet == null) {
      requiredFilesValidationSet = new HashSet<String>();
    }
    if (requiredFilesValidationSet.contains(input)) {
      if (errMsg != null) {
        errMsg.append("The " + input + " file is used in more then one dataset.  ");
      }
      return false;
    }
    requiredFilesValidationSet.add(input);
    return true;
  }

  /**
   * @param input
   * @param enforceUniqueness
   * @param errMsg            will be used if it is not null
   * @return
   */
  public boolean addCurrentLocation(final String input, final boolean enforceUniqueness,
      final StringBuilder errMsg) {
    if (mode != Mode.BATCH) {
      System.err.println(
          "Warning: attempting to set a current location in a non-batch mode batchruntomo command");
      return false;
    }

    boolean retval = true;
    if (doValidation && enforceUniqueness) {
      if (currentLocationValidationSet == null) {
        currentLocationValidationSet = new HashSet<String>();
      }
      if (currentLocationValidationSet.contains(input)) {
        valid = false;
        retval = false;
        if (errMsg != null) {
          errMsg.append("Dataset location must be unique");
        }
      }
      else {
        currentLocationValidationSet.add(input);
      }
    }
    interleavedParameters[InterleavedIndex.CURRENT_LOCATION.index].add(input);
    return retval;
  }

  public void setCPUMachineList(final String input) {
    cpuMachineList = new StringBuilder();
    cpuMachineList.append(input);
  }

  public void setGPUMachineList(final String input) {
    gpuMachineList = new StringBuilder();
    gpuMachineList.append(input);
  }

  public boolean isEmailAddressNull() {
    return emailAddress.isEmpty();
  }

  public void setEmailAddress(final String input) {
    emailAddress.set(input);
  }

  public void resetEmailAddress() {
    emailAddress.reset();
  }

  public String getEmailAddress() {
    return emailAddress.toString();
  }

  public boolean isGpuMachineListNull() {
    return gpuMachineList == null || gpuMachineList.length() == 0;
  }

  public boolean gpuMachineListEquals(final String input) {
    return gpuMachineList != null && gpuMachineList.toString().equals(input);
  }

  public boolean isCpuMachineListNull() {
    return cpuMachineList == null || cpuMachineList.length() == 0;
  }

  public boolean setupValidationCommand() {
    // Create a new SystemProgram object for copytomocom, set the
    // working directory and stdin array.
    // Do not use the -e flag for tcsh since David's scripts handle the failure
    // of commands and then report appropriately. The exception to this is the
    // com scripts which require the -e flag. RJG: 2003-11-06
    command.add("python");
    command.add("-u");
    command.add(BaseManager.getIMODBinPath() + ProcessName.BATCHRUNTOMO);
    command.add("-validation");
    command.add(validationType.toString());
    int len = interleavedParameters[InterleavedIndex.DIRECTIVE_FILE.index].size();
    for (int i = 0; i < len; i++) {
      command.add("-directive");
      String value =
          (String) interleavedParameters[InterleavedIndex.DIRECTIVE_FILE.index].get(i);
      if (value != null) {
        command.add(value);
      }
    }
    batchruntomo =
        new SystemProgram(manager, manager.getPropertyUserDir(), command, AxisID.ONLY);
    batchruntomo.setMessagePrependTag("Beginning to process template file");
    return true;
  }

  public void addDirectiveFile(final DirectiveFile directiveFile) {
    if (directiveFile != null) {
      File file = directiveFile.getFile();
      if (file != null) {
        interleavedParameters[InterleavedIndex.DIRECTIVE_FILE.index]
            .add(directiveFile.getFile().getAbsolutePath());
      }
    }
  }

  public boolean isValid() {
    if (mode == Mode.VALIDATION) {
      return (validationType.equals(VALIDATION_TYPE_BATCH_DIRECTIVE) ||
          validationType.equals(VALIDATION_TYPE_TEMPLATE)) &&
          !interleavedParameters[InterleavedIndex.DIRECTIVE_FILE.index].isEmpty();
    }
    return valid;
  }

  public void setValidationType(final boolean directiveDrivenAutomation) {
    if (directiveDrivenAutomation) {
      validationType.set(VALIDATION_TYPE_BATCH_DIRECTIVE);
    }
    else {
      validationType.set(VALIDATION_TYPE_TEMPLATE);
    }
  }

  /**
   * Return the current command line string
   *
   * @return
   */
  public String getCommandLine() {
    if (batchruntomo == null) {
      return "";
    }
    return batchruntomo.getCommandLine();
  }

  /**
   * Execute the copytomocoms script
   *
   * @return @throws
   * IOException
   */
  public int run() {
    if (batchruntomo == null) {
      return -1;
    }
    int exitValue;

    // Execute the script
    batchruntomo.run();
    exitValue = batchruntomo.getExitValue();
    return exitValue;
  }

  public String getStdErrorString() {
    if (batchruntomo == null) {
      return "ERROR: Batchruntomo is null.";
    }
    return batchruntomo.getStdErrorString();
  }

  public String getStdOutputString() {
    if (batchruntomo == null) {
      return "ERROR: Batchruntomo is null.";
    }
    return batchruntomo.getStdOutputString();
  }

  public String[] getStdError() {
    if (batchruntomo == null) {
      return new String[]{"ERROR: Batchruntomo is null."};
    }
    return batchruntomo.getStdError();
  }

  /**
   * returns a String array of warnings - one warning per element
   * make sure that warnings get into the error log
   *
   * @return
   */
  public ProcessMessages getProcessMessages() {
    if (batchruntomo == null) {
      return null;
    }
    return batchruntomo.getProcessMessages();
  }

  private static final class Mode implements CommandMode {
    private static final Mode VALIDATION = new Mode();
    private static final Mode BATCH = new Mode();

    private Mode() {
    }
  }

  private static final class InterleavedIndex {
    //validation and batch
    private static final InterleavedIndex DIRECTIVE_FILE =
        new InterleavedIndex(0, "DirectiveFile");
    //batch only
    private static final InterleavedIndex ROOT_NAME = new InterleavedIndex(1, "RootName");
    private static final InterleavedIndex CURRENT_LOCATION =
        new InterleavedIndex(2, "CurrentLocation");

    private static final int VALIDATION_LENGTH = 1;
    private static final int BATCH_LENGTH = 3;

    private final int index;
    private final String tag;

    private InterleavedIndex(final int index, final String tag) {
      this.index = index;
      this.tag = tag;
    }

    private static String[] getTags(final CommandMode mode) {
      if (mode == Mode.VALIDATION) {
        return new String[]{DIRECTIVE_FILE.tag};
      }
      if (mode == Mode.BATCH) {
        return new String[]{DIRECTIVE_FILE.tag, ROOT_NAME.tag, CURRENT_LOCATION.tag};
      }
      return null;
    }
  }
}
