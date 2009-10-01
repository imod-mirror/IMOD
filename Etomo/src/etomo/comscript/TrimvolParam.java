/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright (c) 2002 - 2006</p>
 * 
 * <p>Organization: Boulder Laboratory for 3D Fine Structure,
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 * 
 * <p> $Log$
 * <p> Revision 3.33  2009/09/05 00:35:39  sueh
 * <p> bug# 1256 Added blank getIteratorElementList.
 * <p>
 * <p> Revision 3.32  2009/09/01 03:17:46  sueh
 * <p> bug# 1222
 * <p>
 * <p> Revision 3.31  2009/04/27 17:57:34  sueh
 * <p> Removed unnecessary prints.
 * <p>
 * <p> Revision 3.30  2009/03/17 00:33:17  sueh
 * <p> bug# 1186 Pass managerKey to everything that pops up a dialog.
 * <p>
 * <p> Revision 3.29  2009/02/13 02:13:10  sueh
 * <p> bug# 1176 Checking return value of MRCHeader.read.
 * <p>
 * <p> Revision 3.28  2009/02/10 21:42:00  sueh
 * <p> bug# 1143 Added hasInputFileSizeChanged, which returns a boolean and
 * <p> also sets nColumnsChanged, nRowsChanged, and nSectionsChanged.
 * <p> Calling hasInputFileSizeChanged from setDefaultRange.
 * <p>
 * <p> Revision 3.27  2007/12/13 01:07:10  sueh
 * <p> bug# 1056 Added keepSameOrigin.
 * <p>
 * <p> Revision 3.26  2007/11/06 19:17:48  sueh
 * <p> bug# 1047 Added getSubcommandDetails.
 * <p>
 * <p> Revision 3.25  2007/08/21 21:51:35  sueh
 * <p> Temporarily sending the trimvol command to err.
 * <p>
 * <p> Revision 3.24  2007/05/11 15:33:17  sueh
 * <p> bug# 964 Added getStringArray().
 * <p>
 * <p> Revision 3.23  2007/02/05 22:48:39  sueh
 * <p> bug# 962 Added getEtomoNumber, getIntKeyList, and getString.
 * <p>
 * <p> Revision 3.22  2006/10/20 21:42:24  sueh
 * <p> bug# 946  Make rotateX the default reorientation method.
 * <p>
 * <p> Revision 3.21  2006/08/16 18:49:27  sueh
 * <p> bug# 912, bug# 915 Converted scale x and y min and max to an XYParam.
 * <p> No longer converting scale imod coords to index coords.  Added a version.
 * <p> Attempting to convert index coords back to imod coords for older versions.
 * <p>
 * <p> Revision 3.20  2006/08/14 18:33:15  sueh
 * <p> bug#  890 Converted section scale min and max, and fixed scale min and max
 * <p> to EtomoNumber to provide error checking
 * <p>
 * <p> Revision 3.19  2006/07/10 20:04:20  sueh
 * <p> bug# 881 Don't subtract 1 from scale X/Y min/max if it is null
 * <p>
 * <p> Revision 3.18  2006/07/05 23:23:54  sueh
 * <p> Convert rubberband points to coordinates for scaling X and Y.
 * <p>
 * <p> Revision 3.17  2006/06/28 23:28:30  sueh
 * <p> bug# 881 Added scaleXMin, scaleXMax, scaleYMin, and scaleYMax.
 * <p>
 * <p> Revision 3.16  2006/06/27 17:47:19  sueh
 * <p> bug# 879 Add rotateX.
 * <p>
 * <p> Revision 3.15  2006/05/22 22:42:51  sueh
 * <p> bug# 577 Added getCommand().
 * <p>
 * <p> Revision 3.14  2006/05/11 19:50:36  sueh
 * <p> bug# 838 Add CommandDetails, which extends Command and
 * <p> ProcessDetails.  Changed ProcessDetails to only contain generic get
 * <p> functions.  Command contains all the command oriented functions.
 * <p>
 * <p> Revision 3.13  2006/04/06 19:38:35  sueh
 * <p> bug# 808 Implementing ProcessDetails.  Added Fields to pass requests to
 * <p> the generic gets.
 * <p>
 * <p> Revision 3.12  2006/01/20 20:48:23  sueh
 * <p> updated copyright year
 * <p>
 * <p> Revision 3.11  2005/11/19 01:54:59  sueh
 * <p> bug# 744 Moved functions only used by process manager post
 * <p> processing and error processing from Commands to ProcessDetails.
 * <p> This allows ProcesschunksParam to be passed to DetackedProcess
 * <p> without having to add unnecessary functions to it.
 * <p>
 * <p> Revision 3.10  2005/09/02 18:56:20  sueh
 * <p> bug# 720 Pass the manager to TrimvolParam instead of propertyUserDir
 * <p> because TrimvolParam is constructed by MetaData before
 * <p> propertyUserDir is set.
 * <p>
 * <p> Revision 3.9  2005/07/29 00:50:21  sueh
 * <p> bug# 709 Going to EtomoDirector to get the current manager is unreliable
 * <p> because the current manager changes when the user changes the tab.
 * <p> Passing the manager where its needed.
 * <p>
 * <p> Revision 3.8  2005/06/20 16:41:16  sueh
 * <p> bug# 522 Made MRCHeader an n'ton.  Getting instance instead of
 * <p> constructing in setDefaultRange().
 * <p>
 * <p> Revision 3.7  2005/04/25 20:41:32  sueh
 * <p> bug# 615 Passing the axis where a command originates to the message
 * <p> functions so that the message will be popped up in the correct window.
 * <p> This requires adding AxisID to many objects.
 * <p>
 * <p> Revision 3.6  2005/01/08 01:46:27  sueh
 * <p> bug# 578 Changed the names of the statics used to make variables
 * <p> available in the Command interface.  Add GET_.  Updated Command
 * <p> interface.
 * <p>
 * <p> Revision 3.5  2004/12/16 02:14:31  sueh
 * <p> bug# 564 Fixed bug: command array was not refreshing.  Refresh
 * <p> command array when getCommandArray() is called.
 * <p>
 * <p> Revision 3.4  2004/12/08 21:22:34  sueh
 * <p> bug# 564 Implemented Command.  Provided access to swapYZ.
 * <p>
 * <p> Revision 3.3  2004/12/02 18:27:49  sueh
 * <p> bug# 557 Added a static getOutputFile(String datasetName) to put the
 * <p> responsibility of knowing how to build the trimvol output file in
 * <p> TrimvolParam.
 * <p>
 * <p> Revision 3.2  2004/06/22 01:53:33  sueh
 * <p> bug# 441 Added store(), load(), and equals().  Prevented
 * <p> setDefaultRange from overriding non-default values.  Moved
 * <p> the logic for creating the inputFile and outputFile names into
 * <p> this class
 * <p>
 * <p> Revision 3.1  2004/04/22 23:27:28  rickg
 * <p> Switched getIMODBinPath method
 * <p>
 * <p> Revision 3.0  2003/11/07 23:19:00  rickg
 * <p> Version 1.0.0
 * <p>
 * <p> Revision 1.12  2003/11/06 21:28:51  sueh
 * <p> bug307 setDefaultRange(String): Set sectionScaleMin to 1/3
 * <p> y or z max and sectionScaleMax to 2/3 y or z max.
 * <p>
 * <p> Revision 1.11  2003/11/06 16:50:27  rickg
 * <p> Removed -e flag for tcsh execution for all but the com scripts
 * <p>
 * <p> Revision 1.10  2003/11/04 20:56:11  rickg
 * <p> Bug #345 IMOD Directory supplied by a static function from ApplicationManager
 * <p>
 * <p> Revision 1.9  2003/10/20 23:25:26  rickg
 * <p> Bug# 253 Added convert to bytes checkbox
 * <p>
 * <p> Revision 1.8  2003/05/23 22:03:37  rickg
 * <p> Added -P to command string to get shell PID output
 * <p>
 * <p> Revision 1.7  2003/05/21 21:23:46  rickg
 * <p> Added e flag to tcsh execution
 * <p>
 * <p> Revision 1.6  2003/05/14 21:45:59  rickg
 * <p> Added full path to trimvol script for windows
 * <p>
 * <p> Revision 1.5  2003/04/16 22:19:30  rickg
 * <p> Initial revision
 * <p>
 * <p> Revision 1.4  2003/04/16 00:14:12  rickg
 * <p> Trimvol in progress
 * <p>
 * <p> Revision 1.3  2003/04/14 23:56:59  rickg
 * <p> Default state of YZ swap changed to true
 * <p>
 * <p> Revision 1.2  2003/04/10 23:40:40  rickg
 * <p> In progress
 * <p>
 * <p> Revision 1.1  2003/04/09 23:36:57  rickg
 * <p> In progress
 * <p> </p>
 */
package etomo.comscript;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

import etomo.BaseManager;
import etomo.ManagerKey;
import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.type.BaseMetaData;
import etomo.type.ConstEtomoNumber;
import etomo.type.ConstIntKeyList;
import etomo.type.EtomoNumber;
import etomo.type.IteratorElementList;
import etomo.type.ProcessName;
import etomo.type.TomogramState;
import etomo.util.MRCHeader;
import etomo.util.InvalidParameterException;

public class TrimvolParam implements CommandDetails {
  public static final String rcsid = "$Id$";

  private static final String VERSION = "1.1";
  private static final String VERSION_KEY = "Version";

  public static final String PARAM_ID = "Trimvol";
  public static final String CONVERT_TO_BYTES = "ConvertToBytes";
  public static final String FIXED_SCALING = "FixedScaling";
  public static final String FLIPPED_VOLUME = "FlippedVolume";
  private static final String swapYZString = "SwapYZ";
  private static final String ROTATE_X_KEY = "RotateX";
  public static final String INPUT_FILE = "InputFile";
  public static final String OUTPUT_FILE = "OutputFile";

  private static final int commandSize = 3;
  private static final String commandName = "trimvol";

  private EtomoNumber xMin = new EtomoNumber("XMin");
  private EtomoNumber xMax = new EtomoNumber("XMax");
  private EtomoNumber yMin = new EtomoNumber("YMin");
  private EtomoNumber yMax = new EtomoNumber("YMax");
  private EtomoNumber zMin = new EtomoNumber("ZMin");
  private EtomoNumber zMax = new EtomoNumber("ZMax");
  private final XYParam scaleXYParam = new XYParam("Scale");
  private boolean convertToBytes = true;
  private boolean fixedScaling = false;
  private boolean flippedVolume = false;
  private final EtomoNumber sectionScaleMin = new EtomoNumber("SectionScaleMin");
  private final EtomoNumber sectionScaleMax = new EtomoNumber("SectionScaleMax");
  private final EtomoNumber fixedScaleMin = new EtomoNumber("FixedScaleMin");
  private final EtomoNumber fixedScaleMax = new EtomoNumber("FixedScaleMax");
  private boolean swapYZ = false;
  private boolean rotateX = true;
  private String inputFile = "";
  private String outputFile = "";
  private String[] commandArray;
  private AxisID axisID;
  private boolean oldVersion = false;
  private boolean keepSameOrigin = false;
  private boolean nColumnsChanged = false;
  private boolean nRowsChanged = false;
  private boolean nSectionsChanged = false;

  private final BaseManager manager;

  public TrimvolParam(BaseManager manager) {
    this.manager = manager;
  }

  public AxisID getAxisID() {
    return axisID;
  }

  /**
   *  Insert the objects attributes into the properties object.
   */
  public void store(Properties props) {
    store(props, "");
  }

  public void store(Properties props, String prepend) {
    String group;
    prepend += PARAM_ID;
    group = prepend + ".";
    props.setProperty(prepend + "." + VERSION_KEY, VERSION);
    xMin.store(props, prepend);
    xMax.store(props, prepend);
    yMin.store(props, prepend);
    yMax.store(props, prepend);
    zMin.store(props, prepend);
    zMax.store(props, prepend);
    props.setProperty(group + CONVERT_TO_BYTES, String.valueOf(convertToBytes));
    props.setProperty(group + FIXED_SCALING, String.valueOf(fixedScaling));
    props.setProperty(group + FLIPPED_VOLUME, String.valueOf(flippedVolume));
    sectionScaleMin.store(props, prepend);
    sectionScaleMax.store(props, prepend);
    fixedScaleMax.store(props, prepend);
    fixedScaleMin.store(props, prepend);
    fixedScaleMax.store(props, prepend);
    props.setProperty(group + swapYZString, String.valueOf(swapYZ));
    props.setProperty(group + ROTATE_X_KEY, String.valueOf(rotateX));
    props.setProperty(group + INPUT_FILE, inputFile);
    props.setProperty(group + OUTPUT_FILE, outputFile);
    scaleXYParam.store(props, prepend);
  }

  /**
   *  Get the objects attributes from the properties object.
   */
  public void load(Properties props) {
    load(props, "");
  }

  public void load(Properties props, String prepend) {
    String group;
    prepend += PARAM_ID;
    group = prepend + ".";
    /* if (prepend == "") {
     group = PARAM_ID + ".";
     }
     else {
     group = prepend + PARAM_ID + ".";
     }*/
    oldVersion = props.getProperty(group + VERSION_KEY) == null;
    // Load the trimvol values if they are present, don't change the
    // current value if the property is not present
    xMin.loadIfPresent(props, prepend);
    xMax.loadIfPresent(props, prepend);
    yMin.loadIfPresent(props, prepend);
    yMax.loadIfPresent(props, prepend);
    zMin.loadIfPresent(props, prepend);
    zMax.loadIfPresent(props, prepend);
    convertToBytes = Boolean.valueOf(
        props.getProperty(group + CONVERT_TO_BYTES, Boolean
            .toString(convertToBytes))).booleanValue();

    fixedScaling = Boolean
        .valueOf(
            props.getProperty(group + FIXED_SCALING, Boolean
                .toString(fixedScaling))).booleanValue();
    flippedVolume = Boolean.valueOf(
        props.getProperty(group + FLIPPED_VOLUME, Boolean
            .toString(flippedVolume))).booleanValue();
    sectionScaleMin.load(props, prepend);
    sectionScaleMax.load(props, prepend);
    fixedScaleMin.load(props, prepend);
    fixedScaleMax.load(props, prepend);

    swapYZ = Boolean.valueOf(
        props.getProperty(group + swapYZString, Boolean.toString(swapYZ)))
        .booleanValue();

    rotateX = Boolean.valueOf(
        props.getProperty(group + ROTATE_X_KEY, Boolean.toString(rotateX)))
        .booleanValue();

    inputFile = props.getProperty(group + INPUT_FILE, inputFile);

    outputFile = props.getProperty(group + OUTPUT_FILE, outputFile);
    scaleXYParam.load(props, prepend);
    convertOldVersions();
  }

  private void convertOldVersions() {
    if (!oldVersion) {
      return;
    }
    //In the old version, scale x and y min and max had been converted to index
    //coords.  In the current version, they should be imod coords.
    //shift X
    int min;
    int max;
    int shift;
    if (!scaleXYParam.getXMin().isNull()) {
      min = scaleXYParam.getXMin().getInt();
      max = scaleXYParam.getXMax().getInt();
      shift = getScaleShift(min, max);
      scaleXYParam.setXMin(min + shift);
      scaleXYParam.setXMax(max + shift);
    }
    //shift Y
    if (!scaleXYParam.getYMin().isNull()) {
      min = scaleXYParam.getYMin().getInt();
      max = scaleXYParam.getYMax().getInt();
      shift = getScaleShift(min, max);
      scaleXYParam.setYMin(min + shift);
      scaleXYParam.setYMax(max + shift);
    }
  }

  /**
   * attempt to convert scale min and max from index coords to imod coords
   * and correct bug# 915
   * @param defaultMax
   * @param min
   * @param max
   * @return
   */
  private int getScaleShift(int min, int max) {
    //Possibilities:
    //min and max may be reduced by 1 or more
    //min and max may not be reduced at all
    //Assume min and max are reduced by 1, since that is the most likely 
    //situation.
    int shift = 1;
    if (min < 0) {
      //min and max where reduce by more then 1 
      shift = 0 - min + 1;
    }
    return shift;
  }

  /**
   * @return
   */
  public boolean isConvertToBytes() {
    return convertToBytes;
  }

  /**
   * @param convertToBytes
   */
  public void setConvertToBytes(boolean convertToBytes) {
    this.convertToBytes = convertToBytes;
  }

  private void createCommand() {
    ArrayList options = genOptions();
    commandArray = new String[options.size() + commandSize];
    // Do not use the -e flag for tcsh since David's scripts handle the failure 
    // of commands and then report appropriately.  The exception to this is the
    // com scripts which require the -e flag.  RJG: 2003-11-06  
    commandArray[0] = "tcsh";
    commandArray[1] = "-f";
    commandArray[2] = BaseManager.getIMODBinPath() + commandName;
    for (int i = 0; i < options.size(); i++) {
      commandArray[i + commandSize] = (String) options.get(i);
    }
    //TEMP
    for (int i = 0; i < commandArray.length; i++) {
      System.err.print(commandArray[i] + " ");
    }
    System.err.println();
  }

  /**
   * Get the command string specified by the current state
   */
  public ArrayList genOptions() {
    ArrayList options = new ArrayList();
    options.add("-P");

    // TODO add error checking and throw an exception if the parameters have not
    // been set
    if (xMin.getInt() >= 0 && xMax.getInt() >= 0) {
      options.add("-x");
      options.add(xMin.toString() + "," + xMax.toString());
    }
    if (yMin.getInt() >= 0 && yMax.getInt() >= 0) {
      options.add("-y");
      options.add(yMin.toString() + "," + yMax.toString());
    }
    if (zMin.getInt() >= 0 && zMax.getInt() >= 0) {
      options.add("-z");
      options.add(zMin.toString() + "," + zMax.toString());
    }
    if (convertToBytes) {
      if (fixedScaling) {
        options.add("-c");
        options.add(fixedScaleMin.toString() + "," + fixedScaleMax.toString());

      }
      else {
        options.add("-s");
        options.add(String.valueOf(sectionScaleMin) + ","
            + String.valueOf(sectionScaleMax));
        if (!scaleXYParam.getXMin().isNull()
            && !scaleXYParam.getXMax().isNull()) {
          options.add("-sx");
          options.add(scaleXYParam.getXMin().toString() + ","
              + scaleXYParam.getXMax().toString());
        }
        if (!scaleXYParam.getYMin().isNull()
            && !scaleXYParam.getYMax().isNull()) {
          options.add("-sy");
          options.add(scaleXYParam.getYMin().toString() + ","
              + scaleXYParam.getYMax().toString());
        }
      }
    }

    if (flippedVolume) {
      options.add("-f");
    }

    if (swapYZ) {
      options.add("-yz");
    }

    if (rotateX) {
      options.add("-rx");
    }
    if (keepSameOrigin) {
      options.add("-k");
    }
    // TODO check to see that filenames are apropriate
    options.add(inputFile);
    options.add(outputFile);

    return options;
  }

  /**
   * @return int
   */
  public ConstEtomoNumber getFixedScaleMax() {
    return fixedScaleMax;
  }

  /**
   * @return int
   */
  public ConstEtomoNumber getFixedScaleMin() {
    return fixedScaleMin;
  }

  /**
   * @return boolean
   */
  public boolean isFixedScaling() {
    return fixedScaling;
  }

  /**
   * @return int
   */
  public ConstEtomoNumber getSectionScaleMax() {
    return sectionScaleMax;
  }

  /**
   * @return int
   */
  public ConstEtomoNumber getSectionScaleMin() {
    return sectionScaleMin;
  }

  /**
   * @return boolean
   */
  public boolean isSwapYZ() {
    return swapYZ;
  }

  public boolean isRotateX() {
    return rotateX;
  }

  /**
   * @return int
   */
  public int getXMax() {
    return xMax.getInt();
  }

  /**
   * @return int
   */
  public int getXMin() {
    return xMin.getInt();
  }

  /**
   * @return int
   */
  public int getYMax() {
    return yMax.getInt();
  }

  /**
   * @return int
   */
  public int getYMin() {
    return yMin.getInt();
  }

  /**
   * @return XYParam
   */
  public XYParam getScaleXYParam() {
    return scaleXYParam;
  }

  /**
   * @return int
   */
  public int getZMax() {
    return zMax.getInt();
  }

  /**
   * @return int
   */
  public int getZMin() {
    return zMin.getInt();
  }

  /**
   * Sets the fixedScaleMax.
   * @param fixedScaleMax The fixedScaleMax to set
   */
  public ConstEtomoNumber setFixedScaleMax(String fixedScaleMax) {
    return this.fixedScaleMax.set(fixedScaleMax);
  }

  /**
   * Sets the fixedScaleMin.
   * @param fixedScaleMin The fixedScaleMin to set
   */
  public ConstEtomoNumber setFixedScaleMin(String fixedScaleMin) {
    return this.fixedScaleMin.set(fixedScaleMin);
  }

  public void setKeepSameOrigin(boolean input) {
    keepSameOrigin = input;
  }

  /**
   * Sets the fixedScaling.
   * @param fixedScaling The fixedScaling to set
   */
  public void setFixedScaling(boolean fixedScaling) {
    fixedScaleMin.setNullIsValid(!fixedScaling);
    fixedScaleMax.setNullIsValid(!fixedScaling);
    sectionScaleMin.setNullIsValid(fixedScaling);
    sectionScaleMax.setNullIsValid(fixedScaling);
    this.fixedScaling = fixedScaling;
  }

  /**
   * Sets the scaleSectionMax.
   * @param scaleSectionMax The scaleSectionMax to set
   */
  public ConstEtomoNumber setSectionScaleMax(String scaleSectionMax) {
    return this.sectionScaleMax.set(scaleSectionMax);
  }

  /**
   * Sets the scaleSectionMin.
   * @param scaleSectionMin The scaleSectionMin to set
   */
  public ConstEtomoNumber setSectionScaleMin(String scaleSectionMin) {
    return this.sectionScaleMin.set(scaleSectionMin);
  }

  public void setFlippedVolume(boolean input) {
    flippedVolume = input;
  }

  /**
   * Sets the swapYZ.
   * @param swapYZ The swapYZ to set
   */
  public void setSwapYZ(boolean swapYZ) {
    this.swapYZ = swapYZ;
  }

  public void setRotateX(boolean rotateX) {
    this.rotateX = rotateX;
  }

  /**
   * Sets the xMax.
   * @param xMax The xMax to set
   */
  public void setXMax(String xMax) {
    this.xMax.set(xMax);
  }

  /**
   * Sets the xMin.
   * @param xMin The xMin to set
   */
  public void setXMin(String xMin) {
    this.xMin.set(xMin);
  }

  /**
   * Sets the yMax.
   * @param yMax The yMax to set
   */
  public void setYMax(String yMax) {
    this.yMax.set(yMax);
  }

  /**
   * Sets the yMin.
   * @param yMin The yMin to set
   */
  public void setYMin(String yMin) {
    this.yMin.set(yMin);
  }

  /**
   * Sets the zMax.
   * @param zMax The zMax to set
   */
  public void setZMax(String zMax) {
    this.zMax.set(zMax);
  }

  /**
   * Sets the zMin.
   * @param zMin The zMin to set
   */
  public void setZMin(String zMin) {
    this.zMin.set(zMin);
  }

  public boolean isNColumnsChanged() {
    return nColumnsChanged;
  }

  public boolean isNRowsChanged() {
    return nRowsChanged;
  }

  public boolean isNSectionsChanged() {
    return nSectionsChanged;
  }

  private boolean hasInputFileSizeChanged(MRCHeader mrcHeader,
      TomogramState state) {
    boolean changed = false;
    if (!state.isPostProcTrimVolInputNColumnsNull()
        && mrcHeader.getNColumns() != state.getPostProcTrimVolInputNColumns()) {
      changed = true;
      nColumnsChanged = true;
    }
    else {
      nColumnsChanged = false;
    }
    if (!state.isPostProcTrimVolInputNRowsNull()
        && mrcHeader.getNRows() != state.getPostProcTrimVolInputNRows()) {
      changed = true;
      nRowsChanged = true;
    }
    else {
      nRowsChanged = false;
    }
    if (!state.isPostProcTrimVolInputNSectionsNull()
        && mrcHeader.getNSections() != state.getPostProcTrimVolInputNSections()) {
      changed = true;
      nSectionsChanged = true;
    }
    else {
      nSectionsChanged = false;
    }
    return changed;
  }

  public void setDefaultRange(TomogramState state, ManagerKey managerKey)
      throws InvalidParameterException, IOException {
    // Get the data size limits from the image stack
    BaseMetaData metaData = manager.getBaseMetaData();
    MRCHeader mrcHeader = MRCHeader.getInstance(manager.getPropertyUserDir(),
        TrimvolParam.getInputFileName(metaData.getAxisType(), metaData
            .getName()), AxisID.ONLY, managerKey);
    if (!mrcHeader.read()) {
      throw new IOException("file does not exist");
    }
    //Don't override existing values unless the size of the trimvol input file
    //has changed since the last time trimvol was run.
    if (xMin.getInt() != Integer.MIN_VALUE
        && !hasInputFileSizeChanged(mrcHeader, state)) {
      return;
    }

    //Refresh X and Y together.  Refresh Z separately.
    if (nColumnsChanged || ((swapYZ || rotateX) && nSectionsChanged) || !swapYZ
        && !rotateX && nRowsChanged) {
      xMin.set(1);
      xMax.set(mrcHeader.getNColumns());
    }
    if (nRowsChanged) {
      yMin.set(1);
      yMax.set(mrcHeader.getNRows());
    }
    if (nSectionsChanged) {
      zMin.set(1);
      zMax.set(mrcHeader.getNSections());
    }

    // Check the swapped YZ state or rotateX state to decide which dimension to use for the 
    // section range
    if (swapYZ || rotateX) {
      sectionScaleMin.set(yMax.getInt() / 3);
      sectionScaleMax.set(yMax.getInt() * 2 / 3);
    }
    else {
      sectionScaleMin.set(zMax.getInt() / 3);
      sectionScaleMax.set(zMax.getInt() * 2 / 3);
    }
  }

  /**
   * 
   * @return
   */
  public String getInputFileName() {
    return inputFile;
  }

  /**
   * 
   * @param axisType
   * @param datasetName
   * @return
   */
  public static String getInputFileName(AxisType axisType, String datasetName) {
    if (axisType == AxisType.SINGLE_AXIS) {
      return datasetName + "_full.rec";
    }
    return "sum.rec";
  }

  /**
   * 
   * @param axisType
   * @param datasetName
   */
  public void setInputFileName(AxisType axisType, String datasetName) {
    inputFile = getInputFileName(axisType, datasetName);
  }

  public void setInputFileName(String fileName) {
    inputFile = fileName;
  }

  /**
   * 
   * @return
   */
  public String getOutputFileName() {
    return outputFile;
  }

  public static String getOutputFileName(String datasetName) {
    return datasetName + ".rec";
  }

  /**
   * 
   * @param datasetName
   */
  public void setOutputFileName(String file) {
    outputFile = file;
  }

  public boolean getBooleanValue(etomo.comscript.FieldInterface fieldInterface) {
    if (fieldInterface == Fields.SWAP_YZ) {
      return swapYZ;
    }
    if (fieldInterface == Fields.ROTATE_X) {
      return rotateX;
    }
    throw new IllegalArgumentException("field=" + fieldInterface);
  }

  public float getFloatValue(etomo.comscript.FieldInterface fieldInterface) {
    throw new IllegalArgumentException("field=" + fieldInterface);
  }

  public String[] getStringArray(etomo.comscript.FieldInterface fieldInterface) {
    throw new IllegalArgumentException("field=" + fieldInterface);
  }

  public String getString(etomo.comscript.FieldInterface fieldInterface) {
    throw new IllegalArgumentException("field=" + fieldInterface);
  }

  public double getDoubleValue(etomo.comscript.FieldInterface fieldInterface) {
    throw new IllegalArgumentException("field=" + fieldInterface);
  }

  public ConstEtomoNumber getEtomoNumber(
      etomo.comscript.FieldInterface fieldInterface) {
    throw new IllegalArgumentException("field=" + fieldInterface);
  }

  public ConstIntKeyList getIntKeyList(
      etomo.comscript.FieldInterface fieldInterface) {
    throw new IllegalArgumentException("field=" + fieldInterface);
  }

  public Hashtable getHashtable(etomo.comscript.FieldInterface fieldInterface) {
    throw new IllegalArgumentException("field=" + fieldInterface);
  }

  public String[] getCommandArray() {
    createCommand();
    return commandArray;
  }

  public String getCommandLine() {
    if (commandArray == null) {
      return "";
    }
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < commandArray.length; i++) {
      buffer.append(commandArray[i] + " ");
    }
    return buffer.toString();
  }

  public CommandDetails getSubcommandDetails() {
    return null;
  }

  public ProcessName getSubcommandProcessName() {
    return null;
  }

  public String getCommandName() {
    return commandName;
  }

  public ProcessName getProcessName() {
    return ProcessName.TRIMVOL;
  }

  public String getCommand() {
    return commandName;
  }

  public int getIntValue(etomo.comscript.FieldInterface fieldInterface) {
    throw new IllegalArgumentException("field=" + fieldInterface);
  }

  public IteratorElementList getIteratorElementList(final FieldInterface field) {
    throw new IllegalArgumentException("field=" + field);
  }

  public CommandMode getCommandMode() {
    return null;
  }

  public File getCommandOutputFile() {
    return new File(outputFile);
  }

  public static String getName() {
    return commandName;
  }

  /**
   * 
   * @param trim
   * @return
   */
  public boolean equals(TrimvolParam trim) {
    if (!xMin.equals(trim.getXMin())) {
      return false;
    }
    if (!xMax.equals(trim.getXMax())) {
      return false;
    }
    if (!yMin.equals(trim.getYMin())) {
      return false;
    }
    if (!yMax.equals(trim.getYMax())) {
      return false;
    }
    if (!zMin.equals(trim.getZMin())) {
      return false;
    }
    if (!zMax.equals(trim.getZMax())) {
      return false;
    }
    if (convertToBytes != trim.isConvertToBytes()) {
      return false;
    }
    if (fixedScaling != trim.isFixedScaling()) {
      return false;
    }
    if (!sectionScaleMin.equals(trim.sectionScaleMin)) {
      return false;
    }
    if (!sectionScaleMax.equals(trim.sectionScaleMax)) {
      return false;
    }
    if (!fixedScaleMin.equals(trim.fixedScaleMin)) {
      return false;
    }
    if (!fixedScaleMax.equals(trim.fixedScaleMax)) {
      return false;
    }
    if (swapYZ != trim.isSwapYZ()) {
      return false;
    }
    if (rotateX != trim.isRotateX()) {
      return false;
    }
    if (!inputFile.equals(trim.getInputFileName())
        && (inputFile.equals("\\S+") || trim.getInputFileName().equals("\\S+"))) {
      return false;
    }
    if (!outputFile.equals(trim.getCommandOutputFile())
        && (outputFile.equals("\\S+") || trim.getCommandOutputFile().equals(
            "\\S+"))) {
      return false;
    }
    if (!scaleXYParam.equals(trim.scaleXYParam)) {
      return false;
    }
    return true;
  }

  public static final class Fields implements etomo.comscript.FieldInterface {
    private Fields() {
    }

    public static final Fields SWAP_YZ = new Fields();
    public static final Fields ROTATE_X = new Fields();
  }
}