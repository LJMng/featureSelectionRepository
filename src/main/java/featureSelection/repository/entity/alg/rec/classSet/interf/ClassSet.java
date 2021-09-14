package featureSelection.repository.entity.alg.rec.classSet.interf;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;

/**
 * An interface for class item.The interface itself represents a line of an overall table which can be 
 * considered as a set.
 * 
 * @author Benjamin_L
 *
 * @param <Item> can be the class of Universe, which represents a record of a Universe, or can be the class 
 * 		EquivalenceClassItem, which represents a table set of Equivalence class items. 
 */
public interface ClassSet<Item> {		
	/**
	 * Add an Item.
	 * 
	 * @param item
	 * 		The {@link Item} to be added
	 */
	public void addClassItem(Item item);
		
	/**
	 * Return the type of the items
	 * 
	 * @return {@link ClassSetType}
	 */
	public ClassSetType getType();
	
	/**
	 * Get the size of items
	 * 
	 * @return the size of items
	 */
	public int getItemSize();
	
	/**
	 * Get the size of {@link Instance}s.
	 * 
	 * @return An int value
	 */
	public int getInstanceSize();
}