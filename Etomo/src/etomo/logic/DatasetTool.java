package etomo.logic;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import etomo.BaseManager;
import etomo.storage.DataFileFilter;
import etomo.storage.LogFile;
import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.type.DataFileType;
import etomo.type.StringProperty;
import etomo.type.ViewType;
import etomo.ui.UIComponent;
import etomo.ui.swing.UIHarness;
import etomo.util.InvalidParameterException;
import etomo.util.MRCHeader;
import etomo.util.Montagesize;
import etomo.util.Utilities;

/**
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright 2012 - 2014 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class DatasetTool {
  public static final String STANDARD_DATASET_EXT = ".st";
  public static final String ALTERNATE_DATASET_EXT = ".mrc";

  private static final String MESSAGE_TITLE = "Invalid Dataset Directory";

  private DatasetTool() {
  }

  public static String switchExtension(final String file) {
    if (file.endsWith(STANDARD_DATASET_EXT)) {
      return file.substring(0,file.lastIndexOf(".")) + ALTERNATE_DATASET_EXT;
    }
    if (file.endsWith(ALTERNATE_DATASET_EXT)) {
      return file.substring(0,file.lastIndexOf(".")) + STANDARD_DATASET_EXT;
    }
    return file;
  }

  /**
   * Prevent B stacks in the stackList from being passed by.  Do the same to files with
   * name collision.
   *
   * @param component
   * @param stackList
   * @return
   * @see etomo.logic.DatasetTool.StackInfo
   */
  public static List<StackInfo> removeMatchingBStacks(final UIComponent component,
      final File[] stackList) {
    if (stackList == null) {
      return null;
    }
    // The filtered stack list will be returned.
    List<StackInfo> filteredStackList = new ArrayList<StackInfo>();
    //Stack map allows for searching for B stacks and collisions.
    Map<String, StackInfo> stackMap = new Hashtable<String, StackInfo>();
    // Add non-dual axis stacks to the filtered stack list. Filter out matching B stacks.
    for (int i = 0; i < stackList.length; i++) {
      if (stackList[i] == null) {
        continue;
      }
      StackInfo stackInfo = new StackInfo(stackList[i]);
      filteredStackList.add(stackInfo);
      String key = stackInfo.getKey();
      if (!stackMap.containsKey(key)) {
        // Nothing matches this key - can save the current stack in stackMap
        stackMap.put(key, stackInfo);
      }
      else {
        StackInfo mappedStackInfo = stackMap.get(key);
        if (mappedStackInfo == null) {
          //This should not happen.
          stackMap.remove(key);
          stackMap.put(key, stackInfo);
        }
        else {
          //Attach the new stackInfo to the saved one with the same key, or set a collision.
          mappedStackInfo.match(stackInfo);
        }
      }
    }
    // Return filtered stack list.  Stack info instances should be set up to ignore B
    // stacks and collided names.  Report ignored stacks and collisions.
    StringBuilder errMsg = new StringBuilder();
    int len = filteredStackList.size();
    for (int i = 0; i < len; i++) {
      filteredStackList.get(i).report(errMsg);
    }
    if (errMsg.length() > 0) {
      UIHarness.INSTANCE
          .openMessageDialog(component, errMsg.toString(), "File Name Collision");
    }
    return filteredStackList;
  }

  private static AxisID getAxisID(final String fileName) {
    if (fileName.endsWith(AxisID.FIRST.getExtension() + STANDARD_DATASET_EXT) ||
        fileName.endsWith(AxisID.FIRST.getExtension() + ALTERNATE_DATASET_EXT)) {
      return AxisID.FIRST;
    }
    if (fileName.endsWith(AxisID.SECOND.getExtension() + STANDARD_DATASET_EXT) ||
        fileName.endsWith(AxisID.SECOND.getExtension() + ALTERNATE_DATASET_EXT)) {
      return AxisID.SECOND;
    }
    return AxisID.ONLY;
  }

  /**
   * If dualAxis is true, get the stack that matches axisID.  Otherwise return a file
   * made of stackAbsPath.
   *
   * @param stackAbsPath
   * @param axisID
   * @param dualAxis
   * @return
   */
  public static File getStackFile(String stackAbsPath, final AxisID axisID,
      final boolean dualAxis) {
    if (stackAbsPath == null) {
      return null;
    }
    if (dualAxis) {
      String ext = null;
      if (stackAbsPath.endsWith(STANDARD_DATASET_EXT)) {
        ext = STANDARD_DATASET_EXT;
      }
      else if (stackAbsPath.endsWith(ALTERNATE_DATASET_EXT)) {
        ext = ALTERNATE_DATASET_EXT;
      }
      // Switch axes if the axis doesn't match axisID.
      AxisID otherAxisID = axisID.getOtherAxisID();
      if (ext != null && stackAbsPath.endsWith(otherAxisID.getExtension() + ext)) {
        stackAbsPath = stackAbsPath.substring(0,
            stackAbsPath.length() - otherAxisID.getExtension().length() - ext.length()) +
            axisID.getExtension() + ext;
      }
    }
    return new File(stackAbsPath);
  }

  public static String getDatasetName(final String stackName, final boolean dualAxis) {
    if (stackName == null) {
      return null;
    }
    String ext = null;
    if (stackName.endsWith(STANDARD_DATASET_EXT)) {
      ext = STANDARD_DATASET_EXT;
    }
    else if (stackName.endsWith(ALTERNATE_DATASET_EXT)) {
      ext = ALTERNATE_DATASET_EXT;
    }
    int removeChars = 0;
    if (ext != null) {
      removeChars = ext.length();
    }
    if (dualAxis && removeChars > 0) {
      if (stackName.endsWith(AxisID.FIRST.getExtension() + ext)) {
        removeChars += AxisID.FIRST.getExtension().length();
      }
      else if (stackName.endsWith(AxisID.SECOND.getExtension() + ext)) {
        removeChars += AxisID.SECOND.getExtension().length();
      }
    }
    return stackName.substring(0, stackName.length() - removeChars);
  }

  /**
   * Gets a dataset (.edf) file that is in the same directory as the stackFile
   *
   * @param stackFile
   * @param dualAxis
   * @return
   */
  public static File getDatasetFile(final File stackFile, final boolean dualAxis) {
    if (stackFile == null) {
      return null;
    }
    String datasetName = getDatasetName(stackFile.getName(), dualAxis);
    if (datasetName == null) {
      return null;
    }
    return new File(stackFile.getParent(), datasetName + DataFileType.RECON.extension);
  }

  /**
   * Rename the inputFile if it is an .mrc file.  If this is dual axis, also rename the
   * other axis .mrc file.  If inputFile is actually a dataset name, rename the associated
   * .mrc file(s), if they exist.  if an .mrc file name was passed in, return true and set
   * standardizedFilePath to the renamed file path.  If the rename fails, return true and
   * keep standardizedFilePath empty so that the user will reexamine the directory.
   * Otherwise return true.
   *
   * @param manager
   * @param inputFile            - dataset file or dataset name that is treated as if it where a file
   * @param standardizedFilePath - a new string to place in the dataset field
   * @return true if standardizedFilePath should replace the current dataset field entry
   */
  public static boolean standardizeExtension(final BaseManager manager,
      final AxisType axisType, final File inputFile,
      final StringProperty standardizedFilePath) {
    standardizedFilePath.reset();
    // Nothing to do
    if (inputFile == null) {
      return false;
    }
    // The standard extension was used - nothing to do
    String name = inputFile.getName();
    if (name.endsWith(STANDARD_DATASET_EXT)) {
      return false;
    }
    // The entry is a dataset name, not a file
    String absPath = inputFile.getAbsolutePath();
    if (!name.endsWith(ALTERNATE_DATASET_EXT)) {
      if (axisType != AxisType.DUAL_AXIS) {
        // single axis dataset
        File altDatasetFile;
        if (!(new File(absPath + STANDARD_DATASET_EXT).exists())) {
          altDatasetFile = new File(absPath + ALTERNATE_DATASET_EXT);
          if (altDatasetFile.exists()) {
            // rename the .mrc file associated with this dataset name
            if (renameToStandardExtension(manager, altDatasetFile) == null) {
              // Need to reset the dataset field because rename failed
              return true;
            }
          }
        }
      }
      else if (!(new File(absPath + AxisID.FIRST.getExtension() + STANDARD_DATASET_EXT)
          .exists()) &&
          !(new File(absPath + AxisID.SECOND.getExtension() + STANDARD_DATASET_EXT)
              .exists())) {
        // dual axis dataset
        // if the .mrc files associated with this dataset name exist, rename them
        File altDatasetFile =
            new File(absPath + AxisID.FIRST.getExtension() + ALTERNATE_DATASET_EXT);
        // must return true if the rename failed, files may be in an unknown state and
        // the user should reexamine the directory.
        boolean renameFailed = false;
        if (altDatasetFile.exists()) {
          renameFailed = renameToStandardExtension(manager, altDatasetFile) == null;
        }
        altDatasetFile =
            new File(absPath + AxisID.SECOND.getExtension() + ALTERNATE_DATASET_EXT);
        if (altDatasetFile.exists()) {
          if (renameToStandardExtension(manager, altDatasetFile) == null ||
              renameFailed) {
            return true;
          }
        }
        else if (renameFailed) {
          return true;
        }
      }
      // no need to change the dataset field, since its a dataset name without an
      // extension and any renames succeeded.
      return false;
    }
    // The file has the alternative extension - must be renamed
    String newName = renameToStandardExtension(manager, inputFile);
    if (newName != null) {
      standardizedFilePath.set(newName);
    }
    // Handle the second .mrc file in a dual axis dataset
    if (axisType == AxisType.DUAL_AXIS) {
      // Find out if this is an A axis or B axis file
      int index = name.lastIndexOf(".");
      if (index != -1) {
        index--;
      }
      else {
        index = name.length() - 1;
      }
      AxisID axisID = AxisID.getInstance(name.charAt(index));
      if (axisID == null) {
        // Has neither an a or b extension - giving up
        return false;
      }
      // Attempt to rename the second file
      else if (axisID == AxisID.FIRST) {
        axisID = AxisID.SECOND;
      }
      else {
        axisID = AxisID.FIRST;
      }
      StringBuffer secondFileName = new StringBuffer();
      secondFileName.append(name.substring(0, index) + axisID.getExtension());
      if (secondFileName.length() < name.length()) {
        secondFileName.append(name.substring(index + 1));
      }
      renameToStandardExtension(manager,
          new File(inputFile.getParentFile(), secondFileName.toString()));
    }
    return true;
  }

  /**
   * Rename the input file that is not an .st file.  If the file has the .mrc extension,
   * it will substitute the .st extension for the .mrc extension.  Returns the path of the
   * image file as it exists at the end of the this function.  If something has gone
   * wrong, null will be returned.  This forces the user to find the correct file, and
   * reduces the chance that etomo will or
   *
   * @param inputFile - image stack file that is not null and does not have extension .st
   * @return the path of the image file, or null if there has been an error - set this output to the dataset text field
   */
  private static String renameToStandardExtension(final BaseManager manager,
      final File inputFile) {
    String name = inputFile.getName();
    if (name.endsWith(ALTERNATE_DATASET_EXT)) {
      name = name.substring(0, name.length() - ALTERNATE_DATASET_EXT.length());
    }
    File newFile = new File(inputFile.getParentFile(), name + STANDARD_DATASET_EXT);
    try {
      // MUST fail and return false if the destination file already exists.
      System.err.println(
          "Renaming " + inputFile.getAbsolutePath() + " to " + newFile.getAbsolutePath());
      if (Utilities.renameFileSafely(manager, AxisID.ONLY, inputFile, newFile)) {
        return newFile.getAbsolutePath();
      }
    }
    catch (IOException e) {
      e.printStackTrace();
      UIHarness.INSTANCE.openMessageDialog(manager, e.getMessage(), "Rename Failed");
      // Rename appears to have failed, but we don't absolutely know the state of the
      // files. The .st file may or may not exist now, so the user needs to reselect
      // the correct image file or dataset name.
      return null;
    }
    return inputFile.getAbsolutePath();
  }

  /**
   * Validates the dataset directory, including sharing.
   *
   * @param manager      - for popping up error message
   * @param axisID       - for popping up error message
   * @param inputFile    - input file (such as .st file) or the data file
   * @param dataFileType - type of the new dataset
   * @param axisType     - axis type of the new dataset - only required for reconstructions
   * @return
   */
  public static boolean validateDatasetName(final BaseManager manager,
      final UIComponent uiComponent, final AxisID axisID, final File inputFile,
      final DataFileType dataFileType, final AxisType axisType) {
    String errorMessage = null;
    if (inputFile == null) {
      errorMessage = "No input file specified.";
    }
    else if (!inputFile.exists()) {
      Thread.dumpStack();
      errorMessage = "Input file does not exist: " + inputFile.getAbsolutePath();
    }
    else if (!inputFile.isFile()) {
      errorMessage = inputFile.getAbsolutePath() + " must be a file.";
    }
    else if (!inputFile.canRead()) {
      errorMessage = "Unreadable input file: " + inputFile.getAbsolutePath();
    }
    if (errorMessage != null) {
      UIHarness.INSTANCE
          .openMessageDialog(manager, uiComponent, errorMessage, MESSAGE_TITLE, axisID);
      return false;
    }
    String inputFileName = inputFile.getName();
    int extIndex = inputFileName.lastIndexOf('.');
    String inputFileRoot = inputFileName;
    if (extIndex != -1) {
      inputFileRoot = inputFileName.substring(0, extIndex);
    }
    // Check for embedded spaces
    if (inputFileName.matches("\\s*\\S+\\s+\\S+(\\s+\\S+)*\\s*")) {
      UIHarness.INSTANCE.openMessageDialog(manager, uiComponent,
          "The dataset name cannot contain embedded spaces: " +
              inputFile.getAbsolutePath(), MESSAGE_TITLE, axisID);
      return false;
    }
    if (inputFile.getParent().endsWith(" ")) {
      UIHarness.INSTANCE.openMessageDialog(manager, uiComponent,
          "The dataset directory cannot end in a space: " + inputFile.getAbsolutePath(),
          MESSAGE_TITLE, axisID);
      return false;
    }
    File directory = inputFile.getParentFile();
    return validateDatasetName(manager, uiComponent, axisID, directory, inputFileRoot,
        dataFileType, axisType, false);
  }

  /**
   * Validates the dataset directory, including sharing.
   *
   * @param manager
   * @param axisID
   * @param inputFile
   * @param dataFileType
   * @param axisType
   * @return
   */
  public static boolean validateDatasetName(final BaseManager manager,
      final AxisID axisID, final File inputFile, final DataFileType dataFileType,
      final AxisType axisType) {
    if (inputFile == null) {
      UIHarness.INSTANCE
          .openMessageDialog(manager, null, "The input file is empty.", MESSAGE_TITLE,
              axisID);
      return false;
    }
    return validateDatasetName(manager, null, axisID, inputFile.getParentFile(),
        inputFile.getName(), dataFileType, axisType, false);
  }

  /**
   * Validates the dataset directory, including sharing.
   *
   * @param manager       - for popping up error message
   * @param axisID        - for popping up error message
   * @param directory     - directory in which the new dataset will be created
   * @param inputFileRoot - root name of the new dataset
   * @param dataFileType  - type of the new dataset
   * @param axisType      -  axis type of the new dataset - only required for reconstructions
   * @return
   */
  public static boolean validateDatasetName(final BaseManager manager,
      final AxisID axisID, final File directory, final String inputFileRoot,
      final DataFileType dataFileType, final AxisType axisType,
      final boolean datasetName) {
    return validateDatasetName(manager, null, axisID, directory, inputFileRoot,
        dataFileType, axisType, datasetName);
  }

  /**
   * Validates the dataset directory, including sharing.
   *
   * @param manager       - for popping up error message
   * @param axisID        - for popping up error message
   * @param directory     - directory in which the new dataset will be created
   * @param inputFileRoot - root name of the new dataset
   * @param dataFileType  - type of the new dataset
   * @param axisType      -  axis type of the new dataset - only required for reconstructions
   * @return
   */
  public static boolean validateDatasetName(final BaseManager manager,
      final UIComponent uiComponent, final AxisID axisID, final File directory,
      final String inputFileRoot, final DataFileType dataFileType,
      final AxisType axisType, final boolean datasetName) {
    String errorMessage = null;
    if (!directory.exists()) {
      errorMessage = "Directory does not exist: " + directory.getAbsolutePath();
    }
    else if (!directory.isDirectory()) {
      errorMessage = directory.getAbsolutePath() + " must be a directory.";
    }
    else if (!directory.canRead()) {
      errorMessage = "Unreadable directory: " + directory.getAbsolutePath();
    }
    else if (!directory.canWrite()) {
      errorMessage = "Unwritable directory: " + directory.getAbsolutePath();
    }
    else if (dataFileType == null) {
      errorMessage = "No data file type specified";
    }
    // Check for embedded spaces
    else if (inputFileRoot.matches("\\s*\\S+\\s+\\S+(\\s+\\S+)*\\s*")) {
      errorMessage = "The dataset name cannot contain embedded spaces: " + inputFileRoot;
    }
    else {
      File[] fileList = directory.listFiles(new DataFileFilter(true));
      if (fileList == null || fileList.length == 0) {
        return true;
      }
      for (int i = 0; i < fileList.length; i++) {
        if (fileList[i] != null) {
          boolean canShare = true;
          if (dataFileType.hasAxisType) {
            AxisType fileAxisType = null;
            // If the existing data file does not have an axis type, the axis types
            // don't matter.
            if (DataFileType.getInstance(fileList[i].getName()).hasAxisType) {
              fileAxisType = AxisType
                  .getInstance(LogFile.getLineContaining(fileList[i], "Setup.AxisType"));
            }
            canShare =
                canShareWith(dataFileType, inputFileRoot, axisType, fileList[i].getName(),
                    fileAxisType, datasetName);
          }
          else {
            canShare = canShareWith(dataFileType, inputFileRoot, fileList[i].getName(),
                datasetName);
          }
          if (!canShare) {
            errorMessage =
                "Cannot create " + dataFileType + " dataset " + inputFileRoot + " in " +
                    directory + " because " + fileList[i].getName() +
                    " cannot share a directory with this new dataset.  Please select " +
                    "another directory.";
            break;
          }
        }
      }
    }
    if (errorMessage == null) {
      return true;
    }
    UIHarness.INSTANCE
        .openMessageDialog(manager, uiComponent, errorMessage, MESSAGE_TITLE, axisID);
    return false;

  }

  /**
   * Returns true if newDataFileType can share a directory with another data file
   * (existingDataFileName).  This function cannot allow .edf files to share a directory.
   * Call reconCanShareWith to allow .edf file sharing.
   *
   * @param newRoot              - the root of the project to be created
   * @param existingDataFileName - a data file in the directory to be shared
   * @return
   */
  static boolean canShareWith(final DataFileType newDataFileType, final String newRoot,
      final String existingDataFileName, final boolean datasetName) {
    if (newDataFileType.hasAxisType) {
      // handle incorrect data file type
      new InvalidParameterException(
          "Warning: unable to share directories containing " + newDataFileType +
              " file types.  Wrong canShareWith called.  Calling correct canShareWith " +
              "without axis type information.").printStackTrace();
      return canShareWith(newDataFileType, newRoot, null, existingDataFileName, null,
          datasetName);
    }
    // Get the type of the existing data file
    DataFileType existingDataFileType = DataFileType.getInstance(existingDataFileName);
    if (existingDataFileType == null) {
      // Its not a data file
      return true;
    }
    // Get existing data file root
    int extIndex = existingDataFileName.lastIndexOf('.');
    String root = existingDataFileName;
    if (extIndex != -1) {
      root = existingDataFileName.substring(0, extIndex);
    }
    if (newDataFileType == DataFileType.JOIN) {
      if (existingDataFileType == DataFileType.SERIAL_SECTIONS) {
        return false;
      }
      return true;
    }
    if (newDataFileType == DataFileType.PARALLEL) {
      return true;
    }
    if (newDataFileType == DataFileType.PEET) {
      if (existingDataFileType == DataFileType.PEET) {
        // Can share a PEET directory if the root is the same
        return root.equals(newRoot);
      }
      if (existingDataFileType == DataFileType.SERIAL_SECTIONS) {
        return false;
      }
      return true;
    }
    if (newDataFileType == DataFileType.SERIAL_SECTIONS) {
      if (existingDataFileType == DataFileType.RECON ||
          existingDataFileType == DataFileType.JOIN ||
          existingDataFileType == DataFileType.PEET) {
        return false;
      }
      if (existingDataFileType == DataFileType.SERIAL_SECTIONS) {
        // Can share a SERIAL_SECTIONS directory if the root is the same
        return root.equals(newRoot);
      }
      return true;
    }
    if (newDataFileType == DataFileType.TOOLS) {
      if (existingDataFileType == DataFileType.RECON ||
          existingDataFileType == DataFileType.JOIN ||
          existingDataFileType == DataFileType.PEET ||
          existingDataFileType == DataFileType.SERIAL_SECTIONS) {
        // Can share a major project directory, if the root is different
        return !root.equals(newRoot);
      }
      return true;
    }
    return false;
  }

  /**
   * CanShareWith function for DataFileTypes that have an axis type (.edf).
   * Returns true if newDataFileType can share a directory with another data file
   * (existingDataFileName).  The Axis Type parameters can be null if
   * existingDataFileName is not an .edf file.
   *
   * @param newDataFileType      - file type of the new dataset file
   * @param newRoot              - root of the new dataset file
   * @param newAxisType          - axis type of the new dataset file
   * @param existingDataFileName - the existing dataset file
   * @param existingAxisType     - the axis type of the existing dataset file
   * @return
   */
  static boolean canShareWith(final DataFileType newDataFileType, String newRoot,
      final AxisType newAxisType, final String existingDataFileName,
      final AxisType existingAxisType, final boolean datasetName) {
    // check new root
    if (newRoot == null || newRoot.matches("\\s*")) {
      return false;
    }
    // handle incorrect data file types
    if (!newDataFileType.hasAxisType) {
      new IllegalStateException(
          "Wrong canShareWith function called - " + newDataFileType +
              " does not have an axis type.  Calling correct canShareWith.  newRoot:" +
              newRoot).printStackTrace();
      return canShareWith(newDataFileType, existingDataFileName, newRoot, datasetName);
    }
    // Get the type of the existing data file
    DataFileType existingDataFileType = DataFileType.getInstance(existingDataFileName);
    if (existingDataFileType == null) {
      // Its not a data file
      return true;
    }
    // Get existing data file root
    int extIndex = existingDataFileName.lastIndexOf('.');
    String root = existingDataFileName;
    if (extIndex != -1) {
      root = existingDataFileName.substring(0, extIndex);
    }
    // Can't share if the newAxisType is missing
    if ((newAxisType == null || newAxisType == AxisType.NOT_SET) &&
        existingDataFileType.hasAxisType) {
      new InvalidParameterException(
          "Warning: dual and single axis reconstructions of the same stack cannot " +
              "share a directory.\nNewAxisType wasn't set for a " + newDataFileType +
              " data file.  hasAxisType:" + newDataFileType.hasAxisType +
              ",existingDataFileName:" + existingDataFileName + ",newRoot:" + newRoot +
              ",root:" + root).printStackTrace();
      return false;
    }
    // If the data file type uses the axis letter, and the new root (newRoot) ends in "a"
    // or "b", strip the axis letter (BBa -> BB, jawa -> jaw).
    boolean stripped = false;
    String strippedLetter = null;
    if (newDataFileType.hasAxisType && (newRoot.endsWith("a") || newRoot.endsWith("b"))) {
      stripped = true;
      if (newAxisType == AxisType.SINGLE_AXIS) {
        // Record the letter that is stripped because b and a may not be compatible for
        // single axis data file types.
        strippedLetter = newRoot.substring(newRoot.length() - 1);
      }
      newRoot = newRoot.substring(0, newRoot.length() - 1);
    }
    // check for sharing with another .edf file
    // Can share a RECON directory if the root is the same
    if (existingDataFileType == DataFileType.RECON) {
      // Can't share if the existingAxisType is missing
      // Don't have enough information to avoid matching stacks with similar names
      if ((existingAxisType == null || existingAxisType == AxisType.NOT_SET) &&
          existingDataFileType.hasAxisType) {
        new InvalidAlgorithmParameterException(
            "Warning: dual and single axis reconstructions of the same stack cannot " +
                "share a directory.\nExistingAxisType wasn't set for a " +
                existingDataFileType + " data file.  hasAxisType:" +
                newDataFileType.hasAxisType + ",existingDataFileName:" +
                existingDataFileName + ",newRoot:" + newRoot + ",root:" + root)
            .printStackTrace();
        return false;
      }
      // Match the root without an axis letter
      if (root.equals(newRoot)) {
        if (stripped) {
          if (existingAxisType == AxisType.SINGLE_AXIS) {
            // The existing data file is associated with root.st, while the new .edf
            // will be associated with roota.st/rootb.st, so they cannot share the
            // directory.
            return false;
          }
          return true;
        }
        if (existingAxisType == AxisType.DUAL_AXIS && !datasetName) {
          // The existing data file is associated with roota.st/rootb.st, while the new
          // .edf with be associated with root.st, so they cannot share the directory.
          return false;
        }
        // If it is dual axis and it is the dataset name, then root is associated with
        // roota.st/rootb.st.
        return true;
      }
      // Don't add an axis letter to a root that didn't originally have one
      if (!stripped) {
        return false;
      }
      // single axis can match the same single axis .edf file, or a dual axis file
      if (newAxisType == AxisType.SINGLE_AXIS) {
        if (strippedLetter != null) {
          if (root.equals(newRoot + strippedLetter)) {
            if (existingAxisType == AxisType.DUAL_AXIS) {
              // The existing data file is associated with rootxa.st/rootxb.st, while
              // the new .edf with be associated with rootx.st, so they cannot share the
              // directory.
              return false;
            }
            return true;
          }
          return false;
        }
        new IllegalStateException("Letter was stripped, but not recorded.  usesAxisID:" +
            newDataFileType.hasAxisType + ",existingDataFileName:" +
            existingDataFileName + ",newRoot:" + newRoot + ",root:" + root)
            .printStackTrace();
        return false;
      }
      // Dual axis can match the same dual axis file, or both single axis files
      // Add the stripped axis letters back to find a match with root. This is because
      // dual can share a dataset with single or dual if they use the same stack(s).
      if (root.equals(newRoot + AxisID.FIRST.getExtension())) {
        if (!datasetName) {
          if (existingAxisType == AxisType.DUAL_AXIS) {
            // The existing data file is associated with rootaa.st/rootab.st, while
            // the new .edf with be associated with roota.st, so they cannot share the
            // directory.
            return false;
          }
        }
        else if (existingAxisType == AxisType.SINGLE_AXIS) {
          // The existing data file is associated with roota.edf/roota.st, while
          // the new .edf with be associated with rootaa.st/rootab.st, so they cannot
          // share the directory.
          return false;
        }
        // If its a dataset name then roota is associated with rootaa.st/rootab.st.
        // If its not a dataset name then roota is associated with roota.edf/roota.st.
        return true;
      }
      if (root.equals(newRoot + AxisID.SECOND.getExtension())) {
        if (!datasetName) {
          if (existingAxisType == AxisType.DUAL_AXIS) {
            // The existing data file is associated with rootba.st/rootbb.st, while
            // the new .edf with be associated with rootb.st, so they cannot share the
            // directory.
            return false;
          }
        }
        else if (existingAxisType == AxisType.SINGLE_AXIS) {
          // The existing data file is associated with roota.edf/roota.st, while
          // the new .edf with be associated with rootaa.st/rootab.st, so they cannot
          // share the directory.
          return false;
        }
        // If its a dataset name then rootb is associated with rootba.st/rootbb.st.
        // If its not a dataset name then rootb is associated with rootb.edf/rootb.st.
        return true;
      }
      return false;
    }
    if (existingDataFileType == DataFileType.SERIAL_SECTIONS) {
      return false;
    }
    return true;
  }

  /**
   * Pops up an error message and returns false if the view type doesn't match the stack
   * type.
   *
   * @param manager
   * @param axisID
   * @param viewType
   * @param stackFileName
   * @return
   */
  public static boolean validateViewType(final ViewType viewType,
      final String absolutePath, final String stackFileName, final BaseManager manager,
      final UIComponent uiComponent, final AxisID axisID) {
    if (stackFileName == null) {
      return true;
    }
    Montagesize montagesize =
        Montagesize.getInstance(absolutePath, stackFileName, axisID);
    // Run montagesize without the piece list file to see what the stack looks like.
    montagesize.setIgnorePieceListFile(true);
    int exitValue = readMontagesize(montagesize, manager);
    if (!montagesize.pieceListFileExists()) {
      if (exitValue == 0) {
        return validateMontage(viewType, montagesize, absolutePath, stackFileName,
            manager, uiComponent, axisID);
      }
      else if (exitValue == 1 && viewType == ViewType.MONTAGE) {
        UIHarness.INSTANCE.openMessageDialog(manager, uiComponent,
            "The dataset is not a montage.  Please select single frame type.",
            "Incorrect Frame Type", axisID);
        return false;
      }
    }
    // Ignored existing piece list file.
    else if (exitValue == 0) {
      return validateMontage(viewType, montagesize, absolutePath, stackFileName, manager,
          uiComponent, axisID);
    }
    else if (exitValue == 1) {
      // No piece list information available in the stack - run montagesize with with
      montagesize.setIgnorePieceListFile(false);
      exitValue = readMontagesize(montagesize, manager);
      if (exitValue == 0) {
        return validateMontage(viewType, montagesize, absolutePath, stackFileName,
            manager, uiComponent, axisID);
      }
      else if (exitValue == 2 || exitValue == 3) {
        // If they selected single view, go with that and ignore the piece list file
        if (viewType == ViewType.MONTAGE) {
          UIHarness.INSTANCE.openMessageDialog(manager, uiComponent,
              "The piece list file associated with this dataset does not match and the " +
                  "stack does not contain piece list information.  Please select " +
                  "single frame type.", "Incorrect Frame Type", axisID);
          return false;
        }
      }
    }
    return true;
  }

  private static int readMontagesize(final Montagesize montagesize,
      final BaseManager manager) {
    int exitValue;
    try {
      montagesize.read(manager);
      return montagesize.getExitValue();
    }
    catch (InvalidParameterException e) {
      exitValue = montagesize.getExitValue();
      if (montagesize.getExitValue() == 0) {
        return 1;
      }
      return exitValue;
    }
    catch (IOException e) {
      exitValue = montagesize.getExitValue();
      if (montagesize.getExitValue() == 0) {
        return 1;
      }
      return exitValue;
    }
  }

  private static boolean validateMontage(final ViewType viewType,
      final Montagesize montagesize, final String absolutePath,
      final String stackFileName, final BaseManager manager,
      final UIComponent uiComponent, final AxisID axisID) {
    if (viewType != ViewType.MONTAGE) {
      // Currently 1x1 montage works with single view, so only fail if X or Y are
      // different.
      MRCHeader header = MRCHeader.getInstance(absolutePath, stackFileName, axisID);
      try {
        if (!header.read(manager)) {
          UIHarness.INSTANCE
              .openMessageDialog(manager, "File does not exist.", "Entry Error", axisID);
          return false;
        }
      }
      catch (etomo.util.InvalidParameterException except) {
        UIHarness.INSTANCE.openMessageDialog(manager, except.getMessage(),
            "Invalid Parameter Exception", axisID);
        return false;
      }
      catch (IOException except) {
        UIHarness.INSTANCE
            .openMessageDialog(manager, except.getMessage(), "IO Exception", axisID);
        return false;
      }
      if (montagesize.getX().getInt() > header.getNColumns() ||
          montagesize.getY().getInt() > header.getNRows()) {
        UIHarness.INSTANCE.openMessageDialog(manager, uiComponent,
            "The dataset is a montage.  Please select montage frame type.",
            "Incorrect Frame Type", axisID);
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if the stack is a not a montage, or is a 1xn or nx1 montage.
   *
   * @param absolutePath
   * @param stackFileName
   * @param manager
   * @param axisID
   * @return
   */
  public static boolean isOneBy(final String absolutePath, final String stackFileName,
      final BaseManager manager, final AxisID axisID) {
    // montagesize
    Montagesize montagesize =
        Montagesize.getInstance(absolutePath, stackFileName, axisID);
    // Run montagesize without the piece list file to see what the stack looks like.
    montagesize.setIgnorePieceListFile(true);
    int exitValue = readMontagesize(montagesize, manager);
    if (exitValue == 1) {
      return true;
    }
    // header
    MRCHeader header = MRCHeader.getInstance(absolutePath, stackFileName, axisID);
    try {
      if (!header.read(manager)) {
        return true;
      }
    }
    catch (etomo.util.InvalidParameterException except) {
      except.printStackTrace();
      return true;
    }
    catch (IOException except) {
      except.printStackTrace();
      return true;
    }
    if (montagesize.getX().getInt() == header.getNColumns() ||
        montagesize.getY().getInt() == header.getNRows()) {
      return true;
    }
    return false;
  }

  public static boolean validateTiltAngle(final BaseManager manager,
      final AxisID messageAxisID, final String errorTitle, final AxisID axisID,
      final boolean manual, final String angle, final String increment) {
    if (!manual) {
      return true;
    }
    String axisDescr = getAxisDescr(axisID);
    String message = null;
    if (angle == null || angle.matches("\\s*")) {
      message = "Starting angle cannot be empty";
    }
    else if (increment == null || increment.matches("\\s*")) {
      message = "Increment cannot be empty";
    }
    if (message != null) {
      UIHarness.INSTANCE
          .openMessageDialog(manager, message + (axisDescr == null ? "." : axisDescr),
              errorTitle, messageAxisID);
      return false;
    }
    return true;
  }

  private static String getAxisDescr(final AxisID axisID) {
    if (axisID == AxisID.FIRST) {
      return " in Axis A.";
    }
    if (axisID == AxisID.SECOND) {
      return " in Axis B.";
    }
    return null;
  }

  /**
   * Stores a stack file and information about other stack file with the same root name,
   * path, and axis type.  For axis type, any stack ending in the axis letters "a" or "b"
   * is considered to be dual axis type.  A matching stack is a dual axis type stack file
   * which has the same root name, path, and axis type - but a different axis letter.  The
   * assumption is that it will be part of the same tomogram, so the B matching stack is
   * not returned by getStack.
   */
  public static final class StackInfo {
    private File stack = null;
    private AxisID axisID = null;
    private String key = null;
    //A collision is caused by another file with the same root name, path, axis type, and
    // axis letter (for dual axis type), but a different extension.
    private boolean collision = false;
    private StackInfo matchingStack = null;

    private StackInfo(final File stack) {
      this.stack = stack;
    }

    private String getKey() {
      setKey();
      return key;
    }

    /**
     * Matches two files against each other.
     *
     * @param stackInfo
     */
    private void match(final StackInfo stackInfo) {
      if (stackInfo == null) {
        System.out.println("Warning: empty stackInfo parameter");
        Thread.dumpStack();
        return;
      }
      setKey();
      stackInfo.setKey();
      if (key == null || !key.equals(stackInfo.key)) {
        System.out.println("Warning: key, " + key + " is not equal to " + stackInfo.key);
        Thread.dumpStack();
        return;
      }
      setAxisID();
      stackInfo.setAxisID();
      if (axisID == null || axisID == AxisID.ONLY || axisID == stackInfo.axisID) {
        // this stack name is identical to another stack name, but with a different
        // extension.  This is a name collision.  Files with a name collision can not be
        //loaded together.  Neither file will be added to the table.
        setCollision();
        stackInfo.setCollision();
      }
      else {
        // Both stacks are dual axis, and they have different axis letters.  Match the
        // stacks to each other.
        matchingStack = stackInfo;
        stackInfo.matchingStack = this;
      }
    }

    private void setCollision() {
      collision = true;
      //Release the matched stack.  It is now considered to NOT be part of a dual axis
      //tomogram.
      if (matchingStack != null) {
        matchingStack.matchingStack = null;
        matchingStack = null;
      }
    }

    private void report(final StringBuilder errMsg) {
      if (collision) {
        errMsg.append("Warning: Name collision: " + stack.getAbsolutePath() + ".  ");
      }
      else if (matchingStack != null && axisID == AxisID.SECOND) {
        System.err.println("INFO: Assuming " + stack +
            " is a B axis stack.  It will not be added to the table.\nTo add it to the " +
            "table, open it separately from the corresponding A axis stack.");
      }
    }

    /**
     * Returns a stack if the stack can be put into the table.  For stacks with a name
     * collision, and B stacks that have a matching stack, null is returned.
     *
     * @return
     */
    public File getStack() {
      if (!collision && (matchingStack == null || axisID != AxisID.SECOND)) {
        return stack;
      }
      return null;
    }

    public boolean isMatched() {
      return matchingStack != null;
    }

    public boolean isSingleAxis() {
      setAxisID();
      return axisID == null || axisID == AxisID.ONLY;
    }

    private void setAxisID() {
      if (axisID == null) {
        if (stack != null) {
          axisID = getAxisID(stack.getName());
        }
      }
    }

    /**
     * xxxa.st, xxxa.mrc, xxxb.st, and xxxb.mrc have the same key.  A different key will
     * be generated for .xxx.st and xxx.mrc
     */
    private void setKey() {
      setAxisID();
      if (key == null) {
        AxisType axisType;
        if (axisID == null || axisID == AxisID.ONLY) {
          axisType = AxisType.SINGLE_AXIS;
        }
        else {
          axisType = AxisType.DUAL_AXIS;
        }
        File datasetFile = null;
        if (stack != null) {
          datasetFile = getDatasetFile(stack, axisType == AxisType.DUAL_AXIS);
        }
        if (datasetFile != null) {
          key = datasetFile.getAbsolutePath() + axisType.toString();
        }
      }
    }
  }
}
