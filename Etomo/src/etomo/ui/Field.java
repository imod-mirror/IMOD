package etomo.ui;

import etomo.storage.DirectiveDef;

/**
* <p>Description: An interface to allow the generic handling of GUI fields.</p>
* 
* <p>Copyright: Copyright 2014</p>
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
public interface Field {
  public static final String rcsid = "$Id:$";

  public boolean isBoolean();

  /**
   * Returns true if the field contains any kind of text - including spinners.
   * @return
   */
  public boolean isText();

  public String getQuotedLabel();

  public boolean isEnabled();

  /**
   * Resets value - does not change other settings
   */
  public void clear();

  public void setValue(Field from);

  /**
   * No effect in boolean-only fields
   * @param text
   */
  public void setValue(String text);

  /**
   * No effect in text-only fields
   * @param bool
   */
  public void setValue(boolean bool);

  public boolean isEmpty();

  /**
   * @return true if this a binary control (toggle button, radio button, checkbox) and it is selected
   */
  public boolean isSelected();

  public String getText();

  /**
   * Returns true if there is a non-empty, non-whitespace value.  Boolean fields
   * are never empty.
   * @return
   */

  public DirectiveDef getDirectiveDef();

  public void useDefaultValue();

  public boolean equalsDefaultValue();

  public void backup();

  /**
   * Set the value from the backup, and delete the backup.
   */
  public void restoreFromBackup();

  public void checkpoint();

  public void setCheckpoint(FieldSettingInterface input);

  public FieldSettingInterface getCheckpoint();

  public boolean isDifferentFromCheckpoint(boolean alwaysCheck);

  public void clearFieldHighlight();

  public void setFieldHighlight(FieldSettingInterface input);

  public void setFieldHighlight(String input);

  /**
   * No effect in text-only fields
   * @param input
   */
  public void setFieldHighlight(boolean input);

  public FieldSettingInterface getFieldHighlight();

  public boolean equalsFieldHighlight();
}
