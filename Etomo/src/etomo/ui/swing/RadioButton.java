package etomo.ui.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;

import etomo.EtomoDirector;
import etomo.storage.autodoc.AutodocTokenizer;
import etomo.storage.autodoc.ReadOnlySection;
import etomo.type.EnumeratedType;
import etomo.type.EtomoAutodoc;
import etomo.type.EtomoBoolean2;
import etomo.type.UITestFieldType;
import etomo.ui.Field;
import etomo.util.Utilities;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 */
final class RadioButton implements RadioButtonInterface, Field, ActionListener {
  public static final String rcsid = "$Id$";

  private final JRadioButton radioButton;
  private final EnumeratedType enumeratedType;
  private final ButtonGroup group;

  private boolean debug = false;
  private EtomoBoolean2 checkpointValue = null;
  private boolean backupValue = false;
  private boolean fieldIsBackedUp = false;
  private boolean origForegroundSet = false;
  private Color origForeground = null;
  private boolean useFieldHighlight = false;
  private boolean fieldHighlightValue = false;

  RadioButton(final String text) {
    this(text, null, null);
  }

  RadioButton(final String text, final ButtonGroup group) {
    this(text, null, group);
  }

  RadioButton(final ButtonGroup group) {
    this("", null, group);
  }

  RadioButton(final String text, final EnumeratedType enumeratedType) {
    this(text, enumeratedType, null);
  }

  RadioButton(final String text, final EnumeratedType enumeratedType,
      final ButtonGroup group) {
    this.group = group;
    radioButton = new JRadioButton(text);
    radioButton.setModel(new RadioButtonModel(this));
    setName(text);
    this.enumeratedType = enumeratedType;
    if (group != null) {
      group.add(radioButton);
    }
    if (enumeratedType != null && enumeratedType.isDefault()) {
      radioButton.setSelected(true);
    }
  }

  RadioButton(final EnumeratedType enumeratedType, final ButtonGroup group) {
    this.group = group;
    String text = enumeratedType.getLabel();
    radioButton = new JRadioButton(text);
    radioButton.setModel(new RadioButtonModel(this));
    setName(text);
    this.enumeratedType = enumeratedType;
    if (group != null) {
      group.add(radioButton);
    }
    if (enumeratedType != null && enumeratedType.isDefault()) {
      radioButton.setSelected(true);
    }
  }

  RadioButton(final EnumeratedType enumeratedType, final ButtonGroup group,
      final String addToLabel) {
    this.group = group;
    String text = enumeratedType.getLabel() + (addToLabel != null ? addToLabel : "");
    radioButton = new JRadioButton(text);
    radioButton.setModel(new RadioButtonModel(this));
    setName(text);
    this.enumeratedType = enumeratedType;
    if (group != null) {
      group.add(radioButton);
    }
    if (enumeratedType != null && enumeratedType.isDefault()) {
      radioButton.setSelected(true);
    }
  }

  public String toString() {
    return radioButton.getText() + ": " + (radioButton.isSelected() ? "On" : "Off");
  }

  public void checkpoint() {
    if (checkpointValue == null) {
      checkpointValue = new EtomoBoolean2();
    }
    checkpointValue.set(isSelected());
  }

  public void backup() {
    backupValue = isSelected();
    fieldIsBackedUp = true;
  }

  /**
   * If the field was backed up, make the backup value the displayed value if possible,
   * and turn off the back up.  Its impossible to turn off a radio button, so this only
   * works if the backupValue is true.  This relies on the other radio buttons in the
   * group also being backed up.
   */
  public void restoreFromBackup() {
    if (fieldIsBackedUp) {
      if (backupValue) {
        setSelected(true);
      }
      fieldIsBackedUp = false;
    }
  }

  /**
   * No way to clear a radio button
   */
  public void clear() {
  }

  public void copy(final Field copyFrom) {
    if (copyFrom == null) {
      return;
    }
    if (copyFrom.isSelected()) {
      setSelected(true);
    }
  }

  public void useDefaultValue() {
  }

  boolean isCheckpointValue() {
    if (checkpointValue == null) {
      return false;
    }
    return checkpointValue.is();
  }

  /**
   * 
   * @param alwaysCheck - check for difference even when the field is disables or invisible
   * @return
   */
  public boolean isDifferentFromCheckpoint(final boolean alwaysCheck) {
    if (!alwaysCheck && (!isEnabled() || !radioButton.isVisible())) {
      return false;
    }
    return checkpointValue == null || !checkpointValue.equals(isSelected());
  }

  void setText(final String text) {
    radioButton.setText(text);
    setName(text);
  }

  void setBorderPainted(boolean b) {
    radioButton.setBorderPainted(b);
  }

  public void setBorder(Border border) {
    radioButton.setBorder(border);
  }

  Font getFont() {
    return radioButton.getFont();
  }

  public void setForeground(Color fg) {
    radioButton.setForeground(fg);
  }

  public int getWidth() {
    return radioButton.getWidth();
  }

  public int getHeight() {
    return radioButton.getHeight();
  }

  public Border getBorder() {
    return radioButton.getBorder();
  }

  void setName(final String text) {
    String name = Utilities.convertLabelToName(text);
    radioButton.setName(UITestFieldType.RADIO_BUTTON.toString()
        + AutodocTokenizer.SEPARATOR_CHAR + name);
    if (EtomoDirector.INSTANCE.getArguments().isPrintNames()) {
      System.out.println(radioButton.getName() + ' ' + AutodocTokenizer.DEFAULT_DELIMITER
          + ' ');
    }
  }

  boolean equals(final EnumeratedType enumeratedType) {
    return this.enumeratedType == enumeratedType;
  }

  public void setDebug(final boolean input) {
    debug = input;
  }

  void setFieldHighlightValue(final boolean value) {
    if (!useFieldHighlight) {
      useFieldHighlight = true;
      // Radio buttons turn off when another button in the group is turned on. So listen
      // to all of the radio buttons in the group.
      boolean actionListenerAdded = false;
      if (group != null) {
        Enumeration<AbstractButton> enumeration = group.getElements();
        if (enumeration != null) {
          while (enumeration.hasMoreElements()) {
            actionListenerAdded = true;
            enumeration.nextElement().addActionListener(this);
          }
        }
      }
      if (!actionListenerAdded) {
        radioButton.addActionListener(this);
      }
    }
    fieldHighlightValue = value;
    updateFieldHighlight(isSelected());
  }

  public void actionPerformed(final ActionEvent event) {
    boolean selected;
    // Radio buttons cannot be turned off directly. When another button in the group is
    // clicked, this button will turn off if it was on. This response doesn't happen
    // instantly, but its accurate to assume that this button is off when another button
    // was clicked.
    if (!event.getActionCommand().equals(radioButton.getActionCommand())) {
      selected = false;
    }
    else {
      selected = isSelected();
    }
    updateFieldHighlight(selected);
  }

  void updateFieldHighlight(final boolean isSelected) {
    if (useFieldHighlight) {
      if (fieldHighlightValue == isSelected) {
        if (!origForegroundSet) {
          origForegroundSet = true;
          origForeground = radioButton.getForeground();
        }
        radioButton.setForeground(Colors.FIELD_HIGHLIGHT);
      }
      else if (origForeground != null) {
        radioButton.setForeground(origForeground);
      }
      else {
        radioButton.setForeground(Color.black);
      }
    }
  }

  /**
   * Sets a tooltip from a section using the enumeratedType, if it exists.
   * @param section
   */
  void setToolTipText(ReadOnlySection section) {
    String text;
    if (enumeratedType == null) {
      text = EtomoAutodoc.getTooltip(section);
    }
    else {
      text = EtomoAutodoc.getTooltip(section, enumeratedType.toString());
    }
    setToolTipText(text);
  }

  void setToolTipText(final String text) {
    radioButton.setToolTipText(TooltipFormatter.INSTANCE.format(text));
  }

  void setVisible(final boolean visible) {
    radioButton.setVisible(visible);
  }

  void addActionListener(final ActionListener actionListener) {
    radioButton.addActionListener(actionListener);
  }

  void addChangeListener(final ChangeListener listener) {
    radioButton.addChangeListener(listener);
  }

  void setSelected(final boolean selected) {
    radioButton.setSelected(selected);
  }

  public void msgSelected() {
  }

  public boolean isSelected() {
    return radioButton.isSelected();
  }

  AbstractButton getAbstractButton() {
    return radioButton;
  }

  void setPreferredSize(final Dimension preferredSize) {
    radioButton.setPreferredSize(preferredSize);
  }

  public String getText() {
    return radioButton.getText();
  }

  void setModel(final ButtonModel newModel) {
    radioButton.setModel(newModel);
  }

  String getName() {
    return radioButton.getName();
  }

  public EnumeratedType getEnumeratedType() {
    return enumeratedType;
  }

  Component getComponent() {
    return radioButton;
  }

  void setEnabled(final boolean enable) {
    radioButton.setEnabled(enable);
  }

  String getActionCommand() {
    return radioButton.getActionCommand();
  }

  boolean isEnabled() {
    return radioButton.isEnabled();
  }

  void setAlignmentX(float alignmentX) {
    radioButton.setAlignmentX(alignmentX);
  }

  Object[] getSelectedObjects() {
    return radioButton.getSelectedObjects();
  }

  static final class RadioButtonModel extends JToggleButton.ToggleButtonModel {
    private final RadioButtonInterface radioButton;

    RadioButtonModel(RadioButtonInterface radioButton) {
      super();
      this.radioButton = radioButton;
    }

    public void setSelected(boolean selected) {
      super.setSelected(selected);
      radioButton.msgSelected();
    }

    EnumeratedType getEnumeratedType() {
      return radioButton.getEnumeratedType();
    }
  }
}
/**
 * <p> $Log$
 * <p> Revision 1.1  2010/11/13 16:07:34  sueh
 * <p> bug# 1417 Renamed etomo.ui to etomo.ui.swing.
 * <p>
 * <p> Revision 1.25  2010/03/03 05:05:08  sueh
 * <p> bug# 1311 Added getFont and setVisible.
 * <p>
 * <p> Revision 1.24  2009/11/20 17:30:51  sueh
 * <p> bug# 1282 Added prefixes to all of the field names, so that the fields that
 * <p> are actually abstract buttons (radio buttons, etc) won't be activated by a
 * <p> "bn." field command.
 * <p>
 * <p> Revision 1.23  2009/04/13 22:58:01  sueh
 * <p> Removed newstuff.
 * <p>
 * <p> Revision 1.22  2009/02/27 03:54:22  sueh
 * <p> bug# 1172 Added experimental automation recording background color
 * <p> (newstuff only).
 * <p>
 * <p> Revision 1.21  2009/01/20 20:23:00  sueh
 * <p> bug# 1102 Changed UITestField to UITestFieldType.
 * <p>
 * <p> Revision 1.20  2008/05/30 22:33:01  sueh
 * <p> bug# 1102 Isolating the etomo.uitest package so it is not need for
 * <p> running EtomoDirector.
 * <p>
 * <p> Revision 1.19  2008/05/30 21:33:56  sueh
 * <p> bug# 1102 Moved uitest classes to etomo.uitest.
 * <p>
 * <p> Revision 1.18  2008/02/29 20:51:20  sueh
 * <p> bug# 1092 Added toString().
 * <p>
 * <p> Revision 1.17  2007/12/26 22:26:16  sueh
 * <p> bug# 1052 Moved argument handling from EtomoDirector to a separate class.
 * <p>
 * <p> Revision 1.16  2007/09/27 21:04:32  sueh
 * <p> bug# 1044 Expanded RadioButton so it can be used in RadioButtonCell.
 * <p>
 * <p> Revision 1.15  2007/09/07 00:28:23  sueh
 * <p> bug# 989 Using a public INSTANCE to refer to the EtomoDirector singleton
 * <p> instead of getInstance and createInstance.
 * <p>
 * <p> Revision 1.14  2007/05/17 23:50:02  sueh
 * <p> bug# 964 In setToolTipText(), return the tooltip.
 * <p>
 * <p> Revision 1.13  2007/05/08 01:20:51  sueh
 * <p> bug# 964 Added setToolTipText(ReadOnlySection) to set an enum tooltip.
 * <p>
 * <p> Revision 1.12  2007/04/13 20:37:15  sueh
 * <p> bug# 964 Removed radioValue and added EnumeratedType, which is the
 * <p> interface for enumeration types.
 * <p>
 * <p> Revision 1.11  2007/03/20 00:45:40  sueh
 * <p> bug# 964 Added constructors which take ButtonGroup.
 * <p>
 * <p> Revision 1.10  2007/03/07 21:12:32  sueh
 * <p> bug# 981 Turned RadioButton into a wrapper rather then a child of JRadioButton,
 * <p> because it is getting more complicated.
 * <p>
 * <p> Revision 1.9  2007/03/03 01:03:49  sueh
 * <p> bug# 973 Added a RadioButtonModel for classes that use a radio button, and
 * <p> want to respond to the setSelected calls that automatically turn off other buttons
 * <p> in the group.
 * <p>
 * <p> Revision 1.8  2007/02/09 00:52:13  sueh
 * <p> bug# 962 Made TooltipFormatter a singleton and moved its use to low-level ui
 * <p> classes.
 * <p>
 * <p> Revision 1.7  2006/05/16 21:36:30  sueh
 * <p> bug# 856 Changing the name whenever the label is changed so that its easy to
 * <p> see what the name is.
 * <p>
 * <p> Revision 1.6  2006/04/25 19:19:47  sueh
 * <p> bug# 787 Added UITestField, an enum style class which contains the
 * <p> fields found in uitestaxis.adoc files.
 * <p>
 * <p> Revision 1.5  2006/04/06 20:17:51  sueh
 * <p> bug# 808 Moved the function convertLabelToName from UIUtilities to
 * <p> util.Utilities.
 * <p>
 * <p> Revision 1.4  2006/01/12 17:37:28  sueh
 * <p> bug# 798 Moved the autodoc classes to etomo.storage.autodoc.
 * <p>
 * <p> Revision 1.3  2006/01/11 22:32:48  sueh
 * <p> bug# 675 fixed print names functionality
 * <p>
 * <p> Revision 1.2  2006/01/04 20:28:01  sueh
 * <p> bug# 675 For printing the name:  putting the type first and making the type
 * <p> as constant.
 * <p>
 * <p> Revision 1.1  2005/12/23 02:19:17  sueh
 * <p> bug# 675 A class to allow automatic naming of radio buttons.
 * <p> </p>
 */
