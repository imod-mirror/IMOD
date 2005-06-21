/* 
 *  istore.c -- General storage library routines.
 *
 *  Author: David Mastronarde  email: mast@colorado.edu
 *
 *  Copyright (C) 1995-2005 by Boulder Laboratory for 3-Dimensional Electron
 *  Microscopy of Cells ("BL3DEMC") and the Regents of the University of 
 *  Colorado.  See dist/COPYRIGHT for full copyright notice.
 */
/*  $Author$

$Date$

$Revision$

$Log$
Revision 3.1  2005/06/20 22:25:50  mast
Preliminary checkin

*/

#include "imodel.h"
#include "istore.h"

static int storeCompare(const void *s1, const void *s2);

/*!
 * Writes general storage list to the model file with pointer [fout], including
 * the value in [id], which can indicate a model, objext, contour, or mesh
 * list.  Returns non-zero for error.
 */
int imodWriteStore(Ilist *list, int id, FILE *fout)
{
  int i, j, dtype, error;
  Istore *store;
  StoreUnion *item;
  if (!ilistSize(list))
    return 0;
  imodPutInt(fout, &id);
  i = ilistSize(list) * SIZE_STOR;
  imodPutInt(fout, &i);

  for (i = 0; i < ilistSize(list); i++) {
    store = istoreItem(list, i);
    imodPutShort(fout, &store->type);
    imodPutShort(fout, &store->flags);

    /* Set up to write index */
    dtype = store->flags & 3;
    item = &store->index;

    for (j = 0; j < 2; j++) {
      switch (dtype ) {
      case GEN_STORE_INT:
        imodPutInt(fout, &item->i);
        break;
      case GEN_STORE_FLOAT:
        imodPutFloat(fout, &item->f);
        break;
      case GEN_STORE_SHORT:
        imodPutShort(fout, &item->s[0]);
        imodPutShort(fout, &item->s[1]);
        break;
      case GEN_STORE_BYTE:
        if ((error = imodPutBytes(fout, item->b, 4)))
          return(error);
        break;
      }

      /* For second time through, set up to read value */
      dtype = (store->flags >> 2) & 3;
      item = &store->value;
    }
  }
  return 0;
}

/*!
 * Reads a general storage chunk from the model file with pointer [fin].  
 * Allocates a new {Ilist} and returns its address, or NULL if an error
 * occurs.  Set [error] non-zero for error also.
 */
Ilist *imodReadStore(FILE *fin, int *error)
{
  int i, j, dtype, lastIndex, needSort, ind;
  Ilist *list;
  Istore store;
  StoreUnion *item;
  int nread = imodGetInt(fin) / SIZE_STOR;
  needSort = 0;
  *error = 0;
  if (nread <= 0) {
    *error = IMOD_ERROR_READ;
    return(NULL);
  }
  list = ilistNew(sizeof(Istore), nread);
  if (!list) {
    *error = IMOD_ERROR_MEMORY;
    return(NULL);
  }

  for (i = 0; i < nread; i++) {
    store.type = imodGetShort(fin);
    store.flags = imodGetShort(fin);

    /* Set up to read index */
    dtype = store.flags & 3;
    item = &store.index;

    for (j = 0; j < 2; j++) {
      switch (dtype ) {
      case GEN_STORE_INT:
        item->i = imodGetInt(fin);
        break;
      case GEN_STORE_FLOAT:
        item->f = imodGetFloat(fin);
        break;
      case GEN_STORE_SHORT:
        item->s[0] = imodGetShort(fin);
        item->s[1] = imodGetShort(fin);
        break;
      case GEN_STORE_BYTE:
        if (imodGetBytes(fin, item->b, 4)) {
          *error = IMOD_ERROR_READ;
          return(NULL);
        }
        break;
      }

      /* For second time through, set up to read value */
      dtype = (store.flags >> 2) & 3;
      item = &store.value;
    }

    /* Add current store item to list */
    if (ilistAppend(list, &store)) {
      *error = IMOD_ERROR_MEMORY;
      return(NULL);
    }

    /* Keep track of whether it is out of order */
    ind = INT_MAX;
    if (!(store.flags & (GEN_STORE_NOINDEX | 3)))
      ind = store.index.i;
    if (i && ind < lastIndex)
      needSort = 1;
    lastIndex = ind;
  }

  /* Sort if necessary */
  if (needSort)
    istoreSort(list);

  return(list);
}

/*!
 * Sorts the given [list] of {Istore} elements based on their {index} values
 * placing any elements with no index value at the end.
 */
void istoreSort(Ilist *list)
{
  if (ilistSize(list) >= 2)
    qsort(list->data, ilistSize(list), list->dsize, storeCompare);
}

/* Comparison function for sorting the list */
static int storeCompare(const void *s1, const void *s2)
{
  Istore *st1 = (Istore *)s1;
  Istore *st2 = (Istore *)s2;
  int ind1 = INT_MAX;
  int ind2 = INT_MAX;
  if (!(st1->flags & (GEN_STORE_NOINDEX | 3)))
    ind1 = st1->index.i;
  if (!(st2->flags & (GEN_STORE_NOINDEX | 3)))
    ind2 = st2->index.i;
  if (ind1 < ind2)
    return -1;
  if (ind1 > ind2)
    return 1;
  return 0;
}

/*!
 * Inserts the element [store] into the list of sorted {Istore} elements 
 * pointed to by [list], after any existing elements with the same {index}.
 * Works if [list] points to a NULL.  Returns 1 for error.
 */
int istoreInsert(Ilist **list, Istore *store)
{
  int after;
  int lookup = istoreLookup(*list, store->index.i, &after);
  if (!*list)
    *list = ilistNew(sizeof(Istore), 4);
  if (!*list)
    return 1;
  return (ilistInsert(*list, store, after));
}

/*!
 * Looks for elements with [index] in the given [list] of sorted {Istore} 
 * elements, returns [after] with the list index of the first element with an
 * index after the given value, and returns the list index of the first 
 * matching element or -1 if there is no match.
 */
int istoreLookup(Ilist *list, int index, int *after)
{
  int below, above, mid, match = -1;
  int noindex = GEN_STORE_NOINDEX | 3;
  Istore *store;
  *after = 0;
  if (!ilistSize(list))
    return -1;
  below = 0;
  above = ilistSize(list) - 1;

  /* test that first element is below item  - if above, done*/
  store = istoreItem(list, 0);
  if ((store->flags & noindex) || store->index.i > index)
    return -1;
  else if (store->index.i == index)
    match = 0;

  /* test that last element is above item - if below, set after to list
     end and return */
  store = istoreItem(list, above);
  if (match < 0 && !(store->flags & noindex)) {
    if (store->index.i < index) {
      *after = ilistSize(list);
      return -1;
    } else if (store->index.i == index)
      match = 0;
  }

  /* Look at element midway between below and above and replace either the
     below or the above element.  Loop until a match is found or there is no 
     middle left */
  while (match < 0 && above - below > 1) {
    mid = (above + below) / 2;
    store = istoreItem(list, mid);
    if ((store->flags & noindex) || store->index.i > index)
      above = mid;
    else if (store->index.i == index)
      match = mid;
    else
      below = mid;
  }

  /* If there is still no match, then set after to the one above */
  if (match < 0) {
    *after = above;
    return -1;
  }

  /* If there is a match, find first one after the matching index */
  for (mid = match + 1; mid < ilistSize(list); mid++) {
    store = istoreItem(list, mid);
    if ((store->flags & noindex) || store->index.i > index)
      break;
  }
  *after = mid;

  /* Then find first one before the match, and return first match */
  for (mid = match - 1; mid >= 0; mid--) {
    store = istoreItem(list, mid);
    if (store->index.i < index)
      break;
  }
  return (mid + 1);
}

void istoreDump(Ilist *list)
{
  int i, j, dtype;
  Istore *store;
  StoreUnion *item;
  for (i = 0; i < ilistSize(list); i++) {
    store = istoreItem(list, i);
    printf("%6d %6o", store->type, store->flags);

    dtype = store->flags & 3;
    item = &store->index;

    for (j = 0; j < 2; j++) {
      switch (dtype ) {
      case GEN_STORE_INT:
        printf(" %11d", item->i);
        break;
      case GEN_STORE_FLOAT:
        printf(" %12.6g", item->f);
        break;
      case GEN_STORE_SHORT:
        printf(" %6d %6d", item->s[0], item->s[1]);
        break;
      case GEN_STORE_BYTE:
        printf(" %3d %3d %3d %3d", item->b[0], item->b[1], item->b[2],
               item->b[3]);
        break;
      }

      dtype = (store->flags >> 2) & 3;
      item = &store->value;
    }
    printf("\n");
  }    
}



/*!
 * Breaks all changes in [list] at the point index given by [index]; namely 
 * terminates all changes at index - 1 and restarts them at index.  [psize]
 * specifies the number of points in the contour.  If [index] equals [psize]
 * then terminations will be inserted without any restarts.  Returns 1 for 
 * error.
 */
int istoreBreakChanges(Ilist *list, int index, int psize)
{
  Istore store, storeEnd;
  Istore *stp;
  int curStart, curRevert, ptLastSet, ptFirstSet, ptRevert, curSet, i;
  int needRestart;
  if (!ilistSize(list))
    return 0;
  
  for (curStart = 0; curStart < list->size; curStart++) {
    stp = istoreItem(list, curStart);

    /* Done when reach the given index, or a non-index item */
    if ((stp->flags & (GEN_STORE_NOINDEX | 3)) || stp->index.i >= index)
      break;

    /* Skip if it is a one-point type or an end */
    if (stp->flags & (GEN_STORE_ONEPOINT | GEN_STORE_REVERT))
      continue;

    /* Save the item, set up index for last time it was set, and set the
       endpoint past the break index.  Keep track of point and list indexes */
    store = *stp;
    ptFirstSet = stp->index.i;
    ptLastSet = ptFirstSet;
    ptRevert = index + 1;
    curSet = curStart;
    needRestart = index < psize ? 1 : 0;

    /* Loop forward from this point looking for an end */
    for (i = curStart + 1; i < list->size; i++) {
      stp = istoreItem(list, curStart);

      /* This search is done when get past the given index */
      if ((stp->flags & (GEN_STORE_NOINDEX | 3)) || stp->index.i > index)
        break;

      /* If the type matches, stop on an end */
      if (store.type == stp->type) {
        if (stp->flags & GEN_STORE_REVERT) {
          ptRevert = stp->index.i;
          curRevert = i;
          break;
        }

        /* or replace the saved item, unless it is restart after break */
        if (stp->index.i == index) {
          needRestart = 0;
        } else {
          store = *stp;
          ptLastSet = stp->index.i;
          curSet = i;
        }
      }
    }

    /* Easy case: it ends before the break */
    if (ptRevert < index)
      continue;

    /* End on the break gets deleted and will not need a restart */
    if (ptRevert == index) {
      ilistRemove(list, curRevert);
      needRestart = 0;
    }

    if (ptLastSet < index - 1) {

      /* If latest change is before break, insert an end */
      storeEnd = store;
      storeEnd.flags |= GEN_STORE_REVERT;
      storeEnd.index.i = index - 1;
      if (istoreInsert(&list, &storeEnd))
        return 1;
      
    } else if (ptFirstSet < index - 1) {
      
      /* If a successor change starts at break, convert it to an end */
      stp = istoreItem(list, curSet);
      stp->flags |= GEN_STORE_REVERT;
    } else {
      
      /* If original change starts at break, delete it */
      ilistRemove(list, curStart);
      curStart--;
    }
    
    /* Now restart if needed */
    if (needRestart) {
      store.index.i = index;
      if (istoreInsert(&list, &store))
        return 1;
    }
  }
  return 0;
}

/*!
 * Shifts indexes in [list] by [amount], for all storage items with indexes
 * >= [ptIndex].  Set [startScan] to a list index at which to start scanning
 * the list, or to -1 to have the routine search for the starting index.
 */
void istoreShiftIndex(Ilist *list, int ptIndex, int startScan, int amount)
{
  int after, i;
  Istore* stp;
  if (!ilistSize(list))
    return;

  /* Getting starting position if it is not provided */
  if (startScan < 0) {
    startScan = istoreLookup(list, ptIndex, &after);
    if (startScan < 0)
      startScan = after;
  }

  /* From start to end or non-index item, shift all indexes */
  for (i = startScan; i < list->size; i++) {
    stp = istoreItem(list, i);
    if (stp->flags & (GEN_STORE_NOINDEX | 3))
      break;
    if (stp->index.i >= ptIndex)
      stp->index.i += amount;
  }
}

/*!
 * Manages any elements in [list] associated with the point given by [index]
 * when that point is deleted.  They will be moved to the next point or
 * removed, and all following indexes will be reduced by 1.  [psize] must 
 * indicate the size of the contour * before the point is deleted.  Returns 1
 * for error.
 */
int istoreDeletePoint(Ilist *list, int index, int psize)
{
  int after, lind, i, j, needMove, delEnd;
  Istore store;
  Istore *stp;
  int lookup = istoreLookup(list, index, &after);
  if (lookup < 0)
    return 0;

  /* If there are following points, loop on the items, see if each
     item needs to be moved to a following point */
  if (index < psize - 1) {
    for (lind = lookup; lind < after; lind++) {
      stp = istoreItem(list, lind);

      /* Skip single-point items, they just delete */
      if (stp->flags & GEN_STORE_ONEPOINT)
        continue;
      store = *stp;
      needMove = 1;

      /* Look forward for items of same type */
      for (i = after; i < list->size; i++) {
        stp = istoreItem(list, i);

        /* If no items found, done, and still need to move */
        if ((stp->flags & (GEN_STORE_NOINDEX | 3)) || 
            stp->index.i > index + 1)
          break;

        /* If matching item is found, no move needed */
        if (stp->type == store.type) {
          needMove = 0;
          
          /* If it is another start, that is fine */
          if (!(stp->flags & GEN_STORE_REVERT))
            break;

          /* If it is an end, need to look back and see if the point in
          question is a successor; if not this end needs to be removed */
          delEnd = 1;
          for (j = lookup - 1; j >= 0; j--) {
            stp = istoreItem(list, j);
            if (stp->type == store.type) {
              if (!(stp->flags & GEN_STORE_REVERT))
                delEnd = 0;
              break;
            }
          }

          if (delEnd)
            ilistRemove(list, i);
          break;
        }
      }

      /* Insert item at next index if still appropriate */
      if (needMove) {
        store.index.i++;
        if (istoreInsert(&list, &store))
          return 1;
      }
    }
  }

  /* Delete the current point items and then shift indexes */
  ilistShift(list, after, lookup - after);
  list->size -= after - lookup;
  istoreShiftIndex(list, index + 1, -1, lookup);
  return 0;
}

/*!
 * Breaks general storage list in contour [cont] into two pieces and assigns
 * changes starting at [index] or after to the new contour [ncont].  Returns
 * 1 for error.
 */
int istoreBreakContour(Icont *cont, Icont *ncont, int index) 
{
  Ilist *ostore = cont->store;
  Ilist *nstore = NULL;
  int after, i, lookup, size;
  Istore *stp;

  if (!ilistSize(ostore))
    return 0;

  /* If old contour is reduced to zero length, transfer the list to the new */
  if (!index) {
    cont->store = NULL;
    ncont->store = ostore;
    return 0;
  }

  /* Otherwise, break the changes, then get the first index of the break */
  if (istoreBreakChanges(ostore, index, cont->psize))
    return 1;
  lookup = istoreLookup(ostore, index - 1, &after);
  size = ostore->size + 1 - after;
  if (size) {
    nstore = ilistNew(sizeof(Istore), size);
    if (!nstore)
      return 1;
    for (i = after; i < ostore->size; i++) {
      stp = istoreItem(ostore, i);
      ilistAppend(nstore, stp);
    }
    istoreShiftIndex(nstore, index, 0, -index);
  }
  ostore->size -= size;
  ncont->store = nstore;
  return 0;
}

/*!
 * Inverts the changes in the list of {Istore} elements pointed to by [listp]
 * so that the same changes will occur with an inverted contour.  [psize]
 * specifies the size of the corresponding contour.  Returns 1 for error.
 */
int istoreInvert(Ilist **listp, int psize)
{
  Ilist *nlist = NULL;
  Ilist *list = *listp;
  int pmo, curStart, i;
  Istore *stp;
  Istore store;

  if (psize < 2 || !ilistSize(list))
    return 0;
  if (istoreBreakChanges(list, psize, psize))
    return 1;

  pmo = psize - 1;
  for (curStart = 0; curStart < list->size; curStart++) {
    stp = istoreItem(list, curStart);

    /* Copy non-index item */
    if ((stp->flags & (GEN_STORE_NOINDEX | 3))) {
      if (istoreInsert(&nlist, stp)) {
        ilistDelete(nlist);
        return 1;
      }
      continue;
    }

    /* Copy one-point type */
    if (stp->flags & GEN_STORE_ONEPOINT) {
      stp->index.i = pmo - stp->index.i;
      if (istoreInsert(&nlist, stp)) {
        ilistDelete(nlist);
        return 1;
      }
      continue;
    }

    /* Convert the start to an end and add it with inverted index, then
       save the starting change */
    store = *stp;
    store.index.i = pmo - store.index.i;
    store.flags |= GEN_STORE_REVERT;
    if (istoreInsert(&nlist, &store)) {
      ilistDelete(nlist);
      return 1;
    }
    store = *stp;

    /* Loop forward from this point looking for the end */
    for (i = curStart + 1; i < list->size; i++) {
      stp = istoreItem(list, curStart);

      /* If the type matches, output saved item with current index inverted */
      if (store.type == stp->type) {
        store.index.i = pmo - stp->index.i;
        if (istoreInsert(&nlist, &store)) {
          ilistDelete(nlist);
          return 1;
        }

        /* Remove the successor or end, break on an end */
        store = *stp;
        ilistRemove(list, i);
        if (store.flags & GEN_STORE_REVERT)
          break;
      }
    }
  }

  /* Free the remnants of the list and assign new list */
  ilistDelete(list);
  *listp = nlist;
  return 0;
}

/*!
 * Extracts all of the changes in [olist] between indexes [indStart] and
 * [indEnd], inclusive, into the list pointed to by [nlistp].  The indexes
 * of the changes are shifted so that [indStart] is shifted to [newStart].
 * [psize] specifies the size of the corresponding contour.  [nlistp] may point
 * to a NULL.  Returns 1 for error.
 */
int istoreExtractChanges(Ilist *olist, Ilist **nlistp, int indStart, 
                         int indEnd, int newStart, int psize)
{
  Ilist *tmpList;
  int i, lookup, after1, after2;
  Istore *stp;

  if (!ilistSize(olist) || indStart == indEnd)
    return 0;

  /* Copy the list */
  tmpList = ilistDup(olist);
  if (!tmpList)
    return 1;
 
  /* If indices are inverted, then invert the list and invert indices */
  if (indStart > indEnd) {
    if (istoreInvert(&tmpList, psize)) {
      ilistDelete(tmpList);
      return 1;
    }
    indStart = psize - 1 - indStart;
    indEnd = psize - 1 - indEnd;
  }

  /* Break the list before and after the indexes */
  if (indStart && istoreBreakChanges(tmpList, indStart, psize)) {
    ilistDelete(tmpList);
    return 1;
  }
  if (istoreBreakChanges(tmpList, indEnd + 1, psize)) {
    ilistDelete(tmpList);
    return 1;
  }

/* Copy data to new list, shifting indexes */
  lookup = istoreLookup(tmpList, indStart - 1, &after1);
  lookup = istoreLookup(tmpList, indEnd, &after2);
  for (i = after1; i < after2; i++) {
    stp = istoreItem(tmpList, i);
    stp->index.i += newStart - indStart;
    if (istoreInsert(nlistp, stp)) {
      ilistDelete(tmpList);
      return 1;
    }
  }
  ilistDelete(tmpList);
  return 0;
}

/*!
 * Copies all non-index items in [olist] to the list pointed to by [nlistp].
 * Returns 1 for error.
 */
int istoreCopyNonIndex(Ilist *olist, Ilist **nlistp)
{
  int i, lookup, after;
  if (!ilistSize(olist))
    return 0;
  lookup = istoreLookup(olist, INT_MAX, &after);
  for (i = after; i < olist->size; i++)
    if (istoreInsert(nlistp, istoreItem(olist, i)))
      return 1;
  return 0;
}

/*!
 *  Let's see if this gets used!
 */    
int istoreCountObjItems(Ilist *list, int co, int surf)
{
  Istore *stp;
  int i, index, count = 0;
  if (!ilistSize(list))
    return 0;
  for (i = 0; i < list->size; i++) {
    stp = istoreItem(list, i);
    if (stp->flags & (GEN_STORE_NOINDEX | 3))
      break;
    index = stp->index.i;
    if ((!(stp->flags & GEN_STORE_SURFACE) && index == co) ||
        ((stp->flags & GEN_STORE_SURFACE) && index == surf))
      count++;
  }
  return count;
}

/*!
 * 
 */    
Istore *istoreNextObjItem(Ilist *list, int co, int surf, int first)
{
  Istore *stp;
  int i, index;
  if (!ilistSize(list))
    return NULL;
  if (first)
    stp = (Istore *)ilistFirst(list);
  else
    stp = (Istore *)ilistNext(list);
  while (stp) {
    if (!stp || (stp->flags & (GEN_STORE_NOINDEX | 3)))
      return NULL;
    index = stp->index.i;
    if ((!(stp->flags & GEN_STORE_SURFACE) && index == co) ||
        ((stp->flags & GEN_STORE_SURFACE) && index == surf))
      return stp;
    stp = (Istore *)ilistNext(list);
  }
  return NULL;
}

/*!
 * Inserts a change described in [store] into the list pointed to by [listp].
 * An matching change or end at the same index is removed.  Redundant entries
 * of the change are avoided or eliminated.  Returns 1 for error.
 */
int istoreInsertChange(Ilist **listp, Istore *store)
{
  Istore *stp;
  Ilist *list = *listp;
  int i, lookup, after, j, needItem;
  lookup = istoreLookup(list, store->index.i, &after);
  
  /* If there is a match at the current index, eliminate it */
  if (lookup >= 0) {
    for (i = lookup; i < after; i++) {
      stp = istoreItem(list, i);
      if (stp->type == store->type) {
        ilistRemove(list, i);
        i--;
        after--;
      }
    }
  }

  /* Look backwards and see if there is fully matching start; if not insert
     the new item */
  if (lookup < 0)
    lookup = after;
  needItem = 1;
  for (i = lookup - 1; i >= 0; i--) {
    stp = istoreItem(list, i);
    if (stp->type == store->type) {
       if (!(stp->flags & GEN_STORE_REVERT) && 
           stp->value.i == store->value.i)
         needItem = 0;
       break;
    }
  }

  /* Insert if still needed, adjust after index */
  if (needItem) {
    if (istoreInsert(listp, store))
      return 1;
    after++;
    list = *listp;
  }

  /* Look forward and eliminate any fully matching starts */
  for (i = after; i < list->size; i++) {
    stp = istoreItem(list, i);
    if (stp->flags & (GEN_STORE_NOINDEX | 3))
      break;
    if (stp->type == store->type) {
      if (!(stp->flags & GEN_STORE_REVERT))
        break;
      if (stp->value.i == store->value.i) {
        ilistRemove(list, i);
        i--;
      }
    }
  }
  return 0;
}

/*!
 * Inserts an end for a change into [list], where [type] specifies the type and
 * [index] indicates the point index.  A later end will be deleted if there
 * is not an intervening start of the same type, and a start at the given index
 * will also be deleted.
 */
int istoreEndChange(Ilist *list, int type, int index)
{
  Istore *stp;
  Istore store;
  int i, lookup, after, j, needEnd;
  if (!ilistSize(list))
    return 1;
  lookup = istoreLookup(list, index, &after);

  /* Search forward and eliminate matching end, unless there is another set */
  for (i = after; i < list->size; i++) {
    stp = istoreItem(list, i);
    if (stp->flags & (GEN_STORE_NOINDEX | 3))
      break;
    if (stp->type == type) {
      if (!(stp->flags & GEN_STORE_REVERT))
        break;
      ilistRemove(list, i);
      i--;
    }
  }
  
  /* If there is a change at this index it needs to be removed */
  needEnd = 1;
  if (lookup >= 0) {
    for (i = lookup; i < after; i++) {
      stp = istoreItem(list, i);
      if (stp->type == type) {

        /* If end already exists, don't need end */
        needEnd = 0;
        if (!(stp->flags & GEN_STORE_REVERT)) {

          /* Search back for a previous start; if find one, still need end */
          needEnd = 0;
          for (j = lookup - 1; j >= 0; j--) {
            stp = istoreItem(list, j);
            if (stp->type == type) {
              if (!(stp->flags & GEN_STORE_REVERT))
                needEnd = 1;
              break;
            }
          }

          /* Remove start at this index */
          ilistRemove(list, i);
          i--;
          after--;
        }
      }      
    }
  }

  /* Insert the end if still needed */
  if (needEnd) {
    store.type = type;
    store.index.i = index;
    store.flags = GEN_STORE_REVERT;
    store.value.i = 0;
    if (istoreInsert(&list, &store))
      return 1;
  }
  return 0;
}

/*!
 * Clears a whole change sequence in [list] with the type given by [type] and 
 * containing the point at [index].  All changes of the given type are removed
 * from the starting change to an end, if any.  Returns 1 for an empty list.
 */
int istoreClearChange(Ilist *list, int type, int index)
{
  Istore *stp;
  int i, lookup, after, flags;
  if (!ilistSize(list))
    return 1;
  lookup = istoreLookup(list, index - 1, &after);
  
  /* Search forward to end, removing all matching changes and end */
  for (i = after; i < list->size; i++) {
    stp = istoreItem(list, i);
    if (stp->flags & (GEN_STORE_NOINDEX | 3))
      break;
    if (stp->type == type) {
      flags = stp->flags;
      ilistRemove(list, i);
      i--;
      if (flags & GEN_STORE_REVERT)
        break;
    }
  }

  /* Search backward, deleting all matching changes unless an end is found */
  for (i = after - 1; i >= 0; i--) {
    stp = istoreItem(list, i);
    if (stp->type == type) {
      if (stp->flags & GEN_STORE_REVERT)
        break;
      ilistRemove(list, i);
    }
  }
  return 0;
}

/*!
* Fills a draw property structure [props] with default values for the object
* [obj].
*/
void istoreDefaultDrawProps(Iobj *obj, DrawProps *props)
{
  props->red = obj->red;
  props->green = obj->green;
  props->blue = obj->blue;
  props->fillRed = obj->mat1;
  props->fillGreen = obj->mat1b1;
  props->fillBlue = obj->mat1b2;
  props->trans = obj->trans;
  props->connect = 0;
  props->gap = 0;
  props->linewidth = obj->linewidth;
  props->linewidth2 = obj->linewidth2;
  props->symtype = obj->symbol;
  props->symflags = obj->symflags;
  props->symsize = obj->symsize;
}

/*!
* Gets a draw property structure [contProps] for a contour or surface based on
* the default object properties in [defProps] and entries in [list].  For a
* contour, [co] specifies the contour number and [surf] specifies its surface
* number; for a surface, [co] should be negative.  Returns a set of flags for 
* which items are changed from the default.
*/
int istoreContSurfDrawProps(Ilist *list, DrawProps *defProps, 
                            DrawProps *contProps, int co, int surf)
{
  int i, j, lookup, after, retval, which, surfFlag;
  Istore *stp;
  *contProps = *defProps;
  retval = 0;
  if (ilistSize(list))
    return 0;

  /* Set up to loop on surface entries first */
  which = surf;
  surfFlag = GEN_STORE_SURFACE;

  for (j = 0; j < 2; j++) {
    if (which < 0)
      continue;
    lookup = istoreLookup(list, which, &after);
    if (lookup >= 0) {
      for (i = lookup; i < after; i++) {
        stp = istoreItem(list, i);
        if ((stp->flags | GEN_STORE_SURFACE) != surfFlag)
          continue;
        switch (stp->type) {
        case GEN_STORE_COLOR:
          retval |= CHANGED_COLOR;
          contProps->red = stp->value.b[0];
          contProps->green = stp->value.b[1];
          contProps->blue = stp->value.b[2];
          break;

        case GEN_STORE_FCOLOR:
          retval |= CHANGED_FCOLOR;
          contProps->fillRed = stp->value.b[0];
          contProps->fillGreen = stp->value.b[1];
          contProps->fillBlue = stp->value.b[2];
          break;

        case GEN_STORE_TRANS:
          retval |= CHANGED_TRANS;
          contProps->trans = stp->value.i;
          break;

        case GEN_STORE_3DWIDTH:
          retval |= CHANGED_3DWIDTH;
          contProps->linewidth = stp->value.i;
          break;

        case GEN_STORE_2DWIDTH:
          retval |= CHANGED_2DWIDTH;
          contProps->linewidth2 = stp->value.i;
          break;

        case GEN_STORE_SYMSIZE:
          retval |= CHANGED_SYMSIZE;
          contProps->symsize = stp->value.i;
          break;

        case GEN_STORE_SYMTYPE:
          retval |= CHANGED_SYMTYPE;
          contProps->symflags &= ~IOBJ_SYMF_FILL;
          contProps->symtype = stp->value.i;
          if (contProps->symtype < 0) {
            contProps->symtype = -1 - contProps->symtype;
            contProps->symflags |= IOBJ_SYMF_FILL;
          }
          break;
        }
      }
    }

    /* Next pass through, loop on contour entries */
    which = co;
    surfFlag = 0;
  }
  return retval;
}

/*!
* Returns the point index of the first item with a change in the [list], or
* -1 if there are no changes.  Leaves the current index of [list] at this
* item.
*/
int istoreFirstChangeIndex(Ilist *list)
{
  Istore *stp;
  if (!ilistSize(list))
    return -1;
  stp = istoreItem(list, 0);
  if (stp->flags & (GEN_STORE_NOINDEX | 3))
    return -1;
  return stp->index.i;
}

/*!
* Gets the next change in point drawing properties described by [list].   The
* default drawing properties for the contour are supplied in [defProps], and
* the point's drawing properties are returned in [ptProps].  The current
* state of each type of change is returned in [stateFlags], which should be
* zeroed by the caller at the start of a contour and maintained between calls.
* Flags for which properties at this point are returned in [changeFlags], while
* the return value is the point index of the next change in the list, or -1 if
* there are no more changes.
*/
int istoreNextChange(Ilist *list, DrawProps *defProps,
                     DrawProps *ptProps, int *stateFlags, int *changeFlags)
{
  Istore *stp;
  int ending;
  int index = -1;
  *changeFlags = 0;
  
  /* Overkill? These are being marked 3 ways when they change */
  *stateFlags &= ~(CHANGED_GAP | CHANGED_CONNECT);
  ptProps->gap = 0;
  ptProps->connect = 0;
  if (!ilistSize(list))
    return -1;

  while (1) {

    /* If at end of list or item is past index items, return -1 */
    if (list->current >= list->size)
      return -1;
    stp = istoreItem(list, list->current);
    if (stp->flags & (GEN_STORE_NOINDEX | 3))
      return -1;

    /* Record index if not set yet, return index if it has changed */
    if (index < 0)
      index = stp->index.i;
    else if (stp->index.i != index)
      return stp->index.i;

    /* Increment list index for next round */
    list->current++;

    switch (stp->type) {
    case GEN_STORE_COLOR:
      *changeFlags |= CHANGED_COLOR;
      if (ending) {
        ptProps->red = defProps->red;
        ptProps->green = defProps->green;
        ptProps->blue = defProps->blue;
        *stateFlags &= ~CHANGED_COLOR;
      } else {
        ptProps->red = stp->value.b[0];
        ptProps->green = stp->value.b[1];
        ptProps->blue = stp->value.b[2];
        *stateFlags |= CHANGED_COLOR;
      }
      break;

    case GEN_STORE_FCOLOR:
      *changeFlags |= CHANGED_FCOLOR;
      if (ending) {
        ptProps->fillRed = defProps->fillRed;
        ptProps->fillGreen = defProps->fillGreen;
        ptProps->fillBlue = defProps->fillBlue;
        *stateFlags &= ~CHANGED_FCOLOR;
      } else {
        ptProps->fillRed = stp->value.b[0];
        ptProps->fillGreen = stp->value.b[1];
        ptProps->fillBlue = stp->value.b[2];
        *stateFlags |= CHANGED_FCOLOR;
      }
      break;

    case GEN_STORE_TRANS:
      *changeFlags |= CHANGED_TRANS;
      if (ending) {
        ptProps->trans = defProps->trans;
        *stateFlags &= ~CHANGED_TRANS;
      } else {
        ptProps->trans = stp->value.i;
        *stateFlags |= CHANGED_TRANS;
      }
      break;

    case GEN_STORE_GAP:
      *changeFlags |= CHANGED_GAP;
      *stateFlags |= CHANGED_GAP;
      ptProps->gap = 1;
      break;

    case GEN_STORE_CONNECT:
      *changeFlags |= CHANGED_CONNECT;
      *stateFlags |= CHANGED_CONNECT;
      ptProps->connect = stp->value.i;
      break;

    case GEN_STORE_3DWIDTH:
      *changeFlags |= CHANGED_3DWIDTH;
      if (ending) {
        *stateFlags &= ~CHANGED_3DWIDTH;
        ptProps->linewidth = defProps->linewidth;
      } else {
        *stateFlags |= CHANGED_3DWIDTH;
        ptProps->linewidth = stp->value.i;
      }
      break;

    case GEN_STORE_2DWIDTH:
      *changeFlags |= CHANGED_2DWIDTH;
      if (ending) {
        *stateFlags &= ~CHANGED_2DWIDTH;
        ptProps->linewidth = defProps->linewidth2;
      } else {
        *stateFlags |= CHANGED_2DWIDTH;
        ptProps->linewidth2 = stp->value.i;
      }
      break;

    case GEN_STORE_SYMSIZE:
      *changeFlags |= CHANGED_SYMSIZE;
      if (ending) {
        *stateFlags &= ~CHANGED_SYMSIZE;
        ptProps->symsize = defProps->symsize;
      } else {
        *stateFlags |= CHANGED_SYMSIZE;
        ptProps->symsize = stp->value.i;
      }
      break;

    case GEN_STORE_SYMTYPE:
      *changeFlags |= CHANGED_SYMTYPE;
      if (ending) {
        *stateFlags &= ~CHANGED_SYMTYPE;
        ptProps->symflags = defProps->symflags;
        ptProps->symtype = defProps->symtype;
      } else {
        *stateFlags |= CHANGED_SYMTYPE;
        ptProps->symflags &= ~IOBJ_SYMF_FILL;
        ptProps->symtype = stp->value.i;
        if (ptProps->symtype < 0) {
          ptProps->symtype = -1 - ptProps->symtype;
          ptProps->symflags |= IOBJ_SYMF_FILL;
        }
      }
      break;
    }
  }
  return -1;
}

/*!
* Determines the drawing properties for point [pt] in contour [co] of object 
* [obj].  Returns the default contour drawing properties in [contProps], the
* point drawing properties in [ptProps], and the return value is the flags
* for the state of various properties (changed versus default).
*/
int istorePointDrawProps(Iobj *obj, DrawProps *contProps, DrawProps *ptProps,
                         int co, int pt)
{
  int stateFlags = 0;
  int changeFlags, nextChange;
  Ilist *list = obj->cont[co].store;
  DrawProps defProps;

  /* Get the properties for the contour */
  istoreDefaultDrawProps(obj, &defProps);
  istoreContSurfDrawProps(obj->store, &defProps, contProps, co, 
                         obj->cont[co].surf);
  *ptProps = *contProps;

  /* Go through the changes until the point index is passed */
  nextChange = istoreFirstChangeIndex(list);
  while (nextChange >= 0 && nextChange <= pt)
    nextChange = istoreNextChange(list, contProps, ptProps, &stateFlags,
                                  &changeFlags);
  return stateFlags;
}

#ifdef TO_3DMOD
// TEMPORARY
int handleNextChange(Iobj *obj, Ilist *list, DrawProps *defProps, 
                     DrawProps *ptProps, 
                     int *stateFlags, int *changeFlags, int *handleFlags)
{
  int nextChange = istoreNextChange(list, defProps, ptProps, stateFlags,
                                    changeFlags);
  if ((handleFlags & HANDLE_LINE_COLOR) && (changeFlags & CHANGED_COLOR)) {
    glColor
  }

  if ((handleFlags & HANDLE_MESH_COLOR) && (changeFlags & CHANGED_COLOR)) {
    
  }

  if ((handleFlags & HANDLE_MESH_FCOLOR) && (changeFlags & CHANGED_FCOLOR)) {
    
  }

  if ((handleFlags & HANDLE_TRANS) && (changeFlags & CHANGED_TRANS)) {
    
  }

  if ((handleFlags & HANDLE_3DWIDTH) && (changeFlags & CHANGED_3DWIDTH)) {
    //glLineWidth(ptProps->linewidth);
    //glPointSize(ptProps->linewidth);
  }

  if ((handleFlags & HANDLE_2DWIDTH) && (changeFlags & CHANGED_2DWIDTH)) {
    //b3dLineWidth(ptProps->linewidth2)
  }
      
  return  nextChange;
}
#endif
