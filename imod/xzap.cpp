
/*  IMOD VERSION 2.50
 *
 *  xzap.c -- The Zap Window.
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
Revision 1.1.2.7  2002/12/14 05:23:42  mast
backing out the fancy subclass, adjusting for new visual detection

Revision 1.1.2.6  2002/12/13 07:09:19  mast
GLMainWindow needed different name for mouse event processors

Revision 1.1.2.5  2002/12/13 06:06:29  mast
using new glmainwindow and mainglwidget classes

Revision 1.1.2.4  2002/12/12 01:24:50  mast
Added Z slider

Revision 1.1.2.3  2002/12/10 16:57:34  mast
preventing multiple draws, implementing current contour draw while dragging

Revision 1.1.2.2  2002/12/09 23:23:49  mast
Plugged image memory leak

Revision 1.1.2.1  2002/12/09 17:50:33  mast
Qt version

Revision 3.9  2002/12/01 16:51:34  mast
Changes to eliminate warnings on SGI

Revision 3.8  2002/12/01 15:34:41  mast
Changes to get clean compilation with g++

Revision 3.7  2002/11/14 01:15:56  mast
Prevent 3rd mouse button drag from moving scattered points or points off
the section

Revision 3.6  2002/10/22 22:41:47  mast
Changed some debug messages for the expose timeouts

Revision 3.5  2002/09/13 21:03:58  mast
Changes to minimize crashes with Ti4600 when resizing window with R -
elimination of interfering draws, and postpone of draw after expose events

Revision 3.4  2002/07/28 22:58:42  mast
Made I pop Info window to front and added a button to toolbar to do this

Revision 3.3  2002/07/21 20:29:50  mast
changed number of columns for section number to 4

Revision 3.2  2002/01/28 16:53:59  mast
Added section number to call to b3dDrawGreyScalePixelsHQ

Revision 3.1  2001/12/17 18:52:40  mast
Added hotkeys to do smoothing and next section in autocontouring

*/
#include <stdio.h>
#include <math.h>
#include <qcursor.h>
#include <qbitmap.h>
#include <qdatetime.h>
#include <qcursor.h>
#include <qapplication.h>
#include <qpoint.h>
#include <qtimer.h>
#include "zap_classes.h"

#ifdef REPORT_TIMES
#include <sys/times.h>
#include <time.h>
#endif

#define NO_X_INCLUDES
#include <diaP.h>
#include "imod.h"
#include "xzap.h"

#include "qcursor.bits"
#include "qcursor_mask.bits"

//#define XZAP_DEBUG

void inputQDefaultKeys(QKeyEvent *event, ImodView *vw);


static void zapDraw_cb(struct ViewInfo *vi, void *client, int drawflag);
static void zapClose_cb(struct ViewInfo *vi, void *client, int drawflag);
static int zapDraw(ZapStruct *zap);
static int zapReallyDraw(ZapStruct *zap);
static void zapButton1(struct zapwin *zap, int x, int y);
static void zapButton2(struct zapwin *zap, int x, int y);
static void zapButton3(struct zapwin *zap, int x, int y, int controlDown);
static void zapB1Drag(struct zapwin *zap, int x, int y);
static void zapB2Drag(struct zapwin *zap, int x, int y);
static void zapB3Drag(struct zapwin *zap, int x, int y, int controlDown);

static int  zapDrawGraphics(ZapStruct *zap);
static void zapDrawModel(ZapStruct *zap);
static void zapDrawContour(ZapStruct *zap, int co, int ob);
static void zapDrawCurrentContour(ZapStruct *zap, int co, int ob);
static void zapDrawCurrentPoint(ZapStruct *zap, int undraw);
static int  zapDrawAuto(ZapStruct *zap);
static void zapDrawGhost(ZapStruct *zap);
static void zapDrawTools(ZapStruct *zap);
static void zapSetCursor(ZapStruct *zap, int mode);
static int  zapXpos(ZapStruct *zap, double x);
static int  zapYpos(ZapStruct *zap, double x);
static void zapGetixy(ZapStruct *zap, int mx, int my, float *x, float *y);
static int  zapPointVisable(ZapStruct *zap, Ipoint *pnt);
static void zapAutoTranslate(ZapStruct *zap);
static void zapSyncImage(ZapStruct *win);
static void zapQuit(ZapStruct *zap);
static void zapResizeToFit(ZapStruct *zap);



/* DNM 1/19/01: add this to allow key to substitute for middle mouse button */
static int insertDown = 0;

/* DNM 3/9/01: add this to provide gloabl lock on movie snapshot use */
static int movieSnapLock = 0;
static QTime insertTime;
static QTime but1downt;



void zapHelp()
{
  dia_vasmsg
    ("Imod Zap Help\n",
     "---------------------------------------------------------------\n",
     "The Tool Bar\n\n",
     "\tThe Up and Down Arrows step the zoom factor up or down.\n",
     "\tThe Zoom edit box shows the current zoom factor and allows ",
     "one to type in a custom zoom.\n",
     "\tThe checkerboard button toggles between fast rendering and ",
     "slower but higher quality image rendering.\n",
     "\tThe lock button can lock out movements within the Zap window ",
     "and prevent centering on the current model point.\n",
     "\tThe centering button toggles between having the image always ",
     "centered ",
     "on the current model point, and recentering the image only when ",
     "the current point comes near an edge (the default).\n"
     "\tThe modeling direction button toggles between inserting new ",
     "points after the current point (when pointing up) or before ",
     "the current point (when pointing down).\n"
     "\tThe section edit box shows the current section and allows one ",
     "to go directly to a section by typing in a number.\n",
     "\tThe Info button (I) brings the Information Window to the "
     "front and prints information about window and image size (see the "
     "I Hot Key below).\n"
     "\tIf multiple image files have been loaded into Imod, three "
     "additional controls appear.  The Time Lock button will prevent "
     "changes in other windows  from changing the time (image file) "
     "displayed in this Zap window.  The left and right arrows will "
     "step backward and forward in time.\n"
     "\tPress the space bar to hide or show the tool bar.\n\n",
     "---------------------------------------------------------------\n",
     "\nHot Keys special to the Zap window\n\n"
     "\ti toggles the modeling direction.\n",
     "\tS or Ctrl-S saves the Zap window, or the area inside the "
     "rubber band, into an RGB or TIFF file.\n"
     "\tZ toggles auto section advance on and off.  When this is on, ",
     "the section will change automatically after inserting a point if ",
     "there was a section change between that point and the previous ",
     "point.\n",
     "\tb builds a contour when AutoContour window is open.\n",
     "\ta advances to and fills next section when auto contouring.\n",
     "\tu smooths a filled area when auto contouring.\n",
     "\tB toggles the rubber band on and off.  The rubber band can be "
     "used to select an area, then snapshot the area, resize the window "
     "to that area, or find its coordinates.  The size of the band can "
     "be adjusted by placing the pointer near an edge or corner and "
     "dragging with the left mouse button.  The band can be moved as a "
     "unit by placing the pointer near an edge and dragging with the "
     "middle mouse button.\n",
     "\tI prints information about the window and image size and "
     "the coordinates of the image in the window.  If the rubber band "
     "is on, the sizes and coordinates are relative to the rubber band "
     "rather than the window.  The image "
     "coordinates of the lower left and upper right corners, and of "
     "the center, are printed in the Imod info window.  There is also "
     "a fragment of a command line for extracting the image from the "
     "stack with \"newst\".  This key also brings the Information "
     "Window to the front of the display.\n",
     "\tR resizes the window.  With the rubber band off, the window "
     "changes, "
     "if possible, to match the size of the entire image at the "
     "current zoom.  With the rubber band on, it changes to the size "
     "of the rubber band and the image is shifted to display the area "
     "previously in the rubber band.\n",
     "\tArrow keys and the keypad: In movie mode, the arrow keys and "
     "the PageUp and PageDown keys move the current viewing point (the "
     "small cross), while the keypad keys pan the image horizontally,"
     " vertically, and diagonally.  In model mode, the arrow keys pan "
     "the image, the numeric keypad arrows move the current model point "
     "laterally, and the numeric keypad PageUp and PageDown keys move "
     "the current model point in Z.\n"
     "\tIns on the keypad: In movie mode, this key works the same as "
     "the middle mouse button.  A single keystrike adds one point; "
     "holding the key down allows points to be added continuously.\n"
     "\tESC will close the Zap window.\n\n"
     "For other keys, see Help - Hot Keys in the Imod Info Window.\n\n",
     "---------------------------------------------------------------\n",
     "\nMouse button function in movie mode\n\n",
     "\tLeft Button Click: Select the current viewing point, marked by "
     "a small cross.\n",
     "\tLeft Button Drag: Pan the image if it is larger than the "
     "window, or adjust the size of the rubber band."
     "\n"
     "\tMiddle Button: Start movie in forward direction, or stop movie."
     "\n"
     "\tMiddle Button Drag: Move the rubber band.\n",
     "\tRight Button: Start movie in backward direction, or stop movie."
     "\n\n"
     "Mouse button function in model mode\n\n",
     "\tLeft Button Click: Make the nearest model point be the current "
     "model point.  If there is no point nearby, this detaches from the "
     "current point and contour and selects a current viewing point "
     "instead.\n",
     "\tLeft Button Drag: Pan the image if it is larger than the "
     "window, or adjust the size of the rubber band."
     "\n"
     "\tMiddle Button Click: Add one point to the current contour.\n"
     "\tMiddle Button Drag: Continually add points to the current "
     "contour as the mouse is moved, or move the rubber band.\n",
     "\tRight Button Click: Modify the current model point to be at the "
     "selected position.\n",
     "\tRight Button Drag: Continually modify points as the mouse is "
     "moved.  This only works when the current model point is in the "
     "interior of the contour, not at its end.\n",
     "\tCtrl - Right Button Click: Delete any points under the cursor "
     "in the current contour.\n",
     "\tCtrl - Right Button Drag: Continually delete points under the "
     "cursor as the mouse is moved.  At the end, the current point is "
     "set before the last deletion (or after, if modeling direction is "
     "inverted.)\n",
     NULL);
  return;
}

// This receives the close signal back from the controller, tells the
// window to close, and sets the closing flag
static void zapClose_cb(ImodView *vi, void *client, int junk)
{
  ZapStruct *zap = (ZapStruct *)client;

  // Needed?
  if (!zap || zap->closing)
    return;

#ifdef XZAP_DEBUG
  fprintf(stderr, "Sending zap window close.\n");
#endif
  zap->closing = 1;
  zap->qtWindow->close();
}

/* This receives a closing request/signal from the window */
void zapClosing(ZapStruct *zap)
{
#ifdef XZAP_DEBUG
  fprintf(stderr, "ZapClosing received.\n");
#endif

  // If we are not closing it already, start the quit 
  if (!zap->closing) {
    zap->closing = 1;
    zapQuit(zap);
  }

  // Do cleanup (of questionable purpose) 
  zap->popup = False;

  b3dFreeCIImage(zap->image);
  zap->ctrl  = 0;
  zap->image = NULL;
  zap->winx  = zap->winy = 0;
  if (movieSnapLock && zap->movieSnapCount)
    movieSnapLock = 0;

  // What for?
  imod_info_input();     
  free(zap);
#ifdef XZAP_DEBUG
  fprintf(stderr, "Zap Killed.\n");
#endif
}

/* This initiates the quit sequence by telling Control to delete this window */
static void zapQuit(ZapStruct *zap)
{
   ivwDeleteControl(zap->vi, zap->ctrl);
#ifdef XZAP_DEBUG
  fprintf(stderr, "Zap Control deleted.\n");
#endif
  return;
}


/* DNM 3/8/01: check for whether to start a movie snapshot sequence, and 
   manage the lock that keeps more than one window from starting or stopping
   a snap sequence */
static void checkMovieSnap(ZapStruct *zap, int dir)
{
  int start, end;

  /* If the lock is set and this window is the owner, clear the lock */
  if (movieSnapLock && zap->movieSnapCount) {
    zap->movieSnapCount = 0;
    movieSnapLock = 0;
  }

  /* done if no movie, or if the lock is still set, or if no snapshots are
     desired.  I.E., don't let another window start a sequence if one
     was already going */
  if (!zap->vi->zmovie || movieSnapLock || !imcGetSnapshot(zap->vi))
    return;
     
  /* Get start and end of loop, compute count */
  imcGetStartEnd(zap->vi, 2, &start, &end);
  zap->movieSnapCount = (end - start) / imcGetIncrement(zap->vi, 2) + 1;
  if (zap->movieSnapCount < 1)
    zap->movieSnapCount = 1;

  /* double count for normal mode, leave as is for one-way */
  if (!imcGetLoopMode(zap->vi))
    zap->movieSnapCount *= 2;

  /* Set to start or end depending on which button was hit */
  if (dir > 0)
    zap->vi->zmouse = start;
  else
    zap->vi->zmouse = end;

  /* set the lock and draw */
  movieSnapLock = 1;
  zapDraw_cb(zap->vi, zap, IMOD_DRAW_XYZ);
}

// This is the external draw command from the controller
void zapDraw_cb(ImodView *vi, void *client, int drawflag)
{
  ZapStruct *zap = (ZapStruct *)client;
  int *limits;
  int limarr[4];

#ifdef XZAP_DEBUG
  fprintf(stderr, "Zap Draw\n");
#endif

  if (!zap) return;
  if ((!zap->popup) || (!zap->ginit)) return;
     
  zapSetCursor(zap, vi->imod->mousemode);

  // zapDrawTools(zap);

  if (drawflag){
    if (drawflag & IMOD_DRAW_SLICE)
      zap->showslice = 1;

    /* DNM: skip this, it is covered by the zapdraw call below and the
       items that it sets are not needed by the flush or sync */
    /* b3dWinset(XtDisplay(zap->gfx), zap->gfx, (XID)zap->context); */

    if (drawflag & IMOD_DRAW_IMAGE){
      b3dFlushImage(zap->image);
    }
          
    if (!(drawflag & IMOD_DRAW_ACTIVE) && !(drawflag & IMOD_DRAW_NOSYNC))
      zapSyncImage(zap);

    /* DNM: replace multiple calls with one call to internal drawing 
       routine */
    if (zapDraw(zap))
      return;

    /* DNM 3/8/01: add autosnapshot when movieing */
    if (imcGetSnapshot(zap->vi) && zap->vi->zmovie && 
        zap->movieSnapCount) {
      limits = NULL;
      if (zap->rubberband) {
        limits = limarr;
        limarr[0] = zap->bandllx + 1;
        limarr[1] = zap->winy - zap->bandury;
        limarr[2] = zap->bandurx - 1 - zap->bandllx;
        limarr[3] = zap->bandury - 1 - zap->bandlly;
      }
      if (imcGetSnapshot(zap->vi) == 1)
        b3dAutoSnapshot("zap", SnapShot_RGB, limits);
      else
        b3dAutoSnapshot("zap", SnapShot_TIF, limits);
      zap->movieSnapCount--;
      /* When count expires, stop movie and clear the lock */
      if(!zap->movieSnapCount) {
        zap->vi->zmovie = 0;
        movieSnapLock = 0;
      }
    }
  }
  return;
}

/*
 *  Sync the pan position to the current model point. 
 */
#define BORDER_FRAC  0.1
#define BORDER_MIN  50
#define BORDER_MAX  125

static void zapSyncImage(ZapStruct *win)
{
  int syncborder, wposition, wsize, tripshift;
  int trytrans, trydraws, tryborder, trystart;
  ImodView *vi = win->vi;
  if ((!win->lock) && (vi->imod->mousemode == IMOD_MMODEL)){
    if (win->vi->imod->cindex.point >= 0){

      /* If the keepcentered flag is set, just do a shift to center */
      if (win->keepcentered)
        tripshift = 1;
      else {

        /* Otherwise, look at each axis independently.  First see if
           if the position is within the borders for shifting */
        tripshift = 0;
        wsize = win->winx;
        wposition = zapXpos(win, (double)vi->xmouse);
        syncborder = (int)(wsize * BORDER_FRAC);
        if (syncborder < BORDER_MIN)
          syncborder = BORDER_MIN;
        if (syncborder > BORDER_MAX)
          syncborder = BORDER_MAX;
        if (wposition < syncborder || wposition > wsize - syncborder){

          /* If close to a border, do ani mage offset computation
             to see if the display would actually get moved if
             this axis were centered on point */
          trytrans = (int)((vi->xsize * 0.5f) - vi->xmouse + 0.5f);
          trydraws = win->xdrawsize;
          tryborder = win->xborder;
          trystart = win->xstart;
          /* printf ("before %d %d %d %d\n", 
             trydraws, win->xtrans, tryborder, trystart); */
          b3dSetImageOffset(wsize, vi->xsize, win->zoom, &trydraws,
                            &trytrans, &tryborder, &trystart);
          /* printf ("after %d %d %d %d\n", 
             trydraws, trytrans, tryborder, trystart); */
          /* Can't use xtrans for a test, need to use the other
             two values to see if change in display would occur */
          if (tryborder != win->xborder || trystart != win->xstart)
            tripshift += 1;

        }

        /* Same for Y axis */
        wsize = win->winy;
        wposition = zapYpos(win, (double)vi->ymouse);
        syncborder = (int)(wsize * BORDER_FRAC);
        if (syncborder < BORDER_MIN)
          syncborder = BORDER_MIN;
        if (syncborder > BORDER_MAX)
          syncborder = BORDER_MAX;
        if (wposition < syncborder || wposition > wsize - syncborder){
          trytrans = (int)((vi->ysize * 0.5f) - vi->ymouse + 0.5f);
          trydraws = win->ydrawsize;
          tryborder = win->yborder;
          trystart = win->ystart;
          b3dSetImageOffset(wsize, vi->ysize, win->zoom, &trydraws,
                            &trytrans, &tryborder, &trystart);
          if (tryborder != win->yborder || trystart != win->ystart)
            tripshift += 2;
        }
      }

      if (tripshift) {
        /* fprintf(stderr, "tripshift %d\n",tripshift); */
        win->xtrans = (int)((vi->xsize * 0.5f) - vi->xmouse + 0.5f);
        win->ytrans = (int)((vi->ysize * 0.5f) - vi->ymouse + 0.5f);
      }
    }
  }
}

static int drawRetval;

static int zapDraw(ZapStruct *zap)
{
  drawRetval = 0;
  zap->gfx->updateGL();
  return drawRetval;
}


// This is the central drawing routine
/* DNM 6/22/01: change it to return int so that it can return 1 if no drawing
   is done, to avoid double snapshots */
static int zapReallyDraw(ZapStruct *zap)
{
  int ob;
  drawRetval = 0;

  /* DNM 9/10/02: skip a draw if expose timeout is active or a resize
     was started and not finished yet */
#ifdef ZAP_EXPOSE_HACK
  if (zap->exposeTimeOut || zap->resizeSkipDraw) {
#ifdef XZAP_DEBUG
    fprintf(stderr, "Skipping a draw because of expose timeout\n");
#endif
    return 0;
  }
#endif

  zap->gfx->makeCurrent();
  b3dSetCurSize(zap->winx, zap->winy);

  /* But this wait is also needed to prevent the crashes */
  //glXWaitX();
  zapAutoTranslate(zap);

  // If the current only flag is set, swap the displayed buffer into the
  // drawing buffer and just draw the current contour
  if (zap->drawCurrentOnly > 0) {
    if (App->doublebuffer)
      zap->gfx->swapBuffers();
    ob = zap->vi->imod->cindex.object;
    imodSetObjectColor(ob); 
    b3dLineWidth(zap->vi->imod->obj[ob].linewidth2); 
    zapDrawCurrentContour(zap, zap->vi->imod->cindex.contour, ob);
    zap->drawCurrentOnly = -1;
    return 0;
  }
  
  zap->drawCurrentOnly = 0;

  /* DNM: this call returns 1 if further drawing can be skipped */
  if (zapDrawGraphics(zap)) {
    drawRetval = 1;
    return 1;
  }

  zapDrawModel(zap);
  zapDrawCurrentPoint(zap, False);
  zapDrawAuto(zap);
  if (zap->rubberband) {
    b3dColorIndex(App->endpoint);
    b3dDrawRectangle(zap->bandllx, zap->winy - 1 - zap->bandury, 
                     zap->bandurx - zap->bandllx, 
                     zap->bandury - zap->bandlly);
  } 
  zapDrawTools(zap);

  return 0;
}



// This receives the resize events which precede paint signals
void zapResize(ZapStruct *zap, int winx, int winy)
{
   int bandmin = 4;

  ivwControlPriority(zap->vi, zap->ctrl);

#ifdef XZAP_DEBUG
  fprintf(stderr, "RESIZE: ");
#endif

  /* DNM 8/10/01: Needed on RH 7.1/GeForce3 to prevent garbage */
#ifdef ZAP_RESIZE_HACK
  zap->resizedraw2x = 1;
#endif
#ifdef ZAP_EXPOSE_HACK
  zap->resizeSkipDraw = 1;
#endif

#ifdef XZAP_DEBUG
  fprintf(stderr, "Size = %d x %d :", winx, winy);
#endif

#ifdef XZAP_DEBUG
  if (zap->ginit)
    fprintf(stderr, "Old Size = %d x %d :", zap->winx, zap->winy);
#endif
  zap->winx = winx;
  zap->winy = winy;
  b3dSetCurSize(winx, winy);
 
  if (zap->ginit){

    /* Make sure the rubber band stays legal, but keep it same size
       if possible */
    if (zap->rubberband) {
      if (zap->bandurx >= winx) {
        if (zap->bandurx + 1 - zap->bandllx > winx) {
          zap->bandurx = winx - 1;
          if (zap->bandllx > zap->bandurx - bandmin)
            zap->bandllx = zap->bandurx - bandmin;
        } else {
          zap->bandllx -= zap->bandurx + 1 - winx;
          zap->bandurx = winx - 1;
        }
      }
      if (zap->bandury >= winy) {
        if (zap->bandury + 1 - zap->bandlly > winy) {
          zap->bandury = winy - 1;
          if (zap->bandlly > zap->bandury - bandmin)
            zap->bandlly = zap->bandury - bandmin;
        } else {
          zap->bandlly -= zap->bandury + 1 - winy;
          zap->bandury = winy - 1;
        }
      }
    }

    b3dFlushImage(zap->image);
  }

  //  b3dResizeViewport();
  glViewport((GLint)0, (GLint)0, (GLsizei)winx, (GLsizei)winy);
  glMatrixMode(GL_PROJECTION);
  glLoadIdentity();

  glOrtho(0.0 , (GLdouble)winx, 0.0, (GLdouble)winy, 0.5, -0.5);
  glMatrixMode(GL_MODELVIEW);
  glLoadIdentity();

  zap->image =  b3dGetNewCIImage(zap->image, App->depth);
  b3dBufferImage(zap->image);
  zap->ginit = 1;

#ifdef XZAP_DEBUG
  fprintf(stderr, "\n");
#endif
  return;
}



/* DNM 9/10/02: when the time out ends after the last expose event, finally 
   do the draws */
#ifdef ZAP_EXPOSE_HACK
void expose_to(XtPointer client_data, XtIntervalId *id)
{
  ZapStruct *zap = (ZapStruct *)client_data;
  zap->exposeTimeOut = (XtIntervalId)0;
  zap->resizeSkipDraw = 0;
#ifdef XZAP_DEBUG
  fprintf(stderr, "Drawing after expose timeout\n");
#endif
  zapDraw(zap);
  if (zap->resizedraw2x)
    zapDraw(zap);
  zap->resizedraw2x = 0;
}
#endif     


// This receives the paint events generated by the window manager
void zapPaint(ZapStruct *zap)
{
  
#ifdef REPORT_TIMES
  static clock_t lasttime, thistime;
  struct tms buf;
  float elapsed;
#endif
  unsigned int interval = 120;    /* The value from midas - could be less */

#ifdef XZAP_DEBUG
  fprintf(stderr, "Paint:");
#endif

#ifdef REPORT_TIMES
  thistime = times(&buf);
  elapsed = 1000.*(float)(thistime - lasttime) / CLK_TCK;
  printf ("%6.1f\n", elapsed);
  lasttime = thistime;
#endif

  /* DNM 9/10/02: start a timeout after every expose event to avoid crashes
     with GeForce4 Ti4600 - possibly due to collisions with X's redraw of
     the window */
#ifndef ZAP_EXPOSE_HACK
  zapReallyDraw(zap);
  if (zap->resizedraw2x) {
#ifdef XZAP_DEBUG
    fprintf(stderr, "Drawing twice: ");
#endif
    //zapReallyDraw(zap);
    zap->qtWindow->mTimer->start(0, false);
  }    
  zap->resizedraw2x = 0;
#else
  if (zap->exposeTimeOut) {
    XtRemoveTimeOut(zap->exposeTimeOut);
#ifdef XZAP_DEBUG
    fprintf(stderr, "Restarting expose timeout for %d", interval);
  } else {
    fprintf(stderr, "Starting an expose timeout for %d", interval);
#endif
  }
  zap->exposeTimeOut = XtAppAddTimeOut(Dia_context, interval, 
                                       expose_to, (XtPointer)zap);
#endif

#ifdef XZAP_DEBUG
  fprintf(stderr, "\n");
#endif
}


/*****************************************************************************/
/* zap tool bar functions called from interface class                        */

void zapStepZoom(ZapStruct *zap, int step)
{
  ivwControlPriority(zap->vi, zap->ctrl);
  zap->zoom = b3dStepPixelZoom(zap->zoom, step);
  zapDraw(zap);
}

void zapEnteredZoom(ZapStruct *zap, float newZoom)
{
  zap->zoom = newZoom;
  if (zap->zoom <= 0.01)
    zap->zoom = 0.01;
  zapDraw(zap);
  zap->qtWindow->setFocus();
}
 
void zapStateToggled(ZapStruct *zap, int index, int state)
{
  int time;
  ivwControlPriority(zap->vi, zap->ctrl);
  switch (index) {
  case ZAP_TOGGLE_RESOL:
    zap->hqgfx = state;
    zapDraw(zap);
    break;

  case ZAP_TOGGLE_ZLOCK:
    zap->lock = state ? 2 : 0;
    if (!zap->lock) {
      b3dFlushImage(zap->image);
      zapSyncImage(zap);
      zapDraw(zap);
    }
    break;

  case ZAP_TOGGLE_CENTER:
    zap->keepcentered = state;
    if (state) {
      b3dFlushImage(zap->image);
      zapSyncImage(zap);
      zapDraw(zap);
    }
    break;

  case ZAP_TOGGLE_INSERT:
    zap->insertmode = state;
    zap->vi->insertmode = zap->insertmode;
    break;

  case ZAP_TOGGLE_TIMELOCK:
    ivwGetTime(zap->vi, &time);
    zap->timeLock = state ? time : 0;
    if (!zap->timeLock)
      zapDraw(zap);
    break;
  }
}




void zapEnteredSection(ZapStruct *zap, int sec)
{

  ivwControlPriority(zap->vi, zap->ctrl);
  if (zap->lock != 2)
    zap->vi->zmouse = sec-1;
  zap->section = sec-1;
  ivwBindMouse(zap->vi);
  imodDraw(zap->vi, IMOD_DRAW_XYZ);
  zap->qtWindow->setFocus();
}

void zapStepTime(ZapStruct *zap, int step)
{
  ivwControlPriority(zap->vi, zap->ctrl);
  
  // if time locked, advance the time lock and draw this window
  // Does this make sense?
  if (zap->timeLock){
    zap->timeLock + step;
    if (zap->timeLock <= 0)
      zap->timeLock = 1;
    if (zap->timeLock > ivwGetMaxTime(zap->vi))
      zap->timeLock = ivwGetMaxTime(zap->vi);
    zapDraw(zap);

  } else {
    if (step > 0)
      inputNextTime(zap->vi);
    else
      inputPrevTime(zap->vi);
  }
}


/*****************************************************************************/

int imod_zap_open(struct ViewInfo *vi)
{
  ZapStruct *zap;
  int    depth;
  int time, tmax, len, maxlen;
  int needWinx, needWiny;
  QString str;
  int deskWidth = QApplication::desktop()->width();
  int deskHeight = QApplication::desktop()->height();

  zap = (ZapStruct *)malloc(sizeof(ZapStruct));
  if (!zap) return(-1);

  zap->vi     = vi;
  zap->ctrl   = 0;
  zap->closing  = 0;
  /* DNM: setting max size of the topLevelShell didn't work on the PC, so
     let's just explicitly limit the size asked for in the image portion */
  zap->winx = deskWidth - 20;
  zap->winy = deskHeight - 76;
  if (vi->xsize < zap->winx)
    zap->winx   = vi->xsize;
  if (vi->ysize < zap->winy)
    zap->winy   = vi->ysize;
  zap->xtrans = zap->ytrans = 0;
  zap->ztrans = 0;
  zap->hqgfx  = False;
  zap->hide   = False;
  zap->zoom   = 1.0;
  zap->data   = NULL;
  zap->image  = NULL;
  zap->ginit  = False;
  zap->lock   = False;
  zap->keepcentered = False;
  zap->insertmode = 0;
  zap->toolstart = 0;
  zap->showslice = 0;
  zap->timeLock = 0;
  zap->toolSection = -1;
  zap->toolZoom = 0.0f;
  zap->toolTime = 0;
  zap->twod = (!(vi->dim&4));
  zap->sectionStep = 0;
  zap->time = 0;
  zap->mousemode = 0;
  zap->rubberband = 0;
  zap->movieSnapCount = 0;
  zap->resizedraw2x = 0;
#ifdef ZAP_EXPOSE_HACK
  zap->exposeTimeOut = (XtIntervalId)0;
#endif
  zap->resizeSkipDraw = 0;
  zap->drawCurrentOnly = 0;

  imod_info_input();
  needWinx = zap->winx;
  needWiny = zap->winy;


  /* Optional time section : find longest string and pass it in */
  if (vi->nt){
    str = " (999)";
    maxlen = -1;
    for (time = 1; time < zap->vi->nt; time++) {
      len = strlen(ivwGetTimeIndexLabel(zap->vi, time));
      if (len > maxlen) {
        maxlen = len;
        tmax = time;
      }
    }
    
    str += ivwGetTimeIndexLabel(zap->vi, tmax);
  }

  zap->qtWindow = new ZapWindow(zap, str, App->qtRgba, App->qtDoubleBuffer,
				App->qtEnableDepth, NULL, "zap window");
  if (!zap->qtWindow){
    free(zap);
    wprint("Error opening zap window.");
    return(-1);
  }
#ifdef XZAP_DEBUG
  puts("Got a zap window");
#endif
  zap->gfx = zap->qtWindow->mGLw;

  str = imodwfname("Imod ZaP Window:");
  if (str.isEmpty())
    str = "Imod ZaP Window";
  zap->qtWindow->setCaption(str);

  str = imodwfname("ZaP Toolbar:");
  if (str.isEmpty())
    str = "ZaP Toolbar";
  zap->qtWindow->mToolBar->setLabel(str);
  
  zap->ctrl = ivwNewControl(vi, zapDraw_cb, zapClose_cb, 
                            (XtPointer)zap);

  /* DNM: this is the second call to this, which caused hanging when 
     imod_info_input tested on all events but dispatched only X events.
     With dispatching of all events, the call can be left here. */
  imod_info_input();

  // Manage the size and position of the window
  QSize toolSize = zap->qtWindow->mToolBar->sizeHint();

  int newWidth = toolSize.width() > needWinx ? toolSize.width() : needWinx;
  int newHeight = needWiny + (zap->qtWindow->height() - zap->gfx->height());
  // If you can show before the resize, the complete geometry adjustment
  // is not needed
  /*  QPoint pos = zap->qtWindow->pos();
  int xleft = pos.x();
  int ytop = pos.y();
  if (xleft + newWidth > deskWidth - 16)
    xleft = deskWidth - 16 - newWidth;
  if (ytop + newHeight > deskHeight - 40)
  ytop = deskHeight - 40 - newHeight; */
  if (Imod_debug)
    fprintf(stderr, "Sizes: zap %d %d, toolbar %d %d, GL %d %d: "
            "resize %d %d\n", zap->qtWindow->width(), zap->qtWindow->height(), 
            toolSize.width(), toolSize.height(), zap->gfx->width(), 
            zap->gfx->height(), newWidth, newHeight);
  zap->qtWindow->resize( newWidth, newHeight);
  //  zap->qtWindow->setGeometry(xleft, ytop, newWidth, newHeight);

  zap->qtWindow->show();
  zap->popup = True;

#ifdef XZAP_DEBUG
  puts("popup a zap dialog");
#endif

  /* DNM: set cursor after window created so it has model mode cursor if
     an existing window put us in model mode */
  zapSetCursor(zap, vi->imod->mousemode);

  insertTime.start();
  return(0);
}

void zapGotoCurrentPoint(ZapStruct *zap)
{
  return;
}

static void zapAutoTranslate(ZapStruct *zap)
{
  if (zap->lock == 2)
    return;

  zap->section = (int)(zap->vi->zmouse + 0.5f);
     
  zapDrawTools(zap);

  if (zap->lock)
    return;

  return;
}

/* DNM: 2.40 deleted imod_zap_draw which was unused and confusing */


static void zapTranslate(ZapStruct *zap, int x, int y)
{
  ImodView *vw = zap->vi;
  zap->xtrans += x;
  if (zap->xtrans > vw->xsize)
    zap->xtrans = vw->xsize;
  if (zap->xtrans < -vw->xsize)
    zap->xtrans = - vw->xsize;
  zap->ytrans += y;
  if (zap->ytrans > vw->ysize)
    zap->ytrans = vw->ysize;
  if (zap->ytrans < -vw->ysize)
    zap->ytrans = - vw->ysize;
  zapDraw(zap);
  return;
}

/* DNM: change the key definitions that were #ifndef __sgi to #ifdef __vms
   since they seemed to work under Linux */

/* static QTime downtime; */

void zapKeyInput(ZapStruct *zap, QKeyEvent *event)
{
  struct ViewInfo *vi = zap->vi;
  int keysym = event->key();
  static int trans = 5;
  int size;
  int *limits;
  int limarr[4];
  int rx, ix, iy;
  int keypad = event->state() & Qt::Keypad;
  int handled = 0;
  /* downtime.start(); */

  ivwControlActive(vi, 0);
  // NO PLUGINS FOR A WHILE
  //  if (imodPlugHandleKey(vi, event)) return;
  ivwControlActive(vi, 1);

  /* DNM: set global insertmode from this zap's mode to get it to work
     right with Delete key */
  vi->insertmode = zap->insertmode;

  /*      fprintf(stderr, "Zapo got %x keysym\n", keysym); */


  switch(keysym){

  case Qt::Key_Up:
  case Qt::Key_Down: 
  case Qt::Key_Right: 
  case Qt::Key_Left: 
    // Translate with keypad in movie mode or regular arrows in model mode
    if (!keypad && vi->imod->mousemode != IMOD_MMOVIE ||
        keypad && vi->imod->mousemode == IMOD_MMOVIE) {
      if (keysym == Qt::Key_Left)
        zapTranslate(zap, -trans, 0);
      if (keysym == Qt::Key_Right)
        zapTranslate(zap, trans, 0);
      if (keysym == Qt::Key_Down)
        zapTranslate(zap, 0, -trans);
      if (keysym == Qt::Key_Up)
        zapTranslate(zap, 0, trans);
      handled = 1;

      // Move point with keypad in model mode
    } else if (keypad && vi->imod->mousemode != IMOD_MMOVIE) {
      if (keysym == Qt::Key_Left)
        inputPointMove(vi, -1, 0, 0);
      if (keysym == Qt::Key_Right)
        inputPointMove(vi, 1, 0, 0);
      if (keysym == Qt::Key_Down)
        inputPointMove(vi, 0, -1, 0);
      if (keysym == Qt::Key_Up)
        inputPointMove(vi, 0, 1, 0);
      handled = 1;

    }
    break;

  case Qt::Key_Prior:
  case Qt::Key_Next:
    // With keypad, translate in movie mode or move point in model mode
    if (keypad) {
      if (keysym == Qt::Key_Next) {
        if (vi->imod->mousemode == IMOD_MMOVIE)
          zapTranslate(zap, trans, -trans);
        else
          inputPointMove(vi, 0, 0, -1);
      } else {
        if (vi->imod->mousemode == IMOD_MMOVIE)
          zapTranslate(zap, trans, trans);
        else
          inputPointMove(vi, 0, 0, 1);
      }
      handled = 1;

      // with regular keys, handle specially if locked
    } else if (!keypad && zap->lock == 2){
      if (keysym == Qt::Key_Next)
        zap->section--;
      else
        zap->section++;
      if (zap->section < 0) zap->section = 0;
      if (zap->section >= vi->zsize) 
        zap->section = vi->zsize -1;
      zapDraw(zap);
      handled = 1;
    }
    break;
          
  case Qt::Key_Home:
    if (keypad && vi->imod->mousemode == IMOD_MMOVIE) {
      zapTranslate(zap, -trans, trans);
      handled = 1;
    }
    break;

  case Qt::Key_End:
    if (keypad && vi->imod->mousemode == IMOD_MMOVIE) {
      zapTranslate(zap, -trans, -trans);
      handled = 1;
    }
    break;
          
  case Qt::Key_Minus:
    zap->zoom = b3dStepPixelZoom(zap->zoom, -1);
    zapDraw(zap);
    handled = 1;
    break;

  case Qt::Key_Equal:
    zap->zoom = b3dStepPixelZoom(zap->zoom, 1);
    zapDraw(zap);
    handled = 1;
    break;
          
    /* DNM: KP insert key, find mouse position, set flag, pass like 
       middle mouse button */
  case Qt::Key_Insert:
    /* But skip out if in movie mode */
    if (!keypad || vi->imod->mousemode == IMOD_MMOVIE)
      break;

    // It wouldn't work going to a QPoint and accessing it, so do it in shot!
    ix = (zap->gfx->mapFromGlobal(QCursor::pos())).x();
    iy = (zap->gfx->mapFromGlobal(QCursor::pos())).y();
    insertDown = 1;

    /* Use time since last event to determine whether to treat like
       single click or drag */
    rx = insertTime.elapsed();
    insertTime.restart();
    /* fprintf(stderr, " %d %d %d\n ", rx, ix, iy); */
    if(rx > 250)
      zapButton2(zap, ix, iy);
    else
      zapB2Drag(zap, ix, iy); 
    zap->lmx = ix;
    zap->lmy = iy;
    handled = 1;
    break;

    /* DNM 12/13/01: add next and smooth hotkeys to autox */
  case Qt::Key_A:
    autox_next(vi->ax);
    handled = 1;
    break;

  case Qt::Key_U:
    autox_smooth(vi->ax);
    handled = 1;
    break;

  case Qt::Key_B:
    if (event->state() & Qt::ShiftButton) { 
      if (zap->rubberband)
        zap->rubberband = 0;
      else {
        zap->rubberband = 1;
        size = (int)(zap->zoom * vi->xsize / 4);
        if (size > zap->winx / 4)
          size = zap->winx / 4;
        zap->bandllx = zap->winx / 2 - size;
        zap->bandurx = zap->winx / 2 + size;
        size = (int)(zap->zoom * vi->ysize / 4);
        if (size > zap->winy / 4)
          size = zap->winy / 4;
        zap->bandlly = zap->winy / 2 - size;
        zap->bandury = zap->winy / 2 + size;
      }
      zapDraw(zap);
    } else
      autox_build(vi->ax);
    handled = 1;
    break;
          
  case Qt::Key_S:
    if ((event->state() & Qt::ShiftButton) || 
        (event->state() & Qt::ControlButton)){
      zapDraw(zap);
      limits = NULL;
      if (zap->rubberband) {
        limits = limarr;
        limarr[0] = zap->bandllx + 1;
        limarr[1] = zap->winy - zap->bandury;
        limarr[2] = zap->bandurx - 1 - zap->bandllx;
        limarr[3] = zap->bandury - 1 - zap->bandlly;
      }
      if (event->state() & Qt::ShiftButton)
        b3dAutoSnapshot("zap", SnapShot_RGB, limits);
      else
        b3dAutoSnapshot("zap", SnapShot_TIF, limits);
    }else
      inputSaveModel(vi);
    handled = 1;
    break;
          
  case Qt::Key_Escape:
    zapQuit(zap);
    handled = 1;
    break;

  case Qt::Key_R:
    if (event->state() & Qt::ShiftButton) {
      zapResizeToFit(zap);
      handled = 1;
    }
    break;

  case Qt::Key_Z:
    if (event->state() & Qt::ShiftButton) { 
      if(zap->sectionStep) {
        zap->sectionStep = 0;
        wprint("Auto-section advance turned OFF\n");
      } else {
        zap->sectionStep = 1;
        wprint("\aAuto-section advance turned ON\n");
      }
      //XBell(imodDisplay(), 100);
    } else
      imod_zap_open(vi);
    handled = 1;
    break;

  case Qt::Key_I:
    if (event->state() & Qt::ShiftButton)
      zapPrintInfo(zap);
    else {
      zapStateToggled(zap, ZAP_TOGGLE_INSERT, 1 - zap->insertmode);
      zap->qtWindow->setToggleState(ZAP_TOGGLE_INSERT, zap->insertmode);
      wprint("\aToggled modeling direction\n");
      //XBell(imodDisplay(), 100);
    }
    handled = 1;
    break;

    /*
      case Qt::Key_X:
      case Qt::Key_Y:
      {
      Dimension width, height, neww, newh;
      Position dx, dy;
      int delta = 1;
      XtVaGetValues(zap->dialog,
      XmNwidth, &width,
      XmNheight, &height,
      XmNx, &dx, XmNy, &dy,
      NULL);
      if (event->state() & ShiftButton)
      delta = -1;
      if (keysym == Qt::Key_X)
      width += delta;
      else
      height += delta;
      printf ("%d x %d\n", width, height);
      XtConfigureWidget(zap->dialog, dx, dy, width, height, 0);
      }
    */


  default:
    break;

  }

  // If event not handled, pass up to default processor
  if (handled)
    event->accept();    // redundant action - supposed to be the default
  else {
    // What does this mean? Is it wise?  more actions will occur...
    ivwControlActive(vi, 0);
    inputQDefaultKeys(event, vi);
  }
}

void zapKeyRelease(ZapStruct *zap, QKeyEvent *event)
{
  /*  printf ("%d down\n", downtime.elapsed()); */
  insertDown = 0;
}

static int firstdrag = 0;
static int moveband = 0;
static int firstmx, firstmy;

// respond to a mouse press event
void zapMousePress(ZapStruct *zap, QMouseEvent *event)
{
  int button1, button2, button3;
  ivwControlPriority(zap->vi, zap->ctrl);
  
  button1 = event->stateAfter() & Qt::LeftButton ? 1 : 0;
  button2 = event->stateAfter() & Qt::MidButton ? 1 : 0;
  button3 = event->stateAfter() & Qt::RightButton ? 1 : 0;

  /* fprintf(stderr, "click at %d %d\n", event->x(), event->y()); */

  switch(event->button()){
    case Qt::LeftButton:
      but1downt.start();
      firstdrag = 1;
      firstmx = event->x();
      firstmy = event->y();
      break;
    case Qt::MidButton:
      if ((button1) || (button3))
        break;
      zapButton2(zap, event->x(), event->y());
      break;
    case Qt::RightButton:
      if ((button1) || (button2))
        break;
      zapButton3(zap, event->x(), event->y(), 
                 event->state() & Qt::ControlButton);
      break;
    default:
      break;
    }
    zap->lmx = event->x();
    zap->lmy = event->y();

}

// respond to mouse release event
void zapMouseRelease(ZapStruct *zap, QMouseEvent *event)
{
  ivwControlPriority(zap->vi, zap->ctrl);
  if (event->button() == Qt::LeftButton){
    firstdrag = 0;
    if (but1downt.elapsed() > 250) {
        if (zap->hqgfxsave)
          zapDraw(zap);
        zap->hqgfxsave  = 0;
        return;    //IS THIS RIGHT?
      }
    zapButton1(zap, event->x(), event->y());
  }
 
  // Button 2 and band moving, release te band
  if ((event->button() == Qt::MidButton) && zap->rubberband && moveband) {
    moveband = 0;
    if (zap->hqgfxsave)
      zapDraw(zap);
    zap->hqgfxsave  = 0;

    // Button 2 and doing a drag draw - draw for real.
  } else if ((event->button() == Qt::MidButton) && zap->drawCurrentOnly) {
    zap->drawCurrentOnly = 0;
    zapDraw(zap);
  }
}

// Respond to a mouse move event (mouse down)
void zapMouseMove(ZapStruct *zap, QMouseEvent *event)
{
  int button1, button2, button3;
  ivwControlPriority(zap->vi, zap->ctrl);
  
  button1 = event->state() & Qt::LeftButton ? 1 : 0;
  button2 = event->state() & Qt::MidButton ? 1 : 0;
  button3 = event->state() & Qt::RightButton ? 1 : 0;

    /* fprintf(stderr, "mb  %d|%d|%d\n", button1, button2, button3); */
  if ( (button1) && (!button2) && (!button3)){
    /* DNM: wait for a bit, but if we do not replace original 
       lmx, lmy, there is a disconcerting lurch */
      if ((but1downt.elapsed()) > 250)
        zapB1Drag(zap, event->x(), event->y());
      /*  else
          break; */
    }

    if ( (!button1) && (button2) && (!button3))
      zapB2Drag(zap, event->x(), event->y());

    if ( (!button1) && (!button2) && (button3))
      zapB3Drag(zap, event->x(), event->y(),
                event->state() & Qt::ControlButton);

    zap->lmx = event->x();
    zap->lmy = event->y();
}


/* Attach to nearest point in model mode, or just modify the current 
   xmouse, ymouse values */

void zapButton1(ZapStruct *zap, int x, int y)
{
  ImodView *vi   = zap->vi;
  Imod     *imod = vi->imod;
  Ipoint pnt, *spnt;
  Iindex index;
  int i, temp_distance;
  int distance = -1;
  float ix, iy;
  float selsize = IMOD_SELSIZE / zap->zoom;

  zapGetixy(zap, x, y, &ix, &iy);
     
  if (vi->ax)
    if (vi->ax->altmouse == AUTOX_ALTMOUSE_PAINT){
      autox_fillmouse(vi, (int)ix, (int)iy);
      return;
    }
     
  if (vi->imod->mousemode == IMOD_MMODEL){
    pnt.x = ix;
    pnt.y = iy;
    pnt.z = zap->section;
    vi->xmouse = ix;
    vi->ymouse = iy;
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

    /* DNM: add the DRAW_XYZ flag to make it update info and Slicer */
    imodDraw(vi, IMOD_DRAW_RETHINK | IMOD_DRAW_XYZ);
    return;
  }

  vi->xmouse = ix;
  vi->ymouse = iy;

  imodDraw(vi, IMOD_DRAW_XYZ);
}

/* In model mode, add a model point, creating a new contour if necessary */

void zapButton2(ZapStruct *zap, int x, int y)
{
  ImodView *vi = zap->vi;
  Iobj  *obj;
  Icont *cont;
  Ipoint point, *cpoint;
  int   pt;
  float ix, iy;
  float lastz;
  int time;
  int rcrit = 10;   /* Criterion for moving the whole band */
  int dxll, dxur,dyll, dyur;
  zapGetixy(zap, x, y, &ix, &iy);

  if (vi->ax){
    if (vi->ax->altmouse == AUTOX_ALTMOUSE_PAINT){
      /* DNM 2/1/01: need to call with int */
      autox_sethigh(vi, (int)ix, (int)iy);
      return;
    }
  }
     
  moveband = 0;
  /* If rubber band is on and within criterion distance of any edge, set
     flag to move whole band and return */
  if (zap->rubberband) {
    dxll = x - zap->bandllx;
    dxur = x - zap->bandurx;
    dyll = y - zap->bandlly;
    dyur = y - zap->bandury;
    if ((dyll > 0 && dyur < 0 && (dxll < rcrit && dxll > -rcrit ||
                                  dxur < rcrit && dxur > -rcrit)) ||
        (dxll > 0 && dxur < 0 && (dyll < rcrit && dyll > -rcrit ||
                                  dyur < rcrit && dyur > -rcrit))) {
      moveband = 1;
      return;
    }
  }     

  if (vi->imod->mousemode == IMOD_MMODEL){
    obj = imodObjectGet(vi->imod);
    if (!obj)
      return;
    cont = imodContourGet(vi->imod);
    point.x = ix;
    point.y = iy;
    point.z = zap->section;
    if ((zap->twod)&&(cont)&&(cont->psize)){
      point.z = cont->pts->z;
    }
    vi->xmouse = ix;
    vi->ymouse = iy;

    /* If there is no current contour, start a new one */
    if (!cont){
      vi->imod->cindex.contour = obj->contsize - 1;
      NewContour(vi->imod);
      cont = imodContourGet(vi->imod);
      if (!cont)
        return;
      if (iobjFlagTime(obj)){
        ivwGetTime(zap->vi, &time);
        cont->type = time;
        cont->flags |= ICONT_TYPEISTIME;
      }
    }

    /* If contours are closed and Z has changed, start a new contour */
    /* Also check for a change in time, if time data are being modeled */
    if (iobjClose(obj->flags) && !(cont->flags & ICONT_WILD)){
      cpoint = imodPointGet(vi->imod);
      if (cpoint){
        int cz,pz, contim;
        cz = (int)cpoint->z; 
        pz = (int)point.z;
        ivwGetTime(zap->vi, &time);
        contim = time;
        if (iobjFlagTime(obj) && (cont->flags & ICONT_TYPEISTIME))
          contim = cont->type;

        if (cz != pz || time != contim){
          if (cont->psize == 1) {
            wprint("\aStarted a new contour even though last "
                   "contour had only 1 pt.  Use open "
                   "contours to model across sections.\n");
          }
          NewContour(vi->imod);
          cont = imodContourGet(vi->imod);
          if (!cont)
            return;
          if (iobjFlagTime(obj)){
            cont->type = time;
            cont->flags |= ICONT_TYPEISTIME;
          }
        }

      }
    }

    pt = vi->imod->cindex.point;
    if (pt >= 0)
      lastz = cont->pts[pt].z;
    else
      lastz = point.z;

    /* Insert or add point depending on insertion mode and whether at end
       of contour */
    if ((cont->psize - 1) == pt){
      if (zap->insertmode && cont->psize)
        InsertPoint(vi->imod, &point, pt);
      else
        NewPoint(vi->imod, &point);
    }else{
      if (zap->insertmode)
        InsertPoint(vi->imod, &point, pt);
      else
        InsertPoint(vi->imod, &point, pt + 1);
    }

    /* DNM: auto section advance is based on 
       the direction of section change between last
       and just-inserted points */
    if (zap->sectionStep && point.z != lastz) {
      if (point.z - lastz > 0.0)
        vi->zmouse += 1.0;
      else
        vi->zmouse -= 1.0;

      if (vi->zmouse < 0.0)
        vi->zmouse = 0;
      if (vi->zmouse > vi->zsize - 1)
        vi->zmouse = vi->zsize - 1;
               
      imodDraw(vi, IMOD_DRAW_IMAGE | IMOD_DRAW_XYZ);  // Why DRAW_IMAGE?
      imod_info_setocp();
    } else
      imodDraw(vi, IMOD_DRAW_XYZ | IMOD_DRAW_NOSYNC);
      
    return;
  }
  imodMovieXYZT(vi, MOVIE_DEFAULT, MOVIE_DEFAULT, 1,
                MOVIE_DEFAULT);
  checkMovieSnap(zap, 1);
}

/* Delete all points of current contour under the cursor */     
static void zapDelUnderCursor(ZapStruct *zap, int x, int y, Icont *cont)
{
  float ix, iy;
  float crit = 8./ zap->zoom;
  float critsq, dsq;
  int i;
  Ipoint *lpt;
  int deleted = 0;

  zapGetixy(zap, x, y, &ix, &iy);
  critsq = crit * crit;
  for (i = 0; i < cont->psize  && cont->psize > 1; ) {
    lpt = &(cont->pts[i]);
    if (floor((double)lpt->z + 0.5) == zap->section) {
      dsq = (lpt->x - ix) * (lpt->x - ix) +
        (lpt->y - iy) * (lpt->y - iy);
      if (dsq <= critsq) {
        imodPointDelete(cont, i);
        zap->vi->imod->cindex.point = i + zap->insertmode - 1;
        if (zap->vi->imod->cindex.point < 0)
          zap->vi->imod->cindex.point = 0;
        if (zap->vi->imod->cindex.point >= cont->psize)
          zap->vi->imod->cindex.point = cont->psize - 1;
        deleted = 1;
        continue;
      }
    }
    i++;
  }
  if (!deleted)
    return;
  imodDraw(zap->vi, IMOD_DRAW_XYZ | IMOD_DRAW_MOD );
  imod_info_setocp();
}

/* In model mode, modify current point; otherwise run movie */

void zapButton3(ZapStruct *zap, int x, int y, int controlDown)
{
  ImodView *vi = zap->vi;
  Icont *cont;
  int   pt;
  float ix, iy;

  zapGetixy(zap, x, y, &ix, &iy);

  if (vi->ax){
    if (vi->ax->altmouse == AUTOX_ALTMOUSE_PAINT){
      /* DNM 2/1/01: need to call with int */
      autox_setlow(vi, (int)ix, (int)iy);
      return;
    }
  }

  if (vi->imod->mousemode == IMOD_MMODEL){
    cont = imodContourGet(vi->imod);
    pt   = vi->imod->cindex.point;
    if (!cont)
      return;
    if (pt < 0)
      return;

    /* If the control key is down, delete points under the cursor */
    if (controlDown) {
      zapDelUnderCursor(zap, x, y, cont);
      return; 
    }

          
    if (!zapPointVisable(zap, &(cont->pts[pt])))
      return;
    cont->pts[pt].x = ix;
    cont->pts[pt].y = iy;

    vi->xmouse  = ix;
    vi->ymouse  = iy;

    imodDraw(vi, IMOD_DRAW_RETHINK);
    return;
  }
  imodMovieXYZT(vi, MOVIE_DEFAULT, MOVIE_DEFAULT, -1,
                MOVIE_DEFAULT);
  checkMovieSnap(zap, -1);
}

void zapB1Drag(ZapStruct *zap, int x, int y)
{
  static int dragband;
  static int dragging[4];
  int rubbercrit = 10;  /* Criterion distance for grabbing the band */
  int bandmin = 4;     /* Minimum size that the band can become */
  int i, dminsq, dist, distsq, dmin, dxll, dyll, dxur, dyur;
  int minedgex, minedgey;

  if (zap->rubberband && firstdrag) {
    /* First time if rubberbanding, analyze for whether close to a
       corner or an edge */
    dminsq = rubbercrit * rubbercrit;
    minedgex = -1;
    for (i = 0; i < 4; i++)
      dragging[i] = 0;
    dxll = firstmx - zap->bandllx;
    dxur = firstmx - zap->bandurx;
    dyll = firstmy - zap->bandlly;
    dyur = firstmy - zap->bandury;

    /* Find distance from each corner, keep track of a min */
    distsq = dxll * dxll + dyll * dyll;
    if (distsq < dminsq) {
      dminsq = distsq;
      minedgex = 0;
      minedgey = 2;
    }
    distsq = dxur * dxur + dyll * dyll;
    if (distsq < dminsq) {
      dminsq = distsq;
      minedgex = 1;
      minedgey = 2;
    }
    distsq = dxll * dxll + dyur * dyur;
    if (distsq < dminsq) {
      dminsq = distsq;
      minedgex = 0;
      minedgey = 3;
    }
    distsq = dxur * dxur + dyur * dyur;
    if (distsq < dminsq) {
      dminsq = distsq;
      minedgex = 1;
      minedgey = 3;
    }

    /* If we are close to a corner, set up to drag the band */
    if (minedgex >= 0) {
      dragband = 1;
      dragging[minedgex] = 1;
      dragging[minedgey] = 1;
    } else {
      /* Otherwise look at each edge in turn */
      dmin = rubbercrit;
      dist = dxll > 0 ? dxll : -dxll;
      if (dyll > 0 && dyur < 0 && dist < dmin){
        dmin = dist;
        minedgex = 0;
      }
      dist = dxur > 0 ? dxur : -dxur;
      if (dyll > 0 && dyur < 0 && dist < dmin){
        dmin = dist;
        minedgex = 1;
      }
      dist = dyll > 0 ? dyll : -dyll;
      if (dxll > 0 && dxur < 0 && dist < dmin){
        dmin = dist;
        minedgex = 2;
      }
      dist = dyur > 0 ? dyur : -dyur;
      if (dxll > 0 && dxur < 0 && dist < dmin){
        dmin = dist;
        minedgex = 3;
      }
      if (minedgex < 0)
        dragband = 0;
      else {
        dragging[minedgex] = 1;
        dragband = 1;
      }
    }
  }
  firstdrag = 0;
     
  if (zap->rubberband && dragband) {
    /* Move the rubber band */
    if (dragging[0]) {
      zap->bandllx += (x - zap->lmx);
      if (zap->bandllx < 0)
        zap->bandllx = 0;
      if (zap->bandllx > zap->bandurx - bandmin)
        zap->bandllx = zap->bandurx - bandmin;
    }
    if (dragging[1]) {
      zap->bandurx += (x - zap->lmx);
      if (zap->bandurx > zap->winx - 1)
        zap->bandurx = zap->winx - 1;
      if (zap->bandurx < zap->bandllx + bandmin)
        zap->bandurx = zap->bandllx + bandmin;
    }
    if (dragging[2]) {
      zap->bandlly += (y - zap->lmy);
      if (zap->bandlly < 0)
        zap->bandlly = 0;
      if (zap->bandlly > zap->bandury - bandmin)
        zap->bandlly = zap->bandury - bandmin;
    }
    if (dragging[3]) {
      zap->bandury += (y - zap->lmy);
      if (zap->bandury > zap->winy - 1)
        zap->bandury = zap->winy - 1;
      if (zap->bandury < zap->bandlly + bandmin)
        zap->bandury = zap->bandlly + bandmin;
    }

  } else {
    /* Move the image */
    zap->xtrans += (x - zap->lmx);
    zap->ytrans -= (y - zap->lmy);
  }

  zap->hqgfxsave = zap->hqgfx;
  zap->hqgfx = 0;
  zapDraw(zap);
  zap->hqgfx = zap->hqgfxsave;
}

void zapB2Drag(ZapStruct *zap, int x, int y)
{
  ImodView *vi = zap->vi;
  Iobj *obj;
  Icont *cont;
  Ipoint *lpt, cpt;
  float ix, iy;
  double dist;
  int pt;
  int dx, dy;
     
  if (vi->ax){
    if (vi->ax->altmouse == AUTOX_ALTMOUSE_PAINT){
      zapGetixy(zap, x, y, &ix, &iy);
      /* DNM 2/1/01: need to call with int */
      autox_sethigh(vi, (int)ix, (int)iy);
      return;
    }
  }

  if (zap->rubberband && moveband) {
    /* Moving rubber band: get desired move and constrain it to keep
       band in the window */
    dx = x - zap->lmx;
    if (zap->bandllx + dx < 0)
      dx = -zap->bandllx;
    if (zap->bandurx + dx > zap->winx - 1)
      dx = zap->winx - 1 - zap->bandurx;
    dy = y - zap->lmy;
    if (zap->bandlly + dy < 0)
      dy = -zap->bandlly;
    if (zap->bandury + dy > zap->winy - 1)
      dy = zap->winy - 1 - zap->bandury;
    zap->bandllx += dx;
    zap->bandurx += dx;
    zap->bandlly += dy;
    zap->bandury += dy;

    zap->hqgfxsave = zap->hqgfx;
    zap->hqgfx = 0;
    zapDraw(zap);
    zap->hqgfx = zap->hqgfxsave;
    return;
  }

  if (vi->imod->mousemode == IMOD_MMOVIE)
    return;

  if (vi->imod->cindex.point < 0)
    return;

  zapGetixy(zap, x, y, &ix, &iy);

  cpt.x = ix;
  cpt.y = iy;
  cpt.z = zap->section;
     
  obj = imodObjectGet(vi->imod);
  if (!obj)
    return;

  cont = imodContourGet(vi->imod);
  if (!cont)
    return;

  lpt = &(cont->pts[vi->imod->cindex.point]);
  if (zap->twod)
    cpt.z = lpt->z;

  dist = imodel_point_dist( lpt, &cpt);

  if ( dist > vi->imod->res){
    pt = vi->imod->cindex.point;

    /* Insert or add point depending on insertion mode and whether at end
       of contour ; DNM made this work the same as single insert */
    if ((cont->psize - 1) == pt){
      if (zap->insertmode && cont->psize)
        InsertPoint(vi->imod, &cpt, pt);
      else
        NewPoint(vi->imod, &cpt);
    }else{
      if (zap->insertmode)
        InsertPoint(vi->imod, &cpt, pt);
      else
        InsertPoint(vi->imod, &cpt, pt + 1);
    }

    // Set flag for drawing current contour only
    zap->drawCurrentOnly = 1;

    // TODO: figure out the right flags
    imodDraw(vi, IMOD_DRAW_IMAGE | IMOD_DRAW_XYZ | IMOD_DRAW_NOSYNC);
  }
}

void zapB3Drag(ZapStruct *zap, int x, int y, int controlDown)
{
  ImodView *vi = zap->vi;
  Iobj *obj;
  Icont *cont;
  Ipoint *lpt;
  Ipoint pt;
  float ix, iy;

  if (vi->ax){
    if (vi->ax->altmouse == AUTOX_ALTMOUSE_PAINT){
      zapGetixy(zap, x, y, &ix, &iy);
      /* DNM 2/1/01: need to call with int */
      autox_setlow(vi, (int)ix, (int)iy);
      return;
    }
  }

  // if (!(maskr & Button3Mask))
  //  return;

  if (vi->imod->mousemode == IMOD_MMOVIE)
    return;
     
  if (vi->imod->cindex.point < 0)
    return;

  cont = imodContourGet(vi->imod);
  if (!cont)
    return;

  /* DNM 11/13/02: do not allow operation on scattered points */
  obj = imodObjectGet(vi->imod);
  if (iobjScat(obj->flags))
    return;

  if (controlDown) {
    zapDelUnderCursor(zap, x, y, cont);
    return;
  }

  if (vi->imod->cindex.point == (cont->psize - 1))
    return;

  /* DNM 11/13/02: need to test for both next and current points to prevent
     strange moves between sections */
  if (!zapPointVisable(zap, &(cont->pts[vi->imod->cindex.point + 1])) ||
      !zapPointVisable(zap, &(cont->pts[vi->imod->cindex.point])))
    return;

  lpt = &(cont->pts[vi->imod->cindex.point]);
  zapGetixy(zap, x, y, &(pt.x), &(pt.y));
  pt.z = lpt->z;
  if (imodel_point_dist(lpt, &pt) > vi->imod->res){
    ++vi->imod->cindex.point;
    lpt = &(cont->pts[vi->imod->cindex.point]);
    lpt->x = pt.x;
    lpt->y = pt.y;
    lpt->z = pt.z;
    imodDraw(vi, IMOD_DRAW_XYZ | IMOD_DRAW_MOD );
  }
  return;
}


/********************************************************
 * conversion functions between image and window cords. */

/* return x pos in window for given image x cord. */
static int zapXpos(ZapStruct *zap, double x)
{
  return( (int)(((x - zap->xstart) * zap->zoom) 
                + zap->xborder));
}

/* return y pos in window for given image y cord. */
static int zapYpos(ZapStruct *zap, double y)
{
  return((int)(((y - zap->ystart) * zap->zoom)
               + zap->yborder));
}

/* returns image cords in x,y, given mouse coords mx, my */
static void zapGetixy(ZapStruct *zap, int mx, int my, float *x, float *y)
{
  my = zap->winy - my;
  *x = ((float)(mx - zap->xborder) / zap->zoom)
    + (float)zap->xstart;
  *y = ((float)(my - zap->yborder) / zap->zoom)
    + (float)zap->ystart;
  return;
}

/* Prints window size and image coordinates in Info Window */
void zapPrintInfo(ZapStruct *zap)
{
  float xl, xr, yb, yt;
  int ixl, ixr, iyb, iyt;
  int ixcen, iycen, ixofs, iyofs;
  ivwControlPriority(zap->vi, zap->ctrl);
  XRaiseWindow(App->display, XtWindow(App->toplevel));
  if (zap->rubberband) {
    zapGetixy(zap, zap->bandllx + 1, zap->bandlly + 1, &xl, &yt);
    zapGetixy(zap, zap->bandurx - 1, zap->bandury - 1, &xr, &yb);
  } else {
    zapGetixy(zap, 0, 0, &xl, &yt);
    zapGetixy(zap, zap->winx, zap->winy, &xr, &yb);
  }
  ixl = (int)(xl + 0.5);
  ixr = (int)(xr - 0.5);
  iyb = (int)(yb + 0.5);
  iyt = (int)(yt - 0.5);
  wprint("(%d,%d) to (%d,%d); ", ixl + 1, iyb + 1, ixr + 1, iyt + 1);
  ixcen = (ixr + 1 + ixl)/2;
  iycen = (iyt + 1 + iyb)/2;
  ixofs = ixcen - zap->vi->xsize/2;
  iyofs = iycen - zap->vi->ysize/2;
  wprint("Center (%d,%d)\n", ixcen + 1, iycen + 1);
  wprint("To excise: newst -si %d,%d -of %d,%d\n", ixr + 1 - ixl, 
         iyt + 1 - iyb, ixofs, iyofs);
  if (zap->rubberband) 
    wprint("Rubberband: %d x %d; ", zap->bandurx - 1 - zap->bandllx, 
           zap->bandury - 1 - zap->bandlly);
  else
    wprint("Window: %d x %d;   ", zap->winx, zap->winy);
  wprint("Image: %d x %d\n", ixr + 1 - ixl, iyt + 1 - iyb);
}

/* Resize window to fit either whole image or part in rubber band */
static void zapResizeToFit(ZapStruct *zap)
{
  int width, height, neww, newh, limw, limh;
  int dx, dy, newdx, newdy;
  float xl, xr, yb, yt;
  width = zap->qtWindow->width();
  height = zap->qtWindow->height();
  QPoint pos = zap->qtWindow->pos();
  dx = pos.x();
  dy = pos.y();
  if (zap->rubberband) {
    /* If rubberbanding, set size to size of band, and offset
       image by difference between band and window center */
    neww = zap->bandurx -1 - zap->bandllx + width - zap->winx;
    newh = zap->bandury -1 - zap->bandlly + height - zap->winy;
    zapGetixy(zap, zap->bandllx, zap->bandlly, &xl, &yt);
    zapGetixy(zap, zap->bandurx, zap->bandury, &xr, &yb);
    zap->xtrans = (int)(-(xr + xl - zap->vi->xsize) / 2);
    zap->ytrans = (int)(-(yt + yb - zap->vi->ysize) / 2);
    zap->rubberband = 0;
  } else {
    /* Otherwise, make window the right size for the image */
    neww = (int)(zap->zoom * zap->vi->xsize + width - zap->winx);
    newh = (int)(zap->zoom * zap->vi->ysize + height - zap->winy);
  }

  limw = QApplication::desktop()->width();
  limh = QApplication::desktop()->height();
  if (neww > limw - 24)
    neww = limw - 24;
  if (newh > limh - 44)
    newh = limh - 44;
  newdx = dx + width / 2 - neww / 2;
  newdy = dy + height / 2 - newh / 2;
  if (newdx < 16)
    newdx = 16;
  if (newdy < 36)
    newdy = 36;
  if (newdx + neww > limw - 8)
    newdx = limw - 8 - neww;
  if (newdy + newh > limh - 8)
    newdy = limh - 8 - newh;

#ifdef XZAP_DEBUG
  fprintf(stderr, "configuring widget...");
#endif
#ifdef ZAP_EXPOSE_HACK
  imodMovieXYZT(zap->vi, 0, 0, 0, 0);
#endif
  zap->qtWindow->setGeometry(newdx, newdy, neww, newh);
#ifdef XZAP_DEBUG
  fprintf(stderr, "back\n");
#endif
}
     
     

/****************************************************************************/
/* drawing routines.                                                        */

static int doingBWfloat = 0;

/* Draws the image.  Returns 1 if further drawing can be skipped */
static int zapDrawGraphics(ZapStruct *zap)
{
  XGCValues val;
  ImodView *vi = zap->vi;
  unsigned char *pixptr;
  Colorindex *data = (Colorindex *)zap->data;
  int i, j, x, y, z;
  int jsize, xlim;
  int xstop, ystop, ystep;
  int zoom = 1;
  int time;
  int xz, yz;
  unsigned char *imageData;
  int skipDraw = 0;

  ivwGetLocation(vi, &x, &y, &z);

  b3dSetCurPoint(x, y, zap->section);

  zoom = (int)zap->zoom;

  b3dSetImageOffset(zap->winx, vi->xsize, zap->zoom,
                    &zap->xdrawsize, &zap->xtrans, 
                    &zap->xborder, &zap->xstart);

  b3dSetImageOffset(zap->winy, vi->ysize, zap->zoom,
                    &zap->ydrawsize, &zap->ytrans, 
                    &zap->yborder, &zap->ystart);

  if (zap->timeLock) {
    imageData = ivwGetZSectionTime(vi, zap->section, zap->timeLock);
    time = zap->timeLock;
  } else{
    /* flush if time is different. */
    ivwGetTime(vi, &time);
    if (time != zap->time){
      b3dFlushImage(zap->image);
      zap->time = time;
    }
    imageData = ivwGetZSection(vi, zap->section);
  }

  /* DNM: set sliders if doing float.  Set flag that the routine is
     being called, so that it won't be called again when it initiates a
     redraw.  If a redraw actually occurred, set the flag to skip drawing
     on the rest of this invocation and in the rest of zapdraw */

  if(!doingBWfloat) {
    doingBWfloat = 1;
    if (imod_info_bwfloat(vi, zap->section, time) && App->rgba)
      skipDraw = 1;
    doingBWfloat = 0;
  }

  if (!skipDraw) {

    b3dDrawBoxout(zap->xborder, zap->yborder, 
                  zap->xborder + (int)(zap->xdrawsize * zap->zoom),
                  zap->yborder + (int)(zap->ydrawsize * zap->zoom));
    b3dDrawGreyScalePixelsHQ(imageData,
                             vi->xsize, vi->ysize,
                             zap->xstart, zap->ystart,
                             zap->xborder, zap->yborder,
                             zap->xdrawsize, zap->ydrawsize,
                             zap->image,
                             vi->rampbase, 
                             zap->zoom, zap->zoom,
                             zap->hqgfx, zap->section);
  }
  return(skipDraw);
}

static void zapDrawModel(ZapStruct *zap)
{
  ImodView *vi = zap->vi;
  int ob, co;
  int surf = -1;
  Icont *cont = imodContourGet(vi->imod);

  if (vi->imod->drawmode <= 0)
    return;

  zapDrawGhost(zap);

  if (cont)
    surf = cont->surf;

  for(ob = 0; ob < vi->imod->objsize; ob++){
    if (iobjOff(vi->imod->obj[ob].flags))
      continue;
    imodSetObjectColor(ob); 
    b3dLineWidth(vi->imod->obj[ob].linewidth2); 

    for(co = 0; co < vi->imod->obj[ob].contsize; co++){
      if (ob == vi->imod->cindex.object){
        if (co == vi->imod->cindex.contour){
          zapDrawCurrentContour(zap, co, ob);
          continue;
        }
        if (vi->ghostmode & IMOD_GHOST_SURFACE)
          if (surf >= 0)
            if (surf != vi->imod->obj[ob].cont[co].surf){
              b3dColorIndex(App->ghost); 
              zapDrawContour(zap, co, ob);
              imodSetObjectColor(ob);
              continue;
            }
      }

      zapDrawContour(zap, co, ob); 
    }
  }
  return;
}

void zapDrawSymbol(int mx, int my, 
                   unsigned char sym,
                   unsigned char size, 
                   unsigned char flags)
{
    
  switch (sym){
  case IOBJ_SYM_CIRCLE:
    if (flags  & IOBJ_SYMF_FILL)
      b3dDrawFilledCircle(mx, my, size);
    else
      b3dDrawCircle(mx, my, size);
    break;
  case IOBJ_SYM_SQUARE:
    if (flags  & IOBJ_SYMF_FILL)
      b3dDrawFilledSquare(mx, my, size);
    else
      b3dDrawSquare(mx, my, size);
    break;
  case IOBJ_SYM_TRIANGLE:
    if (flags  & IOBJ_SYMF_FILL)
      b3dDrawFilledTriangle(mx, my, size);
    else
      b3dDrawTriangle(mx, my, size);
    break;
  case IOBJ_SYM_STAR:
    break;
  case IOBJ_SYM_NONE:
    b3dDrawPoint(mx, my);
    break;

  default:
    return;

  }
  return;
}

static void zapDrawCurrentContour(ZapStruct *zap, int co, int ob)
{
  ImodView *vi = zap->vi;
  Iobj  *obj  = &(vi->imod->obj[ob]);
  Icont *cont = &(vi->imod->obj[ob].cont[co]);
  Ipoint *point;
  int pt;
  int cz = zap->section;

  if (!cont->psize)
    return;

  if (iobjClose(obj->flags)){
    zapDrawContour(zap, co, ob);
    return;
  }
  if (iobjScat(obj->flags)){
    zapDrawContour(zap, co, ob);
    return;
  }

  /* open contour */
  if (cont->psize > 1){
    for(pt = 0; pt < cont->psize; pt++){
      point = &(cont->pts[pt]);
      if (zapPointVisable(zap, point)){
        zapDrawSymbol(zapXpos(zap, cont->pts[pt].x),
                      zapYpos(zap, cont->pts[pt].y),
                      obj->symbol,
                      obj->symsize,
                      obj->symflags);
        if (pt < (cont->psize - 1))
          if (zapPointVisable(zap, &(cont->pts[pt+1])))
            b3dDrawLine(zapXpos(zap, point->x),
                        zapYpos(zap, point->y),
                        zapXpos(zap, cont->pts[pt+1].x),
                        zapYpos(zap, cont->pts[pt+1].y));
      }
    }
          
    if (vi->drawcursor){
      if ((zapPointVisable(zap, cont->pts)) && 
          (zapPointVisable(zap, &(cont->pts[1]))))
        b3dDrawCircle(zapXpos(zap, cont->pts[0].x),
                      zapYpos(zap, cont->pts[0].y), 3);
      if ((zapPointVisable(zap, &(cont->pts[cont->psize - 1]))) &&
          (!zapPointVisable(zap, &(cont->pts[cont->psize - 2]))))
        b3dDrawCircle
          (zapXpos(zap, cont->pts[cont->psize - 1].x),
           zapYpos(zap, cont->pts[cont->psize - 1].y), 3);
    }
  }else{
    /* DNM: I never stopped being confused by off-section display 
       of single point, so got rid of it */
    if (zapPointVisable(zap, cont->pts))
      zapDrawSymbol(zapXpos(zap, cont->pts[0].x),
                    zapYpos(zap, cont->pts[0].y),
                    obj->symbol,
                    obj->symsize,
                    obj->symflags);
  }

  if (obj->symflags & IOBJ_SYMF_ENDS){
    if (zapPointVisable(zap, &(cont->pts[cont->psize-1]))){
      b3dColorIndex(App->endpoint);
      b3dDrawCross(zapXpos(zap, cont->pts[cont->psize-1].x),
                   zapYpos(zap, cont->pts[cont->psize-1].y), 
                   obj->symsize/2);
    }
    if (zapPointVisable(zap, cont->pts)){
      b3dColorIndex(App->bgnpoint);
      b3dDrawCross(zapXpos(zap, cont->pts->x),
                   zapYpos(zap, cont->pts->y),
                   obj->symsize/2);
    }
    imodSetObjectColor(ob);
  }
  return;
}

static void zapDrawContour(ZapStruct *zap, int co, int ob)
{
  ImodView *vi = zap->vi;
  float vert[3];
  Iobj  *obj  = &(vi->imod->obj[ob]);
  Icont *cont = &(vi->imod->obj[ob].cont[co]);
  Ipoint *point;
  int pt, npt = 0, ptsonsec;
  int curTime = vi->ct;
  float drawsize;

  if ((!cont) || (!cont->psize))
    return;

  if (zap->timeLock) curTime = zap->timeLock;

  /* check for contours that contian time data. */
  /* Don't draw them if the time isn't right. */
  /* DNM 6/7/01: but draw contours with time 0 regardless of time */
  if (vi->nt){
    if (iobjTime(obj->flags)){
      if (cont->type && (curTime != cont->type))
        return;
    }
  }

  if (iobjClose(obj->flags)){
          
    if ((!(cont->flags & ICONT_WILD)) && 
        (!zapPointVisable(zap, &(cont->pts[0])))){
      return;
    }

    b3dBeginLine();
    for (pt = 0; pt < cont->psize; pt++){
      if (!zapPointVisable(zap, &(cont->pts[pt])))
        continue;
      b3dVertex2i(zapXpos(zap, cont->pts[pt].x),
                  zapYpos(zap, cont->pts[pt].y));
    }
          
    if (!(cont->flags & ICONT_OPEN))
      if (!( (co == Model->cindex.contour) &&
             (ob == Model->cindex.object ))){
        point = &(cont->pts[0]);
        if (zapPointVisable(zap, point)){
          b3dVertex2i(zapXpos(zap, point->x),
                      zapYpos(zap, point->y));
        }
      }
    b3dEndLine();

    if (obj->symbol != IOBJ_SYM_NONE)
      for (pt = 0; pt < cont->psize; pt++){
        if (!zapPointVisable(zap, &(cont->pts[pt])))
          continue;
        zapDrawSymbol(zapXpos(zap, cont->pts[pt].x),
                      zapYpos(zap, cont->pts[pt].y),
                      obj->symbol,
                      obj->symsize,
                      obj->symflags);
      }
  }

  if (iobjOpen(obj->flags)){
    if ((!(cont->flags & ICONT_WILD)) && 
        (!zapPointVisable(zap, &(cont->pts[0])))){
      return;
    }
          
    for(pt = 0; pt < cont->psize; pt++){
      point = &(cont->pts[pt]);
      if (zapPointVisable(zap, point)){
        zapDrawSymbol(zapXpos(zap, cont->pts[pt].x),
                      zapYpos(zap, cont->pts[pt].y),
                      obj->symbol,
                      obj->symsize,
                      obj->symflags);
        if (pt < (cont->psize - 1))
          if (zapPointVisable(zap, &(cont->pts[pt+1])))
            b3dDrawLine(zapXpos(zap, point->x),
                        zapYpos(zap, point->y),
                        zapXpos(zap, cont->pts[pt+1].x),
                        zapYpos(zap, cont->pts[pt+1].y));
      }
    }
  }
     
  /* scattered contour */
  if (iobjScat(obj->flags)){
    for (pt = 0; pt < cont->psize; pt++){
      if (zapPointVisable(zap, &(cont->pts[pt]))){
        zapDrawSymbol(zapXpos(zap, cont->pts[pt].x),
                      zapYpos(zap, cont->pts[pt].y),
                      obj->symbol,
                      obj->symsize,
                      obj->symflags);
      }
      drawsize = imodPointGetSize(obj, cont, pt);
      if (drawsize > 0)
        if (zapPointVisable(zap, &(cont->pts[pt]))){
          /* DNM: make the product cast to int, not drawsize */
          b3dDrawCircle(zapXpos(zap, cont->pts[pt].x),
                        zapYpos(zap, cont->pts[pt].y),
                        (int)(drawsize * zap->zoom));
          if (drawsize > 3)
            b3dDrawPlus(zapXpos(zap, cont->pts[pt].x), 
                        zapYpos(zap, cont->pts[pt].y), 3);
        }else{
          if (drawsize > 1){
            /* DNM: fixed this at last, but let size round
               down so circles get smaller*/
            /* draw a smaller circ if further away. */
            vert[0] = (cont->pts[pt].z - zap->section) *
              App->cvi->imod->zscale;
            if (vert[0] < 0)
              vert[0] *= -1.0f;
                        
            if (vert[0] < drawsize - 0.01){
              vert[1] = sqrt((double)(drawsize * 
                                      drawsize) - vert[0] * vert[0])
                * zap->zoom;
              b3dDrawCircle(zapXpos(zap, cont->pts[pt].x),
                            zapYpos(zap, cont->pts[pt].y),
                            (int)vert[1]);
            }
          }
        }
    }
  }
     
  if (obj->symflags & IOBJ_SYMF_ENDS){
    if (zapPointVisable(zap, &(cont->pts[cont->psize-1]))){
      b3dColorIndex(App->endpoint);
      b3dDrawCross(zapXpos(zap, cont->pts[cont->psize-1].x),
                   zapYpos(zap, cont->pts[cont->psize-1].y), 
                   obj->symsize/2);
    }
    if (zapPointVisable(zap, cont->pts)){
      b3dColorIndex(App->bgnpoint);
      b3dDrawCross(zapXpos(zap, cont->pts->x),
                   zapYpos(zap, cont->pts->y),
                   obj->symsize/2);
    }
    imodSetObjectColor(ob);
  }
     
  return;
}


static void zapDrawCurrentPoint(ZapStruct *zap, int undraw)
{
  ImodView *vi = zap->vi;
  Iobj *obj = imodObjectGet(vi->imod);
  Icont *cont = imodContourGet(vi->imod);
  Ipoint *pnt = imodPointGet(vi->imod);
  int psize = 3;
  int x,y;
  int curTime = vi->ct;
  int contime;

  if (!vi->drawcursor) return;

  if (zap->timeLock)
    curTime = zap->timeLock;
  contime = curTime;

  if ((App->cvi->imod->mousemode == IMOD_MMOVIE)||(!pnt)){
    x = zapXpos(zap, (double)((int)vi->xmouse + 0.5));
    y = zapYpos(zap, (double)((int)vi->ymouse + 0.5));
    b3dColorIndex(App->foreground);
    b3dDrawPlus(x, y, psize);
          
  }else{
    if ((cont) && (cont->psize) && (pnt)){

      /* DNM 6/17/01: display off-time features as if off-section */
      if (iobjTime(obj->flags) && (cont->flags & ICONT_TYPEISTIME) &&
          cont->type)
        contime = cont->type;
      x = zapXpos(zap, pnt->x);
      y = zapYpos(zap, pnt->y);
      if (zapPointVisable(zap, pnt) && contime == curTime){
        b3dColorIndex(App->foreground);
      }else{
        b3dColorIndex(App->shadow);
      }
      b3dDrawCircle(x, y, psize);
    }
  }
     
  if (zap->showslice){
    b3dColorIndex(App->foreground);
    b3dDrawLine(x, y,
                zapXpos(zap, zap->vi->slice.zx1+0.5f),
                zapYpos(zap, zap->vi->slice.zy1+0.5f));
    b3dDrawLine(x, y,
                zapXpos(zap, zap->vi->slice.zx2+0.5f), 
                zapYpos(zap, zap->vi->slice.zy2+0.5f));
    zap->showslice = 0;
  }

  /* draw begin/end points for current contour */
  if (cont){
    if (iobjTime(obj->flags) && (cont->flags & ICONT_TYPEISTIME) &&
        cont->type)
      contime = cont->type;
    if (contime != curTime)
      return;

    if (cont->psize > 1){
      if (zapPointVisable(zap, cont->pts)){
        b3dColorIndex(App->bgnpoint);
        b3dDrawCircle(zapXpos(zap, cont->pts->x),
                      zapYpos(zap, cont->pts->y), 2);
      }
      if (zapPointVisable(zap, &(cont->pts[cont->psize - 1]))){
        b3dColorIndex(App->endpoint);
        b3dDrawCircle(zapXpos(zap, cont->pts[cont->psize - 1].x),
                      zapYpos(zap, cont->pts[cont->psize - 1].y), 
                      2);
      }
    }
  }
  return;
}

static void zapDrawGhost(ZapStruct *zap)
{
  int ob, co, i;
  short red, green, blue;
  float vert[2];
  struct Mod_Object *obj;
  struct Mod_Contour *cont;
  Imod *mod = zap->vi->imod;
  int nextz, prevz, iz;

  if (!mod)
    return;

  if ( !(zap->vi->ghostmode & IMOD_GHOST_SECTION))
    return;
     
  obj = imodObjectGet(mod);
  if (!obj ) return;

  /* DNM: don't do scattered points - point size works for that */
  if(iobjScat(obj->flags))
    return;

  red = (int)((obj->red * 255.0) / 3.0);
  green = (int)((obj->green * 255.0) / 3.0);
  blue = (int)((obj->blue * 255.0) / 3.0);

  b3dMapColor(App->ghost, red, green, blue); 
  b3dColorIndex(App->ghost);  

  /* DNM: if it's RGB, just have to set the color here */
  if (App->rgba)
    glColor3f(red/255., green/255., blue/255.);

  /* DNM 6/16/01: need to be based on zap->section, not zmouse */
  nextz = zap->section + 1;
  prevz = zap->section - 1;
     
  for(co = 0; co < obj->contsize; co++){
    cont = &(obj->cont[co]);

    /* DNM: don't display wild contours, only coplanar ones */
    /* By popular demand, display ghosts from lower and upper sections */
    if (cont->pts && !(cont->flags & ICONT_WILD)) {
      iz = (int)floor((double)cont->pts->z + 0.5);
      if ((iz == nextz && (zap->vi->ghostmode & IMOD_GHOST_PREVSEC))
          || (iz == prevz && (zap->vi->ghostmode & IMOD_GHOST_NEXTSEC)
              )){
        b3dBeginLine();
        for (i = 0; i < cont->psize; i++){
          b3dVertex2i(zapXpos(zap, cont->pts[i].x),
                      zapYpos(zap, cont->pts[i].y));
        }

        /* DNM: connect back to start only if closed contour */
        if (iobjClose(obj->flags) && !(cont->flags & ICONT_OPEN))
          b3dVertex2i(zapXpos(zap, cont->pts->x),
                      zapYpos(zap, cont->pts->y));
        b3dEndLine();
      }
    }
  }
  return;
}


static int zapDrawAuto(ZapStruct *zap)
{
  ImodView *vi = zap->vi;
  unsigned long i, j;
  float vert[2];
  unsigned short cdat;
  int x, y;
  unsigned long pixel;
  unsigned long xsize,ysize;
  int rectsize;

  xsize = vi->xsize;
  ysize = vi->ysize;

  if (!vi->ax)
    return(-1);

  if (!vi->ax->filled)
    return(-1);

  if (vi->ax->cz != zap->section)
    return(-1);

  cdat = App->endpoint;

  /* DNM 8/11/01: make rectangle size be nearest integer and not 0 */
  rectsize = zap->zoom < 1 ? 1 : (int)(zap->zoom + 0.5);
  for (j = 0; j < ysize; j++){
    y = zapYpos(zap,j);
    for(i = 0; i < xsize; i++){
      x = zapXpos(zap,i);
      /*DNM 2/1/01: pick a dark and light color to work in rgb mode */
      if (vi->ax->data[i + (j * vi->xsize)] & AUTOX_BLACK){
        pixel = App->ghost;
        b3dColorIndex(pixel);
        b3dDrawFilledRectangle(x, y, rectsize, rectsize);
        continue;
      }
      if (vi->ax->data[i + (j * vi->xsize)] & AUTOX_FLOOD){
        pixel = App->endpoint;
        b3dColorIndex(pixel);
        b3dDrawFilledRectangle(x, y, rectsize, rectsize);
        continue;
      }
               
      if (vi->ax->data[i + (j * vi->xsize)] & AUTOX_WHITE){
        pixel = App->select;
        b3dColorIndex(pixel);
        b3dDrawFilledRectangle(x, y, rectsize, rectsize);
      }

    }
  }

  return(0);
}

  // send a new value of section, zoom, or time label if it has changed
static void zapDrawTools(ZapStruct *zap)
{
  QString qstr;

  if (zap->toolSection != zap->section){
    zap->toolSection = zap->section;
    zap->qtWindow->setSectionText(zap->section + 1);
  }
     
  if (zap->toolZoom != zap->zoom){
    zap->toolZoom = zap->zoom;
    zap->qtWindow->setZoomText(zap->zoom);
  }

  if (zap->vi->nt) {
    int time = zap->timeLock ? zap->timeLock : zap->vi->ct;
    if (zap->toolTime != time){
      zap->toolTime = time;
      qstr.sprintf(" (%3d)", time);
      qstr += ivwGetTimeIndexLabel(zap->vi, time);
      zap->qtWindow->setTimeLabel(qstr);
    }
  }
}

static void zapSetCursor(ZapStruct *zap, int mode)
{
  if (zap->mousemode != mode){
    if (mode == IMOD_MMODEL) {
      QBitmap bmCursor(qcursor_width, qcursor_height, qcursor_bits,
                       true);
      QBitmap bmMask(qcursor_width, qcursor_height, qcursor_mask_bits, 
                     true);
      QCursor cursor(bmCursor, bmMask, qcursor_x_hot, qcursor_y_hot);
      zap->gfx->setCursor(cursor);
    } else
      zap->gfx->unsetCursor();
    zap->mousemode = mode;
  }
  return;
}


static int zapPointVisable(ZapStruct *zap, Ipoint *pnt)
{
  int cz;

  if (zap->twod) return(1);

  /* DNM 11/30/02: replace +/- alternatives with standard nearest int code */
  cz = (int)floor(pnt->z + 0.5);
    
  if (cz == zap->section)
    return(1);
    
  return(0);
}
