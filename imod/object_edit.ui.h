/****************************************************************************
** ui.h extension file, included from the uic-generated form implementation.
**
** If you wish to add, delete or rename slots use Qt Designer which will
** update this file, preserving your code. Create an init() slot in place of
** a constructor, and a destroy() slot in place of a destructor.
*****************************************************************************/

void objectEditForm::helpPressed()
{
    ioew_help();
}

void objectEditForm::nameChanged( const QString & newName )
{
    ioew_nametext(newName.latin1());
}

void objectEditForm::symbolChanged( int value )
{
    ioew_symbol(value);
}

void objectEditForm::OKPressed()
{
    ioew_quit();
}

void objectEditForm::radiusChanged()
{
    QString str = radiusEdit->text();
    int value = atoi(str.latin1());
    if  (value < 0)
	value = 0;
    str.sprintf("%d", value);
    radiusEdit->setText(str);
    ioew_pointsize(value);
}

void objectEditForm::selectedSurface( int value )
{
    ioew_surface(value);
}

void objectEditForm::selectedType( int value )
{
    ioew_open(value);
}

void objectEditForm::sizeChanged( int value )
{
    QString str;
    str.sprintf("%d", value);
    sizeLabel->setText(str);
    ioew_symsize(value);
}

void objectEditForm::toggledDraw( bool state )
{
    ioew_draw(state ? 1 : 0);
}

void objectEditForm::toggledFill( bool state )
{
    ioew_fill(state ? 1 : 0);
}

void objectEditForm::toggledMarkEnds( bool state )
{
    ioew_ends(state ? 1 : 0);
}

void objectEditForm::toggledTime( bool state )
{
    ioew_time(state ? 1 : 0);
}

void objectEditForm::widthChanged( int value )
{
    QString str;
    str.sprintf("%d", value);
    widthLabel->setText(str);
    ioew_linewidth(value);
}	


void objectEditForm::setSymbolProperties( int which, bool fill, bool markEnds, int size )
{
   symbolComboBox->setCurrentItem(which);
    fillCheckBox->blockSignals(true);
    fillCheckBox->setChecked(fill);
    fillCheckBox->blockSignals(false);
    markCheckBox->blockSignals(true);
    markCheckBox->setChecked(markEnds);
    markCheckBox->blockSignals(false);
    sizeSlider->setValue(size);
    QString str;
    str.sprintf("%d", size);
    sizeLabel->setText(str);
}

void objectEditForm::setDrawBox( bool state )
{
    drawCheckBox->blockSignals(true);
    drawCheckBox->setChecked(state);
    drawCheckBox->blockSignals(false);
}

void objectEditForm::setObjectName( char *name )
{
    QString str = name;
    nameEdit->setText(str);
}

void objectEditForm::setTimeBox( bool state, bool enabled )
{
    timeCheckBox->blockSignals(true);
    timeCheckBox->setChecked(state);
    timeCheckBox->blockSignals(false);
    timeCheckBox->setEnabled(enabled);
}

void objectEditForm::setPointRadius( int value )
{
    QString str;
    str.sprintf("%d", value);
    radiusEdit->setText(str);
}

void objectEditForm::setFrontSurface( int value )
{
    surfaceButtonGroup->blockSignals(true);
    surfaceButtonGroup->setButton(value);
    surfaceButtonGroup->blockSignals(false);   
}

void objectEditForm::setObjectType( int value )
{
    typeButtonGroup->blockSignals(true);
    typeButtonGroup->setButton(value);
    typeButtonGroup->blockSignals(false);   
}


void objectEditForm::setLineWidth( int value )
{
    widthSlider->setValue(value);
    QString str;
    str.sprintf("%d", value);
    widthLabel->setText(str);
}


void objectEditForm::closeEvent( QCloseEvent *e )
{
    ioew_closing();
    e->accept();
}


void objectEditForm::keyPressEvent( QKeyEvent * e )
{
    if (e->key() == Qt::Key_Escape)
	ioew_quit();
    e->ignore();
}
