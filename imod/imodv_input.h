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
*/

#ifndef IMODV_INPUT_H
#define IMODV_INPUT_H

#include <qevent.h>

#ifndef IMODV_H
typedef struct __imodv_struct ImodvApp;
#endif

#ifdef __cplusplus
extern "C" {
#endif

#ifndef USE_IMODV_WORKPROC
  void imodvMovieTimeout();
#endif
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
