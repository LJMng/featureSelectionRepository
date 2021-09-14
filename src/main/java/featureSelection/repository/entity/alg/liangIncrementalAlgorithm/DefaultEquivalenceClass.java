package featureSelection.repository.entity.alg.liangIncrementalAlgorithm;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.liangIncrementalAlgorithm.classSetType.ClassSetType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

/**
 * Default implementation of an Equivalence class.
 *
 * @see EquivalenceClassInterf
 */
@Data
@AllArgsConstructor
public class DefaultEquivalenceClass implements EquivalenceClassInterf {
	private ClassSetType classSetType;
	private Collection<Instance> universeInstances;
	
	@Override
	public Collection<Instance> getInstances(ClassSetType classSetType) {
		return this.classSetType.equals(classSetType)? universeInstances: null;
	}
}
