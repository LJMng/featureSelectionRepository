package featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.groupNumberCalculator;

import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.PartitionFactorBasedGroupNumberCalculator;
import org.apache.commons.math3.util.FastMath;

/**
 * Group number is calculated based on the number of attributes left and a partition factor
 * (given in <code>parameter</code>):
 * <pre>
 * 	number = ceil( |left| / <code>factor</code> )
 * </pre>
 * 
 * @see #calculate(AttributeProcessStrategy, int, Object...)
 * 
 * @author Benjamin_L
 */
public class DefaultPartitionFactorBasedGroupNumberCalculator 
	implements PartitionFactorBasedGroupNumberCalculator
{
	@Override
	public int calculate(AttributeProcessStrategy processor, int partitionFactor, Object... args) {
		return (int) FastMath.ceil(processor.left() / (double) partitionFactor);
	}
}
