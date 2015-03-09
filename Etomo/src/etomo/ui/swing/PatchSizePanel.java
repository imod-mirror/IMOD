package etomo.ui.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;

import etomo.comscript.CombineParams;
import etomo.comscript.ConstCombineParams;
import etomo.type.CombinePatchSize;
import etomo.type.EnumeratedType;
import etomo.ui.FieldType;
import etomo.ui.FieldValidationFailedException;

/**
 * <p>Description: Panel to hold the patch size type or x, y, and z values.</p>
 *
 * <p>Copyright: Copyright 2015 by the Regents of the University of Colorado</p>
 * <p/>
 * <p>Organization: Dept. of MCD Biology, University of Colorado</p>
 *
 * @version $Id$
 */
final class PatchSizePanel implements ActionListener {
  private final JPanel pnlRoot = new JPanel();
  private final ButtonGroup bgType = new ButtonGroup();
  private final RadioButton rbTypeMedium = new RadioButton("Medium patches",
    CombinePatchSize.MEDIUM, bgType);
  private final RadioButton rbTypeLarge = new RadioButton("Large patches",
    CombinePatchSize.LARGE, bgType);
  private final RadioButton rbTypeXyz = new RadioButton("Custom", CombinePatchSize.XYZ,
    bgType);
  private final LabeledTextField ltfX = new LabeledTextField(FieldType.INTEGER, "X:");
  private final LabeledTextField ltfY = new LabeledTextField(FieldType.INTEGER, "Y:");
  private final LabeledTextField ltfZ = new LabeledTextField(FieldType.INTEGER, "Z:");

  private final RadioButton rbTypeSmall;
  private final RadioButton rbTypeExtraLarge;
  private final String title;
  private final boolean maxSize;

  private PatchSizePanel(final boolean maxSize) {
    this.maxSize = maxSize;
    String baseTitle = "Patch Size";
    if (!maxSize) {
      title = baseTitle;
      rbTypeSmall = new RadioButton("Small patches", CombinePatchSize.SMALL, bgType);
      rbTypeExtraLarge = null;
    }
    else {
      title = "Max " + baseTitle;
      rbTypeSmall = null;
      rbTypeExtraLarge =
        new RadioButton("Extra Large patches", CombinePatchSize.EXTRA_LARGE, bgType);
    }
  }

  static PatchSizePanel getInstance(final boolean maxSize) {
    PatchSizePanel instance = new PatchSizePanel(maxSize);
    instance.createPanel(maxSize);
    instance.addTooltips();
    instance.addListeners();
    return instance;
  }

  private void createPanel(final boolean maxSize) {
    // init
    if (rbTypeSmall != null) {
      rbTypeSmall.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    rbTypeMedium.setAlignmentX(Component.LEFT_ALIGNMENT);
    rbTypeLarge.setAlignmentX(Component.LEFT_ALIGNMENT);
    if (rbTypeExtraLarge != null) {
      rbTypeExtraLarge.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    rbTypeXyz.setAlignmentX(Component.LEFT_ALIGNMENT);
    // panels
    JPanel pnlType = new JPanel();
    JPanel pnlXYZ = new JPanel();
    // Root
    pnlRoot.setLayout(new BoxLayout(pnlRoot, BoxLayout.X_AXIS));
    pnlRoot.setBorder(new EtchedBorder(title).getBorder());
    pnlRoot.setAlignmentX(Component.CENTER_ALIGNMENT);
    pnlRoot.add(pnlType);
    pnlRoot.add(Box.createHorizontalGlue());
    pnlRoot.add(pnlXYZ);
    // Type
    pnlType.setLayout(new BoxLayout(pnlType, BoxLayout.Y_AXIS));
    if (rbTypeSmall != null) {
      pnlType.add(rbTypeSmall.getComponent());
    }
    pnlType.add(rbTypeMedium.getComponent());
    pnlType.add(rbTypeLarge.getComponent());
    if (rbTypeExtraLarge != null) {
      pnlType.add(rbTypeExtraLarge.getComponent());
    }
    pnlType.add(rbTypeXyz.getComponent());
    // XYZ
    pnlXYZ.setLayout(new BoxLayout(pnlXYZ, BoxLayout.Y_AXIS));
    pnlXYZ.setBorder(new EtchedBorder("In Pixels").getBorder());
    pnlXYZ.add(ltfX.getComponent());
    pnlXYZ.add(Box.createVerticalGlue());
    pnlXYZ.add(ltfY.getComponent());
    pnlXYZ.add(Box.createVerticalGlue());
    pnlXYZ.add(ltfZ.getComponent());
    // update
    actionPerformed(null);
  }

  Component getComponent() {
    return pnlRoot;
  }

  private void addListeners() {
    if (rbTypeSmall != null) {
      rbTypeSmall.addActionListener(this);
    }
    rbTypeMedium.addActionListener(this);
    rbTypeLarge.addActionListener(this);
    if (rbTypeExtraLarge != null) {
      rbTypeExtraLarge.addActionListener(this);
    }
    rbTypeXyz.addActionListener(this);
  }

  public void actionPerformed(final ActionEvent event) {
    EnumeratedType combinedPatchSize =
      ((RadioButton.RadioButtonModel) bgType.getSelection()).getEnumeratedType();
    if (combinedPatchSize != CombinePatchSize.XYZ) {
      ltfX.setText(combinedPatchSize.getValue(CombinePatchSize.X_INDEX));
      ltfY.setText(combinedPatchSize.getValue(CombinePatchSize.Y_INDEX));
      ltfZ.setText(combinedPatchSize.getValue(CombinePatchSize.Z_INDEX));
    }
    updateDisplay();
  }

  private void updateDisplay() {
    boolean enabled = rbTypeXyz.isEnabled();
    ltfX.setEnabled(enabled);
    ltfY.setEnabled(enabled);
    ltfZ.setEnabled(enabled);
    boolean selected = rbTypeXyz.isSelected();
    ltfX.setEditable(selected);
    ltfY.setEditable(selected);
    ltfZ.setEditable(selected);
  }

  void setEnabled(final boolean enabled) {
    if (rbTypeSmall != null) {
      rbTypeSmall.setEnabled(enabled);
    }
    rbTypeMedium.setEnabled(enabled);
    rbTypeLarge.setEnabled(enabled);
    if (rbTypeExtraLarge != null) {
      rbTypeExtraLarge.setEnabled(enabled);
    }
    rbTypeXyz.setEnabled(enabled);
    updateDisplay();
  }

  private boolean isEnabled() {
    return rbTypeMedium.isEnabled();
  }

  public void setParameters(final ConstCombineParams combineParams) {
    CombinePatchSize combinePatchSize;
    if (!maxSize) {
      combinePatchSize = combineParams.getPatchSize();
    }
    else {
      combinePatchSize = combineParams.getAutoPatchFinalSize();
    }
    if (combinePatchSize == CombinePatchSize.SMALL) {
      if (rbTypeSmall != null) {
        rbTypeSmall.setSelected(true);
      }
      else {
        selectTypeXyz(CombinePatchSize.SMALL);
      }
    }
    else if (combinePatchSize == CombinePatchSize.MEDIUM) {
      rbTypeMedium.setSelected(true);
    }
    else if (combinePatchSize == CombinePatchSize.LARGE) {
      rbTypeLarge.setSelected(true);
    }
    else if (combinePatchSize == CombinePatchSize.EXTRA_LARGE) {
      if (rbTypeExtraLarge != null) {
        rbTypeExtraLarge.setSelected(true);
      }
      else {
        selectTypeXyz(CombinePatchSize.EXTRA_LARGE);
      }
    }
    actionPerformed(null);
  }

  public boolean getParameters(final CombineParams combineParams,
    final boolean doValidation) {
    if (isEnabled()) {
      EnumeratedType combinedPatchSize =
        ((RadioButton.RadioButtonModel) bgType.getSelection()).getEnumeratedType();
      if (!maxSize) {
        combineParams.setPatchSize((CombinePatchSize) combinedPatchSize);
      }
      else {
        combineParams.setAutoPatchFinalSize((CombinePatchSize) combinedPatchSize);
      }
      if (combinedPatchSize == CombinePatchSize.XYZ) {
        try {
          String x = ltfX.getText(doValidation);
          String y = ltfY.getText(doValidation);
          String z = ltfZ.getText(doValidation);
          if (!maxSize) {
            combineParams.setPatchSize(x, y, z);
          }
          else {
            combineParams.setAutoPatchFinalSize(x, y, z);
          }
        }
        catch (FieldValidationFailedException e) {
          return false;
        }
      }
    }
    else if (!maxSize) {
      combineParams.resetPatchSize();
    }
    else {
      combineParams.resetExtraResidualTargets();
    }
    return true;
  }

  private void selectTypeXyz(final CombinePatchSize type) {
    rbTypeXyz.setSelected(true);
    ltfX.setText(type.getX());
    ltfY.setText(type.getY());
    ltfZ.setText(type.getZ());
  }

  private void addTooltips() {
    if (!maxSize) {
      rbTypeSmall
        .setToolTipText("Use small patches for refining the alignment with correlation - "
          + "appropriate for feature-rich tomogram from binned CCD camera images "
          + "or from film.");
      rbTypeMedium
        .setToolTipText("Use medium patches for refining the alignment with correlation - "
          + "appropriate for feature-rich tomogram from unbinned CCD camera " + "images.");
      rbTypeLarge
        .setToolTipText("Use large patches for refining the alignment with correlation - may be "
          + "needed for tomogram with sparse features.");
    }
  }
}
