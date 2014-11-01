package etomo.ui.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ColorUIResource;

import etomo.logic.DefaultFinder;
import etomo.storage.DirectiveDef;
import etomo.type.EtomoBoolean2;
import etomo.type.UITestFieldType;
import etomo.ui.BooleanFieldSetting;
import etomo.ui.Field;
import etomo.ui.FieldSettingInterface;
import etomo.util.Utilities;

/**
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p/>
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
 * University of Colorado</p>
 *
 * @author $Author$
 * @version $Revision$
 */
final class CheckBoxCell extends InputCell implements ToggleCell, ActionListener, Field {
  public static final String rcsid =
      "$Id$";

  private final JCheckBox checkBox = new JCheckBox();
  // label: from JCheckBox.getText(). Updated in setLabel(). Always up to date
  // because it is a read only field in JCheckBox.
  private String unformattedLabel = "";
  private BooleanFieldSetting checkpoint = null;
  private boolean backupValue = false;
  private boolean fieldIsBackedUp = false;
  private BooleanFieldSetting fieldHighlight = null;
  private DirectiveDef directiveDef = null;

  CheckBoxCell() {
    super();
    checkBox.setBorderPainted(true);
    checkBox.setBorder(BorderFactory.createEtchedBorder());
    setBackground();
    setForeground();
    setFont();
  }

  Component getComponent() {
    return checkBox;
  }

  UITestFieldType getFieldType() {
    return UITestFieldType.CHECK_BOX;
  }

  public boolean isText() {
    return false;
  }

  public boolean isBoolean() {
    return true;
  }

  public boolean isEmpty() {
    return false;
  }

  /**
   * @param alwaysCheck - check for difference even when the field is disables or
   *                    invisible
   * @return true if different from checkpoint
   */
  public boolean isDifferentFromCheckpoint(final boolean alwaysCheck) {
    if (!alwaysCheck && (!isEnabled() || !checkBox.isVisible())) {
      return false;
    }
    return checkpoint == null || !checkpoint.equals(isSelected());
  }

  public void backup() {
    backupValue = isSelected();
    fieldIsBackedUp = true;
  }

  /**
   * If the field was backed up, make the backup value the displayed value, and turn off
   * the back up.
   */
  public void restoreFromBackup() {
    if (fieldIsBackedUp) {
      setSelected(backupValue);
      fieldIsBackedUp = false;
    }
  }

  public void clear() {
    setSelected(false);
  }

  /**
   * Constructs savedValue (if it doesn't exist).  Saves the current setting.
   */
  public void checkpoint() {
    if (checkpoint == null) {
      checkpoint = new BooleanFieldSetting();
    }
    checkpoint.set(isSelected());
  }

  public void setCheckpoint(FieldSettingInterface input) {
    if (input != null && input.isSet() && input.isBoolean()) {
      if (checkpoint == null) {
        checkpoint = new BooleanFieldSetting();
      }
      checkpoint.copy(input);
    }
    else if (checkpoint != null) {
      checkpoint.reset();
    }
  }

  public FieldSettingInterface getCheckpoint() {
    return checkpoint;
  }

  public void setEnabled(final boolean enabled) {
    setEditable(enabled);
  }

  public void setLabel(final String label) {
    unformattedLabel = label;
    setForeground();
  }

  private void setHtmlLabel(final ColorUIResource color) {
    checkBox.setText(
        "<html><P style=\"font-weight:normal; color:rgb(" + color.getRed() + "," +
            color.getGreen() + "," + color.getBlue() + ")\">" + unformattedLabel +
            "</style>");
  }

  public String getLabel() {
    return unformattedLabel;
  }

  public String getText() {
    return unformattedLabel;
  }

  public String getQuotedLabel() {
    return Utilities.quoteLabel(unformattedLabel);
  }

  public void setValue(final String value) {
    checkBox.setSelected(new EtomoBoolean2().set(value).is());
  }

  public boolean isEnabled() {
    return checkBox.isEnabled();
  }

  public boolean isSelected() {
    return checkBox.isSelected();
  }

  public void setSelected(final boolean selected) {
    checkBox.setSelected(selected);
  }

  public void setValue(final boolean selected) {
    checkBox.setSelected(selected);
  }

  public void setValue(final Field input) {
    if (input == null) {
      clear();
    }
    else {
      setSelected(input.isSelected());
    }
  }

  void setActionCommand(final String input) {
    checkBox.setActionCommand(input);
  }

  String getActionCommand() {
    return checkBox.getActionCommand();
  }

  public void addActionListener(final ActionListener actionListener) {
    checkBox.addActionListener(actionListener);
  }

  public void addChangeListener(ChangeListener listener) {
    checkBox.addChangeListener(listener);
  }

  private void setForeground() {
    checkBox.setForeground(Colors.CELL_FOREGROUND);
    setHtmlLabel(Colors.CELL_FOREGROUND);
  }

  void setDirectiveDef(final DirectiveDef directiveDef) {
    this.directiveDef = directiveDef;
  }

  public DirectiveDef getDirectiveDef() {
    return directiveDef;
  }

  public void setFieldHighlight(final boolean value) {
    if (fieldHighlight == null) {
      fieldHighlight = new BooleanFieldSetting();
      checkBox.addActionListener(this);
    }
    fieldHighlight.set(value);
    updateFieldHighlight();
  }

  public void setFieldHighlight(final String value) {
  }

  public void setFieldHighlight(final FieldSettingInterface settingInterface) {
    BooleanFieldSetting setting = null;
    if (settingInterface != null) {
      setting = settingInterface.getBooleanSetting();
    }
    if (setting == null || !setting.isSet()) {
      clearFieldHighlight();
    }
    else {
      if (fieldHighlight == null) {
        fieldHighlight = new BooleanFieldSetting();
        checkBox.addActionListener(this);
      }
      fieldHighlight.copy(setting);
      updateFieldHighlight();
    }
  }

  public boolean equalsFieldHighlight() {
    return fieldHighlight != null && fieldHighlight.isSet() &&
        fieldHighlight.equals(isSelected());
  }

  public void clearFieldHighlight() {
    if (fieldHighlight != null && fieldHighlight.isSet()) {
      fieldHighlight.reset();
      // Turn off field highlight - parameter doesn't matter since field highlight is off.
      updateFieldHighlight();
    }
  }

  public FieldSettingInterface getFieldHighlight() {
    return fieldHighlight;
  }

  public void actionPerformed(ActionEvent e) {
    updateFieldHighlight();
  }

  void updateFieldHighlight() {
    if (fieldHighlight != null && fieldHighlight.isSet()) {
      if (fieldHighlight.isValue() == isSelected()) {
        checkBox.setForeground(Colors.FIELD_HIGHLIGHT);
      }
      else {
        checkBox.setForeground(Colors.CELL_FOREGROUND);
      }
    }
  }

  public void useDefaultValue() {
    System.err.println("Warning: CheckBoxCell.useDefaultValue has not been implemented");
  }

  public boolean equalsDefaultValue() {
    return false;
  }

  public int getHeight() {
    return checkBox.getHeight() + checkBox.getBorder().getBorderInsets(checkBox).bottom -
        1;
  }

  public int getWidth() {
    return checkBox.getWidth();
  }

  int getLeftBorder() {
    return checkBox.getBorder().getBorderInsets(checkBox).left;
  }

  void setToolTipText(String text) {
    checkBox.setToolTipText(TooltipFormatter.INSTANCE.format(text));
  }
}
/**
 * <p> $Log$
 * <p> Revision 1.1  2010/11/13 16:07:34  sueh
 * <p> bug# 1417 Renamed etomo.ui to etomo.ui.swing.
 * <p>
 * <p> Revision 1.14  2009/01/20 19:49:55  sueh
 * <p> bug# 1102 Added getFieldType.
 * <p>
 * <p> Revision 1.13  2007/09/27 20:49:20  sueh
 * <p> bug# 1044 Implementing ToggleCell and adding addChangeListener() to allow a
 * <p> radio button to be used to select a single queue in the processor table.
 * <p>
 * <p> Revision 1.12  2007/04/02 21:44:31  sueh
 * <p> bug# 964 Implementing Cell interface.
 * <p>
 * <p> Revision 1.11  2007/03/27 19:30:37  sueh
 * <p> bug# 964 Changed InputCell.setEnabled() to setEditable.  Added setEnabled().
 * <p>
 * <p> Revision 1.10  2007/03/01 01:28:11  sueh
 * <p> bug# 964 Made colors constant and moved them to Colors.
 * <p>
 * <p> Revision 1.9  2007/02/09 00:47:49  sueh
 * <p> bug# 962 Made TooltipFormatter a singleton and moved its use to low-level ui
 * <p> classes.
 * <p>
 * <p> Revision 1.8  2007/02/05 23:34:51  sueh
 * <p> bug# 962 Improved tooltip setting.
 * <p>
 * <p> Revision 1.7  2006/10/16 22:50:31  sueh
 * <p> bug# 919  Added setToolTipText().
 * <p>
 * <p> Revision 1.6  2005/11/04 00:53:39  sueh
 * <p> fixed file comment
 * <p>
 * <p> Revision 1.5  2005/08/04 20:07:52  sueh
 * <p> bug# 532 added setSelected() and getWidth().
 * <p>
 * <p> Revision 1.4  2005/08/01 18:07:07  sueh
 * <p> bug# 532 fixed getLabel(), which was returning the checkbox text instead
 * <p> of the unformatted label.
 * <p>
 * <p> Revision 1.3  2005/07/19 22:31:03  sueh
 * <p> bug# 532 changing the look of inUse == false to greyed out text.
 * <p>
 * <p> Revision 1.2  2005/07/11 22:55:40  sueh
 * <p> bug# 619 Added functions:  getBorderHeight and getHeight so that the
 * <p> height of the processor table can be calculated.
 * <p>
 * <p> Revision 1.1  2005/07/01 21:08:54  sueh
 * <p> bug# 619 CheckBoxCell is a writable table cell that inherits InputCell and
 * <p> contains a JCheckBoxCell.
 * <p> </p>
 */
