/*   imodv_stereo.h  -  declarations for imodv_stereo.cpp
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

#ifndef IMODV_STEREO_H
#define IMODV_STEREO_H

#include <GL/gl.h>

#ifndef IMODV_H
typedef struct __imodv_struct ImodvApp;
#endif

#define IMODV_STEREO_OFF 0
#define IMODV_STEREO_RL  1
#define IMODV_STEREO_TB  2
#define IMODV_STEREO_HW  3

/* Stereo Control functions. */
void imodvStereoEditDialog(ImodvApp *a, int state);
void imodvStereoToggle(void);
void stereoHWOff(void);
void stereoClear(GLbitfield mask);
void stereoDrawBuffer(GLenum mode);

#endif
