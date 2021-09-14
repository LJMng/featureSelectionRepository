package featureSelection.repository.entity.alg.rec.classSet.interf.extension.decisionMap;

import featureSelection.repository.entity.alg.rec.classSet.interf.ClassSet;

import java.util.Collection;

/**
 * An interface for Decision values info. for extended {@link ClassSet}s.
 * 
 * @author Benjamin_L
 *
 * @param <Item>
 * 		Can be the class of Universe, which represents a record of a Universe, or can be the
 * 		Equivalence Class, which represents a table set of Equivalence class items.
 * @param <Decision>
 * 		Type of decision info.
 */
public interface DecisionInfoExtendedClassSet<Item, Decision> 
	extends ClassSet<Item>
{
	/**
	 * Get the decision info.
	 * 
	 * @return {@link Decision}.
	 */
	Decision getDecisionInfo();
	
	/**
	 * Get decision values.
	 * 
	 * @return the decision values in {@link Integer} {@link Collection}.
	 */
	Collection<Integer> decisionValues();
	/**
	 * Get the numbers of decision values.
	 * 
	 * @return the numbers in {@link Integer} {@link Collection}.
	 */
	Collection<Integer> numberValues();
}
