package featureSelection.repository.algorithm.opt.artificialFishSwarm.roughEquivalentClassBased.incrementalPartition;

import java.util.Collection;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.algorithm.opt.alg.rec.incrementalPartition.DynamicBasedIPRECBasedOptimization;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.roughEquivalentClassBased.RoughEquivalenceClassBasedFSA;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of {@link RoughEquivalenceClassBasedFSA}, using <code>Incremental Partition</code>
 * Strategy to calculate the positive region.
 * <p>
 * The follow field should be set manually <strong>once <code>this</code> is constructed</strong> by setter:
 * <ul>
 * 	<li>inspectAttributeProcessStrategyClass</li>
 * 	<li>inspectAttributeProcessCapacityCalculator</li>
 * 	<li>incPartitionAttributeProcessStrategy</li>
 * </ul>
 * 
 * @see AbstractIPRECBasedFSA
 * @see DynamicBasedIPRECBasedOptimization
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class DynamicGroupIPRECBasedFSA
	extends AbstractIPRECBasedFSA
	implements DynamicBasedIPRECBasedOptimization
{
	@Setter protected AttributeProcessStrategy incPartitionAttributeProcessStrategy;
	
	public DynamicGroupIPRECBasedFSA(int insSize) {
		super(insSize);
	}
	
	@Override
	public String shortName() {
		return "FSA-IP-REC (Dynamic group number)";
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
		try {
			return calculation.calculate(
						incPartitionAttributeProcessStrategy.initiate(new IntegerArrayIterator(attributes)),
						equClasses, 
						getInstanceSize()
					).getResult();
		} catch (Exception e) {
			log.error("", e);
			throw new RuntimeException(e);
		}
	}
}