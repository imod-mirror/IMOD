/*  IMOD VERSION 2.67
 *
 *  imodjoin.c  -  Program to extract a list of objects from a model
 *
 *  Authors: James Kremer and David Mastronarde   email: mast@colorado.edu
 */

/*****************************************************************************
 *   Copyright (C) 1995-2003 by Boulder Laboratory for 3-Dimensional Fine    *
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
*/

#include <imodel.h>
int *parselist (char *line, int *nlist);

static void usage()
{
    fprintf(stderr, "Usage:\nimodjoin [-o list | -r list] model_1 [-o list]"
	    " model_2 [more models] out_model\n");
    fprintf(stderr, "       Each list of objects can include ranges, e.g. 1-3,6,9,13-15\n");
    fprintf(stderr, "       -o will select particular objects from either model\n");
    fprintf(stderr, "       -r will REPLACE listed objects from model 1 with objects from model 2\n");
    exit(-1);
}

static void parserr(int mod)
{
     fprintf(stderr, "imodjoin: Error parsing object list before model %d\n", 
	     mod);
     usage();
}
static void optionerr(int mod)
{
  fprintf(stderr, "imodjoin: Invalid option before model %d\n", mod);
  usage();
}
static void doublerr(void)
{
  fprintf(stderr, "imodjoin: You cannot use both -o and -r with model 1\n");
  usage();
}
static void readerr(int mod)
{
  fprintf(stderr, "imodjoin: Error reading file for model %d\n", mod);
  exit(1);
}
static void objerr(int ob, int mod)
{
     fprintf(stderr, "imodjoin: Invalid object number %d for model %d\n", ob, 
	     mod);
     exit (1);
}

int main(int argc, char **argv)
{
     Imod *inModel;
    Imod *joinModel;
    int ob, nob, i, origsize;
    int *list1;
    int *list2;
    int nlist1 = 0;
    int nlist2;
    int njoin = 1;
    int replace = 0;
    int iarg = 1;
    char option;
    char backname[257];
    
    if (argc < 4) usage();

    if (*argv[iarg] == '-') {
      
	 if ( (option = *(argv[iarg++] + 1)) == 'o' || option == 'r' ) {
	      list1 = parselist(argv[iarg++], &nlist1);
	      if (!list1)
		   parserr(1);
	      if (option == 'r')
		   replace = 1;
	 } else
	      optionerr(1);
    }

    if (*argv[iarg] == '-') {
	 if ((option = *(argv[iarg] + 1)) == 'o' || option == 'r' )
	      doublerr();
	 else
	      optionerr(1);
    }

    inModel = imodRead(argv[iarg]); iarg++;
    if (!inModel) readerr(1);

    origsize = inModel->objsize;
    /* If there is a -o list on the first file, reorganize the retained 
       objects */
    if (nlist1 && !replace) {
      /* Make nlist new objects, then shift all existing objects to top */
      for (i = 0; i < nlist1; i++)
	imodNewObject(inModel);
      for (ob = origsize - 1; ob >= 0; ob--)
	imodObjectCopy(&inModel->obj[ob], &inModel->obj[ob+nlist1]);

      /* Now copy all of the selected ones into place */
      for (i = 0; i < nlist1; i++) {
	ob = list1[i] - 1;
	if (ob < 0 || ob >= origsize) {
	  fprintf(stderr, "imodjoin: Invalid object number %d\n", ob);
	  exit (1);
	}
	imodObjectCopy(&inModel->obj[ob + nlist1], &inModel->obj[i]);
      }

      /* Delete extra objects by just setting objsize */
      inModel->objsize = nlist1;
    }
    
    /* process arguments, read model, add objects to first model for one
       or more models */
    do {
	 njoin++;

	 if (iarg + 1 >= argc) usage();

	 if (njoin > 2 && replace) {
	      fprintf(stderr, "imodjoin: You cannot use -r with more than 2 "
		     "input models\n");
	      exit(1);
	 }

	 nlist2 = 0;
	 if (*argv[iarg] == '-') {
	      if (iarg + 3 >= argc) usage();
	      if ( (option = *(argv[iarg++] + 1)) == 'o' ) {
		   list2 = parselist(argv[iarg++], &nlist2);
		   if (!list2)
			parserr(njoin);
	      } else
		   optionerr(njoin);
	 }

	 joinModel = imodRead(argv[iarg]); iarg++;
	 if (!joinModel) readerr(njoin);

	 /* If no list for second model, make simple list of all objects */
	 if (!nlist2) {
	      nlist2 = joinModel->objsize;
	      list2 = (int *)malloc(nlist2 * sizeof(int));
	      for (i = 0; i < nlist2; i++)
		   list2[i] = i + 1;
	 }

	 /* Now go through objects in second file, copying selected ones with 
	    or without replacement */
	 for (i = 0; i < nlist2; i++) {
	      ob = list2[i] - 1;
	      if (ob < 0 || ob >= joinModel->objsize) objerr(ob+1, njoin);
	      if (replace && (i < nlist1)) {
		   nob = list1[i] - 1;
		   if (nob < 0 || nob >= origsize) objerr(nob+1, 1);
	      } 
	      else {
		   nob = inModel->objsize;
		   imodNewObject(inModel);
	      }
	      imodObjectCopy(&joinModel->obj[ob], &inModel->obj[nob]);
	 }
	 free(list2);
    } while (iarg + 1 < argc);

    /* set current indexes to -1 to avoid problems */
    inModel->cindex.point  = -1;
    inModel->cindex.contour = -1;
    inModel->cindex.object = 0;

    sprintf(backname, "%s~", argv[argc - 1]);
    rename (argv[argc - 1], backname);
    if (imodOpenFile(argv[argc - 1], "wb", inModel)) {
      fprintf(stderr, "imodjoin: Fatal error opening new model\n");
      exit (1);
    }
    imodWriteFile(inModel);
    exit(0);
}






