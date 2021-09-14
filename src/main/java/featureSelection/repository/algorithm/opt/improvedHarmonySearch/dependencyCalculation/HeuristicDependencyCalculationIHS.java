package featureSelection.repository.algorithm.opt.improvedHarmonySearch.dependencyCalculation;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.dependencyCalculation.HeuristicDependencyCalculationAlgorithm;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.heuristicDependencyCalculation.FeatureImportance4HeuristicDependencyCalculation;

import java.util.Collection;

/**
 * Using {@link HeuristicDependencyCalculationAlgorithm} for fitness calculations and inspections
 * in Improved Harmony Search.
 *
 * @see DependencyCalculationIHS
 * @see HeuristicDependencyCalculationAlgorithm
 * @see FeatureImportance4HeuristicDependencyCalculation
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class HeuristicDependencyCalculationIHS<Cal extends FeatureImportance4HeuristicDependencyCalculation<Sig>, Sig extends Number>
	extends DependencyCalculationIHS<Cal, Sig>
{
	private Collection<Integer> decisionValues;
	
	@Override
	public String shortName() {
		return "IHS-HDC";
	}

	@Override
	public FitnessValue<Sig> fitnessValue(
			Cal calculation, Collection<Instance> collection, int[] attributes
	) {
		if (decisionValues==null) {
			decisionValues = HeuristicDependencyCalculationAlgorithm
								.Basic
								.decisionValues(collection);
		}
		return newFitnessValue(
				calculation.calculate(
					collection,
					decisionValues, 
					new IntegerArrayIterator(attributes)
				).getResult()
			);
	}

	@Override
	public Collection<Integer> inspection(
			Cal calculation, Sig sigDeviation, Collection<Instance> collection,
			int[] attributes
	) {
		return HeuristicDependencyCalculationAlgorithm
				.inspection(
					calculation, 
					sigDeviation, 
					collection,
					attributes
				);
	}
}