package etomo.type;

import java.io.File;
import java.util.Properties;

import etomo.storage.Storable;

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
* <p> Revision 1.1.2.2  2004/10/06 01:57:57  sueh
* <p> bug# 520 Added Z max from header.  Added descriptions of the fields for
* <p> invalidReason.  Added isValidForMakeSamples() - validate when Make
* <p> Samples is pressed.
* <p>
* <p> Revision 1.1.2.1  2004/09/29 19:26:26  sueh
* <p> bug# 520 Divided the SectionTable row into document and view.  This
* <p> class is the const part of the document.  It implements Storable with
* <p> abstract load functions, has an equals function and get functions.
* <p> </p>
*/
public abstract class ConstSectionTableRowData implements Storable {
  public static  final String  rcsid =  "$Id$";
  
  protected static final String rowNumberString = "RowNumber";
  protected static final String sectionString = "Section";
  protected static final String sampleBottomStartString = "SampleBottomStart";
  protected static final String sampleBottomEndString = "SampleBottomEnd";
  protected static final String sampleTopStartString = "SampleTopStart";
  protected static final String sampleTopEndString = "SampleTopEnd";
  protected static final String finalStartString = "FinalStart";
  protected static final String finalEndString = "FinalEnd";
  protected static final String rotationAngleXString = "RotationAngleX";
  protected static final String rotationAngleYString = "RotationAngleY";
  protected static final String rotationAngleZString = "RotationAngleZ";
  protected static final String zMaxString = "ZMax";
  
  protected static final String sampleBottomStartName = "Starting bottom sample slice";
  protected static final String sampleBottomEndName = "Ending bottom sample slice";
  protected static final String sampleTopStartName = "Starting top sample slice";
  protected static final String sampleTopEndName = "Ending top sample slice";
  protected static final String finalStartName = "Final starting slice";
  protected static final String finalEndName = "Final ending slice";
  protected static final String rotationAngleXName = "X rotation angle";
  protected static final String rotationAngleYName = "Y rotation angle";
  protected static final String rotationAngleZName = "Z rotation angle";
  
  protected int rowNumber;
  protected File section;
  protected int sampleBottomStart;
  protected int sampleBottomEnd;
  protected int sampleTopStart;
  protected int sampleTopEnd;
  protected int finalStart;
  protected int finalEnd;
  protected double rotationAngleX;
  protected double rotationAngleY;
  protected double rotationAngleZ;
  protected int zMax = Integer.MIN_VALUE;
  
  protected StringBuffer invalidReason;
  
  public abstract void load(Properties props);
  public abstract void load(Properties props, String prepend);
  
  public String toString() {
    return getClass().getName() + "[" + paramString() + "]";
  }

  protected String paramString() {
    return ",\n" + rowNumberString + "=" + rowNumber + ",\n" + sectionString
        + "=" + section + ",\n" + sampleBottomStartString + "="
        + sampleBottomStart + ",\n" + sampleBottomEndString + "="
        + sampleBottomEnd + ",\n" + sampleTopStartString + "=" + sampleTopStart
        + ",\n" + sampleTopEndString + "=" + sampleTopEnd + ",\n"
        + finalStartString + "=" + finalStart + ",\n" + finalEndString + "="
        + finalEnd + ",\n" + rotationAngleXString + "=" + rotationAngleX
        + ",\n" + rotationAngleYString + "=" + rotationAngleY + ",\n"
        + rotationAngleZString + "=" + rotationAngleZ + ",\n" + zMaxString + "="
        + zMax;
  } 

  public boolean isValidForMakeSamples(int tableSize) {
    invalidReason = new StringBuffer("Row " + rowNumber + ":  ");
    if (section == null) {
      invalidReason.append("The section has not been set.");
      return false;
    }
    if (!section.exists()) {
      invalidReason.append("The section, " + section.getAbsolutePath() + ", does not exist.");
      return false;
    }
    if (!section.isFile()) {
      invalidReason.append("The section, " + section.getAbsolutePath() + ", is not a file.");
      return false;
    }
    if (!section.canRead()) {
      invalidReason.append("The section, " + section.getAbsolutePath() + ", is not readable.");
      return false;
    }
    if (rowNumber > 1 && !isValidSlice(sampleBottomStart, sampleBottomStartName)) {
      return false;
    }
    if (rowNumber > 1 && !isValidSlice(sampleBottomEnd, sampleBottomEndName)) {
      return false;
    }
    if (rowNumber < tableSize && !isValidSlice(sampleTopStart, sampleTopStartName)) {
      return false;
    }
    if (rowNumber < tableSize && !isValidSlice(sampleTopEnd, sampleTopEndName)) {
      return false;
    }
    invalidReason = null;
    return true;
  }
  
  private boolean isValidSlice(int slice, String description) {
    if (slice == Integer.MIN_VALUE) {
      invalidReason.append(description + " is required.");
      return false;
    }
    if (slice < 1) {
      invalidReason.append(description + " must be greater then 0");
    }
    if (zMax != Integer.MIN_VALUE && slice > zMax) {
      invalidReason.append(description + " cannot be greater then " + zMax);
    }
    return true;
  }
  
  public void store(Properties props) {
    store(props, "");
  }

  public void store(Properties props, String prepend) {
    prepend = createPrepend(prepend);
    String group = prepend + ".";
    
    props.setProperty(group + rowNumberString, Integer.toString(rowNumber));
    props.setProperty(group + zMaxString, Double.toString(zMax));
    props.setProperty(group + sectionString, section.getAbsolutePath());   
    props.setProperty(group + sampleBottomStartString, Integer.toString(sampleBottomStart));
    props.setProperty(group + sampleBottomEndString, Integer.toString(sampleBottomEnd));
    props.setProperty(group + sampleTopStartString, Integer.toString(sampleTopStart));
    props.setProperty(group + sampleTopEndString, Integer.toString(sampleTopEnd));
    props.setProperty(group + finalStartString, Integer.toString(finalStart));
    props.setProperty(group + finalEndString, Integer.toString(finalEnd));
    props.setProperty(group + rotationAngleXString, Double.toString(rotationAngleX));
    props.setProperty(group + rotationAngleYString, Double.toString(rotationAngleY));
    props.setProperty(group + rotationAngleZString, Double.toString(rotationAngleZ));
  }
  
  protected String createPrepend(String prepend) {
    if (prepend == "") {
      return getClass().getName() + ".";
    }
    return prepend + "." + getClass().getName();
  }
  
  public boolean equals(Object object) {
    if (!(object instanceof SectionTableRowData))
      return false;

    SectionTableRowData that = (SectionTableRowData) object;
    
    if (rowNumber != that.rowNumber) {
      return false;
    }
    if (!section.equals(that.section)) {
      return false;
    }
    if (sampleBottomStart != that.sampleBottomStart) {
      return false;
    }
    if (sampleBottomEnd != that.sampleBottomEnd) {
      return false;
    }
    if (sampleTopStart != that.sampleTopStart) {
      return false;
    }
    if (sampleTopEnd != that.sampleTopEnd) {
      return false;
    }
    if (finalStart != that.finalStart) {
      return false;
    }
    if (finalEnd != that.finalEnd) {
      return false;
    }
    if (rotationAngleX != that.rotationAngleX) {
      return false;
    }
    if (rotationAngleY != that.rotationAngleY) {
      return false;
    }
    if (rotationAngleZ != that.rotationAngleZ) {
      return false;
    }
    if (rotationAngleZ != that.rotationAngleZ) {
      return false;
    }
    return true;
  }
  
  private static String convertToString(int value) {
    if (value == Integer.MIN_VALUE) {
      return "";
    }
    return Integer.toString(value);
  }
  
  private static String convertToString(double value) {
    if (Double.isNaN(value)) {
      return "";
    }
    return Double.toString(value);
  }
  
  public String getInvalidReason() {
    return invalidReason.toString();
  }
  
  public int getRowNumber() {
    return rowNumber;
  }
  public String getRowNumberString() {
    return convertToString(rowNumber);
  }
  public int getRowIndex() {
    return rowNumber - 1;
  }
  
  public File getSection() {
    return section;
  }
  public String getSectionAbsolutePath() {
    return section.getAbsolutePath();
  }
  public String getSectionName() {
    return section.getName();
  }
  
  public String getSampleBottomStartString() {
    return convertToString(sampleBottomStart);
  }
  
  public String getSampleBottomEndString() {
    return convertToString(sampleBottomEnd);
  }
  
  public String getSampleTopStartString() {
    return convertToString(sampleTopStart);
  }
  
  public String getSampleTopEndString() {
    return convertToString(sampleTopEnd);
  }
  
  public String getFinalStartString() {
    return convertToString(finalStart);
  }
  
  public String getFinalEndString() {
    return convertToString(finalEnd);
  }
  
  public boolean isRotationAngleSet() {
    return !Double.isNaN(rotationAngleX);
  }
  public String getRotationAngleYString() {
    return convertToString(rotationAngleY);
  }
  
  public String getRotationAngleXString() {
    return convertToString(rotationAngleX);
  }
  
  public String getRotationAngleZString() {
    return convertToString(rotationAngleZ);
  }
}
