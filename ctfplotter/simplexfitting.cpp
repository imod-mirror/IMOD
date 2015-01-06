/*
* simplexfitting.cpp - Fits Gaussian or CTF curve to segment of a 1D power 
*                      spectrum by the simplex method.
*
*  Authors: Quanren Xiong and David Mastronarde
*
*  Copyright (C) 2008 by Boulder Laboratory for 3-Dimensional Electron
*  Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
*  Colorado.  See dist/COPYRIGHT for full copyright notice.
* 
*  $Id$
*/
#include <math.h>
#include <stdio.h>
#include "simplexfitting.h"
#include "myapp.h"

#define VARNUM 7
#define MAX_ITER 100
#define MIN_ERROR 0.1

int SimplexFitting::mDim=0;
int SimplexFitting::mIndex1=0;
int SimplexFitting::mIndex2=0;
int SimplexFitting::mPowerInd = 4;
double* SimplexFitting::mRaw=NULL;
DefocusFinder *SimplexFitting::mFinder = NULL;
int SimplexFitting::mNumVar = 0;
float SimplexFitting::mA[7] = {0., 0., 0., 0., 0., 0., 0.};
double SimplexFitting::mExpZero = 0.;
double SimplexFitting::mExpSecondZero = 0.;
float *SimplexFitting::mBaseFreq = NULL;
float *SimplexFitting::mBaseAvg = NULL;
float *SimplexFitting::mBaseWgt = NULL;
int SimplexFitting::mNumBase = 0;
int SimplexFitting::mBaseOrder;
double SimplexFitting::mMinErr;

//SimplexFitting::SimplexFitting(double *rawData, int nRaw, int i_1, int i_2)
SimplexFitting::SimplexFitting(int nRaw, MyApp *app)
{
  mApp = app;
  mRaw = new double[nRaw];
  mBaseFreq = new float[nRaw];
  mBaseAvg = new float[nRaw];
  mBaseWgt = new float[nRaw];
  mDim = nRaw;
  mFinder = &mApp->defocusFinder;
}

SimplexFitting::~SimplexFitting()
{
  delete[] mRaw;
  delete[] mBaseFreq;
  delete[] mBaseAvg;
  delete[] mBaseWgt;
}

void SimplexFitting::setRaw(double *rawData)
{
  for (int i=0;i<mDim;i++) 
    mRaw[i]=rawData[i];
}

/*
 * Fit to a gaussian
 */
int SimplexFitting::fitGaussian(double* fitting, double &err, int
                                   howToInitParams)
// mIndex1 and mIndex2 are the
//starting index and endind index of the fitting range.
{
  float pp[VARNUM+1][VARNUM+1], yy[VARNUM+1];
  float da[VARNUM]={2.0, 2.0, 2.0, 2.0, 2.0};
  float a[VARNUM]={0.7, 0.0, 0.2, 0.0, 0.0};
  float min_a[VARNUM];
  float ptol[VARNUM];  
  int nvar=4;
  int iter, jmin, i,  iter_counter;
  float errmin;
  
  float delfac=2.0;
  float ftol2=5.0e-4;
  float ptol2=5.0e-4;
  float ftol1=1.0e-5;
  float ptol1=1.0e-5;
  
  //init params
  if (howToInitParams == 0) {
   a[0]=mRaw[mIndex1];
   a[2]=1.414*(mIndex2-mIndex1)/(mDim-1);
  } else {
   a[0]=mRaw[(mIndex1+mIndex2)/2-1];
   a[1]=0.5*(mIndex2-mIndex1)/(mDim-1);  //sigma=0.3*(mIndex2-mIndex1)/(mDim-1); 
   a[2]=1.414*0.3*(mIndex2-mIndex1)/(mDim-1);
  }
 
  //find the range of fitting data;
  double min, max;
  double range;
  if( (mIndex2-mIndex1)>0 ){
     if( mRaw[mIndex1]> mRaw[mIndex1+1] ){
       max=mRaw[mIndex1];
       min=mRaw[mIndex1+1];
     }else{
       min=mRaw[mIndex1];
       max=mRaw[mIndex1+1];
     }
     for(i=mIndex1+1;i<mIndex2;i++){
       if( mRaw[i]>max ) 
         max=mRaw[i];
       else if( mRaw[i]<min)
         min=mRaw[i];
     }
     range=max-min;
  } else 
    range=mRaw[mIndex1];
  
  iter_counter=0;
  err=100000.0;
  double scaling;
  
  while(iter_counter<MAX_ITER){
    amoebaInit(&pp[0][0], yy, VARNUM+1, nvar, delfac, ptol2, a, da, 
        &SimplexFitting::funk, ptol); 
    amoeba(&pp[0][0], yy, VARNUM+1, nvar, ftol2, &SimplexFitting::funk,
        &iter, ptol, &jmin);
    for (i = 0; i < nvar; i++) 
      a[i] = pp[i][jmin];
    amoebaInit(&pp[0][0], yy, VARNUM+1, nvar, delfac, ptol1, a, da, 
        &SimplexFitting::funk, ptol); 
    amoeba(&pp[0][0], yy, VARNUM+1, nvar, ftol1, &SimplexFitting::funk,
        &iter, ptol, &jmin);

    for (i = 0; i < nvar; i++) 
      a[i] = pp[i][jmin];
    funk(a, &errmin);
    if( (errmin<err || errmin<MIN_ERROR*range) && a[0]>0.0){
      err=errmin;
      for(i=0;i<nvar;i++) 
        min_a[i]=a[i];
    }

    iter_counter++; 
    if(errmin<MIN_ERROR*range && a[0]>0.0) 
      break;

    //re-initialize parameters
    if(iter_counter%2) 
      scaling=0.5*iter_counter/(MAX_ITER-1);
    else
      scaling=-0.5*iter_counter/(MAX_ITER-1);

    a[0]=(1+scaling)*mRaw[mIndex1];
    if(howToInitParams==0) 
      a[1]=0.0;
    else 
      a[1]=0.5*(mIndex2-mIndex1)/(mDim-1); 
    a[2]=(1+scaling)*1.414*(mIndex2-mIndex1)/(mDim-1);
    a[3]=0.0;
  }// iteration
  
  for (i=0; i < mDim; i++) { 
    fitting[i]=min_a[0]*exp( -((float)(i-mIndex1)/(mDim-1)-min_a[1])*
        ((float)(i-mIndex1)/(mDim-1)-min_a[1])/(min_a[2]*min_a[2])) +min_a[3];
  } 

  if( debugLevel>=3){
   printf("Iteration Num=%d threshold=%f Simplex fitting parameters for \
      range %d to %d are:\n", iter_counter, MIN_ERROR*range, mIndex1, mIndex2);
   printf("Fitting error=%f\t a[0]=%f\t a[1]=%f\t a[2]=%f\t a[3]=%f\n",err,
      min_a[0], min_a[1], min_a[2], min_a[3]); 
  }
  fflush(stdout);
  return 0; 
}

/*
 * Callback function for simplex search for a gaussian
 */
void SimplexFitting::funk(float* param, float* fValue)
{
  int i;
  double x;
  *fValue=0.0;
  // This is not a sum of squares!
  for (i = mIndex1; i < mIndex2; i++) {
    x = (double)(i-mIndex1)/(mDim-1.) - param[1];
    *fValue=*fValue + fabs(mRaw[i] - param[0] *
                           exp(-(x * x) / (param[2] * param[2])) - param[3]); 
  }
}

/*
 * Fit to a CTF-like curve
 */
int SimplexFitting::fitCTF(double* fitting, int nvar, double &err, double &focus)
{
  float pp[VARNUM+1][VARNUM+1], yy[VARNUM+1];
  float da[VARNUM]={2.0, 2.0, 2.0, 2.0, 0.1};
  float ptol[VARNUM];  
  int iter, jmin, i;
  float errmin;
  float delfac=2.0;
  float ftol2=5.0e-4;
  float ptol2=5.0e-4;
  float ftol1=1.0e-5;
  float ptol1=1.0e-5;
  double startDef;
  double rawMin = 1.e30;
  double rawMax = -1.e30;

  // Get starting defocus depending on current option setting, and get 
  // the zero at that defocus
  startDef = mApp->getStartingDefocusAndZeros(mExpZero, mExpSecondZero);

  // Initialize values, or leave previous values for some kinds of 
  // restricted fits
  mNumVar = nvar;
  for (i = mIndex1; i <= mIndex2; i++) {
    rawMin = B3DMIN(rawMin, mRaw[i]);
    rawMax = B3DMAX(rawMax, mRaw[i]);
  }
  mA[0] = startDef;
  if (nvar > 1) {
    mA[1] = rawMin - 0.1 * (rawMax - rawMin);
    da[1] = 0.1 * (rawMax - rawMin);
  }
  if (nvar > 2) {
    mA[2] = (mRaw[mIndex1] - rawMin) / mFinder->CTFvalue(mIndex1/(mDim-1.), startDef);
    da[2] = 0.2 * mA[2];
  }
  if (nvar > 3) {
    mA[3] = 10.;
    mPowerInd = 4;
    if (nvar > 5) {
      mPowerInd = 6;
      mA[4] = mA[2] / 10.;
      mA[5] = 1.;
      da[4] = 0.2 * mA[4];
      da[5] = 0.2;
    }
    mA[mPowerInd] = 1.5;
    da[mPowerInd] = 0.1;
  }
  
  amoebaInit(&pp[0][0], yy, VARNUM+1, nvar, delfac, ptol2, mA, da, 
             &SimplexFitting::funkCTF, ptol); 
  amoeba(&pp[0][0], yy, VARNUM+1, nvar, ftol2, &SimplexFitting::funkCTF,
         &iter, ptol, &jmin);
  for (i = 0; i < nvar; i++) 
    mA[i] = pp[i][jmin];
  amoebaInit(&pp[0][0], yy, VARNUM+1, nvar, delfac, ptol1, mA, da, 
             &SimplexFitting::funkCTF, ptol); 
  amoeba(&pp[0][0], yy, VARNUM+1, nvar, ftol1, &SimplexFitting::funkCTF,
         &iter, ptol, &jmin);

  for (i = 0; i < nvar; i++) 
    mA[i] = pp[i][jmin];
  funkCTF(mA, &errmin);
  err=sqrt(errmin);
  focus = mA[0];

  for (i = 0; i < mDim; i++) { 
    fitting[i] = CTFvalueAtIndex(i, mA);
  } 
  printCTFresult(err);
  fflush(stdout);
  return 0;
}

double SimplexFitting::CTFvalueAtIndex(int ind, float *params)
{
  double ctfval, delx, x;
  x = (double)ind / (mDim - 1.);
  delx = (double)(ind - mIndex1) / (mDim - 1.);
  ctfval = mFinder->CTFvalue(x, (double)params[0]);
  if (mPowerInd == 4)
    return params[1] + params[2] * exp(-params[3] * delx) * 
      pow(fabs(ctfval), (double)params[4]);
  else
    return params[1] + (params[2] * exp(-params[3] * delx) + params[4] * 
                        exp(-params[5] * delx)) * pow(fabs(ctfval), (double)params[6]);
}


/*
 * Recompute a previously fit CTF-like curve at given defocus
 */
void SimplexFitting::recomputeCTF(double* result, double defocus)
{
  int i;
  float err;

  mA[0] = defocus;
  funkCTF(mA, &err);
  err = sqrt(err);
  for (i = 0; i < mDim; i++) { 
    result[i] = CTFvalueAtIndex(i, mA);
  } 
  printCTFresult(err);
  fflush(stdout);
}

void SimplexFitting::printCTFresult(float err)
{
  if (debugLevel >= 1) {
    printf("CTF fitting parameters for range %d to %d are:\n", mIndex1, mIndex2);
    if (mPowerInd == 4)
      printf("Fitting error=%f\t def=%f\t base=%f\t scale=%g\t decay=%f\t pow=%f\n",
             err, mA[0], mA[1], mA[2], mA[3], mA[4]);
    else
      printf("Fitting error=%f\t def=%f\t base=%f\t scale=%g\t decay=%f\t pow=%f\t "
             "scale2=%g\t decay2=%f\n" ,err, mA[0], mA[1], mA[2], mA[3], mA[6], mA[4], 
             mA[5]);
  }
}

/*
 * The callback function for the simplex search fitting to a CTF
 */
void SimplexFitting::funkCTF(float* param, float* fValue)
{
  double x, y, err, zero1, zero2;
  int i;
  float parUse[VARNUM];
  int indKeepPos[5] = {0, 2, 3, 4, 5};
  int numKeepPos = mPowerInd == 4 ? 3 : 5;
  for (i = 0; i < mNumVar; i++)
    parUse[i] = param[i];
  for (i = mNumVar; i < VARNUM; i++)
    parUse[i] = mA[i];

  // Compute the error
  err = 0.;
  for (i = mIndex1; i <= mIndex2; i++) {
    y = CTFvalueAtIndex(i, parUse) - mRaw[i];
    err += y * y;
  }

  // Make error much bigger if power gets too far from 1
  if (parUse[mPowerInd] > 2. || parUse[mPowerInd] < 0.5) {
    x = B3DMAX(parUse[mPowerInd] - 2., 0.5 - parUse[mPowerInd]);
    err *= 5 * (1 + 5 * x);
  }
  
  // If the defocus places the second zero closer to the expected defocus than
  // the first zero, make error bigger
  mFinder->getTwoZeros(parUse[0], zero1, zero2);
  if (fabs(mExpZero - zero2) < 0.33 * fabs(mExpZero - zero1))
    err *= 5. * (3. - fabs(mExpZero - zero2) / mExpZero);
  else if (mExpZero > zero2)
    err *= 5. * (1 + 5. * (mExpZero - zero2));
  else if (fabs(zero1 - mExpSecondZero) < 0.33 * fabs(zero1 - mExpZero))
    err *= 5. * (3. - fabs(zero1 - mExpSecondZero) / zero1);
  else if (zero1 > mExpSecondZero)
    err *= 5. * (1 + 5. * (zero1 - mExpSecondZero));
  if (zero1 > 0.9)
    err *= 5. * (1. + 5. * (zero1 - 0.9));

  // Make error much bigger if defocus becomes negative (overfocus), or if a scale or 
  // decay factor does
  for (i = 0; i < numKeepPos; i++)
    if (parUse[indKeepPos[i]] < 0.0)
      err *= 5. * (1. - 5. * parUse[indKeepPos[i]]);

  *fValue = (float)err;
  /*printf("err=%f  def=%f  fc=%f  scale=%f  decay=%f\n", err, parUse[0], parUse[1],
    parUse[2], parUse[3]);*/
}

/*
 * Fit a polynomial to smoothed points, mostly at minima if the exist, constraining the
 * polynominal to have a monotoninc first derivative, and produce a baseline to subtract 
 * from the log power
 */
void SimplexFitting::fitBaseline(double *average, double *baseline, int order)
{
  float pp[VARNUM+1][VARNUM+1], yy[VARNUM+1];
  float da[VARNUM];
  float ptol[VARNUM], var[VARNUM];  
  int iter, jmin, i;
  float errmin;
  float delfac=2.0;
  float ftol2=5.0e-4;
  float ptol2=5.0e-4;
  float ftol1=1.0e-5;
  float ptol1=1.0e-5;
  double rawMin = 1.e30;
  double rawMax = -1.e30;
  int boxSize = 3;
  float minSearchFrac = 0.5;
  int tailInterval = 3;
  int left = boxSize / 2;
  int right = boxSize - 1 - boxSize / 2;
  int box, ind, indStart, indEnd, nvar, indLast;
  double err, x0, freq, firstFit;
  double *boxAvg;
  float *work;
  
  mApp->getStartingDefocusAndZeros(x0, err);

  for (ind = 0; ind < mDim; ind++)
    baseline[ind] = 0.;

  if (!order)
    return;
  boxAvg = B3DMALLOC(double, mDim);
  //work = B3DMALLOC(float, (order + 1) * (order + 3 + mDim));
  work = B3DMALLOC(float, (order + 2) * mDim + (order + 1) * (order + 3));

  // Get running box averages
  for (box = 0; box < mDim; box++) {
    indStart = B3DMAX(0, box - left);
    indEnd = B3DMIN(mDim - 1, box + right);
    boxAvg[box] = 0.;
    for (ind = indStart; ind <= indEnd; ind++)
      boxAvg[box] += log(average[ind]) / (indEnd + 1 - indStart);
  }

  // Search from  first zero to a certain point for true minima
  indStart = B3DNINT(x0 * (mDim - 1));
  indEnd = indStart + B3DNINT(minSearchFrac * (mDim - indStart));
  mNumBase = 0;
  for (ind = indStart; ind <= indEnd; ind++) {
    if (boxAvg[ind] < boxAvg[ind - 1] && boxAvg[ind] < boxAvg[ind + 1]) {
      
      // It must be monotonic to the next points up on either side
      if (boxAvg[ind - 1] < boxAvg[ind - 2] && boxAvg[ind + 1] < boxAvg[ind + 2]) 
        addBasePoint(ind, boxAvg, indLast, rawMin, rawMax);
    }
  }

  if (mNumBase)
    indStart = indLast + 1;

  // Now add any local minima and points at minimum intervals after the first local
  // minimum if there have been no points before this
  for (ind = indStart; ind < (int)(0.95 * mDim); ind++) {
    if ((boxAvg[ind] < boxAvg[ind - 1] && boxAvg[ind] < boxAvg[ind + 1]) ||
        (mNumBase > 0 && ind - indLast >= tailInterval))
      addBasePoint(ind, boxAvg, indLast, rawMin, rawMax);
  }
  order = B3DMAX(0, B3DMIN(order, mNumBase / 2 - 1));

  // Assign weights proportional to inverse of point spacing; give first point extra
  // weight
  if (order) {
    err = 0.;
    for (ind = 0; ind < mNumBase; ind++) {
      if (!ind)
        mBaseWgt[ind] = 2. / fabs(mBaseFreq[ind + 1] - mBaseFreq[ind]);
      else if (ind == mNumBase - 1)
        mBaseWgt[ind] = 1. / fabs(mBaseFreq[ind] - mBaseFreq[ind - 1]);
      else
        mBaseWgt[ind] = 2. / (fabs(mBaseFreq[ind + 1] - mBaseFreq[ind]) +
                              fabs(mBaseFreq[ind] - mBaseFreq[ind - 1]));
      err += mBaseWgt[ind];
    }
    for (ind = 0; ind < mNumBase; ind++)
      mBaseWgt[ind] /= (err / mNumBase);
  }

  // Start (or end) with a quadratic or linear fit to initialize the search
  var[2] = var[3] = var[4] = 0.;
  if (order && weightedPolyFit(mBaseFreq, mBaseAvg, mBaseWgt, mNumBase, B3DMIN(order, 2),
                               &var[1], &var[0], work))
    order = 0;
  mBaseOrder = order;
  if (order > 2) {

    // Do simplex fitting
    nvar = order + 1;
    mMinErr = 1.e20;
    da[0] = da[1] = da[2] = 0.2 * (rawMax - rawMin);
    da[3] = 0.5 * da[2];
    da[4] = 0.5 * da[2];
    amoebaInit(&pp[0][0], yy, VARNUM+1, nvar, delfac, ptol2, var, da, 
               &SimplexFitting::funkBase, ptol); 
    amoeba(&pp[0][0], yy, VARNUM+1, nvar, ftol2, &SimplexFitting::funkBase,
           &iter, ptol, &jmin);
    mMinErr = 1.e20;
    for (i = 0; i < nvar; i++) 
      var[i] = pp[i][jmin];
    amoebaInit(&pp[0][0], yy, VARNUM+1, nvar, delfac, ptol1, var, da, 
               &SimplexFitting::funkBase, ptol); 
    amoeba(&pp[0][0], yy, VARNUM+1, nvar, ftol1, &SimplexFitting::funkBase,
           &iter, ptol, &jmin);

    for (i = 0; i < nvar; i++) 
      var[i] = pp[i][jmin];
  }

  if (order) {
    funkBase(var, &errmin);
    err = sqrt(errmin);
    if (debugLevel >= 1) {
      printf("Base function fit error=%f\t const=%f\t coeff=%f", err, var[0], var[1]);
      for (ind = 2; ind <= order; ind++)
        printf("\t %f", var[ind]);
      printf("\n");
    }

    // Start the baseline at 0 and compute from the polynomial over the fitted range
    indStart = B3DNINT(mBaseFreq[0] * (mDim - 1.));
    indEnd = B3DNINT(mBaseFreq[mNumBase - 1] * (mDim - 1.));
    freq = mBaseFreq[0];
    firstFit = var[0] + var[1] * freq + var[2] * freq * freq + 
      var[3] * freq * freq * freq + var[4] * freq * freq * freq * freq;
    for (ind = indStart + 1; ind <= indEnd; ind++) {
      freq = ind  / (mDim - 1.);
      baseline[ind] = var[0] + var[1] * freq + var[2] * freq * freq + 
        var[3] * freq * freq * freq + var[4] * freq * freq * freq * freq - firstFit;
      // printf("%3d  %f\n", ind, baseline[ind]);
    }

    // Extrapolate baseline linearly from end of polynomial fit
    for (; ind < mDim; ind++)
      baseline[ind] = 2 * baseline[ind - 1] - baseline[ind - 2];
  }

  fflush(stdout);
  free(boxAvg);
  free(work);
}

// Add a point to the baseline fitting set
void SimplexFitting::addBasePoint(int ind, double *average, int &indLast, double &rawMin,
                                  double &rawMax)
{
  mBaseFreq[mNumBase] = ind / (mDim - 1.);
  mBaseAvg[mNumBase++] = average[ind];
  indLast = ind;
  rawMin = B3DMIN(rawMin, average[ind]);
  rawMax = B3DMAX(rawMax, average[ind]);
  // printf("%3d  %6.3f  %g\n", ind, ind / (mDim - 1.), average[ind]);
}

/*
 * The callback function for the simplex search fitting to baseline polynomial
 */
void SimplexFitting::funkBase(float *param, float *fValue)
{
  double delx, delx2, y, err, freq, rootTerm, inflect;
  int ind;

  // Compute error for different orders of polynomial
  err = 0;
  for (ind = 0; ind < mNumBase; ind++) {
    freq = mBaseFreq[ind];
    y = param[0] + param[1] * freq + param[2] * freq * freq - mBaseAvg[ind];
    if (mBaseOrder > 2)
      y += param[3] * freq * freq * freq;
    if (mBaseOrder > 3)
      y += param[4] * freq * freq * freq * freq;
    err += mBaseWgt[ind] * y * y;
  }

  delx = 0.;
  delx2 = 0.;
  inflect = 0.;
  if (mBaseOrder < 4) {

    // Evaluate zero of second derivative for cubic function and set delx to how far 
    // inside the fitting interval it is
    if (mBaseOrder > 2 && fabs(param[3]) > 1.e-10) {
      inflect = param[2] / (3. * param[3]);
      if (inflect > mBaseFreq[0] && inflect < mBaseFreq[mNumBase - 1])
        delx = B3DMIN(inflect - mBaseFreq[0], mBaseFreq[mNumBase - 1] - inflect);
    }
  } else {

    // Evaluate two zeros if second derivative of 4th degree function, set either
    // delx or delx2 positive by how far it is inside the fitting interval, take the
    // max of those two
    rootTerm = 9. * param[3] - 24. * param[2] * param[4];
    if (rootTerm >= 0) {
      inflect = (-3 * param[3] + sqrt(rootTerm)) / (6. * param[4]);
      if (inflect > mBaseFreq[0] && inflect < mBaseFreq[mNumBase - 1])
        delx = B3DMIN(inflect - mBaseFreq[0], mBaseFreq[mNumBase - 1] - inflect);
      inflect = (-3 * param[3] - sqrt(rootTerm)) / (6. * param[4]);
      if (inflect > mBaseFreq[0] && inflect < mBaseFreq[mNumBase - 1])
        delx2 = B3DMIN(inflect - mBaseFreq[0], mBaseFreq[mNumBase - 1] - inflect);
      delx = B3DMAX(delx, delx2);
    }
  }

  // Bump up the error, more the farther inside the interval the inflection is
  if (delx > 0)
    err *= 5. * (1. + 5. * delx);
  *fValue = (float)err;
  //printf("%s%f  %f %f %f %f infl %.2f delx %.2f\n", err < mMinErr ? "*" : " ",
  //   sqrt(err), param[0], param[1], param[2], param[3], inflect, delx);
  if (err < mMinErr) {
    mMinErr = err;
  }
}

/*  regression.c - replacements for old Fortran regression routines, and robust regression
 *
 *  Author: David Mastronarde   email: mast@colorado.edu
 *
 *  Copyright (C) 2012 by Boulder Laboratory for 3-Dimensional Electron
 *  Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *  Colorado.  See dist/COPYRIGHT for full copyright notice.
 *
 * $Id$
 */
#include <math.h>
#include "imodconfig.h"
#include "b3dutil.h"
#include "string.h"

/* Macro to allow data matrix to be accessed with row, column indices regardless
   of its order */
#define XRC(r, c) (x[(r) * rowStride + (c) * colStride])

#ifdef F77FUNCAP
#define statmatrices STATMATRICES
#define multregress MULTREGRESS
#define robustregress ROBUSTREGRESS
#define polynomialfit POLYNOMIALFIT
#define weightedpolyfit WEIGHTEDPOLYFIT
#else
#define statmatrices statmatrices_
#define multregress multregress_
#define robustregress robustregress_
#define polynomialfit polynomialfit_
#define weightedpolyfit weightedpolyfit_
#endif

/*!
 * Computes basic statistical values and matrices from a data matrix representing a series
 * of measurements of multiple variables.  ^
 * Input parameters:  ^
 * [x]       - Input data matrix  ^
 * [xsize]   - Size of the fastest-progressing dimension of [x]  ^
 * [colFast] - Nonzero if the column dimension is the fastest progressing one  ^
 * [m]       - Number of columns of data, i.e., number of parameters  ^
 * [msize]   - Size of one dimension of the various square output matrices  ^
 * [ndata]   - Number of rows of data; i.e., number of separate measurements ^
 * [ifdisp]  - Value indicating either to skip computation of [d] and [r] (if 0), or to
 * treat the {m+1} column of data as weighting values (if < 0) ^
 * Outputs:
 * [sx]      - Array for the sums of the [m] data columns  ^
 * [ss]      - Array for raw sums of squares and cross-products  ^
 * [ssd]     - Array for sums of deviation squares and cross-products  ^
 * [d]       - Array for variances and covariances  (dispersion matrix) ^
 * [r]       - Array for matrix of correlation coefficients  ^
 * [xm]      - Array for the means of the [m] data columns  ^
 * [sd]      - Array for the standard deviations of the [m] data columns  ^
 * The output matrices will be treated as having leading dimension [msize], so they must 
 * be at least [msize] x [m] in size.  The data element at row i, column j would
 * be accessed as x\[i + j * xsize\] or x\[j\]\[i\] from C, or as x(i,j) from Fortran if
 * [colFast] is 0, or as x\[j + i * xsize\], x\[i\]\[j\], or x(j,i) if [colFast] is 
 * nonzero.  When  weighting is used, the returned values are weighted means, and other 
 * statistics with the weights incorporated.
 */
void statMatrices(float *x, int xsize, int colFast, int m, int msize, int ndata,
                  float *sx, float *ss, float *ssd, float *d, float *r, float *xm,
                  float *sd, int ifdisp)
{
  float fndata, den, weight, wsum;
  int i, j, k;
  int colStride = colFast ? 1 : xsize;
  int rowStride = colFast ? xsize : 1;
  
  fndata = ndata;
  
  for (i = 0; i< m; i++) {
    sx[i] = 0.;
    for (j = 0; j < m; j++) {
      ssd[msize * i + j] = 0.;
      r[msize * i + j] = 0.;
    }
  }
  
  if (ifdisp >= 0) {
    for (i = 0; i< m; i++) {
      for (k = 0; k < ndata; k++)
        sx[i] += XRC(k, i);
      xm[i] = sx[i] / fndata;
    }
  } else {
    wsum = 0.;
    for (k = 0; k < ndata; k++)
      wsum += XRC(k, m);
    for (i = 0; i< m; i++) {
      for (k = 0; k < ndata; k++)
        sx[i] += XRC(k, i) * XRC(k, m);
      xm[i] = sx[i] / wsum;
    }
  }
  
  for (k = 0; k < ndata; k++) {
    weight = 1.;
    if(ifdisp < 0)
      weight = XRC(k, m);
    for (i = 0; i< m; i++)
      for (j = 0; j < m; j++)
        ssd[i * msize + j] += (XRC(k, i) - xm[i]) * (XRC(k, j) - xm[j]) * weight;
  }
  
  for (i = 0; i< m; i++) {
    sd[i] = (float)sqrt((double)(ssd[i * msize + i] / (fndata-1.)));
    for (j = 0; j < m; j++) {
      ss[i * msize + j] = ssd[i * msize + j] + sx[i] * sx[j] / fndata;
      ss[j * msize + i] = ss[i * msize + j];
      ssd[j * msize + i] = ssd[i * msize + j];
    }
  }
  if(ifdisp == 0)
    return;
  for (i = 0; i< m; i++) {
    for (j = 0; j < m; j++) {
      d[i * msize + j] = ssd[i * msize + j] / (fndata-1.);
      d[j * msize + i] = d[i * msize + j];
      den = sd[i] * sd[j];
      r[i * msize + j] = 1.;
      if(den > 1.e-30)
        r[i * msize + j] = d[i * msize + j] / (sd[i] * sd[j]);
      r[j * msize + i] = r[i * msize + j];
    }
  }
}

/*!
 * Fortran wrapper for @statMatrices
 */
void statmatrices(float *x, int *xsize, int *colFast, int *m, int *msize, int *ndata,
                  float *sx, float *ss, float *ssd, float *d, float *r, float *xm,
                  float *sd, int *ifdisp)
{
  statMatrices(x, *xsize, *colFast, *m, *msize, *ndata, sx, ss, ssd, d, r, xm, sd,
               *ifdisp);
}


/*!
 * Computes a multiple linear regression (least-squares fit) for the relationships between
 * one or more dependent variables and a set of independent variables.  ^
 * Input parameters:  ^
 * [x]       - Input data matrix  ^
 * [xsize]   - Size of the fastest-progressing dimension of [x]  ^
 * [colFast] - Nonzero if the column dimension is the fastest progressing one, i.e. if
 * successive values in the array occur in successive columns  ^
 * [m]       - Number of columns of data for independent variables  ^
 * [ndata]   - Number of rows of data; i.e., number of separate measurements ^
 * [nbcol]   - Number of columns of data for dependent variables; i.e., number of 
 * relationships to fit  ^
 * [wgtcol]  - Column number with weighting factors if > 0, otherwise no weighting.  
 * Columns are numbered from 0 when calling from C, or 1 calling from Fortran. ^
 * [bsize]   - Size of the fastest-progressing dimension of [b], the array/matrix to
 * receive the solutions; must be at least [m]  ^
 * [work]    - Any array for temporary use whose size must be at least 
 * (m + nbcol) * (m + nbcol)  floating point elements  ^
 * Outputs:  ^
 * [b]       - Matrix to receive the [nbcol] sets of [m] coefficients.  Each set is 
 * placed in a column of [b], where the row dimension is the fastest progressing one ^
 * [c]       - Array to receive the [m] constant terms of the fits  ^
 * [xm]      - Array for the means of the [m] plus [nbcol] data columns  ^
 * [sd]      - Array for the standard deviations of the [m] plus [nbcol] data columns  ^
 * The independent variables should be placed in the first [m] columns of [x] and the
 * the dependent variables in the next [nbcol] columns. The data element at row i,
 * column j would be accessed as x\[i + j * xsize\] or x\[j\]\[i\] from C, or as x(i,j) 
 * from Fortran if [colFast] is 0, or as x\[j + i * xsize\], x\[i\]\[j\], or x(j,i) if 
 * [colFast] is nonzero.  ^
 * The return value is 1 if [wgtcol] has an inappropriate value, or 3 if @gaussj returns
 * with an error.
 */
int multRegress(float *x, int xsize, int colFast, int m, int ndata, int nbcol,
                int wgtcol, float *b, int bsize, float *c, float *xm, float *sd,
                float *work)
{
  float fndata, den;
  double dsum, wsum;
  int i, j, k, mp;
  int colStride = colFast ? 1 : xsize;
  int rowStride = colFast ? xsize : 1;
  mp = m + nbcol;
  fndata = ndata;
  if (wgtcol > 0 && (wgtcol < mp || (!colFast && wgtcol >= xsize)))
    return 1;

  /* Get the unweighted means */
  if (wgtcol <= 0) {
    for (i = 0; i< mp; i++) {
      dsum = 0.;
      for (k = 0; k < ndata; k++)
        dsum += XRC(k, i);
      xm[i] = dsum / fndata;
    }
  } else {

    /* Or add up the weights and get the weighted means */
    wsum = 0.;
    for (k = 0; k < ndata; k++)
      wsum += XRC(k, wgtcol);
    for (i = 0; i< mp; i++) {
      dsum = 0.;
      for (k = 0; k < ndata; k++)
        dsum += XRC(k, i) * XRC(k, wgtcol);
      xm[i] = dsum / wsum;
    }
  }

  /* Get the sums of squares and cross-products of deviations */
  for (i = 0; i < mp; i++) {
    for (j = i; j < mp; j++) {
      if (i >= m && i != j)
        continue;
      dsum = 0.;
      if (wgtcol <= 0) {
        for (k = 0; k < ndata; k++)
          dsum += (XRC(k, i) - xm[i]) * (XRC(k, j) - xm[j]);
      } else {
        for (k = 0; k < ndata; k++)
          dsum += (XRC(k, i) - xm[i]) * (XRC(k, j) - xm[j]) * XRC(k, wgtcol);
      }
      work[j * mp + i] = dsum;
    }
  }
    
  /* Get the SDs */
  for (i = 0; i< mp; i++)
    sd[i] = (float)sqrt((double)(work[i * mp + i] / (fndata-1.)));

  /* Scale it by n -1 to get covariance and by SD's to get the correlation matrix */
  for (i = 0; i < m; i++) {
    for (j = i; j < mp; j++) {
      den = sd[i] * sd[j];
      if (den < 1.e-30) {
        /* printf("sds %d %d %f %f  den %f\n", i, j, sd[i], sd[j], den);
           fflush(stdout); */
        work[j * mp + i] = 1.;
      } else
        work[j * mp + i] /= den * (fndata-1.);
      if (j < m)
        work[i * mp + j] = work[j * mp + i];
    }
  }
  /* for (i = 0; i < m; i++) {
    for (j = 0; j < mp; j++)
      printf("  %.8f", work[j * mp + i]);
    printf("\n");
    } */
  
  /* The matrix to be solved is now completely filled and symmetric so row/column
     doesn't matter, but to call gaussj we need to transpose the final columns into b */
  for (j = 0; j < nbcol; j++) {
    for (i = 0; i < m; i++)
      b[j + i * nbcol] = work[(j + m) * mp + i];
    /*printf("\nb:");
    for (i = 0; i < m; i++)
    printf("    %g", b[j + i * nbcol]); */
  }

  if (gaussj(work, m, mp, b, nbcol, nbcol))
    return 3;
  
  /*for (j = 0; j < nbcol; j++) {
    printf("\nb sol:");
    for (i = 0; i < m; i++)
      printf("    %g", b[j + i * nbcol]);
  }
  printf("\n");
  fflush(stdout);*/

  /* Scale the coefficients and transpose them back; get constant terms */
  memcpy(work, b, m * nbcol * sizeof(float));
  for (j = 0; j < nbcol; j++) {
    c[j] = xm[m + j];
    for (i = 0; i< m; i++) {
      if (sd[i] < 1.e-30)
        b[i + bsize * j] = 0.;
      else
        b[i + bsize * j] = work[j + i * nbcol] * sd[m + j] / sd[i];
      c[j] -= b[i + bsize * j] * xm[i];
    }
  }
  return 0;
}

/* Fortran wrapper */
int multregress(float *x, int *xsize, int *colFast, int *m, int *ndata, int *nbcol,
                int *wgtcol, float *b, int *bsize, float *c, float *xm, float *sd,
                float *work)
{
  return multRegress(x, *xsize, *colFast, *m, *ndata, *nbcol, *wgtcol - 1, b, *bsize, c,
                     xm, sd, work);
}

/* LAPACK NOTE:  To use ssysv, need to add
ssysv.$(O) ssytrf.$(O) ssytrs.$(O) slasyf.$(O) ssytf2.$(O)
slasyf.f  ssysv.f  ssytf2.f  ssytrf.f  ssytrs.f
to lapack, and
sscal.$(O) sger.$(O)  sgemv.$(O) sswap.$(O) scopy.$(O) isamax.$(O)  sgemm.$(O)\
        ssyr.$(O)
scopy.f  sgemm.f  sgemv.f  sger.f  sscal.f  sswap.f  ssyr.f isamax.f
to blas.  To use sspsv, need to add
ssptrs.$(O) ssptrf.$(O) sspsv.$(O)
sspsv.f  ssptrf.f  ssptrs.f  
to lapack and
sspr.$(O)
sspr.f
to blas

But hey, we could just ask for a double work array here.
The impediment is not doubles, it's having a different library.
Also, a bigger work array is needed not just for doubles but also for dsysv.
*/

/*!
 * Uses multiple linear regression to fit a polynomial of order
 * [order] to [ndata] points whose (x,y) coordinates are in the arrays [x] and [y].
 * It returns the coefficient of x to the i power in the array [slopes] and a
 * constant term in [intcpt].  The equation fit is:  ^
 * Y = intcpt + slopes\[0\] * X + slopes\[1\] * X**2 + ...  ^
 * [work] is an array whose size must be at least ([order] + 1) * ([order] + 3 + [ndata]).
 * The return value is the value returned by @@multRegress@.  Note that a Fortran 
 * function polyfit in libhvem takes care of allocating [work] to the needed size and 
 * calling the Fortran wrapper to this function.
 */
int polynomialFit(float *x, float *y, int ndata, int order, float *slopes, float *intcpt,
                  float *work)
{
  int wdim = order + 1;
  int i, j;
  float *xm = work + wdim * ndata;
  float *sd = xm + wdim;
  float *mwork = sd + wdim;
  if (!order)
    return 1;
  for (i = 0; i < ndata; i++) {
    for (j = 0; j < order; j++)
      work[i + j * ndata] = (float)pow((double)x[i], j + 1.);
    work[i + order * ndata] = y[i];
  }
  return (multRegress(work, ndata, 0, order, ndata, 1, 0, slopes, ndata, intcpt, xm, sd,
                      mwork));
}

/* Fortran wrapper */
int polynomialfit(float *x, float *y, int *ndata, int *order, float *slopes, 
                  float *intcpt, float *work)
{
  return polynomialFit(x, y, *ndata, *order, slopes, intcpt, work);
}

/*!
 * Uses multiple linear regression to fit a polynomial of order
 * [order] to [ndata] points whose (x,y) coordinates are in the arrays [x] and [y].
 * It returns the coefficient of x to the i power in the array [slopes] and a
 * constant term in [intcpt].  The equation fit is:  ^
 * Y = intcpt + slopes\[0\] * X + slopes\[1\] * X**2 + ...  ^
 * [work] is an array whose size must be at least ([order] + 2) * [ndata] +
 * ([order] + 1) * ([order] + 3)).  ^
 * The return value is the value returned by @@multRegress@.  This function is untested.
 */
int weightedPolyFit(float *x, float *y, float *weight, int ndata, int order,
                    float *slopes, float *intcpt, float *work)
{
  int wdim = order + 1;
  int i, j;
  float *xm = work + (order + 2) * ndata;
  float *sd = xm + wdim;
  float *mwork = sd + wdim;
  if (!order)
    return 1;
  for (i = 0; i < ndata; i++) {
    for (j = 0; j < order; j++)
      work[i + j * ndata] = (float)pow((double)x[i], j + 1.);
    work[i + order * ndata] = y[i];
    work[i + (order + 1) * ndata] = weight[i];
  }
  return (multRegress(work, ndata, 0, order, ndata, 1, order + 1, slopes, ndata, intcpt,
                      xm, sd, mwork));
}

/* Fortran wrapper */
int weightedpolyfit(float *x, float *y, float *weight, int *ndata, int *order,
                    float *slopes, float *intcpt, float *work)
{
  return weightedPolyFit(x, y, weight, *ndata, *order, slopes, intcpt, work);
}

/*!
 * Computes a robust least squares fit by iteratively computing a weight from the residual
 * for each data point then doing a weighted regression.  The weight is computed by
 * finding the median and normalized median absolute deviation (MADN) of the residual
 * values.  When there are multiple columns of dependent variables, the square root of the
 * sum of the squares of the residuals for the different variables is used.  Each 
 * residual is standardized by taking (residual - median) / MADN, and the weight is
 * computed from the Tukey bisquare equation using the standardized residual divided by 
 * the specified [kfactor].  However, with multiple dependent variables, all residuals 
 * are positive and ones less than the median are given a weighting of 1.
 * Input parameters:  ^
 * [x]       - Input data matrix  ^
 * [xsize]   - Size of the fastest-progressing dimension of [x]  ^
 * [colFast] - Nonzero if the column dimension is the fastest progressing one  ^
 * [m]       - Number of columns of data for independent variables  ^
 * [ndata]   - Number of rows of data; i.e., number of separate measurements ^
 * [nbcol]   - Number of columns of data for dependent variables; i.e., number of 
 * relationships to fit.   ^
 * [bsize]   - Size of the fastest-progressing dimension of [b], the array/matrix to
 * receive the solutions; must be at least [m]  ^
 * [work]    - Any array for temporary use whose size must be at least the maximum of
 * (m + nbcol) * (m + nbcol) and 2 * ndata floating point elements  ^
 * [kfactor] - Factor by which to divide the standardized residual value in computing the
 * Tukey bisquare weight.  4.68 is the typical value; a different value may be more 
 * appropriate with multiple dependent variables  ^
 * [maxIter] - Maximum number of iterations, or the negative of the maximum to get trace 
 * output on each iteration.  20-50 is probably adequate.  With a negative value, the
 * program outputs the mean and maximum change in weighting and the number of points
 * with weights of 0, between 0 and 0.1, between 0.1 and 0.2, and less than 0.5.   ^
 * [maxZeroWgt] - Maximum number of points allowed to have zero weights.  When this number
 * is exceeded on an iteration, the deviations are sorted and the criterion deviation is 
 * permanently changed to midway 
 * between the deviations for the last point to be retained and first one to be 
 * eliminated.  ^
 * [maxChange]  - Maximum change in weights from one iteration to the next or across two 
 * iterations; the entry should not be smaller than 0.01.  ^
 * [maxOscill]  - Maximum change in weights from one iteration to the next, even if 
 * oscillating. The iterations are terminated when the biggest change in weights between 
 * iterations is less than [maxChange], or when it is less than [maxOscill] and the 
 * biggest change across two iterations is less than [maxChange].  ^
 * Outputs:  ^
 * [b]       - Matrix to receive the [nbcol] sets of [m] coefficients.  Each set is 
 * placed in a column of [b], where the row dimension is the fastest progressing one ^
 * [c]       - Array to receive the [m] constant terms of the fits  ^
 * [xm]      - Array for the means of the [m] plus [nbcol] data columns  ^
 * [sd]      - Array for the standard deviations of the [m] plus [nbcol] data columns  ^
 * [numIter] - Number of iterations  ^
 * The independent variables should be placed in the first [m] columns of [x] and the
 * the dependent variables in the next [nbcol] columns (see @@multRegress@).
 * Final weights are returned in the column of [x] after the dependent variables, and the
 * column after that is used for weights on the previous iteration, so [x]
 * must have at least [m] + [nbcol] + 2 columns.  ^
 * The return value is 1 if there are not enough columns for the weights (detectable 
 * only when [colFast] is nonzero), 3 if @gaussj returns with an error, or -1 if the
 * weights do not converge.
 */
int robustRegress(float *x, int xsize, int colFast, int m, int ndata, int nbcol, float *b,
                  int bsize, float *c, float *xm, float *sd, float *work, float kfactor,
                  int *numIter, int maxIter, int maxZeroWgt, float maxChange,
                  float maxOscill)
{
  int i, j, numOut, ierr, k, iter, num1, num2, num5, report = 0;
  int wgtcol = m + nbcol;
  int prevCol = wgtcol + 1;
  int colStride = colFast ? 1 : xsize;
  int rowStride = colFast ? xsize : 1;
  int splitLastTime = 0, keepCriterion = 0;
  float diff, diffsum, diffmax, median, MADN, dev, weight, ressum, colres;
  float prev, prevmax, criterion;
  if (maxIter < 0) {
    report = 1;
    maxIter = - maxIter;
  }

  if (colFast && prevCol >= xsize)
    return 1;

  /* Initialize weights to 1. */
  for (j = 0; j < ndata; j++)
    XRC(j, wgtcol) = XRC(j, prevCol) = 1.;

  /* Iterate */
  for (iter = 0; iter < maxIter; iter++) {

    /* Get regression solution */
    ierr = multRegress(x, xsize, colFast, m, ndata, nbcol, wgtcol, b, bsize, c, xm, sd,
                       work);
    if (ierr)
      return ierr;

    /* Compute residuals.  Note WORK has to be bigger for this! */
    for (j = 0; j < ndata; j++) {
      if (nbcol == 1) {
        colres = c[0] - XRC(j, m);
        for (i = 0; i < m; i++)
          colres += XRC(j, i) * b[i];
        work[j] = colres;
      } else {
        ressum = 0.;
        for (k = 0; k < nbcol; k++) {
          colres = c[k] - XRC(j, (m + k));
          for (i = 0; i < m; i++)
            colres += XRC(j, i) * b[i + k * bsize];
          /* printf("   %g", colres); */
          ressum += colres * colres;
        }
        work[j] = (float)sqrt(ressum);
        /* printf("   resid  %g\n", work[j]); */
      }
    }

    /* Get the median and MADN */
    rsMedian(work, ndata, &work[ndata], &median);
    rsMADN(work, ndata, median, &work[ndata], &MADN);
    if (!keepCriterion)
      criterion = kfactor * MADN;
    /* printf("Median %g   MADN %g\n", median, MADN); */

    /* Get the new weights and evaluate change from last time */
    diffsum = 0.;
    diffmax = 0.;
    numOut = 0;
    num1 = 0;
    num2 = 0;
    num5 = 0;
    prevmax = 0.;
    
    /* Convert to the deviations that will be compared to criterion and count zero's */
    for (j = 0; j < ndata; j++) {
      work[j] -= median;

      /* For multiple columns, negative deviations are like no deviation */
      if (nbcol > 1)
        work[j] = B3DMAX(0., work[j]);
      else if (work[j] < 0)
        work[j] = -work[j];
      if (work[j] > criterion)
        numOut++;
    }

    /* If there are too many out, need to adjust criterion to limit that number */
    if (numOut > maxZeroWgt) {
      for (j = 0; j < ndata; j++)
        work[ndata + j] = work[j];
      rsSortFloats(&work[ndata], ndata);
      diff = criterion;
      criterion = (work[2 * ndata - maxZeroWgt] + work[2 * ndata - maxZeroWgt - 1]) / 2.;
      if (report)
        printf("%d with zero weight, revising criterion from %g to %g\n", numOut,
               diff, criterion);
      keepCriterion = 1;
    }

    numOut = 0;
    for (j = 0; j < ndata; j++) {

      /* Tests to handle case of MADN zero; avoid division by 0 */
      if (work[j] > criterion) {
        weight = 0.;
        numOut++;
      } else if (work[j] <= 1.e-6 * criterion) {
        weight = 1.;
      } else {
        dev = work[j] / criterion;
        weight = (1 - dev * dev) * (1 - dev * dev);
      }
      if (report) {
        if (weight > 0 && weight <= 0.1)
          num1++;
        if (weight > 0.1 && weight <= 0.2)
          num2++;
        if (weight < 0.5)
          num5++;
      }

      /* Get differences from last time and from time before that */
      diff = fabs(weight - XRC(j, wgtcol));
      diffsum += diff;
      diffmax = B3DMAX(diffmax, diff);
      prev = fabs(weight - XRC(j, prevCol));
      prevmax = B3DMAX(prevmax, prev);
      XRC(j, prevCol) = XRC(j, wgtcol);
      XRC(j, wgtcol) = weight;
    }
    if (report) {
      printf("Iter %3d del mean %.4f max %.4f prev %.4f # 0, 0.1, 0.2, <0.5: %d %d %d "
             "%d\n", iter, diffsum / ndata, diffmax, prevmax, numOut, num1, num2, num5);
      fflush(stdout);
    }

    /* Quit if difference from last time is below limit, or if it is below the oscillation
       limit and difference from the previous time is below limit */
    if (!splitLastTime && (diffmax < maxChange || 
                           (diffmax < maxOscill && prevmax < maxChange)))
      break;

    /* But if we are oscillating, try half-way between and defer testing for 2 rounds */
    if (!splitLastTime && prevmax < maxChange / 2.) {
      splitLastTime = 1;
      if (report) {
        printf("        Oscillation detected: averaging previous weights\n");
        fflush(stdout);
      }
      for (j = 0; j < ndata; j++)
        XRC(j, wgtcol) = (XRC(j, wgtcol) + XRC(j, prevCol)) / 2.;
    } else if (splitLastTime == 1)
      splitLastTime = 2;
    else
      splitLastTime = 0;
  }

  *numIter = iter;
  return (*numIter < maxIter ? 0 : -1);
}

/* Fortran wrapper */
int robustregress(float *x, int *xsize, int *colFast, int *m, int *ndata, int *nbcol, 
                  float *b, int *bsize, float *c, float *xm, float *sd, float *work,
                  float *kfactor, int *numIter, int *maxIter, int *maxZeroWgt, 
                  float *maxChange, float *maxOscillate)
{
  return robustRegress(x, *xsize, *colFast, *m, *ndata, *nbcol, b,
                       *bsize, c, xm, sd, work, *kfactor, numIter, 
                       *maxIter, *maxZeroWgt, *maxChange, *maxOscillate);
}

