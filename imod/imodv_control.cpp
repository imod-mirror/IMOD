/*  IMOD VERSION 2.50
 *
 *  imodv_control.c -- The imodv control edit dialog.
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

    $Log$
    Revision 1.1.2.6  2002/12/17 22:28:20  mast
    cleanup of unused variables and SGI errors

    Revision 1.1.2.5  2002/12/17 18:33:19  mast
    using new includes for imodv compoennts

    Revision 1.1.2.4  2002/12/13 06:08:40  mast
    simplification in dealing with filename string

    Revision 1.1.2.3  2002/12/07 01:21:44  mast
    Taking care of window title

    Revision 1.1.2.2  2002/12/06 22:12:13  mast
    *** empty log message ***

    Revision 1.1.2.1  2002/12/05 16:31:25  mast
    Qt version

    Revision 3.1  2002/12/01 15:34:41  mast
    Changes to get clean compilation with g++

*/
#include <qstring.h>
#include "formv_control.h"
#include <dia.h>
#define NO_X_INCLUDES
#include "imodv.h"
#include "imodP.h"
#include "imodv_control.h"
#include "imodv_gfx.h"
#include "imodv_input.h"

static imodvControlForm *dialog = NULL;
static float lastX = -999;
static float lastY = -999.;
static float lastZ = -999.;
static float lastScale = -999.;

#define ROTATION_MAX 100

////////////////////////////////////////
// Functions called from the form
//

void imodvControlHelp(void)
{
  dia_vasmsg("Imodv Controls Help\n",
             "-----------------------------------------------------\n",
             "This dialog controls model viewing and movement.\n\n",
             "\tThe Zoom Arrows ",
             "increase or decrease zoom factor.\n\n",
             "\tThe Scale text box shows the number of pixels on the "
             "screen per unit of model coordinates; you can enter a "
             "specific scale to control model zoom.\n\n"
             "\tThe Near and far sliders adjust the Z clipping planes.\n\n",
             "\tThe Z-scale slider adjusts the scale for section "
             "thickness.\n\n",
             "\tThe Rotation box edits model rotation. ",
             "The x, y or z axis arrow",
             "gadgets are equivalent to the hotkeys on the numeric keypad."
             "  If you press the Start/Stop Rotation button then one of "
             "the arrow gadgets, the model will start rotating around the "
             "given axis until you press the Start/Stop Rotation button "
             "again.  Otherwise, each arrow will cause a single step "
             "around the given axis.\n\n",
             "\tThe Degrees slider sets the number of degrees",
             "between views during rotation, as well as the step size for "
             "single steps with the arrows or mumeric pad keys.  The same "
             "step size is used when rotating an object clipping with "
             "Ctrl and the keypad keys.\n",
             NULL);
}


/* increase or decrease the zoom, depending on whether zoom > 0 */
void imodvControlZoom(int zoom)
{
  if (zoom > 0)
    imodv_zoomd(Imodv, 1.05);
  else
    imodv_zoomd(Imodv, 0.95238095);
  imodvDraw(Imodv);
  return;
}

/* Set the clipping or perspective values: near = plane 1, far = plane 0,
   perspective (fovy) = plane 2 */
void imodvControlClip(int plane, int value)
{
  ImodvApp *a = Imodv;
  Iview *view, *vwv;
  int m;

  if (!Imodv->imod) return;
  /*     view = &Imodv->imod->view[Imodv->imod->cview];*/
  view = Imodv->imod->view;

  if (plane == IMODV_CONTROL_FAR){
    Imodv->cfar = value;
    if (Imodv->cnear >= Imodv->cfar){
      Imodv->cnear = Imodv->cfar - 1;
      dialog->setViewSlider(IMODV_CONTROL_NEAR, a->cnear);
    }
    view->cnear = Imodv->cnear * 0.001;
    view->cfar  = Imodv->cfar * 0.001;
    if (Imodv->crosset)
      for (m = 0; m < Imodv->nm; m++) {
        vwv = Imodv->mod[m]->view;
        vwv->cfar = view->cfar;
        if (vwv->cnear >= vwv->cfar)
          vwv->cnear = vwv->cfar - 0.001;
      }
  }

  if (plane == IMODV_CONTROL_NEAR){
    Imodv->cnear = value;
    if (Imodv->cfar <= Imodv->cnear){
      Imodv->cfar = Imodv->cnear + 1;
      dialog->setViewSlider(IMODV_CONTROL_FAR, a->cfar);
    }
    view->cnear = Imodv->cnear * 0.001;
    view->cfar  = Imodv->cfar * 0.001;
    if (Imodv->crosset)
      for (m = 0; m < Imodv->nm; m++) {
        vwv = Imodv->mod[m]->view;
        vwv->cnear = view->cnear;
        if (vwv->cfar <= vwv->cnear)
          vwv->cfar = vwv->cnear + 0.001;
      }
  }

  if (plane == IMODV_CONTROL_FOVY) {
    Imodv->fovy = value;
    view->fovy  = Imodv->fovy;
    if (Imodv->crosset)
      for (m = 0; m < Imodv->nm; m++) 
        Imodv->mod[m]->view->fovy = view->fovy;
  }

  imodvDraw(Imodv);
}

void imodvControlZscale(int value)
{
  /*  int m, nm;
      nm = Imodv->nm; */
  Imodv->mod[Imodv->cm]->zscale = value / 100.0;
  /* DNM: this should not be adjusted in tandem in general */
  /*     if (Imodv->crosset)
         for(m = 0; m < nm; m++){
         Imodv->mod[m]->zscale = cbs->value / 10.0;
         }
  */
  imodvDraw(Imodv);
}

void imodvControlScale(float scale)
{
  ImodvApp *a = Imodv;
  int m;
  a->imod->view->rad = 
    0.5 * (a->winx > a->winy ? a->winy : a->winx) / scale;
  if (Imodv->crosset)
    for(m = 0; m < a->nm; m++)
      a->mod[m]->view->rad = a->imod->view->rad;
  imodvDraw(Imodv);
}


/* Start/stop movie */
void imodvControlStart(void)
{
  if (Imodv->movie)
    Imodv->movie = False;
  else
    Imodv->movie = True;
  
  Imodv->md->xrotm = 0;
  Imodv->md->yrotm = 0;
  Imodv->md->zrotm = 0;
  imodvDraw(Imodv);
}

/* Increase/decrease rotation angle using button */
void imodvControlAxisButton(int axisDir)
{
  ImodvApp *a = Imodv;
  switch(axisDir){
  case IMODV_CONTROL_XAXIS:
    imodv_rotate_model(a, 0, a->md->arot, 0);
    break;
  case -IMODV_CONTROL_XAXIS:
    imodv_rotate_model(a, 0, -a->md->arot, 0);
    break;
  case IMODV_CONTROL_YAXIS:
    imodv_rotate_model(a, a->md->arot, 0, 0);
    break;
  case -IMODV_CONTROL_YAXIS:
    imodv_rotate_model(a, -a->md->arot, 0, 0);
    break;
  case IMODV_CONTROL_ZAXIS:
    imodv_rotate_model(a, 0, 0, a->md->arot);
    break;
  case -IMODV_CONTROL_ZAXIS:
    imodv_rotate_model(a, 0, 0, -a->md->arot);
    break;
  }
  imodvDraw(Imodv);
}

/* Respond to a new rotation angle typed in to text box */
void imodvControlAxisText(int axis, float rot)
{
  ImodvApp *a = Imodv;
  int m;

  switch(axis){
  case IMODV_CONTROL_XAXIS:
    a->imod->view->rot.x = rot;
    if (a->moveall)
      for(m = 0; m < a->nm; m++)
        a->mod[m]->view->rot.x = rot;
    break;
  case IMODV_CONTROL_YAXIS:
    a->imod->view->rot.y = rot;
    if (a->moveall)
      for(m = 0; m < a->nm; m++)
        a->mod[m]->view->rot.y = rot;
    break;
  case IMODV_CONTROL_ZAXIS:
    a->imod->view->rot.z = rot;
    if (a->moveall)
      for(m = 0; m < a->nm; m++)
        a->mod[m]->view->rot.z = rot;
    break;
  }
  imodvDraw(Imodv);
}

/* A change in the rotation rate slider*/
void imodvControlRate(int value)
{
  Imodv->md->arot = value;
  if (Imodv->md->xrotm)
    Imodv->md->xrotm = Imodv->md->arot;
  if (Imodv->md->yrotm)
    Imodv->md->yrotm = Imodv->md->arot;
  if (Imodv->md->zrotm)
    Imodv->md->zrotm = Imodv->md->arot;
  imodvDraw(Imodv);
}

/* receive the signal that the dialog is really closing, and set to NULL */
void imodvControlClosing(void)
{
  dialog = NULL;
}

/* Receive the signal to quit from the dialog box */
void imodvControlQuit(void)
{
  dialog->close();
}

////////////////////////////////////////
// Functions called from other parts of imodv
//

/* Set a new rotation rate */
void imodvControlSetArot(ImodvApp *a, int newval)
{
  if (newval > ROTATION_MAX)
    newval = ROTATION_MAX;
  a->md->arot = newval;
  if (a->md->xrotm)
    a->md->xrotm = (a->md->xrotm > 0 ? 1 : -1 ) * a->md->arot;
  if (a->md->yrotm)
    a->md->yrotm = (a->md->yrotm > 0 ? 1 : -1 ) * a->md->arot;
  if (a->md->zrotm)
    a->md->zrotm = (a->md->zrotm > 0 ? 1 : -1 ) * a->md->arot;
  if (dialog)
    dialog->setRotationRate(a->md->arot);
}

/* Set the clipping, perspective, and z-scale sliders */
/* It turns out this is called on every display, so consider making a call
   only if something changes */
void imodvControlSetView(ImodvApp *a)
{
  if (!a->imod) return;
          
  /*DNM 11/30/02: should this really be an int?   are these radians? */
  a->fovy  = (int)a->imod->view->fovy;
  a->cnear = (int)(a->imod->view->cnear * 1000.0 + 0.5);
  a->cfar  = (int)(a->imod->view->cfar * 1000.0 + 0.5);
  if (!dialog) 
    return;

  /* Do not worry about sending out-of-range values.  Qt silently enforces
   its limits.  The only one that would get out of range is zscale, which the
   user might need.  If the user doesn't touch the slider, it stays at the set
   value; if they do, it snaps to the limit. */
  dialog->setViewSlider(IMODV_CONTROL_NEAR, a->cnear);
  dialog->setViewSlider(IMODV_CONTROL_FAR, a->cfar);
  dialog->setViewSlider(IMODV_CONTROL_FOVY, a->fovy);
  dialog->setViewSlider(IMODV_CONTROL_ZSCALE, 
                        (int) (a->imod->zscale * 100.0 + 0.5));
}

void imodvControlUpdate(ImodvApp *a)
{
  float scale;
  if (!dialog) 
    return;

  /* Only update the text boxes if they change */
  if (lastX != a->imod->view->rot.x) {
    lastX = a->imod->view->rot.x;
    dialog->setAxisText(IMODV_CONTROL_XAXIS, lastX);
  }
  if (lastY != a->imod->view->rot.y) {
    lastY = a->imod->view->rot.y;
    dialog->setAxisText(IMODV_CONTROL_YAXIS, lastY);
  }
  if (lastZ != a->imod->view->rot.z) {
    lastZ = a->imod->view->rot.z;
    dialog->setAxisText(IMODV_CONTROL_ZAXIS, lastZ);
  }

  scale = 0.5 * (a->winx > a->winy ? a->winy : a->winx) / 
    a->imod->view->rad;
  if (lastScale != scale) {
    lastScale = scale;
    dialog->setScaleText(lastScale);
  }
}

/* function for opening, closing, or raising the window */
int imodv_control(ImodvApp *a, int state)
{
  QString qstr;
  char *window_name;

  if (!state){
    if (dialog)
      dialog->close();
    return -1;
  }

  if (dialog){
    dialog->raise();
    return -1;
  }
  
  dialog = new imodvControlForm((QWidget *)a->mainWin, NULL, false,
                                Qt::WDestructiveClose);
  if (!dialog){
    dia_err("Failed to create imodv controls window!");
    return(-1);
  }
  window_name = imodwEithername("Imodv Controls: ", a->imod->fileName, 1);
  qstr = window_name;
  if (window_name)
    free(window_name);
  if (!qstr.isEmpty())
    dialog->setCaption(qstr);

  dialog->show();

  lastX = lastY = lastZ = lastScale = -999.;
  imodvControlUpdate(a);
  imodvControlSetArot(a, a->md->arot);
  imodvControlSetView(a);
    
  return(0);
}
