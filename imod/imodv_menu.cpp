/*  IMOD VERSION 2.50
 *
 *  imodv_menu.cpp -- menu actions for imodv main window.
 *
 *  Original author: James Kremer
 *  Revised by: David Mastronarde   email: mast@colorado.edu
 */

/*****************************************************************************
 *   Copyright (C) 1995-2001 by Boulder Laboratory for 3-Dimensional Fine    *
 *   Structure ("BL3DFS") and the Regents of the University of Colorado.     *
 *                                                                           *
 *   BL3DFS reserves the exclusive rights of preparing derivative works,     *
 *   distributing copies for sale, lease or lending and displaying this      *
 *   software and documentation.                                             *
 *   Users may reproduce the software and documentation as long as the       *
 *   copyright notice and other notices are preserved.                       *
 *   Neither the software nor the documentation may be distributed for       *
 *   profit, either in original form or in derivative works.                 *
 *                                                                           *
 *   THIS SOFTWARE AND/OR DOCUMENTATION IS PROVIDED WITH NO WARRANTY,        *
 *   EXPRESS OR IMPLIED, INCLUDING, WITHOUT LIMITATION, WARRANTY OF          *
 *   MERCHANTABILITY AND WARRANTY OF FITNESS FOR A PARTICULAR PURPOSE.       *
 *                                                                           *
 *   This work is supported by NIH biotechnology grant #RR00592,             *
 *   for the Boulder Laboratory for 3-Dimensional Fine Structure.            *
 *   University of Colorado, MCDB Box 347, Boulder, CO 80309                 *
 *****************************************************************************/
/*  $Author$

$Date$

$Revision$

$Log$
Revision 1.1.2.1  2002/12/17 18:43:58  mast
Qt version

Revision 3.2  2002/12/01 15:31:41  mast
Changes to compile with g++; also made only one background color window
be open at any one time.

Revision 3.1  2002/11/05 23:27:46  mast
Changed copyright notice to use defined lab name and years

*/

#include <qfiledialog.h>
#include "imodv_window.h"
#include <Xm/Xm.h>
#include <dia.h>
#include "imodv.h"
#include "b3dgfx.h"
#include "imodv_gfx.h"
#include "imodv_menu.h"
#include "imodv_input.h"
#include "imodv_control.h"

static void imodv_setbgcolor(Widget w, XtPointer client, XtPointer call);

/* Callback function for setting the background color in
 * the model view window.
 */
static int bgColorOpen = False;
static void imodv_setbgcolor(Widget w, XtPointer client, XtPointer call)
{
  DiaColorCallbackStruct *cbs = (DiaColorCallbackStruct *)call;
  ImodvApp *a = (ImodvApp *)client;

  /* Keep track if it is closed */
  if (cbs->reason == DIA_OK || cbs->reason == DIA_CANCEL)
    bgColorOpen = False;

  if (ImodvClosed) return;

  if (cbs->reason == DIA_SLIDER_DRAG)
    return;
  if (cbs->reason == DIA_SLIDER_CHANGED)
    return;
  if (cbs->reason == DIA_CANCEL)
    return;
  if (cbs->reason == DIA_HELP)
    return;

  a->rbgcolor.red   = cbs->red *   255;
  a->rbgcolor.green = cbs->green * 255;
  a->rbgcolor.blue  = cbs->blue *  255;

  imodvDraw(a);
  return;
}
                                 
/* DNM 12/1/02: make this the single path to opening the window, and have
   it keep track of whether window is open or not, and return if it is */
void imodvMenuBgcolor()
{
  short red, green, blue;
  if (bgColorOpen)
    return;

  red   = Imodv->rbgcolor.red / 256;
  green = Imodv->rbgcolor.green / 256;
  blue  = Imodv->rbgcolor.blue / 256;
  dia_setcolor(red, green, blue, "Imodv background color.",
               imodv_setbgcolor, Imodv);
  bgColorOpen = True;
}


/*********************************/
/* Edit menu dispatch function. */
void imodvEditMenu(int item)
{
  switch(item){
  case VEDIT_MENU_OBJECTS:
    objed(Imodv);
    break;
  case VEDIT_MENU_CONTROLS: /* controls */
    imodv_control(Imodv, 1);
    break;
  case VEDIT_MENU_OBJLIST: /* object list */
    imodvObjectListDialog(Imodv, 1);
    break;
  case VEDIT_MENU_BKG: /* background color */
    imodvMenuBgcolor();
    break;
  case VEDIT_MENU_MODELS: /* models */
    imodvModelEditDialog(Imodv, 1);
    break;
  case VEDIT_MENU_VIEWS: /* views */
    imodvViewEditDialog(Imodv, 1);
    break;
  case VEDIT_MENU_IMAGE: /* image */
    imodvImageEditDialog(Imodv, 1);
    break;
    
  }
}

/*********************************/
/* help menu dispatch function. */
void imodvHelpMenu(int item)
{
  switch(item) {
  case VHELP_MENU_MENUS:
    dia_vasmsg
      ("Imodv Help for menus\n",
       "---------------------------------\n",
       "File\n",
       "    Open Model     - Load a new model to view.\n",
       "    Save Model     - Save the current model.\n",
       "    Save Model As. - Save model under a different name.\n",
       "    Snap RGB As... - Save a snapshot to a specified RGB file.\n",
       "    Snap TIFF As.. - Save a snapshot a specified TIFF file.\n",
       "    Zero Snap File # - Reset the counter for snapshot files to 0.\n",
       "    Movie...       - Program a sequence of displays and save them.\n",
       "    Quit           - Quit this program.\n",
                
       "\nEdit\n",
       "    Objects...     - Open the object edit dialog.\n",
       "    Controls...    - Open the display control dialog.\n",
       "    Object List... - Show objects by name with On/Off buttons.\n",
       "    Background...  - Change the background color.\n",
       "    Models...      - Control the display of multiple models.\n",
       "    Views...       - Open a dialog to save and restore views.\n",
       "    Image...       - Display an image slice on the model.\n",
                
       "\nView - Rendering Options\n",
       "    Double Buffer  - Change between double and single\n",
       "                     buffer visual.\n",
       "    Lighting       - Turn rendering with a light on or off.\n"
       "    Wireframe      - Render data in wireframe only.\n",
       "    Low Res        - Display low resolution mesh.\n",
       "    Stereo...      - Open stereo control dialog.\n",
       "    Depth Cue...   - Control dimming of display with distance.\n",
       NULL);
    break;

  case VHELP_MENU_KEYBOARD:
    dia_vasmsg
      ("Imodv Help for Keyboard Commands\n",
       "----------------------------------------------------------\n",
       "\nKeys   | Command \n",
       "----------------------------------------------------------\n",
       "Arrows | Translate model in x and y\n",
       "Page   | Up and Down keys translate model in z\n",
       "Keypad | Rotates model in x, y and z. the '5' key toggles\n",
       "       | movie mode on/off\n"
       " Esc/q | Quit this program\n",
       "  s    | Toggle stereo mode\n",
       "  S    | Snapshot image as an RGB file to imodvnnnn.rgb\n",
       "Ctrl-S | Snapshot image as a TIFF file to imodvnnnn.tif\n",
       "  o    | Output transformation information\n",
       "  c    | Output clipping plane information\n",
       " -/=   | Decrease/Increase zoom\n",
       " _/+   | Decrease/Increase zoom by big steps\n"
       "  m    | Open movie control window\n",
       "  O    | Open Object Edit window\n",
       "  C    | Open controls window\n",
       "  B    | Open background color window\n",
       "  L    | Open Object List window\n",
       "  M    | Open model selection window\n",
       "  v    | Open view editing window\n",
       "  i    | Open image overlay control window\n",
       "  b    | Toggle double buffering\n",
       "  r    | Toggle low resolution drawing of mesh and spheres\n",
       " g/G   | Increase/Decrease the quality of sphere drawing\n",
       " [/]   | Adjust parallax for stereo viewing\n",
       "  l    | Invert the parallax angle\n",
       " ,/.   | Decrease/Increase rotation increment and thus speed\n",
       " 1/2   | Decrease/Increase time for 4D models\n",
       "  8    | Toggle displaying all models or one model\n",
       " 9/0   | Previous/Next model\n",
       "----------------------------------------------------------\n",
       NULL);
    break;
  
  case VHELP_MENU_MOUSE:
    dia_vasmsg
      ("Imodv Help for Mouse Controls\n",
       "----------------------------------------------------------\n",
       "Left Mouse Button Drag\n",
       "\tThe left mouse button moves the model when held down.\n\n",
       "\tWhen the Ctrl key is held down the left mouse button "
       "moves the current object clipping plane.\n\n",
       "Middle Mouse Button Drag\n",
       "\tThe Middle mouse button rotates the model around an axis "
       "perpendicular to the direction of motion of the mouse.\n\n"
       "\tWhen the shift key is held down the middle ",
       "mouse button rotates the light source instead.\n\n",
       "\tWhen the Ctrl key is held down the middle mouse button "
       "rotates the current object clipping plane instead.\n\n",
       "Right Mouse Button\n",
       "\tThe right mouse button controls the pop up menus when",
       "imodv is started with the -noborder option.\n\n",
       "\tWhen running Model View from Imod, clicking on a point in the "
       "model with the right mouse button will select the nearest, "
       "frontmost point in the model as the current model point within "
       "Imod.\n",
       NULL);
    break;

  case VHELP_MENU_ABOUT:
    dia_vasmsg
      ("Imodv OpenGL Version ",
       VERSION_NAME,
       ", originally written by James Kremer and revised",
       "by David Mastronarde\n",
       "Copyright (C)",COPYRIGHT_YEARS,"by",LAB_NAME1,"\n",LAB_NAME2,
       "& Regents of the University of Colorado\n\n",
       NULL);
    break;
  }
}


/****************************************************************************/
/* The FILE MENU */

// load a specified model (only in standalone model)
int imodvLoadModel()
{
  Imod **tmoda;
  Imod *tmod;
  ImodvApp *a = Imodv;
  int i, ob, co;
  QString qname;
  
  if (ImodvClosed || !a->standalone)
    return -1;

  // Need to release the keyboard because window grabs it on ctrl
  a->mainWin->releaseKeyboard();
  qname = QFileDialog::getOpenFileName(QString::null, QString::null, 0, 0, 
                                       "Select Model file to load:");
  if (qname.isEmpty())
    return 1;

  tmod = imodRead((char *)qname.latin1());
  if (!tmod)
    return(-1);

  /* DNM 6/20/01: find out max time and set current time */
  tmod->tmax = 0;
  for (ob = 0; ob < tmod->objsize; ob++)
    for (co = 0; co < tmod->obj[ob].contsize; co++)
      if (tmod->tmax < tmod->obj[ob].cont[co].type)
        tmod->tmax = tmod->obj[ob].cont[co].type;
  tmod->ctime = tmod->tmax ? 1 : 0;

  tmoda = (Imod **)malloc(sizeof(Imod *) * (a->nm + 1));
  for (i = 0; i < a->nm; i++)
    tmoda[i] = a->mod[i];
  tmoda[i] = tmod;
  if (a->nm)
    free(a->mod);
  a->mod = tmoda;

  /*     a->cm = a->nm; */
  a->nm++;
  /*     a->imod = tmod; */

  /* DNM: changes for storage of object properties in view and 
     relying on default scaling */

  imodViewStore(tmod, 0);
     
  if (!tmod->cview){
    imodViewModelDefault(tmod, tmod->view);
  }else
    imodViewUse(tmod);

  imodvSelectModel(a, a->nm - 1);
  return(0);
}

// Save to the existing filename
void imodvFileSave()
{
  /* DNM: added rename of existing file to backup.  Also eliminated use of
     Imodv pointer in favor of a->, and the double save (!?!), and added
     error checks */

  int len, error;
  char *nfname1;
  FILE *fout = NULL;
  ImodvApp *a = Imodv;
  char *filename = a->imod->fileName;

  /* DNM 8/4/01: store the current view when saving, if appropriate */
  imodvAutoStoreView(a);

  len = strlen(filename)+1;

  nfname1 = (char *)malloc(len + 1);
  sprintf(nfname1, "%s~", filename);
  rename(filename, nfname1);


  if (a->imod->fileName)
    fout = fopen(a->imod->fileName, "wb");

  if (fout){
    a->imod->file = fout;
    error = imodWrite(a->imod, a->imod->file);
    /*        error = imodWrite(Imodv->imod, Imodv->imod->file); */
    fflush(fout);
    fclose(fout);
    a->imod->file = NULL;
    if (!error)
      dia_puts("Model file saved.");
  } else
    error = 1;

  if (error) {

    dia_err("File not saved, bad filename or error;"
            " attempting to restore backup file.");
    rename (nfname1, filename);
  }
  free(nfname1);
}

// Save model to new filename
void imodvSaveModelAs()
{
  /* DNM: added rename of existing file and improved error checks */

  ImodvApp *a = Imodv;
  char *filename;
  FILE *fout;
  int len, error;
  char *nfname1;
  QString qname;
  
  a->mainWin->releaseKeyboard();
  qname = QFileDialog::getSaveFileName (QString::null, QString::null, 0, 0, 
                                        "Select file to save model into:");
  if (qname.isEmpty())
    return;
  filename = strdup(qname.latin1());

  /* DNM 8/4/01: store the current view when saving, if appropriate */
  imodvAutoStoreView(a);

  len = strlen(filename)+1;

  nfname1 = (char *)malloc(len + 1);
  sprintf(nfname1, "%s~", filename);
  rename(filename, nfname1);

  fout = fopen(filename, "w");
  if (fout){
    a->imod->file = fout;
    error = imodWrite(Imodv->imod, fout);
    fflush(fout);
    fclose(fout);
    Imodv->imod->file = NULL;

    if (!error) {
      if (a->imod->fileName)
        free(a->imod->fileName);
      a->imod->fileName = (char *)malloc(len);
      if (a->imod->fileName)
        memcpy(a->imod->fileName, filename, len);
          
      if ((strlen(filename)+1) < IMOD_STRSIZE)
        memcpy(a->imod->name, filename, strlen(filename)+1);
      else
        a->imod->name[0] = 0x00;
          
      dia_puts("Model file saved.");
    }
  } else {
    error = 1;
  }
  if (error) {
    dia_err("Error writing model; attempting to restore backup file");
    rename(nfname1, filename);
  }
  free(nfname1);
  free(filename);
}

// The file menu dispatch function
void imodvFileMenu(int item)
{
  QString qname;

  switch (item) {
  case VFILE_MENU_LOAD:
    if (imodvLoadModel() < 0)
      dia_err("Error reading model file.  No model loaded.");
    break;

  case VFILE_MENU_SAVE:
    imodvFileSave();
    break;

  case VFILE_MENU_SAVEAS:
    imodvSaveModelAs();
    break;

  case VFILE_MENU_SNAPRGB:
  case VFILE_MENU_SNAPTIFF:
    Imodv->mainWin->releaseKeyboard();
    qname = QFileDialog::getSaveFileName (QString::null, QString::null, 0, 0, 
                                          "File to snapshot into:");
    if (qname.isEmpty())
      break;
    imodv_auto_snapshot((char *)qname.latin1(), item == VFILE_MENU_SNAPRGB ? 
                        SnapShot_RGB : SnapShot_TIF);
    break;

  case VFILE_MENU_ZEROSNAP:
    imodvResetSnap();
    break;

  case VFILE_MENU_MOVIE:
    imodvMovieDialog(Imodv, 1);
    break;
 
  case VFILE_MENU_QUIT:
    stereoHWOff();
    imodv_exit(Imodv);
    break;
  }
} 

/****************************************************************************/
// The view menu dispatch function
void imodvViewMenu(int item)
{
  ImodvApp *a = Imodv;
  switch (item) {
  case VVIEW_MENU_DB:
    imodv_setbuffer(a);
    break;

  case VVIEW_MENU_LIGHTING:
    if (a->lighting)
      a->imod->view->world &= ~VIEW_WORLD_LIGHT;
    else
      a->imod->view->world |= VIEW_WORLD_LIGHT;
    a->lighting = 1 - a->lighting;
    a->mainWin->setCheckableItem(VVIEW_MENU_LIGHTING, a->lighting);
    imodvDraw(a);
    break;

  case VVIEW_MENU_WIREFRAME:
    if (a->wireframe)
      a->imod->view->world &= ~VIEW_WORLD_WIREFRAME;
    else 
      a->imod->view->world |= VIEW_WORLD_WIREFRAME;
    a->wireframe = 1 - a->wireframe;
    a->mainWin->setCheckableItem(VVIEW_MENU_WIREFRAME, a->wireframe);
    imodvDraw(a);
    break;
 
  case VVIEW_MENU_LOWRES:
    if (a->lowres)
      a->imod->view->world &= ~VIEW_WORLD_LOWRES;
    else 
      a->imod->view->world |= VIEW_WORLD_LOWRES;
    a->lowres = 1 - a->lowres;
    a->mainWin->setCheckableItem(VVIEW_MENU_LOWRES, a->lowres);
    imodvDraw(a);
    break;

  case VVIEW_MENU_STEREO:
    imodvStereoEditDialog(a, 1);
    break;

  case VVIEW_MENU_DEPTH:
    imodvDepthCueEditDialog(a, 1);
    break;
  }
}


/*
 * imodv plugin menu additions.
 */
#ifdef __linux
#define NOPLUGS
#endif
#ifdef __vms
#define NOPLUGS
#endif
#ifdef SVR3
#define NOPLUGS
#endif


#ifndef NOPLUGS
#include <dlfcn.h>
#include <dirent.h>
#include <string.h>
#endif

static void addImodvViewPlugins(Widget w, ImodvApp *a)
{
#ifndef NOPLUGS
  void *handle;
  void (*fptr)(Widget,ImodvApp *);
  char soname[256];
  char *plugdir = getenv("IMOD_PLUGIN_DIR");
  if (!plugdir) return;

  //  XtVaCreateManagedWidget("", xmSeparatorWidgetClass, w, NULL);

#ifdef IMODV_PLUGIN_GENERAL
  {
    DIR *dirp;
    struct direct *dp;

    dirp = opendir(plugdir);
    if (!dirp) return;
    while ((dp = readdir(dirp)) != NULL) {
      char *ext = dp->d_name + dp->d_namlen - 3;
      if (strcmp(ext, ".so") == 0){
        /* try and load plug */
        sprintf(soname, "%s/%s", plugdir, dp->d_name);
                    
        handle = dlopen(soname, RTLD_LAZY);
        if (!handle)
          continue;
                    
        /* find address of function and data objects */
        fptr = (void (*)(Widget, ImodvApp *))dlsym
          (handle, "imodvPlugInAttach");
        if (!fptr){
          dlclose(handle);
          contunue;
        }
        /* invoke function */
        (*fptr)(w, a);
        printf("loaded plugin : %s", dp->d_name);
      }
    }
    closedir(dirp);
  }
#else
  sprintf(soname, "%s/lineage.so", plugdir);
  /* open the needed object */
  handle = dlopen(soname, RTLD_LAZY);
  if (handle){
    /* find address of function and data objects */
    fptr = (void (*)(Widget, ImodvApp *))dlsym
      (handle, "lineageAttach");

    /* invoke function, passing value of integer as a parameter */
    if (fptr){
      (*fptr)(w, a);
      printf("loaded lineage plugin.\n");
    }
  }

#endif

#endif
  return;
}

// Calls to set the menu items as checked/unchecked
void imodvMenuLight(int value)
{
  Imodv->mainWin->setCheckableItem(VVIEW_MENU_LIGHTING, value);
}

void imodvMenuWireframe(int value)
{
  Imodv->mainWin->setCheckableItem(VVIEW_MENU_WIREFRAME, value);
}

void imodvMenuLowres(int value)
{
  Imodv->mainWin->setCheckableItem(VVIEW_MENU_LOWRES, value);
}
