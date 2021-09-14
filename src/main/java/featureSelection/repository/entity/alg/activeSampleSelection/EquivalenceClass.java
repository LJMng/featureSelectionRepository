package featureSelection.repository.entity.alg.activeSampleSelection;

import featureSelection.basic.model.universe.instance.Instance;
import lombok.Data;

import java.util.Collection;
import java.util.LinkedList;

/**
 * An entity for Equivalent Class for <strong>Active Sample Selection</strong> attribute reduction.
 * 
 * @author Benjamin_L
 */
@Data
public class EquivalenceClass implements Cloneable {
	private Collection<Instance> universes;
	private Integer decision;
	
	public EquivalenceClass() {
		universes = new LinkedList<>();
	}
	private EquivalenceClass(Collection<Instance> universes, Integer decision) {
		this.universes = universes;
		this.decision = decision;
	}
	
	public void addUniverse(Instance universe) {
		universes.add(universe);
	}
	
	/**
	 * Get the attribute value of the 1st {@link Instance} of the {@link #universes}.
	 * 
	 * @see Instance#getAttributeValue(int)
	 * 
	 * @param index
	 * 		The index of the value. (starts from 1, 0 as decision attribute)
	 * @return the int value./-1 if the index is illegal.
	 */
	public int getAttributeValue(int index) {
		return universes.iterator().next().getAttributeValue(index);
	}

	@Override
	public EquivalenceClass clone() {
		return new EquivalenceClass(new LinkedList<>(universes), decision);
	}
}
