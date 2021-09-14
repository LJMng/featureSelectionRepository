package featureSelection.repository.algorithm.opt.artificialFishSwarm.classic;

import java.util.Collection;
import java.util.Map;

import common.utils.ArrayCollectionUtils;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionHashMapAlgorithm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.support.calculation.alg.classic.hash.ClassicHashMapCalculation;

/**
 * An implementation of {@link ClassicAttributeReductionFSA} with HashMap search strategy.
 * 
 * @see ClassicAttributeReductionFSA
 * @see ClassicHashMapCalculation
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class ClassicHashMapAttributeReductionFSA
	implements ClassicAttributeReductionFSA<ClassicHashMapCalculation<Double>>
{
	private Map<Integer, Collection<Instance>> decEquClass;
	
	public String shortName() {
		return "FSA-Classic(HashMap)";
	}

	@Override
	public Double dependency(
			ClassicHashMapCalculation<Double> calculation,
			Collection<Instance> universes, Position<?> position
	) {
		if (decEquClass==null) {
			decEquClass = ClassicAttributeReductionHashMapAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute(universes);
		}
		return calculation.calculate(universes, new IntegerArrayIterator(position.getAttributes()), decEquClass, universes.size())
							.getResult();
	}

	@Override
	public Double dependency(
			ClassicHashMapCalculation<Double> calculation,
			Collection<Instance> universes, int[] attributes
	) {
		if (decEquClass==null) {
			decEquClass = ClassicAttributeReductionHashMapAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute(universes);
		}
		return calculation.calculate(universes, new IntegerArrayIterator(attributes), decEquClass, universes.size())
							.getResult();
	}

	@Override
	public int[] inspection(
			ClassicHashMapCalculation<Double> calculation, Double sigDeviation,
			Collection<Instance> instances, int[] attributes
	) {
		Collection<Integer> reduct = 
				ClassicAttributeReductionHashMapAlgorithm
					.inspection(calculation, sigDeviation, instances, attributes);
		return ArrayCollectionUtils.getIntArrayByCollection(reduct);
	}
}