/*  IMOD VERSION 2.7.9
 *
 *  dia_qutils.cpp       Utility calls for using Qt classes
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
*/

#include <qcheckbox.h>
#include <qlabel.h>
#include <qslider.h>
#include <qpushbutton.h>
#include <qlayout.h>

// Make a new push button, add it to the vertical box layout, set for no focus
QPushButton *diaPushButton(char *text, QWidget *parent, 
                                  QVBoxLayout *layout)
{
  QPushButton *button = new QPushButton(text, parent);
  button->setFocusPolicy(QWidget::NoFocus);
  layout->addWidget(button);
  return button;
}

// Make a new check box, add it to the vertical box layout, set for no focus
QCheckBox *diaCheckBox(char *text, QWidget *parent, QVBoxLayout *layout)
{
  QCheckBox *button = new QCheckBox(text, parent);
  button->setFocusPolicy(QWidget::NoFocus);
  layout->addWidget(button);
  return button;
}

// Make a new label and add it to the vertical box layout
QLabel *diaLabel(char *text, QWidget *parent, QVBoxLayout *layout)
{
  QLabel *label = new QLabel(text, parent);
  layout->addWidget(label);
  return label;
}

// Set a checkbox and block the signals
void diaSetChecked(QCheckBox *button, bool state)
{
  button->blockSignals(true);
  button->setChecked(state);
  button->blockSignals(false);
}

// Set a slider and block signals
void diaSetSlider(QSlider *slider, int value)
{
  slider->blockSignals(true);
  slider->setValue(value);
  slider->blockSignals(false);
}
