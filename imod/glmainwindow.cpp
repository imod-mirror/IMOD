/* 
 *  glmainwindow.cpp  -  Implementation of a main window subclass and a 
 *                       GL widget subclass that lives inside the main window
 */
/*  $Author$

    $Date$

    $Revision$

    $Log$
*/

#include <glmainwindow.h>
#include <stdio.h>

GLMainWindow::GLMainWindow(bool rgba, 
                     bool doubleBuffer, QWidget * parent,
                     const char * name, WFlags f) 
  : QMainWindow(parent, name, f)
{
  createGLWidget(rgba, doubleBuffer);
}

void GLMainWindow::createGLWidget(bool rgba, bool doubleBuffer)
{
  QGLFormat glFormat;
  glFormat.setRgba(rgba);
  glFormat.setDoubleBuffer(doubleBuffer);
  mGLw = new MainGLWidget(glFormat, this);
  
  // Set it as main widget, set focus
  setCentralWidget(mGLw);
  setFocusPolicy(QWidget::StrongFocus);
}


GLMainWindow::~GLMainWindow()
{

}

MainGLWidget::MainGLWidget(QGLFormat inFormat, GLMainWindow * parent,
             const char * name)
  : QGLWidget(inFormat, (QWidget *)parent, name)
{
  if (!format().rgba() && inFormat.rgba())
    fprintf(stderr, "Xyz warning: window is color index mode even though rgb "
                "was requested\n");
  if (format().rgba() && !inFormat.rgba())
    fprintf(stderr, "Xyz warning: window is rgb mode even though color index "
                "was requested\n");

  if (format().doubleBuffer() && !inFormat.doubleBuffer())
    fprintf(stderr, "Xyz warning: Double buffering is being used even "
	    "though\n  single buffering was requested\n");
  if (!format().doubleBuffer() && inFormat.doubleBuffer())
    fprintf(stderr, "Xyz warning: Single buffering is being used even "
	    "though\n  double buffering was requested\n");

  mMousePressed = false;
  mWin = parent;
}

void MainGLWidget::mousePressEvent(QMouseEvent * e )
{
  mMousePressed = true;
  mWin->mousePressEvent(e);
}

void MainGLWidget::mouseReleaseEvent ( QMouseEvent * e )
{
  mMousePressed = false;
  mWin->mouseReleaseEvent(e);
}

void MainGLWidget::mouseMoveEvent ( QMouseEvent * e )
{
  if (mMousePressed)
    mWin->mouseMoveEvent(e);
}

MainGLWidget::~MainGLWidget()
{

}
 
