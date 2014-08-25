package etomo.type;
/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright 2014</p>
*
* <p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
* University of Colorado</p>
* 
* @author $Author:$
* 
* @version $Revision:$
* 
* <p> $Log$ </p>
*/
public interface HeaderMetaDataInterface {
  public static final String rcsid = "$Id:$";
  
  public boolean getButtonState(String key, boolean defaultState);
}
