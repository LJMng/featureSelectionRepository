package featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.capacityCalculator;


import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.core.attributeCombination.CapacityCalculator;
import org.apache.commons.math3.util.FastMath;

/**
 * Full name: CapacityCalculator4SqrtAttributeSize (Capacity Calculator 4 Sqrt Attribute Size)
 * <p>
 * A capacity calculator. Return the sqrt of the attribute size as result: |attribute|<sup>1/2</sup>.
 * <p>
 * Result should be no smaller than 1.
 * 
 * @author Benjamin_L
 */
public class CapacityCal4SqrtAttrSize
	implements CapacityCalculator
{
	
	@Override
	public int compute(AttrProcessStrategy4Comb processor, Object...args) {
		return FastMath.max(
					(int) FastMath.floor(FastMath.sqrt(processor.getAllLength())),
					1
				);
	}
}