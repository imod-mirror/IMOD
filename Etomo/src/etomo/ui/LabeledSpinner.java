/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright (c) 2002, 2003</p>
 *
 *<p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 * 
 * <p> $Log$ </p>
 */
package etomo.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;

public class LabeledSpinner {
  public static final String rcsid = "$Id$";

  private JPanel panel = new JPanel();

  private JLabel label = new JLabel();

  private JSpinner spinner = new JSpinner();


  /**
   * @param spinner
   */
  public LabeledSpinner(String spinLabel, SpinnerModel model) {
    label.setText(spinLabel);
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(label);
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

  public Container getContainer() {
    return panel;
  }

  public String getLabel() {
    return label.getText();
  }
  
  public Object getValue() {
    return spinner.getValue();
  }

  public void setValue(Object value) {
    spinner.setValue(value);
  }

  public void setEnabled(boolean isEnabled) {
    spinner.setEnabled(isEnabled);
    label.setEnabled(isEnabled);
  }

  public boolean isEnabled() {
    return (spinner.isEnabled());
  }

  public void setVisible(boolean isVisible) {
    panel.setVisible(isVisible);
  }

  /**
   * Set the absolute preferred size of the text field
   * @param size
   */
  public void setTextPreferredSize(Dimension size) {
    spinner.setPreferredSize(size);
  }

  /**
   * Set the absolute maximum size of the text field
   * @param size
   */
  public void setTextMaxmimumSize(Dimension size) {
    spinner.setMinimumSize(size);
  }

  /**
   * Set the absolute preferred size of the panel
   * @param size
   */
  public void setPreferredSize(Dimension size) {
    panel.setPreferredSize(size);
  }

  /**
   * Set the absolute maximum size of the panel
   * @param size
   */
  public void setMaximumSize(Dimension size) {
    panel.setMaximumSize(size);
  }

  public Dimension getLabelPreferredSize() {
    return label.getPreferredSize();
  }
  
  public void setAlignmentX(float alignment) {
    if (alignment == Component.LEFT_ALIGNMENT) {
      label.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    if (alignment == Component.RIGHT_ALIGNMENT) {
      spinner.setAlignmentX(Component.RIGHT_ALIGNMENT);
    }
  }

  public void setToolTipText(String toolTipText) {
    panel.setToolTipText(toolTipText);
    spinner.setToolTipText(toolTipText);
  }

  public void addMouseListener(MouseListener listener) {
    panel.addMouseListener(listener);
    label.addMouseListener(listener);
    spinner.addMouseListener(listener);
  }
  
}
