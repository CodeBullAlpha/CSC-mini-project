package DataStructures;

import java.util.Random;

public class HashMap<K, V> implements IHashMap<K,V> {

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
    private DLinkedList<V>[] bucketArray;

    public HashMap() {
	capacity = 100000;
	size = 0;

	prime = 894167;

	Random rand = new Random();
	scale = rand.nextInt(prime  - 1) + 1;
	shift = rand.nextInt(prime);

	bucketArray = (DLinkedList<V>[])new DLinkedList[capacity];
    }

    @Override
    public int size() {
	return size;
    }

    @Override
    public boolean isEmpty() {
	return size == 0;
    }

    @Override
    public boolean containsKey(K key) {
	int index = hash(key);

	if(bucketArray[index] == null)
	    return false;
	
	if(bucketArray[index].isEmpty())
	    return false;

	return true;
    }

    @Override
    public V get(K key){
	int index = hash(key);

	if(bucketArray[index] == null || bucketArray[index].isEmpty())
	    return null;

	return bucketArray[index].first().getElement();
    }
    
    @Override
    public V put(K key, V value) {
	int index = hash(key);
	
	if(bucketArray[index] == null) {
	    bucketArray[index] = new DLinkedList<V>();
	}
	
	bucketArray[index].addFirst(value);
	size++;

	return value;
    }
    
    @Override
    public V remove(K key) {
	int index = hash(key);
	
	if(size == 0 || bucketArray[index] == null || bucketArray[index].isEmpty())
	    return null;

	size--;

	return bucketArray[index].removeFirst();
    }

    /**
     * Computes the hash of the supplied key and compresses it to be in the range [0, capacity-1]
     * @return compressed hash of the supplied key
     */
    private int hash(K key){
	return (int)((Math.abs(key.hashCode()*scale + shift) % prime) % capacity);
    }
}
