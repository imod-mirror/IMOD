/*
 *  clip.h -- Header file for command line image proccessing.
 *
 *  Author: James Kremer email: kremer@colorado.edu
 *
 *  Copyright (C) 1995-2005 by Boulder Laboratory for 3-Dimensional Electron
 *  Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *  Colorado.  See dist/COPYRIGHT for full copyright notice.
 */
/*  $Author$

$Date$

$Revision$

$Log$
*/
#ifndef CLIP_H
#define CLIP_H

/* processes */
enum {IP_NONE = 0, IP_ADD, IP_AVERAGE, IP_BRIGHTNESS, IP_COLOR, 
      IP_CONTRAST, IP_CORRELATE, IP_FFT, IP_FILTER, IP_FLIP, IP_GRADIENT, 
      IP_GRAHAM, IP_INFO, IP_JOINRGB, IP_LAPLACIAN, IP_MEDIAN, IP_PEAK,
      IP_PREWITT,
      IP_PROJECT, IP_RESIZE, IP_ROTATE, IP_SHADOW, IP_SHARPEN, IP_SMOOTH,
      IP_SOBEL, IP_SPLITRGB, IP_STAT, IP_TRANSLATE, IP_ZOOM};


#define IP_DEFAULT -99999

#define IP_APPEND_FALSE     0
#define IP_APPEND_OVERWRITE 1
#define IP_APPEND_ADD       2


#ifdef __vms
#define strcasecmp strcmp
#define strncasecmp strncmp
#endif

struct Grap_options
{     
     char  *pname;   /* Program name */
     char  *command; /* process command */
     struct MRCheader *hin;
     struct MRCheader *hin2;
     struct MRCheader *hout;
     float x,  y,  z;
     float x2, y2, z2;
     int   ix, iy, iz, iz2;
     int   ox, oy, oz;
     float cx, cy, cz;
     float high,  low;
     float red, green, blue;
     float thresh;
     float weight;
     float pad;
     int   mode;
     int   dim;
     int   infiles;
     char  **fnames;
     int   sano;
     int   add2file;
     int   isec;
     float val;
     int   nofsecs;  /* Number of sections in section list. */
     int   *secs;    /* Section list for 2-D */

     int   ocanresize; /* output size can be changed. */
     int   ocanchmode;  /* output mode can be changed. */
     char *ofname;
};


/* clip.c */
void usage(void);
void show_error(char *reason);
void show_status(char *info);
void default_options(struct Grap_options *opt);
int *clipMakeSecList(char *clst, int *nofsecs);

/* clip_proc.c */
int grap_resize(struct MRCheader *hin, struct MRCheader *hout,
		struct Grap_options *opt);
int clip_scaling(struct MRCheader *hin, struct MRCheader *hout,
		    struct Grap_options *opt, int process);
int clipEdge(struct MRCheader *hin, struct MRCheader *hout,
	     struct Grap_options *opt, int process);
int grap_flip(struct MRCheader *hin, struct MRCheader *hout,
	      struct Grap_options *opt);
int grap_color(struct MRCheader *hin, struct MRCheader *hout,
	       struct Grap_options *opt);
int clip2d_color(struct MRCheader *hin, struct MRCheader *hout,
		 struct Grap_options *opt);
int grap_average(struct MRCheader *h1, struct MRCheader *h2,
		 struct MRCheader *hout, struct Grap_options *opt);
int clip_joinrgb(struct MRCheader *h1, struct MRCheader *h2,
		 struct MRCheader *hout, struct Grap_options *opt);
int clip_splitrgb(struct MRCheader *h1, struct Grap_options *opt);
int clip2d_average(struct MRCheader *hin, struct MRCheader *hout,
		   struct Grap_options *opt);
int clip_parxyz(struct MRCvolume *v,
		int xmax, int ymax, int zmax,
		float *rx, float *ry, float *rz);
int clip_stat3d(struct MRCvolume *v);
int clip_get_stat3d(struct MRCvolume *v,
		    float *rmin, float *rmax, float *rmean,
		    int *rx, int *ry, int *rz);
int grap_stat(struct MRCheader *hin, struct Grap_options *opt);
int clip_convolve(struct MRCheader *hin, struct MRCheader *hout,
		 struct Grap_options *opt, int process);
int clipMedian(struct MRCheader *hin, struct MRCheader *hout,
               struct Grap_options *opt);
int write_vol(struct MRCslice **vol, struct MRCheader *hout);
int free_vol(struct MRCslice **vol, int z);

/* clip_transform.c */
int grap_rotate(struct MRCheader *hin, struct MRCheader *hout,
		struct Grap_options *opt);
int grap_2dtrans(struct MRCheader *hin, struct MRCheader *hout,
		 struct Grap_options *opt);
int grap_trans(struct MRCheader *hin, struct MRCheader *hout,
	       struct Grap_options *opt);
int grap_zoom(struct MRCheader *hin, struct MRCheader *hout,
	      struct Grap_options *opt);

/* clip_io.c */
void set_input_options(struct Grap_options *opt, struct MRCheader *hin);
int set_output_options(struct Grap_options *opt, struct MRCheader *hout);
int set_options(struct Grap_options *opt,
		struct MRCheader *hin,
		struct MRCheader *hout);
struct MRCvolume *grap_volume_read(struct MRCheader *hin,
				   struct Grap_options *opt);
int grap_volume_write(struct MRCvolume *v,  struct MRCheader *hout,
		      struct Grap_options *opt);
int grap_volume_free(struct MRCvolume *v);
int mrc_head_print(struct MRCheader *data);
int set_mrc_coords(struct Grap_options *opt);

/* fft.c */
int slice_fft(Islice *slice);
int clip_fftvol3(struct MRCvolume *v, int idir);
int clip_fftvol(struct MRCvolume *v);
int clip_3dfft(struct MRCheader *hin, struct MRCheader *hout,
	       struct Grap_options *opt);
int clip_fft(struct MRCheader *hin, struct MRCheader *hout,
	     struct Grap_options *opt);

void mrcToDFFT(float buf[], int nx, int ny, int idir);

/* filter.c */
int clip_3dfilter(struct MRCheader *hin, struct MRCheader *hout,
		  struct Grap_options *opt);
int clip_bandpass_filter(struct MRCheader *hin, struct MRCheader *hout,
			 struct Grap_options *opt);
     

/* correlation.c */
void corr_getmax(Islice *is, int sa, int xm, int ym, float *x, float *y);
Islice *clip_slice_corr(Islice *s1, Islice *s2);
int clip_corr3d(struct MRCheader *hin1, struct MRCheader *hin2,
		struct MRCheader *hout, struct Grap_options *opt);
int grap_3dcorr(struct MRCheader *hin1, struct MRCheader *hin2,
		struct MRCheader *hout, struct Grap_options *opt);
int grap_corr(struct MRCheader *hin1, struct MRCheader *hin2,
	      struct MRCheader *hout, struct Grap_options *opt);
int padfloat_volume(Istack *v, float pad);
int clip_cor_scalevol(Istack *v);
     
#endif /* clip.h */

