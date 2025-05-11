/**
 * 
 */
package DataStructures;

import java.util.Iterator;

/**
 * 
 */
public class MyArrayList <E> implements Iterable<E>{
	
	private static final int DEFUALT_CAPACITY = 16;
	private static int capacity = DEFUALT_CAPACITY;
	private int size = 0;	
	private E[] arrList;

	/**
	 * Default Constructor
	 */
	public MyArrayList() {
		this(capacity);
	}
	
	/**
	 * Parameterized constructor
	 * @param initialCapacity
	 */
	@SuppressWarnings("unchecked")
	public MyArrayList(int initialCapacity) {
		capacity = initialCapacity;
		arrList = (E[]) new Object[capacity];
	}
	
	/**
	 * Dynamically adds new elements to the array.
	 * @param element
	 */
	public void add(E element) {
		expandArray(size);
		arrList[size++] = element;
	}
	
	/**
	 * checks if the array is full. If it is then it will incrent the size of the 
	 * array
	 * @param size
	 */
	@SuppressWarnings("unchecked")
	private void expandArray(int length) {
		if(length >= capacity) {	
			length*=2;
			E[] arrNew = (E[]) new Object[length];
			System.arraycopy(arrList, 0, arrNew, size - 1, length);
			capacity = length;
		}
	}
	
	private class MyArrayListIterator implements Iterator<E>{
		int j = 0;
		
		@Override
		public boolean hasNext() {
			return j < size;
		}

		@Override
		public E next(){
			if(j >= size)
				throw new IllegalStateException("Reached End of List!");
			
			return arrList[j++];
		}
		
	}//end of Iterator
	
	@SuppressWarnings("unchecked")
	@Override
	public DataStructures.Iterator<E> iterator() {
		return (DataStructures.Iterator<E>) new MyArrayListIterator();
	}
}
