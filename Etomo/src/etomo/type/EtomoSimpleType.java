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
* <p> Revision 1.1.2.1  2004/10/21 02:57:50  sueh
* <p> bug# 520 Interface for EtomoInteger, EtomoDouble, EtomoFloat, and
* <p> EtomoLong.
* <p> </p>
*/
public interface EtomoSimpleType {
  public static  final String  rcsid =  "$Id$";
  
  public void setDescription(String description);
  public String set(String value);
   public void reset();
  public void unset();
  public Number getNumber();
  public String getString();
  public String getDescription();
  public boolean isSetAndNotDefault();
  public boolean isSet();
}
