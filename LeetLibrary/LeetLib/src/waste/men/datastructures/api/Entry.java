/**
 * @author TW MANHEMA
 */
package waste.men.datastructures.api;

/**
 * This contains a key-value pair
 */
public interface Entry<K,V> {
	
	/**
	 * 
	 * @return key of the entry
	 */
	public K getKey();
	
	/**
	 * 
	 * @return value of the entry
	 */
	public V getValue();
}
