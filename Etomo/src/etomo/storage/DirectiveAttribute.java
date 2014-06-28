package etomo.storage;

import java.util.Hashtable;

import etomo.storage.autodoc.ReadOnlyAttribute;
import etomo.type.AxisID;

/**
* <p>Description: Represents a directive attribute.  Contains a table of instances that are
* likely to be accessed again.  NOT thread safe.</p>
* 
* <p>Copyright: Copyright 2014</p>
*
* <p>Organization:
* Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
* University of Colorado</p>
* 
* @author $Author$
* 
* @version $Revision$
* 
* <p> $Log$ </p>
*/
final class DirectiveAttribute {
  public static final String rcsid = "$Id:$";

  /**
   * Table of directives that are likely to be accessed again.
   */
  private static final Hashtable<Key, DirectiveAttribute> table = new Hashtable<Key, DirectiveAttribute>();

  private static final String TRUE_VALUE = "1";
  private static final String FALSE_VALUE = "0";

  private final Key key;
  private final DirectiveDef directiveDef;
  private final AxisID axisID;
  private final AttributeMatch primaryAttribute;
  private final AttributeMatch secondaryAttribute;

  private ReadOnlyAttribute parentAttribute = null;

  private DirectiveAttribute(final Key key, final DirectiveDef directiveDef,
      final AxisID axisID, final ReadOnlyAttribute parentAttribute) {
    this.key = key;
    this.directiveDef = directiveDef;
    this.axisID = axisID;
    primaryAttribute = new AttributeMatch(Match.PRIMARY);
    if (directiveDef.hasSecondaryMatch()) {
      secondaryAttribute = new AttributeMatch(Match.SECONDARY);
    }
    else {
      secondaryAttribute = null;
    }
  }

  static DirectiveAttribute getInstance(final DirectiveFile directiveFile,
      final DirectiveDef directiveDef, final AxisID axisID,
      final ReadOnlyAttribute parentAttribute) {
    Key key = new Key(directiveFile, directiveDef, axisID);
    if (table.containsKey(key)) {
      return table.get(key);
    }
    DirectiveAttribute directiveAttribute = new DirectiveAttribute(key, directiveDef,
        axisID, parentAttribute);
    return directiveAttribute;
  }

  /**
   * @return true if the directive is not boolean and the attribute value is null
   */
  boolean overrides(final Match match) {
    loadAttribute(match);
    ReadOnlyAttribute attribute = getAttribute();
    if (directiveDef == null || directiveDef.isBool() || attribute == null) {
      return false;
    }
    return attribute.getValue() == null;
  }

  /**
   * @return true if instance doesn't contain an attribute
   */
  boolean isEmpty(final Match match) {
    loadAttribute(match);
    return attribute == null;
  }

  /**
   * @return the value of the attribute
   */
  String getValue(final Match match) {
    loadAttribute(match);
    if (attribute == null) {
      return null;
    }
    removeFromTable();
    return attribute.getValue();
  }

  /**
   * @return the boolean value of the attribute
   */
  boolean isValue(final Match match) {
    loadAttribute(match);
    if (attribute == null) {
      return false;
    }
    String value = attribute.getValue();
    if (directiveDef.isBool()) {
      if (value == null) {
        System.err.println("Warning: " + directiveDef
            + " is boolean and its value should not be null");
      }
      else {
        value = value.trim();
        if (!value.equals(FALSE_VALUE) && !value.equals(TRUE_VALUE)) {
          System.err.println("Warning: " + directiveDef
              + " is boolean and its value is invalid: " + value);
        }
      }
    }
    else {
      System.err.println("Warning: " + directiveDef + " is not a boolean");
    }
    removeFromTable();
    return toBoolean(value);
  }

  /**
   * Translates the value of the attribute into a boolean.  A null value
   * returns false.  A "0" value returns false.  A "1" returns true.  Everything else
   * returns true.
   * @param value
   * @return
   */
  static boolean toBoolean(String value) {
    if (value == null) {
      return false;
    }
    value = value.trim();
    if (value.equals(FALSE_VALUE)) {
      return false;
    }
    return true;
  }

  /**
   * called when the directive is unlikely to be looked at again.
   */
  private void removeFromTable() {
    if (table.containsKey(key)) {
      table.remove(key);
    }
  }

  private void loadAttribute(final Match match) {
    if (parentAttribute == null) {
      return;
    }
    if (match == Match.PRIMARY) {
      primaryAttribute.loadAttribute(axisID, parentAttribute, directiveDef);
    }
    else if (match == Match.SECONDARY && secondaryAttribute != null) {
      secondaryAttribute.loadAttribute(axisID, parentAttribute, directiveDef);
    }

    if ((primaryAttribute.attribute != null || (secondaryAttribute != null && secondaryAttribute.attribute != null))
        && !table.containsKey(key)) {
      // This instance may be reused - save it
      table.put(key, this);
    }
    if (primaryAttribute.loaded
        && (secondaryAttribute == null || secondaryAttribute.loaded)) {
      parentAttribute = null;
    }
  }

  private static final class AttributeMatch {
    private final Match match;

    private boolean loaded = false;
    private ReadOnlyAttribute attribute = null;

    private AttributeMatch(final Match match) {
      this.match = match;
    }

    private void loadAttribute(final ReadOnlyAttribute parentAttribute,
        final DirectiveDef directiveDef, final AxisID axisID) {
      if (loaded || parentAttribute == null || directiveDef == null) {
        return;
      }
      loaded = true;
      DirectiveType type = directiveDef.getDirectiveType();
      String name = directiveDef.getName(match, axisID);
      // copyarg and setupset
      if (type == DirectiveType.COPY_ARG || type == DirectiveType.SETUP_SET) {
        attribute = parentAttribute.getAttribute(name);
      }
      // runtime
      else if (type == DirectiveType.RUN_TIME) {
        attribute = findAttribute(parentAttribute, directiveDef.getModule(),
            directiveDef.getAxis(match, axisID), name);
      }
      // comparam
      else if (type == DirectiveType.COM_PARAM) {
        attribute = findAttribute(parentAttribute,
            directiveDef.getComfile(match, axisID), directiveDef.getCommand(), name);
      }
    }

    /**
     * Get the three-level down descendant of an attribute.
     * @param attribute
     * @param name1
     * @param name2
     * @param name3
     * @return
     */
    private ReadOnlyAttribute findAttribute(ReadOnlyAttribute attribute,
        final String name1, final String name2, final String name3) {
      if (attribute == null || name1 == null) {
        return null;
      }
      attribute = attribute.getAttribute(name1);
      if (attribute == null || name2 == null) {
        return null;
      }
      attribute = attribute.getAttribute(name2);
      if (attribute == null || name3 == null) {
        return null;
      }
      return attribute.getAttribute(name3);
    }

    /**
     * @return true if the directive is not boolean and the attribute value is null
     */
    boolean overrides(final ReadOnlyAttribute parentAttribute,
        final DirectiveDef directiveDef, final AxisID axisID) {
      loadAttribute(parentAttribute, directiveDef, axisID);
      if (directiveDef == null || directiveDef.isBool() || attribute == null) {
        return false;
      }
      return attribute.getValue() == null;
    }

    /**
     * @return true if instance doesn't contain an attribute
     */
    boolean isEmpty(final ReadOnlyAttribute parentAttribute,
        final DirectiveDef directiveDef, final AxisID axisID) {
      loadAttribute(parentAttribute, directiveDef, axisID);
      return attribute == null;
    }

    /**
     * @return the value of the attribute
     */
    String getValue(final Match match) {
      loadAttribute(match);
      if (attribute == null) {
        return null;
      }
      removeFromTable();
      return attribute.getValue();
    }

    /**
     * @return the boolean value of the attribute
     */
    boolean isValue(final Match match) {
      loadAttribute(match);
      if (attribute == null) {
        return false;
      }
      String value = attribute.getValue();
      if (directiveDef.isBool()) {
        if (value == null) {
          System.err.println("Warning: " + directiveDef
              + " is boolean and its value should not be null");
        }
        else {
          value = value.trim();
          if (!value.equals(FALSE_VALUE) && !value.equals(TRUE_VALUE)) {
            System.err.println("Warning: " + directiveDef
                + " is boolean and its value is invalid: " + value);
          }
        }
      }
      else {
        System.err.println("Warning: " + directiveDef + " is not a boolean");
      }
      removeFromTable();
      return toBoolean(value);
    }
  }

  private static final class Key {
    private final DirectiveDef directiveDef;
    final AxisID axisID;

    private Key(final DirectiveFile directiveFile, final DirectiveDef directiveDef,
        final AxisID axisID) {
      this.directiveDef = directiveDef;
      this.axisID = axisID;
    }

    public int hashCode() {
      if (directiveDef == null) {
        return super.hashCode();
      }
      return toString().hashCode();
    }

    public String toString() {
      if (directiveDef == null) {
        return super.toString();
      }
      return directiveDef.toString() + axisID != null ? "," + axisID.toString() : "";
    }
  }

  static final class Match {
    static final Match PRIMARY = new Match();
    static final Match SECONDARY = new Match();
  }
}
