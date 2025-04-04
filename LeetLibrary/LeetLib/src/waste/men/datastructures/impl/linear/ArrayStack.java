/**
 * 
 */
package waste.men.datastructures.impl.linear;

import waste.men.datastructures.api.*;

/**
 * 
 */
public class ArrayStack <E> implements Stack<E> {
	private static final int CAPACITY = 1000;// default capacity
	private E[] arrStack;
	private int cap; //user specified capacity
	private int size;
	
	/**
	 * Default constructor with a default capacity for the array based stack 
	 */
	public ArrayStack() {
		this(CAPACITY);
	}
	
	/**
	 * Parameterized constructor were user mentions the capacity that the stack
	 * will take at a maximum. 
	 * @param capacity
	 */
	@SuppressWarnings("unchecked")
	public ArrayStack(int capacity) {
		this.cap = capacity;
		arrStack = (E[]) new Object[capacity];
		size = 0;
	}
	
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return size;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return size == 0;
	}

	@Override
	public void push(E e) throws IllegalAccessException {
		// TODO Auto-generated method stub
		if(size <= cap) {
			arrStack[size] = e;
			size++;			
		}
		else {
			throw new IllegalAccessException("STACKOVERFLOW! stack is full");
		}
	}

	@Override
	public E pop() throws IllegalAccessException {
		// TODO Auto-generated method stub
		if(isEmpty()) {
			throw new IllegalAccessException("Empty Stack! cannot remove from an empty stack");
		}
		E removed = arrStack[size-1];
		arrStack[size - 1] = null;
		size--;
		return removed;
	}

	@Override
	public E top() {
		// TODO Auto-generated method stub
		if(isEmpty()) return null;
		return arrStack[size - 1];
	}
	
}
