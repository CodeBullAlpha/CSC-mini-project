/**
 * 
 */
package waste.men.datastructures.api;

/**
 * 
 */
public interface Iterator <E> {
	
	/**
	 * 
	 * @return true if the iterator still has elements after the current one
	 */
	public boolean hasNext();
	
	/**
	 * Returns an element in the iteration, if hasNext is true
	 * @throws an Exception when hasNext returned false and user tries to access a none
	 * existing element
	 * @return the next element in the iteration
	 */
	public E next() throws IllegalStateException;
	
	/**
	 * This method removes the current element in an iterator
	 * This method is optional, and the users that implement this Interface
	 * may choose to implement this method or not.
	 * @return the removed element
	 */
	default E remove() {return null;};
}
