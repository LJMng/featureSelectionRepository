package featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.core.attributeCombination;

import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;

/**
 * Calculator for the capacity of attribute combinations calculations(i.e. 
 * {@link AttrProcessStrategy4Comb})
 * <p>
 * Specifically, this calculator is for Feature Selection <strong>Core</strong> calculation/
 * <strong>Redundancy inspection</strong>. In the calculating of Core/Redundancy inspections, 
 * attributes are separated into groups for further calculations:  
 * <ul>
 * 	<li><strong>a group of examined attributes</strong>
 * 		<p>Attributes to be examined if they are dispensable to preserve the positive region in
 * 			partitions. If positive region remains the same without using them and the examined
 * 			attribute(i.e. attributes in line) in partitions, they are dispensable.
 * 	</li>
 * 	<li><strong>an examined attribute</strong>
 * 		<p>The attribute to be examined if it is dispensable to preserve the positive region in
 * 			partitions or not. If positive region remains the same without using it in partitions,
 * 			it is dispensable.
 * 	</li>
 * 	<li><strong>a group of attributes in line</strong>
 * 		<p>A group of attributes to be examined first if the rest of the attributes(i.e. examing attributes
 * 			and the examined attribute) are dispensable to preserve the positive region in partitions.
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
public interface CapacityCalculator {
	/**
	 * Calculate the capacity for the given {@link AttrProcessStrategy4Comb}.
	 * 
	 * @param processor
	 * 		An {@link AttrProcessStrategy4Comb} instance.
	 * @param args
	 * 		Extra arguments if needed in the implementation.
	 * @return capacity in {@link int}.
	 */
	int compute(AttrProcessStrategy4Comb processor, Object...args);
}
