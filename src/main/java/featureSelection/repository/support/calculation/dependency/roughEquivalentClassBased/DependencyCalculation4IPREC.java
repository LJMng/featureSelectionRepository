package featureSelection.repository.support.calculation.dependency.roughEquivalentClassBased;

import java.util.Collection;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.incrementalPartition.RoughEquivalenceClassDummy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.PartitionResult;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation;
import featureSelection.repository.support.calculation.dependency.DefaultDependencyCalculation;
import lombok.Getter;

/**
 * Dependency Calculation for Incremental Partition REC.
 * 
 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition
 * 
 * @author Benjamin_L
 */
public class DependencyCalculation4IPREC
	extends DefaultDependencyCalculation
	implements RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Double>
{
	private Double positive;
	@Override
	public Double getResult() {
		return positive;
	}
	
	@Getter private int numberOfFeaturesSkipInCalculation = 0;

	@Override
	public RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Double> calculate(
			Collection<EquivalenceClass> equClasses, IntegerIterator attributes,
			Object...args
	) {
		int attrLen = attributes.size();
		// Count the current calculation
		countCalculate(attrLen);
		// Calculate
		if (attrLen==0) {
			positive = 0.0;
		}else {
			int universeSize = (int) args[0];
			positive = computeAttributesInTurn(equClasses, attributes) / (double) universeSize;
		}
		return this;
	}
	
	@Override
	public RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Double>
		calculate(
			AttributeProcessStrategy attributeProcessStrategy,
			Collection<EquivalenceClass> equClasses, Object... args
	) throws Exception {
		int attrLen = attributeProcessStrategy.attributeLength();
		// Count the current calculation
		countCalculate(attrLen);
		// Calculate
		if (attrLen==0) {
			positive = 0.0;
		}else {
			int universeSize = (int) args[0];
			positive = computeAttributesInGroupWithDynamicNumber(attributeProcessStrategy, equClasses) 
							/ (double) universeSize;
		}
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
		numberOfFeaturesSkipInCalculation += (partitionResult.getAttributes().size() - attributes.size());
		return partitionResult.getPositive();
	}
	
	@SuppressWarnings("deprecation")
	private int computeAttributesInGroupWithDynamicNumber(
			AttributeProcessStrategy attributeProcessStrategy, 
			Collection<EquivalenceClass> equClasses
	) {
		PartitionResult<Collection<Integer>, Collection<RoughEquivalenceClassDummy>> partitionResult;
		partitionResult = 
				RoughEquivalenceClassBasedExtensionAlgorithm
					.IncrementalPartition
					.Basic
					.dynamicIncrementalPartition(attributeProcessStrategy, equClasses);
		numberOfFeaturesSkipInCalculation += (partitionResult.getAttributes().size() - attributeProcessStrategy.attributeLength());
		return partitionResult.getPositive();
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}
}