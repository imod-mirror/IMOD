SOURCES	+= autox.c b3dfile.c b3dgfx.c control.c imod_cachefill.c imod_client_message.c imod_cont_copy.c imod_cont_edit.c imod_display.cpp imod_edit.c imod_info.c imod_info_cb.c imod_input.cpp imod_io.cpp imod_iscale.c imod_menu.cpp imod_model_draw.c imod_model_edit.c imod_moviecon.cpp imodplug.c imodv_depthcue.cpp imodv_gfx.cpp imodview.c imodv_image.cpp imodv_input.cpp imodv_light.cpp imodv_menu.cpp imodv_modeled.cpp imodv_movie.cpp imodv_objed.cpp imodv_ogl.cpp imodv_stereo.cpp imodv_views.cpp imod_workprocs.c iproc.c pixelview.c samplemeansd.c sliceproc.c slicer.c wprint.c xgraph.c xtilt.c xtum.c xyz.cpp xzap.cpp imod.cpp imod_object_edit.cpp imodv_control.cpp imodv.cpp zap_classes.cpp imodv_window.cpp arrowbutton.cpp tooledit.cpp multislider.cpp colorselector.cpp dialog_frame.cpp 
HEADERS	+= autox.h b3dgfx.h hotkey.h imod_client_message.h imod.h imod_info.h imod_input.h imod_io.h imodP.h imodv.h iproc.h keypad.h menus.h sliceproc.h slicer.h sslice.h xxyz.h xzap.h imod_object_edit.h imodv_control.h zap_classes.h imod_display.h imodv_window.h imodv_gfx.h imodv_ogl.h imodv_input.h imodv_menu.h imodv_light.h imodv_stereo.h imodv_depthcue.h imodv_views.h imodv_modeled.h imodv_objed.h imodv_image.h imodv_movie.h arrowbutton.h tooledit.h multislider.h colorselector.h dialog_frame.h qcursor.bits qcursor_mask.bits time_lock.bits unlock.bits lock.bits 



# ../include/imodel.h ../include/imodi.h ../include/dia.h ../include/iobj.h \
# ../include/icont.h ../include/imesh.h ../include/ipoint.h ../include/imat.h \
# ../include/iplane.h ../include/iview.h ../include/hvemtypes.h ../include/ilist.h
#The above did not create the desired dependencies.

MOC_DIR = tmp
OBJECTS_DIR = tmp
UI_DIR = tmp

TARGET = imod

include (qconfigure)

helptarget.target = imodhelp.h
helptarget.commands = ./mkargv imodhelp imodhelp.h "Imod_help_text[]"
helptarget.depends = imodhelp mkargvtarget

mkargvtarget.target = mkargv
mkargvtarget.depends = mkargv.o
mkargvtarget.commands = gcc -o mkargv mkargv.o


QMAKE_EXTRA_UNIX_TARGETS += mkargvtarget helptarget
FORMS	= object_edit.ui formv_control.ui formv_movie.ui formv_modeled.ui formv_views.ui formv_depthcue.ui formv_objed.ui 
IMAGES	= uparrow.png downarrow.png rightarrow.png leftarrow.png 
TEMPLATE	=app
CONFIG	+= qt x11 opengl
DBFILE	= imod.db
LANGUAGE	= C++
