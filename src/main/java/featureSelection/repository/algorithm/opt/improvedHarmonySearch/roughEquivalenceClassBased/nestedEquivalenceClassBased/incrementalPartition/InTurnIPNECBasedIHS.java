package featureSelection.repository.algorithm.opt.improvedHarmonySearch.roughEquivalenceClassBased.nestedEquivalenceClassBased.incrementalPartition;

import java.util.Collection;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.PlainNestedEquivalenceClass;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedIncrementalPartitionCalculation;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of NestedEquivalentClassBasedIHS, using <code>Incremental Partition</code> 
 * Strategy to calculate the positive region.
 * <p>
 * The follow field should be set manually <strong>once <code>this</code> is constructed</strong> by setter:
 * <ul>
 * 	<li>inspectAttributeProcessStrategyClass</li>
 * 	<li>inspectAttributeProcessCapacityCalculator</li>
 * </ul>
 * 
 * @see AbstractIPNECBasedIHS
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class InTurnIPNECBasedIHS<Sig extends Number, CollectionItem>
	extends AbstractIPNECBasedIHS<Sig, CollectionItem>
{
	public InTurnIPNECBasedIHS(int universeSize) {
		super(universeSize);
	}
	
	@Override
	public String shortName() {
		return "IHS-IP-NEC (In-turn attribute process)";
	}

	@SuppressWarnings("unchecked")
	@Override
	public FitnessValue<Sig> fitnessValue(
			NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig> calculation,
			Collection<CollectionItem> collection, int[] attributes
	) {
		if (collection.iterator().next() instanceof NestedEquivalenceClass) {
			try {
				return newFitnessValue(
						(Sig) calculation.incrementalCalculate(
								new IntegerArrayIterator(attributes),
								(Collection<PlainNestedEquivalenceClass>) collection,
								getInsSize()
							).getResult()
					);
			} catch (Exception e) {
				log.error("", e);
				return null;
			}
		}else {
			try {
				return newFitnessValue(
						(Sig) calculation.calculate(
								new IntegerArrayIterator(attributes), 
								(Collection<EquivalenceClass>) collection,
								getInsSize()
							).getResult()
					);
			} catch (Exception e) {
				log.error("", e);
				return null;
			}
		}
	}
}