package etomo.type;

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
* <p> Revision 1.1.2.5  2004/10/25 23:06:36  sueh
* <p> bug# 520 Fixed default:  Default doesn't affect the value or the
* <p> resetValue.  Default can returned if value and recommended value are
* <p> not set and the parameter useDefault is true.  When recommended value
* <p> is set, value is changed to recommend value.  Added getNegation().
* <p> Return EtomoSimpleType where possible.
* <p>
* <p> Revision 1.1.2.4  2004/10/22 21:04:20  sueh
* <p> bug# 520 Allowing value initialization in constructor.  Returning this in
* <p> set().
* <p>
* <p> Revision 1.1.2.3  2004/10/22 03:23:53  sueh
* <p> bug# 520 Set value unsetValue when set fails.
* <p>
* <p> Revision 1.1.2.2  2004/10/21 02:53:51  sueh
* <p> bug# 520 Corrected typo.
* <p>
* <p> Revision 1.1.2.1  2004/10/18 18:04:35  sueh
* <p> bug# 520 A class representing a double which handles all issues
* <p> concerning defaults, null values, assigning strings that are blank, and
* <p> handling errors in numeric parsing.  It also implements Storable.
* <p> </p>
*/
public class EtomoDouble extends ConstEtomoDouble {
  public static  final String  rcsid =  "$Id$";
  
  public EtomoDouble() {
    super();
  }
  
  public EtomoDouble(double initialValue) {
    super();
    value = initialValue;
  }
  
  public EtomoDouble(String name) {
    super(name);
  }
  
  public EtomoDouble(String name, double initialValue) {
    super(name);
    value = initialValue;
  }
  
  public void setRecommendedValue(double recommendedValue) {
    this.recommendedValue = recommendedValue;
    setResetValue();
  }
  
  public void setDescription(String description) {
    if (description != null) {
      this.description = description;
    }
    else {
      name = description;
    }
  }
  
  public void load(Properties props) {
    value = Double.parseDouble(props.getProperty(name, Double
        .toString(resetValue)));
  }
  
  public void load(Properties props, String prepend) {
    value = Double.parseDouble(props.getProperty(prepend + "." + name, Double
        .toString(resetValue)));
  }
  
  public EtomoSimpleType set(String value) {
    invalidReason = null;
    if (value == null || !value.matches("\\S+")) {
      this.value = unsetValue;
    }
    else {
      try {
        double parsedValue = Double.parseDouble(value);
        this.value = parsedValue;
      }
      catch (NumberFormatException e) {
        e.printStackTrace();
        invalidReason = "Invalid value:  " + value + ".  " + description + " is a double.";
        this.value = unsetValue;
      }
    }
    return this;
  }
  
  protected EtomoSimpleType set(ConstEtomoDouble that) {
    super.set(that);
    value = that.value;
    defaultValue = that.defaultValue;
    recommendedValue = that.recommendedValue;
    resetValue = that.resetValue;
    return this;
  }
  
  public EtomoSimpleType set(Double value) {
    invalidReason = null;
    this.value = value.doubleValue();
    return this;
  }
  
  public EtomoSimpleType set(double value) {
    invalidReason = null;
    this.value = value;
    return this;
  }
  
  public void reset() {
    value = resetValue;
  }
  
  public void unset() {
    value = unsetValue;
  }
  
  private void setResetValue() {
    if (!Double.isNaN(recommendedValue)) {
      resetValue = recommendedValue;
    }
    else {
      resetValue = unsetValue;
    }
  }

}
