/*   imodv_ogl.h  -  declarations for imodv_ogl.cpp
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

#ifndef IMODV_OGL_H
#define IMODV_OGL_H

#ifndef IMODV_H
typedef struct __imodv_struct ImodvApp;
#endif

void imodvDraw_models(ImodvApp *a);
void imodvDraw_model(ImodvApp *a, Imod *imod);

#endif
