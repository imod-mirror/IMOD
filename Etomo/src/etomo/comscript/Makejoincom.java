package etomo.comscript;

import java.io.IOException;
import java.util.ArrayList;

import etomo.BaseManager;
import etomo.process.SystemProgram;
import etomo.type.ConstJoinMetaData;
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
* <p> $Log$ </p>
*/
public class Makejoincom {
  public static  final String  rcsid =  "$Id$";
  
  private ConstJoinMetaData metaData;
  private StringBuffer commandLine;
  private SystemProgram makejoincom;
  
  public Makejoincom(ConstJoinMetaData metaData) {
    this.metaData = metaData;

    //  Create a new SystemProgram object for setupcombine, set the
    //  working directory and stdin array.
    // Do not use the -e flag for tcsh since David's scripts handle the failure 
    // of commands and then report appropriately.  The exception to this is the
    // com scripts which require the -e flag.  RJG: 2003-11-06  
    commandLine = new StringBuffer("tcsh -f " + BaseManager.getIMODBinPath() + "makejoincom");
    ArrayList options = genOptions();
    for (int i = 0; i < options.size(); i++) {
      commandLine.append(" " + options.get(i));
    }
    makejoincom = new SystemProgram(commandLine.toString());
  }
  
  private ArrayList genOptions() {
    ArrayList options = new ArrayList();
    ArrayList sectionData = metaData.getSectionTableData();
    int sectionDataSize = sectionData.size();
    for (int i = 0; i < sectionDataSize; i++) {
      SectionTableRowData data = (SectionTableRowData) sectionData.get(i);
      if (i < sectionDataSize - 1) {
        options.add("-top " + data.getSampleTopStartString() + ","
            + data.getSampleTopEndString());
      }
      if (i != 0) {
        options.add("-bot " + data.getSampleBottomStartString() + ","
            + data.getSampleBottomEndString());
      }
      if (data.isRotationAngleSet()) {
        options.add("-rot " + data.getRotationAngleXString() + ","
            + data.getRotationAngleYString() + ","
            + data.getRotationAngleZString());
      }
      options.add("\"" + data.getSectionAbsolutePath() + "\"");
    }
    options.add(metaData.getRootName());
    return options;
  }
  
  public int run() throws IOException {
    int exitValue;

    //  Execute the script
    makejoincom.setDebug(true);
    makejoincom.run();
    exitValue = makejoincom.getExitValue();

    //  TODO we really need to find out what the exception/error condition was
    if (exitValue != 0) {
      throw (new IOException(makejoincom.getExceptionMessage()));
    }
    return exitValue;
  }
  
  public String[] getStdError() {
    return makejoincom.getStdError();
  }
}
