/* This file generated automatically from template MaxType.java.in. */
  /*
  The MIT License

  Copyright (c) 2005 
    1. Distributed Systems Group, University of Portsmouth
    2. Community Grids Laboratory, Indiana University 

  Permission is hereby granted, free of charge, to any person obtaining
  a copy of this software and associated documentation files (the
  "Software"), to deal in the Software without restriction, including
  without limitation the rights to use, copy, modify, merge, publish,
  distribute, sublicense, and/or sell copies of the Software, and to
  permit persons to whom the Software is furnished to do so, subject to
  the following conditions:

  The above copyright notice and this permission notice shall be included
  in all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
  KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
  WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
/*
 * File         : MaxDouble.java
 * Author       : Aamir Shafi, Bryan Carpenter
 * Created      : Fri Sep 10 12:22:15 BST 2004
 * Revision     : $Revision: 1.8 $
 * Updated      : $Date: 2005/09/03 12:48:06 $
 */

  package mpi;

  import mpjbuf.*;

  public class MaxDouble extends Max {
    double [] arr = null;
    MaxDouble() {
    }

    void perform (Object buf1, int offset, int count) throws MPIException {
     double[] arr1 = (double[]) buf1;

     for (int i = 0+offset; i < count; i++) {
       if (arr1[i] > arr[i]) {
         arr[i] = arr1[i];
       }
     }
    }

    void createInitialBuffer(Object buf, int offset, int count) 
	    throws MPIException {
      double[] tempArray = (double[]) buf;
      arr = new double[tempArray.length];
      System.arraycopy(buf, offset, arr, offset, count) ;
    }

    void getResultant(Object buf, int offset, int count ) throws MPIException {
      double[] tempArray = (double[]) buf;
      System.arraycopy(arr, offset, tempArray, offset, count);
    }
  }
