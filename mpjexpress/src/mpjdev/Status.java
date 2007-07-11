/*
 The MIT License

 Copyright (c) 2005 - 2007
   1. Distributed Systems Group, University of Portsmouth (2005)
   2. Aamir Shafi (2005 - 2007)
   3. Bryan Carpenter (2005 - 2007)
   4. Mark Baker (2005 - 2007)

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be included
 in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package mpjdev;

import xdev.ProcessID;
import java.util.UUID;

/*
 * File         : Status.java
 * Author       : Sang Lim, Bryan Carpenter, Aamir Shafi
 * Created      : Thu Jan 17 17:20:40 2002
 * Revision     : $Revision: 1.11 $
 * Updated      : $Date: 2005/11/27 19:40:12 $
 */

/**
 * Status object describing a completed communication.
 */

public class Status {

  /**
   * For a receive operation, the source of the message.
   */
  public int source;
  /**
   * For a receive operation, the tag in the message.
   */
  public int tag;
  /**
   * For a `waitany()' operation in class `Request'.
   * This field defines which communication in the `reqs' array was selected.
   */
  public int index;
  public int count;
  public int countInBytes ; 
  public int numEls;
  public mpjbuf.Type type;
  public java.util.UUID srcID = null;

  public Status() {
  }

  public Status(UUID uid, int tag, int index) {
    this.srcID = uid;
    this.tag = tag;
    this.index = index;
  }

  public Status(UUID srcID, int tag, int index, mpjbuf.Type type, int numEls) {
    this.srcID = srcID;
    this.tag = tag;
    this.index = index;
    this.type = type;
    this.numEls = numEls;
  }
  
  public Status(int source, int tag, int index, mpjbuf.Type type, int numEls) {
    this.source = source;
    this.tag = tag;
    this.index = index;
    this.type = type;
    this.numEls = numEls;
  }

  public Status(int source, int tag, int index) {
    this.source = source;
    this.tag = tag;
    this.index = index;
  }
}
