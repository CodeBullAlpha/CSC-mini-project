/**
 * 
 */
package waste.men.datastructures.api;

/**
 * This class stores objects and the insertion and removal of objects follows the 
 * First in First out principle
 */
public interface Queues <E>{
	
	/**
	 * Returns the number of objects in the queue
	 * @return the size of the queue
	 */
	public int size();
	
	/**
	 * This returns a boolean indicating whether the queue is empty or not
	 * @return true if queue is empty and false if at least 1 object is in the queue
	 */
	public boolean isEmpty();
	
	/**
	 * This adds an object to the back of the queue
	 * @param e
	 */
	public void enqueue(E e);
	
	/**
	 * Removes the object that is at the front of the queue and returns that removed object
	 * @return removed object
	 */
	public E dequeue();
	
	/**
	 * Checks for the object that was inserted first in the queue 
	 * @return the object that is at the front of the queue
	 */
	public E first();
}
