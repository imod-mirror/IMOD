package etomo.storage;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import etomo.BaseManager;
import etomo.logic.DatasetTool;
import etomo.logic.UserEnv;
import etomo.storage.autodoc.ReadOnlyAttribute;
import etomo.storage.autodoc.ReadOnlyAttributeIterator;
import etomo.type.AxisID;
import etomo.type.DirectiveFileType;
import etomo.type.EtomoNumber;
import etomo.type.TiltAngleSpec;
import etomo.type.TiltAngleType;
import etomo.ui.FieldType;
import etomo.ui.SetupReconInterface;

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
public class DirectiveFileCollection implements SetupReconInterface {
  public static final String rcsid = "$Id:$";

  private final DirectiveFile[] directiveFileArray = new DirectiveFile[] { null, null,
      null, null };
  private Map<String, String> extraValues = null;

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
   * Checks the directive files in order from highest to lowest priority.  Returns true if
   * the highest priority directive file that contains the directive attribute does not
   * override it.  If none of directive files contain the directive attribute, returns
   * true if extraValues contains it.
   * @param directiveDef
   * @param axisID
   * @return
   */
  public boolean contains(final DirectiveDef directiveDef) {
    for (int i = directiveFileArray.length - 1; i >= 0; i--) {
      DirectiveFile directiveFile = directiveFileArray[i];
      if (directiveFile == null) {
        continue;
      }
      DirectiveAttribute attribute = directiveFile.getAttribute(directiveDef, null);
      return attribute != null && !attribute.isEmpty() && !attribute.overrides();
    }
    return extraValues.containsKey(directiveDef.getKey(null, null));
  }

  /**
   * Return the specified element of the value of the attribute in the highest priority
   * directive file.
   * @param directiveDef
   * @param index - index of element in value to return
   * @return
   */
  public String getValue(final DirectiveDef directiveDef, final int index) {
    if (index < 0) {
      return null;
    }
    String value = null;
    boolean found = false;
    for (int i = directiveFileArray.length - 1; i >= 0; i--) {
      DirectiveFile directiveFile = directiveFileArray[i];
      if (directiveFile == null) {
        continue;
      }
      DirectiveAttribute attribute = directiveFile.getAttribute(directiveDef, null);
      if (attribute != null && !attribute.isEmpty()) {
        found = true;
        if (attribute.overrides()) {
          // highest priority directive file has overridden the directive - return null
          return null;
        }
        value = attribute.getValue();
        //The highest priority directive file takes presidence, so stop looking when the
        //attribute is first found
        break;
      }
    }
    if (!found) {
      //The attribute was never found
      value = extraValues.get(directiveDef.getKey(null, null));
    }
    //Get the element specified by index
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

  public boolean getTiltAngleFields(final AxisID axisID,
      final TiltAngleSpec tiltAngleSpec, final boolean doValidation) {
    tiltAngleSpec.reset();
    for (int i = 0; i < directiveFileArray.length; i++) {
      if (directiveFileArray[i] != null && containsTiltAngleSpec(axisID)) {
        directiveFileArray[i].getTiltAngleFields(axisID, tiltAngleSpec, doValidation);
      }
    }
    return true;
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

  /**
   * If the batch directive file is set and doesn't contain the directiveDef directive,
   * puts the DirectiveDef/value pair into extraValues.
   * @param directiveDef
   * @param value
   * @param axisID
   */
  private void setValue(final DirectiveDef directiveDef, final AxisID axisID,
      final String value) {
    DirectiveFile batch = directiveFileArray[DirectiveFileType.BATCH.getIndex()];
    if (batch != null) {
      if (!batch.contains(directiveDef, axisID)) {
        if (extraValues == null) {
          extraValues = new HashMap<String, String>();
        }
        String key = directiveDef.getKey(axisID, null);
        if (extraValues.containsKey(key)) {
          extraValues.remove(key);
        }
        extraValues.put(key, value);
      }
    }
  }

  private void setValue(final DirectiveDef directiveDef, final int value) {
    setValue(directiveDef, null, String.valueOf(value));
  }

  private boolean containsExtraValue(final DirectiveDef directiveDef, final AxisID axisID) {
    return extraValues != null
        && extraValues.containsKey(directiveDef.getKey(axisID, null));
  }

  private boolean containsExtraValue(final DirectiveDef directiveDef) {
    return containsExtraValue(directiveDef, null);
  }

  /**
   * Returns null if the directiveDef is not in extraValues.  Returns "" if directiveDef
   * is there, but its value is null.  Otherwise returns the value.
   * @param directiveDef
   * @return
   */
  private String getExtraValue(final DirectiveDef directiveDef, final AxisID axisID) {
    String key = directiveDef.getKey(axisID, null);
    if (extraValues != null && extraValues.containsKey(key)) {
      String value = extraValues.get(key);
      if (value == null) {
        return "";
      }
      return value;
    }
    return null;
  }

  private String getExtraValue(final DirectiveDef directiveDef) {
    return getExtraValue(directiveDef, null);
  }

  Iterator<Entry<String, String>> getCopyArgExtraValuesIterator() {
    return extraValues.entrySet().iterator();
  }

  public void setBinning(final int input) {
    setValue(DirectiveDef.BINNING, null, String.valueOf(input));
  }

  public void setImageRotation(final String input) {
    setValue(DirectiveDef.ROTATION, AxisID.FIRST, input);
    if (isValue(DirectiveDef.DUAL)) {
      setValue(DirectiveDef.ROTATION, AxisID.SECOND, input);
    }
  }

  public void setPixelSize(final double input) {
    setValue(DirectiveDef.PIXEL, null, String.valueOf(input));
  }

  public void setTwodir(final AxisID axisID, final double input) {
    setValue(DirectiveDef.TWODIR, axisID, String.valueOf(input));
  }

  /**
   * Returns the value of the leaf attribute defined by directiveDef.  Returns null
   * if the attribute is missing.  Returns an empty string if this attribute is there and
   * it has no value.  If the attribute is in one of the directive files, returns the
   * value from the highest priority file.  If the attribute is not in any of the
   * directive files, function checks extraValues and returns the value from there if it
   * contains the key defined by directiveDef.  Otherwise returns null.
   * @param directiveDef
   * @param axisID
   * @return
   */
  private String getValue(final DirectiveDef directiveDef, final AxisID axisID) {
    for (int i = directiveFileArray.length - 1; i >= 0; i--) {
      if (directiveFileArray[i] != null
          && directiveFileArray[i].contains(directiveDef, axisID)) {
        return directiveFileArray[i].getValue(directiveDef, axisID);
      }
    }
    String key = directiveDef.getKey(axisID, null);
    if (extraValues.containsKey(key)) {
      String value = extraValues.get(key);
      if (value == null) {
        return "";
      }
    }
    return null;
  }

  private String getValue(final DirectiveDef directiveDef) {
    return getValue(directiveDef, null);
  }

  /**
   * Returns true unless return value of getValue is null or 0.
   * @param value
   * @return
   */
  private boolean isValue(final DirectiveDef directiveDef, final AxisID axisID) {
    return DirectiveFile.toBoolean(getValue(directiveDef, axisID));
  }

  private boolean isValue(final DirectiveDef directiveDef) {
    return DirectiveFile.toBoolean(getValue(directiveDef, null));
  }

  /**
   * Returns true if getValue does not return null.  If getValue does return null then
   * either the attribute wasn't there, or it was there and was overridden (the last time
   * it appeared in the collection there was no value).
   * @param parentName
   * @param name
   * @return
   */
  private boolean isValueSet(final DirectiveFile.AttributeName parentName,
      final String name) {
    return getValue(parentName, name) != null;
  }

  public boolean containsBinning() {
    return containsAttribute(AttributeName.COPY_ARG, DirectiveFile.BINNING_NAME);
  }

  public boolean containsDatasetDirectory() {
    return containsAttribute(DirectiveFile.AttributeName.SETUP_SET,
        DirectiveFile.DATASET_DIRECTORY_NAME);
  }

  public boolean containsDistort() {
    return containsAttribute(AttributeName.COPY_ARG, DirectiveFile.DISTORT_NAME);
  }

  public boolean containsFocus(final AxisID axisID) {
    return containsAttribute(AttributeName.COPY_ARG,
        DirectiveFile.convertAttributeName(axisID, DirectiveFile.FOCUS_NAME));
  }

  public boolean containsGold() {
    return containsAttribute(DirectiveFile.AttributeName.COPY_ARG,
        DirectiveFile.GOLD_NAME);
  }

  public boolean containsGradient() {
    return containsAttribute(AttributeName.COPY_ARG, DirectiveFile.GRADIENT_NAME);
  }

  public boolean containsPixel() {
    return containsAttribute(DirectiveFile.AttributeName.COPY_ARG,
        DirectiveFile.PIXEL_NAME);
  }

  public boolean containsTwodir(final AxisID axisID) {
    return containsAttribute(DirectiveFile.AttributeName.COPY_ARG,
        DirectiveFile.convertAttributeName(axisID, DirectiveFile.TWODIR_NAME));
  }

  public boolean containsRotation() {
    return containsAttribute(AttributeName.COPY_ARG, DirectiveFile.ROTATION_NAME);
  }

  public boolean containsTiltAngleSpec(final AxisID axisID) {
    return containsAttribute(AttributeName.COPY_ARG,
        DirectiveFile.convertAttributeName(axisID, DirectiveFile.FIRST_INC_NAME))
        || containsAttribute(AttributeName.COPY_ARG,
            DirectiveFile.convertAttributeName(axisID, DirectiveFile.USE_RAW_TLT_NAME))
        || containsAttribute(AttributeName.COPY_ARG,
            DirectiveFile.convertAttributeName(axisID, DirectiveFile.EXTRACT_NAME));
  }

  public String getBackupDirectory() {
    return null;
  }

  public String getBinning() {
    return getValue(DirectiveFile.AttributeName.COPY_ARG, DirectiveFile.BINNING_NAME);
  }

  /**
   * Returns an entry set containing the names/value pairs in all of the directive files
   * (one entry per name).  This function will not return null.  A name/value pair with a
   * blank values cause the name/value pair to be removed from the entry set.  The
   * name/value pair will be re-added afterwards if a pair with a non-blank value is
   * encountered.
   * @return
   */
  public CopyArgEntrySet getCopyArgEntrySet() {
    return CopyArgEntrySet.getInstance(directiveFileArray);
  }

  public String getDataset() {
    return getName();
  }

  public String getDatasetDirectory() {
    return getValue(DirectiveFile.AttributeName.SETUP_SET,
        DirectiveFile.DATASET_DIRECTORY_NAME);
  }

  public DirectiveFile getDirectiveFile(final DirectiveFileType type) {
    return directiveFileArray[type.getIndex()];
  }

  public DirectiveFileCollection getDirectiveFileCollection() {
    return this;
  }

  public String getDistortionFile() {
    return getValue(DirectiveFile.AttributeName.COPY_ARG, DirectiveFile.DISTORT_NAME);
  }

  /**
   * @param doValidation has no effect
   */
  public String getExcludeList(final AxisID axisID, final boolean doValidation) {
    return getValue(DirectiveFile.AttributeName.COPY_ARG,
        DirectiveFile.convertAttributeName(axisID, DirectiveFile.SKIP_NAME));
  }

  /**
   * @param doValidation has no effect
   */
  public String getTwodir(final AxisID axisID, final boolean doValidation) {
    return getValue(DirectiveFile.AttributeName.COPY_ARG,
        DirectiveFile.convertAttributeName(axisID, DirectiveFile.TWODIR_NAME));
  }

  public boolean isTwodir(final AxisID axisIDn) {
    return isValueSet(DirectiveFile.AttributeName.COPY_ARG,
        DirectiveFile.convertAttributeName(axisID, DirectiveFile.TWODIR_NAME));
  }

  /**
   * @param doValidation has no effect
   */
  public String getFiducialDiameter(final boolean doValidation) {
    return getValue(DirectiveFile.AttributeName.COPY_ARG, DirectiveFile.GOLD_NAME);
  }

  /**
   * @param doValidation has no effect
   */
  public String getImageRotation(final AxisID axisID, final boolean doValidation) {
    return getValue(DirectiveFile.AttributeName.COPY_ARG,
        DirectiveFile.convertAttributeName(axisID, DirectiveFile.ROTATION_NAME));
  }

  /**
   * Returns binning or defaultRetValue if binning is invalid or missing.
   * @param defaultRetValue
   * @return
   */
  public int getIntBinning(final int defaultRetValue) {
    EtomoNumber binning = new EtomoNumber();
    binning.set(getBinning());
    if (binning.isValid() && !binning.isNull()) {
      return binning.getInt();
    }
    return defaultRetValue;
  }

  public String getMagGradientFile() {
    return getValue(DirectiveFile.AttributeName.COPY_ARG, DirectiveFile.GRADIENT_NAME);
  }

  public String getName() {
    return getValue(DirectiveFile.AttributeName.COPY_ARG, DirectiveFile.NAME_NAME);
  }

  public String getPixelSize(final boolean doValidation) {
    return getValue(DirectiveFile.AttributeName.COPY_ARG, DirectiveFile.PIXEL_NAME);
  }

  public boolean isAdjustedFocusSelected(final AxisID axisID) {
    return DirectiveFile.toBoolean(getValue(DirectiveFile.AttributeName.COPY_ARG,
        DirectiveFile.convertAttributeName(axisID, DirectiveFile.FOCUS_NAME)));
  }

  public boolean isDual() {
    return DirectiveFile.toBoolean(getValue(DirectiveFile.AttributeName.COPY_ARG,
        DirectiveFile.DUAL_NAME));
  }

  public boolean containsDual() {
    return containsAttribute(DirectiveFile.AttributeName.COPY_ARG,
        DirectiveFile.DUAL_NAME);
  }

  public boolean isDualAxisSelected() {
    return isDual();
  }

  public boolean isMontage() {
    return DirectiveFile.toBoolean(getValue(DirectiveFile.AttributeName.COPY_ARG,
        DirectiveFile.MONTAGE_NAME));
  }

  public boolean containsMontage() {
    return containsAttribute(DirectiveFile.AttributeName.COPY_ARG,
        DirectiveFile.MONTAGE_NAME);
  }

  public boolean isGpuProcessingSelected(final String propertyUserDir) {
    return UserEnv.isGpuProcessing(manager, axisID, propertyUserDir);
  }

  public boolean isParallelProcessSelected(final String propertyUserDir) {
    return UserEnv.isParallelProcessing(manager, axisID, propertyUserDir);
  }

  public boolean isScanHeader() {
    return DirectiveFile.toBoolean(getValue(DirectiveFile.AttributeName.SETUP_SET,
        DirectiveFile.SCAN_HEADER_NAME));
  }

  public boolean isSingleAxisSelected() {
    return !isDual();
  }

  public boolean isSingleViewSelected() {
    return !DirectiveFile.toBoolean(getValue(DirectiveFile.AttributeName.COPY_ARG,
        DirectiveFile.MONTAGE_NAME));
  }

  public void setBatchDirectiveFile(final DirectiveFile baseDirectiveFile) {
    directiveFileArray[DirectiveFileType.BATCH.getIndex()] = baseDirectiveFile;
    if (baseDirectiveFile != null) {
      setDirectiveFile(baseDirectiveFile.getScopeTemplate(), DirectiveFileType.SCOPE);
      setDirectiveFile(baseDirectiveFile.getSystemTemplate(), DirectiveFileType.SYSTEM);
      setDirectiveFile(baseDirectiveFile.getUserTemplate(), DirectiveFileType.USER);
    }
  }

  public void setDirectiveFile(final String absPath, final DirectiveFileType type) {
    if (absPath == null) {
      directiveFileArray[type.getIndex()] = null;
    }
    else {
      directiveFileArray[type.getIndex()] = DirectiveFile.getInstance(manager, axisID,
          new File(absPath));
    }
  }

  public void setDirectiveFile(final File file, final DirectiveFileType type) {
    if (file == null) {
      directiveFileArray[type.getIndex()] = null;
    }
    else {
      directiveFileArray[type.getIndex()] = DirectiveFile.getInstance(manager, axisID,
          file);
    }
  }

  public boolean validateTiltAngle(final AxisID axisID, final String errorTitle) {
    TiltAngleSpec tiltAngleSpec = new TiltAngleSpec();
    getTiltAngleFields(axisID, tiltAngleSpec, false);
    return DatasetTool.validateTiltAngle(manager, AxisID.ONLY, errorTitle, axisID,
        tiltAngleSpec.getType() == TiltAngleType.RANGE,
        String.valueOf(tiltAngleSpec.getRangeMin()),
        String.valueOf(tiltAngleSpec.getRangeStep()));
  }

  public static final class CopyArgEntrySet {
    private final Map<String, String> pairMap = new HashMap<String, String>();

    /**
     * Don't call constructor directly.
     */
    private CopyArgEntrySet() {
    }

    /**
     * This function should not return null.
     * @return initialized instance
     */
    private static CopyArgEntrySet getInstance(final DirectiveFile[] directiveFileArray) {
      CopyArgEntrySet instance = new CopyArgEntrySet();
      instance.init(directiveFileArray);
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
      // If scan header (only found in the batch directive file and is not in copyarg) is
      // true, then get values from scanning the header which aren't already in the map.
      if (directiveFileArray[DirectiveFileType.BATCH.getIndex()] != null
          && directiveFileArray[DirectiveFileType.BATCH.getIndex()].isScanHeader()) {
        Iterator<Entry<String, String>> iterator = directiveFileArray[DirectiveFileType.BATCH
            .getIndex()].getCopyArgExtraValuesIterator();
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
