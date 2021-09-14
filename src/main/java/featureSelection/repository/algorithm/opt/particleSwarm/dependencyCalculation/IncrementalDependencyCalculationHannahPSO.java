package featureSelection.repository.algorithm.opt.particleSwarm.dependencyCalculation;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.dependencyCalculation.IncrementalDependencyCalculationAlgorithm;
import featureSelection.repository.algorithm.opt.particleSwarm.AbstractHannahPSO;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Integer;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.incrementalDependencyCalculation.FeatureImportance4IncrementalDependencyCalculation;

import java.util.Collection;

/**
 * Using {@link IncrementalDependencyCalculationAlgorithm} for fitness calculations and inspections in 
 * ParticleSwarm4StaticData.
 * 
 * @see AbstractHannahPSO
 * @see IncrementalDependencyCalculationAlgorithm
 * @see FeatureImportance4IncrementalDependencyCalculation
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class IncrementalDependencyCalculationHannahPSO<Cal extends FeatureImportance4IncrementalDependencyCalculation<Sig>, Sig extends Number>
	extends AbstractHannahPSO<Instance, Integer, FitnessValue<Sig>, Cal, Sig>
{
	@Override
	public String shortName() {
		return "PSO-IDC";
	}

	@Override
	public Collection<Integer> inspection(
			Cal calculation, Sig sigDeviation, Collection<Instance> collection,
			int[] positionAttr
	) {
		return IncrementalDependencyCalculationAlgorithm
				.inspection(
						calculation, 
						sigDeviation, 
						collection, 
						positionAttr
				);
	}

	@Override
	public Collection<Integer> inspection(
			Cal calculation, Sig sigDeviation, Collection<Instance> collection,
			Collection<Integer> positionAttr
	) {
		IncrementalDependencyCalculationAlgorithm
			.inspection(
					calculation,
					sigDeviation,
					collection,
					positionAttr
			);
		return positionAttr;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FitnessValue<Sig> fitnessValue(
			Cal calculation, Collection<Instance> collection, int[] attributes
	) {
		Sig sig = calculation.calculate(collection, new IntegerArrayIterator(attributes))
							.getResult();
		return (FitnessValue<Sig>) 
				(sig instanceof Integer? 
						new FitnessValue4Integer(sig.intValue()):
						new FitnessValue4Double(sig.doubleValue())
				);
	}
}