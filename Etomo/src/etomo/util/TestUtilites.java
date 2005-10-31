/**
 * <p>Description: This class prodives utility functions for working unit,
 * functional and integration testing.</p>
 * 
 * <p>Copyright: Copyright (c) 2002, 2003</p>
 *
 *<p>Organization:
 * Boulder Laboratory for 3-Dimensional Electron Microscopy of Cells (BL3DEMC),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 * 
 * <p> $Log$
 * <p> Revision 1.9  2005/04/25 21:44:13  sueh
 * <p> bug# 615 Passing the axis where a command originates to the message
 * <p> functions so that the message will be popped up in the correct window.
 * <p> This requires adding AxisID to many objects.
 * <p>
 * <p> Revision 1.8  2005/02/10 18:57:35  sueh
 * <p> bug# 599 String.matches() isn't handling the Windows file separator "\",
 * <p> since its an escape charactor.  Using String.indexOf() instead.
 * <p>
 * <p> Revision 1.7  2004/12/08 21:33:31  sueh
 * <p> bug# 520 Setting the working directory in TestUtilities.checkoutVector().
 * <p> Also setting the fail message for SystemProcessException in
 * <p> TestUtilities.checkoutVector().  Corrected deletion of vector when vector
 * <p> already exists.
 * <p>
 * <p> Revision 1.6  2004/12/07 23:36:34  sueh
 * <p> bug# 520 Changing print statements.
 * <p>
 * <p> Revision 1.5  2004/12/06 23:37:05  sueh
 * <p> bug# 520 Adding print statements.
 * <p>
 * <p> Revision 1.4  2004/11/24 01:30:24  sueh
 * <p> bug# 520 Getting working directory from EtomoDirector instead of
 * <p> property.  makeDirectories:  making sure to get a valid path for
 * <p> badDirectory.
 * <p>
 * <p> Revision 1.3  2004/04/06 22:56:56  rickg
 * <p> Workaround for buggy cvs exports
 * <p>
 * <p> Revision 1.2  2004/04/06 02:48:13  rickg
 * <p> Added makeDirectories method
 * <p>
 * <p> Revision 1.1  2004/04/02 18:44:26  rickg
 * <p> Initial revision
 * <p> </p>
 */

package etomo.util;

import java.io.File;
import java.io.IOException;

import etomo.BaseManager;
import etomo.EtomoDirectorTestHarness;
import etomo.process.SystemProcessException;
import etomo.process.SystemProgram;
import etomo.type.AxisID;

public class TestUtilites {
  public static final String rcsid = "$Id$";

  /**
   * Make all of the directories on on the specified path if necessary.  If the
   * path begins with the systems separator then it is an absolute path, if not
   * it is relative to the current directory specified by propertyUserDir from
   * EtomoDirector. 
   * @param newDirectory
   */
  public static void makeDirectories(String propertyUserDir, String newDirectory) throws IOException {
    //  Create the test directories
    File directory;
    if (newDirectory.startsWith(File.separator)) {
      directory = new File(newDirectory);
    }
    else {
      directory = new File(propertyUserDir, newDirectory);
    }
    if (!directory.exists()) {
      if (!directory.mkdirs()) {
        throw new IOException("Creating directory: "
            + directory.getAbsolutePath());
      }
    }
  }

  /**
   * Check out the specified test vector into the specified directory. Note the
   * cvs export cannot handle a full path as an argument to -d.  The directory
   * must reside in the current directory.
   * Setting the working directory just before running cvs
   * @param workingDirName - Name of the directory containing the dirName directory.
   * @param dirName - Directory name with no path.
   * @param vector - File to be added to the dirName directory.
   */
  public static void checkoutVector(BaseManager manager, String workingDirName,
      String dirName, String vector) throws SystemProcessException,
      InvalidParameterException {
    //set working directory
    File workingDir = new File(workingDirName);
    String originalDirName = EtomoDirectorTestHarness
        .setCurrentPropertyUserDir(workingDir.getAbsolutePath());
    //check vector
    if (vector.indexOf(File.separator) != -1) {
      throw new InvalidParameterException(
          "vector can not contain path separators");
    }
    //delete existing vector
    File checkoutDir = new File(workingDir, dirName);
    File fileVector = new File(checkoutDir, vector);
    if (fileVector.exists() && !fileVector.delete()) {
      EtomoDirectorTestHarness.setCurrentPropertyUserDir(originalDirName);
      throw new SystemProcessException("Cannot delete vector: " + vector);
    }
    String[] cvsCommand = new String[7];
    cvsCommand[0] = "cvs";
    cvsCommand[1] = "export";
    cvsCommand[2] = "-D";
    cvsCommand[3] = "today";
    cvsCommand[4] = "-d";
    cvsCommand[5] = dirName;
    cvsCommand[6] = "ImodTests/EtomoTests/vectors/" + vector;
    SystemProgram cvs = new SystemProgram(manager.getPropertyUserDir(), cvsCommand,
        AxisID.ONLY);
    cvs.setDebug(true);
    cvs.run();
    for (int i = 0; i < cvsCommand.length; i++) {
      System.err.print(cvsCommand[i] + " ");
    }
    System.err.println();
    if (cvs.getExitValue() > 0) {
      String message = cvs.getStdErrorString()
          + "\nCVSROOT="
          + Utilities.getEnvironmentVariable(manager.getPropertyUserDir(), "CVSROOT",
              AxisID.ONLY) + "\nworkingDirName=" + manager.getPropertyUserDir()
          + "\ndirName=" + dirName + "\nvector=" + vector;
      EtomoDirectorTestHarness.setCurrentPropertyUserDir(originalDirName);
      throw new SystemProcessException(message);
    }
    // NOTE: some version of cvs (1.11.2) have bug that results in a checkout
    // (CVS directory is created) instead of an export when using the -d flag
    // This is a work around to handle that case
    File badDirectory = new File(checkoutDir, "CVS");
    if (badDirectory.exists()) {
      String[] rmCommand = new String[3];
      rmCommand[0] = "rm";
      rmCommand[1] = "-rf";
      rmCommand[2] = badDirectory.getAbsolutePath();
      SystemProgram rm = new SystemProgram(manager.getPropertyUserDir(),
          rmCommand, AxisID.ONLY);
      rm.run();
    }
    EtomoDirectorTestHarness.setCurrentPropertyUserDir(originalDirName);
  }

}