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
* <p> Revision 1.1.2.1  2004/10/18 18:04:55  sueh
* <p> bug# 520 A class representing a float which handles all issues
* <p> concerning defaults, null values, assigning strings that are blank, and
* <p> handling errors in numeric parsing.  It also implements Storable.
* <p> </p>
*/
public class EtomoFloat extends ConstEtomoFloat {
  public static  final String  rcsid =  "$Id$";
  
  EtomoFloat(String name) {
    this.name = name;
    description = name;
  }
  
  public void setDefaultValue(float value) {
    defaultValue = value;
    setResetValue();
  }
  
  public void setRecommendedValue(float value) {
    recommendedValue = value;
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
  
  public String set(String value) {
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
      }
    }
    return invalidReason;
  }
  
  public String set(Float value) {
    this.value = value.floatValue();
    return null;
  }
  
  public String set(float value) {
    this.value = value;
    return null;
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
    else if (!Float.isNaN(defaultValue)) {
      resetValue = defaultValue;
    }
    else {
      resetValue = unsetValue;
    }
  }
}
