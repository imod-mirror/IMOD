/*  IMOD VERSION 2.50
 *
 *  imod_object_edit.c -- Edit how objects are drawn in 2D views
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
Revision 1.1.2.1  2002/12/05 16:30:58  mast
Qt version

Revision 3.1  2002/12/01 15:34:41  mast
Changes to get clean compilation with g++

*/

#include "object_edit.h"
#include <diaP.h>
#define NO_X_INCLUDES
#include "imod.h"
#include "imod_object_edit.h"

static objectEditForm *Ioew_dialog;

static Iobj *getObjectOrClose(void);

#define MAX_SYMBOLS  4
static int symTable[MAX_SYMBOLS] = 
  { IOBJ_SYM_NONE, IOBJ_SYM_CIRCLE, IOBJ_SYM_SQUARE, IOBJ_SYM_TRIANGLE };

void ioew_sgicolor_cb(Widget w, XtPointer client, XtPointer call)
{
  DiaColorCallbackStruct *cbs = (DiaColorCallbackStruct *) call;
  int ob = (int)client;
  Iobj *obj;

  switch(cbs->reason){
  case DIA_INIT:
    break;
  case DIA_APPLY:
  case DIA_OK:
  case DIA_CANCEL:
  case DIA_SLIDER_CHANGED:
  case DIA_SLIDER_DRAG:
    if (ob >= Model->objsize)
      return;
    obj = &(Model->obj[ob]);
    obj->red = cbs->red / 255.0;
    obj->green = cbs->green / 255.0;
    obj->blue = cbs->blue / 255.0;

    /* DNM: if TrueColor, need to free old color and allocate new one */
    /* well, maybe not, but in any case, need to redraw unless it's a 
       drag */
    if (App->rgba) {
      if (cbs->reason == DIA_SLIDER_DRAG)
        break;
      free_object_colors(Model, ob, ob);
      alloc_object_colors(Model, ob, ob);
      imodDraw(App->cvi, IMOD_DRAW_MOD);
    }
    break;
  default:
    break;
  }
  imod_cmap(Model);
  imod_info_setobjcolor();

  return;
}

/* 12/1/02: Eliminated unused ioew_color_cb */

void ioew_help(void)
{
  dia_vasmsg("Object Type Help\n",
             "-------------------\n",
             "This dialog edits the current object. ",
             "One can leave this dialog open and change the current ",
             "object.\n\n"
             "Object Name:\n",
             "\tEnter a name for the object.\n\n",
             "Draw:\n",
             "\tTurns drawing on/off in image windows.\n\n",
             "Symbols:\n",
             "\tChooses how symbols are drawn.  Open or filled symbols of "
             "three shapes may be drawn at each model point.  Symbol size "
             "is governed by the Size slider.  Independent of whether "
             "symbols are drawn at each point, if Mark Ends is selected, "
             "green and red crosses are drawn over the first and last "
             "points of a contour.\n\n",
             "Line Width:\n",
             "\tSets the width for lines drawn on images, but not for "
             "lines in 3D.\n\n",
             "Time data:\n",
             "\tIf multiple image files are loaded, this toggle button "
             "appears next to control whether time information is encoded "
             "in contours as they are drawn.  If the button is on, then "
             "each new contour that is created will be assigned to the "
             "currently displayed time, and it will appear only over "
             "images at that time.  In addition, the Time Index text box "
             "in the Edit-Contour-Type window can be used to adjust the "
             "time value of a contour.  If the button is off, then new "
             "contours will not be assigned to the current time but "
             "rather will have a time value of 0 and will appear over "
             "images at all times.\n\n"
             "Open/Closed/Scattered Toggles:\n",
             "\tSet how points in object are connected.  Open and closed "
             "contour objects are drawn with lines between the points; "
             "closed contours have a line connecting the last point back "
             "to the first one; scattered point objects have no "
             "connecting lines.\n\n",
             "Front Face:\n",
             "\tOutside/Inside toggles select which side of the contours "
             "will be brightly lit after the object is meshed.  This "
             "feature can also be used to select an area of interest as "
             "inside or outside the contours, for some programs.\n\n",
             "Scattered point 3-D radius:\n",
             "\tIf 3D Sphere size is nonzero, then spheres will be drawn "
             "in 3-D.  These spheres appear in"
             " the model view window and appear in cross-section on "
             "one or more slices of the image display.\n",
             NULL);
}

void ioew_draw(int state)
{
  Iobj *obj = getObjectOrClose();
  if (!obj)
    return;
  if (state)
    obj->flags = obj->flags & ~IMOD_OBJFLAG_OFF;
  else
    obj->flags = obj->flags | IMOD_OBJFLAG_OFF;

  imodDraw(App->cvi, IMOD_DRAW_MOD);
}

/* 12/1/02: eliminated unused ioew_trans_cb */

void ioew_fill(int state)
{
  Iobj *obj = getObjectOrClose();
  if (!obj)
    return;
  if (!state)
    obj->symflags = obj->symflags & ~IOBJ_SYMF_FILL;
  else
    obj->symflags = obj->symflags | IOBJ_SYMF_FILL;

  imodDraw(App->cvi, IMOD_DRAW_MOD);
}

void ioew_ends(int state)
{
  Iobj *obj = getObjectOrClose();
  if (!obj)
    return;
  if (!state)
    obj->symflags = obj->symflags & ~IOBJ_SYMF_ENDS;
  else
    obj->symflags = obj->symflags | IOBJ_SYMF_ENDS;
     
  imodDraw(App->cvi, IMOD_DRAW_MOD);
}

void ioew_linewidth(int value)
{
  Iobj *obj = getObjectOrClose();
  if (!obj)
    return;
  obj->linewidth2 = value;
  imodDraw(App->cvi, IMOD_DRAW_MOD);
}

void ioew_open(int value)
{
  Iobj *obj = getObjectOrClose();
  if (!obj)
    return;

  switch (value){
  case 1:
    obj->flags = obj->flags & (~IMOD_OBJFLAG_OPEN);
    obj->flags = obj->flags & (~IMOD_OBJFLAG_SCAT);
    break;
  case 0:
    obj->flags = obj->flags | IMOD_OBJFLAG_OPEN;
    obj->flags = obj->flags & (~IMOD_OBJFLAG_SCAT);
    break;
  case 2:
    /* scattered */
    obj->flags = obj->flags | IMOD_OBJFLAG_SCAT;
    obj->flags = obj->flags | IMOD_OBJFLAG_OPEN;
    break;
  }
  imodDraw(App->cvi, IMOD_DRAW_MOD);
}

void ioew_surface(int value)
{
  Iobj *obj = getObjectOrClose();
  if (!obj)
    return;

  /* TODO: make sure this is the right polarity */
  if (!value) {
    obj->flags &= ~ IMOD_OBJFLAG_OUT;
  } else {
    obj->flags |= IMOD_OBJFLAG_OUT;
  }

  imodDraw(App->cvi, IMOD_DRAW_MOD);
}


void ioew_pointsize(int value)
{
  Iobj *obj = getObjectOrClose();
  if (!obj)
    return;
  obj->pdrawsize = value;
  imodDraw(App->cvi, IMOD_DRAW_MOD);
}

void ioew_nametext(const char *name)
{
  int i;
  Iobj *obj = getObjectOrClose();
  if (!obj)
    return;

  if (name){
    for(i = 0; (i < (IMOD_STRSIZE - 1))&&(name[i]); i++)
      obj->name[i] = name[i];
    obj->name[i] = 0x00;
  }
}

void ioew_symbol(int value)
{
  Iobj *obj = getObjectOrClose();
  if (!obj)
    return;

  obj->symbol = symTable[value];
  imodDraw(App->cvi, IMOD_DRAW_MOD);
}

void ioew_symsize(int value)
{
  Iobj *obj = getObjectOrClose();
  if (!obj)
    return;
  obj->symsize = value;
     
  imodDraw(App->cvi, IMOD_DRAW_MOD);
}

void ioew_time(int state)
{
  Iobj *obj = getObjectOrClose();
  if (!obj)
    return;

  if (!state){
    obj->flags &= ~IMOD_OBJFLAG_TIME;
  }else{
    obj->flags |= IMOD_OBJFLAG_TIME;
  }
  imodDraw(App->cvi, IMOD_DRAW_MOD);
}

int imod_object_edit(Widget top)
{
  QString qstr;
  char *window_name;

  Iobj *obj = imodObjectGet(Model);
  if (!obj){
    dia_err("No Object selected");
    return(-1);
  }

  if (Ioew_dialog){
    Ioew_dialog->raise();
    return(0);
  }
     
  Ioew_dialog = new objectEditForm(NULL, NULL, false, Qt::WDestructiveClose);

  if (!Ioew_dialog){
    dia_err("Object edit failed.");
    return(-1);
  }

  window_name = imodwfname("Imod Object Edit:");
  if (window_name) {
    qstr = window_name;
    free(window_name);
    Ioew_dialog->setCaption(qstr);
  }

  Ioew_dialog->show();

  imod_object_edit_draw();
  return(0);
}


int imod_object_edit_draw(void)
{
  int state = 0;
  int symbol = 0;
  int i;
  Iobj *obj;
  if (!Ioew_dialog)
    return(-1);

  obj = getObjectOrClose();
  if (!obj)
    return (-1);

  Ioew_dialog->setObjectName(obj->name);
  Ioew_dialog->setDrawBox(!(obj->flags & IMOD_OBJFLAG_OFF));

  for (i = 0; i < MAX_SYMBOLS; i++) {
    if (obj->symbol == symTable[i]) {
      symbol = i;
      break;
    }
  }
  Ioew_dialog->setSymbolProperties(symbol, 
                                   obj->symflags & IOBJ_SYMF_FILL != 0,
                                   obj->symflags & IOBJ_SYMF_ENDS != 0, 
                                   (int)obj->symsize);
  Ioew_dialog->setLineWidth((int)obj->linewidth2);
  Ioew_dialog->setTimeBox((obj->flags & IMOD_OBJFLAG_TIME) != 0,
                          App->cvi->nt != 0);

  if (obj->flags & IMOD_OBJFLAG_SCAT)
    state = 2;
  else if (obj->flags & IMOD_OBJFLAG_OPEN)
    state = 1;
  Ioew_dialog->setObjectType(state);

  Ioew_dialog->setFrontSurface(obj->flags & IMOD_OBJFLAG_OUT ? 1 : 0);
  Ioew_dialog->setPointRadius(obj->pdrawsize);

  return(0);
}

/* Get the current object; if it does not exist, close the dialog */
Iobj *getObjectOrClose(void)
{
  Iobj* obj = imodel_object_get(Model);
  if (!obj){
    dia_err("No object! Closing Edit dialog.");
    Ioew_dialog->close();
  }
  return obj;
}

/* But only set the pointer null when the signal comes in that it is closing */
void ioew_closing(void)
{
  imod_cmap(Model);
  Ioew_dialog = NULL;
}

void ioew_quit(void)
{
  Ioew_dialog->close();
}
