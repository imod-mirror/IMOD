package etomo.ui.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JLabel;
import javax.swing.JTextField;

import etomo.EtomoDirector;
import etomo.logic.DefaultFinder;
import etomo.logic.FieldValidator;
import etomo.storage.DirectiveDef;
import etomo.storage.autodoc.AutodocTokenizer;
import etomo.type.EtomoNumber;
import etomo.type.UITestFieldType;
import etomo.ui.FieldSetting;
import etomo.ui.Field;
import etomo.ui.FieldSettingInterface;
import etomo.ui.FieldType;
import etomo.ui.FieldValidationFailedException;
import etomo.ui.TextFieldInterface;
import etomo.ui.UIComponent;
import etomo.util.Utilities;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright 2006</p>
 *
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 */
final class TextField implements UIComponent, SwingComponent, Field, FocusListener,
    TextFieldInterface {
  public static final String rcsid = "$Id$";

  private final JTextField textField = new JTextField();

  private final FieldType fieldType;
  private final String reference;
  private final String locationDescr;

  private boolean required = false;
  private Color origForeground = null;
  private String backupValue = null;
  private boolean fieldIsBackedUp = false;
  private DirectiveDef directiveDef = null;
  private FieldSetting defaultValue = null;
  private FieldSetting checkpoint = null;
  private FieldSetting fieldHighlight = null;
  private String label = null;

  TextField(final FieldType fieldType, final String reference, final String locationDescr) {
    this.locationDescr = locationDescr;
    this.fieldType = fieldType;
    this.reference = reference;
    setName(reference);
    // Set the maximum height of the text field box to twice the
    // font size since it is not set by default
    JLabel label = new JLabel(reference);
    Dimension maxSize = textField.getMaximumSize();
    if (label.getFont().getSize() > textField.getFont().getSize()) {
      maxSize.setSize(maxSize.getWidth(), 2 * label.getFont().getSize());
    }
    else {
      maxSize.setSize(maxSize.getWidth(), 2 * textField.getFont().getSize());
    }
    textField.setMaximumSize(maxSize);
  }

  public boolean isBoolean() {
    return false;
  }

  public boolean isText() {
    return true;
  }

  void setToolTipText(String text) {
    textField.setToolTipText(TooltipFormatter.INSTANCE.format(text));
  }

  public SwingComponent getUIComponent() {
    return this;
  }

  public Component getComponent() {
    return textField;
  }

  void setAlignmentX(float alignmentX) {
    textField.setAlignmentX(alignmentX);
  }

  void setEnabled(boolean enabled) {
    textField.setEnabled(enabled);
    if (enabled) {
      updateFieldHighlight();
    }
  }

  void setEditable(boolean editable) {
    textField.setEditable(editable);
  }

  /**
   * @param alwaysCheck - when false return false when the field is disabled or invisible
   * @return true if text field is different from checkpoint
   */
  public boolean isDifferentFromCheckpoint(final boolean alwaysCheck) {
    if (!alwaysCheck && (!textField.isEnabled() || !textField.isVisible())) {
      return false;
    }
    return checkpoint == null || !checkpoint.equals(getText(), fieldType);
  }

  public void backup() {
    backupValue = textField.getText();
  }

  /**
   * If the field was backed up, make the backup value the displayed value, and turn off
   * the back up.
   */
  public void restoreFromBackup() {
    if (fieldIsBackedUp) {
      setText(backupValue);
      fieldIsBackedUp = false;
    }
  }

  public void clear() {
    setText("");
  }

  public void copy(final Field copyFrom) {
    if (copyFrom == null) {
      return;
    }
    setText(copyFrom.getText());
  }

  public boolean isSelected() {
    return false;
  }

  void setDirectiveDef(final DirectiveDef directiveDef) {
    this.directiveDef = directiveDef;
  }

  public DirectiveDef getDirectiveDef() {
    return directiveDef;
  }

  public void useDefaultValue() {
    if (directiveDef == null || !directiveDef.isComparam()) {
      return;
    }
    // only search for default value once
    if (defaultValue == null) {
      defaultValue = new FieldSetting();
      String value = DefaultFinder.INSTANCE.getDefaultValue(directiveDef);
      if (value != null) {
        // if default value has been found, set it in the field setting
        defaultValue.set(value);
      }
    }
    if (defaultValue.isSet()) {
      setText(defaultValue.getValue());
    }
  }

  public boolean equalsDefaultValue() {
    return defaultValue != null && defaultValue.equals(getText());
  }

  /**
   * Saves the current text as the checkpoint.
   */
  public void checkpoint() {
    if (checkpoint == null) {
      checkpoint = new FieldSetting();
    }
    checkpoint.set(getText());
  }

  public FieldSetting getCheckpoint() {
    return checkpoint;
  }

  public void setCheckpoint(final FieldSettingInterface input) {
    FieldSetting setting = input.getTextSetting();
    if (setting == null) {
      if (checkpoint != null) {
        checkpoint.reset();
      }
    }
    else {
      if (checkpoint == null) {
        checkpoint = new FieldSetting();
      }
      checkpoint.copy(setting);
    }
  }

  public void setFieldHighlightValue(final String value) {
    if (fieldHighlight == null) {
      fieldHighlight = new FieldSetting();
    }
    if (!fieldHighlight.isSet()) {
      textField.addFocusListener(this);
    }
    fieldHighlight.set(value);
    updateFieldHighlight();
  }

  public void setFieldHighlight(final FieldSettingInterface input) {
    FieldSetting setting = input.getTextSetting();
    if (setting == null || !setting.isSet()) {
      clearFieldHighlightValue();
    }
    else {
      if (fieldHighlight == null) {
        fieldHighlight = new FieldSetting();
      }
      if (!fieldHighlight.isSet()) {
        textField.addFocusListener(this);
      }
      fieldHighlight.copy(setting);
      updateFieldHighlight();
    }
  }

  public FieldSetting getFieldHighlight() {
    return fieldHighlight;
  }

  public void clearFieldHighlightValue() {
    if (fieldHighlight != null && fieldHighlight.isSet()) {
      fieldHighlight.reset();
      textField.removeFocusListener(this);
      updateFieldHighlight();
    }
  }

  public boolean equalsFieldHighlightValue() {
    return fieldHighlight != null && fieldHighlight.equals(isSelected());
  }

  public void focusGained(final FocusEvent event) {
  }

  public void focusLost(final FocusEvent event) {
    updateFieldHighlight();
  }

  /**
   * If the field highlight is in use, use the field highlight color on the foreground of
   * the text field if the value of the text field equals the field highlight value.  Save
   * the original foreground.  If the field highlight is in use and the value of the text
   * field does not equal the field highlight value, try to restore the original
   * foreground - or set a foreground color similar to the original one.  Assumes that
   * field highlight is not used when the field is disabled.
   */
  void updateFieldHighlight() {
    if (!textField.isEnabled()) {
      return;
    }
    if (fieldHighlight != null && fieldHighlight.isSet()) {
      String text = textField.getText();
      if (fieldHighlight.equals(text)) {
        if (origForeground == null) {
          origForeground = textField.getForeground();
          if (origForeground == null) {
            origForeground = Color.BLACK;
          }
        }
        textField.setForeground(Colors.FIELD_HIGHLIGHT);
      }
      return;
    }
    if (origForeground != null) {
      // field highlight has been turned off or field highlight doesn't match
      textField.setForeground(origForeground);
    }
  }

  public void setText(String text) {
    textField.setText(text);
  }

  void setRequired(final boolean required) {
    this.required = required;
  }

  /**
   * Validates and returns text in text field.  Should never throw a
   * FieldValidationFailedException when doValidation is false.
   * @param doValidation
   * @return
   * @throws FieldValidationFailedException
   */
  String getText(final boolean doValidation) throws FieldValidationFailedException {
    String text = textField.getText();
    if (doValidation && textField.isEnabled()) {
      text = FieldValidator.validateText(text, fieldType, this, getQuotedReference()
          + (locationDescr == null ? "" : " in " + locationDescr), required, false);
    }
    return text;
  }

  /**
   * get text without validation
   * @return
   */
  public String getText() {
    return textField.getText();
  }

  public boolean isEmpty() {
    String text = getText();
    return text == null || text.matches("\\s*");
  }

  private String getQuotedReference() {
    return Utilities.quoteLabel(reference);
  }

  Font getFont() {
    return textField.getFont();
  }

  Dimension getMaximumSize() {
    return textField.getMaximumSize();
  }

  void setMaximumSize(Dimension size) {
    textField.setMaximumSize(size);
  }

  void setTextPreferredWidth(final double minWidth) {
    Dimension prefSize = textField.getPreferredSize();
    prefSize.setSize(minWidth, prefSize.getHeight());
    textField.setPreferredSize(prefSize);
  }

  void setTextPreferredSize(Dimension size) {
    textField.setPreferredSize(size);
    textField.setMaximumSize(size);
  }

  void setSize(Dimension size) {
    textField.setSize(size);
  }

  Dimension getSize() {
    return textField.getSize();
  }

  Dimension getPreferredSize() {
    return textField.getPreferredSize();
  }

  String getName() {
    return textField.getName();
  }

  public boolean isEnabled() {
    return textField.isEnabled();
  }

  boolean isEditable() {
    return textField.isEditable();
  }

  boolean isVisible() {
    return textField.isVisible();
  }

  public String getQuotedLabel() {
    return Utilities.quoteLabel(label);
  }

  private void setName(String reference) {
    label = reference;
    String name = Utilities.convertLabelToName(reference);
    textField.setName(UITestFieldType.TEXT_FIELD.toString()
        + AutodocTokenizer.SEPARATOR_CHAR + name);
    if (EtomoDirector.INSTANCE.getArguments().isPrintNames()) {
      System.out.println(getName() + ' ' + AutodocTokenizer.DEFAULT_DELIMITER + ' ');
    }
  }
}
/**
 * <p> $Log$
 * <p> Revision 1.1  2010/11/13 16:07:34  sueh
 * <p> bug# 1417 Renamed etomo.ui to etomo.ui.swing.
 * <p>
 * <p> Revision 1.11  2010/03/03 05:08:06  sueh
 * <p> bug# 1311 Changed TextField.setSize to setPreferredSize.  Added getFont, getMaximumSize, setMaximumSize, and setSize.
 * <p>
 * <p> Revision 1.10  2009/11/20 17:37:52  sueh
 * <p> bug# 1282 Added prefixes to all of the field names, so that the fields that
 * <p> are actually abstract buttons (radio buttons, etc) won't be activated by a
 * <p> "bn." field command.
 * <p>
 * <p> Revision 1.9  2009/01/20 20:31:23  sueh
 * <p> bug# 1102 Changed UITestField to UITestFieldType.
 * <p>
 * <p> Revision 1.8  2008/05/30 22:36:39  sueh
 * <p> bug# 1102 Isolating the etomo.uitest package so it is not need for
 * <p> running EtomoDirector.
 * <p>
 * <p> Revision 1.7  2008/05/30 21:34:57  sueh
 * <p> bug# 1102 Moved uitest classes to etomo.uitest.
 * <p>
 * <p> Revision 1.6  2008/02/19 00:47:46  sueh
 * <p> bug# 1078 Added setTextPreferredWidth.
 * <p>
 * <p> Revision 1.5  2007/12/26 22:35:07  sueh
 * <p> bug# 1052 Moved argument handling from EtomoDirector to a separate class.
 * <p>
 * <p> Revision 1.4  2007/09/07 00:29:17  sueh
 * <p> bug# 989 Using a public INSTANCE to refer to the EtomoDirector singleton
 * <p> instead of getInstance and createInstance.
 * <p>
 * <p> Revision 1.3  2007/03/30 23:54:19  sueh
 * <p> bug# 964 Wrapping JTextField instead of inheriting it.  Added automatic sizing.
 * <p>
 * <p> Revision 1.2  2007/02/09 00:53:33  sueh
 * <p> bug# 962 Made TooltipFormatter a singleton and moved its use to low-level ui
 * <p> classes.
 * <p>
 * <p> Revision 1.1  2006/09/13 23:58:14  sueh
 * <p> bug# 924 Added TextField:  extends JTextField and automatically names itself.
 * <p> </p>
 */
