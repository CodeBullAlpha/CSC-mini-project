/**
 * @author TW MANHEMA
 */
package DataStructures;

import java.util.Comparator;


/**
 * This abstract base class is used for assisting implementations
 * that involve the PriorityQueue interface. It implements some
 * of the methods from the interface along with other added functionalities.
 * Different Implementations of Priority Queues will have to inherit this base class.
 */
public abstract class AbstractPriorityQueueBase<K,V> implements PriorityQueue<K, V>{
	/**
	 * inner class for implementing the entry for the priority queue
	 */
	protected static class PriorityQueueEntry<K,V> implements Entry<K, V>{
		
		private K key;
		private V value;
		
		//constructor for the entry
		public PriorityQueueEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}
		
		@Override
		public K getKey() {
			return this.key;
		}

		@Override
		public V getValue() {
			return this.value;
		}
		
		/**
		 * Utility function for setting the key. To be used only by classes that inherit this
		 */
		public void setKey(K key) {
			this.key = key;
		}
		
		/**
		 * Utility for setting the value. To be used by classes that inherit this one
		 */
		public void setValue(V value) {
			this.value = value;
		}
		
	}//end of PQEntry inner class
	
	private Comparator<K> comp;//comparator used for ordering the keys in the priority queue
	
	/**
	 * Default constructor for creating an empty PQ, where 
	 * a comparable, for natural ordering is used to order keys.
	 */
	protected AbstractPriorityQueueBase() {
		this(new DefaultComparator<K>());
	}
	
	/**
	 * Constructor for creating an empty PQ, using a given comparator
	 * to order keys.
	 * @param comp
	 */
	protected AbstractPriorityQueueBase(Comparator<K> comp) {
		this.comp = comp;
	}
	
	/**
	 * This method compares the two entries according to their keys
	 * @return 0 if they are equal. -1 if o1 < o2. 1 if o1 > o2.
	 */
	protected int compare(Entry<K, V> o1, Entry<K, V> o2) {
		return comp.compare(o1.getKey(), o2.getKey());
	}
	
	/**
	 * checks if the given key is valid or not
	 */
	protected boolean validateKey(K key) throws IllegalArgumentException{
		try {
			return comp.compare(key, key) == 0;			
		}catch(ClassCastException cnfe) {
			throw new IllegalArgumentException("Invalid Key!");
		}
	}
	
	/**
	 * Checks if priority queue is empty.
	 */
	public boolean isEmpty() {return size() == 0;}
}
