package featureSelection.repository.algorithm.opt.improvedHarmonySearch.classic;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionSequentialIDAlgorithm;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.classic.sequential.ClassicSequentialIDCalculation;

import java.util.Collection;
import java.util.List;

/**
 * An implementation of {@link ClassicAttributeReductionIHS} with {@link Instance} ID
 * based Sequential search strategy.
 * 
 * @see ClassicAttributeReductionIHS
 * @see ClassicSequentialIDCalculation
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class ClassicSequentialIDAttributeReductionIHS<Sig extends Number>
	extends ClassicAttributeReductionIHS<ClassicSequentialIDCalculation<Sig>, Sig>
{
	private Collection<List<Instance>> decEClasses;
	
	@Override
	public String shortName() {
		return "IHS-Classic(Seq.ID)";
	}

	@Override
	public FitnessValue<Sig> fitnessValue(
			ClassicSequentialIDCalculation<Sig> calculation, Collection<Instance> collection,
			int[] attributes
	) {
		if (decEClasses==null) {
			decEClasses = ClassicAttributeReductionSequentialIDAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute(collection);
		}
		Collection<List<Instance>> equClasses =
				ClassicAttributeReductionSequentialIDAlgorithm
					.Basic
					.equivalenceClass(
						collection, 
						new IntegerArrayIterator(attributes)
				);
		return newFitnessValue(
				calculation.calculate(equClasses, decEClasses, attributes.length, collection.size())
						.getResult()
			);
	}

	@Override
	public Collection<Integer> inspection(
			ClassicSequentialIDCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> collection, int[] attributes
	) {
		return ClassicAttributeReductionSequentialIDAlgorithm
					.inspection(
						calculation, 
						sigDeviation, 
						collection, 
						new IntegerArrayIterator(attributes)
					);
	}
}