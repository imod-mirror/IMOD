
char *Imod_menus_help[] = {
"3dmod: Help for Information Window Menus\n",
"------------------------------------------------------------------------\n",
"The File Menu:\n",
"     New Model  \t\tClears current model and creates a new one.\n",
"     Open Model \t\tLoads a new model file.\n",
"     Save Model \t\tSaves model file to current file name.\n",
"     Save Model as...\tSaves model file, and prompts for a name.\n",
"     Write Model as ---> Writes model to one of the following formats.\n",
"\t\t\t     Imod - native model format.\n",
"\t\t\t     Wimp - Used on BL3DEMC VAX machine.\n",
"\t\t\t     NFF  -\n",
"\t\t\t     Synu - San Diego format.\n",
"     Memory to TIF... \tSaves entire color image from memory to TIFF file.\n"
"     ----------\n",
"     Quit \t\t\tExit program.\n",
"\n",
"The Edit Menu:\n",
"     Model-->\n",
"\tHeader... \tSet: whether model will be drawn or not (t),\n",
"\t\t\t     the Z scale for viewing the model,\n",
"\t\t\t     the resolution, or spacing between points,\n",
"\t\t\t     the pixel size in real units (nm, um, mm).\n"
"\tOffsets... \tShift model data in X, Y, and Z.\n",
"\tClean \t\tDelete all objects that have no points.\n",
"\n",
"     Object-->\n",
"\tNew \t\tCreate a new object.\n",
"\tDelete \t\tDelete current object.\n",
"\tType... \t\tEdit Object type.\n",
"\tColor... \t\tOpens requester for changing object color.\n",
"\tMove... \t\tMove all contours in object to a different object.\n",
"\tInfo \t\tPrint total volume and surface area of object.\n",
"\tClean \t\tDelete empty contours from object.\n",
"\tBreak by Z \tBreak all contours in object into separate ones by Z.\n",
"\tRenumber.. \tMove the current object to a new object number.\n",
"\n",
"     Surface-->\n",
"\tNew \t\tCreates a new contour on a new surface. (N)\n",
"\tGo To... \t\tGo to a different surface.\n",
"\tMove... \t\tMove contours to new surfaces within object.\n",
"\n",
"     Contour-->\n",
"\tNew \t\tCreates a new contour.   (n)\n",
"\tDelete \t\tDeletes current contour  (D)\n",
"\tMove... \t\tMove current contour to a new object #.\n",
"\tCopy... \t\tCopy contours to new object or new Z level\n",
"\tSort \t\tSort all contours in object by their Z values.\n",
"\t----------\n",
"\tBreak... \t\tBreak a contour into two contours.\n",
"\tJoin... \t\tJoin two contours together.\n",
"\tBreak by Z \tBreak a closed contour into separate ones by Z.\n"
"\tFill in Z \tAdd point at each Z level skipped by existing points.\n" 
"\tLoopback \tAdd duplicate points in reverse to make a complex cap.\n" 
"\tInvert \t\tInvert the order of points in the current contour.\n",
"\t----------\n",
"\tInfo \t\tPrint area and length of current contour.\n",
"\tAuto... \t\tOpen window for making new contours using threshold.\n",
"\tType... \t\tSelect a surface, time index or label for a contour.\n",
"\n",
"     Point-->\n",
"\tDelete \t\tDelete Current point. (Delete)\n",
"\tSize... \t\tSet size of individual points.\n",
"\tDistance \tShow distance between model points.\n",
"\tValue \t\tShow current voxel value.\n",
"\tSort by dist \tSort points in contour by interpoint distance.\n",
"\tSort by Z \tSort points in contour by their Z values.\n",
"\n",
"     Image -->\n",
"\tFlip \t\tExchange Y and Z dimensions of the image data.\n",
"\tProcess... \tProcess images by filtering.\n"
"\tReload... \tReload image data with current contrast.\n",
"\tFill Cache \tFill the image cache if there is one.\n",
"\tCache Filler... \tOpen window to control cache filling.\n",
"\n",
"     Movies...\t\tOpen window to control range and speed of movies.\n",
"     Options...\t\tOpen window to set program preferences.\n",
"\n",
"The Image Menu:\n",
"     ZaP \t\tOpen a Zoom and Pan modeling window.\n",
"     XYZ \t\tOpen the XYZ slice viewing window.\n",
"     Slicer \t\tOpen Window showing a sliced view of the\n",
"\t\t\t     image data. (\\)\n",
"     Model View \t\tOpen a 3dmod model view (3dmodv) window. (v)\n",
"     Pixel View \t\tView pixel values in a grid.\n",
"     Graph \t\tOpen the Image Graph window. (G)\n",
"     Tumbler \t\tOpen 3-D projection window.\n",
"",
 NULL};
