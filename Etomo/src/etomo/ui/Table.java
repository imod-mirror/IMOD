package etomo.ui;

import javax.swing.JComponent;

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
public interface Table {
  public static  final String  rcsid =  "$Id$";
  
  public void addCell(JComponent cell);
  public void removeCell(JComponent cell);
}
