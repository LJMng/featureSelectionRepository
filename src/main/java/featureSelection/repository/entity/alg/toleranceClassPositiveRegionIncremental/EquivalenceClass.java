package featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental;

import java.util.Collection;
import java.util.LinkedList;

import featureSelection.basic.model.universe.instance.IncompleteInstance;
import featureSelection.basic.model.universe.instance.Instance;
import lombok.Getter;

/**
 * An entity for equivalent class for TCPR. 
 * <p>
 * {@link Instance}s are saved in {@link #items} as an equivalent class with the same
 * {@link #attributeValue}.
 * 
 * @author Benjamin_L
 */
@Getter
public class EquivalenceClass {
	public final static int MISSING_VALUE = IncompleteInstance.MISSING_VALUE;

	/**
	 * The attribute value that {@link Instance}s in this equivalence class share.
	 */
	private int attributeValue;
	/**
	 * {@link Instance}s.
	 */
	private Collection<Instance> items;

	/**
	 * Constructor for {@link EquivalenceClass}.
	 * 
	 * @see #attributeValue
	 * 
	 * @param attributeValue
	 * 		The attribute value that {@link Instance}s in this equivalent class share.
	 */
	public EquivalenceClass(int attributeValue) {
		items = new LinkedList<>();
		this.attributeValue = attributeValue;
	}
	
	/**
	 * Add an {@link Instance} into current equivalent class.
	 * 
	 * @param ins
	 * 		An {@link Instance} to be added.
	 */
	public void add(Instance ins) {
		items.add(ins);
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EquivalenceClass[");
		builder.append("attributeValue="+attributeValue);
		builder.append(", |items|="+items.size());
		builder.append("]");
		
		return builder.toString();
	}
}