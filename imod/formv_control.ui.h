/****************************************************************************
** ui.h extension file, included from the uic-generated form implementation.
**
** If you wish to add, delete or rename slots use Qt Designer which will
** update this file, preserving your code. Create an init() slot in place of
** a constructor, and a destroy() slot in place of a destructor.
*****************************************************************************/

// Routines to display slider labels, and record the value displayed
void imodvControlForm::displayFarLabel( int value )
{
    QString str;
    str.sprintf("%d", value);
    farTextLabel->setText(str);
    mFarDisplayed = value;
}

void imodvControlForm::displayNearLabel( int value )
{
    QString str;
    str.sprintf("%d", value);
    nearTextLabel->setText(str);
    mNearDisplayed = value;
}

void imodvControlForm::displayPerspectiveLabel( int value )
{
    QString str;
    str.sprintf("%d", value);
    perspectiveTextLabel->setText(str);
    mPerspectiveDisplayed = value;
}

void imodvControlForm::displayRateLabel( int value )
{
    QString str;
    str.sprintf("%.1f", value / 10.);
    degreesTextLabel->setText(str);
    mRateDisplayed = value;
}

void imodvControlForm::displayZscaleLabel( int value )
{
    QString str;
    if (value < 200)
	str.sprintf("%.2f", value / 100.);
    else
	str.sprintf("%.1f", value / 100.);
    zScaleTextLabel->setText(str);
    mZscaleDisplayed = value;
}

// ZOOM UP AND DOWN CONTROL BUTTONS
void imodvControlForm::zoomDown()
{
    imodvControlZoom(-1);
}

void imodvControlForm::zoomUp()
{
    imodvControlZoom(1);
}

// A new value is entered in the scale box
void imodvControlForm::newScale()
{
    QString str = scaleLineEdit->text();
    float value = atof(str.latin1());
    if  (value < 0.001)
	value = 0.001;
    setScaleText(value);
    setFocus();
    imodvControlScale(value);
}

// Changes in the slider positions
void imodvControlForm::nearChanged( int value )
{
    if (!mNearPressed || mCtrlPressed)
	imodvControlClip(IMODV_CONTROL_NEAR, value);
}

void imodvControlForm::farChanged( int value )
{
    if (!mFarPressed || mCtrlPressed)
	imodvControlClip(IMODV_CONTROL_FAR, value);
}

void imodvControlForm::perspectiveChanged( int value )
{
    if (!mPerspectivePressed || mCtrlPressed)
	imodvControlClip(IMODV_CONTROL_FOVY, value);
}

void imodvControlForm::zScaleChanged( int value )
{
    if (!mZscalePressed || mCtrlPressed)
	imodvControlZscale(value);
}

// The rotation buttons
void imodvControlForm::rotateXminus()
{
    imodvControlAxisButton(-IMODV_CONTROL_XAXIS);
}

void imodvControlForm::rotateXplus()
{
    imodvControlAxisButton(IMODV_CONTROL_XAXIS);
}

void imodvControlForm::rotateYminus()
{
    imodvControlAxisButton(-IMODV_CONTROL_YAXIS);
}

void imodvControlForm::rotateYplus()
{
    imodvControlAxisButton(IMODV_CONTROL_YAXIS);
}

void imodvControlForm::rotateZminus()
{
    imodvControlAxisButton(-IMODV_CONTROL_ZAXIS);
}

void imodvControlForm::rotateZplus()
{
    imodvControlAxisButton(IMODV_CONTROL_ZAXIS);
}

// New values entered in the rotation text boxes
void imodvControlForm::newXrotation()
{
    QString str = XLineEdit->text();
    float value = atof(str.latin1());
    setFocus();
    imodvControlAxisText(IMODV_CONTROL_XAXIS, value);
}

void imodvControlForm::newYrotation()
{
    QString str = YLineEdit->text();
    float value = atof(str.latin1());
    setFocus();
    imodvControlAxisText(IMODV_CONTROL_YAXIS, value);
}

void imodvControlForm::newZrotation()
{
    QString str = ZLineEdit->text();
    float value = atof(str.latin1());
    setFocus();
    imodvControlAxisText(IMODV_CONTROL_ZAXIS, value);
}

// Start/top button
void imodvControlForm::startStop()
{
    imodvControlStart();
}

// Rate slider
void imodvControlForm::rateChanged( int value )
{
    if (!mRatePressed || mCtrlPressed)
	imodvControlRate(value);
}

void imodvControlForm::OKPressed()
{
    imodvControlQuit();
}

void imodvControlForm::helpPressed()
{
    imodvControlHelp();    
}

// Routines for imodv_control to set state of dialog
void imodvControlForm::setAxisText( int axis, float value )
{
    QString str;
    str.sprintf("%6.2f", value);
    if (axis == IMODV_CONTROL_XAXIS)
	XLineEdit->setText(str);
    else if (axis == IMODV_CONTROL_YAXIS)
	YLineEdit->setText(str);
    else 
	ZLineEdit->setText(str);
}

void imodvControlForm::setScaleText( float value )
{
    QString str;
    str.sprintf("%.4g", value);
    scaleLineEdit->setText(str);
}

void imodvControlForm::setViewSlider( int which, int value )
{
    switch (which) {
    case IMODV_CONTROL_NEAR:
	nearSlider->setValue(value);
	displayNearLabel(value);
	break;
    case IMODV_CONTROL_FAR:
	farSlider->setValue(value);
	displayFarLabel(value);
	break;
    case IMODV_CONTROL_FOVY:
	perspectiveSlider->setValue(value);
	displayPerspectiveLabel(value);
	break;
    case IMODV_CONTROL_ZSCALE:
	zScaleSlider->setValue(value);
	displayZscaleLabel(value);
	break;
    }
}

void imodvControlForm::setRotationRate( int value )
{
    degreesSlider->setValue(value);
    displayRateLabel(value);
}

void imodvControlForm::closeEvent( QCloseEvent *e )
{
    imodvControlClosing();
    e->accept();
}

// To keep the sliders from being continuously active while dragged, record when they are
// pressed and released
void imodvControlForm::farPressed()
{
    mFarPressed = true;
}

void imodvControlForm::farReleased()
{
    mFarPressed = false;
    farChanged(mFarDisplayed);
}

void imodvControlForm::nearPressed()
{
    mNearPressed = true;
}

void imodvControlForm::nearReleased()
{
    mNearPressed = false;
    nearChanged(mNearDisplayed);
}

void imodvControlForm::zScaleReleased()
{
    mZscalePressed = false;
    zScaleChanged(mZscaleDisplayed);
}

void imodvControlForm::perspectivePressed()
{
    mPerspectivePressed = true;
}

void imodvControlForm::perspectiveReleased()
{
    mPerspectivePressed = false;
    perspectiveChanged(mPerspectiveDisplayed);
}

void imodvControlForm::zScalePressed()
{
    mZscalePressed = true;
}

void imodvControlForm::ratePressed()
{
    mRatePressed = true;
}

void imodvControlForm::rateReleased()
{
    mRatePressed = false;
    rateChanged(mRateDisplayed);
}

void imodvControlForm::init()
{
    int width;
    mNearPressed = false;
    mFarPressed = false;
    mPerspectivePressed = false;
    mZscalePressed = false;
    mRatePressed = false;
    mCtrlPressed = false;
    width =( (2 * 6 + 3) * scaleLineEdit->fontMetrics().width("888888")) / (2 * 6);
    scaleLineEdit->setFixedWidth(width);
    width =( (2 * 7+ 3) * XLineEdit->fontMetrics().width("8888888")) / (2 * 7);
    XLineEdit->setFixedWidth(width);
    YLineEdit->setFixedWidth(width);
    ZLineEdit->setFixedWidth(width);
}

// Key event: send quit signal if an escape, keep track of control key, and pass on to imodv_input
void imodvControlForm::keyPressEvent( QKeyEvent * e )
{
    // fprintf(stderr, "keyEvent\n");
    if (e->key() == Qt::Key_Escape) {
	imodvControlQuit();
    } else {
    
	if (e->key() == Qt::Key_Control) {
	    mCtrlPressed = true;
	    grabKeyboard();
	}
	imodvKeyPress(e);
    }
}

void imodvControlForm::keyReleaseEvent( QKeyEvent * e )
{
  if (e->key() == Qt::Key_Control) {
	mCtrlPressed = false;
        releaseKeyboard();
  }
  imodvKeyRelease(e);
}


