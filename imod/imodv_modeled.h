/*   imodv_modeled.h  -  declarations for imodv_modeled.cpp
 *
 *   Copyright (C) 1995-2002 by Boulder Laboratory for 3-Dimensional Electron
 *   Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *   Colorado.  See implementation file for full copyright notice.
 */                                                                           

/*  $Author$

$Date$

$Revision$

$Log$
Revision 1.1.2.1  2002/12/18 04:10:30  mast
initial creation

*/

#ifndef IMODV_MODELED_H
#define IMODV_MODELED_H

#ifndef IMODV_H
typedef struct __imodv_struct ImodvApp;
#endif
class QString;

void imeSetViewData(int wi);
void imodvModelEditDialog(ImodvApp *a, int state);
int imodvSelectModel(ImodvApp *a, int ncm);
void imodvModeledHelp();
void imodvModeledDone();
void imodvModeledClosing();
void imodvModeledNumber(int which);
void imodvModeledMove(int item);
void imodvModeledView(int item);
void imodvModeledEdit(int item);
void imodvModeledName(QString nameStr);
void imodvModeledScale(int update);

#endif
