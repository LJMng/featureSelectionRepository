package featureSelection.repository.algorithm.opt.particleSwarm.roughEquivalenceClassBased;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.algorithm.opt.genetic.roughEquivalenceClassBased.RoughEquivalenceClassBasedGA;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Integer;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation;

import java.util.Collection;

/**
 * An implementation of {@link RoughEquivalenceClassBasedGA}, using <code>Simple Positive
 * Reduct</code> Strategy to calculate the positive region.
 * 
 * @see RoughEquivalenceClassBasedPSO
 * @see RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation
 * @see RoughEquivalenceClassBasedExtensionAlgorithm
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class RealtimeSimpleCountingRECBasedPSO<Sig extends Number>
	extends RoughEquivalenceClassBasedPSO<EquivalenceClass,
											Integer,
											RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig>,
											Sig>
{
	public RealtimeSimpleCountingRECBasedPSO(int universeSize) {
		super(universeSize);
	}

	@Override
	public String shortName() {
		return "PSO-(R)S(C)-REC";
	}

	@Override
	public Collection<Integer> inspection(
			RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation,
			Sig sigDeviation, Collection<EquivalenceClass> collection, int[] positionAttr
	) {
		return RoughEquivalenceClassBasedExtensionAlgorithm
					.SimpleCounting
					.RealTimeCounting
					.inspection(
						getUniverseSize(),
						calculation,
						sigDeviation,
						collection, 
						positionAttr
				);
	}

	@Override
	public Collection<Integer> inspection(
			RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation,
			Sig sigDeviation, Collection<EquivalenceClass> collection, Collection<Integer> positionAttr
	) {
		RoughEquivalenceClassBasedExtensionAlgorithm
			.SimpleCounting
			.RealTimeCounting
			.inspection(
				getUniverseSize(),
				calculation,
				sigDeviation,
				collection, 
				positionAttr
			);
		return positionAttr;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FitnessValue<Sig> fitnessValue(
			RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation,
			Collection<EquivalenceClass> collection, int[] attributes
	) {
		Sig sig = calculation.calculate(collection, new IntegerArrayIterator(attributes), getUniverseSize())
							.getResult();
		return (FitnessValue<Sig>) 
				(sig instanceof Integer? 
						new FitnessValue4Integer(sig.intValue()):
						new FitnessValue4Double(sig.doubleValue())
				);
	}
}