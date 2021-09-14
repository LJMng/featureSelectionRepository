package featureSelection.repository.support.calculation.alg;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.FeatureImportance;

import java.util.Collection;
import java.util.Map;

public interface FeatureImportance4ToleranceClassPositiveRegionIncremental
	extends FeatureImportance<Integer>
{
	Collection<Instance> getPositiveRegionInstances();
	
	/**
	 * Calculate positive region based on the given {@link ToleranceClass}es.
	 * <p>
	 * For {@link ToleranceClass} please check {@link ToleranceClassPositiveRegionAlgorithm#toleranceClass(
	 * Collection, IntegerIterator)}.
	 * 
	 * @see ToleranceClassPositiveRegionAlgorithm#toleranceClass(Collection, IntegerIterator)
	 * 
	 * @param toleranceClasses
	 * 		A {@link Map} {@link Entry} {@link Collection} of {@link Instance} {@link Collection}.
	 * @return <code>this</code>.
	 */
	FeatureImportance4ToleranceClassPositiveRegionIncremental calculate(
			Collection<Map.Entry<Instance, Collection<Instance>>> toleranceClasses
	);
}
