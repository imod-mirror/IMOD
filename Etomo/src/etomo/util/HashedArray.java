package etomo.util;

import java.util.Vector;

/**
* <p>Description: A list of name, value pairs that can be accessed by keys or 
* indexes and can have non-unique names.</p>
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
public class HashedArray extends ConstHashedArray {
  public static  final String  rcsid =  "$Id$";
  
  public HashedArray() {
    super();
  }
  
  protected HashedArray(Vector array) {
    super(array);
  }
  
  public synchronized UniqueKey add(String name, Object value) {
    UniqueKey key = new UniqueKey(name, this);
    array.add(key);
    map.put(key, value);
    return key;
  }
  
  public synchronized UniqueKey add(int keyIndex, Object value) {
    UniqueKey key = (UniqueKey) array.get(keyIndex);
    map.put(key, value);
    return key;
  }
  
  public synchronized Object remove(UniqueKey key) {
    for (int i = 0; i < array.size(); i++) {
      if (array.get(i).equals(key)) {
        array.remove(i);
      }
    }
    return map.remove(key);
  }
  
  public synchronized UniqueKey rekey(UniqueKey oldKey, String newName) {
    Object value = remove(oldKey);
    return add(newName, value);
  }
}
