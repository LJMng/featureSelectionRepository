package featureSelection.repository.support.calculation.alg.dependencyCalculation.incrementalDependencyCalculation;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.FeatureImportance4DependencyCalculation;

import java.util.Collection;

/**
 * An interface for Feature Importance calculation using Rough Set Theory dependency(Positive
 * region based) alculation based on <strong>Raza's incremental dependency calculation methods
 * </strong>.
 * <p>
 * Implementations should base on the original article 
 * <a href="https://www.sciencedirect.com/science/article/pii/S0020025516000785">
 * "An incremental dependency calculation technique for feature selection using rough sets"</a>
 * by Muhammad Summair Raza, Usman Qamar.
 * 
 * @see FeatureImportance4DependencyCalculation
 * 
 * @author Benjamin_L
 *
 * @param <V>
 * 		Type of feature importance.
 */
@RoughSet
public interface FeatureImportance4IncrementalDependencyCalculation<V> 
	extends FeatureImportance4DependencyCalculation<V>
{
	FeatureImportance4IncrementalDependencyCalculation<V> calculate(
			Collection<Instance> instances, IntegerIterator attribute, Object...args
	);

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