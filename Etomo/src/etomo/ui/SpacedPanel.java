package etomo.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.border.Border;
/**
* <p>Description: A JPanel-like object that places rigid areas between 
* components.  It can optionally create rigid areas outside the components on
* the layout axis.  If rigid areas are needed outside the components on both
* axes, use DoubleSpacedPanel.  It is most useful for creating panels which
* would normally need rigid areas.  It should work like a JPanel.</p>
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
* <p> Revision 1.1.2.1  2004/09/23 23:41:40  sueh
* <p> bug# 520 Panel which automatically places rigid areas between
* <p> components.  Can choose whether to place rigid areas before and after
* <p> placing all components.
* <p> </p>
*/
public class SpacedPanel {
  public static  final String  rcsid =  "$Id$";
  
  Dimension spacing;
  boolean outerSpacing = false;
  JPanel panel;
  int numComponents = 0;
  
  public SpacedPanel(Dimension spacing) {
    this(spacing, false);
  }
  
  public SpacedPanel(Dimension spacing, boolean outerSpacing) {
    this.spacing = spacing;
    this.outerSpacing = outerSpacing;
    panel = new JPanel();
    if (outerSpacing) {
      panel.add(Box.createRigidArea(spacing));
    }
  }

  public Component add(Component comp) {
    numComponents++;
    if (!outerSpacing && numComponents > 1) {
      panel.add(Box.createRigidArea(spacing));
    }
    Component component = panel.add(comp);
    if (outerSpacing) {
      panel.add(Box.createRigidArea(spacing));
    }
    return component;
  }
  
  public Component add(SpacedPanel spacedPanel) {
    return add(spacedPanel.getContainer());
  }
  
  public Component add(DoubleSpacedPanel doubleSpacedPanel) {
    return add(doubleSpacedPanel.getContainer());
  }
  
  public void addRigidArea() {
    panel.add(Box.createRigidArea(spacing));
  }
  
  public void setLayout(LayoutManager mgr) {
    panel.setLayout(mgr);
  }
  
  public void setBorder(Border border) {
    panel.setBorder(border);
  }
  
  public Container getContainer() {
    return panel;
  }
}
