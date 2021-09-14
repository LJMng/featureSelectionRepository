package featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadUnsafe;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.incrementalPartition.RoughEquivalenceClassDummy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.PartitionResult;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation;
import featureSelection.repository.support.calculation.positiveRegion.DefaultPositiveRegionCalculation;

import java.util.Collection;


/**
 * Positive Region Calculation for Incremental Partition REC.
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadUnsafe
public class PositiveRegionCalculation4IPREC
	extends DefaultPositiveRegionCalculation
	implements RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Integer>
{
	private Integer positive;
	@Override
	public Integer getResult() {
		return positive;
	}
	
	@Override
	public RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Integer> calculate(
			Collection<EquivalenceClass> equClasses, IntegerIterator attributes,
			Object...args
	) {
		// Calculate
		positive = attributes.size()==0? 0: computeAttributesInTurn(equClasses, attributes);
		return this;
	}
	
	@Override
	public RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Integer> calculate(
			AttributeProcessStrategy attributeProcessStrategy, Collection<EquivalenceClass> equClasses,
			Object... args
	) {
		// Calculate
		positive = computeAttributesInGroupWithDynamicNumber(attributeProcessStrategy, equClasses);
		return this;
	}
		
	@SuppressWarnings("deprecation")
	private int computeAttributesInTurn(
			Collection<EquivalenceClass> equClasses, IntegerIterator attributes
	) {
		PartitionResult<Collection<Integer>, Collection<RoughEquivalenceClassDummy>> partitionResult;
		partitionResult = 
				RoughEquivalenceClassBasedExtensionAlgorithm
					.IncrementalPartition
					.Basic
					.inTurnIncrementalPartition(equClasses, attributes);
		countCalculate(partitionResult.getAttributes().size());
		return partitionResult.getPositive();
	}
	
	/**
	 * Compute and calculate the positive region using dynamic group number in
	 * <code>IP-REC</code>.
	 * 
	 * @param attributeProcessStrategy
	 * 		{@link AttributeProcessStrategy} instance.
	 * @param equClasses
	 * 		{@link EquivalenceClass} {@link Collection}.
	 * @return int value as positive region.
	 */
	@SuppressWarnings("deprecation")
	private int computeAttributesInGroupWithDynamicNumber(
			AttributeProcessStrategy attributeProcessStrategy, Collection<EquivalenceClass> equClasses
	) {
		PartitionResult<Collection<Integer>, Collection<RoughEquivalenceClassDummy>> partitionResult;
		partitionResult = 
				RoughEquivalenceClassBasedExtensionAlgorithm
					.IncrementalPartition
					.Basic
					.dynamicIncrementalPartition(attributeProcessStrategy, equClasses);
		countCalculate(partitionResult.getAttributes().size());
		return partitionResult.getPositive();
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}
}