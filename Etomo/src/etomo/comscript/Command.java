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
* <p> $Log$
* <p> Revision 1.1.2.1  2004/10/06 01:28:49  sueh
* <p> bug# 520 An interface that allow BackgroundProcess to take a param
* <p> object rather then just a command line.  This allows BackgroundProcess
* <p> to be used by non-generic post-processing functions.
* <p> </p>
*/
public interface Command {
  public static  final String  rcsid =  "$Id$";
  
  public String getCommandLine();
  public String getCommandName();
  public File getOutputFile();
  public int getMode();
}
