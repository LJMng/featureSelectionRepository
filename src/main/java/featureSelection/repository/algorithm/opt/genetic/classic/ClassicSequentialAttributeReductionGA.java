package featureSelection.repository.algorithm.opt.genetic.classic;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionSequentialAlgorithm;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionSequentialIDAlgorithm;
import featureSelection.repository.entity.opt.genetic.impl.fitness.DoubleFitness;
import featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.ClassicChromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.classic.sequential.ClassicSequentialCalculation;

import java.util.Collection;
import java.util.List;

/**
 * An implementation of {@link ClassicAttributeReductionGA} with Sequential search strategy.
 * 
 * @see ClassicSequentialCalculation
 * @see ClassicAttributeReductionGA
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class ClassicSequentialAttributeReductionGA<Chr extends ClassicChromosome<?>, Sig extends Number>
	extends ClassicAttributeReductionGA<Chr, ClassicSequentialCalculation<Sig>, Sig>
{
	private Collection<List<Instance>> decEClasses;
	
	public ClassicSequentialAttributeReductionGA(Class<Chr> chromosomeClass) {
		super(chromosomeClass);
	}
	
	@Override
	public String shortName() {
		return "GA-Classic(Seq)";
	}
	
	@Override
	public FitnessValue<Double> calculateFitness(
			ClassicSequentialCalculation<Sig> calculation,
			Collection<Instance> collection, int... attributes
	) {
		if (decEClasses==null) {
			decEClasses = ClassicAttributeReductionSequentialAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute(collection);
		}
		return new FitnessValue4Double(
				calculation.calculate(collection, new IntegerArrayIterator(attributes), decEClasses, collection.size())
						.getResult()
						.doubleValue()
		);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Fitness<Chr, FitnessValue<Double>>[] calculateFitness(
			ClassicSequentialCalculation<Sig> calculation, Collection<Instance> collection,
			Chr...chromosomes
	) {
		int[] attributes;
		DoubleFitness[] fitness = new DoubleFitness[chromosomes.length];
		
		if (decEClasses==null) {
			decEClasses = ClassicAttributeReductionSequentialIDAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute(collection);
		}
		
		for (int i=0; i<fitness.length; i++) {
			if (chromosomes[i]==null) {
				fitness[i] = new DoubleFitness<>(new FitnessValue4Double(0.0), chromosomes[i]);
			}else {
				attributes = chromosomes[i].getAttributes();
				fitness[i] = new DoubleFitness<>(
								new FitnessValue4Double(
									calculation.calculate(collection, new IntegerArrayIterator(attributes), decEClasses, collection.size())
												.getResult()
												.doubleValue()
								), 
								chromosomes[i]
							);
			}
		}
		return fitness;
	}
	
	@Override
	public Collection<Integer> inspection(
			ClassicSequentialCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> collection, int... attributes
	) {
		return ClassicAttributeReductionSequentialAlgorithm
				.inspection(calculation, 
								sigDeviation, 
								collection, 
								attributes
				);
	}

	@Override
	public Collection<Integer> inspection(
			ClassicSequentialCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> collection, Collection<Integer> attributes
	) {
		ClassicAttributeReductionSequentialAlgorithm
			.inspection(
					calculation, 
					sigDeviation, 
					collection, 
					attributes
		);
		return attributes;
	}
}