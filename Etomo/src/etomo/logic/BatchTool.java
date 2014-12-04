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

import java.io.File;
import java.io.IOException;

/**
 * <p>Description: Shared functions for the BatchRunTomo interface.</p>
 * <p/>
 * <p>Copyright: Copyright 2014 by the Regents of the University of Colorado</p>
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
  public static Autodoc mergeDirectiveFiles(final BaseManager manager,
      final File inputDirectiveFile, final File[] templateFiles) {
    Autodoc totalBatch = null;
    Autodoc totalTemplate = null;
    try {
      totalBatch = AutodocFactory.mergeGlobal(manager, inputDirectiveFile, new File(
          "/home/sueh/defaultBatchruntomo.adoc"));
      // Subtract template files if there is something to subtract them from.
      if (totalBatch != null && templateFiles != null && templateFiles.length > 0) {
        if (templateFiles.length == 1) {
          totalTemplate = AutodocFactory.getWritableAutodocInstance(manager,
              templateFiles[0]);
        }
        else {
          totalTemplate = AutodocFactory.mergeGlobal(manager,
              templateFiles[templateFiles.length - 1],
              templateFiles[templateFiles.length - 2]);
          for (int i = templateFiles.length - 3; i >= 0; i--) {
            totalTemplate = AutodocFactory
                .mergeGlobal(manager, totalTemplate, templateFiles[i]);
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
   * @return
   */
  public static boolean saveFieldToAutodoc(final Field field, final WritableAutodoc autodoc) {
    return saveFieldToAutodoc(field, null, null, autodoc);
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
   * @return true if field is savable
   */
  public static boolean saveFieldToAutodoc(final Field field, final AxisID axisID,
      final AxisType axisType, final WritableAutodoc autodoc) {
    // Don't add directive values that are equal to default values, or directive
    // values that already exists in one of the templates. Values from templates
    // are used as field highlight values. See needInAutodoc.
    DirectiveDef directiveDef = field.getDirectiveDef();
    if (directiveDef != null) {
      if (field.isEnabled()) {
        if (directiveDef.isBoolean() && field.isBoolean() && field.isSelected()) {
          // checkboxes and radio buttons
          if (needInAutodoc(field)) {
            autodoc.addNameValuePairAttribute(directiveDef.getDirective(axisID, axisType),
                "1");
          }
          return true;
        }
        else if (!directiveDef.isBoolean() && field.isText()) {
          if (!field.isBoolean() && !field.isEmpty()) {
            // text fields, file text fields, and spinners
            if (needInAutodoc(field)) {
              autodoc.addNameValuePairAttribute(
                  directiveDef.getDirective(axisID, axisType), field.getText());
            }
            return true;
          }
          else if (field.isBoolean() && field.isSelected()) {
            // radio text fields and checkbox text fields
            if (needInAutodoc(field)) {
              autodoc.addNameValuePairAttribute(directiveDef.getDirective(axisID, axisType),
                  field.getText());
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

  public static String getBoundaryModelName(final String stack, final boolean dualAxis) {
    return FileType.BATCH_RUN_TOMO_BOUNDARY_MODEL.getFileName(
        DatasetTool.getDatasetName(stack, dualAxis),
        dualAxis ? AxisType.DUAL_AXIS : AxisType.SINGLE_AXIS, null);
  }
}
