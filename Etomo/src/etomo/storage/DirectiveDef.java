package etomo.storage;

import etomo.storage.DirectiveAttribute.Match;
import etomo.storage.DirectiveDescrFile.Element;
import etomo.storage.DirectiveFile.Comfile;
import etomo.storage.DirectiveFile.Command;
import etomo.storage.DirectiveFile.Module;
import etomo.storage.autodoc.AutodocTokenizer;
import etomo.type.AxisID;
import etomo.type.AxisType;

/**
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright 2014 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
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
    BINNING_NAME);
  public static final DirectiveDef CS = new DirectiveDef(DirectiveType.COPY_ARG, "Cs");
  public static final DirectiveDef CTF_NOISE = new DirectiveDef(DirectiveType.COPY_ARG,
    "ctfnoise");
  public static final DirectiveDef DEFOCUS = new DirectiveDef(DirectiveType.COPY_ARG,
    "defocus");
  public static final DirectiveDef DISTORT = new DirectiveDef(DirectiveType.COPY_ARG,
    "distort");
  public static final DirectiveDef DUAL =
    new DirectiveDef(DirectiveType.COPY_ARG, "dual");
  public static final DirectiveDef EXTRACT = new DirectiveDef(DirectiveType.COPY_ARG,
    "extract");
  public static final DirectiveDef FIRST_INC = new DirectiveDef(DirectiveType.COPY_ARG,
    "firstinc");
  public static final DirectiveDef FOCUS = new DirectiveDef(DirectiveType.COPY_ARG,
    "focus");
  public static final DirectiveDef GOLD =
    new DirectiveDef(DirectiveType.COPY_ARG, "gold");
  public static final DirectiveDef GRADIENT = new DirectiveDef(DirectiveType.COPY_ARG,
    "gradient");
  public static final DirectiveDef MONTAGE = new DirectiveDef(DirectiveType.COPY_ARG,
    "montage");
  public static final DirectiveDef NAME =
    new DirectiveDef(DirectiveType.COPY_ARG, "name");
  public static final DirectiveDef PIXEL = new DirectiveDef(DirectiveType.COPY_ARG,
    "pixel");
  public static final DirectiveDef ROTATION = new DirectiveDef(DirectiveType.COPY_ARG,
    "rotation");
  public static final DirectiveDef SKIP =
    new DirectiveDef(DirectiveType.COPY_ARG, "skip");
  public static final DirectiveDef TWODIR = new DirectiveDef(DirectiveType.COPY_ARG,
    "twodir");
  public static final DirectiveDef USE_RAW_TLT = new DirectiveDef(DirectiveType.COPY_ARG,
    "userawtlt");
  public static final DirectiveDef VOLTAGE = new DirectiveDef(DirectiveType.COPY_ARG,
    "voltage");

  // setupset

  public static final DirectiveDef DATASET_DIRECTORY = new DirectiveDef(
    DirectiveType.SETUP_SET, "datasetDirectory");
  public static final DirectiveDef SCAN_HEADER = new DirectiveDef(
    DirectiveType.SETUP_SET, "scanHeader");
  public static final DirectiveDef SCOPE_TEMPLATE = new DirectiveDef(
    DirectiveType.SETUP_SET, "scopeTemplate");
  public static final DirectiveDef SYSTEM_TEMPLATE = new DirectiveDef(
    DirectiveType.SETUP_SET, "systemTemplate");
  public static final DirectiveDef USER_TEMPLATE = new DirectiveDef(
    DirectiveType.SETUP_SET, "userTemplate");

  // runtime

  public static final DirectiveDef BIN_BY_FACTOR_FOR_ALIGNED_STACK = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.ALIGNED_STACK, BIN_BY_FACTOR_NAME);
  public static final DirectiveDef CORRECT_CTF = new DirectiveDef(DirectiveType.RUN_TIME,
    Module.ALIGNED_STACK, "correctCTF");
  public static final DirectiveDef ERASE_GOLD = new DirectiveDef(DirectiveType.RUN_TIME,
    Module.ALIGNED_STACK, "eraseGold");
  public static final DirectiveDef FILTER_STACK = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.ALIGNED_STACK, "filterStack");
  public static final DirectiveDef LINEAR_INTERPOLATION = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.ALIGNED_STACK, "linearInterpolation");
  public static final DirectiveDef SIZE_IN_X_AND_Y = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.ALIGNED_STACK, "sizeInXandY");

  public static final DirectiveDef EXTRA_TARGETS = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.COMBINE, "extraTargets");
  public static final DirectiveDef FINAL_PATCH_SIZE = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.COMBINE, "finalPatchSize");
  public static final DirectiveDef LOW_FROM_BOTH_RADIUS = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.COMBINE, "lowFromBothRadius");
  public static final DirectiveDef PATCH_SIZE = new DirectiveDef(DirectiveType.RUN_TIME,
    Module.COMBINE, "patchSize");
  public static final DirectiveDef WEDGE_REDUCTION = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.COMBINE, "wedgeReduction");

  public static final DirectiveDef AUTO_FIT_RANGE_AND_STEP = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.CTF_PLOTTING, "autoFitRangeAndStep");

  public static final DirectiveDef FIDUCIALLESS = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.FIDUCIALS, "fiducialless");
  public static final DirectiveDef SEEDING_METHOD = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.FIDUCIALS, "seedingMethod");
  public static final DirectiveDef TRACKING_METHOD = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.FIDUCIALS, "trackingMethod");

  public static final DirectiveDef BINNING_FOR_GOLD_ERASING = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.GOLD_ERASING, BINNING_NAME);
  public static final DirectiveDef THICKNESS_FOR_GOLD_ERASING = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.GOLD_ERASING, THICKNESS_NAME);

  public static final DirectiveDef RAW_BOUNDARY_MODEL_FOR_PATCH_TRACKING =
    new DirectiveDef(DirectiveType.RUN_TIME, Module.PATCH_TRACKING, "rawBoundaryModel");

  public static final DirectiveDef BIN_BY_FACTOR_FOR_POSITIONING = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.POSITIONING, BIN_BY_FACTOR_NAME);
  public static final DirectiveDef THICKNESS_FOR_POSITIONING = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.POSITIONING, THICKNESS_NAME);
  public static final DirectiveDef WHOLE_TOMOGRAM = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.POSITIONING, "wholeTomogram");

  public static final DirectiveDef ARCHIVE_ORIGINAL = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.PREPROCESSING, "archiveOriginal");
  public static final DirectiveDef REMOVE_XRAYS = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.PREPROCESSING, "removeXrays");

  public static final DirectiveDef NUMBER_OF_MARKERS = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.RAPTOR, "numberOfMarkers");
  public static final DirectiveDef USE_ALIGNED_STACK = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.RAPTOR, "useAlignedStack");

  public static final DirectiveDef BINNED_THICKNESS = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.RECONSTRUCTION, "binnedThickness");
  public static final DirectiveDef DO_BACKPROJ_ALSO = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.RECONSTRUCTION, "doBackprojAlso");
  public static final DirectiveDef EXTRA_THICKNESS = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.RECONSTRUCTION, "extraThickness");
  public static final DirectiveDef FALLBACK_THICKNESS = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.RECONSTRUCTION, "fallbackThickness");
  public static final DirectiveDef USE_SIRT = new DirectiveDef(DirectiveType.RUN_TIME,
    Module.RECONSTRUCTION, "useSirt");

  public static final DirectiveDef RAW_BOUNDARY_MODEL_FOR_SEED_FINDING =
    new DirectiveDef(DirectiveType.RUN_TIME, Module.SEED_FINDING, "rawBoundaryModel");

  public static final DirectiveDef ENABLE_STRETCHING = new DirectiveDef(
    DirectiveType.RUN_TIME, Module.TILT_ALIGNMENT, "enableStretching");

  public static final DirectiveDef REORIENT = new DirectiveDef(DirectiveType.RUN_TIME,
    Module.TRIMVOL, "reorient");

  // comparam

  public static final DirectiveDef LOCAL_ALIGNMENTS = new DirectiveDef(
    DirectiveType.COM_PARAM, Comfile.ALIGN, Command.TILTALIGN, "LocalAlignments");
  public static final DirectiveDef SURFACES_TO_ANALYZE = new DirectiveDef(
    DirectiveType.COM_PARAM, Comfile.ALIGN, Command.TILTALIGN, "SurfacesToAnalyze");

  public static final DirectiveDef TWO_SURFACES = new DirectiveDef(
    DirectiveType.COM_PARAM, Comfile.AUTOFIDSEED, Command.AUTOFIDSEED, "TwoSurfaces");
  public static final DirectiveDef TARGET_NUMBER_OF_BEADS = new DirectiveDef(
    DirectiveType.COM_PARAM, Comfile.AUTOFIDSEED, Command.AUTOFIDSEED,
    "TargetNumberOfBeads");

  public static final DirectiveDef MODEL_FILE = new DirectiveDef(DirectiveType.COM_PARAM,
    Comfile.ERASER, Command.CCDERASER, "ModelFile");

  public static final DirectiveDef LEAVE_ITERATIONS = new DirectiveDef(
    DirectiveType.COM_PARAM, Comfile.SIRTSETUP, Command.SIRTSETUP, "LeaveIterations");
  public static final DirectiveDef SCALE_TO_INTEGER = new DirectiveDef(
    DirectiveType.COM_PARAM, Comfile.SIRTSETUP, Command.SIRTSETUP, "ScaleToInteger");

  public static final DirectiveDef THICKNESS = new DirectiveDef(DirectiveType.COM_PARAM,
    Comfile.TILT, Command.TILT, "THICKNESS");

  public static final DirectiveDef LOCAL_AREA_TARGET_SIZE = new DirectiveDef(
    DirectiveType.COM_PARAM, Comfile.TRACK, Command.BEADTRACK, "LocalAreaTargetSize");

  public static final DirectiveDef LENGTH_OF_PIECES = new DirectiveDef(
    DirectiveType.COM_PARAM, Comfile.XCORR_PT, Command.IMODCHOPCONTS, "LengthOfPieces");

  public static final DirectiveDef SIZE_OF_PATCHES_X_AND_Y = new DirectiveDef(
    DirectiveType.COM_PARAM, Comfile.XCORR_PT, Command.TILTXCORR, "SizeOfPatchesXandY");

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
  private boolean separateBDirective = false;
  private boolean bool = false;
  private boolean templateA = false;
  private boolean templateB = false;
  private boolean batchA = false;
  private boolean batchB = false;
  private boolean directiveDescrLoaded = false;

  /**
   * General constructor
   *
   * @param directiveType
   * @param module
   * @param comfile
   * @param command
   * @param name
   */
  private DirectiveDef(final DirectiveType directiveType, final Module module,
    final Comfile comfile, final Command command, final String name) {
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
  }

  /**
   * Constructor for setupset.copyarg and setupset
   *
   * @param directiveType
   * @param name
   */
  private DirectiveDef(final DirectiveType directiveType, final String name) {
    this(directiveType, null, null, null, name);
  }

  /**
   * Constructor for runtime
   *
   * @param directiveType
   * @param module
   * @param name
   */
  private DirectiveDef(final DirectiveType directiveType, final Module module,
    final String name) {
    this(directiveType, module, null, null, name);
  }

  /**
   * Constructor for comparam
   *
   * @param directiveType
   * @param comfile
   * @param command
   * @param name
   */
  private DirectiveDef(final DirectiveType directiveType, final Comfile comfile,
    final Command command, final String name) {
    this(directiveType, null, comfile, command, name);
  }

  /**
   * @return
   */
  DirectiveType getDirectiveType() {
    return directiveType;
  }

  /**
   * Load information about directive from the directive.csv file.
   */
  private void loadDirectiveDescr() {
    if (directiveDescrLoaded) {
      return;
    }
    directiveDescrLoaded = true;
    Element element = DirectiveDescrFile.INSTANCE.get(getKey(null));
    if (element != null) {
      DirectiveValueType type = element.getValueType();
      if (type == DirectiveValueType.BOOLEAN) {
        bool = true;
      }
      templateA = element.isTemplate();
      batchA = element.isBatch();
    }
    if (directiveType == DirectiveType.COPY_ARG) {
      element = DirectiveDescrFile.INSTANCE.get(getKey(AxisID.SECOND));
      if (element != null) {
        separateBDirective = true;
        templateB = element.isTemplate();
        batchB = element.isBatch();
      }
    }
    if (!separateBDirective) {
      templateB = templateA;
      batchB = batchA;
    }
  }

  boolean hasSecondaryMatch(final AxisID axisID) {
    if (directiveType == DirectiveType.COPY_ARG) {
      if (axisID == AxisID.SECOND) {
        loadDirectiveDescr();
        return templateB || !batchB;// No secondary match for non-templatable copyarg B
      }
      else {
        return false;
      }
    }
    return true;
  }

  public boolean isComparam() {
    return directiveType == DirectiveType.COM_PARAM;
  }

  public boolean isBoolean() {
    loadDirectiveDescr();
    return bool;
  }

  boolean isTemplate(final AxisID axisID) {
    loadDirectiveDescr();
    if (axisID == AxisID.SECOND) {
      return templateB;
    }
    return templateA;
  }

  boolean isBatch(final AxisID axisID) {
    loadDirectiveDescr();
    if (axisID == AxisID.SECOND) {
      return batchB;
    }
    return batchA;
  }

  /**
   * Get name according to which match is being done
   * * <p>How well an attribute matches an axis.  Primary is the first match, secondary is
   * the second match.</p>
   * <p>Copyarg:</p>
   * <p>axis = null: 1. no prefix, does not match b</p>
   * <p>axis = only: 1. no prefix, does not match b</p>
   * <p>axis = first: 1. no prefix, does not match b</p>
   * <p>axis = second:</p>
   * <p>Templatable:1. b prefix,2. no prefix</p>
   * <p>Not templateable: 1. b prefix, does not match no prefix</p>
   *
   * @param match
   * @param axisID
   * @return
   */
  String getName(final Match match, final AxisID axisID) {
    if (directiveType == DirectiveType.COPY_ARG && axisID == AxisID.SECOND) {
      loadDirectiveDescr();
      if (separateBDirective) {
        if (match == Match.PRIMARY) {
          return COPY_ARG_B_AXIS_PREFIX + name;
        }
        if (match == Match.SECONDARY) {
          // No secondary match for non-templatable copyarg B
          if (axisID == AxisID.SECOND && (templateB || !batchB)) {
            return name;
          }
          return null;
        }
      }
    }
    return name;
  }

  /**
   * @return module name for runtime directives
   */
  String getModule() {
    return module;
  }

  /**
   * Returns the axis tag.
   * <p>Runtime and Comparam:</p>
   * <p>axis = null:  1. any, 2. a, does not match b</p>
   * <p>axis = only: 1. any, 2. a, does not match b</p>
   * <p>axis = first: 1. a, 2. any, does not match b</p>
   * <p>axis = second: 1. b, 2. any, does not match a</p>
   *
   * @param match
   * @param axisID
   * @return axis name for runtime directives
   */
  String getAxis(final Match match, final AxisID axisID) {
    if (match == Match.PRIMARY) {
      if (axisID == AxisID.FIRST) {
        return RUN_TIME_A_AXIS_TAG;
      }
      if (axisID == AxisID.SECOND) {
        return RUN_TIME_B_AXIS_TAG;
      }
    }
    else if (match == Match.SECONDARY) {
      if (axisID == null || axisID == AxisID.ONLY) {
        return RUN_TIME_A_AXIS_TAG;
      }
    }
    return RUN_TIME_ANY_AXIS_TAG;
  }

  /**
   * @param match
   * @param axisID
   * @return axis name for runtime directives
   */
  String getComfile(final Match match, final AxisID axisID) {
    if (match == Match.PRIMARY) {
      if (axisID == AxisID.FIRST) {
        return comfile + AxisID.FIRST.getExtension();
      }
      if (axisID == AxisID.SECOND) {
        return comfile + AxisID.SECOND.getExtension();
      }
    }
    else if (match == Match.SECONDARY) {
      if (axisID == null || axisID == AxisID.ONLY) {
        return comfile + AxisID.FIRST.getExtension();
      }
    }
    return comfile;
  }

  /**
   * @return the command name element for comparam directives
   */
  public String getCommand() {
    return command;
  }

  public String getKey(final AxisID axisID) {
    return DirectiveName.makeKey(getDirective(axisID, null));
  }

  public String getName() {
    return name;
  }

  /**
   * Get name according to which match is being done
   *
   * @param axisID
   * @return
   */
  public String getName(final AxisID axisID) {
    loadDirectiveDescr();
    if (directiveType == DirectiveType.COPY_ARG && separateBDirective
      && axisID == AxisID.SECOND) {
      return COPY_ARG_B_AXIS_PREFIX + name;
    }
    return name;
  }

  /**
   * Returns the full directive string with the axis tag.  An unrecognized directive type
   * causes the function to return all of directive elements that are set, followed by
   * the axisID extension.
   *
   * @param axisID
   * @param axisType - when dual, forces the use of "a" in runtime and comparam directives
   * @return
   */
  public String getDirective(final AxisID axisID, final AxisType axisType) {
    return getPrefix() + getAxisTag(axisID, axisType) + getPostfix();
  }

  /**
   * Returns the directive string with no axis tag
   */
  public String toString() {
    return getPrefix() + getPostfix();
  }

  /**
   * @param axisID
   * @param axisType - when dual forces the use of "a" in runtime and comparam directives when axisID is FIRST
   * @return
   */
  private String getAxisTag(final AxisID axisID, final AxisType axisType) {
    if (directiveType == DirectiveType.COPY_ARG) {
      return separateBDirective && axisID == AxisID.SECOND ? COPY_ARG_B_AXIS_PREFIX : "";
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
   *
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
   *
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
