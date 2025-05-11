package DataStructures;

public class CLinkedList <E> implements LinkedListInterface<E> {
	
	public class Node {
		private E data;
		private Node next;
		
		public Node(E data, Node next) {
			this.data = data;
			this.next = next;
		}
	}
	
	//private Node head;//there is no need to explicitly keep track of the head node
	private Node tail;//tail.getNext() will refer to the head of the list
	private int size;
	
	public CLinkedList() {
		this.tail = null;
		this.size = 0;
	}
	
	/**
	 * Moves the first element to the end of the list
	 */
	public void rotate() {
		if(tail == null);
		else {
			/*
			 * The head becomes the tail now
			 * The new head is what the previous head was pointing at
			 * so now tail.next == head.next (assuming head was explicitly defined)
			 */
			tail = tail.next;
		}
	}
	
	@Override
	public void addFirst(E element) {
		// TODO Auto-generated method stub
		if(isEmpty()) {
			tail = new Node(element, null);
			tail.next = tail;//it links to itself, such that it is both tail and head
			size++;
		}
		else {
			Node newNode = new Node(element, tail.next);
			tail.next = newNode;
			size++;
		}
	}

	@Override
	public void addLast(E element) {
		// TODO Auto-generated method stub
		addFirst(element);
		tail = tail.next;
		size++;
	}

	@Override
	public E first() {
		// TODO Auto-generated method stub
		return isEmpty() == true ? null : tail.next.data;
	}

	@Override
	public E last() {
		// TODO Auto-generated method stub
		return isEmpty() == true ? null : tail.data;
	}

	@Override
	public E removeFirst() {
		// TODO Auto-generated method stub
		E removed = tail.next.data;
		if(isEmpty())
			return null;
		else {
			tail.next = tail.next.next;
			size--;
		}
		
		return removed;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return size == 0;
	}

	@Override
	public E getNext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public E getNode(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return size;
	}

}
