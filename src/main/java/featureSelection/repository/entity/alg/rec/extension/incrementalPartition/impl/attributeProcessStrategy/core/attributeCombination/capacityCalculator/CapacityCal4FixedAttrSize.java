package featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.capacityCalculator;

import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.core.attributeCombination.CapacityCalculator;
import lombok.AllArgsConstructor;

/**
 * Full name: CapacityCalculator4FixedAttributeSize (Capacity Calculator 4 Fixed Attribute Size)
 * <p>
 * A capacity calculator. Return a fixed number({@link #capacity}) as the result.
 * 
 * @author Benjamin_L
 */
@AllArgsConstructor
public class CapacityCal4FixedAttrSize
	implements CapacityCalculator
{
	private int capacity;
	
	@Override
	public int compute(AttrProcessStrategy4Comb processor, Object...args) {
		return capacity;
	}
}