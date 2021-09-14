package featureSelection.repository.support.calculation.alg.semisupervisedRepresentative;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.alg.SemisupervisedRepresentativeStrategy;
import featureSelection.basic.support.calculation.FeatureImportance;

import java.util.Collection;
import java.util.Map;

/**
 * An interface for Feature Importance calculation for Semi-supervised Representative Feature
 * Selection.
 * <p>
 * Implementations should base on the original article 
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S0031320316302242">
 * "An efficient semi-supervised representatives feature selection algorithm based on information
 * theory"</a> by Yintong Wang, Jiandong Wang, Hao Liao, Haiyan Chen.
 * 
 * @see SemisupervisedRepresentativeStrategy
 * 
 * @author Benjamin_L
 *
 * @param <V>
 * 		Type of feature importance.
 */
public interface FeatureImportance4SemisupervisedRepresentative<V> 
	extends FeatureImportance<V>,
			SemisupervisedRepresentativeStrategy
{
	/**
	 * Calculate Info. entropy of Equivalence Classes(<code>equClasses</code>).
	 * 
	 * @param equClasses
	 * 		A {@link Map} contains Equivalence Classes whose keys are distinct equivalent key and
	 * 		values are {@link Instance} {@link Collection}s as equivalence classes.
	 * @param args
	 * 		Extra arguments required in calculation.
	 * @return <code>this</code> instance.
	 */
	FeatureImportance4SemisupervisedRepresentative<V> calculate(
			Collection<Collection<Instance>> equClasses, Object...args
	);
}