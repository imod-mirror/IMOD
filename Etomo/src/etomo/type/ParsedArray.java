package etomo.type;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import etomo.storage.autodoc.ReadOnlyAttribute;
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
 * <p> $Log$
 * <p> Revision 1.13  2007/07/31 20:39:52  sueh
 * <p> bug# 1028 added ge(int).
 * <p>
 * <p> Revision 1.12  2007/07/24 04:03:52  sueh
 * <p> bug# 1030 Changed ParsedArray.getParsableStringArray to
 * <p> getPaddedStringArray.  The function is only being used by lstThresholds and it
 * <p> needs padded strings.  Also fixed bugs in the function; it was handling the last
 * <p> number incorrectly.
 * <p>
 * <p> Revision 1.11  2007/07/10 00:31:44  sueh
 * <p> bug# 1022 Added a comment.
 * <p>
 * <p> Revision 1.10  2007/05/11 16:01:12  sueh
 * <p> bug# 964 Added getParsableStringArray(), which expands an array,
 * <p> converting the array descriptors into arrays, and return the resulting array
 * <p> in a String[].
 * <p>
 * <p> Revision 1.9  2007/05/03 21:08:44  sueh
 * <p> bug# 964 Added getFlexibleInstance() and getFlexibleCompactInstance(Type).  Fixed bug in hasParsedNumberSyntax().
 * <p>
 * <p> Revision 1.8  2007/04/26 02:46:56  sueh
 * <p> bug# 964 Fixed problems with defaultValue.  Added ParsedArray.compact
 * <p> when empty array elements should not be displayed (lstThresholds).
 * <p>
 * <p> Revision 1.7  2007/04/20 20:52:49  sueh
 * <p> bug# 964 Fixed a bug in parseArray:  don't increment index if it is set to -1.
 * <p> Fixed getString.
 * <p>
 * <p> Revision 1.6  2007/04/19 21:41:39  sueh
 * <p> bug# 964 Added support for flexible syntax, where the string in the .prm file can
 * <p> be either an array or a number.  Instead of handling this in MatlabParamFile,
 * <p> will always store in the array, but the string that is writen to the .prm file will look
 * <p> like a number if flexibleSyntax is turned on.  Added a way to write to an array
 * <p> descriptor inside of an array (setRawString(int,int,String).
 * <p>
 * <p> Revision 1.5  2007/04/13 21:50:51  sueh
 * <p> bug# 964 Not returning ConstEtomoNumber from ParsedElement, because it
 * <p> must be returned with a getDefaulted... function to be accurate.
 * <p> GetReferenceVolume is returning ParsedElement instead.
 * <p>
 * <p> Revision 1.4  2007/04/13 20:14:05  sueh
 * <p> bug# 964 Added setRawString(String), which parses a list of numbers separated
 * <p> by whitespace or commas.
 * <p>
 * <p> Revision 1.3  2007/04/09 20:59:35  sueh
 * <p> bug# 964 Added missing tokenizer.next() call to parseArray.
 * <p>
 * <p> Revision 1.2  2007/03/31 02:54:01  sueh
 * <p> bug# 964 In getRawString(), using a "," when some of the elements are empty -
 * <p> so that the location of the existing elements isn't lost.  Using ParsedElement.isCollection()
 * <p> to decide whether to use "," in other cases:  using a comma when one of the
 * <p> elements is an array descriptor.
 * <p>
 * <p> Revision 1.1  2007/03/30 23:40:59  sueh
 * <p> bug# 964 Class to parse Matlab arrays.
 * <p> </p>
 */
public final class ParsedArray extends ParsedElement {
  public static final String rcsid = "$Id$";

  static final Character OPEN_SYMBOL = new Character('[');
  static final Character CLOSE_SYMBOL = new Character(']');
  static final Character DIVIDER_SYMBOL = new Character(',');

  private final ParsedElementList array = new ParsedElementList();

  private final EtomoNumber.Type etomoNumberType;

  /**
   * Place the entire array in collections and the individual elements into
   * numbers
   */
  private Integer[] defaultValueArray = null;

  /**
   * FlexibleSyntax allows a ParsedArray to look like a ParsedNumber when it is
   * empty or contains one element that is not a collection.
   */
  private final boolean flexibleSyntax;

  /**
   * No effect on ParsedArray.  Used to set ParsedArrayDescriptor.compact.
   */
  private final boolean compactDescriptor;
  /**
   * When compact is true, only place non-empty elements in the parsable string.
   */
  private final boolean compact;
  private final String key;

  private boolean useIteratorDescriptors = false;

  ParsedArray(EtomoNumber.Type etomoNumberType, boolean flexibleSyntax,
      boolean compact, boolean compactDescriptor, String key,
      boolean useIteratorDescriptors) {
    this.etomoNumberType = etomoNumberType;
    this.flexibleSyntax = flexibleSyntax;
    this.compact = compact;
    this.compactDescriptor = compactDescriptor;
    this.key = key;
    this.useIteratorDescriptors = useIteratorDescriptors;
  }

  public static ParsedArray getInstance() {
    return new ParsedArray(null, false, false, false, null, false);
  }

  public static ParsedArray getIteratorInstance() {
    return new ParsedArray(null, false, false, false, null, true);
  }

  public static ParsedArray getIteratorInstance(String key) {
    return new ParsedArray(null, false, false, false, key, true);
  }

  public static ParsedArray getInstance(EtomoNumber.Type etomoNumberType,
      String key) {
    return new ParsedArray(etomoNumberType, false, false, false, key, false);
  }

  public static ParsedArray getInstance(EtomoNumber.Type etomoNumberType) {
    return new ParsedArray(etomoNumberType, false, false, false, null, false);
  }

  public static ParsedArray getFlexibleInstance() {
    return new ParsedArray(null, true, false, false, null, false);
  }

  public static ParsedArray getFlexibleInstance(EtomoNumber.Type etomoNumberType) {
    return new ParsedArray(etomoNumberType, true, false, false, null, false);
  }

  public static ParsedArray getCompactInstance() {
    return new ParsedArray(null, false, true, false, null, false);
  }

  public static ParsedArray getFlexibleCompactInstance(
      EtomoNumber.Type etomoNumberType) {
    return new ParsedArray(etomoNumberType, true, true, false, null, false);
  }

  public static ParsedArray getFlexibleCompactDescriptorInstance(
      EtomoNumber.Type etomoNumberType) {
    return new ParsedArray(etomoNumberType, true, false, true, null, false);
  }

  /**
   * Get the array stored in this.array, excluding empty ParsedNumbers and
   * expanding array descriptors.  Pad numbers with zeros.
   * @return parsable string version of array
   */
  public String[] getPaddedStringExpandedArray() {
    List expandedArray = getParsedNumberExpandedArray(null);
    if (expandedArray.size() == 0) {
      return new String[0];
    }
    int maxDigits = 0;
    StringBuffer[] bufferArray = new StringBuffer[expandedArray.size()];
    for (int i = 0; i < expandedArray.size(); i++) {
      bufferArray[i] = new StringBuffer();
      bufferArray[i].append(((ParsedNumber) expandedArray.get(i))
          .getParsableString());
      maxDigits = Math.max(maxDigits, bufferArray[i].length());
    }
    String[] returnArray = new String[expandedArray.size()];
    for (int i = 0; i < expandedArray.size(); i++) {
      int padding = maxDigits - bufferArray[i].length();
      for (int j = 0; j < padding; j++) {
        bufferArray[i].insert(0, '0');
      }
      returnArray[i] = bufferArray[i].toString();
    }
    return returnArray;
  }

  public void parse(ReadOnlyAttribute attribute) {
    array.clear();
    resetFailed();
    if (attribute == null) {
      return;
    }
    PrimativeTokenizer tokenizer = createTokenizer(attribute.getValue());
    try {
      parse(tokenizer.next(), tokenizer);
    }
    catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * @return an error message if invalid, otherwise null
   */
  public String validate() {
    String errorMessage = null;
    for (int i = 0; i < array.size(); i++) {
      errorMessage = array.get(i).validate();
      if (errorMessage != null) {
        return errorMessage;
      }
    }
    return getFailedMessage();
  }

  public String getRawString(int index) {
    return array.get(index).getRawString();
  }

  public Number getRawNumber() {
    return array.get(0).getRawNumber();
  }

  public boolean isEmpty(int index) {
    return array.get(index).isEmpty();
  }

  /**
   * Sets an element of an array descriptor at arrayindex.
   * @param arrayIndex
   * @param descriptorIndex
   * @param string
   */
  public void setRawString(int indexOfDescriptor, int indexOfElement,
      String string) {
    insureDescriptorExists(indexOfDescriptor);
    array.get(indexOfDescriptor).setRawString(indexOfElement, string);
  }

  public ParsedElement getFirstDescriptor(int startIndex) {
    for (int i = startIndex; i < array.size(); i++) {
      ParsedElement element = array.get(i);
      if (element.isCollection()) {
        return element;
      }
    }
    return ParsedElementList.EmptyParsedElement.INSTANCE;
  }

  public int getDescriptorIndex() {
    for (int i = 0; i < array.size(); i++) {
      ParsedElement element = array.get(i);
      if (element.isCollection()) {
        return i;
      }
    }
    return -1;
  }

  /**
   * raw strings come from the screen
   * input may be a collection, since it is not indexed, so treat it as  a semi-
   * raw string (it may have commas)
   */
  public void setRawString(String input) {
    if (isDebug()) {
      System.out.println("setRawString:input=" + input);
    }
    array.clear();
    resetFailed();
    if (input == null) {
      return;
    }
    PrimativeTokenizer tokenizer = createTokenizer(input);
    StringBuffer buffer = new StringBuffer();
    Token token = null;
    try {
      token = tokenizer.next();
      //raw strings shouldn't have brackets
      //place input into array starting from the beginning of the list
      parseArray(token, tokenizer, -1);
    }
    catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Parse one or more elements in an array.  Place them in ParsedArray.array
   * starting at index.
   * @param index
   * @param input
   */
  public void setRawStrings(int index, String input) {
    PrimativeTokenizer tokenizer = createTokenizer(input);
    StringBuffer buffer = new StringBuffer();
    Token token = null;
    try {
      token = tokenizer.next();
      //raw strings shouldn't have brackets
      parseArray(token, tokenizer, index);
    }
    catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * @return true if the size of the list is null.  Empty elements in the list
   * do not affect this result
   */
  public boolean isEmpty() {
    return size() == 0;
  }

  public void clear() {
    array.clear();
  }

  public void setRawString(int arrayIndex, int descriptorIndex, float number) {
    insureDescriptorExists(arrayIndex);
    array.get(arrayIndex).setRawString(descriptorIndex, number);
  }

  public ParsedElement getElement(int arrayIndex, int descriptorIndex) {
    return array.get(arrayIndex).getElement(descriptorIndex);
  }

  /**
   * Raw strings go to the screen
   */
  public String getRawString() {
    return getRawString(null, -1);
  }

  public String getRawStrings(int startIndex) {
    return getRawString(null, startIndex);
  }

  public void moveElement(int indexOfDescriptor, int fromIndexOfElement,
      int toIndexOfElement) {
    array.get(indexOfDescriptor).moveElement(fromIndexOfElement,
        toIndexOfElement);
  }

  public String getRawString(int arrayIndex, int descriptorIndex) {
    return array.get(arrayIndex).getRawString(descriptorIndex);
  }

  public void setRawString(int index, String string) {
    PrimativeTokenizer tokenizer = createTokenizer(string);
    try {
      parseElement(tokenizer.next(), tokenizer, index);
    }
    catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  /**
   * Returns true only when all numbers in the array are greater then or equal
   * to the number parameter.
   */
  public boolean ge(int number) {
    boolean greaterOrEqual = true;
    for (int i = 0; i < array.size(); i++) {
      if (!array.get(i).ge(number)) {
        greaterOrEqual = false;
        break;
      }
    }
    return greaterOrEqual;
  }

  /**
   * Parsable strings are saved to the .prm file.
   * Returns [] if the list is empty.  Returns a list surrounded by brackets if
   * there are multiple elements.
   * If flexibleSyntax is true and hasParsedNumberSyntax() returns true,
   * return string without brackets, so that the parsableString
   * looks like a ParsedNumber.
   */
  public String getParsableString() {
    if (isDebug()) {
      System.out.println("getParsableString:flexibleSyntax=" + flexibleSyntax);
    }
    String string = getString(true, -1);
    StringBuffer buffer = new StringBuffer();
    if (flexibleSyntax && hasParsedNumberSyntax()) {
      //return a parsable number
      return string;
    }
    buffer = new StringBuffer(OPEN_SYMBOL.toString());
    buffer.append(string);
    buffer.append(CLOSE_SYMBOL.toString());
    return buffer.toString();
  }

  public String toString() {
    return "[array:" + array + "]";
  }

  public void setDebug(boolean debug) {
    super.setDebug(debug);
    for (int i = 0; i < array.size(); i++) {
      array.get(i).setDebug(debug);
    }
  }

  /**
   * Clear the array member variable, and add each element of the parsedArray
   * to the array.
   * @param parsedArray
   */
  public void set(ParsedElement parsedArray) {
    parsedArray.setDebug(isDebug());
    parsedArray.setDefaultValue(0, defaultValueArray);
    array.clear();
    for (int i = 0; i < parsedArray.size(); i++) {
      array.add(parsedArray.getElement(i));
    }
  }

  public int size() {
    return array.size();
  }

  public ParsedElement getElement(int index) {
    return array.get(index);
  }

  /**
   * This is a array only if starts with "[" (ignoring whitespace).
   * @param attribute
   * @return
   */
  static boolean isArray(Token token) {
    if (token == null) {
      return false;
    }
    if (token.equals(Token.Type.SYMBOL, OPEN_SYMBOL.charValue())) {
      return true;
    }
    return false;
  }

  void setRawString(int index, float number) {
    ParsedNumber element = new ParsedNumber(etomoNumberType,useIteratorDescriptors);
    element.setDebug(isDebug());
    element.setDefaultValue(index, defaultValueArray);
    element.setRawString(number);
    array.set(index, element);
  }

  void setElement(int index, ParsedElement element) {
    element.setDebug(isDebug());
    element.setDefaultValue(index, defaultValueArray);
    array.set(index, element);
  }

  void store(final Properties props, String prepend) {
    StringProperty property = new StringProperty(key);
    property.set(getRawString());
    property.store(props, prepend);
  }

  void load(final Properties props, String prepend) {
    StringProperty property = new StringProperty(key);
    property.load(props, prepend);
    setRawString(property.toString());
  }

  /**
   * Create a list of non-null ParsedNumber based on this.array.  Expand the
   * array descriptors to create the entire array.
   * @return list of ParsedNumbers
   */
  public List getParsedNumberExpandedArray(List parsedNumberExpandedArray) {
    for (int i = 0; i < array.size(); i++) {
      parsedNumberExpandedArray = array.get(i).getParsedNumberExpandedArray(
          parsedNumberExpandedArray);
    }
    return parsedNumberExpandedArray;
  }

  void moveElement(int fromIndex, int toIndex) {
    array.move(fromIndex, toIndex);
    array.get(toIndex).setDefaultValue(toIndex, defaultValueArray);
  }

  boolean isCollection() {
    return true;
  }

  boolean isDefaultedEmpty() {
    for (int i = 0; i < array.size(); i++) {
      if (!array.get(i).isDefaultedEmpty()) {
        return false;
      }
    }
    return true;
  }

  void removeElement(int index) {
    array.remove(index);
  }

  /**
   * Sets defaultValueArray and calls setDefaultValue() for each element in the
   * array.
   * @param numberIndex - not used by a collection
   */
  void setDefaultValue(int numberIndex, Integer[] defaultValueArray) {
    this.defaultValueArray = defaultValueArray;
    for (int i = 0; i < array.size(); i++) {
      array.get(i).setDefaultValue(i, defaultValueArray);
    }
  }

  /**
   * Return true when the array is empty or the array has 1 element and that
   * element has parsed number syntax.
   */
  boolean hasParsedNumberSyntax() {
    int size = array.size();
    if (isDebug()) {
      System.out.println("hasParsedNumberSyntax:size=" + size);
    }
    if (size == 0) {
      return true;
    }
    if (size == 1 && array.get(0).hasParsedNumberSyntax()) {
      return true;
    }
    if (!compact) {
      return false;
    }
    //compact descriptor show only non-empty numbers
    int elementCount = 0;
    for (int i = 0; i < size; i++) {
      if (!array.get(i).isDefaultedEmpty()) {
        elementCount++;
        if (elementCount > 1) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * parse the entire array
   * @return the token that is current when the array is parsed
   */
  Token parse(Token token, PrimativeTokenizer tokenizer) {
    array.clear();
    resetFailed();
    if (token == null) {
      return token;
    }
    try {
      if (token.is(Token.Type.WHITESPACE)) {
        token = tokenizer.next();
      }
      if (token == null) {
        return token;
      }
      if (!token.equals(Token.Type.SYMBOL, OPEN_SYMBOL.charValue())) {
        if (!flexibleSyntax) {
          fail(OPEN_SYMBOL + " never found.");
          return token;
        }
        //Parse this as a number because it is a number and flexibleSyntax is on.
        ParsedNumber number = new ParsedNumber(etomoNumberType,useIteratorDescriptors);
        number.setDebug(isDebug());
        number.setDefaultValue(0, defaultValueArray);
        token = number.parse(token, tokenizer);
        setElement(0, number);
        return token;
      }
      token = tokenizer.next();
      //Remove any whitespace before the first element.
      if (token != null && token.is(Token.Type.WHITESPACE)) {
        token = tokenizer.next();
      }
      token = parseArray(token, tokenizer, -1);
      if (isFailed()) {
        return token;
      }
      if (token != null && token.is(Token.Type.WHITESPACE)) {
        token = tokenizer.next();
      }
      if (token == null
          || !token.equals(Token.Type.SYMBOL, CLOSE_SYMBOL.charValue())) {
        fail(CLOSE_SYMBOL + " never found.");
        return token;
      }
      token = tokenizer.next();
    }
    catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return token;
  }

  /**
   * Create a ParsedArrayDescriptor in the position specified by index, if the
   * position is not a collection.  This creates the correct format and corrects
   * it if it changes.
   * @param arrayIndex
   */
  private void insureDescriptorExists(int index) {
    ParsedElement element = array.get(index);
    if (!element.isCollection()) {
      //this position must contain a descriptor
      ParsedDescriptor descriptor = newDescriptor();
      descriptor.setDebug(isDebug());
      descriptor.setDefaultValue(index, defaultValueArray);
      array.set(index, descriptor);
    }
  }

  /**
   * Raw strings may go to the screen
   * @param parsedNumberSyntax
   * @return
   */
  private String getRawString(EtomoBoolean2 parsedNumberSyntax, int startIndex) {
    return getString(false, startIndex);
  }

  /**
   * Returns a raw string or a parsable string.  A raw string and a parsable
   * string from a collection returns basically the same thing.  The
   * elements are separated by a divider but the "[" and "]" are not added.
   * If parsable is true, the divider may be a space.  If compact is
   * true and a parsable string is being created, exclude empty elements.
   * @param parsable - true when getting parsable string, false when getting a raw string
   * @param startIndex - start index
   */
  private String getString(boolean parsable, int startIndex) {
    if (startIndex == -1) {
      startIndex = 0;
    }
    StringBuffer buffer = new StringBuffer();
    //only use spaces as dividers when there are no empty elements and no array
    //descriptors
    boolean firstElementIsCollection = false;
    boolean empty = true;//set to false when a non-empty element is found
    boolean useSpaceAsDivider = parsable;
    boolean emptyString = true;//for adding the divider symbols
    for (int i = startIndex; i < array.size(); i++) {
      ParsedElement element = array.get(i);
      if (!(compact && parsable) || !element.isDefaultedEmpty()) {
        if (!emptyString) {
          buffer.append(DIVIDER_SYMBOL + " ");
        }
        emptyString = false;
        //Found out if the array contains only empty elements
        if (empty && !element.isDefaultedEmpty()) {
          empty = false;
        }
        //Use the divider symbol when creating a raw strings or a parsable string
        //that contains empty elements or collections.
        if (useSpaceAsDivider
            && (element.isDefaultedEmpty() || element.isCollection())) {
          useSpaceAsDivider = false;
        }
        //remove DIVIDER_SYMBOLs if useSpaceAsDivider is true
        if (useSpaceAsDivider) {
          int dividerIndex = buffer.indexOf(DIVIDER_SYMBOL.toString());
          while (dividerIndex != -1) {
            buffer.deleteCharAt(dividerIndex);
            dividerIndex = buffer.indexOf(DIVIDER_SYMBOL.toString());
          }
        }
        if (parsable) {
          buffer.append(element.getParsableString());
        }
        else {
          buffer.append(element.getRawString());
        }
      }
    }
    if (empty) {
      return "";
    }
    return buffer.toString();
  }

  private Token parseArray(Token token, PrimativeTokenizer tokenizer, int index) {
    if (token == null) {
      return null;
    }
    boolean dividerFound = true;
    //loop until the end of the array
    //can't just check dividerFound, because whitespace can act as a divider,
    //but isn't always the divider
    while (dividerFound && !isFailed() && token != null
        && !token.is(Token.Type.EOL) && !token.is(Token.Type.EOF)
        && !token.equals(Token.Type.SYMBOL, CLOSE_SYMBOL.charValue())) {
      try {
        //parse an element
        token = parseElement(token, tokenizer, index);
        if (index != -1) {
          index++;
        }
        //whitespace may be used as a divider, but it may not always be the divider -
        //it could just be whitespace at the end of the array or whitespace
        //before the divider
        dividerFound = false;
        if (token != null && token.is(Token.Type.WHITESPACE)) {
          dividerFound = true;
          token = tokenizer.next();
        }
        if (token != null
            && token.equals(Token.Type.SYMBOL, DIVIDER_SYMBOL.charValue())) {
          dividerFound = true;
          token = tokenizer.next();
          //A "," is being used as a divider character so remove whitespace
          //after divider
          if (token != null && token.is(Token.Type.WHITESPACE)) {
            token = tokenizer.next();
          }
        }
      }
      catch (IOException e) {
        e.printStackTrace();
        fail(e.getMessage());
      }
    }
    return token;
  }

  private Character getDescriptorDivider() {
    if (useIteratorDescriptors) {
      return ParsedIteratorDescriptor.DIVIDER_SYMBOL;
    }
    return ParsedArrayDescriptor.DIVIDER_SYMBOL;
  }

  private ParsedDescriptor newDescriptor() {
    if (useIteratorDescriptors) {
      return new ParsedIteratorDescriptor();
    }
    return new ParsedArrayDescriptor(etomoNumberType, compactDescriptor);
  }

  /**
   * Adds or sets either a ParsedArrayDescriptor or a ParsedNumber.
   * @param token
   * @param tokenizer
   * @param index adds element when index is -1
   * @return current token when done parsing the element
   */
  private Token parseElement(Token token, PrimativeTokenizer tokenizer,
      int index) {
    //end of list
    if (token.equals(Token.Type.SYMBOL, CLOSE_SYMBOL.charValue())) {
      //This probably doesn't matter because its only necessary to keep track of
      //location of existing elements in a lightly populated array.  Its not really
      //necessary to track the size of the array.  But this does preserve the
      //the original string:
      //[] means no elements, [,] means two elements.  [1,,] means three elements
      if (array.size() > 0) {
        array.addEmptyElement();
      }
      return token;
    }
    //found empty element
    if (token.is(Token.Type.WHITESPACE)
        || token.equals(Token.Type.SYMBOL, DIVIDER_SYMBOL.charValue())) {
      array.addEmptyElement();
      return token;
    }
    //parse element
    //Array descriptors don't have their own open and close symbols, so they
    //look like numbers until to you get to the first divider (":").
    ParsedDescriptor descriptor = newDescriptor();
    descriptor.setDebug(isDebug());
    token = descriptor.parse(token, tokenizer);
    //create the correct type of element
    ParsedElement element;
    if (descriptor.size() == 0) {
      //There's nothing there, so its an empty element
      element = ParsedElementList.EmptyParsedElement.INSTANCE;
    }
    else if (descriptor.size() == 1) {
      //If there is only one number, then there should be no ":" and its really a ParsedNumber.
      element = descriptor.getElement(0);
    }
    else {
      element = descriptor;
    }
    if (index == -1) {
      element.setDefaultValue(array.size(), defaultValueArray);
      array.add(element);
    }
    else {
      element.setDefaultValue(index, defaultValueArray);
      array.set(index, element);
    }
    return token;
  }
}
