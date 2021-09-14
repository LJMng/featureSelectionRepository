package featureSelection.repository.algorithm.opt.genetic.roughEquivalenceClassBased.nestedEquivalentClassBased.incrementalPartition;

import java.util.Collection;
import java.util.Random;

import common.utils.RandomUtils;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.algorithm.opt.alg.rec.incrementalPartition.DynamicBasedIPNECBasedOptimization;
import featureSelection.repository.algorithm.opt.genetic.roughEquivalenceClassBased.nestedEquivalentClassBased.NestedEquivalenceClassBasedGA;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.PlainNestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.RECChromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedIncrementalPartitionCalculation;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of {@link NestedEquivalenceClassBasedGA}, using <code>Incremental
 * Partition</code> Strategy to calculate the positive region.
 * <p>
 * The follow field should be set manually <strong>once <code>this</code> is
 * constructed</strong> by setter:
 * <ul>
 * 	<li>inspectAttributeProcessStrategyClass</li>
 * 	<li>inspectAttributeProcessCapacityCalculator</li>
 * 	<li>incPartitionAttributeProcessStrategy</li>
 * </ul>
 * 
 * @see AbstractIPNECBasedGA
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class DynamicGroupIPNECBasedGA<Chr extends RECChromosome<?>, Sig extends Number, CollectionItem>
	extends AbstractIPNECBasedGA<Chr, Sig, CollectionItem>
	implements DynamicBasedIPNECBasedOptimization
{
	private static boolean shuffleAttributes = true;
	
	@Setter protected AttributeProcessStrategy incPartitionAttributeProcessStrategy;
	
	public DynamicGroupIPNECBasedGA(Class<Chr> chromosomeClass, int insSize) {
		super(chromosomeClass, insSize);
	}
	
	@Override
	public String shortName() {
		return "GA-IP-NEC (Dynamic group number)";
	}

	@SuppressWarnings("unchecked")
	@Override
	public FitnessValue<Double> calculateFitness(
			NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig> calculation,
			Collection<CollectionItem> collectionItem, int...attributes
	) {
		if (shuffleAttributes) {
			// shuffle attributes
			RandomUtils.shuffleArray(attributes, new Random());
		}
		
		if (collectionItem.iterator().next() instanceof NestedEquivalenceClass) {
			try {
				return new FitnessValue4Double(
						calculation.incrementalCalculate(
							incPartitionAttributeProcessStrategy.initiate(new IntegerArrayIterator(attributes)),
							(Collection<PlainNestedEquivalenceClass>) collectionItem,
							getInsSize()
						).getResult()
						.doubleValue()
					);
			} catch (Exception e) {
				log.error("", e);
				return null;
			}
		}else {
			try {
				return new FitnessValue4Double(
						calculation.calculate(
							incPartitionAttributeProcessStrategy.initiate(new IntegerArrayIterator(attributes)), 
							(Collection<EquivalenceClass>) collectionItem,
							getInsSize()
						).getResult()
						.doubleValue()
					);
			} catch (Exception e) {
				log.error("", e);
				return null;
			}
		}
	}
}