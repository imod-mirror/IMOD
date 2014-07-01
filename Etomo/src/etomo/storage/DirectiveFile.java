package etomo.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import etomo.BaseManager;
import etomo.EtomoDirector;
import etomo.storage.DirectiveAttribute.AttributeMatch;
import etomo.storage.DirectiveAttribute.Match;
import etomo.storage.autodoc.AutodocFactory;
import etomo.storage.autodoc.ReadOnlyAttribute;
import etomo.storage.autodoc.ReadOnlyAttributeIterator;
import etomo.storage.autodoc.ReadOnlyAttributeList;
import etomo.storage.autodoc.ReadOnlyAutodoc;
import etomo.type.AxisID;
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

  public static final int AUTO_FIT_RANGE_INDEX = 0;
  public static final int AUTO_FIT_STEP_INDEX = 1;

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

  private AttributeMatch getAttribute(final DirectiveDef directiveDef, final AxisID axisID) {
      AttributeMatch attributeMatch = getAttribute(Match.PRIMARY,
          directiveDef, axisID);
      if (attributeMatch != null) {
        if (attributeMatch.overrides()) {
          return null;
        }
        return attributeMatch;
      }
      attributeMatch = getAttribute(Match.SECONDARY, directiveDef,
        axisID);
      if (attributeMatch != null) {
        if (attributeMatch.overrides()) {
          return null;
        }
        return attributeMatch;
      }
  }

  /**
   * Returns true if the directive file contains the directive attribute and does not
   * override it (an empty value for a non-boolean directive).
   * @param directiveDef
   * @param axisID
   * @return
   */
  public boolean contains(final DirectiveDef directiveDef, final AxisID axisID) {
    DirectiveAttribute attribute = getAttribute(directiveDef, null);
    return attribute != null && !attribute.overrides() && !attribute.isEmpty();
  }

  /**
   * Returns true if the directive file contains the directive attribute and does not
   * override it (an empty value for a non-boolean directive).
   * @param directiveDef
   * @return
   */
  public boolean contains(final DirectiveDef directiveDef) {
    return contains(directiveDef, null);
  }

  /**
   * Return the value of the attribute in the directive file.
   * @param directiveDef
   * @param axisID
   * @return
   */
  public String getValue(final DirectiveDef directiveDef, final AxisID axisID) {
    DirectiveAttribute attribute = getAttribute(directiveDef, axisID);
    if (attribute != null && !attribute.isEmpty()) {
      if (attribute.overrides()) {
        // the directive was overridden - return null
        return null;
      }
      return attribute.getValue();
    }
    return null;
  }

  /**
   * Return the value of the attribute in the directive file.
   * @param directiveDef
   * @param axisID
   * @return
   */
  public String getValue(final DirectiveDef directiveDef) {
    return getValue(directiveDef, null);
  }

  /**
   * Return the boolean value of the attribute in the directive file.
   * @param directiveDef
   * @param axisID
   * @return
   */
  public boolean isValue(final DirectiveDef directiveDef, final AxisID axisID) {
    DirectiveAttribute attribute = getAttribute(directiveDef, axisID);
    if (attribute != null && !attribute.isEmpty()) {
      if (attribute.overrides()) {
        return false;
      }
      return attribute.isValue();
    }
    return false;
  }

  /**
   * Returns an attribute from this directive file that matches the parameters.
   * @param match
   * @param directiveDef
   * @param axisID
   * @return
   */
  AttributeMatch getAttribute(final Match match, final DirectiveDef directiveDef,
      final AxisID axisID) {
    if (directiveDef == null) {
      return null;
    }
    return DirectiveAttribute.getMatch(match, this, getParentAttribute(directiveDef),
        directiveDef, axisID);
  }

  private ReadOnlyAttribute getParentAttribute(final DirectiveDef directiveDef) {
    DirectiveType type = directiveDef.getDirectiveType();
    if (type == DirectiveType.COPY_ARG) {
      return copyArg;
    }
    if (type == DirectiveType.SETUP_SET) {
      return setupSet;
    }
    if (type == DirectiveType.RUN_TIME) {
      return runtime;
    }
    if (type == DirectiveType.COM_PARAM) {
      initComparam();
      return comparam;
    }
    return null;
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
