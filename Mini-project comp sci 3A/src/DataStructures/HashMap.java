package DataStructures;

import java.util.Random;
import java.util.ArrayList;

public class HashMap<K, V> {
    private int size;
    private int capacity;
    private int prime;
    private int scale, shift;
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
    
    public int size() {
	return size;
    }

    public boolean isEmpty() {
	return size == 0;
    }

    public V get(K key){
	int index = hash(key);

	if(bucketArray[index] == null || bucketArray[index].isEmpty())
	    return null;

	return bucketArray[index].first();
    }

    public V put(K key, V value) {
	int index = hash(key);
	
	if(bucketArray[index] == null) {
	    bucketArray[index] = new SLinkedList<V>();
	}

	bucketArray[index].addFirst(value);
	size++;

	return value;
    }

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
