/*   imod_model_edit.h  -  declarations for imod_model_edit.cpp
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
#ifndef MODELOFFSETWINDOW_H
#define MODELOFFSETWINDOW_H

#include "dialog_frame.h"
class QLabel;
class QLineEdit;

class ModelOffsetWindow : public DialogFrame
{
  Q_OBJECT

 public:
  ModelOffsetWindow(QWidget *parent, const char *name = NULL);
  ~ModelOffsetWindow() {};

  public slots:
  void buttonPressed(int which);
  void valueEntered();

 protected:
  void closeEvent ( QCloseEvent * e );
  void keyPressEvent ( QKeyEvent * e );
  void keyReleaseEvent ( QKeyEvent * e );

 private:
  QLabel *mBaseLabel[3];
  QLineEdit *mEditBox[3];
  QLabel *mAppliedLabel;
  void updateLabels();
};
#endif
