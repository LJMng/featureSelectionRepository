package featureSelection.repository.entity.alg.rec.classSet.impl;

import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;
import featureSelection.repository.entity.alg.rec.classSet.interf.ClassSet;
import lombok.Getter;

import java.util.Collection;
import java.util.LinkedList;

/**
 * An Class for rough equivalence class, represents a rough equivalence class whose items
 * are {@link EquivalenceClass}es.
 * <p>
 * {@link #type} is maintained automatically when calling {@link #addClassItem(EquivalenceClass)}
 *
 * @see ClassSet
 * @see EquivalenceClass
 * 
 * @author Benjamin_L
 */
public class RoughEquivalenceClass<EquClass extends EquivalenceClass>
		implements ClassSet<EquClass>
{
	protected Collection<EquClass> equClasses;
	@Getter protected ClassSetType type;
	@Getter protected int decision, instanceSize;
		
	public RoughEquivalenceClass() {
		equClasses = new LinkedList<>();
	}
	
	/**
	 * Add an {@link EquClass} into this rough equivalence class.
	 * <p>
	 * If it is the 1st element, then set the key value, values of the sub attributes, and
	 * decision value.
	 * 
	 * @param item
	 * 		An {@link EquClass} to be added
	 * @return true if added successfully./false if the instance already exists
	 */
	@Override
	public void addClassItem(EquClass item) {
		if (!equClasses.isEmpty()) {
			// If meets the condition of 1-REC
			if (ClassSetType.POSITIVE.equals(type) && item.sortable() && decision==item.getDecisionValue()){
				// do nothing
			// else if meets the condition of -1-REC
			}else if (ClassSetType.NEGATIVE.equals(type) && !item.sortable()) {
				// do nothing
			// else if meets the condition of 0-REC
			}else if (!ClassSetType.BOUNDARY.equals(type)){
				type = ClassSetType.BOUNDARY;	//E.cons = 0
				decision = -1;					//E.dec = -1
			}
		}else {
			type = item.getType();
			decision = item.getDecisionValue();
		}
		equClasses.add(item);
		instanceSize += item.getItemSize();//*/
	}
	
	/**
	 * Get the Equivalence Classes.
	 * 
	 * @return {@link #equClasses}
	 */
	public Collection<EquClass> getItems() {
		return equClasses;
	}
	
	/**
	 * Get the EquivalenceClassItem set's size
	 * 
	 * @return the size of the EquivalenceClassItem set
	 */
	public int getItemSize() {
		return equClasses.size();
	}
}