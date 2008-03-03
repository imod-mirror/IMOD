/*
 *  beadhelper.c -- Special plugin for contour drawing tools
 *
 */

/*****************************************************************************
 *   Copyright (C) 2007 by Andrew Noske from the Institute for Molecular     *
 *   Bioscience at the University of Queensland (Australia)                  *
 *****************************************************************************/

/*  $Author$

    $Date$

    $Revision$

    Revision 0.0  2008/2/25 15:45:41  noske
    Made special module to be used in IMOD

*/

//############################################################

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

#include <qvariant.h>
#include <qaction.h>
#include <qapplication.h>
#include <qpushbutton.h>
#include <qcheckbox.h>
#include <qlabel.h>
#include <qcombobox.h>
#include <qvbuttongroup.h>
#include <qradiobutton.h>
#include <qdialog.h>
#include <qspinbox.h>
#include <qlayout.h>
#include <qgroupbox.h>
#include <qtooltip.h>
#include <qstringlist.h>
#include <qmessagebox.h>
#include <qinputdialog.h>

#include "_common_functions.h"
#include "imodplugin.h"
#include "dia_qtutils.h"
#include "beadhelper.h"

//############################################################

static BeadHelperData plug = { 0, 0 };

//############################################################



//----------------------------------------------------------------------------
//
//          MAPPED FUNCTIONS:
//
//----------------------------------------------------------------------------



//------------------------
//-- MAPPED FUNCTION: Called by the imod plugin load function

char *imodPlugInfo(int *type)
{
  if (type)
    *type = IMOD_PLUG_MENU + IMOD_PLUG_KEYS + IMOD_PLUG_MESSAGE + 
      IMOD_PLUG_MOUSE + IMOD_PLUG_EVENT;
    
  return("Bead Helper");
}

//------------------------
//-- MAPPED FUNCTION: Grab hotkey input. return 1 if we handle the key.
 
int imodPlugKeys(ImodView *vw, QKeyEvent *event)
{
  int keyhandled = 1;
  
  if (!plug.view)          // if plugin window isn't open: don't grab keys
    return 0;
  
  int keysym = event->key();            // key value (Key_A, Key_Space... etc)
  int ctrl    = event->state() & Qt::ControlButton;   // ctrl modifier
  int shift   = event->state() & Qt::ShiftButton;     // shift modifier
  
  switch(keysym)
  {
    case Qt::Key_E:
      plug.window->movePtToExstimatedPosCurrCont();
      plug.window->drawExtraObject(true);
      break;
      
    case Qt::Key_F:                  // fill points in current contour
      plug.window->fillMissingPtsCurrCont();
      break;
      
    case Qt::Key_D:                  // delete points current contour to nearest end
      plug.window->deletePtsCurrContToNearestEnd( false );
      break;
      
    case Qt::Key_R:                  // reduce current point to seed
      if (shift)
        return 0;
      else
        plug.window->reduceCurrContToSeed();
      break;
      
    case Qt::Key_M:                  // move contour
      if (ctrl) {
        plug.window->moveContour();
        return 1;
      }
      else {
        return 0;
      }
      break;
      
    case Qt::Key_H:                  // go to next largest hole
      bead_goToNextBiggestHole( !shift );
      break;
      
    //## LIST OF KEYS THAT REQUIRE A REDRAW OF THE EXTRA OBJECT:
      
    /*case Qt::Key_5:
    case Qt::Key_C:
    case Qt::Key_BracketLeft:
    case Qt::Key_BracketRight:
      plug.window->drawExtraObject(false);
      return 0;*/
      
      /*
    case Qt::Key_T:                  // temporary testing purposes - comment out %%%%
      if (shift)
        plug.window->test();
      else
        return 0;
      break;
      */
      
    default:
      keyhandled = 0;
      break;
  }
  
  return keyhandled;
}


//------------------------
//-- MAPPED FUNCTION: Called when plugin window is started.
//-- Opens the plugin window for user interaction and initilizes data.
//-- See imodplug.h for a list of support functions.

void imodPlugExecute(ImodView *inImodView)
{

  if (plug.window) {      // if already open: bring window to front
    plug.window->raise();
    return;
  }
  
  ivwSetMovieModelMode( plug.view, IMOD_MMODEL );
  
  //## INITIALIZE DATA:
  
  plug.view = inImodView;
  ivwTrackMouseForPlugs(plug.view, 1);
  ivwEnableStipple( plug.view, 1 );
      
  if( !plug.initialized )
  {
    ivwGetImageSize(inImodView, &plug.xsize, &plug.ysize, &plug.zsize);
    plug.middleSlice = int(plug.zsize / 2);
    plug.middlePt.x = plug.xsize / 2;
    plug.middlePt.y = plug.ysize / 2;
    plug.middlePt.z = plug.zsize / 2;
    
    //** MAIN SETTINGS:
    
    plug.sliceMin           = 1;
    plug.sliceMax           = plug.zsize;
    plug.contMin            = 5;
    plug.contMax            = 1000;
    
    plug.showExpectedPos    = 1;
    plug.wheelBehav         = WH_SMART;
    plug.estPosMethod       = EM_NEARESTTWO;
    plug.showSpheres        = true;
    plug.sphereSize         = 2;
    
    plug.lineDisplayType    = LD_OFF;
    plug.tiltDisplayType    = TD_OFF;
    
    //** MORE SETTINGS:
    
    plug.tiltAngle             = -11.7;
    plug.tiltOffsetX           = 0;
    plug.biggestHoleGrid       = 20;
    plug.wheelResistance       = 100;
    
    plug.expPtDisplayType      = ED_CROSS;
    plug.expPtSize             = 6;
    
    plug.autoSaveSettings      = true;
    
    plug.window->loadSettings();
    
    /*bool success =  file_saveStringToFile("beadhelpersettings.txt", "yep\nyep\nyep\n" );
    
    vector<string> file = file_loadTextFromFile("beadhelpersettings.txt");
    for( int i=0; i<file.size(); i++ )
    {
      wprint("LINE");
      cout << file[i] << endl;
    }*/
    
    plug.initialized        = true;
  }
  
  
  //## SET UP EXTRA OBJECTS:
  
  plug.extraObjExtra       = ivwGetFreeExtraObjectNumber(plug.view);
  plug.extraObjTiltAxis    = ivwGetFreeExtraObjectNumber(plug.view);
  plug.extraObjContDisp    = ivwGetFreeExtraObjectNumber(plug.view);
  plug.extraObjExpPos      = ivwGetFreeExtraObjectNumber(plug.view);
  
  Iobj *xobjE = ivwGetAnExtraObject(plug.view, plug.extraObjExpPos);
  imodObjectSetColor(xobjE, 0.8, 0, 0);
  imodObjectSetValue(xobjE, IobjFlagClosed, 0);
  
  Iobj *xobjC = ivwGetAnExtraObject(plug.view, plug.extraObjContDisp);
  imodObjectSetColor(xobjC, 0, 0.4, 0);
  imodObjectSetValue(xobjC, IobjFlagClosed, 0);
  
  Iobj *xobjT = ivwGetAnExtraObject(plug.view, plug.extraObjTiltAxis);
  imodObjectSetColor(xobjT, 1, 1, 0);
  imodObjectSetValue(xobjT, IobjFlagClosed, 0);
  
  Iobj *xobjX = ivwGetAnExtraObject(plug.view, plug.extraObjExtra);
  imodObjectSetColor(xobjX, 1, 0, 1);
  imodObjectSetValue(xobjX, IobjPointSize, 3);
  imodObjectSetValue(xobjX, IobjFlagClosed, 0);
  
  
  //## CREATE THE PLUGING WINDOW:
  
  plug.window  = new BeadHelper(imodDialogManager.parent(IMOD_DIALOG),"Bead Helper");
  
  imodDialogManager.add((QWidget *)plug.window, IMOD_DIALOG);
  plug.window->show();
  
  //## REDRAW:
  
  plug.window->changeSphereSize( plug.sphereSize );
  ivwRedraw( plug.view );
}


//------------------------
//-- MAPPED FUNCTION: Called when initialization is complete
//-- with inReason = IMOD_REASON_STARTUP, or need to update for change
//-- in model with inReason = IMOD_REASON_MODUPDATE.

void imodPlugExecuteType(ImodView *inView, int inType, int inReason)
{
  if( inReason == IMOD_REASON_MODUPDATE
     && ivwGetModel(plug.view)
      && plug.window )
  {
      plug.window->drawExtraObject(false,0);
  }
}


//------------------------
//-- MAPPED FUNCTION: Process wheel events

int imodPlugEvent(ImodView *vw, QEvent *event, float imx, float imy)
{
  if( plug.window == NULL )
    return (0);
  
  if ( event->type() == QEvent::Wheel && plug.wheelBehav!=WH_NONE )
  {
    QWheelEvent *wheelEvent = static_cast<QWheelEvent*>(event);
    
    int change = floor( wheelEvent->delta() / plug.wheelResistance );
    
    if( plug.wheelBehav == WH_POINTS )
    {
      plug.window->advanceSelectedPointInCurrCont( change );
      plug.window->drawExtraObject(false,0);
      ivwRedraw( plug.view );
      return 1;
    }
    else if( plug.wheelBehav == WH_SLICES )
    {
      edit_changeSelectedSlice( change, true );
      plug.window->drawExtraObject(false);
      return 0;
    }
    else if( plug.wheelBehav == WH_SMART )
    {
      if( isCurrContValid() )
      {
        int newSlice = edit_getZOfTopZap() + change;
        keepWithinRange( newSlice, 0, plug.zsize );
        
        if( bead_isPtOnSlice( getCurrCont(), newSlice ) )
        {
          Imod *imod = ivwGetModel(plug.view);
          int objIdx, contIdx, ptIdx;
          imodGetIndex(imod, &objIdx, &contIdx, &ptIdx);
          int newPtIdx = bead_getPtIdxOnSlice( getCurrCont(), newSlice );
          imodSetIndex(imod, objIdx, contIdx, newPtIdx);
          //
          plug.window->drawExtraObject(false,0);
          ivwRedraw( plug.view );
          return 1;
          //return 2;
        }
      }
      edit_changeSelectedSlice( change, true );
      plug.window->drawExtraObject(true,0);
      return 1;
    }
  }
  return 0;
}


//------------------------
//-- MAPPED FUNCTION: Process a mouse event: An example of a circular cursor  
//-- with radius specified in image coordinates

/*
     Mouse event callback function to be defined by plugins with the
     IMOD_PLUG_MOUSE bit set.  ^
     This function can be used to override 3dmod mouse actions in the Zap 
     window.  [imx] and [imy] will contain the image position, and [but1], 
     [but2], and [but3] will indicate the state of the 3 buttons as mapped by 
     user preferences.  The return value should be the sum of two values: 
     ^  1 if the plugin handled the mouse event, indicating that no other action
     should be taken with the event itself by the 3dmod program.
     ^  2 if the specific calling window should draw itself, without issuing a
     general program redraw.  If this is not sufficient, the plugin should call
     ivwRedraw instead of setting this bit.
     ^  A zero return value indicates that 3dmod should process the event as usual.
     ^This function is called only when a mouse button is down, unless mouse
     tracking is enabled with ivwTrackMouseForPlugs.
*/

int imodPlugMouse(ImodView *vw, QMouseEvent *event, float imx, float imy,
                  int but1, int but2, int but3)
{
  plug.mouse.x = imx;
  plug.mouse.y = imy;
  
  return (0);
}





//############################################################

//----------------------------------------------------------------------------
//
//          BeadHelper METHODS:
//
//----------------------------------------------------------------------------



//## WINDOW CLASS CONSTRUCTOR:

static char *buttonLabels[] = {"Done", "Help"};
static char *buttonTips[] = {"Close Bead Helper", "Open help window"};

BeadHelper::BeadHelper(QWidget *parent, const char *name) :
      DialogFrame(parent, 2, buttonLabels, buttonTips, true, "Bead Helper", "", name)
{
  const int LAYOUT_MARGIN   = 4;
  const int LAYOUT_SPACING  = 4;
  const int GROUP_MARGIN    = 1;
  const int SPACER_HEIGHT   = 15;
  
  
  //## Range:
  
  grpRange = new QGroupBox("Range:", this);
  grpRange->setFocusPolicy(QWidget::NoFocus);
  grpRange->setMargin(GROUP_MARGIN);
  
  gridLayout1 = new QGridLayout(grpRange);
  gridLayout1->setSpacing(LAYOUT_SPACING);
  gridLayout1->setMargin(LAYOUT_MARGIN);
  gridLayout1->addItem( new QSpacerItem(1,SPACER_HEIGHT), 0, 0);
  
  lblSlices = new QLabel("slices: ", grpRange);
  lblSlices->setFocusPolicy(QWidget::NoFocus);
  gridLayout1->addWidget(lblSlices, 1, 0);
  
  sliceMinSpinner = new QSpinBox(grpRange);
  sliceMinSpinner->setFocusPolicy(QWidget::ClickFocus);
  sliceMinSpinner->setMinValue(1);
  sliceMinSpinner->setMaxValue(plug.zsize);
  sliceMinSpinner->setValue( plug.sliceMin );
  QToolTip::add(sliceMinSpinner, "Minimum slice value (inclusive)");
  gridLayout1->addWidget(sliceMinSpinner, 1, 1);
  
  lblSlicesTo = new QLabel(" to ", grpRange);
  lblSlicesTo->setFocusPolicy(QWidget::NoFocus);
  gridLayout1->addWidget(lblSlicesTo, 1, 2);
  
  sliceMaxSpinner = new QSpinBox(grpRange);
  sliceMaxSpinner->setFocusPolicy(QWidget::ClickFocus);
  sliceMaxSpinner->setMinValue(1);
  sliceMaxSpinner->setMaxValue(plug.zsize);
  sliceMaxSpinner->setValue( plug.sliceMax );
  QToolTip::add(sliceMaxSpinner, "Maximum slice value (inclusive)");
  gridLayout1->addWidget(sliceMaxSpinner, 1, 3);
  
  lblContours = new QLabel("contours: ", grpRange);
  lblContours->setFocusPolicy(QWidget::NoFocus);
  gridLayout1->addWidget(lblContours, 2, 0);
  
  contMinSpinner = new QSpinBox(grpRange);
  contMinSpinner->setFocusPolicy(QWidget::ClickFocus);
  contMinSpinner->setMinValue(1);
  contMinSpinner->setMaxValue(1000);
  contMinSpinner->setValue( plug.contMin );
  QToolTip::add(contMinSpinner, "Minimum contour value (inclusive)");
  gridLayout1->addWidget(contMinSpinner, 2, 1);
  
  lblContoursTo = new QLabel(" to ", grpRange);
  lblContoursTo->setFocusPolicy(QWidget::NoFocus);
  gridLayout1->addWidget(lblContoursTo, 2, 2);
  
  contMaxSpinner = new QSpinBox(grpRange);
  contMaxSpinner->setFocusPolicy(QWidget::ClickFocus);
  contMaxSpinner->setMinValue(1);
  contMaxSpinner->setMaxValue(1000);
  contMaxSpinner->setValue( plug.contMax );
  QToolTip::add(contMaxSpinner, "Maximum contour value (inclusive)");
  gridLayout1->addWidget(contMaxSpinner, 2, 3);
  
  mLayout->addWidget(grpRange);
  

  //## Actions:
  
  grpActions = new QGroupBox("Actions:", this);
  grpActions->setFocusPolicy(QWidget::NoFocus);
  
  vboxLayout1 = new QVBoxLayout(grpActions);
  vboxLayout1->setSpacing(LAYOUT_SPACING);
  vboxLayout1->setMargin(LAYOUT_MARGIN);
  vboxLayout1->addItem( new QSpacerItem(1,SPACER_HEIGHT) );
  
  deletePtsButton = new QPushButton("Delete Points in Range [d]", grpActions);
  connect(deletePtsButton, SIGNAL(clicked()), this, SLOT(deletePtsInRange()));
  QToolTip::add(deletePtsButton,
                "Deletes all points on the specified views from the specified "
                "range of contours");
  vboxLayout1->addWidget(deletePtsButton);
  
  reduceContsToSeedButton = new QPushButton("Reduce Contours to Seed  [r]", grpActions);
  QObject::connect(reduceContsToSeedButton, SIGNAL(clicked()), this,
                   SLOT(reduceContsToSeed()));
  QToolTip::add(reduceContsToSeedButton,
                "Deletes all points in the current contour except on the middle slice");
  vboxLayout1->addWidget(reduceContsToSeedButton);
  
  movePtsToEstButton = new QPushButton("Move Point to Estimated Pos [e]", grpActions);
  QObject::connect(movePtsToEstButton, SIGNAL(clicked()), this,
                   SLOT(movePtsToExstimatedPos()));
  QToolTip::add(movePtsToEstButton,
                "Move or creates a point on the current slice based on the position of "
                "the closest points either side");
  vboxLayout1->addWidget(movePtsToEstButton);
  
  fillMissingPtsButton = new QPushButton("Fill Missing Points   [f]", grpActions);
  QObject::connect(fillMissingPtsButton, SIGNAL(clicked()), this,
                   SLOT(fillMissingPts()));
  QToolTip::add(fillMissingPtsButton,
                "Fills in missing points");
  vboxLayout1->addWidget(fillMissingPtsButton);
  
  reorderContsButton = new QPushButton("Reorder Contours", grpActions);
  connect(reorderContsButton, SIGNAL(clicked()), this, SLOT(reorderContours()));
  QToolTip::add(reorderContsButton,
                "Reorders the specified range of contours by one of several crititeria");
  vboxLayout1->addWidget(reorderContsButton);
  
  mLayout->addWidget(grpActions);
  
  
  //## Display:
  
  grpDisplay = new QGroupBox("Visual Aids:", this);
  grpDisplay->setFocusPolicy(QWidget::NoFocus);
  grpDisplay->setMargin(GROUP_MARGIN);
  
  gridLayout2 = new QGridLayout(grpDisplay);
  gridLayout2->setSpacing(LAYOUT_SPACING);
  gridLayout2->setMargin(LAYOUT_MARGIN);
  gridLayout2->addItem( new QSpacerItem(1,SPACER_HEIGHT), 0, 0);
  
  showExpectedPosCheckbox = new QCheckBox("show expected position", grpDisplay);
  showExpectedPosCheckbox->setChecked( plug.showExpectedPos );
  QObject::connect(showExpectedPosCheckbox,SIGNAL(clicked()),this,
                   SLOT(changeShowExpectedPos()));
  QToolTip::add(showExpectedPosCheckbox, 
                "Shows the expected position of the current point based on "
                "position of points either side");
  gridLayout2->addMultiCellWidget(showExpectedPosCheckbox, 1, 1, 0, 1);
  
  showSpheresCheckbox = new QCheckBox("sphere size:", grpDisplay);
  showSpheresCheckbox->setChecked( plug.showSpheres );
  QObject::connect(showSpheresCheckbox,SIGNAL(clicked()),this,
                   SLOT(changeShowSpheres()));
  gridLayout2->addWidget(showSpheresCheckbox, 2, 0);
  
  sphereSizeSpinner = new QSpinBox(grpDisplay);
  sphereSizeSpinner->setMinValue(1);
  sphereSizeSpinner->setMaxValue(50);
  sphereSizeSpinner->setFocusPolicy(QWidget::NoFocus);
  sphereSizeSpinner->setValue( plug.sphereSize );
  QObject::connect(sphereSizeSpinner,SIGNAL(valueChanged(int)),this,
                   SLOT(changeSphereSize(int)));
  QToolTip::add(sphereSizeSpinner,
                "The size of the spheres you wish to display");
  gridLayout2->addWidget(sphereSizeSpinner, 2, 1);
  
  lblLineDisplay = new QLabel("line display: ", grpDisplay);
  lblLineDisplay->setFocusPolicy(QWidget::NoFocus);
  gridLayout2->addWidget(lblLineDisplay, 3, 0);
  
  lineDisplayCombo = new QComboBox(grpDisplay);
	lineDisplayCombo->setFocusPolicy(QWidget::NoFocus);
	lineDisplayCombo->insertItem("off");
	lineDisplayCombo->insertItem("curr contour");
  lineDisplayCombo->insertItem("missing pts");
	lineDisplayCombo->insertItem("all contours");
  lineDisplayCombo->insertItem("pt residuals");
  lineDisplayCombo->insertItem("line best fit");
  lineDisplayCombo->setCurrentItem( plug.lineDisplayType );
	connect(lineDisplayCombo, SIGNAL(activated(int)), this,
          SLOT(changeLineDisplayType(int)));
	QToolTip::add(lineDisplayCombo, 
              "Visual aid to let you see the trajectory of contours");
	gridLayout2->addWidget(lineDisplayCombo, 3, 1);
  
  lblTiltDisplay = new QLabel("tilt display: ", grpDisplay);
  lblTiltDisplay->setFocusPolicy(QWidget::NoFocus);
  gridLayout2->addWidget(lblTiltDisplay, 4, 0);
  
  tiltDisplayCombo = new QComboBox(grpDisplay);
	tiltDisplayCombo->setFocusPolicy(QWidget::NoFocus);
	tiltDisplayCombo->insertItem("off");
  tiltDisplayCombo->insertItem("tilt axis");
  tiltDisplayCombo->insertItem("tilt and pt");
  tiltDisplayCombo->setCurrentItem( plug.tiltDisplayType );
	connect(tiltDisplayCombo, SIGNAL(activated(int)), this,
          SLOT(changeTiltDisplayType(int)));
	QToolTip::add(tiltDisplayCombo, 
                "Visual aid to let you see the trajectory of contours");
	gridLayout2->addWidget(tiltDisplayCombo, 4, 1);
  
  mLayout->addWidget(grpDisplay);
  
  
  //## Display:
  
  grpOptions = new QGroupBox("Options:", this);
  grpOptions->setFocusPolicy(QWidget::NoFocus);
  grpOptions->setMargin(GROUP_MARGIN);
  
  gridLayout3 = new QGridLayout(grpOptions);
  gridLayout3->setSpacing(LAYOUT_SPACING);
  gridLayout3->setMargin(LAYOUT_MARGIN);
  gridLayout3->addItem( new QSpacerItem(1,SPACER_HEIGHT), 0, 0);
  
  lblWheelBehav = new QLabel("wheel behavior:", grpOptions);
  gridLayout3->addWidget(lblWheelBehav, 1, 0);
  
  wheelBehavCombo = new QComboBox(grpOptions);
  wheelBehavCombo->setFocusPolicy(QWidget::NoFocus);
	wheelBehavCombo->insertItem("none");
	wheelBehavCombo->insertItem("scroll points");
	wheelBehavCombo->insertItem("scroll slices");
  wheelBehavCombo->insertItem("smart scroll");
  wheelBehavCombo->setCurrentItem( plug.wheelBehav );
	connect(wheelBehavCombo, SIGNAL(activated(int)), this,
          SLOT(changeWheelBehav(int)));
  QToolTip::add(wheelBehavCombo, 
                "Allows you to use the mouse wheel to scroll through points");
  gridLayout3->addWidget(wheelBehavCombo, 1, 1);
  
  lblEstMethod = new QLabel("estimation meth:", grpOptions);
  gridLayout3->addWidget(lblEstMethod, 2, 0);
  
  estPosMethodCombo = new QComboBox(grpOptions);
  estPosMethodCombo->setFocusPolicy(QWidget::NoFocus);
	estPosMethodCombo->insertItem("nearest 2 pts");
	estPosMethodCombo->insertItem("curve");
	estPosMethodCombo->insertItem("local curve");
  estPosMethodCombo->insertItem("...");
  estPosMethodCombo->setCurrentItem( plug.estPosMethod );
	connect(estPosMethodCombo, SIGNAL(activated(int)), this,
          SLOT(changeEstPosMethod(int)));
  QToolTip::add(estPosMethodCombo, 
                "The method used to estimate the position of points "
                "on a given slice of the contour" );
  gridLayout3->addWidget(estPosMethodCombo, 2, 1);
  
  mLayout->addWidget(grpOptions);
  
  otherActionsButton = new QPushButton("More Actions", grpOptions);
  connect(otherActionsButton, SIGNAL(clicked()), this, SLOT(otherActions()));
  QToolTip::add(otherActionsButton,
                "Contains several other actions I didn't want to sqeeze "
                "into this window");
  gridLayout3->addWidget(otherActionsButton, 3, 0);
  
  otherSettingsButton = new QPushButton("More Settings", grpOptions);
  connect(otherSettingsButton, SIGNAL(clicked()), this, SLOT(otherSettings()));
  QToolTip::add(otherSettingsButton,
                "Contains several other actions I didn't want to sqeeze "
                "into this window");
  gridLayout3->addWidget(otherSettingsButton, 3, 1);
  
  mLayout->addStretch();
  this->adjustSize();
  
  connect(this, SIGNAL(actionPressed(int)), this, SLOT(buttonPressed(int)));
}


//## SLOTS:



//------------------------
//-- Adds points in the shape of a crosshair to contour

void cont_addCrosshair( Icont *cont, Ipoint center, float size, int slice )
{
  float h = size/2.0;
  imodPointAppendXYZ( cont, center.x,     center.y,      slice );
  imodPointAppendXYZ( cont, center.x,     center.y+h,    slice );
  imodPointAppendXYZ( cont, center.x,     center.y-h,    slice );
  imodPointAppendXYZ( cont, center.x,     center.y,      slice );
  imodPointAppendXYZ( cont, center.x+h,   center.y,      slice );
  imodPointAppendXYZ( cont, center.x-h,   center.y,      slice );
  imodPointAppendXYZ( cont, center.x,     center.y,      slice );
}

//------------------------
//-- Adds points in the shape of a cross to contour

void cont_addCross( Icont *cont, Ipoint center, float size, int slice )
{
  float h = size/2.0;
  imodPointAppendXYZ( cont, center.x,     center.y,      slice );
  imodPointAppendXYZ( cont, center.x+h,   center.y+h,    slice );
  imodPointAppendXYZ( cont, center.x-h,   center.y-h,    slice );
  imodPointAppendXYZ( cont, center.x,     center.y,      slice );
  imodPointAppendXYZ( cont, center.x+h,   center.y-h,    slice );
  imodPointAppendXYZ( cont, center.x-h,   center.y+h,    slice );
  imodPointAppendXYZ( cont, center.x,     center.y,      slice );
}

//------------------------
//-- Adds points in the shape of a cross to contour

void cont_addDiamond( Icont *cont, Ipoint center, float size, int slice )
{
  float h = size/2.0;
  imodPointAppendXYZ( cont, center.x-h,   center.y,      slice );
  imodPointAppendXYZ( cont, center.x,     center.y-h,    slice );
  imodPointAppendXYZ( cont, center.x+h,   center.y,      slice );
  imodPointAppendXYZ( cont, center.x,     center.y+h,    slice );
  imodPointAppendXYZ( cont, center.x-h,   center.y,      slice );
}

//------------------------
//-- Adds points in the shape of a cross to contour

void cont_addArrow( Icont *cont, Ipoint from, Ipoint to, float size, int slice )
{
  float h = size/2.0;
  imodPointAppendXYZ( cont, from.x,   from.y,     slice );
  imodPointAppendXYZ( cont, to.x,     to.y,         slice );
  
  float lineLen = line_distBetweenPts2D( &to, &from );
  if(lineLen == 0)
    return;
  
  float fractAlong = size / lineLen;
  Ipoint arrow = line_findPtFractBetweenPts2D( &to, &from, fractAlong );
  point_rotatePointAroundPoint2D( &arrow, &to, 40*DEGS_TO_RADS );
  imodPointAppendXYZ( cont, arrow.x,   arrow.y,      slice );
  imodPointAppendXYZ( cont, to.x,      to.y,         slice );
  point_rotatePointAroundPoint2D( &arrow, &to, -80*DEGS_TO_RADS );
  imodPointAppendXYZ( cont, arrow.x,   arrow.y,      slice );
}

//------------------------
//-- Adds points in the shape of a cross to contour

void cont_makeContShowingMissingPoints( Icont *to, Icont *from, int slice, float radius )
{
  for(int p=0; p<psize(from); p++)  // for each point: draw little verticle line
  {
    imodPointAppendXYZ( to, getPt(from,p)->x, getPt(from,p)->y, slice );
    imodPointAppendXYZ( to, getPt(from,p)->x, getPt(from,p)->y+radius, slice );
    imodPointAppendXYZ( to, getPt(from,p)->x, getPt(from,p)->y-radius, slice );
    imodPointAppendXYZ( to, getPt(from,p)->x, getPt(from,p)->y, slice );
    
    if( getPtZInt(from,p+1) != getPtZInt(from,p) + 1  )
      imodPointAppendXYZ( to, getPt(from,p)->x, getPt(from,p)->y, -1 );
  }
  imodPointAppendXYZ( to, 0, 0, -1 );
  
  for( int z=0; z<plug.zsize; z++)    // for each slice:
  {
    if( !bead_isPtOnSlice(from,z) )     // if missin point:
    {                                     // draw a cross at it's expected poistion
      Ipoint expectedPt;
      if ( bead_getExpectedPosOfPoint( from, z, &expectedPt ) )
      {
        cont_addCross( to, expectedPt, radius*3.0, slice );
        imodPointAppendXYZ( to, expectedPt.x, expectedPt.y, -1 );
      }
    }
  }
}



//------------------------
//-- Accesses the extra object and draw a red deform circle and/or other
//-- reference contour at the last recorded position of the mouse. What is
//-- drawn depends on what drawing mode is selected.

bool BeadHelper::drawExtraObject( bool redraw, int drawflag, int slice )
{
  Iobj *xobjE = ivwGetAnExtraObject(plug.view, plug.extraObjExpPos);
  Iobj *xobjC = ivwGetAnExtraObject(plug.view, plug.extraObjContDisp);
  Iobj *xobjT = ivwGetAnExtraObject(plug.view, plug.extraObjTiltAxis);
  
  if ( !plug.window || !xobjC || !xobjE || !xobjT )
    return 0;
  
  //## CLEAR EXTRA OBJECTS:
  
  ivwClearAnExtraObject( plug.view, plug.extraObjContDisp );
  ivwClearAnExtraObject( plug.view, plug.extraObjExpPos );
  ivwClearAnExtraObject( plug.view, plug.extraObjTiltAxis );
  
  
  //## GET Z VALUE:
  
  int ix, iy,iz;
  ivwGetLocation(plug.view, &ix, &iy, &iz);
  plug.mouse.z = iz;
  
  float x = plug.mouse.x;
  float y = plug.mouse.y;
  float z = plug.mouse.z;
  
  float zapZoom = 1.0f;
  ivwGetTopZapZoom(plug.view, &zapZoom);
  float sc = fDivide( 1.0f, zapZoom);
  
  Imod *imod = ivwGetModel(plug.view);
  Iobj *obj = imodObjectGet(imod);
  Icont *cont = getCurrCont();
  
  
  //## IF SHOW EXPECTED POSITION: DRAW CROSSHAIR AT EXPECTED POSITION
  
  if( plug.showExpectedPos && isCurrContValid() )
  {
    Ipoint ptEst;
    bool success = bead_getExpectedPosOfPoint( cont, z, &ptEst );
    
    if(success)
    {
      Icont *xcont = imodContourNew();
      
      if( bead_isPtOnSlice(cont, z) )
      {
        if( plug.expPtDisplayType == ED_DIAMOND )
          cont_addDiamond( xcont, ptEst, plug.expPtSize*sc, z );
        else if( plug.expPtDisplayType == ED_CROSS )
          cont_addCross( xcont, ptEst, plug.expPtSize*sc, z );
        else {
          Ipoint *pt = bead_getPtOnSlice(cont, z);
          cont_addArrow( xcont, *pt, ptEst, plug.expPtSize*sc, z );
        }
      }
      else
      {
        if( plug.expPtDisplayType == ED_DIAMOND )
          cont_addDiamond( xcont, ptEst, 2*plug.expPtSize*sc, z );
        else
          cont_addCross( xcont, ptEst, 2*plug.expPtSize*sc, z );
      }
      imodContourSetFlag(xcont, ICONT_DRAW_ALLZ, 1);
      imodObjectAddContour(xobjE, xcont);
    }
  }
  
  //## DRAW APPROPRIATE LINE DISPLAY:
  
  switch ( plug.lineDisplayType )
  {
    case( LD_CURRENT ):
    {
      if( isCurrContValid() )
      {
        Icont *xcont = imodContourDup( getCurrCont() );
        changeZValue( xcont, z );
        imodContourSetFlag(xcont, ICONT_DRAW_ALLZ, 1);
        imodObjectAddContour(xobjC, xcont);
      }
    }break;
    
    case( LD_CURRMISSING ):
    {
      if( isCurrContValid() )
      {
        ivwClearAnExtraObject(plug.view, plug.extraObjContDisp);
        Icont *xcont = imodContourNew();
        cont_makeContShowingMissingPoints( xcont, getCurrCont(), z, sc*2 );
        
        imodContourSetFlag(xcont, ICONT_DRAW_ALLZ, 1);
        imodObjectAddContour(xobjC, xcont);
      }
    }break;
      
      
    case( LD_ALL ):
    {
        ivwClearAnExtraObject(plug.view, plug.extraObjContDisp);
        for(int c=0; c<imodObjectGetMaxContour(obj);c++)
        {
          Icont *xcont = imodContourDup( getCont(obj,c) );
          changeZValue( xcont, z );
          imodContourSetFlag(xcont, ICONT_DRAW_ALLZ | ICONT_MMODEL_ONLY, 1);
          imodObjectAddContour(xobjC, xcont);
        }
    }break;
    
    case( LD_SLICE_RESID ):
    {
      if( isCurrContValid() )
      {
        for(int p=0; p<psize(cont); p++)
        {
          Ipoint *pt    = getPt(cont,p);
          Ipoint ptEst;
          if( bead_getExpectedPosOfPoint(cont, getPtZInt(cont,p), &ptEst) )
          {
            Icont *xcont  = imodContourNew();
            Ipoint resid  = line_findPtFractBetweenPts2D( pt, &ptEst, 1.0 );
            imodPointAppendXYZ( xcont, pt->x, pt->y, z );
            imodPointAppendXYZ( xcont, resid.x, resid.y, z );
            imodContourSetFlag(xcont, ICONT_DRAW_ALLZ, 1);
            imodObjectAddContour(xobjC, xcont);
          }
        }
      }
    } break;
    
    case( LD_BEST_FIT ):
    {
      imodObjectSetColor(xobjC, 1,1,1);
      
      if( isCurrContValid() )
      {
        float gradient, offset;
        bool success = bead_calcLineOfBestFit( cont, &gradient, &offset );
        if(success)
        {
          Icont *xcont = imodContourNew();
          imodPointAppendXYZ( xcont, 0, (offset), z );
          imodPointAppendXYZ( xcont, plug.xsize, (gradient*plug.xsize + offset), z );
          imodContourSetFlag( xcont, ICONT_STIPPLED | ICONT_DRAW_ALLZ, 1 );
          imodObjectAddContour( xobjC, xcont );
        }
      }
    } break;
  }
  
  //## DRAW TILT AXIS:
  
  if ( plug.tiltDisplayType != TD_OFF )
  {
    Icont *xcontT = imodContourNew();
    float gradientP = tan( (plug.tiltAngle)*DEGS_TO_RADS );
    // calculate gradient perpendicular to the tilt axis
    
    float offsetX = plug.middlePt.x + plug.tiltOffsetX;
    float startX   = offsetX + ((plug.ysize/2.0)*gradientP);
    float endX     = offsetX - ((plug.ysize/2.0)*gradientP);
    
    imodPointAppendXYZ( xcontT, startX, 0, z );
    imodPointAppendXYZ( xcontT, endX, plug.ysize, z );
    imodContourSetFlag( xcontT, ICONT_DRAW_ALLZ, 1 );
    imodObjectAddContour( xobjT, xcontT );
    
    if( plug.tiltDisplayType == TD_TILTAXISPT )
    {
      if( cont && bead_isPtOnSlice( cont, plug.middleSlice) )
      {
        Icont *xcontP = imodContourNew();
        Ipoint *pt = bead_getPtOnSlice( cont, plug.middleSlice );
        float startYP   = pt->y - ((pt->x - 0)*gradientP);
        float endYP     = pt->y + ((plug.xsize - pt->x)*gradientP);
        imodPointAppendXYZ( xcontP, 0, startYP, z );
        imodPointAppendXYZ( xcontP, plug.xsize, endYP, z );
        imodContourSetFlag( xcontP, ICONT_STIPPLED | ICONT_DRAW_ALLZ, 1 );
        imodObjectAddContour( xobjT, xcontP );
      }
      else if( isCurrPtValid() )
      {
        Icont *xcontP = imodContourNew();
        Ipoint *pt = getCurrPt();
        float startYP   = pt->y - ((pt->x - 0)*gradientP);
        float endYP     = pt->y + ((plug.xsize - pt->x)*gradientP);
        imodPointAppendXYZ( xcontP, 0, startYP, z );
        imodPointAppendXYZ( xcontP, plug.xsize, endYP, z );
        imodContourSetFlag( xcontP, ICONT_STIPPLED | ICONT_DRAW_ALLZ, 1 );
        imodObjectAddContour( xobjT, xcontP );
      }
    }
  }
  
  
  if( redraw )
    ivwDraw( plug.view, 0 );
  return true;
}



  
//------------------------
//-- Clears all the contents of the extra object.

void BeadHelper::clearExtraObj()
{
  Iobj *obj = ivwGetExtraObject(plug.view);
  int ncont = imodObjectGetMaxContour(obj);
  if (!ncont)
    return;
  
  Icont *cont = getCont(obj, 0);
  for (int co = ncont - 1; co >= 0; co--)
    imodObjectRemoveContour(obj, co);
  imodContoursDelete(cont, ncont);
}


//------------------------
//-- Loads most of the settings for BeadHelperData from "beadhelpersettings.txt"

void BeadHelper::loadSettings()
{
  
  vector<string> file = file_loadTextFromFile("beadhelpersettings.txt");
  for( int i=0; i<file.size(); i++ )
  {
    string l = file[i];
    if( l.length() <= 1 )
      continue;
    
    string value = string_explodeGetArgument( l, " ", 2, true );
    float valueF = string_getFloatFromString( value );
    int   valueI = int(valueF);
    bool  valueB = !(valueI == 0); 
    
    if (string_startsWith(l, "showExpectedPos "))   plug.showExpectedPos = valueB;
    if (string_startsWith(l, "wheelBehav "))        plug.wheelBehav = valueI;
    if (string_startsWith(l, "estPosMethod "))      plug.estPosMethod = valueI;
    if (string_startsWith(l, "showSpheres "))       plug.showSpheres = valueB;
    if (string_startsWith(l, "sphereSize "))        plug.sphereSize = valueI;
    if (string_startsWith(l, "lineDisplayType "))   plug.lineDisplayType = valueI;
    if (string_startsWith(l, "tiltDisplayType "))   plug.tiltDisplayType = valueI;
    if (string_startsWith(l, "tiltAngle "))         plug.tiltAngle = valueF;
    if (string_startsWith(l, "biggestHoleGrid "))   plug.biggestHoleGrid = valueI;
    if (string_startsWith(l, "wheelResistance "))   plug.wheelResistance = valueI;
    if (string_startsWith(l, "expPtDisplayType "))  plug.expPtDisplayType = valueI;
    if (string_startsWith(l, "expPtSize "))         plug.expPtSize = valueI;
    if (string_startsWith(l, "autoSaveSettings "))  plug.autoSaveSettings = valueI;
  }
}


//------------------------
//-- Saves most of the settings within BeadHelperData to "beadhelpersettings.txt"
//-- so they will load next time Bead Helper is started

void BeadHelper::saveSettings()
{
  wprint("SAVING SETTINGS");
  
  string text;
  
  text += "showExpectedPos "      + toString( plug.showExpectedPos ) + "\n";
  text += "wheelBehav "           + toString( plug.wheelBehav )+ "\n";
  text += "estPosMethod "         + toString( plug.estPosMethod )+ "\n";
  text += "showSpheres "          + toString( plug.showSpheres )+ "\n";
  text += "sphereSize "           + toString( plug.sphereSize )+ "\n";
  text += "lineDisplayType "      + toString( plug.lineDisplayType )+ "\n";
  text += "tiltDisplayType "      + toString( plug.tiltDisplayType )+ "\n";
  text += "\n";
  text += "tiltAngle "            + toString( plug.tiltAngle )+ "\n";
  text += "tiltOffsetX "          + toString( plug.tiltOffsetX )+ "\n";
  text += "biggestHoleGrid "      + toString( plug.biggestHoleGrid )+ "\n";
  text += "wheelResistance "      + toString( plug.wheelResistance )+ "\n";
  text += "expPtDisplayType "     + toString( plug.expPtDisplayType )+ "\n";
  text += "expPtSize "            + toString( plug.expPtSize )+ "\n";
  text += "autoSaveSettings "     + toString( plug.autoSaveSettings )+ "\n";
  
  bool success =  file_saveStringToFile("beadhelpersettings.txt", text )+ "\n";
}




//------------------------
//-- Deletes all points within the specified range of views from the specified range
//-- of contours.

void BeadHelper::deletePtsInRange()
{
  if( !updateAndVerifyRanges() )
    return;
  
  //## DISPLAY YES/NO TEXTBOX GIVING USER OPTION TO CANCEL:
  
  int numConts  = (plug.contMax - plug.contMin) + 1;
  int numSlices = (plug.sliceMax - plug.sliceMin) + 1;
  int numPts = numConts * numSlices;
  string incMiddleSlice = isBetweenAsc(plug.sliceMin,plug.middleSlice,plug.sliceMax) ?
    "\n\nTHIS INCLUDES THE MIDDLE SLICE!\n" : "";
  if( numPts > 10 )
  {
    string msg = "This operation will delete ALL points on "
    + toString(numConts) + " contours ("
    + toString(plug.contMin+1) + "-" + toString(plug.contMax+1)
    + ") on " + toString(numSlices) + " slices ("
    + toString(plug.sliceMin+1) + "-" + toString(plug.sliceMax+1) + ")."
    + incMiddleSlice 
    + "\nAre you sure you want to continue ?";
    
    if( !MsgBoxYesNo( this, msg ) )
      return;
  }
  
  //## DELETE ALL POINTS IN RANGE:
  
  Imod *imod = ivwGetModel(plug.view);
  Iobj *obj = imodObjectGet(imod);
  int objIdx, contIdx, ptIdx;
  imodGetIndex(imod, &objIdx, &contIdx, &ptIdx);
  
  int numPtsDeleted = 0;
  
  for( int c=MAX(plug.contMin,0); c<MIN(plug.contMax,imodObjectGetMaxContour(obj)); c++)
  {
    imodSetIndex(imod, objIdx, c, 0);
    undoContourDataChgCC( plug.view );      // REGISTER UNDO
    
    Icont *cont = getCont( obj, c );
    
    for( int p=0; p<psize(cont); p++ )
    {
      if( isBetweenAsc( plug.sliceMin, getPtZInt(cont,p), plug.sliceMax ) )
      {
        imodPointDelete( cont, p );
        p--;
        numPtsDeleted++;
      }
    }
  }
  
  if(numPtsDeleted)
    undoFinishUnit( plug.view );          // FINISH UNDO
  
  imodSetIndex(imod, objIdx, contIdx, ptIdx);
  
  wprint("Deleted %d points\n", numPtsDeleted);
  ivwRedraw( plug.view );
}

//------------------------
//-- Deletes all points within the current contour from the specified range
//-- of views.

void BeadHelper::deletePtsCurrContInRange()
{
  if( !updateAndVerifyRanges() || !isCurrContValid() )
    return;
  
  int numPtsDeleted = 0;
  
  undoContourDataChgCC( plug.view );      // REGISTER UNDO
  Icont *cont = getCurrCont();
  
  for( int p=0; p<psize(cont); p++ )
  {
    if( isBetweenAsc( plug.sliceMin, getPtZInt(cont,p), plug.sliceMax ) )
    {
      imodPointDelete( cont, p );
      p--;
      numPtsDeleted++;
    }
  }
  
  if(numPtsDeleted)
   undoFinishUnit( plug.view );           // FINISH UNDO
    
  wprint("Deleted %d points\n", numPtsDeleted);
  ivwRedraw( plug.view );
}

//------------------------
//-- Deletes all points within the current contour from the current point to the
//-- nearest end inclusive

void BeadHelper::deletePtsCurrContToNearestEnd( bool inclusive )
{
  if( !updateAndVerifyRanges() || !isCurrPtValid() )
    return;
  
  undoContourDataChgCC( plug.view );      // REGISTER UNDO
  Icont *cont = getCurrCont();
  
  Imod *imod = ivwGetModel(plug.view);
  int objIdx, contIdx, ptIdx;
  imodGetIndex(imod, &objIdx, &contIdx, &ptIdx);
  
  int numPts = psize( cont );
  int middlePt = numPts / 2.0f;
  int numPtsDeleted;
  
  if ( ptIdx >= middlePt )    // if closest to end pt: delete pts to end
  {
    if(!inclusive)
      ptIdx++;
    numPtsDeleted = numPts - ptIdx;
    for( int p=ptIdx; p<psize(cont); )
      imodPointDelete( cont, ptIdx );
  }
  else                        // (else) if closest to start pt: delete pts from start
  {
    if(!inclusive)
      ptIdx--;
    numPtsDeleted = ptIdx+1;
    for( int p=0; p<numPtsDeleted; p++ )
      imodPointDelete( cont, 0 );
    imodSetIndex( imod, objIdx, contIdx, 0 );
  }
  
  if(numPtsDeleted)
    undoFinishUnit( plug.view );        // FINISH UNDO
  
  wprint("Deleted %d points\n", numPtsDeleted);
  ivwRedraw( plug.view );
}


//------------------------
//-- Deletes all points from each contour in range except the point in
//-- the middle slice - effectively reducing each contour to its "seed"

void BeadHelper::reduceContsToSeed()
{
  if( !updateAndVerifyRanges() )
    return;
  
  Imod *imod = ivwGetModel(plug.view);
  Iobj *obj  = getCurrObj();
  int objIdx, contIdx, ptIdx;
  imodGetIndex(imod, &objIdx, &contIdx, &ptIdx);
  int numContsReduced = 0;
  
  for( int c=MAX(plug.contMin,0); c<MIN(plug.contMax,imodObjectGetMaxContour(obj)); c++)
  {
    imodSetIndex(imod, objIdx, c, 0);
    Icont *cont = getCont( obj, c );
    
    if( psize(cont) > 1 && bead_isPtOnSlice(cont,plug.middleSlice) )
    {
      Ipoint *pt = bead_getPtOnSlice(cont,plug.middleSlice);
      Ipoint seedPt = *pt;
      int numPtsToDel = psize(cont) - 1;
      undoContourDataChgCC( plug.view );      // REGISTER UNDO
      deleteAllPts(cont);
      imodPointAppend( cont, &seedPt );
      numContsReduced++;
    }
  }
  
  imodSetIndex(imod, objIdx, contIdx, 0);
  undoFinishUnit( plug.view );                // FINISH UNDO
  ivwRedraw( plug.view );
}


//------------------------
//-- Deletes all points from the current contour except the point in the middle slice - 
//-- effectively reducing it to its "seed" - and returns the number of points removed

void BeadHelper::reduceCurrContToSeed()
{
  if( !isCurrContValid() || isEmpty(getCurrCont()) )
    return;
  
  Icont *cont = getCurrCont();
  
  if( psize(cont) == 1 )
  {
    wprint("ERROR: Contour only has one point\n");
  }
  if ( bead_isPtOnSlice(cont,plug.middleSlice) )
  {
    Ipoint *pt = bead_getPtOnSlice(cont,plug.middleSlice);
    Ipoint seedPt = *pt;
    int numPtsToDel = psize(cont) - 1;
    
    undoContourDataChgCC( plug.view );      // REGISTER UNDO
    deleteAllPts(cont);
    imodPointAppend( cont, &seedPt );
    undoFinishUnit( plug.view );            // FINISH UNDO
    
    ivwRedraw( plug.view );
  }
  else
  {
    wprint("ERROR: Current contour is missing middle point\n");
  }
}


//------------------------
//-- Moves the point in the current contour on the current slice to it's 
//-- expected position, based on the position of the points before and/or after it.
//-- Note that if there is no point on the current view a new point is added.

void BeadHelper::movePtsToExstimatedPos()
{
  if( !updateAndVerifyRanges() )
    return;
  
  
  //## DISPLAY YES/NO TEXTBOX GIVING USER OPTION TO CANCEL:
  
  int numConts  = (plug.contMax - plug.contMin) + 1;
  int numSlices = (plug.sliceMax - plug.sliceMin) + 1;
  int numPts = numConts * numSlices;
  if( numPts > 10 )
  {
    string msg = "This operation will move ALL points on "
                 + toString(numConts) + " contours ("
                 + toString(plug.contMin+1) + "-" + toString(plug.contMax+1)
                 + ") on " + toString(numSlices) + " slices ("
                 + toString(plug.sliceMin+1) + "-" + toString(plug.sliceMax+1) + ")."
                 + "\nTotal points = " + toString(numPts)
                 + "\nAre you sure you want to continue ?";
    if( !MsgBoxYesNo( this, msg ) )
      return;
  }
  
  //## ITERATE THROUGH SPECIFIED RANGE OF CONTOURS AND MOVE ALL POINTS WITHIN
  //## SPECIFIED VIEWS TO THEIR EXPECTED POSITION: 
  
  int currSlice = edit_getZOfTopZap();
  Imod *imod = ivwGetModel(plug.view);
  Iobj *obj  = getCurrObj();
  int objIdx, contIdx, ptIdx;
  imodGetIndex(imod, &objIdx, &contIdx, &ptIdx);
  
  int numPtsMoved = 0;
  int numPtsAdded = 0;
  
  for( int c=MAX(plug.contMin,0); c<MIN(plug.contMax,imodObjectGetMaxContour(obj)); c++)
  {
    imodSetIndex(imod, objIdx, c, 0);
    Icont *cont = getCont( obj, c );
    int numPtsBefore = psize(cont);
    
    for( int z=plug.sliceMin; z<=plug.sliceMax; z++)
    {
      undoContourDataChg( plug.view, objIdx, c );       // REGISTER UNDO
      
      if( bead_insertPtAtEstimatedPos( cont, z, true ) )
        numPtsMoved++;
    }
    int numPtsAddedCont = psize(cont) - numPtsBefore;
    numPtsMoved -= numPtsAddedCont;
    numPtsAdded += numPtsAddedCont;
  }
  
  if( numPtsMoved || numPtsAdded )                      // FINISH UNDO
    undoFinishUnit( plug.view );
  
  //## OUTPUT RESULTS:
  
  wprint( "Moved %d points and added %d points\n", numPtsMoved, numPtsAdded );
  imodSetIndex(imod, objIdx, contIdx, ptIdx);
  ivwRedraw( plug.view );
}

//------------------------
//-- Moves the point in the current contour on the current slice to it's 
//-- expected position, based on the position of the points before and/or after it.
//-- Note that if there is no point on the current view a new point is added.

void BeadHelper::movePtToExstimatedPosCurrCont()
{
  if( !isCurrObjValid() )
    return;
  
  Ipoint expectedPt;
  int currSlice = edit_getZOfTopZap();
  Imod *imod = ivwGetModel(plug.view);
  Icont *cont = getCurrCont();
  int objIdx, contIdx, ptIdx;
  imodGetIndex(imod, &objIdx, &contIdx, &ptIdx);
  
  if( bead_getExpectedPosOfPoint( cont, currSlice, &expectedPt ) )
  {
    undoContourDataChgCC( plug.view );      // REGISTER UNDO
    ptIdx = bead_insertOrOverwritePoint( cont, &expectedPt );
    undoFinishUnit( plug.view );            // FINISH UNDO
  }
  
  imodSetIndex(imod, objIdx, contIdx, ptIdx);
  ivwRedraw( plug.view );
}

//------------------------
//-- Fills any missing points in the specified range of views for the specified
//-- contours.

void BeadHelper::fillMissingPts()
{
  if( !updateAndVerifyRanges() )
    return;
  
  Imod *imod = ivwGetModel(plug.view);
  Iobj *obj  = getCurrObj();
  int objIdx, contIdx, ptIdx;
  imodGetIndex(imod, &objIdx, &contIdx, &ptIdx);
  
  int numContsChanged = 0;
  int numPtsAddedTotal = 0;
  
  for( int c=MAX(plug.contMin,0); c<MIN(plug.contMax,imodObjectGetMaxContour(obj)); c++)
  {
    imodSetIndex(imod, objIdx, c, 0);
    Icont *cont = getCont( obj, c );
    
    undoContourDataChgCC( plug.view );      // REGISTER UNDO
    int numPtsAdded = bead_fillMissingPtsOnCont( getCurrCont(),
                                                 plug.sliceMin, plug.sliceMax );
    
    numPtsAddedTotal += numPtsAdded;
    if( numPtsAdded )
      numContsChanged++;
  }
  
  if( numPtsAddedTotal )
    undoFinishUnit( plug.view );            // FINISHED UNDO
  
  imodSetIndex(imod, objIdx, contIdx, ptIdx);
  wprint("Changed %d contours\n", numContsChanged);
  wprint("Added %d points\n", numPtsAddedTotal);
  ivwRedraw( plug.view );
}


//------------------------
//-- Fills any missing points in the current contour over all views.

void BeadHelper::fillMissingPtsCurrCont()
{
  if( !isCurrContValid() )
    return;
  
  undoContourDataChgCC( plug.view );      // REGISTER UNDO
  int numPtsAdded = bead_fillMissingPtsOnCont( getCurrCont(), 0, plug.zsize-1 );
  if( numPtsAdded )
    undoFinishUnit( plug.view );          // FINISHED UNDO
  
  wprint("Added %d points to contour\n", numPtsAdded);
  ivwRedraw( plug.view );
}



//------------------------
//-- Gives a choice of several other options for the user.

void BeadHelper::otherActions()
{
  updateAndVerifyRanges();
  
  static int action = 0;
  
  //## GET USER INPUT FROM CUSTOM DIALOG:
  
	CustomDialog ds;
  int ID_ACTION = ds.addRadioGrp( "action:",
                                  "calculate tilt angle,"
                                  "smooth points,"
                                  "show fiducials on bottom as purple,"
                                  "show contour turning points,"
                                  "copy points to memory", action );
	GuiDialogCustomizable dlg(&ds, "Perform Action", this);
	dlg.exec();
	if( ds.cancelled )
		return;
	action            = ds.getResultRadioGrp	( ID_ACTION );
  
  switch(action)
  {
    case(0):      // calculate tilt angle
    {
      float tiltAngleEst = bead_estimateTiltAngle();
      if(tiltAngleEst == 0)
      {
        MsgBox( "Was unable to measure any angles based on contours provided" );
      }
      else
      {
        string msg = "Current tilt angle = " + toString( plug.tiltAngle)
        + "\nEstimated tilt angle = " + toString(tiltAngleEst)
        + "\n... Change tilt angle to this value?";
        if( MsgBoxYesNo( this, msg ) )
          plug.tiltAngle = tiltAngleEst;
      }
    } break;
      
    case(1):      // smooth points
    {
      smoothPtsInRange();
    } break;
    
    case(2):      // show fiducials on top as stippled
    {
      //bead_showBottomContoursInPurple();
      bead_showBottomContoursStippledUsingDirection();
    } break;
    
      
    
    case(3):      // show contour turning points
    {
      bead_showContourTurningPts();
    } break;
  }
}

//------------------------
//-- Allows user to change other plugin values/settings.

void BeadHelper::otherSettings()
{
  //## GET USER INPUT FROM CUSTOM DIALOG:
  
	CustomDialog ds;
  int ID_TILTANGLE      = ds.addLineEdit( "tilt angle:",
                                          toString(plug.tiltAngle).c_str() );
  int ID_TILTOFFSET     = ds.addSpinBox ( "tilt x offset:", -200, 200,
                                          plug.tiltOffsetX, 1,
                                          "How far the tilt axis is shifted along X "
                                          "from passing through the center" );
  int ID_BIGGESTHOLES   = ds.addSpinBox ( "biggest hole grid size:",
                                          1, 1000, plug.biggestHoleGrid, 5,
                                          "The distance between grid points used to "
                                          "search for the point furthest from any "
                                          "seed points and the edge" );
  int ID_MIDDLESLICE    = ds.addSpinBox ( "middle (seed) view:",
                                          1, plug.zsize+1, plug.middleSlice+1, 1,
                                          "The view used to seed points - this is "
                                          "usually the middle-most view, but not "
                                          "in all cases" );
  int ID_WHEELRESIST    = ds.addSpinBox ( "wheel resistance:",
                                          10, 1000, plug.wheelResistance, 10,
                                          "the higher the value, the slower "
                                          "mouse scrolling works" );
  int ID_EXPPTDISPLAY   = ds.addComboBox( "expected pt display:",
                                          "cross,"
                                          "diamond,"
                                          "arrow", plug.expPtDisplayType,
                                           "Garry is gay  :-)" );
  int ID_EXPPTSIZE      = ds.addSpinBox ( "expected pt size:",
                                           1, 200, plug.expPtSize, 1 );
  int ID_AUTOSAVE       = ds.addCheckBox( "save settings on close", 
                                          plug.autoSaveSettings,
                                          "automatically saves your Bead Helper "
                                          "settings to 'beadhelpersettings.txt' "
                                          "when you close 3dmod, so they will load "
                                          "next time you open 3dmod");
  
	GuiDialogCustomizable dlg(&ds, "Other Settings", this);
	dlg.exec();
	if( ds.cancelled )
		return;
	string tiltAngleStr        = ds.getResultLineEdit	( ID_TILTANGLE );
  plug.tiltOffsetX           = ds.getResultSpinBox  ( ID_TILTOFFSET );
  plug.biggestHoleGrid       = ds.getResultSpinBox  ( ID_BIGGESTHOLES );
  plug.middleSlice           = ds.getResultSpinBox  ( ID_MIDDLESLICE ) - 1;
  plug.wheelResistance       = ds.getResultSpinBox  ( ID_WHEELRESIST );
  plug.expPtDisplayType      = ds.getResultComboBox ( ID_EXPPTDISPLAY );
  plug.expPtSize             = ds.getResultSpinBox  ( ID_EXPPTSIZE );
  plug.autoSaveSettings      = ds.getResultCheckBox ( ID_AUTOSAVE );
  
  float newTiltAngle = string_getFloatFromString( tiltAngleStr );
  if( newTiltAngle < -200 || newTiltAngle >200 )
    wprint("\aERROR: Invalid tilt angle entered"); 
  else
    plug.tiltAngle = newTiltAngle;
  
  ivwRedraw( plug.view );
}


//------------------------
//-- Smooths points on the specified views for the specified contours.

void BeadHelper::smoothPtsInRange()
{
  updateAndVerifyRanges();
  
  //## GET USER INPUT FROM CUSTOM DIALOG:
  
  int numConts  = (plug.contMax - plug.contMin) + 1;
  int numSlices = (plug.sliceMax - plug.sliceMin) + 1;
  int numPts = numConts * numSlices;
  string msg = "Range: "
    + toString(numConts) + " contours ("
    + toString(plug.contMin+1) + "-" + toString(plug.contMax+1)
    + ") on " + toString(numSlices) + " slices ("
    + toString(plug.sliceMin+1) + "-" + toString(plug.sliceMax+1) + ")."
    + "\nTotal points = " + toString(numPts);
  
	CustomDialog ds;
  int ID_DUMMY          = ds.addLabel   ( msg.c_str() );
  int ID_SMOOTHCRITERIA = ds.addRadioGrp( "smooth using:",
                                          "catmull rom on 3 key points,"
                                          "rotation angle", 0 );
  int ID_YAXISONLY      = ds.addCheckBox( "change y value only", true );
  int ID_ITERATIONS     = ds.addSpinBox ( "iterations:", 1, 10, plug.contMin, 1,
                                          "The more iterations, the further points "
                                          "will be moved" );
	GuiDialogCustomizable dlg(&ds, "Smoothing Options", this);
	dlg.exec();
	if( ds.cancelled )
		return;
	int smoothCriteria  = ds.getResultRadioGrp	( ID_SMOOTHCRITERIA );
  bool yAxisOnly      = ds.getResultCheckBox  ( ID_YAXISONLY );
  int iterations      = ds.getResultSpinBox   ( ID_ITERATIONS );
  
  bead_smoothConts( smoothCriteria, yAxisOnly, iterations, true );
  
  ivwRedraw( plug.view );
}

//------------------------
//-- Prompts for a criteria for reordering, and reorderes specified range of contours
//-- using this criteria.

void BeadHelper::reorderContours()
{
  updateAndVerifyRanges();
  
  //## GET USER INPUT FROM CUSTOM DIALOG:
  
	CustomDialog ds;
  int ID_CONTMIN       = ds.addSpinBox ( "starting at:", 1, 10, plug.contMin, 1,
                          "only contours AFTER this contour will be reordered" );
	int ID_SORTCRITERIA  = ds.addRadioGrp( "sort by:",
                          "deviation (asc),"
                          "avg grey value (desc),"
                          "dist from middle (asc),"
                          "num missing pts (asc),"
                          "random,", 0 );
	int ID_REVERSE       = ds.addCheckBox( "reverse order", false );
  int ID_PRINTVALS     = ds.addCheckBox( "print values", true );
	GuiDialogCustomizable dlg(&ds, "Sorting Options", this);
	dlg.exec();
	if( ds.cancelled )
		return;
  int contMin         = ds.getResultSpinBox   ( ID_CONTMIN );
  int reverseOrder    = ds.getResultCheckBox  ( ID_REVERSE );
  int printVals       = ds.getResultCheckBox  ( ID_PRINTVALS );
	int sortCriteria		= ds.getResultRadioGrp	( ID_SORTCRITERIA );
  
  bead_reorderConts( sortCriteria, contMin-1, reverseOrder, printVals );
  
  ivwRedraw( plug.view );
}


//------------------------
//-- Moves the current contour to a different index within the current object.
//-- Prompts the user what position to move the contour.

void BeadHelper::moveContour()
{
  if( !isCurrContValid() )
    return;
  
  Imod *imod  = ivwGetModel(plug.view);
  Iobj *obj   = getCurrObj();
  Icont *cont = getCurrCont();
  int objIdx, contIdx, ptIdx;
  imodGetIndex(imod, &objIdx, &contIdx, &ptIdx);
  
  int lastCont = imodObjectGetMaxContour( obj );
  bool ok;
  int newContIdx = QInputDialog::getInteger( "Move contour", "Move to:",
                                             lastCont, 1, lastCont, 1, &ok, this ) - 1;
  if ( ok && contIdx != newContIdx )
  {
    Icont *contCopy = imodContourDup( cont );
    undoContourRemovalCO( plug.view, contIdx );                 // REGISTER UNDO
    imodObjectRemoveContour( obj, contIdx );
    undoContourAddition( plug.view, objIdx, newContIdx );       // REGISTER UNDO
    imodObjectInsertContour( obj, contCopy, newContIdx );
    undoFinishUnit( plug.view );                                // FINISH UNDO
    imodSetIndex(imod, objIdx, newContIdx, ptIdx);
    ivwRedraw( plug.view );
  }
}


//------------------------
//-- Updates the range values and corrects any returns true if a 
//-- valid contour and slice range has been provided.
//-- Otherwise displays error message and returns false.

bool BeadHelper::updateAndVerifyRanges()
{
  if( !isCurrObjValid() )
  {
    MsgBox("You have not selected a valid object");
    return false;
  }
  
  plug.sliceMin = sliceMinSpinner->value() - 1;
  plug.sliceMax = sliceMaxSpinner->value() - 1;
  plug.contMin = contMinSpinner->value() - 1;
  plug.contMax = contMaxSpinner->value() - 1;
  
  int maxContIdx = imodObjectGetMaxContour( getCurrObj() )-1;
  plug.contMax = MIN( plug.contMax, maxContIdx );
  
  if ( plug.contMin > plug.contMax  )
  {
    MsgBox("\aBad contour range");
    contMinSpinner->setValue( plug.contMax+1 );
    return false;
  }
  if ( plug.sliceMin > plug.sliceMax )
  {
    MsgBox("\aBad slice range");
    sliceMinSpinner->setValue( plug.sliceMax+1 );
    return false;
  }
  
  return true;
}


//------------------------
//-- Advances the currently selected point within the contour by the specified amount.

bool BeadHelper::advanceSelectedPointInCurrCont( int change )
{
  if( !isCurrObjValid() || isEmpty( getCurrCont() ) )
    return false;
  
  Imod *imod = ivwGetModel(plug.view);
  Icont *cont = getCurrCont();
  int objIdx, contIdx, ptIdx;
  imodGetIndex(imod, &objIdx, &contIdx, &ptIdx);
  
  ptIdx += change;
  keepWithinRange( ptIdx, 0, psize(cont)-1 );
  
  imodSetIndex(imod, objIdx, contIdx, ptIdx);
}


//------------------------
//-- Method used for testing new routines.

void BeadHelper::test()
{  
  Icont *cont = getCurrCont();
  
  if( !isContValid(cont) )
  {
    wprint("Have not selected valid contour\n");
    return;
  }
  
  
  //float a, b, c;
  //bead_calcQuadraticCurve( *getPt(cont,0), *getPt(cont,1), *getPt(cont,2), &a, &b, &c );
  
  int topSlice = edit_getZOfTopZap();
  Imod *imod = ivwGetModel(plug.view);
  Iobj *obj = imodObjectGet(imod);
  int objIdx, contIdx, ptIdx;
  imodGetIndex(imod, &objIdx, &contIdx, &ptIdx);
  float z = (getPt(cont,ptIdx)->z);
  
  
  
  Icont *ptsByDist = imodContourNew();
  bead_getSpacedOutPoints( cont, z, ptsByDist, 5 );
  imodObjectAddContour( obj, ptsByDist );
  
  /*
  Ipoint *p1 = getPt( cont, 0 );
  Ipoint *p2 = getPt( cont, psize(cont) / 2 );
  Ipoint *p3 = getPt( cont, psize(cont)-1 );
  
  
  float aX,bX,cX;
  
  bead_calcQuadraticCurve( p1->z, p2->z, p3->z,
                           p1->x, p2->x, p3->x,
                           &aX, &bX, &cX );
  
  float aY,bY,cY;
  
  bead_calcQuadraticCurve( p1->z, p2->z, p3->z,
                           p1->y, p2->y, p3->y,
                           &aY, &bY, &cY );
  
  
  Icont *newCont = imodContourNew();
  for( float z=0; z<plug.zsize; z++)
  {
    float y = aY*SQ(z) + bY*z + cY;
    float x = aX*SQ(z) + bX*z + cX;
    imodPointAppendXYZ( newCont, x, y, z );
  }
  imodObjectAddContour( obj, newCont );
  
  
  
  

  
  
  /*
  float z = (getPt(getCurrCont(),ptIdx)->z);
  int zInt = (int)(getPt(getCurrCont(),ptIdx)->z);
  int zRounded = floor( getPt(getCurrCont(),ptIdx)->z + 0.5 );
  
  wprint ("current slice = %d \n", topSlice);
  wprint ("current point.z = %f  %d   %d\n", z, zInt, zRounded );
  
  //## TEST GET TURNING POINT:
  
  QKeyEvent *e = new QKeyEvent(QEvent::KeyPress, Qt::Key_M, 'O', 0);
  ivwControlKey(0, e);
  
  imodSetIndex(imod, 0, 0, 0);
  
  bead_showBottomContoursInPurple();
  
  //## DRAW LINE OF BEST FIT FOR CURRENT CONTOUR:
  
  float gradient, offset;
  //Icont *cont = getCurrCont();
  
  bool success = bead_calcLineOfBestFit( cont, &gradient, &offset );
  
  if(success)
    wprint("y = %f * x + %f\n", gradient, offset);
  else
    wprint("fail");
  
  Icont *newCont = imodContourNew();
  
  Ipoint pt;
  pt.z = topSlice;
  pt.x = 0;
  pt.y = gradient*pt.x + offset;
  
  imodPointAppend( newCont, &pt );
  
  pt.x = plug.xsize;
  pt.y = gradient*pt.x + offset;
  
  imodPointAppend( newCont, &pt );
  
  imodObjectAddContour( obj,newCont );
  
  */
  ivwRedraw( plug.view );
}



//## BASIC METHODS TO CHANGE PLUG DATA:




//------------------------
//-- Change changeShowExpectedPos

void BeadHelper::changeShowExpectedPos() {
  plug.showExpectedPos = showExpectedPosCheckbox->isChecked();
  drawExtraObject(true);
}



//------------------------
//-- Change showSpheres

void BeadHelper::changeShowSpheres() {
  plug.showSpheres = showSpheresCheckbox->isChecked();
  
  if( plug.showSpheres )
    imodObjectSetValue( getCurrObj(), IobjPointSize, plug.sphereSize );
  else
    imodObjectSetValue( getCurrObj(), IobjPointSize, 0 );
  
  drawExtraObject(true);
}

//------------------------
//-- Change sphereSize

void BeadHelper::changeSphereSize( int value ) {
  plug.sphereSize = value; 
  changeShowSpheres();
}

//------------------------
//-- Change lineDisplayType

void BeadHelper::changeLineDisplayType(int value) {
  plug.lineDisplayType = value;
  drawExtraObject(true);
}

//------------------------
//-- Change lineDisplayType

void BeadHelper::changeTiltDisplayType(int value) {
  plug.tiltDisplayType = value;
  drawExtraObject(true);
}

//------------------------
//-- Change wheelBehav

void BeadHelper::changeWheelBehav(int value) {
  plug.wheelBehav = value;
}

//------------------------
//-- Change wheelBehav

void BeadHelper::changeEstPosMethod(int value) {
  plug.estPosMethod = value;
  drawExtraObject(true);
}



//## PROTECTED:


//------------------------
//-- Called to display help window.

void BeadHelper::buttonPressed(int which)
{
  if (!which)
    close();
  else
  {
    QString str = QString(getenv("IMOD_DIR"));
    str += QString("/lib/imodplug/beadhelper.html");
    
    imodShowHelpPage(str);
  }
}

//------------------------
//-- Window closing event handler - removes this pluging from the imod dialog manager

void BeadHelper::closeEvent ( QCloseEvent * e )
{
  imodDialogManager.remove((QWidget *)plug.window);
  clearExtraObj();
  ivwFreeExtraObject(plug.view, plug.extraObjContDisp);
  ivwTrackMouseForPlugs(plug.view, 0);
  ivwEnableStipple( plug.view, 0 );
  
  if( plug.autoSaveSettings )
    plug.window->saveSettings();
  
  plug.view = NULL;
  plug.window = NULL;
  e->accept();
}


//------------------------
//-- Key press event handler - closes on escape or passes on event to "ivwControlKey"

void BeadHelper::keyPressEvent ( QKeyEvent * e )
{
  if (e->key() == Qt::Key_Escape)
    close();
  else
    ivwControlKey(0, e);
}

//------------------------
//-- Key release event hander - passes on event to "ivwControlKey"

void BeadHelper::keyReleaseEvent ( QKeyEvent * e )
{
  ivwControlKey(1, e);
}






//############################################################



//----------------------------------------------------------------------------
//
//          GUI FUNCTIONS:
//
//----------------------------------------------------------------------------


//---------
//-- Converts a QString to a standard string

string qStringToString( QString qstr )
{
  string str = "";
  for( int i=0; i<qstr.length(); i++ )
    str +=  qstr.at(i).latin1();
  return str;
}

//---------
//-- Display a simple message box

void MsgBox( string str )
{
  QMessageBox::information(0, "...", str.c_str() );
}

//---------
//-- Display a tyes/no dialog box and return "true" if use clicks yes.

bool MsgBoxYesNo( QWidget *parent, string str )
{
  int result = QMessageBox::information( parent, "...", str.c_str(),
                                         QMessageBox::Yes, QMessageBox::No );
  return ( result == QMessageBox::Yes );
}

//---------
//-- Display an input dialog and return the string entered by the user.

string InputBoxString( QWidget *parent, string title, string label, string defaultStr )
{
  return qStringToString( QInputDialog::getText(title.c_str(), label.c_str(),
                                                QLineEdit::Normal, defaultStr.c_str()));
}


//----------------------------------------------------------------------------
//
//          SIMPLE FUNCTIONS:
//
//----------------------------------------------------------------------------



//---------------------------------
//-- Returns a pointer to the currently selected object.

Iobj *getCurrObj()
{
  Imod *imod = ivwGetModel(plug.view);
  return ( imodObjectGet(imod) );
}


//---------------------------------
//-- Returns a pointer to the currently selected contour.

Icont *getCurrCont()
{
  Imod *imod = ivwGetModel(plug.view);
  return ( imodContourGet(imod) );
}


//---------------------------------
//-- Returns a pointer to the currently selected point.

Ipoint *getCurrPt()
{
  Imod *imod = ivwGetModel(plug.view);
  return ( imodPointGet(imod) );
}


//---------------------------------
//-- Returns true if the object is valid

bool isCurrObjValid()
{
  Iobj *obj = getCurrObj();
  return (obj!=NULL);
}


//---------------------------------
//-- Returns true is a valid contour is selected.

bool isCurrContValid()
{
  return ( isContValid( getCurrCont() ) );
}


//---------------------------------
//-- Returns true is a valid point is selected.

bool isCurrPtValid()
{
  Ipoint *pt = getCurrPt();
  return (pt!=NULL);
}





//----------------------------------------------------------------------------
//
//          EDITING FUNCTIONS:
//
//----------------------------------------------------------------------------




//------------------------
//-- Gets the slice value of the top Zap window or returns -1 if no Zap

int edit_getZOfTopZap()
{
  int currSlice = -1;
  int noZap = ivwGetTopZapZslice(plug.view, &currSlice);   // gets current slice
  if (noZap == 1)   // if no top ZAP window:
    return (-1);
  return (currSlice);
}


//------------------------
//-- Sets the top ZAP window to focus on the selected point and slice.

int edit_setZapLocation( float x, int y, int z, bool redraw )
{
  ivwSetLocation( plug.view, x, y, z );
  if( redraw )
    ivwDraw( plug.view, IMOD_DRAW_XYZ | IMOD_DRAW_NOSYNC );
}


//------------------------
//-- Changes the Z slice by calling page up or page down

int edit_changeSelectedSlice( int changeZ, bool redraw )
{
  int ix, iy, iz;
  ivwGetLocation( plug.view, &ix, &iy, &iz );
  edit_setZapLocation( ix, iy, iz+changeZ, redraw );
}



//------------------------
//-- Sets the top ZAP window to focus on the selected point.
//-- Note that the method to do this is quite crude, because it
//-- involves the creationg and then delestion of a point,
//-- since "ivwSetLocation" alone is not enough to shift the view.

bool bead_focusOnPointCrude( float x, float y, float z )
{
  if( !isCurrObjValid() )
    return false;
  
  Imod *imod = ivwGetModel(plug.view);
  Iobj *obj = imodObjectGet(imod);
  int objIdx, contIdx, ptIdx;
  imodGetIndex(imod, &objIdx, &contIdx, &ptIdx);
  
  Icont *tempCont = imodContourNew();
  imodPointAppendXYZ( tempCont, x,y,z );
  int tempContIdx = imodObjectAddContour( obj,tempCont );
  imodSetIndex(imod, objIdx, tempContIdx, 0);
  imodObjectRemoveContour( obj, tempContIdx );
  ivwSetLocation(plug.view, x, y, z);
  ivwRedraw( plug.view );
  imodContourDelete(tempCont);
}




//------------------------
//-- Returns true if there is a point in "cont" on the specified slice.

bool bead_isPtOnSlice( Icont *cont, int slice )
{
  for( int p=0; p<psize(cont); p++ )
    if( getPtZInt(cont,p) == slice )
      return true;
  return false;
}

//------------------------
//-- Returns the index of the first point in "cont" on the specified slice or
//-- returns -1 if there is no point on that contour.

int bead_getPtIdxOnSlice( Icont *cont, int slice )
{
  for( int p=0; p<psize(cont); p++ )
    if( getPtZInt(cont,p) == slice )
      return p;
  return (NO_POINT);
}


//------------------------
//-- Returns pointer to the first point in "cont" on the specified slice
//-- or returns NULL if there is no point on that contour.

Ipoint *bead_getPtOnSlice( Icont *cont, int slice )
{
  for( int p=0; p<psize(cont); p++ )
    if( getPtZInt(cont,p) == slice )
      return getPt(cont,p);
  return NULL;
}


//------------------------
//-- Returns the index within the contour where you would insert
//-- a point on the specified slice (i.e. the first index 
//-- where z >= "slice" ).

int bead_getExpPtIdxForSlice( Icont *cont, int slice )
{
  for( int p=0; p<psize(cont); p++ )
    if( getPtZInt(cont,p) >= slice )
      return (p);
  return psize(cont)-1;
}

//------------------------
//-- Returns the index to the point which is closest to current slice
//-- or returns -1 if no points

int bead_getClosestPtIdxToSlice( Icont *cont, int slice )
{
  if( isEmpty(cont) )
    return (NO_POINT);
  
  int closestDistInZ = INT_MAX;
  int closestIdx = 0;
  
  for( int p=0; p<psize(cont); p++ )
  { 
    int distInZ = ABS( getPtZInt(cont,p) - slice );
    if (distInZ < closestDistInZ)
    {
      closestDistInZ = distInZ;
      closestIdx = p;
    }
  }
  return closestIdx;
}


//------------------------
//-- Returns the point which is closest to current slice
//-- or returns NULL if no points

Ipoint *bead_getClosestPtToSlice( Icont *cont, int slice )
{
  int closestPtIdx = bead_getClosestPtIdxToSlice( cont, slice );
  if (closestPtIdx == NO_POINT)
    return NULL;
  return getPt(cont, closestPtIdx);
}

//------------------------
//-- Returns the two points closest to the given slice, but not actually on the slice.
//-- Returns false if there are not enough points.

bool bead_getClosestTwoPointsToSlice( Icont *cont, int slice, Ipoint *pt1, Ipoint *pt2 )
{
  if( !cont || psize(cont) < 3 )
    return false;
  
  int closestDistInZ1 = INT_MAX;
  int closestDistInZ2 = INT_MAX;
  
  for( int p=0; p<psize(cont); p++ )
  {
    int distInZ = ABS( getPtZInt(cont,p) - slice );
    
    if (distInZ == 0)
    {
      continue;
    }
    else if( distInZ <= closestDistInZ1 )
    {
      closestDistInZ2 = closestDistInZ1;
      closestDistInZ1 = distInZ;
      *pt2 = *pt1;
      *pt1 = *getPt(cont,p);
    }
    else if( distInZ <= closestDistInZ2 )
    {
      closestDistInZ2 = distInZ;
      *pt2 = *getPt(cont,p);
    }
  }
  return true;
}




//------------------------
//-- Returns the two points closest to the given slice, but not actually on the slice.
//-- Returns false if there are not enough points.

bool bead_getSpacedOutPoints( Icont *cont, int slice,
                              Icont *ptsByDist, int minZBetweenPts )
{
  imodContourDefault( ptsByDist );
  
  if( !cont || psize(cont) < 5 )
    return false;
  
  //## POPULATE AND SORT LIST OF POINTS IN THE CONTOUR ABOVE AND BELOW THE SLICE:
  
  Icont *ptsAbove = imodContourNew();
  Icont *ptsBelow = imodContourNew();
  
  for( int p=0; p<psize(cont); p++ )
  {
    Ipoint *pt = getPt(cont,p);
    float x = pt->x;
    float y = pt->y;
    float z = int(pt->z + 0.5);
    
    if( getPtZInt(cont,p) < slice )
      imodPointAppendXYZ( ptsBelow, x,y,z );
    if( getPtZInt(cont,p) > slice )
      imodPointAppendXYZ( ptsAbove, x,y,z );
  }
  imodel_contour_invert( ptsBelow );    // inverts order of points in the points below
                                        // so they are in order of distance from slice
  
  //## DELETE POINTS WITHIN THE LIST SUCH THAT REMAINING POINTS ARE SPACED AT
  //## LEAST "minZBetweenPts" APART
  
  int nextZA = slice + minZBetweenPts;
  for(int i=0; i<psize(ptsAbove); i++)
  {
    if( getPtZInt(ptsAbove,i) < nextZA )
    {
      imodPointDelete(ptsAbove,i);
      i--;
    }
    else
      nextZA = getPtZInt(ptsAbove,i) + minZBetweenPts;
  }
  
  int nextZB = slice - minZBetweenPts;
  for(int i=0; i<psize(ptsBelow); i++)
  {
    if( getPtZInt(ptsBelow,i) > nextZB )
    {
      imodPointDelete(ptsBelow,i);
      i--;
    }
    else
      nextZB = getPtZInt(ptsBelow,i) - minZBetweenPts;
  }
  
  //## COMBINE LISTS SUCH THAT POINTS ARE IN ORDER OF DISTANCE FROM THE SLICE:
  
  float mid = plug.middleSlice;       // used as tie breaker
  
  int a=0;
  int b=0;
  while( b<psize(ptsBelow) && a<psize(ptsAbove) ) 
  {
    Ipoint *ptA = getPt(ptsAbove, a);
    Ipoint *ptB = getPt(ptsBelow, b);
    
    float ptADist = ptA->z - slice;
    float ptBDist = ptB->z - slice;
    
    bool ptAIsAbove = ptADist < ptBDist
                      || (ptADist == ptBDist && ABS(ptA->z-mid) < (ptB->z-mid) );
    
    if( ptAIsAbove )
    {
      imodPointAppend( ptsByDist, ptA );
      a++;
    }
    else
    {
      imodPointAppend( ptsByDist, ptB );
      b++;
    }
  }
  for(;a<psize(ptsAbove);a++)
    imodPointAppend( ptsByDist, getPt(ptsAbove, a) );
  for(;b<psize(ptsBelow);b++)
    imodPointAppend( ptsByDist, getPt(ptsBelow, b) );
  
  imodContourDelete( ptsAbove );
  imodContourDelete( ptsBelow );
  
  return true;
}


//------------------------
//-- Outputs the expected position of the point within "cont" on the specified
//-- slice based on the position of the points before and after this slice.
//-- Returns true if successful or false if contour is invalid or there are not
//-- enough points to make a reliable prediction

bool bead_getExpectedPosOfPoint( Icont *cont, int slice, Ipoint *pt )
{
  if( !cont || psize(cont) < 3 )
    return false;
  
  
  Ipoint expectedPt;
  float z = slice;
  expectedPt.z = z;
  

  
  switch( plug.estPosMethod )
  {
    case (EM_NEARESTTWO):
    {
      Ipoint pt1, pt2;
      bead_getClosestTwoPointsToSlice( cont, slice, &pt1, &pt2 );
      
      float fractToExpectedPos = fDivide( (slice - pt1.z) , ( pt2.z - pt1.z) ); 
      
      expectedPt = line_findPtFractBetweenPts( &pt1, &pt2, fractToExpectedPos );
      break;
    }
      
    case (EM_QUADRATIC):
    {
      Ipoint *p1 = getPt( cont, 0 );
      Ipoint *p2 = getPt( cont, psize(cont) / 2 );
      Ipoint *p3 = getPt( cont, psize(cont)-1 );
      
      Ipoint *seedPt = bead_getClosestPtToSlice( cont, plug.middleSlice );
      if ( (seedPt->z == plug.middleSlice) && (psize(cont) > 20)
          && (seedPt->z > p1->z+10) && (seedPt->z < p3->z-10) )
      {
        p2 = seedPt;
      }
      
          // calculate the quadratic function representing the bend along x
          // relative to z (usually this is greatest):
      
      float aX,bX,cX;
      bead_calcQuadraticCurve( p1->z, p2->z, p3->z, p1->x, p2->x, p3->x, &aX, &bX, &cX );         //     |_____ z
      expectedPt.x = aX*(z*z) + bX*z + cX;
      
          // calculate the quadratic function representing the bend along y
          // relative to z - this is usually small because usually the
          // tilt axis is only 12 degrees off the Y axis):
      
      float aY,bY,cY;
      bead_calcQuadraticCurve( p1->z, p2->z, p3->z, p1->y, p2->y, p3->y, &aY, &bY, &cY );         //     |_____ z
      expectedPt.y = aY*(z*z) + bY*z + cY;
      
      break;
    }
    
    case (EM_LOCALQUADRATIC):
    {
      Icont *ptsByDist = imodContourNew();
      bead_getSpacedOutPoints( cont, z, ptsByDist, 10 );
      
      if( psize(ptsByDist) < 3 )
        return (false);
      
      Ipoint *p1 = bead_getClosestPtToSlice( cont, slice );
      Ipoint *p2 = getPt( ptsByDist, 0 );
      Ipoint *p3 = getPt( ptsByDist, 1 );
      
      if( p2->z == p1->z)
        p2 = getPt( ptsByDist, 2 );
      
      // calculate the quadratic function representing the bend along x
      // relative to z (usually this is greatest):
      
      float aX,bX,cX;
      bead_calcQuadraticCurve( p1->z, p2->z, p3->z, p1->x, p2->x, p3->x, &aX, &bX, &cX );         //     |_____ z
      expectedPt.x = aX*(z*z) + bX*z + cX;
      
      // calculate the quadratic function representing the bend along y
      // relative to z - this is usually small because usually the
      // tilt axis is only 12 degrees off the Y axis):
      
      float aY,bY,cY;
      bead_calcQuadraticCurve( p1->z, p2->z, p3->z, p1->y, p2->y, p3->y, &aY, &bY, &cY );         //     |_____ z
      expectedPt.y = aY*(z*z) + bY*z + cY;
      
      break;
    }
      
    case (EM_LASTFOUR):
    {
      if( psize(cont) < 8 )
        return (false);
      
      int midPtIdx  = psize(cont) / 2;
      int ptIdx     = bead_getExpPtIdxForSlice( cont, slice );
      bool searchUp = midPtIdx > ptIdx;
      bool isOnPt   = getPtZInt( cont, ptIdx ) == slice;
      
      int directionAsc = (searchUp) ? 1 : -1;
      int offset       = (isOnPt) ? 1 : 0;
      
      int firstIdx = directionAsc * (4+offset);
      
      Ipoint *p1 = getPt( cont, firstIdx );
      Ipoint *p2 = getPt( cont, firstIdx + 1*directionAsc );
      Ipoint *p3 = getPt( cont, firstIdx + 2*directionAsc );
      Ipoint *p4 = getPt( cont, firstIdx + 3*directionAsc );
      
      Icont *lastFourPts = imodContourNew();
      
      
      float gradient, offsetY;
      bool success = bead_calcLineOfBestFit( lastFourPts, &gradient, &offsetY );
      float angle = atan( gradient ) * RADS_TO_DEGS;
      
      
      //float angleP1P4 = line_getAngle2D( pt, p4 );
      //float angleP1P4 = line_getAngle2D( pt, p4 );
      //float angleP1P4 = line_getAngle2D( pt, p4 );
        
        
      wprint("last four not implemented\n");
      return false;
    }
  }
  
  *pt = expectedPt;
  return true;
}

//------------------------
//-- Inserts a point into the approprite place in the contour (in ascending z order)
//-- and returns its index.
//-- If there is already a point at the given z value it is overwritten.

int bead_insertOrOverwritePoint( Icont *cont, Ipoint *pt )
{
  for( int p=0; p<psize(cont); p++ )
  {
    if ( getPtZInt(cont,p) == (int)pt->z ) {
      *getPt(cont,p) = *pt;
      return p;
    }
    else if( getPtZInt(cont,p) > (int)pt->z ) {
      //Ipoint *newPt = *pt;
      imodPointAdd(cont,pt,p);
      return p;
    }
  }
  imodPointAppend(cont,pt);
  return (psize(cont)-1);
}


//------------------------
//-- Inserts a point on the given view into the expected position
//-- within the given contour. If a point already exists at that view
//-- it is overwritten is "overwrite" is true.
//-- Returns true if a point was inserted or overwritten.

bool bead_insertPtAtEstimatedPos( Icont *cont, int slice, bool overwrite )
{
  if( !cont || psize(cont) < 3 )
    return false;
  
  if( !overwrite && bead_isPtOnSlice(cont,slice) )
    return false;
  
  Ipoint expectedPt;
  if( bead_getExpectedPosOfPoint( cont, slice, &expectedPt ) )
  {
    bead_insertOrOverwritePoint( cont, &expectedPt );
    return true;
  }
  
  return false;
}







//------------------------
//-- Fills in all missing points in current contour within slices between
//-- "minZ" and "maxZ" inclusive, by starting at the middle point and
//-- using the expected middle position. Returns the number of points added.

int bead_fillMissingPtsOnCont( Icont *cont, int minZ, int maxZ )
{
  if( !cont || psize(cont) < 3 || minZ > maxZ )
    return 0;
  
  int middleZ = avg(maxZ,minZ);
  int pointsAdded = 0;
  
  for( int z=middleZ; z<=maxZ; z++ )
  {
    if( bead_insertPtAtEstimatedPos(cont,z,false) )
      pointsAdded++;
  }
  for( int z=middleZ-1; z>=minZ; z-- )
  {
    if( bead_insertPtAtEstimatedPos(cont,z,false) )
      pointsAdded++;
  }
  
  return pointsAdded;
}



//------------------------
//-- Calculates the devition of each point in the contour from it's expected position
//-- based on the position of the two "reference" points either side of it,
//-- which is then divided by the distance between the two "reference" points.
//-- This is done so a deviation of say, 3 pixels, for a point at high tilt
//-- (where the points move rapidly) has a much heavier weighting than it would
//-- at low tilt (where the movement of points is small and easier to see).
//-- 
//-- The weighted deviations are added together and then divided by 
//-- the number of points and returned. The higher the value returned, the
//-- more bumpy the curve.

float bead_calcWeightedDevFromExpected( Icont *cont )
{
  if( !cont || psize(cont) < 5 )
    return 0;
  
  float totalWeightedDev = 0;
  
  for (int p=1; p<psize(cont)-1; p++)
  {
    Ipoint *pt1 = getPt(cont,p-1);
    Ipoint *pt2 = getPt(cont,p);
    Ipoint *pt3 = getPt(cont,p+1);
    
    if(p==0)
      pt1 = getPt(cont,2);
    else if(p==psize(cont)-1)
      pt3 = getPt(cont,psize(cont)-2);
    
    float fractToExpectedPt2 = fDivide( (pt2->z - pt1->z) , ( pt3->z - pt1->z) ); 
    Ipoint expectedPosPt2 = line_findPtFractBetweenPts( pt1, pt3, fractToExpectedPt2 );
    
    float distFromExpected = line_distBetweenPts2D( pt2, &expectedPosPt2 );
    float distPt1AndPt3    = line_distBetweenPts2D( pt1, pt3 );
    float crudeWeigthedDev = distFromExpected / (distPt1AndPt3 + 3);
    
    totalWeightedDev += crudeWeigthedDev;
  }
  
  float finalWeightedDev = totalWeightedDev / (float)psize(cont);
  
  return (finalWeightedDev);
}


//------------------------
//-- Returns the grey scale value in memory of the pixel nearest to the given point

float bead_getGreyValue( Ipoint *pt )
{
  int x = int(pt->x + 0.5);
  int y = int(pt->y + 0.5);
  int z = int(pt->z + 0.5);
  
  float greyVal = ivwGetFileValue( plug.view, x,y,z );
  return greyVal;
}

//------------------------
//-- Returns the average grey value of the points in the contour.

float bead_avgGreyValueOfPts( Icont *cont )
{
  if( isEmpty(cont) )
    return 0;
  
  float totalGreyVal = 0;
  
  for (int p=0; p<psize(cont); p++)
    totalGreyVal += bead_getGreyValue( getPt(cont,p) );
  
  float avgGreyVal = totalGreyVal / (float)psize(cont);
  
  return (avgGreyVal);
}


//------------------------
//-- Returns the distance of the middle-most point from the
//-- center of the tomogram, or returns FLOAT_MAX if there
//-- are no points

float bead_distFromMiddle( Icont *cont )
{
  if( isEmpty(cont) )
    return FLOAT_MAX;
  
  Ipoint *closestPtToMiddle = bead_getClosestPtToSlice( cont, plug.middleSlice );
  
  return ( line_distBetweenPts2D( closestPtToMiddle, &plug.middlePt ) );
}


//------------------------
//-- Sorts and reorderes all contours past "minCont" according to the specified
//-- "sortCriteria" (see enum contsortcriteria).
//-- Setting "reverse" will sort the value in descending instead of descending.
//-- Setting "printVals" will output the sort values for each contour.

void bead_reorderConts( int sortCriteria, int minCont,          
                        bool reverse, bool printVals )
{
  Imod *imod = ivwGetModel(plug.view);
  Iobj *obj = imodObjectGet(imod);
  int objIdx, contIdx, ptIdx;
  imodGetIndex(imod, &objIdx, &contIdx, &ptIdx);
  
  int nConts = imodObjectGetMaxContour(obj);
  int minC = MAX( minCont,0 );
  int maxC = imodObjectGetMaxContour(obj);
  
  //## CREATE VECTOR OF FLOATS TO SORT CONTOURS BY AND CALCULATE SORT VALUES
  //## USING CHOSEN SORT CRITERIA:
  
  vector<IdxToSort> sortVals;               // stores a idx and float for each contour
                                            // and is used to sort the contours
  sortVals.resize( nConts );
  
  for( int c=0; c<nConts; c++)
  {
    sortVals[c].idx = c;
    Icont *cont = getCont(obj,c);
    
    switch(sortCriteria)
    {
      case(SORT_DEV):
        sortVals[c].float1 = bead_calcWeightedDevFromExpected(cont);
        break;
      case(SORT_AVG_GREY):
        sortVals[c].float1 = bead_avgGreyValueOfPts(cont);
        break;
      case(SORT_DIST_FROM_MIDDLE):
        sortVals[c].float1 = bead_distFromMiddle(cont);
        break;
      case(SORT_MISSING_PTS):
        sortVals[c].float1 = plug.zsize - psize(cont);
        break;
      case(SORT_RANDOM):
        sortVals[c].float1 = rand();
        break;   
    }
  }
  
  //## SORT THE CHOSEN RANGE OF VALUES WITHIN THE SORT VECTOR:

  sortVals = vector_sort( sortVals, minC );
  
  if( sortCriteria==SORT_AVG_GREY )
    reverse = !reverse;
  
  if( reverse )
    sortVals = vector_reverse( sortVals, minC, maxC );
  
  
  //## REORDER THE CONTOURS WITHIN THE OBJECT USING THE NOW SORTED SORT VECTOR:
  
  Iobj *objCopy = imodObjectDup( obj );
  
  int numContsChanged = 0;
  for( int c=minC; c<maxC; c++)
  {
    if( sortVals[c].idx != c )
    {
      Icont *cont = getCont(obj,c);
      imodSetIndex(imod, objIdx, c, 0);
      undoContourDataChgCC( plug.view );      // REGISTER UNDO
      Icont *newCont = getCont( objCopy, sortVals[c].idx );
      cont_copyPoints( newCont, cont, true );
      numContsChanged++;
    }
  }
  
  if(numContsChanged)
    undoFinishUnit( plug.view );              // FINISH UNDO
  
  imodObjectDelete( objCopy );
  imodSetIndex(imod, objIdx, nConts-1, 0);
  
  //## OUTPUT RESULTS:
  
  if( printVals )
  {
    if      (sortCriteria==SORT_DEV)              wprint("\nWEIGHTED DEVIATIONS:\n");
    else if (sortCriteria==SORT_AVG_GREY)         wprint("\nAVERAGE GREY VALUES:\n");
    else if (sortCriteria==SORT_DIST_FROM_MIDDLE)   wprint("\nDIST FROM MIDDLE:\n");
    for( int c=0; c<nConts; c++)
      wprint( "cont %d = %f\n", c+1, sortVals[c].float1 );
  }
  
  wprint("\n%d contours have been reordered\n", numContsChanged);
}





//------------------------
//-- Smooths all the points within the given range of contours, over the
//-- given range of points.
//-- This is similar in some ways to "movePtsToExstimatedPos()" except
//-- that it only moves point towards the estimated position.

void bead_smoothConts( int smoothCriteria, bool yAxisOnly, int iterations,
                       bool currContOnly )
{
  ;
}




//------------------------
//-- Uses the "least fitting squares" equation to generate the line of best fit
//-- which represents a linear path (y=a+bx) through the set of scattered
//-- point in the given contour which minimizes the sum of squared residual
//-- distances from the line to each point along y.
//-- 
//-- y = gradient*x + offset
//-- 
//-- see: http://mathworld.wolfram.com/LeastSquaresFitting.html

bool bead_calcLineOfBestFit( Icont *cont, float *gradient, float *offset )
{
  if ( isEmpty(cont) || psize(cont) < 5 )
    return false;
  
  float n = (float) psize(cont);
  
  float sumX    = 0;
  float sumXSq  = 0;
  float sumY    = 0;
  float sumXY   = 0;
  
  for( int p=0; p<psize(cont); p++ )
  {
    float x = getPt(cont,p)->x;
    float y = getPt(cont,p)->y;
    
    sumX     += x;
    sumXSq   += (x*x);
    sumY     += y;
    sumXY    += (x*y);
  }
  
  float avgX = sumX / n;
  float avgY = sumY / n;
  
  *gradient = ( sumXY - (n*avgX*avgY) ) / ( sumXSq - (n*SQ(avgX)) );
  *offset   = ( (avgY*sumXSq) - (avgX*sumXY) ) / ( sumXSq - (n*SQ(avgX)) );
  
  float a = ( sumXY - (n*avgX*avgY) ) / ( sumXSq - (n*SQ(avgX)) );
  float b   = ( (avgY*sumXSq) - (avgX*sumXY) ) / ( sumXSq - (n*SQ(avgX)) );
  
  wprint("avgX = %f, avgY = %f\n", avgX, avgY);
  wprint("y = %f * x + %f\n", a, b);
  wprint("y = %f * x + %f\n", *gradient, *offset);
  
  return true;
}



//------------------------
//-- Calculates the quadratic function from three given points.
//-- Returns false if two of the given points have the same x value,
//-- therefore cannot calculate the equation.
//-- 
//-- y = a(x^2) + b(x) + c
//-- 
//-- see: http://mathworld.wolfram.com/LeastSquaresFitting.html

bool bead_calcQuadraticCurve( float x1, float x2, float x3, float y1, float y2, float y3,
                              float *a, float *b, float *c )
{
  //a = [(Y2-Y1)(X1-X3) + (Y3-Y1)(X2-X1)]/[(X1-X3)(X2^2-X1^2) + (X2-X1)(X3^2-X1^2)]
  //b = [(Y2 - Y1) - A(X2^2 - X1^2)] / (X2-X1)
  //c = Y1 - AX1^2 - BX1
  
  float divisorA = (x1-x3)*(SQ(x2)-SQ(x1)) + (x2-x1)*(SQ(x3)-SQ(x1));
  float divisorB = (x2-x1);
  
  *a = fDivide ( ( (y2-y1)*(x1-x3) + (y3-y1)*(x2-x1) ), divisorA );
  *b = fDivide (  ( (y2 - y1) - (*a)*(SQ(x2) - SQ(x1)) ), divisorB );
  *c = y1 - (*a)*SQ(x1) - (*b)*x1;
  
  return ( divisorA!=0 && divisorB != 0);
}


//------------------------
//-- Estimates the "turning point" - the point in the contours where the points
//-- stops shifting in X and Y and (usually) changes directions.

bool bead_estimateTurningPointOfCont( Icont *cont, Ipoint *pt,
                                      float minDistRequired, int *idx )
{
  if( isEmpty(cont) || psize(cont)<5 )
    return false;
  
  float minDistBetweenViews = FLOAT_MAX;
  
  for (int p=0; p<psize(cont)-1; p++)
  {
    Ipoint *pt1 = getPt(cont,p);
    Ipoint *pt2 = getPt(cont,p+1);
    
    float distZ = ABS(pt2->z - pt1->z);
    float distXY = line_distBetweenPts2D( pt2, pt1 );
    
    if( distZ == 0 ) {
      wprint( "ERROR: Two points on view %d", getPtZInt(cont,p) );
      continue;
    }
    
    float distBetweenViews = distXY / distZ;
    
    if( distBetweenViews < minDistBetweenViews )
    {
      minDistBetweenViews = distBetweenViews;
      *idx = p;
      //*pt = line_getPtHalfwayBetween( pt1, pt2 );
      *pt = *getPt( cont, p );
    }
  }
  
  return ( minDistBetweenViews <= minDistRequired );
}






//------------------------
//-- Uses a grid of points to determine the "biggest hole" between
//-- seed points on the middle slice. If "findNextBiggest" is true
//-- and the user has not added any points since the last iteration
//-- it will instead find the "next biggest hole". Returns true if a hole
//-- was found.


float maxDistLastIteration = FLOAT_MAX;
int numSeedPtsLastIteration = 0;


bool bead_goToNextBiggestHole( bool findNextBiggest )
{
  if( !isCurrObjValid() )
    return false;
  
  //## CREATE A NEW CONTOUR CONTAINING ALL SEED POINTS:
  
  Icont *allSeedPts = imodContourNew();
  Imod *imod = ivwGetModel(plug.view);
  Iobj *obj = imodObjectGet(imod);
  for (int c=0; c<imodObjectGetMaxContour(obj); c++)
  {
    Icont *cont = getCont(obj,c);
    Ipoint *pt = bead_getPtOnSlice(cont,plug.middleSlice);
    if( pt != NULL )
      imodPointAppendXYZ( allSeedPts, pt->x, pt->y, pt->z );
  }
  
  //## DETERMINE WETHER USER HAS ADDED POINTS SINCE LAST ITERATION, IF NOT
  //## GO TO NEXT BIGGEST HOLE (INSTEAD OF BIGGEST HOLE)
  
  int numSeedPts = psize(allSeedPts);
  float maxDistAllowed = FLOAT_MAX;
  if ( findNextBiggest && numSeedPts == numSeedPtsLastIteration ) {
    maxDistAllowed = maxDistLastIteration;
    wprint("Finding biggest hole\n");
  }
  else {
    wprint("Finding NEXT biggest hole\n");
  }
  
  if( numSeedPts == 0 )
  {
    wprint("No seed points exist\n");
    bead_focusOnPointCrude( plug.middlePt.x, plug.middlePt.y, plug.middlePt.z );
    maxDistLastIteration = FLOAT_MAX;
    numSeedPtsLastIteration = 0;
    return true;
  }
  
  //## INITIALIZE A GRID OF POINT TO MEASURE THE DISTANCE TO THE NEAREST
  //## SEED POINT OR EDGE:
  
  int colsX = int(plug.xsize / plug.biggestHoleGrid);
  int rowsY = int(plug.ysize / plug.biggestHoleGrid);
  
  float sideLenX = fDivide(plug.xsize, colsX);
  float sideLenY = fDivide(plug.ysize, rowsY);
  
  vector< vector<float> >minDistGrid( colsX, rowsY );
  
  
  //## UPDATE GRID POINTS TO CONTAIN DISTANCE TO NEAREST EDGE:
  
  for(int x=0; x<colsX; x++)
  {
    float nearestSideX = MIN(x,colsX-x) * sideLenX;
    for(int y=0; y<rowsY; y++)
    {
      float nearestSideY = MIN(y,rowsY-y) * sideLenY;
      minDistGrid[x][y] = MIN( nearestSideX, nearestSideY);
    }
  }
  
  
  //## FOR EACH GRID POINTS, UPDATE TO CONTAIN DISTANCE TO CLOSEST POINT:
  
  Ipoint pt;
  
  for(int x=0; x<colsX; x++)
  {
    pt.x = x * sideLenX;
    for(int y=0; y<rowsY; y++)
    {
      pt.y = y * sideLenY;
      for (int p=0; p<psize(allSeedPts); p++)
      {
        float dist = line_distBetweenPts2D( &pt, getPt(allSeedPts,p) );
        minDistGrid[x][y] = MIN( minDistGrid[x][y], dist );
      }
    }
  }
  
  //## FIND THE POINT WITH THE MINIMUM DISTANCE:
    
  float maxDist = 0;
  Ipoint maxPt;
  
  for(int x=0; x<colsX; x++)
  { 
    for(int y=0; y<rowsY; y++)
    { 
    if( minDistGrid[x][y] > maxDist && minDistGrid[x][y] < maxDistAllowed )
      {
        maxDist = minDistGrid[x][y];
        maxPt.x = x * sideLenX;
        maxPt.y = y * sideLenY;
        maxPt.z = plug.middleSlice;
      }
    }
  }
  
  
  if( maxDist == 0 )
  {
    wprint("\aNo more holes found\n"
           "Use 'H' or change 'big hole grid size' in settings\n");
    return false;
  }
  else
  {
    wprint(" --> distance: %d pixels\n", (int)maxDist ); 
  }
  
  //## SET FOCUS ON NEW LOCATION:
  
  bead_focusOnPointCrude( maxPt.x, maxPt.y, maxPt.z );
  imodContourDelete(allSeedPts);  
  
  maxDistLastIteration = maxDist;
  numSeedPtsLastIteration = numSeedPts;
  
  return true;
}


//------------------------
//-- Estimates the tilt angle by averaging the gradient for the "line of best fit"
//-- over all contours.

float bead_estimateTiltAngle()
{
  Imod *imod = ivwGetModel(plug.view);
  Iobj *obj = imodObjectGet(imod);
  
  float angleSum = 0;
  int   totalConts = 0;
  
  wprint( "\nCONTOUR ANGLES: \n" );
  for (int c=0; c<imodObjectGetMaxContour(obj); c++)
  {
    Icont *cont = getCont(obj,c); 
    
    float gradient, offset;
    bool success = bead_calcLineOfBestFit( cont, &gradient, &offset );
    
    if( success )
    {
      float angle = atan( gradient ) * RADS_TO_DEGS;
      //wprint("cont %d \tangle=%f\n", c, angle);     //%%%%
      
      angleSum += angle;
      totalConts++;
    }
  }
  
  if( totalConts == 0 )
  {
    wprint("/aERROR: No contours could be measured"); 
    return 0;
  }
  
  float avgAngle = angleSum / (float)totalConts;
  wprint("\nAverage angle over %d contours = %f", totalConts, avgAngle);
  return (avgAngle);
}


//------------------------
//-- Goes through all contours, estimates which contours are on the top
//-- based on the x value of their matching end points, and makes them stipped.
//-- If the last point has a greater x value than than the first point
//-- then the fiducial should be below the tilt axis... unfortunatly
//-- sections are often not flat, and so this function is only successful
//-- when the section is relatively flat

bool bead_showBottomContoursStippledUsingDirection()
{
  Imod *imod = ivwGetModel(plug.view);
  Iobj *obj = imodObjectGet(imod);
  
  //## FOR EACH CONTOUR DETERMINE THE DIFFERENCE IN X
  //## BETWEEN THE POINTS AT THE HIGHEST MATCHING TILT ANGLE:
  
  for (int c=0; c<imodObjectGetMaxContour(obj); c++)
  {
    Icont *cont = getCont(obj,c); 
    
    if( psize(cont) < 5 )
    {
      wprint("cont %d - too few points\n",c+1);
      continue;
    }
    
    Ipoint startPt;
    Ipoint endPt;
    
    startPt.z = 0;
    endPt.z = plug.zsize-1;
    bool pointsFound = false;
    
    for( int z=0; z<4; z++ )
    {
      int zOpposite = plug.zsize-1 - z;
      if( bead_isPtOnSlice(cont,z) && bead_isPtOnSlice(cont,zOpposite) )
      {
        startPt = *getPt(cont,z);
        endPt   = *getPt(cont,zOpposite);
        pointsFound = true;
        break;
      }
    }
    
    if( !pointsFound )
    {
      Ipoint *p1 = getPt( cont, 0 );
      Ipoint *p2 = getPt( cont, psize(cont) / 2 );
      Ipoint *p3 = getPt( cont, psize(cont)-1 );
      
      // calculate the quadratic function representing the bend along x relative to z:
      float aX,bX,cX;
      if( bead_calcQuadraticCurve( p1->z, p2->z, p3->z,
                                   p1->x, p2->x, p3->x,
                                   &aX, &bX, &cX ) )
      {
        //wprint("*",c+1);
        startPt.x = cX;
        endPt.x   = aX*(endPt.z*endPt.z) + bX*(endPt.z) + cX;
      }
      else
      {
        //wprint("cont %d - did not find matching points\n",c+1);
        continue;
      }
    }
    
    int diffMatchingEndPtsInX = int(endPt.x - startPt.x);
    
    if( diffMatchingEndPtsInX > 0 )
    {
      //wprint("cont %d - TOP     \tdist= %d\n",c+1,diffMatchingEndPtsInX);
      imodContourSetFlag( cont, ICONT_STIPPLED, 1 );
      //Icont *copyCont = imodContourDup(cont);
      //imodObjectAddContour( xobjC, cont );
    }
    else
    {
      //wprint("cont %d - BOTTOM \tdist = %d\n",c+1,diffMatchingEndPtsInX);
    }
  }
}

//------------------------
//-- Goes through all contours, estimates which contours are on the bottom,
//-- and show them in purple on an extra object.
//-- It does this by trying to account for the possible slant of the
//-- section in x and y directions relative to the tilt axis.

  // UNFINISHED

bool bead_showBottomContoursInPurple()
{
  Imod *imod = ivwGetModel(plug.view);
  Iobj *obj = imodObjectGet(imod);
  
  Iobj *xobj2 = ivwGetAnExtraObject(plug.view, plug.extraObjExtra);
  ivwClearAnExtraObject(plug.view, plug.extraObjExtra);
  imodObjectSetColor(xobj2, 1, 0, 1);
  imodObjectSetValue(xobj2, IobjPointSize, 3);
  imodObjectSetValue(xobj2, IobjFlagClosed, 0);
  
  
  //## TRY TO DETERMINE TURNING POINT FOR EACH CONTOUR:
  
  Icont *turningPts = imodContourNew();       // stores a turning point corresponding
                                              // to each contout in the object
  
  int turningZMin = plug.zsize * 0.2;
  int turningZMax = plug.zsize * 0.8;
  
  for (int c=0; c<imodObjectGetMaxContour(obj); c++)
  {
    Ipoint turningPt;
    int idx;
    Icont *cont = getCont(obj,c); 
    bool success = bead_estimateTurningPointOfCont( cont, &turningPt, 3.0, &idx );
    if( !success || turningPt.z < turningZMin || turningPt.z > turningZMax )
      turningPt.z = -1;
    imodPointAppend( turningPts, &turningPt );
  }
  
  
  //## SHOW TURNING POINTS IN NEW OBJECT:
  
  for (int p=0; p<psize(turningPts); p++)
  {
    Icont *newCont = imodContourNew();
    imodPointAppend( newCont, getPt(turningPts,p) );
    imodPointSetSize( newCont, 0, 6  );
    imodObjectAddContour( xobj2, newCont );
  }
  
  
  //## CREATE A LIST OF GOOD TURNING POINTS ONLY: 
  
  Icont *goodTurningPts = imodContourDup( turningPts );
  for (int p=0; p<psize(goodTurningPts); p++)
  {
    if( getPt(goodTurningPts,p)->z == -1 )
    {
      imodPointDelete(goodTurningPts, p);
      p--;
    }
  }
  
  
  //## NORMALIZE TURNING POINTS BY ROTATING THEN MOVING EACH
  //## RELATIVE TO THE TILT AXIS
  
  Icont *turningPtsNormalized = imodContourDup( goodTurningPts );
  
  float theta = -plug.tiltAngle * DEGS_TO_RADS;
  for (int p=0; p<psize(turningPtsNormalized); p++)
  {
    Ipoint *pt = getPt(turningPtsNormalized,p);
    if( pt->z != -1 )
    {
      point_rotatePointAroundPoint2D( pt, &plug.middlePt, theta );
      pt->x -= plug.middlePt.x;
      pt->y -= plug.middlePt.y;
    }
  }
  
  
  //## DETERMINE A LINE OF BEST FIT THROUGH THE MIDDLE
  //## ALONG X-Z AND Y-Z:
  
  Icont *goodTurningPtsXZ = imodContourDup( goodTurningPts );
  for (int p=0; p<psize(goodTurningPtsXZ); p++)
  {
    Ipoint *pt = getPt(goodTurningPtsXZ,p);
    pt->y = pt->z;
  }
  
  float gradientXZ, offsetXZ;
  bool successXZ = bead_calcLineOfBestFit( goodTurningPtsXZ, &gradientXZ, &offsetXZ );
  
  
  /*
  
  Icont *goodTurningPtsYZ = imodContourDup( goodTurningPts );
  for (int p=0; p<psize(goodTurningPtsYZ); p++)
  {
    Ipoint *pt = getPt(goodTurningPtsYZ,p);
    pt->x = pt->y;
    pt->y = pt->z;
  }
  
  float gradientYZ, offsetYZ;
  bool successYZ = bead_calcLineOfBestFit( goodTurningPtsYZ, &gradientYZ, &offsetYZ );
  
  */
  
  //## ORDER TURNING 
  
  //imodObjectAddContour( obj, turningPtsNormalized );  //%%%%%
  //imodObjectAddContour( obj, turningPts );  //%%%%%
  
}



//------------------------
//-- Goes through all contours and tries to find a turning point
//-- of each contour (where it changes direction).

bool bead_showContourTurningPts()
{
  Imod *imod = ivwGetModel(plug.view);
  Iobj *obj = imodObjectGet(imod);
  
  Iobj *xobj2 = ivwGetAnExtraObject(plug.view, plug.extraObjExtra);
  ivwClearAnExtraObject(plug.view, plug.extraObjExtra);
  imodObjectSetColor(xobj2, 1, 0, 1);
  imodObjectSetValue(xobj2, IobjPointSize, 3);
  imodObjectSetValue(xobj2, IobjFlagClosed, 0);
  
  //## TRY TO DETERMINE TURNING POINT FOR EACH CONTOUR:
  
  Icont *turningPts = imodContourNew();       // stores a turning point corresponding
                                              // to each contout in the object
  
  int turningZMin = plug.zsize * 0.1;
  int turningZMax = plug.zsize * 0.9;
  
  for (int c=0; c<imodObjectGetMaxContour(obj); c++)
  {
    Ipoint turningPt;
    int idx;
    Icont *cont = getCont(obj,c); 
    bool success = bead_estimateTurningPointOfCont( cont, &turningPt, 3.0, &idx );
    if( !success || turningPt.z < turningZMin || turningPt.z > turningZMax )
      turningPt.z = -1;
    imodPointAppend( turningPts, &turningPt );
  }
  
  //## SHOW TURNING POINTS IN NEW OBJECT:
  
  for (int p=0; p<psize(turningPts); p++)
  {
    Icont *newCont = imodContourNew();
    imodPointAppend( newCont, getPt(turningPts,p) );
    imodPointSetSize( newCont, 0, 6  );
    imodObjectAddContour( xobj2, newCont );
  }  
}





