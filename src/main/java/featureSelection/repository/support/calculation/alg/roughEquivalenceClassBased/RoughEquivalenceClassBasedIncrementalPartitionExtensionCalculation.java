package featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;

import java.util.Collection;

@RoughSet
public interface RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<V>
	extends FeatureImportance4RoughEquivalenceClassBased<V>
{
	RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<V> calculate(
			Collection<EquivalenceClass> equClasses, IntegerIterator attributes,
			Object...args);
	RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<V> calculate(
			AttributeProcessStrategy attributeProcessStrategy,
			Collection<EquivalenceClass> equClasses,
			Object...args) throws Exception;
}