/**
 * @author TW MANHEMA
 */
package DataStructures;

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
