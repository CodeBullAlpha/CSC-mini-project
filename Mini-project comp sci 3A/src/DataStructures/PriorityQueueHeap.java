package DataStructures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;


/**
 * A realization of the priority queue data structure, using a Heap Binary tree for
 * its implementation
 */
public class PriorityQueueHeap<K,V> extends AbstractPriorityQueueBase<K, V>
									implements Serializable{

	/**
	 * Defualt Serial ID
	 */
	private static final long serialVersionUID = 1L;
	//the heap, which will be an array list of entries
	protected ArrayList<Entry<K, V>> heap;
	
	/**
	 * Default constructor, were the comparison of keys is based
	 * on the comparable, natural ordering
	 */
	public PriorityQueueHeap() {
		super();
		heap = new ArrayList<>();
	}
	
	public ArrayList<Entry<K, V>> getHeap() {
		return heap;
	}

	/**
	 * Constructor with user defined comparator passed as parameter
	 * @param comp
	 */
	public PriorityQueueHeap(Comparator<K> comp) {
		super(comp);
		heap = new ArrayList<>();
	}
	
	//utilities
	
	//parent of a given node based on index of an array
	protected int parent(int i) {
		return (i-1) / 2;//returns the index of the parent of the given node(index)
	}
	
	//left node of the given node(index)
	protected int left(int i) {
		return 2*(i) + 1;//return index of the left child of the given node(index)
	}
	
	//right node of the given node(index)
	protected int right(int i) {
		return 2*(i) + 2;//return index of the right child of the given node(index)
	}
	
	//checks if given node(index) has a left child
	protected boolean hasLeft(int i) {
		/*
		 * checks if the index of the parent's left child is within
		 * the range of the size of the heap, if not then that means
		 * the given parent has no left child 
		 */
		return left(i) < heap.size();
	}
	
	//checks if given node(index) has a right child
	protected boolean hasRight(int i) {
		/*
		 * checks if the index of the parent's right child is within
		 * the range of the size of the heap, if not then that means
		 * the given parent has no right child 
		 */
		return right(i) < heap.size();
	}
	
	/*
	 * Will swap the two indexes given.
	 * This is used for ensuring that the heap binary tree
	 * does not violate the heap-order property. Will be used by
	 * upHeap and downHeap
	 */
	protected void swap(int i, int j) {
		Entry<K,V> temp = heap.get(i);
		heap.set(i,heap.get(j));
		heap.set(j, temp);
	}
	
	/*
	 * Moves the entry in the given index, higher, if the 
	 * heap-order property is violated during the insertion of 
	 * a new Entry in the tree.
	 */
	protected void upHeap(int i) {
		while(i > 0) {
			int p = parent(i);
			//checks if the inserted entry is greater than parent
			if(compare(heap.get(i), heap.get(p)) >= 0)
				break;
			swap(i,p);//swap the entries if parent is 
			i = p;//continue from the parent
		}//end while
	}
	
	/*
	 *Moves the entry in the given index, lower, if the
	 *heap property is violated during deletion of an entry 
	 */
	protected void downHeap(int i) {
		
		while(hasLeft(i)) {
			int leftChild = left(i);
			int smallerChild = leftChild;
			if(hasRight(i)) {
				int rightChild = right(i);
				if(compare(heap.get(leftChild), heap.get(rightChild)) > 0) {
					smallerChild = rightChild;
				}//end inner if
			}//end if
			
			if(compare(heap.get(smallerChild), heap.get(i) ) >= 0) {
				break;
			}
			swap(i,smallerChild);
			i = smallerChild;
		}
	}
	@Override
	/**
	 * {@inheritDoc}
	 */
	public int size() {
		return heap.size();
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public Entry<K, V> insert(K key, V value) throws IllegalArgumentException {
		validateKey(key);
		Entry<K, V> newEntry = new PriorityQueueEntry<>(key, value);
		heap.add(newEntry);
		upHeap(heap.size() - 1);
		return newEntry;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public Entry<K, V> min() {
		return heap.isEmpty() ? null : heap.get(0);
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public Entry<K, V> removeMin() {
		if(heap.isEmpty()) 
			return null;
		Entry<K, V> removedEntry = min();
		swap(0, heap.size() - 1);
		heap.remove(heap.size() - 1);
		downHeap(0);
		return removedEntry;
	}

}
