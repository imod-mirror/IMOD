/* 
 *  glmainwindow.h  -  header for a main window subclass and a 
 *                       GL widget subclass that lives inside the main window
 */
/*  $Author$

    $Date$

    $Revision$

    $Log$
*/

#ifndef GLMAINWINDOW_H
#define GLMAINWINDOW_H

// Include the essentials
#include <qmainwindow.h>
#include <qgl.h>

// Forward declare the widget
class MainGLWidget;

class GLMainWindow : public QMainWindow
{
  Q_OBJECT

    public:
  GLMainWindow(bool rgba, bool doubleBuffer, QWidget * parent = 0, 
	       const char * name = 0, 
	       WFlags f = Qt::WDestructiveClose || Qt::WType_TopLevel) ;
  ~GLMainWindow();

  virtual void initializeGL() {};
  virtual void paintGL() {};
  virtual void resizeGL( int wdth, int hght ) {};
  virtual void mousePressEvent(QMouseEvent * e ) {};
  virtual void mouseReleaseEvent ( QMouseEvent * e ) {};
  virtual void mouseMoveEvent ( QMouseEvent * e ) {};
  void createGLWidget(bool rgba, bool doubleBuffer);

  MainGLWidget *mGLw;

 private:

};

class MainGLWidget : public QGLWidget
{
  Q_OBJECT

    public:
  MainGLWidget(QGLFormat format, GLMainWindow * parent = 0,
        const char * name = 0);
  ~MainGLWidget();
 
 protected:
  void initializeGL() {mWin->initializeGL();};
  void paintGL() {mWin->paintGL();};
  void resizeGL( int wdth, int hght ) {mWin->resizeGL(wdth, hght);};
  void mousePressEvent(QMouseEvent * e );
  void mouseReleaseEvent ( QMouseEvent * e );
  void mouseMoveEvent ( QMouseEvent * e );

 private:
  GLMainWindow *mWin;
  bool mMousePressed;
};

#endif
