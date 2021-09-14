package featureSelection.repository.algorithm.opt.genetic.roughEquivalenceClassBased;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.opt.genetic.impl.fitness.DoubleFitness;
import featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.RECChromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation;

import java.util.Collection;

/**
 * An implementation of {@link RoughEquivalenceClassBasedGA}, using <code>Simple Positive
 * Reduct</code> Strategy to calculate the positive region.
 * 
 * @see RoughEquivalenceClassBasedGA
 * @see RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation
 * @see RoughEquivalenceClassBasedExtensionAlgorithm.SimpleCounting.RealTimeCounting
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class RealtimeSimpleCountingRECBasedGA<Chr extends RECChromosome<?>, Sig extends Number>
	extends RoughEquivalenceClassBasedGA<RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig>,
											Sig, EquivalenceClass, Chr, FitnessValue<Double>>
{
	public RealtimeSimpleCountingRECBasedGA(Class<Chr> chromosomeClass, int insSize) {
		super(chromosomeClass, insSize);
	}
	
	@Override
	public String shortName() {
		return "GA-S-NEC";//"GA-(R)S(C)-REC";
	}

	@Override
	public FitnessValue<Double> calculateFitness(
			RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation,
			Collection<EquivalenceClass> collection, int...attributes
	) {
		return new FitnessValue4Double(
					calculation.calculate(
							collection, new IntegerArrayIterator(attributes), getInsSize()
						).getResult()
						.doubleValue()
				);
	}

	@Override
	public DoubleFitness<Chr>[] calculateFitness(
			RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation,
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
			RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation,
			Sig sigDeviation, Collection<EquivalenceClass> equClasses, int...attributes
	) {
		return RoughEquivalenceClassBasedExtensionAlgorithm
				.SimpleCounting
				.RealTimeCounting
				.inspection(getInsSize(), calculation, sigDeviation, equClasses, attributes);
	}

	@Override
	public Collection<Integer> inspection(
			RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation,
			Sig sigDeviation, Collection<EquivalenceClass> collection, Collection<Integer> attributes
	) {
		 RoughEquivalenceClassBasedExtensionAlgorithm
		 	.SimpleCounting
		 	.RealTimeCounting
		 	.inspection(getInsSize(), calculation, sigDeviation, collection, attributes);
		 return attributes;
	}
}