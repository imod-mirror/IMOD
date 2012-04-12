/* Model draw, used only by the slicer window */

/*  $Author$

$Date$

$Revision$
Log at end */

#include "imod.h"
#include "imod_display.h"
#include "b3dgfx.h"
#include "xzap.h"
#include "istore.h"
#include "finegrain.h"

static void imodDrawContourLines(Iobj *obj, int co, GLenum mode);
static void imodDrawObjectSymbols(ImodView *vi, Iobj *obj);
static void imodDrawSpheres(ImodView *vi, Iobj *obj);
static void imodDrawSymbol(Ipoint *point, int sym, int size, int flags, 
                           int linewidth);


void imodDrawModel(ImodView *vi, Imod *imod)
{
  Iobj  *obj;
  Icont *cont;
  Ipoint *pnt;
  int imPtSize, modPtSize, backupSize, curSize;
  int ob, co;
  bool hasSpheres;

  if (imod->drawmode <= 0)
    return;

  for(ob = 0; ob < imod->objsize; ob++){
    obj = &imod->obj[ob];
    if (iobjOff(imod->obj[ob].flags))
      continue;

    imodSetObjectColor(ob);
    glLineWidth((GLfloat)obj->linewidth2);

    hasSpheres = iobjScat(obj->flags) || obj->pdrawsize;
    for (co = 0; co < obj->contsize && !hasSpheres; co++)
      hasSpheres = obj->cont[co].sizes != NULL;

    if (hasSpheres)
      imodDrawSpheres(vi, obj);
    
    if (!iobjScat(obj->flags)) {
      
      /* Draw all of the objects lines first. */
      for(co = 0; co < obj->contsize; co++){
	cont = &obj->cont[co];
	if (!cont->psize)
	  continue;
	
        /* DNM 2/21/03: don't draw contours from other times */
	if (ivwTimeMismatch(vi, 0, obj, cont))
          continue;

	/* todo: ghost contour ? */
	/* check ghostmode and surface ghostmode. */
	
	if (iobjOpen(obj->flags)){
	  imodDrawContourLines(obj, co, GL_LINE_STRIP);
	  continue;
	}
	if (cont->flags & ICONT_OPEN)
	  imodDrawContourLines(obj, co, GL_LINE_STRIP);
	else
	  imodDrawContourLines(obj, co, GL_LINE_LOOP);
      }
    }
    imodDrawObjectSymbols(vi, obj);
  }
     
  /* draw ends of current contour. */
  /* draw current point symbol.    */
  obj = imodObjectGet(vi->imod);
  cont = imodContourGet(vi->imod);
  pnt = imodPointGet(vi->imod);
  zapCurrentPointSize(obj, &modPtSize, &backupSize, &imPtSize);

  /* draw if current contour exists and it is not at wrong time */
  if (cont && cont->psize && !ivwTimeMismatch(vi, 0, obj, cont)) { 
    
    /* draw ends if more than one point */
    if (cont->psize > 1) {
      b3dColorIndex(App->bgnpoint);
      imodDrawSymbol(cont->pts, IOBJ_SYM_CIRCLE, modPtSize, 0, 
                     obj->linewidth2);
      b3dColorIndex(App->endpoint);
      imodDrawSymbol(&(cont->pts[cont->psize - 1]), IOBJ_SYM_CIRCLE, 
                     modPtSize, 0, obj->linewidth2);
    }

    /* draw current point in model mode */
    if (pnt && vi->imod->mousemode == IMOD_MMODEL) {
      b3dColorIndex(App->curpoint);
      curSize = modPtSize;
      if (cont->psize > 1 && 
          (pnt == cont->pts || pnt == cont->pts + cont->psize - 1))
        curSize = backupSize;
      imodDrawSymbol(pnt, IOBJ_SYM_CIRCLE, curSize, 0, obj->linewidth2);
    }
  }   
  return;
}

static void imodDrawContourLines(Iobj *obj, int co, GLenum mode)
{
  Ipoint *point;
  Icont *cont = &obj->cont[co];
  DrawProps contProps, ptProps;
  int pt, lpt;
  int nextChange, stateFlags, changeFlags;
  int handleFlags = HANDLE_LINE_COLOR | HANDLE_2DWIDTH;
     
  point = cont->pts;
  lpt = cont->psize;
  nextChange = ifgHandleContChange(obj, co, &contProps, &ptProps, &stateFlags,
                                   handleFlags, 0);
  if (contProps.gap)
    return;

  glBegin(GL_LINE_STRIP);
  for (pt = 0; pt < lpt; pt++, point++) {
    glVertex3fv((float *)point);
    ptProps.gap = 0;
    if (nextChange == pt)
      nextChange = ifgHandleNextChange(obj, cont->store, &contProps, 
                                       &ptProps, &stateFlags, 
                                       &changeFlags, handleFlags, 0);
    if (ptProps.gap) {
      glEnd();
      glBegin(GL_LINE_STRIP);
    }
  }
  if (mode == GL_LINE_LOOP && !ptProps.gap)
    glVertex3fv((float *)cont->pts);

  glEnd();
  return;
}

static void imodDrawObjectSymbols(ImodView *vi, Iobj *obj)
{
  Icont  *cont;
  Ipoint *point;
  Ipoint vert;
  int co, pt, lpt;
  DrawProps contProps, ptProps;
  int nextChange, stateFlags, changeFlags;
  int handleFlags = HANDLE_LINE_COLOR | HANDLE_2DWIDTH;

  for (co = 0; co < obj->contsize; co++) {
    cont = &obj->cont[co];

    if (ivwTimeMismatch(vi, 0, obj, cont))
      continue;

    nextChange = ifgHandleContChange(obj, co, &contProps, &ptProps,
                                     &stateFlags, handleFlags, 0);
    if (contProps.gap)
      continue;

    lpt = cont->psize;
    point = cont->pts;
    for (pt = 0; pt < lpt; pt++, point++){
      if (nextChange == pt)
        nextChange = ifgHandleNextChange(obj, cont->store, &contProps, 
                                         &ptProps, &stateFlags, 
                                         &changeFlags, handleFlags, 0);
      if (ptProps.symtype != IOBJ_SYM_NONE)
        imodDrawSymbol(point, ptProps.symtype, ptProps.symsize, 
                       ptProps.symflags, ptProps.linewidth2);
    }

    /* draw end markers for all kinds of contours */
    if (obj->symflags & IOBJ_SYMF_ENDS && lpt) {
      point = cont->pts;
      b3dColorIndex(App->bgnpoint);
      b3dLineWidth(contProps.linewidth2);

      for (pt = 0; pt < 2; pt++) {
        vert = *point;
        glBegin(GL_LINES);
        vert.x -= obj->symsize/2.;
        vert.y -= obj->symsize/2.;
	glVertex3fv((float *)&vert);
        vert.x += obj->symsize;
        vert.y += obj->symsize;
	glVertex3fv((float *)&vert);
        glEnd();

        glBegin(GL_LINES);
        vert.x -= obj->symsize;
	glVertex3fv((float *)&vert);
        vert.x += obj->symsize;
        vert.y -= obj->symsize;
	glVertex3fv((float *)&vert);
        glEnd();

        point = &(cont->pts[lpt - 1]);
        b3dColorIndex(App->endpoint);
      }
      
    }
  }
  return;
}

static void imodDrawSpheres(ImodView *vi, Iobj *obj)
{
  Icont *cont;
  Ipoint *point;
  int pt, co;
  int stepRes;
  DrawProps contProps, ptProps;
  int nextChange, stateFlags, changeFlags;
  int handleFlags = HANDLE_LINE_COLOR;
  GLdouble drawsize;
  GLuint listIndex;
#ifdef GLU_QUADRIC_HACK
  GLUquadricObj *qobj = gluNewQuadric();
#else
  static GLUquadricObj *qobj = NULL;
  if (!qobj)
    qobj = gluNewQuadric();
#endif

  /* The default is GLU_FILL, and it makes it dotty at the equator */
  /* But GLU_LINE does not give a circle on every slice */
  gluQuadricDrawStyle(qobj, GLU_FILL);

  /* Make a display list for the default size */
  drawsize = obj->pdrawsize / vi->xybin;
  if (drawsize) {
    stepRes = (int)(drawsize < 5 ? drawsize + 4 : 8);
    listIndex = glGenLists(1);
    glNewList(listIndex, GL_COMPILE);
    gluSphere(qobj, drawsize , stepRes * 2, stepRes);
    glEndList();
  }

  for (co = 0; co < obj->contsize; co++) {
    cont = &obj->cont[co];
    if (!cont->psize)
      continue;
    if (ivwTimeMismatch(vi, 0, obj, cont))
      continue;

    nextChange = ifgHandleContChange(obj, co, &contProps, &ptProps, 
                                     &stateFlags, handleFlags, 0);
    if (contProps.gap)
      continue;
    for (pt = 0, point = cont->pts; pt < cont->psize; pt++, point++) {
      if (nextChange == pt)
        nextChange = ifgHandleNextChange(obj, cont->store, &contProps, 
                                         &ptProps, &stateFlags, 
                                         &changeFlags, handleFlags, 0);
      drawsize = imodPointGetSize(obj, cont, pt)  / vi->xybin;
      if (!drawsize)
	continue;
      glPushMatrix();
      glTranslatef(point->x, point->y, point->z);

      /* Use the display list if default; otherwise individual size */
      if (drawsize == obj->pdrawsize)
	glCallList(listIndex);
      else {
	stepRes = (int)(drawsize < 5 ? drawsize + 4 : 8);
	gluSphere(qobj, drawsize , stepRes * 2, stepRes);
      }
      glPopMatrix();
    }
  }
  if (obj->pdrawsize)
    glDeleteLists(listIndex, 1);

#ifdef GLU_QUADRIC_HACK
  gluDeleteQuadric(qobj);
#endif
}

void imodDrawSymbol(Ipoint *point, int sym, int size, int flags, int linewidth)
{
  double inner, outer;
  int slices, loops;
  Ipoint vert;
#ifdef GLU_QUADRIC_HACK
  GLUquadricObj *qobj;
  if (sym == IOBJ_SYM_CIRCLE)
    qobj = gluNewQuadric();
#else
  static GLUquadricObj *qobj = NULL;
  if (!qobj)
    qobj = gluNewQuadric();
#endif

  switch(sym){
          
  case IOBJ_SYM_CIRCLE:
    outer = size;
    if (flags  & IOBJ_SYMF_FILL)
      inner = 0.0;
    else
      inner = outer - linewidth;
    slices = (int)(outer + 4);
    loops = 1;
    glPushMatrix();
    glTranslatef(point->x, point->y, point->z);
    gluDisk(qobj, inner, outer, slices, loops);
    glPopMatrix();
    break;
          
  case IOBJ_SYM_SQUARE:
    glBegin((flags & IOBJ_SYMF_FILL) ? GL_POLYGON : GL_LINE_LOOP);
    vert = *point;
    vert.x -= (size/2);
    vert.y -= (size/2);
    glVertex3fv((float *)&vert);
    vert.x += size;
    glVertex3fv((float *)&vert);
    vert.y += size;
    glVertex3fv((float *)&vert);
    vert.x -= size;
    glVertex3fv((float *)&vert);
    glEnd();
    break;
          
  case IOBJ_SYM_TRIANGLE:
    glBegin((flags & IOBJ_SYMF_FILL) ? GL_POLYGON : GL_LINE_LOOP);
    vert = *point;
    vert.y += size;
    glVertex3fv((float *)&vert);
    vert.x += size;
    vert.y -= (size + (size/2));
    glVertex3fv((float *)&vert);
    vert.x -= 2 * size;
    glVertex3fv((float *)&vert);
    glEnd();
    break;
          
  case IOBJ_SYM_STAR:
    break;

  case IOBJ_SYM_NONE:
    glBegin(GL_POINTS);
    glVertex3fv((float *)point);
    glEnd();
    break;
          

  default:
    break;
  }
     
#ifdef GLU_QUADRIC_HACK
  if (sym == IOBJ_SYM_CIRCLE)
    gluDeleteQuadric(qobj);
#endif
  return;
}

/*
$Log$
Revision 4.10  2004/11/20 05:05:27  mast
Changes for undo/redo capability

Revision 4.9  2004/11/02 20:16:34  mast
Switched to using curpoint color for current point

Revision 4.8  2004/01/05 18:36:27  mast
Divide point size by binning

Revision 4.7  2003/10/27 22:25:07  mast
Fix bug of trying to draw end symbols for empty contours

Revision 4.6  2003/04/17 19:02:59  mast
adding hack for GL-context dependent gluQuadric

Revision 4.5  2003/03/28 05:02:30  mast
Needed to remove include of glu.h for Mac

Revision 4.4  2003/03/12 06:37:42  mast
Added end markers and current object markers, simplified time logic

Revision 4.3  2003/03/03 22:15:55  mast
Added ability to draw spheres for any points with size

Revision 4.2  2003/02/27 19:38:56  mast
Change include to qgl, and have it only draw at the current time

Revision 4.1  2003/02/10 20:29:00  mast
autox.cpp

Revision 1.1.2.2  2003/01/27 00:30:07  mast
Pure Qt version and general cleanup

Revision 1.1.2.1  2003/01/23 23:06:04  mast
conversion to cpp

Revision 3.1.2.1  2003/01/06 15:38:42  mast
Add sphere drawing

Revision 3.1  2002/12/01 15:34:41  mast
Changes to get clean compilation with g++

*/