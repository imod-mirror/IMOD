package etomo.util;

import java.util.Hashtable;
import java.util.Vector;

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
* <p> Revision 1.1.2.2  2004/10/28 22:18:51  sueh
* <p> bug# 520 Clarified code by improving names for member variables, and
* <p> parameters.
* <p>
* <p> Revision 1.1.2.1  2004/09/13 19:18:58  sueh
* <p> bug# 520 A const base class for HashedArray to allow it to be passed
* <p> without allowing it to be changed.  Contains the member variables and all
* <p> functions that do not change them.
* <p> </p>
*/
public class ConstHashedArray {
  public static  final String  rcsid =  "$Id$";
  
  Hashtable map = null;
  Vector keyArray = null;
  
  public ConstHashedArray() {
    map = new Hashtable();
    keyArray = new Vector();
  }
  
  protected ConstHashedArray(Vector keyArray) {
    map = new Hashtable();
    this.keyArray = new Vector(keyArray);
  }
  
  public Object get(UniqueKey key) {
    return map.get(key);
  }
  
  public Object get(int index) {
    UniqueKey key = (UniqueKey) keyArray.get(index);
    if (key == null) {
      return null;
    }
    return map.get(key);
  }
  
  public UniqueKey getKey(int index) {
    return (UniqueKey) keyArray.get(index);
  }
  
  public int size() {
    return keyArray.size();
  }
  
  //FIXME are the elements in the new array copies?  Should they be?  
  //If they aren't
  //copies, should this function be in HashedArray?
  public HashedArray getEmptyHashedArray() {
    return new HashedArray(keyArray);
  }
  
  public String toString() {
    return getClass().getName() + "[" + paramString() + "]";
  }
  
  protected String paramString() {
    StringBuffer buffer = new StringBuffer(",map=");
    for (int i = 0; i < keyArray.size(); i++) {
      UniqueKey key = (UniqueKey) keyArray.get(i);
      buffer.append("\nkey=" + key);
      if (key != null) {
        buffer.append(",value=" + map.get(key));
      }
    }
    return buffer.toString();
  }
}
