package etomo.storage;

import etomo.storage.autodoc.ReadOnlyAttribute;

/**
* <p>Description: </p>
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

  private final boolean bool;

  private ReadOnlyAttribute attribute = null;

  DirectiveAttribute(final boolean bool) {
    this.bool = bool;
  }

  void setAttribute(final ReadOnlyAttribute input) {
    attribute = input;
  }

  /**
   * @param directiveDef
   * @param axisID
   * @return true if the directive is not boolean and the attribute value is null
   */
  boolean overrides() {
    if (bool) {
      return false;
    }
    if (attribute == null) {
      return false;
    }
    return attribute.getValue() == null;
  }

  /**
   * @return true if instance doesn't contain an attribute
   */
  boolean isEmpty() {
    return attribute == null;
  }
}
