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
* <p> $Log$ </p>
*/
public class ConstHashedArray {
  public static  final String  rcsid =  "$Id$";
  
  Hashtable map = null;
  Vector array = null;
  
  public ConstHashedArray() {
    map = new Hashtable();
    array = new Vector();
  }
  
  protected ConstHashedArray(Vector array) {
    map = new Hashtable();
    this.array = new Vector(array);
  }
  
  public Object get(UniqueKey key) {
    return map.get(key);
  }
  
  public Object get(int keyIndex) {
    UniqueKey key = (UniqueKey) array.get(keyIndex);
    if (key == null) {
      return null;
    }
    return map.get(key);
  }
  
  public UniqueKey getKey(int index) {
    return (UniqueKey) array.get(index);
  }
  
  public int size() {
    return array.size();
  }
  
  //FIXME are the elements in the new array copies?  Should they be?  
  //If they aren't
  //copies, should this function be in HashedArray?
  public HashedArray getEmptyHashedArray() {
    return new HashedArray(array);
  }
  
  public String toString() {
    return getClass().getName() + "[" + paramString() + "]";
  }
  
  protected String paramString() {
    StringBuffer buffer = new StringBuffer(",map=");
    for (int i = 0; i < array.size(); i++) {
      UniqueKey key = (UniqueKey) array.get(i);
      buffer.append("\nkey=" + key);
      if (key != null) {
        buffer.append(",value=" + map.get(key));
      }
    }
    return buffer.toString();
  }
}
