package featureSelection.repository.algorithm.opt.genetic.roughEquivalenceClassBased.incrementalPartition;

import java.util.Collection;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.algorithm.opt.alg.rec.incrementalPartition.IPRECBasedOptimization;
import featureSelection.repository.algorithm.opt.genetic.roughEquivalenceClassBased.RoughEquivalenceClassBasedGA;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.core.attributeCombination.CapacityCalculator;
import featureSelection.repository.entity.opt.genetic.GenerationRecord;
import featureSelection.repository.entity.opt.genetic.impl.fitness.DoubleFitness;
import featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.RECChromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of {@link RoughEquivalenceClassBasedGA}, using <code>Incremental Partition</code>
 * Strategy to calculate the positive region. 
 * <p>
 * This is just an abstract class for <code>Incremental Partition</code> REC usage.
 * <p>
 * The follow field should be set manually <strong>once <code>this</code> is constructed</strong> by setter:
 * <ul>
 * 	<li>inspectAttributeProcessStrategyClass</li>
 * 	<li>inspectAttributeProcessCapacityCalculator</li>
 * </ul>
 * 
 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition
 * @see DynamicGroupIPRECBasedGA
 * @see InTurnIPRECBasedGA
 * @see AttrProcessStrategy4Comb
 * @see CapacityCalculator
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public abstract class AbstractIPRECBasedGA<Chr extends RECChromosome<?>,
											Sig extends Number>
	extends RoughEquivalenceClassBasedGA<RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Sig>,
											Sig, EquivalenceClass, Chr, FitnessValue<Double>>
	implements IPRECBasedOptimization
{
	@Setter protected Class<? extends AttrProcessStrategy4Comb> inspectAttributeProcessStrategyClass;
	@Setter protected CapacityCalculator inspectAttributeProcessCapacityCalculator;
	
	public AbstractIPRECBasedGA(Class<Chr> chromosomeClass, int insSize) {
		super(chromosomeClass, insSize);
	}

	@Override
	public int compareBestFitness(
			Fitness<Chr, FitnessValue<Double>> fitness,
			GenerationRecord<Chr, FitnessValue<Double>> geneRecord
	) {
		Double v1 = fitness==null || fitness.getFitnessValue()==null?
						0.0: fitness.getFitnessValue().getValue();
		Double v2 = geneRecord==null || geneRecord.getBestFitness()==null?
						0.0: geneRecord.getBestFitness().getValue().doubleValue();
		return Double.compare(v1, v2);
	}

	@Override
	public DoubleFitness<Chr>[] calculateFitness(
			RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Sig> calculation,
			Collection<EquivalenceClass> collection, @SuppressWarnings("unchecked") Chr...chromosomes
	) {
		int[] attributes;
		@SuppressWarnings("unchecked")
		DoubleFitness<Chr>[] fitness = new DoubleFitness[chromosomes.length];
		for (int i=0; i<fitness.length; i++) {
			if (chromosomes[i]==null) {
				fitness[i] = new DoubleFitness<>(new FitnessValue4Double(0.0), chromosomes[i]);
			}else {
				attributes = chromosomes[i].getAttributes();
				fitness[i] = new DoubleFitness<>(
								calculateFitness(calculation, collection, attributes), 
								chromosomes[i]
							);
			}
		}
		return fitness;
	}
	
	@Override
	public Collection<Integer> inspection(
			RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Sig> calculation,
			Sig sigDeviation, Collection<EquivalenceClass> collection, int...attributes
	) {
		return inspection(collection, new IntegerArrayIterator(attributes));
	}

	@Override
	public Collection<Integer> inspection(
			RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Sig> calculation,
			Sig sigDeviation, Collection<EquivalenceClass> collection,
			Collection<Integer> attributes
	) {
		return inspection(collection, new IntegerCollectionIterator(attributes));
	}
	
	@SuppressWarnings("deprecation")
	private Collection<Integer> inspection(
			Collection<EquivalenceClass> collection, IntegerIterator attributes
	){
		AttrProcessStrategyParams inspectAttributeProcessStrategyParams =
				new AttrProcessStrategyParams()
					.set(AttrProcessStrategy4Comb.PARAMETER_EXAM_CAPACITY_CALCULATOR, 
							inspectAttributeProcessCapacityCalculator
					);
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