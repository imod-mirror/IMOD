/*  IMOD VERSION 2.7.9
 *
 *  zap_classes.h -- Header file for ZaP mainwindow and GLwidget classes.
 *
 *  Author: David Mastronarde   email: mast@colorado.edu
 */

/*****************************************************************************
 *   Copyright (C) 1995-2002 by Boulder Laboratory for 3-Dimensional Fine    *
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
*/
#ifndef ZAP_CLASSES_H
#define ZAP_CLASSES_H

#define ZAP_TOGGLE_RESOL 0
#define ZAP_TOGGLE_ZLOCK 1
#define ZAP_TOGGLE_CENTER 2
#define ZAP_TOGGLE_INSERT 3
#define ZAP_TOGGLE_TIMELOCK 4


#include <qmainwindow.h>
#include <qgl.h>
#include <qevent.h>
#include <qstring.h>
class QToolButton;
class ToolEdit;
class QLabel;
class QToolBar;
class QSignalMapper;

struct zapwin;
class ZapGL;

class ZapWindow : public QMainWindow
{
  Q_OBJECT

 public:
  ZapWindow(struct zapwin *zap, QString timeLabel, bool rgba, 
            bool doubleBuffer, QWidget * parent = 0,
            const char * name = 0, WFlags f = WType_TopLevel) ;
  ~ZapWindow();
  ZapGL *mGLgfx;
  QToolBar *mToolBar;

  public slots:
    void zoomUp();
    void zoomDown();
    void help();
    void info();
    void newZoom();
    void newSection();
    void timeBack();
    void timeForward();
    void toggleClicked(int index);
    void setToggleState(int index, int state);
    void setZoomText(float zoom);
    void setSectionText(int section);
    void setTimeLabel(QString label);

 protected:
    void keyPressEvent ( QKeyEvent * e );
    void keyReleaseEvent ( QKeyEvent * e );
    void closeEvent ( QCloseEvent * e );

 private:
    void setupToggleButton(QToolBar *toolBar, QSignalMapper *mapper, 
                           int index);

    struct zapwin *mZap;
    QToolButton *mToggleButs[5];
    int mToggleStates[5];
    ToolEdit *mZoomEdit;
    ToolEdit *mSectionEdit;
    QLabel *mTimeLabel;
};

class ZapGL : public QGLWidget
{
  Q_OBJECT

 public:
  ZapGL(struct zapwin *zap, QGLFormat format, QWidget * parent = 0,
        const char * name = 0);
  ~ZapGL();
  void glReallyDraw() {glDraw();};
 
protected:
  void initializeGL();
  void paintGL();
  void resizeGL( int wdth, int hght );
  void mousePressEvent(QMouseEvent * e );
  void mouseReleaseEvent ( QMouseEvent * e );
  void mouseMoveEvent ( QMouseEvent * e );

 private:
  struct zapwin *mZap;
  bool mMousePressed;
};

#endif     // ZAP_CLASSES_H
