package etomo.ui;

import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

/**
* <p>Description:</p>
*
* <p>Copyright: Copyright © 2002, 2003</p>
*
* <p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEM),
* University of Colorado</p>
*
* @author $$Author$$
*
* @version $$Revision$$
*
* <p> $$Log$
* <p> $Revision 1.1  2003/12/31 01:22:02  sueh
* <p> $bug# 372 holds attribute data
* <p> $$ </p>
*/

public class Attribute implements AttributeCollection {
  public static final String rcsid = "$$Id$$";
  
  String key = null; //required
  Token name = null; //required
  Token value = null; //optional
  HashMap attributeMap = null; //optional
  
  
  public static String getKey(Token name) {
    if (name == null) {
      return null;
    }
    return name.getKey(true);
  }
 
  public static String getKey(String name) {
    if (name == null) {
      return null;
    }
    return Token.getKey(name);
  }
 
  public Attribute(Token name) {
    this.name = name;
    key = name.getKey(true);
  }
  
  public AttributeCollection addAttribute(Token name) {
    Attribute existingAttribute = null;
    if (attributeMap == null) {
      attributeMap = new HashMap();
    }
    String key = Attribute.getKey(name);
    existingAttribute = (Attribute) attributeMap.get(key);
    if (existingAttribute == null) {
      Attribute newAttribute = new Attribute(key, name);
      attributeMap.put(key, newAttribute);
      return newAttribute;
    }
    return existingAttribute;
  }
  
  public void setValue(Token value) {
    this.value = value;
  }

  public Attribute getAttribute(String name) {
    if (attributeMap == null) {
      return null;
    }
    String key = getKey(name);
    Attribute attribute = (Attribute) attributeMap.get(key);
    return attribute;
  }

  public final void print() {
    print(0);
  }
  
  private final void print(int level) {
    Token token = null;
    if (level == 0) {
      System.out.print("Attribute: ");
    }
    else {
      for (int i = 0; i < level; i++) {
        System.out.print("  ");
      }
    }
    System.out.print(key + ":(");
    System.out.print(name.getValue(true));
    if (value != null) {
      System.out.print("," + value.getValue(true));
    }
    System.out.print(")");
    if (attributeMap != null) {
      Attribute attribute = null;
      Collection collection = attributeMap.values();
      Iterator iterator = collection.iterator();
      if (iterator.hasNext()) {
        System.out.println(":");
        while (iterator.hasNext()) {
          attribute = (Attribute) iterator.next();
          attribute.print(level+1);
        }
      }
    }
    else {
      System.out.println();
    }
  }

  public String getKey() {
    return key;
  }
  
  public String getName() {
    return name.getValue(true);
  }
 
  public String getValue() {
    if (value == null) {
      return null;
    }
    return value.getValue(true);
  }
 
  public int hashCode() {
    return key.hashCode();
  }
  
  public String toString() {
    return getClass().getName() + "[" + paramString() + "]";
  }

  //protected methods

  protected String paramString() {
    StringBuffer buffer = new StringBuffer(",key=" + key + ",name=");
    buffer.append(name.getValue(true) + ",value=");
    if (value != null) {
      buffer.append(value.getValue(true) + ",attributeMap=");
    }
    if (attributeMap != null) {
      buffer.append(attributeMap.toString());
    }
    return buffer.toString();
  }
  
  private Attribute(String key, Token name) {
    this.key = key;
    this.name = name;
  }

}
