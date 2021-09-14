package featureSelection.repository.entity.alg.conflictDecreaseReduction;

import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.basic.model.universe.instance.Instance;

import java.util.Set;

/**
 * A significance package for {@link ConflictRegionDecreaseAlgorithm}.
 * 
 * @author Benjamin_L
 *
 * @param <U>
 * 		Type of {@link UniverseInstance}. In this case, {@link InstanceRepresentative}.
 */
@ReturnWrapper
public class SignificancePack<U extends Instance> {
	private int attribute;
	private Set<Set<U>> conflictR;

	public SignificancePack<U> setAttribute(int attribute) {
		this.attribute = attribute;
		return this;
	}
	
	public SignificancePack<U> setConflictR(Set<Set<U>> conflictR) {
		this.conflictR = conflictR;
		return this;
	}
	
	public int getAttribute() {
		return attribute;
	}
	
	public Set<Set<U>> getConflictR() {
		return conflictR;
	}
}