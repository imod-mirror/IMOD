package etomo.ui;

import java.awt.Color;

import javax.swing.plaf.ColorUIResource;

import etomo.util.Utilities;

/**
 * <p>Description: </p>
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
 * 
 * <p> $Log$
 * <p> Revision 1.5  2007/02/20 20:36:10  sueh
 * <p> bug# 964 Improved the colors.
 * <p>
 * <p> Revision 1.4  2007/02/19 22:00:41  sueh
 * <p> bug# 964 Added background color for PEET interface.
 * <p>
 * <p> Revision 1.3  2006/04/07 23:32:18  sueh
 * <p> bug# 846 Changing the background colors for java 1.5.
 * <p>
 * <p> Revision 1.2  2006/04/06 23:33:58  sueh
 * <p> bug# 844 Added colors for the join and the generic parallel processing
 * <p> windows.
 * <p>
 * <p> Revision 1.1  2005/04/16 01:54:54  sueh
 * <p> bug# 615 Class to hold static colors.
 * <p> </p>
 */
public final class Colors {
  public static final String rcsid = "$Id$";

  static final ColorUIResource CELL_FOREGROUND = new ColorUIResource(0, 0, 0);
  static final ColorUIResource CELL_NOT_IN_USE_FOREGROUND = new ColorUIResource(
      102, 102, 102);
  static final ColorUIResource CELL_ERROR_BACKGROUND = new ColorUIResource(255, 204, 204);
  static final ColorUIResource BACKGROUND = new ColorUIResource(255, 255, 255);
  static final ColorUIResource WARNING_BACKGROUND = new ColorUIResource(255,
      255, 204);
  static final ColorUIResource HIGHLIGHT_BACKGROUND = new ColorUIResource(204,
      255, 255);
  static final ColorUIResource FOREGROUND = new ColorUIResource(0, 0, 0);
  
  static final int java1_5Change = 17;//half the difference between 1.4 color and 1.5 color

  private static final ColorUIResource CELL_GREYOUT = new ColorUIResource(25, 25, 25);

  private static Color backgroundA = null;
  private static Color backgroundB = null;
  private static Color backgroundJoin = null;
  private static Color backgroundParallel = null;
  private static Color backgroundPeet = null;
  private static ColorUIResource cellDisabledBackground = null;
  private static ColorUIResource cellDisabledhighlightBackground=null;
  private static ColorUIResource cellDisabledWarningBackground=null;
  private static ColorUIResource cellDisabledErrorBackground=null;

  static Color getBackgroundA() {
    if (backgroundA == null) {
      backgroundA = getBackground(153, 179, 204);//saphire
    }
    return backgroundA;
  }

  static Color getBackgroundB() {
    if (backgroundB == null) {
      backgroundB = getBackground(153, 204, 179);//jade
    }
    return backgroundB;
  }

  static Color getBackgroundJoin() {
    if (backgroundJoin == null) {
      backgroundJoin = getBackground(179, 153, 204);//violet
    }
    return backgroundJoin;
  }

  static Color getBackgroundParallel() {
    if (backgroundParallel == null) {
      backgroundParallel = getBackground(166, 204, 153);//lime
    }
    return backgroundParallel;
  }

  static Color getBackgroundPeet() {
    if (backgroundPeet == null) {
      backgroundPeet = getBackground(166, 153, 204);//purple
    }
    return backgroundPeet;
  }

  private static Color getBackground(final int r, final int g, final int b) {
    int change = 0;
    if (Utilities.isJava1_5()) {
      change = java1_5Change;
    }
    Color background = new Color(r + change, g + change, b + change);
    return background;
  }

  static ColorUIResource getCellDisabledBackground() {
    if (cellDisabledBackground == null) {
      cellDisabledBackground = subtractColor(
          BACKGROUND, CELL_GREYOUT);
    }
    return cellDisabledBackground;
  }
  
  static ColorUIResource getCellDisabledhighlightBackground() {
    if (cellDisabledhighlightBackground == null) {
      cellDisabledhighlightBackground = subtractColor(HIGHLIGHT_BACKGROUND, CELL_GREYOUT);
    }
    return cellDisabledhighlightBackground;
  }
  
  static ColorUIResource getCellDisabledWarningBackground() {
    if (cellDisabledWarningBackground==null) {
      cellDisabledWarningBackground = subtractColor(WARNING_BACKGROUND, CELL_GREYOUT);
    }
    return cellDisabledWarningBackground;
  }
  
  static ColorUIResource getCellDisabledErrorBackground() {
    if(cellDisabledErrorBackground==null) {
      cellDisabledErrorBackground = subtractColor(CELL_ERROR_BACKGROUND, CELL_GREYOUT);
    }
    return cellDisabledErrorBackground;
  }
  
  static ColorUIResource subtractColor(Color color, Color subtractColor) {
    return new ColorUIResource(color.getRed() - subtractColor.getRed(), color
        .getGreen()
        - subtractColor.getGreen(), color.getBlue() - subtractColor.getBlue());
  }
}
/**
 * <p> $Log$
 * <p> Revision 1.5  2007/02/20 20:36:10  sueh
 * <p> bug# 964 Improved the colors.
 * <p>
 * <p> Revision 1.4  2007/02/19 22:00:41  sueh
 * <p> bug# 964 Added background color for PEET interface.
 * <p>
 * <p> Revision 1.3  2006/04/07 23:32:18  sueh
 * <p> bug# 846 Changing the background colors for java 1.5.
 * <p>
 * <p> Revision 1.2  2006/04/06 23:33:58  sueh
 * <p> bug# 844 Added colors for the join and the generic parallel processing
 * <p> windows.
 * <p>
 * <p> Revision 1.1  2005/04/16 01:54:54  sueh
 * <p> bug# 615 Class to hold static colors.
 * <p> </p>
 */
