/*   imod_cont_copy.h  -  declarations for imod_cont_copy.cpp
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

#ifndef IMOD_CONT_COPY_H
#define IMOD_CONT_COPY_H

#include "dialog_frame.h"
class QButtonGroup;
class QSpinBox;
class QComboBox;
class QRadioButton;

#ifndef IMODP_H
typedef struct ViewInfo ImodView;
#endif

class ContourCopy : public DialogFrame
{
  Q_OBJECT

 public:
  ContourCopy(QWidget *parent, const char *name = NULL);
  ~ContourCopy() {};

  public slots:
  void buttonPressed(int which);
  void placeSelected(int which);
  void toValueChanged(int value);
  void rangeSelected(int which);

 protected:
  void closeEvent ( QCloseEvent * e );
  void keyPressEvent ( QKeyEvent * e );
  void keyReleaseEvent ( QKeyEvent * e );

 private:
  void update();
  void apply();
  QComboBox *mToCombo;
  QButtonGroup *mRadioGroup;
  QSpinBox *mToSpinBox;
  QRadioButton *mTimeRadio;
};

int openContourCopyDialog(ImodView *vw);

#endif
