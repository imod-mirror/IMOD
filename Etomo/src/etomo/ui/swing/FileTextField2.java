package etomo.ui.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import etomo.BaseManager;
import etomo.EtomoDirector;
import etomo.storage.DirectiveDef;
import etomo.ui.FieldSetting;
import etomo.ui.Field;
import etomo.ui.FieldSettingInterface;
import etomo.ui.FieldType;
import etomo.ui.TextFieldInterface;
import etomo.util.FilePath;
import etomo.util.Utilities;

/**
* <p>Description: Like FileTextField but handles relative paths</p>
* 
* <p>Copyright: Copyright 2011</p>
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
final class FileTextField2 implements FileTextFieldInterface, Field, ActionListener,
    TextFieldInterface {
  public static final String rcsid = "$Id:$";

  // Assuming the field type is always non-numeric
  private final FieldType STRING_FIELD_TYPE = FieldType.STRING;

  private final JPanel panel = new JPanel();

  private final SimpleButton button;
  private final TextField field;
  private final JLabel label;
  private final boolean labeled;
  private final BaseManager manager;
  final boolean alternateLayout;
  private final GridBagLayout layout;
  private final GridBagConstraints constraints;

  private List<ResultListener> resultListenerList = null;
  private int fileSelectionMode = -1;
  private FileFilter fileFilter = null;
  private boolean absolutePath = false;
  private boolean useTextAsOriginDir = false;
  private boolean turnOffFileHiding = false;

  /**
   * If origin is valid, it overrides originEtomoRunDir.
   */
  private File origin = null;
  /**
   * If true, then the origin directory of the file is the directory in which etomo was
   * run.  Useful when a dataset location has not been set.
   */
  private boolean originEtomoRunDir = false;
  private DirectiveDef directiveDef = null;

  public String toString() {
    return super.toString() + ":[text:" + field.getText() + ",label:" + label.getText();
  }

  private FileTextField2(final BaseManager manager, final String label,
      final boolean labeled, final boolean peet, final boolean alternateLayout) {
    if (!peet) {
      button = new SimpleButton(new ImageIcon(
          ClassLoader.getSystemResource("images/openFile.gif")));
    }
    else {
      button = new SimpleButton(new ImageIcon(
          ClassLoader.getSystemResource("images/openFilePeet.png")));
    }
    button.setName(label);
    field = new TextField(STRING_FIELD_TYPE, label, null);
    this.label = new JLabel(label);
    this.labeled = labeled;
    this.manager = manager;
    this.alternateLayout = alternateLayout;
    if (!alternateLayout) {
      layout = new GridBagLayout();
      constraints = new GridBagConstraints();
    }
    else {
      layout = null;
      constraints = null;
    }
  }

  /**
   * Get an unlabeled instance with a PEET-style button.  The starting directory for the
   * file chooser and the origin of relative files is the manager's property user
   * directory.
   * @param manager
   * @param name
   * @return
   */
  static FileTextField2 getUnlabeledPeetInstance(final BaseManager manager,
      final String name) {
    FileTextField2 instance = new FileTextField2(manager, name, false, true, false);
    instance.createPanel();
    instance.addListeners();
    return instance;
  }

  /**
   * Get a labeled instance with a PEET-style button.  The starting directory for the
   * file chooser and the origin of relative files is the manager's property user
   * directory.
   * @param manager
   * @param name
   * @return
   */
  static FileTextField2 getPeetInstance(final BaseManager manager, final String name) {
    FileTextField2 instance = new FileTextField2(manager, name, true, true, false);
    instance.createPanel();
    instance.addListeners();
    return instance;
  }

  static FileTextField2 getInstance(final BaseManager manager, final String name) {
    FileTextField2 instance = new FileTextField2(manager, name, true, false, false);
    instance.createPanel();
    instance.addListeners();
    return instance;
  }

  static FileTextField2 getAltLayoutInstance(final BaseManager manager, final String name) {
    FileTextField2 instance = new FileTextField2(manager, name, true, false, true);
    instance.createPanel();
    instance.addListeners();
    return instance;
  }

  private void createPanel() {
    // init
    field.setTextPreferredSize(new Dimension(250 * (int) Math.round(UIParameters.INSTANCE
        .getFontSizeAdjustment()), FixedDim.folderButton.height));
    button.setName(label.getText());
    button.setPreferredSize(FixedDim.folderButton);
    button.setMaximumSize(FixedDim.folderButton);
    if (!alternateLayout) {
      // panel
      panel.setLayout(layout);
      constraints.fill = GridBagConstraints.BOTH;
      constraints.weightx = 0.0;
      constraints.weighty = 0.0;
      constraints.gridheight = 1;
      constraints.gridwidth = 1;
      if (labeled) {
        layout.setConstraints(label, constraints);
        panel.add(label);
      }
      constraints.insets = new Insets(0, 0, 0, -1);
      layout.setConstraints(field.getComponent(), constraints);
      panel.add(field.getComponent());
      constraints.insets = new Insets(0, -1, 0, 0);
      layout.setConstraints(button, constraints);
      panel.add(button);
    }
    else {
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      panel.add(label);
      panel.add(field.getComponent());
      panel.add(button);
      panel.add(Box.createHorizontalGlue());
    }
  }

  private void addListeners() {
    button.addActionListener(new FileTextField2ActionListener(this));
  }

  /**
   * Adds a result listener to a list of result listeners.  A null listener has no effect.
   * @param listener
   */
  void addResultListener(final ResultListener listener) {
    if (listener == null) {
      return;
    }
    if (resultListenerList == null) {
      resultListenerList = new ArrayList<ResultListener>();
    }
    resultListenerList.add(listener);
  }

  Component getRootPanel() {
    return panel;
  }

  /**
   * @return a label suitable for a message - in single quotes and truncated at the colon.
   */
  public String getQuotedLabel() {
    return Utilities.quoteLabel(label.getText());
  }

  /**
   * Opens a file chooser and notifies the result listener list.
   */
  private void action() {
    String filePath = getFileChooserLocation();
    JFileChooser chooser = new FileChooser(new File(filePath));
    chooser.setDialogTitle(Utilities.stripLabel(label.getText()));
    if (fileSelectionMode != -1) {
      chooser.setFileSelectionMode(fileSelectionMode);
    }
    if (fileFilter != null) {
      chooser.setFileFilter(fileFilter);
    }
    chooser.setFileHidingEnabled(!turnOffFileHiding);
    chooser.setPreferredSize(UIParameters.INSTANCE.getFileChooserDimension());
    int returnVal = chooser.showOpenDialog(panel);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      setFile(chooser.getSelectedFile());
    }
    if (resultListenerList != null && resultListenerList.size() > 0) {
      Iterator<ResultListener> i = resultListenerList.iterator();
      while (i.hasNext()) {
        i.next().processResult(this);
      }
    }
  }

  /**
   * Sets field width with font adjustment.
   * @param width
   */
  void setAdjustedFieldWidth(final double width) {
    field.setTextPreferredWidth(width * UIParameters.INSTANCE.getFontSizeAdjustment());
  }

  void setAbsolutePath(final boolean input) {
    this.absolutePath = input;
  }

  void setOriginEtomoRunDir(final boolean input) {
    this.originEtomoRunDir = input;
  }

  void setPreferredWidth(final double width) {
    field.setTextPreferredWidth(width);
  }

  /**
   * Sets the origin member variable which overrides the originEtomoRunDir member variable
   * and the propertyUserDir when it is a valid directory.
   * @return
   */
  void setOrigin(final File input) {
    origin = input;
  }

  void setOrigin(final String input) {
    if (input != null) {
      origin = new File(input);
    }
  }

  /**
   * If useTextAsOriginDir is true, the text in the text field with be where the file
   * chooser opens, if the text field contains a directory.
   * @param input
   */
  void setUseTextAsOriginDir(final boolean input) {
    useTextAsOriginDir = input;
  }

  public boolean isEmpty() {
    String text = field.getText();
    return text == null || text.matches("\\s*");
  }

  public boolean isEnabled() {
    return button.isEnabled();
  }

  boolean exists() {
    if (!isEmpty()) {
      return getFile().exists();
    }
    return false;
  }

  public File getFile() {
    if (!isEmpty()) {
      return FilePath.buildAbsoluteFile(getOriginDir(), field.getText());
    }
    return null;
  }

  public boolean equals(final FileTextField2 input) {
    if (input == null) {
      return false;
    }
    File file = getFile();
    File inputFile = input.getFile();
    if (file == null) {
      return inputFile == null;
    }
    return file.equals(inputFile);
  }

  /**
   * Saves the current text as the checkpoint.
   */
  public void checkpoint() {
    field.checkpoint();
  }

  public void setCheckpoint(final FieldSettingInterface input) {
    if (input == null) {
      field.setCheckpoint(null);
    }
    else {
      field.setCheckpoint(input.getBooleanSetting());
    }
  }

  public void backup() {
    field.backup();
  }

  /**
   * If the field was backed up, make the backup value the displayed value, and turn off
   * the back up.
   */
  public void restoreFromBackup() {
    field.restoreFromBackup();
  }

  void setDirectiveDef(final DirectiveDef directiveDef) {
    field.setDirectiveDef(directiveDef);
  }

  public void useDefaultValue() {
    field.useDefaultValue();
  }

  public void setFieldHighlightValue(final String value) {
    // This class connects the button action to field highlight.
    if (!field.isFieldHighlightSet()) {
      button.addActionListener(this);
    }
    field.setFieldHighlightValue(value);
  }

  public void setFieldHighlight(final FieldSettingInterface input) {
    boolean set = field.isFieldHighlightSet();
    field.setFieldHighlight(input);
    if (input == null || !input.isSet()) {
      clearFieldHighlightValue();
    }
    else if (!set) {
      button.addActionListener(this);
    }
  }

  public void clearFieldHighlightValue() {
    button.removeActionListener(this);
    field.clearFieldHighlightValue();
  }

  public void actionPerformed(ActionEvent e) {
    field.updateFieldHighlight();
  }

  /**
   * 
   * @param alwaysCheck - check for difference even when the field is disabled or invisible
   * @return
   */
  public boolean isDifferentFromCheckpoint(final boolean alwaysCheck) {
    return field.isDifferentFromCheckpoint(alwaysCheck);
  }

  /**
   * Gets the origin directory.
   * @return
   */
  private String getOriginDir() {
    if (origin != null && origin.exists() && origin.isDirectory()) {
      return origin.getAbsolutePath();
    }
    if (manager == null || originEtomoRunDir) {
      return EtomoDirector.INSTANCE.getOriginalUserDir();
    }
    return manager.getPropertyUserDir();
  }

  private String getFileChooserLocation() {
    if (useTextAsOriginDir) {
      File dir = getFile();
      if (dir != null && dir.isDirectory()) {
        return dir.getAbsolutePath();
      }
    }
    return getOriginDir();
  }

  /**
   * Adds the text of the file path to the field.  The file path will be either absolute 
   * or relative depending on the member variable absolutePath.  The directory will be set 
   * to propertyUserDir, unless the member variable originEtomoRunDir is true.  The 
   * directory will be used as the origin when building a relative file, or when building 
   * an absolute file out of a relative file.
   * @param file
   */
  public void setFile(final File file) {
    if (absolutePath) {
      field.setText(FilePath.buildAbsoluteFile(getOriginDir(), file).getPath());
    }
    else {
      field.setText(FilePath.getRelativePath(getOriginDir(), file));
    }
  }

  /**
   * Sets the file selection mode to be used in the file chooser.
   * @param input
   */
  void setFileSelectionMode(final int input) {
    if (input != FileChooser.FILES_ONLY && input != FileChooser.DIRECTORIES_ONLY
        && input != FileChooser.FILES_AND_DIRECTORIES) {
      System.err.println("WARNING: Incorrect file chooser file selection mode: " + input);
      return;
    }
    fileSelectionMode = input;
  }

  void setTurnOffFileHiding(final boolean input) {
    turnOffFileHiding = input;
  }

  void setFileFilter(final FileFilter input) {
    fileFilter = input;
  }

  public FileFilter getFileFilter() {
    return fileFilter;
  }

  public String getText() {
    return field.getText();
  }

  public void setText(final String text) {
    field.setText(text);
  }

  public void clear() {
    field.setText("");
  }

  public void copy(final Field from) {
    field.copy(from);
  }

  public boolean isSelected() {
    return false;
  }

  void setEnabled(final boolean enabled) {
    field.setEnabled(enabled);
    button.setEnabled(enabled);
  }

  void setFieldEditable(final boolean editable) {
    field.setEditable(editable);
  }

  void setToolTipText(String text) {
    field.setToolTipText(text);
    text = TooltipFormatter.INSTANCE.format(text);
    panel.setToolTipText(text);
    button.setToolTipText(text);
  }

  void setFieldToolTipText(final String text) {
    field.setToolTipText(text);
  }

  void setButtonToolTipText(final String text) {
    button.setToolTipText(TooltipFormatter.INSTANCE.format(text));
  }

  private final class FileTextField2ActionListener implements ActionListener {
    private final FileTextField2 adaptee;

    private FileTextField2ActionListener(final FileTextField2 adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(final ActionEvent event) {
      adaptee.action();
    }
  }
}
