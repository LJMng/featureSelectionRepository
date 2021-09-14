package featureSelection.repository.algorithm.opt.particleSwarm.classic;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionSequentialAlgorithm;
import featureSelection.repository.algorithm.opt.particleSwarm.AbstractHannahPSO;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Integer;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.support.calculation.alg.classic.sequential.ClassicSequentialCalculation;

import java.util.Collection;
import java.util.List;

/**
 * Implementation of {@link AbstractHannahPSO} with classic reduction algorithm, using Sequential Search 
 * strategy.
 *
 * @see AbstractHannahPSO
 * @see ClassicSequentialCalculation
 * @see ClassicAttributeReductionSequentialAlgorithm
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class ClassicSequentialAttributeReductionHannahPSO<Cal extends ClassicSequentialCalculation<Sig>, Sig extends Number>
	extends AbstractHannahPSO<Instance, Integer, FitnessValue<Sig>, Cal, Sig>
{
	private Collection<List<Instance>> decEClasses;

	@SuppressWarnings("unchecked")
	@Override
	public FitnessValue<Sig> fitnessValue(
			Cal calculation, Collection<Instance> instances, int[] attributes
	) {
		if (decEClasses==null) {
			decEClasses = ClassicAttributeReductionSequentialAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute(instances);
		}
		
		Number sig = calculation.calculate(
						instances,
						new IntegerArrayIterator(attributes),
						decEClasses,
						instances.size()
					).getResult();
		return (FitnessValue<Sig>) 
				(sig instanceof Integer? 
						new FitnessValue4Integer(sig.intValue()):
						new FitnessValue4Double(sig.doubleValue())
				);
	}
	
	@Override
	public Collection<Integer> inspection(
			Cal calculation, Sig sigDeviation, Collection<Instance> collection,
			int[] positionAttr
	) {
		if (decEClasses==null) {
			decEClasses = ClassicAttributeReductionSequentialAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute(collection);
		}
		return ClassicAttributeReductionSequentialAlgorithm
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
		if (decEClasses==null) {
			decEClasses = ClassicAttributeReductionSequentialAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute(collection);
		}
		ClassicAttributeReductionSequentialAlgorithm
			.inspection(
				calculation, 
				sigDeviation, 
				collection, 
				positionAttr
			);
		return positionAttr;
	}

	@Override
	public String shortName() {
		return "PSO-Classic(Seq.)";
	}
}