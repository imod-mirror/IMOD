package etomo.comscript;

import java.io.File;
import java.util.ArrayList;

import etomo.BaseManager;
import etomo.process.SystemProgram;
import etomo.type.ConstEtomoFloat;
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
* <p> $Log$
* <p> Revision 1.1.2.1  2004/10/21 02:32:20  sueh
* <p> bug# 520 Param for finishjoin.
* <p> </p>
*/
public class FinishjoinParam {
  public static  final String  rcsid =  "$Id$";
  
  
  private ConstJoinMetaData metaData;
  private String[] commandArray;
  private SystemProgram program;
  
  public FinishjoinParam(ConstJoinMetaData metaData) {
    this.metaData = metaData;

    //  Create a new SystemProgram object for setupcombine, set the
    //  working directory and stdin array.
    // Do not use the -e flag for tcsh since David's scripts handle the failure 
    // of commands and then report appropriately.  The exception to this is the
    // com scripts which require the -e flag.  RJG: 2003-11-06  
    ArrayList options = genOptions();
    commandArray = new String[options.size() + 3];
    commandArray[0] = "tcsh";
    commandArray[1] = "-f";
    commandArray[2] = BaseManager.getIMODBinPath() + "finishjoin";          
    for (int i = 0; i < options.size(); i++) {
      commandArray[i + 3] = (String) options.get(i);
    }
    program = new SystemProgram(commandArray);
    program.setWorkingDirectory(new File(metaData.getWorkingDir()));
  }
  
  public String[] getCommandArray() {
    return commandArray;
  }
  
  private ArrayList genOptions() {
    ArrayList options = new ArrayList();
    if (metaData.getUseAlignmentRefSection()) {
      options.add("-r");
      options.add(metaData.getAlignmentRefSection().getString());
    }
    ConstEtomoInteger sizeInX = metaData.getSizeInX();
    ConstEtomoInteger sizeInY = metaData.getSizeInY();
    ConstEtomoFloat offsetInX = metaData.getOffsetInX();
    ConstEtomoFloat offsetInY = metaData.getOffsetInY();
    if (sizeInX.isSetAndNotDefault() || sizeInY.isSetAndNotDefault()) {
      options.add("-s");
      options.add(sizeInX.getString() + "," + sizeInY.getString());
      
    }
    if (offsetInX.isSetAndNotDefault() || offsetInY.isSetAndNotDefault()) {
      options.add("-o");
      options.add(offsetInX.getString() + "," + offsetInY.getString());
    }
    options.add(metaData.getRootName());
    ArrayList sectionData = metaData.getSectionTableData();
    int sectionDataSize = sectionData.size();
    for (int i = 0; i < sectionDataSize; i++) {
      ConstSectionTableRowData data = (SectionTableRowData) sectionData.get(i);
      options.add(data.getFinalStartString() + "," + data.getFinalEndString());
    }
    return options;
  }
}
