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
* <p> Revision 1.1.2.7  2004/11/08 22:22:55  sueh
* <p> bug# 520 set() returns ConstEtomoInteger so the result has more functionality.
* <p>
* <p> Revision 1.1.2.6  2004/10/30 02:34:38  sueh
* <p> bug# 520 SetRecommendedValue no longer changes value.  This way it
* <p> can be set after initialization.  Added set(ConstEtomoInteger to set all
* <p> values from another instance.
* <p>
* <p> Revision 1.1.2.5  2004/10/25 23:07:13  sueh
* <p> bug# 520 Fixed default:  Default doesn't affect the value or the
* <p> resetValue.  Default can returned if value and recommended value are
* <p> not set and the parameter useDefault is true.  When recommended value
* <p> is set, value is changed to recommend value.  Added getNegation().
* <p> Return EtomoSimpleType where possible.
* <p>
* <p> Revision 1.1.2.4  2004/10/22 21:04:59  sueh
* <p> bug# 520 Allowing value initialization in constructor.  Returning this in
* <p> set().  Added plus().
* <p>
* <p> Revision 1.1.2.3  2004/10/22 03:24:58  sueh
* <p> bug# 520 Added parameterless constructor and construstor which sets the
* <p> default value.
* <p>
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
    super();
  }
  
  public EtomoInteger(int initialValue) {
    super();
    value = initialValue;
  }
  
  public EtomoInteger(String name) {
    super(name);
  }
  
  public EtomoInteger(String name, int initialValue) {
    super(name);
    value = initialValue;
  }
    
  public void load(Properties props) {
    value = Integer.parseInt(props.getProperty(name, Integer
        .toString(resetValue)));
  }
  public void load(Properties props, String prepend) {
    value = Integer.parseInt(props.getProperty(prepend + "." + name, Integer
        .toString(resetValue)));
  }
  
  public ConstEtomoInteger set(String value) {
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
    if (isSet() && ceilingValue != Integer.MIN_VALUE && this.value > ceilingValue) {
      this.value = ceilingValue;
    }
    return this;
  }
  
  protected EtomoSimpleType set(ConstEtomoInteger that) {
    super.set(that);
    value = that.value;
    defaultValue = that.defaultValue;
    recommendedValue = that.recommendedValue;
    resetValue = that.resetValue;
    ceilingValue = that.ceilingValue;
    return this;
  }
  
  public EtomoSimpleType set(Integer value) {
    invalidReason = null;
    this.value = value.intValue();
    if (isSet() && ceilingValue != Integer.MIN_VALUE && this.value > ceilingValue) {
      this.value = ceilingValue;
    }
    return this;
  }
  
  public EtomoSimpleType set(int value) {
    invalidReason = null;
    this.value = value;
    if (isSet() && ceilingValue != Integer.MIN_VALUE && this.value > ceilingValue) {
      this.value = ceilingValue;
    }
    return this;
  }
  
  public EtomoSimpleType reset() {
    value = resetValue;
    if (isSet() && ceilingValue != Integer.MIN_VALUE && value > ceilingValue) {
      value = ceilingValue;
    }
    return this;
  }
  
  public EtomoSimpleType unset() {
    value = unsetValue;
    return this;
  }
  
}
