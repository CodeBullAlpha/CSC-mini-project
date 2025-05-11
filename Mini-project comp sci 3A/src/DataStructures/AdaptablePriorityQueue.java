package DataStructures;

import java.util.Comparator;



/**
 * 
 */
public class AdaptablePriorityQueue <K, V> extends PriorityQueueHeap<K, V> 
						   implements AdaptablePriorityQueueInterface<K, V>{

	/**
	 * This is an extension of the entry class, which maintains its location
	 */
	protected static class AdaptablePQEntry<K, V> extends PriorityQueueEntry<K, V>{
		private int index;
		public AdaptablePQEntry(K key, V value, int index) {
			super(key, value);
			this.index = index;
		}
		public int getIndex() {
			return index;
		}
		public void setIndex(int i) {
			index = i;
		}
		
	}//end of nested class
	
	/**
	 * Creates an empty adaptable priority queue with a default comparator 
	 * which compares keys using natural ordering
	 */
	public AdaptablePriorityQueue() {
		super();
	}
	

	/**
	 * Creates an empty adaptable priority queue with a user defined comparator 
	 * which compares keys using natural ordering
	 */
	public AdaptablePriorityQueue(Comparator<K> comp) {
		super(comp);
	}
	
	/**
	 * Checks if the entry passed is location aware and returns the location aware entry
	 * 
	 */
	protected AdaptablePQEntry<K, V> validateEntry(Entry<K, V> entry)
								throws IllegalArgumentException{
		
		if(!(entry instanceof AdaptablePQEntry)) {
			throw new IllegalArgumentException("Invalid entry");
		}
		AdaptablePQEntry<K, V> locator = (AdaptablePQEntry<K, V>) entry;//safe casting the given entry
		int i = locator.getIndex();
		//checking the index bounds and checking if the element at that index is correct
		if(i > heap.size() || heap.get(i) != locator) {
			throw new IllegalArgumentException("Invalid entry");
		}
		return locator;
	}
	
	/**
	 * swaps the entries in the given indices of the underlying arraylist
	 * while updating and maintaining the location of the entries.
	 */
	protected void swap(int i, int j) {
		super.swap(i, j);//performs the swap
		((AdaptablePQEntry<K, V>) heap.get(i)).setIndex(i);//rest the entry's index
		((AdaptablePQEntry<K, V>) heap.get(j)).setIndex(j);//reset the entry's index
		
	}
	
	/**
	 * This restores the heap properties by moving the entry at the given index,
	 * either through upheap or downheap.
	 */
	protected void bubble(int i) {
		if(i > 0 && compare(heap.get(i),heap.get(parent(i))) < 0) {
			upHeap(i);
		}
		else {
			downHeap(i);
		}
	}
	
	public PriorityQueueEntry<K, V> insert(K key, V value) throws IllegalArgumentException{
		validateKey(key);//check if the key is correct
		PriorityQueueEntry<K, V> newEntry = new AdaptablePQEntry<>(key, value, heap.size());
		heap.add(newEntry);//add to the end of the list
		upHeap(heap.size() - 1);//upheap the newly added entry
		return  newEntry;
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public Entry<K, V> remove(Entry<K, V> entry) throws IllegalArgumentException{
		AdaptablePQEntry<K, V> locator = validateEntry(entry);
		Entry<K, V> removed = entry;
		int i = locator.getIndex();
		
		if(i == heap.size() - 1) {//entry is already at the end so no need for bubble
			heap.remove(i);
		}
		else {
			swap(i, heap.size() - 1);//swap the index of given entry with last index
			heap.remove(heap.size() - 1);//remove the entry
			bubble(i);			
		}
		return removed;
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public Entry<K, V> replaceKey(Entry<K, V> entry, K key) throws IllegalArgumentException{
		validateEntry(entry);
		validateKey(key);
		AdaptablePQEntry<K, V> locator = (AdaptablePQEntry<K, V>)entry;
		locator.setKey(key);//log issue: setKey needs to be protected not public
		bubble(locator.getIndex());
		return locator;
	}
	
	@Override
	public Entry<K, V> replaceValue(Entry<K, V> entry, V value) {
		AdaptablePQEntry<K, V> locator = validateEntry(entry);
		locator.setValue(value);
		return locator;
	}
}

