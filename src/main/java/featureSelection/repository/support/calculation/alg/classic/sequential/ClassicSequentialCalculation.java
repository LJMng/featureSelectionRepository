package featureSelection.repository.support.calculation.alg.classic.sequential;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.support.calculation.alg.classic.ClassicReductionCalculation;

import java.util.Collection;
import java.util.List;

/**
 * An interface for Classic Feature Importance calculation using set intersect calculations. 
 * Calculations are implemented using sequential Structure {@link Collection}.
 * 
 * @author Benjamin_L
 *
 * @param <V>
 * 		Type of feature importance.
 */
public interface ClassicSequentialCalculation<V extends Number> 
	extends ClassicReductionCalculation<V>
{
	ClassicSequentialCalculation<V> calculate(
			Collection<Instance> instances, IntegerIterator attributes,
			Collection<List<Instance>> decEClasses,
			Object...args);
	
	V difference(V v1, V v2);
}