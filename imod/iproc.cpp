/*  IMOD VERSION 2.50
 *
 *  iproc.c -- image processing for imod.
 *
 *  Original author: James Kremer
 *  Revised by: David Mastronarde   email: mast@colorado.edu
 */

/*****************************************************************************
 *   Copyright (C) 1995-2001 by Boulder Laboratory for 3-Dimensional Fine    *
 *   Structure ("BL3DFS") and the Regents of the University of Colorado.     *
 *                                                                           *
 *   BL3DFS reserves the exclusive rights of preparing derivative works,     *
 *   distributing copies for sale, lease or lending and displaying this      *
 *   software and documentation.                                             *
 *   Users may reproduce the software and documentation as long as the       *
 *   copyright notice and other notices are preserved.                       *
 *   Neither the software nor the documentation may be distributed for       *
 *   profit, either in original form or in derivative works.                 *
 *                                                                           *
 *   THIS SOFTWARE AND/OR DOCUMENTATION IS PROVIDED WITH NO WARRANTY,        *
 *   EXPRESS OR IMPLIED, INCLUDING, WITHOUT LIMITATION, WARRANTY OF          *
 *   MERCHANTABILITY AND WARRANTY OF FITNESS FOR A PARTICULAR PURPOSE.       *
 *                                                                           *
 *   This work is supported by NIH biotechnology grant #RR00592,             *
 *   for the Boulder Laboratory for 3-Dimensional Fine Structure.            *
 *   University of Colorado, MCDB Box 347, Boulder, CO 80309                 *
 *****************************************************************************/
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
#include <qvbox.h>
#include "dia_qtutils.h"
#include "multislider.h"
#include "imod.h"
#include "imod_display.h"
#include "iproc.h"
#include "sliceproc.h"
#include "imod_info_cb.h"
#include "control.h"

#define MAX_LIST_TO_SHOW 6

/* internal functions. */
static void clearsec(ImodIProc *ip);
static void savesec(ImodIProc *ip);
static void cpdslice(Islice *sl, ImodIProc *ip);

static void edge_cb();
static void mkedge_cb(IProcWindow *win, QWidget *parent, QVBoxLayout *layout);
static void thresh_cb();
static void mkthresh_cb(IProcWindow *win, QWidget *parent, 
                        QVBoxLayout *layout);
static void smooth_cb();
static void sharpen_cb();
static void grow_cb();
static void shrink_cb();

/* The table of entries and callbacks */
ImodIProcData proc_data[] = {
  {"edge", edge_cb, mkedge_cb, NULL},
  {"threshold", thresh_cb, mkthresh_cb, NULL},
  {"smooth", smooth_cb, NULL, "Smooth Image."},
  {"sharpen", sharpen_cb, NULL, "Sharpen Edges."},
  {"dilation", grow_cb, NULL, "Grow Threshold Area."},
  {"erosion", shrink_cb, NULL, "Shrink Threshold Area."},
  NULL,
};

/* Static variables for proc structure and a slice */
static ImodIProc proc = {0, 0};
static Islice s;

static void cpdslice(Islice *sl, ImodIProc *ip)
{
  register unsigned char *from, *to, *last;
  int rampbase = ip->vw->rampbase;
  from = sl->data.b;
  to = ivwGetZSectionTime(ip->vw, ip->idatasec, ip->idatatime);
  if (!to) return;
  /*     to = ip->vw->idata[ip->idatasec]; */
  last = to + (ip->vw->xsize * ip->vw->ysize);
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


/*
 * CALLBACK FUNCTIONS FOR THE VARIOUS FILTERS
 */

// Edge enhancement
static void edge_cb()
{
  ImodIProc *ip = &proc;
  Islice *gs;

  switch (ip->edge){
  case 0:
    sliceByteEdgeSobel(&s);
    if(App->depth == 8)
      sliceByteAdd(&s, ip->vw->rampbase);
    break;

  case 1:
    sliceByteEdgePrewitt(&s);
    if(App->depth == 8)
      sliceByteAdd(&s, ip->vw->rampbase);
    break;

  case 2:
    sliceByteEdgeLaplacian(&s);
    break;
	  
  case 3:
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
  imodDraw(ip->vw, IMOD_DRAW_IMAGE);
  return;
}

// Threshold
static void thresh_cb()
{
  ImodIProc *ip = &proc;
  int xysize, thresh, minv, maxv;
  unsigned char *idat, *last;
     
  thresh = ip->threshold;
     
  if (App->depth == 8){
    thresh = (int)
      ((((float)ip->vw->rampsize/256.0f)*thresh) + ip->vw->rampbase);
    minv = ip->vw->rampbase;
    maxv = ip->vw->rampsize + minv;
  }else{
    minv = 0; maxv = 255;
  }

  xysize = ip->vw->xsize * ip->vw->ysize;
  idat = ivwGetCurrentZSection(ip->vw);
  if (!idat) return;
  for(last = idat + xysize; idat != last; idat++){
    if (*idat > thresh)
      *idat = maxv;
    else
      *idat = minv;
  }
  imodDraw(ip->vw, IMOD_DRAW_IMAGE);
  return;
}

// Smoothing
static void smooth_cb()
{
  ImodIProc *ip = &proc;
  sliceByteSmooth(&s);
  imodDraw(ip->vw, IMOD_DRAW_IMAGE);
}

// Sharpening
static void sharpen_cb()
{
  ImodIProc *ip = &proc;
  sliceByteSharpen(&s);
  imodDraw(ip->vw, IMOD_DRAW_IMAGE);
}

// Growing a thresholded area
static void grow_cb()
{
  ImodIProc *ip = &proc;

  if (App->depth == 8){
    s.min = ip->vw->rampbase;
    s.max = ip->vw->rampsize + s.min;
  }else{
    s.min = 0; s.max = 255;
  }

  // If the slice is not modified, run a threshold on it
  if (!ip->modified)
    thresh_cb();

  sliceByteGrow(&s,  (int)s.max);
  imodDraw(ip->vw, IMOD_DRAW_IMAGE);
}

// Shrinking a thresholded area
static void shrink_cb()
{
  ImodIProc *ip = &proc;

  if (App->depth == 8){
    s.min = ip->vw->rampbase;
    s.max = ip->vw->rampsize + s.min;
  }else{
    s.min = 0; s.max = 255;
  }

  // If the slice is not modified, run a threshold on it
  if (!ip->modified)
    thresh_cb();

  sliceByteShrink(&s,  (int)s.max);
  imodDraw(ip->vw, IMOD_DRAW_IMAGE);
}


/* Reset and get new data buffer */
int iprocRethink(struct ViewInfo *vw)
{
  if (proc.dia){
    if (proc.idata) {
      clearsec(&proc);
      proc.idatasec = -1;
      free(proc.idata);
    }
    proc.idata = (unsigned char *)malloc(vw->xsize * vw->ysize);
  }
  return 0;
}

/* Open the processing dialog box */
int inputIProcOpen(struct ViewInfo *vw)
{
  if (!proc.dia){
    if (!proc.vw) {
      proc.procnum = 0;
      proc.threshold = 128;
      proc.edge = 0;
    }
    proc.vw = vw;
    proc.idatasec = -1;
    proc.idatatime = 0;
    proc.modified = 0;
    proc.idata = (unsigned char *)malloc(vw->xsize * vw->ysize);

    if (!proc.idata)
      return(-1);
    proc.dia = new IProcWindow(imodDialogManager.parent(IMOD_DIALOG), NULL);
    imodDialogManager.add((QWidget *)proc.dia, IMOD_DIALOG);

  }else{
    proc.dia->raise();
  }
  return(0);
}

/* clear the section back to original data. */
static void clearsec(ImodIProc *ip)
{
  register unsigned char *from, *to, *last;
     
  if (ip->idatasec < 0 || !ip->modified)
    return;

  from = ip->idata;
  to = ivwGetZSectionTime(ip->vw, ip->idatasec, ip->idatatime);
  if (!to) return;
  last = to + (ip->vw->xsize * ip->vw->ysize);
  do{
    *to++ = *from++;
  }while (to != last);
  ip->modified = 0;
  imod_info_float_clear(ip->idatasec, ip->idatatime);
  return;
}

/* save the processing image to buffer. */
static void savesec(ImodIProc *ip)
{
  register unsigned char *from, *to, *last;
     
  if (ip->idatasec < 0)
    return;


  to   = ip->idata;
  from = ivwGetZSectionTime(ip->vw, ip->idatasec, ip->idatatime);
  if (!from) return;
  last = to + (ip->vw->xsize * ip->vw->ysize);
  do{
    *to++ = *from++;
  }while (to != last);
}


/* Functions to make the widgets for particular filters */
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
  char *sliderLabel[] = {"Threshold filter value"};
  MultiSlider *slider = new MultiSlider(parent, 1, sliderLabel, 0, 254);
  slider->setValue(0, proc.threshold);
  QObject::connect(slider, SIGNAL(sliderChanged(int, int, bool)), win, 
          SLOT(threshChanged(int, int, bool)));
  layout->addLayout(slider->getLayout());
}



/* THE WINDOW CLASS CONSTRUCTOR */

static char *buttonLabels[] = {"Apply", "More", "Reset", "Save", "Done",
                               "Help"};
static char *buttonTips[] = {"Operate on current section",
                             "Reiterate operation on current section",
                             "Reset section to unprocessed image",
                             "Replace section in memory with processed image",
                             "Close dialog box", "Open help window"};

IProcWindow::IProcWindow(QWidget *parent, const char *name)
  : DialogFrame(parent, 6, buttonLabels, buttonTips, true, 
                " ", "", name)
{
  int i;
  int width = 0, height = 0;
  QString str;
  QVBoxLayout *vLayout;
  QWidget *control;

  // Put an H layout inside the main layout, then fill that with the
  // List box and the widget stack
  QHBoxLayout *hLayout = new QHBoxLayout(mLayout);
  mListBox = new QListBox(this);
  hLayout->addWidget(mListBox);
  mListBox->setFocusPolicy(QListBox::NoFocus);

  mStack = new QWidgetStack(this);
  hLayout->addWidget(mStack);

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

    // Add widget to layout first, and get a size; then add to stack
    hLayout->addWidget(control);
    QSize size = control->sizeHint();
    if (width < size.width())
      width = size.width();
    if (height < size.height())
      height = size.height();
    mStack->addWidget(control, i);
  }

  // Finalize list box setting and connections
  mListBox->setCurrentItem(proc.procnum);
  connect(mListBox, SIGNAL(highlighted(int)), this,
          SLOT(filterHighlighted(int)));
  connect(mListBox, SIGNAL(selected(int)), this, SLOT(filterSelected(int)));
  if (i > MAX_LIST_TO_SHOW)
    i = MAX_LIST_TO_SHOW;
  mListBox->setMinimumHeight(i * mListBox->itemHeight() + 4);

  // Set minimum size of stack
  mStack->setMinimumWidth(width);
  mStack->setMinimumHeight(height);
  mStack->raiseWidget(proc.procnum);

  connect(this, SIGNAL(actionPressed(int)), this, SLOT(buttonPressed(int)));
  setCaption(imodCaption("Imod Image Processing"));
  show();
}

/* Action functions */
void IProcWindow::threshChanged(int which, int value, bool dragging)
{
  proc.threshold = value;
}

void IProcWindow::filterHighlighted(int which)
{
  proc.procnum = which;
  mStack->raiseWidget(which);
}

void IProcWindow::filterSelected(int which)
{
  filterHighlighted(which);
  apply();
}

void IProcWindow::edgeSelected(int which)
{
  proc.edge = which;
}

// Respond to button press
void IProcWindow::buttonPressed(int which)
{
  ImodIProc *ip = &proc;

  int cz =  (int)(ip->vw->zmouse + 0.5f);

  switch (which) {
  case 0:  // Apply
    apply();
    break;

  case 1:  // More
    /* If this is not the same section, treat it as an Apply */
    if (cz != ip->idatasec || ip->vw->ct != ip->idatatime) {
      apply();
      return;
    }

    /* Otherwise operate on the current data without restoring it */
    if ( proc_data[ip->procnum].cb) {
      imod_info_float_clear(cz, ip->vw->ct);
      proc_data[ip->procnum].cb();
      ip->modified = 1;
    }
    break;

  case 2: // reset
    clearsec(ip);
    imodDraw(ip->vw, IMOD_DRAW_IMAGE);
    break;

  case 3: // save
    ip->modified = 0;
    ip->idatasec = -1;
    break;

  case 4: // Done
    close();
    break;

  case 5: // Help
    dia_vasmsg
      ("~~~~~~~~~~~~~~~~~~~~~~~~\n"
       "Imod Image Processing \n"
       "~~~~~~~~~~~~~~~~~~~~~~~~"
       "\n\n",
       "Various kinds of simple filters can be applied with these "
       "controls.  The filter will always be applied to the current "
       "section.\n\n",
       "Single-click in the list of filters to select the current filter "
       "to be applied to the data; in some cases there will be further "
       "parameters to select.\n\n"
       "Selecting the [Apply] button will apply the current filter to the "
       "ORIGINAL image data.  Double-clicking in the filter list is the "
       "same as selecting the [Apply] button.\n\n"
       "Selecting the [More] button will apply the filter to the CURRENT "
       "image data, as modified by previous filter operations.\n\n"
       "Selecting the [Reset] button, applying a filter to a different "
       "section, closing the window with [Done], or flipping the data "
       "volume will all restore the original image data for a section, "
       "unless you select the [Save] button.  [Save] will permanently "
       "replace the image data in memory with the processed data.\n\n",
       NULL);
    break;
  }
}

// Apply the current filter
void IProcWindow::apply()
{
  ImodIProc *ip = &proc;
  unsigned char *image = ivwGetCurrentZSection(ip->vw);

  if (!image) 
    return;
  sliceInit(&s, ip->vw->xsize, ip->vw->ysize, 0, image);

  int cz =  (int)(ip->vw->zmouse + 0.5f);

  /* Unconditionally restore data if modified */
  clearsec(ip);

  /* If this is a new section, save the data */
  if (cz != ip->idatasec || ip->vw->ct != ip->idatatime) {
    ip->idatasec = cz;
    ip->idatatime = ip->vw->ct;
    savesec(ip);
  }
    
  /* Operate on the original data */
  if ( proc_data[ip->procnum].cb) {
    imod_info_float_clear(cz, ip->vw->ct);
    proc_data[ip->procnum].cb();
    ip->modified = 1;
  }
}

// The window is closing, clean up and remove from manager
void IProcWindow::closeEvent ( QCloseEvent * e )
{
  ImodIProc *ip = &proc;
  clearsec(ip);
  imodDialogManager.remove((QWidget *)ip->dia);
  imodDraw(ip->vw, IMOD_DRAW_IMAGE);
  free(ip->idata);
  ip->dia = NULL;
  e->accept();
}

// Close on escape, pass on keys
void IProcWindow::keyPressEvent ( QKeyEvent * e )
{
  if (e->key() == Qt::Key_Escape)
    close();
  else
    ivwControlKey(0, e);
}

void IProcWindow::keyReleaseEvent ( QKeyEvent * e )
{
  ivwControlKey(1, e);
}

/*

    $Log$
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
