package etomo.type;

import java.io.File;
import java.util.Properties;

import etomo.ApplicationManager;
import etomo.EtomoDirector;
import etomo.comscript.TiltalignParam;
import etomo.comscript.TrimvolParam;
import etomo.util.MRCHeader;

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
* <p> Revision 1.7  2005/01/10 23:54:23  sueh
* <p> bug# 578 Switched member variables from EtomoBoolean to EtomoState.
* <p> Added initialize(int) to initialize EtomoState variables to
* <p> NO_RESULTS_VALUE.  Removed backward compatibility function for
* <p> newstFiducialAlignment.  EtomoState variables are necessary when the
* <p> state information was not saved in the past.
* <p>
* <p> Revision 1.6  2005/01/08 01:54:43  sueh
* <p> bug# 578 Removed alignSkewOption and alignXStretchOption.  Added
* <p> madeZFactors and newstFiducialAlignment.  Added
* <p> getBackwordCompatible functions for madeZFactors and
* <p> newstFiducialessAlignment.
* <p>
* <p> Revision 1.5  2005/01/06 18:19:05  sueh
* <p> bug# 578 added alignSkewOption and alignXStretchOption.
* <p>
* <p> Revision 1.4  2004/12/16 02:31:24  sueh
* <p> bug# 564 Manage trimvol flipped state and squeezevol flipped state
* <p> separately.  If trimvol flipped state changes and squeezevol is not rerun,
* <p> the squeezevol parameters with still load onto the screen correctly.
* <p>
* <p> Revision 1.3  2004/12/14 21:49:04  sueh
* <p> bug# 572:  Removing state object from meta data and managing it with a
* <p> manager class.  All state variables saved after a process is run belong in
* <p> the state object.
* <p>
* <p> Revision 1.2  2004/12/08 21:32:04  sueh
* <p> bug# 564 Added access to flipped.
* <p>
* <p> Revision 1.1  2004/12/07 22:54:07  sueh
* <p> bug# 564 Contains state variables to be saved in the .edf file.
* <p> </p>
*/
public class TomogramState implements BaseState {
  public static  final String  rcsid =  "$Id$";
  
  private static final String groupString = "ReconstructionState";
  
  EtomoState trimvolFlipped = new EtomoState("TrimvolFlipped");
  EtomoState squeezevolFlipped = new EtomoState("SqueezevolFlipped");
  EtomoState madeZFactors = new EtomoState("MadeZFactors");
  EtomoState newstFiducialessAlignment = new EtomoState("NewstFiducialessAlignment");
  EtomoState usedLocalAlignments = new EtomoState("UsedLocalAlignments");
  
  public TomogramState() {
    reset();
  }
  
  private void reset() {
    trimvolFlipped.reset();
    squeezevolFlipped.reset();
    madeZFactors.reset();
    newstFiducialessAlignment.reset();
    usedLocalAlignments.reset();
  }
  
  public void initialize(int value) {
    trimvolFlipped.set(value);
    squeezevolFlipped.set(value);
    madeZFactors.set(value);
    newstFiducialessAlignment.set(value);
    usedLocalAlignments.set(value);
  }
  
  public void store(Properties props) {
    store(props, "");
  }
  
  public void store(Properties props, String prepend) {
    prepend = createPrepend(prepend);
    String group = prepend + ".";
    trimvolFlipped.store(props, prepend);
    squeezevolFlipped.store(props, prepend);
    madeZFactors.store(props, prepend);
    newstFiducialessAlignment.store(props, prepend);
    usedLocalAlignments.store(props, prepend);
  }

  public boolean equals(TomogramState that) {
    if (!trimvolFlipped.equals(that.trimvolFlipped)) {
      return false;
    }
    if (!squeezevolFlipped.equals(that.squeezevolFlipped)) {
      return false;
    }
    if (!madeZFactors.equals(that.madeZFactors)) {
      return false;
    }
    if (!newstFiducialessAlignment.equals(that.newstFiducialessAlignment)) {
      return false;
    }
    if (!usedLocalAlignments.equals(that.usedLocalAlignments)) {
      return false;
    }
    return true;
  }
  
  protected static String createPrepend(String prepend) {
    if (prepend == "") {
      return groupString;
    }
    return prepend + "." + groupString;
  }

  public void load(Properties props) {
    load(props, "");
  }

  public void load(Properties props, String prepend) {
    reset();
    prepend = createPrepend(prepend);
    String group = prepend + ".";
    trimvolFlipped.load(props, prepend);
    squeezevolFlipped.load(props, prepend);
    madeZFactors.load(props, prepend);
    newstFiducialessAlignment.load(props, prepend);
    usedLocalAlignments.load(props, prepend);
  }
  
  public ConstEtomoNumber setTrimvolFlipped(boolean trimvolFlipped) {
    return this.trimvolFlipped.set(trimvolFlipped);
  }
  
  public ConstEtomoNumber setSqueezevolFlipped(boolean squeezevolFlipped) {
    return this.squeezevolFlipped.set(squeezevolFlipped);
  }
  
  public ConstEtomoNumber setMadeZFactors(boolean madeZFactors) {
    return this.madeZFactors.set(madeZFactors);
  }
  
  public ConstEtomoNumber setNewstFiducialessAlignment(boolean newstFiducialessAlignment) {
    return this.newstFiducialessAlignment.set(newstFiducialessAlignment);
  }
  
  public ConstEtomoNumber setUsedLocalAlignments(boolean usedLocalAlignments) {
    return this.usedLocalAlignments.set(usedLocalAlignments);
  }
  
  public ConstEtomoNumber getTrimvolFlipped() {
    return trimvolFlipped;
  }
  
  public ConstEtomoNumber getSqueezevolFlipped() {
    return squeezevolFlipped;
  }
  
  public ConstEtomoNumber getMadeZFactors() {
    return madeZFactors;
  }
  
  public ConstEtomoNumber getNewstFiducialessAlignment() {
    return newstFiducialessAlignment;
  }
  
  public ConstEtomoNumber getUsedLocalAlignments() {
    return usedLocalAlignments;
  }
  
  /**
   * Backward compatibility
   * function decide whether trimvol is flipped based on the header
   * @return
   */
  public boolean getBackwardCompatibleTrimvolFlipped() {
    //If trimvol has not been run, then assume that the tomogram has not been
    //flipped.
    EtomoDirector etomoDirector = EtomoDirector.getInstance();
    ApplicationManager manager = (ApplicationManager) etomoDirector
        .getCurrentManager();
    String datasetName = manager.getMetaData().getDatasetName();
    File trimvolFile = new File(etomoDirector.getCurrentPropertyUserDir(),
        TrimvolParam.getOutputFileName(datasetName));
    if (!trimvolFile.exists()) {
      return false;
    }
    MRCHeader header = new MRCHeader(trimvolFile.getAbsolutePath());
    try {
      header.read();
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    if (header.getNRows() < header.getNSections()) {
      System.err.println("Assuming that " + trimvolFile.getName() + " has not been flipped\n"
          + "because the Y is less then Z in the header.");
      return false;
    }
    System.err.println("Assuming that " + trimvolFile.getName() + " has been flipped\n" 
        + "because the Y is greater or equal to Z in the header.");
    return true;
  }
  
  /**
   * Backward compatibility
   * function decide whether tiltalign was run with local alignments based
   * file time.
   * @return
   */
  public boolean getBackwardCompatibleUsedLocalAlignments(AxisID axisID) {
    String userDir = EtomoDirector.getInstance().getCurrentPropertyUserDir();
    ApplicationManager manager = (ApplicationManager) EtomoDirector.getInstance()
        .getCurrentManager();
    String datasetName = manager.getMetaData().getDatasetName();
    File localXfFile = new File(userDir,
        datasetName + axisID.getExtension() + "local.xf");
    File transformFile = new File(userDir,
        datasetName + axisID.getExtension() + ".tltxf");
    if (!localXfFile.exists()) {
      System.err.println("Assuming that local alignments where not used "
          + "\nbecause " + localXfFile.getName() + " does not exist.");
      return false;
    }
    if (!transformFile.exists()) {
      System.err.println("Assuming that local alignments where not used "
          + "\nbecause " + transformFile.getName() + " does not exist.");
      return false;
    }
    if (localXfFile.lastModified() < transformFile.lastModified()) {
      System.err.println("Assuming that local alignments where not used "
          + "\nbecause " + localXfFile.getName() + " was modified before "
          + transformFile.getName() + ".");
      return false;
    }
    System.err.println("Assuming that local alignments where used "
        + "\nbecause " + localXfFile.getName() + " was modified after "
        + transformFile.getName() + ".");
    return true;
  }
  
  /**
   * Backward compatibility
   * function decide whether z factors where made based on the relationship
   * between .zfac file and the .tltxf file
   * @return
   */
  public boolean getBackwardCompatibleMadeZFactors(AxisID axisID) {
    EtomoDirector etomoDirector = EtomoDirector.getInstance();
    String userDir = etomoDirector.getCurrentPropertyUserDir();
    ApplicationManager manager = (ApplicationManager) etomoDirector
        .getCurrentManager();
    String datasetName = manager.getMetaData().getDatasetName();
    File zFactorFile = new File(userDir, TiltalignParam.getOutputZFactorFileName(datasetName, axisID));
    File tltxfFile = new File(userDir, datasetName + axisID.getExtension() + ".tltxf");
    if (!zFactorFile.exists()) {
      System.err.println("Assuming that madeZFactors is false\n" 
          + "because " + zFactorFile.getName() + " does not exist.");
      return false;
    }
    if (!tltxfFile.exists()) {
      System.err.println("Assuming that madeZFactors is false\n" 
          + "because " + tltxfFile.getName() + " does not exist.");
      return false;
    }
    if (zFactorFile.lastModified() < tltxfFile.lastModified()) {
      System.err.println("Assuming that madeZFactors is false\n" 
          + "because " + zFactorFile.getName() + " is older then " + tltxfFile.getName() + ".");

      return false;
    }
    System.err.println("Assuming that madeZFactors is true\n" 
        + "because " + zFactorFile.getName() + " was modified after " + tltxfFile.getName() + ".");
    return true;
  }

}
