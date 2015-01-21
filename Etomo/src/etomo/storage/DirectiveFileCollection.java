package etomo.storage;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import etomo.BaseManager;
import etomo.EtomoDirector;
import etomo.logic.DatasetTool;
import etomo.logic.UserEnv;
import etomo.storage.DirectiveAttribute.AttributeMatch;
import etomo.storage.DirectiveAttribute.Match;
import etomo.storage.autodoc.ReadOnlyAttribute;
import etomo.storage.autodoc.ReadOnlyAttributeIterator;
import etomo.type.AxisID;
import etomo.type.DirectiveFileType;
import etomo.type.TiltAngleSpec;
import etomo.type.TiltAngleType;
import etomo.type.UserConfiguration;
import etomo.ui.FieldType;
import etomo.ui.SetupReconInterface;

/**
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright 2012 - 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public class DirectiveFileCollection implements SetupReconInterface,
  DirectiveFileInterface {
  private static final String TRUE = "1";
  private final DirectiveFile[] directiveFileArray = new DirectiveFile[] { null, null,
    null, null };
  private Map<String, String> copyArgExtraValues = null;

  private final BaseManager manager;
  private final AxisID axisID;

  private boolean debug = false;

  public DirectiveFileCollection(final BaseManager manager, final AxisID axisID) {
    this.manager = manager;
    this.axisID = axisID;
  }

  public void setDebug(final boolean input) {
    debug = input;
  }

  /**
   * Returns a non-empty attributeMatch or null.  Directive files are checked in order of
   * priority (batch to scope) and the highest priority file containing the directive, is
   * used.  An overridden directive (no value and non-boolean) causes a null to be
   * returned.  Two different matches are used - first the primary match, and then the
   * secondary match.  The difference between them is how closely they match the axisID.
   * The primary match has priority over the secondary match, even when the primary match
   * is found in a lower priority file.
   *
   * @param directiveDef
   * @param axisID
   * @param templatesOnly - causes function to ignore the batch directive file
   * @return
   */
  private AttributeMatch getAttribute(final DirectiveDef directiveDef,
    final AxisID axisID, final boolean templatesOnly) {
    int start = directiveFileArray.length - 1;
    int end = 0;
    boolean template = directiveDef.isTemplate(axisID);
    boolean batch = directiveDef.isBatch(axisID);
    if (templatesOnly && !template) {
      // templateOnly set, and its batch only - nothing to do
      return null;
    }
    if (templatesOnly || (!batch && template)) {
      start--;// skip batch directive file
    }
    if (batch && !template) {
      end = start;// only look at batch directive file
    }
    AttributeMatch secondaryMatch = null;
    // Search for a primary match starting with the highest priority file, or the highest
    // priority template if templateOnly is true.
    for (int i = start; i >= end; i--) {
      DirectiveFile directiveFile = directiveFileArray[i];
      if (directiveFile == null) {
        continue;
      }
      AttributeMatch primaryMatch =
        directiveFile.getAttribute(Match.PRIMARY, directiveDef, axisID);
      if (primaryMatch != null) {
        if (primaryMatch.overrides()) {
          return null;
        }
        // Missing primary match - if the secondary match hasn't been set from a higher
        // priority file, try to set it.
        if (primaryMatch.isEmpty()
          && (secondaryMatch == null || secondaryMatch.isEmpty())) {
          secondaryMatch =
            directiveFile.getAttribute(Match.SECONDARY, directiveDef, axisID);
          continue;
        }
        return primaryMatch;
      }
    }
    // A primary match was not found. See if a secondary match was set.
    if (secondaryMatch != null) {
      if (secondaryMatch.overrides()) {
        return null;
      }
      if (!secondaryMatch.isEmpty()) {
        return secondaryMatch;
      }
    }
    return null;
  }

  /**
   * Returns true if a non-empty, non-overriding directive, or an extraValue is found.
   *
   * @param directiveDef
   * @param axisID
   * @param templatesOnly - if true, batch directive file and copyArgExtraValues are ignored
   * @return
   */
  private boolean contains(final DirectiveDef directiveDef, final AxisID axisID,
    final boolean templatesOnly) {
    AttributeMatch attributeMatch = getAttribute(directiveDef, axisID, templatesOnly);
    if (attributeMatch != null) {
      return true;
    }
    if (templatesOnly) {
      return false;
    }
    // An attribute has not been found - look for an extra value - these are associated
    // with the directive file.
    if (directiveDef.getDirectiveType() == DirectiveType.COPY_ARG
      && copyArgExtraValues != null) {
      return copyArgExtraValues.containsKey(directiveDef.getName(axisID));
    }
    return false;
  }

  public boolean contains(final DirectiveDef directiveDef, final AxisID axisID) {
    return contains(directiveDef, axisID, false);
  }

  /**
   * Returns true a non-empty, non-overriding directive, or an extraValue is found.
   *
   * @param directiveDef
   * @return
   */
  public boolean contains(final DirectiveDef directiveDef) {
    return contains(directiveDef, null, false);
  }

  public boolean contains(final DirectiveDef directiveDef, final boolean templatesOnly) {
    return contains(directiveDef, null, templatesOnly);
  }

  /**
   * Returns the directive value if a non-empty, non-overriding directive,
   * or an extraValue
   * is found.
   *
   * @param directiveDef
   * @param axisID
   * @return
   */
  private String getValue(final DirectiveDef directiveDef, final AxisID axisID,
    final boolean templatesOnly) {
    AttributeMatch attributeMatch = getAttribute(directiveDef, axisID, templatesOnly);
    if (attributeMatch != null) {
      return attributeMatch.getValue();
    }
    if (templatesOnly) {
      return null;
    }
    // An attribute has not been found - look for an extra value.
    if (directiveDef.getDirectiveType() == DirectiveType.COPY_ARG
      && copyArgExtraValues != null) {
      return copyArgExtraValues.get(directiveDef.getName(axisID));
    }
    return null;
  }

  public String getValue(final DirectiveDef directiveDef, final AxisID axisID) {
    return getValue(directiveDef, axisID, false);
  }

  /**
   * Returns the directive value if a non-empty, non-overriding directive,
   * or an extraValue
   * is found.
   *
   * @param directiveDef
   * @return
   */
  public String getValue(final DirectiveDef directiveDef) {
    return getValue(directiveDef, null, false);
  }

  public String getValue(final DirectiveDef directiveDef, final boolean templateOnly) {
    return getValue(directiveDef, null, templateOnly);
  }

  /**
   * Return the specified element of the directive value if a non-empty, non-overriding
   * directive, or an extraValue is found.
   *
   * @param directiveDef
   * @param index        - index of element in value to return
   * @return
   */
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
      return value;
    }
    return null;
  }

  /**
   * Returns the boolean version of a directive value if a non-empty, non-overriding
   * directive, or an extraValue is found.
   *
   * @param directiveDef
   * @param axisID
   * @param templateOnly - causes function to ignore the batch directive file and
   *                     copyrgExtraValues
   * @return
   */
  private boolean isValue(final DirectiveDef directiveDef, final AxisID axisID,
    final boolean templateOnly) {
    AttributeMatch attributeMatch = getAttribute(directiveDef, axisID, templateOnly);
    if (attributeMatch != null) {
      return attributeMatch.isValue();
    }
    if (templateOnly) {
      return false;
    }
    // An attribute has not been found - look for an extra value.
    if (directiveDef.getDirectiveType() == DirectiveType.COPY_ARG
      && copyArgExtraValues != null) {
      return DirectiveAttribute.toBoolean(copyArgExtraValues.get(directiveDef
        .getName(axisID)));
    }
    return false;
  }

  public boolean isValue(final DirectiveDef directiveDef, final AxisID axisID) {
    return isValue(directiveDef, axisID, false);
  }

  /**
   * Returns the boolean version of a directive value if a non-empty, non-overriding
   * directive, or an extraValue is found.
   *
   * @param directiveDef
   * @return
   */
  public boolean isValue(final DirectiveDef directiveDef) {
    return isValue(directiveDef, null, false);
  }

  public boolean isValue(final DirectiveDef directiveDef, final boolean templateOnly) {
    return isValue(directiveDef, null, templateOnly);
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
    else {
      // Must set something here, so use the settings values
      UserConfiguration userConfiguration = EtomoDirector.INSTANCE.getUserConfiguration();
      if (userConfiguration.isTiltAnglesRawtltFile()) {
        tiltAngleSpec.setType(TiltAngleType.FILE);
      }
      else {
        // Default
        tiltAngleSpec.setType(TiltAngleType.EXTRACT);
      }
    }
    return true;
  }

  /**
   * Puts the DirectiveDef/value pair into extraValues.
   *
   * @param directiveDef
   * @param axisID
   * @param value
   */
  private void setCopyArgValue(final DirectiveDef directiveDef, final AxisID axisID,
    final String value) {
    if (directiveDef != null) {
      if (copyArgExtraValues == null) {
        copyArgExtraValues = new HashMap<String, String>();
      }
      copyArgExtraValues.put(directiveDef.getName(axisID), value);
    }
  }

  public void setBinning(final int input) {
    setCopyArgValue(DirectiveDef.BINNING, null, String.valueOf(input));
  }

  public void setImageRotation(final String input) {
    setCopyArgValue(DirectiveDef.ROTATION, AxisID.FIRST, input);
    if (isValue(DirectiveDef.DUAL)) {
      setCopyArgValue(DirectiveDef.ROTATION, AxisID.SECOND, input);
    }
  }

  public void setPixelSize(final double input) {
    setCopyArgValue(DirectiveDef.PIXEL, null, String.valueOf(input));
  }

  public void setTwodir(final AxisID axisID, final double input) {
    setCopyArgValue(DirectiveDef.TWODIR, axisID, String.valueOf(input));
  }

  public void initTiltAngleFields(final AxisID axisID, final TiltAngleSpec tiltAngleSpec,
    final UserConfiguration userConfiguration) {
    if (!copyArgExtraValues.containsKey(DirectiveDef.USE_RAW_TLT.getName(axisID))
      && tiltAngleSpec.getType() == TiltAngleType.FILE
      || userConfiguration.isTiltAnglesRawtltFile()) {
      setCopyArgValue(DirectiveDef.USE_RAW_TLT, axisID, TRUE);
    }
    else if (!copyArgExtraValues.containsKey(DirectiveDef.EXTRACT.getName(axisID))
      && tiltAngleSpec.getType() == TiltAngleType.EXTRACT) {
      setCopyArgValue(DirectiveDef.EXTRACT, axisID, TRUE);
    }
    else if (!copyArgExtraValues.containsKey(DirectiveDef.FIRST_INC.getName(axisID))
      && tiltAngleSpec.getType() == TiltAngleType.RANGE) {
      setCopyArgValue(DirectiveDef.FIRST_INC, axisID, String.valueOf(tiltAngleSpec
        .getRangeMin())
        + ", " + String.valueOf(tiltAngleSpec.getRangeStep()));
    }
  }

  public boolean containsTiltAngleSpec(final AxisID axisID) {
    return contains(DirectiveDef.FIRST_INC, axisID)
      || contains(DirectiveDef.USE_RAW_TLT, axisID)
      || contains(DirectiveDef.EXTRACT, axisID);
  }

  public String getBackupDirectory() {
    return null;
  }

  public String getBinning() {
    return getValue(DirectiveDef.BINNING);
  }

  public String getDataset() {
    return getValue(DirectiveDef.NAME);
  }

  public DirectiveFile getDirectiveFile(final DirectiveFileType type) {
    return directiveFileArray[type.getIndex()];
  }

  public DirectiveFileCollection getDirectiveFileCollection() {
    return this;
  }

  public String getDistortionFile() {
    return getValue(DirectiveDef.DISTORT);
  }

  /**
   * @param doValidation has no effect
   */
  public String getExcludeList(final AxisID axisID, final boolean doValidation) {
    return getValue(DirectiveDef.SKIP, axisID);
  }

  /**
   * @param doValidation has no effect
   */
  public String getTwodir(final AxisID axisID, final boolean doValidation) {
    return getValue(DirectiveDef.TWODIR, axisID);
  }

  public boolean isTwodir(final AxisID axisID) {
    // Contains returns true if a directive contains this attribute, and it is set to a
    // value. Since twodir is not a boolean, no value would mean that it is overridden -
    // which causes contains() to return false.
    return contains(DirectiveDef.TWODIR, axisID);
  }

  /**
   * @param doValidation has no effect
   */
  public String getFiducialDiameter(final boolean doValidation) {
    return getValue(DirectiveDef.GOLD);
  }

  /**
   * @param doValidation has no effect
   */
  public String getImageRotation(final AxisID axisID, final boolean doValidation) {
    return getValue(DirectiveDef.ROTATION, axisID);
  }

  public String getMagGradientFile() {
    return getValue(DirectiveDef.GRADIENT);
  }

  public String getPixelSize(final boolean doValidation) {
    return getValue(DirectiveDef.PIXEL);
  }

  public boolean isAdjustedFocusSelected(final AxisID axisID) {
    return isValue(DirectiveDef.FOCUS, axisID);
  }

  public boolean isDualAxisSelected() {
    return isValue(DirectiveDef.DUAL);
  }

  public boolean isGpuProcessingSelected(final String propertyUserDir) {
    return UserEnv.isGpuProcessing(manager, axisID, propertyUserDir);
  }

  public boolean isParallelProcessSelected(final String propertyUserDir) {
    return UserEnv.isParallelProcessing(manager, axisID, propertyUserDir);
  }

  public boolean isSingleAxisSelected() {
    return !isValue(DirectiveDef.DUAL);
  }

  public boolean isSingleViewSelected() {
    return !isValue(DirectiveDef.MONTAGE);
  }

  /**
   * Sets the batch directive file, and then sets the template files from the the batch
   * directive file.
   *
   * @param batchDirectiveFile
   */
  public void setup(final DirectiveFile batchDirectiveFile) {
    directiveFileArray[DirectiveFileType.BATCH.getIndex()] = batchDirectiveFile;
    if (batchDirectiveFile != null) {
      setDirectiveFile(
        batchDirectiveFile.getAttribute(DirectiveDef.SCOPE_TEMPLATE, null),
        DirectiveFileType.SCOPE);
      setDirectiveFile(batchDirectiveFile
        .getAttribute(DirectiveDef.SYSTEM_TEMPLATE, null), DirectiveFileType.SYSTEM);
      setDirectiveFile(batchDirectiveFile.getAttribute(DirectiveDef.USER_TEMPLATE, null),
        DirectiveFileType.USER);
    }
  }

  /**
   * Sets a directive file.  No directive file other then the one being set is changed by
   * this function.
   *
   * @param attribute
   * @param type
   */
  private void setDirectiveFile(final AttributeMatch attribute,
    final DirectiveFileType type) {
    if (attribute == null) {
      directiveFileArray[type.getIndex()] = null;
    }
    else {
      String absPath = attribute.getValue();
      if (absPath == null) {
        directiveFileArray[type.getIndex()] = null;
      }
      else {
        directiveFileArray[type.getIndex()] =
          DirectiveFile.getInstance(manager, axisID, new File(absPath),
            type == DirectiveFileType.BATCH);
      }
    }
  }

  /**
   * Sets a directive file.  No directive file other then the one being set is changed by
   * this function.
   *
   * @param file
   * @param type
   */
  public void setDirectiveFile(final File file, final DirectiveFileType type) {
    if (file == null) {
      directiveFileArray[type.getIndex()] = null;
    }
    else {
      directiveFileArray[type.getIndex()] =
        DirectiveFile.getInstance(manager, axisID, file, type == DirectiveFileType.BATCH);
    }
  }

  public boolean validateTiltAngle(final AxisID axisID, final String errorTitle) {
    TiltAngleSpec tiltAngleSpec = new TiltAngleSpec();
    getTiltAngleFields(axisID, tiltAngleSpec, false);
    return DatasetTool.validateTiltAngle(manager, AxisID.ONLY, errorTitle, axisID,
      tiltAngleSpec.getType() == TiltAngleType.RANGE, String.valueOf(tiltAngleSpec
        .getRangeMin()), String.valueOf(tiltAngleSpec.getRangeStep()));
  }

  /**
   * Returns an entry set containing the names/value pairs in all of the directive files
   * (one entry per name).  This function will not return null.  A name/value pair with a
   * blank values cause the name/value pair to be removed from the entry set.  The
   * name/value pair will be re-added afterwards if a pair with a non-blank value is
   * encountered.
   *
   * @return
   */
  public CopyArgEntrySet getCopyArgEntrySet() {
    return CopyArgEntrySet.getInstance(this);
  }

  public static final class CopyArgEntrySet {
    private final Map<String, String> pairMap = new HashMap<String, String>();

    private final DirectiveFileCollection directiveFileCollection;

    /**
     * Don't call constructor directly.
     */
    private CopyArgEntrySet(final DirectiveFileCollection directiveFileCollection) {
      this.directiveFileCollection = directiveFileCollection;
    }

    /**
     * This function should not return null.
     *
     * @return initialized instance
     */
    private static CopyArgEntrySet getInstance(
      final DirectiveFileCollection directiveFileCollection) {
      CopyArgEntrySet instance = new CopyArgEntrySet(directiveFileCollection);
      instance.init(directiveFileCollection.directiveFileArray);
      return instance;
    }

    /**
     * Load all of the directive file copyarg values into pairMap.  Pairs with the same
     * name as a previously saved pair overrides the previous pair.  A pair with a blank
     * value is not saved and causes the pair with the same name in the map to be removed.
     * After loading all of copyarg values, load the scan header output from the directive
     * file if scan header is in the map and is set to "1".  Only load pairs with names
     * that are not already in the map, because the directive files all override scan
     * header.
     *
     * @param directiveFileArray
     */
    private void init(final DirectiveFile[] directiveFileArray) {
      for (int i = 0; i < directiveFileArray.length; i++) {
        if (directiveFileArray[i] != null) {
          ReadOnlyAttributeIterator iterator = directiveFileArray[i].getCopyArgIterator();
          if (iterator == null) {
            continue;
          }
          while (iterator.hasNext()) {
            ReadOnlyAttribute attribute = iterator.next();
            String name = attribute.getName();
            String value = attribute.getValue();
            pairMap.remove(name);
            // A blank value means remove the previously added pair, otherwise override
            // the previous added pair with the new value.
            if (value != null) {
              pairMap.put(name, value);
            }
          }
        }
      }
      // Get values from the .etomo file and scanning the header which aren't already in
      // the map.
      if (directiveFileCollection.copyArgExtraValues != null) {
        Iterator<Entry<String, String>> iterator =
          directiveFileCollection.copyArgExtraValues.entrySet().iterator();
        while (iterator.hasNext()) {
          Entry<String, String> entry = iterator.next();
          String name = entry.getKey();
          if (!pairMap.containsKey(name)) {
            pairMap.put(name, entry.getValue());
          }
        }
      }
    }

    public Iterator<Entry<String, String>> iterator() {
      return pairMap.entrySet().iterator();
    }
  }
}
