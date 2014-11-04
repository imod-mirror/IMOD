package etomo.logic;

import etomo.storage.DirectiveDef;
import etomo.storage.autodoc.WritableAutodoc;
import etomo.type.AxisID;
import etomo.type.AxisType;
import etomo.type.FileType;
import etomo.ui.Field;

/**
 * <p>Description: Shared functions for the BatchRunTomo interface.</p>
 * <p/>
 * <p>Copyright: Copyright 2014</p>
 * <p/>
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 *
 * @author $Author$
 * @version $Revision$
 *          <p/>
 *          <p> $Log$ </p>
 */
public final class BatchTool {
  public static final String rcsid =
      "$Id$";

  /**
   * If the field highlight value is set, it is set to a value coming from a template.  So
   * if the field highlight value is equals to the current value, then the field is not
   * needed in the autodoc because it does not change the value of the directive.
   *
   * @param field - GUI field
   * @return true if the field does not match its default or its field highlight value
   */
  public static boolean needInAutodoc(final Field field) {
    return !field.equalsDefaultValue() && !field.equalsFieldHighlight();
  }

  /**
   * Calls saveAutodoc with a null axisID and axisType.
   *
   * @param field
   * @param autodoc
   * @return
   */
  public static boolean saveAutodoc(final Field field, final WritableAutodoc autodoc) {
    return saveAutodoc(field, null, null, autodoc);
  }

  /**
   * Saves the field to the autodoc.  Returns if true if the field can be saved in the
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
  public static boolean saveAutodoc(final Field field, final AxisID axisID,
                                    final AxisType axisType,
                                    final WritableAutodoc autodoc) {
    // Don't add directive values that are equal to default values, or directive
    // values that already exists in one of the templates. Values from templates
    // are used as field highlight values. See needInAutodoc.
    DirectiveDef directiveDef = field.getDirectiveDef();
    if (directiveDef != null) {
      if (field.isEnabled()) {
        if (directiveDef.isBoolean() && field.isBoolean() && field.isSelected()) {
          // checkboxes and radio buttons
          if (needInAutodoc(field)) {
            autodoc.addNameValuePair(directiveDef.getDirective(axisID, axisType), "1");
          }
          return true;
        }
        else if (!directiveDef.isBoolean() && field.isText()) {
          if (!field.isBoolean() && !field.isEmpty()) {
            // text fields, file text fields, and spinners
            if (needInAutodoc(field)) {
              autodoc.addNameValuePair(directiveDef.getDirective(axisID, axisType),
                  field.getText());
            }
            return true;
          }
          else if (field.isBoolean() && field.isSelected()) {
            // radio text fields and checkbox text fields
            if (needInAutodoc(field)) {
              autodoc.addNameValuePair(directiveDef.getDirective(axisID, axisType),
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
    return FileType.BATCH_RUN_TOMO_BOUNDARY_MODEL
        .getFileName(DatasetTool.getDatasetName(stack, dualAxis),
            dualAxis ? AxisType.DUAL_AXIS : AxisType.SINGLE_AXIS, null);
  }
}

