package etomo.process;

import java.io.File;
import java.io.IOException;

import etomo.ApplicationManager;
import etomo.EtomoDirector;
import etomo.comscript.ComScriptManager;
import etomo.comscript.TiltParam;
import etomo.type.AxisID;
import etomo.type.ConstEtomoNumber;
import etomo.type.ProcessName;
import etomo.util.InvalidParameterException;
import etomo.util.MRCHeader;

/**
 * <p>Description: </p>
 * 
 * <p>Copyright: Copyright (c) 2002, 2003</p>
 * 
 * <p>Organization: Boulder Laboratory for 3D Electron Microscopy (BL3DEM),
 * University of Colorado</p>
 * 
 * @author $Author$
 * 
 * @version $Revision$
 * 
 * <p> $Log$
 * <p> Revision 3.17  2007/09/07 00:19:37  sueh
 * <p> bug# 989 Using a public INSTANCE to refer to the EtomoDirector singleton
 * <p> instead of getInstance and createInstance.
 * <p>
 * <p> Revision 3.16  2006/10/24 21:40:46  sueh
 * <p> bug# 947 Passing the ProcessName to AxisProcessPanel.
 * <p>
 * <p> Revision 3.15  2006/09/22 18:19:09  sueh
 * <p> bug# 931 Passing the process name to super().
 * <p>
 * <p> Revision 3.14  2006/09/19 22:30:02  sueh
 * <p> bug# 920 Refreshing meta data values in TiltParam each time tilt.com is loaded.
 * <p>
 * <p> Revision 3.13  2006/08/11 00:17:59  sueh
 * <p> bug# 739 Added reloadWatchedFile() and loadTiltParam().
 * <p>
 * <p> Revision 3.12  2006/08/02 22:27:37  sueh
 * <p> bug# 768 Added getReconnectInstance(), to get an instances where reconnect
 * <p> is true.
 * <p>
 * <p> Revision 3.11  2005/10/21 16:53:18  sueh
 * <p> bug# 690 Fixed calcFileSize():  X should come from columns and y
 * <p> should come from rows.  Getting image binned.  Dividing tiltPara.width
 * <p> and/or slice range by image binned.  Dividing Z by image binned.
 * <p>
 * <p> Revision 3.10  2005/07/29 00:53:02  sueh
 * <p> bug# 709 Going to EtomoDirector to get the current manager is unreliable
 * <p> because the current manager changes when the user changes the tab.
 * <p> Passing the manager where its needed.
 * <p>
 * <p> Revision 3.9  2005/06/20 16:48:41  sueh
 * <p> bug# 522 Made MRCHeader an n'ton.  Getting instance instead of
 * <p> constructing in calcFileSize().
 * <p>
 * <p> Revision 3.8  2005/04/25 20:49:57  sueh
 * <p> bug# 615 Passing the axis where a command originates to the message
 * <p> functions so that the message will be popped up in the correct window.
 * <p> This requires adding AxisID to many objects.
 * <p>
 * <p> Revision 3.7  2004/11/19 23:26:09  sueh
 * <p> bug# 520 merging Etomo_3-4-6_JOIN branch to head.
 * <p>
 * <p> Revision 3.6.4.2  2004/10/11 02:04:52  sueh
 * <p> bug# 520 Using a variable called propertyUserDir instead of the "user.dir"
 * <p> property.  This property would need a different value for each manager.
 * <p> This variable can be retrieved from the manager if the object knows its
 * <p> manager.  Otherwise it can retrieve it from the current manager using the
 * <p> EtomoDirector singleton.  If there is no current manager, EtomoDirector
 * <p> gets the value from the "user.dir" property.
 * <p>
 * <p> Revision 3.6.4.1  2004/09/29 19:11:46  sueh
 * <p> bug# 520 Removing pass-through function calls.
 * <p>
 * <p> Revision 3.6  2004/06/18 05:36:59  rickg
 * <p> Handle y slice when step size not specified
 * <p>
 * <p> Revision 3.5  2004/06/17 23:55:51  rickg
 * <p> Bug #460 moved getting of current time into FileSizeProcessMonitor on
 * <p> instantiation
 * <p>
 * <p> Revision 3.4  2004/06/17 23:34:32  rickg
 * <p> Bug #460 added script starting time to differentiate old data files
 * <p>
 * <p> Revision 3.3  2004/06/17 23:06:21  rickg
 * <p> Bug #460 Using nio FileChannle.size() method to monitor file since it seems 
 * <p> to be much more reliable than the File.length() method
 * <p>
 * <p> Revision 3.2  2004/06/14 17:26:47  sueh
 * <p> bug# 460 set startTime in the constructor
 * <p>
 * <p> Revision 3.1  2004/03/24 03:09:28  rickg
 * <p> Tilt getter name change for incrSlice
 * <p>
 * <p> Revision 3.0  2003/11/07 23:19:01  rickg
 * <p> Version 1.0.0
 * <p>
 * <p> Revision 1.3  2003/07/31 22:54:17  rickg
 * <p> 2GB fix.  An intermediate setp in the fileSize calculation over
 * <p> flowed the temporay int that was created.
 * <p>
 * <p> Revision 1.2  2003/07/01 19:30:06  rickg
 * <p> Added mode bytes handling
 * <p> Get input and output filenames from tilt?.com
 * <p>
 * <p> Revision 1.1  2003/06/27 20:29:25  rickg
 * <p> Initial rev (in progress)
 * <p> </p>
 */

final class TiltProcessMonitor extends FileSizeProcessMonitor {
  public static final String rcsid = "$Id$";

  private TiltParam tiltParam = null;

  public TiltProcessMonitor(ApplicationManager appMgr, AxisID id) {
    super(appMgr, id, ProcessName.TILT);
  }

  public static TiltProcessMonitor getReconnectInstance(
      ApplicationManager appMgr, AxisID id) {
    TiltProcessMonitor instance = new TiltProcessMonitor(appMgr, id);
    instance.setReconnect(true);
    return instance;
  }

  /* (non-Javadoc)
   * @see etomo.process.FileSizeProcessMonitor#calcFileSize()
   */
  void calcFileSize() throws InvalidParameterException, IOException {
    long nX;
    long nY;
    long nZ;
    int modeBytes = 4;

    // Get the depth, mode, any mods to the X and Y size from the tilt 
    // command script and the input and output filenames. 
    loadTiltParam();
    // Get the header from the aligned stack to use as default nX and
    // nY parameters
    String alignedFilename = applicationManager.getPropertyUserDir() + "/"
        + tiltParam.getInputFile();

    MRCHeader alignedStack = MRCHeader.getInstance(applicationManager
        .getPropertyUserDir(), alignedFilename, axisID);
    alignedStack.read();

    nX = alignedStack.getNColumns();
    nY = alignedStack.getNRows();

    nZ = tiltParam.getThickness();
    if (tiltParam.hasMode()) {
      switch (tiltParam.getMode()) {
      case 0:
        modeBytes = 1;
        break;
      case 1:
        modeBytes = 2;
        break;

      case 2:
        modeBytes = 4;
        break;

      case 3:
        modeBytes = 4;
        break;

      case 4:
        modeBytes = 8;
        break;

      case 16:
        modeBytes = 3;
        break;

      default:
        throw new InvalidParameterException("Unknown mode parameter");
      }
    }
    // Get the imageBinned from prenewst.com script
    long imageBinned = 1;
    ConstEtomoNumber number = tiltParam.getImageBinned();
    if (number != null) {
      imageBinned = number.getLong();
    }
    //adjust x and y
    if (tiltParam.hasWidth()) {
      nX = tiltParam.getWidth() / imageBinned;
    }
    if (tiltParam.hasSlice()) {
      int sliceRange = tiltParam.getIdxSliceStop()
          - tiltParam.getIdxSliceStart() + 1;
      // Divide by the step size if present
      if (tiltParam.getIncrSlice() == Integer.MIN_VALUE) {
        nY = sliceRange / imageBinned;
      }
      else {
        nY = sliceRange / tiltParam.getIncrSlice() / imageBinned;
      }
    }
    long fileSize = 1024 + ((long) nX * nY) * (nZ / imageBinned) * modeBytes;
    nKBytes = (int) (fileSize / 1024);

    if (EtomoDirector.INSTANCE.isDebug()) {
      System.err.println("TiltProcessMonitor.calcFileSize:fileSize=" + fileSize
          + ",nX=" + nX + ",nY=" + nY + ",nZ=" + nZ + ",imageBinned="
          + imageBinned);
    }
    applicationManager.getMainPanel().setProgressBar("Calculating tomogram",
        nKBytes, axisID,ProcessName.TILT);
  }

  protected void reloadWatchedFile() {
    loadTiltParam();
    // Create a file object describing the file to be monitored
    watchedFile = new File(applicationManager.getPropertyUserDir(), tiltParam
        .getOutputFile());
  }

  private void loadTiltParam() {
    if (tiltParam != null) {
      return;
    }
    ComScriptManager comScriptManager = applicationManager
        .getComScriptManager();
    comScriptManager.loadTilt(axisID);
    tiltParam = comScriptManager.getTiltParam(axisID);
    applicationManager.getMetaData().setTiltParam(tiltParam, axisID);
  }
}
