package etomo.type;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import etomo.ui.JoinDialog;
import etomo.util.Utilities;

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
* <p> Revision 1.1.2.14  2004/11/08 22:22:09  sueh
* <p> bug# 520 Remove default from shift in X and Y.
* <p>
* <p> Revision 1.1.2.13  2004/10/29 22:12:55  sueh
* <p> bug# 520  Added removeSectionTableData() to remove section table data
* <p> rows from the meta data file before adding them.  This gets rid of deleted
* <p> rows.
* <p>
* <p> Revision 1.1.2.12  2004/10/29 01:19:29  sueh
* <p> bug# 520 Removing workingDir.  Calling isValid with workingDir.  Moving
* <p> file validations to Utilities.
* <p>
* <p> Revision 1.1.2.11  2004/10/25 23:09:16  sueh
* <p> bug# 520 Added get functions.
* <p>
* <p> Revision 1.1.2.10  2004/10/22 21:02:12  sueh
* <p> bug# 520 Simplifying by passing EtomoSimpleType instead of String and
* <p> int in get functions.
* <p>
* <p> Revision 1.1.2.9  2004/10/22 03:22:52  sueh
* <p> bug# 520 Reducing the number of ConstJoinMetaData functions by
* <p> passing EtomoInteger, EtomoFloat, etc and using their get() and
* <p> getString() functions.
* <p>
* <p> Revision 1.1.2.8  2004/10/21 02:50:06  sueh
* <p> bug# 520 Added get functions.
* <p>
* <p> Revision 1.1.2.7  2004/10/18 18:01:46  sueh
* <p> bug# 520 Added fields from JoinDialog.  Converted densityRefSection to
* <p> an EtomoInteger.  Added validation checks for rootName and workingDir.
* <p>
* <p> Revision 1.1.2.6  2004/10/15 00:17:15  sueh
* <p> bug# 520 Added toString().  Fixed createPrepend().
* <p>
* <p> Revision 1.1.2.5  2004/10/14 02:28:12  sueh
* <p> bug# 520 Added getWorkingDir().
* <p>
* <p> Revision 1.1.2.4  2004/10/11 02:07:17  sueh
* <p> bug# 520 Fixed a bug in ConstMetaData where the open edf file menu
* <p> item wasn't working because it was validating the propertyUserDir of the
* <p> current manager, not the parent of the edf file being opened.  Now able
* <p> to pass in the edf file to get the parent from to use in validation.
* <p>
* <p> Revision 1.1.2.3  2004/10/06 01:54:45  sueh
* <p> bug# 520 Removed Use density reference checkbox.  Created
* <p> isValidForMakeSamples() which validates for the situation when Make
* <p> Samples is pressed.
* <p>
* <p> Revision 1.1.2.2  2004/10/01 19:45:26  sueh
* <p> bug# 520 Define a new join string that will go in the menu.  Set a file
* <p> extension value.
* <p>
* <p> Revision 1.1.2.1  2004/09/29 19:17:41  sueh
* <p> bug# 520 The const part of the JoinMetaData class.  Implements
* <p> storable with abstract load functions.  Contains member variables and
* <p> get functions.
* <p> </p>
*/
public abstract class ConstJoinMetaData extends BaseMetaData {
  public static final String rcsid = "$Id$";

  protected static final String latestRevisionNumber = "1.0";
  private static final String newJoinTitle = "New Join";

  protected static final String groupString = "Join";
  protected static final String sectionTableDataSizeString = "SectionTableDataSize";
  protected static final String rootNameString = "RootName";
  protected static final String fullLinearTransformationString = "FullLinearTransformation";
  protected static final String rotationTranslationMagnificationString = "RotationTranslationMagnification";
  protected static final String rotationTranslationString = "RotationTranslation";
  protected static final String useAlignmentRefSectionString = "UseAlignmentRefSection";

  protected static final boolean defaultFullLinearTransformation = true;

  protected ArrayList sectionTableData;
  protected String rootName;
  protected EtomoInteger densityRefSection = new EtomoInteger("DensityRefSection");;
  protected EtomoDouble sigmaLowFrequency = new EtomoDouble("SigmaLowFrequency");
  protected EtomoDouble cutoffHighFrequency = new EtomoDouble("CutoffHighFrequency");
  protected EtomoDouble sigmaHighFrequency = new EtomoDouble("SigmaHighFrequency");
  protected boolean fullLinearTransformation;
  protected boolean rotationTranslationMagnification;
  protected boolean rotationTranslation;
  protected boolean useAlignmentRefSection;
  protected EtomoInteger alignmentRefSection = new EtomoInteger("AlignmentRefSection");
  protected EtomoInteger sizeInX = new EtomoInteger("SizeInX");
  protected EtomoInteger sizeInY = new EtomoInteger("SizeInY");
  protected EtomoInteger shiftInX = new EtomoInteger("ShiftInX");
  protected EtomoInteger shiftInY = new EtomoInteger("ShiftInY");
  protected EtomoInteger useEveryNSlices = new EtomoInteger("UseEveryNSlices");
  protected EtomoInteger trialBinning = new EtomoInteger("TrialBinning");

  public abstract void load(Properties props);
  public abstract void load(Properties props, String prepend);

  public ConstJoinMetaData() {
    fileExtension = "ejf";
    densityRefSection.setDefault(1);
    sigmaLowFrequency.setDefault(0);
    cutoffHighFrequency.setDefault(0);
    sigmaHighFrequency.setDefault(0);
    alignmentRefSection.setDefault(1);
    trialBinning.setDefault(1);
    useEveryNSlices.setDefault(1);
    shiftInX.setDefault(0);
    shiftInY.setDefault(0);
    cutoffHighFrequency.setRecommendedValue(0.25);
    sigmaHighFrequency.setRecommendedValue(0.05);
  }
  
  public String toString() {
    return getClass().getName() + "[" + paramString() + "]";
  }

  protected String paramString() {
    StringBuffer buffer = new StringBuffer(super.paramString()
        + ",\nlatestRevisionNumber=" + latestRevisionNumber
        + ",\nnewJoinTitle=" + newJoinTitle + ",\ngroupString=" + groupString
        + ",\n" + densityRefSection.getDescription() + "="
        + densityRefSection.getString() + ",\n" + rootNameString + "="
        + rootName + ",\n" + sigmaLowFrequency.getDescription() + "="
        + sigmaLowFrequency.getString() + ",\n"
        + cutoffHighFrequency.getDescription() + "="
        + cutoffHighFrequency.getString() + ",\n"
        + sigmaHighFrequency.getDescription() + "="
        + sigmaHighFrequency.getString() + ",\n"
        + fullLinearTransformationString + "=" + fullLinearTransformation
        + ",\n" + rotationTranslationMagnificationString + "="
        + rotationTranslationMagnification + ",\n" + rotationTranslationString
        + "=" + rotationTranslation + ",\n" + useAlignmentRefSectionString
        + "=" + useAlignmentRefSection + ",\n"
        + alignmentRefSection.getDescription() + "="
        + alignmentRefSection.getString() + ",\n" + sizeInX.getDescription()
        + "=" + sizeInX.getString() + ",\n" + sizeInY.getDescription() + "="
        + sizeInY.getString() + ",\n" + shiftInX.getDescription() + "="
        + shiftInX.getString() + ",\n" + shiftInY.getDescription() + "="
        + shiftInY.getString() + ",\n" + useEveryNSlices.getDescription() + "="
        + useEveryNSlices.getString() + ",\n" + trialBinning.getDescription() + "="
        + trialBinning.getString());
    if (sectionTableData != null) {
      buffer.append(",\n" + sectionTableDataSizeString + "="
          + sectionTableData.size());
      for (int i = 0; i < sectionTableData.size(); i++) {
        ConstSectionTableRowData row = (ConstSectionTableRowData) sectionTableData
            .get(i);
        buffer.append(row.toString());
      }
    }
    return buffer.toString();
  } 

  public ArrayList getSectionTableData() {
    return sectionTableData;
  }

  public void store(Properties props, String prepend) {
    removeSectionTableData(props, prepend);
    prepend = createPrepend(prepend);
    String group = prepend + ".";
    props.setProperty(group + revisionNumberString, latestRevisionNumber);
    props.setProperty(group + rootNameString, rootName);
    densityRefSection.store(props, prepend);
    props.setProperty(group + sectionTableDataSizeString, Integer
        .toString(sectionTableData.size()));
    sigmaLowFrequency.store(props, prepend);
    cutoffHighFrequency.store(props, prepend);
    sigmaHighFrequency.store(props, prepend);
    props.setProperty(group + fullLinearTransformationString, Boolean.toString(fullLinearTransformation));
    props.setProperty(group + rotationTranslationMagnificationString, Boolean.toString(rotationTranslationMagnification));
    props.setProperty(group + rotationTranslationString, Boolean.toString(rotationTranslation));
    props.setProperty(group + useAlignmentRefSectionString, Boolean.toString(useAlignmentRefSection));
    alignmentRefSection.store(props, prepend);
    sizeInX.store(props, prepend);
    sizeInY.store(props, prepend);
    shiftInX.store(props, prepend);
    shiftInY.store(props, prepend);
    useEveryNSlices.store(props, prepend);
    trialBinning.store(props, prepend);
    if (sectionTableData != null) {
      for (int i = 0; i < sectionTableData.size(); i++) {
        ((SectionTableRowData) sectionTableData.get(i)).store(props, prepend);
      }
    }
  }
  
  public void removeSectionTableData(Properties props, String prepend) {
    prepend = createPrepend(prepend);
    String group = prepend + ".";
    if (sectionTableData != null) {
      for (int i = 0; i < sectionTableData.size(); i++) {
        ((SectionTableRowData) sectionTableData.get(i)).remove(props, prepend);
      }
    }
  }

  protected static String createPrepend(String prepend) {
    if (prepend == "") {
      return groupString;
    }
    return prepend + "." + groupString;
  }

  public boolean isValid(File workingDir) {
    if (workingDir == null) {
      invalidReason = JoinDialog.WORKING_DIRECTORY_TEXT + " is empty.";
      return false;
    }
    if (!workingDir.isDirectory()) {
      invalidReason = workingDir.getAbsolutePath() + ", must be a directory.";
      return false;
    }
    StringBuffer buffer = new StringBuffer();
    if (!Utilities.isValidFile(workingDir, true, true, buffer)) {
      invalidReason = buffer.toString();
    }
    return isValid();
  }
  
  public boolean isValid() {
    if (rootName == null || !rootName.matches("\\S+")) {
      invalidReason = rootNameString + " is empty.";
      return false;
    }
    return true;
  }


  public boolean equals(Object object) {
    if (!super.equals(object)) {
      return false;
    }

    if (!(object instanceof ConstJoinMetaData))
      return false;
    ConstJoinMetaData that = (ConstJoinMetaData) object;

    if ((sectionTableData == null && that.sectionTableData != null)
        || (sectionTableData != null && that.sectionTableData == null)) {
      return false;
    }
    if (sectionTableData.size() != that.sectionTableData.size()) {
      return false;
    }

    for (int i = 0; i < sectionTableData.size(); i++) {
      if (!((SectionTableRowData) sectionTableData.get(i))
          .equals(sectionTableData.get(i))) {
        return false;
      }
    }
    return true;
  }

  public String getRootName() {
    return rootName;
  }
  
  public boolean isRootNameSet() {
    return rootName != null && rootName.matches("\\S+");
  }
  
  public ConstEtomoInteger getDensityRefSection() {
    return densityRefSection;
  }
  
  public ConstEtomoInteger getUseEveryNSlices() {
    return useEveryNSlices;
  }
  
  public ConstEtomoInteger getTrialBinning() {
    return trialBinning;
  }

  public String getMetaDataFileName() {
    if (rootName.equals("")) {
      return null;
    }
    return rootName + "." + fileExtension;
  }

  public String getName() {
    if (rootName.equals("")) {
      return newJoinTitle;
    }
    return rootName;
  }
  
  public EtomoSimpleType getSigmaLowFrequency() {
    return sigmaLowFrequency;
  }
  
  public EtomoSimpleType getCutoffHighFrequency() {
    return cutoffHighFrequency;
  }
  
  public EtomoSimpleType getSigmaHighFrequency() {
    return sigmaHighFrequency;
  }

  public static String getNewFileTitle() {
    return newJoinTitle;
  }
  
  public boolean isFullLinearTransformation() {
    return fullLinearTransformation;
  }
  
  public boolean isRotationTranslationMagnification() {
    return rotationTranslationMagnification;
  }
  
  public boolean isRotationTranslation() {
    return rotationTranslation;
  }
  
  public boolean isUseAlignmentRefSection() {
    return useAlignmentRefSection;
  }
  
  public EtomoSimpleType getAlignmentRefSection() {
    return alignmentRefSection;
  }
  
  public ConstEtomoInteger getSizeInX() {
    return sizeInX;
  }
  
  public ConstEtomoInteger getSizeInY() {
    return sizeInY;
  }
  
  public boolean getUseAlignmentRefSection() {
    return useAlignmentRefSection;
  }
  
  public ConstEtomoInteger getShiftInX() {
    return shiftInX;
  }
  
  public ConstEtomoInteger getShiftInY() {
    return shiftInY;
  }
  
}