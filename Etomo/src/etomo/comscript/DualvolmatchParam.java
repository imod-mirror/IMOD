package etomo.comscript;

import etomo.type.EtomoNumber;
import etomo.type.ScriptParameter;

/**
 * <p>Description: Represents the dualvolmatch parameters.</p>
 * <p/>
 * <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class DualvolmatchParam implements CommandParam {
  ScriptParameter maximumResidual = new ScriptParameter(EtomoNumber.Type.DOUBLE,
    "MaximumResidual");
  
  public void initializeDefaults() {
    maximumResidual.reset();
  }

  public void parseComScriptCommand(final ComScriptCommand scriptCommand)
    throws FortranInputSyntaxException, InvalidParameterException {
    initializeDefaults();
    maximumResidual.parse(scriptCommand);
  }

  public void updateComScriptCommand(final ComScriptCommand scriptCommand)
    throws BadComScriptException {
    scriptCommand.useKeywordValue();
    maximumResidual.updateComScript(scriptCommand);
  }
  
  public String getMaximumResidual() {
    return maximumResidual.toString();
  }
}
