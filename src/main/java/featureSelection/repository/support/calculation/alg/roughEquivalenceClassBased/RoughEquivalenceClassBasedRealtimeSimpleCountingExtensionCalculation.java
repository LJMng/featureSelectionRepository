package featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;

import java.util.Collection;

@RoughSet
public interface RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<V>
	extends FeatureImportance4RoughEquivalenceClassBased<V>
{
	RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<V> calculate(
			Collection<EquivalenceClass> equClasses, IntegerIterator attributes, Object...args
	);
}