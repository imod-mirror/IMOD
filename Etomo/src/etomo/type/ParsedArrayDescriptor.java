package etomo.type;

import etomo.ui.Token;
import etomo.util.PrimativeTokenizer;

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
* @version $Revision$
* 
* <p> $Log$ </p>
*/

final class ParsedArrayDescriptor extends ParsedElement {
  public static  final String  rcsid =  "$Id$";
  
  static final Character DIVIDER_SYMBOL = new Character(':');
  
  private final ParsedElementList list = new ParsedElementList();
  
  int size() {
    return list.size();
  }
  
  Token parse(Token token,PrimativeTokenizer tokenizer) {return null;}
  
  ParsedElement getElement(int index) {
    return list.get(index);
  }
  
  String getRawString() {
    StringBuffer buffer = new StringBuffer();
    for (int i=0;i<list.size();i++) {
      if (i>0) {
        buffer.append(DIVIDER_SYMBOL.charValue());
      }
      buffer.append(list.get(i).getRawString());
    }
    return buffer.toString();
  }
  
  String getParsableString() {
    return getRawString();
  }
}
