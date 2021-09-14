package featureSelection.repository.algorithm.opt.improvedHarmonySearch.roughEquivalenceClassBased.incrementalPartition;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation;

import java.util.Collection;

/**
 * An implementation of RoughEquivalentClassBasedIHS, using <code>Incremental Partition</code>
 * Strategy to calculate the positive region.
 * <p>
 * The follow field should be set manually <strong>once <code>this</code> is constructed</strong>
 * by setter:
 * <ul>
 * 	<li>inspectAttributeProcessStrategyClass</li>
 * 	<li>inspectAttributeProcessCapacityCalculator</li>
 * </ul>
 * 
 * @see AbstractIPRECBasedIHS
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class InTurnIPRECBasedIHS<Sig extends Number>
	extends AbstractIPRECBasedIHS<Sig>
{
	public InTurnIPRECBasedIHS(int universeSize) {
		super(universeSize);
	}
	
	@Override
	public String shortName() {
		return "IHS-IP-REC (In-turn attribute process)";
	}

	@Override
	public FitnessValue<Sig> fitnessValue(
			RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Sig> calculation,
			Collection<EquivalenceClass> equClasses, int[] attributes
	) {
		return newFitnessValue(
				calculation.calculate(
					equClasses, 
					new IntegerArrayIterator(attributes),
					getInsSize()
				).getResult()
			);
	}
}