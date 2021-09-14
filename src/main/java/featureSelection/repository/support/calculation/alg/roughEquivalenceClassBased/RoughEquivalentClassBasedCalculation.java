package featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.repository.entity.alg.rec.classSet.interf.ClassSet;

import java.util.Collection;

@RoughSet
public interface RoughEquivalentClassBasedCalculation<V, CSet extends ClassSet<?>>
	extends FeatureImportance4RoughEquivalenceClassBased<V>
{
	RoughEquivalentClassBasedCalculation<V, CSet> calculate(
			Collection<CSet> classSet, int universeSize, Object...args
	);
}
