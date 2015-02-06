package etomo.ui;

import etomo.storage.DirectiveDef;

/**
 * <p>Description: An interface to allow the generic handling of GUI fields.</p>
 * <p/>
 * <p>Copyright: Copyright 2012 - 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public interface Field {
  public boolean isBoolean();

  /**
   * Returns true if the field contains any kind of text - including spinners.
   *
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
   *
   * @param text
   */
  public void setValue(String text);

  /**
   * No effect in text-only fields
   *
   * @param bool boolean value
   */
  public void setValue(boolean bool);

  public boolean isEmpty();

  /**
   * @return true if this a binary control (toggle button, radio button,
   * checkbox) and it is selected
   */
  public boolean isSelected();

  /**
   * @return true if the field must currently contain text (not required when disabled)
   */
  public boolean isRequired();

  public String getText();

  public String getText(boolean doValidation, FieldDisplayer fieldDisplayer)
    throws FieldValidationFailedException;

  /**
   * Returns true if there is a non-empty, non-whitespace value.  Boolean fields
   * are never empty.
   *
   * @return
   */

  public DirectiveDef getDirectiveDef();

  /**
   * Use DefaultFinder to find and set a default value.  DefaultFinder requires
   * DirectiveDef, and only works for comparam directives.
   */
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

  public boolean isFieldHighlightSet();

  public void clearFieldHighlight();

  public void setFieldHighlight(FieldSettingInterface input);

  public void setFieldHighlight(String input);

  /**
   * No effect in text-only fields
   *
   * @param input
   */
  public void setFieldHighlight(boolean input);

  public FieldSettingInterface getFieldHighlight();

  public boolean equalsFieldHighlight();
}
