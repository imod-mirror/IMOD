#!/bin/bash
# This script will make sure that there is a current python.exe in cygwin
# Newer cygwin has only a cygwin link from python to python2.x.exe
# This will not work when called from non-cygwin applications (java)
doCopy=0
if [[ -e /bin/python.exe && -h /bin/python ]] ; then

    # The IMOD installStub has been copying the link target to python.exe
    # If it already exists, check whether that copy is the same as the current link target
    diff -q /bin/python.exe /bin/python > /dev/null 2>&1
    if [ $? -ne 0 ] ; then 
        echo "The copy of python in /bin/python.exe appears to be out of date"
        echo "Making a new copy of the current python to /bin/python.exe"
        doCopy=1
        cp -Lf /bin/python /bin/python.exe
        if [ $? -ne 0 ] ; then 
            echo "This copy command gave an error"
            exit 1
        fi
    fi
elif [[ ! -e /bin/python.exe ]] ; then

    # Or, if there is no python.exe and there is a link, do the copy
    if [ -h /bin/python ] ; then 
        echo "The Cygwin link to python will not work for IMOD"
        echo "Making a copy of its target that is an actual /bin/python.exe"
        doCopy=1
    else
        echo "There is no Python installed in Cygwin"
        exit 1
    fi
fi
if [ doCopy == 1 ] ; then
    cp -Lf /bin/python /bin/python.exe
    if [ $? -ne 0 ] ; then 
        echo "There was an error copying to /bin/python.exe"
        exit 1
    fi
fi
exit 0
