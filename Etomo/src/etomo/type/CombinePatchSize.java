package etomo.type;

import etomo.comscript.SetupCombine;

/**
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright 2002 - 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 *
 * <p> $Log$
 * <p> Revision 3.1  2006/05/12 00:08:45  sueh
 * <p> bug# 857 Placed the setupcombine options in getOption().
 * <p>
 * <p> Revision 3.0  2003/11/07 23:19:01  rickg
 * <p> Version 1.0.0
 * <p>
 * <p> Revision 2.0  2003/01/24 20:30:31  rickg
 * <p> Single window merge to main branch
 * <p>
 * <p> Revision 1.1.2.1  2003/01/24 18:37:54  rickg
 * <p> Single window GUI layout initial revision
 * <p>
 * <p> Revision 1.1  2002/09/09 22:57:02  rickg
 * <p> Initial CVS entry, basic functionality not including combining
 * <p> </p>
 */

public final class CombinePatchSize implements EnumeratedType {
  public static final CombinePatchSize SMALL = new CombinePatchSize("Small", "S");
  public static final CombinePatchSize MEDIUM = new CombinePatchSize("Medium", "M");
  public static final CombinePatchSize LARGE = new CombinePatchSize("Large", "L");
  public static final CombinePatchSize EXTRA_LARGE = new CombinePatchSize("Extra large",
    "E");
  public static final CombinePatchSize XYZ = new CombinePatchSize("Custom", null);

  // must be in the same order at the output of "setupcombine -i"
  private static final CombinePatchSize PATCH_SIZE_ARRAY[] = new CombinePatchSize[] {
    SMALL, MEDIUM, LARGE, EXTRA_LARGE };
  public static final int X_INDEX = 0;
  public static final int Y_INDEX = 1;
  public static final int Z_INDEX = 2;
  private static final int OUTPUT_X_INDEX = 1;
  private static final int OUTPUT_Y_INDEX = 2;
  private static final int OUTPUT_Z_INDEX = 3;

  private final String label, value;
  private String x = null;
  private String y = null;
  private String z = null;

  private CombinePatchSize(final String label, final String value) {
    this.label = label;
    this.value = value;
  }

  /**
   * Takes a string representation of an CombinePatchSize type and returns the
   * correct static object.  The string is case insensitive.  Null is returned if
   * the string is not one of the possibilities.
   */
  public static CombinePatchSize getInstance(final String input) {
    if (input == null) {
      return null;
    }
    if (input.equalsIgnoreCase(SMALL.value) || input.equalsIgnoreCase(SMALL.label)) {
      return SMALL;
    }
    if (input.equalsIgnoreCase(MEDIUM.value) || input.equalsIgnoreCase(MEDIUM.label)) {
      return MEDIUM;
    }
    if (input.equalsIgnoreCase(LARGE.value) || input.equalsIgnoreCase(LARGE.label)) {
      return LARGE;
    }
    if (input.equalsIgnoreCase(EXTRA_LARGE.value)
      || input.equalsIgnoreCase(EXTRA_LARGE.label)) {
      return EXTRA_LARGE;
    }
    // For XYZ look for something with on or more commas, or an integer.
    if (input.indexOf(",") != -1) {
      return XYZ;
    }
    try {
      Double.parseDouble(input);
      return XYZ;
    }
    catch (NumberFormatException e) {
      // Not numeric
      return null;
    }
  }

  public String getX() {
    loadXYZ();
    return x;
  }

  public String getY() {
    loadXYZ();
    return y;
  }

  public String getZ() {
    loadXYZ();
    return z;
  }

  private void loadXYZ() {
    if (x != null) {
      return;
    }
    // Do this once only.
    synchronized (PATCH_SIZE_ARRAY) {
      if (x != null) {
        return;
      }
      String[] output = SetupCombine.getInfoOnPatchSizes();
      // Set x, y, and z so something other then null.
      for (int i = 0; i < PATCH_SIZE_ARRAY.length; i++) {
        String[] xyz = new String[] { "", "", "" };
        if (output != null && output.length > i && output[i] != null) {
          String[] array = output[i].split("\\s+");
          if (array != null) {
            if (array.length > OUTPUT_X_INDEX && array[OUTPUT_X_INDEX] != null) {
              xyz[X_INDEX] = array[OUTPUT_X_INDEX];
            }
            if (array.length > OUTPUT_Y_INDEX && array[OUTPUT_Y_INDEX] != null) {
              xyz[Y_INDEX] = array[OUTPUT_Y_INDEX];
            }
            if (array.length > OUTPUT_Z_INDEX && array[OUTPUT_Z_INDEX] != null) {
              xyz[Z_INDEX] = array[OUTPUT_Z_INDEX];
            }
          }
        }
        PATCH_SIZE_ARRAY[i].x = xyz[X_INDEX];
        PATCH_SIZE_ARRAY[i].y = xyz[Y_INDEX];
        PATCH_SIZE_ARRAY[i].z = xyz[Z_INDEX];
      }
    }
  }

  public String getOption() {
    if (this == XYZ) {
      return null;
    }
    return value;
  }

  public boolean isDefault() {
    return this == MEDIUM;
  }

  public ConstEtomoNumber getValue() {
    return null;
  }

  public ConstEtomoNumber getValue(final int index) {
    EtomoNumber size = new EtomoNumber();
    if (index == X_INDEX) {
      size.set(getX());
    }
    else if (index == Y_INDEX) {
      size.set(getY());
    }
    else if (index == Z_INDEX) {
      size.set(getZ());
    }
    else {
      return null;
    }
    return size;
  }

  public String toString() {
    return getOption();
  }

  public String getLabel() {
    return label;
  }
}
