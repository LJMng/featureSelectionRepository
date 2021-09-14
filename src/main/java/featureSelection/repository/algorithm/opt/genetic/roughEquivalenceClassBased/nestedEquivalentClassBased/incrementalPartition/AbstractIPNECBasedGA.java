package featureSelection.repository.algorithm.opt.genetic.roughEquivalenceClassBased.nestedEquivalentClassBased.incrementalPartition;

import java.util.Arrays;
import java.util.Collection;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedAlgorithm;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.algorithm.opt.alg.rec.incrementalPartition.IPNECBasedOptimization;
import featureSelection.repository.algorithm.opt.genetic.roughEquivalenceClassBased.nestedEquivalentClassBased.NestedEquivalenceClassBasedGA;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.core.attributeCombination.CapacityCalculator;
import featureSelection.repository.entity.opt.genetic.GenerationRecord;
import featureSelection.repository.entity.opt.genetic.impl.chromosome.ChromosomeFactory;
import featureSelection.repository.entity.opt.genetic.impl.fitness.DoubleFitness;
import featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.RECChromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedIncrementalPartitionCalculation;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of {@link NestedEquivalenceClassBasedGA}, using <code>Incremental Partition</code>
 * Strategy to calculate the positive region. 
 * <p>
 * This is just an abstract class for <code>Incremental Partition</code> NEC.
 * <p>
 * The follow field should be set manually <strong>once <code>this</code> is constructed</strong> by setter:
 * <ul>
 * 	<li>inspectAttributeProcessStrategyClass</li>
 * 	<li>inspectAttributeProcessCapacityCalculator</li>
 * </ul>
 * 
 * @see DynamicGroupIPNECBasedGA
 * @see InTurnIPNECBasedGA
 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition
 * @see AttrProcessStrategy4Comb
 * @see CapacityCalculator
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public abstract class AbstractIPNECBasedGA<Chr extends RECChromosome<?>,
											Sig extends Number,
											CollectionItem>
	extends NestedEquivalenceClassBasedGA<NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig>,
											Sig, CollectionItem, Chr, FitnessValue<Double>>
	implements IPNECBasedOptimization
{
	@Setter protected Class<? extends AttrProcessStrategy4Comb> inspectAttributeProcessStrategyClass;
	@Setter protected CapacityCalculator inspectAttributeProcessCapacityCalculator;
	
	public AbstractIPNECBasedGA(Class<Chr> chromosomeClass, int insSize) {
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

	@SuppressWarnings("unchecked")
	@Override
	public DoubleFitness<Chr>[] calculateFitness(
			NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig> calculation,
			Collection<CollectionItem> nestedEquClasses, Chr...chromosomes
	) {
		FitnessValue<Double> fitnessValue;
		int[] attributes;
		DoubleFitness<Chr>[] fitness = new DoubleFitness[chromosomes.length];
		for (int i=0; i<fitness.length; i++) {
			// if chromosome is null, fitness is 0.
			if (chromosomes[i]==null) {
				fitness[i] = new DoubleFitness<>(new FitnessValue4Double(0.0), chromosomes[i]);
			}else {
				attributes = chromosomes[i].getAttributes();
				// if empty attribute, fitness is 0.
				if (attributes.length==0) {
					fitness[i] = new DoubleFitness<>(new FitnessValue4Double(0.0), chromosomes[i]);
				// else calculate fitness
				}else {
					fitnessValue = calculateFitness(calculation, nestedEquClasses, attributes);
					if (calculation.getPartitionAttributes().size()==attributes.length) {
						// chromosome remains the same and load <code>DoubleFitness</code>
						fitness[i] = new DoubleFitness<>(
										fitnessValue, 
										chromosomes[i]
									);
					}else {
						// chromosome changed load <code>DoubleFitness</code>
						if (int[].class.equals(chromosomes[i].encodedTypeClass())) {
							int[] gene = new int[chromosomes[i].encodedValuesLength()];
							Arrays.fill(gene, -1);
							int genePointer = 0;
							for (int parAttr: calculation.getPartitionAttributes())	
								gene[genePointer++] = parAttr;
								
							fitness[i] = new DoubleFitness<>(
											fitnessValue, 
											(Chr) ChromosomeFactory.getChromosome(
													gene, chromosomes[i].getClass()
											)
										);
						}else {
							byte[] gene = new byte[chromosomes[i].encodedValuesLength()];
							for (int parAttr: calculation.getPartitionAttributes())
								gene[parAttr-1] = (byte) 1;
								
							fitness[i] = new DoubleFitness<>(
											fitnessValue, 
											(Chr) ChromosomeFactory.getChromosome(
													gene, chromosomes[i].getClass()
											)
										);
						}
					}//*/
				}
			}
		}
		return fitness;
	}
	
	@Override
	public Collection<Integer> inspection(
			NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig> calculation,
			Sig sigDeviation, Collection<CollectionItem> collection, int...attributes
	) {
		return inspection(collection, new IntegerArrayIterator(attributes));
	}

	@Override
	public Collection<Integer> inspection(
			NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig> calculation,
			Sig sigDeviation, Collection<CollectionItem> collection,
			Collection<Integer> attributes
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
}