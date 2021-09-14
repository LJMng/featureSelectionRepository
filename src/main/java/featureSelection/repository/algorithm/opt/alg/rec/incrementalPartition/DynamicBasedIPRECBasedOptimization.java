package featureSelection.repository.algorithm.opt.alg.rec.incrementalPartition;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;

@RoughSet
public interface DynamicBasedIPRECBasedOptimization
	extends IPRECBasedOptimization
{
	/**
	 * Set the {@link AttributeProcessStrategy} for incremental partition.
	 * 
	 * @see AttributeProcessStrategy
	 *
	 * @param incPartitionAttributeProcessStrategy {@link AttributeProcessStrategy}.
	 */
	void setIncPartitionAttributeProcessStrategy(AttributeProcessStrategy incPartitionAttributeProcessStrategy);
}
