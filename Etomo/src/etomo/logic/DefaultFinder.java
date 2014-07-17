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
* <p>Copyright: Copyright 2014</p>
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
public final class DefaultFinder {
  public static final String rcsid = "$Id:$";

  public static final DefaultFinder INSTANCE = new DefaultFinder();

  public ReadOnlyAutodoc progDefaultsAutodoc = null;

  private DefaultFinder() {
  }

  /**
   * Gets the default value from the autodoc corresponding to the directiveDef, or from
   * progDefaults as a fallback.
   * @param manager
   * @param directiveDef - a comparam directiveDef
   * @return
   */
  public String getDefaultValue(final DirectiveDef directiveDef) {
    if (!directiveDef.isComparam()) {
      return null;
    }
    // If the autodoc has not been loaded, look for directiveDef.command in
    // progDefaults.adoc to see if the autodoc has defaults before loading it. If
    // progDefefault.adoc doesn't show any defaults for the command, return null.
    String name = directiveDef.getName();
    boolean autodocLoaded = AutodocFactory.isLoaded(name);
    String command = directiveDef.getCommand();
    ReadOnlySection commandSection = null;
    if (!autodocLoaded) {
      if (progDefaultsAutodoc == null) {
        try {
          progDefaultsAutodoc = AutodocFactory
              .getComInstance(AutodocFactory.PROG_DEFAULTS);
          if (progDefaultsAutodoc != null) {
            commandSection = progDefaultsAutodoc.getSection("Program", command);
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
      }
    }
    // If the autodoc is already loaded, or command is present progDefaults.adoc, try to
    // get the default from the autodoc. If it is not there, try to get it from
    // progDefaults.
    if (autodocLoaded || commandSection != null) {
      try {
        ReadOnlyAutodoc autodoc = AutodocFactory.getInstance(null, command);
        if (autodoc != null) {
          ReadOnlySection fieldSection = autodoc.getSection(
              EtomoAutodoc.FIELD_SECTION_NAME, name);
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
   * Converts a default value to boolean.
   * @param value
   * @return
   */
  public static boolean toBoolean(String value) {
    if (value == null) {
      return false;
    }
    value = value.trim();
    if (value.equals("")) {
      // Generally the presence of a boolean name signifies that it is true.
      return true;
    }
    if (value.equals(1) || value.compareToIgnoreCase("t") == 0
        || value.compareToIgnoreCase("true") == 0 || value.compareToIgnoreCase("y") == 0
        || value.compareToIgnoreCase("yes") == 0) {
      return true;
    }
    if (value.equals(0) || value.compareToIgnoreCase("f") == 0
        || value.compareToIgnoreCase("false") == 0 || value.compareToIgnoreCase("n") == 0
        || value.compareToIgnoreCase("no") == 0) {
      return false;
    }
    return false;
  }
}
