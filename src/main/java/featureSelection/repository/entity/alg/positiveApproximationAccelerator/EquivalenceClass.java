package featureSelection.repository.entity.alg.positiveApproximationAccelerator;

import java.util.Collection;
import java.util.HashSet;

import featureSelection.basic.model.universe.instance.Instance;
import lombok.Data;

/**
 * An entity for equivalent class.
 * 
 * @author Benjamin_L
 */
@Data
public class EquivalenceClass {
	private Collection<Instance> instances;
	private int decisionValue;
	
	public EquivalenceClass() {
		instances = new HashSet<>();
	}
	
	public void addUniverse(Instance u) {
		instances.add(u);
	}
}
