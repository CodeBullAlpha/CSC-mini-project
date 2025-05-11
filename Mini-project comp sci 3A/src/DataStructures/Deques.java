/**
 * @author TW MANHEMA
 */
package DataStructures;

/**
 * 
 */
public interface Deques <E>{
	
	/**
	 * This method adds an object at the front of the Deque
	 */
	public void addFirst(E e);
	
	/**
	 * This methods removes an object to the front of the Deque
	 */
	public void addLast(E e);
	
	/**
	 * @return the object removed from the front of the Deque
	 */
	public E removeFirst();
	
	/**
	 * @return the object removed from the back of the Deque
	 */
	public E removeLast();
	
	/**
	 * @return object at the front of the Deque
	 */
	public E first();
	
	/**
	 * @return object at the back of the Deque
	 */
	public E last();
	
	/**
	 * @return true or false
	 */
	public boolean isEmpty();
	
	/**
	 * 
	 * @return number of objects in the Deque
	 */
	public int size();
}
