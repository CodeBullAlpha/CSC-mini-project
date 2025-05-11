/**
 * @author TW MANHEMA
 */
package DataStructures;
/**
 * A collection of objects that are inserted and removed
 * using the First in Last out principle.
 */
public interface Stack <E> {
	
	/**
	 * This keeps track of the number of objects stored in the stack
	 * @return size of the stack
	 */
	public int size();
	
	/**
	 * This checks whether the stack contains any objects or not
	 * @return true if the stack is empty or false if it is occupied by atleast 1 object
	 */
	public boolean isEmpty();
	
	/**
	 * 
	 * @param e takes in an  argument which will be an object that is 
	 * to be put into the stack
	 * @throws IllegalAccessException 
	 */
	public void push(E e) throws IllegalAccessException;
	
	/**
	 * Removes the object that is at the top of the stack
	 * @return the object that was removed from the stack
	 * @throws IllegalAccessException 
	 */
	public E pop() throws IllegalAccessException;
	
	/**
	 * This checks for the object that was last inserted into the stack
	 * @return the object that was last inserted into the stack
	 */
	public E top();
	
	
}
