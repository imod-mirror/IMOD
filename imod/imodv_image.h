/*   imodv_image.h  -  declarations for imodv_image.cpp
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

#ifndef IMODV_IMAGE_H
#define IMODV_IMAGE_H

#ifndef IMODV_H
typedef struct __imodv_struct ImodvApp;
#endif

  /* Image Control functions. */
  void imodvDrawImage(ImodvApp *a);
  void imodvImageEditDialog(ImodvApp *a, int state);

#endif
