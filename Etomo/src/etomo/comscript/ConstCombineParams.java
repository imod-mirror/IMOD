package etomo.comscript;

import java.util.ArrayList;

import etomo.type.CombinePatchSize;
import etomo.type.FiducialMatch;

/**
 * <p>Description: A read only model of the parameter interface for the
 * setupcombine script</p>
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
 * <p> Revision 1.4  2002/10/09 17:28:37  rickg
 * <p> Fixed javadoc tag
 * <p>
 * <p> Revision 1.3  2002/10/08 23:59:22  rickg
 * <p> Added isPatchBoundarySet method
 * <p> Added basic isValid method
 * <p>
 * <p> Revision 1.2  2002/10/03 03:59:31  rickg
 * <p> Added path X,Y,Z min and max attributes
 * <p>
 * <p> Revision 1.1  2002/09/09 22:57:02  rickg
 * <p> Initial CVS entry, basic functionality not including combining
 * <p> </p>
 */

public class ConstCombineParams {
  public static final String rcsid =
    "$Id$";

  protected String revisionNumber = "1.0";

  protected boolean matchBtoA = true;
  protected FiducialMatch fiducialMatch = FiducialMatch.BOTH_SIDES;
  protected StringList fiducialMatchListA = new StringList(0);
  protected StringList fiducialMatchListB = new StringList(0);
  protected CombinePatchSize patchSize = CombinePatchSize.MEDIUM;
  protected int patchXMin = 0;
  protected int patchXMax = 0;
  protected int patchYMin = 0;
  protected int patchYMax = 0;
  protected int patchZMin = 0;
  protected int patchZMax = 0;

  protected String patchRegionModel = "";
  protected String tempDirectory = "";
  protected boolean manualCleanup = false;

  protected ArrayList invalidReasons = new ArrayList();

  public ConstCombineParams() {

  }

  /**
   * Returns true if the patch boundary values have been modified
   */
  public boolean isPatchBoundarySet() {
    if (patchXMin == 0
      && patchXMax == 0
      && patchYMin == 0
      && patchYMax == 0
      && patchZMin == 0
      && patchZMax == 0) {
      return false;
    }
    return true;
  }

  /**
   * Checks the validity of the attribute values.
   * @return true if all entries are valid, otherwise the reasons are 
   * available through the method getInvalidReasons.
   */
  public boolean isValid() {
    boolean valid = true;
    //  Clear any previous reasons from the list
    invalidReasons.clear();

    if (patchXMin < 1) {
      valid = false;
      invalidReasons.add("X min value is less than 1");
    }
    if (patchXMax < 1) {
      valid = false;
      invalidReasons.add("X max value is less than 1");
    }
    if (patchXMin > patchXMax) {
      valid = false;
      invalidReasons.add("X min value is greater than the X max value");
    }

    if (patchYMin < 1) {
      valid = false;
      invalidReasons.add("Y min value is less than 1");
    }
    if (patchYMax < 1) {
      valid = false;
      invalidReasons.add("Y max value is less than 1");
    }
    if (patchYMin > patchYMax) {
      valid = false;
      invalidReasons.add("Y min value is greater than the Y max value");
    }

    if (patchZMin < 1) {
      valid = false;
      invalidReasons.add("Z min value is less than 1");
    }
    if (patchZMax < 1) {
      valid = false;
      invalidReasons.add("ZX max value is less than 1");
    }
    if (patchZMin > patchZMax) {
      valid = false;
      invalidReasons.add("Z min value is greater than the Z max value");
    }
    return valid;
  }

  /**
   * Returns the reasons the attribute values are invalid as a string array.
   */
  public String[] getInvalidReasons() {
    return (String[]) invalidReasons.toArray(new String[invalidReasons.size()]);
  }

  public String getRevisionNumber() {
    return revisionNumber;
  }

  public boolean getMatchBtoA() {
    return matchBtoA;
  }

  public FiducialMatch getFiducialMatch() {
    return fiducialMatch;
  }

  public String getFiducialMatchListA() {
    return fiducialMatchListA.toString();
  }

  public String getFiducialMatchListB() {
    return fiducialMatchListB.toString();
  }

  public CombinePatchSize getPatchSize() {
    return patchSize;
  }

  public String getPatchRegionModel() {
    return patchRegionModel;
  }

  public String getTempDirectory() {
    return tempDirectory;
  }

  public boolean getManualCleanup() {
    return manualCleanup;
  }
  /**
   * Returns the patchXMax.
   * @return int
   */
  public int getPatchXMax() {
    return patchXMax;
  }

  /**
   * Returns the patchXMin.
   * @return int
   */
  public int getPatchXMin() {
    return patchXMin;
  }

  /**
   * Returns the patchYMax.
   * @return int
   */
  public int getPatchYMax() {
    return patchYMax;
  }

  /**
   * Returns the patchYMin.
   * @return int
   */
  public int getPatchYMin() {
    return patchYMin;
  }

  /**
   * Returns the patchZMax.
   * @return int
   */
  public int getPatchZMax() {
    return patchZMax;
  }

  /**
   * Returns the patchZMin.
   * @return int
   */
  public int getPatchZMin() {
    return patchZMin;
  }

}
