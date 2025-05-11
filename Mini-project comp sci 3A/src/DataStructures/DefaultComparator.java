/**
 * @author TW MANHEMA
 */
package DataStructures;

import java.util.Comparator;

/**
 * This class uses the natural order for comparisons.
 * This is a utility class which will be used by other classes such 
 * as PriorityQueue Abstract Class for their implementations.
 * Other Classes may use this for methods and algorithms that 
 * may require natural ordering.
 */
public class DefaultComparator<E> implements Comparator<E>{

	@SuppressWarnings("unchecked")
	@Override
	/**
	 * Will compare two given objects using natural ordering of
	 * the comparable interface.
	 * @param o1
	 * @param o2
	 * @return 0 if they are equal or 
	 * 1 if o1 > o2 or
	 * -1 if o1 < o2
	 */
	public int compare(E o1, E o2) throws ClassCastException{
		return ((Comparable<E>)o1).compareTo(o2);//we cast o1 to a comparable and call the method compareTo, for natural ordering
	}

}
