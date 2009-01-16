/*
 *  midas.c -- Main manual image alignment program.
 *
 *  Original author: James Kremer
 *  Revised by: David Mastronarde   email: mast@colorado.edu
 *
 *  Copyright (C) 1995-2005 by Boulder Laboratory for 3-Dimensional Electron
 *  Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *  Colorado.  See dist/COPYRIGHT for full copyright notice.
 *
 *  $Id$
 *  Log at end of file
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "midas.h"
#include "mrcc.h"
#include "b3dutil.h"
#include "math.h"
#include <qmenu.h>
#include <qmenubar.h>
#include <qgridlayout.h>
#include <qstringlist.h>
#include <qregexp.h>
#include <qlayout.h>
#include <QDoubleSpinBox>
//Added by qt3to4:
#include <QKeyEvent>
#include <QKeySequence>
#include <QLabel>
#include <QFrame>
#include <QCloseEvent>
#include <QAction>
#include "arrowbutton.h"
#include "dia_qtutils.h"
#ifndef NO_IMOD_FORK
#include <unistd.h>
#endif

#define ADD_ACTION(a, b, c) menuActions[c] = a##Menu->addAction(b); \
connect(menuActions[c], SIGNAL(triggered()), a##Mapper, SLOT(map())); \
a##Mapper->setMapping(menuActions[c], c);

#define ADD_ACTION_KEY(a, b, c, d) menuActions[c] = a##Menu->addAction(b); \
menuActions[c]->setShortcut(QKeySequence(d)); \
connect(menuActions[c], SIGNAL(triggered()), a##Mapper, SLOT(map())); \
a##Mapper->setMapping(menuActions[c], c);

MidasView *VW;
int Midas_debug = 0;

#define ARROW_SIZE 19

static void usage(void)
{
     char *pname = "midas";
     QString qstr;

     printf("%s version %s\n", pname, MIDAS_VERSION_STRING);
     imodCopyright();
     qstr.sprintf("Usage: %s [options] <mrc filename> "
	     "[transform filename]\n", pname);
     qstr += "options:\n";
     qstr += "   -g\t\t output global transforms (default"
       " is local)\n";
     qstr += "   -r <filename>\t load reference image file\n";
     qstr += "   -rz <section>\t section # for reference (default 0)\n";
     qstr += "   -p <filename>\t load piece list file for fixing montages\n";
     qstr += "   -c <size list>\t align chunks of sections; list # of sections "
       "in chunks\n",
     qstr += "   -C <size>\t set cache size to given number of "
       "sections\n"; 
     qstr += "   -s <min,max>\t set intensity scaling; min to 0 and"
       " max to 255\n";
     qstr += "   -b <size>\t set initial size for block copies\n";
     qstr += "   -a <angle>\t rotate all images by angle.\n";
     qstr += "   -t <filename>\t load tilt angles from file and use cosine "
       "stretching.\n";
     qstr += "   -o <filename>\t output transforms to given file instead of "
       "input file\n";
     qstr += "   -O <letters>\t two letters for colors of previous/current in"
       " overlay\n";
     qstr += "   -S\t\t use single-buffered visual\n";
     qstr += "   -D\t\t debug mode - do not run in background\n";
     qstr += "   -q\t\t suppress reminder message when fixing edges\n";
#ifdef _WIN32
     dia_puts(LATIN1(qstr));
#else
     printf(LATIN1(qstr));
#endif
     exit(3);
}

int main (int argc, char **argv)
{
  MidasView midasView, *vw;
  FILE *file;
  int i, k;
  bool doubleBuffer = true;
  int styleSet = 0;
  QStringList chunkList;
  bool ok;
  int chunkErr = 0;
  int oarg = 0;

#ifdef NO_IMOD_FORK
  int dofork = 0;
#else
  int dofork = 1;
#endif

  vw = VW = &midasView;

  // Prescan for style and debug flags
  for (i = 1; i < argc; i++) {
    if (argv[i][0] == '-' && argv[i][1] == 's' && argv[i][2] == 't'
	&& argv[i][3] == 'y' && argv[i][4] == 'l' && argv[i][5] == 'e')
      styleSet = 1;
    if (argv[i][0] == '-' && argv[i][1] == 'D')
      dofork = 0;
  }

#ifndef NO_IMOD_FORK
  // Fork before starting Qt application
  if (dofork) {
    if (fork())
      exit(0);
  }
#endif

  QApplication myapp(argc, argv);
  diaSetTitle("Midas");
  b3dSetStoreError(1);

  if (argc < 2)
    usage();

#ifdef __linux
  if (!styleSet)
    QApplication::setStyle("windows");
#endif

  new_view(VW);

  for (i = 1; i < argc ; i++){
    if (argv[i][0] == '-'){
      switch (argv[i][1]){
      case 'r': /* reference image */
	if (argv[i][2] == 'z'){
	  vw->xsec = atoi(argv[++i]);
	}else{
	  vw->refname = argv[++i];
	}
	break;
		    
      case 'p': /* piece list */
	vw->plname = argv[++i];
	break;

      case 'g':
	vw->xtype = XTYPE_XG;
	break;

      case 'C':
	vw->cachein = atoi(argv[++i]);
	break;

      case 'b':
	vw->boxsize = atoi(argv[++i]);
	break;

      case 'a':
	vw->globalRot = atof(argv[++i]);
        vw->rotMode = 1;
	break;

      case 't': /* tilt angles */
	vw->tiltname = argv[++i];
        vw->cosStretch = -1;
        vw->rotMode = 1;
	break;

      case 's':
	sscanf(argv[++i], "%f%*c%f", &(vw->sminin), &(vw->smaxin));
	break;

      case 'D':
	Midas_debug = 1;
	break;

      case 'q':
	vw->quiet = 1;
	break;

      case 'S':
	doubleBuffer = false;
	break;

      case 'c':
        chunkList = QString(argv[++i]).split(",", QString::SkipEmptyParts);
        vw->numChunks = chunkList.count();
        break;

      case 'o':
        vw->oname = strdup(argv[++i]);
        break;

      case 'O':
        oarg = ++i;
        break;

      default:
        printf("Illegal option entered: %s\n", argv[i]);
	usage();
	break;
      }
    }else 
      break;
  }
     
  if (i < argc - 2 || i == argc)
    usage();
     

  /* If there are two args left, the last one is name of transform file */
  if (i == argc - 2) {
    /* It gets freed by a save-as, so need to copy to malloc'd space */
    vw->xname = (char *)malloc(strlen(argv[argc - 1]) + 2);
    strcpy(vw->xname, argv[argc - 1]);
    file = fopen(vw->xname, "r");
    if (file) {
      /* If file opened, close and mark that need to read it */
      fclose(file);
      vw->didsave = -1;
    } else {
      /* Otherwise give warning that new file will be used */
      fprintf(stderr, "Transform file (%s) not found;\n"
	      " transforms will be saved in a "
	      "new file by that name.\n", vw->xname);
    }
  }

  // Process overlay color entry
  if (oarg) {
    if (strlen(argv[oarg]) != 2)
      midas_error("Two letters must be entered with -O: two of r g b c m y",
                  "", 1);
    for (k = 0; k < 3; k++)
      vw->imageForChannel[k] = 0;
    for (k = 0; k < 2; k++) {
      switch (argv[oarg][k]) {
      case 'r':
        vw->imageForChannel[2] += k + 1;
        break;
      case 'g':
        vw->imageForChannel[1] += k + 1;
        break;
      case 'b':
        vw->imageForChannel[0] += k + 1;
        break;
      case 'c':
        vw->imageForChannel[1] += k + 1;
        vw->imageForChannel[0] += k + 1;
        break;
      case 'm':
        vw->imageForChannel[2] += k + 1;
        vw->imageForChannel[0] += k + 1;
        break;
      case 'y':
        vw->imageForChannel[2] += k + 1;
        vw->imageForChannel[1] += k + 1;
        break;
      default:
        midas_error("The letters entered with -O must be two of r g b c m y",
                  "", 1);
      }
    }

    // If there is any overlap one will add to 3
    for (k = 0; k < 3; k++)
      if (vw->imageForChannel[k] > 2)
        midas_error("The two letters entered with -O must specify different "
                    "color channels for previous and current images", "", 1);
  }

  // Check other entries if doing montage mode
  if (vw->plname) {
    if (vw->refname || vw->rotMode || vw->numChunks)
      midas_error("You cannot use the -p option with the ", 
                  (char *)(vw->rotMode ? "-a or -t option." : 
                           (vw->refname ? "-r option." : "-c option.")), 1);

    if (vw->didsave != -1)
      midas_error("The last entry on the line must be the name of"
	      " an existing edge\n correlation displacement file.", "", 1);

    if (vw->xtype == XTYPE_XG)
      dia_puts("The -g option has no effect when fixing montage overlaps.");
    vw->xtype = XTYPE_MONT;
  }	       

  // Check features if doing reference mode or chunk mode
  if (vw->refname || vw->numChunks) {
    if (vw->rotMode)
      dia_puts("The -a or -t options have no effect with alignment to a "
	      "reference section or in chunk mode.");
    vw->rotMode = 0;
    vw->cosStretch = 0;
    if (vw->xtype == XTYPE_XG)
      dia_puts("The -g option has no effect with alignment to a "
	      "reference section or in chunk.");
    if (vw->refname)
      vw->xtype = XTYPE_XREF;
  }

  if (vw->cosStretch && vw->xtype == XTYPE_XG)
    midas_error("Global alignment mode cannot be used with cosine stretching",
                "", 1);

  // If doing chunk mode, get sizes, make sure no zeros, defer further checking
  if (vw->numChunks) {
    if (vw->refname)
      midas_error("Chunk alignment cannot be done in reference alignment mode",
                  "", 1);
    vw->chunk = (struct Midas_chunk *)malloc((vw->numChunks + 1) * 
                                                  sizeof(struct Midas_chunk));
    if (!vw->chunk)
      midas_error("Error getting memory for chunk data.", "", 3);

    vw->chunk[0].start = 0;
    for (k = 0; k < vw->numChunks; k++) {
      vw->chunk[k].size = chunkList[k].toInt(&ok);
      if (vw->chunk[k].size <= 0 || !ok)
        chunkErr = 1;
      vw->chunk[k + 1].start = vw->chunk[k].start + vw->chunk[k].size;
    }

    if (chunkErr || vw->numChunks < 2) 
      midas_error("The -c option must be followed by a comma-separated list",
                  " of the number of sections in each chunk.", 1);
  }

  if (load_view(VW, argv[i]))
    midas_error("Error opening ", argv[i], 3);

  // Increase the default point size if font is specified in points,
  // or if not, increase the pixel size.  GAVE IT UP 10/12/08
  /*  QFont newFont = QApplication::font();
  float pointSize = newFont.pointSizeFloat();
  if (pointSize > 0) {
    if (Midas_debug)
      fprintf(stderr, "Default font point size %f\n", pointSize);
    newFont.setPointSizeFloat(pointSize * 1.2);
  } else {
    int pixelSize = newFont.pixelSize();
    if (Midas_debug)
      fprintf(stderr, "Default font pixel size %d\n", pixelSize);
    newFont.setPixelSize((int)floor(pixelSize * 1.2 + 0.5));
  }
  QApplication::setFont(newFont); */

  // Create the components (window creates the GL widget)
  vw->midasSlots = new MidasSlots();
  vw->midasWindow = new MidasWindow(doubleBuffer);
  //myapp.setMainWidget(vw->midasWindow);

  vw->midasWindow->show();
  vw->midasWindow->setFocus();

  return myapp.exec();
}


MidasWindow::MidasWindow(bool doubleBuffer, QWidget * parent, Qt::WFlags f)
  : QMainWindow(parent, f)
{
  int newWidth, newHeight, xleft, ytop;
  int commandWidth, commandHeight;

  // Yes this goes out of scope, but this was a prototype for 3dmod
  QAction *menuActions[LAST_MENU_ID];

  // Create the menus in the menubar
  QMenu *fileMenu = menuBar()->addMenu("&File");
  QMenu *editMenu = menuBar()->addMenu("&Edit");
  menuBar()->addSeparator();
  QMenu *helpMenu = menuBar()->addMenu("&Help");
  QSignalMapper *fileMapper = new QSignalMapper(this);
  QSignalMapper *editMapper = new QSignalMapper(this);
  QSignalMapper *helpMapper = new QSignalMapper(this);
  connect(fileMapper, SIGNAL(mapped(int)), VW->midasSlots, 
          SLOT(slotFilemenu(int)));
  connect(editMapper, SIGNAL(mapped(int)), VW->midasSlots, 
          SLOT(slotEditmenu(int)));
  connect(helpMapper, SIGNAL(mapped(int)), VW->midasSlots, 
          SLOT(slotHelpmenu(int)));

  // Create file menu
  ADD_ACTION(file, "&Load transforms", FILE_MENU_LOAD);
  ADD_ACTION_KEY(file, "&Save transforms", FILE_MENU_SAVE, Qt::Key_S);
  ADD_ACTION(file, "Sa&ve transforms as...", FILE_MENU_SAVE_AS);
  ADD_ACTION(file, "Save &contrast-scaled image...", FILE_MENU_SAVE_IMAGE);
  ADD_ACTION(file, "&Transform model...", FILE_MENU_TRANSFORM);
  fileMenu->addSeparator();
  ADD_ACTION(file, "&Quit", FILE_MENU_QUIT);
  if (VW->xtype == XTYPE_XF || VW->xtype == XTYPE_MONT)
    menuActions[FILE_MENU_TRANSFORM]->setEnabled(false);
  
  // Create Edit menu
  ADD_ACTION(edit, "&Store section transform", EDIT_MENU_STORE);
  ADD_ACTION(edit, "&Reset to unit transform", EDIT_MENU_RESET);
  ADD_ACTION(edit, "Re&vert to stored transform", EDIT_MENU_REVERT);
  ADD_ACTION(edit, "&Mirror around X axis", EDIT_MENU_MIRROR);
  menuActions[EDIT_MENU_MIRROR]->setEnabled(VW->xtype != XTYPE_MONT);
  
  // Create Help menu
  ADD_ACTION(help, "&Controls", HELP_MENU_CONTROLS);
  ADD_ACTION(help, "&Hotkeys", HELP_MENU_HOTKEYS);
  ADD_ACTION(help, "&Mouse", HELP_MENU_MOUSE);
  ADD_ACTION(help, "Man &Page", HELP_MENU_MANPAGE);
  helpMenu->addSeparator();
  ADD_ACTION(help, "&About Midas", HELP_MENU_ABOUT);

  // Create main widget control panel
  QWidget *mainbox = new QWidget(this);
  QHBoxLayout *mainlay = new QHBoxLayout(mainbox);
  mainlay->setContentsMargins(2, 2, 2, 2);
  setCentralWidget(mainbox);
  QWidget *outer = new QWidget(mainbox);
  mainlay->addWidget(outer);
  QVBoxLayout *col = new QVBoxLayout(outer);
  col->setSpacing(4);
  col->setContentsMargins(3, 3, 3, 3);

  // Need GLwidget next
  QGLFormat glFormat;
  glFormat.setRgba(true);
  glFormat.setDoubleBuffer(doubleBuffer);
  VW->midasGL = new MidasGL(glFormat, mainbox);
  mainlay->addWidget(VW->midasGL);

  mainlay->setStretchFactor(col, 0);
  mainlay->setStretchFactor(VW->midasGL, 1);

  createSectionControls(col);
  makeSeparator(col, 2);
  createContrastControls(col);
  makeSeparator(col, 2);
  createZoomBlock(col);
  makeSeparator(col, 1);
  createViewToggle(col);
  makeSeparator(col, 2);
  createParameterDisplay(col);

  col->addStretch();

  // set window width from current width plus image width
  QSize comSize = col->sizeHint();
  QSize winSize = sizeHint();
  newWidth = winSize.width() + VW->xsize;
  commandWidth = comSize.width();

  // Set height from max of command area and image height, plus difference
  // between command and window height, which is menu area
  commandHeight = comSize.height();
  newHeight = (winSize.height() - commandHeight) + 
    B3DMAX(commandHeight, VW->ysize);

  // But limit by size of display, allow extra on top for title bar, make
  // sure it is on screen for Windows
  diaLimitWindowSize(newWidth, newHeight);

  QRect pos = geometry();
  xleft = pos.x();
  ytop = pos.y();
  diaLimitWindowPos(newWidth, newHeight, xleft, ytop);

  resize(newWidth, newHeight);
  move(xleft, ytop);

  // This should be a good thing, because widgets were all initialized with
  // extreme numbers
  outer->setFixedWidth(commandWidth);
  setFocusPolicy(Qt::StrongFocus);
}

MidasWindow::~MidasWindow()
{
}

// Process a close event by calling the quit routine and ignoring event
void MidasWindow::closeEvent ( QCloseEvent * e )
{
  VW->midasSlots->slotMidas_quit();
  e->ignore();
}

void MidasWindow::keyPressEvent ( QKeyEvent * e )
{
  VW->midasSlots->midas_keyinput(e);
}

void MidasWindow::keyReleaseEvent ( QKeyEvent * e )
{
  if (e->key() == Qt::Key_Control) {
    VW->ctrlPressed = 0;
    VW->midasGL->manageMouseLabel(" ");
  }
  if (e->key() == Qt::Key_Shift) {
    VW->shiftPressed = 0;
    VW->midasGL->manageMouseLabel(" ");
  }
}

void MidasWindow::makeSeparator(QVBoxLayout *parent, int width)
{
  QFrame *frame = new QFrame();
  parent->addWidget(frame);
  frame->setFrameStyle(QFrame::Plain | QFrame::HLine);
  frame->setLineWidth(width);
}

void MidasWindow::makeTwoArrows(QHBoxLayout *parent, int direction, 
                                int signal, QSignalMapper *mapper, bool repeat)
  
{
  parent->setSpacing(4);
  ArrowButton *arrow = new ArrowButton(direction < 0 ? 
                                       Qt::LeftArrow : Qt::UpArrow, NULL);
  parent->addWidget(arrow);
  arrow->setFixedWidth(ARROW_SIZE);
  arrow->setFixedHeight(ARROW_SIZE);
  arrow->setAutoRepeat(repeat);
  mapper->setMapping(arrow, direction * signal);
  QObject::connect(arrow, SIGNAL(clicked()), mapper, SLOT(map()));

  arrow = new ArrowButton(direction < 0 ? Qt::RightArrow : Qt::DownArrow, 
                          NULL);
  parent->addWidget(arrow);
  arrow->setFixedWidth(ARROW_SIZE);
  arrow->setFixedHeight(ARROW_SIZE);
  arrow->setAutoRepeat(repeat);
  mapper->setMapping(arrow, -direction * signal);
  QObject::connect(arrow, SIGNAL(clicked()), mapper, SLOT(map()));
}

QLabel *MidasWindow::makeArrowRow(QVBoxLayout *parent, int direction, 
                                  int signal, QSignalMapper *mapper, 
                                  bool repeat, QString textlabel, int decimals,
                                  int digits, float value)
{
  char string[32];
  QLabel *label;
  QString str;
  QHBoxLayout *row = diaHBoxLayout(parent);
  makeTwoArrows(row, direction, signal, mapper, repeat);
  
  label = diaLabel(LATIN1(textlabel), NULL, row);
  label->setAlignment(Qt::AlignLeft | Qt::AlignVCenter);
  VW->midasSlots->sprintf_decimals(string, decimals, digits, value);
  str = string;
  label = diaLabel(LATIN1(str), NULL, row);
  label->setAlignment(Qt::AlignRight | Qt::AlignVCenter);
  return (label);
}

QSignalMapper *MidasWindow::makeLabeledArrows(QVBoxLayout *parent, 
                                              QString textlabel,
					      QLabel **outLabel, bool repeat)
{
  QHBoxLayout *row = diaHBoxLayout(parent);
  QSignalMapper *mapper = new QSignalMapper();
  
  makeTwoArrows(row, 1, 1, mapper, repeat);
  
  *outLabel = diaLabel(LATIN1(textlabel), NULL, row);
  (*outLabel)->setAlignment(Qt::AlignLeft | Qt::AlignVCenter);
  //row->setStretchFactor(*outLabel, 5);
  return (mapper);
}

QSpinBox *MidasWindow::makeSpinBoxRow(QHBoxLayout *row, char *labText,
                                      int minz, int maxz)
{
  QSpinBox *spin = (QSpinBox *)diaLabeledSpin
    (0, (float)minz, (float)maxz, 1., labText, NULL,  row);
  spin->setFixedWidth(60);
  return spin;
}

void MidasWindow::createParameterDisplay(QVBoxLayout *col)
{
  int i;
  QLabel *label;
  
  for (i = 0; i < 3; i++) {
    VW->mouseLabel[i] = diaLabel(" ", NULL, col);
    VW->mouseLabel[i]->setAlignment(Qt::AlignCenter);
  }
  VW->midasGL->manageMouseLabel(" ");

  QSignalMapper *paramMapper = new QSignalMapper(col);
  QSignalMapper *incMapper = new QSignalMapper(col);
  VW->wParameter[3] = makeArrowRow
    (col, -1, 4, paramMapper, true, "X translation",
     VW->midasSlots->getParamDecimals(3), VW->midasSlots->getParamDigits(3),
     -1000.0);
  VW->wParameter[4] = makeArrowRow
    (col, 1, 5, paramMapper, true, "Y translation",
     VW->midasSlots->getParamDecimals(4), VW->midasSlots->getParamDigits(4),
     -1000.0);
  VW->wIncrement[2] = makeArrowRow
    (col, 1, 3, incMapper, false, "   increment ",
     VW->midasSlots->getIncDecimals(2),  VW->midasSlots->getIncDigits(2),
     VW->increment[2]);
  if (VW->xtype != XTYPE_MONT) {
    makeSeparator(col, 1);
    VW->wParameter[0] = makeArrowRow
      (col, -1, 1, paramMapper, true, "Rotation    ",
       VW->midasSlots->getParamDecimals(0), VW->midasSlots->getParamDigits(0),
       -179.);
    VW->wIncrement[0] = makeArrowRow
      (col, 1, 1, incMapper, false, "   increment",
       VW->midasSlots->getIncDecimals(0), VW->midasSlots->getIncDigits(0),
       VW->increment[0]);
    makeSeparator(col, 1);

    VW->wParameter[1] = makeArrowRow
      (col, 1, 2, paramMapper, true, "Magnification",
       VW->midasSlots->getParamDecimals(1),  VW->midasSlots->getParamDigits(1),
       1.0);
    VW->wParameter[2] = makeArrowRow
      (col, 1, 3, paramMapper, true, "Stretch      ",
       VW->midasSlots->getParamDecimals(2),  VW->midasSlots->getParamDigits(2),
       1.0);
    VW->wIncrement[1] = makeArrowRow
      (col, 1, 2, incMapper, false, "   factor    ", 
       VW->midasSlots->getIncDecimals(1),  VW->midasSlots->getIncDigits(1),
       VW->increment[1]);

  }     

  // Connect mappers to slots
  QObject::connect(paramMapper, SIGNAL(mapped(int)), VW->midasSlots,
		   SLOT(slotParameter(int)));
  QObject::connect(incMapper, SIGNAL(mapped(int)), VW->midasSlots,
		   SLOT(slotIncrement(int)));

  if (VW->xtype != XTYPE_MONT) {
    VW->anglescale = diaSlider(-900, 900, 100, 0, NULL, col);
    VW->anglescale->setPageStep(10);
    QHBoxLayout *slideBox = diaHBoxLayout(col);
    QLabel *slideName = diaLabel("Stretch Angle", NULL, slideBox);
    slideName->setAlignment(Qt::AlignLeft);
    VW->anglelabel = diaLabel("0.0", NULL, slideBox);
    VW->anglelabel->setAlignment(Qt::AlignRight);
    QObject::connect(VW->anglescale, SIGNAL(valueChanged(int)),
		     VW->midasSlots, SLOT(slotAngle(int)));

    if (VW->rotMode) {
      makeSeparator(col, 1);
      QHBoxLayout *globRotBox = diaHBoxLayout(col);
      //QLabel *globLabel = diaLabel("Global rotation", NULL, globRotBox);
      //globLabel->setAlignment(Qt::AlignLeft);

      VW->globRotSpin = (QDoubleSpinBox *)diaLabeledSpin
        (1, -180., 180., 1., "Global rotation", NULL, globRotBox);
      VW->globRotSpin->setFixedWidth
        (VW->globRotSpin->fontMetrics().width("-180.0000"));
      VW->globRotSpin->setValue((int)floor(VW->globalRot * 10. + 0.5));
      QObject::connect(VW->globRotSpin, SIGNAL(valueChanged(double)), 
                       VW->midasSlots, SLOT(slotGlobRot(double)));
      QCheckBox *check = diaCheckBox("Mouse shifts X only", NULL, col);
      check->setChecked(false);
      QObject::connect(check, SIGNAL(toggled(bool)), VW->midasSlots,
                       SLOT(slotConstrainMouse(bool)));
      if (VW->cosStretch) {
        check = diaCheckBox("Apply cosine stretch", NULL, col);
        check->setChecked(false);
        QObject::connect(check, SIGNAL(toggled(bool)), VW->midasSlots,
                         SLOT(slotCosStretch(bool)));
        VW->cosStretch = 0;

        QHBoxLayout *tiltOffBox = diaHBoxLayout(col);
        VW->tiltOffSpin = (QDoubleSpinBox *)diaLabeledSpin
          (1, -90., 90., 1., "Tilt angle offset", NULL, tiltOffBox);
        VW->tiltOffSpin->setFixedWidth
          (VW->tiltOffSpin->fontMetrics().width("-180.0000"));
        VW->tiltOffSpin->setValue(0.);
        QObject::connect(VW->tiltOffSpin, SIGNAL(valueChanged(double)), 
                         VW->midasSlots, SLOT(slotTiltOff(double)));
      }
    }

  } else {
    makeSeparator(col, 2);

    VW->wMeanerr = diaLabel("Mean error: 100.00", NULL, col);
    VW->wMeanerr->setAlignment(Qt::AlignLeft);
    label = diaLabel("Top 4 errors:", NULL, col);
    label->setAlignment(Qt::AlignLeft);

    QGridLayout *grid = new QGridLayout();
    col->addLayout(grid);
    grid->setSpacing(5);
    QSignalMapper *mapper = new QSignalMapper();
    for (i = 0; i < 4; i++) {
      VW->wToperr[i] = new QPushButton("X 199: 50.00  ");
      grid->addWidget(VW->wToperr[i], i / 2, i % 2);
      mapper->setMapping(VW->wToperr[i], i);
      QObject::connect(VW->wToperr[i], SIGNAL(clicked()), mapper, SLOT(map()));
      VW->wToperr[i]->setFocusPolicy(Qt::NoFocus);
    }
    QObject::connect(mapper, SIGNAL(mapped(int)), 
		     VW->midasSlots, SLOT(slotTop_error(int)));

    VW->wCurerr = diaLabel("This edge: -50.00, -50.00", NULL, col);
    VW->wCurerr->setAlignment(Qt::AlignLeft);
    VW->wLeaverr = diaLabel("Leave-out: -50.00, -50.00", NULL, col);
    VW->wLeaverr->setAlignment(Qt::AlignLeft);

    QPushButton *button = diaPushButton("Apply Leave-out Error", NULL, col);
    QObject::connect(button, SIGNAL(clicked()), VW->midasSlots,
		     SLOT(slotLeave_out()));
  }

}

void MidasWindow::createSectionControls(QVBoxLayout *parent)
{
  QHBoxLayout *row;
  QVBoxLayout *col = parent;
  QLabel *label;
  int maxz;

  // Reference section text box
  if (VW->xtype != XTYPE_MONT) {
    row = diaHBoxLayout(col);
    maxz = (VW->xtype == XTYPE_XREF) ? VW->refzsize : VW->zsize;
    VW->refSpin = makeSpinBoxRow(row, "Reference Sec. ", 1, maxz);
    QObject::connect(VW->refSpin, SIGNAL(valueChanged(int)),
                     VW->midasSlots, SLOT(slotRefValue(int)));
  }

  // Current section text box
  row = diaHBoxLayout(col);
  maxz = (VW->xtype == XTYPE_MONT) ? VW->maxzpiece + 1 : VW->zsize;
  VW->curSpin = makeSpinBoxRow(row, "Current Sec. ", 1, maxz);
  QObject::connect(VW->curSpin, SIGNAL(valueChanged(int)),
                   VW->midasSlots, SLOT(slotCurValue(int)));
  
  if (VW->numChunks) {

    // Chunk mode: just add a chunk spin box
    row = diaHBoxLayout(col);
    VW->chunkSpin = makeSpinBoxRow(row, "Current Chunk ", 2, VW->numChunks);
    QObject::connect(VW->chunkSpin, SIGNAL(valueChanged(int)),
                     VW->midasSlots, SLOT(slotChunkValue(int)));
  
  } else if (VW->xtype != XTYPE_XREF && VW->xtype != XTYPE_MONT) {
    
    // Non-montage: the difference checkbox and mode label

    VW->difftoggle  = diaCheckBox("Keep Curr - Ref diff = 1", NULL, col);
    VW->difftoggle->setChecked(true);
    QObject::connect(VW->difftoggle, SIGNAL(toggled(bool)), 
		     VW->midasSlots, SLOT(slotKeepdiff(bool)));

    if (VW->xtype == XTYPE_XF)
      label = diaLabel ("Local Alignment Mode", NULL, col);
    else
      label = diaLabel ("Global Alignment Mode", NULL, col);
    label->setAlignment(Qt::AlignCenter);

  } else if (VW->xtype == XTYPE_MONT) { 

    // Make the X and Y radio buttons, and edge number textbox
    row = diaHBoxLayout(col);
    row->setSpacing(3);
    VW->edgeGroup = new QButtonGroup();
    VW->wXedge = diaRadioButton("X", NULL, VW->edgeGroup, row, 0, NULL);
    VW->wYedge = diaRadioButton("Y", NULL, VW->edgeGroup, row, 1, NULL);
    QObject::connect(VW->edgeGroup, SIGNAL(buttonClicked(int)),
		     VW->midasSlots, SLOT(slotXory(int)));

    VW->edgeSpin = makeSpinBoxRow(row, "Edge ", 1, VW->maxedge[VW->xory]);
    QObject::connect(VW->edgeSpin, SIGNAL(valueChanged(int)),
                     VW->midasSlots, SLOT(slotEdgeValue(int)));

    VW->midasSlots->manage_xory(VW);
    
  } else {
    label = diaLabel("Reference Alignment Mode", NULL, col);
    label->setAlignment(Qt::AlignCenter);
  }
}

void MidasWindow::createZoomBlock(QVBoxLayout *parent)
{
  QString str;

  QSignalMapper *mapper = makeLabeledArrows(parent, "Zoom  1.00", 
					    &VW->zoomlabel, false);
  QObject::connect(mapper, SIGNAL(mapped(int)), VW->midasSlots,
		     SLOT(slotZoom(int)));

  str.sprintf("Block size %2d", VW->boxsize);
  mapper = makeLabeledArrows(parent, str, &VW->blocklabel, false);
  QObject::connect(mapper, SIGNAL(mapped(int)), VW->midasSlots,
		     SLOT(slotBlock(int)));
  
  QCheckBox *check = diaCheckBox("Interpolate", NULL, parent);
  check->setChecked(VW->fastip == 0);
  QObject::connect(check, SIGNAL(toggled(bool)), VW->midasSlots,
		     SLOT(slotInterpolate(bool)));
}

void MidasWindow::createViewToggle(QVBoxLayout *parent)
{

  VW->overlaytoggle = diaCheckBox("Overlay view", NULL, parent);
  VW->overlaytoggle->setChecked(VW->vmode == MIDAS_VIEW_COLOR);
  QObject::connect(VW->overlaytoggle, SIGNAL(toggled(bool)), VW->midasSlots,
		   SLOT(slotOverlay(bool)));

  QPushButton *button = diaPushButton("Toggle Ref/Cur", NULL, parent);
  QObject::connect(button, SIGNAL(pressed()), VW->midasSlots,
		     SLOT(slotAlign_arm()));
  QObject::connect(button, SIGNAL(released()), VW->midasSlots,
		     SLOT(slotAlign_disarm()));
}

void MidasWindow::createContrastControls(QVBoxLayout *parent)
{
  QHBoxLayout *row;

  row = diaHBoxLayout(parent);
  row->setSpacing(3);
  diaLabel("Black", NULL, row);
  VW->wBlacklevel = diaSlider(0, 255, 1, 0, NULL, row);
  VW->wBlackval = diaLabel("000", NULL, row);
  QObject::connect(VW->wBlacklevel, SIGNAL(valueChanged(int)),
		   VW->midasSlots, SLOT(slotBlacklevel(int)));
  QObject::connect(VW->wBlacklevel, SIGNAL(sliderPressed()),
		   VW->midasSlots, SLOT(slotBlackPressed()));
  QObject::connect(VW->wBlacklevel, SIGNAL(sliderReleased()),
		   VW->midasSlots, SLOT(slotBlackReleased()));

  row = diaHBoxLayout(parent);
  row->setSpacing(3);
  diaLabel("White", NULL, row);
  VW->wWhitelevel = diaSlider(0, 255, 1, 255, NULL, row);
  row->addWidget(VW->wWhitelevel);
  VW->wWhiteval = diaLabel("255", NULL, row);
  QObject::connect(VW->wWhitelevel, SIGNAL(valueChanged(int)),
		   VW->midasSlots, SLOT(slotWhitelevel(int)));
  QObject::connect(VW->wWhitelevel, SIGNAL(sliderPressed()),
		   VW->midasSlots, SLOT(slotWhitePressed()));
  QObject::connect(VW->wWhitelevel, SIGNAL(sliderReleased()),
		   VW->midasSlots, SLOT(slotWhiteReleased()));
     
  QCheckBox *check = diaCheckBox("Apply to only one sec.", NULL, parent);
  check->setChecked(false);
  QObject::connect(check, SIGNAL(toggled(bool)), VW->midasSlots,
		     SLOT(slotApplyone(bool)));

  VW->reversetoggle = diaCheckBox("Reverse contrast", NULL, parent);
  VW->reversetoggle->setChecked(false);
  QObject::connect(VW->reversetoggle, SIGNAL(toggled(bool)), VW->midasSlots,
		     SLOT(slotReverse(bool)));

  QPushButton *button = diaPushButton("Auto Contrast", NULL, parent);
  QObject::connect(button, SIGNAL(clicked()), VW->midasSlots,
		     SLOT(slotAutoContrast()));
}



void midas_error(const char *tmsg, const char *bmsg, int retval)
{
  QString str;
  /* if (VW->midasWindow == NULL)
    fprintf(stderr, "%s %s\n", tmsg, bmsg);
    else { */
    str.sprintf("%s %s\n", tmsg, bmsg);
    dia_err(LATIN1(str));
    //}  
  if (retval)
    exit(retval);

  return;
}

/*

$Log$
Revision 3.23  2009/01/15 16:30:19  mast
Qt 4 port

Revision 3.22  2008/11/02 15:00:45  mast
Changed so cosine stretch option is not on by default

Revision 3.21  2008/10/13 04:36:00  mast
Added cosine stretch, switched to 3 lines of mouse reminders, got rid of
larger font

Revision 3.20  2007/10/03 21:36:10  mast
Added ImodAssistant help object

Revision 3.19  2006/06/26 15:48:19  mast
    Added autocontrast function

    Revision 3.18  2006/05/20 16:07:56  mast
    Changes to allow mirroring around X axis

    Revision 3.17  2006/05/13 22:52:52  mast
    Changes to allow overlay colors to be specified

    Revision 3.16  2006/03/01 19:16:03  mast
    Fixed bug in setting window size and eliminated debug output, called 
    library routines for limiting window size and position

    Revision 3.15  2005/03/10 21:04:14  mast
    Added -q option for use from etomo

    Revision 3.14  2004/11/05 18:53:22  mast
    Include local files with quotes, not brackets

    Revision 3.13  2004/10/25 18:51:52  mast
    Added optoin to output to different file from input file

    Revision 3.12  2004/07/12 18:42:30  mast
    Changes for chunk alignment and for switching to spin boxes

    Revision 3.11  2004/07/07 19:25:31  mast
    Changed exit(-1) to exit(3) for Cygwin

    Revision 3.10  2004/05/28 18:56:13  mast
    needed to parse gloabal rotation as float

    Revision 3.9  2003/12/17 21:44:19  mast
    Changes to implement global rotations

    Revision 3.8  2003/11/01 16:43:10  mast
    changed to put out virtually all error messages to a window

    Revision 3.7  2003/06/20 19:35:41  mast
    Connected top error buttons to mapper

    Revision 3.6  2003/05/26 01:02:33  mast
    Added label to show mouse action

    Revision 3.5  2003/02/28 21:36:08  mast
    connect to focusLost signal of ToolEdit

    Revision 3.4  2003/02/28 18:10:58  mast
    Fix include fiddling

    Revision 3.3  2003/02/27 23:06:51  mast
    Fiddling with includes some more

    Revision 3.2  2003/02/27 20:19:10  mast
    Changes in includes for Windows

    Revision 3.1  2003/02/10 20:49:57  mast
    Merge Qt source

    Revision 1.1.2.4  2003/01/30 01:10:25  mast
    Move fork to before starting application

    Revision 1.1.2.3  2003/01/26 23:20:33  mast
    using new library

    Revision 1.1.2.2  2002/12/06 19:05:01  mast
    Changes for binary file reading under windows

    Revision 1.1.2.1  2002/12/05 03:13:02  mast
    New Qt version

    Revision 3.4  2002/11/05 23:54:24  mast
    Changed to get a visual then pass it to GLw.

    Revision 3.3  2002/11/05 23:29:13  mast
    Changed to call imodCopyright

    Revision 3.2  2002/08/19 04:46:10  mast
    Changed number of columns in edge number text box to 4

*/
