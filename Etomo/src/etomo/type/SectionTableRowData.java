package etomo.type;

import java.io.File;
import java.util.Properties;

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
* <p> Revision 1.1.2.1  2004/09/29 19:32:58  sueh
* <p> bug# 520 Divided the SectionTable row into document and view.  This
* <p> class is the non-const part of the document.  It implements the Storable
* <p> load functions, and has set functions.
* <p> </p>
*/
public class SectionTableRowData extends ConstSectionTableRowData {
  public static final String rcsid = "$Id$";
  
  public SectionTableRowData() {
    reset();
  }
  
  private void reset() {
    rowNumber = Integer.MIN_VALUE;
    section = null;
    sampleBottomStart = Integer.MIN_VALUE;
    sampleBottomEnd = Integer.MIN_VALUE;
    sampleTopStart = Integer.MIN_VALUE;
    sampleTopEnd = Integer.MIN_VALUE;
    finalStart = 1;
    finalEnd = zMax;
    rotationAngleX = Double.NaN;
    rotationAngleY = Double.NaN;
    rotationAngleZ = Double.NaN;
  }
  
  /**
   *  Get the objects attributes from the properties object.
   */
  public void load(Properties props) {
    load(props, "");
  }

  public void load(Properties props, String prepend) {
    reset();
    prepend = createPrepend(prepend);
    String group = prepend + ".";

    rowNumber = Integer.parseInt(props.getProperty(group + rowNumberString,
        Integer.toString(Integer.MIN_VALUE)));
    zMax = Integer.parseInt(props.getProperty(group + zMaxString,
        Integer.toString(Integer.MIN_VALUE)));
    section = new File(props.getProperty(group + sectionString));
    sampleBottomStart = Integer.parseInt(props.getProperty(group
        + sampleBottomStartString, Integer.toString(Integer.MIN_VALUE)));
    sampleBottomEnd = Integer.parseInt(props.getProperty(group
        + sampleBottomEndString, Integer.toString(Integer.MIN_VALUE)));
    sampleTopStart = Integer.parseInt(props.getProperty(group
        + sampleTopStartString, Integer.toString(Integer.MIN_VALUE)));
    sampleTopEnd = Integer.parseInt(props.getProperty(group
        + sampleTopEndString, Integer.toString(Integer.MIN_VALUE)));
    finalStart = Integer.parseInt(props.getProperty(group + finalStartString,
        Integer.toString(Integer.MIN_VALUE)));
    finalEnd = Integer.parseInt(props.getProperty(group + finalEndString,
        Integer.toString(zMax)));
    rotationAngleX = Double.parseDouble(props.getProperty(group
        + rotationAngleXString, Double.toString(Double.NaN)));
    rotationAngleY = Double.parseDouble(props.getProperty(group
        + rotationAngleYString, Double.toString(Double.NaN)));
    rotationAngleZ = Double.parseDouble(props.getProperty(group
        + rotationAngleZString, Double.toString(Double.NaN)));
  }
  
  
  public void setRowNumber(int rowNumber) {
    this.rowNumber = rowNumber;
  }
  public void setRowNumber(String rowNumber) {
    this.rowNumber = Integer.parseInt(rowNumber);
  }
  
  public void setSection(File section) {
    this.section = section;
  }
  
  public void setZMax(int zMax) {
    this.zMax = zMax;
    finalEnd = zMax;
  }
  
  public int parseInt(String value, String valueName) {
    invalidReason = null;
    int intValue;
    if (value == null || !value.matches("\\S+")) {
      return Integer.MIN_VALUE;
    }
    try {
      intValue = Integer.parseInt(value);
    }
    catch (NumberFormatException e) {
      e.printStackTrace();
      invalidReason = new StringBuffer("Row " + rowNumber + ":  " + valueName
          + " must be an integer.");
      return Integer.MIN_VALUE;
    }
    return intValue;
  }

  public double parseDouble(String value, String valueName) {
    invalidReason = null;
    double doubleValue;
    if (value == null || !value.matches("\\S+")) {
      return Double.NaN;
    }
    try {
      doubleValue = Double.parseDouble(value);
    }
    catch (NumberFormatException e) {
      e.printStackTrace();
      invalidReason = new StringBuffer("Row " + rowNumber + ":  " + valueName
          + " must be a number.");
      return Double.NaN;
    }
    return doubleValue;
  }

  public boolean setSampleBottomStart(String sampleBottomStart) {
    this.sampleBottomStart = parseInt(sampleBottomStart, sampleBottomStartName);
    if (invalidReason != null) {
      return false;
    }
    return true;
  }

  public boolean setSampleBottomEnd(String sampleBottomEnd) {
    this.sampleBottomEnd = parseInt(sampleBottomEnd, sampleBottomEndName);
    if (invalidReason != null) {
      return false;
    }
    return true;
  }
  
  public boolean setSampleTopStart(String sampleTopStart) {
    this.sampleTopStart = parseInt(sampleTopStart, sampleTopStartName);
    if (invalidReason != null) {
      return false;
    }
    return true;
  }
  
  public boolean setSampleTopEnd(String sampleTopEnd) {
    this.sampleTopEnd = parseInt(sampleTopEnd, sampleTopEndName);
    if (invalidReason != null) {
      return false;
    }
    return true;
  }
  
  public boolean setFinalStart(String finalStart) {
    this.finalStart = parseInt(finalStart, finalStartName);
    if (invalidReason != null) {
      return false;
    }
    return true;
  }
  
  public boolean setFinalEnd(String finalEnd) {
    this.finalEnd = parseInt(finalEnd, finalEndName);
    if (invalidReason != null) {
      return false;
    }
    return true;
  }
  
  public boolean setRotationAngleX(String rotationAngleX) {
    this.rotationAngleX = parseDouble(rotationAngleX, rotationAngleXName);
    if (invalidReason != null) {
      return false;
    }
    return true;
  }
  
  public boolean setRotationAngleY(String rotationAngleY) {
    this.rotationAngleY = parseDouble(rotationAngleY, rotationAngleYName);
    if (invalidReason != null) {
      return false;
    }
    return true;
  }
  
  public boolean setRotationAngleZ(String rotationAngleZ) {
    this.rotationAngleZ = parseDouble(rotationAngleZ, rotationAngleZName);
    if (invalidReason != null) {
      return false;
    }
    return true;
  }
}