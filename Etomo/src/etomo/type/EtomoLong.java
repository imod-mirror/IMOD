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
* <p> Revision 1.1.2.5  2004/10/30 02:34:55  sueh
* <p> bug# 520 SetRecommendedValue no longer changes value.  This way it
* <p> can be set after initialization.  Added set(ConstEtomoLongto set all
* <p> values from another instance.
* <p>
* <p> Revision 1.1.2.4  2004/10/25 23:07:23  sueh
* <p> bug# 520 Fixed default:  Default doesn't affect the value or the
* <p> resetValue.  Default can returned if value and recommended value are
* <p> not set and the parameter useDefault is true.  When recommended value
* <p> is set, value is changed to recommend value.  Added getNegation().
* <p> Return EtomoSimpleType where possible.
* <p>
* <p> Revision 1.1.2.3  2004/10/22 21:05:13  sueh
* <p> bug# 520 Allowing value initialization in constructor.  Returning this in
* <p> set().
* <p>
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
    
  public void load(Properties props) {
    value = Long.parseLong(props.getProperty(name, Long
        .toString(resetValue)));
  }
  public void load(Properties props, String prepend) {
    value = Long.parseLong(props.getProperty(prepend + "." + name, Long
        .toString(resetValue)));
  }
  
  public EtomoSimpleType set(String value) {
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
    if (isSet() && ceilingValue != Long.MIN_VALUE && this.value > ceilingValue) {
      this.value = ceilingValue;
    }
    return this;
  }
  
  protected EtomoSimpleType set(ConstEtomoLong that) {
    super.set(that);
    value = that.value;
    defaultValue = that.defaultValue;
    recommendedValue = that.recommendedValue;
    resetValue = that.resetValue;
    ceilingValue = that.ceilingValue;
    return this;
  }
  
  public EtomoSimpleType set(Long value) {
    invalidReason = null;
    this.value = value.intValue();
    if (isSet() && ceilingValue != Long.MIN_VALUE && this.value > ceilingValue) {
      this.value = ceilingValue;
    }
    return this;
  }
  
  public EtomoSimpleType set(long value) {
    invalidReason = null;
    this.value = value;
    if (isSet() && ceilingValue != Long.MIN_VALUE && this.value > ceilingValue) {
      this.value = ceilingValue;
    }
    return this;
  }
  
  public EtomoSimpleType reset() {
    value = resetValue;
    if (isSet() && ceilingValue != Long.MIN_VALUE && value > ceilingValue) {
      value = ceilingValue;
    }
    return this;
  }
  
  public EtomoSimpleType unset() {
    value = unsetValue;
    return this;
  }

}
