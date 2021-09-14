package featureSelection.repository.algorithm.opt.genetic.roughEquivalenceClassBased.incrementalPartition;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.algorithm.opt.genetic.roughEquivalenceClassBased.RoughEquivalenceClassBasedGA;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.RECChromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation;

import java.util.Collection;

/**
 * An implementation of {@link RoughEquivalenceClassBasedGA}, using <code>Incremental Partition</code>
 * Strategy to calculate the positive region.
 * <p>
 * The follow field should be set manually <strong>once <code>this</code> is constructed</strong> by setter:
 * <li>inspectAttributeProcessStrategyClass</li>
 * <li>inspectAttributeProcessCapacityCalculator</li>
 * 
 * @see AbstractIPRECBasedGA
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class InTurnIPRECBasedGA<Chr extends RECChromosome<?>, Sig extends Number>
	extends AbstractIPRECBasedGA<Chr, Sig>
{
	public InTurnIPRECBasedGA(Class<Chr> chromosomeClass, int insSize) {
		super(chromosomeClass, insSize);
	}
	
	@Override
	public String shortName() {
		return "GA-IP-REC (In-turn attribute process)";
	}

	@Override
	public FitnessValue<Double> calculateFitness(
			RoughEquivalenceClassBasedIncrementalPartitionExtensionCalculation<Sig> calculation,
			Collection<EquivalenceClass> collection, int...attributes
	) {
		return new FitnessValue4Double(
					calculation.calculate(
						collection, new IntegerArrayIterator(attributes), getInsSize()
					).getResult()
					.doubleValue()
				);
	}

}