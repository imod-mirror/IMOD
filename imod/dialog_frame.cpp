/*  IMOD VERSION 2.7.9
 *
 *  dialog_frame.cpp       Implementation of a base class for non-modal dialogs
 *
 *  Author: David Mastronarde   email: mast@colorado.edu
 */

/*****************************************************************************
 *   Copyright (C) 1995-2002 by Boulder Laboratory for 3-Dimensional         *
 *   Electron Microscopy of Cells ("BL3DEMC") and the Regents of the         *
 *   University of Colorado.                                                 *
 *                                                                           *
 *   BL3DEMC reserves the exclusive rights of preparing derivative works,    *
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
 *   for the Boulder Laboratory for 3-Dimensional EM of Cells.               *
 *   University of Colorado, MCDB Box 347, Boulder, CO 80309                 *
 *****************************************************************************/
/*  $Author$

$Date$

$Revision$

$Log$
*/

/* DialogFrame provides a widget whose default style is to be a dialog box that
   destroys itself on closing.  Its main area is a QVBoxLayout, mLayout,
   that can be populated with widgets by the inheriting class.  The bottom
   row will have numButton buttons, with text given in "labels".  The
   buttons will be equally sized if equalSized is true; otherwise they will
   all be just big enough for their respective text.  The window title will
   be set to "caption", or to "fallback" if "caption" is null.
 */

#include <qlayout.h>
#include <qframe.h>
#include <qpushbutton.h>
#include <qsignalmapper.h>
#include "dialog_frame.h"

DialogFrame::DialogFrame(QWidget *parent, int numButtons, char *labels[], 
			 bool equalSized, char *caption, char *fallback,
			 const char *name, WFlags fl)
  : QWidget(parent, name, fl)
{
  int width = 0;
  int i, twidth;
  QString str;
  QPushButton *button;
  
  // Get outer layout then the layout that derived class will build into
  QVBoxLayout *outerLayout = new QVBoxLayout(this, 8, 6, "outer layout");
  mLayout = new QVBoxLayout(outerLayout);

  // Make the line
  QFrame *line = new QFrame(this);
  line->setFrameShape( QFrame::HLine );
  line->setFrameShadow( QFrame::Sunken );
  outerLayout->addWidget(line);

  // If equalsized buttons, find maximum width
  for (i = 0; i < numButtons; i++) {
    str = labels[i];
    twidth = (int)(1.25 * fontMetrics().width(str));
    if (width < twidth)
      width = twidth;
  }

  // set up signal mapper for the buttons
  QSignalMapper *pressMapper = new QSignalMapper(this);
  connect(pressMapper, SIGNAL(mapped(int)), this, SLOT(buttonPressed(int)));
  QSignalMapper *releaseMapper = new QSignalMapper(this);
  connect(releaseMapper, SIGNAL(mapped(int)), this, SLOT(buttonReleased(int)));
  
  // Make a layout and put the buttons in it
  QHBoxLayout *layout2 = new QHBoxLayout(0, 0, 6, "bottom layout");
  outerLayout->addLayout(layout2);

  for (i = 0; i < 3; i++) {
    str = labels[i];
    button = new QPushButton(str, this, labels[i]);
    if (!equalSized)
      width = (int)(1.25 * fontMetrics().width(str));
    button->setFixedWidth(width);
    button->setFocusPolicy(NoFocus);
    layout2->addWidget(button);
    pressMapper->setMapping(button, i);
    releaseMapper->setMapping(button, i);
    connect(button, SIGNAL(pressed()), pressMapper, SLOT(map()));
    connect(button, SIGNAL(released()), releaseMapper, SLOT(map()));
  }

  setFocusPolicy(StrongFocus);

  str = caption;
  if (str.isEmpty())
      str = fallback;
  setCaption(str);
}

void DialogFrame::buttonPressed(int which)
{
  emit actionPressed(which);
}

void DialogFrame::buttonReleased(int which)
{
  emit actionReleased(which);
}
