package etomo.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.border.Border;

/**
* <p>Description: A box layout JPanel-like object that can either use X_AXIS or 
* Y_AXIS.  It places rigid areas outside the components on both the x axis and 
* the y axis.  It also places rigid areas between components.  It can be created
* with or without a border.  It is most useful for bordered panels, and some
* panels containing bordered panels.  It should work like a JPanel, except that
* layout and border are set during construction.</p>
* 
* <p>Copyright: Copyright (c) 2002, 2003, 2004</p>
*
*<p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
* University of Colorado</p>
* 
* @author $Author$
* 
* @version $Revision$
* 
* <p> $Log$
* <p> Revision 1.1.2.1  2004/09/23 23:36:12  sueh
* <p> bug# 520 A panel which has rigid areas on both the X axis and Y axis.
* <p> Useful with borders and panels containing panels with borders.
* <p> </p>
*/
public class DoubleSpacedPanel {
  public static final String rcsid = "$Id$";

  Dimension outerPixels;
  Dimension innerPixels;
  SpacedPanel innerPanel;
  SpacedPanel outerPanel;

  public DoubleSpacedPanel(boolean xAxisLayout, Dimension xPixels,
      Dimension yPixels) {
    this(xAxisLayout, xPixels, yPixels, null);
  }

  public DoubleSpacedPanel(boolean xAxisLayout, Dimension xPixels,
      Dimension yPixels, Border border) {
    if (xAxisLayout) {
      outerPixels = yPixels;
      innerPixels = xPixels;
      intialize(border, BoxLayout.Y_AXIS, BoxLayout.X_AXIS);
    }
    else {
      outerPixels = xPixels;
      innerPixels = yPixels;
      intialize(border, BoxLayout.X_AXIS, BoxLayout.Y_AXIS);
    }
  }

  private void intialize(Border border, int outerAxis, int innerAxis) {
    outerPanel = new SpacedPanel(outerPixels, true);
    innerPanel = new SpacedPanel(innerPixels, true);
    outerPanel.setLayout(new BoxLayout(outerPanel.getContainer(), outerAxis));
    if (border != null) {
      outerPanel.setBorder(border);
    }
    innerPanel.setLayout(new BoxLayout(innerPanel.getContainer(), innerAxis));
    outerPanel.add(innerPanel.getContainer());
  }

  public Component add(Component comp) {
    return innerPanel.add(comp);
  }

  public Component add(DoubleSpacedPanel doubleSpacedPanel) {
    return innerPanel.add(doubleSpacedPanel.getContainer());
  }

  public Component add(SpacedPanel spacedPanel) {
    return innerPanel.add(spacedPanel.getContainer());
  }

  public Container getContainer() {
    return outerPanel.getContainer();
  }
}