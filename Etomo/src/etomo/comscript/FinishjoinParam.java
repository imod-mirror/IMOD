package etomo.comscript;

import java.io.File;
import java.util.ArrayList;

import etomo.BaseManager;
import etomo.EtomoDirector;
import etomo.process.SystemProgram;
import etomo.type.ConstEtomoInteger;
import etomo.type.ConstJoinMetaData;
import etomo.type.ConstSectionTableRowData;
import etomo.type.EtomoInteger;
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
* <p> Revision 1.1.2.6  2004/10/30 01:28:16  sueh
* <p> bug# 520 Added comments.
* <p>
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
public class FinishjoinParam implements Command {
  public static final String  rcsid =  "$Id$";
  
  public static final int FINISH_JOIN_MODE = -1;
  public static final int MAX_SIZE_MODE = -2;
  public static final int TRIAL_MODE = -3;
  
  public static final String SIZE_TAG = "Maximum size required:";
  public static final String OFFSET_TAG = "Offset needed to center:";
  public static final int SIZE_IN_X_INDEX = 3;
  public static final int SIZE_IN_Y_INDEX = 4;
  public static final int OFFSET_IN_X_INDEX = 4;
  public static final int OFFSET_IN_Y_INDEX = 5;
  
  private static final String commandName = "finishjoin";
  private ConstJoinMetaData metaData;
  private String[] commandArray;
  private SystemProgram program;
  private String rootName;
  private File outputFile;
  private int mode;
  
  public FinishjoinParam(ConstJoinMetaData metaData, int mode) {
    this.metaData = metaData;
    this.mode = mode;
    rootName = metaData.getRootName();
    outputFile = new File(EtomoDirector.getInstance().getCurrentPropertyUserDir(), rootName + ".join");
    ArrayList options = genOptions();
    commandArray = new String[options.size() + 3];
    commandArray[0] = "tcsh";
    commandArray[1] = "-f";
    commandArray[2] = BaseManager.getIMODBinPath() + commandName;          
    for (int i = 0; i < options.size(); i++) {
      commandArray[i + 3] = (String) options.get(i);
    }
    program = new SystemProgram(commandArray);
    program.setWorkingDirectory(new File(EtomoDirector.getInstance().getCurrentPropertyUserDir()));
  }
  
  public String[] getCommandArray() {
    return commandArray;
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
  
  public static String getName() {
    return commandName;
  }
  
  public static ConstEtomoInteger getShift(String offset) {
    return new EtomoInteger().set(offset).getNegation();
  }
  
  public File getOutputFile() {
    return outputFile;
  }
  
  public int getMode() {
    return mode;
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
    ConstEtomoInteger shiftInX = metaData.getShiftInX();
    ConstEtomoInteger shiftInY = metaData.getShiftInY();
    if (shiftInX.isSetAndNotDefault() || shiftInY.isSetAndNotDefault()) {
      options.add("-o");
      //both numbers must exist
      //offset is a negative shift
      options.add(shiftInX.getNegation().getString(true) + "," + shiftInY.getNegation().getString(true));
    }
    if (mode == MAX_SIZE_MODE) {
      options.add("-m");
    }
    options.add(rootName);
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
