/*  IMOD VERSION 2.50
 *
 *  imod_info_cb.c -- Callback functions for the Information Window.
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
Revision 1.1.2.1  2003/01/06 15:52:16  mast
changes for Qt version of slicer

Revision 3.5.2.3  2002/12/19 04:37:12  mast
Cleanup of unused global variables and defines

Revision 3.5.2.2  2002/12/09 17:42:32  mast
remove include of zap

Revision 3.5.2.1  2002/12/05 16:29:32  mast
add include of imod_object_edit.h

Revision 3.5  2002/12/01 15:34:41  mast
Changes to get clean compilation with g++

Revision 3.4  2002/11/25 19:21:40  mast
In imod_info_setxyz, elimiated call to redraw pixelview; this is now
in the control list for redrawing

Revision 3.3  2002/09/27 19:54:14  rickg
Reverted calls to LoadModel to match changes to imod_io
Removed or commented out unreferenced variables.

Revision 3.2  2002/09/13 21:08:42  mast
Changed call to LoadModel to add NULL filename argument

Revision 3.1  2002/01/29 03:10:00  mast
Call imodDraw instead of xyz_draw after changing model/movie mode

*/

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "imod_object_edit.h"
#include "form_info.h"

#include "imod_info.h"
#include "imod.h"
#include "imod_info_cb.h"
#include "hotslider.h"

extern "C" {
int sampleMeanSD(unsigned char *image, int type, int nx, int ny, float sample, 
                 float matt, float *mean, float *sd);
}

/* Global variable: the forbid level, hope to eliminate */
int ImodForbidLevel = 0;

static int ctrlPressed = 0;
static int Imod_obj_cnum = -1;
static int float_on = 0;

/*
 * FUNCTIONS FOR THE CONTROLS TO REPORT CHANGES
 *
 * New object, contour, or point
 */
void imodInfoNewOCP(int which, int value, int edited)
{
  int ob, co, pt;
  Imod *imod = App->cvi->imod;
  imodGetIndex(imod, &ob, &co, &pt);

  ImodInfoWin->setFocus();

  // Get to index value; if it is illegal, try to refresh window and return
  value--;
  if (value < 0) {
    imod_info_setocp();
    return;
  }

  switch (which) {
  case 0:
    // Object change.  If via the edit box, detach from contour/point
    if (edited) {
      imod->cindex.object = value;
      imod->cindex.contour =  - 1;
      imod->cindex.point =  - 1;
    } else {
      imodSetIndex(imod, value, co, pt);
      inputKeepContourAtSameTime(App->cvi);
    }
    break;

  case 1:
    // Contour change.  If via edit box, detach from point
    if (edited) {
      imod->cindex.contour = value;
      imod->cindex.point = -1;
    } else {
      imodSetIndex(imod, ob, value, pt);
      inputRestorePointIndex(App->cvi);
    }
    break;

  case 2:
    // Point change
    imodSetIndex(imod, ob, co, value);
    break;
  }

  imod_setxyzmouse();
  if (!which)
    imod_object_edit_draw();   // CHECK FOR REDUNDANCY
}

/*
 * New point position
 */
void imodInfoNewXYZ(int *values)
{
  ImodInfoWin->setFocus();
  App->cvi->xmouse = values[0] - 1;
  App->cvi->ymouse = values[1] - 1;
  App->cvi->zmouse = values[2] - 1 ;
  imodDraw(App->cvi, IMOD_DRAW_XYZ);
}

/*
 * New positions of the black/white sliders
 */
void imodInfoNewBW(int which, int value, int dragging)
{
  int white, black;
  int float_save = float_on;

  // THIS IS REALLY SCARY UNLESS WE ASSERT THE SLIDERS BACK
  if (ImodForbidLevel) {
    ImodInfoWidget->setBWSliders(App->cvi->black, App->cvi->white);
    return;
  }

  // Exit if RGBA and there is not a hot slider active
  if (App->rgba && dragging && 
      ((ctrlPressed && hotSliderFlag() != HOT_SLIDER_KEYDOWN) || 
       (!ctrlPressed && hotSliderFlag() != HOT_SLIDER_KEYUP)))
    return;

  // Keep the sliders from crossing
  if (which) {
    white = value;
    black = App->cvi->black;
    if (black > white) {
      black = white;
      ImodInfoWidget->setBWSliders(black, white);
    }
  } else {
    white = App->cvi->white;
    black = value;
    if (black > white) {
      white = black;
      ImodInfoWidget->setBWSliders(black, white);
    }
  }

  xcramp_setlevels(App->cvi->cramp,black,white);
  App->cvi->black = black;
  App->cvi->white = white;

  /* Set the float flag to false to prevent this change from being 
     undone in a redraw */
  float_on = FALSE;
  imod_info_setbw(black, white);
  float_on = float_save;
}

/*
 * Float button, movie-model mode, ctrl key, and quit
 */
void imodInfoFloat(int state)
{
  if (state) {
    float_on = 1;
    imod_info_setbw(App->cvi->black, App->cvi->white);
  } else
    float_on = 0;
}

/* DNM 6/8/01: fixed bug in getting mode, changed to pass mode to function */
void imodInfoMMSelected(int mode)
{
  imod_set_mmode(mode ? IMOD_MMODEL : IMOD_MMOVIE);

  /* DNM 1/28/02: change from drawing xyz to general drawing */
  imodDraw(App->cvi, IMOD_DRAW_MOD);
  return;
}

void imodInfoCtrlPress(int pressed)
{
  ctrlPressed = pressed;
}

void imodInfoQuit()
{
  imod_quit();
  return;
}


/****************************************************************************/
/* support functions for setting controls                                   */
/****************************************************************************/

/*
 * Set the object color in the controls
 */
void imod_info_setobjcolor(void)
{
  int red, green, blue;
  double clev, th = 32.;
  Iobj *obj;

  obj = imodel_object_get(Model);
  if (!obj){
    red = green = blue = 128;
  }else{
    red   = (int)(255 * obj->red);
    green = (int)(255 * obj->green);
    blue  = (int)(255 * obj->blue);
  }
  QColor backColor(red, green, blue);

  // Observations on 1/12/03: black is hard to see on pure red below 240,
  // and on pure green below 175.
  // Pure blue at 255 is as hard to see as pure green at 90, so its threshold
  // is 255*175/90 = 495
  // But then need to subtract a threshold because dim colors tend not to
  // add correctly
  clev = (red > th ? (red - th) / (240. - th) : 0) + 
    (green > th ? (green - th) / (175. - th) : 0) + 
    (blue > th ? (blue - th) / (495. - th) : 0);
  red = clev > 1.0 ? 0 : 255;

  QColor foreColor(red, red, red);

  ImodInfoWidget->setObjectColor(foreColor, backColor);

  /* This was here for Color index mode - hard to see what it did
    XStoreColors(XtDisplay(App->toplevel), App->cmap, &color, 1);
    if (App->cmap != App->cmapGL)
      XStoreColors(XtDisplay(App->toplevel), App->cmapGL, &color, 1);
  */
}

/*
 * Set the object, contour, and point.
 * InfoControls figures out if things have changed
 */
void imod_info_setocp(void)
{
  Imod *imod = Model;
  int val[3], max[3];
  Iobj *obj;
  Icont *cont;
     
  obj = imodel_object_get(imod);
  cont = imodContourGet(imod);
     
  // Object is either present or not
  max[0] = imod->objsize;
  val[0] = obj ? imod->cindex.object + 1 : 0;

  // If object present, set up contour values; otherwise set for blanks
  if (obj) {
    max[1] = obj->contsize;
    val[1] = cont ? imod->cindex.contour + 1 : 0;
  } else {
    max[1] = -1;
    val[1] = -1;
  }

  // If contour present, set up point values; otherwise set for blanks
  if (cont) {
    max[2] = cont->psize;
    val[2] = cont->psize ? imod->cindex.point + 1 : 0;
  } else {
    max[2] = -1;
    val[2] = -1;
  }
 
  // Send values
  ImodInfoWidget->updateOCP(val, max);
  
  // Update color if object has changed
  if ((Imod_obj_cnum != imod->cindex.object)
      && (imod->cindex.object != -1)){

    Imod_obj_cnum = imod->cindex.object;
    imod_info_setobjcolor();

  }
  contSurfShow();
  inputContourMoveDialogUpdate();
}

/*
 * Set X, Y, Z
 * InfoControls figures out if things have changed
 */
void imod_info_setxyz(void)
{
  int xyz[3], xyzs[3];

  ivwBindMouse(App->cvi);
  xyz[0] = (int)(App->cvi->xmouse + 1);
  xyz[1] = (int)(App->cvi->ymouse + 1);
  xyz[2] = (int)(App->cvi->zmouse + 1);
  xyzs[0] = App->cvi->xsize;
  xyzs[1] = App->cvi->ysize;
  xyzs[2] = App->cvi->zsize;

  ImodInfoWidget->updateXYZ(xyz, xyzs);
}

/* Static variables for the keeping track of floating */
static int ref_section;
static int ref_black = 0;
static int ref_white = 255;
static int last_section = 0;
static int ref_time;
static int last_time = 0;
static float *sec_mean = NULL;
static float *sec_sd = NULL;
static int table_size = 0;
static int tdim = 0;

/*
 * Set the black/white sliders, draw if necessary
 */
void imod_info_setbw(int black, int white)
{
  static int oblack = 0;
  static int owhite = 255;
  int remute = FALSE;

  if (oblack != black || owhite != white){
    oblack = black;
    owhite = white;
    ImodInfoWidget->setBWSliders(black, white);
    remute = TRUE;
  }

  /* if we are using a colormap that isn't
   * mutable then we need to redraw all image data.
   * use the IMOD_DRAW_IMAGE flag to redraw all image
   * data and clear all image caches, and IMOD_DRAW_NOSYNC to prevent
   * panning the zap window to the current model point
   */
  if ((remute) && (App->rgba)){
    imodDraw(App->cvi, IMOD_DRAW_IMAGE | IMOD_DRAW_NOSYNC);
  }

  /* DNM: set this information as values for a new reference section for
     floated intensities */
  ref_section = last_section;
  ref_time = last_time;
  ref_black = black;
  ref_white = white;
  return;
}


/* Implements floating; i.e. adjusting of sliders according to changes in the
   mean and SD between images 
   Returns 0 if nothing was changed, or 1 if black/white levels changed */
int imod_info_bwfloat(ImodView *vw, int section, int time)
{
  float sample, matt;
  int i, newwhite, newblack, err1;
  int save_ref_sec,save_ref_black, save_ref_white, save_ref_time;
  int needsize, iref, isec;
  float sloperatio;
  unsigned char *image;
  int retval = 0;

  if (float_on) {

    /* Make sure table exists and is the right size */
    tdim = ivwGetMaxTime(vw) + 1;
    needsize = vw->zsize * tdim;
    if (table_size == 0) {
      sec_mean = (float *)malloc(needsize * sizeof(float));
      sec_sd = (float *)malloc(needsize * sizeof(float));
    } else if (table_size != needsize) {
      sec_mean = (float *)realloc(sec_mean, 
                                  needsize * sizeof(float));
      sec_sd = (float *)realloc(sec_sd, needsize * sizeof(float));
    }

    if (!sec_mean || !sec_sd) {
      imod_info_float_clear(-1, -1);
      return 0;
    }
	  
    /* Clear out any new entries */
    if (table_size < needsize)
      for (i = table_size; i < needsize; i++)
        sec_mean[i] = sec_sd[i] = -1;
    table_size = needsize;

    if ((ref_section + 1) * (ref_time + 1) > table_size ||
        (section + 1) * (time + 1) > table_size)
      return 0;

    /* Get information about reference and 
       current sections if necessary */

    if (time > 0 && ref_time == 0)
      ref_time = 1;

    matt = 0.05;
    sample = 10000.0/(vw->xsize*vw->ysize);
    if (sample > 1.0)
      sample = 1.0;
	  
    err1 = 0;
    iref = tdim * ref_section + ref_time;
    isec = tdim * section + time;
    if (sec_sd[iref] < 0 ) {
      image = ivwGetZSectionTime(vw, ref_section, ref_time);
      err1 = sampleMeanSD(image, 0, vw->xsize, vw->ysize, sample,
                          matt, &sec_mean[iref], &sec_sd[iref]);
    }
	       
    if (!err1 && sec_sd[isec] < 0 ) {
      image = ivwGetZSectionTime(vw, section, time);
      err1 = sampleMeanSD(image, 0, vw->xsize, vw->ysize, sample,
                          matt, &sec_mean[isec], &sec_sd[isec]);
    }
	       
    if (!err1) {
	    
      /* Compute new black and white sliders */
      sloperatio = sec_sd[isec] / sec_sd[iref];

      newblack = (int)(sec_mean[isec] - 
        (sec_mean[iref] - ref_black) * sloperatio + 0.5);
      newwhite = (int)(newblack + sloperatio * (ref_white - ref_black) + 0.5);
		    
      if (newblack < 0)
        newblack = 0;
      if (newwhite > 255)
        newwhite = 255;

      /* Set the sliders and the ramp; save and restore
         reference section information */
      if (newwhite != vw->white || newblack != vw->black) {
        vw->black = newblack;
        vw->white = newwhite;
        xcramp_setlevels(vw->cramp, vw->black, vw->white);
        save_ref_sec = ref_section;
        save_ref_time = ref_time;
        save_ref_black = ref_black;
        save_ref_white = ref_white;
        imod_info_setbw(vw->black, vw->white);
        ref_section = save_ref_sec;
        ref_time = save_ref_time;
        ref_black = save_ref_black;
        ref_white = save_ref_white;
        retval = 1;
      }
    }
  }

  last_section = section;
  last_time = time;
  return retval;
}

/* Clear the information for floating sections - for one section or all 
   section < 0 and time < 0: clear entire table
   section = - number of sections and time >=0; clear all at this time
   section >= 0, time >= 0; just clear this section
*/
void imod_info_float_clear(int section, int time)
{
  int i;

  if (section < 0 && time < 0) {
    if (sec_mean)
      free(sec_mean);
    if (sec_sd)
      free(sec_sd);
    sec_mean = NULL;
    sec_sd = NULL;
    table_size = 0;
  } else if (section < 0) {
    if ((-section) * tdim + time >= table_size)
      return;
    for (i = 0; i < -section; i++) {
      sec_mean[i * tdim + time] = -1;
      sec_sd[i * tdim + time] = -1;
    }
  } else if (section * tdim + time < table_size) {
    sec_mean[section * tdim + time] = -1;
    sec_sd[section * tdim + time] = -1;
  }
  return;
}

/****************************************************************************/
/*  Imod link functions */

int imod_info_input(void)
{
  //     XEvent event_return;
  XFlush(XtDisplay(App->toplevel));
  /* This loop will execute if there is any pending event but will only
     process an X event. It was here originally.  */
  /*     while(XtAppPending(App->context)){
	 XtAppNextEvent(App->context, &event_return);
	 XtDispatchEvent(&event_return);
	 } */

  /* This loop will execute only if there is an X event, and process it.
     It would avoid hanging up waiting for input */
  /* while(XtAppPending(App->context) & XtIMXEvent){
     XtAppNextEvent(App->context, &event_return);
     XtDispatchEvent(&event_return);
     } */

  /* This loop will execute if there is any pending event and process all
     kinds of events.  It seems good to process all events... */
  while(XtAppPending(App->context) & XtIMAll)
    XtAppProcessEvent(App->context, XtIMAll);


  return(0);
}

int imod_open(FILE *mfin)
{
  if (mfin == NULL)
    /* new model */
    {
      Model = imodNew();
      imodNewObject(Model);
    }
  else
    {
      Model = (struct Mod_Model *)LoadModel(mfin);
      if (Model == NULL){
        return(-1);
      }
    }
  return(0);
}

void show_status(char *info)
{
  if (!info)
    return;

  imod_info_msg(info, " ");
  return;
}

// Unused
void imod_show_info(char *info, int line)
{
  if (!info)
    return;
     
  if (line == 1)
    imod_info_msg(info, NULL);
  if (line == 2)
    imod_info_msg(NULL, info);
  return;
}

void imod_info_msg(char *top, char *bot)
{
  if (top){
    wprint("%s\n", top);
  }

  if (bot){
    wprint("%s\n", bot);
  }
}

void imod_info_forbid(void)
{
  ImodForbidLevel++;
}

void imod_info_enable(void)
{
  ImodForbidLevel--;
  if (ImodForbidLevel < 0)
    ImodForbidLevel = 0;
}

/* DNM 6/8/01: changed so that it gets called with the actual mode to be
   changed to, or with a different value to toggle the mode */
void imod_set_mmode(int mode)
{
  if (Model){
    if (mode == IMOD_MM_TOGGLE) {
      if (Model->mousemode == IMOD_MMOVIE)
        mode = IMOD_MMODEL;
      else
        mode = IMOD_MMOVIE;
    }

    Model->mousemode = mode;
    ImodInfoWidget->setMovieModel(mode == IMOD_MMOVIE ? 0 : 1);

    if (mode == IMOD_MMODEL) {
      App->cvi->xmovie = App->cvi->ymovie = App->cvi->zmovie = 
        App->cvi->tmovie = 0;
    }
  }
  imodDraw(App->cvi, IMOD_DRAW_MOD | IMOD_DRAW_NOSYNC);
}

void imod_draw_window(void)
{
  if (Model){
    imod_info_setocp();
  }
  if (App->cvi){
    imod_info_setxyz();
  }
}

void imod_imgcnt(char *string)
{
  wprint("%s\n%s\r", Statstring, string);
  imod_info_input();
}
