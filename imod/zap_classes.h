/*  IMOD VERSION 2.7.9
 *
 *  zap_classes.h -- Header file for ZaP mainwindow class.
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
Revision 1.1.2.3  2002/12/12 01:25:23  mast
added Z slider

Revision 1.1.2.2  2002/12/09 23:24:12  mast
*** empty log message ***

Revision 1.1.2.1  2002/12/09 17:48:09  mast
Initial addition to source

*/
#ifndef ZAP_CLASSES_H
#define ZAP_CLASSES_H

#define ZAP_TOGGLE_RESOL 0
#define ZAP_TOGGLE_ZLOCK 1
#define ZAP_TOGGLE_CENTER 2
#define ZAP_TOGGLE_INSERT 3
#define ZAP_TOGGLE_TIMELOCK 4


#include <glmainwindow.h>

class QToolButton;
class ToolEdit;
class QLabel;
class QToolBar;
class QSignalMapper;
class QSlider;

struct zapwin;

class ZapWindow : public GLMainWindow
{
  Q_OBJECT

 public:
  ZapWindow(struct zapwin *zap, QString timeLabel, bool rgba, 
            bool doubleBuffer, QWidget * parent = 0,
            const char * name = 0, WFlags f = WType_TopLevel) ;
  ~ZapWindow();
  void paintGL();
  void resizeGL( int wdth, int hght );
  void mousePressEvent(QMouseEvent * e );
  void mouseReleaseEvent ( QMouseEvent * e );
  void mouseMoveEvent ( QMouseEvent * e );
  QToolBar *mToolBar;

  public slots:
    void zoomUp();
    void zoomDown();
    void help();
    void info();
    void newZoom();
    void newSection();
    void sliderChanged(int value);
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
    QSlider *mSecSlider;
};

#endif     // ZAP_CLASSES_H
