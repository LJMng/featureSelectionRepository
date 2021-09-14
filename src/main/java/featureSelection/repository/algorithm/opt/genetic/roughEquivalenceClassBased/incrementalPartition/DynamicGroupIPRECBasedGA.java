package featureSelection.repository.algorithm.opt.genetic.roughEquivalenceClassBased.incrementalPartition;

import java.util.Collection;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.algorithm.opt.alg.rec.incrementalPartition.DynamicBasedIPRECBasedOptimization;
import featureSelection.repository.algorithm.opt.genetic.roughEquivalenceClassBased.RoughEquivalenceClassBasedGA;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.RECChromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of {@link RoughEquivalenceClassBasedGA}, using <code>Incremental
 * Partition</code> Strategy to calculate the positive region.
 * <p>
 * The follow field should be set manually <strong>once <code>this</code> is constructed</strong>
 * by setter:
 * <ul>
 * 	<li>inspectAttributeProcessStrategyClass</li>
 * 	<li>inspectAttributeProcessCapacityCalculator</li>
 * 	<li>incPartitionAttributeProcessStrategy</li>
 * </ul>
 * 
 * @see AbstractIPRECBasedGA
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class DynamicGroupIPRECBasedGA<Chr extends RECChromosome<?>, Sig extends Number>
	extends AbstractIPRECBasedGA<Chr, Sig>
	implements DynamicBasedIPRECBasedOptimization
{
	@Setter protected AttributeProcessStrategy incPartitionAttributeProcessStrategy;
	
	public DynamicGroupIPRECBasedGA(Class<Chr> chromosomeClass, int universeSize) {
		super(chromosomeClass, universeSize);
	}
	
	@Override
	public String shortName() {
		return "GA-IP-REC (Dynamic group number)";
	}

	@Override
	public FitnessValue<Double> calculateFitness(
			RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Sig> calculation,
			Collection<EquivalenceClass> equClasses, int...attributes
	) {
		try {
			return new FitnessValue4Double(
					calculation.calculate(
						incPartitionAttributeProcessStrategy.initiate(new IntegerArrayIterator(attributes)),
						equClasses,
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