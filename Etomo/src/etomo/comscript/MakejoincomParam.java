package etomo.comscript;

import java.io.File;
import java.util.ArrayList;

import etomo.BaseManager;
import etomo.EtomoDirector;
import etomo.process.SystemProgram;
import etomo.type.ConstEtomoNumber;
import etomo.type.ConstJoinMetaData;
import etomo.type.ConstSectionTableRowData;
import etomo.type.SectionTableRowData;

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
* <p> Old log: Makejoincom.java,v
* <p> Revision 1.1.2.1  2004/09/29 17:46:50  sueh
* <p> bug# 520 Class to run the makejoincom script.  Gets its options from
* <p> ConstJoinMetaData.
* <p> </p>
* 
* <p> $Log$
* <p> Revision 1.1.2.10  2004/11/15 22:17:10  sueh
* <p> bug# 520 Implementing Command.
* <p>
* <p> Revision 1.1.2.9  2004/10/30 01:31:40  sueh
* <p> bug# 520 bug fix: A missing rotationAngleX caused the param to not use
* <p> the -rot options.  Now the option will be used if any rotation angle is set and
* <p> the unset ones will be defaulted.
* <p>
* <p> Revision 1.1.2.8  2004/10/29 22:07:58  sueh
* <p> bug# 520 Use -tmpext to set the temp file extension to .rot.
* <p>
* <p> Revision 1.1.2.7  2004/10/29 01:17:19  sueh
* <p> bug# 520 Removed working directory from meta data.  Getting working
* <p> directory from propertyUserDir.
* <p>
* <p> Revision 1.1.2.6  2004/10/22 20:57:50  sueh
* <p> bug# 520 Simplifying ConstSectionTableRowData by passing
* <p> EtomoSimpleType instead of String and int.
* <p>
* <p> Revision 1.1.2.5  2004/10/22 03:20:52  sueh
* <p> bug# 520 Reducing the number of ConstJoinMetaData functions by
* <p> passing EtomoInteger, EtomoFloat, etc and using its get() and getString()
* <p> functions.
* <p>
* <p> Revision 1.1.2.4  2004/10/21 02:34:59  sueh
* <p> bug# 520 Removed unnecessary function run().
* <p>
* <p> Revision 1.1.2.3  2004/10/18 17:42:02  sueh
* <p> bug# 520 Added -reference to the command string.
* <p>
* <p> Revision 1.1.2.2  2004/10/14 02:27:53  sueh
* <p> bug# 520 Setting working directory in SystemProgram.
* <p>
* <p> Revision 1.1.2.1  2004/10/08 15:50:06  sueh
* <p> bug# 520 Renamed Makejoincom to MakejoincomParam.  Switched from
* <p> a command line to a command array because of the possibility of spaces
* <p> within parameters.
* <p> </p>
*/
public class MakejoincomParam implements Command {
  public static  final String  rcsid =  "$Id$";
  
  private static final int commandSize = 3;
  private static final String commandName = "makejoincom";
  
  private ConstJoinMetaData metaData;
  private String[] commandArray;
  private SystemProgram program;
  
  public MakejoincomParam(ConstJoinMetaData metaData) {
    this.metaData = metaData;

    //  Create a new SystemProgram object for setupcombine, set the
    //  working directory and stdin array.
    // Do not use the -e flag for tcsh since David's scripts handle the failure 
    // of commands and then report appropriately.  The exception to this is the
    // com scripts which require the -e flag.  RJG: 2003-11-06  
    ArrayList options = genOptions();
    commandArray = new String[options.size() + commandSize];
    commandArray[0] = "tcsh";
    commandArray[1] = "-f";
    commandArray[2] = BaseManager.getIMODBinPath() + commandName;          
    for (int i = 0; i < options.size(); i++) {
      commandArray[i + commandSize] = (String) options.get(i);
    }
    program = new SystemProgram(commandArray);
    program.setWorkingDirectory(new File(EtomoDirector.getInstance().getCurrentPropertyUserDir()));
  }
  
  public String[] getCommandArray() {
    return commandArray;
  }
  
  private ArrayList genOptions() {
    ArrayList options = new ArrayList();
    ArrayList sectionData = metaData.getSectionTableData();
    int sectionDataSize = sectionData.size();
    for (int i = 0; i < sectionDataSize; i++) {
      ConstSectionTableRowData data = (SectionTableRowData) sectionData.get(i);
      if (i < sectionDataSize - 1) {
        options.add("-top");
        //both numbers must exist
        options.add(data.getSampleTopStart().toString() + ","
            + data.getSampleTopEnd().toString());
      }
      if (i != 0) {
        options.add("-bot");
        //both numbers must exist
        options.add(data.getSampleBottomStart().toString() + ","
            + data.getSampleBottomEnd().toString());
      }
      //Add optional rotation angles
      ConstEtomoNumber rotationAngleX = data.getRotationAngleX();
      ConstEtomoNumber rotationAngleY = data.getRotationAngleY();
      ConstEtomoNumber rotationAngleZ = data.getRotationAngleZ();
      if (rotationAngleX.isSetAndNotDefault()
          || rotationAngleY.isSetAndNotDefault()
          || rotationAngleZ.isSetAndNotDefault()) {
        options.add("-rot");
        //all three numbers must exist
        options.add(rotationAngleX.toString(true) + ","
            + rotationAngleY.toString(true) + ","
            + rotationAngleZ.toString(true));
      }
      options.add(data.getSectionAbsolutePath());
    }
    options.add("-tmpext");
    options.add("rot");
    ConstEtomoNumber densityRefSection = metaData.getDensityRefSection();
    if (densityRefSection.isSetAndNotDefault()) {
      options.add("-ref");
      options.add(densityRefSection.toString());
    }
    options.add(metaData.getRootName());
    return options;
  }
  
  public int getBinning() {
    return 1;
  }
  
  public String getCommandLine() {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < commandArray.length; i++) {
      buffer.append(commandArray[i] + " ");
    }
    return buffer.toString();
  }
  
  public String getCommandName() {
    return commandName;
  }
  
  public int getIntegerValue(int name) {
    return Integer.MIN_VALUE;
  }
  
  public int getMode() {
    return 0;
  }
  
  public File getOutputFile() {
    return null;
  }
  
  public static String getName() {
    return commandName;
  }

}
