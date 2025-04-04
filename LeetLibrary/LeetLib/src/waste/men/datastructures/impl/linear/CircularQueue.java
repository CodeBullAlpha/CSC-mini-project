/**
 * 
 */
package waste.men.datastructures.impl.linear;

import waste.men.datastructures.api.*;

/**
 * 
 */
public class CircularQueue <E> extends LinkedListQueue<E>{
	
	/**
	 * Moves the front object to the back of the queue
	 */
	  public void rotate() {
		  this.enqueue(this.dequeue()); // removes the front object and places it at the back
	  }
}
