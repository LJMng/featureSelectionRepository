package featureSelection.repository.algorithm.opt.improvedHarmonySearch.roughEquivalenceClassBased.incrementalPartition;

import java.util.Collection;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.algorithm.opt.alg.rec.incrementalPartition.DynamicBasedIPRECBasedOptimization;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of RoughEquivalentClassBasedIHS, using <code>Incremental Partition</code>
 * Strategy to calculate the positive region.
 * <p>
 * The follow field should be set manually <strong>once <code>this</code> is constructed</strong>
 * by setter:
 * <ul>
 * 	<li>inspectAttributeProcessStrategyClass</li>
 * 	<li>inspectAttributeProcessCapacityCalculator</li>
 * 	<li>incPartitionAttributeProcessStrategy</li>
 * </ul>
 * 
 * @see AbstractIPRECBasedIHS
 * @see DynamicBasedIPRECBasedOptimization
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class DynamicGroupIPRECBasedIHS<Sig extends Number>
	extends AbstractIPRECBasedIHS<Sig>
	implements DynamicBasedIPRECBasedOptimization
{
	@Setter protected AttributeProcessStrategy incPartitionAttributeProcessStrategy;
	
	public DynamicGroupIPRECBasedIHS(int universeSize) {
		super(universeSize);
	}
	
	@Override
	public String shortName() {
		return "IHS-IP-REC (Dynamic group number)";
	}

	@Override
	public FitnessValue<Sig> fitnessValue(
			RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Sig> calculation,
			Collection<EquivalenceClass> equClasses, int[] attributes
	) {
		try {
			return newFitnessValue(
					calculation.calculate(
						incPartitionAttributeProcessStrategy.initiate(
							new IntegerArrayIterator(attributes)
						),
						equClasses, 
						getInsSize()
					).getResult()
				);
		} catch (Exception e) {
			log.error("", e);
			throw new RuntimeException(e);
		}
	}
}