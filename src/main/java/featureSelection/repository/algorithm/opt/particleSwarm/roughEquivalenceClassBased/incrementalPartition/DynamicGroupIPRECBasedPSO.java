package featureSelection.repository.algorithm.opt.particleSwarm.roughEquivalenceClassBased.incrementalPartition;

import java.util.Collection;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.algorithm.opt.alg.rec.incrementalPartition.DynamicBasedIPRECBasedOptimization;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.roughEquivalentClassBased.incrementalPartition.AbstractIPRECBasedFSA;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue.FitnessValue4Integer;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of {@link AbstractIncrementalPartitionRoughEquivalenceClassBasedPSO},
 * using <code>Incremental Partition</code> Strategy to calculate the positive region.
 * <p>
 * The follow field should be set manually <strong>once <code>this</code> is constructed</strong> by setter:
 * <ul>
 * 	<li>inspectAttributeProcessStrategyClass</li>
 * 	<li>inspectAttributeProcessCapacityCalculator</li>
 * 	<li>incPartitionAttributeProcessStrategy</li>
 * </ul>
 * 
 * @see AbstractIPRECBasedFSA
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class DynamicGroupIPRECBasedPSO<Sig extends Number>
	extends AbstractIPRECBasedPSO<Sig>
	implements DynamicBasedIPRECBasedOptimization
{
	@Setter protected AttributeProcessStrategy incPartitionAttributeProcessStrategy;

	public DynamicGroupIPRECBasedPSO(int universeSize) {
		super(universeSize);
	}
	
	@Override
	public String shortName() {
		return "PSO-IP-REC (Dynamic group number)";
	}

	@SuppressWarnings("unchecked")
	@Override
	public FitnessValue<Sig> fitnessValue(
			RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Sig> calculation,
			Collection<EquivalenceClass> equClasses, int[] attributes
	) {
		Sig sig;
		try {
			sig = calculation.calculate(
						incPartitionAttributeProcessStrategy.initiate(new IntegerArrayIterator(attributes)),
						equClasses,
						getUniverseSize()
					).getResult();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
		return (FitnessValue<Sig>) 
				(sig instanceof Integer? 
						new FitnessValue4Integer(sig.intValue()):
						new FitnessValue4Double(sig.doubleValue())
				);
	}
}