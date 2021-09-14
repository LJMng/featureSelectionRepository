package featureSelection.repository.support.calculation.alg;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.FeatureImportance;

import java.util.Collection;

/**
 * An interface for Feature Importance calculation for Asit.K.Das incremental Feature Selection
 * algorithm.
 * <p>
 * Implementations should base on the original paper
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S1568494618300462">
 * "A group incremental feature selection for classification using rough set theory based genetic
 * algorithm"</a> by Asit.K.Das, Shampa Sengupta, Siddhartha Bhattacharyya.
 *
 * @param <V>
 *     	Type of feature (subset) importance.
 */
@RoughSet
public interface FeatureImportance4AsitKDas<V extends Number>
	extends FeatureImportance<V>
{
	public static final String CALCULATION_NAME = "Asit.K.Das";
	
	FeatureImportance4AsitKDas<V> calculate(Collection<Instance> universes, IntegerIterator attributes, Object...args);
	
	/**
	 * Difference between <code>v1</code> and <code>v2</code>. Usually calculated by v1-v2.
	 * 
	 * @param v1
	 * 		Value 1.
	 * @param v2
	 * 		Value 2.
	 * @return difference in V.
	 */
	V difference(V v1, V v2);
}