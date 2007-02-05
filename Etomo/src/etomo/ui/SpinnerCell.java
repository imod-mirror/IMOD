package etomo.ui;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ColorUIResource;

import etomo.type.EtomoNumber;

/**
 * <p>Description: Table cell with an integer JSpinner</p>
 * 
 * <p>Copyright: Copyright (c) 2005</p>
 *
 *<p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 */
class SpinnerCell extends InputCell {
  public static final String rcsid = "$Id$";

  private final EtomoNumber disabledValue;
  private final EtomoNumber savedValue;
  private JSpinner spinner = null;
  private final EtomoNumber.Type type;

  public String toString() {
    return getTextField().getText();
  }

  static SpinnerCell getIntInstance(int min, int max) {
    return new SpinnerCell(min, max);
  }

  static SpinnerCell getLongInstance(long min, long max) {
    return new SpinnerCell(min, max);
  }

  void setEditable(boolean editable) {
    getTextField().setEditable(editable);
  }

  void setEnabled(boolean enabled) {
    if (this.enabled == enabled) {
      return;
    }
    super.setEnabled(enabled);
    if (!disabledValue.isNull()) {
      if (enabled) {
        if (disabledValue.equals((Number) spinner.getValue())
            && !savedValue.isNull()) {
          spinner.setValue(savedValue.getNumber());
        }
      }
      else {
        savedValue.set((Number) spinner.getValue());
        spinner.setValue(disabledValue.getNumber());
      }
    }
  }

  void setDisabledValue(int disabledValue) {
    this.disabledValue.set(disabledValue);
  }

  protected final Component getComponent() {
    return spinner;
  }

  final void setValue(int value) {
    setValue(new EtomoNumber(type).set(value).getNumber());
  }

  final void setValue(long value) {
    setValue(new EtomoNumber(type).set(value).getNumber());
  }

  final void setValue(String value) {
    setValue(new EtomoNumber(type).set(value).getNumber());
  }

  final int getIntValue() {
    return ((Integer) spinner.getValue()).intValue();
  }

  final long getLongValue() {
    return ((Long) spinner.getValue()).longValue();
  }

  final String getStringValue() {
    if (type == EtomoNumber.Type.INTEGER) {
      return String.valueOf(getIntValue());
    }
    if (type == EtomoNumber.Type.LONG) {
      return String.valueOf(getLongValue());
    }
    return null;
  }

  final int getWidth() {
    return spinner.getWidth();
  }

  final void addChangeListener(ChangeListener changeListener) {
    spinner.addChangeListener(changeListener);
  }

  final void removeChangeListener(ChangeListener changeListener) {
    spinner.removeChangeListener(changeListener);
  }

  void setToolTipText(String toolTipText) {
    TooltipFormatter tooltipFormatter = new TooltipFormatter();
    String tooltip = tooltipFormatter.setText(toolTipText).format();
    spinner.setToolTipText(tooltip);
    getTextField().setToolTipText(tooltip);
  }

  /**
   * This probably doesn't work.  Should use something like
   * UIUtilities.highlightJTextComponents.  Will need to control the background
   * color more carefully.
   */
  protected final void setBackground(ColorUIResource color) {
    getTextField().setBackground(color);
  }

  protected final void setForeground() {
    JFormattedTextField textField = getTextField();
    if (inUse) {
      textField.setForeground(foreground);
      textField.setDisabledTextColor(foreground);
    }
    else {
      textField.setForeground(notInUseForeground);
      textField.setDisabledTextColor(notInUseForeground);
    }
  }

  private SpinnerCell(int min, int max) {
    super();
    type = EtomoNumber.Type.INTEGER;
    disabledValue = new EtomoNumber();
    savedValue = new EtomoNumber();
    spinner = new JSpinner(new SpinnerNumberModel(min, min, max, 1));
    setBackground();
    setFont();
    setForeground();
    spinner.setBorder(BorderFactory.createEtchedBorder());
    getTextField().setHorizontalAlignment(JTextField.LEFT);
  }

  private SpinnerCell(long min, long max) {
    super();
    type = EtomoNumber.Type.LONG;
    disabledValue = new EtomoNumber(EtomoNumber.Type.LONG);
    savedValue = new EtomoNumber(EtomoNumber.Type.LONG);
    spinner = new JSpinner(new SpinnerNumberModel(new Long(min), new Long(min),
        new Long(max), new Long(1)));
    setBackground();
    setFont();
    setForeground();
    spinner.setBorder(BorderFactory.createEtchedBorder());
    getTextField().setHorizontalAlignment(JTextField.LEFT);
  }

  private final JFormattedTextField getTextField() {
    return ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
  }

  private final void setValue(Number value) {
    savedValue.set(value);
    if (enabled || disabledValue.isNull()) {
      spinner.setValue(value);
    }
  }
}
/**
 * <p> $Log$
 * <p> Revision 1.7  2006/10/16 22:53:13  sueh
 * <p> bug# 919  Added setToolTipText().
 * <p>
 * <p> Revision 1.6  2005/09/13 00:02:47  sueh
 * <p> bug# 532 Fixed bug in setEnabled() by return if nothing has to be done.
 * <p>
 * <p> Revision 1.5  2005/08/04 20:19:46  sueh
 * <p> bug# 532 added getWidth().
 * <p>
 * <p> Revision 1.4  2005/07/29 19:48:02  sueh
 * <p> bug# 692 Changed ConstEtomoNumber.getInteger() to getInt.
 * <p>
 * <p> Revision 1.3  2005/07/19 22:35:48  sueh
 * <p> bug# 532 changing the look of inUse == false to greyed out text.
 * <p> Changing the look of error == true to red background.
 * <p>
 * <p> Revision 1.2  2005/07/01 23:06:30  sueh
 * <p> bug# 619 added addChangeListener
 * <p>
 * <p> Revision 1.1  2005/07/01 21:24:32  sueh
 * <p> bug# 619 A writable table cell that extends InputCell and contains a
 * <p> JSpinner.  Only contains integers.
 * <p> </p>
 */
