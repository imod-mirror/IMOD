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
* <p> Revision 1.1.2.4  2004/10/15 00:46:03  sueh
* <p> bug# 520 Initializing rowNumber on construction so that it can be used in
* <p> load.
* <p>
* <p> Revision 1.1.2.3  2004/10/08 16:24:37  sueh
* <p> bug# 520 AMoved initialization of invalidReason to
* <p> SectionTableRowData.reset().
* <p>
* <p> Revision 1.1.2.2  2004/10/06 02:18:37  sueh
* <p> bug# 520 Made the defaults for Final start and end based on Z min and
* <p> max.  Saved Z Max.  Added a generic parseInt() function to set
* <p> invalidReason when Integer.parseInt() failed.  Did the same for
* <p> parseDouble.
* <p>
* <p> Revision 1.1.2.1  2004/09/29 19:32:58  sueh
* <p> bug# 520 Divided the SectionTable row into document and view.  This
* <p> class is the non-const part of the document.  It implements the Storable
* <p> load functions, and has set functions.
* <p> </p>
*/
public class SectionTableRowData extends ConstSectionTableRowData {
  public static final String rcsid = "$Id$";
  
  public SectionTableRowData(int rowNumber) {
    reset();
    this.rowNumber.set(rowNumber);
  }
  
  private void reset() {
    invalidReason = null;
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

    rowNumber.load(props, prepend);
    zMax = Integer.parseInt(props.getProperty(group + zMaxString,
        Integer.toString(Integer.MIN_VALUE)));
    String sectionName = props.getProperty(group + sectionString, null);
    if (sectionName != null) {
      section = new File(sectionName);
    }
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
    this.rowNumber.set(rowNumber);
  }
  public void setRowNumber(String rowNumber) {
    this.rowNumber.set(rowNumber);
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