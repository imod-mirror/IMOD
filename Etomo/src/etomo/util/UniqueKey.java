package etomo.util;

/**
* <p>Description: key that is unique to an array</p>
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
* <p> Revision 1.1.2.1  2004/09/13 19:20:23  sueh
* <p> bug# 520 An immutable key which is unque to the HashedArray passed
* <p> to it.  Contains a hashCode() function.
* <p> </p>
*/
public class UniqueKey {
  public static  final String  rcsid =  "$Id$";
  
  private final String name;
  private long count = 0;
  
  UniqueKey(String name, HashedArray keyedStorage) {
    this.name = name;
    makeUnique(keyedStorage);
  }
  
  private void makeUnique(HashedArray keyedStorage) {
    for (int i = 0; i < keyedStorage.size(); i++) {
      UniqueKey storedKey = (UniqueKey) keyedStorage.getKey(i);
      if (storedKey.name.equals(name)) {
        count = storedKey.count + 1;
      }
    }
  }
  
  public boolean equals(UniqueKey that) {
    if (name.equals(that.name) && count == that.count) {
      return true;
    }
    return false;
  }
  
  public String getName() {
    return name;
  }
  
  public int hashCode() {
    Long count = new Long(this.count);
    return name.hashCode() + count.hashCode();
  }
  
  public String toString() {
    return getClass().getName() + "[" + paramString() + "]";
  }
  
  private String paramString() {
    return ",name=" + name + ",count =" + count;
  }
}
