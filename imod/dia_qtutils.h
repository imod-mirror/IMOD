/*   dia_qutils.h  -  declarations for dia_qutils.cpp
 *
 *   Copyright (C) 1995-2002 by Boulder Laboratory for 3-Dimensional Electron
 *   Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *   Colorado.  See implementation file for full copyright notice.
 */                                                                           

/*  $Author$

$Date$

$Revision$

$Log$
*/

#ifndef DIA_QTUTILS_H
#define DIA_QTUTILS_H
class QCheckBox;
class QLabel;
class QPushButton;
class QVBoxLayout;
class QWidget;
class QSlider;

void diaSetSlider(QSlider *slider, int value);
void diaSetChecked(QCheckBox *button, bool state);
QLabel *diaLabel(char *text, QWidget *parent, QVBoxLayout *layout);
QPushButton *diaPushButton(char *text, QWidget *parent, 
			   QVBoxLayout *layout);
QCheckBox *diaCheckBox(char *text, QWidget *parent, QVBoxLayout *layout);
#endif
