package featureSelection.repository.algorithm.opt.improvedHarmonySearch.classic;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionSequentialAlgorithm;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.classic.sequential.ClassicSequentialCalculation;

import java.util.Collection;
import java.util.List;

/**
 * An implementation of {@link ClassicAttributeReductionIHS} with Sequential search strategy.
 * 
 * @see ClassicAttributeReductionIHS
 * @see ClassicSequentialCalculation
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class ClassicSequentialAttributeReductionIHS<Sig extends Number>
	extends ClassicAttributeReductionIHS<ClassicSequentialCalculation<Sig>, Sig>
{
	private Collection<List<Instance>> decEClasses;

	@Override
	public String shortName() {
		return "IHS-Classic(Seq.)";
	}

	@Override
	public FitnessValue<Sig> fitnessValue(
			ClassicSequentialCalculation<Sig> calculation, Collection<Instance> collection, int[] attributes
	) {
		if (decEClasses==null) {
			decEClasses = ClassicAttributeReductionSequentialAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute(collection);
		}
		return newFitnessValue(
					calculation.calculate(collection, new IntegerArrayIterator(attributes), decEClasses)
								.getResult()
				);
	}

	@Override
	public Collection<Integer> inspection(
			ClassicSequentialCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> collection, int[] attributes
	) {
		return ClassicAttributeReductionSequentialAlgorithm
				.inspection(
						calculation,
						sigDeviation, 
						collection,
						attributes
				);
	}
}