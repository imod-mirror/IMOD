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
Revision 3.2.2.2  2002/12/12 02:45:56  mast
*** empty log message ***

Revision 3.2.2.1  2002/12/12 01:22:29  mast
Changes to become Qt window

Revision 3.2  2002/11/25 19:22:16  mast
Added a structure element for control id

Revision 3.1  2002/01/28 16:54:55  mast
Added structure elements for new enhancements

*/

#ifndef XXYZ_H
#define XXYZ_H

#define XYZ_BSIZE 8

#include <glmainwindow.h>
#include "b3dgfx.h"

/* Forward declarations to minimize includes */
struct ViewInfo;
struct Mod_object;
struct Mod_contour;
class XyzWindow;

struct xxyzwin
{
  struct ViewInfo *vi;   /* Image Data information.              */
  XyzWindow *dialog;         /* The top widget of the xyz window     */
  MainGLWidget *glw;            /* The drawing widget of the xyz window */
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


class XyzWindow : public GLMainWindow
{
  Q_OBJECT

    public:
  XyzWindow(struct xxyzwin *xyz, bool rgba, bool doubleBuffer,
	    QWidget * parent = 0, const char * name = 0,
	    WFlags f = Qt::WDestructiveClose || Qt::WType_TopLevel) ;
  ~XyzWindow();

  void paintGL();
  void resizeGL( int wdth, int hght );
  void mousePressEvent(QMouseEvent * e );
  void mouseReleaseEvent ( QMouseEvent * e );
  void mouseMoveEvent ( QMouseEvent * e );
  void Draw();

 protected:
  void keyPressEvent ( QKeyEvent * e );
  void closeEvent ( QCloseEvent * e );

 private:
  void Quit();
  int Getxyz(int x, int y, int *mx, int *my, int *mz);
  void B1Press(int x, int y);
  void B2Press(int x, int y);
  void B3Press(int x, int y);
  void B1Drag(int x, int y);
  void B2Drag(int x, int y);
  void B3Drag(int x, int y);
  void DrawAuto();
  void DrawModel();
  void DrawImage();
  void GetCIImages();
  void SetSubimage(int absStart, int winSize, int imSize, float zoom,
		   int *drawsize, int *woffset, int *dataStart);
  void DrawGhost();
  void DrawContour(struct Mod_Object *obj, int ob, struct Mod_Contour *cont);
  void DrawCurrentContour(struct Mod_Object *obj, int ob, 
			  struct Mod_Contour *cont);
  void DrawCurrentPoint();
  void DrawCurrentLines();



  struct xxyzwin *mXyz;
};

/* Global functions */
int xxyz_open(struct ViewInfo *vi);

#endif /* xxyz.h */

