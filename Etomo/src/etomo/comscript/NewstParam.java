package etomo.comscript;

import java.util.ArrayList;

/*
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Organization: Boulder Laboratory for 3D Fine Structure,
 * University of Colorado</p>
 *
 * @author $Author$
 *
 * @version $Revision$
 *
 * <p> $Log$
 * <p> Revision 1.1  2002/09/09 22:57:02  rickg
 * <p> Initial CVS entry, basic functionality not including combining
 * <p> </p>
 */
public class NewstParam extends ConstNewstParam {
  public static final String rcsid =
    "$Id$";

  /**
   * Get the parameters from the ComScriptCommand
   * @param scriptCommand the ComScriptCommand containg the newst command
   * and parameters.
   */
  public void initialize(ComScriptCommand scriptCommand) {
    // FIXME this needs to throw some exceptions
    String[] cmdLineArgs = scriptCommand.getCommandLineArgs();
    reset();

    for (int i = 0; i < cmdLineArgs.length - 2; i++) {
      if (cmdLineArgs[i].startsWith("-si")) {
        i++;
        size = cmdLineArgs[i];
        useSize = true;
      }
      if (cmdLineArgs[i].startsWith("-o")) {
        i++;
        offset = cmdLineArgs[i];
        useOffset = true;
      }
      if (cmdLineArgs[i].startsWith("-x")) {
        i++;
        transformFile = cmdLineArgs[i];
        useTransformFile = true;
      }
    }
    inputFile = cmdLineArgs[cmdLineArgs.length - 2];
    outputFile = cmdLineArgs[cmdLineArgs.length - 1];
  }

  /**
   * Update the script command with the current valus of this NewstParam
   * object
   * @param scriptCommand the script command to be updated
   */
  public void updateComScript(ComScriptCommand scriptCommand)
    throws BadComScriptException {
    // Create a new command line argument array

    ArrayList cmdLineArgs = new ArrayList(20);

    if (useSize) {
      cmdLineArgs.add("-size");
      cmdLineArgs.add(size.toString());
    }

    if (useOffset) {
      cmdLineArgs.add("-offset");
      cmdLineArgs.add(offset.toString());
    }

    if (useTransformFile) {
      cmdLineArgs.add("-xform");
      cmdLineArgs.add(transformFile);
    }

    cmdLineArgs.add(inputFile);
    cmdLineArgs.add(outputFile);

    int nArgs = cmdLineArgs.size();
    scriptCommand.setCommandLineArgs(
      (String[]) cmdLineArgs.toArray(new String[nArgs]));
  }

  public void setInputFile(String filename) {
    inputFile = filename;
  }

  public void setOutputFile(String filename) {
    outputFile = filename;
  }

  public void setTransformFile(String filename) {
    transformFile = filename;
    useTransformFile = true;
  }

  public void setSize(String newSize) throws FortranInputSyntaxException {
    size = newSize;
    useSize = true;
  }

  public void setOffset(String newOffset) throws FortranInputSyntaxException {
    offset = newOffset;
    useOffset = true;
  }

  private void reset() {
    inputFile = "";
    outputFile = "";

    useTransformFile = false;
    transformFile = "";

    useSize = false;
    size = "";

    useOffset = false;
    offset = "";
  }
}
