/*  IMOD VERSION 2.50
 *
 *  xzap.h -- Header file for ZaP Window.
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
Revision 3.2.2.4  2002/12/14 05:23:42  mast
backing out the fancy subclass, adjusting for new visual detection

Revision 3.2.2.3  2002/12/13 06:06:30  mast
using new glmainwindow and mainglwidget classes

Revision 3.2.2.2  2002/12/10 16:57:34  mast
preventing multiple draws, implementing current contour draw while dragging

Revision 3.2.2.1  2002/12/09 17:50:17  mast
Initial changes to get Qt version

Revision 3.2  2002/12/01 15:34:41  mast
Changes to get clean compilation with g++

Revision 3.1  2002/09/13 21:04:57  mast
Added resizeSkipDraw to prevent redraws during resize

*/

#ifndef XZAP_H
#define XZAP_H

#ifdef __cplusplus
extern "C" {
#endif

  class ZapWindow;
  class ZapGL;

  void zapClosing(struct zapwin *zap);
  void zapPaint(struct zapwin *zap);
  void zapResize(struct zapwin *zap, int winx, int winy);
  void zapKeyInput(struct zapwin *zap, QKeyEvent *e);
  void zapKeyRelease(struct zapwin *zap, QKeyEvent *e);
  void zapMousePress(struct zapwin *zap, QMouseEvent *e);
  void zapMouseRelease(struct zapwin *zap, QMouseEvent *e);
  void zapMouseMove(struct zapwin *zap, QMouseEvent *e);
  void zapHelp(void);
  void zapEnteredZoom(struct zapwin *zap, float newZoom);
  void zapEnteredSection(struct zapwin *zap, int section);
  void zapStepZoom(struct zapwin *zap, int step);
  void zapStateToggled(struct zapwin *zap, int index, int state);
  void zapPrintInfo(struct zapwin *zap);
  void zapStepTime(struct zapwin *zap, int step);
  void zapDrawSymbol(int mx, int my, unsigned char sym, unsigned char size,
		     unsigned char flags);
  int  imod_zap_open(struct ViewInfo *vi);

  typedef struct zapwin
  {
    ZapWindow *qtWindow;               /* Zap window widget. */
    ZapGL *gfx;                  /* Image sub window.  */
    int    winx,      winy;      /* Image window size. */
    int    xborder,   yborder;   /* border around image window. */
    int    xstart,    ystart;
    int    xdrawsize, ydrawsize;
    int    xtrans,    ytrans,    ztrans;
    int    lmx,       lmy;

    int    ginit;

    int    hqgfx, hide;
    int    hqgfxsave;           /* Place to save hqgfx when dragging */
    int    resizedraw2x;        /* Flag to draw twice after resize */
    int    resizeSkipDraw;      /* Flag  to skip drawing during resize */
    int    drawCurrentOnly;
    // XtIntervalId exposeTimeOut; /* Timeouts during expose cascade */

    int rubberband;    /* Rubber banding flag and corner coordinates */
    int bandllx;
    int bandurx;
    int bandlly;
    int bandury;

    int movieSnapCount; /* Counter if this window is doing movie snapshots */

    float  zoom;
    char   *data;

    /* The graphic image buffer. */
    B3dCIImage *image;

    int section;
    int sectionStep; /* auto step image after new model point. */
    int time;
    int lock;
    int keepcentered;
    int mousemode;
    int popup;
    int   toolSection;
    float toolZoom;
    int   toolTime;

    short insertmode;
    short showslice;

    /* Pointer to view and control sturctures. */
    ImodView    *vi;
    int         ctrl;
    int toolstart;

    /* Special, lock time */
    int    timeLock;
    int    twod;

  }ZapStruct;
#ifdef __cplusplus
}
#endif

#endif
