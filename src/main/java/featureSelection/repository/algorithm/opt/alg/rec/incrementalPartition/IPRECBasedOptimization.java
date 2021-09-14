package featureSelection.repository.algorithm.opt.alg.rec.incrementalPartition;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedAlgorithm;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.core.attributeCombination.CapacityCalculator;

/**
 * Optimization Algorithm using IP-REC for Feature Selection.
 *
 * @see RoughEquivalenceClassBasedAlgorithm
 */
@RoughSet
public interface IPRECBasedOptimization {
	/**
	 * Set <code>Attribute Process Strategy</code> for Inspection.
	 *
	 * @param v
	 * 		The class of {@link AttrProcessStrategy4Comb} to be set.
	 */
	void setInspectAttributeProcessStrategyClass(Class<? extends AttrProcessStrategy4Comb> v);

	/**
	 * Set {@link CapacityCalculator} for <code>Attribute Processing</code>.
	 *
	 * @param v
	 * 		The {@link CapacityCalculator} to be set.
	 */
	void setInspectAttributeProcessCapacityCalculator(CapacityCalculator v);
}
