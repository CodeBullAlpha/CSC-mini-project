/**
 * 
 */
package waste.men.datastructures.impl.linear;

import waste.men.datastructures.api.*;

/**
 * 
 */
public class ArrayQueue <E> implements Queues<E> {
	private static final int CAPACITY = 1000;
	private int cap;
	private E[] arrQue;
	private int size;
	private int front;//front pointer of the stack, used in array to keep ttrack of bottom stack
	private int back;//back pointer of the stack, used in array to keep track of the top of stack
	/**
	 * Default constructor with a default capacity
	 */
	public ArrayQueue() {this(CAPACITY);}
	
	/**
	 * Parameterized constructor were user defines the maximum capacity of the stack
	 * @param capacity
	 */
	@SuppressWarnings("unchecked")
	public ArrayQueue(int capacity) {
		this.cap = capacity;
		arrQue = (E[]) new Object[cap];
		size = 0;
		front = back = 0;
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

	/**
	 * Adds an object to the back of the queue
	 * @param e
	 */
	@Override
	public void enqueue(E e) {
		// TODO Auto-generated method stub
		if(size == cap) {
			throw new IllegalStateException("Queue is full!");
		}
		
		/*
		 * Finds the next index in the array, at the back of the queue
		 * that is free to add an object to, using modular arithmetic
		 * if the front is at index 3 and the current size is 30 and
		 * capacity is 40, then the next available spot is at (3 + 30 % 40 = index 33)
		 * the next available spot
		 */
		back = (front + size) % cap;
		arrQue[back] = e;
		size++;//increase the size so that when we get to the end of the list we can flag the queue as full
	}

	/**
	 * Removes the object that is in the front of the queue
	 * @return removed object
	 */
	@Override
	public E dequeue() {
		// TODO Auto-generated method stub
		if(isEmpty()) {
			throw new IllegalStateException("Cannot remove from empty queue");
		}
		E removed = arrQue[front];
		arrQue[front] = null;
		front = (front + 1) % cap;//updates the front of the queue pointer on the array
		return removed;
	}
	
	/**
	 * 
	 * @return object at the front of queue
	 */
	@Override
	public E first() {
		// TODO Auto-generated method stub
		if(isEmpty()) return null;
		return arrQue[front];
	}

}
