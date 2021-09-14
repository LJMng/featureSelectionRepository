package featureSelection.repository.entity.alg.xu;

/**
 * An interface for Deque.
 * 
 * @author Benjamin_L
 *
 * @param <Item>
 * 		Deque element type.
 */
public interface Deque<Item> {
	int size();
	boolean isEmpty();
	void addFirst(Item item);
	void addLast(Item item);
	Item popFirst();
	Item popLast();
}