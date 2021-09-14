package featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.entity.alg.rec.nestedEC.NestedEquivalenceClassesInfo;

import java.util.Collection;

@RoughSet
public interface NestedEquivalenceClassBasedIncrementalPartitionCalculation<V>
	extends FeatureImportance4NestedEquivalenceClassBased<V>
{
	NestedEquivalenceClassesInfo<Collection<NestedEquivalenceClass<EquivalenceClass>>> getNecInfoWithCollection();
	
	NestedEquivalenceClassBasedIncrementalPartitionCalculation<V> calculate(
			IntegerIterator attributes, Collection<EquivalenceClass> equClasses,
			Object...args) throws Exception;
	NestedEquivalenceClassBasedIncrementalPartitionCalculation<V> incrementalCalculate(
			IntegerIterator attributes, Collection<? extends NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses,
			Object...args) throws Exception;
	
	NestedEquivalenceClassBasedIncrementalPartitionCalculation<V> calculate(
			AttributeProcessStrategy attributeProcessStrategy,
			Collection<EquivalenceClass> equClasses,
			Object...args) throws Exception;
	NestedEquivalenceClassBasedIncrementalPartitionCalculation<V> incrementalCalculate(
			AttributeProcessStrategy attributeProcessStrategy,
			Collection<? extends NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses,
			Object...args) throws Exception;
}