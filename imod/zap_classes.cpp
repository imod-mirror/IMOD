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

#include "imodP.h"
#include "zap_classes.h"
#include "xzap.h"
#include "tooledit.h"
#include "arrowbutton.h"

#define SECTION_WIDTH 40
#define ZOOM_WIDTH 40
#define AUTO_RAISE false
#define MIN_SLIDER_WIDTH 20
#define MAX_SLIDER_WIDTH 100

#define BM_WIDTH 16
#define BM_HEIGHT 16

// Unfinished business: recovering bitmap files
#include "unlock.bits"
#include "lock.bits"
#include "time_unlock.bits"
#include "time_lock.bits"

static unsigned char lowRes_bits[] = {
  0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0x0f, 0x0f, 0x0f, 0x0f,
  0x0f, 0x0f, 0x0f, 0x0f, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0,
  0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f};

static unsigned char highRes_bits[] = {
  0xcc, 0xcc, 0xcc, 0xcc, 0x33, 0x33, 0x33, 0x33, 0xcc, 0xcc, 0xcc, 0xcc,
  0x33, 0x33, 0x33, 0x33, 0xcc, 0xcc, 0xcc, 0xcc, 0x33, 0x33, 0x33, 0x33,
  0xcc, 0xcc, 0xcc, 0xcc, 0x33, 0x33, 0x33, 0x33};

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
  { {lowRes_bits, highRes_bits},
    {unlock_bits, lock_bits},
    {smartCenter_bits, keepCenter_bits},
    {insert_after_bits, insert_before_bits},
    {time_unlock_bits, time_lock_bits}};

static QBitmap *bitmaps[5][2];
static int firstTime = 1;

ZapWindow::ZapWindow(struct zapwin *zap, QString timeLabel, bool rgba, 
                     bool doubleBuffer, QWidget * parent,
                     const char * name, WFlags f) 
  : GLMainWindow(rgba, doubleBuffer, parent, name, f)
{
  int j;
  mZap = zap;

  // Get the toolbar, add zoom arrows
  mToolBar = new QToolBar(this, "zap toolbar");
  if (!AUTO_RAISE) {
    QBoxLayout *boxLayout = mToolBar->boxLayout();
    boxLayout->setSpacing(4);
  }
  ArrowButton *arrow = new ArrowButton(Qt::UpArrow, mToolBar, "zoomup button");
  arrow->setAutoRaise(AUTO_RAISE);
  connect(arrow, SIGNAL(clicked()), this, SLOT(zoomUp()));
  arrow = new ArrowButton(Qt::DownArrow, mToolBar, "zoom down button");
  arrow->setAutoRaise(AUTO_RAISE);
  connect(arrow, SIGNAL(clicked()), this, SLOT(zoomDown()));

  mZoomEdit = new ToolEdit(mToolBar, "zoom edit box");
  mZoomEdit->setFixedWidth(ZOOM_WIDTH);
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
  int swidth = zap->vi->zsize < MAX_SLIDER_WIDTH ? 
    zap->vi->zsize : MAX_SLIDER_WIDTH;
  swidth = swidth > MIN_SLIDER_WIDTH ? swidth : MIN_SLIDER_WIDTH;
  mSecSlider->setFixedWidth(swidth + 15);    // 10 was needed
  connect(mSecSlider, SIGNAL(valueChanged(int)), this, 
	  SLOT(sliderChanged(int)));

  // Section edit box
  mSectionEdit = new ToolEdit(mToolBar, "section edit box");
  mSectionEdit->setFixedWidth(SECTION_WIDTH);
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
  button->setFixedWidth(35);
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
  createGLWidget(rgba, doubleBuffer);

  // dock on top and bottom only
  setDockEnabled(mToolBar, Left, FALSE );
  setDockEnabled(mToolBar, Right, FALSE );
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
  mSecSlider->setValue(section);
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

void ZapWindow::paintGL()
{
  zapPaint(mZap);
}

void ZapWindow::resizeGL( int wdth, int hght )
{
  zapResize(mZap, wdth, hght);
}

void ZapWindow::mousePressEvent(QMouseEvent * e )
{
  zapMousePress(mZap, e);
}

void ZapWindow::mouseReleaseEvent ( QMouseEvent * e )
{
  zapMouseRelease(mZap, e);
}

void ZapWindow::mouseMoveEvent ( QMouseEvent * e )
{
  zapMouseMove(mZap, e);
}



