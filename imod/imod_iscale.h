/*   imod_iscale.h  -  declarations for imod_iscale.cpp
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

#ifndef IMOD_ISCALE_H
#define IMOD_ISCALE_H

#include "dialog_frame.h"
class QLabel;
class QLineEdit;

#ifndef IMODP_H
typedef struct ViewInfo ImodView;
#endif

class ImageScaleWindow : public DialogFrame
{
  Q_OBJECT

 public:
  ImageScaleWindow(QWidget *parent, const char *name = NULL);
  ~ImageScaleWindow() {};
  void showFileAndMMM();
  void updateLimits();

  public slots:
  void buttonPressed(int which);

 protected:
  void closeEvent ( QCloseEvent * e );
  void keyPressEvent ( QKeyEvent * e );
  void keyReleaseEvent ( QKeyEvent * e );

 private:
  QLineEdit *mEditBox[2];
  QLabel *mFileLabel;
  QLabel *mMMMLabel;
  void applyLimits();
  void computeScale();
};

void imodImageScaleDialog(ImodView *iv);
void imodImageScaleUpdate(ImodView *iv);

#endif
