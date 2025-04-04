/**
 * 
 */
package waste.men.datastructures.impl.linear;

import waste.men.datastructures.api.*;

/**
 * This class is a circular based Queue which is implemented using the circular
 * linked list class. This class makes use of the Adapter Design principles
 * were the Adapter is the class itself and the adaptee is the circular linked
 * list
 */
public class LinkedCircularQueue <E> implements Queues<E>{
	
	CLinkedList<E> circleList = new CLinkedList<E>();//adaptee
	
	/**
	 * Moves the object in the front of the queue to the back of the queue
	 */
	public void rotate() {
		circleList.rotate();
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return circleList.isEmpty();
	}

	@Override
	public void enqueue(E e) {
		// TODO Auto-generated method stub
		circleList.addLast(e);
	}

	@Override
	public E dequeue() {
		// TODO Auto-generated method stub
		return circleList.removeFirst();
	}

	@Override
	public E first() {
		// TODO Auto-generated method stub
		return circleList.first();
	}

}
