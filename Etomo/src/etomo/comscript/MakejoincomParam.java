package etomo.comscript;

import java.io.File;
import java.util.ArrayList;

import etomo.BaseManager;
import etomo.EtomoDirector;
import etomo.process.SystemProgram;
import etomo.type.ConstEtomoInteger;
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
public class MakejoincomParam {
  public static  final String  rcsid =  "$Id$";
  
  private static final int commandSize = 3;
  
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
    commandArray[2] = BaseManager.getIMODBinPath() + "makejoincom";          
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
        options.add(data.getSampleTopStart().getString() + ","
            + data.getSampleTopEnd().getString());
      }
      if (i != 0) {
        options.add("-bot");
        options.add(data.getSampleBottomStart().getString() + ","
            + data.getSampleBottomEnd().getString());
      }
      if (data.isRotationAngleSet()) {
        options.add("-rot");
        options.add(data.getRotationAngleXString() + ","
            + data.getRotationAngleYString() + ","
            + data.getRotationAngleZString());
      }
      options.add(data.getSectionAbsolutePath());
    }
    ConstEtomoInteger densityRefSection = metaData.getDensityRefSection();
    if (densityRefSection.isSetAndNotDefault()) {
      options.add("-ref");
      options.add(densityRefSection.getString());
    }
    options.add(metaData.getRootName());
    return options;
  }
}
