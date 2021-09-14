package featureSelection.repository.support.calculation.alg;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.alg.positiveApproximationAccelerator.EquivalenceClass;

import java.util.Collection;

/**
 * An interface for Feature Importance calculation using Rough Set Theory dependency(Positive region based) 
 * calculation using <strong>equivalent class based calculations</strong>:
 * <pre>
 * C_(x) = {x∈U, [x]<sub>R</sub>⊆X}
 * Pos<sub>C</sub>(D) = ∪<sub>x∈U/D</sub> C_(x)
 * </pre>
 * 
 * @author Benjamin_L
 *
 * @param <V>
 * 		Type of feature importance.
 */
@RoughSet
public interface PositiveApproximationAcceleratorCalculation<V> extends FeatureImportance<V> {
	/**
	 * Calculate <strong>Shannon Conditional Entropy(SCE)</strong>.
	 * 
	 * @param equClasses
	 * 		{@link EquivalenceClass} {@link Collection}: <strong>U/P</strong>
	 * @param attributeLength
	 * 		The length of attributes used in partitioning: <strong>|P|</strong>
	 * @param args
	 * 		Extra arguments.
	 * @return Calculated Shannon Conditional Entropy value.
	 */
	public PositiveApproximationAcceleratorCalculation<V> calculate(
			Collection<EquivalenceClass> equClasses, int attributeLength, Object...args
	);
}
