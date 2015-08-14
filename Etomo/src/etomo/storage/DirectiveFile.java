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
 * <p/>
 * <p>Copyright: Copyright 2012 - 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class DirectiveFile implements DirectiveFileInterface {
  public static final String rcsid = "$Id:$";

  public static final int AUTO_FIT_RANGE_INDEX = 0;
  public static final int AUTO_FIT_STEP_INDEX = 1;

  private final AxisID axisID;
  private final BaseManager manager;

  private File file = null;
  private boolean batchDirectiveFileType = false;
  private ReadOnlyAttribute copyArg = null;
  private ReadOnlyAttribute runtime = null;
  private ReadOnlyAttribute setupSet = null;
  private ReadOnlyAttribute comparam = null;
  private boolean copyArgSet = false;
  private boolean runtimeSet = false;
  private boolean setupSetSet = false;
  private boolean comparamSet = false;
  private ReadOnlyAutodoc autodoc = null;
  private boolean debug = false;

  private DirectiveFile(final BaseManager manager, final AxisID axisID) {
    this.manager = manager;
    this.axisID = axisID;
  }

  /**
   * Returns an instance loaded with the batch directive from the etomo parameters.
   * Returns null if an autodoc could not be loaded.
   *
   * @param manager
   * @param axisID
   * @return
   */
  public static DirectiveFile getArgInstance(final BaseManager manager,
    final AxisID axisID) {
    DirectiveFile instance = new DirectiveFile(manager, axisID);
    if (instance.setFile(EtomoDirector.INSTANCE.getArguments().getDirective(), true)) {
      return instance;
    }
    return null;
  }

  /**
   * Returns an instance loaded with the file parameter.  Returns null if an autodoc could
   * not be loaded.
   *
   * @param manager
   * @param axisID
   * @return
   */
  public static DirectiveFile getInstance(final BaseManager manager, final AxisID axisID,
    final File file, final boolean batch) {
    DirectiveFile instance = new DirectiveFile(manager, axisID);
    if (instance.setFile(file, batch)) {
      return instance;
    }
    return null;
  }

  /**
   * Returns an attribute from this directive file that matches the parameters.
   *
   * @param match
   * @param directiveDef
   * @param axisID
   * @return
   */
  AttributeMatch getAttribute(final Match match, final DirectiveDef directiveDef,
    final AxisID axisID) {
    if (directiveDef == null || (batchDirectiveFileType && !directiveDef.isBatch(axisID))
      || (!batchDirectiveFileType && !directiveDef.isTemplate(axisID))) {
      return null;
    }
    return DirectiveAttribute.getMatch(match, this, getParentAttribute(directiveDef),
      directiveDef, axisID);
  }

  /**
   * Returns the best matching AttributeMatch.
   *
   * @param directiveDef
   * @param axisID
   * @return
   */
  AttributeMatch getAttribute(final DirectiveDef directiveDef, final AxisID axisID) {
    if (directiveDef == null || (batchDirectiveFileType && !directiveDef.isBatch(axisID))
      || (!batchDirectiveFileType && !directiveDef.isTemplate(axisID))) {
      return null;
    }
    AttributeMatch attributeMatch = getAttribute(Match.PRIMARY, directiveDef, axisID);
    if (attributeMatch != null && !attributeMatch.isEmpty()) {
      if (attributeMatch.overrides()) {
        return null;
      }
      return attributeMatch;
    }
    attributeMatch = getAttribute(Match.SECONDARY, directiveDef, axisID);
    if (attributeMatch != null && !attributeMatch.isEmpty()) {
      if (attributeMatch.overrides()) {
        return null;
      }
      return attributeMatch;
    }
    return null;
  }

  /**
   * Returns true if the directive file contains the directive attribute and does not
   * override it (an empty value for a non-boolean directive).
   *
   * @param directiveDef
   * @param axisID
   * @return
   */
  public boolean contains(final DirectiveDef directiveDef, final AxisID axisID) {
    AttributeMatch attribute = getAttribute(directiveDef, axisID);
    return attribute != null;
  }

  public boolean containsValue(final DirectiveDef directiveDef) {
    AttributeMatch attribute = getAttribute(directiveDef, null);
    if (attribute != null) {
      String value = attribute.getValue();
      return value != null && !value.matches("\\s*");
    }
    return false;
  }

  /**
   * Returns true if the directive file contains the directive attribute and does not
   * override it (an empty value for a non-boolean directive).
   *
   * @param directiveDef
   * @return
   */
  public boolean contains(final DirectiveDef directiveDef) {
    return contains(directiveDef, null);
  }

  public boolean contains(final DirectiveDef directiveDef, final boolean templateOnly) {
    // template/batch issues are already being handled in getAttribute
    return contains(directiveDef, null);
  }

  /**
   * Return the value of the attribute in the directive file.
   *
   * @param directiveDef
   * @param axisID
   * @return
   */
  public String getValue(final DirectiveDef directiveDef, final AxisID axisID) {
    AttributeMatch attribute = getAttribute(directiveDef, axisID);
    if (attribute != null) {
      return attribute.getValue();
    }
    return null;
  }

  /**
   * Return the value of the attribute in the directive file.
   *
   * @param directiveDef
   * @return
   */
  public String getValue(final DirectiveDef directiveDef) {
    return getValue(directiveDef, null);
  }

  public String getValue(final DirectiveDef directiveDef, final boolean templateOnly) {
    // template/batch issues are already being handled in getAttribute
    return getValue(directiveDef, null);
  }

  public String getValue(final DirectiveDef directiveDef, final int index) {
    if (index < 0) {
      return null;
    }
    String value = getValue(directiveDef);
    // Get the element specified by index
    if (value != null) {
      String divider = ",";
      if (value.indexOf(divider) != -1) {
        String[] array = value.split(divider);
        if (array != null && index < array.length) {
          return array[index];
        }
      }
      if (index == 0) {
        return value;
      }
    }
    return null;
  }

  /**
   * Return the boolean value of the attribute in the directive file.
   *
   * @param directiveDef
   * @param axisID
   * @return
   */
  public boolean isValue(final DirectiveDef directiveDef, final AxisID axisID) {
    AttributeMatch attribute = getAttribute(directiveDef, axisID);
    if (attribute != null) {
      return attribute.isValue();
    }
    return false;
  }

  public boolean isValue(final DirectiveDef directiveDef) {
    return isValue(directiveDef, null);
  }

  public boolean isValue(final DirectiveDef directiveDef, final boolean templateOnly) {
    // template/batch issues are already being handled in getAttribute
    return isValue(directiveDef, null);
  }

  /**
   * Loads the file and opens the autodoc.  Resets the instance so that it will read from
   * the new autodoc.  Returns true if the autodoc was opened successfully, false if the
   * autodoc open failed.
   *
   * @return
   */
  public boolean setFile(final File directiveFile, final boolean batch) {
    file = directiveFile;
    this.batchDirectiveFileType = batch;
    copyArgSet = false;
    runtimeSet = false;
    setupSetSet = false;
    comparamSet = false;
    copyArg = null;
    runtime = null;
    setupSet = null;
    comparam = null;
    try {
      autodoc =
        (ReadOnlyAutodoc) AutodocFactory.getInstance(manager, file, axisID, false);
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

  private ReadOnlyAttribute getParentAttribute(final DirectiveDef directiveDef) {
    if (directiveDef == null) {
      return null;
    }
    return getParentAttribute(directiveDef.getDirectiveType());
  }

  private ReadOnlyAttribute getParentAttribute(final DirectiveType type) {
    ReadOnlyAttribute parentAttribute = null;
    if (type == DirectiveType.COPY_ARG) {
      if (!copyArgSet) {
        copyArgSet = true;
        setupSet = getParentAttribute(DirectiveType.SETUP_SET);
        if (setupSet != null && autodoc != null) {
          copyArg = setupSet.getAttribute(type.toString());
        }
      }
      parentAttribute = copyArg;
    }
    else if (type == DirectiveType.SETUP_SET) {
      if (!setupSetSet) {
        setupSetSet = true;
        if (autodoc != null) {
          setupSet = autodoc.getAttribute(type.toString());
        }
      }
      parentAttribute = setupSet;
    }
    else if (type == DirectiveType.RUN_TIME) {
      if (!runtimeSet) {
        runtimeSet = true;
        if (autodoc != null) {
          runtime = autodoc.getAttribute(type.toString());
        }
      }
      parentAttribute = runtime;
    }
    else if (type == DirectiveType.COM_PARAM) {
      if (!comparamSet) {
        comparamSet = true;
        if (autodoc != null) {
          comparam = autodoc.getAttribute(type.toString());
        }
      }
      parentAttribute = comparam;
    }
    if (comparamSet && copyArgSet && setupSetSet && runtimeSet) {
      autodoc = null;
    }
    return parentAttribute;
  }

  public void setDebug(final boolean input) {
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
    if (file != null) {
      return file.getAbsolutePath();
    }
    return super.toString();
  }

  static final class Module {
    public static final Module ALIGNED_STACK = new Module("AlignedStack");
    public static final Module COMBINE = new Module("Combine");
    public static final Module CTF_PLOTTING = new Module("CTFplotting");
    public static final Module FIDUCIALS = new Module("Fiducials");
    public static final Module GOLD_ERASING = new Module("GoldErasing");
    public static final Module PATCH_TRACKING = new Module("PatchTracking");
    public static final Module POSITIONING = new Module("Positioning");
    public static final Module PREPROCESSING = new Module("Preprocessing");
    public static final Module RAPTOR = new Module("RAPTOR");
    public static final Module RECONSTRUCTION = new Module("Reconstruction");
    public static final Module RESTRICT_ALIGN = new Module("RestrictAlign");
    public static final Module SEED_FINDING = new Module("SeedFinding");
    public static final Module TILT_ALIGNMENT = new Module("TiltAlignment");
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
    static final Command IMODCHOPCONTS = new Command("imodchopconts");
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
