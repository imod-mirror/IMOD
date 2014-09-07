package etomo.ui;

import etomo.storage.DirectiveDef;

/**
* <p>Description: An interface to allow simple commands to be run against a group of 
* fields.</p>
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

  public boolean isDifferentFromCheckpoint(boolean alwaysCheck);

  public void backup();

  public void useDefaultValue();

  public void restoreFromBackup();

  public void checkpoint();

  public void clear();

  public void clearFieldHighlightValue();

  /**
   * @return true if this a binary control (toggle button, radio button, checkbox) and it is selected
   */
  public boolean isSelected();

  public String getText();

  public void copy(Field from);

  public boolean equalsDefaultValue();

  public boolean equalsFieldHighlightValue();

  public DirectiveDef getDirectiveDef();

  public String getQuotedLabel();

  public boolean isBoolean();

  /**
   * Returns true if the field contains any kind of text - including spinners.
   * @return
   */
  public boolean isText();

  /**
   * Returns true if there is a non-empty, non-whitespace value.  Boolean fields
   * are never empty.
   * @return
   */
  public boolean isEmpty();

  public boolean isEnabled();

  public FieldSettingInterface getCheckpoint();

  public void setCheckpoint(FieldSettingInterface checkpoint);

  public FieldSettingInterface getFieldHighlight();

  public void setFieldHighlight(FieldSettingInterface fieldHighlight);
}
