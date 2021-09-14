package featureSelection.repository.support.calculation.alg;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.alg.liuQuickHash.EquivalenceClass;
import featureSelection.repository.entity.alg.liuQuickHash.RoughEquivalenceClass;

import java.util.Collection;

@RoughSet
public interface FeatureImportance4LiuQuickHash<V extends Number>
	extends FeatureImportance<V>
{
	public FeatureImportance4LiuQuickHash<V> calculate(
			Collection<EquivalenceClass> universes, IntegerIterator attributes, Object...args);

	public FeatureImportance4LiuQuickHash<V> calculate4Incremental(
			Collection<RoughEquivalenceClass> roughClasses, IntegerIterator attributes, Object...args);

	/**
	 * Difference between <code>v1</code> and <code>v2</code>. Usually calculated by v1-v2.
	 * 
	 * @param v1
	 * 		Value 1.
	 * @param v2
	 * 		Value 2.
	 * @return difference in V.
	 */
	public V difference(V v1, V v2);
}