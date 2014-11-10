package etomo.ui.swing;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;

import etomo.storage.DirectiveDef;
import etomo.type.ConstEtomoNumber;
import etomo.type.EnumeratedType;
import etomo.type.ParsedElement;
import etomo.ui.Field;
import etomo.ui.FieldSettingBundle;
import etomo.ui.FieldSettingInterface;
import etomo.ui.FieldType;
import etomo.ui.FieldValidationFailedException;
import etomo.util.Utilities;

/**
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright 2006 - 2014 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 *          <p/>
 *          <p> $Log$
 *          <p> Revision 1.1  2010/11/13 16:07:34  sueh
 *          <p> bug# 1417 Renamed etomo.ui to etomo.ui.swing.
 *          <p>
 *          <p> Revision 1.10  2010/03/03 05:06:31  sueh
 *          <p> bug# 1311 Changed TextField.setSize to setPreferredSize.
 *          <p>
 *          <p> Revision 1.9  2009/11/20 17:31:49  sueh
 *          <p> bug# 1282 Changed validate to handle the name prefixes.
 *          <p>
 *          <p> Revision 1.8  2009/09/01 03:18:25  sueh
 *          <p> bug# 1222
 *          <p>
 *          <p> Revision 1.7  2009/02/19 01:45:50  sueh
 *          <p> bug# 1178 In setToolTipText stop formatting tooltip twice.
 *          <p>
 *          <p> Revision 1.6  2009/02/19 01:44:19  sueh
 *          <p> bug# 1178 In setToolTipText stop formatting tooltip twice.
 *          <p>
 *          <p> Revision 1.5  2008/11/20 01:46:57  sueh
 *          <p> bug# 1147 Commented out code that was not in use.
 *          <p>
 *          <p> Revision 1.4  2007/04/13 20:38:46  sueh
 *          <p> bug# 964Added EnumeratedType, which is the interface for enumeration types.
 *          <p>
 *          <p> Revision 1.3  2007/03/30 23:52:28  sueh
 *          <p> bug# 964 Switched from JTextField to etomo.ui.TextField, which names itself.
 *          <p>
 *          <p> Revision 1.2  2007/03/07 21:13:18  sueh
 *          <p> bug# 981 Turned RadioButton into a wrapper rather then a child of JRadioButton,
 *          <p> because it is getting more complicated.  Added radioValue - a way to assign an
 *          <p> integer value to each radio button in a group.
 *          <p>
 *          <p> Revision 1.1  2007/03/03 01:05:57  sueh
 *          <p> bug# 973 Class combining a RadioButton and JTextField.  Turns off the
 *          <p> JTextField when the radio button is not selected.
 *          <p> </p>
 */
final class RadioTextField implements RadioButtonInterface, Field {
  private final JPanel rootPanel = new JPanel();
  private final RadioButton radioButton;
  private final TextField textField;

  private boolean debug = false;
  private DirectiveDef directiveDef = null;

  /**
   * Constructs local instance, adds listener, and returns.
   *
   * @param label
   * @param group
   * @return
   */
  static RadioTextField getInstance(final FieldType fieldType, final String label,
      final ButtonGroup group) {
    RadioTextField radioTextField = new RadioTextField(fieldType, label, group, null);
    radioTextField.addListeners();
    return radioTextField;
  }

  static RadioTextField getInstance(final FieldType fieldType, final String label,
      final ButtonGroup group, String locationDescr) {
    RadioTextField radioTextField =
        new RadioTextField(fieldType, label, group, locationDescr);
    radioTextField.addListeners();
    return radioTextField;
  }

  private RadioTextField(final FieldType fieldType, final String label,
      final ButtonGroup group, final String locationDescr) {
    radioButton = new RadioButton(label);
    textField = new TextField(fieldType, label, locationDescr);
    init(group);
  }

  private void init(final ButtonGroup group) {
    radioButton.setModel(new RadioButton.RadioButtonModel(this));
    group.add(radioButton.getAbstractButton());
    rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.X_AXIS));
    rootPanel.add(radioButton.getComponent());
    rootPanel.add(textField.getComponent());
    setTextFieldEnabled();
  }

  public boolean isText() {
    return true;
  }

  public boolean isBoolean() {
    return true;
  }

  void setTextPreferredWidth(final double minWidth) {
    Dimension prefSize = textField.getPreferredSize();
    prefSize.setSize(minWidth, prefSize.getHeight());
    textField.setTextPreferredSize(prefSize);
  }

  Container getContainer() {
    return rootPanel;
  }

  void setText(final int value) {
    textField.setText(String.valueOf(value));
  }

  void setText(final long value) {
    textField.setText(String.valueOf(value));
  }

  void setText(final double value) {
    textField.setText(String.valueOf(value));
  }

  void setText(final ParsedElement value) {
    if (value == null) {
      textField.setText("");
    }
    else {
      textField.setText(value.getRawString());
    }
  }

  public void setText(final String text) {
    textField.setText(text);
    if (debug) {
      System.out.println("RadioTextField:setText:text:" + text);
      Thread.dumpStack();
    }
  }

  public void backup() {
    radioButton.backup();
    textField.backup();
  }

  /**
   * If a field was backed up, make the backup value the displayed value, and turn off
   * the back up.  This has no effect on a radio button with a backupValue of false,
   * other then to turn off the backup.
   */
  public void restoreFromBackup() {
    radioButton.restoreFromBackup();
    textField.restoreFromBackup();
  }

  public void clear() {
    radioButton.clear();
    textField.clear();
  }

  public void setValue(final Field input) {
    radioButton.setValue(input);
    textField.setValue(input);
  }

  public void setValue(final String value) {
    textField.setValue(value);
  }

  public void setValue(final boolean value) {
    radioButton.setValue(value);
  }

  void setDirectiveDef(final DirectiveDef directiveDef) {
    this.directiveDef = directiveDef;
  }

  public void useDefaultValue() {
    radioButton.useDefaultValue();
    textField.useDefaultValue();
  }

  public boolean equalsDefaultValue() {
    return radioButton.equalsDefaultValue() && textField.equalsDefaultValue();
  }

  public DirectiveDef getDirectiveDef() {
    return directiveDef;
  }

  public boolean isFieldHighlightSet() {
    return radioButton.isFieldHighlightSet() || textField.isFieldHighlightSet();
  }

  public void setFieldHighlight(final String text) {
    textField.setFieldHighlight(text);
  }

  public void setFieldHighlight(final boolean bool) {
    radioButton.setFieldHighlight(bool);
  }

  public FieldSettingInterface getFieldHighlight() {
    FieldSettingBundle bundle = new FieldSettingBundle();
    bundle.addBooleanSetting(radioButton.getFieldHighlight());
    bundle.addTextSetting(textField.getFieldHighlight());
    return bundle;
  }

  public void setFieldHighlight(final FieldSettingInterface input) {
    radioButton.setFieldHighlight(input);
    textField.setFieldHighlight(input);
  }

  public void clearFieldHighlight() {
    textField.clearFieldHighlight();
    radioButton.clearFieldHighlight();
  }

  public boolean equalsFieldHighlight() {
    return textField.equalsFieldHighlight() && radioButton.equalsFieldHighlight();
  }

  public void checkpoint() {
    radioButton.checkpoint();
    textField.checkpoint();
  }

  public void setCheckpoint(final FieldSettingInterface input) {
    radioButton.setCheckpoint(input);
    textField.setCheckpoint(input);
  }

  public FieldSettingInterface getCheckpoint() {
    FieldSettingBundle bundle = new FieldSettingBundle();
    bundle.addBooleanSetting(radioButton.getCheckpoint());
    bundle.addTextSetting(textField.getCheckpoint());
    return bundle;
  }

  public boolean isDifferentFromCheckpoint(final boolean alwaysCheck) {
    return radioButton.isDifferentFromCheckpoint(alwaysCheck) ||
        textField.isDifferentFromCheckpoint(alwaysCheck);
  }

  void setDebug(final boolean debug) {
    this.debug = debug;
  }

  void setText(final ConstEtomoNumber text) {
    textField.setText(text.toString());
  }

  String getLabel() {
    return radioButton.getText();
  }

  public String getQuotedLabel() {
    return Utilities.quoteLabel(getLabel());
  }

  void setRequired(final boolean required) {
    textField.setRequired(required);
  }

  String getText(final boolean doValidation) throws FieldValidationFailedException {
    String text = textField.getText(doValidation);
    if (text == null || text.matches("\\s*")) {
      return "";
    }
    return text;
  }

  /**
   * return text without validation
   *
   * @return
   */
  public String getText() {
    String text = textField.getText();
    if (text == null || text.matches("\\s*")) {
      return "";
    }
    return text;
  }

  public EnumeratedType getEnumeratedType() {
    return radioButton.getEnumeratedType();
  }

  public boolean isSelected() {
    return radioButton.isSelected();
  }

  public boolean isEmpty() {
    String text = textField.getText();
    return text == null || text.matches("\\s*");
  }

  void setEnabled(final boolean enable) {
    radioButton.setEnabled(enable);
    setTextFieldEnabled();
  }

  public boolean isEnabled() {
    return radioButton.isEnabled();
  }

  public void msgSelected() {
    setTextFieldEnabled();
  }

  void setSelected(boolean selected) {
    radioButton.setSelected(selected);
    if (debug) {
      System.out.println("RadioTextField:setSelected:selected:" + selected);
    }
  }

  void setToolTipText(final String text) {
    radioButton.setToolTipText(text);
    textField.setToolTipText(text);
  }

  void setRadioButtonToolTipText(final String text) {
    radioButton.setToolTipText(text);
  }

  void setTextFieldToolTipText(final String text) {
    textField.setToolTipText(text);
  }

  void addActionListener(ActionListener actionListener) {
    radioButton.addActionListener(actionListener);
  }

  String getActionCommand() {
    return radioButton.getActionCommand();
  }

  /**
   * @return null if instance is in a valid state
   */
  String validate() {
    if (!radioButton.getName().endsWith(textField.getName().substring(2))) {
      return "Fields should have the same name, except for the prefix";
    }
    if (!radioButton.isEnabled() && textField.isEnabled()) {
      return "Fields should enable and disable together";
    }
    if (!radioButton.isSelected() && textField.isEnabled()) {
      return "Text field should be disabled when radio button is not selected";
    }
    if (radioButton.isEnabled() && radioButton.isSelected() && !textField.isEnabled()) {
      return "text field should be enabled when radio button is selected";
    }
    return null;
  }

  private void setTextFieldEnabled() {
    textField.setEnabled(radioButton.isEnabled() && radioButton.isSelected());
  }

  private void addListeners() {
    radioButton.addActionListener(new RTFActionListener(this));
  }

  private void action(final ActionEvent actionEvent) {
    if (actionEvent.getActionCommand().equals(radioButton.getActionCommand())) {
      setTextFieldEnabled();
    }
  }

  private static final class RTFActionListener implements ActionListener {
    private final RadioTextField radioTextField;

    private RTFActionListener(final RadioTextField radioTextField) {
      this.radioTextField = radioTextField;
    }

    public void actionPerformed(final ActionEvent actionEvent) {
      radioTextField.action(actionEvent);
    }
  }
}
