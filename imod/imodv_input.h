/*   imodv_input.h  -  declarations for imodv_input.cpp
 *
 *   Copyright (C) 1995-2002 by Boulder Laboratory for 3-Dimensional Electron
 *   Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *   Colorado.  See implementation file for full copyright notice.
 */                                                                           

/*  $Author$

$Date$

$Revision$

$Log$
Revision 1.1.2.2  2002/12/17 21:38:18  mast
include time.h so clock_t is defined

Revision 1.1.2.1  2002/12/17 17:41:01  mast
initial creation

*/

#ifndef IMODV_INPUT_H
#define IMODV_INPUT_H

#include <qevent.h>
#include <time.h>

#ifndef IMODV_H
typedef struct __imodv_struct ImodvApp;
#endif

#ifdef __cplusplus
extern "C" {
#endif

#ifndef USE_IMODV_WORKPROC
  void imodvMovieTimeout();
#endif
  void imodvCloseDialogs();
  void imodvRemoveDialog(QWidget * widget);
  void imodvAddDialog(QWidget *widget);
  void imodvHideDialogs();
  void imodvShowDialogs();
  void imodvKeyPress(QKeyEvent *event);
  void imodvKeyRelease(QKeyEvent *event);
  void imodvMousePress(QMouseEvent *event);
  void imodvMouseRelease(QMouseEvent *event);
  void imodvMouseMove(QMouseEvent *event);
  void imodv_rotate_model(ImodvApp *a, int x, int y, int z);
  void imodv_zoomd(ImodvApp *a, double zoom);
  void imodvQuit();
  void imodv_exit(ImodvApp *a);
  clock_t imodv_sys_time(void);
  
#ifdef __cplusplus
}
#endif

#endif
