/*   imodv_menu.h  -  declarations for imodv_menu.cpp
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

#ifndef IMODV_MENU_H
#define IMODV_MENU_H

  /* menu.c functions */
void imodvMenuLight(int value);
void imodvMenuWireframe(int value);
void imodvMenuLowres(int value);
void imodvFileSave(void);
void imodvEditMenu(int item);
void imodvHelpMenu(int item);
void imodvFileMenu(int item);
void imodvViewMenu(int item);
void imodvMenuBgcolor();
int imodvLoadModel();
void imodvSaveModelAs();

#endif

