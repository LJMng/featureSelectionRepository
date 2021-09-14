package featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased;

import featureSelection.basic.annotation.theory.RoughSet;

import java.util.Collection;

@RoughSet
public interface FeatureImportance4NestedEquivalenceClassBased<V>
	extends FeatureImportance4RoughEquivalenceClassBased<V>
{

	/**
	 * Get the attributes used in the last partition execution.
	 * 
	 * @return An {@link Integer} {@link Collection} with attributes.(Starts from 1)
	 */
	Collection<Integer> getPartitionAttributes();
}