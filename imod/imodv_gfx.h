/*   imodv_gfx.h  -  declarations for imodv_gfx.cpp
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

#ifndef IMODV_GFX_H
#define IMODV_GFX_H

#ifndef IMODV_H
typedef struct __imodv_struct ImodvApp;
#endif

#ifndef IMODV_WINDOW_H
class ImodvGL;
#endif

int imodv_auto_snapshot(char *inName, int format_type);
void imodvResetSnap();
int imodv_winset(ImodvApp *a);
void imodvDraw(ImodvApp *a);
void imodvPaintGL();
void imodvResizeGL(ImodvGL *GLw, int winx, int winy);
void imodvInitializeGL();
void imodv_setbuffer(ImodvApp *a);

#endif
