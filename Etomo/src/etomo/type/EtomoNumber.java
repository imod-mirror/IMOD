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
* <p> $Log$ </p>
*/
public class EtomoNumber extends ConstEtomoNumber {
  public static  final String  rcsid =  "$Id$";
  
  public EtomoNumber(int type) {
    super(type);
  }
  
  public EtomoNumber(int type, String name) {
    super(type, name);
  }
  
  public EtomoNumber(int type, int initialValue) {
    super(type, initialValue);
  }
  
  public EtomoNumber(ConstEtomoNumber that) {
    super(that);
  }
  
  public void load(Properties props) {
    set(props.getProperty(name, resetValue.toString()));
  }
  public void load(Properties props, String prepend) {
    set(props.getProperty(prepend + "." + name, resetValue.toString()));
  }
  
  public EtomoNumber set(String value) {
    invalidReason = null;
    if (value == null || !value.matches("\\S+")) {
      this.value = newNumber();
    }
    else {
      StringBuffer invalidBuffer = new StringBuffer();
      this.value = newNumber(value, invalidBuffer);
      if (invalidBuffer.length() > 0) {
        invalidReason = invalidBuffer.toString();
        this.value = newNumber();
      }
    }
    if (!isNull(ceilingValue) && !isNull() && gt(this.value, ceilingValue)) {
      this.value = newNumber(ceilingValue);
    }
    return this;
  }
  
  public EtomoNumber set(Object value) {
    Number setValue = (Number) value;
    invalidReason = null;
    if (value == null) {
      this.value = newNumber();
    }
    else if (!isNull(ceilingValue) && !isNull(setValue) && gt(setValue, ceilingValue)) {
      this.value = newNumber(ceilingValue);
    }
    else {
      this.value = newNumber(setValue);
    }
    return this;
  }
  
  public EtomoNumber set(int value) {
    invalidReason = null;
    this.value = newNumber(value);
    if (!isNull(ceilingValue) && !isNull() && gt(this.value, ceilingValue)) {
      this.value = newNumber(ceilingValue);
    }
    return this;
  }
  
  public EtomoNumber set(long value) {
    invalidReason = null;
    this.value = newNumber(value);
    if (!isNull(ceilingValue) && !isNull() && gt(this.value, ceilingValue)) {
      this.value = newNumber(ceilingValue);
    }
    return this;
  }
  
  public EtomoNumber reset() {
    value = newNumber(resetValue);
    return this;
  }
  
}
