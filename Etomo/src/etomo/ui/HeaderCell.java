package etomo.ui;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
* <p> $Log$ </p>
*/
class HeaderCell {
  public static  final String  rcsid =  "$Id$";
  
  private static ColorUIResource foreground = null;
  private static ColorUIResource background = null;
  
  private JButton cell;
  private Table table;
  
  HeaderCell(Table table) {
    this(table, null, -1);
  }
  
  HeaderCell(Table table, String text) {
    this(table, text, -1);
  }
  
  HeaderCell(Table table, String text, int width) {
    initializeColor();
    this.table = table;
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
  
  void add() {
    table.addCell(cell);
  }
  
  void remove() {
    table.removeCell(cell);
  }
  
  void setText(String text) {
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
