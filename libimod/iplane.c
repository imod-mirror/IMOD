/*
 *  iplane.c -- graphic plane library elements.
 *
 *  Original author: James Kremer
 *  Revised by: David Mastronarde   email: mast@colorado.edu
 *
 *  Copyright (C) 1995-2005 by Boulder Laboratory for 3-Dimensional Electron
 *  Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *  Colorado.  See dist/COPYRIGHT for full copyright notice.
 */

/*  $Author$

$Date$

$Revision$

$Log$
*/

#include "imodel.h"

Iplane *imodPlanesNew(int size)
{
     Iplane *pl;
     int p;

     if (size <= 0)
	  return(NULL);
     
     pl = (Iplane *)malloc(sizeof(Iplane) * size);
     if (pl){
	  for(p = 0; p < size; p++)
	       imodPlaneInit(&(pl[p]));
     }
     return(pl);
}

void imodPlaneDelete(Iplane *plane)
{
     if (plane)
	  free(plane);
}

void imodPlaneInit(Iplane *plane)
{
     /* default plane z <= 0 */
     plane->a = plane->b = plane->d = 0.0f;
     plane->c = -1.0f;
}

void imodPlaneAxisRotate(Iplane *plane, double angle, int axis)
{
     Ipoint rpt;
     Imat *mat = imodMatNew(3);
     
     imodMatRot(mat, angle, axis);
     imodMatTransform(mat, (Ipoint *)plane, &rpt);
     imodMatDelete(mat);
     plane->a = rpt.x;
     plane->b = rpt.y;
     plane->c = rpt.z;
     return;
}

/* rotate plane by data in point x = alpha, y = beta, z = gamma. */
void imodPlaneRotate(Iplane *plane, double angle, Ipoint *pnt)
{
     Ipoint rpt;
     Imat *mat = imodMatNew(3);

     imodMatRotateVector(mat, angle, pnt);
     imodMatTransform(mat, (Ipoint *)plane, &rpt);
     imodMatDelete(mat);
     plane->a = rpt.x;
     plane->b = rpt.y;
     plane->c = rpt.z;
     return;
}

/* Translate plane by given point (x, y, z) */
void imodPlaneTranslate(Iplane *plane, Ipoint *pnt)
{
     plane->a += pnt->x;
     plane->b += pnt->y;
     plane->c += pnt->z;
     return;
}

/* set plane parameters from a point on a plane and a normal */
void imodPlaneSetPN(Iplane *plane, Ipoint *pnt, Ipoint *nor)
{
     plane->a = nor->x;
     plane->b = nor->y;
     plane->c = nor->z;
     plane->d = (pnt->x * nor->x) + (pnt->y * nor->y) + (pnt->z * nor->z);
     return;
}

/* Returns True if point is in the half space defined by clipping plane. */
/* Returns False if point needs to be clipped.                           */
int imodPlaneClip(Iplane *plane, Ipoint *pnt)
{
     if ( ((plane->a * pnt->x) + (plane->b * pnt->y) + 
	 (plane->c * pnt->z) + plane->d) >= 0)
	  return(1);
     else
	  return(0);
}

/* Check a series of planes */
int imodPlanesClip(Iplane *plane, int nplanes, Ipoint *pnt)
{
     int pn;
     for(pn = 0; pn < nplanes; pn++){
	  if (!imodPlaneClip(&(plane[pn]), pnt))
	       return(0);
     }
     return(1);
}


/* Initialize all clip planes and other parameters of the clip plane set */
void   imodClipsInitialize(IclipPlanes *clips)
{
  int i;
  clips->count       = 0;
  clips->flags = 0;
  clips->trans = 0;
  clips->plane = 0;
  for (i = 0; i < IMOD_CLIPSIZE; i++) {
    clips->normal[i].x = clips->normal[i].y = 0.0f;
    clips->normal[i].z = -1.0f;
    clips->point[i].x = clips->point[i].y = clips->point[i].z = 0.0f;
  }
}

/* Read all the clip planes of a set based on the size of the chunk */
int imodClipsRead(IclipPlanes *clips, FILE *fin)
{
  int size, nread;
  size = imodGetInt(fin);
  nread = (size - SIZE_CLIP) / 24 + 1;

  imodGetBytes(fin, (unsigned char *)&clips->count, 4);
  imodGetFloats(fin, (float *)&clips->normal[0], 3 * nread);
  imodGetFloats(fin, (float *)&clips->point[0], 3 * nread);
  return(ferror(fin));
}

/* Fix the count of clip planes for old files or for a count set to zero
   when written in a new file.
   If clip is 0 and the first plane is not in initialized state,
   turn off first flag and set clip 1; otherwise, if old model
   and clip is nonzero, set the first flag and make sure clip is 1 */
void imodClipsFixCount(IclipPlanes *clips, b3dUInt32 flags)
{
  if (!clips->count && 
      (clips->point[0].x || clips->point[0].y || 
       clips->point[0].z || clips->normal[0].x || 
       clips->normal[0].y || clips->normal[0].z != -1.f)) {
    clips->flags &= 254;
    clips->count = 1;
  } else if (clips->count && !(flags & IMODF_MULTIPLE_CLIP)) {
    clips->flags |= 1;
    clips->count = 1;
  }
}

/* Set plane parameters from clipping plane sets
   Either objClips or glbClips can be null; i.e. one set of planes can be used
   Set nPlanes to zero before first call */
void imodPlaneSetFromClips(IclipPlanes *objClips, IclipPlanes *glbClips,
                           Iplane *plane, int maxPlanes, int *nPlanes)
{
  int i, doGlobal = 1;
  if (objClips) {
    if (objClips->flags & (1 << 7))
      doGlobal = 0;
    for (i = 0; i < objClips->count; i++)
      if ((objClips->flags & (1 << i)) && *nPlanes < maxPlanes) {
        imodPlaneSetPN(&plane[*nPlanes], &objClips->point[i], 
                       &objClips->normal[i]);
        (*nPlanes)++;
      }
  }

  if (doGlobal && glbClips) {
    for (i = 0; i < glbClips->count; i++)
      if ((glbClips->flags & (1 << i)) && *nPlanes < maxPlanes) {
        imodPlaneSetPN(&plane[*nPlanes], &glbClips->point[i], 
                       &glbClips->normal[i]);
        (*nPlanes)++;
      }
  }
}

/* Transform clipping planes; mat has point transform and mat2 has normal */
void imodClipsTrans(IclipPlanes *clips, Imat *mat, Imat *mat2)
{
  int i;
  Ipoint pnt, pnt2;
  for (i = 0; i < clips->count; i++) {

    /* The clipping point is maintained as the negative of an actual location
     so it needs to be inverted, transformed, then reinverted */
    pnt2.x = -clips->point[i].x;
    pnt2.y = -clips->point[i].y;
    pnt2.z = -clips->point[i].z;
    imodMatTransform(mat, &pnt2, &pnt);
    clips->point[i].x = -pnt.x;
    clips->point[i].y = -pnt.y;
    clips->point[i].z = -pnt.z;

    /* Transform and renormalize the normal */
    imodMatTransform(mat2, &clips->normal[i], &pnt);
    imodPointNormalize(&pnt);
    clips->normal[i] = pnt;
  }
}
