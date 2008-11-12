package etomo.ui;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ColorUIResource;

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
 * 
 * <p> $Log$ </p>
 */
final class RadioButtonCell extends InputCell implements ToggleCell {
  public static final String rcsid = "$Id$";

  private final RadioButton radioButton;

  RadioButtonCell(final ButtonGroup buttonGroup) {
    super();
    radioButton = new RadioButton(buttonGroup);
    radioButton.setBorderPainted(true);
    radioButton.setBorder(BorderFactory.createEtchedBorder());
    setBackground();
    setForeground();
    setFont();
  }

  private void setForeground() {
     radioButton.setForeground(Colors.CELL_FOREGROUND);
    setHtmlLabel(Colors.CELL_FOREGROUND);
  }

  private void setHtmlLabel(final ColorUIResource color) {
    radioButton.setText("<html><P style=\"font-weight:normal; color:rgb("
        + color.getRed() + "," + color.getGreen() + "," + color.getBlue()
        + ")\">" + unformattedLabel + "</style>");
  }
  
  public void setSelected(final boolean selected) {
    radioButton.setSelected(selected);
  }
  
  public int getWidth() {
    return radioButton.getWidth();
  }
  
  public int getHeight() {
    return radioButton.getHeight()
        + radioButton.getBorder().getBorderInsets(radioButton.getComponent()).bottom - 1;
  }
  
  public void setLabel(final String label) {
    unformattedLabel = label;
    setForeground();
  }
  
  void setToolTipText(String text) {
    radioButton.setToolTipText(TooltipFormatter.INSTANCE.format(text));
  }

  public void addActionListener(final ActionListener actionListener) {
    radioButton.addActionListener(actionListener);
  }
  
  public void addChangeListener(ChangeListener listener) {
    radioButton.addChangeListener(listener);
  }

  Component getComponent() {
    return radioButton.getComponent();
  }
  
  public void setEnabled(final boolean enabled) {
    setEditable(enabled);
  }

  public String getLabel() {
    return unformattedLabel;
  }

  private String unformattedLabel = "";

  public boolean isSelected() {
    return radioButton.isSelected();
  }

}
