package featureSelection.repository.support.calculation.alg.dependencyCalculation;


import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.support.calculation.FeatureImportance;

/**
 * An interface for Feature Importance calculation using Rough Set Theory dependency(Positive
 * region based) calculation based on <strong>Raza's dependency calculation methods</strong>.
 * <p>
 * Implementations should base on original papers by Muhammad Summair Raza, Usman Qamar:
 * <ul>
 * 	<li>
 * 	<a href="https://www.sciencedirect.com/science/article/pii/S0020025516000785">
 * 	"An incremental dependency calculation technique for feature selection using rough sets"</a>
 * 	</li>
 * 	<li>
 * 	<a href="https://www.sciencedirect.com/science/article/abs/pii/S0031320318301432">
 * 	"A heuristic based dependency calculation technique for rough set theory"</a>
 * 	</li>
 * 	<li>
 * 	<a href="https://www.sciencedirect.com/science/article/abs/pii/S0888613X17300178">
 * 	"Feature selection using rough set-based direct dependency calculation by avoiding the
 * 	positive region"</a>,
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 *
 * @param <V>
 * 		Type of feature importance.
 */
@RoughSet
public interface FeatureImportance4DependencyCalculation<V> 
	extends FeatureImportance<V>
{}
