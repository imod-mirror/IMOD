c	  SHUFFLER manages the input sections in the big array ARRAY.
c	  IZWANT specifies the piece # that is wanted (numbered from 1, not 0).
c	  The routine looks to see if it is already in memory; if not, it loads
c	  the section, replacing the frame that has not been used in the
c	  longest time, and returns the index of the frame's start in INDEX
c
	subroutine shuffler(array,izwant,index)
c
	parameter (memlim=128)
	integer*4 izmemlist(memlim),lastused(memlim)
	common /shuffl/ npixin,limsec,jusecount,izmemlist,lastused
c
	real*4 array(*)
	logical alreadyin
c	  
	minused=jusecount+1
	do i=1,limsec
	  if(izmemlist(i).eq.izwant)then
	    index=(i-1)*npixin+1
	    jusecount=jusecount+1
	    lastused(i)=jusecount
	    return
	  endif
	  if(minused.gt.lastused(i))then
	    minused=lastused(i)
	    ioldest=i
	  endif
	enddo
	index=(ioldest-1)*npixin+1
	jusecount=jusecount+1
	lastused(ioldest)=jusecount
	izmemlist(ioldest)=izwant
c	print *,'reading section',izwant-1,'  index',index
	call imposn(1,izwant-1,0)		!yes izwant starts at 1
	call irdsec(1,array(index),*99)
	return
99	stop 'error reading file'
	end



