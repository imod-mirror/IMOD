package etomo.storage.autodoc;

import etomo.storage.LogFile;
import etomo.ui.swing.Token;

import java.io.IOException;
import java.util.ArrayList;

/**
 * <p>Description:</p>
 * <p/>
 * <p>Copyright: Copyright 2002 - 2014 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 *          <p/>
 *          <p> $$Log$
 *          <p> $Revision 1.15  2010/11/13 16:05:36  sueh
 *          <p> $bug# 1417 Renamed etomo.ui to etomo.ui.swing.
 *          <p> $
 *          <p> $Revision 1.14  2009/03/09 17:26:02  sueh
 *          <p> $bug# 1199 Returning the last nameValuePair (duplicate attribute) all
 *          <p> $functions that get the value, except print() which prints them all.
 *          <p> $
 *          <p> $Revision 1.13  2009/02/04 23:30:00  sueh
 *          <p> $bug# 1158 Changed id and exceptions classes in LogFile.
 *          <p> $
 *          <p> $Revision 1.12  2009/01/20 19:31:16  sueh
 *          <p> $bug# 1102 Added getFirstAttribute.
 *          <p> $
 *          <p> $Revision 1.11  2007/04/11 21:47:10  sueh
 *          <p> $bug# 964 Allow removal of an occurrence of an attribute.  To allow the removal
 *          <p> $of attributes, I added boolean occurrences.  When occurrences is 0, then
 *          <p> $attribute will not print, and exists() will return false.  This allows the removal of
 *          <p> $the attribute without changing the tree structure.  Attributes are removed by
 *          <p> $NameValuePairs.
 *          <p> $
 *          <p> $Revision 1.10  2007/04/09 20:15:46  sueh
 *          <p> $bug# 964 Moved the value to the associated name/value pair.  Changed
 *          <p> $the Vector member variable from values to nameValuePairList.  Associated the
 *          <p> $last attribute in each name/value pair with the name value pair.  This is the
 *          <p> $attribute which used to contain the value.  The name/value pair also contained
 *          <p> $the value; so it was duplicated.  This made it difficult to add a value to an
 *          <p> $existing attribute.  GetValue() gets the value from the associated name/value
 *          <p> $pair.  Also removed the old nameValuePairList member variable, because it
 *          <p> $wasn't being used for anything.
 *          <p> $
 *          <p> $Revision 1.9  2007/03/23 20:28:58  sueh
 *          <p> $bug# 964 Added getMultiLineValue().
 *          <p> $
 *          <p> $Revision 1.8  2007/03/15 21:43:59  sueh
 *          <p> $bug# 964 Added changeValue() which overwrites the first token with the String
 *          <p> $parameter and removes the rest of the token link list.
 *          <p> $
 *          <p> $Revision 1.7  2007/03/08 21:45:14  sueh
 *          <p> $bug# 964 Save name/value pairs in the parser.  Fixing printing.
 *          <p> $
 *          <p> $Revision 1.6  2007/03/07 21:04:36  sueh
 *          <p> $bug# 964 Fixed printing.
 *          <p> $
 *          <p> $Revision 1.5  2007/03/01 01:15:21  sueh
 *          <p> $bug# 964 Added comments.
 *          <p> $
 *          <p> $Revision 1.4  2006/06/14 21:19:33  sueh
 *          <p> $bug# 852 Added isBase() and isAttribute()
 *          <p> $
 *          <p> $Revision 1.3  2006/06/14 00:13:27  sueh
 *          <p> $bug# 852 Added function isGlobal so that it is possible to tell whether an attribute
 *          <p> $is global or part of a section.
 *          <p> $
 *          <p> $Revision 1.2  2006/05/01 21:16:12  sueh
 *          <p> $bug# 854
 *          <p> $
 *          <p> $Revision 1.1  2006/01/12 17:01:17  sueh
 *          <p> $bug# 798 Moved the autodoc classes to etomo.storage.
 *          <p> $
 *          <p> $Revision 1.9  2006/01/11 21:49:57  sueh
 *          <p> $bug# 675 Moved value formatting to Token.  Use
 *          <p> $Token.getFormattedValues(boolean format) to get a value that has
 *          <p> $formatting strings.  If format is true then use the formatting strings,
 *          <p> $otherwise strip them.  Added references to the parent of the attribute and
 *          <p> $the list of name/value pairs the attribute is in.
 *          <p> $
 *          <p> $Revision 1.8  2006/01/03 23:23:26  sueh
 *          <p> $bug# 675 Added equalsName().
 *          <p> $
 *          <p> $Revision 1.7  2005/12/23 02:08:37  sueh
 *          <p> $bug# 675 Encapsulated the list of attributes into AttributeList.  There is a
 *          <p> $list of attributes in three classes.
 *          <p> $
 *          <p> $Revision 1.6  2005/11/10 18:11:42  sueh
 *          <p> $bug# 733 Added getAttribute(int name), which translates name into a string
 *          <p> $and calls getAttribute(String).
 *          <p> $
 *          <p> $Revision 1.5  2005/09/21 16:22:56  sueh
 *          <p> $bug# 532 Added getUnformattedValue(EtomoNumber)
 *          <p> $
 *          <p> $Revision 1.4  2005/05/17 19:31:59  sueh
 *          <p> $bug# 372 Reducing the visibility of functions and member variables.
 *          <p> $Removing unused function getValue().
 *          <p> $
 *          <p> $Revision 1.3  2005/02/15 19:30:44  sueh
 *          <p> $bug# 602 Added getUnformattedValue() and getFormattedValue() to get the
 *          <p> $value either ignoring or using the BREAK and INDENT tokens.
 *          <p> $
 *          <p> $Revision 1.2  2004/01/01 00:42:45  sueh
 *          <p> $bug# 372 correcting interface
 *          <p> $
 *          <p> $Revision 1.1  2003/12/31 01:22:02  sueh
 *          <p> $bug# 372 holds attribute data
 *          <p> $$ </p>
 */

final class Attribute extends WriteOnlyAttributeList implements WritableAttribute {
  private final WriteOnlyAttributeList parent;
  private final Token name;
  private final String key;
  private final int lineNum;

  /**
   * An attribute can occur more then once.  When occurrences is less then 1,
   * this attribute no longer exists.
   */
  private int occurrences = 1;

  /**
   * nameValuePairList will be instantiated if the attribute is the last attribute
   * in the name of at least one name/value pair.  The value of the attribute is
   * retrieved through the name/value pair.
   */
  private ArrayList<NameValuePair> nameValuePairList = null;
  /**
   * children will be instantiated if the attribute is the not the last attribute
   * in the name of at least one name/value pair.
   */
  private AttributeList children = null;

  Attribute(WriteOnlyAttributeList parent,
  /* WriteOnlyNameValuePairList nameValuePairList, */Token name, final int lineNum) {
    this.parent = parent;
    // this.nameValuePairList = nameValuePairList;
    this.name = name;
    this.lineNum = lineNum;
    key = name.getKey();
  }

  /**
   * Adds names and values to this instance from the merge attribute, when the names are
   * not found in this instance.
   *
   * @param mergeAttribute
   * @param parent         location of statement list
   */
  void merge(final Attribute mergeAttribute, final WriteOnlyStatementList parent) {
    if (mergeAttribute == null) {
      return;
    }
    //If this attribute has no occurrences, add it back in and entirely replace its data
    // with the merge attribute's data.
    if (!exists()) {
      add();
      children = mergeAttribute.children;
      nameValuePairList = mergeAttribute.nameValuePairList;
      graftStatements(parent);
    }
    else {  //Attempt to merge the branch.
      if (mergeAttribute.children != null) {
        if (children == null && mergeAttribute.children != null) {
          //This branch doesn't exist in the instance, add the whole branch
          children = mergeAttribute.children;
          mergeAttribute.children.graftStatements(parent);
        }
        else {
          //This attribute does exist so far, continue to attempt to merge
          children.merge(mergeAttribute.children, parent);
        }
      }
    }
  }

  /**
   * Graft statements belonging to this instance onto parent
   *
   * @param parent
   */
  void graftStatements(final WriteOnlyStatementList parent) {
    if (parent == null) {
      return;
    }
    if (nameValuePairList != null) {
      int len = nameValuePairList.size();
      for (int i = 0; i < len; i++) {
        parent.graft(nameValuePairList.get(i));
      }
    }
    if (children != null) {
      children.graftStatements(parent);
    }
  }

  /**
   * Removes name/value pairs from this instance when they are identical to name/value
   * pairs in the subtract attribute.  The subtraction functionality used on the name/
   * value pair list is N**2.  However this functionality is currently only being used in
   * situations where there should be no duplicate name/value pairs.  If this
   * functionality must be used on autodocs where there are many duplicate name/value
   * pairs, this algorithm will not suffice.
   *
   * @param subtractAttribute
   */
  void subtract(final Attribute subtractAttribute) {
    if (subtractAttribute == null) {
      return;
    }
    if (children != null && subtractAttribute.children != null) {
      children.subtract(subtractAttribute.children);
    }
    //Remove matching values
    if (nameValuePairList != null && subtractAttribute.nameValuePairList != null) {
      int subtractLen = subtractAttribute.nameValuePairList.size();
      int len = nameValuePairList.size();
      if (subtractLen * len > 100) {
        System.err.println(
            "Warning: Attribute.subtract:duplicate name/value pairs - process may run slowly.");
      }
      for (int i = 0; i < subtractLen; i++) {
        NameValuePair subtractNameValuePair = subtractAttribute.nameValuePairList.get(i);
        if (subtractNameValuePair != null) {
          for (int j = 0; j < len; j++) {
            NameValuePair nameValuePair = nameValuePairList.get(j);
            if (nameValuePair != null) {
              //TODO use equalsValue function
              String value = nameValuePair.getValue();
              String subtractValue = subtractNameValuePair.getValue();
              if ((value == null && subtractValue == null) ||
                  (value != null && subtractValue != null &&
                      value.trim().equals(subtractValue.trim()))) {
                //remove name/value pair
                nameValuePairList.remove(j);
                nameValuePair.remove();
                break;
              }
            }
          }
        }
      }
    }
  }

  String getKey() {
    return key;
  }

  /**
   * Global attributes are not in sections
   */

  boolean isGlobal() {
    return parent.isGlobal();
  }

  public int getLineNum() {
    return lineNum;
  }

  /**
   * First attribute in name value pair - parent is a section or an autodoc
   *
   * @return
   */
  boolean isBase() {
    return !parent.isAttribute();
  }

  boolean isAttribute() {
    return true;
  }

  void add() {
    occurrences++;
  }

  void remove() {
    occurrences--;
  }

  boolean exists() {
    return occurrences >= 1;
  }

  static String getKey(Token name) {
    if (name == null) {
      return null;
    }
    return name.getKey();
  }

  static String getKey(String name) {
    if (name == null) {
      return null;
    }
    return Token.convertToKey(name);
  }

  /* boolean equalsName(String name) { if (name == null) { return false; } return
   * key.equals(Token.convertToKey(name)); } */
  WriteOnlyAttributeList addAttribute(final Token name, final int lineNum) {
    if (children == null) {
      children = new AttributeList(this/* , nameValuePairList */);
    }
    return children.addAttribute(name, lineNum);
  }

  /**
   * Added a multi-attribute name/value pair to the children member variable
   *
   * @param index         points to the name of the attribute to be added at this level
   * @param name
   * @param lineNum
   * @param value
   * @param nameValuePair
   */
  void addAttribute(int index, final String[] name, final int lineNum, final String value,
      final NameValuePair nameValuePair) {
    if (children == null) {
      children = new AttributeList(this);
    }
    children.addAttribute(index, name, lineNum, value, nameValuePair);
  }

  synchronized void addNameValuePair(NameValuePair nameValuePair) {
    if (nameValuePairList == null) {
      // complete construction before assigning to keep the unsynchronized
      // functions from seeing a partially constructed instance.
      nameValuePairList = new ArrayList<NameValuePair>();
    }
    nameValuePairList.add(nameValuePair);
  }

  public synchronized void setValue(String newValue) {
    if (nameValuePairList == null) {
      // This attribute is never the last attribute in a name/value pair.
      // Therefore there is no value to change
      // To add a value to this attribute you would have to create the name/value pair
      // where this attribute is the last attribute in the name.
      return;
    }
    // current we can only modify the last name/value pair found
    NameValuePair nameValuePair = getNameValuePair();
    Token value = new Token();
    value.set(Token.Type.ANYTHING, newValue);
    nameValuePair.setValue(value);
  }

  public Attribute getAttribute(int name) {
    if (children == null) {
      return null;
    }
    return children.getAttribute(String.valueOf(name));
  }

  public ReadOnlyAttribute getAttribute(String name) {
    if (children == null) {
      return null;
    }
    return children.getAttribute(name);
  }

  /**
   * Gets the first attribute in children with an onameValuePairccurrences value of at least
   * one.
   */
  public ReadOnlyAttribute getFirstAttribute() {
    if (children == null) {
      return null;
    }
    return children.getFirstAttribute();
  }

  public void write(LogFile file, LogFile.WriterId writerId)
      throws LogFile.LockException, IOException {
    if (!exists()) {
      return;
    }
    name.write(file, writerId);
  }

  void print(int level) {
    if (exists()) {
      Autodoc.printIndent(level++);
      System.out.print(name.getValues());
      if (nameValuePairList == null) {
        System.out.println(".");
      }
      else {
        int len = nameValuePairList.size();
        if (len > 0) {
          for (int i = 0; i < len; i++) {
            if (i > 0) {
              Autodoc.printIndent(level);
            }
            NameValuePair nameValuePair = nameValuePairList.get(i);
            if (nameValuePair != null) {
              System.out.print(" = ");
              Token value = nameValuePair.getTokenValue();
              if (value == null) {
                System.out.println("null");
              }
              else {
                System.out.println(value.getValues());
              }
            }
          }
        }
        else {
          System.out.println();
        }
      }
    }
    if (children != null) {
      children.print(level);
    }
  }

  WriteOnlyAttributeList getParent() {
    return parent;
  }

  Token getNameToken() {
    return name;
  }

  public String getName() {
    return name.getValues();
  }

  public ReadOnlyAttributeList getChildren() {
    return children;
  }

  public String getMultiLineValue() {
    NameValuePair nameValuePair = getNameValuePair();
    if (nameValuePair != null) {
      Token value = nameValuePair.getTokenValue();
      if (value != null) {
        return value.getMultiLineValues();
      }
    }
    return null;
  }

  /**
   * Gets the value from the first nameValuePair in the nameValuePairList.
   */
  public String getValue() {
    NameValuePair nameValuePair = getNameValuePair();
    if (nameValuePair != null) {
      Token value = nameValuePair.getTokenValue();
      if (value != null) {
        return value.getValues();
      }
    }
    return null;
  }

  void removeNameValuePair(NameValuePair pair) {
    nameValuePairList.remove(pair);
  }

  /**
   * Gets the last nameValuePair in the nameValuePairList.
   */
  NameValuePair getNameValuePair() {
    if (nameValuePairList != null && nameValuePairList.size() > 0) {
      return nameValuePairList.get(nameValuePairList.size() - 1);
    }
    return null;
  }

  public Token getValueToken() {
    NameValuePair nameValuePair = getNameValuePair();
    if (nameValuePair != null) {
      return nameValuePair.getTokenValue();
    }
    return null;
  }

  public int hashCode() {
    return key.hashCode();
  }

  public String toString() {
    return getClass().getName() + "[" + ",name=" + name + ",\nchildren=" + children + "]";
  }
}
