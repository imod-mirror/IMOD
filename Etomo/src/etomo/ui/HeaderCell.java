package etomo.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.plaf.ColorUIResource;

/**
* <p>Description: </p>
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
* <p> Revision 1.1.2.2  2004/10/13 23:08:37  sueh
* <p> bug# 520 Changed the add() functions.  No longer relying on the Table
* <p> interface because a FieldCell instance could be added to different panels.
* <p> The Add() functions remember what panel they where added to.  The
* <p> remove() requires no parameters.
* <p>
* <p> Revision 1.1.2.1  2004/10/01 19:56:55  sueh
* <p> bug# 520 A header designed designed to be used with a gridbag layout.
* <p> It can be used with any ui object with implements Table.  It is actually a
* <p> disabled button with bolded text and an etched border.  It uses
* <p> TableHeader colors.
* <p> </p>
*/
class HeaderCell {
  public static  final String  rcsid =  "$Id$";
  
  private static ColorUIResource foreground = null;
  private static ColorUIResource background = null;
  
  private JButton cell;
  private JPanel jpanelContainer = null;
  private String text = "";
  
  HeaderCell() {
    this(null, -1);
  }
  
  HeaderCell(String text) {
    this(text, -1);
  }
  
  HeaderCell(String text, int width) {
    initializeColor();
    if (text == null) {
      cell = new JButton();
    }
    else {
      String htmlText = "<html><b>" + text + "</b>";
      cell = new JButton(htmlText);
    }
    cell.setBorder(BorderFactory.createEtchedBorder());
    cell.setEnabled(false);
    cell.setForeground(foreground);
    cell.setBackground(background);
    if (width > 0) {
      Dimension size = cell.getPreferredSize();
      size.width = width;
      cell.setPreferredSize(size);
    }
  }
  
  void add(JPanel panel, GridBagLayout layout, GridBagConstraints constraints) {
    layout.setConstraints(cell, constraints);
    panel.add(cell);
    jpanelContainer = panel;
  }
  
  void remove() {
    if (jpanelContainer != null) {
      jpanelContainer.remove(cell);
      jpanelContainer = null;
    }
  }
  
  String getText() {
    return text;
  }
  
  void setText(String text) {
    this.text = text;
    String htmlText = "<html><b>" + text + "</b>";
    cell.setText(htmlText);
  }
  
  private void initializeColor() {
    if (foreground == null) {
      foreground = UIUtilities.getDefaultUIColor("TableHeader.foregroundvalue");
      if (foreground == null) {
        foreground = new ColorUIResource(0, 0, 0);
      }
    }
    if (background == null) {
      background = UIUtilities.getDefaultUIColor("TableHeader.backgroundvalue");
      if (background == null) {
        background = new ColorUIResource(204, 204, 204);
      }
    }
  }
}
