/*
 * simplexfitting.h - Header for SimplexFitting class
 *
 *  $Id$
 *
 *  $Log$
 *  Revision 1.2  2009/08/10 22:34:39  mast
 *  General reworking of program
 *
 *
 */
#ifndef CURVEFITTING_H
#define CURVEFITTING_H

#include "b3dutil.h"
#include "defocusfinder.h"

class MyApp;

class SimplexFitting
{
 public:
  //SimplexFitting(double *rawData, int nRaw, int index1, int index2);
  SimplexFitting(int nRaw, MyApp *app);
  ~SimplexFitting();
  static void funk(float *, float *);
  static void funkCTF(float *, float *);
  static void funkBase(float *, float *);
  int fitGaussian(double* result, double &err, int howToInitParams);
  int fitCTF(double* result, int nvar, double &err, double &focus);
  void recomputeCTF(double *result, double defocus);
  static double CTFvalueAtIndex(int ind, float *params);
  void setRange(int n1 , int n2){ mIndex1=n1; mIndex2=n2;}
  void setRaw(double *rawData);
  int getDim() {return mDim;}
  void printCTFresult(float err);
  void fitBaseline(double *average, double *baseline, int order);
  void addBasePoint(int ind, double *average, int &indLast, double &rawMin,
                    double &rawMax);

 private:
  MyApp *mApp;
  static int mDim;
  static int mIndex1;
  static int mIndex2;
  static int mNumVar;
  static int mPowerInd;
  static double mExpZero;
  static double mExpSecondZero;
  static float mA[7];
  static double *mRaw;
  static float *mBaseFreq;
  static float *mBaseAvg;
  static float *mBaseWgt;
  static int mNumBase;
  static int mBaseOrder;
  static double mMinErr;
  static DefocusFinder *mFinder;
};

#endif
