package etomo.storage;

import etomo.storage.DirectiveFile.Comfile;
import etomo.storage.DirectiveFile.Command;
import etomo.storage.DirectiveFile.Module;
import etomo.storage.autodoc.AutodocTokenizer;
import etomo.type.AxisID;
import etomo.type.AxisType;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright 2014</p>
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
public final class DirectiveDef {
  public static final String rcsid = "$Id:$";

  public static final String RUN_TIME_ANY_AXIS_TAG = "any";
  public static final String RUN_TIME_A_AXIS_TAG = "a";
  public static final String RUN_TIME_B_AXIS_TAG = "b";
  private static final String COPY_ARG_B_AXIS_PREFIX = "b";

  private static final String BIN_BY_FACTOR_NAME = "binByFactor";
  private static final String BINNING_NAME = "binning";
  private static final String THICKNESS_NAME = "thickness";

  // copyarg

  public static final DirectiveDef BINNING = new DirectiveDef(DirectiveType.COPY_ARG,
      BINNING_NAME, false, false);
  public static final DirectiveDef CS = new DirectiveDef(DirectiveType.COPY_ARG, "Cs",
      false, false);
  public static final DirectiveDef CTF_NOISE = new DirectiveDef(DirectiveType.COPY_ARG,
      "ctfnoise", false, false);
  public static final DirectiveDef DEFOCUS = new DirectiveDef(DirectiveType.COPY_ARG,
      "defocus", false, false);
  public static final DirectiveDef DISTORT = new DirectiveDef(DirectiveType.COPY_ARG,
      "distort", false, false);
  public static final DirectiveDef DUAL = new DirectiveDef(DirectiveType.COPY_ARG,
      "dual", false, true);
  public static final DirectiveDef EXTRACT = new DirectiveDef(DirectiveType.COPY_ARG,
      "extract", true, true);
  public static final DirectiveDef FIRST_INC = new DirectiveDef(DirectiveType.COPY_ARG,
      "firstinc", true, false);
  public static final DirectiveDef FOCUS = new DirectiveDef(DirectiveType.COPY_ARG,
      "focus", true, true);
  public static final DirectiveDef GOLD = new DirectiveDef(DirectiveType.COPY_ARG,
      "gold", false, false);
  public static final DirectiveDef GRADIENT = new DirectiveDef(DirectiveType.COPY_ARG,
      "gradient", false, false);
  public static final DirectiveDef MONTAGE = new DirectiveDef(DirectiveType.COPY_ARG,
      "montage", false, true);
  public static final DirectiveDef NAME = new DirectiveDef(DirectiveType.COPY_ARG,
      "name", false, false);
  public static final DirectiveDef PIXEL = new DirectiveDef(DirectiveType.COPY_ARG,
      "pixel", false, false);
  public static final DirectiveDef ROTATION = new DirectiveDef(DirectiveType.COPY_ARG,
      "rotation", true, false);
  public static final DirectiveDef SKIP = new DirectiveDef(DirectiveType.COPY_ARG,
      "skip", true, false);
  public static final DirectiveDef TWODIR = new DirectiveDef(DirectiveType.COPY_ARG,
      "twodir", true, false);
  public static final DirectiveDef USE_RAW_TLT = new DirectiveDef(DirectiveType.COPY_ARG,
      "userawtlt", true, true);
  public static final DirectiveDef VOLTAGE = new DirectiveDef(DirectiveType.COPY_ARG,
      "voltage", false, false);

  // setupset

  public static final DirectiveDef DATASET_DIRECTORY = new DirectiveDef(
      DirectiveType.SETUP_SET, "datasetDirectory", false);
  public static final DirectiveDef SCAN_HEADER = new DirectiveDef(
      DirectiveType.SETUP_SET, "scanHeader", true);
  public static final DirectiveDef SCOPE_TEMPLATE = new DirectiveDef(
      DirectiveType.SETUP_SET, "scopeTemplate", false);
  public static final DirectiveDef SYSTEM_TEMPLATE = new DirectiveDef(
      DirectiveType.SETUP_SET, "systemTemplate", false);
  public static final DirectiveDef USER_TEMPLATE = new DirectiveDef(
      DirectiveType.SETUP_SET, "userTemplate", false);

  // runtime

  public static final DirectiveDef BIN_BY_FACTOR_FOR_ALIGNED_STACK = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.ALIGNED_STACK, BIN_BY_FACTOR_NAME, false);
  public static final DirectiveDef CORRECT_CTF = new DirectiveDef(DirectiveType.RUN_TIME,
      Module.ALIGNED_STACK, "correctCTF", true);
  public static final DirectiveDef ERASE_GOLD = new DirectiveDef(DirectiveType.RUN_TIME,
      Module.ALIGNED_STACK, "eraseGold", false);
  public static final DirectiveDef FILTER_STACK = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.ALIGNED_STACK, "filterStack", true);
  public static final DirectiveDef LINEAR_INTERPOLATION = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.ALIGNED_STACK, "linearInterpolation", true);
  public static final DirectiveDef SIZE_IN_X_AND_Y = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.ALIGNED_STACK, "sizeInXandY", false);

  public static final DirectiveDef AUTO_FIT_RANGE_AND_STEP = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.CTF_PLOTTING, "autoFitRangeAndStep", false);

  public static final DirectiveDef FIDUCIALLESS = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.FIDUCIALS, "fiducialless", true);
  public static final DirectiveDef SEEDING_METHOD = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.FIDUCIALS, "seedingMethod", false);
  public static final DirectiveDef TRACKING_METHOD = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.FIDUCIALS, "trackingMethod", false);

  public static final DirectiveDef BINNING_FOR_GOLD_ERASING = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.GOLD_ERASING, BINNING_NAME, false);
  public static final DirectiveDef THICKNESS_FOR_GOLD_ERASING = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.GOLD_ERASING, THICKNESS_NAME, false);

  public static final DirectiveDef CONTOUR_PIECES = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.PATCH_TRACKING, "contourPieces", false);

  public static final DirectiveDef BIN_BY_FACTOR_FOR_POSITIONING = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.POSITIONING, BIN_BY_FACTOR_NAME, false);
  public static final DirectiveDef THICKNESS_FOR_POSITIONING = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.POSITIONING, THICKNESS_NAME, false);
  public static final DirectiveDef WHOLE_TOMOGRAM = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.POSITIONING, "wholeTomogram", true);

  public static final DirectiveDef ARCHIVE_ORIGINAL = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.PREPROCESSING, "archiveOriginal", true);
  public static final DirectiveDef REMOVE_XRAYS = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.PREPROCESSING, "removeXrays", true);

  public static final DirectiveDef NUMBER_OF_MARKERS = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.RAPTOR, "numberOfMarkers", false);
  public static final DirectiveDef USE_ALIGNED_STACK = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.RAPTOR, "useAlignedStack", true);

  public static final DirectiveDef BINNED_THICKNESS = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.RECONSTRUCTION, "binnedThickness", false);
  public static final DirectiveDef DO_BACKPROJ_ALSO = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.RECONSTRUCTION, "doBackprojAlso", true);
  public static final DirectiveDef EXTRA_THICKNESS = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.RECONSTRUCTION, "extraThickness", false);
  public static final DirectiveDef FALLBACK_THICKNESS = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.RECONSTRUCTION, "fallbackThickness", false);
  public static final DirectiveDef USE_SIRT = new DirectiveDef(DirectiveType.RUN_TIME,
      Module.RECONSTRUCTION, "useSirt", true);

  public static final DirectiveDef REORIENT = new DirectiveDef(DirectiveType.RUN_TIME,
      Module.TRIMVOL, "reorient", false);

  // comparam

  public static final DirectiveDef SURFACES_TO_ANALYZE = new DirectiveDef(
      DirectiveType.COM_PARAM, Comfile.ALIGN, Command.TILTALIGN, "SurfacesToAnalyze",
      false);

  public static final DirectiveDef TWO_SURFACES = new DirectiveDef(
      DirectiveType.COM_PARAM, Comfile.AUTOFIDSEED, Command.AUTOFIDSEED, "TwoSurfaces",
      true);
  public static final DirectiveDef TARGET_NUMBER_OF_BEADS = new DirectiveDef(
      DirectiveType.COM_PARAM, Comfile.AUTOFIDSEED, Command.AUTOFIDSEED,
      "TargetNumberOfBeads", false);

  public static final DirectiveDef MODEL_FILE = new DirectiveDef(DirectiveType.COM_PARAM,
      Comfile.ERASER, Command.CCDERASER, "ModelFile", false);

  public static final DirectiveDef LEAVE_ITERATIONS = new DirectiveDef(
      DirectiveType.COM_PARAM, Comfile.SIRTSETUP, Command.SIRTSETUP, "LeaveIterations",
      false);
  public static final DirectiveDef SCALE_TO_INTEGER = new DirectiveDef(
      DirectiveType.COM_PARAM, Comfile.SIRTSETUP, Command.SIRTSETUP, "ScaleToInteger",
      false);

  public static final DirectiveDef THICKNESS = new DirectiveDef(DirectiveType.COM_PARAM,
      Comfile.TILT, Command.TILT, "THICKNESS", false);

  public static final DirectiveDef LOCAL_AREA_TARGET_SIZE = new DirectiveDef(
      DirectiveType.COM_PARAM, Comfile.TRACK, Command.BEADTRACK, "LocalAreaTargetSize",
      false);

  public static final DirectiveDef SIZE_OF_PATCHES_X_AND_Y = new DirectiveDef(
      DirectiveType.COM_PARAM, Comfile.XCORR_PT, Command.TILTXCORR, "SizeOfPatchesXandY",
      false);

  private final DirectiveType directiveType;
  private final String module;
  private final String comfile;
  private final String command;
  private final String name;
  /**
   * True if this is a copyarg directive, there is a "b" version of the directive, and this
   * is not the "b" version of the directive.
   * True if the directive type is runtime comparam.
   */
  private final boolean twoAxis;
  private final boolean bool;

  /**
   * General constructor
   * @param directiveType
   * @param module
   * @param comfile
   * @param command
   * @param name
   */
  private DirectiveDef(final DirectiveType directiveType, final Module module,
      final Comfile comfile, final Command command, final String name,
      final boolean twoAxis, final boolean bool) {
    this.directiveType = directiveType;
    if (module != null) {
      this.module = module.toString();
    }
    else {
      this.module = null;
    }
    if (comfile != null) {
      this.comfile = comfile.toString();
    }
    else {
      this.comfile = null;
    }
    if (command != null) {
      this.command = command.toString();
    }
    else {
      this.command = null;
    }
    this.name = name;
    this.twoAxis = twoAxis;
    this.bool = bool;
  }

  /**
   * Constructor for setupset.copyarg
   * @param directiveType
   * @param name
   */
  private DirectiveDef(final DirectiveType directiveType, final String name,
      final boolean twoAxis, final boolean bool) {
    this(directiveType, null, null, null, name, twoAxis, bool);
  }

  /**
   * Constructor for setupset
   * @param directiveType
   * @param name
   */
  private DirectiveDef(final DirectiveType directiveType, final String name, boolean bool) {
    this(directiveType, null, null, null, name, false, bool);
  }

  /**
   * Constructor for runtime
   * @param directiveType
   * @param module
   * @param name
   */
  private DirectiveDef(final DirectiveType directiveType, final Module module,
      final String name, final boolean bool) {
    this(directiveType, module, null, null, name, true, bool);
  }

  /**
   * Constructor for comparam
   * @param directiveType
   * @param comfile
   * @param command
   * @param name
   */
  private DirectiveDef(final DirectiveType directiveType, final Comfile comfile,
      final Command command, final String name, final boolean bool) {
    this(directiveType, null, comfile, command, name, true, bool);
  }

  /**
   * @return
   */
  DirectiveType getDirectiveType() {
    return directiveType;
  }

  /**
   * @return module name for runtime directives
   */
  String getModule() {
    return module;
  }

  boolean isBool() {
    return bool;
  }

  /**
   * Returns the comfile name for comparam directives
   * @param axisID
   * @param axisType - when dual, forces the use of the "a" postfix
   * @return
   */
  String getComfile(final AxisID axisID, final AxisType axisType) {
    if (comfile != null) {
      return comfile + getAxisTag(axisID, axisType);
    }
    return null;
  }

  String getComfile() {
    if (comfile != null) {
      return comfile;
    }
    return null;
  }

  /**
   * @return the command name element for comparam directives
   */
  String getCommand() {
    return command;
  }

  /**
   * Returns the name element.  For copyarg, returns the "b" name when axisID is
   * AxisID.SECOND.
   * @param axisID
   * @return
   */
  public String getName(final AxisID axisID) {
    if (twoAxis && directiveType == DirectiveType.COPY_ARG && axisID == AxisID.SECOND) {
      return AxisID.SECOND.getExtension() + name;
    }
    return name;
  }

  /**
   * Returns the full directive string with the axis tag.  An unrecognized directive type
   * causes the function to return all of directive elements that are set, followed by
   * the axisID extension.
   * @param axisID
   * @param axisType - when dual, forces the use of "a" in runtime and comparam directives
   * @return
   */
  public String getDirective(final AxisID axisID, final AxisType axisType) {
    return getPrefix() + getAxisTag(axisID, axisType) + getPostfix();
  }

  public String getKey(final AxisID axisID) {
    DirectiveName directiveName=new DirectiveName();
    directiveName.setKey(this,axisID);
    return directiveName.getKey();
  }

  /**
   * Returns the directive string with no axis tag
   */
  public String toString() {
    return getPrefix() + getPostfix();
  }

  /**
   * Returns the axis tag.
   * @param axisID
   * @param axisType - when dual forces the use of "a" in runtime and comparam directives when axisID is FIRST
   * @return
   */
  private String getAxisTag(final AxisID axisID, final AxisType axisType) {
    if (directiveType == DirectiveType.COPY_ARG) {
      return twoAxis && axisID == AxisID.SECOND ? COPY_ARG_B_AXIS_PREFIX : "";
    }
    if (directiveType == DirectiveType.SETUP_SET) {
      return "";
    }
    String axisTag;
    if (directiveType == DirectiveType.RUN_TIME) {
      if (axisID == AxisID.SECOND) {
        return RUN_TIME_B_AXIS_TAG;
      }
      if (axisType == AxisType.DUAL_AXIS && axisID == AxisID.FIRST) {
        return RUN_TIME_A_AXIS_TAG;
      }
      return RUN_TIME_ANY_AXIS_TAG;
    }
    if (directiveType == DirectiveType.COM_PARAM) {
      if (axisID == AxisID.SECOND
          || (axisType == AxisType.DUAL_AXIS && axisID == AxisID.FIRST)) {
        return axisID.getExtension();
      }
      return AxisID.ONLY.getExtension();
    }
    return (axisID != null && axisID != AxisID.ONLY) ? (AutodocTokenizer.SEPARATOR_CHAR + axisID
        .getExtension()) : "";
  }

  /**
   * Creates the directive string up to the axis tag, or the whole directive string for
   * directives with no axis tag.
   * @return
   */
  private String getPrefix() {
    if (directiveType == DirectiveType.COPY_ARG) {
      return directiveType.getKey() + AutodocTokenizer.SEPARATOR_CHAR;
    }
    if (directiveType == DirectiveType.SETUP_SET) {
      return directiveType.getKey() + AutodocTokenizer.SEPARATOR_CHAR + name;
    }
    if (directiveType == DirectiveType.RUN_TIME) {
      return directiveType.getKey() + AutodocTokenizer.SEPARATOR_CHAR + module
          + AutodocTokenizer.SEPARATOR_CHAR;
    }
    if (directiveType == DirectiveType.COM_PARAM) {
      return directiveType.getKey() + AutodocTokenizer.SEPARATOR_CHAR + comfile;
    }
    return (directiveType != null ? directiveType.getKey() : "")
        + (module != null ? AutodocTokenizer.SEPARATOR_CHAR + module : module)
        + (comfile != null ? AutodocTokenizer.SEPARATOR_CHAR + comfile : comfile)
        + (command != null ? AutodocTokenizer.SEPARATOR_CHAR + command : command)
        + (name != null ? AutodocTokenizer.SEPARATOR_CHAR + name : name);
  }

  /**
   * Creates the directive string after the axis tag.  Returns nothing for directives with
   * no axis tag.
   * @return
   */
  private String getPostfix() {
    if (directiveType == DirectiveType.COPY_ARG) {
      return name;
    }
    if (directiveType == DirectiveType.SETUP_SET) {
      return "";
    }
    if (directiveType == DirectiveType.RUN_TIME) {
      return AutodocTokenizer.SEPARATOR_CHAR + name;
    }
    if (directiveType == DirectiveType.COM_PARAM) {
      return AutodocTokenizer.SEPARATOR_CHAR + command + AutodocTokenizer.SEPARATOR_CHAR
          + name;
    }
    return "";
  }
}
