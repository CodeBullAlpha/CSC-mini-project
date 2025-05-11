package DataStructures;
/**
 * @author TW MANHEMA
 */


/**
 * 
 */
public interface LinkedListInterface <E>{
	
	/**
	 * 
	 * @param element
	 */
	public void addFirst(E element);
	
	/**
	 * 
	 * @param element
	 */
	public void addLast(E element);
	
	/**
	 * 
	 * @return
	 */
	public E first();
	
	/**
	 * 
	 * @return
	 */
	public E last();
	
	/**
	 * 
	 * @return
	 */
	public E removeFirst();
	
	/**
	 * 
	 * @return
	 */
	public boolean isEmpty();
	
	/**
	 * 
	 * @return
	 */
	public E getNext();
	
	/**
	 * 
	 * @return
	 */
	public E getNode(int i);
	
	/**
	 * 
	 * @return
	 */
	public int size();
	
}
