package featureSelection.repository.algorithm.opt.particleSwarm.roughEquivalenceClassBased.nestedEquivalenceClassBased.incrementalPartition;

import java.util.Collection;
import java.util.HashSet;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedAlgorithm;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.algorithm.opt.alg.rec.incrementalPartition.IPNECBasedOptimization;
import featureSelection.repository.algorithm.opt.particleSwarm.roughEquivalenceClassBased.nestedEquivalenceClassBased.NestedEquivalenceClassBasedPSO;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.core.attributeCombination.CapacityCalculator;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.HannahFitness;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedIncrementalPartitionCalculation;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of {@link NestedEquivalenceClassBasedPSO}, using <code>Incremental Partition</code>
 * Strategy to calculate the positive region. 
 * <p>
 * This is just an abstract class for <code>Incremental Partition</code> NEC.
 * <p>
 * The follow field should be set manually <strong>once <code>this</code> is constructed</strong> by setter:
 * <ul>
 * <li>inspectAttributeProcessStrategyClass</li>
 * <li>inspectAttributeProcessCapacityCalculator</li>
 * </ul>
 * 
 * @see DynamicGroupIPNECBasedPSO
 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition
 * @see AttrProcessStrategy4Comb
 * @see CapacityCalculator
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public abstract class AbstractIPNECBasedPSO<CollectionItem, Sig extends Number>
	extends NestedEquivalenceClassBasedPSO<CollectionItem,
											Integer,
											NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig>,
											Sig>
	implements IPNECBasedOptimization
{
	@Setter protected Class<? extends AttrProcessStrategy4Comb> inspectAttributeProcessStrategyClass;
	@Setter protected CapacityCalculator inspectAttributeProcessCapacityCalculator;
	
	public AbstractIPNECBasedPSO(int universeSize) {
		super(universeSize);
	}

	@Override
	public Collection<Integer> inspection(
			NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig> calculation,
			Sig sigDeviation, Collection<CollectionItem> collection, int[] attributes
	) {
		return inspection(collection, new IntegerArrayIterator(attributes));
	}

	@Override
	public Collection<Integer> inspection(
			NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig> calculation,
			Sig sigDeviation, Collection<CollectionItem> collection, Collection<Integer> attributes
	) {
		return inspection(collection, new IntegerCollectionIterator(attributes));
	}
	
	@SuppressWarnings("unchecked")
	private Collection<Integer> inspection(
			Collection<CollectionItem> collection, IntegerIterator attributes
	){
		AttrProcessStrategyParams inspectAttributeProcessStrategyParams =
				new AttrProcessStrategyParams()
					.set(AttrProcessStrategy4Comb.PARAMETER_EXAM_CAPACITY_CALCULATOR, 
							inspectAttributeProcessCapacityCalculator
					);
		
		if (collection.iterator().next() instanceof NestedEquivalenceClass) {
			try {
				return NestedEquivalenceClassBasedAlgorithm
						.IncrementalPartition
						.Inspection
						.computeNestedEquivalenceClasses(
							inspectAttributeProcessStrategyClass
								.getConstructor(AttrProcessStrategyParams.class)
								.newInstance(inspectAttributeProcessStrategyParams)
								.initiate(attributes),
							(Collection<NestedEquivalenceClass<EquivalenceClass>>) collection
						);
			} catch (Exception e) {
				log.error("", e);
				return null;
			}
		}else {
			try {
				return NestedEquivalenceClassBasedAlgorithm
						.IncrementalPartition
						.Inspection
						.computeEquivalenceClasses(
							inspectAttributeProcessStrategyClass
								.getConstructor(AttrProcessStrategyParams.class)
								.newInstance(inspectAttributeProcessStrategyParams)
								.initiate(attributes),
							(Collection<EquivalenceClass>) collection
						);
			} catch (Exception e) {
				log.error("", e);
				return null;
			}
		}
	}

	
	@Override
	public HannahFitness<FitnessValue<Sig>> fitness(
			NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig> calculation,
			Collection<CollectionItem> collection, int[] attributesSrc, int[] attributeIndexes
	) {	
		int[] attrValues;
		if (attributesSrc.length==attributeIndexes.length) {
			attrValues = attributesSrc;
		}else {
			attrValues = new int[attributeIndexes.length];
			for (int i=0; i<attrValues.length; i++)	attrValues[i] = attributesSrc[attributeIndexes[i]];
		}
		FitnessValue<Sig> fitnessValue = fitnessValue(calculation, collection, attrValues);
		if (attributeIndexes.length > calculation.getPartitionAttributes().size()) {
			Collection<Integer> partitionAttrs = new HashSet<>(calculation.getPartitionAttributes());
			
			byte[] coding = new byte[attributesSrc.length];
			for (int i=0, j=0; i<attributesSrc.length && j<partitionAttrs.size(); i++) {
				if (partitionAttrs.contains(attributesSrc[i])) {
					coding[i] = (byte) 1;
					j++;
				}
			}
			return new HannahFitness<>(fitnessValue, toPosition(coding));
		}else {
			return new HannahFitness<>(
					fitnessValue,
					toPosition(attributeIndexes, attributesSrc.length)
				);
		}
		
	}
}