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
* <p> $Log$ </p>
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
  
  public abstract void load(Properties props);
  public abstract void load(Properties props, String prepend);
  
  public void store(Properties props) {
    store(props, "");
  }

  public void store(Properties props, String prepend) {
    prepend = createPrepend(prepend);
    String group = prepend + ".";
    
    props.setProperty(group + rowNumberString, Integer.toString(rowNumber));
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
