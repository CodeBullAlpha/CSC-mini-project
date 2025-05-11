/**
 * @author TW MANHEMA
 */
package DataStructures;
/**
 * 
 */
public interface PositionalList <E>{
	
	/**
	 * 
	 * @return the number of positions
	 */
	public int size();
	
	/**
	 * Checks if the list is empty
	 * @return true if there are no positions
	 */
	public boolean isEmpty();
	
	/**
	 * Checks for the first position in the list
	 * @return the first position in the list
	 */
	public Position<E> first();
	
	/**
	 * Checks for the last position in the list
	 * @return the last position in the list
	 */
	public Position<E> last();
	
	/**
	 * Gets the position that is before the given position in the list
	 * @param p
	 * @return the position that is before p
	 * @throws IllegalArgumentException if p does not exist
	 */
	public Position<E> before(Position<E> p) throws IllegalArgumentException;
	
	/**
	 * Gets the position that is after the given position in the list
	 * @param p
	 * @return the position that is after p
	 * @throws IllegalArgumentException if p does not exist
	 */
	public Position<E> after(Position<E> p) throws IllegalArgumentException;
	
	/**
	 * Creates a new position and the element given belongs to that new position
	 * which then is added to the front of the list
	 * @param element
	 * @return the new position containing element
	 */
	public Position<E> addFirst(E element);
	
	/**
	 * Creates a new position and the element given belongs to that new position
	 * which then is added to the back of the list
	 * @param element
	 * @return the new position containing element
	 */
	public Position<E> addLast(E element);
	
	/**
	 * Creates a new position and the element given belongs to that new position
	 * which then is added to the front of the given position
	 * @param p 
	 * @param element
	 * @return the new position containing element
	 * @throws IllegalArgumentException if the passed position p does not exist
	 */
	public Position<E> addBefore(Position<E> p, E element) throws IllegalArgumentException;
	
	/**
	 * Creates a new position and the element given belongs to that new position
	 * which then is added to the back of the given position
	 * @param p 
	 * @param element
	 * @return the new position containing element
	 * @throws IllegalArgumentException if the passed position p does not exist
	 */
	public Position<E> addAfter(Position<E> p, E element) throws IllegalArgumentException;
	
	/**
	 * Replaces an element at Position p with a new element 
	 * @param p
	 * @param element
	 * @return the replaced element 
	 * @throws IllegalArgumentException if the passed position does not exist
	 */
	public E set(Position<E> p, E element) throws IllegalArgumentException;
	
	/**
	 * Removes the element stored at position p, this invalidates p
	 * @param p
	 * @param element
	 * @return the removed element
	 * @throws IllegalArgumentException if the passed position does not exist
	 */
	public E remove(Position<E> p) throws IllegalArgumentException;
};
