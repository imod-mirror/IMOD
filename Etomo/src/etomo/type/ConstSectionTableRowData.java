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
* <p> Revision 1.1.2.10  2004/10/30 02:32:47  sueh
* <p> bug# 520 Converted rotation angles to EtomoSimpleType.
* <p>
* <p> Revision 1.1.2.9  2004/10/29 22:13:21  sueh
* <p> bug# 520  Added remove() to remove section table data
* <p> rows from the meta data file.  This gets rid of deleted rows.
* <p>
* <p> Revision 1.1.2.8  2004/10/25 23:01:37  sueh
* <p> bug# 520 Fixed chunk size by passing the number of rows to
* <p> ConstJoinMetaData.getChunkSize and checking the rowNumber when
* <p> calculating chunk size.  Added xmax and ymax from header.
* <p>
* <p> Revision 1.1.2.7  2004/10/22 21:03:21  sueh
* <p> bug# 520 Simplifying by passing EtomoSimpleType instead of String and
* <p> int in get functions.  Removed validation.  Converted ints to
* <p> EtomoInteger as necessary.
* <p>
* <p> Revision 1.1.2.6  2004/10/22 03:23:19  sueh
* <p> bug# 520 Converted rowNumber to an EtomoInteger.
* <p>
* <p> Revision 1.1.2.5  2004/10/21 02:52:47  sueh
* <p> bug# 520 Added get functions.
* <p>
* <p> Revision 1.1.2.4  2004/10/15 00:18:24  sueh
* <p> bug# 520 Fix createPrepend().  Fix store().  Prevent underflow in
* <p> getRowIndex().
* <p>
* <p> Revision 1.1.2.3  2004/10/08 16:11:24  sueh
* <p> bug# 520 Added toString() and moved initialization of invalidReason to
* <p> SectionTableRowData.reset().
* <p>
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
  
  protected static final String groupString = "SectionTableRow";
  protected static final String sectionString = "Section";
  protected static final String finalStartString = "FinalStart";
  protected static final String finalEndString = "FinalEnd";
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
  
  protected EtomoInteger rowNumber = new EtomoInteger("RowNumber");
  protected File section;
  protected EtomoInteger sampleBottomStart = new EtomoInteger("SampleBottomStart");
  protected EtomoInteger sampleBottomEnd = new EtomoInteger("SampleBottomEnd");
  protected EtomoInteger sampleTopStart = new EtomoInteger("SampleTopStart");
  protected EtomoInteger sampleTopEnd = new EtomoInteger("SampleTopEnd");
  protected int finalStart;
  protected int finalEnd;
  protected EtomoDouble rotationAngleX = new EtomoDouble("RotationAngleX");
  protected EtomoDouble rotationAngleY = new EtomoDouble("RotationAngleY");
  protected EtomoDouble rotationAngleZ = new EtomoDouble("RotationAngleZ");
  protected EtomoInteger xMax = new EtomoInteger("XMax");
  protected EtomoInteger yMax = new EtomoInteger("YMax");
  protected int zMax = Integer.MIN_VALUE;
  
  protected StringBuffer invalidReason;
  
  public abstract void load(Properties props);
  public abstract void load(Properties props, String prepend);
  
  protected ConstSectionTableRowData() {
    rotationAngleX.setDefault(0);
    rotationAngleY.setDefault(0);
    rotationAngleZ.setDefault(0);
  }
  
  public String toString() {
    return getClass().getName() + "[" + paramString() + "]";
  }

  protected String paramString() {
    return ",\n" + rowNumber.getDescription() + "=" + rowNumber.getString()
        + ",\n" + sectionString + "=" + section + ",\n"
        + sampleBottomStart.getDescription() + "="
        + sampleBottomStart.getString() + ",\n"
        + sampleBottomEnd.getDescription() + "=" + sampleBottomEnd.getString()
        + ",\n" + sampleTopStart.getDescription() + "=" + sampleTopStart
        + ",\n" + sampleTopEnd.getDescription() + "="
        + sampleTopEnd.getString() + ",\n" + finalStartString + "="
        + finalStart + ",\n" + finalEndString + "=" + finalEnd + ",\n"
        + rotationAngleX.getDescription() + "=" + rotationAngleX.getString()
        + ",\n" + rotationAngleY.getDescription() + "="
        + rotationAngleY.getString() + ",\n" + rotationAngleZ.getDescription()
        + "=" + rotationAngleZ.getString() + ",\n" + xMax.getDescription()
        + "=" + xMax.getString() + ",\n" + yMax.getDescription() + "="
        + yMax.getString() + ",\n" + zMaxString + "=" + zMax;
  } 
  
  public void store(Properties props) {
    store(props, "");
  }

  public void store(Properties props, String prepend) {
    prepend = createPrepend(prepend);
    String group = prepend + ".";
    rowNumber.store(props, prepend);
    xMax.store(props, prepend);
    yMax.store(props, prepend);
    props.setProperty(group + zMaxString, Integer.toString(zMax));
    props.setProperty(group + sectionString, section.getAbsolutePath());  
    sampleBottomStart.store(props, prepend);
    sampleBottomEnd.store(props, prepend);
    sampleTopStart.store(props, prepend);
    sampleTopEnd.store(props, prepend);
    props.setProperty(group + finalStartString, Integer.toString(finalStart));
    props.setProperty(group + finalEndString, Integer.toString(finalEnd));
    rotationAngleX.store(props, prepend);
    rotationAngleY.store(props, prepend);
    rotationAngleZ.store(props, prepend);
  }
  
  void remove(Properties props, String prepend) {
    prepend = createPrepend(prepend);
    String group = prepend + ".";
    rowNumber.store(props, prepend);
    xMax.remove(props, prepend);
    yMax.remove(props, prepend);
    props.remove(group + zMaxString);
    props.remove(group + sectionString);  
    sampleBottomStart.remove(props, prepend);
    sampleBottomEnd.remove(props, prepend);
    sampleTopStart.remove(props, prepend);
    sampleTopEnd.remove(props, prepend);
    props.remove(group + finalStartString);
    props.remove(group + finalEndString);
    rotationAngleX.remove(props, prepend);
    rotationAngleY.remove(props, prepend);
    rotationAngleZ.remove(props, prepend);
  }

  
  protected String createPrepend(String prepend) {
    if (prepend == "") {
      return groupString + "." + rowNumber.getString();
    }
    return prepend + "." + groupString + "." + rowNumber.getString();
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
  
  public ConstEtomoInteger getRowNumber() {
    return rowNumber;
  }

  public int getRowIndex() {
    if (rowNumber.lessThen(0)) {
      return -1;
    }
    return rowNumber.get() - 1;
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
  
  public ConstEtomoInteger getXMax() {
    return xMax;
  }
  
  public ConstEtomoInteger getYMax() {
    return yMax;
  }
  
  public int getZMax() {
    return zMax;
  }
  
  public EtomoSimpleType getSampleBottomStart() {
    return sampleBottomStart;
  }
  
  public EtomoSimpleType getSampleBottomEnd() {
    return sampleBottomEnd;
  }
  
  public EtomoSimpleType getSampleTopStart() {
    return sampleTopStart;
  }
  
  public EtomoSimpleType getSampleTopEnd() {
    return sampleTopEnd;
  }
  
  public int getSampleBottomNumberSlices() {
    if (sampleBottomEnd.greaterOrEqual(sampleBottomStart)) {
      return sampleBottomEnd.get() - sampleBottomStart.get() + 1;
    }
    return 0;
  }
  
  public int getSampleTopNumberSlices() {
    if (sampleTopEnd.greaterOrEqual(sampleTopStart)) {
      return sampleTopEnd.get() - sampleTopStart.get() + 1;
    }
    return 0;
  }
  
  public ConstEtomoInteger getChunkSize(int tableSize) {
    if (tableSize <= 1) {
      return new EtomoInteger(0);
    }
    if (rowNumber.equals(1)) {
      return new EtomoInteger(getSampleTopNumberSlices());
    }
    if (rowNumber.equals(tableSize)) {
      return new EtomoInteger(getSampleBottomNumberSlices());
    }
    return new EtomoInteger(getSampleBottomNumberSlices() + getSampleTopNumberSlices());
  }
  
  public String getFinalStartString() {
    return convertToString(finalStart);
  }
  
  public String getFinalEndString() {
    return convertToString(finalEnd);
  }
  
  public EtomoSimpleType getRotationAngleX() {
    return rotationAngleX;
  }
  
  public EtomoSimpleType getRotationAngleY() {
    return rotationAngleY;
  }

  public EtomoSimpleType getRotationAngleZ() {
    return rotationAngleZ;
  }
  
}
