/**
 * 
 */
package waste.men.datastructures.impl.linear;

import waste.men.datastructures.api.*;

/**
 * 
 */
public class SLinkedList <E> implements LinkedListInterface<E>{
	
	/**
	 * This is the node class that will store each element of a node and the 
	 * reference of the next node of the linked list
	 */
	private class Node{
		private E element;
		private Node next;
		//private E nextVal;
		public Node(E element, Node next) {
			setElement(element);
			setNext(next);
			
		}
		
		public void setElement(E e) {
			this.element = e;
		}
		
		public void setNext(Node n) {
			this.next = n;
		}
		public Node getNextNode() {
			return this.next;
		}
		
		public E getElement() {
			return this.element;
		}
	}//end of inner class
	
	private Node head;
	private Node tail;
	//private Node next;
	private int size = 0;
	private E element;
	
	public SLinkedList() {
		head = tail =  null;
	}
	
	@Override
	/**
	 * Adds an object at the front of the linked list, making the recently added
	 * object's node the new head
	 * @param element
	 */
	public void addFirst(E element) {
		// TODO Auto-generated method stub
		if(isEmpty()) {
			Node newNode = new Node(element, null);
			tail = head = newNode;
			size++;
		}
		else {
			Node newNode = new Node(element, head);
			head = newNode;
			size++;
		}
	}
	
	/**
	 * Adds an object at the back of the linked list, making the recently added
	 * object's node be the new tail
	 * @param element
	 */
	@Override
	public void addLast(E element) {
		// TODO Auto-generated method stub
		if(isEmpty()) {
			Node newNode = new Node(element, null);
			head = tail = newNode;
			size++;
		}
		else {
			Node newNode = new Node(element, null);
			tail = newNode;
			size++;
		}
	}

	@Override
	public E first() {
		// TODO Auto-generated method stub
		return ( head == null ? null : head.getElement() );
	}

	@Override
	public E last() {
		// TODO Auto-generated method stub
		return ( tail == null ? null : tail.getElement() );
	}

	@Override
	public E removeFirst() {
		// TODO Auto-generated method stub
		if(isEmpty()) {
			return null;
		}
		E removed = head.element;
		head = head.next;
		size--;
		if(size == 0) tail = null;
		return removed;
		
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return  size == 0;
	}
	
	
	@Override
	public String toString() {
		if(isEmpty()) return "Empty";
		StringBuilder str = new StringBuilder();
		Node currentNode = head;
		for(int i = 0; i < size; i++) {
			str.append(currentNode.getElement().toString() + "->");
			currentNode = currentNode.getNextNode();
			if(currentNode == null) return str.toString();
		}
		return str.toString();
	}
	
	@Override
	public E getNext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public E getNode(int i) {
		// TODO Auto-generated method stub
		int count = 0;
		Node node = head;
		if(isEmpty()) return null;
		while(count < size) {
			if(i == count) {
				return node.element;
			}
			else {
				node = node.next;
				count++;
			}	
		}
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return size;
	}
}
