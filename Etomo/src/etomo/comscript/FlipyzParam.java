package etomo.comscript;

import java.io.File;
import java.util.ArrayList;

import etomo.BaseManager;

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
public class FlipyzParam implements Command {
  public static  final String  rcsid =  "$Id$";
  
  public static final String command = "clipflipyz";
  private StringBuffer commandLine;
  private File flipFile;
  
  public FlipyzParam(File tomogram, File workingDir) {
    commandLine = new StringBuffer("tcsh -f " + "/home/sueh/workspace/Etomo_3-4-6_JOIN/scripts/" + command);
    ArrayList options = genOptions(tomogram, workingDir);
    for (int i = 0; i < options.size(); i++) {
      commandLine.append(" " + options.get(i));
    }
  }
  
  private ArrayList genOptions(File tomogram, File workingDir) {
    ArrayList options = new ArrayList(2);
    options.add(tomogram.getAbsolutePath());
    int index = tomogram.getName().lastIndexOf('.');
    StringBuffer flipFileName = new StringBuffer();
    if (index == -1) {
      flipFileName.append(tomogram.getName()); 
    }
    else {
      flipFileName.append(tomogram.getName().substring(0, index));
    }
    flipFile = new File(workingDir, flipFileName + ".flip");
    options.add(flipFile.getAbsolutePath());
    return options;
  }
  
  public File getFlipFile() {
    return flipFile;
  }
  
  public String getCommandLine() {
    return commandLine.toString();
  }
  
  public String getCommandName() {
    return command;
  }
  
  public static String getName() {
    return command;
  }
  
  public File getOutputFile() {
    return flipFile;
  }
}
