package etomo.comscript;

import java.io.File;
import java.util.ArrayList;

import etomo.BaseManager;
import etomo.process.SystemProgram;
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
* <p> Revision 1.1.2.1  2004/10/21 02:35:21  sueh
* <p> bug# 520 Param for running Midas.
* <p> </p>
*/
public class MidasParam implements Command {
  public static  final String  rcsid =  "$Id$";
  
  private static final int commandSize = 1;
  private static final String commandName = "midas";
  
  private ConstJoinMetaData metaData;
  private String[] commandArray;
  private SystemProgram program;
  private String workingDir;
  private File outputFile = null;
  private String rootName = null;
  private String outputFileName = null;
  
  public MidasParam(ConstJoinMetaData metaData) {
    this.metaData = metaData;
    workingDir = metaData.getWorkingDir();
    rootName = metaData.getRootName();
    outputFileName = rootName + ".xf";
    outputFile = new File(workingDir, outputFileName);
    ArrayList options = genOptions();
    commandArray = new String[options.size() + commandSize];
    commandArray[0] = BaseManager.getIMODBinPath() + commandName;    
    for (int i = 0; i < options.size(); i++) {
      commandArray[i + commandSize] = (String) options.get(i);
    }
    program = new SystemProgram(commandArray);
    program.setWorkingDirectory(new File(workingDir));
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
  
  private ArrayList genOptions() {
    ArrayList options = new ArrayList();
    ArrayList sectionData = metaData.getSectionTableData();
    int sectionDataSize = sectionData.size();
    StringBuffer chunkSize = new StringBuffer();
    options.add("-c");
    for (int i = 0; i < sectionDataSize; i++) {
      ConstSectionTableRowData data = (SectionTableRowData) sectionData.get(i);
      chunkSize.append(data.getChunkSize().getString());
      if (i < sectionDataSize - 1) {
        chunkSize.append(",");
      }
    }
    options.add(chunkSize.toString());
    options.add("-b");
    options.add("0");
    options.add("-D");
    options.add(rootName + ".sample");
    options.add(outputFileName);
    return options;
  }
  
  public File getOutputFile() {
    return outputFile;
  }
  
  public static String getName() {
    return commandName;
  }

}
