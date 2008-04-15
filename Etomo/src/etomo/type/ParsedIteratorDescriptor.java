package etomo.type;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright 2006</p>
 *
 * <p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$`
 * 
 * <p> $Log$
 * <p> Revision 1.3  2008/04/08 23:59:20  sueh
 * <p> bug# 1105 Changed the array used in getParsedNumberExpandedArray
 * <p> to a ParsedElementList because it always holds ParsedNumbers.
 * <p>
 * <p> Revision 1.2  2008/04/02 02:20:10  sueh
 * <p> bug# 1097 Moved functionality out of ParsedDescriptor and into child
 * <p> classes.  This is because the child class are now less similar to each
 * <p> other.
 * <p>
 * <p> Revision 1.1  2007/11/06 19:49:43  sueh
 * <p> bug# 1047 Class to parsed iterator descriptors such as 4-9 and 5-2.
 * <p> </p>
 */
final class ParsedIteratorDescriptor extends ParsedDescriptor {
  public static final String rcsid = "$Id$";

  static final Character DIVIDER_SYMBOL = new Character('-');
  
  private boolean debug = false;

  private ParsedIteratorDescriptor(EtomoNumber.Type etomoNumberType,
      boolean debug, EtomoNumber defaultValue) {
    super(ParsedElementType.NON_MATLAB_ARRAY, etomoNumberType, debug,
        defaultValue);
  }

  static ParsedIteratorDescriptor getInstance(boolean debug,
      EtomoNumber defaultValue) {
    return new ParsedIteratorDescriptor(EtomoNumber.Type.INTEGER, debug,
        defaultValue);
  }
  
  public void setDebug(boolean input) {
    debug = input;
    descriptor.setDebug(input);
    for (int i = 0; i < size(); i++) {
      getElement(i).setDebug(input);
    }
  }
  
  boolean isDebug() {
    return debug;
  }

  Character getDividerSymbol() {
    return DIVIDER_SYMBOL;
  }
}
