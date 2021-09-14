package featureSelection.repository.algorithm.opt.artificialFishSwarm.dependencyCalculation;

import common.utils.ArrayCollectionUtils;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.dependencyCalculation.HeuristicDependencyCalculationAlgorithm;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.func.ArtificialFishSwarm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.heuristicDependencyCalculation.FeatureImportance4HeuristicDependencyCalculation;

import java.util.Collection;

/**
 * Using {@link HeuristicDependencyCalculationAlgorithm} for fitness calculations and inspections in 
 * {@link ArtificialFishSwarm}.
 * 
 * @see FeatureImportance4HeuristicDependencyCalculation
 * @see HeuristicDependencyCalculationAlgorithm
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class HeuristicDependencyCalculationFSA<Cal extends FeatureImportance4HeuristicDependencyCalculation<Double>>
	implements DependencyCalculationFSA<Cal>
{
	private Collection<Integer> decisionValues;
	
	@Override
	public String shortName() {
		return "FSA-HDC";
	}

	@Override
	public Double dependency(Cal calculation, Collection<Instance> universes, Position<?> position) {
		return dependency(calculation, universes, position.getAttributes());
	}

	@Override
	public Double dependency(Cal calculation, Collection<Instance> universes, int[] attributes) {
		if (decisionValues==null) {
			decisionValues = HeuristicDependencyCalculationAlgorithm
								.Basic
								.decisionValues(universes);
		}
		return calculation.calculate(universes, decisionValues, new IntegerArrayIterator(attributes), universes.size())
						.getResult()
						.doubleValue();
	}

	@Override
	public int[] inspection(Cal calculation, Double sigDeviation, Collection<Instance> universes, int[] attributes) {
		return ArrayCollectionUtils.getIntArrayByCollection(
					HeuristicDependencyCalculationAlgorithm
					.inspection(
						calculation,
						sigDeviation,
						universes,
						attributes
					)
				);
	}
}