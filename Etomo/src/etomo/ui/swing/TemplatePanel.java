package etomo.ui.swing;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import etomo.BaseManager;
import etomo.logic.ConfigTool;
import etomo.storage.DirectiveDef;
import etomo.storage.DirectiveFile;
import etomo.storage.DirectiveFileCollection;
import etomo.storage.autodoc.WritableAutodoc;
import etomo.type.AxisID;
import etomo.type.DirectiveFileType;
import etomo.type.UserConfiguration;

/**
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright 2013 - 2014 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
final class TemplatePanel {
  private static String EMPTY_OPTION = "None available";
  private static String SELECT_OPTION1 = "No selection (";
  private static final String SELECT_OPTION2 = " available)";
  private static final int NUM_TEMPLATES = 3;

  private final JPanel pnlRoot = new JPanel();
  private final ComboBox cmbScopeTemplate =
      ComboBox.getInstance("Scope template:", false);
  private final ComboBox cmbSystemTemplate =
      ComboBox.getInstance("System template:", false);
  private final ComboBox cmbUserTemplate = ComboBox.getInstance("User template:", false);

  private final TemplateActionListener listener;
  private final BaseManager manager;
  private final AxisID axisID;
  final SettingsDialog settings;
  private final DirectiveFileCollection directiveFileCollection;
  private final boolean drawBorder;

  private File[] scopeTemplateFileList = null;
  private File[] systemTemplateFileList = null;
  private File[] userTemplateFileList = null;
  private File newUserTemplateDir = null;
  private boolean actionsActive = true;

  private TemplatePanel(final BaseManager manager, final AxisID axisID,
      final TemplateActionListener listener, final SettingsDialog settings,
      final boolean drawBorder, final DirectiveFileCollection directiveFileCollection) {
    this.listener = listener;
    this.manager = manager;
    this.axisID = axisID;
    this.settings = settings;
    this.drawBorder = drawBorder;
    if (directiveFileCollection == null) {
      this.directiveFileCollection = new DirectiveFileCollection(manager, axisID);
    }
    else {
      this.directiveFileCollection = directiveFileCollection;
    }
  }

  static TemplatePanel getInstance(final BaseManager manager, final AxisID axisID,
      final TemplateActionListener listener, final String title,
      final SettingsDialog settings) {
    TemplatePanel instance =
        new TemplatePanel(manager, axisID, listener, settings, true, null);
    instance.createPanel(title);
    instance.addListeners();
    return instance;
  }

  static TemplatePanel getBorderlessInstance(final BaseManager manager,
      final AxisID axisID, final TemplateActionListener listener, final String title,
      final SettingsDialog settings,
      final DirectiveFileCollection directiveFileCollection,
      final boolean delayListeners) {
    TemplatePanel instance = new TemplatePanel(manager, axisID, listener, settings, false,
        directiveFileCollection);
    instance.createPanel(title);
    if (!delayListeners) {
      instance.addListeners();
    }
    return instance;
  }

  private void createPanel(final String title) {
    // init
    scopeTemplateFileList = ConfigTool.getScopeTemplateFiles();
    if (scopeTemplateFileList != null && scopeTemplateFileList.length > 0) {
      cmbScopeTemplate
          .addItem(SELECT_OPTION1 + scopeTemplateFileList.length + SELECT_OPTION2);
      for (int i = 0; i < scopeTemplateFileList.length; i++) {
        cmbScopeTemplate.addItem(scopeTemplateFileList[i].getName());
      }
    }
    else {
      cmbScopeTemplate.addItem(EMPTY_OPTION);
      cmbScopeTemplate.setComboBoxEnabled(false);
    }
    cmbScopeTemplate.setSelectedIndex(0);
    systemTemplateFileList = ConfigTool.getSystemTemplateFiles();
    if (systemTemplateFileList != null && systemTemplateFileList.length > 0) {
      cmbSystemTemplate
          .addItem(SELECT_OPTION1 + systemTemplateFileList.length + SELECT_OPTION2);
      for (int i = 0; i < systemTemplateFileList.length; i++) {
        cmbSystemTemplate.addItem(systemTemplateFileList[i].getName());
      }
    }
    else {
      cmbSystemTemplate.addItem(EMPTY_OPTION);
      cmbSystemTemplate.setComboBoxEnabled(false);
    }
    cmbSystemTemplate.setSelectedIndex(0);
    loadUserTemplate();
    pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.Y_AXIS));
    if (drawBorder) {
      if (title != null) {
        pnlRoot.setBorder(new EtchedBorder(title).getBorder());
      }
      else {
        pnlRoot.setBorder(BorderFactory.createEtchedBorder());
      }
    }
    pnlRoot.add(Box.createRigidArea(FixedDim.x0_y2));
    pnlRoot.add(cmbScopeTemplate.getComponent());
    pnlRoot.add(Box.createRigidArea(FixedDim.x0_y3));
    pnlRoot.add(cmbSystemTemplate.getComponent());
    pnlRoot.add(Box.createRigidArea(FixedDim.x0_y3));
    pnlRoot.add(cmbUserTemplate.getComponent());
    pnlRoot.add(Box.createRigidArea(FixedDim.x0_y2));
  }

  void addListeners() {
    addActionListener(listener);
    cmbUserTemplate.addFocusListener(new TemplateFocusListener(this));
  }

  Component getComponent() {
    return pnlRoot;
  }

  void addActionListener(final ActionListener listener) {
    cmbScopeTemplate.addActionListener(listener);
    cmbSystemTemplate.addActionListener(listener);
    cmbUserTemplate.addActionListener(listener);
  }

  void saveAutodoc(final WritableAutodoc autodoc) {
    File templateFile = getScopeTemplateFile();
    if (templateFile != null) {
      autodoc
          .addNameValuePairAttribute(DirectiveDef.SCOPE_TEMPLATE.getDirective(null, null),
              templateFile.getAbsolutePath());
    }
    templateFile = getSystemTemplateFile();
    if (templateFile != null) {
      autodoc.addNameValuePairAttribute(
          DirectiveDef.SYSTEM_TEMPLATE.getDirective(null, null),
          templateFile.getAbsolutePath());
    }
    templateFile = getUserTemplateFile();
    if (templateFile != null) {
      autodoc
          .addNameValuePairAttribute(DirectiveDef.USER_TEMPLATE.getDirective(null, null),
              templateFile.getAbsolutePath());
    }
  }

  void setFieldHighlight() {
    cmbScopeTemplate.setFieldHighlight();
    cmbSystemTemplate.setFieldHighlight();
    cmbUserTemplate.setFieldHighlight();
  }

  private void loadUserTemplate() {
    userTemplateFileList = null;
    cmbUserTemplate.removeAllItems();
    // If the user template directory is in a different directory from the location of the
    // default user template, the user template will not be loaded.
    userTemplateFileList = ConfigTool.getUserTemplateFiles(newUserTemplateDir);
    if (userTemplateFileList != null && userTemplateFileList.length > 0) {
      cmbUserTemplate
          .addItem(SELECT_OPTION1 + userTemplateFileList.length + SELECT_OPTION2);
      cmbUserTemplate.setComboBoxEnabled(true);
      for (int i = 0; i < userTemplateFileList.length; i++) {
        cmbUserTemplate.addItem(userTemplateFileList[i].getName());
      }
    }
    else {
      cmbUserTemplate.addItem(EMPTY_OPTION);
      cmbUserTemplate.setComboBoxEnabled(false);
    }
    cmbUserTemplate.setSelectedIndex(0);
  }

  private File getTemplateFile(final ComboBox cmbTemplate,
      final File[] templateFileList) {
    if (cmbTemplate.isEnabled()) {
      int i = cmbTemplate.getSelectedIndex() - 1;
      if (i != -1 && templateFileList != null) {
        return templateFileList[i];
      }
    }
    return null;
  }

  /**
   * Choose the combobox element that matches template
   *
   * @param template         - either an absolute file path or a file name with no path
   *                         (imodhelp)
   * @param templateFileList
   * @param cmbTemplate
   */
  private void setTemplate(final String template, final File[] templateFileList,
      final ComboBox cmbTemplate) {
    if (templateFileList == null || template == null) {
      return;
    }
    boolean absPath = false;
    if (template.indexOf(File.separator) != -1) {
      absPath = true;
    }
    // If template doesn't match something in templateFileList, nothing will be
    // selected in the combobox.
    for (int i = 0; i < templateFileList.length; i++) {
      if ((absPath && templateFileList[i].getAbsolutePath().equals(template)) ||
          (!absPath && templateFileList[i].getName().equals(template))) {
        cmbTemplate.setSelectedIndex(i + 1);
        break;
      }
    }
  }

  void clear() {
    cmbScopeTemplate.setSelectedIndex(0);
    cmbSystemTemplate.setSelectedIndex(0);
    cmbUserTemplate.setSelectedIndex(0);
  }

  /**
   * When input is false, indirectly inactivates actions by preventing the recognition of
   * action commands.
   * @param input
   */
  void activateActions(final boolean input) {
    actionsActive = input;
  }

  boolean equalsActionCommand(final String actionCommand) {
    if (!actionsActive) {
      return false;
    }
    return actionCommand.equals(cmbScopeTemplate.getActionCommand()) ||
        actionCommand.equals(cmbSystemTemplate.getActionCommand()) ||
        actionCommand.equals(cmbUserTemplate.getActionCommand());
  }

  void getParameters(final UserConfiguration userConfig) {
    userConfig.setScopeTemplate(getScopeTemplateFile());
    userConfig.setSystemTemplate(getSystemTemplateFile());
    userConfig.setUserTemplate(getUserTemplateFile());
  }

  /**
   * Refresh the directive file collection and return it.
   *
   * @return
   */
  DirectiveFileCollection getDirectiveFileCollection() {
    refreshDirectiveFileCollection();
    return directiveFileCollection;
  }

  /**
   * Refresh the directive file collection and return it.
   *
   * @return
   */
  void refreshDirectiveFileCollection() {
    directiveFileCollection
        .setDirectiveFile(getScopeTemplateFile(), DirectiveFileType.SCOPE);
    directiveFileCollection
        .setDirectiveFile(getSystemTemplateFile(), DirectiveFileType.SYSTEM);
    directiveFileCollection
        .setDirectiveFile(getUserTemplateFile(), DirectiveFileType.USER);
  }

  File[] getFiles() {
    File[] files = new File[NUM_TEMPLATES];
    int i = 0;
    files[i++] = getScopeTemplateFile();
    files[i++] = getSystemTemplateFile();
    files[i++] = getUserTemplateFile();
    return files;
  }

  private File getScopeTemplateFile() {
    return getTemplateFile(cmbScopeTemplate, scopeTemplateFileList);
  }

  private File getSystemTemplateFile() {
    return getTemplateFile(cmbSystemTemplate, systemTemplateFileList);
  }

  private File getUserTemplateFile() {
    return getTemplateFile(cmbUserTemplate, userTemplateFileList);
  }

  private void focusGained() {
    // Only need to listen to user template combobox
    reloadUserTemplate();
  }

  /**
   * Updates the user template combobox. It may need to change if settings are modified.
   */
  private void reloadUserTemplate() {
    if (settings != null && !settings.equalsUserTemplateDir(newUserTemplateDir)) {
      // If a new user template directory has been entered, reload the user template combo
      // box.
      newUserTemplateDir = settings.getUserTemplateDir();
      loadUserTemplate();
    }
  }

  boolean isAppearanceSettingChanged(final UserConfiguration userConfig) {
    if (!userConfig.equalsScopeTemplate(getScopeTemplateFile()) ||
        !userConfig.equalsSystemTemplate(getSystemTemplateFile()) ||
        !userConfig.equalsUserTemplate(getUserTemplateFile())) {
      return true;
    }
    return false;
  }

  void setParameters(final UserConfiguration userConfig) {
    if (userConfig.isScopeTemplateSet()) {
      setTemplate(userConfig.getScopeTemplate(), scopeTemplateFileList, cmbScopeTemplate);
    }
    if (userConfig.isSystemTemplateSet()) {
      setTemplate(userConfig.getSystemTemplate(), systemTemplateFileList,
          cmbSystemTemplate);
    }
    if (userConfig.isUserTemplateSet()) {
      setTemplate(userConfig.getUserTemplate(), userTemplateFileList, cmbUserTemplate);
    }
  }

  /**
   * Set the template file if the entry exists in the directiveFile, otherwise don't
   * change it.
   *
   * @param directiveFile
   */
  void setParameters(final DirectiveFile directiveFile) {
    if (directiveFile == null) {
      return;
    }
    DirectiveDef directiveDef = DirectiveDef.SCOPE_TEMPLATE;
    if (directiveFile.contains(directiveDef)) {
      setTemplate(directiveFile.getValue(directiveDef), scopeTemplateFileList,
          cmbScopeTemplate);
    }
    directiveDef = DirectiveDef.SYSTEM_TEMPLATE;
    if (directiveFile.contains(directiveDef)) {
      setTemplate(directiveFile.getValue(directiveDef), systemTemplateFileList,
          cmbSystemTemplate);
    }
    directiveDef = DirectiveDef.USER_TEMPLATE;
    if (directiveFile.contains(directiveDef)) {
      reloadUserTemplate();
      setTemplate(directiveFile.getValue(directiveDef), userTemplateFileList,
          cmbUserTemplate);
    }
    refreshDirectiveFileCollection();
  }

  private static final class TemplateFocusListener implements FocusListener {
    private final TemplatePanel template;

    TemplateFocusListener(final TemplatePanel template) {
      this.template = template;
    }

    public void focusGained(final FocusEvent e) {
      template.focusGained();
    }

    public void focusLost(final FocusEvent e) {
    }
  }
}
