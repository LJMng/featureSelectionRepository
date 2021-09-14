package featureSelection.repository.entity.alg.rec.classSet.impl;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;
import featureSelection.repository.entity.alg.rec.classSet.interf.ClassSet;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A Class for saving Equivalence Class items, all instance in an Equivalent Class are considered
 * equivalent based on attribute values.
 * 
 * @see Instance
 * @see ClassSet
 * 
 * @author Benjamin_L
 */
@Data
@NoArgsConstructor
public class EquivalenceClass implements ClassSet<Instance>, Cloneable {
	protected int[] attrValue;
	protected Integer decValue;
	protected int instanceCount;
	
	public EquivalenceClass(Instance instance) {
		decValue = instance.getAttributeValue(0);	// dec
		attrValue = instance.getConditionAttributeValues();
		instanceCount = 1;
	}
	
	/**
	 * Set un-sortable(in-consistent), meaning {@link Instance}s in this {@link ClassSet}
	 * don't share the same decision value.
	 * 
	 * @return this {@link EquivalenceClass}.
	 */
	public EquivalenceClass setUnsortable() {
		if (sortable())	decValue = null;
		return this;
	}
	
	/**
	 * Get whether this equivalence class is sortable(consistent) or not(in-consistent).
	 * 
	 * @see #decValue
	 * 
	 * @return true if sortable
	 */
	public boolean sortable() {
		return decValue!=null;
	}
	
	/**
	 * Return the sortability(consistency).
	 *
	 * @see ClassSetType#POSITIVE
	 * @see ClassSetType#NEGATIVE
	 * 
	 * @return {@link ClassSetType}
	 */
	@Override
	public ClassSetType getType() {
		return sortable()? ClassSetType.POSITIVE : ClassSetType.NEGATIVE ;
	}

	/**
	 * Get the attribute value by attribute index.
	 * 
	 * @param index
	 * 		The index of the attribute.(attribute starts from 0)
	 * @return attribute value
	 */
	public int getAttributeValueAt(int index) {
		return attrValue[index];
	}

	/**
	 * Get the attribute values.
	 *
	 * @see #attrValue
	 *
	 * @return An int array of the attribute's values
	 */
	public int[] getAttributeValues() {
		return attrValue;
	}

	/**
	 * Get the decision value.
	 * 
	 * @return -1 if not sortable(in-consistent)./ the decision value if sortable(consistent).
	 */
	public int getDecisionValue() {
		return sortable()? decValue: -1;
	}
	
	/**
	 * "Add" an {@link Instance} into this equivalence class:
	 * <pre>instanceCount++</pre>
	 * 
	 * @param ins
	 * 		The {@link Instance} to be added.
	 */
	public void addClassItem(Instance ins) {
		instanceCount++;
	}
	
	/**
	 * Merge the given {@link EquivalenceClass} with <code>this</code>:
	 * <pre>
	 * if (instanceCount==0) {
	 * 	attrValue = equClass.attrValue;
	 * 	this.decValue = equClass.decValue;
	 * }else {
	 * 	if (sortable()) {
	 * 		if (this.decValue!=equClass.decValue) {
	 * 			decValue = null;
	 * 			return true;
	 * 		}
	 * 	}
	 * }
	 * instanceCount+=equClass.instanceCount;
	 * return false;
	 * </pre>
	 * 
	 * @param equClass
	 * 		{@link EquivalenceClass} to be merged.
	 * @return True if <code>this.decValue</code> change into <code>null</code>(i.e. cnst=false)
	 */
	public boolean mergeClassItemsAndClassSetTypeHasChanged(EquivalenceClass equClass) {
		boolean changed = false;
		if (instanceCount==0) {
			attrValue = equClass.attrValue;
			this.decValue = equClass.decValue;
		}else {
			if (sortable()) {
				if (this.decValue!=equClass.decValue) {
					decValue = null;
					if (!changed)	changed = true;
				}
			}
		}
		instanceCount +=equClass.instanceCount;
		return changed;
	}
	
	/**
	 * Get the instance number of this equivalence class.
	 * 
	 * @return instance number.
	 */
	public int getItemSize() {
		return instanceCount;
	}
		
	@Override
	public int getInstanceSize() {
		return getItemSize();
	}
	
	public String toString() {
		return String.format("(Ux%d) %s %s d=%2d", instanceCount, getType().getAbbreviation(),
								new IntArrayKey(attrValue), getDecisionValue());
	}

	/**
	 * <strong>Create</strong> a new {@link EquivalenceClass} instance as the clone with fields
	 * being set based on the original one.
	 */
	@Override
	public EquivalenceClass clone() {
		EquivalenceClass clone = new EquivalenceClass();
		clone.setAttrValue(attrValue);
		clone.setDecValue(decValue);
		clone.setInstanceCount(instanceCount);
		return clone;
	}
}