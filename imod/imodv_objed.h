/*   imodv_objed.h  -  declarations for imodv_objed.cpp
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

#ifndef IMODV_OBJED_H
#define IMODV_OBJED_H

#ifndef IMODV_H
typedef struct __imodv_struct ImodvApp;
#endif

/******************************************************************
 * Public Interface function prototypes.
 ******************************************************************/

/* Close up and clean the object edit dialog. */
int object_edit_kill(void);

/* Create and init the object edit dialog. */
void objed(ImodvApp *a);
void imodvObjedNewView(void);
void imodvObjectListDialog(ImodvApp *a, int state);

#endif
