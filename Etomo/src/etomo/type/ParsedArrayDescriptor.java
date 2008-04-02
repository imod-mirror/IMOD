package etomo.type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import etomo.ui.Token;
import etomo.util.PrimativeTokenizer;

/**
 * <p>Description: A description of an array.  A correct array descriptor
 * consists of the start, increment, and end values or the start and end values
 * (increment is assumed to be 1).  The increment may be negative which means
 * that the start value should be >= to the end value.  If the increment is
 * positive, the start value should be <= to the end value.  If the increment is
 * zero, the array described is [start, end].  If the descriptor contains only
 * one value, the array described is [value].  Values after the first three
 * values are ignored.
 * 
 * The descriptor member variable should only contain ParsedNumbers.  The string
 * representation of an array descriptor uses ":" to divide the values and does
 * not use open and close symbols such as square brackets.
 * 
 * The array descriptor is only used as an element of a ParsedArray.</p>
 * 
 * <H4>Matlab Variable Syntax</H4>
 * 
 * <H5>Array descriptor</H5><UL>
 * <LI>Delimiters: none
 * <LI>Divider: ":"
 * <LI>Empty descriptor:<UL>
 *   <LI>With 0 elements: illegal
 *   <LI>With 2 elements (j:k): empty if j > k
 *   <LI>With 3 elements (j:i:k): empty if<UL>
 *     <LI>i == 0 or 
 *     <LI>(i > 0 and j > k) or 
 *     <LI>(i < 0 and j < k)</UL></UL>
 * <LI>Elements: numbers
 * <LI>Number of elements: 2, or 3<UL>
 *   <LI>2 elements (j:k): [j,j+1,...,k]
 *   <LI>3 elements (j:i:k): [j,j+i,j+2i,...,k]</UL>
 * <LI>Empty element: With 2 elements, i is assumed to be 1.</UL>
 * 
 * @see ParsedList
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
 * <p> Revision 1.13  2007/11/06 19:39:34  sueh
 * <p> bug# 1047 Moved most of the code to parent class ParsedDescriptor so that
 * <p> ParsedIteratorDescriptor.
 * <p>
 * <p> Revision 1.12  2007/07/31 20:40:02  sueh
 * <p> bug# 1028 added ge(int).
 * <p>
 * <p> Revision 1.11  2007/07/31 16:30:00  sueh
 * <p> bug# 1033 In getArray(List) include the last number in the arrray, if it is one of the
 * <p> number specified by the start and increment numbers.
 * <p>
 * <p> Revision 1.10  2007/07/24 04:04:33  sueh
 * <p> bug# 1030 Fixed getArray(List); it was handling the last number incorrectly.
 * <p>
 * <p> Revision 1.9  2007/05/11 16:02:06  sueh
 * <p> bug# 964 Added getArray(List), which converts an array descriptor into
 * <p> an array.
 * <p>
 * <p> Revision 1.8  2007/05/03 21:08:59  sueh
 * <p> bug# 964 Fixed bug in hasParsedNumberSyntax().
 * <p>
 * <p> Revision 1.7  2007/04/26 02:47:06  sueh
 * <p> bug# 964 Fixed problems with defaultValue.  Added ParsedArray.compact
 * <p> when empty array elements should not be displayed (lstThresholds).
 * <p>
 * <p> Revision 1.6  2007/04/19 21:42:42  sueh
 * <p> bug# 964 Added boolean compact.  A compact instance ignores all the empty
 * <p> numbers when writing to the .prm file.
 * <p>
 * <p> Revision 1.5  2007/04/13 21:51:03  sueh
 * <p> bug# 964 Not returning ConstEtomoNumber from ParsedElement, because it
 * <p> must be returned with a getDefaulted... function to be accurate.
 * <p> GetReferenceVolume is returning ParsedElement instead.
 * <p>
 * <p> Revision 1.4  2007/04/13 20:14:25  sueh
 * <p> bug# 964 Added setRawString(String), which parses a list of numbers separated
 * <p> by :'s.
 * <p>
 * <p> Revision 1.3  2007/04/09 21:00:29  sueh
 * <p> bug# 964 Added parsing.
 * <p>
 * <p> Revision 1.2  2007/03/31 02:54:24  sueh
 * <p> bug# 964 Added isCollection().
 * <p>
 * <p> Revision 1.1  2007/03/30 23:41:13  sueh
 * <p> bug# 964 Class to parse Matlab array descriptors.
 * <p> </p>
 */

public final class ParsedArrayDescriptor extends ParsedDescriptor {
  public static final String rcsid = "$Id$";

  static final Character DIVIDER_SYMBOL = new Character(':');
  private static final int START_INDEX = 0;
  private static final int INCREMENT_INDEX = 1;
  private static final int END_INDEX = 2;
  private static final int NO_INCREMENT_SIZE = 2;
  private static final int MAX_SIZE = 3;
  private static final ParsedElementType type = ParsedElementType.MATLAB;

  private final ParsedElementList descriptor = new ParsedElementList(type);
  private final EtomoNumber.Type etomoNumberType;

  private boolean dividerParsed = false;

  public ParsedArrayDescriptor(final EtomoNumber.Type etomoNumberType) {
    this.etomoNumberType = etomoNumberType;
    descriptor.add(ParsedNumber.getArrayInstance(type, etomoNumberType));
    descriptor.add(ParsedNumber.getArrayInstance(type, etomoNumberType));
    descriptor.add(ParsedNumber.getArrayInstance(type, etomoNumberType));
  }

  public void setRawStringEnd(final String input) {
    descriptor.get(END_INDEX).setRawString(input);
  }

  public void setRawStringIncrement(final String input) {
    descriptor.get(INCREMENT_INDEX).setRawString(input);
  }

  public String getRawStringEnd() {
    return descriptor.get(END_INDEX).getRawString();
  }

  public String getRawStringIncrement() {
    return descriptor.get(INCREMENT_INDEX).getRawString();
  }

  /**
   * Empty in this case doesn't refer to the result of expanding this descriptor
   * but to the values in the descriptor.  Since class always creates three
   * ParsedNumbers in descriptor, function return true if all of the elements in
   * descriptor are empty.
   */
  public boolean isEmpty() {
    for (int i = 0; i < descriptor.size(); i++) {
      if (!descriptor.get(i).isEmpty()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Set each of the three elements in the descriptor with the string in the
   * first three elements in input.
   * @param input
   */
  public void set(final ParsedElement input) {
    clear();
    int max = Math.min(size(), input.size());
    for (int i = 0; i < max; i++) {
      setRawString(i, input.getRawString(i));
    }
  }

  /**
   * Set number at index if index between 0 and 2.
   */
  void setRawString(final int index, final float number) {
    if (index < 0) {
      return;
    }
    if (index > END_INDEX) {
      new IllegalStateException("Unable to add element " + index + 1
          + ".  No more then " + END_INDEX + 1
          + " elements are allowed in an array descriptor.").printStackTrace();
      return;
    }
    ParsedNumber element = (ParsedNumber) descriptor.get(index);
    element.clear();
    element.setDebug(isDebug());
    element.setRawString(number);
  }

  void setRawString(final int index, final String string) {
    if (index < 0) {
      return;
    }
    if (index > END_INDEX) {
      fail("Unable to add element " + index + 1 + ".  No more then "
          + END_INDEX + 1 + " elements are allowed in an array descriptor.");
      return;
    }
    ParsedNumber element = (ParsedNumber) descriptor.get(index);
    element.clear();
    element.setDebug(isDebug());
    element.setRawString(string);
  }

  String validate() {
    for (int i = 0; i < descriptor.size(); i++) {
      String errorMessage = descriptor.get(i).validate();
      if (errorMessage != null) {
        return errorMessage;
      }
    }
    if (descriptor.size() < NO_INCREMENT_SIZE || descriptor.size() > MAX_SIZE) {
      return "Array descriptors can contain either " + NO_INCREMENT_SIZE
          + " or " + MAX_SIZE + " elements.";
    }
    ParsedElement start = deriveLimit((ParsedNumber) descriptor.get(END_INDEX),
        true, descriptor.get(START_INDEX));
    ParsedElement end = deriveLimit((ParsedNumber) descriptor.get(START_INDEX),
        false, descriptor.get(END_INDEX));
    if (start.isEmpty() || end.isEmpty()) {
      return "Array descriptors must contain at least a start or an end.  "
          + "The missing value must be deriveable.";
    }
    return getFailedMessage();
  }

  boolean wasDividerParsed() {
    return dividerParsed;
  }

  Token parse(Token token, final PrimativeTokenizer tokenizer) {
    tokenizer.setDebug(isDebug());
    clear();
    resetFailed();
    if (token == null) {
      return null;
    }
    boolean dividerFound = true;
    //loop until a divider isn't found, this should be the end of the descriptor
    EtomoNumber index = new EtomoNumber();
    index.set(0);
    while (dividerFound && !isFailed()) {
      try {
        //parse an element.
        token = parseElement(token, tokenizer, index);
        //whitespace is not allowed in a file descriptor and it may be used as
        //an array divider, so it shouldn't be removed
        dividerFound = false;
        //if the divider symbol is found, continue parsing elements
        if (token != null
            && token.equals(Token.Type.SYMBOL, DIVIDER_SYMBOL.charValue())) {
          dividerFound = true;
          //Until the first divider is found this may not be a descriptor.
          dividerParsed = true;
          token = tokenizer.next();
        }
      }
      catch (IOException e) {
        e.printStackTrace();
        fail(e.getMessage());
      }
    }
    //If there are only 2 elements, then the second on should be in the end slot.
    if (index.equals(END_INDEX)) {
      ParsedElement increment = getElement(INCREMENT_INDEX);
      setRawString(END_INDEX, increment.getRawString());
      increment.clear();
    }
    return token;
  }

  void removeElement(final int index) {
    descriptor.get(index).clear();
  }

  ParsedElement getElement(final int index) {
    return descriptor.get(index);
  }

  /**
   * Append an array of non-empty ParsedNumbers described by this.descriptor.
   * Construct parsedNumberExpandedArray if it is null.
   * @param parsedNumberExpandedArray the array to be added to and returned
   * @return parsedNumberExpandedArray
   */
  List getParsedNumberExpandedArray(List parsedNumberExpandedArray) {
    if (parsedNumberExpandedArray == null) {
      parsedNumberExpandedArray = new ArrayList();
    }
    ParsedNumber start = (ParsedNumber) deriveLimit((ParsedNumber) descriptor
        .get(END_INDEX), true, descriptor.get(START_INDEX));
    ParsedNumber end = (ParsedNumber) deriveLimit((ParsedNumber) descriptor
        .get(START_INDEX), false, descriptor.get(END_INDEX));
    if (start.isEmpty() || end.isEmpty()) {
      //Invalid descriptor.
      return parsedNumberExpandedArray;
    }
    EtomoNumber increment = new EtomoNumber(etomoNumberType);
    increment.set(descriptor.get(INCREMENT_INDEX).getRawNumber());
    if (increment.isNull()) {
      increment.set(1);
    }
    if (increment.isNegative() && start.lt(end)) {
      //Empty descriptor
      return parsedNumberExpandedArray;
    }
    if (!increment.isNegative() && end.lt(start)) {
      //Empty descriptor
      return parsedNumberExpandedArray;
    }
    //Add the first element to the array.
    parsedNumberExpandedArray.add(start);
    if (start.equals(end)) {
      //Descriptor with one element.
      return parsedNumberExpandedArray;
    }
    //Add elements to the expanded array.
    EtomoNumber current = new EtomoNumber(etomoNumberType);
    current.set(start.getRawNumber());
    current.add(increment);
    EtomoNumber last = new EtomoNumber(etomoNumberType);
    last.set(end.getRawNumber());
    boolean increasing = !increment.isNegative();
    while ((increasing && current.lt(last))
        || (!increasing && current.gt(last))) {
      parsedNumberExpandedArray.add(current);
      current = new EtomoNumber(current);
      current.add(increment);
    }
    //Add the last element to the array.
    parsedNumberExpandedArray.add(end);
    return parsedNumberExpandedArray;
  }

  /**
   * Always returns three.  Size() is used when indexing the descriptor.  Use
   * isEmpty() to figure out if the descriptor contains no values.
   */
  int size() {
    return descriptor.size();
  }

  /**
   * Return a two or three element array descriptor.  Returns two elements if
   * increment is not set.  If start or end is not set, it may be derived from
   * the other limit.
   * @param parsable - true when creating a parsable string
   */
  String getString(final boolean parsable) {
    ParsedElement start = deriveLimit((ParsedNumber) descriptor.get(END_INDEX),
        true, descriptor.get(START_INDEX));
    if (isDebug()) {
      System.out.println("ParsedArrayDescriptor.getString:start=" + start);
    }
    ParsedElement increment = descriptor.get(INCREMENT_INDEX);
    ParsedElement end = deriveLimit((ParsedNumber) descriptor.get(START_INDEX),
        false, descriptor.get(END_INDEX));
    String startString;
    String incrementString;
    String endString;
    if (parsable) {
      startString = start.getParsableString();
      incrementString = increment.getParsableString();
      endString = end.getParsableString();
    }
    else {
      startString = start.getRawString();
      incrementString = increment.getRawString();
      endString = end.getRawString();
    }
    StringBuffer buffer = new StringBuffer();
    buffer.append(startString + DIVIDER_SYMBOL.toString());
    //Never use an empty increment element.
    if (!increment.isEmpty()) {
      buffer.append(incrementString + DIVIDER_SYMBOL.toString());
    }
    buffer.append(endString);
    return buffer.toString();
  }

  /**
   * The descriptor has a fixed size so don't clear it.  Only clear each
   * element.
   */
  void clear() {
    for (int i = 0; i < descriptor.size(); i++) {
      descriptor.get(i).clear();
    }
  }

  /**
   * Set element in descriptor to parsed element.  Only the first three elements
   * can be set.  Index is incremented each time function is run.
   * @param token
   * @param tokenizer
   * @param index
   * @return
   */
  private Token parseElement(Token token, final PrimativeTokenizer tokenizer,
      final EtomoNumber index) {
    //parse a number
    ParsedElement element = null;
    if (index.le(END_INDEX)) {
      element = descriptor.get(index.getInt());
      element.clear();
      element.setDebug(isDebug());
    }
    token = element.parse(token, tokenizer);
    if (element == null) {
      new IllegalStateException("Unable to add element " + index + 1
          + ".  No more then " + END_INDEX + 1
          + " elements are allowed in an array descriptor.").printStackTrace();
    }
    index.add(1);
    return token;
  }

  /**
   * Return limit if it exists.  If not then return a limit derived from
   * referenceLimit.  In that case limit == referenceLimit * -1 (handles an
   * array that goes from -j to j or from j to -j).  This function should not
   * change anything in descriptor.
   * @param referenceLimit
   * @param limit
   * @return limit or negated referenceLimit
   */
  private ParsedElement deriveLimit(final ParsedNumber referenceLimit,
      boolean endReference, final ParsedElement limit) {
    if (isDebug()) {
      System.out.println("ParsedArrayDescriptor.getString:referenceLimit="
          + referenceLimit + ",limit=" + limit);
    }
    //Limit already exists - return it.
    if (!limit.isEmpty()) {
      return limit;
    }
    //Try to derive limit from referenceLimit (limit = referenceLimit * -1).
    //referenceLimit doesn't exist or can't be negated.
    if (referenceLimit.isEmpty() || referenceLimit.equals(0)) {
      return limit;
    }
    //A missing increment is 1.
    //With an end reference, reference and increment must have the same signs in
    //order to derive the limit.
    //With a start reference, reference and increment must have the oposite
    //signs in order to derive the limit.
    ParsedNumber increment = (ParsedNumber) descriptor.get(INCREMENT_INDEX);
    if (endReference) {
      //Must have the same sign to derive the limit.
      if (increment.isEmpty()) {
        if (referenceLimit.isNegative()) {
          return limit;
        }
      }
      else if (referenceLimit.isNegative() != increment.isNegative()) {
        return limit;
      }
    }
    else {
      //Must have the opposite sign to derive the limit.
      if (increment.isEmpty()) {
        if (!referenceLimit.isNegative()) {
          return limit;
        }
      }
      else if (referenceLimit.isNegative() == increment.isNegative()) {
        return limit;
      }
    }
    ParsedNumber negatedReferenceLimit = ParsedNumber.getArrayInstance(type,
        etomoNumberType);
    negatedReferenceLimit.setRawString(referenceLimit.getNegatedRawNumber());
    return negatedReferenceLimit;
  }
}
