package etomo.storage;

import etomo.storage.DirectiveFile.Comfile;
import etomo.storage.DirectiveFile.Command;
import etomo.storage.DirectiveFile.Module;
import etomo.type.AxisID;

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

  private static final String BIN_BY_FACTOR_NAME = "binByFactor";
  private static final String BINNING_NAME = "binning";
  private static final String THICKNESS_NAME = "thickness";

  public static final DirectiveDef BINNING = new DirectiveDef(DirectiveType.COPY_ARG,
      BINNING_NAME, false);
  public static final DirectiveDef DISTORT = new DirectiveDef(DirectiveType.COPY_ARG,
      "distort", false);
  public static final DirectiveDef DUAL = new DirectiveDef(DirectiveType.COPY_ARG,
      "dual", false);
  public static final DirectiveDef EXTRACT = new DirectiveDef(DirectiveType.COPY_ARG,
      "extract", true);
  public static final DirectiveDef FIRST_INC = new DirectiveDef(DirectiveType.COPY_ARG,
      "firstinc", true);
  public static final DirectiveDef FOCUS = new DirectiveDef(DirectiveType.COPY_ARG,
      "focus", true);
  public static final DirectiveDef GOLD = new DirectiveDef(DirectiveType.COPY_ARG,
      "gold", false);
  public static final DirectiveDef GRADIENT = new DirectiveDef(DirectiveType.COPY_ARG,
      "gradient", false);
  public static final DirectiveDef MONTAGE = new DirectiveDef(DirectiveType.COPY_ARG,
      "montage", false);
  public static final DirectiveDef PIXEL = new DirectiveDef(DirectiveType.COPY_ARG,
      "pixel", false);
  public static final DirectiveDef ROTATION = new DirectiveDef(DirectiveType.COPY_ARG,
      "rotation", false);
  public static final DirectiveDef USE_RAW_TLT = new DirectiveDef(DirectiveType.COPY_ARG,
      "userawtlt", true);

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

  public static final DirectiveDef BIN_BY_FACTOR_FOR_ALIGNED_STACK = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.ALIGNED_STACK, BIN_BY_FACTOR_NAME);
  public static final DirectiveDef SIZE_IN_X_AND_Y = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.ALIGNED_STACK, "sizeInXandY");

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

  public static final DirectiveDef BIN_BY_FACTOR_FOR_POSITIONING = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.POSITIONING, BIN_BY_FACTOR_NAME);
  public static final DirectiveDef THICKNESS_FOR_POSITIONING = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.POSITIONING, THICKNESS_NAME);
  public static final DirectiveDef WHOLE_TOMOGRAM = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.POSITIONING, "wholeTomogram");

  public static final DirectiveDef REMOVE_XRAYS = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.PREPROCESSING, "removeXrays");

  public static final DirectiveDef NUMBER_OF_MARKERS = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.RAPTOR, "numberOfMarkers");
  public static final DirectiveDef USE_ALIGNED_STACK = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.RAPTOR, "useAlignedStack");

  public static final DirectiveDef USE_SIRT = new DirectiveDef(DirectiveType.RUN_TIME,
      Module.RECONSTRUCTION, "useSirt");

  public static final DirectiveDef ARCHIVE_ORIGINAL = new DirectiveDef(
      DirectiveType.RUN_TIME, Module.PREPROCESSING, "archiveOriginal");

  public static final DirectiveDef SURFACES_TO_ANALYZE = new DirectiveDef(
      DirectiveType.COM_PARAM, Comfile.ALIGN, Command.TILTALIGN, "SurfacesToAnalyze");
  
  public static final DirectiveDef TWO_SURFACES = new DirectiveDef(
      DirectiveType.COM_PARAM, Comfile.AUTOFIDSEED, Command.AUTOFIDSEED, "TwoSurfaces");

  public static final DirectiveDef MODEL_FILE = new DirectiveDef(DirectiveType.COM_PARAM,
      Comfile.ERASER, Command.CCDERASER, "ModelFile");

  public static final DirectiveDef LOCAL_AREA_TARGET_SIZE = new DirectiveDef(
      DirectiveType.COM_PARAM, Comfile.TRACK, Command.BEADTRACK, "LocalAreaTargetSize");

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

  /**
   * Global constructor
   * @param directiveType
   * @param module
   * @param comfile
   * @param command
   * @param name
   */
  private DirectiveDef(final DirectiveType directiveType, final Module module,
      final Comfile comfile, final Command command, final String name,
      final boolean twoAxis) {
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
  }

  /**
   * Constructor for setupset.copyarg
   * @param directiveType
   * @param name
   */
  private DirectiveDef(final DirectiveType directiveType, final String name,
      final boolean twoAxis) {
    this(directiveType, null, null, null, name, twoAxis);
  }

  /**
   * Constructor for setupset
   * @param directiveType
   * @param name
   */
  private DirectiveDef(final DirectiveType directiveType, final String name) {
    this(directiveType, null, null, null, name, false);
  }

  /**
   * Constructor for runtime
   * @param directiveType
   * @param module
   * @param name
   */
  private DirectiveDef(final DirectiveType directiveType, final Module module,
      final String name) {
    this(directiveType, module, null, null, name, true);
  }

  /**
   * Constructor for comparam
   * @param directiveType
   * @param comfile
   * @param command
   * @param name
   */
  private DirectiveDef(final DirectiveType directiveType, final Comfile comfile,
      final Command command, final String name) {
    this(directiveType, null, comfile, command, name, true);
  }

  DirectiveType getDirectiveType() {
    return directiveType;
  }

  String getModule() {
    return module;
  }

  String getComfile(final AxisID axisID) {
    if (comfile != null) {
      return comfile + (axisID != null ? axisID.getExtension() : "");
    }
    return null;
  }

  String getCommand() {
    return command;
  }

  String getName(final AxisID axisID) {
    if (twoAxis && directiveType == DirectiveType.COPY_ARG && axisID == AxisID.SECOND) {
      return AxisID.SECOND.getExtension() + name;
    }
    return name;
  }

  public String getKey(final AxisID axisID) {
    return directiveType + comfile + command + module + name
        + (axisID == AxisID.SECOND ? axisID.getExtension() : "");
  }

  public String getDescr() {
    if (directiveType == DirectiveType.COPY_ARG
        || directiveType == DirectiveType.SETUP_SET) {
      return directiveType + "." + name;
    }
    else if (directiveType == DirectiveType.RUN_TIME) {
      return directiveType + "." + module + "..." + name;
    }
    else if (directiveType == DirectiveType.COM_PARAM) {
      return directiveType + "." + comfile + "..." + command + name;
    }
    return null;
  }
}
