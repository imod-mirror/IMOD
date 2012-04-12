/*
 *  iproc.c -- image processing for 3dmod.
 *
 *  Original author: James Kremer
 *  Revised by: David Mastronarde   email: mast@colorado.edu
 *
 *  Copyright (C) 1995-2005 by Boulder Laboratory for 3-Dimensional Electron
 *  Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *  Colorado.  See dist/COPYRIGHT for full copyright notice.
 */

/*  $Author$

$Date$

$Revision$
Log at end of file
*/

#include <qlabel.h>
#include <qwidgetstack.h>
#include <qlayout.h>
#include <qtooltip.h>
#include <qcombobox.h>
#include <qlistbox.h>
#include <qspinbox.h>
#include <qvbox.h>
#include <qcheckbox.h>
#include <qpushbutton.h>
#include <qradiobutton.h>
#include <qvbuttongroup.h>
#include "dia_qtutils.h"
#include "tooledit.h"
#include "multislider.h"
#include "imod.h"
#include "imod_display.h"
#include "iproc.h"
#include "sliceproc.h"
#include "xcorr.h"
#include "xzap.h"
#include "imod_info.h"
#include "imod_info_cb.h"
#include "control.h"
#include "preferences.h"

/* internal functions. */
static void clearsec(ImodIProc *ip);
static void savesec(ImodIProc *ip);
static void cpdslice(Islice *sl, ImodIProc *ip);
static void copyAndDisplay();
static void setSliceMinMax(bool actual);
static void freeArrays(ImodIProc *ip);
static void  setUnscaledK();

static void edge_cb();
static void mkedge_cb(IProcWindow *win, QWidget *parent, QVBoxLayout *layout);
static void thresh_cb();
static void mkthresh_cb(IProcWindow *win, QWidget *parent, 
                        QVBoxLayout *layout);
static void smooth_cb();
static void sharpen_cb();
static void mkFourFilt_cb(IProcWindow *win, QWidget *parent, 
                          QVBoxLayout *layout);
static void fourFilt_cb();
static void mkFFT_cb(IProcWindow *win, QWidget *parent, QVBoxLayout *layout);
static void fft_cb();
static void mkMedian_cb(IProcWindow *win, QWidget *parent, 
                        QVBoxLayout *layout);
static void median_cb();
static void mkAnisoDiff_cb(IProcWindow *win, QWidget *parent,
                           QVBoxLayout *layout);
static void anisoDiff_cb();


/* The table of entries and callbacks */
ImodIProcData proc_data[] = {
  {"FFT", fft_cb, mkFFT_cb, NULL},
  {"Fourier filter", fourFilt_cb, mkFourFilt_cb, NULL},
  {"smooth", smooth_cb, NULL, "Smooth Image."},
  {"median", median_cb, mkMedian_cb, NULL},
  {"diffusion", anisoDiff_cb, mkAnisoDiff_cb, NULL},
  {"edge", edge_cb, mkedge_cb, NULL},
  {"sharpen", sharpen_cb, NULL, "Sharpen Edges."},
  {"threshold", thresh_cb, mkthresh_cb, NULL},
  NULL,
};

/* Static variables for proc structure and a slice */
static ImodIProc proc = {0, 0};
static Islice s;

/*
 * CALLBACK FUNCTIONS FOR THE VARIOUS FILTERS
 */

// New rule 11/07/04: Set the desired output min and max before calling 
// routines in sliceproc or xcorr to either the data range with 
// setSliceMinMax(false) or the existing input range with  setSliceMinMax(true)

// Edge enhancement
static void edge_cb()
{
  ImodIProc *ip = &proc;
  Islice *gs;

  switch (ip->edge){
  case 0:
    setSliceMinMax(false);
    sliceByteEdgeSobel(&s);
    break;

  case 1:
    setSliceMinMax(false);
    sliceByteEdgePrewitt(&s);
    break;

  case 2:
    setSliceMinMax(false);
    sliceByteEdgeLaplacian(&s);
    break;
	  
  case 3:
    setSliceMinMax(false);
    sliceByteGraham(&s);
    break;

  case 4:
    gs = sliceGradient(&s);
    if (!gs) return;
    cpdslice(gs, ip);
    break;

  default:
    break;
  }
}

// Threshold
static void thresh_cb()
{
  ImodIProc *ip = &proc;
  int xysize, thresh, minv, maxv;
  unsigned char *idat, *last;
     
  setSliceMinMax(false);
  thresh = ip->threshold;
     
  if (App->depth == 8){
    thresh = (int)
      ((((float)ip->vi->rampsize/256.0f)*thresh) + ip->vi->rampbase);
    minv = ip->vi->rampbase;
    maxv = ip->vi->rampsize + minv - 1;
  }else{
    minv = 0; maxv = 255;
  }

  xysize = ip->vi->xsize * ip->vi->ysize;
  idat = ip->iwork;
  for(last = idat + xysize; idat != last; idat++){
    if (*idat > thresh)
      *idat = maxv;
    else
      *idat = minv;
  }

  if (ip->threshGrow)
    sliceByteGrow(&s,  (int)s.max);
  if (ip->threshShrink)
    sliceByteShrink(&s,  (int)s.max);
}

// Smoothing
static void smooth_cb()
{
  setSliceMinMax(true);
  sliceByteSmooth(&s);
}

// Sharpening
static void sharpen_cb()
{
  setSliceMinMax(true);
  sliceByteSharpen(&s);
}

// Fourier filter
static void fourFilt_cb()
{
  ImodIProc *ip = &proc;
  setSliceMinMax(true);
  sliceFourierFilter(&s, ip->sigma1, ip->sigma2, ip->radius1, ip->radius2);
}

// FFT
static void fft_cb()
{
  int ix0, ix1, iy0, iy1, nxuse, nyuse;
  ix0 = iy0 = 0;
  nxuse = s.xsize;
  nyuse = s.ysize;
  if (proc.fftSubset)
    zapSubsetLimits(proc.vi, ix0, iy0, nxuse, nyuse);
  ix1 = ix0 + nxuse - 1;
  iy1 = iy0 + nyuse - 1;
  setSliceMinMax(false);
  proc.fftScale = sliceByteBinnedFFT(&s, proc.fftBinning, ix0, ix1, iy0, iy1,
                                     &proc.fftXcen, &proc.fftYcen);
}

// Median filter
static void median_cb()
{
  unsigned char *to;
  unsigned char **from;
  int i, j, k, z;
  ImodIProc *ip = &proc;
  int depth = ip->median3D ? ip->medianSize : 1;
  int zst = B3DMAX(0, ip->idatasec - depth / 2);
  int znd = B3DMIN(ip->vi->zsize - 1, ip->idatasec + (depth - 1) / 2);

  ip->medianVol.zsize = depth = znd + 1 - zst;
  ip->medianVol.vol = (Islice **)malloc(depth * sizeof(Islice *));
  if (!ip->medianVol.vol)
    return;

  for (i = 0, z = zst; z <= znd; z++, i++) {

    // Get a slice and get the array of pointers
    ip->medianVol.vol[i] = sliceCreate(ip->vi->xsize, ip->vi->ysize, 
                                       SLICE_MODE_BYTE);
    from = ivwGetZSectionTime(ip->vi, z, ip->idatatime);

    // If either is in error, clean up what is already allocated
    if (!from || !ip->medianVol.vol[i]) {
      for (j = 0; j <= i; j++)
        if (ip->medianVol.vol[j])
          sliceFree(ip->medianVol.vol[j]);
      free(ip->medianVol.vol);
      return;
    }

    // Copy data
    to = ip->medianVol.vol[i]->data.b;
    for (j = 0; j < ip->vi->ysize; j++)
      for (k = 0; k < ip->vi->xsize; k++)
        *to++ = from[j][k];
  }

  sliceMedianFilter(&s, &ip->medianVol, ip->medianSize);

  // Clean up
  for (j = 0; j < depth; j++)
    sliceFree(ip->medianVol.vol[j]);
  free(ip->medianVol.vol);
}

// Anisotropic diffusion
static void anisoDiff_cb()
{
  ImodIProc *ip = &proc;
  ip->andfK = ip->andfKEdit->text().toDouble();
  ip->andfLambda = ip->andfLambdaEdit->text().toDouble();
  sliceByteAnisoDiff(&s, ip->andfImage, ip->andfImage2, ip->andfStopFunc + 2,
                     ip->andfK, ip->andfLambda, ip->andfIterations, 
                     &ip->andfIterDone);
}

// Set the min and max of the static slice to full range, or actual values
static void setSliceMinMax(bool actual)
{
  if (actual) {
    sliceMinMax(&s);
  } else if (App->depth == 8){
    s.min = proc.vi->rampbase;
    s.max = proc.vi->rampsize + s.min - 1;
  } else {
    s.min = 0;
    s.max = 255;
  }
}


/* Reset and get new data buffer */
int iprocRethink(struct ViewInfo *vi)
{
  if (proc.dia){
    if (proc.isaved) {
      clearsec(&proc);
      proc.idatasec = -1;
      freeArrays(&proc);
    }
    proc.isaved = (unsigned char *)malloc(vi->xsize * vi->ysize);
    proc.iwork = (unsigned char *)malloc(vi->xsize * vi->ysize);

    proc.andfImage = allocate2D_float(vi->ysize + 2, vi->xsize + 2);
    proc.andfImage2 = allocate2D_float(vi->ysize + 2, vi->xsize + 2);
    proc.andfIterDone = 0;
    proc.andfDoneLabel->setText("0 done");

    if (!proc.isaved || !proc.iwork || !proc.andfImage || !proc.andfImage2) {
      freeArrays(&proc);
      wprint("\aCannot get new memory for processing window!\n");
      proc.dia->close();
      return 1;
    }

    proc.dia->limitFFTbinning();
  }
  return 0;
}

/* Open the processing dialog box */
int inputIProcOpen(struct ViewInfo *vi)
{
  if (!proc.dia){
    if (!proc.vi) {
      proc.procnum = 0;
      proc.threshold = 128;
      proc.threshGrow = false;
      proc.threshShrink = false;
      proc.edge = 0;
      proc.sigma1 = 0.;
      proc.sigma2 = 0.05f;
      proc.radius1 = 0.;
      proc.radius2 = 0.5f;
      proc.fftBinning = 1;
      proc.fftScale = 0.;
      proc.fftSubset = false;
      proc.medianSize = 3;
      proc.median3D = true;
      proc.andfK = 2.;
      proc.andfStopFunc = 0;
      proc.andfLambda = 0.2;
      proc.andfIterations = 5;
      proc.isaved = NULL;
      proc.iwork = NULL;
      proc.andfImage = NULL;
      proc.andfImage2 = NULL;
    }
    proc.vi = vi;
    proc.idatasec = -1;
    proc.idatatime = 0;
    proc.modified = 0;
    proc.andfIterDone = 0;

    proc.isaved = (unsigned char *)malloc(vi->xsize * vi->ysize);
    proc.iwork = (unsigned char *)malloc(vi->xsize * vi->ysize);

    proc.andfImage = allocate2D_float(vi->ysize + 2, vi->xsize + 2);
    proc.andfImage2 = allocate2D_float(vi->ysize + 2, vi->xsize + 2);

    if (!proc.isaved || !proc.iwork || !proc.andfImage || !proc.andfImage2) {
      freeArrays(&proc);
      wprint("\aCannot get memory for processing window!\n");
      return(-1);
    }

    proc.dia = new IProcWindow(imodDialogManager.parent(IMOD_DIALOG), NULL);
    imodDialogManager.add((QWidget *)proc.dia, IMOD_DIALOG);

  }else{
    proc.dia->raise();
  }
  return(0);
}

static void freeArrays(ImodIProc *ip)
{
  if (ip->isaved)
    free(ip->isaved);
  ip->isaved = NULL;
  if (ip->iwork)
    free(ip->iwork);
  ip->iwork = NULL;
  if (ip->andfImage) {
    free(ip->andfImage[0]);
    free(ip->andfImage);
    ip->andfImage = NULL;
  }
  if (ip->andfImage2) {
    free(ip->andfImage2[0]);
    free(ip->andfImage2);
    ip->andfImage2 = NULL;
  }
}


bool iprocBusy(void)
{
#ifdef QT_THREAD_SUPPORT
  if (!proc.dia)
    return false;
  return proc.dia->mRunningProc;
#else
  return false;
#endif
}

/*
 * DATA COPYING FUNCTIONS
 */

/* Copy data from a generated slice into the working buffer and free slice */
static void cpdslice(Islice *sl, ImodIProc *ip)
{
  register unsigned char *from, *to, *last;
  int rampbase = ip->vi->rampbase;
  from = sl->data.b;
  to = ip->iwork;
  if (!to) return;

  last = to + (ip->vi->xsize * ip->vi->ysize);
  if (App->depth > 8){
    do{
      *to++ = *from++;
    }while (to !=  last);
  }else{
    do{
      *to++ = *from++ + rampbase;
    }while (to !=  last);
  }
  sliceFree(sl);
}

/* Copy the working buffer back to the display memory and draw */
static void copyAndDisplay()
{
  ImodIProc *ip = &proc;
  unsigned char **to = ivwGetZSectionTime(ip->vi, ip->idatasec, ip->idatatime);
  unsigned char *from = ip->iwork;
  int i, j;
  int cz =  (int)(ip->vi->zmouse + 0.5f);
  
  for (j = 0; j < ip->vi->ysize; j++)
    for (i = 0; i < ip->vi->xsize; i++)
      to[j][i] = *from++;

  imod_info_float_clear(cz, ip->vi->ct);
  imodDraw(ip->vi, IMOD_DRAW_IMAGE);
}


/* clear the section back to original data. */
static void clearsec(ImodIProc *ip)
{
  register unsigned char *from, *to;
  unsigned char **to2;
  int i, j;
     
  if (ip->idatasec < 0 || !ip->modified)
    return;

  from = ip->isaved;
  to = ip->iwork;
  to2 = ivwGetZSectionTime(ip->vi, ip->idatasec, ip->idatatime);
  if (!to2)
    return;
  for (j = 0; j < ip->vi->ysize; j++)
    for (i = 0; i < ip->vi->xsize; i++)
      *to++ = to2[j][i] = *from++;

  ip->modified = 0;
  imod_info_float_clear(ip->idatasec, ip->idatatime);
}

/* save the displayed image to saved and working buffers. */
static void savesec(ImodIProc *ip)
{
  register unsigned char *to, *to2;
  unsigned char **from;
  int i, j;
     
  if (ip->idatasec < 0)
    return;

  to   = ip->isaved;
  to2  = ip->iwork;
  from = ivwGetZSectionTime(ip->vi, ip->idatasec, ip->idatatime);
  if (!from) 
    return;
  for (j = 0; j < ip->vi->ysize; j++)
    for (i = 0; i < ip->vi->xsize; i++)
      *to++ = *to2++ = from[j][i];
}


/*
 * FUNCTIONS TO MAKE THE WIDGETS FOR PARTICULAR FILTERS
 */
static void mkedge_cb(IProcWindow *win, QWidget *parent, QVBoxLayout *layout)
{
  diaLabel("Edge Enhancement Filter Type:", parent, layout);
  QComboBox *edgeBox = new QComboBox(parent);
  layout->addWidget(edgeBox);
  edgeBox->insertItem("Sobel");
  edgeBox->insertItem("Prewitt");
  edgeBox->insertItem("Laplacian");
  edgeBox->insertItem("Graham");
  edgeBox->insertItem("Gradient");
  edgeBox->setFocusPolicy(QComboBox::NoFocus);
  edgeBox->setCurrentItem(proc.edge);
  QObject::connect(edgeBox, SIGNAL(activated(int)), win, 
                   SLOT(edgeSelected(int)));
}

static void mkthresh_cb(IProcWindow *win, QWidget *parent, QVBoxLayout *layout)
{
  char *sliderLabel[] = {"Threshold filter value" };
  MultiSlider *slider = new MultiSlider(parent, 1, sliderLabel, 0, 254);
  slider->setValue(0, proc.threshold);
  QObject::connect(slider, SIGNAL(sliderChanged(int, int, bool)), win, 
          SLOT(threshChanged(int, int, bool)));
  layout->addLayout(slider->getLayout());
  QCheckBox *check = diaCheckBox("Grow thresholded area", parent,
                                 layout);
  diaSetChecked(check, proc.threshGrow);
  QObject::connect(check, SIGNAL(toggled(bool)), win, SLOT(growChanged(bool)));
  QToolTip::add(check, "Apply dilation to grow area selected area ");
  check = diaCheckBox("Shrink thresholded area", parent, layout);
  diaSetChecked(check, proc.threshShrink);
  QObject::connect(check, SIGNAL(toggled(bool)), win, 
                   SLOT(shrinkChanged(bool)));
  QToolTip::add(check, "Apply erosion to shrink selected area");
}

static void mkFourFilt_cb(IProcWindow *win, QWidget *parent,
                          QVBoxLayout *layout)
{
  char *sliderLabel[] = {"Low-frequency sigma", "High-frequency cutoff",
                         "High-frequency falloff"};
  diaLabel("Filtering in Fourier Space", parent, layout);
  MultiSlider *slider = new MultiSlider(parent, 3, sliderLabel, 0, 200, 3);
  slider->setRange(1, 0, 500);
  slider->setRange(2, 1, 200);
  slider->setValue(0, (int)(1000. * proc.sigma1));
  slider->setValue(1, (int)(1000. * proc.radius2));
  slider->setValue(2, (int)(1000. * proc.sigma2));
  QObject::connect(slider, SIGNAL(sliderChanged(int, int, bool)), win, 
          SLOT(fourFiltChanged(int, int, bool)));
  layout->addLayout(slider->getLayout());
  QToolTip::add((QWidget *)slider->getSlider(0), "Sigma for inverted Gaussian"
                " high-pass filter (0 at origin)");
  QToolTip::add((QWidget *)slider->getSlider(1), "Cutoff radius for Gaussian"
                " low-pass filter");
  QToolTip::add((QWidget *)slider->getSlider(0), "Sigma for Gaussian"
                "low-pass filter starting at cutoff");
}

static void mkFFT_cb(IProcWindow *win, QWidget *parent, QVBoxLayout *layout)
{
  diaLabel("Fourier transform", parent, layout);
  QHBoxLayout *hLayout = new QHBoxLayout(layout);
  QLabel *label = new QLabel("Binning", parent);
  label->setAlignment(Qt::AlignRight | Qt::AlignVCenter);
  hLayout->addWidget(label);
  proc.fftBinSpin = new QSpinBox(1, 8, 1, parent);
  hLayout->addWidget(proc.fftBinSpin);
  proc.fftBinSpin->setFocusPolicy(QWidget::ClickFocus);
  QObject::connect(proc.fftBinSpin, SIGNAL(valueChanged(int)), win, 
                   SLOT(binningChanged(int)));
  QCheckBox *check = diaCheckBox("Use Zap window subarea", parent, layout);
  diaSetChecked(check, proc.fftSubset);
  QObject::connect(check, SIGNAL(toggled(bool)), win,
                   SLOT(subsetChanged(bool)));
  QToolTip::add(check, "Do FFT of area displayed or within rubber band "
                "in active Zap window");

  proc.fftLabel1 = diaLabel("  ", parent, layout);
  proc.fftLabel2 = diaLabel("  ", parent, layout);

  hLayout = new QHBoxLayout(layout);
  QHBox *spacer = new QHBox(parent);
  hLayout->addWidget(spacer);
  hLayout->setStretchFactor(spacer, 10);
  proc.freqButton = diaPushButton("Report frequency", parent, hLayout);
  QObject::connect(proc.freqButton, SIGNAL(clicked()), win,
                   SLOT(reportFreqClicked()));
  QToolTip::add(proc.freqButton,
                "Compute resolution at current marker or model point");
  proc.freqButton->setEnabled(false);
  spacer = new QHBox(parent);
  hLayout->addWidget(spacer);
  hLayout->setStretchFactor(spacer, 10);

  proc.fftLabel3 = diaLabel("  ", parent, layout);
  win->limitFFTbinning();
}

static void mkMedian_cb(IProcWindow *win, QWidget *parent, 
                        QVBoxLayout *layout)
{
  diaLabel("Median filter", parent, layout);
  QHBoxLayout *hLayout = new QHBoxLayout(layout);
  QLabel *label = new QLabel("Size", parent);
  label->setAlignment(Qt::AlignRight | Qt::AlignVCenter);
  hLayout->addWidget(label);
  QSpinBox *sizeSpin = new QSpinBox(2, 9, 1, parent);
  diaSetSpinBox(sizeSpin, proc.medianSize);
  hLayout->addWidget(sizeSpin);
  sizeSpin->setFocusPolicy(QWidget::ClickFocus);
  QObject::connect(sizeSpin, SIGNAL(valueChanged(int)), win, 
                   SLOT(medSizeChanged(int)));
  QCheckBox *check = diaCheckBox("Compute median in 3D cube", parent, layout);
  diaSetChecked(check, proc.median3D);
  QObject::connect(check, SIGNAL(toggled(bool)), win,
                   SLOT(med3DChanged(bool)));
  QToolTip::add(check, "Take median in 3D cube instead of median in 2D within"
                " this section");
}

static void mkAnisoDiff_cb(IProcWindow *win, QWidget *parent,
                           QVBoxLayout *layout)
{
  QString str;
  diaLabel("Anisotropic diffusion", parent, layout);

  // The edge stopping radio buttons
  QVButtonGroup *stopGroup = new QVButtonGroup("Edge Stopping Function", 
                                               parent);
  stopGroup->setInsideSpacing(0);
  stopGroup->setInsideMargin(5);
  layout->addWidget(stopGroup);
  QRadioButton *radio = diaRadioButton("Rational", stopGroup);
  QToolTip::add(radio, "Use rational edge stopping function; may require "
                "smaller K values");
  radio = diaRadioButton("Tukey biweight", stopGroup);
  QToolTip::add(radio, "Use Tukey biweight edge stopping function; may require"
                " larger K values");
  diaSetGroup(stopGroup, proc.andfStopFunc);

  // Iteration spin box and report of # done
  QHBoxLayout *hLayout = new QHBoxLayout(layout);
  QLabel *label = new QLabel("Iterations", parent);
  label->setAlignment(Qt::AlignRight | Qt::AlignVCenter);
  hLayout->addWidget(label);
  QSpinBox *iterSpin = new QSpinBox(1, 1000, 1, parent);
  diaSetSpinBox(iterSpin, proc.andfIterations);
  QToolTip::add(iterSpin, "Number of time steps to take in one run");
  hLayout->addWidget(iterSpin);
  iterSpin->setFocusPolicy(QWidget::ClickFocus);
  QObject::connect(iterSpin, SIGNAL(valueChanged(int)), win, 
                   SLOT(andfIterChanged(int)));
  proc.andfDoneLabel = new QLabel("0 done", parent);
  hLayout->addWidget(proc.andfDoneLabel);

  // K edit box and report of unscaled K value
  hLayout = new QHBoxLayout(layout);
  label = new QLabel("K", parent);
  label->setAlignment(Qt::AlignRight | Qt::AlignVCenter);
  hLayout->addWidget(label);
  proc.andfKEdit = new ToolEdit(parent, 6);
  hLayout->addWidget(proc.andfKEdit);
  QToolTip::add(proc.andfKEdit, "Gradient threshold parameter controlling "
                "edge stopping function");
  str.sprintf("%.5g", proc.andfK);
  proc.andfKEdit->setText(str);
  proc.andfKEdit->setFocusPolicy(QWidget::ClickFocus);
  proc.andfScaleLabel = new QLabel(" ", parent);
  setUnscaledK();
  hLayout->addWidget(proc.andfScaleLabel);
  QObject::connect(proc.andfKEdit, SIGNAL(returnPressed()), win,
                   SLOT(setFocus()));
  QObject::connect(proc.andfKEdit, SIGNAL(returnPressed()), win,
                   SLOT(andfKEntered()));
  QObject::connect(proc.andfKEdit, SIGNAL(focusLost()), win,
                   SLOT(andfKEntered()));

  // Lambda edit box
  hLayout = new QHBoxLayout(layout);
  label = new QLabel("Lambda", parent);
  label->setAlignment(Qt::AlignRight | Qt::AlignVCenter);
  hLayout->addWidget(label);
  proc.andfLambdaEdit = new ToolEdit(parent, 6);
  hLayout->addWidget(proc.andfLambdaEdit);
  QToolTip::add(proc.andfLambdaEdit, "Size of time step");
  str.sprintf("%g", proc.andfLambda);
  proc.andfLambdaEdit->setText(str);
  proc.andfLambdaEdit->setFocusPolicy(QWidget::ClickFocus);
  QObject::connect(proc.andfLambdaEdit, SIGNAL(returnPressed()), win,
                   SLOT(setFocus()));
  QHBox *spacer = new QHBox(parent);
  hLayout->addWidget(spacer);
  hLayout->setStretchFactor(spacer, 100);
}

static void setUnscaledK()
{
  QString str;
  str.sprintf("unscaled: %.5g",  proc.andfK / proc.vi->image->slope);
  proc.andfScaleLabel->setText(str);
}

/* THE WINDOW CLASS CONSTRUCTOR */
static char *buttonLabels[] = {"Apply", "More", "Toggle", "Reset", "Save", 
                               "Done", "Help"};
static char *buttonTips[] = {"Operate on current section (hot key a)",
                             "Reiterate operation on current section (hot key"
                             " b)",
                             "Toggle between processed and original image",
                             "Reset section to unprocessed image",
                             "Replace section in memory with processed image",
                             "Close dialog box", "Open help window"};

IProcWindow::IProcWindow(QWidget *parent, const char *name)
  : DialogFrame(parent, 7, 1, buttonLabels, buttonTips, false, 
                ImodPrefs->getRoundedStyle(), " ", "", name)
{
  int i;
  QString str;
  QVBoxLayout *vLayout;
  QWidget *control;
  mRunningProc = false;

  // Put an H layout inside the main layout, then fill that with the
  // List box and the widget stack
  QHBoxLayout *hLayout = new QHBoxLayout(mLayout);
  mListBox = new QListBox(this);
  hLayout->addWidget(mListBox);
  mListBox->setFocusPolicy(QListBox::NoFocus);

  mStack = new QWidgetStack(this);
  hLayout->addWidget(mStack);

  // Put a spacer on the right to keep the list box position from changing
  QHBox *hspace = new QHBox(this);
  hLayout->addWidget(hspace);
  hLayout->setStretchFactor(hspace, 5);
  

  for (i = 0; (proc_data[i].name); i++) {

    // For each item, add to list box, make a widget and give it a V layout
    mListBox->insertItem(proc_data[i].name);
    control = new QWidget(this);
    vLayout = new QVBoxLayout(control, 3, 6);

    // Call the make widget function or just add a label
    if (proc_data[i].mkwidget)
      proc_data[i].mkwidget (this, control, vLayout);
    else {
      diaLabel(proc_data[i].label, control, vLayout);
    }

    // Fill box with spacer
    QVBox *spacer = new QVBox(control);
    vLayout->addWidget(spacer);
    vLayout->setStretchFactor(spacer, 100);

    // Add widget to stack and set size policy to ignored
    mStack->addWidget(control, i);
    control->setSizePolicy(QSizePolicy(QSizePolicy::Ignored,
                                       QSizePolicy::Ignored));
  }

  // Finalize list box setting and connections
  mListBox->setCurrentItem(proc.procnum);
  connect(mListBox, SIGNAL(highlighted(int)), this,
          SLOT(filterHighlighted(int)));
  connect(mListBox, SIGNAL(selected(int)), this, SLOT(filterSelected(int)));
  
  // 1/27/05: Forget this, panels set the height now; but fix the width
  // to avoid bottom scroll
  //if (i > MAX_LIST_TO_SHOW)
  //  i = MAX_LIST_TO_SHOW;
  //mListBox->setMaximumHeight(i * mListBox->itemHeight() + 4);
  QSize size = mListBox->sizeHint();
  mListBox->setFixedWidth(size.width() + 4);

  filterHighlighted(proc.procnum);

  connect(this, SIGNAL(actionClicked(int)), this, SLOT(buttonClicked(int)));
  connect(this, SIGNAL(actionPressed(int)), this, SLOT(buttonPressed(int)));
  setCaption(imodCaption("3dmod Image Processing"));
  show();
}

/* Action functions */
void IProcWindow::threshChanged(int which, int value, bool dragging)
{
  proc.threshold = value;
}

void IProcWindow::fourFiltChanged(int which, int value, bool dragging)
{
  if (!which)
    proc.sigma1 = 0.001 * value;
  else if (which == 1)
    proc.radius2 = 0.001 * value;
  else if (which == 2)
    proc.sigma2 = 0.001 * value;
}

void IProcWindow::binningChanged(int val)
{
  setFocus();
  proc.fftBinning = val;
}
void IProcWindow::subsetChanged(bool state)
{
  proc.fftSubset = state;
}

void IProcWindow::reportFreqClicked()
{
  double dx, dy, xpt, ypt, dist;
  Ipoint *curpt;
  QString str;
  Imod *imod = proc.vi->imod;
    
  if (proc.fftScale <= 0.)
    return;

  // Use the current model point if defined and in model mode, or use 
  // current marker point
  curpt = imodPointGet(imod);
  if (proc.vi->imod->mousemode == IMOD_MMODEL && curpt) {
    xpt = curpt->x - 0.5f;
    ypt = curpt->y - 0.5f;
  } else {
    xpt = (int)proc.vi->xmouse;
    ypt = (int)proc.vi->ymouse;
  }
  dx = xpt - proc.fftXcen;
  dy = ypt - proc.fftYcen;
  dist = proc.fftScale * sqrt(dx * dx + dy * dy) / 
    (imod->pixsize * proc.vi->xybin);
  if (dist) 
    str.sprintf("Freq: %.4g/%s  (%.4g %s)", dist, imodUnits(imod), 
                1. / dist, imodUnits(imod));
  else
    str.sprintf("Freq: 0/%s", imodUnits(imod));
  proc.fftLabel3->setText(str);
}


void IProcWindow::growChanged(bool state)
{
  proc.threshGrow = state;
}
void IProcWindow::shrinkChanged(bool state)
{
  proc.threshShrink = state;
}

// To switch filters, set the size policy of the current widget back to ignored
// raise the new widget, set its size policy, make the stack process geometry
// again then adjust window size
void IProcWindow::filterHighlighted(int which)
{
  QWidget *control = mStack->visibleWidget();
  if (control)
    control->setSizePolicy(QSizePolicy(QSizePolicy::Ignored, 
                                       QSizePolicy::Ignored));
  proc.procnum = which;
  mStack->raiseWidget(which);
  control = mStack->visibleWidget();
  control->setSizePolicy(QSizePolicy(QSizePolicy::Expanding,
                                     QSizePolicy::Expanding));
  mStack->adjustSize();
  adjustSize();
}

void IProcWindow::filterSelected(int which)
{
  filterHighlighted(which);
  if (!proc.dia->mRunningProc)
    apply();
}

void IProcWindow::edgeSelected(int which)
{
  proc.edge = which;
}

void IProcWindow::medSizeChanged(int val)
{ 
  setFocus();
  proc.medianSize = val;
}

void IProcWindow::med3DChanged(bool state)
{
  proc.median3D = state;
}

void IProcWindow::andfIterChanged(int val)
{
  setFocus();
  proc.andfIterations = val;
}
void IProcWindow::andfFuncClicked(int val)
{
  proc.andfStopFunc = val;
}

void IProcWindow::andfKEntered()
{
  proc.andfK = proc.andfKEdit->text().toDouble();
  setUnscaledK();
}


// Respond to button click (release)
void IProcWindow::buttonClicked(int which)
{
  ImodIProc *ip = &proc;

  int cz =  (int)(ip->vi->zmouse + 0.5f);
  setFocus();

  if (which < 5 && ip->vi->loadingImage)
    return;

  switch (which) {
  case 0:  // Apply
    apply();
    break;

  case 1:  // More
    /* If this is not the same section, treat it as an Apply */
    if (cz != ip->idatasec || ip->vi->ct != ip->idatatime) {
      apply();
      break;
    }

    /* Otherwise operate on the current data without restoring it */
    if (proc_data[ip->procnum].cb)
      startProcess();
    break;

  case 2:  // Toggle
    if (ip->modified && cz == ip->idatasec && ip->vi->ct == ip->idatatime)
      copyAndDisplay();
    break;

  case 3: // reset
    clearsec(ip);
    imodDraw(ip->vi, IMOD_DRAW_IMAGE);
    break;

  case 4: // save
    ip->modified = 0;
    ip->idatasec = -1;
    break;

  case 5: // Done
    close();
    break;

  case 6: // Help
    imodShowHelpPage("imageProc.html");
    break;
  }
}

// Respond to button press for toggle button only - redisplay original data
// but re-mark as modified
void IProcWindow::buttonPressed(int which)
{
  unsigned char *from;
  unsigned char **to2;
  int i, j;
  ImodIProc *ip = &proc;
  int cz =  (int)(ip->vi->zmouse + 0.5f);

  if (which != 2 || !ip->modified || cz != ip->idatasec || 
      ip->vi->ct != ip->idatatime)
    return;
     
  from = ip->isaved;
  to2 = ivwGetZSectionTime(ip->vi, ip->idatasec, ip->idatatime);
  if (!to2)
    return;
  for (j = 0; j < ip->vi->ysize; j++)
    for (i = 0; i < ip->vi->xsize; i++)
      to2[j][i] = *from++;

  imod_info_float_clear(ip->idatasec, ip->idatatime);
  imodDraw(ip->vi, IMOD_DRAW_IMAGE);
}

// Apply the current filter
void IProcWindow::apply()
{
  ImodIProc *ip = &proc;
  sliceInit(&s, ip->vi->xsize, ip->vi->ysize, 0, ip->iwork);

  int cz =  (int)(ip->vi->zmouse + 0.5f);

  /* Unconditionally restore data if modified */
  clearsec(ip);

  ip->andfIterDone = 0;
  ip->andfDoneLabel->setText("0 done");

  /* If this is a new section, save the data */
  if (cz != ip->idatasec || ip->vi->ct != ip->idatatime) {
    ip->idatasec = cz;
    ip->idatatime = ip->vi->ct;
    savesec(ip);
  }

  // Make sure there is floating info for this data so it can be saved when
  // it is cleared
  imod_info_bwfloat(ip->vi, ip->idatasec, ip->idatatime);
  imodInfoSaveNextClear();
    
  /* Operate on the original data */
  startProcess();
}

void IProcWindow::startProcess()
{
  ImodIProc *ip = &proc;
  int i;
  if (!proc_data[ip->procnum].cb)
    return;
  ip->fftScale = 0.;
  ip->freqButton->setEnabled(false);

#ifdef QT_THREAD_SUPPORT

  // If running in a thread, set flag, disable buttons except help,
  // start timer and start thread
  mRunningProc = true;
  for (i = 0; i < mNumButtons - 1; i++)
    mButtons[i]->setEnabled(false);
  ImodInfoWin->manageMenus();
  mTimerID = startTimer(50);
  mProcThread = new IProcThread;

  // Priorities not available in Qt 3.1
#if QT_VERSION >= 0x030200
  mProcThread->start(QThread::LowPriority);
#else
  mProcThread->start();
#endif

#else

  // Otherwise just start the process directly and do finishing tasks
  proc_data[ip->procnum].cb();
  finishProcess();
#endif
}

void IProcWindow::finishProcess()
{
  ImodIProc *ip = &proc;
  QString str;
  float xrange, yrange;
  ip->modified = 1;
  copyAndDisplay();
  if (ip->fftScale < 0.) {
    wprint("\aMemory error trying to do FFT!\n");
  } else if (ip->fftScale > 0.) {
    str.sprintf("Scale: %.3g/pixel per FFT pixel", ip->fftScale);
    ip->fftLabel1->setText(str);
    xrange = 0.5 * ip->fftScale * ip->vi->xsize;
    xrange = xrange <= 0.5 ? xrange : 0.5f;
    yrange = 0.5 * ip->fftScale * ip->vi->ysize;
    yrange = yrange <= 0.5 ? yrange : 0.5f;
    str.sprintf("Range: +/- %.4f in X, +/- %.4f in Y", xrange, yrange);
    ip->fftLabel2->setText(str);
    ip->freqButton->setEnabled(true);
  }
  if (ip->andfIterDone) {
    str.sprintf("%d done", ip->andfIterDone);
    ip->andfDoneLabel->setText(str);
    setUnscaledK();
  }
}


void IProcWindow::timerEvent(QTimerEvent *e)
{
#ifdef QT_THREAD_SUPPORT
  int i;
  if (mProcThread->running())
    return;
  killTimer(mTimerID);
  for (i = 0; i < mNumButtons - 1; i++)
    mButtons[i]->setEnabled(true);
  delete mProcThread;
  mRunningProc = false;
  finishProcess();
  ImodInfoWin->manageMenus();
#endif
}

void IProcWindow::limitFFTbinning()
{
  ImodIProc *ip = &proc;
  int limit = 16;
  if (limit > ip->vi->xsize)
    limit = ip->vi->xsize;
  if (limit > ip->vi->ysize)
    limit = ip->vi->ysize;
  if (ip->fftBinning > limit)
    ip->fftBinning = limit;
  ip->fftBinSpin->blockSignals(true);
  ip->fftBinSpin->setMaxValue(limit);
  ip->fftBinSpin->setValue(ip->fftBinning);
  ip->fftBinSpin->blockSignals(false);
}

void IProcWindow::fontChange( const QFont & oldFont )
{
  mRoundedStyle = ImodPrefs->getRoundedStyle();
  DialogFrame::fontChange(oldFont);
}

// The window is closing, clean up and remove from manager
void IProcWindow::closeEvent ( QCloseEvent * e )
{
  ImodIProc *ip = &proc;
  if (!ip->dia || mRunningProc)
    return;
  clearsec(ip);
  imodDialogManager.remove((QWidget *)ip->dia);
  imodDraw(ip->vi, IMOD_DRAW_IMAGE);
  freeArrays(ip);
  ip->dia = NULL;
  e->accept();
}

// Close on escape, pass on keys
void IProcWindow::keyPressEvent ( QKeyEvent * e )
{
  int modkey = e->state() & (Qt::ShiftButton | Qt::ControlButton);
  if (e->key() == Qt::Key_A && !modkey) {
    if (!iprocBusy() && !proc.vi->loadingImage)
      apply();
  } else if (e->key() == Qt::Key_B && !modkey) {
    if (!iprocBusy() && !proc.vi->loadingImage)
      buttonClicked(1);
  } else if (e->key() == Qt::Key_Escape)
    close();
  else
    ivwControlKey(0, e);
}

void IProcWindow::keyReleaseEvent ( QKeyEvent * e )
{
  ivwControlKey(1, e);
}

#ifdef QT_THREAD_SUPPORT
// A very simple thread run command!
void IProcThread::run()
{
  proc_data[proc.procnum].cb();
}
#endif

/*

    $Log$
    Revision 4.19  2005/03/23 18:46:37  mast
    Added a and b hot keys, consolidated grow and shrink in threshold

    Revision 4.18  2005/03/09 21:20:12  mast
    converted diffusion to floats

    Revision 4.17  2005/02/12 01:36:18  mast
    Made call to save bwfloat data on every apply, rearranged list

    Revision 4.16  2005/02/10 00:12:00  mast
    Fixed allocation of diffusion arrays to be right when not square

    Revision 4.15  2005/01/28 05:39:59  mast
    Added anisotropic diffusion

    Revision 4.14  2005/01/07 21:59:01  mast
    Added median filter, converted help page

    Revision 4.13  2004/11/11 15:55:34  mast
    Changes to do FFT in a subarea

    Revision 4.12  2004/11/09 17:54:24  mast
    Fixed problem in running non-threaded, changed Qt version cutoff for
    setting thread priority

    Revision 4.11  2004/11/08 06:03:10  mast
    Needed to make some more thread items conditional

    Revision 4.10  2004/11/08 05:41:52  mast
    Needed to make priority on starting thread conditional on Qt version

    Revision 4.9  2004/11/07 23:05:24  mast
    Execute in thread, added FFT and fourier filter, fixed scaling problems

    Revision 4.8  2004/11/04 23:30:55  mast
    Changes for rounded button style

    Revision 4.7  2004/02/12 00:16:18  mast
    Changed the setSizePolicy calls to be compatible to Qt 3.0.5

    Revision 4.6  2004/01/22 19:09:38  mast
    Added a button to toggle between processed and original image, and changed
    geometry management to resize to the widget so large panels can be inserted

    Revision 4.5  2004/01/05 18:04:56  mast
    Prevented operating on images while data being loaded; renamed vw to vi

    Revision 4.4  2003/09/16 02:10:26  mast
    Changed to make a working copy of the image data using the new line
    pointers, operate on the working copy, and save back into the display
    data as needed.

    Revision 4.3  2003/04/25 03:28:32  mast
    Changes for name change to 3dmod

    Revision 4.2  2003/04/17 18:43:38  mast
    adding parent to window creation

    Revision 4.1  2003/02/10 20:29:02  mast
    autox.cpp

    Revision 1.1.2.2  2003/01/27 00:30:07  mast
    Pure Qt version and general cleanup

    Revision 1.1.2.1  2003/01/23 19:57:06  mast
    Qt version

    Revision 3.2.2.1  2003/01/13 01:15:43  mast
    changes for Qt version of info window

    Revision 3.2  2002/12/01 15:34:41  mast
    Changes to get clean compilation with g++

*/