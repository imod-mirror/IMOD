package etomo.comscript;

/**
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
 * <p> Revision 2.0  2003/01/24 20:30:31  rickg
 * <p> Single window merge to main branch
 * <p>
 * <p> Revision 1.4.2.1  2003/01/24 18:33:42  rickg
 * <p> Single window GUI layout initial revision
 * <p>
 * <p> Revision 1.4  2003/01/08 04:00:21  rickg
 * <p> Mods in progress
 * <p>
 * <p> Revision 1.3  2003/01/06 04:50:54  rickg
 * <p> Added command line arguments for b to a and search
 * <p> rotation directions as well as getter and setters for those
 * <p> parameters
 * <p>
 * <p> Revision 1.2  2003/01/04 00:37:27  rickg
 * <p> Added datasetName, bToA, searchDirection, centerView* members
 * <p> Added centerView command line args
 * <p> Added necessary spaces after command line args
 * <p>
 * <p> Revision 1.1  2002/12/31 00:57:08  rickg
 * <p> Initial revision
 * <p>
 * <p> </p>
 */
public class TransferfidParam {
  public static final String rcsid =
    "$Id$";
  String inputImageFile = "";
  String outputImageFile = "";
  String inputModelFile = "";
  String outputModelFile = "";
  String datasetName = "";
  boolean bToA = false;
  boolean runMidas = false;
  int searchDirection = 0; // 0 - both, -1 => -90, 1=> +90  
  int centerViewA = 0; // 0 => default selected by script
  int centerViewB = 0;

  boolean createLog = false;

  public TransferfidParam() {
  }

  /**
   * Get the command string specified by the current state
   */
  public String getCommandString() {
    StringBuffer commandLine = new StringBuffer("transferfid ");

    if (bToA) {
      commandLine.append("-b ");
    }
    if (!inputImageFile.equals("")) {
      commandLine.append("-ia " + inputImageFile + " ");
    }

    if (!outputImageFile.equals("")) {
      commandLine.append("-ib " + outputImageFile + " ");
    }

    if (!inputModelFile.equals("")) {
      commandLine.append("-f " + inputModelFile + " ");
    }

    if (!outputModelFile.equals("")) {
      commandLine.append("-o " + outputModelFile + " ");
    }

    if (centerViewA > 0) {
      commandLine.append("-za " + String.valueOf(centerViewA) + " ");
    }

    if (centerViewB > 0) {
      commandLine.append("-zb " + String.valueOf(centerViewA) + " ");
    }

    if (searchDirection > 0) {
      commandLine.append("-a 90 ");
    }

    if (searchDirection < 0) {
      commandLine.append("-a -90 ");
    }

    if (runMidas) {
      commandLine.append("-m ");
    }

    commandLine.append(datasetName);

    return commandLine.toString();
  }

  /**
   * Returns the inputImageFile.
   * @return String
   */
  public String getInputImageFile() {
    return inputImageFile;
  }

  /**
   * Returns the inputModelFile.
   * @return String
   */
  public String getInputModelFile() {
    return inputModelFile;
  }

  /**
   * Returns the outputImageFile.
   * @return String
   */
  public String getOutputImageFile() {
    return outputImageFile;
  }

  /**
   * Returns the outputModelFile.
   * @return String
   */
  public String getOutputModelFile() {
    return outputModelFile;
  }

  /**
   * Returns the runMidas.
   * @return boolean
   */
  public boolean isRunMidas() {
    return runMidas;
  }

  /**
   * Sets the inputImageFile.
   * @param inputImageFile The inputImageFile to set
   */
  public void setInputImageFile(String inputImageFile) {
    this.inputImageFile = inputImageFile;
  }

  /**
   * Sets the inputModelFile.
   * @param inputModelFile The inputModelFile to set
   */
  public void setInputModelFile(String inputModelFile) {
    this.inputModelFile = inputModelFile;
  }

  /**
   * Sets the outputImageFile.
   * @param outputImageFile The outputImageFile to set
   */
  public void setOutputImageFile(String outputImageFile) {
    this.outputImageFile = outputImageFile;
  }

  /**
   * Sets the outputModelFile.
   * @param outputModelFile The outputModelFile to set
   */
  public void setOutputModelFile(String outputModelFile) {
    this.outputModelFile = outputModelFile;
  }

  /**
   * Sets the runMidas.
   * @param runMidas The runMidas to set
   */
  public void setRunMidas(boolean runMidas) {
    this.runMidas = runMidas;
  }

  /**
   * Returns the setName.
   * @return String
   */
  public String getDatasetName() {
    return datasetName;
  }

  /**
   * Sets the setName.
   * @param setName The setName to set
   */
  public void setDatasetName(String datasetName) {
    this.datasetName = datasetName;
  }

  /**
   * Returns the bToA.
   * @return boolean
   */
  public boolean isBToA() {
    return bToA;
  }

  /**
   * Sets the bToA.
   * @param bToA The bToA to set
   */
  public void setBToA(boolean bToA) {
    this.bToA = bToA;
  }

  /**
   * Returns the centerViewA.
   * @return int
   */
  public int getCenterViewA() {
    return centerViewA;
  }

  /**
   * Returns the centerViewB.
   * @return int
   */
  public int getCenterViewB() {
    return centerViewB;
  }

  /**
   * Returns the searchDirection.
   * @return int
   */
  public int getSearchDirection() {
    return searchDirection;
  }

  /**
   * Sets the centerViewA.
   * @param centerViewA The centerViewA to set
   */
  public void setCenterViewA(int centerViewA) {
    this.centerViewA = centerViewA;
  }

  /**
   * Sets the centerViewB.
   * @param centerViewB The centerViewB to set
   */
  public void setCenterViewB(int centerViewB) {
    this.centerViewB = centerViewB;
  }

  /**
   * Sets the searchDirection.
   * @param searchDirection The searchDirection to set
   */
  public void setSearchDirection(int searchDirection) {
    this.searchDirection = searchDirection;
  }

  /**
   * Returns the createLog.
   * @return boolean
   */
  public boolean isCreateLog() {
    return createLog;
  }

  /**
   * Sets the createLog.
   * @param createLog The createLog to set
   */
  public void setCreateLog(boolean createLog) {
    this.createLog = createLog;
  }

}
