/*   imodv_views.h  -  declarations for imodv_views.cpp
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

#ifndef IMODV_VIEWS_H
#define IMODV_VIEWS_H

#ifndef IMODV_H
typedef struct __imodv_struct ImodvApp;
#endif

/* view editing functions */
void imodvUpdateModel(ImodvApp *a);
void imodvViewEditDialog(ImodvApp *a, int state);
void imodvAutoStoreView(ImodvApp *a);

#endif

