/*
  AMAT_TO_ROTMAGSTR converts a 2 by 2 transformation matrix AMAT into
  four "natural" parameters of image transformation: THETA is overall
  rotation, SMAG is overall magnification, STR is a unidirectional
  stretch, and PHI is the angle of the stretch axis.
  Two equivalent solutions are possible, with the stretch axis in the
  first or fourth quadrant.  The program returns the solution that
  makes the magnification SMAG nearer to 1.0.
  
  To solve for the variables, the first step is to solve for THETA by
  taking the arctangent of a function of the AMAT values.  It is then
  possible to compute F1, F2 and F3, intermediate factors whose
  equations are listed in ROTMAGSTR_TO_AMAT below. The equations for
  F1, F2 and F3 can then be solved explicitly for the square of the
  cosine of PHI.  There are two solutions, which are both valid, but
  in different quadrants.  The sign of F2, and whether one of the
  formulas for STR would yield a value > or < 1, then determines which
  solution is valid for the first quadrant.  STR is computed from
  one of two different formulas, depending on whether PHI is near 45
  degrees or not, then SMAG is computed.
  
  David Mastronarde 12/29/88, vastly improved 2/5/92
  ported to C, 11/29/00, for new midas program
*/
/*  $Author$

    $Date$

    $Revision$

    $Log$
*/

#include <math.h>
void amat_to_rotmagstr(float *amat, float *theta, float *smag, float *str,
		       float *phi)
{	
  float a11 = amat[0];
  float a12 = amat[3];
  float a21 = amat[1];
  float a22 = amat[4];
  double ator = 0.0174532925;
  float costh, sinth, f1, f2, f3, fnum, fden, factmp, dentmp, dtheta;
  double afac, bfac, cosphisq, sinphisq;

       
  /* first determine if there is an axis inversion: find angle from
     transformed X axis to transformed Y axis and reduce to -180 to 180
     If difference is negative then invert Y components of matrix */
       
  dtheta = (float)((atan2((double)a22, (double)a12) - 
                    atan2((double)a21, (double)a11)) / ator);
  if (dtheta > 180.)
    dtheta = dtheta - 360.;
  if (dtheta <= -180)
    dtheta = dtheta + 360.;
  if (dtheta < 0.) {
    a12 = -a12;
    a22 = -a22;
  }

  /*  next find the rotation angle theta that gives the same solution for
      f2 when derived from a11 and a21 as when derived from a12 and a22 */

  *theta = 0.;
  if (a21 != a12 || a22 != -a11)
    *theta = (float)(atan2((double)(a21-a12), (double)(a22+a11)) / ator);
  costh = (float)cos(ator * *theta);
  sinth = (float)sin(ator * *theta);
  f1 = a11*costh+a21*sinth;
  f2 = a21*costh-a11*sinth;
  f3 = a22*costh-a12*sinth;
     
  /* Next solve for phi */
     
  if (f2 < 1.e-10 && f2 > -1.e-10) 

    /*    if f2 = 0, pick phi = 0., set cos phi to 1. */
    cosphisq = 1.;
  else {
	  
    /* otherwise, solve quadratic equation, pick the solution that is
       right for the first quadrant */
    afac = (f3-f1)*(f3-f1);
    bfac = 4.*f2*f2;
    cosphisq = 0.5*(1.+sqrt(1.-bfac/(bfac+afac)));
    sinphisq = 1.-cosphisq;
    fnum = f1*cosphisq-f3*sinphisq;
    if (fnum < 0.)
      fnum = -fnum;
    fden = f3*cosphisq-f1*sinphisq;
    if (fden < 0.)
      fden = -fden;
    if(f2 > 0. && fnum < fden || f2 < 0. && fnum > fden)
      cosphisq = 1.-cosphisq;
  }
  *phi = (float)(acos(sqrt(cosphisq)) / ator);
  sinphisq = 1.-cosphisq;
     
  /*  solve for str. */
     
  if (cosphisq-0.5 > 0.25 || cosphisq - 0.5 < - 0.25)

    /* for angles far from 45 deg, use an equation that is good at 0
       or 90 deg but blows up at 45 deg. */
    *str = (float)((f1*cosphisq-f3*sinphisq)/(f3*cosphisq-f1*sinphisq));

  else {

    /*  for angles near 45 deg, use an equation that is good there but
        blows up at 0. */
    factmp = (f1+f3)*sqrt(cosphisq*sinphisq);
    *str = (float)((factmp+f2)/(factmp-f2));
  }
     
  /* solve for smag from the equation for f1, or f2 if that would fail
     (which it does with stretch -1 along 45 degree line) */
     
  dentmp = *str * cosphisq + sinphisq;
  if(dentmp > 1.e-5 || dentmp < -1.e-5)
    *smag = (float)(f1/dentmp);
  else
    *smag = (float)(1./((*str-1.)*sqrt(cosphisq*sinphisq)));
     
  /* if it will make smag closer to 1.0, flip stretch axis 90 deg */
     
  f1 = *smag - 1;
  f2 = *str * *smag - 1.;
  if (f1 < 0.)
    f1 = -f1;
  if (f2 < 0.)
    f2 = -f2;
  if(f1 > f2) {
    *smag = * smag * *str;
    *str = 1. / *str;
    *phi = *phi-90.;
  }

       
  /* Now if there is an inversion, then invert the stretch, mirror the
     stretch axis, and add a rotation to bring inverted point along stretch
     axis to a point mirrored around X */
       
  if (dtheta < 0) {
    *str = -*str;
    *phi = -*phi;
    *theta = *theta + 180. - 2. * *phi;
    if (*theta > 180.) *theta = *theta - 180.;
  }
}