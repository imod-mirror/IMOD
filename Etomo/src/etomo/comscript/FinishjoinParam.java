package etomo.comscript;

import java.io.File;
import java.util.ArrayList;

import etomo.BaseManager;
import etomo.EtomoDirector;
import etomo.process.SystemProgram;
import etomo.type.ConstJoinMetaData;
import etomo.type.ConstSectionTableRowData;
import etomo.type.EtomoSimpleType;
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
* <p> Revision 1.1.2.5  2004/10/29 01:17:06  sueh
* <p> bug# 520 Removed working directory from meta data.  Getting working
* <p> directory from propertyUserDir.
* <p>
* <p> Revision 1.1.2.4  2004/10/25 22:58:24  sueh
* <p> bug# 520 Use the negative of shift in X, Y when passing to finish join.
* <p>
* <p> Revision 1.1.2.3  2004/10/22 20:55:34  sueh
* <p> bug# 520 Using EtomoSimpleType where possible.  Changed offsetInX, Y
* <p> to shiftInX, Y.
* <p>
* <p> Revision 1.1.2.2  2004/10/22 03:20:38  sueh
* <p> bug# 520 Reducing the number of ConstJoinMetaData functions by
* <p> passing EtomoInteger, EtomoFloat, etc and using its get() and getString()
* <p> functions.
* <p>
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
    program.setWorkingDirectory(new File(EtomoDirector.getInstance().getCurrentPropertyUserDir()));
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
    //Add optional size
    EtomoSimpleType sizeInX = metaData.getSizeInX();
    EtomoSimpleType sizeInY = metaData.getSizeInY();
    if (sizeInX.isSetAndNotDefault() || sizeInY.isSetAndNotDefault()) {
      options.add("-s");
      //both numbers must exist
      options.add(sizeInX.getString(true) + "," + sizeInY.getString(true));
    }
    //Add optional offset
    EtomoSimpleType shiftInX = metaData.getShiftInX();
    EtomoSimpleType shiftInY = metaData.getShiftInY();
    if (shiftInX.isSetAndNotDefault() || shiftInY.isSetAndNotDefault()) {
      options.add("-o");
      //both numbers must exist
      //offset is a negative shift
      options.add(shiftInX.getNegation().getString(true) + "," + shiftInY.getNegation().getString(true));
    }
    options.add(metaData.getRootName());
    ArrayList sectionData = metaData.getSectionTableData();
    int sectionDataSize = sectionData.size();
    for (int i = 0; i < sectionDataSize; i++) {
      ConstSectionTableRowData data = (SectionTableRowData) sectionData.get(i);
      //both numbers must exist
      options.add(data.getFinalStartString() + "," + data.getFinalEndString());
    }
    return options;
  }
}
