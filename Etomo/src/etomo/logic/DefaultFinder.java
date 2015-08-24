package etomo.logic;

import java.io.FileNotFoundException;
import java.io.IOException;

import etomo.storage.DirectiveDef;
import etomo.storage.LogFile;
import etomo.storage.autodoc.AutodocFactory;
import etomo.storage.autodoc.ReadOnlyAttribute;
import etomo.storage.autodoc.ReadOnlyAutodoc;
import etomo.storage.autodoc.ReadOnlySection;
import etomo.type.EtomoAutodoc;

/**
* <p>Description: </p>
*
 * <p>Copyright: Copyright 2014 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
*/
public final class DefaultFinder {
  public static final String rcsid = "$Id:$";

  public static final DefaultFinder INSTANCE = new DefaultFinder();

  public ReadOnlyAutodoc progDefaultsAutodoc = null;

  private DefaultFinder() {}

  /**
   * Gets the default value from the autodoc corresponding to the directiveDef, or from
   * progDefaults as a fallback.
   * @param manager
   * @param directiveDef - a comparam directiveDef
   * @return
   */
  public String getDefaultValue(final DirectiveDef directiveDef) {
    String command;
    if (directiveDef.isComparam()) {
      command = directiveDef.getCommand();
    }
    else if (directiveDef.isRuntime()) {
      // Warning: not all of the directive modules have a corresponding .adoc file.
      command = directiveDef.getModule();
      if (command == null) {
        return null;
      }
      command = command.toLowerCase();
    }
    else {
      return null;
    }
    boolean autodocLoaded = AutodocFactory.isLoaded(command);
    ReadOnlySection commandSection = null;
    // If the autodoc has not been loaded, look for directiveDef.command in
    // progDefaults.adoc to see if the autodoc has defaults before loading it. If
    // progDefefault.adoc doesn't show any defaults for the command, return null.
    if (!autodocLoaded) {
      commandSection = getCommandSection(command);
    }
    // If the autodoc is already loaded, or command is present in progDefaults.adoc, try
    // to get the default from the autodoc.
    if (autodocLoaded || commandSection != null) {
      String name = directiveDef.getName();
      //Runtime names start with a small letter.  Capitalize it so it matches the
      //parameter name, which will be capitalized.
      if (directiveDef.isRuntime() && name != null && !name.isEmpty()) {
        String firstLetter = name.substring(0, 1).toUpperCase();
        if (name.length() > 1) {
          name = firstLetter + name.substring(1);
        }
        else {
          name = firstLetter;
        }
      }
      try {
        ReadOnlyAutodoc autodoc = AutodocFactory.getInstance(null, command);
        if (autodoc != null) {
          ReadOnlySection fieldSection =
            autodoc.getSection(EtomoAutodoc.FIELD_SECTION_NAME, name);
          if (fieldSection != null) {
            ReadOnlyAttribute attribute = fieldSection.getAttribute("default");
            if (attribute != null) {
              String value = attribute.getValue();
              if (value != null) {
                return value;
              }
            }
          }
        }
      }
      catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
      catch (LogFile.LockException e) {
        e.printStackTrace();
      }
      // Fallback - if the default was not found in the autodoc, try to find it in
      // progDefaults.
      if (commandSection == null) {
        commandSection = getCommandSection(command);
      }
      if (commandSection != null) {
        ReadOnlyAttribute attribute = commandSection.getAttribute(name);
        if (attribute != null) {
          return attribute.getValue();
        }
      }
    }
    return null;
  }

  /**
   * Returns a command section in the progDefaults autodoc.
   * @param command
   * @return
   */
  private ReadOnlySection getCommandSection(final String command) {
    try {
      if (progDefaultsAutodoc == null) {
        progDefaultsAutodoc = AutodocFactory.getComInstance(AutodocFactory.PROG_DEFAULTS);
      }
      if (progDefaultsAutodoc != null) {
        return progDefaultsAutodoc.getSection("Program", command);
      }
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (LogFile.LockException e) {
      e.printStackTrace();
    }
    return null;
  }
}
