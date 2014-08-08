package etomo.ui.swing;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.border.BevelBorder;

import etomo.type.UITestFieldType;
import etomo.ui.UIComponent;

/**
* <p>Description: </p>
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
public final class MinibuttonCell extends InputCell implements UIComponent,
    SwingComponent {
  public static final String rcsid = "$Id:$";

  private final Minibutton button;

  MinibuttonCell(final Icon icon) {
    button = Minibutton.getSquareInstance(icon,
        BorderFactory.createBevelBorder(BevelBorder.RAISED));
  }

  public Component getComponent() {
    return button;
  }

  public SwingComponent getUIComponent() {
    return this;
  }

  UITestFieldType getFieldType() {
    return UITestFieldType.BUTTON;
  }

  int getWidth() {
    return button.getWidth();
  }

  void addActionListener(final ActionListener listener) {
    button.addActionListener(listener);
  }

  public void setEnabled(boolean enable) {
    button.setEnabled(enable);
  }

  public void setDisabledIcon(final Icon icon) {
    button.setDisabledIcon(icon);
  }

  public void setPressedIcon(final Icon icon) {
    button.setPressedIcon(icon);
  }

  void setToolTipText(String text) {
    button.setToolTipText(TooltipFormatter.INSTANCE.format(text));
  }

  void setActionCommand(final String input) {
    button.setActionCommand(input);
  }

  String getActionCommand() {
    return button.getActionCommand();
  }
}
