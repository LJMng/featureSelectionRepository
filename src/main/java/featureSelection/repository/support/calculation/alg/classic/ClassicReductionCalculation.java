package featureSelection.repository.support.calculation.alg.classic;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.support.calculation.FeatureImportance;

/**
 * An interface for Feature Importance calculation using Rough Set Theory dependency(Positive
 * region based) calculation using <strong>set intersect calculations</strong>:
 * <pre>
 * C_(x) = {x∈U, [x]<sub>R</sub>⊆X}
 * Pos<sub>C</sub>(D) = ∪<sub>x∈U/D</sub> C_(x)
 * </pre>
 * 
 * @author Benjamin_L
 *
 * @param <Sig>
 * 		Type of feature importance.
 */
@RoughSet
public interface ClassicReductionCalculation<Sig extends Number> 
	extends FeatureImportance<Sig>
{}
