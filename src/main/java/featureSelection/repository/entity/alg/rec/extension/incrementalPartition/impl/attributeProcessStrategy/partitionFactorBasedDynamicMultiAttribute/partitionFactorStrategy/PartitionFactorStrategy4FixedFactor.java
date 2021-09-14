package featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.partitionFactorStrategy;

import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.PartitionFactorStrategy;
import lombok.AllArgsConstructor;

/**
 * An entity implements {@link PartitionFactorStrategy} using the given fixed number {@link #factor} as 
 * partitioning factor.
 * <p>
 * The minimum partitioning factor should be no less than 1.
 * 
 * @author Benjamin_L
 */
@AllArgsConstructor
public class PartitionFactorStrategy4FixedFactor 
	implements PartitionFactorStrategy
{
	private int factor;
	
	@Override
	public int compute(AttributeProcessStrategy processor, Object... args) {	return factor;	}
}