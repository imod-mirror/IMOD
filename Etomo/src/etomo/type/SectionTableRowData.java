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
* <p> $Log$ </p>
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
    finalStart = Integer.MIN_VALUE;
    finalEnd = Integer.MIN_VALUE;
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
        Integer.toString(Integer.MIN_VALUE)));
    rotationAngleX = Integer.parseInt(props.getProperty(group
        + rotationAngleXString, Double.toString(Double.NaN)));
    rotationAngleY = Integer.parseInt(props.getProperty(group
        + rotationAngleYString, Double.toString(Double.NaN)));
    rotationAngleZ = Integer.parseInt(props.getProperty(group
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
  
  public void setSampleBottomStart(String sampleBottomStart) {
    this.sampleBottomStart = Integer.parseInt(sampleBottomStart);
  }

  public void setSampleBottomEnd(String sampleBottomEnd) {
    this.sampleBottomEnd = Integer.parseInt(sampleBottomEnd);
  }
  
  public void setSampleTopStart(String sampleTopStart) {
    this.sampleTopStart = Integer.parseInt(sampleTopStart);
  }
  
  public void setSampleTopEnd(String sampleTopEnd) {
    this.sampleTopEnd = Integer.parseInt(sampleTopEnd);
  }
  
  public void setFinalStart(String finalStart) {
    this.finalStart = Integer.parseInt(finalStart);
  }
  
  public void setFinalEnd(String finalEnd) {
    this.finalEnd = Integer.parseInt(finalEnd);
  }
  
  public void setRotationAngleX(String rotationAngleX) {
    this.rotationAngleX = Double.parseDouble(rotationAngleX);
  }
  
  public void setRotationAngleY(String rotationAngleY) {
    this.rotationAngleY = Double.parseDouble(rotationAngleY);
  }
  
  public void setRotationAngleZ(String rotationAngleZ) {
    this.rotationAngleZ = Double.parseDouble(rotationAngleZ);
  }
}