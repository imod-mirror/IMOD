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
* <p> Revision 1.1.2.2  2004/10/21 02:56:11  sueh
* <p> bug# 520 Made constructor public.
* <p>
* <p> Revision 1.1.2.1  2004/10/18 18:05:16  sueh
* <p> bug# 520 A class representing an integer which handles all issues
* <p> concerning defaults, null values, assigning strings that are blank, and
* <p> handling errors in numeric parsing.  It also implements Storable.
* <p> </p>
*/
public class EtomoInteger extends ConstEtomoInteger {
  public static  final String  rcsid =  "$Id$";
  
  public EtomoInteger() {
    name = super.toString();
    description = name;
  }
  
  public EtomoInteger(int defaultValue) {
    name = super.toString();
    description = name;
    setDefaultValue(defaultValue);
  }
  
  public EtomoInteger(String name) {
    this.name = name;
    description = name;
  }
  
  public void setDefaultValue(int value) {
    defaultValue = value;
    setResetValue();
  }
  
  public void setRecommendedValue(int value) {
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
    value = Integer.parseInt(props.getProperty(name, Integer
        .toString(resetValue)));
  }
  public void load(Properties props, String prepend) {
    value = Integer.parseInt(props.getProperty(prepend + "." + name, Integer
        .toString(resetValue)));
  }
  
  public String set(String value) {
    invalidReason = null;
    if (value == null || !value.matches("\\S+")) {
      this.value = unsetValue;
    }
    else {
      try {
        int parsedValue = Integer.parseInt(value);
        this.value = parsedValue;
      }
      catch (NumberFormatException e) {
        e.printStackTrace();
        invalidReason = "Invalid value:  " + value + ".  " + description + " is an integer.";
        this.value = unsetValue;
      }
    }
    return invalidReason;
  }
  
  public String set(Integer value) {
    this.value = value.intValue();
    return null;
  }
  
  public String set(int value) {
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
    if (recommendedValue != unsetValue) {
      resetValue = recommendedValue;
    }
    else if (defaultValue != unsetValue) {
      resetValue = defaultValue;
    }
    else {
      resetValue = unsetValue;
    }
  }
}
