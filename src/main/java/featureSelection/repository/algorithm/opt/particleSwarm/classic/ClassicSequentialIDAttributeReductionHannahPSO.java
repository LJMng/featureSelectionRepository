package featureSelection.repository.algorithm.opt.particleSwarm.classic;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionSequentialIDAlgorithm;
import featureSelection.repository.algorithm.opt.particleSwarm.AbstractHannahPSO;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Integer;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.support.calculation.alg.classic.sequential.ClassicSequentialIDCalculation;

import java.util.Collection;
import java.util.List;

/**
 * Implementation of {@link AbstractHannahPSO} with classic reduction algorithm, using
 * {@link Instance#getNum()}(ID) based Sequential search strategy.
 * 
 * @see AbstractHannahPSO
 * @see ClassicSequentialIDCalculation
 * @see ClassicAttributeReductionSequentialIDAlgorithm
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class ClassicSequentialIDAttributeReductionHannahPSO<Cal extends ClassicSequentialIDCalculation<Sig>, Sig extends Number>
	extends AbstractHannahPSO<Instance, Integer, FitnessValue<Sig>, Cal, Sig>
{
	private Collection<List<Instance>> decEClasses;

	@SuppressWarnings("unchecked")
	@Override
	public FitnessValue<Sig> fitnessValue(Cal calculation, Collection<Instance> collection, int[] attributes) {
		if (decEClasses==null) {
			decEClasses = ClassicAttributeReductionSequentialIDAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute(collection);
		}
		Collection<List<Instance>> eClasses =
				ClassicAttributeReductionSequentialIDAlgorithm
					.Basic
					.equivalenceClass(collection, new IntegerArrayIterator(attributes));
		Number sig = calculation.calculate(eClasses, decEClasses, attributes.length, collection.size())
							.getResult();
		
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
		return ClassicAttributeReductionSequentialIDAlgorithm
				.inspection(
						calculation,
						sigDeviation, 
						collection, 
						new IntegerArrayIterator(positionAttr)
				);
	}

	@Override
	public Collection<Integer> inspection(
			Cal calculation, Sig sigDeviation, Collection<Instance> collection,
			Collection<Integer> positionAttr
	) {
		return ClassicAttributeReductionSequentialIDAlgorithm
					.inspection(
							calculation,
							sigDeviation,
							collection, 
							new IntegerCollectionIterator(positionAttr)
					);
	}

	@Override
	public String shortName() {
		return "PSO-Classic(Seq.ID)";
	}
}