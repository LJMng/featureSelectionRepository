package featureSelection.repository.algorithm.opt.particleSwarm.roughEquivalenceClassBased.nestedEquivalenceClassBased.incrementalPartition;

import java.util.Collection;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.algorithm.opt.alg.rec.incrementalPartition.DynamicBasedIPNECBasedOptimization;
import featureSelection.repository.algorithm.opt.particleSwarm.roughEquivalenceClassBased.nestedEquivalenceClassBased.NestedEquivalenceClassBasedPSO;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.PlainNestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Integer;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedIncrementalPartitionCalculation;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of {@link NestedEquivalenceClassBasedPSO}, using <code>Incremental Partition</code>
 * Strategy to calculate the positive region.
 * <p>
 * The follow field should be set manually <strong>once <code>this</code> is constructed</strong> by setter:
 * <ul>
 * 	<li>inspectAttributeProcessStrategyClass</li>
 * 	<li>inspectAttributeProcessCapacityCalculator</li>
 * 	<li>incPartitionAttributeProcessStrategy</li>
 * </ul>
 * 
 * @see AbstractIPNECBasedPSO
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class DynamicGroupIPNECBasedPSO<CollectionItem, Sig extends Number>
	extends AbstractIPNECBasedPSO<CollectionItem, Sig>
	implements DynamicBasedIPNECBasedOptimization
{
	@Setter protected AttributeProcessStrategy incPartitionAttributeProcessStrategy;

	public DynamicGroupIPNECBasedPSO(int universeSize) {
		super(universeSize);
	}
	
	@Override
	public String shortName() {
		return "PSO-IP-NEC (Dynamic group number)";
	}

	@SuppressWarnings("unchecked")
	@Override
	public FitnessValue<Sig> fitnessValue(
			NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig> calculation,
			Collection<CollectionItem> collectionItem, int[] attributes
	) {
		Sig sig;
		if (collectionItem.iterator().next() instanceof NestedEquivalenceClass) {
			try {
				sig = calculation.incrementalCalculate(
							incPartitionAttributeProcessStrategy.initiate(new IntegerArrayIterator(attributes)),
							(Collection<PlainNestedEquivalenceClass>) collectionItem,
							getUniverseSize()
						).getResult();
			} catch (Exception e) {
				log.error("", e);
				return null;
			}
		}else {
			try {
				sig = calculation.calculate(
							incPartitionAttributeProcessStrategy.initiate(new IntegerArrayIterator(attributes)), 
							(Collection<EquivalenceClass>) collectionItem,
							getUniverseSize()
						).getResult();
			} catch (Exception e) {
				log.error("", e);
				return null;
			}
		}
		return (FitnessValue<Sig>) 
				(sig instanceof Integer? 
						new FitnessValue4Integer(sig.intValue()):
						new FitnessValue4Double(sig.doubleValue())
				);
	}
}