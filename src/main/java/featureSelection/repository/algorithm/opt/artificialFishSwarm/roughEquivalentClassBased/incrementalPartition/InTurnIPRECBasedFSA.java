package featureSelection.repository.algorithm.opt.artificialFishSwarm.roughEquivalentClassBased.incrementalPartition;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.roughEquivalentClassBased.RoughEquivalenceClassBasedFSA;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation;

import java.util.Collection;

/**
 * An implementation of {@link RoughEquivalenceClassBasedFSA}, using <code>Incremental
 * Partition</code> Strategy to calculate the positive region.
 * <p>
 * The follow field should be set manually <strong>once <code>this</code> is constructed</strong>
 * by setter:
 * <ul>
 * 	<li>inspectAttributeProcessStrategyClass</li>
 * 	<li>inspectAttributeProcessCapacityCalculator</li>
 * </ul>
 *
 * @see AbstractIPRECBasedFSA
 *
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class InTurnIPRECBasedFSA
	extends AbstractIPRECBasedFSA
{
	public InTurnIPRECBasedFSA(int insSize) {
		super(insSize);
	}
	
	@Override
	public String shortName() {
		return "FSA-IP-REC (In-turn attribute process)";
	}

	@Override
	public Double dependency(
			RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Double> calculation,
			Collection<EquivalenceClass> equClasses, Position<?> position
	) {
		return dependency(calculation, equClasses, position.getAttributes());
	}

	@Override
	public Double dependency(
			RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Double> calculation,
			Collection<EquivalenceClass> equClasses, int[] attributes
	) {
		return calculation.calculate(equClasses, new IntegerArrayIterator(attributes), getInstanceSize())
				.getResult();
	}
}