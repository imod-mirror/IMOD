/*  IMOD VERSION 2.41
 *
 *  xyz.c -- Open the XYZ Window; View the X, Y and Z axis.
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
Revision 1.1.2.10  2003/01/27 00:30:07  mast
Pure Qt version and general cleanup

Revision 1.1.2.9  2003/01/23 20:13:33  mast
add include of imod_input

Revision 1.1.2.8  2003/01/13 01:15:43  mast
changes for Qt version of info window

Revision 1.1.2.7  2003/01/06 15:50:47  mast
Use imodCaption and viewport xy routine

Revision 1.1.2.6  2003/01/03 16:46:18  mast
Simplified closing logic

Revision 1.1.2.5  2003/01/02 15:43:37  mast
accept key input from controlled; use a cache sum to detect if xz and yz
data need redrawing

Revision 1.1.2.4  2002/12/14 05:23:42  mast
backing out the fancy subclass, adjusting for new visual detection

Revision 1.1.2.3  2002/12/13 07:09:19  mast
GLMainWindow needed different name for mouse event processors

Revision 1.1.2.2  2002/12/13 06:06:29  mast
using new glmainwindow and mainglwidget classes

Revision 1.1.2.1  2002/12/12 02:41:10  mast
Qt version

Revision 3.6  2002/12/01 15:34:41  mast
Changes to get clean compilation with g++

Revision 3.5  2002/11/27 03:22:12  mast
Changed argumnet 3 of xyz_draw_cb from long to int to avoid warnings

Revision 3.4  2002/11/25 19:23:38  mast
Made it add itself to list of controls, and restructured the
structure for closing the window to accomodate that change.

Revision 3.3  2002/01/29 03:11:47  mast
Fixed bug in xxyz_draw from accessing elements of xx before xx existence
test

Revision 3.2  2002/01/28 16:58:52  mast
Major enhancements: Made it use the same image drawing code as
the Zap window so that it would not be slow with fractional zooms; added
ability to display in high-resolution mode and to take snapshots of the
window; added ability to pan the window with the mouse; made the model
display have fixed line widths and symbol sizes independent of zoom; made
attachment to the nearest model point work just like in the Zap window;
added ability to riffle through images by dragging the current point
indicators with the mouse.
Note that the use of the b3dDrawGreyScalePixelsHQ routine is incompatible
with the now-obsolete PIXEL_DRAW_HACK required with Nvidia 0.9.5 drivers.
Removed non OpenGL code for readability.

Revision 3.1  2001/12/17 18:51:49  mast
Removed call to autox_build

*/
#include <math.h>
#include <qdatetime.h>
#include <qapplication.h>

// Couldn't include imod first here, and this flag didn't help
#include "xxyz.h"
#include "imod.h"
#include "imod_display.h"
#include "b3dgfx.h"
#include "xzap.h"
#include "control.h"
#include "imod_info_cb.h"
#include "imod_input.h"
#include "autox.h"
#include "imod_edit.h"
#include "imod_workprocs.h"

/*************************** internal functions ***************************/
static void xyzKey_cb(ImodView *vi, void *client, int released, QKeyEvent *e);
static void xyzClose_cb(ImodView *vi, void *client, int junk);
static void xyzDraw_cb(ImodView *vi, void *client, int drawflag);

/* The resident pointer to the structure */
static struct xxyzwin *XYZ = NULL;

static QTime but1downt;
static int xyzShowSlice = 0;

/* routine for opening or raising the window */
int xxyz_open(ImodView *vi)
{
  int i,msize;
  float newzoom;
  struct xxyzwin *xx;
  int deskWidth = QApplication::desktop()->width();
  int deskHeight = QApplication::desktop()->height();
  int needWinx, needWiny;

  if (XYZ){
    XYZ->dialog->raise();
    return(0);
  }

  xx = (struct xxyzwin *)malloc(sizeof(struct xxyzwin));
  if (!xx)
    return(-1);

  xx->xydata = xx->xzdata = xx->yzdata = NULL;

  /* DNM 1/19/02: need separate fdata for each side panel */
  xx->fdataxz  = (unsigned char *)malloc(vi->xsize * vi->zsize);
  xx->fdatayz  = (unsigned char *)malloc(vi->ysize * vi->zsize);

  xx->winx = vi->xsize + vi->zsize + (3 * XYZ_BSIZE);
  xx->winy = vi->ysize + vi->zsize + (3 * XYZ_BSIZE);
  xx->vi   = vi;
  xx->exposed = 0;

  xx->zoom = 1.0;
  xx->xtrans = 0;
  xx->ytrans = 0;
  xx->hq = 0;

  while (xx->winx > deskWidth - 20 ||
         xx->winy > deskHeight - 40){
    newzoom = b3dStepPixelZoom(xx->zoom, -1);
    if (newzoom == xx->zoom)
      break;
    xx->zoom = newzoom;
    xx->winx = (int)((vi->xsize + vi->zsize) * xx->zoom + (3 * XYZ_BSIZE));
    xx->winy = (int)((vi->ysize + vi->zsize) * xx->zoom + (3 * XYZ_BSIZE));
  }
     
  xx->dialog = new XyzWindow(xx, App->rgba, App->doublebuffer, 
			     App->qtEnableDepth, NULL,
			     "xyz window");
  if ((!xx->dialog)||
      (!xx->fdataxz) || (!xx->fdatayz)){
    wprint("Error:\n\tXYZ window can't open due to low memory\n");
    if(xx->fdataxz)
      free(xx->fdataxz);
    if(xx->fdatayz)
      free(xx->fdatayz);
    free(xx);
    return(-1);
  }
  
  xx->glw = xx->dialog->mGLw;
  if (!App->rgba)
    xx->glw->setColormap(*(App->qColormap));

  xx->dialog->setCaption(imodCaption("Imod XYZ Window"));

  xx->lx = xx->ly = xx->lz = xx->lastCacheSum -1;
  XYZ  = xx;

  xx->ctrl = ivwNewControl(vi, xyzDraw_cb, xyzClose_cb, xyzKey_cb, 
			   (void *)xx);
  
  // This one we can resize before showing, since there is no toolbar size
  // that needs to get corrected
  QSize winSize = xx->dialog->sizeHint();
  QSize glSize = xx->glw->sizeHint();
  int newHeight = xx->winy + winSize.height() - 
    (glSize.height() > 0 ? glSize.height() : 0);
  xx->dialog->resize(xx->winx, newHeight);

  imod_info_input();
  xx->dialog->show();

  return(0);
}

/* The controller calls here to draw the window */
static void xyzDraw_cb(ImodView *vi, void *client, int drawflag)
{
  struct xxyzwin *xx = (struct xxyzwin *)client;

  if ((!vi) || (!xx) || (!drawflag)) return;
     
  if (drawflag & IMOD_DRAW_COLORMAP) {
    xx->glw->setColormap(*(App->qColormap));
    return;
  }

  if (drawflag){
    if (drawflag & IMOD_DRAW_IMAGE){

      /* This happens whens a flip occurs: get new image spaces */
      xx->lx = xx->ly = xx->lz = -1;
      b3dFlushImage(xx->xydata);
      b3dFlushImage(xx->xzdata);
      b3dFlushImage(xx->yzdata);
      if(xx->fdataxz)
        free(xx->fdataxz);
      if(xx->fdatayz)
        free(xx->fdatayz);
      xx->fdataxz  = (unsigned char *)malloc(vi->xsize * vi->zsize);
      xx->fdatayz  = (unsigned char *)malloc(vi->ysize * vi->zsize);
    }
    if (drawflag & IMOD_DRAW_SLICE)
      xyzShowSlice = 1;
    xx->dialog->Draw();
  }
  return;
}


// This receives the close signal back from the controller, and tells the
// window to close
static void xyzClose_cb(ImodView *vi, void *client, int junk)
{
  struct xxyzwin *xx = (struct xxyzwin *)client;
  xx->dialog->close();
}

// This receives a key from the controller
static void xyzKey_cb(ImodView *vi, void *client, int released, QKeyEvent *e)
{
  struct xxyzwin *xx = (struct xxyzwin *)client;
  if (!released)
    xx->dialog->keyPressPassedOn(e);
}


// Implementation of the window class
XyzWindow::XyzWindow(struct xxyzwin *xyz, bool rgba, bool doubleBuffer, 
		     bool enableDepth, QWidget * parent,
                     const char * name, WFlags f) 
  : QMainWindow(parent, name, f)
{
  mXyz = xyz;
  QGLFormat glFormat;
  glFormat.setRgba(rgba);
  glFormat.setDoubleBuffer(doubleBuffer);
  glFormat.setDepth(enableDepth);
  mGLw = new XyzGL(xyz, glFormat, this);
  
  // Set it as main widget, set focus
  setCentralWidget(mGLw);
  setFocusPolicy(QWidget::StrongFocus);
}

// Whan a close event comes in, tell control to remove window, clean up
//  and accept
void XyzWindow::closeEvent (QCloseEvent * e )
{
  struct xxyzwin *xx = mXyz;
  ivwRemoveControl(mXyz->vi, mXyz->ctrl);

  /* DNM 11/17/01: stop x and y movies when close window */
  imodMovieXYZT(xx->vi, 0, 0, MOVIE_DEFAULT, MOVIE_DEFAULT);

  if(xx->fdataxz)
    free(xx->fdataxz);
  if(xx->fdatayz)
    free(xx->fdatayz);

  b3dFreeCIImage(xx->xydata);
  b3dFreeCIImage(xx->xzdata);
  b3dFreeCIImage(xx->yzdata);

  free(xx);
  XYZ = NULL;

  e->accept();
}

/* DNM 1/19/02: Add this function to get the CI Images whenever size has
   changed - now that we can do HQ graphics, they need to be the window size */
void XyzWindow::GetCIImages()
{
  struct xxyzwin *xx = mXyz;
  int xdim, ydim1, ydim2;

  xdim = xx->winx;
  ydim1 = ydim2 = xx->winy;
  xx->xydata = (B3dCIImage *)b3dGetNewCIImageSize
    (xx->xydata, App->depth, xdim, ydim1);

  xx->xzdata = (B3dCIImage *)b3dGetNewCIImageSize
    (xx->xzdata, App->depth, xdim, ydim2);

  xx->yzdata = (B3dCIImage *)b3dGetNewCIImageSize
    (xx->yzdata, App->depth, xdim, ydim1);
  return;
}


int XyzWindow::Getxyz(int x, int y, int *mx, int *my, int *mz)
{
  struct xxyzwin *xx = mXyz;
  int nx, ny, nz;
  /* DNM 1/23/02: turn this from float to int to keep calling expressions
     as ints */
  int b2 = XYZ_BSIZE;
  float scale;
  struct ViewInfo *vi = xx->vi;

  y = xx->winy - y - 1;
  x -= XYZ_BSIZE + xx->xwoffset;
  y -= XYZ_BSIZE + xx->ywoffset;

  nx = (int)(vi->xsize * xx->zoom);
  ny = (int)(vi->ysize * xx->zoom);
  nz = (int)(vi->zsize * xx->zoom);

  scale = 1.0/xx->zoom;

  /* Click in main image, Z-Section */
  if (mouse_in_box(0, 0, nx, ny, x, y)){
    *mx = (int)(x * scale);
    *my = (int)(y * scale);
    *mz = (int)vi->zmouse;
    ivwBindMouse(vi);
    return(3);
  }

  /* Click in top image, Y-Section */
  if (mouse_in_box(0, ny + b2,
                   nx,  ny + nz - 1 + b2, x, y)){
    *mx = (int)(x * scale);
    *my = (int)vi->ymouse;
    *mz = (int)((y - b2 - ny) * scale);
    ivwBindMouse(vi);
    return(2);
  }

  /* Click in right image */
  if (mouse_in_box(nx + b2, 0,
                   nx + b2 + nz - 1,
                   ny, x, y)){
    *mx = (int)vi->xmouse;
    *my = (int)(y * scale);
    *mz = (int)((x - b2 - nx) * scale);
    return(1);
  }

  /* Z-Section Gadget */
  if (mouse_in_box(nx + b2, ny + b2,
                   nx + b2 + nz - 1,
                   ny + b2 + nz - 1, x, y)){
    *mx = (int)vi->xmouse;
    *my = (int)vi->ymouse;
    *mz = (int)(0.5 * ((y - b2 - ny) + (x - b2 - nx)) * scale);
    return(6);
  }
     
  /* Y-Section Gadget */
  if (mouse_in_box(0, ny, nx, ny + b2, x, y)){
    *mx = (int)(x * scale);
    *my = (int)vi->ymouse;
    *mz = (int)vi->zmouse;
    return(5);
  }
     
  /* X-Section Gadget */
  if (mouse_in_box(nx, 0, nx + b2, ny, x, y)){
    *mx = (int)vi->xmouse;
    *my = (int)(y * scale);
    *mz = (int)vi->zmouse;
    return(4);
  }
     
  return(0);
}


void XyzWindow::B1Press(int x, int y)
{
  struct xxyzwin *xx = mXyz;
  int mx, my, mz;
  ImodView *vi   = xx->vi;
  Imod     *imod = vi->imod;
  Ipoint pnt, *spnt;
  Iindex index;
  int i, temp_distance;
  int distance = -1;
  float selsize = IMOD_SELSIZE / xx->zoom;
  int box = Getxyz(x, y, &mx, &my, &mz);

  if (!box)
    return;

  if (xx->vi->ax){
    if (xx->vi->ax->altmouse == AUTOX_ALTMOUSE_PAINT){
      autox_fillmouse(xx->vi, mx, my);
      return;
    }
  }

  /* DNM 1/23/02: Adopt code from Zap window, get nearest point if in the
     main display panel */
  if (xx->vi->imod->mousemode == IMOD_MMODEL && box == 3){
    pnt.x = mx;
    pnt.y = my;
    pnt.z = mz;
    vi->xmouse = mx;
    vi->ymouse = my;
    vi->zmouse = mz;
    vi->imod->cindex.contour = -1;
    vi->imod->cindex.point = -1;

    for (i = 0; i < imod->objsize; i++){
      index.object = i;
      temp_distance = imod_obj_nearest
        (&(vi->imod->obj[i]), &index , &pnt, selsize);
      if (temp_distance == -1)
        continue;
      if (distance == -1 || distance > temp_distance){
        distance      = temp_distance;
        vi->imod->cindex.object  = index.object;
        vi->imod->cindex.contour = index.contour;
        vi->imod->cindex.point   = index.point;
        spnt = imodPointGet(vi->imod);
        if (spnt){
          vi->xmouse = spnt->x;
          vi->ymouse = spnt->y;
        }
      }
    }
    ivwBindMouse(xx->vi);
    imodDraw(vi, IMOD_DRAW_RETHINK | IMOD_DRAW_XYZ);
    return;
  }

  xx->vi->xmouse = mx;
  xx->vi->ymouse = my;
  xx->vi->zmouse = mz;
  ivwBindMouse(xx->vi);
     
  /* DNM 1/23/02: make it update all windows */
  imodDraw(xx->vi, IMOD_DRAW_XYZ);
  return;
}

void XyzWindow::B2Press(int x, int y)
{
  struct xxyzwin *xx = mXyz;
  int mx, my, mz;
  int movie;
  struct Mod_Object  *obj;
  struct Mod_Contour *cont;
  struct Mod_Point   point;
  int pt;

  movie = Getxyz(x, y, &mx, &my, &mz);
  if (!movie)
    return;

  if (xx->vi->ax){
    if (xx->vi->ax->altmouse == AUTOX_ALTMOUSE_PAINT){
      autox_sethigh(xx->vi, mx, my);
      return;
    }
  }


  if (xx->vi->imod->mousemode == IMOD_MMOVIE){
    switch(movie){
    case 1:
    case 4:
      imodMovieXYZT(xx->vi, 1,
                    MOVIE_DEFAULT, MOVIE_DEFAULT, MOVIE_DEFAULT);
      break;
    case 2:
    case 5:
      imodMovieXYZT(xx->vi, MOVIE_DEFAULT, 1,
                    MOVIE_DEFAULT, MOVIE_DEFAULT);
      break;
    case 3:
    case 6:
      imodMovieXYZT(xx->vi, MOVIE_DEFAULT, MOVIE_DEFAULT, 1,
                    MOVIE_DEFAULT);
      break;
    }
    return;
  }

  if (movie != 3)
    return;

  obj = imodObjectGet(xx->vi->imod);
  if (!obj)
    return;

  cont = imodContourGet(xx->vi->imod);
  if (!cont){
    xx->vi->imod->cindex.contour = obj->contsize - 1;
    NewContour(xx->vi->imod);
    cont = imodContourGet(xx->vi->imod);
    if (!cont)
      return;
  }

  /* DNM: don't make closed contours wild if they're not */
  if (cont->psize &&  iobjClose(obj->flags) && !(cont->flags & ICONT_WILD)
      && cont->pts[0].z != mz) {
    wprint("\aXYZ will not add a point on a different section to"
           " a co-planar closed contour");
    return;
  }
  point.x = mx;
  point.y = my;
  point.z = mz;
  pt = xx->vi->imod->cindex.point;

  if ((cont->psize - 1) == pt)
    NewPoint(xx->vi->imod, &point);
  else
    InsertPoint(xx->vi->imod, &point, pt + 1);

  /* For a non-closed contour, maintain the wild flag */
  if (!iobjClose(obj->flags) && !(cont->flags & ICONT_WILD))
    imodel_contour_check_wild(cont);
  xx->vi->xmouse  = mx;
  xx->vi->ymouse  = my;

  /* DNM 1/23/02: make it update all windows */
  imodDraw(xx->vi, IMOD_DRAW_XYZ | IMOD_DRAW_MOD);
  return;
}

void XyzWindow::B3Press(int x, int y)
{
  struct xxyzwin *xx = mXyz;
  int mx, my, mz;
  int movie;
  struct Mod_Contour *cont;
  int pt;

  movie = Getxyz(x, y, &mx, &my, &mz);
  if (!movie)
    return;

  if (xx->vi->ax){
    if (xx->vi->ax->altmouse == AUTOX_ALTMOUSE_PAINT){
      autox_setlow(xx->vi, mx, my);
      return;
    }
  }
     
     
  if (xx->vi->imod->mousemode == IMOD_MMOVIE){
    switch(movie){
    case 1:
      imodMovieXYZT(xx->vi, -1, 
                    MOVIE_DEFAULT, MOVIE_DEFAULT, MOVIE_DEFAULT);
      break;
    case 2:
      imodMovieXYZT(xx->vi, MOVIE_DEFAULT, -1,
                    MOVIE_DEFAULT, MOVIE_DEFAULT);
      break;
    case 3:
      imodMovieXYZT(xx->vi, MOVIE_DEFAULT, MOVIE_DEFAULT, -1,
                    MOVIE_DEFAULT);
      break;
    default:
      break;
    }
    return;
  }

  if (movie != 3)
    return;

  cont = imodContourGet(xx->vi->imod);
  pt   = xx->vi->imod->cindex.point;
  if (!cont)
    return;
  if (pt < 0)
    return;
  if (!ivwPointVisible(xx->vi, &(cont->pts[pt])))
    return;
         
  cont->pts[pt].x = mx;
  cont->pts[pt].y = my;
  xx->vi->xmouse  = mx;
  xx->vi->ymouse  = my;
  ivwBindMouse(xx->vi);

  /* DNM 1/23/02: make it update all windows */
  imodDraw(xx->vi, IMOD_DRAW_XYZ | IMOD_DRAW_MOD);
  return;
}

/* DNM 1/20/02: add statements to implement pan */
void XyzWindow::B1Drag(int x, int y)
{
  struct xxyzwin *xx = mXyz;
  int sx, sy;
  int nx, ny;
  int b2 = XYZ_BSIZE;
  float scale;
  struct ViewInfo *vi = xx->vi;
  int newVal;

  sy = xx->winy - y - 1;
  sx = x - (XYZ_BSIZE + xx->xwoffset);
  sy -= XYZ_BSIZE + xx->ywoffset;

  nx = (int)(vi->xsize * xx->zoom);
  ny = (int)(vi->ysize * xx->zoom);

  scale = 1.0/xx->zoom;

  switch (xx->whichbox) {
  case 0:
  case 1:
  case 2:
  case 3:
    xx->xtrans += (x - xx->lmx);
    xx->ytrans -= (y - xx->lmy);
    Draw();
    return;

  case 6:
    newVal = (int)(0.5 * ((sy - b2 - ny) + (sx - b2 - nx)) * scale);
    if (xx->vi->zmouse == newVal)
      return;
    xx->vi->zmouse = newVal;
    break;

  case 5:
    newVal = (int)(sx * scale);
    if (xx->vi->xmouse == newVal)
      return;
    xx->vi->xmouse = newVal; 
    break;

  case 4:
    newVal = (int)(sy * scale);
    if (xx->vi->ymouse == newVal)
      return;
    xx->vi->ymouse = newVal;
    break;

  }
  ivwBindMouse(xx->vi);
  imodDraw(xx->vi, IMOD_DRAW_XYZ);
  return;
}

void XyzWindow::B2Drag(int x, int y)
{
  struct xxyzwin *xx = mXyz;
  int mx, my, mz, box;
  struct Mod_Object  *obj;
  struct Mod_Contour *cont;
  struct Mod_Point   point;
  double dist;
  int pt;

  if (xx->vi->ax){
    if (xx->vi->ax->altmouse == AUTOX_ALTMOUSE_PAINT){
      box = Getxyz(x, y, &mx, &my, &mz);
      if (box != 3)
        return;
      autox_sethigh(xx->vi, mx, my);
      return;
    }
  }

  if (xx->vi->imod->mousemode != IMOD_MMODEL)
    return;

  box = Getxyz(x, y, &mx, &my, &mz);
  if (box != 3)
    return;

  obj = imodel_object_get(xx->vi->imod);
  if (!obj)
    return;
  cont = imodContourGet(xx->vi->imod);
  if (!cont)
    return;
  pt = xx->vi->imod->cindex.point;
  if (pt < 0)
    return;

  /* DNM: don't make closed contours wild if they're not */
  if (cont->psize &&  iobjClose(obj->flags) && !(cont->flags & ICONT_WILD)
      && cont->pts[0].z != mz) {
    wprint("\aXYZ will not add a point on a different section to"
           " a co-planar closed contour");
    return;
  }
  point.x = mx;
  point.y = my;
  point.z = mz;

  dist = imodel_point_dist(&point, &(cont->pts[pt]));
  if (dist < xx->vi->imod->res)
    return;

  if ((cont->psize - 1) == pt)
    NewPoint(xx->vi->imod, &point);
  else
    InsertPoint(xx->vi->imod, &point, pt + 1);

  /* For a non-closed contour, maintain the wild flag */
  if (!iobjClose(obj->flags) && !(cont->flags & ICONT_WILD))
    imodel_contour_check_wild(cont);
  xx->vi->xmouse  = mx;
  xx->vi->ymouse  = my;
  ivwBindMouse(xx->vi);

  /* DNM 1/23/02: make it update all windows */
  imodDraw(xx->vi, IMOD_DRAW_XYZ | IMOD_DRAW_MOD);
  return;
}

void XyzWindow::B3Drag(int x, int y)
{
  struct xxyzwin *xx = mXyz;
  int mx, my, mz, box;
  struct Mod_Object  *obj;
  struct Mod_Contour *cont;
  struct Mod_Point   point;
  double dist;
  int pt;

  if (xx->vi->ax){
    if (xx->vi->ax->altmouse == AUTOX_ALTMOUSE_PAINT){
      box = Getxyz(x, y, &mx, &my, &mz);
      if (box != 3)
        return;
      autox_setlow(xx->vi, mx, my);
      return;
    }
  }

  if (xx->vi->imod->mousemode != IMOD_MMODEL)
    return;

  box = Getxyz(x, y, &mx, &my, &mz);
  if (box != 3)
    return;

  obj = imodObjectGet(xx->vi->imod);
  if (!obj)
    return;
  cont = imodContourGet(xx->vi->imod);
  if (!cont)
    return;
  pt = xx->vi->imod->cindex.point;
  if (pt < 0)
    return;

  point.x = mx;
  point.y = my;
  point.z = mz;

  dist = imodel_point_dist(&point, &(cont->pts[pt]));
  if (dist < xx->vi->imod->res)
    return;

  pt++;
  if (pt >= cont->psize)
    pt = cont->psize - 1;

  cont->pts[pt].x = mx;
  cont->pts[pt].y = my;
  cont->pts[pt].z = mz;
  xx->vi->imod->cindex.point = pt;
  xx->vi->xmouse  = mx;
  xx->vi->ymouse  = my;
  ivwBindMouse(xx->vi);

  /* DNM 1/23/02: make it update all windows */
  imodDraw(xx->vi, IMOD_DRAW_XYZ | IMOD_DRAW_MOD);
  return;
}


void XyzWindow::SetSubimage(int absStart, int winSize, int imSize, 
			       float zoom, int *drawsize, int *woffset,
			       int *dataStart)
{
  *dataStart = 0;
  *woffset = absStart;

  /* If the absolute starting point is negative, then adjust the data start
     for this; throw away one more pixel and set a small window offset
     if necessary to keep pixels synchronized */
  if (absStart < 0) {
    *dataStart = (int)(-absStart / zoom);
    *woffset = 0;
    if (zoom * *dataStart < -absStart) {
      (*dataStart)++;
      *woffset = (int)(zoom * *dataStart + absStart);
    }
  }

  /* limit # of pixels to draw if it goes past end of window - this also
     takes care of case where image starts past end of window */
  *drawsize = imSize - *dataStart;
  if (*drawsize * zoom + *woffset > winSize)
    *drawsize = (int)((winSize - *woffset) / zoom);
}     

void XyzWindow::DrawImage()
{
  struct xxyzwin *win = mXyz;
  unsigned int x, y, z, i;
  int nx = win->vi->xsize;
  int ny = win->vi->ysize;
  int nz = win->vi->zsize;
  unsigned char *id;
  unsigned char *fdata;
  unsigned long cyi;
  int cx, cy, cz, iz;
  int imdataxsize;
  unsigned char **imdata;
  int extraImSize;
  int dataOffset;
  int drawsize;
  GLenum type, format;
  GLint  unpack = b3dGetImageType(&type, &format);
  int sx = (int)(nx * win->zoom);
  int sy = (int)(ny * win->zoom);
  int sz = (int)(nz * win->zoom);
  int wx1, wx2, wy1, wy2;
  int xoffset1, xoffset2, yoffset1, yoffset2;
  int width1, height1, width2, height2, cacheSum, xslice, yslice;
          
          
  if (!win) return;

  if (!win->exposed) return;     /* DNM: avoid crashes if Zap is movieing*/

  /* DNM 3/5/01: changed to avoid very slow ivwGetValue when data are in
     cache; set up image pointer tables */
  imdata = (unsigned char **)
    malloc(sizeof(unsigned char *) * win->vi->zsize);

  if (!imdata)
    return;

  // Keep track of a sum of Z values in the cache in order to detect 
  // Changes in available data that will require redisplay of XZ and YZ data
  cacheSum = 0;
  if (win->vi->vmSize) {
    /* For cached data, get pointers to data that exist at this time */
    for (i = 0; i < win->vi->zsize; i++)
      imdata[i] = NULL;
    for (i = 0; i < win->vi->vmSize; i++) {
      iz = win->vi->vmCache[i].cz;
      if (iz < win->vi->zsize && iz >= 0 &&
          win->vi->vmCache[i].ct == win->vi->ct){
        imdata[iz] = win->vi->vmCache[i].sec->data.b;
	cacheSum += iz;
      }
    }

  } else if (!win->vi->fakeImage) {
    /* for loaded data, get pointers from win->vi */
    for (i = 0; i < win->vi->zsize; i++)
      imdata[i] = win->vi->idata[i];
  }
  /* Just take the X size, do not allow for possibility of cached data 
     having different X sizes */
  imdataxsize = win->vi->xsize;

  if (win->vi->vmSize){
    win->lx = win->ly = -1;
  }
          
  glClearIndex(App->background);
  /* DNM: need to set clear colors for rgb mode */
  if (App->rgba)
    glClearColor(64./255., 64./255 , 96./255, 0.);
     
  /* DNM 1/20/02: remove the XYZ_CLEAR_HACK */
     
  glClear(GL_COLOR_BUFFER_BIT);

  ivwGetLocation(win->vi, &cx, &cy, &cz);
  cyi = cy * nx;

  /* Pass the image offset routine the effective image size, including
     de-zoomed borders, and convert a data offset into a negative window
     offset */
  extraImSize = (int)floor((double)(3. * XYZ_BSIZE / win->zoom + 0.5));
  b3dSetImageOffset(win->winx, nx + nz + extraImSize, win->zoom, &drawsize,
                    &win->xtrans, &win->xwoffset, &dataOffset);
  if (dataOffset)
    win->xwoffset = -(int)floor((double)(dataOffset * win->zoom + 0.5));

  b3dSetImageOffset(win->winy, ny + nz + extraImSize, win->zoom, &drawsize,
                    &win->ytrans, &win->ywoffset, &dataOffset);
  if (dataOffset)
    win->ywoffset = -(int)floor((double)(dataOffset * win->zoom + 0.5));


  /* Now compute drawing parameters for each of the subareas */
  SetSubimage(win->xwoffset + XYZ_BSIZE, win->winx, nx, win->zoom,
	      &width1, &wx1, &xoffset1);
  SetSubimage(win->xwoffset + sx + 2 * XYZ_BSIZE, win->winx, nz, 
	      win->zoom, &width2, &wx2, &xoffset2);
  SetSubimage(win->ywoffset + XYZ_BSIZE, win->winy, ny, win->zoom,
	      &height1, &wy1, &yoffset1);
  SetSubimage(win->ywoffset + sy + 2 * XYZ_BSIZE, win->winy, nz, 
	      win->zoom, &height2, &wy2, &yoffset2);

  /* printf ("width1 %d  height1 %d  width2 %d  height2 %d\n", width1,
     height1, width2, height2);
     printf ("wx1 %d  xoffset1 %d  wy1 %d  yoffset1 %d\n", wx1,
     xoffset1, wy1, yoffset1); */
  if (width1 > 0 && height1 > 0){
    win->lz = cz;
    id = ivwGetCurrentZSection(win->vi);
    b3dDrawGreyScalePixelsHQ(id, nx,ny, xoffset1, yoffset1, wx1, wy1,
                             width1, height1, win->xydata,
                             win->vi->rampbase, win->zoom, win->zoom, 
                             win->hq, cz);
  }

  // Send out a negative xslice or yslice if the data are being reloaded,
  // this is the best way to signal that they are new to the matching routine
  if (width2 > 0 && height1 > 0) {
    xslice = cx;
    fdata  = win->fdatayz;
    if (cx != win->lx || cacheSum != win->lastCacheSum){
      xslice = -1 - cx;
      win->lx = cx;
      for(z = 0; z < nz; z++) {
        if (!win->vi->fakeImage && imdata[z]) {
          for(i = z, y = 0; y < ny; y++, i += nz)
            fdata[i] = imdata[z][cx + (y * imdataxsize)];
        } else {
          for(i= z, y = 0; y < ny; y++, i += nz)
            fdata[i] = 0;
        }
      }
    }
    b3dDrawGreyScalePixelsHQ(win->fdatayz, nz, ny, xoffset2, yoffset1,
                             wx2, wy1, width2, height1, win->yzdata,
                             win->vi->rampbase, win->zoom, win->zoom, 
                             win->hq, xslice);
  }

  if (width1 > 0 && height2 > 0) {
    yslice = cy;
    fdata  = win->fdataxz;
    if (cy != win->ly || cacheSum != win->lastCacheSum){
      yslice = -1 - cy;
      win->ly = cy;
      for(i = 0,z = 0; z < nz; z++) {
        if (!win->vi->fakeImage && imdata[z]) {
          for(x = 0; x < nx; x++, i++)
            fdata[i] = imdata[z][x + (cy * imdataxsize)];
        } else {
          for(x = 0; x < nx; x++, i++)
            fdata[i] = 0;
        }
      }
    }       
    b3dDrawGreyScalePixelsHQ(win->fdataxz, nx, nz, xoffset1, yoffset2,
                             wx1, wy2, width1, height2, win->xzdata,
                             win->vi->rampbase, win->zoom, win->zoom, 
                             win->hq, yslice);
  }
  win->lastCacheSum = cacheSum;

  free(imdata);
     
  return;
}

void XyzWindow::DrawCurrentLines()
{
  struct xxyzwin *xx = mXyz;
  int cx, cy, cz;
  float z = xx->zoom;
  int bx = XYZ_BSIZE + xx->xwoffset;
  int by = XYZ_BSIZE + xx->ywoffset;
  int bx2 = (int)(bx + XYZ_BSIZE + floor((double)(xx->vi->xsize * z + 0.5)));
  int by2 = (int)(by + XYZ_BSIZE + floor((double)(xx->vi->ysize * z + 0.5)));

  /* DNM 1/23/02: Put the line in the middle now that one can drag it */
  int bpad = XYZ_BSIZE / 2;

  int nx = xx->vi->xsize;
  int ny = xx->vi->ysize;
  int nz = xx->vi->zsize;

  b3dColorIndex(App->background);
  b3dLineWidth(1);

  ivwGetLocation(xx->vi, &cx, &cy, &cz);
  b3dColorIndex(App->foreground);

  /* draw xz location line. */
  b3dDrawLine(bx2, 
              (int)(by2 + z * cz),
              (int)(bx2 + z * (nz - 1)),
              (int)(by2 + z * cz));

  b3dDrawLine((int)(bx2 + z * cz),
              by2,
              (int)(bx2 + z * cz),
              (int)(by2 + z * (nz - 1)));
     
  b3dDrawLine((int)(bx + z * cx),
              (int)(by + z * ny + bpad),
              (int)(bx2 + z * nz),
              (int)(by + z * ny + bpad));
  b3dDrawLine((int)(bx + z * nx + bpad),
              (int)(by2 + z * nz),
              (int)(bx + z * nx + bpad),
              (int)(by + z * cy));
  return;
}

void XyzWindow::DrawGhost()
{
  return;
}

/* DNM 1/20/02: add argument ob to be able to reset color properly */
void XyzWindow::DrawContour(Iobj *obj, int ob, Icont *cont)
{
  struct xxyzwin *xx = mXyz;
  ImodView *vi = xx->vi;
  Ipoint *point;
  int pt, npt = 0, ptsonsec;
  float vert[2];
  float drawsize;
  float z = xx->zoom;
  int bx = XYZ_BSIZE + xx->xwoffset;
  int by = XYZ_BSIZE + xx->ywoffset;
  int bx2 = (int)(bx + XYZ_BSIZE + floor((double)(vi->xsize * z + 0.5)));
  int by2 = (int)(by + XYZ_BSIZE + floor((double)(vi->ysize * z + 0.5)));
     
  if (!cont->psize)
    return;
     
  if (iobjClose(obj->flags)){
    /* closed  contour: draw all points on visible section, just
       connecting visible ones if points go out of section */
    b3dBeginLine();
    for (pt = 0; pt < cont->psize; pt++){
      if (!ivwPointVisible(vi, &(cont->pts[pt]))) {
        /* DNM: If contour is wild, keep going; if not, done with
           it */
        if (cont->flags & ICONT_WILD)
          continue;
        break;
      }
      b3dVertex2i((int)(z * cont->pts[pt].x + bx),  
                  (int)(z * cont->pts[pt].y + by));
    }
    if (ivwPointVisible(vi, cont->pts)){
      b3dVertex2i((int)(bx + z * cont->pts->x), 
                  (int)(by + z * cont->pts->y));
    }
    b3dEndLine();
          
    /* draw symbols for all points on section */
    for (pt = 0; pt < cont->psize; pt++){
      if (!ivwPointVisible(vi, &(cont->pts[pt]))) {
        /* DNM: If contour is wild, keep going; if not, done with
           it */
        if (cont->flags & ICONT_WILD)
          continue;
        break;
      }
      zapDrawSymbol((int)(z * cont->pts[pt].x + bx), 
                    (int)(z * cont->pts[pt].y + by),
                    obj->symbol, obj->symsize, obj->symflags);
    }
  }
     
  /* Open contours: draw symbols for all points on section and connecting
     lines just between point pairs on section */
  if (iobjOpen(obj->flags)){
    for(pt = 0; pt < cont->psize; pt++){
      point = &(cont->pts[pt]);
      if (ivwPointVisible(vi, point)){
        zapDrawSymbol((int)(z * cont->pts[pt].x + bx), 
                      (int)(z * cont->pts[pt].y + by),
                      obj->symbol, obj->symsize, obj->symflags);
                    

        if (ivwPointVisible(vi, &(cont->pts[pt+1])))
          b3dDrawLine((int)(z * point->x + bx),
                      (int)(z * point->y + by),
                      (int)(z * cont->pts[pt+1].x + bx),
                      (int)(z * cont->pts[pt+1].y + by));
                    
      }
               
    }            
  }
     
  /* scattered contour */
  if (iobjScat(obj->flags)){
    for (pt = 0; pt < cont->psize; pt++){
      /* draw symbol if point on section */
      if (ivwPointVisible(vi, &(cont->pts[pt])))
        zapDrawSymbol((int)(z * cont->pts[pt].x + bx), 
                      (int)(z * cont->pts[pt].y + by),
                      obj->symbol, obj->symsize, obj->symflags);
               
      drawsize = imodPointGetSize(obj, cont, pt);
      if (drawsize > 0)
        if (ivwPointVisible(vi, &(cont->pts[pt]))){
          /* If there's a size, draw a circle and a plus for a
             circle that's big enough */
          b3dDrawCircle((int)(bx + z * cont->pts[pt].x),
                        (int)(by + z * cont->pts[pt].y),
                        (int)(z * drawsize));
          if (drawsize > 3)
            b3dDrawPlus((int)(bx + z * cont->pts[pt].x),
                        (int)(by + z * cont->pts[pt].y), 3);
        }else{
          /* for off-section, compute size of circle and draw 
             that */
          if (drawsize > 1){
            /* draw a smaller circ if further away. */
            vert[0] = (cont->pts[pt].z - vi->zmouse) *
              App->cvi->imod->zscale;
            if (vert[0] < 0)
              vert[0] *= -1.0f;

            if (vert[0] < drawsize - 0.01){
              vert[1] =
                z * sqrt((double)
                         (drawsize * drawsize
                          - vert[0] * vert[0]));
                                   
              b3dDrawCircle((int)(bx + z*cont->pts[pt].x),
                            (int)(by + z*cont->pts[pt].y),
                            (int)vert[1]);
            }
          }
        }
    }
  }

  /* draw end markers if requested */
  if (obj->symflags & IOBJ_SYMF_ENDS){
    if (ivwPointVisible(vi, &(cont->pts[cont->psize-1]))){
      b3dColorIndex(App->endpoint);
      b3dDrawCross((int)(bx + z * cont->pts[cont->psize-1].x),
                   (int)(by + z * cont->pts[cont->psize-1].y),
                   obj->symsize/2);
    }
    if (ivwPointVisible(vi, cont->pts)){
      b3dColorIndex(App->bgnpoint);
      b3dDrawCross((int)(bx + z * cont->pts->x),
                   (int)(by + z * cont->pts->y),
                   obj->symsize/2);
    }
    /* DNM 1/21/02: need to reset color this way, not wih b3dColorIndex*/
    imodSetObjectColor(ob);

  }
  return;
}

void XyzWindow::DrawCurrentContour(Iobj *obj, int ob, Icont *cont)
{
  struct xxyzwin *xx = mXyz;
  ImodView *vi = xx->vi;
  Ipoint *point;
  int pt;
  int cz = (int)(vi->zmouse+0.5f);
  float z = xx->zoom;
  int bx = XYZ_BSIZE + xx->xwoffset;
  int by = XYZ_BSIZE + xx->ywoffset;
  int bx2 = (int)(bx + XYZ_BSIZE + floor((double)(vi->xsize * z + 0.5)));
  int by2 = (int)(by + XYZ_BSIZE + floor((double)(vi->ysize * z + 0.5)));
  int drawsize;

  if (!cont->psize)
    return;
     
  if (iobjClose(obj->flags)){
          
    /* closed contours: draw lines, including points on section */
    b3dBeginLine();
    for (pt = 0; pt < cont->psize; pt++){
      if (!ivwPointVisible(vi, &(cont->pts[pt]))) {
        /* DNM: If contour is wild, keep going; if not, done with
           it */
        if (cont->flags & ICONT_WILD)
          continue;
        break;
      }
      b3dVertex2i((int)(z * cont->pts[pt].x + bx), 
                  (int)(z * cont->pts[pt].y + by));
    }
    b3dEndLine();

    /* draw symbols for points on section */
    if (obj->symbol != IOBJ_SYM_NONE)
      for (pt = 0; pt < cont->psize; pt++){
        if (!ivwPointVisible(vi, &(cont->pts[pt]))) {
          if (cont->flags & ICONT_WILD)
            continue;
          break;
        }
        zapDrawSymbol((int)(z * cont->pts[pt].x + bx), 
                      (int)(z * cont->pts[pt].y + by),
                      obj->symbol, obj->symsize, obj->symflags);
      }

    /* Draw projection of all lines into x/z view */
    b3dBeginLine();
    for (pt = 0; pt < cont->psize; pt++){
      b3dVertex2i((int)(z * cont->pts[pt].x + bx),  
                  (int)(z * cont->pts[pt].z + by2));
    }
    b3dEndLine();


    /* Draw projection of all lines into y/z view */
    b3dBeginLine();
    for (pt = 0; pt < cont->psize; pt++){
      b3dVertex2i((int)(z * cont->pts[pt].z + bx2), 
                  (int)(z * cont->pts[pt].y + by));
    }
    b3dEndLine();
  }

  if (iobjOpen(obj->flags)){
    /* Open objects: try to draw solid lines that are on-section and
       dashed lines that are not */
    if (cont->psize > 1){
      for(pt = 1; pt < cont->psize; pt++){
                    
        if ((cont->pts[pt].z == cz) && (cont->pts[pt-1].z == cz)){
          b3dDrawLine((int)(bx + z * cont->pts[pt].x),
                      (int)(by + z * cont->pts[pt].y),
                      (int)(bx + z * cont->pts[pt-1].x),
                      (int)(by + z * cont->pts[pt-1].y));
          continue;
        }
        b3dLineStyle(B3D_LINESTYLE_DASH);
        b3dDrawLine((int)(bx + z * cont->pts[pt].x),
                    (int)(by + z * cont->pts[pt].y),
                    (int)(bx + z * cont->pts[pt-1].x),
                    (int)(by + z * cont->pts[pt-1].y));
        b3dLineStyle(B3D_LINESTYLE_SOLID);
                    
        /* Draw a circle on an isolated point on-section */
        if (pt < cont->psize - 1)
          if ((cont->pts[pt].z == cz) &&
              (cont->pts[pt-1].z != cz) &&
              (cont->pts[pt+1].z != cz))
            b3dDrawCircle((int)(bx + z * cont->pts[pt].x),
                          (int)(by + z *cont->pts[pt].y), 3);
      }
      /* Draw circles on first or last points if they are isolated
         on-section */
      if ((cont->pts[0].z == cz) && (cont->pts[1].z != cz))
        b3dDrawCircle((int)(bx + z * cont->pts[0].x),
                      (int)(by + z * cont->pts[0].y), 3);
      if ((cont->pts[cont->psize - 1].z == cz) &&
          (cont->pts[cont->psize - 2].z != cz))
        b3dDrawCircle
          ((int)(bx + z * cont->pts[cont->psize - 1].x),
           (int)(by + z * cont->pts[cont->psize - 1].y), 3);

      /* draw symbols for points on section */
      if (obj->symbol != IOBJ_SYM_NONE)
        for (pt = 0; pt < cont->psize; pt++){
          if (!ivwPointVisible(vi, &(cont->pts[pt]))) {
            if (cont->flags & ICONT_WILD)
              continue;
            break;
          }
          zapDrawSymbol((int)(z * cont->pts[pt].x + bx), 
                        (int)(z * cont->pts[pt].y + by),
                        obj->symbol, obj->symsize,
                        obj->symflags);
        }

    }else{

      /* Draw a circle for a single point, regardless of z */
      b3dDrawCircle((int)(bx + z * cont->pts[0].x),
                    (int)(by + z * cont->pts[0].y), 3);
    }

    /* Draw projection of all lines into x/z view */
    b3dBeginLine();
    for (pt = 0; pt < cont->psize; pt++){
      b3dVertex2i((int)(z * cont->pts[pt].x + bx),  
                  (int)(z * cont->pts[pt].z + by2));
    }
    b3dEndLine();


    /* Draw projection of all lines into y/z view */
    b3dBeginLine();
    for (pt = 0; pt < cont->psize; pt++){
      b3dVertex2i((int)(z * cont->pts[pt].z + bx2),
                  (int)(z * cont->pts[pt].y + by));
    }
    b3dEndLine();

    return;
  }

  /* scatterd object */
  if (iobjScat(obj->flags)){
    /* Just draw the contour with other routines */
    DrawContour(obj, ob, cont);
          
    /* Draw points in other windows if that is where they are */
    for (pt = 0; pt < cont->psize; pt++){
      drawsize = (int)(z * imodPointGetSize(obj, cont, pt));
      if ((int)cont->pts[pt].x == vi->xmouse){
        zapDrawSymbol((int)(z * cont->pts[pt].z + bx2),
                      (int)(z * cont->pts[pt].y + by),
                      obj->symbol, obj->symsize, obj->symflags);
        if (drawsize > 0)
          b3dDrawCircle((int)(bx2 + z * cont->pts[pt].z),
                        (int)(by + z * cont->pts[pt].y),
                        drawsize);
      }
      if ((int)cont->pts[pt].y == vi->ymouse){
        zapDrawSymbol((int)(z * cont->pts[pt].x + bx), 
                      (int)(z * cont->pts[pt].z + by2),
                      obj->symbol, obj->symsize, obj->symflags);
        if (drawsize > 0)
          b3dDrawCircle((int)(bx + z * cont->pts[pt].x),
                        (int)(by2 + z * cont->pts[pt].z),
                        drawsize);
      }
    }
  }

  /* draw end symbols in x/y views */
  if (obj->symflags & IOBJ_SYMF_ENDS){
    if (ivwPointVisible(vi, &(cont->pts[cont->psize-1]))){
      b3dColorIndex(App->endpoint);
      b3dDrawCross((int)(bx + z * cont->pts[cont->psize-1].x),
                   (int)(by + z * cont->pts[cont->psize-1].y),
                   obj->symsize/2);
    }
    if (ivwPointVisible(vi, cont->pts)){
      b3dColorIndex(App->bgnpoint);
      b3dDrawCross((int)(bx + z * cont->pts->x),
                   (int)(by + z * cont->pts->y),
                   obj->symsize/2);
    }
    /* DNM 1/21/02: need to reset color this way, not wih ColorIndex */
    imodSetObjectColor(ob);
  }
     
  return;
}

void XyzWindow::DrawModel()
{
  struct xxyzwin *xx = mXyz;
  Imod *imod = xx->vi->imod;
  Iobj *obj;
  Icont *cont;
  int ob, co;

  if (imod->drawmode <= 0)
    return;
  if (xx->vi->ghostmode)
    DrawGhost();
     
  for(ob = 0; ob < imod->objsize; ob++){
    imodSetObjectColor(ob);
    obj = &(imod->obj[ob]);
    b3dLineWidth(obj->linewidth2);
    for(co = 0; co < imod->obj[ob].contsize; co++){
      cont = &(obj->cont[co]);
      if ((co == imod->cindex.contour) &&
          (ob == imod->cindex.object))
        DrawCurrentContour(obj, ob, cont);
      else
        DrawContour(obj, ob, cont);
    }
  }

  return;
}

void XyzWindow::DrawCurrentPoint()
{
  struct xxyzwin *xx = mXyz;
  Icont *cont = imodContourGet(xx->vi->imod);
  Ipoint *pnt = imodPointGet(xx->vi->imod);
  int psize = 3;
  int cx, cy, cz;
  float z = xx->zoom;
  int bx = XYZ_BSIZE + xx->xwoffset;
  int by = XYZ_BSIZE + xx->ywoffset;
  int bx2 = (int)(bx + XYZ_BSIZE + floor((double)(xx->vi->xsize * z + 0.5)));
  int by2 = (int)(by + XYZ_BSIZE + floor((double)(xx->vi->ysize * z + 0.5)));

  ivwGetLocation(xx->vi, &cx, &cy, &cz);

  if (!xx->vi->drawcursor)
    return;

  /* Draw begin and end points of selected contour. */
  if (cont){
    if (cont->psize > 1){
      if ((int)cont->pts->z == cz){
        b3dColorIndex(App->bgnpoint);
        b3dDrawCircle((int)(z * cont->pts->x+bx),
                      (int)(z * cont->pts->y+by), 2);
      }
      if ((int)cont->pts[cont->psize - 1].z == cz){
        b3dColorIndex(App->endpoint);
        b3dDrawCircle((int)(z * cont->pts[cont->psize - 1].x+bx),
                      (int)(z*cont->pts[cont->psize - 1].y+by), 2);
      }
    }
  }
     
  /* Draw location of current point. */
  /* DNM 1/21/02: do it like zap window, draw only if in model mode,
     otherwise draw crosses at current mouse point */
  if (xx->vi->imod->mousemode == IMOD_MMODEL &&  pnt){
          
    if ((int)(pnt->z) == cz){
      b3dColorIndex(App->foreground);
    }else{
      b3dColorIndex(App->shadow);
    }
    b3dDrawCircle((int)(z * pnt->x+bx), (int)(z * pnt->y+by), psize);
    b3dColorIndex(App->foreground);
    b3dDrawPlus((int)(z*pnt->x+bx), (int)(z*cz + by2), 3);
    b3dDrawPlus((int)(z * cz + bx2), (int)(by+z*pnt->y), 3);
    return;
  }
  b3dColorIndex(App->foreground);
  b3dDrawPlus((int)(z*cx+bx), (int)(z*cy+by), 3);
  b3dDrawPlus((int)(z*cx+bx), (int)(z*cz+by2), 3);
  b3dDrawPlus((int)(bx2+z*cz), (int)(by+z*cy), 3);
     
  return;
}

void XyzWindow::DrawAuto()
{
  struct xxyzwin *xx = mXyz;
  ImodView *vi = xx->vi;
  int i, j;
  float vert[2];
  unsigned short cdat;
  int x, y;
  unsigned long pixel;
     

  if (!vi->ax)
    return;
     
  if (!vi->ax->filled)
    return;
     
  if (vi->ax->cz != vi->zmouse)
    return;

  /* Buggy need to fix. */

#ifdef FIX_xyzDrawAuto_BUG
  cdat = App->endpoint;

  for (j = 0; j < vi->ysize; j++){
    y = j + XYZ_BSIZE;
    for(i = 0; i < vi->xsize; i++){
      x = i + XYZ_BSIZE;
      if (vi->ax->data[i + (j * vi->xsize)] & AUTOX_FLOOD){
        pixel = App->endpoint;
        if (vi->ax->data[i + (j * vi->xsize)] & AUTOX_BLACK){
          pixel = vi->rampbase;
        }
      }else{
        if (vi->ax->data[i + (j * vi->xsize)] & AUTOX_BLACK){
          pixel = vi->rampbase;
        }
                    
        if (vi->ax->data[i + (j * vi->xsize)] & AUTOX_WHITE){
          pixel = vi->rampbase + vi->rampsize;
        }
      }
      b3dColorIndex(pixel);
      b3dDrawFilledRectangle(x, y, 1,1);
    }
  }
#endif
  return;
}


void XyzWindow::Draw()
{
  mGLw->updateGL();
}


// Key handler
void XyzWindow::keyPressEvent ( QKeyEvent * event )
{
  struct xxyzwin *xx = mXyz;
  struct ViewInfo *vi = xx->vi;

  int keysym = event->key();
  int state = event->state();
  
  // Start with this at 1: set to 0 if NOT handled
  int handled = 1;

  ivwControlPriority(xx->vi, xx->ctrl);

  switch(keysym){

  case Qt::Key_Minus:
    xx->zoom = b3dStepPixelZoom(xx->zoom, -1);
    Draw();
    break;
             
  case Qt::Key_Equal:
    xx->zoom = b3dStepPixelZoom(xx->zoom, 1);
    Draw();
    break;

  case Qt::Key_S:
    if ((state & Qt::ShiftButton) || (state & Qt::ControlButton)){

      // Need to draw the window now (didn't have to in X version)
      Draw();
      if (state & Qt::ShiftButton)
	b3dAutoSnapshot("xyz", SnapShot_RGB, NULL);
      else 
	b3dAutoSnapshot("xyz", SnapShot_TIF, NULL);
    } else
      handled = 0;
    break;

  case Qt::Key_R:
    xx->hq = 1 - xx->hq;
    if (xx->hq)
      wprint("\aHigh-resolution mode ON\n");
    else
      wprint("\aHigh-resolution mode OFF\n");
    Draw();
    break;

  case Qt::Key_Escape:
    close();
    break;

  default:
    handled = 0;
    break;
  }

  if (!handled)
    inputQDefaultKeys(event, vi);
}

XyzGL::XyzGL(struct xxyzwin *xyz, QGLFormat inFormat, XyzWindow * parent,
             const char * name)
  : QGLWidget(inFormat, parent, name)
{
  mMousePressed = false;
  mXyz = xyz;
  mWin = parent;
}


// The main drawing routine
/* DNM 1/21/02: eliminate OpenGL scaling of native coordinates, make all
   model drawing routines multiply coordinates by zoom */
/* DNM 1/28/02: moved uses of the elements of xx to after the test for zz */
void XyzGL::paintGL()
{
  struct xxyzwin *xx = mXyz;
  float z;
  int bx, by, bx2, by2;

  if (!xx)
    return;
  if (!xx->exposed) return;     /* DNM: avoid crashes if Zap is movieing*/

  b3dSetCurSize(xx->winx, xx->winy);
  z = xx->zoom;
  bx = XYZ_BSIZE + xx->xwoffset;
  by = XYZ_BSIZE + xx->ywoffset;
  bx2 = (int)(bx + XYZ_BSIZE + floor((double)(xx->vi->xsize * z + 0.5)));
  by2 = (int)(by + XYZ_BSIZE + floor((double)(xx->vi->ysize * z + 0.5)));


  mWin->DrawImage();

  mWin->DrawModel();
  mWin->DrawCurrentLines();
  mWin->DrawCurrentPoint();
  mWin->DrawAuto();

  if (xyzShowSlice){
    b3dColorIndex(App->foreground);
          
    b3dDrawLine((int)(bx + (xx->vi->slice.zx1 * xx->zoom)), 
                (int)(by + (xx->vi->slice.zy1 * xx->zoom)),
                (int)(bx + (xx->vi->slice.zx2 * xx->zoom)), 
                (int)(by + (xx->vi->slice.zy2 * xx->zoom)));

    b3dDrawLine((int)(bx + (xx->vi->slice.yx1 * xx->zoom)),
                (int)(by2+ (xx->vi->slice.yz1 * xx->zoom)),
                (int)(bx + (xx->vi->slice.yx2 * xx->zoom)),
                (int)(by2+ (xx->vi->slice.yz2 * xx->zoom)));

    b3dDrawLine((int)(bx2+ (xx->vi->slice.xz1 * xx->zoom)),
                (int)(by + (xx->vi->slice.xy1 * xx->zoom)),
                (int)(bx2+ (xx->vi->slice.xz2 * xx->zoom)),
                (int)(by + (xx->vi->slice.xy2 * xx->zoom)));

    xyzShowSlice = 0;
  }
  glFlush();

}

// The routine that initializes or reinitializes upon resize
void XyzGL::resizeGL( int winx, int winy )
{
  struct xxyzwin *xx = mXyz;

  ivwControlPriority(xx->vi, xx->ctrl);

  xx->winx = winx;
  xx->winy = winy;
  b3dSetCurSize(winx, winy);

  b3dResizeViewportXY(winx, winy);
  
  mWin->GetCIImages();
  xx->exposed = 1;
}

// Handlers for mouse events
void XyzGL::mousePressEvent(QMouseEvent * event )
{
  int button1, button2, button3;
  int mx, my, mz;
  mMousePressed = true;

  ivwControlPriority(mXyz->vi, mXyz->ctrl);
  
  button1 = event->state() & Qt::LeftButton ? 1 : 0;
  button2 = event->state() & Qt::MidButton ? 1 : 0;
  button3 = event->state() & Qt::RightButton ? 1 : 0;

  switch(event->button()){
  case Qt::LeftButton:
    if ((button2) || (button3))
        break;
    but1downt.start();
    mXyz->whichbox = mWin->Getxyz(event->x(), event->y(), &mx, &my, &mz);
    break;

  case Qt::MidButton:
    if ((button1) || (button3))
      break;
    mWin->B2Press(event->x(), event->y());
    break;

  case Qt::RightButton:
    if ((button1) || (button2))
      break;
    mWin->B3Press(event->x(), event->y());
    break;

  default:
    break;
  }

  mXyz->lmx = event->x();
  mXyz->lmy = event->y();
}

void XyzGL::mouseReleaseEvent( QMouseEvent * event )
{
  mMousePressed = false;
  if (event->button() == Qt::LeftButton){
      if (but1downt.elapsed() > 250)
        return;
      mWin->B1Press(event->x(), event->y());
  }
}

void XyzGL::mouseMoveEvent( QMouseEvent * event )
{
  int button1, button2, button3;
  if(!mMousePressed)
    return;

  ivwControlPriority(mXyz->vi, mXyz->ctrl);
  
  button1 = event->state() & Qt::LeftButton ? 1 : 0;
  button2 = event->state() & Qt::MidButton ? 1 : 0;
  button3 = event->state() & Qt::RightButton ? 1 : 0;

  if ( (button1) && (!button2) && (!button3) && but1downt.elapsed() > 250)
    mWin->B1Drag(event->x(), event->y());
  if ( (!button1) && (button2) && (!button3))
    mWin->B2Drag(event->x(), event->y());
  if ( (!button1) && (!button2) && (button3))
    mWin->B3Drag(event->x(), event->y());
  
  mXyz->lmx = event->x();
  mXyz->lmy = event->y();
}
