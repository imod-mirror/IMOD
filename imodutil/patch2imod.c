/*  IMOD VERSION 2.50
 *
 *  $Id$
 *
 *  Author: David Mastronarde  email: mast@colorado.edu
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

#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>

#include <imodel.h>


static int fgetline(FILE *fp, char s[],int limit);
struct Mod_Model *imod_from_patches(FILE *fin, float scale);

main( int argc, char *argv[])
{
     int i;
     FILE *fin, *fout;
     struct Mod_Model *Model;
     float scale = 10.0;
     char *filename;
     struct stat buf;

     if (argc < 2){
	  
	  printf("patch2imod version 1.0 usage:\n");
	  printf("patch2imod [-s scale] patch_file imod_model\n");
	  printf("    Displacements are multiplied by \"scale\" (default %.1f)"
		 " to make vectors.\n", scale);
	  exit(1);

     }


     for (i = 1; i < argc ; i++){
	  if (argv[i][0] == '-'){
	       switch (argv[i][1]){
		    
		  case 's':
		    sscanf(argv[++i], "%f", &scale);
		    break;

		  default:
		    fprintf(stderr, "Illegal argument\n");
		    exit(1);
		    break;
	       }
	  }else
	       break;
     }
     if (i > (argc - 2)){
	  printf("wrong # of arguments\n");
	  printf("patch2imod version 1.0 usage:\n");
	  printf("patch2imod [-s scale] patch_file imod_model\n");
	  exit(1);
     }


     fin = fopen(argv[i++], "r");
     if (!fin){
	  fprintf(stderr, "Couldn't open %s\n", argv[--i]);
	  exit(-1);
     }

     if (!stat(argv[i], &buf)) {
	  filename = (char *)malloc(strlen(argv[i]) + 2);
	  sprintf(filename, "%s~", argv[i]);
	  if (rename(argv[i], filename)) {
	       fprintf(stderr, "Error renaming existing output file to %s\n",
		       filename);
	       exit(1);
	  }
     }

     fout = fopen(argv[i], "w");
     if (!fout){
	  fprintf(stderr, "Couldn't open %s\n", argv[i]);
	  exit(-1);
     }

     Model = (struct Mod_Model *)imod_from_patches(fin, scale);
     
     imodWrite(Model, fout);

     imodFree(Model);
}





#define MAXLINE 128

struct Mod_Model *imod_from_patches(FILE *fin, float scale)
{
     int len;
     int i, npatch;
     
     char line[MAXLINE];
     struct Mod_Model *mod;
     Ipoint *pts;
     int ix, iy, iz;
     int xmin, ymin, zmin, xmax, ymax, zmax;
     float dx, dy, dz;

     fgetline(fin,line,MAXLINE);
     sscanf(line, "%d", &npatch);
     if (npatch < 1) {
	  fprintf(stderr, "Error - implausible number of patches = %d.\n",
		  npatch);
	  exit(1);
     }

     mod = imodNew();     
     if (!mod){
	  fprintf(stderr, "Couldn't get new model\n");
	  return(NULL);
     }
     imodNewObject(mod);
     mod->obj->contsize = npatch;
     mod->obj->cont = imodContoursNew(npatch);
     mod->obj->flags |= IMOD_OBJFLAG_OPEN;
     mod->obj->symbol = IOBJ_SYM_CIRCLE;
     mod->flags |= IMODF_FLIPYZ;
     mod->pixsize = scale;
     xmin = ymin= zmin = 1000000;
     xmax = ymax = zmax = -1000000;
     for (i = 0; i < npatch; i++) {
	  pts = (Ipoint *)malloc(2 * sizeof(Ipoint));
	  mod->obj->cont[i].pts = pts;
	  mod->obj->cont[i].psize = 2;
	  len = fgetline(fin,line, MAXLINE);
	  if (len < 3) {
	       fprintf(stderr, "Error reading file at line %d.\n", i + 1);
	       exit(1);
	  }

	  /* DNM 11/15/01: have to handle either with commas or without,
	     depending on whether it was produced by patchcorr3d or 
	     patchcrawl3d */
	  if (strchr(line, ','))
	       sscanf(line, "%d %d %d %f, %f, %f", &ix, &iz, &iy, &dx, &dz,
		      &dy);
	  else
	       sscanf(line, "%d %d %d %f %f %f", &ix, &iz, &iy, &dx, &dz,
		      &dy);

	  pts[0].x = ix;
	  pts[0].y = iy;
	  pts[0].z = iz;
	  pts[1].x = ix + scale * dx;
	  pts[1].y = iy + scale * dy;
	  pts[1].z = iz + scale * dz;
	  if (ix < xmin)
	       xmin = ix;
	  if (ix > xmax)
	       xmax = ix;
	  if (iy < ymin)
	       ymin = iy;
	  if (iy > ymax)
	       ymax = iy;
	  if (iz < zmin)
	       zmin = iz;
	  if (iz > zmax)
	       zmax = iz;
     }
     
     mod->xmax = xmax + xmin;
     mod->ymax = ymax + ymin;
     mod->zmax = zmax + zmin;
     return(mod);
     
}

static int fgetline(FILE *fp, char s[],int limit)
{
     int c, i, length;

     if (fp == NULL){
	  fprintf(stderr, "fgetline: file pointer not valid\n");
	  return(0);
     }

     if (limit < 3){
	  fprintf(stderr, "fgetline: limit (%d) > 2,\n", limit);
	  return(0);
     }
     
     for (i=0; ( ((c = getc(fp)) != EOF) && (i < (limit-1)) && (c != '\n') ); i++)
	  s[i]=c;
     
     if (i == 1){
	  if (c == EOF){
	       return(0);
	  }
	  if (c == '\n'){
	       s[++i] = '\0';
	       return(1);
	  }
     }
	       

     s[i]='\0';
     length = i;

     if (c == EOF)
	  return (-1 * length);
     else
	  return (length);
}
