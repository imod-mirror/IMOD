/*
 *  imodv_control.c -- The imodv control edit dialog.
 *
 *  Original author: James Kremer
 *  Revised by: David Mastronarde   email: mast@colorado.edu
 *
 *  Copyright (C) 1995-2004 by Boulder Laboratory for 3-Dimensional Electron
 *  Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *  Colorado.  See dist/COPYRIGHT for full copyright notice.
 */

/*  $Author$
    
$Date$

$Revision$
Log at end of file
*/

#include <qstring.h>
#include "formv_control.h"
#include "dia_qtutils.h"
#include "imodv.h"
#include "imod.h"
#include "imodv_control.h"
#include "imodv_gfx.h"
#include "imodv_input.h"
#include "imod_model_edit.h"
#include "control.h"

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
  dia_vasmsg("3dmodv Controls Help\n",
             "-----------------------------------------------------\n",
             "This dialog controls model viewing and movement.\n\n",
             "\tThe Zoom Arrows ",
             "increase or decrease zoom factor.\n\n",
             "\tThe Scale text box shows the number of pixels on the "
             "screen per unit of model coordinates; you can enter a "
             "specific scale to control model zoom.\n\n"
             "\tThe Near and Far sliders adjust the Z clipping planes.  When "
             "the model is zoomed up, it will be clipped more for a given "
             "setting of these sliders.  To prevent undesired clipping with "
             "very high zooms, near clipping is effectively disabled with "
             "Near at 0, and far clipping is disabled with Far at 1000.\n\n",
             "\tThe Z-scale slider adjusts the scale for section "
             "thickness relative to the pixel size in X and Y.\n\n",
             "\tThe Rotation box edits model rotation. ",
             "The X, Y or Z axis arrow",
             "buttons are equivalent to the hot keys on the numeric keypad.  "
             "In addition, you can type specific angles into the text boxes.\n"
             "\tIf you press the Start/Stop Rotation button then one of "
             "the arrow buttons, the model will start rotating around the "
             "given axis until you press the Start/Stop Rotation button "
             "again.  Otherwise, each arrow will cause a single step "
             "around the given axis.\n",
             "\tThe speed controls allow you adjust the rate of rotation in "
             "degrees per second (\"deg/sec\").  Press the arrow buttons to "
             "increase or decrease the speed, or enter a specific value in "
             "the text box.\n",
             "\tThe Degrees slider sets the the step size for "
             "single steps with the arrows or numeric pad keys.  The same "
             "step size is used when rotating an object clipping plane with "
             CTRL_STRING" and the keypad keys.\n",
             "\tNote that the . and , hot keys will increase both the step "
             "size and rotation speed in tandem, whereas the controls in this "
             "dialog box allow you to adjust them independently.",
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
void imodvControlClip(int plane, int value, bool dragging)
{
  ImodvApp *a = Imodv;
  Iview *view, *vwv;
  int m;
  static bool sliding = false;

  if (!Imodv->imod) return;
  /*     view = &Imodv->imod->view[Imodv->imod->cview];*/
  view = Imodv->imod->view;

  if (!sliding) {
    imodvRegisterModelChg();
    imodvFinishChgUnit();
  }
  sliding = dragging;

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

void imodvControlZscale(int value, bool dragging)
{
  static bool sliding = false;
  if (!sliding) {
    imodvRegisterModelChg();
    imodvFinishChgUnit();
  }
  sliding = dragging;

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
  imodModelEditUpdate();
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
    Imodv->movie = 0;
  else
    Imodv->movie = 1;
  
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
    imodv_rotate_model(a, a->md->arot, 0, 0);
    break;
  case -IMODV_CONTROL_XAXIS:
    imodv_rotate_model(a, -a->md->arot, 0, 0);
    break;
  case IMODV_CONTROL_YAXIS:
    imodv_rotate_model(a, 0, a->md->arot, 0);
    break;
  case -IMODV_CONTROL_YAXIS:
    imodv_rotate_model(a, 0, -a->md->arot, 0);
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
  /* DNM 11/3/03: do not change movie rates with new constant-speed scheme */
}

/* Change in the movie spped */
void imodvControlSpeed(float value)
{
  Imodv->movieSpeed = value;
}

void imodvControlIncSpeed(int step)
{
  if (step > 0) {
    Imodv->movieSpeed *= IMODV_ROTATION_FACTOR;
    if (Imodv->movieSpeed > 360.)
      Imodv->movieSpeed = 360.;
  } else {
    Imodv->movieSpeed /= IMODV_ROTATION_FACTOR;
    if (Imodv->movieSpeed < 3.6)
      Imodv->movieSpeed = 3.6;
  }
  if (dialog)
    dialog->setSpeedText(Imodv->movieSpeed);
}


/* receive the signal that the dialog is really closing, and set to NULL */
void imodvControlClosing(void)
{
  imodvDialogManager.remove((QWidget *)dialog);
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
  /* DNM 11/3/03: do not change movie rates with new constant-speed scheme */

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
  
  dialog = new imodvControlForm(imodvDialogManager.parent(IMODV_DIALOG), NULL,
                                Qt::WDestructiveClose | Qt::WType_TopLevel);
  if (!dialog){
    dia_err("Failed to create 3dmodv controls window!");
    return(-1);
  }
  window_name = imodwEithername("3dmodv Controls: ", a->imod->fileName, 1);
  qstr = window_name;
  if (window_name)
    free(window_name);
  if (!qstr.isEmpty())
    dialog->setCaption(qstr);

  imodvDialogManager.add((QWidget *)dialog, IMODV_DIALOG);
  dialog->show();

  lastX = lastY = lastZ = lastScale = -999.;
  imodvControlUpdate(a);
  imodvControlSetArot(a, a->md->arot);
  imodvControlSetView(a);
  dialog->setSpeedText(a->movieSpeed);
    
  return(0);
}

/*
    $Log$
    Revision 4.7  2004/06/10 00:33:02  mast
    Documented new behavior of clip planes

    Revision 4.6  2003/11/04 04:42:46  mast
    Add new calls for rotation speed and remove code for changing [xyz]rotm

    Revision 4.5  2003/10/01 05:04:19  mast
    change include from imodP to imod after eliminating imod.h from imodP.h

    Revision 4.4  2003/04/28 04:00:56  mast
    Fix help text on hotkey and gadget

    Revision 4.3  2003/04/25 03:28:32  mast
    Changes for name change to 3dmod

    Revision 4.2  2003/04/17 18:43:38  mast
    adding parent to window creation

    Revision 4.1  2003/02/10 20:29:00  mast
    autox.cpp

    Revision 1.1.2.12  2003/01/27 00:30:07  mast
    Pure Qt version and general cleanup

    Revision 1.1.2.11  2003/01/23 20:09:19  mast
    update z scale in model-header dialog

    Revision 1.1.2.10  2003/01/18 01:10:17  mast
    add include of dia_qtutils

    Revision 1.1.2.9  2003/01/13 07:21:38  mast
    Changes to use new dialog manager class

    Revision 1.1.2.8  2002/12/30 06:49:50  mast
    rationalizing dialogs as widgets and using dialog list

    Revision 1.1.2.7  2002/12/23 05:00:25  mast
    Make imodv mainwindow be parent

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
