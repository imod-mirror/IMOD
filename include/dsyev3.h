// ----------------------------------------------------------------------------
// Numerical diagonalization of 3x3 matrcies
// Copyright (C) 2006  Joachim Kopp
// ----------------------------------------------------------------------------
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
// ----------------------------------------------------------------------------
#ifndef __DSYEV3_H
#define __DSYEV3_H
#ifdef __cplusplus
extern "C" {
#endif

  int dsyevh3(double A[3][3], double Q[3][3], double w[3]);
  int dsyevq3(double A[3][3], double Q[3][3], double w[3]);
  void dsytrd3(double A[3][3], double Q[3][3], double d[3], double e[2]);
  int dsyevc3(double A[3][3], double w[3]);

#ifdef __cplusplus
}
#endif

#endif
