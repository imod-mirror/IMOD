package etomo.comscript;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

/*
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Organization: Boulder Laboratory for 3D Fine Structure,
 * University of Colorado</p>
 *
 * @author $Author$
 *
 * @version $Revision$
 *
 * <p> $Log$
 * <p> Revision 3.0  2003/11/07 23:19:00  rickg
 * <p> Version 1.0.0
 * <p>
 * <p> Revision 2.7  2003/10/28 18:46:59  sueh
 * <p> removing prints
 * <p>
 * <p> Revision 2.6  2003/10/02 18:57:46  sueh
 * <p> bug236 added testing:
 * <p> NewstParamTest
 * <p> ComScriptTest
 * <p>
 * <p> Removed marks
 * <p>
 * <p> Revision 2.5  2003/09/29 23:34:57  sueh
 * <p> bug236 Added UseLinearInterpolation to
 * <p> TomogramGenerationDialog.
 * <p>
 * <p> UseLinearInterpolation:
 * <p> check box
 * <p> Advanced
 * <p> newst -linear
 * <p>
 * <p> Files:
 * <p> ComScriptManager.java
 * <p> ConstNewstParam.java
 * <p> NewstParam.java
 * <p> TomogramGenerationDialog.java
 * <p> ApplicationManager.java
 * <p>
 * <p> Revision 2.4  2003/07/25 22:54:14  rickg
 * <p> CommandParam method name changes
 * <p>
 * <p> Revision 2.3  2003/06/25 22:16:29  rickg
 * <p> changed name of com script parse method to parseComScript
 * <p>
 * <p> Revision 2.2  2003/03/20 17:23:37  rickg
 * <p> Comment update
 * <p>
 * <p> Revision 2.1  2003/03/02 23:30:41  rickg
 * <p> Combine layout in progress
 * <p>
 * <p> Revision 2.0  2003/01/24 20:30:31  rickg
 * <p> Single window merge to main branch
 * <p>
 * <p> Revision 1.1.2.1  2003/01/24 18:33:42  rickg
 * <p> Single window GUI layout initial revision
 * <p>
 * <p> Revision 1.1  2002/09/09 22:57:02  rickg
 * <p> Initial CVS entry, basic functionality not including combining
 * <p> </p>
 */
public class NewstParam extends ConstNewstParam implements CommandParam {
  public static final String rcsid = 
  "$Id$";

  /**
   * Get the parameters from the ComScriptCommand
   * @param scriptCommand the ComScriptCommand containg the newst command
   * and parameters.
   */
  public void parseComScriptCommand(ComScriptCommand scriptCommand)
  throws FortranInputSyntaxException {
    // TODO error checking - throw exceptions for bad syntax
    String[] cmdLineArgs = scriptCommand.getCommandLineArgs();
    reset();
    for (int i = 0; i < cmdLineArgs.length - 2; i++) {
      if (cmdLineArgs[i].startsWith("-si")) {
        i++;
        sizeToOutputInXandY.validateAndSet(cmdLineArgs[i]);
      }
      if (cmdLineArgs[i].startsWith("-o")) {
        i++;
        offsetsInXandY.add(cmdLineArgs[i]);
      }
      if (cmdLineArgs[i].startsWith("-x")) {
        i++;
        transformFile = cmdLineArgs[i];
      }
      if (cmdLineArgs[i].startsWith("-l")) {
        linearInterpolation = true;
      }
    }
    inputFile.add(cmdLineArgs[cmdLineArgs.length - 2]);
    outputFile.add(cmdLineArgs[cmdLineArgs.length - 1]);
  }

  /**
   * Update the script command with the current valus of this NewstParam
   * object
   * @param scriptCommand the script command to be updated
   */
  public void updateComScriptCommand(ComScriptCommand scriptCommand)
  throws BadComScriptException {
    // Create a new command line argument array

    ArrayList cmdLineArgs = new ArrayList(20);
    
    for(Iterator i = inputFile.iterator(); i.hasNext(); ) {
        cmdLineArgs.add("-InputFile");
        cmdLineArgs.add((String) i.next());
     }

    for(Iterator i = outputFile.iterator(); i.hasNext();) {
      cmdLineArgs.add("-OutputFile");
      cmdLineArgs.add((String) i.next());
    }
    
    if(!fileOfInputs.equals("")) {
      cmdLineArgs.add("-FileOfInputs");
      cmdLineArgs.add(fileOfInputs);
    }
    
    if(!fileOfOutputs.equals("")) {
      cmdLineArgs.add("-FileOfOutputs");
      cmdLineArgs.add(fileOfOutputs);
    }
    
    for(Iterator i = sectionsToRead.iterator(); i.hasNext();) {
      cmdLineArgs.add("-SectionsToRead");
      cmdLineArgs.add((String) i.next());
    }
    
    for(Iterator i = numberToOutput.iterator(); i.hasNext();) {
      cmdLineArgs.add("-NumberToOutput");
      cmdLineArgs.add((String) i.next());
    }
    
    if (sizeToOutputInXandY.valuesSet()) {
      cmdLineArgs.add("-SizeToOutputInXandY");
      cmdLineArgs.add(sizeToOutputInXandY.toString());
    }

    if (modeToOutput > Integer.MIN_VALUE) {
      cmdLineArgs.add("-ModeToOutput");
      cmdLineArgs.add(String.valueOf(modeToOutput));
    }
    
    for(Iterator i = offsetsInXandY.iterator(); i.hasNext();) {
      cmdLineArgs.add("-OffsetsInXandY");
      cmdLineArgs.add((String) i.next());
    }

    if (applyOffsetsFirst) {
      cmdLineArgs.add("-ApplyOffsetsFirst");
    }
  	
    if(!transformFile.equals("")) {
  	  cmdLineArgs.add("-TransformFile");
      cmdLineArgs.add(transformFile);
    }
  	
  	if(!useTransformLines.equals("")) {
  	  cmdLineArgs.add("-UseTransformLines");
  	  cmdLineArgs.add(useTransformLines);
  	}
  	
  	if(!Float.isNaN(rotateByAngle)) {
  	  cmdLineArgs.add("-RotateByAngle");
  	  cmdLineArgs.add(String.valueOf(rotateByAngle));
  	}
  	
  	if(!Float.isNaN(expandByFactor)) {
  	  cmdLineArgs.add("-ExpandByFactor");
  	  cmdLineArgs.add(String.valueOf(expandByFactor));
  	}

  	if (binByFactor > Integer.MIN_VALUE) {
  	  cmdLineArgs.add("-BinByFactor");
  	  cmdLineArgs.add(String.valueOf(binByFactor));
  	}

  	if (linearInterpolation) {
      cmdLineArgs.add("-LinearInterpolation");
    }
  	
  	if (floatDensities > Integer.MIN_VALUE) {
  	  cmdLineArgs.add("-FloatDensities");
  	  cmdLineArgs.add(String.valueOf(floatDensities));
  	}
    
  	if (contrastBlackWhite.valuesSet()) {
  	  cmdLineArgs.add("-ContrastBlackWhite");
  	  cmdLineArgs.add(String.valueOf(contrastBlackWhite.toString()));
  	}
  	
  	if (scaleMinAndMax.valuesSet()) {
  	  cmdLineArgs.add("-ScaleMinAndMax");
  	  cmdLineArgs.add(String.valueOf(scaleMinAndMax.toString()));
  	}

  	if(!distortionField.equals("")) {
  	  cmdLineArgs.add("-DistortionField");
  	  cmdLineArgs.add(distortionField);
  	}
  	
  	if (imagesAreBinned > Integer.MIN_VALUE) {
  	  cmdLineArgs.add("-ImagesAreBinned");
  	  cmdLineArgs.add(String.valueOf(imagesAreBinned));
  	}
  	
  	if (testLimits.valuesSet()) {
  	  cmdLineArgs.add("-TestLimits");
  	  cmdLineArgs.add(String.valueOf(testLimits.toString()));
  	}
  	
  	if(!parameterFile.equals("")) {
  	  cmdLineArgs.add("-ParameterFile");
  	  cmdLineArgs.add(parameterFile);
  	}
  	
  	int nArgs = cmdLineArgs.size();
    scriptCommand.setCommandLineArgs(
    (String[]) cmdLineArgs.toArray(new String[nArgs]));
  }

  public void setSize(String newSize) throws FortranInputSyntaxException {
    sizeToOutputInXandY.validateAndSet(newSize);
  }

  public void setOffset(String newOffset) throws FortranInputSyntaxException {
    offsetsInXandY.clear();
    offsetsInXandY.add(newOffset);
  }

  private void reset() {
    initializeEmpty();
  }

  /**
   * @param applyOffsetsFirst The applyOffsetsFirst to set.
   */
  public void setApplyOffsetsFirst(boolean applyOffsetsFirst) {
    this.applyOffsetsFirst = applyOffsetsFirst;
  }

  /**
   * @param binByFactor The binByFactor to set.
   */
  public void setBinByFactor(int binByFactor) {
    this.binByFactor = binByFactor;
  }

  /**
   * @param contrastBlackWhite The contrastBlackWhite to set.
   */
  public void setContrastBlackWhite(FortranInputString contrastBlackWhite) {
    this.contrastBlackWhite = contrastBlackWhite;
  }

  /**
   * @param distortionField The distortionField to set.
   */
  public void setDistortionField(String distortionField) {
    this.distortionField = distortionField;
  }

  /**
   * @param expandByFactor The expandByFactor to set.
   */
  public void setExpandByFactor(float expandByFactor) {
    this.expandByFactor = expandByFactor;
  }

  /**
   * @param fileOfInputs The fileOfInputs to set.
   */
  public void setFileOfInputs(String fileOfInputs) {
    this.fileOfInputs = fileOfInputs;
  }

  /**
   * @param fileOfOutputs The fileOfOutputs to set.
   */
  public void setFileOfOutputs(String fileOfOutputs) {
    this.fileOfOutputs = fileOfOutputs;
  }

  /**
   * @param floatDensities The floatDensities to set.
   */
  public void setFloatDensities(int floatDensities) {
    this.floatDensities = floatDensities;
  }

  /**
   * @param imagesAreBinned The imagesAreBinned to set.
   */
  public void setImagesAreBinned(int imagesAreBinned) {
    this.imagesAreBinned = imagesAreBinned;
  }

  /**
   * @param inputFile The inputFile to set.
   */
  public void setInputFile(Vector inputFile) {
    this.inputFile = inputFile;
  }

  /**
   * @param linearInterpolation The linearInterpolation to set.
   */
  public void setLinearInterpolation(boolean linearInterpolation) {
    this.linearInterpolation = linearInterpolation;
  }

  /**
   * @param modeToOutput The modeToOutput to set.
   */
  public void setModeToOutput(int modeToOutput) {
    this.modeToOutput = modeToOutput;
  }

  /**
   * @param numberToOutput The numberToOutput to set.
   */
  public void setNumberToOutput(Vector numberToOutput) {
    this.numberToOutput = numberToOutput;
  }

  /**
   * @param offsetsInXandY The offsetsInXandY to set.
   */
  public void setOffsetsInXandY(Vector offsetsInXandY) {
    this.offsetsInXandY = offsetsInXandY;
  }

  /**
   * @param outputFile The outputFile to set.
   */
  public void setOutputFile(Vector outputFile) {
    this.outputFile = outputFile;
  }

  /**
   * @param parameterFile The parameterFile to set.
   */
  public void setParameterFile(String parameterFile) {
    this.parameterFile = parameterFile;
  }

  /**
   * @param rotateByAngle The rotateByAngle to set.
   */
  public void setRotateByAngle(float rotateByAngle) {
    this.rotateByAngle = rotateByAngle;
  }

  /**
   * @param scaleMinAndMax The scaleMinAndMax to set.
   */
  public void setScaleMinAndMax(FortranInputString scaleMinAndMax) {
    this.scaleMinAndMax = scaleMinAndMax;
  }

  /**
   * @param sectionsToRead The sectionsToRead to set.
   */
  public void setSectionsToRead(Vector sectionsToRead) {
    this.sectionsToRead = sectionsToRead;
  }

  /**
   * @param sizeToOutputInXandY The sizeToOutputInXandY to set.
   */
  public void setSizeToOutputInXandY(FortranInputString sizeToOutputInXandY) {
    this.sizeToOutputInXandY = sizeToOutputInXandY;
  }

  /**
   * @param testLimits The testLimits to set.
   */
  public void setTestLimits(FortranInputString testLimits) {
    this.testLimits = testLimits;
  }

  /**
   * @param transformFile The transformFile to set.
   */
  public void setTransformFile(String transformFile) {
    this.transformFile = transformFile;
  }

  /**
   * @param useTransformLines The useTransformLines to set.
   */
  public void setUseTransformLines(String useTransformLines) {
    this.useTransformLines = useTransformLines;
  }
}
