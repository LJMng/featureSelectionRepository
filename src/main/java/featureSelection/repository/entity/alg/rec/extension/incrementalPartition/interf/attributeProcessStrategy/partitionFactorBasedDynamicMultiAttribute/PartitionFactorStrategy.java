package featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute;

import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.partitionFactorStrategy.PartitionFactorStrategy4FixedFactor;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.partitionFactorStrategy.PartitionFactorStrategy4SqrtAttrNumber;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;

/**
 * An interface for computing Partition Factor Strategy.
 * 
 * @see PartitionFactorStrategy4FixedFactor
 * @see PartitionFactorStrategy4SqrtAttrNumber
 * 
 * @author Benjamin_L
 */
public interface PartitionFactorStrategy {
	int compute(AttributeProcessStrategy processor, Object...args);
}