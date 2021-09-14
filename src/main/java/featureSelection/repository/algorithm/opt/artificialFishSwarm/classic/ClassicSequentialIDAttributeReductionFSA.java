package featureSelection.repository.algorithm.opt.artificialFishSwarm.classic;

import common.utils.ArrayCollectionUtils;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionSequentialIDAlgorithm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.support.calculation.alg.classic.sequential.ClassicSequentialIDCalculation;

import java.util.Collection;
import java.util.List;

/**
 * An implementation of {@link ClassicAttributeReductionFSA} with {@link Instance} ID based
 * Sequential search strategy.
 * 
 * @see ClassicAttributeReductionFSA
 * @see ClassicSequentialIDCalculation
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class ClassicSequentialIDAttributeReductionFSA
	implements ClassicAttributeReductionFSA<ClassicSequentialIDCalculation<Double>>
{
	private Collection<List<Instance>> decEClasses;
	
	@Override
	public String shortName() {
		return "FSA-Classic(Seq.ID)";
	}
	
	@Override
	public Double dependency(
			ClassicSequentialIDCalculation<Double> calculation, Collection<Instance> universes,
			Position<?> position
	) {
		return dependency(calculation, universes, position.getAttributes());
	}

	@Override
	public Double dependency(
			ClassicSequentialIDCalculation<Double> calculation, Collection<Instance> universes,
			int[] attributes
	) {
		if (decEClasses==null) {
			decEClasses = ClassicAttributeReductionSequentialIDAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute(universes);
		}
		Collection<List<Instance>> eClasses =
				ClassicAttributeReductionSequentialIDAlgorithm
					.Basic
					.equivalenceClass(universes, new IntegerArrayIterator(attributes));
		return calculation.calculate(eClasses, decEClasses, attributes.length, universes.size())
						.getResult();
	}

	@Override
	public int[] inspection(
			ClassicSequentialIDCalculation<Double> calculation, Double sigDeviation,
			Collection<Instance> collectionItems, int[] attributes
	) {
		return ArrayCollectionUtils.getIntArrayByCollection(
					ClassicAttributeReductionSequentialIDAlgorithm
						.inspection(calculation, sigDeviation, collectionItems, new IntegerArrayIterator(attributes))
				);
	}
}