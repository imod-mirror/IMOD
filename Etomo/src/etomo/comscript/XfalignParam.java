package etomo.comscript;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import etomo.BaseManager;
import etomo.process.SystemProgram;
import etomo.type.ConstEtomoDouble;
import etomo.type.ConstJoinMetaData;

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
public class XfalignParam {
  public static  final String  rcsid =  "$Id$";
  
  private ConstJoinMetaData metaData;
  private String[] commandArray;
  private SystemProgram makejoincom;
  
  public XfalignParam(ConstJoinMetaData metaData) {
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
    commandArray[2] = BaseManager.getIMODBinPath() + "xfalign";          
    for (int i = 0; i < options.size(); i++) {
      commandArray[i + 3] = (String) options.get(i);
    }
    makejoincom = new SystemProgram(commandArray);
    makejoincom.setWorkingDirectory(new File(metaData.getWorkingDir()));
  }
  
  public String[] getCommandArray() {
    return commandArray;
  }
  
  private ArrayList genOptions() {
    ArrayList options = new ArrayList();
    options.add("-tomo");
    options.add("-pre");
    ConstEtomoDouble sigmaLowFrequency = metaData.getSigmaLowFrequencyField();
    ConstEtomoDouble cutoffHighFrequency = metaData.getCutoffHighFrequencyField();
    ConstEtomoDouble sigmaHighFrequency = metaData.getSigmaHighFrequencyField();
    if ((sigmaLowFrequency.isSet() && !sigmaLowFrequency.isDefault())
        ||(cutoffHighFrequency.isSet() && !cutoffHighFrequency.isDefault())
        ||(sigmaHighFrequency.isSet() && !sigmaHighFrequency.isDefault())) {
      options.add("-fil");
      options.add(sigmaLowFrequency + "," + sigmaHighFrequency + ",0," + cutoffHighFrequency);
    }
    if (!metaData.isFullLinearTransformation()) {
      if (metaData.isRotationTranslationMagnification()) {
        options.add("-par");
        options.add("4");
      }
      else if (metaData.isRotationTranslation()) {
        options.add("-par");
        options.add("3");
      }
    }
    String rootName = metaData.getRootName();
    options.add(rootName + ".sampavg");
    options.add(rootName + ".xf");
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
