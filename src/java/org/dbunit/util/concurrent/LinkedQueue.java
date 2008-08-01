/*
  File: LinkedQueue.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  11Jun1998  dl               Create public version
  25aug1998  dl               added peek
  10dec1998  dl               added isEmpty
  10oct1999  dl               lock on node object to ensure visibility
*/

package org.dbunit.util.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A linked list based channel implementation.
 * The algorithm avoids contention between puts
 * and takes when the queue is not empty. 
 * Normally a put and a take can proceed simultaneously. 
 * (Although it does not allow multiple concurrent puts or takes.)
 * This class tends to perform more efficently than
 * other Channel implementations in producer/consumer
 * applications.
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]
 * 
 * 
 * @author Doug Lea
 * @author Last changed by: $Author$
 * @version $Revision$ $Date$
 * @since ? (pre 2.1)
 */
public class LinkedQueue implements Channel {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(LinkedQueue.class);


  /** 
   * Dummy header node of list. The first actual node, if it exists, is always 
   * at head_.next. After each take, the old first node becomes the head.
   **/
  protected LinkedNode head_;         

  /**
   * Helper monitor for managing access to last node.
   **/
  protected final Object putLock_ = new Object(); 

  /** 
   * The last node of list. Put() appends to list, so modifies last_
   **/
  protected LinkedNode last_;         

  /**
   * The number of threads waiting for a take.
   * Notifications are provided in put only if greater than zero.
   * The bookkeeping is worth it here since in reasonably balanced
   * usages, the notifications will hardly ever be necessary, so
   * the call overhead to notify can be eliminated.
   **/
  protected int waitingForTake_ = 0;  

  public LinkedQueue() {
    head_ = new LinkedNode(null); 
    last_ = head_;
  }

  /** Main mechanics for put/offer **/
  protected void insert(Object x) {
        logger.debug("insert(x=" + x + ") - start");
 
    synchronized(putLock_) {
      LinkedNode p = new LinkedNode(x);
      synchronized(last_) {
        last_.next = p;
        last_ = p;
      }
      if (waitingForTake_ > 0)
        putLock_.notify();
    }
  }

  /** Main mechanics for take/poll **/
  protected synchronized Object extract() {
        logger.debug("extract() - start");

    synchronized(head_) {
      Object x = null;
      LinkedNode first = head_.next;
      if (first != null) {
        x = first.value;
        first.value = null;
        head_ = first; 
      }
      return x;
    }
  }


  public void put(Object x) throws InterruptedException {
        logger.debug("put(x=" + x + ") - start");

    if (x == null) throw new IllegalArgumentException();
    if (Thread.interrupted()) throw new InterruptedException();
    insert(x); 
  }

  public boolean offer(Object x, long msecs) throws InterruptedException {
        logger.debug("offer(x=" + x + ", msecs=" + msecs + ") - start");
 
    if (x == null) throw new IllegalArgumentException();
    if (Thread.interrupted()) throw new InterruptedException();
    insert(x); 
    return true;
  }

  public Object take() throws InterruptedException {
        logger.debug("take() - start");

    if (Thread.interrupted()) throw new InterruptedException();
    // try to extract. If fail, then enter wait-based retry loop
    Object x = extract();
    if (x != null)
      return x;
    else { 
      synchronized(putLock_) {
        try {
          ++waitingForTake_;
          for (;;) {
            x = extract();
            if (x != null) {
              --waitingForTake_;
              return x;
            }
            else {
              putLock_.wait(); 
            }
          }
        }
        catch(InterruptedException ex) {
          --waitingForTake_; 
          putLock_.notify();
          throw ex; 
        }
      }
    }
  }

  public Object peek() {
        logger.debug("peek() - start");

    synchronized(head_) {
      LinkedNode first = head_.next;
      if (first != null) 
        return first.value;
      else 
        return null;
    }
  }    


  public boolean isEmpty() {
        logger.debug("isEmpty() - start");

    synchronized(head_) {
      return head_.next == null;
    }
  }    

  public Object poll(long msecs) throws InterruptedException {
        logger.debug("poll(msecs=" + msecs + ") - start");

    if (Thread.interrupted()) throw new InterruptedException();
    Object x = extract();
    if (x != null) 
      return x;
    else {
      synchronized(putLock_) {
        try {
          long waitTime = msecs;
          long start = (msecs <= 0)? 0 : System.currentTimeMillis();
          ++waitingForTake_;
          for (;;) {
            x = extract();
            if (x != null || waitTime <= 0) {
              --waitingForTake_;
              return x;
            }
            else {
              putLock_.wait(waitTime); 
              waitTime = msecs - (System.currentTimeMillis() - start);
            }
          }
        }
        catch(InterruptedException ex) {
          --waitingForTake_; 
          putLock_.notify();
          throw ex; 
        }
      }
    }
  }
}


