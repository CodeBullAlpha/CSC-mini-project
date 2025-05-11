package DataStructures;
/**
 * 
 */
public class SLinkedListStack <E> implements Stack<E>, Cloneable {
	
	/*
	 * This will create an empty an empty linked list
	 * Making use of the Adapter Design Pattern, most of the methods of this class
	 * will be defined by the methods of the object we just instantiated
	 */
	private SLinkedList<E> listStack = new SLinkedList<E>();

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return listStack.size();//the size of the linked list will be the same as that of this stack
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return listStack.isEmpty();
	}

	@Override
	public void push(E e) throws IllegalAccessException {
		// TODO Auto-generated method stub
		listStack.addFirst(e);
		
	}

	@Override
	public E pop() throws IllegalAccessException {
		// TODO Auto-generated method stub
		return listStack.removeFirst();
	}

	@Override
	public E top() {
		// TODO Auto-generated method stub
		return listStack.first();
	}
}
