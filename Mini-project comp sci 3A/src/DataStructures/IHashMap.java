package DataStructures;

public interface IHashMap<K, V>{
    /**
     * @return current number of items in the HashMap.
     */
    public int size();

    /**
     * @return a boolean of whether or not the hashmap has items.
     */
    public boolean isEmpty();

    /**
     * @param key supplied key
     * @return value associated with the supplied key
     */
    public V get(K key);

    /**
     * Associates a key with a value and returns the value.
     * @param key supplied key.
     * @param value supplied value to associate with key.
     * @return value associated with key 
     */
    public V put(K key, V value);

    /**
     * Removes the first value associated with supplied key and returns it.
     * @param key
     * @return old value associated with supplied key.
     */
    public V remove(K key);
    
}

    

