package featureSelection.repository.algorithm.opt.artificialFishSwarm.roughEquivalentClassBased;

import common.utils.ArrayCollectionUtils;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation;

import java.util.Collection;

/**
 * An implementation of {@link RoughEquivalenceClassBasedFSA}, using <code>Simple Positive
 * Reduct</code> Strategy to calculate the positive region.
 * 
 * @see RoughEquivalenceClassBasedFSA
 * @see RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class RealtimeSimpleCountingRoughEquivalenceClassBasedFSA
	extends RoughEquivalenceClassBasedFSA<RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Double>,
											Double,
											EquivalenceClass>
{
	public RealtimeSimpleCountingRoughEquivalenceClassBasedFSA(int instanceSize) {
		super(instanceSize);
	}
	
	@Override
	public String shortName() {
		return "FSA-(R)S(C)-REC";
	}

	@Override
	public Double dependency(
			RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Double> calculation,
			Collection<EquivalenceClass> collectionItems, Position<?> position
	) {
		return dependency(calculation, collectionItems, position.getAttributes());
	}

	@Override
	public Double dependency(
			RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Double> calculation,
			Collection<EquivalenceClass> collectionItems, int[] attributes
	) {
		return calculation.calculate(collectionItems, new IntegerArrayIterator(attributes), getInstanceSize())
							.getResult();
	}

	@Override
	public int[] inspection(
			RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Double> calculation,
			Double sigDeviation, Collection<EquivalenceClass> equClasses, int[] attributes
	) {
		return ArrayCollectionUtils.getIntArrayByCollection(
					RoughEquivalenceClassBasedExtensionAlgorithm
						.SimpleCounting
						.RealTimeCounting
						.inspection(
							getInstanceSize(),
							calculation,
							sigDeviation,
							equClasses, 
							attributes
						)
				);
	}
}