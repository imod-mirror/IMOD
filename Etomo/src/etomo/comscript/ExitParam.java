package etomo.comscript;

import java.util.ArrayList;

/**
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Organization: Boulder Laboratory for 3D Fine Structure,
 * University of Colorado</p>
 *
 * @author $$Author$$
 *
 * @version $$Revision$$
 *
 * <p> $$Log$$ </p>
 */

public class ExitParam
  extends ConstExitParam
  implements CommandParam {
  public static final String rcsid =
    "$$Id$$";
    /* (non-Javadoc)
     * @see 
     * etomo.comscript.CommandParam#initialize(etomo.comscript.ComScriptCommand)
     */
    public void parseComScriptCommand(ComScriptCommand scriptCommand)
      throws
        BadComScriptException,
        FortranInputSyntaxException,
        InvalidParameterException {
      String[] cmdLineArgs = scriptCommand.getCommandLineArgs();
      reset();

      resultValue = Integer.parseInt(cmdLineArgs[0]);
    }

    /* (non-Javadoc)
     * @see etomo.comscript.CommandParam#updateComScript 
     * (etomo.comscript.ComScriptCommand)
     */
    public void updateComScriptCommand(ComScriptCommand scriptCommand)
      throws BadComScriptException {

      // Create a new command line argument array
      ArrayList cmdLineArgs = new ArrayList(20);

      cmdLineArgs.add(String.valueOf(resultValue));

      int nArgs = cmdLineArgs.size();
      scriptCommand.setCommandLineArgs(
        (String[]) cmdLineArgs.toArray(new String[nArgs]));
    }
  
    public void initializeDefaults() {
      reset();
    }

    /**
       * Sets the string.
       * @param string - The string to set
       */
    public void setResultValue(int resultValue) {
      this.resultValue = resultValue;
    }
  }
