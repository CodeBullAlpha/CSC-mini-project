package waste.men.datastructures.impl.linear;

import waste.men.datastructures.api.*;

public class DLinkedList <E> implements PositionalList<E>{
	
	/**
	 * 
	 */
	@SuppressWarnings("hiding")
	private class Node<E> implements Position<E>{
		private E element;
		private Node<E> prev;
		private Node<E> next;
		
		public Node(E element, Node<E> prev, Node<E> next) {
			setElement(element);
			setPrev(prev);
			setNext(next);
		}
		
		public void setPrev(Node<E> prev) {
			this.prev = prev;
		}
		
		public void setNext(Node<E> next) {
			this.next = next;
		}
		
		public void setElement(E element) {
			this.element = element;
		}
		
		public Node<E> getPrev() {
			return this.prev;
		}
		
		public Node<E> getNext() {
			return this.next;
		}

		@Override
		public E getElement() throws IllegalStateException {
			if(getNext() == null) throw new IllegalStateException("Position is no longer valid");
			return this.element;
		}
		
	}//end of inner Node<E> class
	
	/*
	 * 
	 */
	private Node<E> header;
	private Node<E> trailer;
	private int size;
	
	/**
	 * 
	 */
	public DLinkedList() {
		header = new Node<>(null, null, null);
		trailer = new Node<>(null, header, null);
		header.setNext(trailer);
		size = 0;
	}
	
	/*
	 * The following two methods are utility methods used for 
	 * validating the conversion between nodes and position, ensuring
	 * that this class is robust and highly abstracts the nodes from the user
	 */
	
	/**
	 * Checks if the position is valid, casts the position
	 * to a node, then returns a node
	 */
	private Node<E> validate(Position<E> p) throws IllegalArgumentException{
		//check if the position is an instance of node
		if( !(p instanceof Node) ) 
			throw new IllegalArgumentException("Invalid position passed!");
		Node<E> node = (Node<E>)p; // safely casting position to a node
		if(node.getNext() == null)//checks if position passed, still exists in the list
			throw new IllegalArgumentException("position given is no longer in the list");
		return node;//return the node representation of the given position
	}
	
	/**
	 * returns the given node as a position
	 */
	private Position<E> position(Node<E> node){
		if(node == header || node == trailer)
			return null;//sentinels are not to be exposed to the user of this class
		return node;//implicitly casts the given node to type Position<E>
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public Position<E> addFirst(E element) {
		// TODO Auto-generated method stub
		return addBetween(element,header,header.getNext());
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public Position<E> addLast(E element) {
		// TODO Auto-generated method stub
		return addBetween(element, trailer.getPrev(), trailer);
	}

	@Override
	/**
	 *{@inheritDoc} 
	 */
	public Position<E> first() {
		return position(header.getNext());
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public Position<E> last() {
		return position(trailer.getPrev()); 
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public Position<E> before(Position<E> p){
		Node<E> tempNode = validate(p);//validate the given position and returns its node representation
		return position(tempNode.getPrev());//gets and returns the previous position
	}
	
	@Override
	public Position<E> after(Position<E> p){
		Node<E> tempNode = validate(p);//validate the given position and return its node
		return position(tempNode.getNext());// gets and returns the next position
	}
	
	/**
	 *{@inheritDoc}
	 */
	public E remove(Position<E> p)throws IllegalArgumentException{
		Node<E> tempNode = validate(p);
		Node<E> tempNodePrev = tempNode.getPrev();
		Node<E> tempNodeNext = tempNode.getNext();
		
		//Making the previous to point to the next
		tempNodePrev.setNext(tempNodeNext);
		//Making the next's previous to point to tempNodePrev
		tempNodeNext.setPrev(tempNodePrev);
		size--;
		
		E removedElement = tempNode.getElement();
		
		//Helping the garbage collector
		tempNode.setElement(null);
		tempNode.setNext(null);
		tempNode.setPrev(null);
		
		return removedElement;
	}
	/**
	 * Removes the first position from the list
	 * @return the removed position 
	 */
	public E removeFirst() {
		// TODO Auto-generated method stub
		return remove(first());
	}
	
	/**
	 * Removes the last position from the list
	 * @return
	 */
	public E removeLast() {
		return remove(last());
	}

	@Override
	/**
	 * 
	 */
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return size == 0;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public int size() {
		return size;
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return null;
	}
	
	/**
	 * Adds an element between two given positions in the linked list.
	 * Creates a new position that will contain the element and will be between
	 * the two given nodes
	 */
	private Position<E> addBetween(E element, Node<E> prev, Node<E> next) {
		
		Node<E> newNode = new Node<>(element, prev, next);
		prev.setNext(newNode);
		next.setPrev(newNode);
		size++;
		return newNode;//implicitly is converted to Position<E>
	}
	
	/**
	 * {@inheritDoc}
	 */
	public E set(Position<E> p,E element) {
		Node<E> tempNode = validate(p);
		tempNode.setElement(element);
		return tempNode.getElement();
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public Position<E> addBefore(Position<E> p, E element) throws IllegalArgumentException {
		Node<E> tempNode = validate(p);
		return addBetween(element, tempNode.getPrev(), tempNode);
	}

	@Override
	public Position<E> addAfter(Position<E> p, E element) throws IllegalArgumentException {
		Node<E> tempNode = validate(p);
		return addBetween(element, tempNode, tempNode.getNext());
	}

}
