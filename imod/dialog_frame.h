/*   dialog_frame.h  -  declarations for dialog_frame.cpp
 *
 *   Copyright (C) 1995-2002 by Boulder Laboratory for 3-Dimensional Electron
 *   Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *   Colorado.  See implementation file for full copyright notice.
 */                                                                           

/*  $Author$

$Date$

$Revision$

$Log$
Revision 1.1.2.2  2003/01/01 05:45:15  mast
rationalizing toplevel versus dialog style

Revision 1.1.2.1  2002/12/29 04:15:04  mast
Initial creation

*/

#ifndef DIALOG_FRAME_H
#define DIALOG_FRAME_H
#include <qwidget.h>

class QVBoxLayout;

class DialogFrame : public QWidget
{
  Q_OBJECT

 public:
  DialogFrame(QWidget *parent, int numButtons, char *labels[], char *tips[],
	      bool equalSized, char *caption, char *fallback,
	      const char *name = 0, 
	      WFlags fl = Qt::WDestructiveClose | Qt::WType_TopLevel);
  ~DialogFrame() {};

 signals:
  void actionPressed(int which);
  void actionReleased(int which);

  public slots:
    void buttonPressed(int which);
    void buttonReleased(int which);

 protected:
  QVBoxLayout *mLayout;
};
#endif
