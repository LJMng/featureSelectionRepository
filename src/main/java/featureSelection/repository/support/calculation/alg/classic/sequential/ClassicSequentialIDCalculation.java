package featureSelection.repository.support.calculation.alg.classic.sequential;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.support.calculation.alg.classic.ClassicReductionCalculation;

import java.util.Collection;
import java.util.List;

/**
 * An interface for Classic Feature Importance calculation using set intersect calculations. 
 * Calculations are implemented using sequential Structure {@link Collection} based on 
 * {@link Instance#getNum()}.
 * 
 * @author Benjamin_L
 *
 * @param <V>
 * 		Type of feature importance.
 */
@RoughSet
public interface ClassicSequentialIDCalculation<V extends Number> 
	extends ClassicReductionCalculation<V>
{
	/**
	 * Calculate the feature importance by <strong>Classic reduction</strong> using <code>{@link Instance}
	 * ID based Sequential Search</code> when searching is involved.
	 * 
	 * @param equClasses
	 * 		Global Equivalent Class {@link Collection} of {@link Instance}s, sorting by particular
	 * 		<code>conditional attribute values</code>: U/B
	 * @param decEClasses
	 * 		Global Equivalent Class {@link Collection} of {@link Instance}s, sorting by <code>decision
	 * 		attribute value</code>: U/D
	 * @param attributeLength
	 * 		The length of attributes involved in sorting <code>eClasses</code>.
	 * @param args
	 * 		Extra arguments.
	 * @return <code>this</code>.
	 */
	ClassicSequentialIDCalculation<V> calculate(
			Collection<List<Instance>> equClasses, Collection<List<Instance>> decEClasses,
			int attributeLength, Object...args
	);
	
	V difference(V v1, V v2);
}