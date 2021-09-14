package featureSelection.repository.algorithm.opt.improvedHarmonySearch.classic;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionHashMapAlgorithm;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.classic.hash.ClassicHashMapCalculation;

import java.util.Collection;
import java.util.Map;

/**
 * An implementation of {@link ClassicAttributeReductionIHS} with HashMap search strategy.
 * 
 * @see ClassicAttributeReductionIHS
 * @see ClassicHashMapCalculation
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class ClassicHashMapAttributeReductionIHS<Sig extends Number>
	extends ClassicAttributeReductionIHS<ClassicHashMapCalculation<Sig>, Sig>
{
	private Map<Integer, Collection<Instance>> decEClasses;

	@Override
	public String shortName() {
		return "IHS-Classic(HashMap)";
	}

	@Override
	public FitnessValue<Sig> fitnessValue(
			ClassicHashMapCalculation<Sig> calculation, Collection<Instance> collection,
			int[] attributes
	) {
		if (decEClasses==null) {
			decEClasses = ClassicAttributeReductionHashMapAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute(collection);
		}
		return newFitnessValue(
				calculation.calculate(
					collection, 
					new IntegerArrayIterator(attributes),
					decEClasses, 
					collection.size()
				).getResult()
			);
	}
	
	@Override
	public Collection<Integer> inspection(
			ClassicHashMapCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> collection, int[] attributes
	) {
		return ClassicAttributeReductionHashMapAlgorithm
					.inspection(
						calculation, 
						sigDeviation, 
						collection, 
						attributes
					);
	}

}