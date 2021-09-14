package featureSelection.repository.algorithm.opt.artificialFishSwarm.dependencyCalculation;

import common.utils.ArrayCollectionUtils;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.dependencyCalculation.IncrementalDependencyCalculationAlgorithm;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.func.ArtificialFishSwarm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.incrementalDependencyCalculation.FeatureImportance4IncrementalDependencyCalculation;

import java.util.Collection;

/**
 * Using {@link IncrementalDependencyCalculationAlgorithm} for fitness calculations and inspections in 
 * {@link ArtificialFishSwarm}.
 * 
 * @see FeatureImportance4IncrementalDependencyCalculation
 * @see IncrementalDependencyCalculationAlgorithm
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class IncrementalDependencyCalculationFSA<Cal extends FeatureImportance4IncrementalDependencyCalculation<Double>>
	implements DependencyCalculationFSA<Cal>
{
	@Override
	public String shortName() {
		return "FSA-IDC";
	}

	@Override
	public Double dependency(Cal calculation, Collection<Instance> instances, Position<?> position) {
		return dependency(calculation, instances, position.getAttributes());
	}

	@Override
	public Double dependency(Cal calculation, Collection<Instance> instances, int[] attributes) {
		return calculation.calculate(instances, new IntegerArrayIterator(attributes), instances.size())
						.getResult()
						.doubleValue();
	}

	@Override
	public int[] inspection(Cal calculation, Double sigDeviation, Collection<Instance> instances, int[] attributes) {
		return ArrayCollectionUtils.getIntArrayByCollection(
					IncrementalDependencyCalculationAlgorithm
						.inspection(
								calculation,
								sigDeviation,
								instances,
								attributes
						)
				);
	}
}