package etomo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import etomo.type.Plugin;
import etomo.type.PluginNiche;
import etomo.type.UserConfiguration;
import etomo.ui.UIComponent;
import etomo.ui.swing.Popup;
import etomo.ui.swing.UIHarness;

/**
 * <p>Description: Manages plugins. </p>
 * <p/>
 * <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
public final class PluginFactory {
  public static final PluginFactory INSTANCE = new PluginFactory();

  private boolean loaded = false;
  private ArrayList<Plugin> list = null;

  private PluginFactory() {}

  public Plugin getPlugin(final PluginNiche niche) {
    if (niche == null) {
      return null;
    }
    if (list == null) {
      return null;
    }
    for (int i = 0; i < list.size(); i++) {
      Plugin plugin = list.get(i);
      if (plugin.getPluginNiche() == niche) {
        return plugin;
      }
    }
    return null;
  }

  public void loadPlugins(final UIComponent uiComponent) {
    if (loaded) {
      return;
    }
    synchronized (INSTANCE) {
      if (loaded) {
        return;
      }
      loaded = true;
      ServiceLoader<Plugin> loader = ServiceLoader.load(Plugin.class);
      ArrayList<Plugin> unknownList = null;
      try {
        Iterator<Plugin> pluginList = loader.iterator();
        UserConfiguration configuration = null;
        while (pluginList.hasNext()) {
          Plugin plugin = pluginList.next();
          //Make sure plugin conforms to the correct interface(s) for its niche.
          if (plugin != null) {
            if (!PluginNiche.validate(plugin)) {
              continue;
            }
            // Check for plugins that have already been allowed or excluded.
            if (configuration == null) {
              configuration = EtomoDirector.INSTANCE.getUserConfiguration();
            }
            if (configuration.hasPlugin(plugin.getKey())) {
              if (configuration.isPlugin(plugin.getKey())) {
                // Add plugins that have already been verified.
                if (list == null) {
                  list = new ArrayList<Plugin>();
                }
                list.add(plugin);
              }
            }
            else {
              // No information on the this plugin so ask the user.
              if (unknownList == null) {
                unknownList = new ArrayList<Plugin>();
              }
              unknownList.add(plugin);
            }
          }
        }
      }
      catch (ServiceConfigurationError e) {
        e.printStackTrace();
      }
      // Ask the user about new plugins
      if (unknownList != null) {
        for (int i = 0; i < unknownList.size(); i++) {
          Plugin plugin = unknownList.get(i);
          Popup popup =
            Popup.getYesNoInstance(uiComponent, "Allow plugin?",
              "An external plugin for eTomo was found.\n" + plugin.getTitle()
                + " version: " + plugin.getVersion() + "\n" + plugin.getDescription()
                + "\n\nUse this plugin?", "Do not ask about this plugin again.");
          UIHarness.INSTANCE.openPopup(popup);
          boolean allowed = false;
          if (popup.isYes()) {
            allowed = true;
            if (list == null) {
              list = new ArrayList<Plugin>();
            }
            list.add(plugin);
          }
          if (popup.isCheckboxSelected()) {
            EtomoDirector.INSTANCE.getUserConfiguration().setPlugin(plugin.getKey(),
              allowed);
          }
        }
      }
    }
  }
}
