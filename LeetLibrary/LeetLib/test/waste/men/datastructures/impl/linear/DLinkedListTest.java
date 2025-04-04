/**
 * 
 */
package waste.men.datastructures.impl.linear;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import waste.men.datastructures.api.Position;

/**
 * 
 */
public class DLinkedListTest {
	
	private DLinkedList<String> list;
	
	@BeforeEach
	/**
	 * This creates a fresh list before every test
	 */
	void setUp() {
		list = new DLinkedList<String>();
	}
	
	@Test
	@DisplayName("New list should be empty")
	/**
	 * Testing the isEmpty() method
	 */
	void emptyListTest() {
		assertTrue(list.isEmpty());//should return true
		assertEquals(0, list.size());//should return true
	}
	
	@Test
	@DisplayName("when addFirst() is called, first() and size() should be updated")
	/**
	 * Tests 3 methods in 1 go. Tests if
	 * addFirst() works and if so, then
	 * first() method has to be updated as well as the size() method
	 */
	void addFirstTest() {
		Position<String> p1 = list.addFirst("First Element"); //creates the
		assertEquals("First Element", p1.getElement());//must be true
		assertEquals(1, list.size());
		assertEquals(p1, list.first());
	}
	
	@Test
	@DisplayName("when remove() is called it is to return the removed element")
	void removeTest() {
		Position<String> p1 = list.addFirst("Front");
		assertEquals("Front", list.remove(p1));
	}
}
