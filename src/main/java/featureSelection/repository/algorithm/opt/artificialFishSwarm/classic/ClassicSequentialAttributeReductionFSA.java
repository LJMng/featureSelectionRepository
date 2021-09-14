package featureSelection.repository.algorithm.opt.artificialFishSwarm.classic;

import common.utils.ArrayCollectionUtils;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionSequentialAlgorithm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.support.calculation.alg.classic.sequential.ClassicSequentialCalculation;

import java.util.Collection;
import java.util.List;

/**
 * An implementation of {@link ClassicSequentialIDAttributeReductionFSA} with Sequential search
 * strategy.
 * 
 * @see ClassicAttributeReductionFSA
 * @see ClassicSequentialCalculation
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class ClassicSequentialAttributeReductionFSA
	implements ClassicAttributeReductionFSA<ClassicSequentialCalculation<Double>>
{
	private Collection<List<Instance>> decEClasses;
	
	@Override
	public String shortName() {
		return "FSA-Classic(Seq)";
	}

	@Override
	public Double dependency(
			ClassicSequentialCalculation<Double> calculation, Collection<Instance> universes,
			Position<?> position
	) {
		if (decEClasses==null) {
			decEClasses = ClassicAttributeReductionSequentialAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute(universes);
							
		}
		return calculation.calculate(universes, new IntegerArrayIterator(position.getAttributes()), decEClasses, universes.size())
						.getResult();
	}

	@Override
	public Double dependency(
			ClassicSequentialCalculation<Double> calculation, Collection<Instance> universes,
			int[] attributes
	) {
		if (decEClasses==null) {
			decEClasses = ClassicAttributeReductionSequentialAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute(universes);
							
		}
		return calculation.calculate(universes, new IntegerArrayIterator(attributes), decEClasses, universes.size())
						.getResult();
	}

	@Override
	public int[] inspection(
			ClassicSequentialCalculation<Double> calculation, Double sigDeviation,
			Collection<Instance> collectionItems, int[] attributes
	) {
		return ArrayCollectionUtils.getIntArrayByCollection(
					ClassicAttributeReductionSequentialAlgorithm
						.inspection(calculation, sigDeviation, collectionItems, attributes)
				);
	}
}