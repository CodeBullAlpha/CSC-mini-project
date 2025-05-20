/**
 * 
 */
package DataStructures;

import java.util.Random;
//import java.util.Iterator;
//import java.util.Iterator;

/**
 * 
 */
public class MyArrayList <E> implements Iterable<E>{
	
    private static final int DEFAULT_CAPACITY = 16;
    private static int capacity = DEFAULT_CAPACITY;
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
     * @return the current number of elements in the arraylist
     */
    public int size() {
	return size;
    }

    /**
     * @return boolean on whether or not there are items in the arraylist
     */
    public boolean isEmpty() {
	return size == 0;
    }

    /**
     * Returns the element located at the supplied index.
     * @param index index of the item to return
     * @return element located at the supplied index.
     */
    public E get(int index) {
	if(index <= 0 || index >= size)
	    return null;
	return arrList[index];
    }

    /**
     * Adds an item to the specified index of the array list
     * @param index index of item to add
     * @param item item to add to specified index
     */
    public void add(int index, E item) {
	if(index <= 0 || index >= size)
	    return;
	arrList[index] = item;
    }

    /**
     * Checks if an item is contained in the array list
     * @param item item to search for
     * @return a boolean on whether or not the arraylist contains the supplied element;
     */
    public boolean contains(E item) {
	for(int i = 0; i < size; i++) {
	    if(arrList[i].equals(item))
		return true;
	}

	return false;
    }
    
    /**
     * Clears all the elements of the arraylist
     */
    @SuppressWarnings("unchecked")
    public void clear() {
	size = 0;
	capacity = DEFAULT_CAPACITY;
	arrList = (E[])new Object[capacity];
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
     * Returns a sub list starting and ending at the supplied indices
     * @param startIndex where the sub list should start
     * @param endIndex where the sub list should end
     */
    public CustomArrayList<E> subList(int startIndex, int endIndex) {
	CustomArrayList<E> subList = new CustomArrayList<>();

	for(int i = startIndex; i < endIndex; i++) {
	    subList.add(get(i));
	}

	return subList;
    }

    /**
     * Shuffles the items in the array list
     */
    public void shuffle(){
	Random rand = new Random();
	int newIndex = 0;
	for(int i = size - 1; i >= 1; i--) {
	    newIndex = rand.nextInt(0, size);
	    E tmp = arrList[i];
	    arrList[i] = arrList[newIndex];
	    arrList[newIndex] = tmp;
	}
    }

    /**
     * Removes an item at the specified index
     * @param index index of the item to remove.
     */

    public void remove(int index) {
	if(index <= 0 || index >= size)
	    return;

	for(int i = index + 1; i < size; i++)
	    arrList[i-1] = arrList[i];

	--capacity;
    }

    /**
     * Removes the specified item (if contained) from the list;
     * @param item item to remove.
     */
    public void remove(E item) {
	int index = -1;
	
	for(int i = 0; i < size; i++)
	    if(item.equals(arrList[i]))
		index = i;

	remove(index);
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
	
    @Override
    public Iterator<E> iterator() {
	return  (Iterator<E>) new MyArrayListIterator();
    }
}
