package etomo.ui;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
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
public class FieldCell {
  public static  final String  rcsid =  "$Id$";
  
  private static ColorUIResource foreground = null;
  private static ColorUIResource notInUseForeground = null;
  private static ColorUIResource background = null;
  private static ColorUIResource disabledBackground = null;
  private static ColorUIResource highlightedBackground = null;
  private static ColorUIResource disabledhighlightedBackground = null;
  private static ColorUIResource headerBackground = null;
  
  private JTextField cell;
  private Table table;
  boolean inUse = true;
  boolean enabled = true;
  boolean highlighted = false;
  
  FieldCell(Table table) {
    initializeColor();
    this.table = table;
    cell = new JTextField();
    cell.setBorder(BorderFactory.createEtchedBorder());
    setColor();
  }
  
  void add() {
    table.addCell(cell);
  }
  
  void remove() {
    table.removeCell(cell);
  }
  
  void setText(String text) {
    cell.setText(text);
  }
  
  String getText() {
    return cell.getText();
  }
  
  void setEnabled(boolean enabled) {
    this.enabled = enabled;
    cell.setEnabled(enabled);
    setColor();
  }
  
  void setHighlighted(boolean highlighted) {
    this.highlighted = highlighted;
    setColor();
  }
  
  void setInUse(boolean inUse) {
    this.inUse = inUse;
    setColor();
  }
    
  private void setColor() {
    if (highlighted) {
      if (enabled) {
        cell.setBackground(highlightedBackground);
      }
      else {
        cell.setBackground(disabledhighlightedBackground);
      }
    }
    else if (enabled) {
      cell.setBackground(background);
    }
    else {
      cell.setBackground(disabledBackground);
    }
    if (inUse) {
      cell.setForeground(foreground);
      cell.setDisabledTextColor(foreground);
    }
    else {
      cell.setForeground(notInUseForeground);
      cell.setDisabledTextColor(notInUseForeground);
    }
  }
  
  private void initializeColor() {
    if (foreground == null) {
      foreground = UIUtilities.getDefaultUIColor("ToggleButton.foregroundvalue");
      if (foreground == null) {
        foreground = new ColorUIResource(0, 0, 0);
      }
    }
    if (notInUseForeground == null) {
      notInUseForeground = UIUtilities.getDefaultUIColor("ToggleButton.disabledSelectedTextvalue");
      if (notInUseForeground == null) {
        notInUseForeground = new ColorUIResource(102, 102, 102);
      }
    }
    if (background == null) {
      background = UIUtilities.getDefaultUIColor("Table.backgroundvalue");
      if (background == null) {
        background = new ColorUIResource(255, 255, 255);
      }
    }
    if (highlightedBackground == null) {
      highlightedBackground = UIUtilities.getDefaultUIColor("Table.selectionBackgroundvalue");
      if (highlightedBackground == null) {
        highlightedBackground = new ColorUIResource(204, 255, 255);
      }
    }
    
    if (headerBackground == null) {
      headerBackground = UIUtilities.getDefaultUIColor("TableHeader.backgroundvalue");
      if (headerBackground == null) {
        headerBackground = new ColorUIResource(204, 204, 204);
      }
    }
    int backgroundRed = background.getRed();
    int backgroundGreen = background.getGreen();
    int backgroundBlue = background.getBlue();
    int red = backgroundRed - headerBackground.getRed();
    int green = backgroundGreen - headerBackground.getGreen();
    int blue = backgroundBlue - headerBackground.getBlue();
    int greyoutValue = (red + green + blue) / 6;
    
    if (disabledBackground == null) {
      disabledBackground = new ColorUIResource(backgroundRed - greyoutValue,
          backgroundGreen - greyoutValue, backgroundBlue - greyoutValue);
    }
    if (disabledhighlightedBackground == null) {
      disabledhighlightedBackground = new ColorUIResource(highlightedBackground
          .getRed()
          - greyoutValue, highlightedBackground.getGreen() - greyoutValue,
          highlightedBackground.getBlue() - greyoutValue);
    }

  }
}
