package etomo.storage.autodoc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import etomo.storage.AutodocFilter;
import etomo.storage.LogFile;
import etomo.type.AxisID;

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
public final class AutodocFactory {
  public static final String rcsid = "$Id$";

  public static final String VERSION = "1.2";
  public static final String TILTXCORR = "tiltxcorr";
  public static final String MTF_FILTER = "mtffilter";
  public static final String COMBINE_FFT = "combinefft";
  public static final String TILTALIGN = "tiltalign";
  public static final String CCDERASER = "ccderaser";
  public static final String SOLVEMATCH = "solvematch";
  public static final String BEADTRACK = "beadtrack";
  public static final String DENS_MATCH = "densmatch";
  public static final String CORR_SEARCH_3D = "corrsearch3d";
  public static final String XFJOINTOMO = "xfjointomo";
  public static final String CPU = "cpu";
  public static final String UITEST = "uitest";

  private static final String TEST = "test";
  private static final String UITEST_AXIS = "uitest_axis";

  private static Autodoc TILTXCORR_INSTANCE = null;
  private static Autodoc TEST_INSTANCE = null;
  private static Autodoc UITEST_INSTANCE = null;
  private static Autodoc MTF_FILTER_INSTANCE = null;
  private static Autodoc COMBINE_FFT_INSTANCE = null;
  private static Autodoc TILTALIGN_INSTANCE = null;
  private static Autodoc CCDERASER_INSTANCE = null;
  private static Autodoc SOLVEMATCH_INSTANCE = null;
  private static Autodoc BEADTRACK_INSTANCE = null;
  private static Autodoc CPU_INSTANCE = null;
  private static Autodoc DENS_MATCH_INSTANCE = null;
  private static Autodoc CORR_SEARCH_3D_INSTANCE = null;
  private static Autodoc XFJOINTOMO_INSTANCE = null;
  
  private static final HashMap UITEST_AXIS_MAP =   new HashMap();
  
  private AutodocFactory() {}

  public static ReadOnlyAutodoc getInstance(String name, AxisID axisID)
      throws FileNotFoundException, IOException, LogFile.ReadException {
    if (name == null) {
      throw new IllegalStateException("name is null");
    }
    Autodoc autodoc = getExistingAutodoc(name);
    if (autodoc != null) {
      return autodoc;
    }
    autodoc = new Autodoc();
    if (name.equals(UITEST)) {
      autodoc.initializeUITest(name, axisID);
    }
    if (name.equals(CPU)) {
      autodoc.initializeCpu(name, axisID);
    }
    else {
      autodoc.initialize(name, axisID);
    }
    return autodoc;
  }

  public static ReadOnlyAutodoc getMatlabInstance(File file)
      throws IOException, LogFile.ReadException {
    if (file == null) {
      throw new IllegalStateException("file is null");
    }
    Autodoc autodoc = new Autodoc(true);
    try {
      autodoc.initialize(file, true);
      return autodoc;
    }
    catch (FileNotFoundException e) {
      return null;
    }
  }

  public static ReadOnlyAutodoc getInstance(File file) throws IOException,
      LogFile.ReadException {
    if (file == null) {
      throw new IllegalStateException("file is null");
    }
    Autodoc autodoc = new Autodoc();
    try {
      autodoc.initialize(file, true);
      return autodoc;
    }
    catch (FileNotFoundException e) {
      return null;
    }
  }

  /**
   * For testing initializes but doesn't parse and store data.  Call
   * runInternalTest on the resulting instance.
   * @param file
   * @param storeData
   * @return
   * @throws IOException
   * @throws LogFile.ReadException
   */
  public static ReadOnlyAutodoc getTestInstance(File file) throws IOException,
      LogFile.ReadException {
    if (file == null) {
      throw new IllegalStateException("file is null");
    }
    Autodoc autodoc = new Autodoc();
    try {
      autodoc.initialize(file, false);
      return autodoc;
    }
    catch (FileNotFoundException e) {
      return null;
    }
  }

  public static ReadOnlyAutodoc getEmptyMatlabInstance(File file)
      throws IOException, LogFile.ReadException {
    if (file == null) {
      throw new IllegalStateException("file is null");
    }
    return new Autodoc(true);
  }

  public static ReadOnlyAutodoc getInstance(String fileName, String name,
      AxisID axisID) throws FileNotFoundException, IOException,
      LogFile.ReadException {
    if (name == null) {
      throw new IllegalStateException("name is null");
    }
    Autodoc autodoc = getExistingAutodoc(fileName, name);
    if (autodoc != null) {
      return autodoc;
    }
    autodoc = new Autodoc();
    if (name.equals(UITEST_AXIS)) {
      autodoc.initializeUITest(fileName, axisID);
      return autodoc;
    }
    throw new IllegalArgumentException("Illegal autodoc name: " + name + ".");
  }

  /**
   * open and preserve an autodoc without a type for testing
   * @param autodocFile
   * @param axisID
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static Autodoc getInstance(File directory, String autodocFileName,
      AxisID axisID) throws FileNotFoundException, IOException,
      LogFile.ReadException {
    if (autodocFileName == null) {
      return null;
    }
    File autodocFile = new File(directory, autodocFileName);
    AutodocFilter filter = new AutodocFilter();
    if (!filter.accept(autodocFile)) {
      throw new IllegalArgumentException(autodocFile + " is not an autodoc.");
    }
    Autodoc autodoc = getExistingUITestAxisAutodoc(autodocFile);
    if (autodoc != null) {
      return autodoc;
    }
    autodoc = new Autodoc();
    UITEST_AXIS_MAP.put(autodocFile, autodoc);
    autodoc.initializeUITestAxis(LogFile.getInstance(autodocFile), axisID);
    return autodoc;
  }
  
  public static void setAbsoluteDir(String absoluteDir) {
    Autodoc.setAbsoluteDir(absoluteDir);
  }

  private static Autodoc getExistingUITestAxisAutodoc(File autodocFile) {
    if (UITEST_AXIS_MAP == null) {
      return null;
    }
    Autodoc autodoc = (Autodoc) UITEST_AXIS_MAP.get(autodocFile);
    return autodoc;
  }

  private static Autodoc getExistingAutodoc(String fileName, String name) {
    if (name.equals(UITEST_AXIS)) {
      if (UITEST_AXIS_MAP == null) {
        return null;
      }
      return (Autodoc) UITEST_AXIS_MAP.get(fileName);
    }
    throw new IllegalArgumentException("Illegal autodoc name: " + name + ".");
  }

  private static Autodoc getExistingAutodoc(String name) {
    if (name.equals(TILTXCORR)) {
      return TILTXCORR_INSTANCE;
    }
    if (name.equals(TEST)) {
      return TEST_INSTANCE;
    }
    if (name.equals(UITEST)) {
      return UITEST_INSTANCE;
    }
    if (name.equals(MTF_FILTER)) {
      return MTF_FILTER_INSTANCE;
    }
    if (name.equals(COMBINE_FFT)) {
      return COMBINE_FFT_INSTANCE;
    }
    if (name.equals(TILTALIGN)) {
      return TILTALIGN_INSTANCE;
    }
    if (name.equals(CCDERASER)) {
      return CCDERASER_INSTANCE;
    }
    if (name.equals(SOLVEMATCH)) {
      return SOLVEMATCH_INSTANCE;
    }
    if (name.equals(BEADTRACK)) {
      return BEADTRACK_INSTANCE;
    }
    if (name.equals(CPU)) {
      return CPU_INSTANCE;
    }
    if (name.equals(DENS_MATCH)) {
      return DENS_MATCH_INSTANCE;
    }
    if (name.equals(CORR_SEARCH_3D)) {
      return CORR_SEARCH_3D_INSTANCE;
    }
    if (name.equals(XFJOINTOMO)) {
      return XFJOINTOMO_INSTANCE;
    }
    throw new IllegalArgumentException("Illegal autodoc name: " + name + ".");
  }

  /**
   * for testing
   * @param name
   */
  public static void resetInstance(String name) {
    if (name.equals(TILTXCORR)) {
      TILTXCORR_INSTANCE = null;
    }
    else if (name.equals(TEST)) {
      TEST_INSTANCE = null;
    }
    else if (name.equals(UITEST)) {
      UITEST_INSTANCE = null;
    }
    else if (name.equals(MTF_FILTER)) {
      MTF_FILTER_INSTANCE = null;
    }
    else if (name.equals(COMBINE_FFT)) {
      COMBINE_FFT_INSTANCE = null;
    }
    else if (name.equals(TILTALIGN)) {
      TILTALIGN_INSTANCE = null;
    }
    else if (name.equals(CCDERASER)) {
      CCDERASER_INSTANCE = null;
    }
    else if (name.equals(SOLVEMATCH)) {
      SOLVEMATCH_INSTANCE = null;
    }
    else if (name.equals(BEADTRACK)) {
      BEADTRACK_INSTANCE = null;
    }
    else if (name.equals(CPU)) {
      CPU_INSTANCE = null;
    }
    else if (name.equals(DENS_MATCH)) {
      DENS_MATCH_INSTANCE = null;
    }
    else if (name.equals(CORR_SEARCH_3D)) {
      CORR_SEARCH_3D_INSTANCE = null;
    }
    else if (name.equals(XFJOINTOMO)) {
      XFJOINTOMO_INSTANCE = null;
    }
    else {
      throw new IllegalArgumentException("Illegal autodoc name: " + name + ".");
    }
  }
}
