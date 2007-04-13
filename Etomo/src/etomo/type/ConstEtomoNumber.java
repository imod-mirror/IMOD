package etomo.type;

import java.util.Properties;
import java.util.Vector;

import etomo.storage.Storable;
import etomo.ui.UIHarness;
import etomo.util.Utilities;

/**
 * <p>Description: </p>
 * 
 * <p>DisplayValue verses defaultValue:</p>
 * 
 * <p>The display value is the value returned when the instance is null.  Use
 * the display value to prevent an instance from returning the null value or a
 * blank string.  The display value does not affect the result of the isNull()
 * function.<p>
 * 
 * <p>The default value is just an extra value that ConstEtomoNumber can store.
 * "Get" functions with the parameter "boolean defaultIfNull" are convenience
 * functions which return the default value when the instance is null.  Use the
 * default value as a way to store the default of the instance in one place.</p>
 * 
 * <p>ScriptParameter uses the default value to decide whether an instance needs
 * to be placed in a script.</p>
 * 
 * <p>Copyright: Copyright (c) 2002 - 2006</p>
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
 * <p> Revision 1.47  2007/03/30 23:40:34  sueh
 * <p> bug# 964 Made ConstEtomoNumber(Type) work correctly when parameter is null.
 * <p>
 * <p> Revision 1.46  2007/03/26 18:37:07  sueh
 * <p> bug# 964 Changed getDouble(boolean defaultIfNull) to getDefaultDouble() so that
 * <p> the functionality will be remembered and used.
 * <p>
 * <p> Revision 1.45  2007/02/06 19:47:45  sueh
 * <p> bug# 962 Fixed failure in unit test.
 * <p>
 * <p> Revision 1.44  2007/02/05 23:10:02  sueh
 * <p> bug# 962 Moved EtomoNumber type info to inner class.
 * <p>
 * <p> Revision 1.43  2006/11/15 20:43:19  sueh
 * <p> bug# 872 Fixed bug - store and remove are not handling a null prepend.
 * <p>
 * <p> Revision 1.42  2006/10/17 20:04:25  sueh
 * <p> bug# 939  Added defaultValue, getDouble(boolean), getValue(boolean),
 * <p> isDefault(Number), setDefault(int), and useDefaultAsDisplayValue().  Placed the
 * <p> code from initialize(Number) into initialize().
 * <p>
 * <p> Revision 1.41  2006/09/13 23:31:30  sueh
 * <p> bug# 920 Added getFloat().
 * <p>
 * <p> Revision 1.40  2006/08/29 20:04:53  sueh
 * <p> bug# 924 Added static store for storing optional member variables.
 * <p>
 * <p> Revision 1.39  2006/07/19 15:22:44  sueh
 * <p> bug# 903 Added get(int) and get(ConstEtomoNumber).
 * <p>
 * <p> Revision 1.38  2006/06/27 18:30:45  sueh
 * <p> bug# 692 Removed todo comment
 * <p>
 * <p> Revision 1.37  2006/06/22 21:13:58  sueh
 * <p> *** empty log message ***
 * <p>
 * <p> Revision 1.36  2006/06/21 16:50:11  sueh
 * <p> bug# 692 Fix null pointer exception in getInvalidReason().  Add
 * <p> resetInvalidReason() call to setValidFloor().
 * <p>
 * <p> Revision 1.35  2006/06/09 21:53:32  sueh
 * <p> bug# 692 Added resetInvalidReason() calls to setValidValues() and
 * <p> setNullIsValid().  Added debug, which can be set  for an instance and used to
 * <p> debug validation.
 * <p>
 * <p> Revision 1.34  2006/06/08 19:46:21  sueh
 * <p> bug# 692 Changed the float and double null constants to capitals
 * <p>
 * <p> Revision 1.33  2006/06/07 23:50:18  sueh
 * <p> bug# 692 Changed selfTest to internalTest.  Changed setTestCopy to
 * <p> internalTestDeepCopy and stopped calling it from the copy constructor.
 * <p>
 * <p> Revision 1.32  2006/04/06 20:09:48  sueh
 * <p> bug# 808 Fixed a bug: equals(constEtomoNumber) wasn't handling a null
 * <p> ConstEtomoNumber.
 * <p>
 * <p> Revision 1.31  2006/01/27 18:39:06  sueh
 * <p> bug# 801 Added isInt() to get the type of the number.
 * <p>
 * <p> Revision 1.30  2005/10/27 00:31:01  sueh
 * <p> bug# 725 Added newNumber(float) and validateInputType(float).
 * <p>
 * <p> Revision 1.29  2005/07/29 19:46:14  sueh
 * <p> bug# 692 Changed ConstEtomoNumber.getInteger() to getInt.
 * <p>
 * <p> Revision 1.28  2005/07/26 23:00:07  sueh
 * <p> bug# 692
 * <p>
 * <p> Revision 1.27  2005/07/21 22:00:51  sueh
 * <p> bug# 532 Added validFloor.
 * <p>
 * <p> Revision 1.26  2005/06/21 16:32:03  sueh
 * <p> bug# 692 Added getType().
 * <p>
 * <p> Revision 1.25  2005/06/20 16:53:11  sueh
 * <p> bug# 692 Moved selftest convenience variable to util.Utilities.  Change
 * <p> validateCopy() to selfTestCopy() because it is just testing the correctness
 * <p> of ConstEtomoNumber code.
 * <p>
 * <p> Revision 1.24  2005/06/16 21:20:31  sueh
 * <p> bug# 692 Fixed validateCopy().
 * <p>
 * <p> Revision 1.23  2005/06/16 20:00:00  sueh
 * <p> bug# 692 Making self test variables boolean instead of EtomoBoolean2 to
 * <p> avoid test problems.  Added validation functions.
 * <p>
 * <p> Revision 1.22  2005/06/14 23:06:34  sueh
 * <p> bug# 687 Fixed toString(Vector), which was only returning the first two
 * <p> elements and repeating the second element.
 * <p>
 * <p> Revision 1.21  2005/06/13 23:36:16  sueh
 * <p> bug# 583 Fixed a bug in newNumber(double) where it was setting a long
 * <p> null value to the int null value.
 * <p>
 * <p> Revision 1.20  2005/06/10 23:18:06  sueh
 * <p> bug# 583 Added member variables:  floorValue, selfTest.  Add functions:
 * <p> isNamed, runSelfTest, selfTest, setFloor.
 * <p>
 * <p> Revision 1.19  2005/05/12 01:27:39  sueh
 * <p> bug# 658 Removed recommendedValue, since it isn't practical to get to
 * <p> information required to set it.  Improved invalidReason messages.  Allow
 * <p> isValid() and validate() to get the field description from the caller.
 * <p>
 * <p> Revision 1.18  2005/05/10 02:20:16  sueh
 * <p> bug# 658 corrected comment
 * <p>
 * <p> Revision 1.17  2005/05/10 02:18:25  sueh
 * <p> bug# 658 Setting invalidReason with functions resetInvalidReason and
 * <p> addInvalidReason.  Change validate() to setInvalidReason().  Make
 * <p> validate() a public function like isValid(), except that it throws an
 * <p> exception.  The preventNullValue member variable is unnecessary because
 * <p> just setting displayValue has the same effect - remove it.  Add
 * <p> nullIsValid member variable (default true).  NullIsValid set to false
 * <p> doesn't prevent current value from being null, so a display value isn't
 * <p> necessary.  But, isValid() or validate() will fail in this situation.
 * <p> Added recommended value.  This value will only appear in invalidReason
 * <p> messages as a suggestion.  Removed useDisplayValue, since turning
 * <p> displayValue on and off is confusing.  Fixed a bug in toString(Vector).
 * <p> To create a required field:
 * <p> Call setNullIsValid(false)
 * <p> If possible call setRecommendedValue()
 * <p> Call isValid() or validate() to validate the field
 * <p>
 * <p> Revision 1.16  2005/04/25 20:50:29  sueh
 * <p> bug# 615 Passing the axis where a command originates to the message
 * <p> functions so that the message will be popped up in the correct window.
 * <p> This requires adding AxisID to many objects.
 * <p>
 * <p> Revision 1.15  2005/03/08 01:59:56  sueh
 * <p> bug# 533 Making the long null value public.
 * <p>
 * <p> Revision 1.14  2005/01/25 22:48:00  sueh
 * <p> Added is(Number value) to convert value to a boolean.
 * <p>
 * <p> Revision 1.13  2005/01/25 21:56:30  sueh
 * <p> Changing resetValue to displayValue.  Removing empty functionality.  Removing
 * <p> displayDefault functionality.  Adding boolean useDisplayValue, but not
 * <p> adding function to set it, because it is equivalent to setting
 * <p> displayValue to null.  An inheriting class might want to turn off
 * <p> useDisplayValue to prevent displayValue from being used on the screen.
 * <p> Store only the currentValue.  Moving defaultValue to inherit class
 * <p> ScriptParameter.
 * <p>
 * <p> Revision 1.12  2005/01/22 04:11:06  sueh
 * <p> bug# 509, bug# 591  Ignore empty in comparisons.  Change validate() to
 * <p> ignore empty when looking at validValues.  Also in validate() fail on null
 * <p> or empty if preventNullValue is on.
 * <p>
 * <p> Revision 1.11  2005/01/21 23:04:12  sueh
 * <p> bug# 509 bug# 591  Added isUpdateCommand() in place of
 * <p> isSetAndNotDefault() as a standard why to decide if a parameter should
 * <p> be placed in a comscript.  Changed the name of value to currentValue.
 * <p> Added an empty value.  The currentValue is set to empty value when
 * <p> set(String) receives an empty or whitespace filled string.  The distinguishes
 * <p> it from a currentValue that was never set or set incorrectly.  THe only
 * <p> function which doesn't treate empty the same as null is getValue().  This
 * <p> allows EtomoNumber to display a blank field, even when the resetValue is
 * <p> in use.  However, since store() and updateCommand(), and
 * <p> isUpdateCommand() treate empty just like a null, the blank field isn't
 * <p> remembered.  This prevents the comscript, .edf, .ejf files from having lots
 * <p> of empty fields in them.  This means that the reset value will show when
 * <p> they are reloaded.  If a blank numeric field must be remembered and reload
 * <p> blank, write an alternate way to save it, treating empty like a regular value.
 * <p>
 * <p> Revision 1.10  2005/01/14 23:02:34  sueh
 * <p> Removing originalVersion because it is not being used.
 * <p>
 * <p> Revision 1.9  2005/01/10 23:48:08  sueh
 * <p> bug# 578 Changing getValue() to protected, so it can be used by
 * <p> EtomoState.
 * <p>
 * <p> Revision 1.8  2005/01/10 23:26:46  sueh
 * <p> bug# 578 Standardized class so that every use of value goes through
 * <p> getValue().  GetValue() tries to find a non-null value by looking first at
 * <p> value, then resetValue, and then defaultValue (if displayDefault is set).
 * <p> Replacing isNull() with isSet().  IsSet() does not use getValue(), since it is
 * <p> querying whether the value was set (by set(), resetValue() with a non-null
 * <p> resetValue, or with an initialValue).  Added a new isNull() that uses
 * <p> getValue().
 * <p>
 * <p> Revision 1.7  2005/01/06 18:16:29  sueh
 * <p> bug# 578 Make integer null value static public.
 * <p>
 * <p> Revision 1.6  2004/12/29 00:05:29  sueh
 * <p> bug# 567 Added update(ComScriptCommand) to update value where the
 * <p> keyword in ComScriptCommand equals name.  Added validValues:  a list
 * <p> of valid values.  Added validate() to check validValues when value is
 * <p> changed.
 * <p>
 * <p> Revision 1.5  2004/12/16 02:27:18  sueh
 * <p> bug# 564 Remove recommendedValue.  Use resetValue instead.  Added
 * <p> is().
 * <p>
 * <p> Revision 1.4  2004/11/30 00:35:03  sueh
 * <p> bug# 556 Making isValid() error message clearer.
 * <p>
 * <p> Revision 1.3  2004/11/24 01:04:01  sueh
 * <p> bug# 520 Allow class to display its own error message when required
 * <p> (isValue).
 * <p>
 * <p> Revision 1.2  2004/11/19 23:33:29  sueh
 * <p> bug# 520 merging Etomo_3-4-6_JOIN branch to head.
 * <p>
 * <p> Revision 1.1.2.4  2004/11/19 19:38:23  sueh
 * <p> bug# 520 Made getValue() private.
 * <p>
 * <p> Revision 1.1.2.3  2004/11/19 03:02:57  sueh
 * <p> bug# 520 Added a default displayDefault.  If not overriden it will affect how
 * <p> get, toString, and equals fuctions work.  The default value is used only if
 * <p> displayDefault is true.  DisplayDefault can be overriden by a parameter.
 * <p> Added getValue to simplify choosing the first non-null value to work with.
 * <p>
 * <p> Revision 1.1.2.2  2004/11/19 00:04:05  sueh
 * <p> bug# 520 changed the equals functions so that they work on the same
 * <p> principle as the get functions, since they will be comparing values that
 * <p> came from get.  If value is null, compare resetValue.  Added a useDefault
 * <p> boolean:  if value and resetValue are null, compare defaultValue.
 * <p>
 * <p> Revision 1.1.2.1  2004/11/16 02:26:06  sueh
 * <p> bug# 520 Replacing EtomoInteger, EtomoDouble, EtomoFloat, and
 * <p> EtomoLong with EtomoNumber.  EtomoNumber acts a simple numeric
 * <p> type which handles null values, defaults, and recommended values.
 * <p> EtomoNumber stores its values in Number variables and is created with a
 * <p> required type parameter to keep track of its numeric type.
 * <p> </p>
 */
public abstract class ConstEtomoNumber implements Storable {
  public static final String rcsid = "$Id$";

  //null values
  private static final double DOUBLE_NULL_VALUE = Double.NaN;
  public static final float FLOAT_NULL_VALUE = Float.NaN;
  public static final int INTEGER_NULL_VALUE = Integer.MIN_VALUE;
  public static final long LONG_NULL_VALUE = Long.MIN_VALUE;
  //null for types not currently supported
  private static final short shortNullValue = Short.MIN_VALUE;
  private static final byte byteNullValue = Byte.MIN_VALUE;

  //type
  protected final Type type;//defaults to integer, can't be changed once it is set
  //name and description
  protected final String name;//defaults to Object.toString(), can't be changed once it is set
  protected String description;//optional, defaults to name
  //value of the instance
  protected Number currentValue;//optional, defaults to newNumber()
  //value to display when currentValue isNull()
  protected Number displayValue;//optional, defaults to newNumber()
  //numbers that can modify currentValue
  protected Number ceilingValue;//optional, defaults to newNumber()
  protected Number floorValue;//optional, defaults to newNumber()

  //validatation numbers.  Use isValid() or validate() to find out if
  //  currentValue is valid.
  //  validValues overrides validFloor
  protected boolean nullIsValid = true;//optional
  //set of valid values
  protected Vector validValues = null;//optional
  //number below validFloor are invalid
  private Number validFloor;//optional, defaults to newNumber()
  protected Number defaultValue;

  //internal validation result
  protected StringBuffer invalidReason = null;
  private boolean debug = false;

  /**
   * Construct a ConstEtomoNumber with type = INTEGER_TYPE
   *
   */
  protected ConstEtomoNumber() {
    type = Type.INTEGER;
    name = super.toString();
    description = name;
    initialize();
  }

  /**
   * Construct a ConstEtomoNumber with type = INTEGER_TYPE
   * @param name the name of the instance
   */
  protected ConstEtomoNumber(String name) {
    type = Type.INTEGER;
    this.name = name;
    description = name;
    initialize();
  }

  protected ConstEtomoNumber(Type type) {
    if (type == null) {
      this.type = Type.INTEGER;
    }
    else {
      this.type = type;
    }
    name = super.toString();
    description = name;
    initialize();
  }

  protected ConstEtomoNumber(Type type, String name) {
    this.type = type;
    this.name = name;
    description = name;
    initialize();
  }

  /**
   * Makes a deep copy of instance.
   * Returns empty instance when instance is null
   * @param that
   */
  protected ConstEtomoNumber(ConstEtomoNumber instance) {
    if (instance == null) {
      type = Type.INTEGER;
      name = super.toString();
      description = name;
      initialize();
      return;
    }
    type = instance.type;
    //OK to assign Strings because they are immutable
    name = instance.name;
    description = instance.description;
    //OK to assign Numbers because they are immutable
    currentValue = instance.currentValue;
    displayValue = instance.displayValue;
    ceilingValue = instance.ceilingValue;
    floorValue = instance.floorValue;
    validFloor = instance.validFloor;
    nullIsValid = instance.nullIsValid;
    defaultValue = instance.defaultValue;
    if (instance.validValues != null && instance.validValues.size() > 0) {
      validValues = new Vector(instance.validValues.size());
      for (int i = 0; i < instance.validValues.size(); i++) {
        validValues.add(newNumber((Number) instance.validValues.get(i)));
      }
    }
    if (instance.invalidReason != null) {
      invalidReason = new StringBuffer(instance.invalidReason.toString());
    }
  }

  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
  }

  public int getDisplayInteger() {
    validateReturnTypeInteger();
    return displayValue.intValue();
  }

  /**
   * If validValues has been set, look for currentValue in validValues. Set
   * invalidReasion if  currentValue is not found.  Null is ignored.
   * Set invalidReason if currentValue is null and nullIsValid is false.
   */
  protected void setInvalidReason() {
    if (debug) {
      System.err.println(description + ":start setInvalidReason: currentValue="
          + currentValue);
    }
    //Pass when there are no validation settings
    if (nullIsValid && validValues == null && isNull(validFloor)) {
      if (debug) {
        System.err.println(description
            + ":end setInvalidReason: no validation values set");
      }
      return;
    }
    //Catch illegal null values
    if (isNull(currentValue)) {
      if (nullIsValid) {
        if (debug) {
          System.err.println(description
              + ":end setInvalidReason:  null not allowed");
        }
        return;
      }
      addInvalidReason("This field cannot be empty.");
    }
    //Validate against validValues, overrides validFloor
    else if (validValues != null) {
      if (debug) {
        System.err.println(description + ":setInvalidReason:  validValues="
            + validValues);
      }
      for (int i = 0; i < validValues.size(); i++) {
        if (equals(currentValue, (Number) validValues.get(i))) {
          if (debug) {
            System.err.println(description
                + ":end setInvalidReason:  valid value=" + validValues.get(i));
          }
          return;
        }
      }
      addInvalidReason(toString(currentValue) + " is not a valid value.");
      addInvalidReason("Valid values are " + toString(validValues) + ".");
      if (debug) {
        System.err.println(description
            + ":end setInvalidReason:  current value not in valid value list");
      }
      return;
    }
    //If validValues is not set, validate against validFloor
    else if (!isNull(validFloor)) {
      if (ge(currentValue, validFloor)) {
        if (debug) {
          System.err.println(description
              + ":end setInvalidReason:  valid floor=" + validFloor);
        }
        return;
      }
      addInvalidReason(toString(currentValue) + " is not a valid value.");
      addInvalidReason("Valid values are greater or equal to "
          + toString(validFloor) + ".");
      if (debug) {
        System.err
            .println("setInvalidReason:  current value less then valid floor");
      }
    }
    if (debug) {
      System.err.println(description + ":end setInvalidReason:  valid");
    }
  }

  /**
   * Returns ceilingValue if value > ceilingValue.
   * Otherwise returns value.
   * Ignores null
   * @param value
   * @return
   */
  protected Number applyCeilingValue(Number value) {
    if (value != null && !isNull(ceilingValue) && !isNull(value)
        && gt(value, ceilingValue)) {
      return newNumber(ceilingValue);
    }
    return value;
  }

  /**
   * Returns floorValue if value < floorValue.  Otherwise returns values.
   * Ignores null.
   * @param value
   * @return
   */
  protected Number applyFloorValue(Number value) {
    if (value != null && !isNull(floorValue) && !isNull(value)
        && lt(value, floorValue)) {
      return newNumber(floorValue);
    }
    return value;
  }

  /**
   * If invalidReason is set, return true
   * @return
   */
  public boolean isValid() {
    return invalidReason == null;
  }

  /**
   * If invalidReason is set, display an error message and throw an exception
   * @param errorTitle
   * @param axisID
   * @throws InvalidEtomoNumberException
   */
  public void validate(String errorTitle, String description, AxisID axisID)
      throws InvalidEtomoNumberException {
    if (!isValid(true, errorTitle, description, axisID)) {
      throw new InvalidEtomoNumberException(invalidReason.toString());
    }
  }

  /**
   * If invalidReason is set, display an error message and return true
   * @param displayErrorMessage
   * @param errorTitle
   * @param axisID
   * @return
   */
  public boolean isValid(boolean displayErrorMessage, String errorTitle,
      AxisID axisID) {
    return isValid(displayErrorMessage, errorTitle, null, axisID);
  }

  /**
   * If invalidReason is set, display an error message and return true
   * @param errorTitle
   * @param description
   * @param axisID
   * @return
   */
  public boolean isValid(String errorTitle, String description, AxisID axisID) {
    return isValid(true, errorTitle, description, axisID);
  }

  /**
   * If invalidReason is set, display an error message and return true
   * @param displayErrorMessage
   * @param errorTitle
   * @param description
   * @param axisID
   * @return
   */
  public boolean isValid(boolean displayErrorMessage, String errorTitle,
      String description, AxisID axisID) {
    if (invalidReason != null && displayErrorMessage) {
      if (description == null) {
        UIHarness.INSTANCE.openMessageDialog(this.description + ": "
            + invalidReason, errorTitle, axisID);
      }
      else {
        description = description.trim();
        if (description.endsWith(":")) {
          UIHarness.INSTANCE.openMessageDialog(description + "  "
              + invalidReason, errorTitle, axisID);
        }
        else {
          UIHarness.INSTANCE.openMessageDialog(description + ":  "
              + invalidReason, errorTitle, axisID);
        }
      }
    }
    return invalidReason == null;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public String getInvalidReason() {
    if (invalidReason == null) {
      return "";
    }
    return invalidReason.toString();
  }

  public String classInfoString() {
    return getClass().getName() + "[" + paramString() + "]";
  }

  protected String paramString() {
    StringBuffer buffer = new StringBuffer(",\ntype=" + type + ",\nname="
        + name + ",\ndescription=" + description + ",\ninvalidReason="
        + invalidReason + ",\ncurrentValue=" + currentValue
        + ",\ndisplayValue=" + displayValue + ",\nceilingValue=" + ceilingValue
        + ",\nfloorValue=" + floorValue + ",\nnullIsValid=" + nullIsValid
        + ",\nvalidValues=");
    if (validValues == null) {
      buffer.append("null");
    }
    else {
      buffer.append(validValues.toString());
    }
    return buffer.toString();
  }

  /**
   * Sets ceiling value.  Ceiling value is applied when the value is set; if the
   * value being set is more then the ceiling value, the value is set equals to
   * the ceiling value.
   * @param ceilingValue
   * @return
   */
  public ConstEtomoNumber setCeiling(int ceilingValue) {
    this.ceilingValue = newNumber(ceilingValue);
    validateFloorAndCeiling();
    currentValue = applyCeilingValue(currentValue);
    return this;
  }

  /**
   * Sets floor value.  Floor value is applied when the value is set; if the
   * value being set is less then the floor value, the value is set equals to
   * the floor value.
   * @param floorValue
   * @return
   */
  public ConstEtomoNumber setFloor(int floorValue) {
    this.floorValue = newNumber(floorValue);
    validateFloorAndCeiling();
    currentValue = applyFloorValue(currentValue);
    return this;
  }

  void internalTest() {
    if (!Utilities.isSelfTest()) {
      return;
    }
    //Name should never be null.
    if (name == null) {
      throw new IllegalStateException("name cannot be null.");
    }
    //currentValue should never be null
    if (currentValue == null) {
      throw new IllegalStateException("currentValue cannot be null.");
    }
    //displayValue should never be null
    if (displayValue == null) {
      throw new IllegalStateException("displayValue cannot be null.");
    }
    //ceilingValue should never be null
    if (ceilingValue == null) {
      throw new IllegalStateException("ceilingValue cannot be null.");
    }
    //floorValue should never be null
    if (floorValue == null) {
      throw new IllegalStateException("floorValue cannot be null.");
    }
    //validFloor should never be null
    if (validFloor == null) {
      throw new IllegalStateException("validFloor cannot be null.");
    }
    //defaultValue should never be null
    if (defaultValue == null) {
      throw new IllegalStateException("defaultValue cannot be null.");
    }
    //Type should be either double, float, integer, or long.
    if (type != Type.DOUBLE && type != Type.FLOAT && type != Type.INTEGER
        && type != Type.LONG) {
      throw new IllegalStateException("type is not valid.  type=" + type);
    }
    //Type constants must be unique.
    if (Type.DOUBLE == Type.FLOAT || Type.DOUBLE == Type.INTEGER
        || Type.DOUBLE == Type.LONG || Type.FLOAT == Type.INTEGER
        || Type.FLOAT == Type.LONG || Type.INTEGER == Type.LONG) {
      throw new IllegalStateException(
          "Type constants are the same.\nDOUBLE_TYPE=" + Type.DOUBLE
              + ",FLOAT_TYPE=" + Type.FLOAT + ",INTEGER_TYPE=" + Type.INTEGER
              + ",LONG_TYPE=" + Type.LONG);
    }
    //All members of type Number must be created with the current type
    //The type should be set only once.
    if (type == Type.DOUBLE) {
      if (!(currentValue instanceof Double)) {
        throw new IllegalStateException(
            "currentValue doesn't match the current type.  currentValue.getClass()="
                + currentValue.getClass() + ",type=" + type);
      }
      if (!(displayValue instanceof Double)) {
        throw new IllegalStateException(
            "displayValue doesn't match the current type.  displayValue.getClass()="
                + displayValue.getClass() + ",type=" + type);
      }
      if (!(ceilingValue instanceof Double)) {
        throw new IllegalStateException(
            "ceilingValue doesn't match the current type.  ceilingValue.getClass()="
                + ceilingValue.getClass() + ",type=" + type);
      }
      if (!(floorValue instanceof Double)) {
        throw new IllegalStateException(
            "floorValue doesn't match the current type.  floorValue.getClass()="
                + floorValue.getClass() + ",type=" + type);
      }
      if (!(validFloor instanceof Double)) {
        throw new IllegalStateException(
            "validFloor doesn't match the current type.  validFloor.getClass()="
                + validFloor.getClass() + ",type=" + type);
      }
      if (!(defaultValue instanceof Double)) {
        throw new IllegalStateException(
            "defaultValue doesn't match the current type.  defaultValue.getClass()="
                + defaultValue.getClass() + ",type=" + type);
      }
    }
    if (type == Type.FLOAT) {
      if (!(currentValue instanceof Float)) {
        throw new IllegalStateException(
            "currentValue doesn't match the current type.  currentValue.getClass()="
                + currentValue.getClass() + ",type=" + type);
      }
      if (!(displayValue instanceof Float)) {
        throw new IllegalStateException(
            "displayValue doesn't match the current type.  displayValue.getClass()="
                + displayValue.getClass() + ",type=" + type);
      }
      if (!(ceilingValue instanceof Float)) {
        throw new IllegalStateException(
            "ceilingValue doesn't match the current type.  ceilingValue.getClass()="
                + ceilingValue.getClass() + ",type=" + type);
      }
      if (!(floorValue instanceof Float)) {
        throw new IllegalStateException(
            "floorValue doesn't match the current type.  floorValue.getClass()="
                + floorValue.getClass() + ",type=" + type);
      }
      if (!(validFloor instanceof Float)) {
        throw new IllegalStateException(
            "validFloor doesn't match the current type.  validFloor.getClass()="
                + validFloor.getClass() + ",type=" + type);
      }
      if (!(defaultValue instanceof Float)) {
        throw new IllegalStateException(
            "defaultValue doesn't match the current type.  defaultValue.getClass()="
                + defaultValue.getClass() + ",type=" + type);
      }
    }
    if (type == Type.INTEGER) {
      if (!(currentValue instanceof Integer)) {
        throw new IllegalStateException(
            "currentValue doesn't match the current type.  currentValue.getClass()="
                + currentValue.getClass() + ",type=" + type);
      }
      if (!(displayValue instanceof Integer)) {
        throw new IllegalStateException(
            "displayValue doesn't match the current type.  displayValue.getClass()="
                + displayValue.getClass() + ",type=" + type);
      }
      if (!(ceilingValue instanceof Integer)) {
        throw new IllegalStateException(
            "ceilingValue doesn't match the current type.  ceilingValue.getClass()="
                + ceilingValue.getClass() + ",type=" + type);
      }
      if (!(floorValue instanceof Integer)) {
        throw new IllegalStateException(
            "floorValue doesn't match the current type.  floorValue.getClass()="
                + floorValue.getClass() + ",type=" + type);
      }
      if (!(validFloor instanceof Integer)) {
        throw new IllegalStateException(
            "validFloor doesn't match the current type.  validFloor.getClass()="
                + validFloor.getClass() + ",type=" + type);
      }
      if (!(defaultValue instanceof Integer)) {
        throw new IllegalStateException(
            "defaultValue doesn't match the current type.  defaultValue.getClass()="
                + defaultValue.getClass() + ",type=" + type);
      }
    }
    if (type == Type.LONG) {
      if (!(currentValue instanceof Long)) {
        throw new IllegalStateException(
            "currentValue doesn't match the current type.  currentValue.getClass()="
                + currentValue.getClass() + ",type=" + type);
      }
      if (!(displayValue instanceof Long)) {
        throw new IllegalStateException(
            "displayValue doesn't match the current type.  displayValue.getClass()="
                + displayValue.getClass() + ",type=" + type);
      }
      if (!(ceilingValue instanceof Long)) {
        throw new IllegalStateException(
            "ceilingValue doesn't match the current type.  ceilingValue.getClass()="
                + ceilingValue.getClass() + ",type=" + type);
      }
      if (!(floorValue instanceof Long)) {
        throw new IllegalStateException(
            "floorValue doesn't match the current type.  floorValue.getClass()="
                + floorValue.getClass() + ",type=" + type);
      }
      if (!(validFloor instanceof Long)) {
        throw new IllegalStateException(
            "validFloor doesn't match the current type.  validFloor.getClass()="
                + validFloor.getClass() + ",type=" + type);
      }
      if (!(defaultValue instanceof Long)) {
        throw new IllegalStateException(
            "defaultValue doesn't match the current type.  defaultValue.getClass()="
                + defaultValue.getClass() + ",type=" + type);
      }
    }
    //floorValue <= ceilingValue
    validateFloorAndCeiling();
    //valid validValues
    if (validValues != null) {
      for (int i = 0; i < validValues.size(); i++) {
        if (isNull((Number) validValues.get(i))) {
          throw new IllegalStateException(
              "ValidValues elements cannot be null.");
        }
      }
    }
    //test deep copy on assignment
    if (currentValue == displayValue) {
      throw new IllegalStateException(
          "newNumber() was not use in an assignment:  currentValue, displayValue");
    }
    if (currentValue == ceilingValue) {
      throw new IllegalStateException(
          "newNumber() was not use in an assignment:  currentValue, ceilingValue");
    }
    if (currentValue == floorValue) {
      throw new IllegalStateException(
          "newNumber() was not use in an assignment:  currentValue, floorValue.");
    }
    if (currentValue == validFloor) {
      throw new IllegalStateException(
          "newNumber() was not use in an assignment:  currentValue, validFloor.");
    }
    if (displayValue == ceilingValue) {
      throw new IllegalStateException(
          "newNumber() was not use in an assignment:  displayValue, ceilingValue");
    }
    if (displayValue == floorValue) {
      throw new IllegalStateException(
          "newNumber() was not use in an assignment:  displayValue, floorValue.");
    }
    if (displayValue == validFloor) {
      throw new IllegalStateException(
          "newNumber() was not use in an assignment:  displayValue, validFloor.");
    }
    if (ceilingValue == floorValue) {
      throw new IllegalStateException(
          "newNumber() was not use in an assignment:  ceilingValue, floorValue.");
    }
    if (ceilingValue == validFloor) {
      throw new IllegalStateException(
          "newNumber() was not use in an assignment:  ceilingValue, validFloor.");
    }
    if (floorValue == validFloor) {
      throw new IllegalStateException(
          "newNumber() was not use in an assignment:  floorValue, validFloor.");
    }
  }

  /**
   * Set the value will be returned if the user does not set a value or there is no
   * value to load.
   * @param displayValue
   * @return
   */
  public ConstEtomoNumber setDisplayValue(int displayValue) {
    this.displayValue = newNumber(displayValue);
    return this;
  }

  public ConstEtomoNumber setDisplayValue(boolean displayValue) {
    this.displayValue = newNumber(displayValue);
    return this;
  }

  protected ConstEtomoNumber setDisplayValue(Number displayValue) {
    this.displayValue = newNumber(displayValue);
    return this;
  }

  /**
   * Set the value will be used if the user does not set a value or there is no
   * value to load.  Also used in reset().
   * @param resetValue
   * @return
   */
  public void setDisplayValue(double displayValue) {
    this.displayValue = newNumber(displayValue);
  }

  public void setDisplayValue(long displayValue) {
    this.displayValue = newNumber(displayValue);
  }

  public void setDescription(String description) {
    if (description != null) {
      this.description = description;
    }
    else {
      this.description = name;
    }
  }

  public ConstEtomoNumber setNullIsValid(boolean nullIsValid) {
    resetInvalidReason();
    this.nullIsValid = nullIsValid;
    setInvalidReason();
    return this;
  }

  /**
   * Set a list of non-null valid values
   * A null param or an empty list causes this.validValues to be set to null.
   * @param validValues
   * @return
   */
  public ConstEtomoNumber setValidValues(int[] validValues) {
    resetInvalidReason();
    if (validValues == null || validValues.length == 0) {
      this.validValues = null;
    }
    else {
      this.validValues = new Vector(validValues.length);
      for (int i = 0; i < validValues.length; i++) {
        int validValue = validValues[i];
        if (!isNull(validValue)) {
          this.validValues.add(this.newNumber(validValues[i]));
        }
      }
    }
    setInvalidReason();
    return this;
  }

  /**
   * Sets valid floor.  Valid floor does not change a value being set.  It
   * causes validation to fail if the current value is less then the valid
   * floor.
   * @param validFloor
   * @return
   */
  public ConstEtomoNumber setValidFloor(int validFloor) {
    resetInvalidReason();
    this.validFloor = newNumber(validFloor);
    setInvalidReason();
    return this;
  }

  public void store(Properties props) {
    if (isNull(currentValue)) {
      remove(props);
      return;
    }
    props.setProperty(name, toString(currentValue));
  }

  public void store(Properties props, String prepend) {
    if (isNull(currentValue)) {
      remove(props, prepend);
      return;
    }
    if (prepend == null) {
      store(props);
    }
    else {
      props.setProperty(prepend + "." + name, toString(currentValue));
    }
  }

  public static void store(EtomoNumber etomoNumber, String name,
      Properties props, String prepend) {
    if (etomoNumber == null) {
      props.remove(prepend + '.' + name);
      return;
    }
    etomoNumber.store(props, prepend);
  }

  public void remove(Properties props) {
    props.remove(name);
  }

  public void remove(Properties props, String prepend) {
    if (prepend == null) {
      remove(props);
    }
    else {
      props.remove(prepend + "." + name);
    }
  }

  public String toString() {
    return toString(getValue());
  }

  /**
   * If default is set and isNull() is true, defaultValue will be returned, even
   * if displayValue is set.  If defaultValue is not set, or isNull() is false,
   * then it works the same as setValue().
   * @return
   */
  public String toDefaultedString() {
    return toString(getDefaultedValue());
  }

  public int getInt() {
    validateReturnTypeInteger();
    return getValue().intValue();
  }

  public boolean is() {
    if (isNull() || equals(0)) {
      return false;
    }
    return true;
  }

  public boolean isPositive() {
    return gt(getValue(), newNumber(0));
  }

  public boolean isNegative() {
    return lt(getValue(), newNumber(0));
  }

  public long getLong() {
    validateReturnTypeLong();
    return getValue().longValue();
  }

  public float getFloat() {
    validateReturnTypeFloat();
    return getValue().floatValue();
  }

  public double getDouble() {
    validateReturnTypeDouble();
    return getValue().doubleValue();
  }

  /**
   * If default is set and isNull() is true, defaultValue will be returned, even
   * if displayValue is set.  If defaultValue is not set, or isNull() is false,
   * then it works the same as setValue().
   * @return
   */
  public double getDefaultedDouble() {
    validateReturnTypeDouble();
    return getDefaultedValue().doubleValue();
  }

  public ConstEtomoNumber setDefault(int defaultValue) {
    this.defaultValue = newNumber(defaultValue);
    return this;
  }

  public ConstEtomoNumber setDefault(boolean defaultValue) {
    this.defaultValue = newNumber(defaultValue);
    return this;
  }

  /**
   * Returns true if currentValue is not null and is equal to defaultValue.
   * This function is not effected by displayValue.
   * @return
   */
  public boolean isDefault() {
    return isDefault(currentValue);
  }

  public boolean isDefaultSet() {
    return !isNull(defaultValue);
  }

  /**
   * Returns true if defaultValue is not null and value is equal to
   * defaultValue.
   * @return
   */
  protected boolean isDefault(Number value) {
    if (isNull(defaultValue)) {
      return false;
    }
    return equals(value, defaultValue);
  }

  public ConstEtomoNumber useDefaultAsDisplayValue() {
    return setDisplayValue(defaultValue);
  }

  public Number getNumber() {
    return newNumber(getValue());
  }

  /**
   * Returns true if getValue() equals that.getValue().
   * @param that
   * @return
   */
  public boolean equals(ConstEtomoNumber that) {
    if (that == null) {
      return false;
    }
    return equals(getValue(), that.getValue());
  }

  public boolean gt(int value) {
    return gt(getValue(), newNumber(value));
  }

  public boolean lt(int value) {
    return lt(getValue(), newNumber(value));
  }

  public boolean gt(ConstEtomoNumber etomoNumber) {
    if (etomoNumber == null) {
      return false;
    }
    return gt(getValue(), etomoNumber.getValue());
  }

  public boolean equals(int value) {
    return equals(getValue(), newNumber(value));
  }

  public boolean equals(double value) {
    return equals(getValue(), newNumber(value));
  }

  /**
   * Returns true if currentValue is null.  IsNull() does not use getValue() and
   * ignores displayValue, so it shows whether the instance has been explicitely
   * set.
   * @return
   */
  public boolean isNull() {
    return isNull(currentValue);
  }

  public boolean equals(Number value) {
    return equals(getValue(), value);
  }

  public boolean equals(String value) {
    return equals(getValue(), newNumber(value, new StringBuffer()));
  }

  public boolean isNamed(String name) {
    return this.name.equals(name);
  }

  public final boolean isInt() {
    return type == Type.INTEGER;
  }

  private void initialize() {
    ceilingValue = newNumber();
    floorValue = newNumber();
    this.displayValue = newNumber();
    currentValue = newNumber();
    validFloor = newNumber();
    defaultValue = newNumber();
  }

  /**
   * If the currentValue is not null, returns it.  If the currentValue is null,
   * returns the displayValue.  So, if the displayValue is null also, it returns
   * null.
   * @return
   */
  protected Number getValue() {
    if (!isNull(currentValue)) {
      return currentValue;
    }
    return displayValue;
  }

  /**
   * If default is set and isNull() is true, defaultValue will be returned, even
   * if displayValue is set.  If defaultValue is not set, or isNull() is false,
   * then it works the same as setValue().
   * @return
   */
  Number getDefaultedValue() {
    if (isDefaultSet() && isNull()) {
      return defaultValue;
    }
    return getValue();
  }
  
  
  public boolean getDefaultedBoolean() {
    Number value = getDefaultedValue();
    if (isNull(value) || equals(value,newNumber(0))) {
      return false;
    }
    return true;
  }

  String toString(Number value) {
    if (isNull(value)) {
      return "";
    }
    return value.toString();
  }

  private String toString(Vector numberVector) {
    if (numberVector == null || numberVector.size() == 0) {
      return "";
    }
    StringBuffer buffer = new StringBuffer(toString((Number) numberVector
        .get(0)));
    for (int i = 1; i < numberVector.size(); i++) {
      buffer.append("," + toString((Number) numberVector.get(i)));
    }
    return buffer.toString();
  }

  protected void resetInvalidReason() {
    invalidReason = null;
  }

  protected void addInvalidReason(String message) {
    if (message == null) {
      return;
    }
    if (invalidReason == null) {
      invalidReason = new StringBuffer(message);
    }
    else {
      invalidReason.append("\n" + message);
    }
  }

  protected Number newNumber() {
    if (type == Type.DOUBLE) {
      return new Double(DOUBLE_NULL_VALUE);
    }
    if (type == Type.FLOAT) {
      return new Float(FLOAT_NULL_VALUE);
    }
    if (type == Type.INTEGER) {
      return new Integer(INTEGER_NULL_VALUE);
    }
    if (type == Type.LONG) {
      return new Long(LONG_NULL_VALUE);
    }
    throw new IllegalStateException("type=" + type);
  }

  /**
   * Creates a new number based on the type member variable.
   * @param value
   * @return
   */
  protected Number newNumber(Number value) {
    if (value == null) {
      return newNumber();
    }
    validateInputType(value);
    if (isNull(value)) {
      return newNumber();
    }
    if (type == Type.DOUBLE) {
      return new Double(value.doubleValue());
    }
    if (type == Type.FLOAT) {
      return new Float(value.floatValue());
    }
    if (type == Type.INTEGER) {
      return new Integer(value.intValue());
    }
    if (type == Type.LONG) {
      return new Long(value.longValue());
    }
    throw new IllegalStateException("type=" + type);
  }

  /**
   * Override this class to display numbers as descriptive character strings.
   * @param value
   * @param invalidBuffer
   * @return
   */
  protected Number newNumber(String value, StringBuffer invalidBuffer) {
    if (value == null || value.matches("\\s*")) {
      return newNumber();
    }
    try {
      if (type == Type.DOUBLE) {
        return new Double(value);
      }
      if (type == Type.FLOAT) {
        return new Float(value);
      }
      if (type == Type.INTEGER) {
        return new Integer(value);
      }
      if (type == Type.LONG) {
        return new Long(value);
      }
      throw new IllegalStateException("type=" + type);
    }
    catch (NumberFormatException e) {
      invalidBuffer.append(value + " is not a valid number.");
      return newNumber();
    }
  }

  protected Number newNumber(int value) {
    validateInputType(value);
    if (type == Type.DOUBLE) {
      return new Double(new Integer(value).doubleValue());
    }
    if (type == Type.FLOAT) {
      return new Float(new Integer(value).floatValue());
    }
    if (type == Type.INTEGER) {
      return new Integer(value);
    }
    if (type == Type.LONG) {
      return new Long(new Integer(value).longValue());
    }
    throw new IllegalStateException("type=" + type);
  }

  protected Number newNumber(boolean value) {
    if (value) {
      return newNumber(1);
    }
    return newNumber(0);
  }

  protected Number newNumber(double value) {
    validateInputType(value);
    if (Double.isNaN(value)) {
      return newNumber();
    }
    if (type == Type.DOUBLE) {
      return new Double(value);
    }
    if (type == Type.FLOAT) {
      return new Float(new Double(value).floatValue());
    }
    if (type == Type.INTEGER) {
      return new Integer(new Double(value).intValue());
    }
    if (type == Type.LONG) {
      return new Long(new Double(value).longValue());
    }
    throw new IllegalStateException("type=" + type);
  }

  protected Number newNumber(float value) {
    validateInputType(value);
    if (Double.isNaN(value)) {
      return newNumber();
    }
    if (type == Type.DOUBLE) {
      return new Double(new Float(value).doubleValue());
    }
    if (type == Type.FLOAT) {
      return new Float(value);
    }
    if (type == Type.INTEGER) {
      return new Integer(new Float(value).intValue());
    }
    if (type == Type.LONG) {
      return new Long(new Float(value).longValue());
    }
    throw new IllegalStateException("type=" + type);
  }

  protected Number newNumber(long value) {
    validateInputType(value);
    if (value == LONG_NULL_VALUE) {
      return newNumber();
    }
    if (type == Type.DOUBLE) {
      return new Double(new Long(value).doubleValue());
    }
    if (type == Type.FLOAT) {
      return new Float(new Long(value).floatValue());
    }
    if (type == Type.INTEGER) {
      return new Integer(new Long(value).intValue());
    }
    if (type == Type.LONG) {
      return new Long(value);
    }
    throw new IllegalStateException("type=" + type);
  }

  protected boolean isNull(Number value) {
    if (value instanceof Double) {
      return Double.isNaN(value.doubleValue());
    }
    if (value instanceof Float) {
      return Float.isNaN(value.floatValue());
    }
    if (value instanceof Integer) {
      return isNull(value.intValue());
    }
    if (value instanceof Long) {
      return value.longValue() == LONG_NULL_VALUE;
    }
    if (value instanceof Short) {
      return value.shortValue() == shortNullValue;
    }
    if (value instanceof Byte) {
      return value.byteValue() == byteNullValue;
    }
    throw new IllegalStateException("Unknown type.  value.getClass()="
        + value.getClass());
  }

  protected boolean isNull(int value) {
    return value == INTEGER_NULL_VALUE;
  }

  protected boolean gt(Number number, Number compValue) {
    if (isNull(number) || isNull(compValue)) {
      return false;
    }
    validateInputType(number);
    validateInputType(compValue);
    if (type == Type.DOUBLE) {
      return number.doubleValue() > compValue.doubleValue();
    }
    if (type == Type.FLOAT) {
      return number.floatValue() > compValue.floatValue();
    }
    if (type == Type.INTEGER) {
      return number.intValue() > compValue.intValue();
    }
    if (type == Type.LONG) {
      return number.longValue() > compValue.longValue();
    }
    throw new IllegalStateException("type=" + type);
  }

  protected boolean ge(Number number, Number compValue) {
    if (isNull(number) || isNull(compValue)) {
      return false;
    }
    validateInputType(number);
    validateInputType(compValue);
    if (type == Type.DOUBLE) {
      return number.doubleValue() >= compValue.doubleValue();
    }
    if (type == Type.FLOAT) {
      return number.floatValue() >= compValue.floatValue();
    }
    if (type == Type.INTEGER) {
      return number.intValue() >= compValue.intValue();
    }
    if (type == Type.LONG) {
      return number.longValue() >= compValue.longValue();
    }
    throw new IllegalStateException("type=" + type);
  }

  protected boolean lt(Number number, Number compValue) {
    if (isNull(number) || isNull(compValue)) {
      return false;
    }
    validateInputType(number);
    validateInputType(compValue);
    if (type == Type.DOUBLE) {
      return number.doubleValue() < compValue.doubleValue();
    }
    if (type == Type.FLOAT) {
      return number.floatValue() < compValue.floatValue();
    }
    if (type == Type.INTEGER) {
      return number.intValue() < compValue.intValue();
    }
    if (type == Type.LONG) {
      return number.longValue() < compValue.longValue();
    }
    throw new IllegalStateException("type=" + type);
  }

  protected boolean equals(Number number, Number compValue) {
    if (isNull(number) && isNull(compValue)) {
      return true;
    }
    if (isNull(number) || isNull(compValue)) {
      return false;
    }
    validateInputType(number);
    validateInputType(compValue);
    if (type == Type.DOUBLE) {
      return number.doubleValue() == compValue.doubleValue();
    }
    if (type == Type.FLOAT) {
      return number.floatValue() == compValue.floatValue();
    }
    if (type == Type.INTEGER) {
      return number.intValue() == compValue.intValue();
    }
    if (type == Type.LONG) {
      return number.longValue() == compValue.longValue();
    }
    throw new IllegalStateException("type=" + type);
  }

  /**
   * Validation to avoid data corruption
   *
   */
  private void validateReturnTypeInteger() {
    if (type != Type.INTEGER) {
      throw new IllegalStateException(
          "Cannot place a float, long or double into an integer.");
    }
  }

  /**
   * Validation to avoid data corruption
   *
   */
  private void validateReturnTypeLong() {
    if (type != Type.INTEGER && type != Type.LONG) {
      throw new IllegalStateException(
          "Cannot place a float or double into a long.");
    }
  }

  private void validateReturnTypeFloat() {
    if (type != Type.INTEGER && type != Type.FLOAT) {
      throw new IllegalStateException(
          "Cannot place a long or double into a float.");
    }
  }

  /**
   * Validation to avoid data corruption
   *
   */
  private void validateReturnTypeDouble() {
  }

  /**
   * Validation to avoid data corruption
   * @param input
   */
  private void validateInputType(Number input) {
    if (input instanceof Double && type != Type.DOUBLE) {
      throw new IllegalStateException(
          "Cannot place a Double into anything but a Double.  Type=" + type);
    }
    if (input instanceof Float && type != Type.DOUBLE && type != Type.FLOAT) {
      throw new IllegalStateException(
          "Cannot place a Float into anything but a Double or a Float.  Type="
              + type);
    }
    if (input instanceof Long && type != Type.DOUBLE && type != Type.LONG) {
      throw new IllegalStateException(
          "Cannot place a Long into anything but a Double or a Long.  Type="
              + type);

    }
  }

  /**
   * Validation to avoid data corruption.  Currently nothing to do
   * @param input
   */
  private void validateInputType(int input) {
  }

  /**
   * Validation to avoid data corruption
   * @param input
   */
  private void validateInputType(float input) {
    if (type != Type.FLOAT && type != Type.DOUBLE) {
      throw new IllegalStateException(
          "Cannot place a float into anything but a Double or Float.  Type="
              + type);
    }
  }

  /**
   * Validation to avoid data corruption
   * @param input
   */
  private void validateInputType(double input) {
    if (type != Type.DOUBLE) {
      throw new IllegalStateException(
          "Cannot place a double into anything but a Double.  Type=" + type);
    }
  }

  /**
   * Validation to avoid data corruption
   * @param input
   */
  private void validateInputType(long input) {
    if (type != Type.DOUBLE && type != Type.LONG) {
      throw new IllegalStateException(
          "Cannot place a long into anything but a Double or Long.  Type="
              + type);
    }
  }

  Type getType() {
    return type;
  }

  /**
   * Validation for floor and ceiling
   *
   */
  private void validateFloorAndCeiling() {
    //if floorValue and ceilingValue are both used, then floorValue must be less
    //then or equal to ceilingValue.
    if (!isNull(ceilingValue) && !isNull(floorValue)
        && gt(floorValue, ceilingValue)) {
      throw new IllegalStateException(
          "FloorValue cannot be greater then ceilingValue.\nfloorValue="
              + floorValue + ", ceilingValue=" + ceilingValue);
    }
  }

  /**
   * Check that a deep copy was done and that all data was copied.
   * Use self test on this because, if one copy works, they all should work.
   * @param original
   */
  void internalTestDeepCopy(ConstEtomoNumber original) {
    if (!Utilities.isSelfTest()) {
      return;
    }
    if (original == null) {
      return;
    }
    //deep copy test
    //ok for name to be the same instance because it is final
    if (this == original) {
      throw new IllegalStateException("Incorrect copy: was not a deep copy");
    }
    //equality test
    if (type != original.type
        || !name.equals(original.name)
        || !description.equals(original.description)
        || !equals(currentValue, original.currentValue)
        || !equals(displayValue, original.displayValue)
        || !equals(ceilingValue, original.ceilingValue)
        || !equals(floorValue, original.floorValue)
        || !equals(validFloor, original.validFloor)
        || !equals(defaultValue, original.defaultValue)
        || (validValues != null && original.validValues == null)
        || (validValues == null && original.validValues != null)
        || (validValues != null && original.validValues != null && !validValues
            .equals(original.validValues))
        || nullIsValid != original.nullIsValid
        || (invalidReason != null && original.invalidReason == null)
        || (invalidReason == null && original.invalidReason != null)
        || (invalidReason != null && original.invalidReason != null && !invalidReason
            .toString().equals(original.invalidReason.toString()))) {
      throw new IllegalStateException("Incorrect copy: this="
          + this.classInfoString() + ",original=" + original.classInfoString());
    }
  }

  public static final class Type {
    public static final Type DOUBLE = new Type();
    public static final Type FLOAT = new Type();
    public static final Type INTEGER = new Type();
    public static final Type LONG = new Type();

    public static Type getDefault() {
      return INTEGER;
    }
  }
}