package featureSelection.repository.support.calculation.alg.classic.hash;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.support.calculation.alg.classic.ClassicReductionCalculation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * An interface for Classic Feature Importance calculation using set intersect calculations. 
 * Calculations are implemented using {@link HashMap}.
 * 
 * @author Benjamin_L
 *
 * @param <V>
 * 		Type of feature importance.
 */
@RoughSet
public interface ClassicHashMapCalculation<V extends Number> 
	extends ClassicReductionCalculation<V>
{
	/**
	 * Calculate attribute significance using hash technique when searching.
	 * 
	 * @param instances
	 * 		An {@link Instance} {@link Collection}.
	 * @param attributes
	 * 		Attributes of {@link Instance}.
	 * @param decEClasses
	 * 		Equivalence Classes induced by Decision attribute.
	 * @param args
	 * 		Extra arguments.
	 * @return <code>this</code>.
	 */
	ClassicHashMapCalculation<V> calculate(
			Collection<Instance> instances, IntegerIterator attributes,
			Map<Integer, Collection<Instance>> decEClasses,
			Object...args);
}