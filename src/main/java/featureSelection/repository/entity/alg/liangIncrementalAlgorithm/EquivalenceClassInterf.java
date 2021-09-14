package featureSelection.repository.entity.alg.liangIncrementalAlgorithm;


import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.liangIncrementalAlgorithm.classSetType.ClassSetType;

import java.util.Collection;

public interface EquivalenceClassInterf {
	/**
	 * Get the {@link ClassSetType} in this equivalence class.
	 *
	 * @return {@link ClassSetType}.
	 */
	ClassSetType getClassSetType();

	/**
	 * Get all instances of the given type in this equivalence class.
	 *
	 * @see ClassSetType#MIXED
	 * @see ClassSetType#PREVIOUS
	 * @see ClassSetType#NEW
	 */
	Collection<Instance> getInstances(ClassSetType classSetType);
}