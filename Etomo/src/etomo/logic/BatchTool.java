package etomo.logic;

import etomo.BaseManager;
import etomo.storage.DirectiveDef;
import etomo.storage.LogFile;
import etomo.storage.autodoc.Autodoc;
import etomo.storage.autodoc.AutodocFactory;
import etomo.storage.autodoc.WritableAutodoc;
import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.type.FileType;
import etomo.ui.Field;
import etomo.ui.FieldDisplayer;
import etomo.ui.FieldValidationFailedException;

import java.io.File;
import java.io.IOException;

/**
 * <p>Description: Shared functions for the BatchRunTomo interface.</p>
 * <p/>
 * <p>Copyright: Copyright 2014 - 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class BatchTool {
  /**
   * Merges the default batchruntomo autodoc into the input directive file.  Merges the
   * three template files together.  Subtracts the template merge from the batch merge and
   * returns the result.
   *
   * @param manager
   * @param inputDirectiveFile
   * @param templateFiles      standard order: scope to user
   * @return
   */
  public static Autodoc graftDirectiveFiles(final BaseManager manager,
    final File inputDirectiveFile, final File[] templateFiles) {
    File batchDefault =
      FileType.DEFAULT_BATCH_RUN_TOMO_AUTODOC.getFile(manager, AxisID.ONLY);
    boolean batchDefaultExists = batchDefault.exists();
    boolean inputDirectiveFileSet =
      inputDirectiveFile != null && !inputDirectiveFile.exists();
    if (!inputDirectiveFileSet && !batchDefaultExists) {
      return null;
    }
    Autodoc totalBatch = null;
    Autodoc totalTemplate = null;
    try {
      if (!inputDirectiveFileSet) {
        totalBatch = AutodocFactory.getWritableAutodocInstance(manager, batchDefault);
      }
      else if (!batchDefaultExists) {
        totalBatch =
          AutodocFactory.getWritableAutodocInstance(manager, inputDirectiveFile);
      }
      else {
        totalBatch =
          AutodocFactory.graftMergeGlobal(manager, inputDirectiveFile, batchDefault);
      }
      // Subtract template files if there is something to subtract them from.
      if (totalBatch != null && templateFiles != null && templateFiles.length > 0) {
        if (templateFiles.length == 1) {
          totalTemplate =
            AutodocFactory.getWritableAutodocInstance(manager, templateFiles[0]);
        }
        else {
          totalTemplate =
            AutodocFactory.graftMergeGlobal(manager,
              templateFiles[templateFiles.length - 1],
              templateFiles[templateFiles.length - 2]);
          for (int i = templateFiles.length - 3; i >= 0; i--) {
            totalTemplate =
              AutodocFactory.graftMergeGlobal(manager, totalTemplate, templateFiles[i]);
          }
        }
      }
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    // Subtract the templates from the batch file merge
    try {
      if (totalBatch != null && totalTemplate != null) {
        return AutodocFactory.subtractGlobal(totalBatch, totalTemplate);
      }
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return totalBatch;
  }

  /**
   * The field highlight value comes from a template.  The default value comes from the
   * corresponding comscript autodoc default attribute.  A set field highlight overrides
   * a default value, since the template value overrides the default comscript value.
   *
   * @param field - GUI field
   * @return true if the field's current value is not the same as what would be obtained from the templates/default value.
   */
  public static boolean needInAutodoc(final Field field) {
    if (field.isFieldHighlightSet()) {
      return !field.equalsFieldHighlight();
    }
    return !field.equalsDefaultValue();
  }

  /**
   * Calls saveAutodoc with a null axisID and axisType.
   *
   * @param field
   * @param autodoc
   * @return true if field is savable
   * @throws FieldValidationFailedException
   */
  public static boolean saveFieldToAutodoc(final Field field,
    final WritableAutodoc autodoc) {
    try {
      return saveFieldToAutodoc(field, null, null, autodoc, false, null);
    }
    catch (FieldValidationFailedException e) {
      // shouldn't happen because validation boolean was false
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Calls saveAutodoc with a null axisID and axisType.
   *
   * @param field
   * @param autodoc
   * @param doValidation
   * @return true if field is valid (if doValidation is on) and savable
   * @throws FieldValidationFailedException
   */
  public static boolean saveFieldToAutodoc(final Field field,
    final WritableAutodoc autodoc, final boolean doValidation,
    final FieldDisplayer fieldDisplayer) throws FieldValidationFailedException {
    return saveFieldToAutodoc(field, null, null, autodoc, doValidation, fieldDisplayer);
  }

  /**
   * 
   * @param field
   * @param axisID
   * @param axisType
   * @param autodoc
   * @return true if field is savable
   * @throws FieldValidationFailedException
   */
  public static boolean saveFieldToAutodoc(final Field field, final AxisID axisID,
    final AxisType axisType, final WritableAutodoc autodoc) {
    try {
      return saveFieldToAutodoc(field, axisID, axisType, autodoc, false, null);
    }
    catch (FieldValidationFailedException e) {
      // shouldn't happen because validation boolean was false
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Adds the field to the autodoc.  Returns if true if the field can be saved in the
   * autodoc.  If the field is not needed in the autodoc because it is set to default or
   * the same is a template value, it won't be saved, but this function will still return
   * true.
   *
   * @param field    - GUI field
   * @param autodoc  - autodoc for saving directives
   * @param axisID   axisID of field (optional for axis A)
   * @param axisType axis type of dataset (optional for axis A)
   * @param doValidation
   * @return true if field is valid (if doValidation is on) and savable
   * @throws FieldValidationFailedException
   */
  public static boolean saveFieldToAutodoc(final Field field, final AxisID axisID,
    final AxisType axisType, final WritableAutodoc autodoc, final boolean doValidation,
    final FieldDisplayer fieldDisplayer) throws FieldValidationFailedException {
    // Don't add directive values that are equal to default values, or directive
    // values that already exists in one of the templates. Values from templates
    // are used as field highlight values. See needInAutodoc.
    DirectiveDef directiveDef = field.getDirectiveDef();
    if (directiveDef != null) {
      if (field.isEnabled()) {
        if (directiveDef.isBoolean() && field.isBoolean() && field.isSelected()) {
          // checkboxes and radio buttons
          if (needInAutodoc(field)) {
            autodoc.addNameValuePairAttribute(
              directiveDef.getDirective(axisID, axisType), "1");
          }
          return true;
        }
        else if (!directiveDef.isBoolean() && field.isText()) {
          if (!field.isBoolean() && (!field.isEmpty() || field.isRequired())) {
            // text fields, file text fields, and spinners
            if (needInAutodoc(field)) {
              autodoc.addNameValuePairAttribute(directiveDef.getDirective(axisID,
                axisType), field.getText(doValidation, fieldDisplayer));
            }
            return true;
          }
          else if (field.isBoolean() && field.isSelected()) {
            // radio text fields and checkbox text fields
            if (needInAutodoc(field)) {
              autodoc.addNameValuePairAttribute(directiveDef.getDirective(axisID,
                axisType), field.getText(doValidation, fieldDisplayer));
            }
            return true;
          }
        }
      }
    }
    else {
      System.err.println("ERROR: " + field.getQuotedLabel() + " has a null directive.");
      Thread.dumpStack();
    }
    return false;

  }

  public static String getModelFileName(final FileType fileType, final String stack,
    final boolean dualAxis) {
    if (fileType != null) {
      return fileType.getFileName(DatasetTool.getDatasetName(stack, dualAxis),
        dualAxis ? AxisType.DUAL_AXIS : AxisType.SINGLE_AXIS, null);
    }
    return null;
  }
}
