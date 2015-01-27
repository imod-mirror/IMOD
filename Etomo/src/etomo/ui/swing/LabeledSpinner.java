package etomo.ui.swing;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import etomo.EtomoDirector;
import etomo.logic.DefaultFinder;
import etomo.storage.DirectiveDef;
import etomo.storage.autodoc.AutodocTokenizer;
import etomo.type.ConstEtomoNumber;
import etomo.type.EtomoNumber;
import etomo.type.UITestFieldType;
import etomo.ui.FieldSettingInterface;
import etomo.ui.TextFieldSetting;
import etomo.ui.Field;
import etomo.util.Utilities;

/**
 * <p>Description: A spinner widget with a label.</p>
 * <p/>
 * <p>Copyright: Copyright 2002 - 2014 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 *          <p/>
 *          <p> $Log$
 *          <p> Revision 1.1  2010/11/13 16:07:34  sueh
 *          <p> bug# 1417 Renamed etomo.ui to etomo.ui.swing.
 *          <p>
 *          <p> Revision 1.28  2009/11/20 17:27:21  sueh
 *          <p> bug# 1282 Added prefixes to all of the field names, so that the fields that
 *          <p> are actually abstract buttons (radio buttons, etc) won't be activated by a
 *          <p> "bn." field command.
 *          <p>
 *          <p> Revision 1.27  2009/09/20 21:27:19  sueh
 *          <p> bug# 1268 Added a default value.  Set spinner value to default value in
 *          <p> setValue when parameter value is null.  Removed setValue(Object)
 *          <p> because it would be hard to tell if the parameter value was null.
 *          <p>
 *          <p> Revision 1.26  2009/09/01 03:18:24  sueh
 *          <p> bug# 1222
 *          <p>
 *          <p> Revision 1.25  2009/04/13 22:55:56  sueh
 *          <p> Removed newstuff.
 *          <p>
 *          <p> Revision 1.24  2009/02/27 03:53:01  sueh
 *          <p> bug# 1172 Added experimental automation recording background color
 *          <p> (newstuff only).
 *          <p>
 *          <p> Revision 1.23  2009/01/20 20:12:07  sueh
 *          <p> bug# 1102 Changed UITestField to UITestFieldType.  Simplified the name
 *          <p> by removing the expanded state portion.
 *          <p>
 *          <p> Revision 1.22  2008/05/30 22:32:01  sueh
 *          <p> bug# 1102 Isolating the etomo.uitest package so it is not need for
 *          <p> running EtomoDirector.
 *          <p>
 *          <p> Revision 1.21  2008/05/30 21:31:24  sueh
 *          <p> bug# 1102 Moved uitest classes to etomo.uitest.
 *          <p>
 *          <p> Revision 1.20  2007/12/26 22:24:29  sueh
 *          <p> bug# 1052 Moved argument handling from EtomoDirector to a separate class.
 *          <p>
 *          <p> Revision 1.19  2007/09/07 00:27:11  sueh
 *          <p> bug# 989 Using a public INSTANCE to refer to the EtomoDirector singleton
 *          <p> instead of getInstance and createInstance.
 *          <p>
 *          <p> Revision 1.18  2007/05/01 22:28:09  sueh
 *          <p> bug# 964 Added setMax(int).
 *          <p>
 *          <p> Revision 1.17  2007/03/01 01:39:08  sueh
 *          <p> bug# 964 Moved colors from UIUtilities to Colors.
 *          <p>
 *          <p> Revision 1.16  2007/02/09 00:50:18  sueh
 *          <p> bug# 962 Made TooltipFormatter a singleton and moved its use to low-level ui
 *          <p> classes.
 *          <p>
 *          <p> Revision 1.15  2007/02/05 23:39:32  sueh
 *          <p> bug# 962 Added setHighlight.
 *          <p>
 *          <p> Revision 1.14  2006/11/07 22:44:13  sueh
 *          <p> bug# 954 Added tooltip to label.
 *          <p>
 *          <p> Revision 1.13  2006/04/25 19:15:21  sueh
 *          <p> bug# 787 Added UITestField, an enum style class which contains the
 *          <p> fields found in uitestaxis.adoc files.
 *          <p>
 *          <p> Revision 1.12  2006/04/06 20:16:56  sueh
 *          <p> bug# 808 Moved the function convertLabelToName from UIUtilities to
 *          <p> util.Utilities.
 *          <p>
 *          <p> Revision 1.11  2006/01/12 17:11:29  sueh
 *          <p> bug# 798 Reducing the visibility and inheritability of ui classes.
 *          <p>
 *          <p> Revision 1.10  2006/01/11 22:11:10  sueh
 *          <p> bug# 675 corrected print name functionality
 *          <p>
 *          <p> Revision 1.9  2006/01/04 20:25:51  sueh
 *          <p> bug# 675 For printing the name:  putting the type first and making the type
 *          <p> as constant.
 *          <p>
 *          <p> Revision 1.8  2005/12/23 02:15:26  sueh
 *          <p> bug# 675 Named the spinner so it can be found by JfcUnit.
 *          <p>
 *          <p> Revision 1.7  2005/06/13 23:37:15  sueh
 *          <p> bug# 675 Added a setName() call to the constructor to try out jfcUnit.
 *          <p>
 *          <p> Revision 1.6  2005/01/14 23:05:20  sueh
 *          <p> Passing back Number instead of Object from getValue().
 *          <p>
 *          <p> Revision 1.5  2004/11/19 23:57:05  sueh
 *          <p> bug# 520 merging Etomo_3-4-6_JOIN branch to head.
 *          <p>
 *          <p> Revision 1.4.4.4  2004/11/16 02:29:10  sueh
 *          <p> bug# 520 Replacing EtomoSimpleType, EtomoInteger, EtomoDouble,
 *          <p> EtomoFloat, and EtomoLong with EtomoNumber.
 *          <p>
 *          <p> Revision 1.4.4.3  2004/10/22 21:08:45  sueh
 *          <p> bug# 520 Using EtomoSimpleType where possible.
 *          <p>
 *          <p> Revision 1.4.4.2  2004/10/22 03:27:16  sueh
 *          <p> bug# 520 Added setValue(ConstEtomoInteger).
 *          <p>
 *          <p> Revision 1.4.4.1  2004/09/23 23:38:18  sueh
 *          <p> bug# 520 Added setModel() so that the spinner model can be changed.
 *          <p>
 *          <p> Revision 1.4  2004/04/07 21:04:02  rickg
 *          <p> Alignment is now set on the panel
 *          <p>
 *          <p> Revision 1.3  2004/03/24 03:04:56  rickg
 *          <p> Fixed setMaximumSize bug
 *          <p>
 *          <p> Revision 1.2  2004/03/13 00:32:11  rickg
 *          <p> New setValue(int) method
 *          <p>
 *          <p> Revision 1.1  2004/02/05 04:37:15  rickg
 *          <p> Initial revision
 *          <p> </p>
 */
final class LabeledSpinner implements Field, ChangeListener, FocusListener {
  private final JPanel panel = new JPanel();
  private final JLabel label = new JLabel();
  private final JSpinner spinner = new JSpinner();

  private final Integer defaultValue;
  private SpinnerNumberModel model;
  private int minimum;
  private int maximum;

  private Color origLabelForeground = null;
  private Color origTextForeground = null;
  private DirectiveDef directiveDef = null;
  private TextFieldSetting backup = null;
  private TextFieldSetting defaultValueSetting = null;
  private TextFieldSetting checkpoint = null;
  private TextFieldSetting fieldHighlight = null;

  private LabeledSpinner(final String spinLabel, int value, int minimum, int maximum,
      int stepSize, final int defaultValue, final int hgap) {
    this.defaultValue = new Integer(defaultValue);
    this.minimum = minimum;
    this.maximum = maximum;
    model = new SpinnerNumberModel(value, minimum, maximum, stepSize);
    // set name
    String name = Utilities.convertLabelToName(spinLabel);
    spinner.setName(
        UITestFieldType.SPINNER.toString() + AutodocTokenizer.SEPARATOR_CHAR + name);
    if (EtomoDirector.INSTANCE.getArguments().isPrintNames()) {
      System.out
          .println(spinner.getName() + ' ' + AutodocTokenizer.DEFAULT_DELIMITER + ' ');
    }
    // set label
    label.setText(spinLabel);
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(label);
    if (hgap > 0) {
      panel.add(Box.createRigidArea(new Dimension(hgap, 0)));
    }
    panel.add(spinner);
    spinner.setModel(model);

    // Set the maximum height of the text field box to twice the
    // font size since it is not set by default
    Dimension maxSize = spinner.getMaximumSize();
    if (label.getFont().getSize() > spinner.getFont().getSize()) {
      maxSize.setSize(maxSize.getWidth(), 2 * label.getFont().getSize());
    }
    else {
      maxSize.setSize(maxSize.getWidth(), 2 * spinner.getFont().getSize());
    }
    spinner.setMaximumSize(maxSize);
  }

  static LabeledSpinner getDefaultedInstance(final String spinLabel, final int value,
      final int minimum, final int maximum, final int stepSize, final int defaultValue) {
    return new LabeledSpinner(spinLabel, value, minimum, maximum, stepSize, defaultValue,
        0);
  }

  static LabeledSpinner getInstance(final String spinLabel, final int value,
      final int minimum, final int maximum, final int stepSize) {
    return new LabeledSpinner(spinLabel, value, minimum, maximum, stepSize, value, 0);
  }

  static LabeledSpinner getInstance(final String spinLabel, final int value,
      final int minimum, final int maximum, final int stepSize, final int hgap) {
    return new LabeledSpinner(spinLabel, value, minimum, maximum, stepSize, value, hgap);
  }

  public boolean isText() {
    return true;
  }

  public boolean isBoolean() {
    return false;
  }

  void setMax(final int max) {
    maximum = max;
    model.setMaximum(new Integer(max));
  }

  void setModel(final int value, final int minimum, final int maximum,
      final int stepSize) {
    this.minimum = minimum;
    this.maximum = maximum;
    model = new SpinnerNumberModel(value, minimum, maximum, stepSize);
    spinner.setModel(model);
  }

  Container getContainer() {
    return panel;
  }

  String getLabel() {
    return label.getText();
  }

  public String getQuotedLabel() {
    return Utilities.quoteLabel(getLabel());
  }

  public void checkpoint() {
    if (checkpoint == null) {
      checkpoint = new TextFieldSetting(EtomoNumber.Type.INTEGER);
    }
    checkpoint.set(getText());
  }

  public TextFieldSetting getCheckpoint() {
    return checkpoint;
  }

  public void setCheckpoint(final FieldSettingInterface input) {
    if (checkpoint == null && input != null && input.isSet() && input.isText()) {
      checkpoint = new TextFieldSetting(EtomoNumber.Type.INTEGER);
    }
    if (checkpoint != null) {
      checkpoint.copy(input);
    }
  }

  public void backup() {
    if (backup == null) {
      backup = new TextFieldSetting(EtomoNumber.Type.INTEGER);
    }
    backup.set(getValue());
  }

  void setDirectiveDef(final DirectiveDef directiveDef) {
    this.directiveDef = directiveDef;
  }

  public DirectiveDef getDirectiveDef() {
    return directiveDef;
  }

  public boolean equalsDefaultValue() {
    return defaultValueSetting != null && defaultValueSetting.equals(getText());
  }

  public void useDefaultValue() {
    if (directiveDef == null || !directiveDef.isComparam()) {
      if (defaultValueSetting != null && defaultValueSetting.isSet()) {
        defaultValueSetting.reset();
      }
      return;
    }
    // only search for default value once
    if (defaultValueSetting == null) {
      defaultValueSetting = new TextFieldSetting(EtomoNumber.Type.INTEGER);
      String value = DefaultFinder.INSTANCE.getDefaultValue(directiveDef);
      if (value != null) {
        // if default value has been found, set it in the field setting
        defaultValueSetting.set(value);
      }
    }
    if (defaultValueSetting.isSet()) {
      setText(defaultValueSetting.getValue());
    }
  }

  /**
   * If the field was backed up, make the backup value the displayed value, and turn off
   * the back up.
   */
  public void restoreFromBackup() {
    if (backup != null && backup.isSet()) {
      setValue(backup.getValue());
      backup.reset();
    }
  }

  public void clear() {
    spinner.setValue(minimum);
  }

  public void setValue(final Field input) {
    if (input == null) {
      clear();
    }
    else {
      setText(input.getText());
    }
  }

  public boolean isSelected() {
    return false;
  }

  public boolean isEmpty() {
    String text = getText();
    return text == null || text.matches("\\s*");
  }

  public String getText() {
    return getValue().toString();
  }

  public boolean isFieldHighlightSet() {
    return fieldHighlight != null && fieldHighlight.isSet();
  }

  public boolean equalsFieldHighlight() {
    return fieldHighlight != null && fieldHighlight.equals(getText());
  }

  /**
   * Creates and sets the field highlight
   * @param value
   */
  public void setFieldHighlight(final String value) {
    if (fieldHighlight == null && value != null) {
      fieldHighlight = new TextFieldSetting(EtomoNumber.Type.INTEGER);
      spinner.addChangeListener(this);
      spinner.addFocusListener(this);
    }
    fieldHighlight.set(value);
    updateFieldHighlight();
  }

  public void setFieldHighlight(final boolean value) {
  }

  public void clearFieldHighlight() {
    if (fieldHighlight != null && fieldHighlight.isSet()) {
      fieldHighlight.reset();
      updateFieldHighlight();
    }
  }

  public TextFieldSetting getFieldHighlight() {
    return fieldHighlight;
  }

  public void setFieldHighlight(final FieldSettingInterface input) {
    if (fieldHighlight == null && input != null && input.isSet() && input.isText()) {
      fieldHighlight = new TextFieldSetting(EtomoNumber.Type.INTEGER);
      spinner.addChangeListener(this);
      spinner.addFocusListener(this);
    }
    if (fieldHighlight != null) {
      fieldHighlight.copy(input);
      updateFieldHighlight();
    }
  }

  public void clearFieldHighlightValue() {
    fieldHighlight.reset();
    updateFieldHighlight();
  }

  public void stateChanged(ChangeEvent e) {
    updateFieldHighlight();
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
    // To avoid constantly updating the foreground color, assuming that fieldHighlight is
    // never reassigned to null
    if (fieldHighlight == null || !isEnabled()) {
      return;
    }
    if (fieldHighlight.isSet() && fieldHighlight.equals(getValue())) {
      // save the original color
      if (origTextForeground == null) {
        origTextForeground = spinner.getForeground();
        if (origTextForeground == null) {
          origTextForeground = Color.BLACK;
        }
      }
      if (origLabelForeground == null) {
        origLabelForeground = label.getForeground();
        if (origLabelForeground == null) {
          origLabelForeground = Color.BLACK;
        }
      }
      label.setForeground(Colors.FIELD_HIGHLIGHT);
      spinner.setForeground(Colors.FIELD_HIGHLIGHT);
    }
    else {
      if (origTextForeground != null) {
        spinner.setForeground(origTextForeground);
      }
      if (origLabelForeground != null) {
        label.setForeground(origLabelForeground);
      }
    }
  }

  /**
   * Resets to checkpointValue if checkpointValue has been set.  Otherwise has no effect.
   */
  void resetToCheckpoint() {
    if (checkpoint == null || !checkpoint.isSet()) {
      return;
    }
    setText(checkpoint.getValue());
  }

  /**
   * @param alwaysCheck - check for difference even when the field is disables or
   *                    invisible
   * @return
   */
  public boolean isDifferentFromCheckpoint(final boolean alwaysCheck) {
    if (!alwaysCheck && (!isEnabled() || !isVisible())) {
      return false;
    }
    return checkpoint == null || !checkpoint.equals(getValue());
  }

  Number getValue() {
    return (Number) spinner.getValue();
  }

  /**
   * @param number
   * @return true if value empty or >= min and <= max (if max set)
   */
  boolean isInRange(final ConstEtomoNumber number) {
    if (number == null || number.isNull()) {
      return true;
    }
    return number.ge(minimum) && (maximum <= minimum || number.le(maximum));
  }

  void setValue(final ConstEtomoNumber value) {
    if (value.isNull()) {
      spinner.setValue(defaultValue);
    }
    else {
      spinner.setValue(value.getNumber());
    }
  }

  public void setText(final String value) {
    setValue(value);
  }

  public void setValue(final String value) {
    if (value == null || value.matches("\\s*")) {
      spinner.setValue(defaultValue);
    }
    else {
      EtomoNumber number = new EtomoNumber();
      number.set(value);
      setValue(number);
    }
  }

  public void setValue(final boolean value) {
  }

  void setValue(final int value) {
    if (value == EtomoNumber.INTEGER_NULL_VALUE) {
      spinner.setValue(defaultValue);
    }
    else {
      spinner.setValue(new Integer(value));
    }
  }

  void setEnabled(final boolean isEnabled) {
    spinner.setEnabled(isEnabled);
    label.setEnabled(isEnabled);
    if (isEnabled) {
      updateFieldHighlight();
    }
  }

  public boolean isEnabled() {
    return (spinner.isEnabled());
  }

  boolean isVisible() {
    return panel.isVisible();
  }

  void setVisible(final boolean isVisible) {
    panel.setVisible(isVisible);
  }

  void setHighlight(final boolean highlight) {
    JFormattedTextField textField = getTextField();
    if (highlight) {
      textField.setBackground(Colors.HIGHLIGHT_BACKGROUND);
    }
    else {
      textField.setBackground(Colors.BACKGROUND);
    }
  }

  private JFormattedTextField getTextField() {
    return ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
  }

  /**
   * Set the absolute preferred size of the text field
   *
   * @param size
   */
  void setTextPreferredSize(final Dimension size) {
    spinner.setPreferredSize(size);
  }

  /**
   * Set the absolute maximum size of the text field
   *
   * @param size
   */
  void setTextMaxmimumSize(final Dimension size) {
    spinner.setMaximumSize(size);
  }

  void setPreferredWidth(final int width) {
    Dimension dim = spinner.getPreferredSize();
    dim.width = width * (int) Math.round(UIParameters.getInstance().getFontSizeAdjustment());
    spinner.setPreferredSize(dim);
    spinner.setMaximumSize(dim);
  }

  /**
   * Set the absolute maximum size of the panel
   *
   * @param size
   */
  void setMaximumSize(final Dimension size) {
    panel.setMaximumSize(size);
  }

  Dimension getLabelPreferredSize() {
    return label.getPreferredSize();
  }

  void setAlignmentX(final float alignment) {
    panel.setAlignmentX(alignment);
  }

  void setToolTipText(final String text) {
    String tooltip = TooltipFormatter.INSTANCE.format(text);
    panel.setToolTipText(tooltip);
    spinner.setToolTipText(tooltip);
    getTextField().setToolTipText(tooltip);
    label.setToolTipText(tooltip);
  }

  void addMouseListener(final MouseListener listener) {
    panel.addMouseListener(listener);
    label.addMouseListener(listener);
    spinner.addMouseListener(listener);
  }

  void addChangeListener(final ChangeListener listener) {
    spinner.addChangeListener(listener);
  }
}
