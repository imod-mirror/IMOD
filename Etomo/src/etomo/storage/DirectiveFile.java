package etomo.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import etomo.BaseManager;
import etomo.EtomoDirector;
import etomo.storage.autodoc.AutodocFactory;
import etomo.storage.autodoc.ReadOnlyAttribute;
import etomo.storage.autodoc.ReadOnlyAttributeIterator;
import etomo.storage.autodoc.ReadOnlyAttributeList;
import etomo.storage.autodoc.ReadOnlyAutodoc;
import etomo.type.AxisID;
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

  private static final String TRUE_VALUE = "1";
  private static final String FALSE_VALUE = "0";

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

  /**
   * Returns the attribute associated with directiveDef and axisID.  For copyarg: look
   * under the B directive when axisID is AxisID.SECOND, otherwise look under the A
   * directive.  For runtime:  look under the "any" directive tree.  If DirectiveDef.name
   * isn't a leaf of this tree, look under "a" for AxisID.ONLY or .FIRST, and look under
   * "b" for AxisID.SECOND.  For comparam use a simliar algorithm to runtime:  Look for a
   * comfile name with no postfix first.  If the name isn't a leaf of this directive tree,
   * look under comfilea for AxisID.FIRST, and comfileb for AxisID.SECOND.
   * @param directiveDef
   * @param axisID
   * @return
   */
  DirectiveAttribute getAttribute(final DirectiveDef directiveDef, final AxisID axisID) {
    // get parent attribute
    DirectiveType type = directiveDef.getDirectiveType();
    ReadOnlyAttribute attribute = getParentAttribute(type);
    if (attribute == null) {
      return null;
    }
    String name = directiveDef.getName(axisID);
    DirectiveAttribute directiveAttribute = new DirectiveAttribute(directiveDef.isBool());
    // copyarg and setupset
    if (type == DirectiveType.COPY_ARG || type == DirectiveType.SETUP_SET) {
      directiveAttribute.setAttribute(attribute.getAttribute(name));
      return directiveAttribute;
    }
    // runtime
    if (type == DirectiveType.RUN_TIME) {
      // get module attribute
      attribute = attribute.getAttribute(directiveDef.getModule());
      if (attribute == null) {
        return null;
      }
      // try different axis attributes to get to the name attribute
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
    }
    // comparam
    else if (type == DirectiveType.COM_PARAM) {
      // try different comfile postfixes to get to the name attribute
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
      }
    }
    directiveAttribute.setAttribute(attribute);
    return directiveAttribute;
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
      return attribute;
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
   * @param directiveDef
   * @param axisID
   * @return the attribute value or null if the attribute is null
   */
  String getValue(final DirectiveDef directiveDef, final AxisID axisID) {
    ReadOnlyAttribute attribute = getAttribute(directiveDef, axisID);
    if (attribute == null) {
      return null;
    }
    return attribute.getValue();
  }

  /**
   * Translates the value of the attribute into a boolean.  A null attribute or value
   * returns false.  A "0" value returns false.  A "1" returns true.  Everything else
   * returns true.
   * @param directiveDef
   * @param axisID
   * @return
   */
  boolean isValue(final DirectiveDef directiveDef, final AxisID axisID) {
    boolean bool = directiveDef.isBool();
    if (!bool) {
      System.err.println("Warning: " + directiveDef + " is not a boolean");
    }
    ReadOnlyAttribute attribute = getAttribute(directiveDef, axisID);
    if (attribute == null) {
      return false;
    }
    String value = attribute.getValue();
    if (value == null) {
      if (bool) {
        System.err.println("Warning: " + directiveDef
            + " is boolean and its value should not be null");
      }
      return false;
    }
    value = value.trim();
    if (value.equals(FALSE_VALUE)) {
      return false;
    }
    if (value.equals(TRUE_VALUE)) {
      return true;
    }
    if (bool) {
      System.err.println("Warning: " + directiveDef
          + " is boolean and its value is invalid: " + value);
    }
    return true;
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

  public String toString() {
    return getFile().getAbsolutePath();
  }

  static final class Module {
    public static final Module ALIGNED_STACK = new Module("AlignedStack");
    public static final Module CTF_PLOTTING = new Module("CTFplotting");
    public static final Module FIDUCIALS = new Module("Fiducials");
    public static final Module GOLD_ERASING = new Module("GoldErasing");
    public static final Module PATCH_TRACKING = new Module("PatchTracking");
    public static final Module POSITIONING = new Module("Positioning");
    public static final Module PREPROCESSING = new Module("Preprocessing");
    public static final Module RAPTOR = new Module("RAPTOR");
    public static final Module RECONSTRUCTION = new Module("Reconstruction");
    public static final Module TRIMVOL = new Module("Trimvol");

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
    static final Comfile SIRTSETUP = new Comfile("sirtsetup");
    static final Comfile TILT = new Comfile("tilt");
    static final Comfile TRACK = new Comfile("track");
    static final Comfile XCORR_PT = new Comfile("xcorr_pt");
    
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
    static final Command BEADTRACK = new Command("beadtrack");
    static final Command CCDERASER = new Command("ccderaser");
    static final Command SIRTSETUP = new Command("sirtsetup");
    static final Command TILT = new Command("tilt");
    static final Command TILTALIGN = new Command("tiltalign");
    static final Command TILTXCORR = new Command("tiltxcorr");
    
    private final String tag;

    private Command(final String tag) {
      this.tag = tag;
    }

    public String toString() {
      return tag;
    }
  }
}
