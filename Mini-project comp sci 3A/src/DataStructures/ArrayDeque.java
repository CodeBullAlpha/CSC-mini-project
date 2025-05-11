package DataStructures;

/**
 * 
 */
public class ArrayDeque <E> implements Deques<E>, Cloneable{
	
	private static final int CAPACITY = 1000;
	private E [] circleQue;
	private int size;
	private int front;
	private int back;
	private int cap;
	
	public ArrayDeque() {this(CAPACITY);}
	
	@SuppressWarnings("unchecked")
	/**
	 * Constructor, takes an initial capacity size
	 * @param capacity
	 */
	public ArrayDeque(int capacity) {
		this.cap = capacity;
		circleQue = (E[]) new Object[cap];
		front = back = 0;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	/**
	 * {@inheritDoc}
	 */
	public Deques<E> clone() throws CloneNotSupportedException{
		Deques<E> cloned = (ArrayDeque<E>) super.clone();
		
		if(isEmpty()) {
			return null;
		}
		Deques<E> temp = new ArrayDeque<E>();
		for(int i = 0; i < size(); i++) {
			temp.addFirst(cloned.removeFirst());
		}
		return temp;
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public void addFirst(E e) {
	
		/*
		 * If the Deque is full, then that means the index in the array that 
		 * comes before the front pointer is occupied.
		 * If the Deque is not full, then the index before the front pointer is
		 * unoccupied and we can fill it in and assign that to be the new front
		 */
		if(size == cap) {
			throw new IllegalStateException("Deque is FULL");
		}
		
		front = ( (front - 1) + cap) % cap;//moves the front pointer back by 1 index
		circleQue[front] = e;
		size++;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void addLast(E e) {
		if(size == cap) {
			throw new IllegalStateException("Deque is FULL");
		}
		back = front + size % cap;
		circleQue[back] = e;
		size++;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public E removeFirst() {
		if(isEmpty()) {
			throw new IllegalStateException("Cannot remove from an empty Deque");
		}
		E removed = first();
		circleQue[front] = null;
		front = (front + 1) % cap;
		size--;
		return removed;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public E removeLast() {
		if(isEmpty()) {
			throw new IllegalStateException("Cannot remove from an empty Deque");
		}
		E removed = last();
		circleQue[back - 1] = null;
		back = (back - 1) % cap;
		size--;
		return removed;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public E first() {
		if(isEmpty()) return null;
		return circleQue[front];
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public E last() {
		if(isEmpty()) return null;
		return circleQue[back-1];
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public int size() {
		return size;
	}

}
