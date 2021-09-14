package featureSelection.repository.algorithm.opt.particleSwarm.classic;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionHashMapAlgorithm;
import featureSelection.repository.algorithm.opt.particleSwarm.AbstractHannahPSO;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Integer;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.support.calculation.alg.classic.hash.ClassicHashMapCalculation;

import java.util.Collection;
import java.util.Map;

/**
 * Implementation of {@link AbstractHannahPSO} with classic reduction algorithm, using HashMap 
 * search strategy.
 * 
 * @see AbstractHannahPSO
 * @see ClassicHashMapCalculation
 * @see ClassicAttributeReductionHashMapAlgorithm
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class ClassicHashMapAttributeReductionHannahPSO<Cal extends ClassicHashMapCalculation<Sig>, Sig extends Number>
	extends AbstractHannahPSO<Instance, Integer, FitnessValue<Sig>, Cal, Sig>
{
	private Map<Integer, Collection<Instance>> decEClasses;

	@SuppressWarnings("unchecked")
	@Override
	public FitnessValue<Sig> fitnessValue(
			Cal calculation, Collection<Instance> collection, int[] attributes
	) {
		if (decEClasses==null) {
			decEClasses = ClassicAttributeReductionHashMapAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute(collection);
		}
		Number sig =  calculation.calculate(
						collection, 
						new IntegerArrayIterator(attributes),
						decEClasses,
						collection.size()
					).getResult();
		return (FitnessValue<Sig>)
				(sig instanceof Integer? 
						new FitnessValue4Integer(sig.intValue()):
						new FitnessValue4Double(sig.doubleValue())
				);
	}
	
	@Override
	public Collection<Integer> inspection(
			Cal calculation, Sig sigDeviation, Collection<Instance> collection, int[] positionAttr
	) {
		return ClassicAttributeReductionHashMapAlgorithm
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
		ClassicAttributeReductionHashMapAlgorithm
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
		return "PSO-Classic(HashMap)";
	}

}