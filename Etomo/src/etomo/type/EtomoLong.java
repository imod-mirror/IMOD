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
* <p> Revision 1.1.2.2  2004/10/22 03:25:19  sueh
* <p> bug# 520 Set value unsetValue when set(String) fails.
* <p>
* <p> Revision 1.1.2.1  2004/10/21 02:57:05  sueh
* <p> bug# 520 EtomoSimpleType which encapsulates long.
* <p> </p>
*/
public class EtomoLong extends ConstEtomoLong {
  public static  final String  rcsid =  "$Id$";
  
  public EtomoLong() {
    super();
  }
  
  public EtomoLong(long initialValue) {
    super();
    value = initialValue;
  }
  
  public EtomoLong(String name) {
    super(name);
  }
  
  public EtomoLong(String name, long initialValue) {
    super(name);
    value = initialValue;
  }
  
  public void setDefaultValue(long value) {
    defaultValue = value;
    setResetValue();
  }
  
  public void setRecommendedValue(long value) {
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
    value = Long.parseLong(props.getProperty(name, Long
        .toString(resetValue)));
  }
  public void load(Properties props, String prepend) {
    value = Long.parseLong(props.getProperty(prepend + "." + name, Long
        .toString(resetValue)));
  }
  
  public EtomoLong set(String value) {
    invalidReason = null;
    if (value == null || !value.matches("\\S+")) {
      this.value = unsetValue;
    }
    else {
      try {
        long parsedValue = Long.parseLong(value);
        this.value = parsedValue;
      }
      catch (NumberFormatException e) {
        e.printStackTrace();
        invalidReason = "Invalid value:  " + value + ".  " + description + " is an integer.";
        this.value = unsetValue;
      }
    }
    return this;
  }
  
  public EtomoLong set(Long value) {
    invalidReason = null;
    this.value = value.intValue();
    return this;
  }
  
  public EtomoLong set(long value) {
    invalidReason = null;
    System.out.println("value=" + value);
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
