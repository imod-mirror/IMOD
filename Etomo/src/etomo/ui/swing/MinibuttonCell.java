package etomo.ui.swing;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.border.BevelBorder;

import etomo.type.UITestFieldType;

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
public final class MinibuttonCell extends InputCell {
  public static final String rcsid = "$Id:$";

  private final Minibutton button;

  MinibuttonCell(final Icon icon) {
    button = Minibutton.getSquareInstance(icon,BorderFactory.createBevelBorder(BevelBorder.RAISED));
  }

  Component getComponent() {
    return button;
  }

  UITestFieldType getFieldType() {
    return UITestFieldType.BUTTON;
  }

  int getWidth() {
    return button.getWidth();
  }

  public void setEnabled(boolean enable) {
    button.setEnabled(enable);
  }

  void setToolTipText(String text) {
    button.setToolTipText(TooltipFormatter.INSTANCE.format(text));
  }

}
