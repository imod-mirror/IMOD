/*   imodv_movie.h  -  declarations for imodv_movie.cpp
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

#ifndef IMODV_MOVIE_H
#define IMODV_MOVIE_H

#define IMODV_MOVIE_FULLAXIS_X -1
#define IMODV_MOVIE_FULLAXIS_Y 1

typedef struct __imodv_struct ImodvApp;

void imodvMovieFullAxis(int ixy);
void imodvMovieSetStart();
void imodvMovieSetEnd();
void imodvMovieDialog(ImodvApp *a, int state);
void imodvMovieFullAxis();
void imodvMovieHelp();
void imodvMovieQuit();
void imodvMovieMake();
void imodvMovieStop();
void imodvMovieHelp();
void imodvMovieClosing();

#endif
