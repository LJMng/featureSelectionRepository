package featureSelection.repository.algorithm.opt.improvedHarmonySearch.roughEquivalenceClassBased;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation;

import java.util.Collection;

/**
 * An implementation of {@link RoughEquivalenceClassBasedIHS}, using <code>Simple Positive Reduct</code>
 * Strategy to calculate the positive region.
 * 
 * @see RoughEquivalenceClassBasedIHS
 * @see RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation}
 * @see RoughEquivalenceClassBasedExtensionAlgorithm.SimpleCounting.RealTimeCounting
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class RealtimeSimpleCountingRECBasedIHS<Sig extends Number>
	extends RoughEquivalenceClassBasedIHS<RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig>,
											Sig,
											EquivalenceClass>
{
	public RealtimeSimpleCountingRECBasedIHS(int insSize) {
		super(insSize);
	}
	
	@Override
	public String shortName() {
		return "IHS-(R)S(C)-REC";
	}

	@Override
	public FitnessValue<Sig> fitnessValue(
			RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation,
			Collection<EquivalenceClass> collection, int[] attributes
	) {
		return newFitnessValue(
				calculation.calculate(collection, new IntegerArrayIterator(attributes), getInsSize())
							.getResult()
			);
	}

	@Override
	public Collection<Integer> inspection(
			RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation,
			Sig sigDeviation, Collection<EquivalenceClass> equClasses, int[] attributes
	) {
		return RoughEquivalenceClassBasedExtensionAlgorithm
				.SimpleCounting
				.RealTimeCounting
				.inspection(
					getInsSize(),
					calculation,
					sigDeviation,
					equClasses, 
					attributes
				);
	}
}