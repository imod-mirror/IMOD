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
* <p> Revision 1.1.2.6  2004/10/30 02:34:24  sueh
* <p> bug# 520 SetRecommendedValue no longer changes value.  This way it
* <p> can be set after initialization.  Added set(ConstEtomoFloat) to set all
* <p> values from another instance.
* <p>
* <p> Revision 1.1.2.5  2004/10/25 23:07:02  sueh
* <p> bug# 520 Fixed default:  Default doesn't affect the value or the
* <p> resetValue.  Default can returned if value and recommended value are
* <p> not set and the parameter useDefault is true.  When recommended value
* <p> is set, value is changed to recommend value.  Added getNegation().
* <p> Return EtomoSimpleType where possible.
* <p>
* <p> Revision 1.1.2.4  2004/10/22 21:04:32  sueh
* <p> bug# 520 Allowing value initialization in constructor.  Returning this in
* <p> set().
* <p>
* <p> Revision 1.1.2.3  2004/10/22 03:24:04  sueh
* <p> bug# 520 Set value unsetValue when set fails.
* <p>
* <p> Revision 1.1.2.2  2004/10/21 02:55:45  sueh
* <p> bug# 520 Pulled out const part of object.
* <p>
* <p> Revision 1.1.2.1  2004/10/18 18:04:55  sueh
* <p> bug# 520 A class representing a float which handles all issues
* <p> concerning defaults, null values, assigning strings that are blank, and
* <p> handling errors in numeric parsing.  It also implements Storable.
* <p> </p>
*/
public class EtomoFloat extends ConstEtomoFloat {
  public static  final String  rcsid =  "$Id$";
  
  public EtomoFloat() {
    super();
  }
  
  public EtomoFloat(float initialValue) {
    super();
    value = initialValue;
  }
  
  public EtomoFloat(String name) {
    super(name);
  }
  
  public EtomoFloat(String name, float initialValue) {
    super(name);
    value = initialValue;
  }
  
  public void load(Properties props) {
    value = Float.parseFloat(props.getProperty(name, Float
        .toString(resetValue)));
  }
  
  public void load(Properties props, String prepend) {
    String valueString = props.getProperty(prepend + "." + name, Float
        .toString(resetValue));
    value = Float.parseFloat(valueString);
  }
  
  public EtomoSimpleType set(String value) {
    invalidReason = null;
    if (value == null || !value.matches("\\S+")) {
      this.value = unsetValue;
    }
    else {
      try {
        float parsedValue = Float.parseFloat(value);
        this.value = parsedValue;
      }
      catch (NumberFormatException e) {
        e.printStackTrace();
        invalidReason = "Invalid value:  " + value + ".  " + description + " is a float.";
        this.value = unsetValue;
      }
    }
    if (isSet() && !Float.isNaN(ceilingValue) && this.value > ceilingValue) {
      this.value = ceilingValue;
    }
    return this;
  }
  
  protected EtomoSimpleType set(ConstEtomoFloat that) {
    super.set(that);
    value = that.value;
    defaultValue = that.defaultValue;
    recommendedValue = that.recommendedValue;
    resetValue = that.resetValue;
    ceilingValue = that.ceilingValue;
    return this;
  }
  
  public EtomoSimpleType set(Float value) {
    invalidReason = null;
    this.value = value.floatValue();
    if (isSet() && !Float.isNaN(ceilingValue) && this.value > ceilingValue) {
      this.value = ceilingValue;
    }
    return this;
  }
  
  public EtomoSimpleType set(float value) {
    invalidReason = null;
    this.value = value;
    if (isSet() && !Float.isNaN(ceilingValue) && this.value > ceilingValue) {
      this.value = ceilingValue;
    }
    return this;
  }
  
  public EtomoSimpleType reset() {
    value = resetValue;
    if (isSet() && !Float.isNaN(ceilingValue) && value > ceilingValue) {
      value = ceilingValue;
    }
    return this;
  }
  
  public EtomoSimpleType unset() {
    value = unsetValue;
    return this;
  }

}
