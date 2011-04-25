package etomo.ui.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import etomo.EtomoDirector;
import etomo.storage.autodoc.AutodocTokenizer;
import etomo.type.EtomoNumber;
import etomo.type.UITestFieldType;
import etomo.util.Utilities;

/**
 * <p>Description: Check box and text field.  Text field is enabled only when
 * check box is checked.</p>
 * 
 * <p>Copyright: Copyright 2010</p>
 *
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 * 
 * <p> $Log$
 * <p> Revision 1.4  2011/04/04 17:17:27  sueh
 * <p> bug# 1416 Added savedValue, checkpoint, isChanged.
 * <p>
 * <p> Revision 1.3  2011/02/22 18:05:48  sueh
 * <p> bug# 1437 Reformatting.
 * <p>
 * <p> Revision 1.2  2010/12/05 04:58:39  sueh
 * <p> bug# 1416 Added setEnabled.
 * <p>
 * <p> Revision 1.1  2010/11/13 16:07:34  sueh
 * <p> bug# 1417 Renamed etomo.ui to etomo.ui.swing.
 * <p>
 * <p> Revision 1.2  2010/03/05 04:02:32  sueh
 * <p> bug# 1319 Added setLabel.
 * <p>
 * <p> Revision 1.1  2010/03/03 05:02:42  sueh
 * <p> bug# 1311 A checkbox which enables/disables a text field.
 * <p> </p>
 */
final class CheckTextField implements StateChangeActionAndDocumentSource {
  public static final String rcsid = "$Id$";

  private final JPanel pnlRoot = new JPanel();
  private final CheckBox checkBox;
  private final JTextField textField = new JTextField();
  private final String label;
  private final EtomoNumber.Type numericType;

  private String savedTextFieldValue = null;
  private EtomoNumber nSavedTextFieldValue = null;
  private StateChangedReporter reporter = null;

  private CheckTextField(final String label, final EtomoNumber.Type numericType) {
    this.label = label;
    this.numericType = numericType;
    checkBox = new CheckBox();
    setLabel(label);
  }

  static CheckTextField getInstance(final String label) {
    CheckTextField instance = new CheckTextField(label, null);
    instance.createPanel();
    instance.updateDisplay();
    instance.addListeners();
    return instance;
  }

  static CheckTextField getNumericInstance(final String tfLabel,
      final EtomoNumber.Type numericType) {
    return new CheckTextField(tfLabel, numericType);
  }

  public boolean equals(final Object object) {
    return object == checkBox || object == textField;
  }

  public boolean equals(final Document document) {
    return textField.getDocument() == document;
  }

  void setLabel(final String label) {
    checkBox.setText(label);
    String name = Utilities.convertLabelToName(label);
    textField.setName(UITestFieldType.TEXT_FIELD.toString()
        + AutodocTokenizer.SEPARATOR_CHAR + name);
    if (EtomoDirector.INSTANCE.getArguments().isPrintNames()) {
      System.out.println(textField.getName() + ' ' + AutodocTokenizer.DEFAULT_DELIMITER
          + ' ');
    }
  }

  /**
   * Checkpoints checkbox.  Saves the current state of the textfield.
   */
  void checkpoint() {
    checkBox.checkpoint();
    savedTextFieldValue = textField.getText();
    if (numericType != null) {
      if (nSavedTextFieldValue == null) {
        nSavedTextFieldValue = new EtomoNumber(numericType);
      }
      nSavedTextFieldValue.set(savedTextFieldValue);
    }
    reporter.msgCheckpointed(this);
  }

  /**
   * Calls isDifferentFromCheckpoint()
   */
  public boolean getState() {
    return isDifferentFromCheckpoint();
  }

  public void setReporter(StateChangedReporter reporter) {
    this.reporter = reporter;
  }

  /**
   * Returns true if a checkpoint was done, and either the checkbox is changed or (if the
   * checkbox is selected) the text field text has changed since the checkpoint.
   * @return
   */
  boolean isDifferentFromCheckpoint() {
    if (checkBox.isDifferentFromCheckpoint()) {
      return true;
    }
    if (!checkBox.isSelected() || savedTextFieldValue == null) {
      return false;
    }
    if (numericType == null) {
      return !savedTextFieldValue.equals(textField.getText());
    }
    return !nSavedTextFieldValue.equals(textField.getText());
  }

  void setEnabled(final boolean enable) {
    checkBox.setEnabled(enable);
    textField.setEnabled(enable);
  }

  private void createPanel() {
    //root panel
    pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.X_AXIS));
    pnlRoot.add(checkBox);
    pnlRoot.add(textField);
  }

  void setToolTipText(final String text) {
    String tooltip = TooltipFormatter.INSTANCE.format(text);
    pnlRoot.setToolTipText(tooltip);
    textField.setToolTipText(tooltip);
    checkBox.setToolTipText(text);
  }

  Component getRootComponent() {
    return pnlRoot;
  }

  private void addListeners() {
    checkBox.addActionListener(new CheckTextFieldActionListener(this));
  }

  public void addActionListener(ActionListener actionListener) {
    checkBox.addActionListener(actionListener);
  }

  public void addDocumentListener(DocumentListener listener) {
    textField.getDocument().addDocumentListener(listener);
  }

  void setText(final String input) {
    textField.setText(input);
  }

  String getLabel() {
    return label;
  }

  void setSelected(final boolean selected) {
    checkBox.setSelected(selected);
    updateDisplay();
  }

  boolean isSelected() {
    return checkBox.isSelected();
  }

  String getText() {
    return textField.getText();
  }

  void setTextPreferredWidth(int width) {
    Dimension size = textField.getSize();
    size.width = width;
    textField.setPreferredSize(size);
  }

  void setTextFieldVisible(boolean visible) {
    textField.setVisible(visible);
  }

  private void updateDisplay() {
    textField.setEnabled(checkBox.isSelected());
  }

  private void action() {
    updateDisplay();
  }

  private static final class CheckTextFieldActionListener implements ActionListener {
    private final CheckTextField checkTextField;

    private CheckTextFieldActionListener(final CheckTextField checkTextField) {
      this.checkTextField = checkTextField;
    }

    public void actionPerformed(final ActionEvent actionEvent) {
      checkTextField.action();
    }
  }
}
