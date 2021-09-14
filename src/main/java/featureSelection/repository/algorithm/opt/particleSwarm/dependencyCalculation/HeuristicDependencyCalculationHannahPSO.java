package featureSelection.repository.algorithm.opt.particleSwarm.dependencyCalculation;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.dependencyCalculation.HeuristicDependencyCalculationAlgorithm;
import featureSelection.repository.algorithm.opt.particleSwarm.AbstractHannahPSO;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Integer;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.heuristicDependencyCalculation.FeatureImportance4HeuristicDependencyCalculation;

import java.util.Collection;

/**
 * Using {@link HeuristicDependencyCalculationAlgorithm} for fitness calculations and inspections in 
 * ParticleSwarm4StaticData.
 * 
 * @see AbstractHannahPSO
 * @see HeuristicDependencyCalculationAlgorithm
 * @see FeatureImportance4HeuristicDependencyCalculation
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class HeuristicDependencyCalculationHannahPSO<Cal extends FeatureImportance4HeuristicDependencyCalculation<Sig>, Sig extends Number>
	extends AbstractHannahPSO<Instance, Integer, FitnessValue<Sig>, Cal, Sig>
{
	private Collection<Integer> decisionValues;
	
	@Override
	public String shortName() {
		return "PSO-HDC";
	}

	@Override
	public Collection<Integer> inspection(
			Cal calculation, Sig sigDeviation, Collection<Instance> collection, int[] positionAttr
	) {
		return HeuristicDependencyCalculationAlgorithm
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
		return HeuristicDependencyCalculationAlgorithm
				.inspection(
					calculation,
					sigDeviation,
					collection,
					positionAttr
				);
	}

	@SuppressWarnings("unchecked")
	@Override
	public FitnessValue<Sig> fitnessValue(Cal calculation, Collection<Instance> collection, int[] attributes) {
		if (decisionValues==null) {
			decisionValues = HeuristicDependencyCalculationAlgorithm
								.Basic
								.decisionValues(collection);
		}
		Sig sig = calculation.calculate(collection, decisionValues, new IntegerArrayIterator(attributes))
							.getResult();
		return (FitnessValue<Sig>) 
				(sig instanceof Integer? 
						new FitnessValue4Integer(sig.intValue()):
						new FitnessValue4Double(sig.doubleValue())
				);
	}
}