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
Revision 1.1.2.2  2002/12/23 04:51:01  mast
Qt version

Revision 1.1.2.1  2002/12/18 04:10:30  mast
initial creation

*/

#ifndef IMODV_VIEWS_H
#define IMODV_VIEWS_H

#define VIEW_LABEL_LENGTH  32

#ifndef IMODV_H
typedef struct __imodv_struct ImodvApp;
#endif

/* view editing functions */
void imodvUpdateModel(ImodvApp *a);
void imodvViewEditDialog(ImodvApp *a, int state);
void imodvAutoStoreView(ImodvApp *a);
void imodvViewsHelp();
void imodvViewsDefault(bool draw);
void imodvViewsDone();
void imodvViewsClosing();
void imodvViewsSave();;
void imodvViewsGoto(int item, bool draw);
void imodvViewsStore(int item);
void imodvViewsNew(const char *label);;
void imodvViewsDelete(int item, int newCurrent);
void imodvViewsLabel(const char *label, int item);;
void imodvViewsAutostore(int state);

#endif

