SOURCES	+= autox.c b3dfile.c b3dgfx.c control.c imod_cachefill.c imod_client_message.c imod_cont_copy.c imod_cont_edit.c imod_display.c imod_edit.c imod_info.c imod_info_cb.c imod_input.c imod_io.c imod_iscale.c imod_menu.c imod_model_draw.c imod_model_edit.c imod_moviecon.c imodplug.c imodv_depthcue.c imodv_gfx.c imodview.c imodv_image.c imodv_input.c imodv_light.c imodv_menu.c imodv_modeled.c imodv_movie.c imodv_objed.c imodv_ogl.c imodv_stereo.c imodv_views.c imod_workprocs.c iproc.c pixelview.c samplemeansd.c sliceproc.c slicer.c wprint.c xgraph.c ximodv.c xtilt.c xtum.c xyz.c xzap.c imod.cpp imod_object_edit.cpp imodv_control.cpp imodv.cpp 
HEADERS	+= autox.h b3dgfx.h hotkey.h imod_client_message.h imod.h imod_info.h imod_input.h imod_io.h imodP.h imodv.h iproc.h keypad.h menus.h options.h sliceproc.h slicer.h sslice.h xxyz.h xzap.h imod_object_edit.h imodv_control.h

# ../include/imodel.h ../include/imodi.h ../include/dia.h ../include/iobj.h \
# ../include/icont.h ../include/imesh.h ../include/ipoint.h ../include/imat.h \
# ../include/iplane.h ../include/iview.h ../include/hvemtypes.h ../include/ilist.h
#The above did not create the desired dependencies.

MOC_DIR = tmp
OBJECTS_DIR = tmp
UI_DIR = tmp

TARGET = imod

FORMS	= object_edit.ui formv_control.ui 
IMAGES	= uparrow.png downarrow.png rightarrow.png leftarrow.png 
TEMPLATE	=app
CONFIG	+= qt x11 open_gl
DBFILE	= imod.db
LANGUAGE	= C++
include (qconfigure)

helptarget.target = imodhelp.h
helptarget.commands = ./mkargv imodhelp imodhelp.h "Imod_help_text[]"
helptarget.depends = imodhelp mkargvtarget

mkargvtarget.target = mkargv
mkargvtarget.depends = mkargv.o
mkargvtarget.commands = gcc -o mkargv mkargv.o

menutarget.target = tmp/imod_menu.o
menutarget.depends = imodhelp.h

QMAKE_EXTRA_UNIX_TARGETS += mkargvtarget helptarget menutarget
#TARGETDEPS = imodhelp.h
