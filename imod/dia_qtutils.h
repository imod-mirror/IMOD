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
Revision 1.1.2.1  2002/12/30 06:32:59  mast
Initial creation

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
void diaSetTitle(char *title);

#ifdef __cplusplus
extern "C" {
#endif
  int dia_err(char *message);
  int dia_puts(char *message);
  void dia_vasmsg(char *msg, ...);
  void dia_smsg(char **msg);
#ifdef __cplusplus
}
#endif
#endif
