/*  IMOD VERSION 2.40
 *
 *  imod_model_edit.c -- model edit dialog functions.
 *
 *  Original author: James Kremer
 *  Revised by: David Mastronarde   email: mast@colorado.edu
 */

/*****************************************************************************
 *   Copyright (C) 1995-2000 by Boulder Laboratory for 3-Dimensional Fine    *
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

$Log$
Revision 3.0.2.2  2003/01/13 01:15:43  mast
changes for Qt version of info window

Revision 3.0.2.1  2002/12/23 04:59:19  mast
Make routine for parsing pixel size string

*/
#include <qlineedit.h>
#include <qlabel.h>
#include <qlayout.h>
#include <qtooltip.h>
#include "dia_qtutils.h"
#include "imod_model_edit.h"
#include <Xm/Xm.h>
#include <Xm/RowColumn.h>
#include <Xm/Separator.h>
#include <Xm/Label.h>
#include <Xm/Text.h>
#include <Xm/ToggleB.h>
#include <Xm/PushB.h>
#include <stdio.h>
#include <math.h>

#include "diaP.h"
#include "imod.h"
#include "imod_info_cb.h"
#include "control.h"

static struct
{
  diaDialog     *dia;
  ImodView      *vw;        /* image data to model                       */

  /* Widgets used in model edit. */
  Widget wDraw;
  Widget wZscale;
  Widget wPixelSize;
  Widget wResolution;
  Widget wTimeCopy;

}ThisDialog = { NULL, 0 };

static void workarea_cb(Widget w, XtPointer client, XtPointer call);
static void setvw(void);

static void quit_cb(Widget w, XtPointer client, XtPointer call)
{
  setvw();
  diaDestroyDialog(ThisDialog.dia);
  ThisDialog.dia = NULL;
  return;
}

int openModelEdit(ImodView *vw)
{
     
  if (ThisDialog.dia){
    XRaiseWindow(XtDisplay(ThisDialog.dia->dialog), 
		 XtWindow(ThisDialog.dia->dialog));
    return(0);
  }
     
  ThisDialog.vw = vw;
  ThisDialog.dia = diaVaCreateDialog
    ("Imod: Model Edit",
     App->toplevel, App->context,
     DiaNcontrolButton, "Done",
     quit_cb, (XtPointer)&ThisDialog,
     DiaNworkAreaFunc, workarea_cb, (XtPointer)&ThisDialog,
     DiaNwindowQuit, quit_cb, (XtPointer)&ThisDialog,
     0);     
     
  return 0;
}

/****************************************************************************/

static void setwidgets(void)
{
  char *units;
  char string[32];

  XmToggleButtonSetState
    (ThisDialog.wDraw,
     (ThisDialog.vw->imod->drawmode > 0) ? True : False,
     False);


  sprintf(string, "%g", ThisDialog.vw->imod->zscale);
  XmTextSetString(ThisDialog.wZscale, string);

  sprintf(string, "%d", ThisDialog.vw->imod->res);
  XmTextSetString(ThisDialog.wResolution, string);
        
  units = imodUnits(ThisDialog.vw->imod);
  if (units)
    sprintf(string, "%g %s", ThisDialog.vw->imod->pixsize, units);
  else
    sprintf(string, "%g", ThisDialog.vw->imod->pixsize);

  XmTextSetString(ThisDialog.wPixelSize, string);



}

static void setvw(void)
{
  char *string;
  float fscale;

  string = XmTextGetString(ThisDialog.wZscale);
  ThisDialog.vw->imod->zscale = atof(string);
  free(string);

  string = XmTextGetString(ThisDialog.wResolution);
  ThisDialog.vw->imod->res = atoi(string);
  free(string);

  string = XmTextGetString(ThisDialog.wPixelSize);
  setPixsizeAndUnits(ThisDialog.vw->imod, string);
  free(string);

}

/* DNM 12/21/02: interpret the pixel size from string and set into model */
void setPixsizeAndUnits(Imod *imod, char *string)
{
  float fscale = 0.;
  
  if (!string || string[0] == 0x00)
    return;
  fscale = atof(string);
  if (!fscale)
    return;
 
  /* Leave unchanged if zero, otherwise find the units */
  imod->pixsize = fscale;
  imod->units = IMOD_UNIT_PIXEL;
  if (strstr(string, "km"))
    imod->units = IMOD_UNIT_KILO;
  if (strstr(string, "m"))
    imod->units = IMOD_UNIT_METER;
  if (strstr(string, "cm"))
    imod->units = IMOD_UNIT_CM;
  if (strstr(string, "mm"))
    imod->units = IMOD_UNIT_MM;
  if (strstr(string, "um"))
    imod->units = IMOD_UNIT_UM;
  if (strstr(string, "nm"))
    imod->units = IMOD_UNIT_NM;
  if (strstr(string, "A"))
    imod->units = IMOD_UNIT_ANGSTROM;
  if (strstr(string, "pm"))
    imod->units = IMOD_UNIT_PM;
}


static void setvw_cb(Widget w, XtPointer client, XtPointer call)
{
  setvw();
}

static void modeldraw_cb(Widget w, XtPointer client, XtPointer call)
{
  ThisDialog.vw->imod->drawmode -= (2 * ThisDialog.vw->imod->drawmode);
  setwidgets();
  imodDraw(ThisDialog.vw, IMOD_DRAW_MOD);
}

static void timecopy_cb(Widget w, XtPointer client, XtPointer call)
{
  Imod *imod         = ThisDialog.vw->imod;
  int currentObject  = imod->cindex.object;
  int currentContour = imod->cindex.contour;
  int currentPoint   = imod->cindex.point;
  int currentTime    = ThisDialog.vw->ct;
  int nextTime       = currentTime + 1;
  int whichTime      = (int)client;
  char *string;
  Iobj  *obj;
  Icont *cont, *dupcont;
  int ob, co;
     
  if (whichTime){
    string = XmTextGetString(ThisDialog.wTimeCopy);
    nextTime = atoi(string);
    free(string);
  }

  if (nextTime > ThisDialog.vw->nt){
    wprint("Warning: Copy failed.\n"
	   "\tNext time point is invalid.\n");
    return;
  }

  for (ob = 0; ob < imod->objsize; ob++){
    obj = &imod->obj[ob];
    if (!iobjFlagTime(obj)) continue;
    imod->cindex.object = ob;
    for(co = 0; co < obj->contsize; co++){
      cont = &obj->cont[co];
      if (cont->type != currentTime)
	continue;
      dupcont = imodContourDup(cont);
      dupcont->type = nextTime;
      NewContour(imod);
      cont  = imodContourGet(imod);
      *cont = *dupcont;
      free(dupcont);
    }
  }

  imod->cindex.object  = currentObject;
  imod->cindex.contour = currentContour;
  imod->cindex.point   = currentPoint;
  imod_info_setocp();
  wprint("Time copy completed.\n");
  return;
}

static void workarea_cb(Widget w, XtPointer client, XtPointer call)
{
  Widget rowcol, grid;

  rowcol = XtVaCreateWidget
    ("rowcol", xmRowColumnWidgetClass, w, NULL);

  ThisDialog.wDraw = XtVaCreateManagedWidget
    ("Draw Model",  xmToggleButtonWidgetClass, rowcol, NULL);
  XtAddCallback(ThisDialog.wDraw, XmNvalueChangedCallback, 
		modeldraw_cb, NULL);

  grid = XtVaCreateWidget
    ("rowcol", xmRowColumnWidgetClass, rowcol, 
     XmNpacking, XmPACK_COLUMN,
     XmNnumColumns, 2,
     XmNorientation, XmVERTICAL,
     NULL);

  XtVaCreateManagedWidget
    ("Z-Scale", xmLabelWidgetClass, grid, NULL);
  XtVaCreateManagedWidget
    ("Resolution", xmLabelWidgetClass, grid, NULL);
  XtVaCreateManagedWidget
    ("Pixel Size", xmLabelWidgetClass, grid, NULL);

  ThisDialog.wZscale = XtVaCreateManagedWidget
    ("Zscale", xmTextWidgetClass, grid,  NULL);
  XtAddCallback(ThisDialog.wZscale, XmNactivateCallback, 
		setvw_cb, NULL);
  ThisDialog.wResolution = XtVaCreateManagedWidget
    ("Resolution", xmTextWidgetClass, grid,  NULL);
  XtAddCallback(ThisDialog.wResolution, XmNactivateCallback, 
		setvw_cb, NULL);
  ThisDialog.wPixelSize = XtVaCreateManagedWidget
    ("pixsize", xmTextWidgetClass, grid,  NULL);
  XtAddCallback(ThisDialog.wPixelSize, XmNactivateCallback, 
		setvw_cb, NULL);

  XtManageChild(grid);

  /* Add Time controls. */
  if (ThisDialog.vw->nt){
    Widget row, button;

    XtVaCreateManagedWidget
      ("separator", xmSeparatorWidgetClass, rowcol, NULL);

    button = XtVaCreateManagedWidget
      ("Copy Time Data to Next Index", 
       xmPushButtonWidgetClass, rowcol,  NULL);
    XtAddCallback(button, XmNactivateCallback, 
		  timecopy_cb, 0);
          
    row = XtVaCreateWidget
      ("rowcol", xmRowColumnWidgetClass, rowcol,
       XmNorientation, XmHORIZONTAL,
       NULL);
    button = XtVaCreateManagedWidget
      ("Copy Time Data to ", xmPushButtonWidgetClass, row, NULL);
    XtAddCallback(button, XmNactivateCallback, 
		  timecopy_cb, (XtPointer)1);

    ThisDialog.wTimeCopy = XtVaCreateManagedWidget
      ("label", xmTextWidgetClass, row, NULL);

    XtManageChild(row);
  }
     
  XtManageChild(rowcol);
  setwidgets();
  return;
}

/****************************************************************************/


static struct
{
  ModelOffsetWindow   *dia;
  ImodView      *vw;        /* image data to model                       */
  Ipoint        applied;
  Ipoint        base;

}OffsetDialog = { NULL, 0 };


static void imodTransXYZ(Imod *imod, Ipoint trans)
{
  int ob, co, pt,  me, i;
  Iobj *obj;
  Icont *cont;
  Imesh *mesh;
     
  for(ob = 0; ob < imod->objsize; ob++){
    obj = &(imod->obj[ob]);
    for(co = 0; co < obj->contsize; co++){
      cont = &(obj->cont[co]);
      for(pt = 0; pt < cont->psize; pt++){
	cont->pts[pt].x += trans.x;
	cont->pts[pt].y += trans.y;
	cont->pts[pt].z += trans.z;
      }
    }

    /* Translate the meshes too */
    for(me = 0; me < obj->meshsize; me++) {
      mesh = &obj->mesh[me];
      if (!mesh || !mesh->vsize)
	continue;
      for(i = 0; i < mesh->vsize; i += 2){
	mesh->vert[i].x += trans.x;
	mesh->vert[i].y += trans.y;
	mesh->vert[i].z += trans.z;
      }
    }
  }
}

int openModelOffset(ImodView *vw)
{
     
  if (OffsetDialog.dia){
    OffsetDialog.dia->raise();
    return(0);
  }
     
  OffsetDialog.vw = vw;
  OffsetDialog.base.x = OffsetDialog.base.y = OffsetDialog.base.z = 0.0;
  OffsetDialog.applied = OffsetDialog.base;

  OffsetDialog.dia = new ModelOffsetWindow(NULL, "model offset");

  imodDialogManager.add((QWidget *)OffsetDialog.dia, IMOD_DIALOG);
  return 0;
}


static char *buttonLabels[] = {"Apply", "Revert", "Set Base", "Done"};
static char *buttonTips[] = 
  {"Make total model offsets be entered values plus base values", 
   "Restore model offsets to zero", 
   "Make current offset be a base for further incremental offsets",
   "Close dialog box and permanently accept currently applied offsets"};


ModelOffsetWindow::ModelOffsetWindow(QWidget *parent, const char *name)
  : DialogFrame(parent, 4, buttonLabels, buttonTips, false, 
                " ", "", name)
{
  char *xyz[] = {"X", "Y", "Z"};
  QString str;
  QLabel *label;

  diaLabel("Total offsets to be applied:", this, mLayout);
  QGridLayout *grid = new QGridLayout(mLayout, 3, 3);

  for (int i = 0; i < 3; i++) {
    str = xyz[i];
    label = new QLabel(str + ":", this);
    grid->addWidget(label, i, 1);
    mEditBox[i] = new QLineEdit(this);
    grid->addWidget(mEditBox[i], i, 2);
    //   mEditBox[i]->setFocusPolicy(ClickFocus);
    connect(mEditBox[i], SIGNAL(returnPressed()), this, SLOT(valueEntered()));
    QToolTip::add(mEditBox[i], "Enter offset to apply in " + str);
    mBaseLabel[i] = new QLabel(" ", this);
    grid->addWidget(mBaseLabel[i], i, 3);
  }

  diaLabel("Current applied offset:", this, mLayout);
  mAppliedLabel = diaLabel("C", this, mLayout);
  updateLabels();

  connect(this, SIGNAL(actionPressed(int)), this, SLOT(buttonPressed(int)));
  setCaption(imodCaption("Imod Model Offset"));
  show();
}

void ModelOffsetWindow::buttonPressed(int which)
{
  Ipoint offset;
  Ipoint oldApply;
  int i;
  setFocus();
  switch (which) {
  case 0:  // Apply
    for (i = 0; i < 3; i++)
      *(&offset.x + i) = mEditBox[i]->text().toFloat();

    oldApply =  OffsetDialog.applied;
    OffsetDialog.applied.x = OffsetDialog.base.x + offset.x;
    OffsetDialog.applied.y = OffsetDialog.base.y + offset.y;
    OffsetDialog.applied.z = OffsetDialog.base.z + offset.z;
    offset.x = OffsetDialog.applied.x - oldApply.x;
    offset.y = OffsetDialog.applied.y - oldApply.y;
    offset.z = OffsetDialog.applied.z - oldApply.z;

    imodTransXYZ(OffsetDialog.vw->imod, offset);
    imodDraw(OffsetDialog.vw, IMOD_DRAW_MOD);
    updateLabels();
    break;

  case 1:  // revert
    offset.x = -OffsetDialog.applied.x;
    offset.y = -OffsetDialog.applied.y;
    offset.z = -OffsetDialog.applied.z;
    
    imodTransXYZ(OffsetDialog.vw->imod, offset);
    OffsetDialog.base.x = OffsetDialog.base.y = OffsetDialog.base.z = 0.0;
    OffsetDialog.applied = OffsetDialog.base;
    imodDraw(OffsetDialog.vw, IMOD_DRAW_MOD);
    updateLabels();
    break;

  case 2: // New  base
    OffsetDialog.base = OffsetDialog.applied;
    for (i = 0; i < 3; i++)
      mEditBox[i]->clear();
    updateLabels();
    break;

  case 3: // Done
    close();
    break;
  }
}

void ModelOffsetWindow::valueEntered()
{
  buttonPressed(0);
}

void ModelOffsetWindow::updateLabels()
{
  QString str;
  for (int i = 0; i < 3; i++){
    str.sprintf("+%9.2f base offset", *(&OffsetDialog.base.x + i));
    mBaseLabel[i]->setText(str);
  }
  str.sprintf("%9.2f,%9.2f,%9.2f", 
	      OffsetDialog.applied.x, OffsetDialog.applied.y,
	      OffsetDialog.applied.z);
  mAppliedLabel->setText(str);
}

void ModelOffsetWindow::closeEvent ( QCloseEvent * e )
{
  imodDialogManager.remove((QWidget *)OffsetDialog.dia);
  OffsetDialog.dia = NULL;
  e->accept();
}

void ModelOffsetWindow::keyPressEvent ( QKeyEvent * e )
{
  if (e->key() == Qt::Key_Escape)
    close();
  else
    ivwControlKey(0, e);
}

void ModelOffsetWindow::keyReleaseEvent ( QKeyEvent * e )
{
    ivwControlKey(1, e);
}

