package featureSelection.repository.algorithm.opt.artificialFishSwarm.dependencyCalculation;

import common.utils.ArrayCollectionUtils;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.dependencyCalculation.DirectDependencyCalculationAlgorithm;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.func.ArtificialFishSwarm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.directDependencyCalculation.FeatureImportance4DirectDependencyCalculation;

import java.util.Collection;

/**
 * Using {@link DirectDependencyCalculationAlgorithm} for fitness calculations and inspections in 
 * {@link ArtificialFishSwarm}.
 * 
 * @see FeatureImportance4DirectDependencyCalculation
 * @see DirectDependencyCalculationAlgorithm
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class DirectDependencyCalculationFSA<Cal extends FeatureImportance4DirectDependencyCalculation<Double>>
	implements DependencyCalculationFSA<Cal>
{
	@Override
	public String shortName() {
		return "FSA-DDC";
	}

	@Override
	public Double dependency(Cal calculation, Collection<Instance> universes, Position<?> position) {
		return dependency(calculation, universes, position.getAttributes());
	}

	@Override
	public Double dependency(Cal calculation, Collection<Instance> universes, int[] attributes) {
		return calculation.calculate(universes, new IntegerArrayIterator(attributes), universes.size())
						.getResult()
						.doubleValue();
	}

	@Override
	public int[] inspection(Cal calculation, Double sigDeviation, Collection<Instance> universes, int[] attributes) {
		return ArrayCollectionUtils.getIntArrayByCollection(
				DirectDependencyCalculationAlgorithm
					.inspection(
						calculation,
						sigDeviation,
						universes,
						attributes
					)
				);
	}
}