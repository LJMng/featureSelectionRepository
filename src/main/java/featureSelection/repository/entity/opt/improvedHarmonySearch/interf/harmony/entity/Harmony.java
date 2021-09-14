package featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity;

import featureSelection.basic.model.optimization.AttributeEncoding;

public interface Harmony<T> extends AttributeEncoding<T>, Cloneable {
	/**
	 * Add an index into Harmony record.
	 * 
	 * @param attribute
	 * 		The attribute index. (Starts from 0)
	 * @return True if added successfully.
	 */
	boolean addAttribute(int attribute);
	/**
	 * Remove an index into Harmony record.
	 * 
	 * @param attribute
	 * 		The attribute index. (Starts from 0)
	 * @return True if attribute is not in Harmony now.
	 */
	boolean removeAttribute(int attribute);
	
	boolean containsAttribute(int attribute);
	
	Harmony<T> clone();
}