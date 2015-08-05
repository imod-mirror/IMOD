package etomo.type;

/**
 * <p>Description: An enumeration of places where plugins can be used.  Contains
 * infomation about the plugin interface required for each niche.</p>
 * <p>
 * Plugins need to implement etomo.type.Plugin and return an instance of PluginNiche
 * (see etomo.type.Plugin.getPluginNiche()).  Different plugin niches work with different
 * descendents of etomo.type.Plugin.  A plugin is only valid if it implements the right
 * interface.  See the static instances defined in this class for valid niche/plugin-
 * interface combinations.</p>
 * <pre>
 * The plugin directory is located in the same directory as the IMOD directory:
 * $IMOD_DIR/../EtomoPlugins.  The plugin should be in a .jar file.
 * 
 * Creating a plugin from a class in a imaginary example program called Jabberwocky:
 *
 * cd Jabberwocky
 * ls -R
 * src
 * bin
 * ./src/lc/actions:
 * Gyre.java
 * Gimble.java
 * ./src/lc/critters:
 * Tove.java
 * Jabberwock.java
 * ./src/lc/people:
 * Son.java
 * ./src/lc/people/stuff:
 * VorpalSword.java
 * ./src/lc/places:
 * Wabe.java
 * ./src/lc/util:
 * Brillig.java
 *
 * 
 * This is how you would make VorpalSword.java available for use in eTomo as a plugin:
 * 
 * 1. Choose a PluginNiche instance.
 * 2. In VorpalSword.java, inherit the plugin interface associated with the plugin
 *    niche.
 * 3. Compile the Jabberwocky program.  This should place .class files in the bin
 *    directory.
 * 4. Go to the bin directory.
 * 5. Create a directory called META-INF
 * 6. In META-INF create a directory called services
 * 7. In META-INF/services create a file called etomo.type.Plugin
 * 8. Add the following lines to etomo.type.Plugin (end the file with an empty line):
lc/people/stuff/VorpalSword


 * 9. In the bin directory, run:
 *    jar -cf VorpalSword.jar META-INF/services lc/people/stuff/VorpalSword.class
 * 10. This will create a file called VorpalSword.jar.  You can look at the contents of
 *     The jar file by running:  jar -tf VorpalSword.jar
 * 11. A default manifest file was created by the jar program.  It has to be modifed.
 * 12. Extract the manifest file by running:
 *     jar -xf VorpalSword.jar META-INF/MANIFEST.MF
 * 13. Edit META-INF/MANIFEST.MF and add a Class-Path entry, set to the relative location
 *     of the etomo.jar file:
Manifest-Version: 1.0
Class-Path: ../IMOD/bin/etomo.jar
Created-By: 1.7.0_79 (Oracle Corporation)


 * 14. In the bin directory, recreate the jar file with the corrected manifest by running:
 *     jar -cmf META-INF/MANIFEST.MF VorpalSword.jar META-INF/services lc/people/stuff/VorpalSword.class
 * 15. Place VorpalSword.jar in $IMOD_DIR/../EtomoPlugins.
 * </pre>
 * <p/>
 * <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class PluginNiche {
  /**
   * The plugin's panel is brought up by a radio button at the top of the Tomogram
   * Generation dialog.  This niche can contain only one plugin.
   */
  public static final PluginNiche TOMOGRAM_GENERATION = new PluginNiche(
    "Tomogram Generation", etomo.ui.swing.Plugin.class);

  private final String descr;
  private final Class pluginInterface;

  private PluginNiche(final String descr, final Class pluginInterface) {
    this.descr = descr;
    this.pluginInterface = pluginInterface;
  }

  public String toString() {
    return descr;
  }

  public Class getPluginInterface() {
    return pluginInterface;
  }

  public static boolean validate(Plugin plugin) {
    if (plugin == null) {
      return false;
    }
    PluginNiche niche = TOMOGRAM_GENERATION;
    if (plugin.getPluginNiche() == niche) {
      if (!niche.pluginInterface.isInstance(plugin)) {
        System.err.println("Error: Invalid plugin " + plugin.getTitle()
          + ".  A plugin for the " + niche.descr + " niche must implement "
          + niche.pluginInterface + ".");
        return false;
      }
      return true;
    }
    System.err.println("Error: Unknown plugin " + plugin.getTitle()
      + ".  Plugin is not associated with a known PluginNiche.");
    return false;
  }
}
