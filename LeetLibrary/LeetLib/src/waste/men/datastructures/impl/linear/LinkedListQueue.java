/**
 * 
 */
package waste.men.datastructures.impl.linear;

import waste.men.datastructures.api.*;

/**
 * 
 */
public class LinkedListQueue <E> implements Queues<E>, Cloneable{
	
	private SLinkedList<E> listQue;
	
	public LinkedListQueue() {
		listQue = new SLinkedList<E>();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Queues<E> clone() throws CloneNotSupportedException{
		LinkedListQueue<E> cloned = (LinkedListQueue<E>)super.clone();
		if(isEmpty()) {
			return null;		
		}
		Queues<E> temp = new LinkedListQueue<E>();
		for(int i = 0; i < cloned.size(); i++) {
			temp.enqueue((cloned.dequeue()));
		}
		return temp;
		
	}
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return listQue.size();
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return listQue.isEmpty();
	}

	@Override
	public void enqueue(E e) {
		// TODO Auto-generated method stub
		listQue.addLast(e);
		
	}

	@Override
	public E dequeue() {
		// TODO Auto-generated method stub
		return listQue.removeFirst();
	}

	@Override
	public E first() {
		// TODO Auto-generated method stub
		return listQue.first();
	}

}
