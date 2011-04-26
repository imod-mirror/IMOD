include ../configure
# Makefile for the python components, installs scripts to bin and modules
# to pylib
#
# $Id$
# No more Log
#

SCRIPTS = vmstopy tomostitch edgepatches fitpatches sirtsetup splitblend \
setupcombine splittilt imodcpuload gpumonitor splitcombine copytomocoms \
squeezevol tomosnapshot
MODULES = imodpy.py pysed.py supermont.py pip.py

all:

install:  $(CMDWRAPPERS)
	mkdir -p $(INSTDIR)/pylib
	$(CP) $(SCRIPTS) $(INSTBINDIR)
	$(CP) $(MODULES) $(INSTDIR)/pylib

cmdwrappers:
	../scripts/copywrappers $(INSTBINDIR) . $(SCRIPTS)

clean:
	$(FIND) . -type f -name "*~"   -exec rm "{}" \;