/*  IMOD VERSION 2.7.9
 *
 *  zap_classes.cpp -- implementation  ZaP mainwindow and QGLWidget classes.
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
Revision 1.1.2.11  2003/01/13 01:15:43  mast
changes for Qt version of info window

Revision 1.1.2.10  2003/01/02 15:40:27  mast
use dia call to block signals when setting toolbar slider

Revision 1.1.2.9  2002/12/17 17:30:50  mast
Adding timer for redraws, using tooledit with column specifier

Revision 1.1.2.8  2002/12/17 04:45:54  mast
Use new ability to set columns in tooledits

Revision 1.1.2.7  2002/12/14 05:23:42  mast
backing out the fancy subclass, adjusting for new visual detection

Revision 1.1.2.6  2002/12/13 07:09:19  mast
GLMainWindow needed different name for mouse event processors

Revision 1.1.2.5  2002/12/13 06:06:29  mast
using new glmainwindow and mainglwidget classes

Revision 1.1.2.4  2002/12/12 01:25:14  mast
added z slider

Revision 1.1.2.3  2002/12/09 23:24:06  mast
*** empty log message ***

Revision 1.1.2.2  2002/12/09 22:00:29  mast
include stdio and stdlib for atof/atoi calls

Revision 1.1.2.1  2002/12/09 17:47:51  mast
Initial addition to source

*/
#include <stdlib.h>
#include <stdio.h>
#include <qtoolbutton.h>
#include <qlabel.h>
#include <qbitmap.h>
#include <qtoolbar.h>
#include <qsignalmapper.h>
#include <qpushbutton.h>
#include <qlayout.h>
#include <qslider.h>
#include <qtimer.h>

#include "imodP.h"
#include "zap_classes.h"
#include "xzap.h"
#include "tooledit.h"
#include "arrowbutton.h"
#include "dia_qtutils.h"

#define AUTO_RAISE true
#define MIN_SLIDER_WIDTH 20
#define MAX_SLIDER_WIDTH 100

#define BM_WIDTH 16
#define BM_HEIGHT 16

// Unfinished business: recovering bitmap files
#include "unlock.bits"
#include "lock.bits"
#include "time_unlock.bits"
#include "time_lock.bits"
#include "lowres.bits"
#include "highres.bits"

static unsigned char insert_after_bits[] = {
  0x00, 0x00, 0xc0, 0x03, 0xc0, 0x03, 0xc0, 0x03, 0xc0, 0x03, 0x80, 0x01,
  0x80, 0x01, 0x80, 0x01, 0x80, 0x01, 0x80, 0x01, 0x80, 0x01, 0x80, 0x01,
  0x80, 0x01, 0x80, 0x01, 0xff, 0xff, 0xff, 0xff};

static unsigned char insert_before_bits[] = {
  0xff, 0xff, 0xff, 0xff, 0x80, 0x01, 0x80, 0x01, 0x80, 0x01, 0x80, 0x01,
  0x80, 0x01, 0x80, 0x01, 0x80, 0x01, 0x80, 0x01, 0x80, 0x01, 0xc0, 0x03,
  0xc0, 0x03, 0xc0, 0x03, 0xc0, 0x03, 0x00, 0x00};

static unsigned char keepCenter_bits[] = {
  0xff, 0xff, 0x01, 0x80, 0x01, 0x80, 0x01, 0x80, 0x01, 0x80, 0x01, 0x80,
  0x01, 0x80, 0x81, 0x81, 0x81, 0x81, 0x01, 0x80, 0x01, 0x80, 0x01, 0x80,
  0x01, 0x80, 0x01, 0x80, 0x01, 0x80, 0xff, 0xff};

static unsigned char smartCenter_bits[] = {
  0xff, 0xff, 0x01, 0x80, 0x01, 0x80, 0xf9, 0x9f, 0x09, 0x90, 0x09, 0x90,
  0x09, 0x90, 0x09, 0x90, 0x09, 0x90, 0x09, 0x90, 0x09, 0x90, 0x09, 0x90,
  0xf9, 0x9f, 0x01, 0x80, 0x01, 0x80, 0xff, 0xff};

static unsigned char *bitList[5][2] =
  { {lowres_bits, highres_bits},
    {unlock_bits, lock_bits},
    {smartCenter_bits, keepCenter_bits},
    {insert_after_bits, insert_before_bits},
    {time_unlock_bits, time_lock_bits}};

static QBitmap *bitmaps[5][2];
static int firstTime = 1;

ZapWindow::ZapWindow(struct zapwin *zap, QString timeLabel, bool rgba, 
                     bool doubleBuffer, bool enableDepth, QWidget * parent,
                     const char * name, WFlags f) 
  : QMainWindow(parent, name, f)
{
  int j;
  ArrowButton *arrow;

  mZap = zap;

  // Get the toolbar, add zoom arrows
  mToolBar = new QToolBar(this, "zap toolbar");
  if (!AUTO_RAISE) {
    QBoxLayout *boxLayout = mToolBar->boxLayout();
    boxLayout->setSpacing(4);
  }

  arrow = new ArrowButton(Qt::UpArrow, mToolBar, "zoomup button");
  arrow->setAutoRaise(AUTO_RAISE);
  connect(arrow, SIGNAL(clicked()), this, SLOT(zoomUp()));
  arrow = new ArrowButton(Qt::DownArrow, mToolBar, "zoom down button");
  arrow->setAutoRaise(AUTO_RAISE);
  connect(arrow, SIGNAL(clicked()), this, SLOT(zoomDown()));

  mZoomEdit = new ToolEdit(mToolBar, 5, "zoom edit box");
  //mZoomEdit->setFixedWidth(ZOOM_WIDTH);
  mZoomEdit->setFocusPolicy(QWidget::ClickFocus);
  mZoomEdit->setAlignment(Qt::AlignRight);
  connect(mZoomEdit, SIGNAL(returnPressed()), this, SLOT(newZoom()));
  connect(mZoomEdit, SIGNAL(lostFocus()), this, SLOT(newZoom()));

// Make the 4 toggle buttons and their signal mapper
  QSignalMapper *toggleMapper = new QSignalMapper(mToolBar);
  connect(toggleMapper, SIGNAL(mapped(int)), this, SLOT(toggleClicked(int)));
  for (j = 0; j < 4; j++)
    setupToggleButton(mToolBar, toggleMapper, j);

  // Section slider
  QLabel *label = new QLabel("Z", mToolBar);
  label->setAlignment(Qt::AlignRight | Qt::AlignVCenter);
  mSecSlider = new QSlider(1, zap->vi->zsize, 1, 1, Qt::Horizontal, mToolBar,
			  "section slider");
  QSize hint = mSecSlider->minimumSizeHint();
  /* fprintf(stderr, "minimum slider size %d minimum hint size %d\n", 
     mSecSlider->minimumWidth(), hint.width()); */
  int swidth = zap->vi->zsize < MAX_SLIDER_WIDTH ? 
    zap->vi->zsize : MAX_SLIDER_WIDTH;
  swidth = swidth > MIN_SLIDER_WIDTH ? swidth : MIN_SLIDER_WIDTH;
  mSecSlider->setFixedWidth(swidth + hint.width() + 5);
  connect(mSecSlider, SIGNAL(valueChanged(int)), this, 
	  SLOT(sliderChanged(int)));

  // Section edit box
  mSectionEdit = new ToolEdit(mToolBar, 4, "section edit box");
  // mSectionEdit->setFixedWidth(SECTION_WIDTH);
  mSectionEdit->setFocusPolicy(QWidget::ClickFocus);
  mSectionEdit->setAlignment(Qt::AlignRight);
  connect(mSectionEdit, SIGNAL(returnPressed()), this, SLOT(newSection()));
  connect(mSectionEdit, SIGNAL(lostFocus()), this, SLOT(newSection()));
  
  // Info and help buttons
  QPushButton *button = new QPushButton("I", mToolBar, "I button");
  button->setFixedWidth(15);
  button->setFocusPolicy(QWidget::NoFocus);
  connect(button, SIGNAL(clicked()), this, SLOT(info()));

  button = new QPushButton("Help", mToolBar, "Help button");
  button->setFixedWidth((int)(1.2 *fontMetrics().width("Help")));
  button->setFocusPolicy(QWidget::NoFocus);
  connect(button, SIGNAL(clicked()), this, SLOT(help()));

  // Optional section if time enabled
  if (!timeLabel.isEmpty()) {
    mToolBar->addSeparator();
    setupToggleButton(mToolBar, toggleMapper, 4);

    label = new QLabel("4th D", mToolBar);
    label->setAlignment(Qt::AlignRight | Qt::AlignVCenter);

    arrow = new ArrowButton(Qt::LeftArrow, mToolBar, "time back button");
    connect(arrow, SIGNAL(clicked()), this, SLOT(timeBack()));
    arrow->setAutoRaise(AUTO_RAISE);
    arrow->setAutoRepeat(true);
    arrow = new ArrowButton(Qt::RightArrow, mToolBar, "time forward button");
    connect(arrow, SIGNAL(clicked()), this, SLOT(timeForward()));
    arrow->setAutoRaise(AUTO_RAISE);
    arrow->setAutoRepeat(true);

    mTimeLabel = new QLabel(timeLabel, mToolBar, "time label");
  }
  firstTime = 0;

  // Need GLwidget next
  QGLFormat glFormat;
  glFormat.setRgba(rgba);
  glFormat.setDoubleBuffer(doubleBuffer);
  glFormat.setDepth(enableDepth);
  mGLw = new ZapGL(zap, glFormat, this);
  
  // Set it as main widget, set focus, dock on top and bottom only
  setCentralWidget(mGLw);
  setFocusPolicy(QWidget::StrongFocus);
  setDockEnabled(mToolBar, Left, FALSE );
  setDockEnabled(mToolBar, Right, FALSE );

  // This makes the toolbar give a proper size hint before showing window
  setUpLayout();

  mTimer = new QTimer(this);
  connect(mTimer, SIGNAL(timeout()), this, SLOT(timeoutSlot()));
}


ZapWindow::~ZapWindow()
{

}

// Make the two bitmaps, add the toggle button to the tool bar, and add
// it to the signal mapper
void ZapWindow::setupToggleButton(QToolBar *toolBar, QSignalMapper *mapper, 
                           int ind)
{
  if (firstTime) {
    bitmaps[ind][0] = new QBitmap(BM_WIDTH, BM_HEIGHT, bitList[ind][0], true);
    bitmaps[ind][1] = new QBitmap(BM_WIDTH, BM_HEIGHT, bitList[ind][1], true);
  }
  mToggleButs[ind] = new QToolButton(toolBar, "toolbar toggle");
  mToggleButs[ind]->setPixmap(*bitmaps[ind][0]);
  mToggleButs[ind]->setAutoRaise(AUTO_RAISE);
  mapper->setMapping(mToggleButs[ind],ind);
  connect(mToggleButs[ind], SIGNAL(clicked()), mapper, SLOT(map()));
  mToggleStates[ind] = 0;
}

void ZapWindow::zoomUp()
{
  zapStepZoom(mZap, 1);
}

void ZapWindow::zoomDown()
{
  zapStepZoom(mZap, -1);
}

// A new zoom or section was entered - let zap decide on limits and refresh box
void ZapWindow::newZoom()
{
  QString str = mZoomEdit->text();
  zapEnteredZoom(mZap, atof(str.latin1()));
}

void ZapWindow::newSection()
{
  QString str = mSectionEdit->text();
  zapEnteredSection(mZap, atoi(str.latin1()));
}

void ZapWindow::sliderChanged(int value)
{
  zapEnteredSection(mZap, value);
}

void ZapWindow::help()
{
  zapHelp();
}

void ZapWindow::info()
{
  zapPrintInfo(mZap);
}

void ZapWindow::timeBack()
{
  zapStepTime(mZap, -1);
}

void ZapWindow::timeForward()
{
  zapStepTime(mZap, 1);
}

// One of toggle buttons needs to change state
void ZapWindow::toggleClicked(int index)
{
  int state = 1 - mToggleStates[index];
  mToggleStates[index] = state; 
  mToggleButs[index]->setPixmap(*bitmaps[index][state]);
  zapStateToggled(mZap, index, state);
}

void ZapWindow::timeoutSlot()
{
  mTimer->stop();
  mGLw->updateGL();
}

// This allows zap to set one of the buttons
void ZapWindow::setToggleState(int index, int state)
{
  mToggleStates[index] = state ? 1 : 0;
  mToggleButs[index]->setPixmap(*bitmaps[index][state]);
}

void ZapWindow::setZoomText(float zoom)
{
  QString str;
  str.sprintf("%.2f", zoom);
  mZoomEdit->setText(str);
}

void ZapWindow::setSectionText(int section)
{
  QString str;
  str.sprintf("%d", section);
  mSectionEdit->setText(str);
  diaSetSlider(mSecSlider, section);
}

void ZapWindow::setTimeLabel(QString label)
{
  mTimeLabel->setText(label);
}

void ZapWindow::keyPressEvent ( QKeyEvent * e )
{
  zapKeyInput(mZap, e);
}
void ZapWindow::keyReleaseEvent (QKeyEvent * e )
{
  zapKeyRelease(mZap, e);
}

// Whan a close event comes in, inform zap, and accept
void ZapWindow::closeEvent (QCloseEvent * e )
{
  zapClosing(mZap);
  e->accept();
}

ZapGL::ZapGL(struct zapwin *zap, QGLFormat inFormat, QWidget * parent,
             const char * name)
  : QGLWidget(inFormat, parent, name)
{
  mMousePressed = false;
  mZap = zap;
}

ZapGL::~ZapGL()
{

}
 
void ZapGL::initializeGL()
{

}

void ZapGL::paintGL()
{
  zapPaint(mZap);
}

void ZapGL::resizeGL( int wdth, int hght )
{
  zapResize(mZap, wdth, hght);
}

void ZapGL::mousePressEvent(QMouseEvent * e )
{
  mMousePressed = true;
  zapMousePress(mZap, e);
}

void ZapGL::mouseReleaseEvent ( QMouseEvent * e )
{
  mMousePressed = false;
  zapMouseRelease(mZap, e);
}

void ZapGL::mouseMoveEvent ( QMouseEvent * e )
{
  if (mMousePressed)
    zapMouseMove(mZap, e);
}
