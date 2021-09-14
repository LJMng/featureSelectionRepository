package featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.partitionFactorStrategy;

import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.PartitionFactorStrategy;
import org.apache.commons.math3.util.FastMath;

import lombok.AllArgsConstructor;

/**
 * Full name: PartitionFactorStrategy4SqrtAttributeNumber (Partition Factor Strategy 4 Sqrt Attribute Number)
 * <p>
 * An entity implements {@link PartitionFactorStrategy} using the sqrt of attribute number as result
 * for partitioning factor: |attribute|<sup>1/2</sup>
 * <p>
 * The minimum partitioning factor should be no less than 1.
 * 
 * @author Benjamin_L
 */
@AllArgsConstructor
public class PartitionFactorStrategy4SqrtAttrNumber 
	implements PartitionFactorStrategy
{
	@Override
	public int compute(AttributeProcessStrategy processor, Object... args) {
		return (int) FastMath.max(1, FastMath.sqrt(processor.attributeLength()));
	}
}