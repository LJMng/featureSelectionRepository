package featureSelection.repository.algorithm.opt.improvedHarmonySearch.roughEquivalenceClassBased.incrementalPartition;

import java.util.ArrayList;
import java.util.Collection;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.algorithm.opt.alg.rec.incrementalPartition.IPRECBasedOptimization;
import featureSelection.repository.algorithm.opt.improvedHarmonySearch.roughEquivalenceClassBased.RoughEquivalenceClassBasedIHS;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.core.attributeCombination.CapacityCalculator;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of {@link RoughEquivalenceClassBasedIHS}, using <code>Incremental Partition
 * </code> Strategy to calculate the positive region.
 * <p>
 * This is just an abstract class for <code>Incremental Partition</code> REC usage.
 * <p>
 * The follow field should be set manually <strong>once <code>this</code> is constructed</strong> by setter:
 * <ul>
 * 	<li>inspectAttributeProcessStrategyClass</li>
 * 	<li>inspectAttributeProcessCapacityCalculator</li>
 * </ul>
 * 
 * @see DynamicGroupIPRECBasedIHS
 * @see InTurnIPRECBasedIHS
 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition
 * @see AttrProcessStrategy4Comb
 * @see CapacityCalculator
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public abstract class AbstractIPRECBasedIHS<Sig extends Number>
	extends RoughEquivalenceClassBasedIHS<RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Sig>,
											Sig,
											EquivalenceClass>
	implements IPRECBasedOptimization
{
	@Setter protected Class<? extends AttrProcessStrategy4Comb> inspectAttributeProcessStrategyClass;
	@Setter protected CapacityCalculator inspectAttributeProcessCapacityCalculator;
	
	public AbstractIPRECBasedIHS(int universeSize) {
		super(universeSize);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public Collection<Integer> inspection(
			RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Sig> calculation,
			Sig sigDeviation, Collection<EquivalenceClass> collection, int[] attributes
	) {
		if (attributes.length>1) {
			AttrProcessStrategyParams inspectAttributeProcessStrategyParams =
					new AttrProcessStrategyParams()
						.set(AttrProcessStrategy4Comb.PARAMETER_EXAM_CAPACITY_CALCULATOR, 
								inspectAttributeProcessCapacityCalculator);
			try {
				return RoughEquivalenceClassBasedExtensionAlgorithm
							.IncrementalPartition
							.Inspection
							.compute(
								inspectAttributeProcessStrategyClass
									.getConstructor(AttrProcessStrategyParams.class)
									.newInstance(inspectAttributeProcessStrategyParams)
									.initiate(new IntegerArrayIterator(attributes)),
								collection
						);
			} catch (Exception e) {
				log.error("", e);
				return null;
			}
		}else {
			Collection<Integer> red = new ArrayList<>(attributes.length);
			for (int attr: attributes)	red.add(attr);
			return red;
		}
	}
}