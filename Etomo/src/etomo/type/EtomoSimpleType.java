package etomo.type;

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
* <p> Revision 1.1.2.5  2004/10/30 02:35:44  sueh
* <p> bug# 520 Added set(EtomoSimpleType) to set all values from another
* <p> instance.
* <p>
* <p> Revision 1.1.2.4  2004/10/25 23:08:16  sueh
* <p> bug# 520 Added abstract functions getString(boolean),
* <p> getNumber(boolean), and getNegation().
* <p>
* <p> Revision 1.1.2.3  2004/10/22 21:05:45  sueh
* <p> bug# 520 Converted to an abstract class.
* <p>
* <p> Revision 1.1.2.2  2004/10/22 03:25:33  sueh
* <p> bug# 520 added getNumber().
* <p>
* <p> Revision 1.1.2.1  2004/10/21 02:57:50  sueh
* <p> bug# 520 Interface for EtomoInteger, EtomoDouble, EtomoFloat, and
* <p> EtomoLong.
* <p> </p>
*/
public abstract class EtomoSimpleType {
  public static  final String  rcsid =  "$Id$";
  
  protected String name;
  protected String description = null;
  protected String invalidReason = null;
  
  public abstract String getString();
  public abstract String getString(boolean useDefault);
  public abstract boolean isSetAndNotDefault();
  public abstract boolean isSet();
  public abstract Number getNumber();
  public abstract Number getNumber(boolean useDefault);
  
  protected EtomoSimpleType() {
    name = super.toString();
    description = name;
  }
  
  protected EtomoSimpleType(String name) {
    this.name = name;
    description = name;
  }
  
  protected EtomoSimpleType set(EtomoSimpleType that) {
    name = that.name;
    description = that.description;
    invalidReason = that.invalidReason;
    return this;
  }
  
  public String getDescription() {
    return description;
  }
  
  public boolean isValid() {
    return invalidReason == null;
  }
  
  public String getInvalidReason() {
    return invalidReason;
  }
  
  public String classInfoString() {
    return getClass().getName() + "[" + paramString() + "]";
  }
  
  protected String paramString() {
    return ",\nname=" + name + ",\ndescription=" + description
        + ",\ninvalidReason=" + invalidReason;
  }
}
