/*  IMOD VERSION 2.02
 *
 *  xxyz.h -- Include file for xyz.c, the XYZ Window.
 *
 *  Author: James Kremer email: kremer@colorado.edu
 */

/*****************************************************************************
 *   Copyright (C) 1995-1996 by Boulder Laboratory for 3-Dimensional Fine    *
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
Revision 3.2  2002/11/25 19:22:16  mast
Added a structure element for control id

Revision 3.1  2002/01/28 16:54:55  mast
Added structure elements for new enhancements

*/

#ifndef XXYZ_H
#define XXYZ_H

#define XYZ_BSIZE 8

#include <qmainwindow.h>
#include <qgl.h>
#include <qevent.h>
#include <qstring.h>
#include "b3dgfx.h"

struct ViewInfo;
class XyzGL;
class XyzWindow;

struct xxyzwin
{
  struct ViewInfo *vi;   /* Image Data information.              */
  XyzWindow *dialog;         /* The top widget of the xyz window     */
  XyzGL *glw;            /* The drawing widget of the xyz window */
  int ctrl;              /* id of control */
     
  unsigned char *fdataxz; /* tmp data storage for xz image       */
  unsigned char *fdatayz; /* tmp data storage for yz image       */
  B3dCIImage *xydata;    /* Draw buffer for Z slices.            */
  B3dCIImage *xzdata;    /* Draw buffer for Y slices.            */
  B3dCIImage *yzdata;    /* Draw buffer for X slices.            */

  int winx, winy;         /* Size of xyz window.                  */
  int exposed;
  float zoom;
  int closing;

  int lx, ly, lz;

  int xtrans, ytrans;     /* translation (pan) in image coords */
  int xwoffset,ywoffset;  /* offset in window coordinates */
  int lmx, lmy;           /* last mouse position for panning */
  int hq;                 /* High resolution flag */
  int whichbox;           /* box that left mouse button went down in */
};


class XyzWindow : public QMainWindow
{
  Q_OBJECT

    public:
  XyzWindow(struct xxyzwin *xyz, bool rgba, 
            bool doubleBuffer, QWidget * parent = 0,
            const char * name = 0, WFlags f = WType_TopLevel) ;
  ~XyzWindow();
  XyzGL *mGLgfx;

  public slots:

 protected:
  void keyPressEvent ( QKeyEvent * e );
  void closeEvent ( QCloseEvent * e );

 private:

  struct xxyzwin *mXyz;
};

class XyzGL : public QGLWidget
{
  Q_OBJECT

    public:
  XyzGL(struct xxyzwin *xyz, QGLFormat format, QWidget * parent = 0,
        const char * name = 0);
  ~XyzGL();
 
 protected:
  void initializeGL();
  void paintGL();
  void resizeGL( int wdth, int hght );
  void mousePressEvent(QMouseEvent * e );
  void mouseReleaseEvent ( QMouseEvent * e );
  void mouseMoveEvent ( QMouseEvent * e );

 private:
  struct xxyzwin *mXyz;
  bool mMousePressed;
};

/* Functions */
#ifdef __cplusplus
extern "C" {
#endif


#ifdef __cplusplus
}
#endif

#endif /* xxyz.h */

