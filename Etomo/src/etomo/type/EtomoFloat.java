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
  
  public void setRecommendedValue(float recommendedValue) {
    this.recommendedValue = recommendedValue;
    value = recommendedValue;
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
    return this;
  }
  
  public EtomoSimpleType set(Float value) {
    invalidReason = null;
    this.value = value.floatValue();
    return this;
  }
  
  public EtomoSimpleType set(float value) {
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
    if (!Float.isNaN(recommendedValue)) {
      resetValue = recommendedValue;
    }
    else {
      resetValue = unsetValue;
    }
  }
}
