package etomo.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import etomo.comscript.ConstTiltalignParam;
import etomo.comscript.FortranInputSyntaxException;
import etomo.comscript.StringList;
import etomo.comscript.TiltalignParam;
import etomo.type.AxisID;
import etomo.util.FidXyz;
import etomo.util.InvalidParameterException;
import etomo.util.MRCHeader;

/**
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
 * <p> Revision 3.3  2004/07/02 17:42:18  sueh
 * <p> bug#461 removing prints
 * <p>
 * <p> Revision 3.2  2004/07/02 00:40:07  sueh
 * <p> bug# 461 adding a connection to fid.xyz, preali header, and rawstack
 * <p> header to the constructor.  Info from these files is required
 * <p> to calculate z shift.  Correcting the calculation of binning
 * <p> when caculating z shift.
 * <p>
 * <p> Revision 3.1  2004/06/21 17:16:37  rickg
 * <p> Bug #461 z shift is scaled by the prealigned binning
 * <p>
 * <p> Revision 3.0  2003/11/07 23:19:01  rickg
 * <p> Version 1.0.0
 * <p>
 * <p> Revision 2.10  2003/10/30 01:43:44  rickg
 * <p> Bug# 338 Remapped context menu entries
 * <p>
 * <p> Revision 2.9  2003/10/28 20:59:33  rickg
 * <p> Bug# 280 Tooltips
 * <p>
 * <p> Revision 2.8  2003/10/22 22:53:37  rickg
 * <p> Bug# 279 Removed "linear"
 * <p>
 * <p> Revision 2.7  2003/10/20 20:08:37  sueh
 * <p> Bus322 corrected labels
 * <p>
 * <p> Revision 2.6  2003/10/14 20:30:25  rickg
 * <p> Bug#279  Label layout and name changes
 * <p>
 * <p> Revision 2.5  2003/10/09 23:21:21  rickg
 * <p> Bug#279  Label layout and name changes
 * <p>
 * <p> Revision 2.4  2003/05/27 08:50:28  rickg
 * <p> Context menu handled by parent window
 * <p>
 * <p> Revision 2.3  2003/05/23 22:03:06  rickg
 * <p> Changed default tilt angle group size to 5
 * <p>
 * <p> Revision 2.2  2003/03/20 17:43:52  rickg
 * <p> Comment update
 * <p>
 * <p> Revision 2.1  2003/03/02 23:30:41  rickg
 * <p> Combine layout in progress
 * <p>
 * <p> Revision 2.0  2003/01/24 20:30:31  rickg
 * <p> Single window merge to main branch
 * <p>
 * <p> Revision 1.23  2003/01/15 00:11:42  rickg
 * <p> Fixed handling of xstretch and skew types (both global and
 * <p> local) when the original align.com does not have those
 * <p> parameters set and the user requests them.
 * <p>
 * <p> Revision 1.22.2.2  2003/01/24 19:04:54  rickg
 * <p> Merged changes from main branch
 * <p>
 * <p> Revision 1.22.2.1  2003/01/24 18:43:37  rickg
 * <p> Single window GUI layout initial revision
 * <p>
 * <p> Revision 1.22  2003/01/06 05:57:29  rickg
 * <p> Quick fix for residual threshold value text field size.  Needs to be more
 * <p> robust
 * <p>
 * <p> Revision 1.21  2003/01/04 00:24:29  rickg
 * <p> Wrap tab pane with a border to ID it as a tiltalign
 * <p> panel.
 * <p>
 * <p> Revision 1.20  2002/12/31 23:13:47  rickg
 * <p> Layout simplification
 * <p>
 * <p> Revision 1.19  2002/12/24 01:08:08  rickg
 * <p> Moved min local patch size to advanced
 * <p>
 * <p> Revision 1.18  2002/12/20 01:08:19  rickg
 * <p> Spelling correction
 * <p>
 * <p> Revision 1.17  2002/12/18 19:15:31  rickg
 * <p> Added advanced capability for metro factor and
 * <p> cycle limit.
 * <p> Ordered handling of panel updates
 * <p>
 * <p> Revision 1.16  2002/12/18 00:53:00  rickg
 * <p> Update in progress
 * <p>
 * <p> Revision 1.15  2002/12/17 01:02:08  rickg
 * <p> Additional groups are now advanced
 * <p>
 * <p> Revision 1.14  2002/12/10 21:33:10  rickg
 * <p> Add residual threshold control
 * <p>
 * <p> Revision 1.13  2002/12/06 01:02:58  rickg
 * <p> Redesign in progress
 * <p>
 * <p> Revision 1.12  2002/12/05 01:21:31  rickg
 * <p> Redesign in progress
 * <p>
 * <p> Revision 1.11  2002/12/04 04:42:13  rickg
 * <p> Redesign in progress
 * <p>
 * <p> Revision 1.10  2002/12/04 01:19:10  rickg
 * <p> Redesign in progress
 * <p>
 * <p> Revision 1.9  2002/12/03 05:41:13  rickg
 * <p> redesign in progress
 * <p>
 * <p> Revision 1.8  2002/12/03 05:22:29  rickg
 * <p> added getLocalRotationSolutionGroupSize
 * <p>
 * <p> Revision 1.7  2002/12/03 00:53:20  rickg
 * <p> Redesign in progress
 * <p>
 * <p> Revision 1.6  2002/11/25 15:59:37  rickg
 * <p> Removed local skew radio buttons
 * <p>
 * <p> Revision 1.5  2002/11/22 00:56:52  rickg
 * <p> removal of non-used fields in progress
 * <p>
 * <p> Revision 1.4  2002/11/14 21:18:37  rickg
 * <p> Added anchors into the tomoguide
 * <p>
 * <p> Revision 1.3  2002/10/17 23:40:32  rickg
 * <p> Added methods to set titl/mag and distortion defaults.
 * <p>
 * <p> Revision 1.2  2002/10/16 23:20:36  rickg
 * <p> Reformat
 * <p>
 * <p> Revision 1.1  2002/09/09 22:57:02  rickg
 * <p> Initial CVS entry, basic functionality not including combining
 * <p> </p>
 */

public class TiltalignPanel {
  public static final String rcsid = "$Id$";

  private AxisID axisID;

  private int prealignedBinning = 1;

  //  TODO need recomended default for all sub groups see (align.com)
  private final int defaultTiltAngleType = 5;
  private final int defaultTiltAngleGroupSize = 5;
  private final int defaultMagnificationType = 3;
  private final int defaultDistortionType = 2;
  private final int defaultXstretchType = 3;
  private final int defaultXstretchGroupSize = 7;
  private final int defaultSkewType = 3;
  private final int defaultSkewGroupSize = 11;

  private final int defaultLocalRotationType = 3;
  private final int defaultLocalRotationGroupSize = 6;
  private final int defaultLocalTiltAngleType = 5;
  private final int defaultLocalTiltAngleGroupSize = 6;
  private final int defaultLocalMagnificationType = 3;
  private final int defaultLocalMagnificationGroupSize = 7;
  private final int defaultLocalDistortionType = 2;
  private final int defaultLocalXstretchType = 3;
  private final int defaultLocalXstretchGroupSize = 7;
  private final int defaultLocalSkewType = 3;
  private final int defaultLocalSkewGroupSize = 11;

  private JTabbedPane tabPane = new JTabbedPane();

  //  General pane
  private JPanel pnlGeneral = new JPanel();

  private LabeledTextField ltfResidualThreshold = new LabeledTextField(
    "Threshold for residual report: ");

  private JRadioButton rbResidAllViews = new JRadioButton("All views");
  private JRadioButton rbResidNeighboring = new JRadioButton(
    "Neighboring views");
  private ButtonGroup bgResidualThreshold = new ButtonGroup();
  private JPanel pnlResidualThreshold = new JPanel();

  private JRadioButton rbSingleFiducialSurface = new JRadioButton(
    "Assume fiducials on 1 surface for analysis");
  private JRadioButton rbDualFiducialSurfaces = new JRadioButton(
    "Assume fiducials on 2 surfaces for analysis");
  private ButtonGroup bgFiducialSurfaces = new ButtonGroup();
  private JPanel pnlFiducialSurfaces = new JPanel();

  private LabeledTextField ltfExcludeList = new LabeledTextField(
    "List of views to exclude: ");
  private LabeledTextField ltfSeparateViewGroups = new LabeledTextField(
    "Separate view groups: ");

  private JPanel pnlVolumeParameters = new JPanel();
  private LabeledTextField ltfTiltAngleOffset = new LabeledTextField(
    "Total tilt angle offset: ");
  private LabeledTextField ltfTiltAxisZShift = new LabeledTextField(
    "Tilt axis z shift: ");

  private JPanel pnlMinimizationParams = new JPanel();
  private LabeledTextField ltfMetroFactor = new LabeledTextField(
    "Metro factor: ");
  private LabeledTextField ltfCycleLimit = new LabeledTextField("Cycle limit: ");

  private JPanel pnlLocalParameters = new JPanel();
  private JCheckBox cbLocalAlignments = new JCheckBox("Enable local alignments");
  private LabeledTextField ltfNLocalPatches = new LabeledTextField(
    "Number of local patches (x,y): ");
  private LabeledTextField ltfMinLocalPatchSize = new LabeledTextField(
    "Min. local patch size or overlap factor (x,y): ");
  private LabeledTextField ltfMinLocalFiducials = new LabeledTextField(
    "Min. number of fiducials (total, each surface): ");

  //  Global variables pane
  private JPanel pnlGlobalVariable = new JPanel();

  //  Tilt angle pane
  private JRadioButton rbTiltAngleFixed = new JRadioButton("Fixed tilt angles");
  private JRadioButton rbTiltAngleAll = new JRadioButton(
    "Solve for all except minimum tilt");
  private JRadioButton rbTiltAngleAutomap = new JRadioButton(
    "Group tilt angles ");
  private ButtonGroup bgTiltAngleSolution = new ButtonGroup();
  private JPanel pnlTiltAngleSolution = new JPanel();

  private LabeledTextField ltfTiltAngleGroupSize = new LabeledTextField(
    "Group size: ");
  private LabeledTextField ltfTiltAngleNonDefaultGroups = new LabeledTextField(
    "Non-default grouping: ");

  //  Magnfication pane
  private JRadioButton rbMagnificationFixed = new JRadioButton(
    "Fixed magnification at 1.0");
  private JRadioButton rbMagnificationAll = new JRadioButton(
    "Solve for all magnifications");
  private JRadioButton rbMagnificationAutomap = new JRadioButton(
    "Group magnifications");
  private ButtonGroup bgMagnificationSolution = new ButtonGroup();
  private JPanel pnlMagnificationSolution = new JPanel();

  private LabeledTextField ltfMagnificationReferenceView = new LabeledTextField(
    "Reference view: ");
  private LabeledTextField ltfMagnificationGroupSize = new LabeledTextField(
    "Group size: ");
  private LabeledTextField ltfMagnificationNonDefaultGroups = new LabeledTextField(
    "Non-default grouping: ");

  //  Compression pane
  /*  private JRadioButton rbCompressionAll =
   new JRadioButton("Solve for all Compressions");
   private JRadioButton rbCompressionAutomapLinear =
   new JRadioButton("Group compression (first order fit)");
   private JRadioButton rbCompressionAutomapFixed =
   new JRadioButton("Group compression (zeroth order fit)");
   private ButtonGroup bgCompressionSolution = new ButtonGroup();
   private JPanel pnlCompressionSolution = new JPanel();
   
   private LabeledTextField ltfCompressionReferenceView =
   new LabeledTextField("Compression reference view: ");
   private LabeledTextField ltfCompressionGroupSize =
   new LabeledTextField("Group size: ");
   private LabeledTextField ltfCompressionAdditionalGroups =
   new LabeledTextField("Additional group list: ");
   */
  // GlobalDistortion pane
  private JPanel pnlDistortionSolution = new JPanel();
  private JCheckBox cbDistortion = new JCheckBox("Enable");

  private LabeledTextField ltfXstretchGroupSize = new LabeledTextField(
    "X stretch group size: ");
  private LabeledTextField ltfXstretchNonDefaultGroups = new LabeledTextField(
    "X stretch non-default grouping: ");

  private LabeledTextField ltfSkewGroupSize = new LabeledTextField(
    "Skew group size: ");
  private LabeledTextField ltfSkewNonDefaultGroups = new LabeledTextField(
    "Skew non-default grouping: ");

  //  Local variables pane
  private JPanel pnlLocalSolution = new JPanel();

  //  Local tilt angle pane
  private JPanel pnlLocalRotationSolution = new JPanel();
  private JCheckBox cbLocalRotation = new JCheckBox("Enable");

  private LabeledTextField ltfLocalRotationGroupSize = new LabeledTextField(
    "Group size: ");
  private LabeledTextField ltfLocalRotationNonDefaultGroups = new LabeledTextField(
    "Non-default grouping: ");

  //  Local tilt angle pane
  private JPanel pnlLocalTiltAngleSolution = new JPanel();
  private JCheckBox cbLocalTiltAngle = new JCheckBox("Enable");

  private LabeledTextField ltfLocalTiltAngleGroupSize = new LabeledTextField(
    "Group size: ");
  private LabeledTextField ltfLocalTiltAngleNonDefaultGroups = new LabeledTextField(
    "Non-default grouping: ");

  // Local magnfication pane
  private JPanel pnlLocalMagnificationSolution = new JPanel();
  private JCheckBox cbLocalMagnification = new JCheckBox("Enable");

  private LabeledTextField ltfLocalMagnificationGroupSize = new LabeledTextField(
    "Group size: ");
  private LabeledTextField ltfLocalMagnificationNonDefaultGroups = new LabeledTextField(
    "Non-default grouping: ");

  //  Local distortion pane
  private JPanel pnlLocalDistortionSolution = new JPanel();
  private JCheckBox cbLocalDistortion = new JCheckBox("Enable");

  private LabeledTextField ltfLocalXstretchGroupSize = new LabeledTextField(
    "X stretch group size: ");
  private LabeledTextField ltfLocalXstretchNonDefaultGroups = new LabeledTextField(
    "X stretch non-default grouping: ");

  private LabeledTextField ltfLocalSkewGroupSize = new LabeledTextField(
    "Skew group size: ");
  private LabeledTextField ltfLocalSkewNonDefaultGroups = new LabeledTextField(
    "Skew non-default grouping: ");
    
  private FidXyz fidXyz = null;
  private MRCHeader rawstackHeader = null;
  private MRCHeader prealiHeader = null;

  /**
   * Constructor
   * @param axis
   */
  public TiltalignPanel(
    AxisID axis,
    FidXyz fidXyz,
    MRCHeader prealiHeader,
    MRCHeader rawstackHeader) {
    axisID = axis;
    this.fidXyz = fidXyz;
    this.prealiHeader = prealiHeader;
    this.rawstackHeader = rawstackHeader;

    tabPane.setBorder(new EtchedBorder("Tiltalign Parameters").getBorder());
    //  Create the tabs
    createGeneralTab();
    createGlobalSolutionTab();
    createLocalSolutionTab();
    setToolTipText();
  }

  /**
   * Set the values of the panel using a constant tiltalign parameter
   * object
   */
  public void setParameters(ConstTiltalignParam params) {
    try {
      fidXyz.read();
      rawstackHeader.read();
      prealiHeader.read();
    }
    catch (IOException except) {
      except.printStackTrace();
      return;
    }
    catch (InvalidParameterException except) {
      except.printStackTrace();
      return;
    }
    //  General panel parameters

    if (params.getNSurfaceAnalysis() == 2) {
      rbDualFiducialSurfaces.setSelected(true);
    }
    else {
      rbSingleFiducialSurface.setSelected(true);
    }

    ltfResidualThreshold.setText(Math.abs(params.getResidualThreshold()));
    if (params.getResidualThreshold() < 0) {
      rbResidNeighboring.setSelected(true);
    }
    else {
      rbResidAllViews.setSelected(true);
    }

    int excludeType = params.getIncludeExcludeType();
    if (excludeType == 0 || excludeType == 3) {
      ltfExcludeList.setEnabled(true);
      ltfExcludeList.setText(params.getIncludeExcludeList());
    }
    else {
      ltfExcludeList.setEnabled(false);
    }

    ltfSeparateViewGroups.setText(params.getSeparateViewGroups());
    ltfTiltAngleOffset.setText(params.getTiltAngleOffset());
    
    if (fidXyz.exists() && fidXyz.isEmpty()) {
      //if file exists but is empty, assume that tilt align failed and use
      //pre align instead
      //multiply by the binning previously used by pre align
      ltfTiltAxisZShift.setText(params.getTiltAxisZShift()
        * Math.round(prealiHeader.getXPixelSpacing() / rawstackHeader.getXPixelSpacing()));
    }
    else if (!fidXyz.exists() || !fidXyz.isPixelSizeSet()) {
      //if fidXyz.pixelSize could not be read from fid.xyz, then binning must
      //have been 1 the last time align.com was run.
      ltfTiltAxisZShift.setText(params.getTiltAxisZShift());
    }
    else {
      //multiply by the binning previously used by align.com
      ltfTiltAxisZShift.setText(params.getTiltAxisZShift()
        * Math.round(fidXyz.getPixelSize() / rawstackHeader.getXPixelSpacing()));
    }
      
    ltfMetroFactor.setText(params.getMetroFactor());
    ltfCycleLimit.setText(params.getCycleLimit());

    cbLocalAlignments.setSelected(params.getLocalAlignments());
    ltfNLocalPatches.setText(params.getNLocalPatches());
    ltfMinLocalPatchSize.setText(params.getMinLocalPatchSize());
    ltfMinLocalFiducials.setText(params.getMinLocalFiducials());

    //  Tilt angle solution parameters
    int solutionType = params.getTiltAngleSolutionType();
    if (solutionType == 0) {
      rbTiltAngleFixed.setSelected(true);
    }
    if (solutionType == 2) {
      rbTiltAngleAll.setSelected(true);
    }
    if (solutionType == 5) {
      rbTiltAngleAutomap.setSelected(true);
    }
    if (solutionType == 5) {
      ltfTiltAngleGroupSize.setText(params.getTiltAngleSolutionGroupSize());
      ltfTiltAngleNonDefaultGroups.setText(params
        .getTiltAngleSolutionAdditionalGroups());
    }

    //  Magnification solution parameters
    //  TODO what to do if the magnification type is not one of the cases
    //  below
    ltfMagnificationReferenceView.setText(params
      .getMagnificationSolutionReferenceView());
    solutionType = params.getMagnificationSolutionType();
    if (solutionType == 0) {
      rbMagnificationFixed.setSelected(true);
    }
    if (solutionType == 1) {
      rbMagnificationAll.setSelected(true);
    }
    if (solutionType == defaultMagnificationType) {
      rbMagnificationAutomap.setSelected(true);
    }

    if (solutionType > 2) {
      ltfMagnificationGroupSize.setText(params
        .getMagnificationSolutionGroupSize());
      ltfMagnificationNonDefaultGroups.setText(params
        .getMagnificationSolutionAdditionalGroups());
    }

    //  Compression solution parameters
    /*
     ltfCompressionReferenceView.setText(
     params.getCompressionSolutionReferenceView());
     solutionType = params.getCompressionSolutionType();
     if (solutionType == 1) {
     rbCompressionAll.setSelected(true);
     }
     if (solutionType == 3) {
     rbCompressionAutomapLinear.setSelected(true);
     }
     if (solutionType == 4) {
     rbCompressionAutomapFixed.setSelected(true);
     }
     
     if (solutionType > 2) {
     ltfCompressionGroupSize.setText(params.getCompressionSolutionGroupSize());
     ltfCompressionAdditionalGroups.setText(
     params.getCompressionSolutionAdditionalGroups());
     }
     */
    //  Global distortion solution type
    solutionType = params.getDistortionSolutionType();
    if (solutionType == 0) {
      cbDistortion.setSelected(false);
    }
    else {
      cbDistortion.setSelected(true);

      ltfXstretchGroupSize.setText(params.getXstretchSolutionGroupSize());
      ltfXstretchNonDefaultGroups.setText(params
        .getXstretchSolutionAdditionalGroups());

      //   skew solution parameters
      ltfSkewGroupSize.setText(params.getSkewSolutionGroupSize());
      ltfSkewNonDefaultGroups.setText(params.getSkewSolutionAdditionalGroups());
    }

    // Local rotation solution parameters
    // NOTE this is brittle since we are mapping a numeric value to a boolean
    // at David's request
    solutionType = params.getLocalRotationSolutionType();
    if (solutionType == 0) {
      cbLocalRotation.setSelected(false);
    }
    else {
      cbLocalRotation.setSelected(true);
      ltfLocalRotationGroupSize.setText(params
        .getLocalRotationSolutionGroupSize());
      ltfLocalTiltAngleNonDefaultGroups.setText(params
        .getLocalTiltAdditionalGroups());
    }

    // Local tilt angle solution parameters
    solutionType = params.getLocalTiltSolutionType();
    if (solutionType == 0) {
      cbLocalTiltAngle.setSelected(false);
    }
    else {
      cbLocalTiltAngle.setSelected(true);
      ltfLocalTiltAngleGroupSize
        .setText(params.getLocalTiltSolutionGroupSize());
      ltfLocalTiltAngleNonDefaultGroups.setText(params
        .getLocalTiltAdditionalGroups());
    }

    //  Local magnification solution parameters
    solutionType = params.getLocalMagnificationSolutionType();
    if (solutionType == 0) {
      cbLocalMagnification.setSelected(false);
    }
    else {
      cbLocalMagnification.setSelected(true);
      ltfLocalMagnificationGroupSize.setText(params
        .getLocalMagnificationSolutionGroupSize());
      ltfLocalMagnificationNonDefaultGroups.setText(params
        .getLocalMagnificationSolutionAdditionalGroups());
    }

    //  Local distortion solution type
    solutionType = params.getLocalDistortionSolutionType();
    if (solutionType == 0) {
      cbLocalDistortion.setSelected(false);
    }
    else {
      cbLocalDistortion.setSelected(true);

      ltfLocalXstretchGroupSize.setText(params
        .getLocalXstretchSolutionGroupSize());
      ltfLocalXstretchNonDefaultGroups.setText(params
        .getLocalXstretchSolutionAdditionalGroups());

      //  Local skew solution parameters
      ltfLocalSkewGroupSize.setText(params.getLocalSkewSolutionGroupSize());
      ltfLocalSkewNonDefaultGroups.setText(params
        .getLocalSkewSolutionAdditionalGroups());
    }

    //  Set the UI to match the data
    updateEnabled();
  }

  /**
   * Get the values from the panel by updating tiltalign parameter
   * object.  Currently this makes the assumption that the argument
   * contains valid parameters and that only the known parameters will
   * be changed.
   */
  public void getParameters(TiltalignParam params)
    throws FortranInputSyntaxException {
    try {
      prealiHeader.read();
      //raw stack won't change and doesn't really have to be read again
      rawstackHeader.read();
    }
    catch (IOException except) {
      except.printStackTrace();
      return;
    }
    catch (InvalidParameterException except) {
      except.printStackTrace();
      return;
    }
    String badParameter = "";
    try {
      if (rbDualFiducialSurfaces.isSelected()) {
        params.setNSurfaceAnalysis(2);
      }
      else {
        params.setNSurfaceAnalysis(1);
      }

      badParameter = ltfResidualThreshold.getLabel();
      double resid = Double.parseDouble(ltfResidualThreshold.getText());
      if (rbResidNeighboring.isSelected()) {
        resid *= -1;
      }
      params.setResidualThreshold(resid);

      //  Currently only supports Exclude list or blank entries
      badParameter = ltfExcludeList.getLabel();
      if (ltfExcludeList.isEnabled()) {
        StringList temp = new StringList(0);
        temp.parseString(ltfExcludeList.getText());
        if (temp.getNElements() > 0) {
          params.setIncludeExcludeType(3);
        }
        else {
          params.setIncludeExcludeType(0);
        }
        params.setIncludeExcludeList(temp.toString());

      }
      badParameter = ltfSeparateViewGroups.getLabel();
      params.setSeparateViewGroups(ltfSeparateViewGroups.getText());

      badParameter = ltfTiltAngleOffset.getLabel();
      params.setTiltAngleOffset(ltfTiltAngleOffset.getText());

      badParameter = ltfTiltAxisZShift.getLabel();
      
      //divide by the binning used to create the .preali file
      params.setTiltAxisZShift(
        Double.parseDouble(ltfTiltAxisZShift.getText())
          / Math.round(
            prealiHeader.getXPixelSpacing()
              / rawstackHeader.getXPixelSpacing()));

      badParameter = ltfMetroFactor.getLabel();
      params.setMetroFactor(ltfMetroFactor.getText());

      badParameter = ltfCycleLimit.getLabel();
      params.setCycleLimit(ltfCycleLimit.getText());

      badParameter = cbLocalAlignments.getText();
      params.setLocalAlignments(cbLocalAlignments.isSelected());

      badParameter = ltfNLocalPatches.getLabel();
      params.setNLocalPatches(ltfNLocalPatches.getText());

      badParameter = ltfMinLocalPatchSize.getLabel();
      params.setMinLocalPatchSize(ltfMinLocalPatchSize.getText());

      badParameter = ltfMinLocalFiducials.getLabel();
      params.setMinLocalFiducials(ltfMinLocalFiducials.getText());

      // Tilt angle pane
      int type = 0;
      if (rbTiltAngleFixed.isSelected())
        type = 0;
      if (rbTiltAngleAll.isSelected())
        type = 2;
      if (rbTiltAngleAutomap.isSelected())
        type = 5;
      params.setTiltAngleSolutionType(type);
      if (type > 2) {
        badParameter = ltfTiltAngleGroupSize.getLabel();
        params.setTiltAngleSolutionGroupSize(ltfTiltAngleGroupSize.getText());

        badParameter = ltfTiltAngleNonDefaultGroups.getLabel();
        params
          .setTiltAngleSolutionAdditionalGroups(ltfTiltAngleNonDefaultGroups
            .getText());
      }

      // Magnification pane
      badParameter = ltfMagnificationReferenceView.getLabel();
      params.setMagnificationReferenceView(ltfMagnificationReferenceView
        .getText());

      if (rbMagnificationFixed.isSelected())
        type = 0;
      if (rbMagnificationAll.isSelected())
        type = 1;
      if (rbMagnificationAutomap.isSelected())
        type = defaultMagnificationType;
      params.setMagnificationType(type);

      if (type > 2) {
        badParameter = ltfMagnificationGroupSize.getLabel();
        params.setMagnificationSolutionGroupSize(ltfMagnificationGroupSize
          .getText());

        badParameter = ltfMagnificationNonDefaultGroups.getLabel();
        params
          .setMagnificationSolutionAdditionalGroups(ltfMagnificationNonDefaultGroups
            .getText());
      }

      // Compression pane
      /*      badParameter = ltfCompressionReferenceView.getLabel();
       params.setCompressionReferenceView(ltfCompressionReferenceView.getText());
       
       if (rbCompressionAll.isSelected())
       type = 1;
       if (rbCompressionAutomapLinear.isSelected())
       type = 3;
       if (rbCompressionAutomapFixed.isSelected())
       type = 4;
       params.setCompressionType(type);
       
       if (type > 2) {
       badParameter = ltfCompressionGroupSize.getLabel();
       params.setCompressionSolutionGroupSize(
       ltfCompressionGroupSize.getText());
       
       badParameter = ltfCompressionAdditionalGroups.getLabel();
       params.setCompressionSolutionAdditionalGroups(
       ltfCompressionAdditionalGroups.getText());
       }
       */

      // Distortion pane
      type = 0;
      if (cbDistortion.isSelected()) {
        //  Set the necessary types for distortion xstretch and skew
        params.setDistortionSolutionType(defaultDistortionType);
        params.setXstretchType(defaultXstretchType);
        params.setSkewType(defaultSkewType);

        badParameter = ltfXstretchGroupSize.getLabel();
        params.setXstretchSolutionGroupSize(ltfXstretchGroupSize.getText());

        badParameter = ltfXstretchNonDefaultGroups.getLabel();
        params.setXstretchSolutionAdditionalGroups(ltfXstretchNonDefaultGroups
          .getText());

        badParameter = ltfSkewGroupSize.getLabel();
        params.setSkewSolutionGroupSize(ltfSkewGroupSize.getText());

        badParameter = ltfSkewNonDefaultGroups.getLabel();
        params.setSkewSolutionAdditionalGroups(ltfSkewNonDefaultGroups
          .getText());
      }
      else {
        params.setDistortionSolutionType(0);
      }

      //  Get the local alignment parameters
      // Rotation pane
      // NOTE this only works if 0 and 5 are valid local tilt angle codes
      type = 0;
      if (cbLocalRotation.isSelected())
        type = defaultLocalRotationType;
      params.setLocalRotationSolutionType(type);

      if (type == defaultLocalRotationType) {
        badParameter = ltfLocalRotationGroupSize.getLabel();
        params.setLocalRotationSolutionGroupSize(ltfLocalRotationGroupSize
          .getText());

        badParameter = ltfLocalRotationNonDefaultGroups.getLabel();
        params
          .setLocalRotationSolutionAdditionalGroups(ltfLocalRotationNonDefaultGroups
            .getText());
      }

      // Tilt angle pane
      type = 0;
      if (cbLocalTiltAngle.isSelected())
        type = defaultLocalTiltAngleType;
      params.setLocalTiltSolutionType(type);

      if (type == defaultLocalTiltAngleType) {
        badParameter = ltfLocalTiltAngleGroupSize.getLabel();
        params.setLocalTiltSolutionGroupSize(ltfLocalTiltAngleGroupSize
          .getText());

        badParameter = ltfLocalTiltAngleNonDefaultGroups.getLabel();
        params
          .setLocalTiltSolutionAdditionalGroups(ltfLocalTiltAngleNonDefaultGroups
            .getText());
      }

      // Local magnification pane
      if (cbLocalMagnification.isSelected()) {
        params.setLocalMagnificationType(defaultLocalMagnificationType);
        badParameter = ltfLocalMagnificationGroupSize.getLabel();
        params
          .setLocalMagnificationSolutionGroupSize(ltfLocalMagnificationGroupSize
            .getText());

        badParameter = ltfLocalMagnificationNonDefaultGroups.getLabel();
        params
          .setLocalMagnificationSolutionAdditionalGroups(ltfLocalMagnificationNonDefaultGroups
            .getText());

      }
      else {
        params.setLocalMagnificationType(0);
      }

      // Distortion pane
      type = 0;
      if (cbLocalDistortion.isSelected()) {
        params.setLocalDistortionSolutionType(defaultLocalDistortionType);
        params.setLocalXstretchType(defaultLocalXstretchType);
        params.setLocalSkewType(defaultLocalXstretchType);

        badParameter = ltfLocalXstretchGroupSize.getLabel();
        params.setLocalXstretchSolutionGroupSize(ltfLocalXstretchGroupSize
          .getText());

        badParameter = ltfLocalXstretchNonDefaultGroups.getLabel();
        params
          .setLocalXstretchSolutionAdditionalGroups(ltfLocalXstretchNonDefaultGroups
            .getText());

        badParameter = ltfLocalSkewGroupSize.getLabel();
        params.setLocalSkewSolutionGroupSize(ltfLocalSkewGroupSize.getText());

        badParameter = ltfLocalSkewNonDefaultGroups.getLabel();
        params
          .setLocalSkewSolutionAdditionalGroups(ltfLocalSkewNonDefaultGroups
            .getText());
      }
      else {
        params.setLocalDistortionSolutionType(0);
      }
    }
    catch (FortranInputSyntaxException except) {
      String message = badParameter + " " + except.getMessage();
      throw new FortranInputSyntaxException(message);
    }
    catch (NumberFormatException except) {
      String message = badParameter + " " + except.getMessage();
      throw new NumberFormatException(message);
    }
  }

  /**
   * Make the panel visible
   * @param state
   */
  void setVisible(boolean state) {
    pnlGeneral.setVisible(state);
  }

  /**
   *
   */
  void setLargestTab() {
    //tabPane.setSelectedComponent(pnlGlobalVariable);
    tabPane.setSelectedComponent(pnlLocalSolution);
  }

  void setFirstTab() {
    tabPane.setSelectedComponent(pnlGeneral);
  }

  void setAdvanced(boolean state) {

    //    ltfMetroFactor.setVisible(state);
    //    ltfCycleLimit.setVisible(state);
    pnlMinimizationParams.setVisible(state);
    ltfMagnificationReferenceView.setVisible(state);
    ltfTiltAngleNonDefaultGroups.setVisible(state);
    ltfMagnificationNonDefaultGroups.setVisible(state);
    ltfXstretchNonDefaultGroups.setVisible(state);
    ltfSkewNonDefaultGroups.setVisible(state);
    ltfLocalRotationNonDefaultGroups.setVisible(state);
    ltfLocalTiltAngleNonDefaultGroups.setVisible(state);
    ltfLocalMagnificationNonDefaultGroups.setVisible(state);
    ltfLocalXstretchNonDefaultGroups.setVisible(state);
    ltfLocalSkewNonDefaultGroups.setVisible(state);
    ltfMinLocalPatchSize.setVisible(state);
  }

  void selectGlobalDistortion() {
    if (cbDistortion.isSelected()) {
      cbLocalDistortion.setSelected(true);
      setDistortionDefaults();
      setLocalDistortionDefaults();
    }
    else {
      cbLocalDistortion.setSelected(false);
      setTiltAndMagnificationDefaults();
    }
    updateEnabled();
  }

  void selectLocalDistortion() {
    if (cbLocalDistortion.isSelected()) {
      setLocalDistortionDefaults();
    }
    updateEnabled();
  }

  /**
   * Set the UI parameters to the defaults for a tilt/mag solution.
   */
  void setTiltAndMagnificationDefaults() {
    rbTiltAngleAll.setSelected(true);
    cbDistortion.setSelected(false);
    cbLocalDistortion.setSelected(false);
  }

  /**
   * Set the UI parameters to the default for a distortion solution.  If the
   * group size and additional group lists do not contain any text set them
   * to the defaults.
   */
  void setDistortionDefaults() {
    rbTiltAngleAutomap.setSelected(true);
    if (ltfTiltAngleGroupSize.getText().matches("^\\s*$")) {
      ltfTiltAngleGroupSize.setText(defaultTiltAngleGroupSize);
    }

    cbDistortion.setSelected(true);
    cbLocalDistortion.setSelected(true);

    // If any of the size fields are empty fill them in with the defaults
    // This will happen if someone starts with a com file with distortion
    // disabled and then enables distortion
    if (ltfXstretchGroupSize.getText().matches("^\\s*$")) {
      ltfXstretchGroupSize.setText(defaultXstretchGroupSize);
    }
    if (ltfSkewGroupSize.getText().matches("^\\s*$")) {
      ltfSkewGroupSize.setText(defaultSkewGroupSize);
    }
  }

  void setLocalDistortionDefaults() {
    // If any of the size fields are empty fill them in with the defaults
    // This will happen if someone starts with a com file with distortion
    // disabled and then enables distortion
    if (ltfLocalXstretchGroupSize.getText().matches("^\\s*$")) {
      ltfLocalXstretchGroupSize.setText(defaultLocalXstretchGroupSize);
    }
    if (ltfLocalSkewGroupSize.getText().matches("^\\s*$")) {
      ltfLocalSkewGroupSize.setText(defaultLocalSkewGroupSize);
    }
  }

  // Residual solution panel, nothing much to do.  This is here so that
  // this section matches the other's pattern
  void updateResidualSolutionPanel() {

  }

  // Fiducial solution radio buttons, nothing much to do.  This is here so that
  // this section matches the other's pattern
  void updateFiducialSolutionPanel() {

  }

  //  Local alignment state
  void updateLocalAlignmentState() {
    boolean state = cbLocalAlignments.isSelected();
    ltfNLocalPatches.setEnabled(state);
    ltfMinLocalPatchSize.setEnabled(state);
    ltfMinLocalFiducials.setEnabled(state);
    tabPane.setEnabledAt(tabPane.indexOfComponent(pnlLocalSolution), state);
  }

  /**
   * Signal each pane to update its enabled/disabled state.
   */
  public void updateEnabled() {
    //  update all of the enable/disable states
    updateLocalAlignmentState();

    updateTiltAngleSolutionPanel();
    updateMagnificationSolutionPanel();
    //    updateCompressionSolutionPanel();
    updateDistortionSolutionPanel();

    updateLocalRotationSolutionPanel();
    updateLocalTiltAngleSolutionPanel();
    updateLocalMagnificationSolutionPanel();
    updateLocalDistortionSolutionPanel();
  }

  /**
   * Update the enabled/disabled state of the specified solution panel.
   */
  void updateTiltAngleSolutionPanel() {
    boolean state = rbTiltAngleAutomap.isSelected();
    ltfTiltAngleGroupSize.setEnabled(state);
    ltfTiltAngleNonDefaultGroups.setEnabled(state);
  }

  void updateMagnificationSolutionPanel() {
    boolean state = rbMagnificationAutomap.isSelected();
    ltfMagnificationGroupSize.setEnabled(state);
    ltfMagnificationNonDefaultGroups.setEnabled(state);
  }

  /*
   void updateCompressionSolutionPanel() {
   boolean state =
   rbCompressionAutomapLinear.isSelected()
   || rbCompressionAutomapFixed.isSelected();
   ltfCompressionGroupSize.setEnabled(state);
   ltfCompressionAdditionalGroups.setEnabled(state);
   }
   */

  void updateDistortionSolutionPanel() {
    //  Xstretch and skew panel state
    boolean state = cbDistortion.isSelected();
    ltfXstretchGroupSize.setEnabled(state);
    ltfXstretchNonDefaultGroups.setEnabled(state);
    ltfSkewGroupSize.setEnabled(state);
    ltfSkewNonDefaultGroups.setEnabled(state);
  }

  void updateLocalRotationSolutionPanel() {
    boolean state = cbLocalRotation.isSelected();
    ltfLocalRotationGroupSize.setEnabled(state);
    ltfLocalRotationNonDefaultGroups.setEnabled(state);
  }

  void updateLocalTiltAngleSolutionPanel() {
    boolean state = cbLocalTiltAngle.isSelected();
    ltfLocalTiltAngleGroupSize.setEnabled(state);
    ltfLocalTiltAngleNonDefaultGroups.setEnabled(state);
  }

  void updateLocalMagnificationSolutionPanel() {
    boolean state = cbLocalMagnification.isSelected();
    ltfLocalMagnificationGroupSize.setEnabled(state);
    ltfLocalMagnificationNonDefaultGroups.setEnabled(state);
  }

  void updateLocalDistortionSolutionPanel() {
    boolean state = cbLocalDistortion.isSelected();
    ltfLocalXstretchGroupSize.setEnabled(state);
    ltfLocalXstretchNonDefaultGroups.setEnabled(state);
    ltfLocalSkewGroupSize.setEnabled(state);
    ltfLocalSkewNonDefaultGroups.setEnabled(state);
  }

  Container getContainer() {
    return tabPane;
  }

  /**
   * 
   * @param panel
   * @param group
   * @param items
   * @param listener
   */
  private void createRadioBox(JPanel panel, ButtonGroup group,
    JRadioButton[] items, ActionListener listener) {
    int width = 300;
    int radioButtonHeight = 18;
    Dimension radioButtonItemSize = new Dimension(width, radioButtonHeight);

    // Add the items to the group and to the panel
    for (int i = 0; i < items.length; i++) {
      group.add(items[i]);
      panel.add(items[i]);
      items[i].addActionListener(listener);
      items[i].setPreferredSize(radioButtonItemSize);
    }

  }

  /**
   * Layout the general parameters tab
   */
  private void createGeneralTab() {

    pnlGeneral.setLayout(new BoxLayout(pnlGeneral, BoxLayout.Y_AXIS));
    pnlGeneral.add(Box.createRigidArea(FixedDim.x0_y5));

    pnlGeneral.add(ltfExcludeList.getContainer());
    pnlGeneral.add(Box.createRigidArea(FixedDim.x0_y5));

    pnlGeneral.add(ltfSeparateViewGroups.getContainer());
    pnlGeneral.add(Box.createRigidArea(FixedDim.x0_y10));

    pnlResidualThreshold.setLayout(new BoxLayout(pnlResidualThreshold,
      BoxLayout.X_AXIS));
    pnlResidualThreshold.setBorder(new EtchedBorder("Residual Reporting")
      .getBorder());
    ltfResidualThreshold.setColumns(10);
    pnlResidualThreshold.add(ltfResidualThreshold.getContainer());
    pnlResidualThreshold.add(new JLabel(" s.d. relative to   "));
    JRadioButton[] items = new JRadioButton[2];
    items[0] = rbResidAllViews;
    items[1] = rbResidNeighboring;
    JPanel pnlRBResidual = new JPanel();
    pnlRBResidual.setLayout(new BoxLayout(pnlRBResidual, BoxLayout.Y_AXIS));

    ResidualRadioListener residualRadioListener = new ResidualRadioListener(
      this);
    createRadioBox(pnlRBResidual, bgResidualThreshold, items,
      residualRadioListener);
    pnlResidualThreshold.add(pnlRBResidual);

    pnlGeneral.add(pnlResidualThreshold);
    pnlGeneral.add(Box.createRigidArea(FixedDim.x0_y10));

    pnlFiducialSurfaces.setLayout(new BoxLayout(pnlFiducialSurfaces,
      BoxLayout.X_AXIS));
    pnlFiducialSurfaces
      .setBorder(new EtchedBorder("Analysis of Surface Angles").getBorder());

    //  Need an extra panel to make border extend the appropriate width
    JPanel pnlRBFiducual = new JPanel();
    pnlRBFiducual.setLayout(new BoxLayout(pnlRBFiducual, BoxLayout.Y_AXIS));
    items = new JRadioButton[2];
    items[0] = rbSingleFiducialSurface;
    items[1] = rbDualFiducialSurfaces;
    FiducialRadioListener fiducialRadioListener = new FiducialRadioListener(
      this);
    createRadioBox(pnlRBFiducual, bgFiducialSurfaces, items,
      fiducialRadioListener);

    pnlFiducialSurfaces.add(pnlRBFiducual);
    pnlFiducialSurfaces.add(Box.createHorizontalGlue());
    pnlGeneral.add(pnlFiducialSurfaces);
    pnlGeneral.add(Box.createRigidArea(FixedDim.x0_y10));

    pnlVolumeParameters.setLayout(new BoxLayout(pnlVolumeParameters,
      BoxLayout.Y_AXIS));
    pnlVolumeParameters
      .setBorder(new EtchedBorder("Volume Position Parameters").getBorder());
    pnlVolumeParameters.add(ltfTiltAngleOffset.getContainer());
    pnlVolumeParameters.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlVolumeParameters.add(ltfTiltAxisZShift.getContainer());
    pnlVolumeParameters.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlGeneral.add(pnlVolumeParameters);
    pnlGeneral.add(Box.createRigidArea(FixedDim.x0_y10));

    pnlMinimizationParams.setLayout(new BoxLayout(pnlMinimizationParams,
      BoxLayout.Y_AXIS));
    pnlMinimizationParams.setBorder(new EtchedBorder("Minimization Parameters")
      .getBorder());
    pnlMinimizationParams.add(ltfMetroFactor.getContainer());
    pnlMinimizationParams.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlMinimizationParams.add(ltfCycleLimit.getContainer());
    pnlMinimizationParams.add(Box.createRigidArea(FixedDim.x0_y5));

    pnlGeneral.add(pnlMinimizationParams);
    pnlGeneral.add(Box.createRigidArea(FixedDim.x0_y10));

    pnlLocalParameters.setLayout(new BoxLayout(pnlLocalParameters,
      BoxLayout.Y_AXIS));
    pnlLocalParameters.setBorder(new EtchedBorder("Local Alignment Parameters")
      .getBorder());
    pnlLocalParameters.add(cbLocalAlignments);
    cbLocalAlignments.setAlignmentX(Container.RIGHT_ALIGNMENT);
    pnlLocalParameters.add(Box.createRigidArea(FixedDim.x0_y5));
    LocalAlignmentsListener localAlignmentsListener = new LocalAlignmentsListener(
      this);
    cbLocalAlignments.addActionListener(localAlignmentsListener);

    pnlLocalParameters.add(ltfNLocalPatches.getContainer());
    pnlLocalParameters.add(Box.createRigidArea(FixedDim.x0_y5));

    pnlLocalParameters.add(ltfMinLocalPatchSize.getContainer());
    pnlLocalParameters.add(Box.createRigidArea(FixedDim.x0_y5));

    pnlLocalParameters.add(ltfMinLocalFiducials.getContainer());
    pnlGeneral.add(pnlLocalParameters);
    pnlGeneral.add(Box.createVerticalGlue());

    tabPane.addTab("General", pnlGeneral);
  }

  /**
   * Layout the global estimate tab
   */
  private void createGlobalSolutionTab() {
    pnlGlobalVariable.setLayout(new BoxLayout(pnlGlobalVariable,
      BoxLayout.Y_AXIS));

    //  Layout the global tilt angle estimate pane
    pnlTiltAngleSolution.setLayout(new BoxLayout(pnlTiltAngleSolution,
      BoxLayout.Y_AXIS));

    JRadioButton[] items = new JRadioButton[3];
    items[0] = rbTiltAngleFixed;
    items[1] = rbTiltAngleAll;
    items[2] = rbTiltAngleAutomap;
    TiltAngleRadioListener tiltAngleRadioListener = new TiltAngleRadioListener(
      this);
    createRadioBox(pnlTiltAngleSolution, bgTiltAngleSolution, items,
      tiltAngleRadioListener);
    pnlTiltAngleSolution.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlTiltAngleSolution.add(ltfTiltAngleGroupSize.getContainer());
    pnlTiltAngleSolution.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlTiltAngleSolution.add(ltfTiltAngleNonDefaultGroups.getContainer());

    pnlTiltAngleSolution.setBorder(new EtchedBorder("Tilt Angle Solution Type")
      .getBorder());

    //  Layout the global magnification variable parameters
    pnlMagnificationSolution.setLayout(new BoxLayout(pnlMagnificationSolution,
      BoxLayout.Y_AXIS));
    items = new JRadioButton[3];
    items[0] = rbMagnificationFixed;
    items[1] = rbMagnificationAll;
    items[2] = rbMagnificationAutomap;
    MagnificationRadioListener magnificationRadioListener = new MagnificationRadioListener(
      this);
    createRadioBox(pnlMagnificationSolution, bgMagnificationSolution, items,
      magnificationRadioListener);

    pnlMagnificationSolution.add(ltfMagnificationReferenceView.getContainer());
    pnlMagnificationSolution.add(Box.createRigidArea(FixedDim.x0_y5));

    pnlMagnificationSolution.add(ltfMagnificationGroupSize.getContainer());
    pnlMagnificationSolution.add(Box.createRigidArea(FixedDim.x0_y5));

    pnlMagnificationSolution.add(ltfMagnificationNonDefaultGroups
      .getContainer());
    pnlMagnificationSolution.setBorder(new EtchedBorder(
      "Magnification Solution Type").getBorder());

    // Layout the global distortion pane
    createVariablePanel(pnlDistortionSolution, cbDistortion,
      ltfXstretchGroupSize, ltfXstretchNonDefaultGroups,
      "Distortion Solution Type");

    pnlDistortionSolution.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlDistortionSolution.add(ltfSkewGroupSize.getContainer());

    pnlDistortionSolution.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlDistortionSolution.add(ltfSkewNonDefaultGroups.getContainer());

    DistortionCheckListener DistortionCheckListener = new DistortionCheckListener(
      this);
    cbDistortion.addActionListener(DistortionCheckListener);

    //  Add the individual panes to the tab
    pnlGlobalVariable.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlGlobalVariable.add(pnlTiltAngleSolution);
    pnlGlobalVariable.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlGlobalVariable.add(Box.createVerticalGlue());
    pnlGlobalVariable.add(pnlMagnificationSolution);
    pnlGlobalVariable.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlGlobalVariable.add(Box.createVerticalGlue());
    pnlGlobalVariable.add(pnlDistortionSolution);

    tabPane.addTab("Global Variables", pnlGlobalVariable);

  }

  /*  private void createCompressionTab() {
   //  Compression solution
   //      ltfCompressionReferenceView.setMaximumSize(dimLTF);
   pnlCompressionSolution.add(ltfCompressionReferenceView.getContainer());
   JRadioButton[] items = new JRadioButton[3];
   items[0] = rbCompressionAll;
   items[1] = rbCompressionAutomapLinear;
   items[2] = rbCompressionAutomapFixed;
   CompressionRadioListener compressionRadioListener =
   new CompressionRadioListener(this);
   createRadioBox(
   pnlCompressionSolution,
   bgCompressionSolution,
   items,
   "Compression solution type",
   compressionRadioListener);
   
   pnlCompressionSolution.add(ltfCompressionGroupSize.getContainer());
   pnlCompressionSolution.add(ltfCompressionAdditionalGroups.getContainer());
   pnlCompressionSolution.add(Box.createRigidArea(FixedDim.x0_y5));
   pnlCompressionSolution.add(Box.createVerticalGlue());
   
   }
   */

  private void createLocalSolutionTab() {
    //  Construct the local solution panel
    pnlLocalSolution
      .setLayout(new BoxLayout(pnlLocalSolution, BoxLayout.Y_AXIS));
    //pnlLocalSolution.setPreferredSize(new Dimension(400, 350));

    //  Construct the rotation solution objects
    createVariablePanel(pnlLocalRotationSolution, cbLocalRotation,
      ltfLocalRotationGroupSize, ltfLocalRotationNonDefaultGroups,
      "Local Rotation Solution Type");
    LocalRotationCheckListener localRotationCheckListener = new LocalRotationCheckListener(
      this);
    cbLocalRotation.addActionListener(localRotationCheckListener);

    //  Construct the tilt angle solution objects
    createVariablePanel(pnlLocalTiltAngleSolution, cbLocalTiltAngle,
      ltfLocalTiltAngleGroupSize, ltfLocalTiltAngleNonDefaultGroups,
      "Local Tilt Angle Aolution Type");

    LocalTiltAngleCheckListener localTiltAngleCheckListener = new LocalTiltAngleCheckListener(
      this);
    cbLocalTiltAngle.addActionListener(localTiltAngleCheckListener);

    //  Construct the local magnification pane
    createVariablePanel(pnlLocalMagnificationSolution, cbLocalMagnification,
      ltfLocalMagnificationGroupSize, ltfLocalMagnificationNonDefaultGroups,
      "Local Magnification Solution Type");

    LocalMagnificationCheckListener localMagnificationCheckListener = new LocalMagnificationCheckListener(
      this);
    cbLocalMagnification.addActionListener(localMagnificationCheckListener);

    //  Construction the local distortion pane
    createVariablePanel(pnlLocalDistortionSolution, cbLocalDistortion,
      ltfLocalXstretchGroupSize, ltfLocalXstretchNonDefaultGroups,
      "Local Distortion Solution Type");

    pnlLocalDistortionSolution.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlLocalDistortionSolution.add(ltfLocalSkewGroupSize.getContainer());
    pnlLocalDistortionSolution.add(Box.createRigidArea(FixedDim.x0_y5));
    pnlLocalDistortionSolution.add(ltfLocalSkewNonDefaultGroups.getContainer());

    LocalDistortionCheckListener localDistortionCheckListener = new LocalDistortionCheckListener(
      this);
    cbLocalDistortion.addActionListener(localDistortionCheckListener);

    pnlLocalSolution.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlLocalSolution.add(pnlLocalRotationSolution);

    pnlLocalSolution.add(Box.createVerticalGlue());
    pnlLocalSolution.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlLocalSolution.add(pnlLocalTiltAngleSolution);

    pnlLocalSolution.add(Box.createVerticalGlue());
    pnlLocalSolution.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlLocalSolution.add(pnlLocalMagnificationSolution);

    pnlLocalSolution.add(Box.createVerticalGlue());
    pnlLocalSolution.add(Box.createRigidArea(FixedDim.x0_y10));
    pnlLocalSolution.add(pnlLocalDistortionSolution);

    tabPane.addTab("Local Variables", pnlLocalSolution);
  }

  private void createVariablePanel(JPanel panel, JCheckBox checkBox,
    LabeledTextField groupSize, LabeledTextField additionalGroups, String title) {

    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    //  Add the check box
    //    checkBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(checkBox);

    //  Add the group size labeled text field
    //    groupSize.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(groupSize.getContainer());

    //  Add the additional groups labeled text field
    panel.add(Box.createRigidArea(FixedDim.x0_y5));
    //    additionalGroups.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(additionalGroups.getContainer());

    panel.setBorder(new EtchedBorder(title).getBorder());
  }
  class ResidualRadioListener implements ActionListener {
    TiltalignPanel panel;

    ResidualRadioListener(TiltalignPanel adaptee) {
      panel = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      panel.updateResidualSolutionPanel();
    }
  }

  class FiducialRadioListener implements ActionListener {
    TiltalignPanel panel;

    FiducialRadioListener(TiltalignPanel adaptee) {
      panel = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      panel.updateFiducialSolutionPanel();
    }
  }

  class TiltAngleRadioListener implements ActionListener {
    TiltalignPanel panel;

    TiltAngleRadioListener(TiltalignPanel adaptee) {
      panel = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      panel.updateTiltAngleSolutionPanel();
    }
  }

  class MagnificationRadioListener implements ActionListener {
    TiltalignPanel panel;

    MagnificationRadioListener(TiltalignPanel adaptee) {
      panel = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      panel.updateMagnificationSolutionPanel();
    }
  }

  class DistortionCheckListener implements ActionListener {
    TiltalignPanel panel;

    DistortionCheckListener(TiltalignPanel adaptee) {
      panel = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      panel.selectGlobalDistortion();
    }
  }

  class LocalAlignmentsListener implements ActionListener {
    TiltalignPanel panel;

    LocalAlignmentsListener(TiltalignPanel adaptee) {
      panel = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      panel.updateLocalAlignmentState();
    }
  }

  class LocalRotationCheckListener implements ActionListener {
    TiltalignPanel panel;

    LocalRotationCheckListener(TiltalignPanel adaptee) {
      panel = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      panel.updateLocalRotationSolutionPanel();
    }
  }

  class LocalTiltAngleCheckListener implements ActionListener {
    TiltalignPanel panel;

    LocalTiltAngleCheckListener(TiltalignPanel adaptee) {
      panel = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      panel.updateLocalTiltAngleSolutionPanel();
    }
  }

  class LocalMagnificationCheckListener implements ActionListener {
    TiltalignPanel panel;

    LocalMagnificationCheckListener(TiltalignPanel adaptee) {
      panel = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      panel.updateLocalMagnificationSolutionPanel();
    }
  }

  class LocalDistortionCheckListener implements ActionListener {
    TiltalignPanel panel;

    LocalDistortionCheckListener(TiltalignPanel adaptee) {
      panel = adaptee;
    }

    public void actionPerformed(ActionEvent event) {
      panel.selectLocalDistortion();
    }
  }

  /*
   class CompressionRadioListener implements ActionListener {
   TiltalignPanel panel;
   
   CompressionRadioListener(TiltalignPanel adaptee) {
   panel = adaptee;
   }
   public void actionPerformed(ActionEvent event) {
   panel.updateCompressionSolutionPanel();
   }
   }
   */

  /**
   * Initialize the tooltip text for the axis panel objects
   */
  private void setToolTipText() {
    String text;
    TooltipFormatter tooltipFormatter = new TooltipFormatter();

    // General tab
    text = "List of views to exclude from alignment and reconstruction.";
    ltfExcludeList.setToolTipText(tooltipFormatter.setText(text).format());
    text = "Lists of views to group separately from other views.  Multiple "
      + "lists can be entered; separate them by spaces.";
    ltfSeparateViewGroups.setToolTipText(tooltipFormatter.setText(text)
      .format());
    text = "Threshold number of SDs above mean for reporting large residuals.";
    ltfResidualThreshold
      .setToolTipText(tooltipFormatter.setText(text).format());

    text = "Apply criterion relative to mean/SD of residuals on all views.";
    rbResidAllViews.setToolTipText(tooltipFormatter.setText(text).format());
    text = "Apply criterion relative to mean/SD of residuals on neighboring views.";
    rbResidNeighboring.setToolTipText(tooltipFormatter.setText(text).format());

    text = "Fit one plane to all points to find angles of section.";
    rbSingleFiducialSurface.setToolTipText(tooltipFormatter.setText(text)
      .format());

    text = "Divide points into two groups and fit two planes to find angles of "
      + "section.";
    rbDualFiducialSurfaces.setToolTipText(tooltipFormatter.setText(text)
      .format());

    text = "Total amount to add to all tilt angles.";
    ltfTiltAngleOffset.setToolTipText(tooltipFormatter.setText(text).format());

    text = "Distance to shift tilt axis in Z for reconstruction.";
    ltfTiltAxisZShift.setToolTipText(tooltipFormatter.setText(text).format());

    text = "A step size factor; try changing by +/-10% if solutions fail.";
    ltfMetroFactor.setToolTipText(tooltipFormatter.setText(text).format());

    text = "Limit on number of iterations to find a solution.";
    ltfCycleLimit.setToolTipText(tooltipFormatter.setText(text).format());

    text = "Compute alignments in local areas after finding global solution.";
    cbLocalAlignments.setToolTipText(tooltipFormatter.setText(text).format());

    text = "Number of overlapping local areas to use in the X and Y directions.";
    ltfNLocalPatches.setToolTipText(tooltipFormatter.setText(text).format());

    text = "Minimum size of patches in pixels, or minimum fractional overlap between"
      + " patches, in the X and Y directions.";
    ltfMinLocalPatchSize
      .setToolTipText(tooltipFormatter.setText(text).format());

    text = "Minimum total number of fiducials required in each local area, and "
      + "minimum on each surface if two surfaces were analyzed for.";
    ltfMinLocalFiducials
      .setToolTipText(tooltipFormatter.setText(text).format());

    //  Global variables
    text = "Do not solve for tilt angles.";
    rbTiltAngleFixed.setToolTipText(tooltipFormatter.setText(text).format());

    text = "Solve for each tilt angle independently.";
    rbTiltAngleAll.setToolTipText(tooltipFormatter.setText(text).format());

    text = "Group views to solve for fewer tilt angle variables.";
    rbTiltAngleAutomap.setToolTipText(tooltipFormatter.setText(text).format());

    text = "Basic grouping size for tilt angles (grouping will be less at high tilt"
      + " and more at low tilt).";
    ltfTiltAngleGroupSize.setToolTipText(tooltipFormatter.setText(text)
      .format());

    text = "Sets of views with non-default grouping.  For each set, enter starting"
      + " and ending view number and group size, separated by commas; "
      + "separate multiple sets with spaces.";
    ltfTiltAngleNonDefaultGroups.setToolTipText(tooltipFormatter.setText(text)
      .format());

    text = "Do not solve for magnifications.";
    rbMagnificationFixed
      .setToolTipText(tooltipFormatter.setText(text).format());

    text = "Solve for magnification at each view independently.";
    rbMagnificationAll.setToolTipText(tooltipFormatter.setText(text).format());

    text = "Group views to solve for fewer magnification variables.";
    rbMagnificationAutomap.setToolTipText(tooltipFormatter.setText(text)
      .format());

    text = "View at which magnification will be fixed at 1.0.";
    ltfMagnificationReferenceView.setToolTipText(tooltipFormatter.setText(text)
      .format());

    text = "Grouping size for magnifications.";
    ltfMagnificationGroupSize.setToolTipText(tooltipFormatter.setText(text)
      .format());

    text = "Sets of views with non-default grouping.  For each set, enter starting"
      + " and ending view number and group size, separated by commas; "
      + "separate multiple sets with spaces.";
    ltfMagnificationNonDefaultGroups.setToolTipText(tooltipFormatter.setText(
      text).format());

    text = "Solve for distortions in the plane of section.";
    cbDistortion.setToolTipText(tooltipFormatter.setText(text).format());

    text = "Basic grouping size for X stretch (grouping will be less at high tilt "
      + "and more at low tilt).";
    ltfXstretchGroupSize
      .setToolTipText(tooltipFormatter.setText(text).format());

    text = "Sets of views with non-default grouping for X stretch.  For each set, "
      + "enter starting and ending view number and group size, separated by"
      + " commas; separate multiple sets with spaces.";
    ltfXstretchNonDefaultGroups.setToolTipText(tooltipFormatter.setText(text)
      .format());

    text = "Grouping size for skew angles.";
    ltfSkewGroupSize.setToolTipText(tooltipFormatter.setText(text).format());

    text = "Sets of views with non-default grouping for skew angles.  For each set,"
      + " enter starting and ending view number and group size, separated by"
      + " commas; separate multiple sets with spaces.";
    ltfSkewNonDefaultGroups.setToolTipText(tooltipFormatter.setText(text)
      .format());

    text = "Solve for local in-plane rotations.";
    cbLocalRotation.setToolTipText(tooltipFormatter.setText(text).format());

    text = "Grouping size for local rotations.";
    ltfLocalRotationGroupSize.setToolTipText(tooltipFormatter.setText(text)
      .format());

    text = "Sets of views with non-default grouping.  For each set, enter starting"
      + " and ending view number and group size, separated by commas; "
      + "separate multiple sets with spaces.";
    ltfLocalRotationNonDefaultGroups.setToolTipText(tooltipFormatter.setText(
      text).format());

    text = "Solve for local changes in tilt angle.";
    cbLocalTiltAngle.setToolTipText(tooltipFormatter.setText(text).format());

    text = "Grouping size for local tilt angle changes.";
    ltfLocalTiltAngleGroupSize.setToolTipText(tooltipFormatter.setText(text)
      .format());

    text = "Sets of views with non-default grouping.  For each set, enter starting"
      + " and ending view number and group size, separated by commas; "
      + "separate multiple sets with spaces.";
    ltfLocalTiltAngleNonDefaultGroups.setToolTipText(tooltipFormatter.setText(
      text).format());

    text = "Solve for local changes in magnification.";
    cbLocalMagnification
      .setToolTipText(tooltipFormatter.setText(text).format());

    text = "Grouping size for local magnification changes.";
    ltfLocalMagnificationGroupSize.setToolTipText(tooltipFormatter
      .setText(text).format());

    text = "Sets of views with non-default grouping.  For each set, enter starting"
      + " and ending view number and group size, separated by commas; "
      + "separate multiple sets with spaces";
    ltfLocalMagnificationNonDefaultGroups.setToolTipText(tooltipFormatter
      .setText(text).format());

    text = "Solve for local distortions.";
    cbLocalDistortion.setToolTipText(tooltipFormatter.setText(text).format());

    text = "Grouping size for local X stretch variables.";
    ltfLocalXstretchGroupSize.setToolTipText(tooltipFormatter.setText(text)
      .format());
    text = "Sets of views with non-default grouping for X stretch.  For each set,"
      + " enter starting and ending view number and group size, separated by"
      + " commas; separate multiple sets with spaces.";
    ltfLocalXstretchNonDefaultGroups.setToolTipText(tooltipFormatter.setText(
      text).format());

    text = "Grouping size for local skew angle variables.";
    ltfLocalSkewGroupSize.setToolTipText(tooltipFormatter.setText(text)
      .format());

    text = "Sets of views with non-default grouping for skew angles.  For each set,"
      + " enter starting and ending view number and group size, separated by"
      + " commas; separate multiple sets with spaces.";
    ltfLocalSkewNonDefaultGroups.setToolTipText(tooltipFormatter.setText(text)
      .format());
  }

  /**
   * @return Returns the currentPrealignedBinning.
   */
  public int getPrealignedBinning() {
    return prealignedBinning;
  }

  /**
   * @param currentPrealignedBinning The currentPrealignedBinning to set.
   */
  public void setPrealignedBinning(int binning) {
    prealignedBinning = binning;
  }
}