/*   imodv_depthcue.h  -  declarations for imodv_depthcue.cpp
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

#ifndef IMODV_DEPTHCUE_H
#define IMODV_DEPTHCUE_H

#ifndef IMODV_H
typedef struct __imodv_struct ImodvApp;
#endif

/* depth cue functions */
void imodvDepthCueSet(void);
void imodvDepthCueSetWidgets(void);
void imodvDepthCueEditDialog(ImodvApp *a, int state);

#endif
