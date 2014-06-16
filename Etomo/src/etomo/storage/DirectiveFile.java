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
import etomo.storage.autodoc.AutodocFactory;
import etomo.storage.autodoc.ReadOnlyAttribute;
import etomo.storage.autodoc.ReadOnlyAttributeIterator;
import etomo.storage.autodoc.ReadOnlyAttributeList;
import etomo.storage.autodoc.ReadOnlyAutodoc;
import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.type.ConstEtomoNumber;
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

  public static final String CS_NAME = "Cs";
  public static final String CTF_NOISE_NAME = "ctfnoise";
  public static final String DEFOCUS_NAME = "defocus";
  static final String FALSE_VALUE = "0";
  public static final String NAME_NAME = "name";
  public static final String REORIENT_NAME = "reorient";
  public static final String ROTATION_NAME = "rotation";
  public static final String SKIP_NAME = "skip";
  static final String SURFACES_TO_ANALYZE_NAME = "SurfacesToAnalyze";
  static final String TILT_ALIGN_COMMAND = "tiltalign";
  static final String TRUE_VALUE = "1";
  public static final String TWODIR_NAME = "twodir";
  public static final String TWO_SURFACES_NAME = "TwoSurfaces";
  public static final String VOLTAGE_NAME = "voltage";

  private static boolean debug = false;

  private final AxisID axisID;
  private final BaseManager manager;
  private final File file;

  private ReadOnlyAttribute copyArg = null;
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

  /**
   * Returns the leaf (name) attribute defined by directiveDef.  For copyarg: look under
   * the B directive when axisID is AxisID.SECOND, otherwise look under the A directive.
   * For runtime:  look under the "any" directive tree.  If DirectiveDef.name isn't a leaf
   * of this tree, look under "a" for AxisID.ONLY or .FIRST.  And look under "b" for
   * AxisID.SECOND.  For comparam use a simliar algorithm to runtime:  Look for a comfile
   * name with no postfix first.  If the name isn't a leaf of this directive tree, look
   * under comfilea for AxisID.FIRST, and comfileb for AxisID.SECOND.
   * @param directiveDef
   * @param axisID
   * @return the attribute of the directive name
   */
  public ReadOnlyAttribute getAttribute(final DirectiveDef directiveDef,
      final AxisID axisID) {
    // get parent attribute
    DirectiveType type = directiveDef.getDirectiveType();
    ReadOnlyAttribute attribute = getParentAttribute(type);
    if (attribute == null) {
      return null;
    }
    String name = directiveDef.getName(axisID);
    // get the directive name attribute
    if (type == DirectiveType.COPY_ARG || type == DirectiveType.SETUP_SET) {
      attribute = attribute.getAttribute(name);
    }
    else if (type == DirectiveType.RUN_TIME) {
      // get module attribute
      attribute = attribute.getAttribute(directiveDef.getModule());
      if (attribute == null) {
        return null;
      }
      // try different axis attributes to get the name attribute
      attribute = getAttribute(attribute, DirectiveDef.RUN_TIME_ANY_AXIS_TAG, name, null);
      if (attribute == null) {
        // Can't find name under "any" - look under the axis letter.
        String axisTag;
        if (axisID == AxisID.ONLY || axisID == AxisID.FIRST) {
          axisTag = DirectiveDef.RUN_TIME_A_AXIS_TAG;
        }
        else if (axisID == AxisID.SECOND) {
          axisTag = DirectiveDef.RUN_TIME_B_AXIS_TAG;
        }
        else {
          return null;
        }
        attribute = getAttribute(attribute, axisTag, name, null);
      }
      if (attribute == null) {
        return null;
      }
    }
    else if (type == DirectiveType.COM_PARAM) {
      // try different comfile postfixes to get the name attribute
      attribute = getAttribute(attribute, directiveDef.getComfile(),
          directiveDef.getCommand(), name);
      if (attribute == null) {
        String axisTag;
        // Can't find name with no axis tag - try the a or b postfix.
        if (axisID == AxisID.FIRST) {
          axisTag = AxisID.FIRST.getExtension();
        }
        else if (axisID == AxisID.SECOND) {
          axisTag = axisID.getExtension();
        }
        else {
          return null;
        }
        attribute = getAttribute(attribute, directiveDef.getComfile() + axisTag,
            directiveDef.getCommand(), name);
        if (attribute == null) {
          return null;
        }
      }
    }
    return attribute;
  }

  /**
   * Get the descendant of attribute, up to three levels down.
   * @param attribute
   * @param name1
   * @param name2
   * @param name3
   * @return
   */
  private ReadOnlyAttribute getAttribute(ReadOnlyAttribute attribute, final String name1,
      final String name2, final String name3) {
    if (attribute == null || name1 == null) {
      return null;
    }
    attribute = attribute.getAttribute(name1);
    if (attribute == null || name2 == null) {
      return attribute;
    }
    attribute = attribute.getAttribute(name2);
    if (attribute == null || name3 == null) {
      return attribute;
    }
    return attribute.getAttribute(name3);
  }

  /**
   * Returns true if the leaf attribute defined by directiveDef is present.
   * @param directiveDef
   * @param axisID
   * @return
   */
  public boolean contains(final DirectiveDef directiveDef, final AxisID axisID) {
    return getAttribute(directiveDef, axisID) != null;
  }

  /**
   * Returns true if the leaf attribute defined by directiveDef is present.
   * @param directiveDef
   * @return
   */
  public boolean contains(final DirectiveDef directiveDef) {
    return getAttribute(directiveDef, null) != null;
  }

  /**
   * Returns the value of the leaf attribute defined by directiveDef.  Returns null
   * of the attribute is missing.  Returns an empty string if this attribute is there and
   * it has no value.
   * @param directiveDef
   * @param axisID
   * @return
   */
  public String getValue(final DirectiveDef directiveDef, final AxisID axisID) {
    ReadOnlyAttribute attribute = getAttribute(directiveDef, axisID);
    if (attribute != null) {
      String value = attribute.getValue();
      if (value == null) {
        return "";
      }
      return value;
    }
    return null;
  }

  public boolean isValue(final DirectiveDef directiveDef, final AxisID axisID) {
    return toBoolean(getValue(directiveDef, axisID));
  }

  public boolean isValue(final DirectiveDef directiveDef) {
    return toBoolean(getValue(directiveDef, null));
  }

  public String getValue(final DirectiveDef directiveDef) {
    return getValue(directiveDef, null);
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

  /**
   * Puts the name/value pair into extra values if it isn't in copyArg.
   * @param name
   * @param value
   */
  private void setCopyArgValue(final String name, final String value) {
    if (copyArg.getAttribute(name) == null) {
      if (extraValues == null) {
        extraValues = new HashMap<String, String>();
      }
      if (extraValues.containsKey(name)) {
        extraValues.remove(name);
      }
      extraValues.put(name, value);
    }
  }

  /**
   * Puts the DirectiveDef+axisID/value pair into extra-values if the directive isn't in
   * the directive file.
   * @param directiveDef
   * @param value
   * @param axisID
   */
  void setValue(final DirectiveDef directiveDef, final AxisID axisID, final String value) {
    if (!contains(directiveDef, axisID)) {
      if (extraValues == null) {
        extraValues = new HashMap<String, String>();
      }
      String key = directiveDef.getKey(axisID);
      if (extraValues.containsKey(key)) {
        extraValues.remove(key);
      }
      extraValues.put(key, value);
    }
  }

  /**
   * Puts the DirectiveDef/value pair into extra-values if the directive isn't in
   * the directive file.
   * @param directiveDef
   * @param value
   */
  void setValue(final DirectiveDef directiveDef, final int value) {
    setValue(directiveDef, null, String.valueOf(value));
  }

  boolean containsExtraValue(final DirectiveType directiveType, final String name) {
    return directiveType == DirectiveType.COPY_ARG && extraValues != null
        && extraValues.containsKey(name);
  }

  boolean containsExtraValue(final DirectiveDef directiveDef, final AxisID axisID) {
    return extraValues != null && extraValues.containsKey(directiveDef.getKey(axisID));
  }

  String getExtraValue(final DirectiveDef directiveDef) {
    String key = directiveDef.getKey(axisID);
    if (extraValues != null && extraValues.containsKey(key)) {
      return extraValues.get(key);
    }
    return null;
  }

  boolean containsExtraValue(final DirectiveDef directiveDef) {
    return containsExtraValue(directiveDef, null);
  }

  String getExtraValue(final DirectiveType directiveType, final String name) {
    if (directiveType == DirectiveType.COPY_ARG && extraValues != null) {
      return extraValues.get(name);
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
    if (containsAttribute(section, RUN_TIME_ANY_AXIS_NAME, name)) {
      value = getValue(section, RUN_TIME_ANY_AXIS_NAME, name);
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

  public static void setDebug(final boolean input) {
    debug = input;
  }

  Iterator<Entry<String, String>> getCopyArgExtraValuesIterator() {
    return extraValues.entrySet().iterator();
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

  public File getFile() {
    return file;
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
    if (contains(DirectiveDef.FIRST_INC, axisID)) {
      tiltAngleSpec.setType(TiltAngleType.RANGE);
      String value = getValue(DirectiveDef.FIRST_INC, axisID);
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
    else if (isValue(DirectiveDef.EXTRACT, axisID)) {
      tiltAngleSpec.setType(TiltAngleType.EXTRACT);
    }
    else if (isValue(DirectiveDef.USE_RAW_TLT, axisID)) {
      tiltAngleSpec.setType(TiltAngleType.FILE);
    }
    return true;
  }

  public void setTwodir(final AxisID axisID, final double input) {
    setCopyArgValue(convertAttributeName(axisID, TWODIR_NAME), Double.toString(input));
  }

  public void setImageRotation(final String input) {
    setCopyArgValue(convertAttributeName(AxisID.FIRST, ROTATION_NAME), input);
    if (isValue(DirectiveDef.DUAL)) {
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
    public static final Module CTF_PLOTTING = new Module("CTFplotting");
    public static final Module FIDUCIALS = new Module("Fiducials");
    public static final Module GOLD_ERASING = new Module("GoldErasing");
    public static final Module POSITIONING = new Module("Positioning");
    public static final Module PREPROCESSING = new Module("Preprocessing");
    public static final Module RAPTOR = new Module("RAPTOR");
    public static final Module RECONSTRUCTION = new Module("Reconstruction");

    private final String tag;

    private Module(final String tag) {
      this.tag = tag;
    }

    public String toString() {
      return tag;
    }
  }

  static final class Comfile {
    static final Comfile ALIGN = new Comfile("align");
    static final Comfile AUTOFIDSEED = new Comfile("autofidseed");
    static final Comfile ERASER = new Comfile("eraser");
    static final Comfile TRACK = new Comfile("track");

    private final String tag;

    private Comfile(final String tag) {
      this.tag = tag;
    }

    public String toString() {
      return tag;
    }
  }

  static final class Command {
    static final Command AUTOFIDSEED = new Command("autofidseed");
    static final Command CCDERASER = new Command("ccderaser");
    static final Command BEADTRACK = new Command("beadtrack");
    static final Command TILTALIGN = new Command("tiltalign");

    private final String tag;

    private Command(final String tag) {
      this.tag = tag;
    }

    public String toString() {
      return tag;
    }
  }
}
