package etomo.comscript;

import java.io.File;

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
public interface Command {
  public static  final String  rcsid =  "$Id$";
  
  public String getCommandLine();
  public String getCommandName();
  public File getOutputFile();
}
