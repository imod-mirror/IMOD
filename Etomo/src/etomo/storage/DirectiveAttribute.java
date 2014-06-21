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

  private ReadOnlyAttribute parentAttribute = null;
  private ReadOnlyAttribute attribute = null;

  private DirectiveAttribute(final Key key, final DirectiveDef directiveDef,
      final AxisID axisID, final ReadOnlyAttribute parentAttribute) {
    this.key = key;
    this.directiveDef = directiveDef;
    this.axisID = axisID;
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
  boolean overrides() {
    loadAttribute();
    if (directiveDef == null || directiveDef.isBool() || attribute == null) {
      return false;
    }
    return attribute.getValue() == null;
  }

  /**
   * @return true if instance doesn't contain an attribute
   */
  boolean isEmpty() {
    loadAttribute();
    return attribute == null;
  }

  /**
   * @return the value of the attribute
   */
  String getValue() {
    loadAttribute();
    if (attribute == null) {
      return null;
    }
    removeFromTable();
    return attribute.getValue();
  }

  /**
   * @return the boolean value of the attribute
   */
  boolean isValue() {
    loadAttribute();
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

  /**
   * Loads the attribute associated with directiveDef and axisID.  This function is run
   * once per instance.  For copyarg: look under the B directive when axisID is
   * AxisID.SECOND, otherwise look under the A directive.  For runtime:  look under the
   * "any" directive tree.  If DirectiveDef.name isn't a leaf of this tree, look under "a"
   * for AxisID.ONLY or .FIRST, and look under "b" for AxisID.SECOND.  For comparam use a
   * simliar algorithm to runtime:  Look for a comfile name with no postfix first.  If the
   * name isn't a leaf of this directive tree, look under comfilea for AxisID.FIRST, and
   * comfileb for AxisID.SECOND.
   */
  private void loadAttribute() {
    if (parentAttribute == null || directiveDef == null) {
      return;
    }
    // Don't forget the set the parentAttribute to null.
    String name = directiveDef.getName(axisID);
    DirectiveType type = directiveDef.getDirectiveType();
    // copyarg and setupset
    if (type == DirectiveType.COPY_ARG || type == DirectiveType.SETUP_SET) {
      attribute = parentAttribute.getAttribute(name);
    }
    // runtime
    else if (type == DirectiveType.RUN_TIME) {
      // get module attribute
      ReadOnlyAttribute moduleAttribute = parentAttribute.getAttribute(directiveDef
          .getModule());
      if (moduleAttribute != null) {
        // try different axis attributes to get to the name attribute
        attribute = getAttribute(moduleAttribute, DirectiveDef.RUN_TIME_ANY_AXIS_TAG,
            name, null);
        if (attribute == null) {
          // Can't find name under "any" - look under the axis letter.
          String axisTag = null;
          if (axisID == AxisID.ONLY || axisID == AxisID.FIRST) {
            axisTag = DirectiveDef.RUN_TIME_A_AXIS_TAG;
          }
          else if (axisID == AxisID.SECOND) {
            axisTag = DirectiveDef.RUN_TIME_B_AXIS_TAG;
          }
          if (axisTag != null) {
            attribute = getAttribute(moduleAttribute, axisTag, name, null);
          }
        }
      }
    }
    // comparam
    else if (type == DirectiveType.COM_PARAM) {
      // try different comfile postfixes to get to the name attribute
      attribute = getAttribute(parentAttribute, directiveDef.getComfile(),
          directiveDef.getCommand(), name);
      if (attribute == null) {
        String axisTag = null;
        // Can't find name with no axis tag - try the a or b postfix.
        if (axisID == AxisID.FIRST) {
          axisTag = AxisID.FIRST.getExtension();
        }
        else if (axisID == AxisID.SECOND) {
          axisTag = axisID.getExtension();
        }
        if (axisTag != null) {
          attribute = getAttribute(parentAttribute, directiveDef.getComfile() + axisTag,
              directiveDef.getCommand(), name);
        }
      }
    }
    // Remove the parentAttribute so this function cannot be run more then once.
    parentAttribute = null;
    // parentAttribute is now null - safe to run any function
    if (attribute != null && !isEmpty() && !overrides()) {
      //This instance is likely to be reused
      if (!table.containsKey(key)) {
        table.put(key, this);
      }
    }
  }

  /**
   * Get the descendant of attribute, up to three levels down.
   * @param attribute
   * @param name1
   * @param name2
   * @param name3
   * @return
   */
  private ReadOnlyAttribute getAttribute(ReadOnlyAttribute attribute, final String name1,
      final String name2, final String name3) {
    if (attribute == null || name1 == null) {
      return attribute;
    }
    attribute = attribute.getAttribute(name1);
    if (attribute == null || name2 == null) {
      return attribute;
    }
    attribute = attribute.getAttribute(name2);
    if (attribute == null || name3 == null) {
      return attribute;
    }
    return attribute.getAttribute(name3);
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
}
