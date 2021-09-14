package featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute;

import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;

public interface PartitionFactorBasedGroupNumberCalculator {
	/**
	 * Calculate based on the given partition factor.
	 * 
	 * @param processor
	 * 		An {@link AttributeProcessStrategy} instance.
	 * @param partitionFactor
	 * 		A factor for calculation.
	 * @param args
	 * 		Extra arguments.
	 * @return calculated number of a group.
	 */
	int calculate(AttributeProcessStrategy processor, int partitionFactor, Object...args);
}
