package DataStructures;

import java.util.Random;
import java.util.ArrayList;

public class HashMap<K, V> {

    /*
     * Current number of items in the HashMap.
    */
    private int size;

    /*
     * Fixed capacity of the HashMap's underlying bucket array.
    */
    private int capacity;

    /*
     * Integers used in computing a key's hash.
    */    
    private int prime;
    private int scale, shift;


    /*
     * The underlying bucket array used to store items.
    */    
    private SLinkedList<V>[] bucketArray;

    public HashMap() {
	capacity = 100000;
	size = 0;

	prime = 894167;

	Random rand = new Random();
	scale = rand.nextInt(prime  - 1) + 1;
	shift = rand.nextInt(prime);

	bucketArray = (SLinkedList<V>[])new SLinkedList[capacity];
    }

    /*
     * @return current number of items in the HashMap.
     */
    public int size() {
	return size;
    }

    /*
     * @return a boolean of whether or not the hashmap has items.
     */
    public boolean isEmpty() {
	return size == 0;
    }

    /* @param key supplied key
     * @return value associated with the supplied key
     */
    public V get(K key){
	int index = hash(key);

	if(bucketArray[index] == null || bucketArray[index].isEmpty())
	    return null;

	return bucketArray[index].first();
    }

    /*
     * Associates a key with a value and returns the value.
     * @param key supplied key.
     * @param value supplied value to associate with key.
     * @return value associated with key 
     */
    public V put(K key, V value) {
	int index = hash(key);
	
	if(bucketArray[index] == null) {
	    bucketArray[index] = new SLinkedList<V>();
	}
	
	bucketArray[index].addFirst(value);
	size++;

	return value;
    }

    /*
     * Removes the first value associated with supplied key and returns it.
     * @param key
     * @return old value associated with supplied key.
     */
    public V remove(K key) {
	int index = hash(key);
	
	if(size == 0 || bucketArray[index] == null || bucketArray[index].isEmpty())
	    return null;

	size--;

	return bucketArray[index].removeFirst();
    }
    
    private int hash(K key){
	return (int)((Math.abs(key.hashCode()*scale + shift) % prime) % capacity);
    }
}
