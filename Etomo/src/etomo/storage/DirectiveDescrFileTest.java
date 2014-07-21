package etomo.storage;

import java.io.File;

import etomo.BaseManager;
import etomo.EtomoDirector;
import etomo.storage.DirectiveDescrFile.Element;
import etomo.type.AxisID;
import etomo.type.FileType;
import etomo.util.TestUtilites;
import junit.framework.TestCase;

/**
* <p>Description: </p>
* 
* <p>Copyright: Copyright 2013</p>
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
public class DirectiveDescrFileTest extends TestCase {
  public static final String rcsid = "$Id:$";

  public void testIterator() {
    BaseManager manager = (BaseManager) EtomoDirector.INSTANCE.getCurrentManagerForTest();
    DirectiveDescrFile descrFile = DirectiveDescrFile.INSTANCE;
    AxisID axisID = AxisID.ONLY;
    descrFile.setFile(new File(TestUtilites.INSTANCE.getUnitTestData().getAbsolutePath(),
        FileType.DIRECTIVES_DESCR.getFileName(manager, axisID)));
    DirectiveDescrFile.Iterator iterator = descrFile.getIterator(manager, axisID);
    Element element = iterator.next();
    assertNotNull("file title", element);
    assertTrue("hasNext doesn't increment the iterator", iterator.hasNext());
    assertTrue("file title", element.isSection());
    assertFalse("file title", element.isDirective());
    assertEquals("file title", "Batch/Template Directives", element.getName());
    assertNull("file title", element.getDescription());
    assertEquals("file title", element.getValueType(), DirectiveValueType.UNKNOWN);
    assertFalse("file title", element.isBatch());
    assertFalse("file title", element.isTemplate());
    assertNull("file title", element.getEtomoColumn());

    // has next is not necessary because next increments the iterator
    element = iterator.next();
    assertTrue("title", element.isSection());
    assertFalse("title", element.isDirective());
    assertEquals("title", "Directives for Batch Processing and Template Files",
        element.getName());
    assertNull("title", element.getDescription());
    assertEquals("title", element.getValueType(), DirectiveValueType.UNKNOWN);
    assertFalse("title", element.isBatch());
    assertFalse("title", element.isTemplate());
    assertNull("title", element.getEtomoColumn());

    assertTrue("column header", iterator.hasNext());
    element = iterator.next();
    assertFalse("column header", element.isSection());
    assertTrue("column header", element.isDirective());
    assertEquals("column header", "Directive", element.getName());
    assertEquals("column header", "Definition", element.getDescription());
    assertEquals("column header", element.getValueType(), DirectiveValueType.UNKNOWN);
    assertFalse("column header", element.isBatch());
    assertFalse("column header", element.isTemplate());
    assertNull("column header", element.getEtomoColumn());

    assertTrue("blank", iterator.hasNext());
    element = iterator.next();
    assertFalse("blank", element.isSection());
    assertFalse("blank", element.isDirective());
    assertNull("blank", element.getName());
    assertNull("blank", element.getDescription());
    assertEquals("blank", element.getValueType(), DirectiveValueType.UNKNOWN);
    assertFalse("blank", element.isBatch());
    assertFalse("blank", element.isTemplate());
    assertNull("blank", element.getEtomoColumn());

    assertTrue("header - Arguments to copytomocoms", iterator.hasNext());
    element = iterator.next();
    assertTrue("header - Arguments to copytomocoms", element.isSection());
    assertFalse("header - Arguments to copytomocoms", element.isDirective());
    assertEquals("header - Arguments to copytomocoms", "Arguments to copytomocoms",
        element.getName());
    assertNull("header - Arguments to copytomocoms", element.getDescription());
    assertEquals("header - Arguments to copytomocoms", element.getValueType(),
        DirectiveValueType.UNKNOWN);
    assertFalse("header - Arguments to copytomocoms", element.isBatch());
    assertFalse("header - Arguments to copytomocoms", element.isTemplate());
    assertNull("header - Arguments to copytomocoms", element.getEtomoColumn());

    assertTrue("name", iterator.hasNext());
    element = iterator.next();
    assertFalse("name", element.isSection());
    assertTrue("name", element.isDirective());
    assertEquals("name", "setupset.copyarg.name", element.getName());
    assertEquals("name", "Root name of data set", element.getDescription());
    assertTrue("name", element.getValueType() == DirectiveValueType.STRING);
    assertTrue("name", element.isBatch());
    assertFalse("name", element.isTemplate());
    assertNull("name", element.getEtomoColumn());

    assertTrue("dual", iterator.hasNext());
    element = iterator.next();
    assertFalse("dual", element.isSection());
    assertTrue("dual", element.isDirective());
    assertEquals("dual", "setupset.copyarg.dual", element.getName());
    assertEquals("dual", "Dual-axis data set", element.getDescription());
    assertTrue("dual", element.getValueType() == DirectiveValueType.BOOLEAN);
    assertTrue("dual", element.isBatch());
    assertTrue("dual", element.isTemplate());
    assertTrue("dual", element.getEtomoColumn() == DirectiveDescrEtomoColumn.SO);

    assertTrue("montage", iterator.hasNext());
    element = iterator.next();
    assertFalse("montage", element.isSection());
    assertTrue("montage", element.isDirective());
    assertEquals("montage", "setupset.copyarg.montage", element.getName());
    assertEquals("montage", "Data are montaged", element.getDescription());
    assertTrue("montage", element.getValueType() == DirectiveValueType.BOOLEAN);
    assertTrue("montage", element.isBatch());
    assertTrue("montage", element.isTemplate());
    assertTrue("montage", element.getEtomoColumn() == DirectiveDescrEtomoColumn.SD);

    assertTrue("pixel", iterator.hasNext());
    element = iterator.next();
    assertFalse("pixel", element.isSection());
    assertTrue("pixel", element.isDirective());
    assertEquals("pixel", "setupset.copyarg.pixel", element.getName());
    assertEquals("pixel", "Pixel size of images in nanometers", element.getDescription());
    assertTrue("pixel", element.getValueType() == DirectiveValueType.FLOATING_POINT);
    assertTrue("pixel", element.isBatch());
    assertTrue("pixel", element.isTemplate());
    assertNull("pixel", element.getEtomoColumn());

    // gold
    iterator.next();
    // rotation
    iterator.next();
    // brotation
    iterator.next();

    assertTrue("firstinc", iterator.hasNext());
    element = iterator.next();
    assertFalse("firstinc", element.isSection());
    assertTrue("firstinc", element.isDirective());
    assertEquals("firstinc", "setupset.copyarg.firstinc", element.getName());
    assertEquals("firstinc", "First tilt angle and tilt angle increment",
        element.getDescription());
    assertTrue("firstinc",
        element.getValueType() == DirectiveValueType.FLOATING_POINT_PAIR);
    assertTrue("firstinc", element.isBatch());
    assertFalse("firstinc", element.isTemplate());
    assertNull("firstinc", element.getEtomoColumn());

    // bfirstinc
    iterator.next();
    // userawtlt
    iterator.next();
    // buserawtlt
    iterator.next();
    // extract
    iterator.next();
    // bextract
    iterator.next();

    assertTrue("skip", iterator.hasNext());
    element = iterator.next();
    assertFalse("skip", element.isSection());
    assertTrue("skip", element.isDirective());
    assertEquals("skip", "setupset.copyarg.skip", element.getName());
    assertEquals("skip", "List of views to exclude from processing for A or only axis",
        element.getDescription());
    assertTrue("skip", element.getValueType() == DirectiveValueType.LIST);
    assertTrue("skip", element.isBatch());
    assertFalse("skip", element.isTemplate());
    assertNull("skip", element.getEtomoColumn());

    // bskip
    iterator.next();
    // distort
    iterator.next();

    assertTrue("binning", iterator.hasNext());
    element = iterator.next();
    assertFalse("binning", element.isSection());
    assertTrue("binning", element.isDirective());
    assertEquals("binning", "setupset.copyarg.binning", element.getName());
    assertEquals("binning", "Binning of raw images", element.getDescription());
    assertTrue("binning", element.getValueType() == DirectiveValueType.INTEGER);
    assertTrue("binning", element.isBatch());
    assertTrue("binning", element.isTemplate());
    assertNull("binning", element.getEtomoColumn());

    // gradient
    iterator.next();
    // focus
    iterator.next();
    // bfocus
    iterator.next();
    // defocus
    iterator.next();
    // voltage
    iterator.next();

    assertTrue("Cs", iterator.hasNext());
    element = iterator.next();
    assertFalse("Cs", element.isSection());
    assertTrue("Cs", element.isDirective());
    assertEquals("Cs", "setupset.copyarg.Cs", element.getName());
    assertEquals("Cs", "Spherical aberration", element.getDescription());
    assertTrue("Cs", element.getValueType() == DirectiveValueType.FLOATING_POINT);
    assertTrue("Cs", element.isBatch());
    assertTrue("Cs", element.isTemplate());
    assertTrue("Cs", element.getEtomoColumn() == DirectiveDescrEtomoColumn.SD);

    // ctfnoise
    iterator.next();
    // blank
    iterator.next();
    // header - Other setup parameters
    iterator.next();

    assertTrue("scopeTemplate", iterator.hasNext());
    element = iterator.next();
    assertFalse("scopeTemplate", element.isSection());
    assertTrue("scopeTemplate", element.isDirective());
    assertEquals("scopeTemplate", "setupset.scopeTemplate", element.getName());
    assertEquals("scopeTemplate", "Name of scope template file to use",
        element.getDescription());
    assertTrue("scopeTemplate", element.getValueType() == DirectiveValueType.STRING);
    assertTrue("scopeTemplate", element.isBatch());
    assertFalse("scopeTemplate", element.isTemplate());
    assertNull("scopeTemplate", element.getEtomoColumn());

    // systemTemplate
    iterator.next();
    // userTemplate
    iterator.next();
    // scanHeader
    iterator.next();
    // datasetDirectory
    iterator.next();
    // blank
    iterator.next();
    // header - Preprocessing
    iterator.next();

    assertTrue("removeXrays", iterator.hasNext());
    element = iterator.next();
    assertFalse("removeXrays", element.isSection());
    assertTrue("removeXrays", element.isDirective());
    assertEquals("removeXrays", "runtime.Preprocessing.any.removeXrays",
        element.getName());
    assertEquals("removeXrays", "Run ccderaser to remove X rays",
        element.getDescription());
    assertTrue("removeXrays", element.getValueType() == DirectiveValueType.BOOLEAN);
    assertTrue("removeXrays", element.isBatch());
    assertFalse("removeXrays", element.isTemplate());
    assertTrue("removeXrays", element.getEtomoColumn() == DirectiveDescrEtomoColumn.NE);

    assertTrue("PeakCriterion", iterator.hasNext());
    element = iterator.next();
    assertFalse("PeakCriterion", element.isSection());
    assertTrue("PeakCriterion", element.isDirective());
    assertEquals("PeakCriterion", "comparam.eraser.ccderaser.PeakCriterion",
        element.getName());
    assertEquals("PeakCriterion", "Peak criterion # of SDs", element.getDescription());
    assertTrue("PeakCriterion",
        element.getValueType() == DirectiveValueType.FLOATING_POINT);
    assertTrue("PeakCriterion", element.isBatch());
    assertTrue("PeakCriterion", element.isTemplate());
    assertTrue("PeakCriterion", element.getEtomoColumn() == DirectiveDescrEtomoColumn.SD);

    // DiffCriterion
    iterator.next();
    // MaximumRadius
    iterator.next();
    // ModelFile
    iterator.next();
    // LineObjects
    iterator.next();
    // BoundaryObjects
    iterator.next();
    // AllSectionObjects
    iterator.next();
    // blank
    iterator.next();
    // header - Coarse alignment
    iterator.next();
    // FilterRadius2
    iterator.next();
    // FilterSigma2
    iterator.next();
    // fiducialless
    iterator.next();

    assertTrue("newstack.BinByFactor", iterator.hasNext());
    element = iterator.next();
    assertFalse("newstack.BinByFactor", element.isSection());
    assertTrue("newstack.BinByFactor", element.isDirective());
    assertEquals("newstack.BinByFactor", "comparam.prenewst.newstack.BinByFactor",
        element.getName());
    assertEquals("newstack.BinByFactor", "Coarse aligned stack binning",
        element.getDescription());
    assertTrue("newstack.BinByFactor",
        element.getValueType() == DirectiveValueType.INTEGER);
    assertTrue("newstack.BinByFactor", element.isBatch());
    assertTrue("newstack.BinByFactor", element.isTemplate());
    assertTrue("newstack.BinByFactor",
        element.getEtomoColumn() == DirectiveDescrEtomoColumn.SD);

    // ModeToOutput
    iterator.next();

    assertTrue("blendmont.BinByFactor", iterator.hasNext());
    element = iterator.next();
    assertFalse("blendmont.BinByFactor", element.isSection());
    assertTrue("blendmont.BinByFactor", element.isDirective());
    assertEquals("blendmont.BinByFactor", "comparam.preblend.blendmont.BinByFactor",
        element.getName());
    assertEquals("blendmont.BinByFactor", "Coarse aligned stack binning",
        element.getDescription());
    assertTrue("blendmont.BinByFactor",
        element.getValueType() == DirectiveValueType.INTEGER);
    assertTrue("blendmont.BinByFactor", element.isBatch());
    assertTrue("blendmont.BinByFactor", element.isTemplate());
    assertTrue("blendmont.BinByFactor",
        element.getEtomoColumn() == DirectiveDescrEtomoColumn.SD);

    // blank
    iterator.next();
    // header - Tracking choices
    iterator.next();
    // trackingMethod
    iterator.next();
    // seedingMethod
    iterator.next();
    // blank
    iterator.next();
    // header - Beadtracking
    iterator.next();
    // LightBeads
    iterator.next();
    // LocalAreaTracking
    iterator.next();

    assertTrue("LocalAreaTargetSize", iterator.hasNext());
    element = iterator.next();
    assertFalse("LocalAreaTargetSize", element.isSection());
    assertTrue("LocalAreaTargetSize", element.isDirective());
    assertEquals("LocalAreaTargetSize", "comparam.track.beadtrack.LocalAreaTargetSize",
        element.getName());
    assertEquals("LocalAreaTargetSize", "Size of local areas", element.getDescription());
    assertTrue("LocalAreaTargetSize",
        element.getValueType() == DirectiveValueType.INTEGER_PAIR);
    assertTrue("LocalAreaTargetSize", element.isBatch());
    assertTrue("LocalAreaTargetSize", element.isTemplate());
    assertTrue("LocalAreaTargetSize",
        element.getEtomoColumn() == DirectiveDescrEtomoColumn.SD);

    // SobelFilterCentering
    iterator.next();
    // KernelSigmaForSobel
    iterator.next();
    // RoundsOfTracking
    iterator.next();
    // NumberOfRuns
    iterator.next();
    // blank
    iterator.next();
    // header - Auto seed finding
    iterator.next();

    assertTrue("TwoSurfaces", iterator.hasNext());
    element = iterator.next();
    assertFalse("TwoSurfaces", element.isSection());
    assertTrue("TwoSurfaces", element.isDirective());
    assertEquals("TwoSurfaces", "comparam.autofidseed.autofidseed.TwoSurfaces",
        element.getName());
    assertEquals("TwoSurfaces", "Whether beads on 2 surfaces", element.getDescription());
    assertTrue("TwoSurfaces", element.getValueType() == DirectiveValueType.BOOLEAN);
    assertTrue("TwoSurfaces", element.isBatch());
    assertTrue("TwoSurfaces", element.isTemplate());
    assertTrue("TwoSurfaces", element.getEtomoColumn() == DirectiveDescrEtomoColumn.NES);

    DirectiveDescrFile.INSTANCE.releaseIterator(iterator);
  }
}
