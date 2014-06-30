package etomo.storage;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import etomo.BaseManager;
import etomo.logic.DatasetTool;
import etomo.logic.UserEnv;
import etomo.storage.DirectiveAttribute.Match;
import etomo.storage.autodoc.ReadOnlyAttribute;
import etomo.storage.autodoc.ReadOnlyAttributeIterator;
import etomo.type.AxisID;
import etomo.type.DirectiveFileType;
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
   * Checks the directive files in order from highest to lowest priority.  Returns true if
   * the highest priority directive file that contains the directive attribute does not
   * override it (an empty value for a non-boolean directive).  If none of directive files
   * contain the directive attribute, returns true if copyArgExtraValues contains it.
   * @param directiveDef
   * @param axisID
   * @return
   */
  public boolean contains(final DirectiveDef directiveDef, final AxisID axisID) {
    DirectiveAttribute secondaryMatch = null;
    for (int i = directiveFileArray.length - 1; i >= 0; i--) {
      DirectiveFile directiveFile = directiveFileArray[i];
      if (directiveFile == null) {
        continue;
      }
      DirectiveAttribute attribute = directiveFile.getAttribute(Match.PRIMARYdirectiveDef, axisID);
      if (attribute != null) {
        if (attribute.overrides()) {
          return false;
        }
        if (attribute.isEmpty()) {
          continue;
        }
        return true;
      }
      else {
        continue;
      }
    }
    if (directiveDef.getDirectiveType() == DirectiveType.COPY_ARG
        && copyArgExtraValues != null) {
      return copyArgExtraValues.containsKey(directiveDef.getName(axisID));
    }
    return false;
  }

  /**
   * Checks the directive files in order from highest to lowest priority.  Returns true if
   * the highest priority directive file that contains the directive attribute does not
   * override it (an empty value for a non-boolean directive).  If none of directive files
   * contain the directive attribute, returns true if copyArgExtraValues contains it.
   * @param directiveDef
   * @return
   */
  public boolean contains(final DirectiveDef directiveDef) {
    return contains(directiveDef, null);
  }

  /**
   * Return the value of the attribute in the highest priority directive file.  Return the
   * attribute value from extraValues if the attribute is not present in any directive
   * file.
   * @param directiveDef
   * @param axisID
   * @return
   */
  public String getValue(final DirectiveDef directiveDef, final AxisID axisID) {
    boolean found = false;
    for (int i = directiveFileArray.length - 1; i >= 0; i--) {
      DirectiveFile directiveFile = directiveFileArray[i];
      if (directiveFile == null) {
        continue;
      }
      DirectiveAttribute attribute = directiveFile.getAttribute(directiveDef, axisID);
      if (attribute != null) {
        if (!attribute.isEmpty()) {
          found = true;
          if (attribute.overrides()) {
            // highest priority directive file has overridden the directive - return null
            return null;
          }
          return attribute.getValue();
        }
      }
    }
    if (!found) {
      // The attribute is not in any directive file
      if (directiveDef.getDirectiveType() == DirectiveType.COPY_ARG
          && copyArgExtraValues != null) {
        return copyArgExtraValues.get(directiveDef.getName(axisID));
      }
    }
    return null;
  }

  /**
   * Return the value of the attribute in the highest priority directive file.  Return the
   * attribute value from extraValues if the attribute is not present in any directive
   * file.
   * @param directiveDef
   * @return
   */
  public String getValue(final DirectiveDef directiveDef) {
    return getValue(directiveDef, null);
  }

  /**
   * Return the boolean value of the attribute in the highest priority directive file.
   * Return the attribute value from extraValues if the attribute is not present in any
   * directive file.
   * @param directiveDef
   * @param axisID
   * @return
   */
  public boolean isValue(final DirectiveDef directiveDef, final AxisID axisID) {
    boolean found = false;
    for (int i = directiveFileArray.length - 1; i >= 0; i--) {
      DirectiveFile directiveFile = directiveFileArray[i];
      if (directiveFile == null) {
        continue;
      }
      DirectiveAttribute attribute = directiveFile.getAttribute(directiveDef, axisID);
      if (attribute != null && !attribute.isEmpty()) {
        found = true;
        if (attribute.overrides()) {
          // highest priority directive file has overridden the directive - return false
          return false;
        }
        return attribute.isValue();
      }
    }
    if (!found) {
      // The attribute is not in any directive file
      if (directiveDef.getDirectiveType() == DirectiveType.COPY_ARG
          && copyArgExtraValues != null) {
        return DirectiveAttribute.toBoolean(copyArgExtraValues.get(directiveDef
            .getName(axisID)));
      }
    }
    return false;
  }

  /**
   * Return the boolean value of the attribute in the highest priority directive file.
   * Return the attribute value from extraValues if the attribute is not present in any
   * directive file.
   * @param directiveDef
   * @return
   */
  public boolean isValue(final DirectiveDef directiveDef) {
    return isValue(directiveDef, null);
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
   * Puts the DirectiveDef/value pair into extraValues.
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
   * @param batchDirectiveFile
   */
  public void setup(final DirectiveFile batchDirectiveFile) {
    directiveFileArray[DirectiveFileType.BATCH.getIndex()] = batchDirectiveFile;
    if (batchDirectiveFile != null) {
      setDirectiveFile(
          batchDirectiveFile.getAttribute(DirectiveDef.SCOPE_TEMPLATE, null),
          DirectiveFileType.SCOPE);
      setDirectiveFile(
          batchDirectiveFile.getAttribute(DirectiveDef.SYSTEM_TEMPLATE, null),
          DirectiveFileType.SYSTEM);
      setDirectiveFile(batchDirectiveFile.getAttribute(DirectiveDef.USER_TEMPLATE, null),
          DirectiveFileType.USER);
    }
  }

  /**
   * Sets a directive file.  No directive file other then the one being set is changed by
   * this function.
   * @param attribute
   * @param type
   */
  private void setDirectiveFile(final DirectiveAttribute attribute,
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
        directiveFileArray[type.getIndex()] = DirectiveFile.getInstance(manager, axisID,
            new File(absPath));
      }
    }
  }

  /**
   * Sets a directive file.  No directive file other then the one being set is changed by
   * this function.
   * @param file
   * @param type
   */
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

  /**
   * Returns an entry set containing the names/value pairs in all of the directive files
   * (one entry per name).  This function will not return null.  A name/value pair with a
   * blank values cause the name/value pair to be removed from the entry set.  The
   * name/value pair will be re-added afterwards if a pair with a non-blank value is
   * encountered.
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
      if (directiveFileArray[DirectiveFileType.BATCH.getIndex()] != null) {
        DirectiveAttribute attribute = directiveFileArray[DirectiveFileType.BATCH
            .getIndex()].getAttribute(DirectiveDef.SCAN_HEADER, null);
        if (attribute.isValue()) {
          Iterator<Entry<String, String>> iterator = directiveFileCollection.copyArgExtraValues
              .entrySet().iterator();
          while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            String name = entry.getKey();
            if (!pairMap.containsKey(name)) {
              pairMap.put(name, entry.getValue());
            }
          }
        }
      }
    }

    public Iterator<Entry<String, String>> iterator() {
      return pairMap.entrySet().iterator();
    }
  }
}
