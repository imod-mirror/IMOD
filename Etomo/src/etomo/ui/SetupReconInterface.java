package etomo.ui;

import etomo.storage.DirectiveFileCollection;
import etomo.type.AxisID;
import etomo.type.TiltAngleSpec;
import etomo.type.UserConfiguration;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright 2012 - 2015 by the Regents of the University of Colorado</p>
* <p/>
* <p>Organization: Dept. of MCD Biology, University of Colorado</p>
*
* @version $Id$
*/
public interface SetupReconInterface {
  public void setBinning(int input);

  public void setImageRotation(String input);

  public void setPixelSize(double input);

  public String getDataset();

  public boolean isDualAxisSelected();

  public String getDistortionFile();

  public String getMagGradientFile();

  public boolean validateTiltAngle(AxisID axisID, String errorTitle);

  public boolean isSingleViewSelected();

  public String getBackupDirectory();

  public String getBinning();

  public String getExcludeList(AxisID axisID, boolean doValidation)
    throws FieldValidationFailedException;

  public String getTwodir(AxisID axisID, boolean doValidation)
    throws FieldValidationFailedException;

  public boolean isTwodir(AxisID axisID);

  public void setTwodir(AxisID axisID, double input);

  public String getFiducialDiameter(boolean doValidation)
    throws FieldValidationFailedException;

  public String getImageRotation(AxisID axisID, boolean doValidation)
    throws FieldValidationFailedException;

  public String getPixelSize(boolean doValidation) throws FieldValidationFailedException;

  public boolean isAdjustedFocusSelected(AxisID axisID);

  public boolean isSingleAxisSelected();

  public boolean isGpuProcessingSelected(String propertyUserDir);

  public boolean isParallelProcessSelected(String propertyUserDir);

  public boolean getTiltAngleFields(AxisID axisID, TiltAngleSpec tiltAngleSpec,
    boolean doValidation);

  public DirectiveFileCollection getDirectiveFileCollection();

  public void initTiltAngleFields(AxisID axisID, TiltAngleSpec tiltAngleSpec,
    UserConfiguration userConfiguration);
}
