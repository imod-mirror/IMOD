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
  order = B3DMIN(order, mNumBase / 2 - 1);

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

