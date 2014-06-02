package etomo.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import etomo.BaseManager;
import etomo.EtomoDirector;
import etomo.logic.SeedingMethod;
import etomo.storage.autodoc.AutodocFactory;
import etomo.storage.autodoc.ReadOnlyAttribute;
import etomo.storage.autodoc.ReadOnlyAttributeIterator;
import etomo.storage.autodoc.ReadOnlyAttributeList;
import etomo.storage.autodoc.ReadOnlyAutodoc;
import etomo.type.AxisID;
import etomo.type.ConstEtomoNumber;
import etomo.type.DirectiveFileType;
import etomo.type.EtomoNumber;
import etomo.type.TiltAngleSpec;
import etomo.type.TiltAngleType;
import etomo.ui.FieldType;
import etomo.ui.swing.UIHarness;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright 2012</p>
*
* <p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
* University of Colorado</p>
* 
* @author $Author$
* 
* @version $Revision$
* 
* <p> $Log$ </p>
*/
public final class DirectiveFile {
  public static final String rcsid = "$Id:$";

  static final String A_AXIS_NAME = "a";
  public static final String ANY_AXIS_NAME = "any";
  public static final String AUTO_FID_SEED_COMMAND = "autofidseed";
  public static final String AUTO_FIT_RANGE_AND_STEP_NAME = "autoFitRangeAndStep";
  static final String B_AXIS_NAME = "b";
  public static final String BINNING_NAME = "binning";
  public static final String CS_NAME = "Cs";
  public static final String CTF_NOISE_NAME = "ctfnoise";
  public static final String CTF_PLOTTING_MODULE_NAME = "CTFplotting";
  static final String DATASET_DIRECTORY_NAME = "datasetDirectory";
  public static final String DEFOCUS_NAME = "defocus";
  public static final String DISTORT_NAME = "distort";
  public static final String DUAL_NAME = "dual";
  public static final String EXTRACT_NAME = "extract";
  static final String FALSE_VALUE = "0";
  public static final String FIDUCIALLESS_NAME = "fiducialless";
  public static final String FIDUCIALS_MODULE_NAME = "Fiducials";
  public static final String FIRST_INC_NAME = "firstinc";
  public static final String FOCUS_NAME = "focus";
  public static final String GOLD_ERASING_MODULE_NAME = "GoldErasing";
  public static final String GOLD_NAME = "gold";
  public static final String GRADIENT_NAME = "gradient";
  public static final String MONTAGE_NAME = "montage";
  public static final String NAME_NAME = "name";
  public static final String NUMBER_OF_MARKERS_NAME = "numberOfMarkers";
  public static final String PIXEL_NAME = "pixel";
  public static final String POSITIONING_MODULE_NAME = "Positioning";
  public static final String RAPTOR_MODULE_NAME = "RAPTOR";
  public static final String RECONSTRUCTION_MODULE_NAME = "Reconstruction";
  public static final String REMOVE_XRAYS_NAME = "removeXrays";
  public static final String REORIENT_NAME = "reorient";
  public static final String ROTATION_NAME = "rotation";
  static final String SCAN_HEADER_NAME = "scanHeader";
  public static final String SCOPE_TEMPLATE_NAME = "scopeTemplate";
  public static final String SEEDING_METHOD_NAME = "seedingMethod";
  public static final String SIZE_IN_X_AND_Y_NAME = "sizeInXandY";
  public static final String SKIP_NAME = "skip";
  static final String SURFACES_TO_ANALYZE_NAME = "SurfacesToAnalyze";
  public static final String SYSTEM_TEMPLATE_NAME = "systemTemplate";
  public static final String THICKNESS_NAME = "thickness";
  static final String TILT_ALIGN_COMMAND = "tiltalign";
  public static final String TRACKING_METHOD_NAME = "trackingMethod";
  static final String TRUE_VALUE = "1";
  public static final String TWODIR_NAME = "twodir";
  public static final String TWO_SURFACES_NAME = "TwoSurfaces";
  public static final String USE_ALIGNED_STACK_NAME = "useAlignedStack";
  public static final String USE_RAW_TLT_NAME = "userawtlt";
  public static final String USE_SIRT_NAME = "useSirt";
  public static final String USER_TEMPLATE_NAME = "userTemplate";
  public static final String VOLTAGE_NAME = "voltage";
  public static final String WHOLE_TOMOGRAM_NAME = "wholeTomogram";

  private static boolean debug = false;

  private final AxisID axisID;
  private final BaseManager manager;
  private final File file;

  private ReadOnlyAttribute copyArg = null;
  private Map<String, String> copyArgExtraValues = null;
  private ReadOnlyAttribute runtime = null;
  private ReadOnlyAttribute setupSet = null;
  private ReadOnlyAttribute comparam = null;

  private DirectiveFile(final BaseManager manager, final AxisID axisID, final File file) {
    this.manager = manager;
    this.axisID = axisID;
    this.file = file;
  }

  /**
   * @return a valid DirectiveFile instance or null if it there was an initialization
   * failure.
   * @param manager
   * @param axisID
   * @return
   */
  public static DirectiveFile getInstance(final BaseManager manager, final AxisID axisID) {
    DirectiveFile instance = new DirectiveFile(manager, axisID, EtomoDirector.INSTANCE
        .getArguments().getDirective());
    if (!instance.init()) {
      return null;
    }
    return instance;
  }

  public static DirectiveFile getInstance(final BaseManager manager, final AxisID axisID,
      final File file) {
    DirectiveFile instance = new DirectiveFile(manager, axisID, file);
    if (!instance.init()) {
      return null;
    }
    return instance;
  }

  /**
   * Called by getInstance.
   * @return false if failed
   */
  private boolean init() {
    try {
      ReadOnlyAutodoc autodoc = (ReadOnlyAutodoc) AutodocFactory.getInstance(manager,
          file, axisID);
      setupSet = autodoc.getAttribute(DirectiveType.SETUP_SET.toString());
      if (setupSet != null) {
        copyArg = setupSet.getAttribute(DirectiveType.COPY_ARG.toString());
      }
      runtime = autodoc.getAttribute(DirectiveType.RUN_TIME.toString());
    }
    catch (FileNotFoundException e) {
      UIHarness.INSTANCE.openMessageDialog(manager, e.getMessage(),
          "Directive File Not Found");
      return false;
    }
    catch (IOException e) {
      UIHarness.INSTANCE.openMessageDialog(manager, e.getMessage(),
          "Directive File Read Failure");
      return false;
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
      UIHarness.INSTANCE.openMessageDialog(manager, e.getMessage(),
          "Directive File Read Failure");
      return false;
    }
    return true;
  }

  private void initComparam() {
    if (comparam != null) {
      return;
    }
    try {
      ReadOnlyAutodoc autodoc = (ReadOnlyAutodoc) AutodocFactory.getInstance(manager,
          file, axisID);
      comparam = autodoc.getAttribute(DirectiveType.COM_PARAM.toString());
    }
    catch (FileNotFoundException e) {
      UIHarness.INSTANCE.openMessageDialog(manager, e.getMessage(),
          "Directive File Not Found");
    }
    catch (IOException e) {
      UIHarness.INSTANCE.openMessageDialog(manager, e.getMessage(),
          "Directive File Read Failure");
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
      UIHarness.INSTANCE.openMessageDialog(manager, e.getMessage(),
          "Directive File Read Failure");
    }
  }

  boolean containsAttribute(final DirectiveType directiveType, final String name) {
    return getAttribute(directiveType, name) != null;
  }

  private boolean containsAttribute(final DirectiveType directiveType,
      final String sectionName, final AxisID axisID, final String name) {
    ReadOnlyAttribute parent = getAttribute(directiveType, sectionName);
    if (parent == null) {
      return false;
    }
    ReadOnlyAttribute axis = parent.getAttribute(ANY_AXIS_NAME);
    if (axis != null && axis.getAttribute(name) != null) {
      return true;
    }
    String axisName = getAxisName(axisID);
    if (axisName != null) {
      axis = parent.getAttribute(axisName);
      if (axis != null && axis.getAttribute(name) != null) {
        return true;
      }
    }
    return false;
  }

  private boolean containsAttribute(final DirectiveDef directiveDef, final AxisID axisID) {
    ReadOnlyAttribute attribute = getParentAttribute(directiveDef.directiveType);
    if (attribute == null) {
      return false;
    }
    if (directiveDef.directiveType == DirectiveType.RUN_TIME) {
      attribute = attribute.getAttribute(directiveDef.module.tag);
    }
    if (attribute == null) {
      return false;
    }
    if (directiveDef.directiveType == DirectiveType.RUN_TIME) {
      ReadOnlyAttribute axis = attribute.getAttribute(ANY_AXIS_NAME);
      if (axis != null && axis.getAttribute(directiveDef.name) != null) {
        return true;
      }
      String axisName = getAxisName(axisID);
      if (axisName != null) {
        axis = attribute.getAttribute(axisName);
        if (axis != null && axis.getAttribute(directiveDef.name) != null) {
          return true;
        }
      }
    }
    return false;
  }

  boolean containsComparamAttribute(final String fileName, final String commandName,
      final String name) {
    ReadOnlyAttribute attribute = getAttribute(DirectiveType.COM_PARAM, fileName);
    if (attribute == null) {
      return false;
    }
    attribute = attribute.getAttribute(commandName);
    if (attribute == null) {
      return false;
    }
    if (attribute.getAttribute(name) == null) {
      return false;
    }
    return true;
  }

  boolean containsComparamAttribute(final String fileName, final AxisID axisID,
      final String commandName, final String name) {
    ReadOnlyAttribute attribute = getAttribute(DirectiveType.COM_PARAM, fileName
        + (axisID != null ? axisID.getExtension() : ""));
    if (attribute == null) {
      return false;
    }
    attribute = attribute.getAttribute(commandName);
    if (attribute == null) {
      return false;
    }
    if (attribute.getAttribute(name) == null) {
      return false;
    }
    return true;
  }

  private boolean containsAttribute(final ReadOnlyAttribute parent,
      final String axisName, final String name) {
    if (parent == null) {
      return false;
    }
    ReadOnlyAttribute axis = parent.getAttribute(axisName);
    if (axis != null && axis.getAttribute(name) != null) {
      return true;
    }
    return false;
  }

  /**
   * Converts the base name to either name or bname, depending on the axis ID.
   * @param axisID
   * @param name - the base name
   * @return Correct name for the axis ID.
   */
  public static String convertAttributeName(final AxisID axisID, final String baseName) {
    return (axisID == AxisID.SECOND ? B_AXIS_NAME : "") + baseName;
  }

  private ReadOnlyAttribute getAttribute(final DirectiveType directiveType,
      final String name) {
    ReadOnlyAttribute parent = getParentAttribute(directiveType);
    if (parent == null) {
      return null;
    }
    return parent.getAttribute(name);
  }

  private String getAxisName(final AxisID axisID) {
    if (axisID == AxisID.ONLY || axisID == AxisID.FIRST) {
      return A_AXIS_NAME;
    }
    if (axisID == AxisID.SECOND) {
      return B_AXIS_NAME;
    }
    return null;
  }

  private ReadOnlyAttribute getParentAttribute(final DirectiveType directiveType) {
    if (directiveType == DirectiveType.COPY_ARG) {
      return copyArg;
    }
    if (directiveType == DirectiveType.SETUP_SET) {
      return setupSet;
    }
    if (directiveType == DirectiveType.RUN_TIME) {
      return runtime;
    }
    if (directiveType == DirectiveType.COM_PARAM) {
      initComparam();
      return comparam;
    }
    return null;
  }

  private boolean isValue(final DirectiveType directiveType, final String name) {
    ReadOnlyAttribute attribute = getAttribute(directiveType, name);
    if (attribute == null) {
      return false;
    }
    return toBoolean(attribute.getValue());
  }

  private boolean isValue(final DirectiveType directiveType, final String sectionName,
      final AxisID axisID, final String name) {
    ReadOnlyAttribute section = getAttribute(directiveType, sectionName);
    if (section == null) {
      return false;
    }
    String value = null;
    if (containsAttribute(section, ANY_AXIS_NAME, name)) {
      value = getValue(section, ANY_AXIS_NAME, name);
    }
    String axisName = getAxisName(axisID);
    if (axisName != null && containsAttribute(section, axisName, name)) {
      value = getValue(section, axisName, name);
    }
    return toBoolean(value);
  }

  /**
   * Puts the name/value pair into extra values if it isn't in copyArg.
   * @param name
   * @param value
   */
  private void setCopyArgValue(final String name, final String value) {
    if (copyArg.getAttribute(name) == null) {
      if (copyArgExtraValues == null) {
        copyArgExtraValues = new HashMap<String, String>();
      }
      if (copyArgExtraValues.containsKey(name)) {
        copyArgExtraValues.remove(name);
      }
      copyArgExtraValues.put(name, value);
    }
  }

  boolean containsExtraValue(final DirectiveType directiveType, final String name) {
    return directiveType == DirectiveType.COPY_ARG && copyArgExtraValues != null
        && copyArgExtraValues.containsKey(name);
  }

  String getExtraValue(final DirectiveType directiveType, final String name) {
    if (directiveType == DirectiveType.COPY_ARG && copyArgExtraValues != null) {
      return copyArgExtraValues.get(name);
    }
    return null;
  }

  /**
   * Returns null if the attribute called name is not there.
   * @param directiveType
   * @param name
   * @return
   */
  String getValue(final DirectiveType directiveType, final String name) {
    ReadOnlyAttribute parent = getParentAttribute(directiveType);
    if (parent == null) {
      return null;
    }
    ReadOnlyAttribute attribute = parent.getAttribute(name);
    if (attribute == null) {
      return null;
    }
    return attribute.getValue();
  }

  /**
   * Returns null if the attribute called name is not there.  Returns an empty string if
   * this attribute is there and it has no value.
   * @param directiveType
   * @param sectionName
   * @param axisID
   * @param name
   * @return
   */
  private String getValue(final DirectiveType directiveType, final String sectionName,
      final AxisID axisID, final String name) {
    ReadOnlyAttribute section = getAttribute(directiveType, sectionName);
    if (section == null) {
      return null;
    }
    String value = null;
    if (containsAttribute(section, ANY_AXIS_NAME, name)) {
      value = getValue(section, ANY_AXIS_NAME, name);
      if (value == null) {
        value = "";
      }
    }
    String axisName = getAxisName(axisID);
    if (axisName != null && containsAttribute(section, axisName, name)) {
      value = getValue(section, axisName, name);
    }
    if (value == null) {
      value = "";
    }
    return value;
  }

  /**
   * Returns null if the attribute called name is not there.  Returns an empty string if
   * this attribute is there and it has no value.
   * @param fileName
   * @param commandName
   * @param name
   * @return
   */
  String getComparamValue(final String fileName, final String commandName,
      final String name) {
    ReadOnlyAttribute attribute = getAttribute(DirectiveType.COM_PARAM, fileName);
    if (attribute == null) {
      return null;
    }
    attribute = attribute.getAttribute(commandName);
    if (attribute == null) {
      return null;
    }
    attribute = attribute.getAttribute(name);
    if (attribute == null) {
      return null;
    }
    String value = attribute.getValue();
    if (value == null) {
      value = "";
    }
    return value;
  }

  /**
   * Returns null if the attribute called name is not there.  Returns an empty string if
   * this attribute is there and it has no value.
   * @param fileName
   * @param axisID
   * @param commandName
   * @param name
   * @return
   */
  String getComparamValue(final String fileName, final AxisID axisID,
      final String commandName, final String name) {
    ReadOnlyAttribute attribute = getAttribute(DirectiveType.COM_PARAM, fileName
        + (axisID != null ? axisID.getExtension() : ""));
    if (attribute == null) {
      return null;
    }
    attribute = attribute.getAttribute(commandName);
    if (attribute == null) {
      return null;
    }
    attribute = attribute.getAttribute(name);
    if (attribute == null) {
      return null;
    }
    String value = attribute.getValue();
    if (value == null) {
      value = "";
    }
    return value;
  }

  private String getValue(final ReadOnlyAttribute parent, final String axisName,
      final String name) {
    if (parent == null) {
      return null;
    }
    ReadOnlyAttribute axis = parent.getAttribute(axisName);
    if (axis == null) {
      return null;
    }
    ReadOnlyAttribute attribute = axis.getAttribute(name);
    if (attribute == null) {
      return null;
    }
    return attribute.getValue();
  }

  /**
   * Returns true unless value is null or 0.  This function does not treate null as an
   * error, so it is not necessary to check for the existance of a directive before
   * calling it.
   * @param value
   * @return
   */
  static boolean toBoolean(final String value) {
    if (value == null || value.equals(FALSE_VALUE)) {
      return false;
    }
    else if (value.equals(TRUE_VALUE)) {
      return true;
    }
    System.err.println("Error: incorrect boolean value: " + value
        + ".  Valid boolean values are 0 or 1.  Treating value as 1.");
    Thread.dumpStack();
    return true;
  }

  static ConstEtomoNumber toNumber(final String value, EtomoNumber.Type numberType) {
    if (value == null) {
      return null;
    }
    EtomoNumber number = new EtomoNumber(numberType);
    number.set(value);
    return number;
  }

  public boolean containsAlignedStackBinByFactor(final AxisID axisID) {
    return containsAttribute(DirectiveDescr.BIN_BY_FACTOR_FOR_ALIGNED_STACK, axisID);
  }

  public boolean containsAlignedStackSizeInXandY(final AxisID axisID) {
    return containsAttribute(AttributeName.RUN_TIME, ALIGNED_STACK_MODULE_NAME, axisID,
        DirectiveDescr.SIZE_IN_X_AND_Y);
  }

  public boolean containsCTFplottingAutoFitRangeAndStep(final AxisID axisID) {
    return containsAttribute(AttributeName.RUN_TIME, CTF_PLOTTING_MODULE_NAME, axisID,
        AUTO_FIT_RANGE_AND_STEP_NAME);
  }

  public boolean containsDual() {
    return containsAttribute(AttributeName.COPY_ARG, DUAL_NAME);
  }

  public boolean containsFiducialsFiducialless(final AxisID axisID) {
    return containsAttribute(AttributeName.RUN_TIME, FIDUCIALS_MODULE_NAME, axisID,
        FIDUCIALLESS_NAME);
  }

  public boolean containsFiducialsSeedingMethod(final AxisID axisID) {
    return containsAttribute(AttributeName.RUN_TIME, FIDUCIALS_MODULE_NAME, axisID,
        SEEDING_METHOD_NAME);
  }

  public boolean containsFiducialsTrackingMethod(final AxisID axisID) {
    return containsAttribute(AttributeName.RUN_TIME, FIDUCIALS_MODULE_NAME, axisID,
        TRACKING_METHOD_NAME);
  }

  public boolean containsGoldErasingBinning(final AxisID axisID) {
    return containsAttribute(AttributeName.RUN_TIME, GOLD_ERASING_MODULE_NAME, axisID,
        BINNING_NAME);
  }

  public boolean containsGoldErasingThickness(final AxisID axisID) {
    return containsAttribute(AttributeName.RUN_TIME, GOLD_ERASING_MODULE_NAME, axisID,
        THICKNESS_NAME);
  }

  public boolean containsMontage() {
    return containsAttribute(AttributeName.COPY_ARG, MONTAGE_NAME);
  }

  public boolean containsPositioningBinByFactor(final AxisID axisID) {
    return containsAttribute(AttributeName.RUN_TIME, POSITIONING_MODULE_NAME, axisID,
        BIN_BY_FACTOR_NAME);
  }

  public boolean containsPositioningThickness(final AxisID axisID) {
    return containsAttribute(AttributeName.RUN_TIME, POSITIONING_MODULE_NAME, axisID,
        THICKNESS_NAME);
  }

  public boolean containsPositioningWholeTomogram(final AxisID axisID) {
    return containsAttribute(AttributeName.RUN_TIME, POSITIONING_MODULE_NAME, axisID,
        WHOLE_TOMOGRAM_NAME);
  }

  public boolean containsRaptorNumberOfMarkers(final AxisID axisID) {
    return containsAttribute(AttributeName.RUN_TIME, RAPTOR_MODULE_NAME, axisID,
        NUMBER_OF_MARKERS_NAME);
  }

  public boolean containsRaptorUseAlignedStack(final AxisID axisID) {
    return containsAttribute(AttributeName.RUN_TIME, RAPTOR_MODULE_NAME, axisID,
        USE_ALIGNED_STACK_NAME);
  }

  public boolean containsReconstructionUseSirt(final AxisID axisID) {
    return containsAttribute(AttributeName.RUN_TIME, RECONSTRUCTION_MODULE_NAME, axisID,
        USE_SIRT_NAME);
  }

  public boolean containsTemplate(final DirectiveFileType type) {
    if (type == DirectiveFileType.SCOPE) {
      return containsAttribute(AttributeName.SETUP_SET, SCOPE_TEMPLATE_NAME);
    }
    if (type == DirectiveFileType.SYSTEM) {
      return containsAttribute(AttributeName.SETUP_SET, SYSTEM_TEMPLATE_NAME);
    }
    if (type == DirectiveFileType.USER) {
      return containsAttribute(AttributeName.SETUP_SET, USER_TEMPLATE_NAME);
    }
    return false;
  }

  public static void setDebug(final boolean input) {
    debug = input;
  }

  public String getAlignedStackBinByFactor(final AxisID axisID) {
    return getValue(AttributeName.RUN_TIME, ALIGNED_STACK_MODULE_NAME, axisID,
        BIN_BY_FACTOR_NAME);
  }

  public String getAlignedStackSizeInXandY(final AxisID axisID) {
    return getValue(AttributeName.RUN_TIME, ALIGNED_STACK_MODULE_NAME, axisID,
        SIZE_IN_X_AND_Y_NAME);
  }

  public String getAlignedStackSizeInXandYDescr() {
    return AttributeName.RUN_TIME + "." + ALIGNED_STACK_MODULE_NAME + "..."
        + SIZE_IN_X_AND_Y_NAME;
  }

  Iterator<Entry<String, String>> getCopyArgExtraValuesIterator() {
    return copyArgExtraValues.entrySet().iterator();
  }

  ReadOnlyAttributeIterator getCopyArgIterator() {
    if (copyArg == null) {
      return null;
    }
    ReadOnlyAttributeList list = copyArg.getChildren();
    if (list != null) {
      return list.iterator();
    }
    return null;
  }

  public String getCTFplottingAutoFitRangeAndStep(final AxisID axisID) {
    return getValue(AttributeName.RUN_TIME, CTF_PLOTTING_MODULE_NAME, axisID,
        AUTO_FIT_RANGE_AND_STEP_NAME);
  }

  public String getCTFplottingAutoFitRangeAndStepDescr() {
    return AttributeName.RUN_TIME + "." + CTF_PLOTTING_MODULE_NAME + "..."
        + AUTO_FIT_RANGE_AND_STEP_NAME;
  }

  public SeedingMethod getFiducialsSeedingMethod(final AxisID axisID) {
    return SeedingMethod.getInstance(getValue(AttributeName.RUN_TIME,
        FIDUCIALS_MODULE_NAME, axisID, SEEDING_METHOD_NAME));
  }

  public String getFiducialsTrackingMethod(final AxisID axisID) {
    return getValue(AttributeName.RUN_TIME, FIDUCIALS_MODULE_NAME, axisID,
        TRACKING_METHOD_NAME);
  }

  public File getFile() {
    return file;
  }

  public String getGoldErasingBinning(final AxisID axisID) {
    return getValue(AttributeName.RUN_TIME, GOLD_ERASING_MODULE_NAME, axisID,
        BINNING_NAME);
  }

  public String getGoldErasingThickness(final AxisID axisID) {
    return getValue(AttributeName.RUN_TIME, GOLD_ERASING_MODULE_NAME, axisID,
        THICKNESS_NAME);
  }

  public String getPositioningBinByFactor(final AxisID axisID) {
    return getValue(AttributeName.RUN_TIME, POSITIONING_MODULE_NAME, axisID,
        BIN_BY_FACTOR_NAME);
  }

  public String getPositioningThickness(final AxisID axisID) {
    return getValue(AttributeName.RUN_TIME, POSITIONING_MODULE_NAME, axisID,
        THICKNESS_NAME);
  }

  public String getRaptorNumberOfMarkers(final AxisID axisID) {
    return getValue(AttributeName.RUN_TIME, RAPTOR_MODULE_NAME, axisID,
        NUMBER_OF_MARKERS_NAME);
  }

  public String getTemplate(final DirectiveFileType type) {
    if (type == DirectiveFileType.SCOPE) {
      return getValue(AttributeName.SETUP_SET, SCOPE_TEMPLATE_NAME);
    }
    if (type == DirectiveFileType.SYSTEM) {
      return getValue(AttributeName.SETUP_SET, SYSTEM_TEMPLATE_NAME);
    }

    if (type == DirectiveFileType.USER) {
      return getValue(AttributeName.SETUP_SET, USER_TEMPLATE_NAME);
    }
    return null;
  }

  /**
   * @param doValidation has no effect.
   * @return true
   */
  public boolean getTiltAngleFields(final AxisID axisID,
      final TiltAngleSpec tiltAngleSpec, final boolean doValidation) {
    if (tiltAngleSpec == null) {
      return true;
    }
    if (containsAttribute(AttributeName.COPY_ARG,
        convertAttributeName(axisID, FIRST_INC_NAME))) {
      tiltAngleSpec.setType(TiltAngleType.RANGE);
      String value = getValue(AttributeName.COPY_ARG,
          convertAttributeName(axisID, FIRST_INC_NAME));
      String[] arrayValue = null;
      if (value != null) {
        arrayValue = value.trim().split(FieldType.CollectionType.ARRAY.getSplitter());
      }
      if (arrayValue != null && arrayValue.length > 0) {
        tiltAngleSpec.setRangeMin(arrayValue[0]);
      }
      if (arrayValue != null && arrayValue.length > 1) {
        tiltAngleSpec.setRangeStep(arrayValue[1]);
      }
    }
    else if (toBoolean(getValue(AttributeName.COPY_ARG,
        convertAttributeName(axisID, EXTRACT_NAME)))) {
      tiltAngleSpec.setType(TiltAngleType.EXTRACT);
    }
    else if (toBoolean(getValue(AttributeName.COPY_ARG,
        convertAttributeName(axisID, USE_RAW_TLT_NAME)))) {
      tiltAngleSpec.setType(TiltAngleType.FILE);
    }
    return true;
  }

  public boolean isDual() {
    return isValue(AttributeName.COPY_ARG, DUAL_NAME);
  }

  public boolean isFiducialsFiducialless(final AxisID axisID) {
    return isValue(AttributeName.RUN_TIME, FIDUCIALS_MODULE_NAME, axisID,
        FIDUCIALLESS_NAME);
  }

  public boolean isPositioningWholeTomogram(final AxisID axisID) {
    return isValue(AttributeName.RUN_TIME, POSITIONING_MODULE_NAME, axisID,
        WHOLE_TOMOGRAM_NAME);
  }

  public boolean isRaptorUseAlignedStack(final AxisID axisID) {
    return isValue(AttributeName.RUN_TIME, RAPTOR_MODULE_NAME, axisID,
        USE_ALIGNED_STACK_NAME);
  }

  public boolean isReconstructionUseSirt(final AxisID axisID) {
    return isValue(AttributeName.RUN_TIME, RECONSTRUCTION_MODULE_NAME, axisID,
        USE_SIRT_NAME);
  }

  boolean isScanHeader() {
    return isValue(AttributeName.SETUP_SET, SCAN_HEADER_NAME);
  }

  public void setBinning(final int input) {
    setCopyArgValue(BINNING_NAME, Integer.toString(input));
  }

  public void setTwodir(final AxisID axisID, final double input) {
    setCopyArgValue(convertAttributeName(axisID, TWODIR_NAME), Double.toString(input));
  }

  public void setImageRotation(final String input) {
    setCopyArgValue(convertAttributeName(AxisID.FIRST, ROTATION_NAME), input);
    if (isDual()) {
      setCopyArgValue(convertAttributeName(AxisID.SECOND, ROTATION_NAME), input);
    }
  }

  public void setPixelSize(final double input) {
    setCopyArgValue(PIXEL_NAME, Double.toString(input));
  }

  public String toString() {
    return getFile().getAbsolutePath();
  }

  static final class FileName {
    static final FileName ALIGN = new FileName("align");

    private final String tag;

    private FileName(final String tag) {
      this.tag = tag;
    }
  }

  static final class Module {
    public static final Module ALIGNED_STACK = new Module("AlignedStack");
    public static final Module PREPROCESSING = new Module("Preprocessing");
    private final String tag;

    Module(final String tag) {
      this.tag = tag;
    }
  }

  private static final class DirectiveDef {
    private static final DirectiveDef ARCHIVE_ORIGINAL = new DirectiveDef(
        DirectiveType.RUN_TIME, Module.PREPROCESSING, "archiveOriginal");
    private static final DirectiveDef BIN_BY_FACTOR_FOR_ALIGNED_STACK = new DirectiveDef(
        DirectiveType.RUN_TIME, Module.ALIGNED_STACK, "binByFactor");

    private final DirectiveType directiveType;
    private final Module module;
    private final String name;

    private DirectiveDef(final DirectiveType directiveType, final Module module,
        final String name) {
      this.directiveType = directiveType;
      this.module = module;
      this.name = name;
    }
  }
}
