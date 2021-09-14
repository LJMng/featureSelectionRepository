package featureSelection.repository.algorithm.opt.improvedHarmonySearch.dependencyCalculation;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.dependencyCalculation.DirectDependencyCalculationAlgorithm;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.directDependencyCalculation.FeatureImportance4DirectDependencyCalculation;

import java.util.Collection;

/**
 * Using {@link DirectDependencyCalculationAlgorithm} for fitness calculations and inspections in 
 * Improved Harmony Search.
 *
 * @see DependencyCalculationIHS
 * @see DirectDependencyCalculationAlgorithm
 * @see FeatureImportance4DirectDependencyCalculation
 *
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class DirectDependencyCalculationIHS<Cal extends FeatureImportance4DirectDependencyCalculation<Sig>, Sig extends Number>
	extends DependencyCalculationIHS<Cal, Sig>
{
	@Override
	public String shortName() {
		return "IHS-DDC";
	}

	@Override
	public FitnessValue<Sig> fitnessValue(
			Cal calculation, Collection<Instance> collection, int[] attributes
	) {
		return newFitnessValue(
				calculation.calculate(collection, new IntegerArrayIterator(attributes))
							.getResult()
			);
	}

	@Override
	public Collection<Integer> inspection(
			Cal calculation, Sig sigDeviation, Collection<Instance> collection,
			int[] attributes
	) {
		return DirectDependencyCalculationAlgorithm
				.inspection(
						calculation, 
						sigDeviation,
						collection,
						attributes
				);
	}
}