SOURCES += midas.cpp slots.cpp file_io.cpp graphics.cpp transforms.cpp \
        amat_to_rotmagstr.cpp gaussj.cpp arrowbutton.cpp

HEADERS += midas.h graphics.h slots.h arrowbutton.h

MOC_DIR = tmp
OBJECTS_DIR = tmp

TEMPLATE = app
CONFIG += qt open_gl debug warn_on

INCLUDEPATH += ../include
LIBS = -L../buildlib -limod -liimod -ltiff -lGL
DEFINES += _FILE_OFFSET_BITS=64
