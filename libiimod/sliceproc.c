/*
 *  sliceproc.c -- image slice support for 3dmod and clip
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

    Log at end of file
*/

#include <math.h>
#include "b3dutil.h"
#include "mrcc.h"
#include "sliceproc.h"

/* DNM 11/7/04: All routines expect the min and max of the input slice to be
   set to the desired range for scaling the output */

#define RANGE(s, i, j)  (((i)>0)&&((j)>0)&&((i)<(s)->xsize)&&((j)<(s)->ysize))
#define GETVAL(s, i, j) ((s)->data.b[(i) + ((j) * (s)->xsize)])
#define GETSVAL(t, i, j) ((t)->data.s[(i) + ((j) * (t)->xsize)])
#define GETFVAL(s, i, j) ((s)->data.f[(i) + ((j) * (s)->xsize)])

int  myRank = 0;

int sliceByteConvolve(Islice *sin, int mask[3][3]);
static void sliceScaleAndFree(Islice *sout, Islice *sin);
static float selectFloat(int s, float *r, int num);
static int selectInt(int s, int *r, int num);

static int SmoothKernel[3][3] = 
  { 
    {1, 2, 1},
    {2, 4, 2},
    {1, 2, 1}
  };

static int SharpenKernel[3][3] =
  {
    {-1,  -1, -1},
    {-1,  9, -1},
    {-1,  -1, -1}
  };

static int LaplacianKernel[3][3] =
  {
    {1,  1, 1},
    {1,  -4, 1},
    {1,  1, 1}
  };


int sliceByteAdd(Islice *sin, int inVal)
{
  int imax = sin->xsize * sin->ysize;
  int aval;
  int i;
     
  unsigned char *image = sin->data.b;

  for(i = 0; i < imax; i++){
    aval = image[i] + inVal;
    if (aval > 255) aval = 255;
    if (aval < 0) aval = 0;
    image[i] = aval;
  }
  return 0;
}

int sliceByteEdgeTwo(Islice *sin, int center)
{
  Islice *sout = sliceCreate(sin->xsize, sin->ysize,  SLICE_MODE_FLOAT);
  float val;
  int imax = sin->xsize - 1;
  int jmax = sin->ysize - 1;
  int i, j, x, y;
  float range;
  float Sr, Sc, k;

  for(i = 1; i < imax; i++) 
    for(j = 1; j < jmax; j++){
      Sr = (GETVAL(sin, i-1, j-1) + 
            (center * GETVAL(sin, i, j-1)) + GETVAL(sin, i+1, j-1)) -
        (GETVAL(sin, i-1, j+1) +
         (center * GETVAL(sin, i, j+1)) + GETVAL(sin, i+1, j+1));
               
      Sc = (GETVAL(sin, i+1, j+1) +  center * GETVAL(sin, i+1, j) +
            GETVAL(sin, i+1, j-1)) -
        (GETVAL(sin, i-1, j+1) +
         (center * GETVAL(sin, i-1, j)) +
         GETVAL(sin, i-1, j-1));
      Sr *= Sr;
      Sc *= Sc;
      val = sqrt(Sr + Sc);

      GETFVAL(sout, i, j) = val;
    }

  for(j = 1; j < jmax; j++){
    GETFVAL(sout, 0, j) =  GETFVAL(sout, 1, j);
    GETFVAL(sout, imax, j) =  GETFVAL(sout, imax-1, j);
  }

  for(i = 0; i <= imax; i++){
    GETFVAL(sout, i, 0) =  GETFVAL(sout, i, 1);
    GETFVAL(sout, i, jmax) =  GETFVAL(sout, i, jmax-1);
  }
  sliceScaleAndFree(sout, sin);
  return(0);
}

int sliceByteEdgeSobel(Islice *sin)
{
  return(sliceByteEdgeTwo(sin, 2));
}
int sliceByteEdgePrewitt(Islice *sin)
{
  return(sliceByteEdgeTwo(sin, 1));
}
int sliceByteEdgeLaplacian(Islice *sin)
{
  return(sliceByteConvolve(sin, LaplacianKernel));
}

int sliceByteSharpen(Islice *sin)
{
  return(sliceByteConvolve(sin, SharpenKernel));
}

/* DNM 11/07/04: Removed smoothing routine that did the same as convolving with
   this kernel */

int sliceByteSmooth(Islice *sin)
{
  return(sliceByteConvolve(sin, SmoothKernel));
}

int sliceByteConvolve(Islice *sin, int mask[3][3])
{
  Islice *sout = sliceCreate(sin->xsize, sin->ysize,  SLICE_MODE_SHORT);
  int imax = sin->xsize - 1;
  int jmax = sin->ysize - 1;
  int i, j, m, n;
  int val;

  /* 11/7/04: No longer need to determine mean of mask with new scaling */

  for(i = 1; i < imax; i++) 
    for(j = 1; j < jmax; j++){
      val = (GETVAL(sin, i+1, j+1) * mask[0][0] +
             GETVAL(sin,   i, j+1) * mask[0][1] +
             GETVAL(sin, i-1, j+1) * mask[0][2] +
             GETVAL(sin, i+1, j) * mask[1][0] +
             GETVAL(sin,   i, j) * mask[1][1] +
             GETVAL(sin, i-1, j) * mask[1][2] +
             GETVAL(sin, i+1, j-1) * mask[2][0] +
             GETVAL(sin,   i, j-1) * mask[2][1] +
             GETVAL(sin, i-1, j-1) * mask[2][2]);
      GETSVAL(sout, i, j) = val;
    }

  for(j = 1; j < jmax; j++){
    GETSVAL(sout, 0, j) =  GETSVAL(sout, 1, j);
    GETSVAL(sout, imax, j) =  GETSVAL(sout, imax-1, j);
  }
  for(i = 0; i <= imax; i++){
    GETSVAL(sout, i, 0) =  GETSVAL(sout, i, 1);
    GETSVAL(sout, i, jmax) =  GETSVAL(sout, i, jmax-1);
  }

  sliceScaleAndFree(sout, sin);
  return(0);
}




static int nay8(Islice *sin, int i, int j, int val)
{
  int n, m, k = 0;
  int x, y;

  if ( GETVAL(sin, i, j) != val)
    return(0);
     
  for (n = -1; n <= 1; n++){
    y = n + j;
    for(m = -1; m <= 1 ; m++){
      x = m + i;
      if ((x > 0) && (y > 0) && (x < sin->xsize) && (y < sin->ysize))
        if (GETVAL(sin, x, y) == val) k++; 
    }
  }
  return(k-1);
}

int sliceByteThreshold(Islice *sin, int val)
{
  int imax = sin->xsize * sin->ysize;
  int i;
  unsigned char *image = sin->data.b;
    
  int pmin = (int)sin->min;
  int pmax = (int)sin->max;
  int thresh = pmin + val;
    

  for(i = 0; i < imax; i++){
    if (image[i] < thresh)
      image[i] = pmax;
    else
      image[i] = pmin;
  }
  return 0;
}

int sliceByteGrow(Islice *sin, int val)
{
  int imax = sin->xsize;
  int jmax = sin->ysize;
  int i, j, m, n, x, y;

  for(j = 0; j < jmax; j++)
    for(i = 0; i < imax; i++){
      if ( GETVAL(sin, i, j) != val) continue;
      for(m = -1; m <= 1; m++){
        y = j + m;
        if ((y < 0) || (y >= sin->ysize)) continue;
        for(n = -1; n <= 1; n++){
          x = n + i;
          if ((x == i) && (y == j)) continue;
          if ((x < 0) || (x >= sin->xsize)) continue;
          if (GETVAL(sin, x, y) == sin->min)
            GETVAL(sin, x, y) = (unsigned char)(sin->max - 1);
        }
      }
    }


  for(j = 0; j < jmax; j++)
    for(i = 0; i < imax; i++){
      if ( GETVAL(sin, i, j) == (sin->max - 1)){
        GETVAL(sin, i, j) = val;
      }
    } 
  return(0);
}

int sliceByteShrink(Islice *sin, int val)
{
  Islice *sout = sliceCreate(sin->xsize, sin->ysize,  SLICE_MODE_BYTE);
  int imax = sin->xsize;
  int jmax = sin->ysize;
  int i, j, m, n;
  int tval;
  int white = 0;

  unsigned char pmin = (unsigned char)sin->min;
  unsigned char pmax = (unsigned char)sin->max;

  for(j = 0; j < jmax; j++)
    for(i = 0; i < imax; i++){
      GETVAL(sout, i, j) = 0;
    }


  for(j = 0; j < jmax; j++)
    for(i = 0; i < imax; i++){
      if (nay8(sin, i, j, pmax) < 7)
        GETVAL(sout, i, j)  = 1;
    } 

     
  for(j = 0; j < jmax; j++)
    for(i = 0; i < imax; i++){
      if (GETVAL(sout, i, j))
        GETVAL(sin, i, j)  = pmin;
    }
  return 0;
}

int sliceByteGraham(Islice *sin)
{
  Islice *sout = sliceCreate(sin->xsize, sin->ysize,  SLICE_MODE_FLOAT);
  float val;
  int imax = sin->xsize - 1;
  int jmax = sin->ysize - 1;
  int i, j, x, y;
  float range;
  float Ixx, Iyy, k;
  float ld = 1.0f/6.0f;
  float hd = 1.0f/3.0f;
  float delta = 5;

  for(i = 1; i < imax; i++) 
    for(j = 1; j < jmax; j++){
      Ixx  = GETVAL(sin, i+1, j+1) * ld;
      Ixx -= GETVAL(sin,   i, j+1) * hd;
      Ixx += GETVAL(sin, i-1, j+1) * ld;
      Ixx += GETVAL(sin, i+1,   j) * ld;
      Ixx -= GETVAL(sin,   i,   j) * hd;
      Ixx += GETVAL(sin, i-1,   j) * ld;
      Ixx += GETVAL(sin, i+1, j-1) * ld;
      Ixx -= GETVAL(sin,   i, j-1) * hd;
      Ixx += GETVAL(sin, i-1, j-1) * ld;

      Iyy  = GETVAL(sin, i+1, j+1) * ld;
      Iyy += GETVAL(sin,   i, j+1) * ld;
      Iyy += GETVAL(sin, i-1, j+1) * ld;
      Iyy -= GETVAL(sin, i+1,   j) * hd;
      Iyy -= GETVAL(sin,   i,   j) * hd;
      Iyy -= GETVAL(sin, i-1,   j) * hd;
      Iyy += GETVAL(sin, i+1, j-1) * ld;
      Iyy += GETVAL(sin,   i, j-1) * ld;
      Iyy += GETVAL(sin, i-1, j-1) * ld;


      if (Ixx < delta){
        if (Iyy < delta){
          val = (GETVAL(sin, i+1, j+1) +
                 GETVAL(sin,   i, j+1) + 
                 GETVAL(sin, i-1, j+1) +
                 GETVAL(sin, i+1, j) +
                 GETVAL(sin,   i, j) +
                 GETVAL(sin, i-1, j) +
                 GETVAL(sin, i+1, j-1) +
                 GETVAL(sin,   i, j-1) +
                 GETVAL(sin, i-1, j-1)) / 9.0f;
        }else{
          val = (GETVAL(sin, i+1, j) +
                 GETVAL(sin,   i, j) +
                 GETVAL(sin, i-1, j)) / 3.0f;
        }
      }else{
        if (Iyy < delta){
          val = (GETVAL(sin, i, j+1) +
                 GETVAL(sin, i, j) +
                 GETVAL(sin, 1, j-1)) / 3.0f;
        }else{
          val = GETVAL(sin,   i, j);
        }
      }
      GETFVAL(sout, i, j) = val;
    }

  for(j = 1; j < jmax; j++){
    GETFVAL(sout, 0, j) =  GETFVAL(sout, 1, j);
    GETFVAL(sout, imax, j) =  GETFVAL(sout, imax-1, j);
  }

  for(i = 0; i <= imax; i++){
    GETFVAL(sout, i, 0) =  GETFVAL(sout, i, 1);
    GETFVAL(sout, i, jmax) =  GETFVAL(sout, i, jmax-1);
  }

  sliceScaleAndFree(sout, sin);
  return(0);

}

/*
 * Apply a 2-D or 3-D median filter to the slices contained in volume v,
 * with size as the 2D size, and put the output into sout, which should
 * already be set up for size and mode, but need not have the
 * same mode as the input slices.  
 * Input and output modes can be byte, short, or float.
 */
int sliceMedianFilter(Islice *sout, struct MRCvolume *v, int size)
{
  Islice *sl = v->vol[0];
  float *fVals = NULL;
  int *iVals = NULL;
  int isFloat = 0;
  int blockSize = v->zsize * size * size;
  int nVals, x, y, z, xs, ys, ix, iy, xe, ye, dminus, dplus, select;
  int *iout;
  float *fout;
  Ival val;
  b3dFloat *fin;
  b3dUByte *bin;
  b3dInt16 *sin;

  /* Get array for data, make sure mode is legal */
  if (sl->mode == SLICE_MODE_FLOAT) {
    fVals = (float *)malloc(blockSize * sizeof(float));
    if (!fVals)
      return (-1);
    isFloat = 1;
  } else if (sl->mode == SLICE_MODE_BYTE || sl->mode == SLICE_MODE_SHORT) {
    iVals = (int *)malloc(blockSize * sizeof(int));
    if (!iVals)
      return (-1);
  } else
    return (-2);

  dminus = size / 2;
  dplus = (size + 1) / 2;

  /* Loop on positions */
  for (y = 0; y < sl->ysize; y++) {
    ys = B3DMAX(0, y - dminus);
    ye = B3DMIN(sl->ysize, y + dplus);
    for (x = 0; x < sl->xsize; x++) {
      xs = B3DMAX(0, x - dminus);
      xe = B3DMIN(sl->xsize, x + dplus);
      nVals = v->zsize * (xe - xs) * (ye - ys);
      select = (nVals + 1) / 2;

      /* Loop on slices and subareas to load arrays */
      switch (sl->mode) {
      case SLICE_MODE_FLOAT:
        fout = fVals;
        for (z = 0; z < v->zsize; z++) 
          for (iy = ys; iy < ye; iy++) {
            fin = &v->vol[z]->data.f[xs + iy * sl->xsize];
            for (ix = xs; ix < xe; ix++)
              *fout++ = *fin++;
          }
        val[0] = selectFloat(select, fVals, nVals);
        if (!(nVals % 2))
          val[0] = 0.5 * (val[0] + selectFloat(select + 1, fVals, nVals));
        break;
        
      case SLICE_MODE_SHORT:
        iout = iVals;
        for (z = 0; z < v->zsize; z++) 
          for (iy = ys; iy < ye; iy++) {
            sin = &v->vol[z]->data.s[xs + iy * sl->xsize];
            for (ix = xs; ix < xe; ix++)
              *iout++ = *sin++;
          }
        val[0] =selectInt(select, iVals, nVals);
        if (!(nVals % 2))
          val[0] = 0.5f * (val[0] + selectInt(select + 1, iVals, nVals));
        break;
        
      case SLICE_MODE_BYTE:
        iout = iVals;
        for (z = 0; z < v->zsize; z++) 
          for (iy = ys; iy < ye; iy++) {
            bin = &v->vol[z]->data.b[xs + iy * sl->xsize];
            for (ix = xs; ix < xe; ix++)
              *iout++ = *bin++;
          }
        val[0] =selectInt(select, iVals, nVals);
        if (!(nVals % 2))
          val[0] = 0.5f * (val[0] + selectInt(select + 1, iVals, nVals));
        break;
      }
      slicePutVal(sout, x, y, val);
    }
  }
  if (iVals)
    free(iVals);
  if (fVals)
    free(fVals);
  return 0;
}

/* 
 * Do anisotropic diffusion on slice
 * outMode specifies the output mode; CC is edge stopping type (1, 2, 3)
 * k is the threshold parameter, lambda is step size, iterations is just that
 * clearFlag is one of ANISO_CLEAR_AT_END, ANISO_LEAVE_OPEN. ANISO_CLEAR_ONLY
 * to allow additional iterations to be done on the existing data
 */
int sliceAnisoDiff(Islice *sl,  int outMode, int CC, double k, double lambda,
                   int iterations, int clearFlag)
{
  static double **image, **image2, **imout;
  static int iterDone = 0;
  int i, j;
  int n = sl->xsize;
  int m = sl->ysize;

  /* If just clearing, free arrays if allocated, set iterations to 0 */
  if (clearFlag == ANISO_CLEAR_ONLY) {
    if (iterDone) {
      free(image[0]);
      free(image);
      free(image2[0]);
      free(image2);
      iterDone = 0;
    }
    return 0;
  }

  /* Convert slice to float for copying to/from double */
  if (sl->mode != SLICE_MODE_FLOAT && sliceNewMode(sl, SLICE_MODE_FLOAT) < 0)
    return -1;


  /* If no iterations yet, get double arrays */
  if (!iterDone) {
    image = allocate2D_double(m + 2, n + 2);
    if (!image)
      return -1;
    image2 = allocate2D_double(m + 2, n + 2);
    if (!image2) {
      free(image[0]);
      free(image);
      return -1;
    }

    /* Copy data into array */
    for (j = 0; j < m; j++)
      for (i = 0; i < n; i++)
        image[j + 1][i + 1] = sl->data.f[i + j * sl->xsize];
  }

  /* alternate between two matrices to avoid memcopy */	
  /* printf("m = %d n = %d CC = %d k = %f lambda = %f, iter = %d\n",
     m,n,CC,k,lambda, iterations); */
  for (i = 0; i < iterations; i++, iterDone++) {
	if ( iterDone % 2 == 0 ) {
      updateMatrix(image2,image,m,n,CC,k,lambda,1);
      imout = image2;
	} else {
      updateMatrix(image,image2,m,n,CC,k,lambda,1);
      imout = image;
	}
  }
  
  /* Copy data back to slice */
  for (j = 0; j < m; j++)
    for (i = 0; i < n; i++)
      sl->data.f[i + j * sl->xsize] = (float)imout[j + 1][i + 1];

  /* Free data if doing one-shot operation */
  if (clearFlag == ANISO_CLEAR_AT_END) {
    free(image[0]);
    free(image);
    free(image2[0]);
    free(image2);
    iterDone = 0;
  }

  /* convert slice to output mode */
  if (outMode != SLICE_MODE_FLOAT && sliceNewMode(sl, outMode) < 0)
    return -1;

  return 0;
}

/*
 * Byte version with arrays already allocated and passed in 
 * iterDone is a pointer to variable for keeping track of total iterations 
 */
void sliceByteAnisoDiff(Islice *sl, double **image, double **image2, 
                        int CC, double k, double lambda, int iterations, 
                        int *iterDone)
{
  double **imout;
  int val;
  int i, j;
  int n = sl->xsize;
  int m = sl->ysize;

  /* If no iterations yet, Copy data into array */
  if (!(*iterDone)) {
    for (j = 0; j < m; j++)
      for (i = 0; i < n; i++)
        image[j + 1][i + 1] = sl->data.b[i + j * sl->xsize];
  }

  /* alternate between two matrices to avoid memcopy */	
  /* printf("m = %d n = %d CC = %d k = %f lambda = %f, iter = %d\n",
     m,n,CC,k,lambda, iterations); */
  for (i = 0; i < iterations; i++, (*iterDone)++) {
	if ( (*iterDone) % 2 == 0 ) {
      updateMatrix(image2,image,m,n,CC,k,lambda,1);
      imout = image2;
	} else {
      updateMatrix(image,image2,m,n,CC,k,lambda,1);
      imout = image;
	}
  }
  
  /* Copy data back to slice */
  for (j = 0; j < m; j++) {
    for (i = 0; i < n; i++) {
      val = (int)imout[j + 1][i + 1];
      if (val < 0)
        val = 0;
      if (val > 255)
        val = 255;
      sl->data.b[i + j * sl->xsize] = (unsigned char)val;
    }
  }
}

/* allocate 2D array of doubles that is contiguous in memory
 *
 *  array is actual a 1D vector of length m x n
 *      with a 2D array mapped onto the 1D vector
 */
double **allocate2D_double(int m, int n ) 
{
  double  **a;
  double  *fake;
  int i;
  
  if ( (a = (double **)malloc(sizeof(double *)*m)) == NULL )
    return NULL;
  
  if ( (fake = (double *) malloc(m*n*sizeof(double))) == NULL ) {
    free(a);
    return NULL;
  }

  for (i = 0; i < m; i++)
	a[i] = &(fake[i*n]);
  
  return a;
}


/* A routine for finding min and max as rapidly as possible for the three basic
   data modes */
int sliceMinMax(Islice *s)
{
  int imin, imax, ival, i;
  float fmin, fmax, fval;
  switch (s->mode) {
  case SLICE_MODE_BYTE:
    imin = imax = s->data.b[0];
    for (i = 1; i < s->xsize * s->ysize; i++) {
      ival = s->data.b[i];
      if (imin > ival)
        imin = ival;
      if (imax < ival)
        imax = ival;
    }
    s->min = imin;
    s->max = imax;
    break;

  case SLICE_MODE_SHORT:
    imin = imax = s->data.s[0];
    for (i = 1; i < s->xsize * s->ysize; i++) {
      ival = s->data.s[i];
      if (imin > ival)
        imin = ival;
      if (imax < ival)
        imax = ival;
    }
    s->min = imin;
    s->max = imax;
    break;

  case SLICE_MODE_FLOAT:
    fmin = fmax = s->data.f[0];
    for (i = 1; i < s->xsize * s->ysize; i++) {
      fval = s->data.f[i];
      if (fmin > fval)
        fmin = fval;
      if (fmax < fval)
        fmax = fval;
    }
    s->min = fmin;
    s->max = fmax;
    break;

  default:
    return 1;
  }
  return 0;
}

/* Determine min and max of slice sout, determine scaling to match them to the 
   min and max values provided in slice sin, scale data into sin and free sout
*/ 
static void sliceScaleAndFree(Islice *sout, Islice *sin)
{
  float aval, mval;
  int imax, i;

  sliceMinMax(sout);
  imax = sin->xsize * sin->ysize;
  mval = (sin->max - sin->min) / (sout->max - sout->min);
  aval = sin->min - mval * sout->min;
  if (sout->mode == SLICE_MODE_FLOAT)
    for(i = 0; i < imax; i++)
      sin->data.b[i] = (unsigned char)(sout->data.f[i] * mval + aval);
  else if (sout->mode == SLICE_MODE_SHORT)
    for(i = 0; i < imax; i++)
      sin->data.b[i] = (unsigned char)(sout->data.s[i] * mval + aval);
  else
    for(i = 0; i < imax; i++)
      sin->data.b[i] = (unsigned char)(sout->data.b[i] * mval + aval);

  sliceFree(sout);
}

/* 
 * Routines for selecting item number s (numbered from 1) out of num items
 * selectFloat takes a float array, selectInt takes an int array
 * Based on a pascal program apparently from Handbook of Data Structures and 
 * Algorithms, by Gonnet and Baeza-Yates 
 */
static float selectFloat(int s, float *r, int num)
{
  int lo = 0;
  int up = num - 1;
  int i, j;
  float temp;
  s--;
  while (up >= s && s >= lo) {
    i = lo;
    j = up;
    temp = r[s];
    r[s] = r[lo];
    r[lo] = temp;
    while (i < j) {
      while (r[j] > temp)
        j--;
      r[i] = r[j];
      while (i < j && r[i] <= temp)
        i++;
      r[j] = r[i];
    }
    r[i] = temp;
    if (s < i)
      up = i - 1;
    else
      lo = i + 1;
  }

  return r[s];
}

static int selectInt(int s, int *r, int num)
{
  int lo = 0;
  int up = num - 1;
  int i, j;
  int temp;
  s--;
  while (up >= s && s >= lo) {
    i = lo;
    j = up;
    temp = r[s];
    r[s] = r[lo];
    r[lo] = temp;
    while (i < j) {
      while (r[j] > temp)
        j--;
      r[i] = r[j];
      while (i < j && r[i] <= temp)
        i++;
      r[j] = r[i];
    }
    r[i] = temp;
    if (s < i)
      up = i - 1;
    else
      lo = i + 1;
  }

  return r[s];
}

/*
    $Log$
    Revision 3.2  2005/01/27 05:56:56  mast
    Added anisotropic diffusion

    Revision 3.1  2005/01/07 20:00:45  mast
    Moved to libiimod to make available to clip, added median filter

    Revision 3.6  2004/12/22 15:21:15  mast
    Fixed problems discovered with Visual C compiler

    Revision 3.5  2004/11/07 23:01:27  mast
    Really fixed scaling, used short slices to prevent loss of resolution

    Revision 3.4  2004/11/05 19:08:12  mast
    Include local files with quotes, not brackets

    Revision 3.3  2004/10/29 22:16:55  mast
    Fixed some scaling problems

    Revision 3.2  2004/09/24 18:15:55  mast
    Fixed bug in Sobel filter

    Revision 3.1  2002/12/01 15:34:41  mast
    Changes to get clean compilation with g++

*/
/* DNM 11-11-98: changed loops for filling in the edges after processing
   so that the corners would not have spurious data */

