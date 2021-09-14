package featureSelection.repository.support.calculation.alg;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.FeatureImportance;

import java.util.Collection;

@RoughSet
public interface FeatureImportance4LiuRoughSet<V extends Number>
	extends FeatureImportance<V>
{
	public FeatureImportance4LiuRoughSet<V> calculate(
			Collection<Instance> universes, IntegerIterator attributes, Object...args);

	public FeatureImportance4LiuRoughSet<V> calculate4Incremental(
			Collection<Collection<Instance>> roughClasses, IntegerIterator attributes, Object...args);

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