package featureSelection.repository.algorithm.opt.artificialFishSwarm.roughEquivalentClassBased.incrementalPartition;

import java.util.Collection;

import common.utils.ArrayCollectionUtils;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.algorithm.opt.alg.rec.incrementalPartition.IPRECBasedOptimization;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.roughEquivalentClassBased.RoughEquivalenceClassBasedFSA;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.core.attributeCombination.CapacityCalculator;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of {@link RoughEquivalenceClassBasedFSA}, using <code>Incremental
 * Partition</code> Strategy to calculate the positive region.
 * <p>
 * This is just an abstract class for <code>Incremental Partition</code> REC usage.
 * <p>
 * The follow field should be set manually <strong>once <code>this</code> is constructed</strong>
 * by setter:
 * <ul>
 * 	<li>inspectAttributeProcessStrategyClass</li>
 * 	<li>inspectAttributeProcessCapacityCalculator</li>
 * </ul>
 *
 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition
 * @see AttrProcessStrategy4Comb
 * @see CapacityCalculator
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public abstract class AbstractIPRECBasedFSA
	extends RoughEquivalenceClassBasedFSA<RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Double>,
											Double,
											EquivalenceClass>
	implements IPRECBasedOptimization
{
	@Setter protected Class<? extends AttrProcessStrategy4Comb> inspectAttributeProcessStrategyClass;
	@Setter protected CapacityCalculator inspectAttributeProcessCapacityCalculator;
	
	public AbstractIPRECBasedFSA(int insSize) {
		super(insSize);
	}
	
	@Override
	public int[] inspection(
			RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Double> calculation,
			Double sigDeviation, Collection<EquivalenceClass> collection, int[] attributes
	) {
		return ArrayCollectionUtils.getIntArrayByCollection(
					inspection(collection, new IntegerArrayIterator(attributes))
				);
	}
	
	@SuppressWarnings("deprecation")
	private Collection<Integer> inspection(
			Collection<EquivalenceClass> collection, IntegerIterator attributes
	){
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
								.initiate(attributes),
							collection
					);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}