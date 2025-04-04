/**
 * @author TW MANHEMA
 */
package waste.men.datastructures.api;

/**
 * This interface defines the operations of a priority queue
 */
public interface PriorityQueue <K,V>{
	
	/**
	 * 
	 * @return number of elements in PQ
	 */
	public int size();
	
	/**
	 * 
	 * @return true if PQ is empty else false
	 */
	public boolean isEmpty();
	
	/**
	 * insert a key-value pair into the priority queue 
	 * @param key
	 * @param value
	 * @return an entry object of the key-value added
	 * @throws IllegalArgumentException
	 */
	Entry<K,V> insert(K key, V value) throws IllegalArgumentException;
	
	/**
	 * Checks for the value that has a minimal key associated with it and returns the key-value pair.
	 * This, however, does not remove any entry. 
	 * @return the entry with minimal key (highest priority in the queue) 
	 */
	Entry<K, V> min();
	
	/**
	 * Checks for the entry with the minimal key and returns it.
	 * This then removes that entry from the queue.
	 * @return entry with minimal key (highest priority in the queue) 
	 */
	Entry<K, V> removeMin();

}
