package featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.nestedEC.NestedEquivalenceClassesInfo;

import java.util.Collection;
import java.util.Map;

@RoughSet
public interface NestedEquivalenceClassBasedRealtimeSimpleCountingCalculation<V>
	extends FeatureImportance4NestedEquivalenceClassBased<V>
{
	NestedEquivalenceClassesInfo<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>> getNecInfoWithMap();
	
	FeatureImportance4NestedEquivalenceClassBased<V> calculate(Collection<EquivalenceClass> equ, IntegerIterator attributes, Object...args);
}