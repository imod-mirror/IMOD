#include <math.h>
#include <stdio.h>
#include <qfile.h>
#include <qlabel.h>

#include "simplexfitting.h"
#include "linearfitting.h"
#include "plotter.h"
#include "imod_assistant.h"
#include "myapp.h"

#include "b3dutil.h"
#include "mrcfiles.h"
#include "mrcslice.h"
#include "cfft.h"
#include "parse_params.h"

#define MIN_ANGLE 1.0e-6  //tilt Angle less than this is treated as 0.0;
ImodAssistant *ctfHelp=NULL;

int main(int argc, char *argv[])
{
  int numOptArgs, numNonOptArgs;
  int numOptions=17;
  char *options[]={"param:Parameter:PF:", "config:ConfigFile:CH:", 
    "stack:InputStack:CH:", "angle:AngleFile:CH:", "defFn:DefocusFile:CH:",
    "cs:SphericalAberration:F:", "aAngle:AxisAngle:F:",
    "psRes:PSResolution:I:", "tileSize:TileSize:I:", "defTol:DefocusTol:I:",
    "pixelSize:PixelSize:F:", "ampConstrast:AmplitudeContrast:F:",
    "expDef:ExpectedDefocus:F:", "range:AngleRange:FP:", "volt:Voltage:I:",
    "leftTol:LeftDefTol:F:", "rightTol:RightDefTol:F:"};

  char *cfgFn, *stackFn, *angleFn, *defFn;
  float tiltAxisAngle;
  int volt, nDim, tileSize;
  float defocusTol, pixelSize, lowAngle, highAngle;
  float expectedDef, leftDefTol, rightDefTol;
  float ampContrast, cs;

  //PipReadOrParseOptions(argc, argv, options, numOptions, argv[0], 
  PipReadOrParseOptions(argc, argv, options, numOptions, "ctfplotter", 
      1, 0, 0, &numOptArgs, &numNonOptArgs, NULL);

  if (PipGetString("ConfigFile", &cfgFn))
    exitError("No Config file specified\n");
  if (PipGetString("InputStack", &stackFn))
    exitError("No stack specified\n");
  if (PipGetString("AngleFile", &angleFn))
  {
    angleFn=NULL;
    printf("No angle file is specified, tilt angle is assumed to be 0.0\n");
  }
  if ( PipGetString("DefocusFile", &defFn) )
    exitError("output defocus file is not specified \n");
  if (PipGetInteger("Voltage", &volt))
    exitError("Voltage is not specified\n");
  if (PipGetFloat("SphericalAberration", &cs) )
      exitError("Spherical Aberration is not specified\n");
  if (PipGetInteger("PSResolution", &nDim))
    exitError("PS Resolution is not specified\n");
  if (PipGetInteger("TileSize", &tileSize))
    exitError("No TileSize specified\n");
  if (PipGetFloat("DefocusTol", &defocusTol))
    exitError("No DefousTol specified\n");
  if (PipGetFloat("PixelSize", &pixelSize))
    exitError("No PixelSize specified\n");
  if (PipGetFloat("AmplitudeContrast", &ampContrast))
    exitError("No AmplitudeContrast is specified\n");
  if( PipGetFloat("AxisAngle", &tiltAxisAngle) )
     exitError("No AxisAngle specified\n"); 
  if(PipGetFloat("ExpectedDefocus", &expectedDef))
    exitError("No expected defocus is specified\n");
  if(PipGetFloat("LeftDefTol", &leftDefTol))
    exitError("No left defocus  tolerance is specified\n");
  if(PipGetFloat("RightDefTol", &rightDefTol))
    exitError("No right defocus  tolerance is specified\n");
  if(PipGetTwoFloats("AngleRange", &lowAngle, &highAngle))
    exitError("No AngleRange specified\n");
 
  double *rAvg=(double *)malloc(nDim*sizeof(double));

  ctfHelp = new ImodAssistant("html","IMOD.adp", "ctfguide");
  MyApp app(argc, argv, volt, pixelSize, (double)ampContrast, cs, defFn,
      (int)nDim, (double)defocusTol, tileSize, 
      (double)tiltAxisAngle, -90.0, 90.0, (double)expectedDef, 
      (double)leftDefTol, (double)rightDefTol);
  //set the angle range for noise PS computing;
  app.setPS(rAvg);
  
  Plotter plotter;
  plotter.setCaption(QObject::tr("CTF Plot"));
  app.setMainWidget(&plotter);
  
  /*****begin of computing noise PS;**********/
  FILE *fpCfg;
  if( (fpCfg=fopen(cfgFn, "r"))==0 ){
    printf("ERROR: - could not open config file %s\n", cfgFn);
    exit(1);
  }
  char p[1024];
  int read;
  int noiseFileCounter=0;
  
  // only to find how many noise files are provided;
  while( (read=fgetline(fpCfg, p, 1024)) >0 ) noiseFileCounter++;
  rewind(fpCfg);
  printf("There are %d noise files specified\n", noiseFileCounter);
  
  double *noisePs=(double *)malloc( noiseFileCounter*nDim*sizeof(double));
  double *noiseMean=(double *)malloc( noiseFileCounter*sizeof(double) );
  double *currPS;
  int *index=(int *)malloc(noiseFileCounter*sizeof(double) );
  int i, j;
  
  noiseFileCounter=0;
  while( (read=fgetline(fpCfg, p, 1024))>0){
    //if(p[read-1]=='\n') p[read-1]='\0';  //remove '\n' at the end;
    app.setSlice(p, NULL);
    app.computeInitPS();
    currPS=app.getPS();
    //for(i=0;i<nDim;i++) noisePs[noiseFileCounter][i]=*(currPS+i);
    for(i=0;i<nDim;i++) *(noisePs+noiseFileCounter*nDim+i)=*(currPS+i);
    noiseMean[noiseFileCounter]=app.getStackMean();
    noiseFileCounter++;
  }
  for(i=0;i<noiseFileCounter;i++){
    printf("noiseMean[%d]=%f\n", i, noiseMean[i]);
    index[i]=i;
  }
  //sorting;
  double tempMean;
  double tempIndex;
  for(i=0;i<noiseFileCounter;i++)
    for(j=i+1;j<noiseFileCounter;j++)
      if( noiseMean[i]>noiseMean[j]){
         tempMean=noiseMean[i];
         noiseMean[i]=noiseMean[j];
         noiseMean[j]=tempMean;

         tempIndex=index[i];
         index[i]=index[j];
         index[j]=tempIndex;
      }
  /****end of computing noise PS; ******/

  app.setLowAngle(lowAngle);
  app.setHighAngle(highAngle);
  app.setSlice(stackFn, angleFn);
  double stackMean;
  app.computeInitPS();
  stackMean=app.getStackMean(); //used to choose right noise PS below;

  //binary search to set up the noise PS;
  i=0;
  j=noiseFileCounter-1;
  while( i<(j-1) ){
   if( stackMean > noiseMean[ (i+j)/2 ] ) i=(i+j)/2;
   else j=(i+j)/2;
  }
  printf("Stack mean=%f, Interplating between noise file %d and file %d for \
      noise level of this mean\n", stackMean, i, j);
  app.setNoisePS(noisePs+ index[i]*nDim, noisePs+index[j]*nDim);
  app.setNoiseMean(noiseMean[i], noiseMean[j]);

  if(app.defocusFinder.getExpDefocus()>0) {
    int firstZeroIndex=B3DNINT( app.defocusFinder.getExpZero()*nDim );
    app.setX1Range(firstZeroIndex-16, firstZeroIndex-1);
    double coef=0.5*app.defocusFinder.wavelength*app.defocusFinder.csTwo/ \
               pixelSize;
    int secZeroIndex=B3DNINT( ( sqrt(2.0*app.defocusFinder.csOne/ \
            app.defocusFinder.getExpDefocus())/coef )*nDim );
    app.setX2Range(firstZeroIndex+1, secZeroIndex);
    app.simplexEngine=new SimplexFitting(nDim);
    app.linearEngine=new LinearFitting(nDim);
    app.plotFitPS(); //fit and plot the stack PS;
  }else{
    printf("Invalid expected defocus, it must be >0\n");
    exit(-1);
  }
  
  plotter.resize(768, 624);
  plotter.show();
  app.exec();
  free(rAvg);
  free(noisePs);
  free(noiseMean);
  free(index);
}

int ctfShowHelpPage(const char *page)
{
  if(ctfHelp)
    return (ctfHelp->showPage(page)>0 ? 1 : 0);
  else
    return 1;
}
