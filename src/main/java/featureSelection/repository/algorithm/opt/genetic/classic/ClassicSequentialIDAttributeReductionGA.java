package featureSelection.repository.algorithm.opt.genetic.classic;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionSequentialIDAlgorithm;
import featureSelection.repository.entity.opt.genetic.impl.fitness.DoubleFitness;
import featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.ClassicChromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.classic.sequential.ClassicSequentialIDCalculation;

import java.util.Collection;
import java.util.List;

/**
 * An implementation of {@link ClassicAttributeReductionGA} with {@link Instance} ID
 * based Sequential search strategy.
 * 
 * @see ClassicSequentialIDCalculation
 * @see ClassicAttributeReductionGA
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class ClassicSequentialIDAttributeReductionGA<Chr extends ClassicChromosome<?>, Sig extends Number>
	extends ClassicAttributeReductionGA<Chr, ClassicSequentialIDCalculation<Sig>, Sig>
{
	private Collection<List<Instance>> decEClasses;
	
	public ClassicSequentialIDAttributeReductionGA(Class<Chr> chromosomeClass) {
		super(chromosomeClass);
	}
	
	@Override
	public String shortName() {
		return "GA-Classic(Seq.ID)";
	}
	
	@Override
	public FitnessValue<Double> calculateFitness(
			ClassicSequentialIDCalculation<Sig> calculation,
			Collection<Instance> collection, int... attributes
	) {
		Collection<List<Instance>> equClass =
				ClassicAttributeReductionSequentialIDAlgorithm
					.Basic
					.equivalenceClass(collection, new IntegerArrayIterator(attributes));
		if (decEClasses==null) {
			decEClasses = ClassicAttributeReductionSequentialIDAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute(collection);
		}
		return new FitnessValue4Double(
					calculation.calculate(equClass, decEClasses, attributes.length, collection.size())
						.getResult()
						.doubleValue()
				);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Fitness<Chr, FitnessValue<Double>>[] calculateFitness(
			ClassicSequentialIDCalculation<Sig> calculation, Collection<Instance> collection,
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
				Collection<List<Instance>> eClasses =
						ClassicAttributeReductionSequentialIDAlgorithm
							.Basic
							.equivalenceClass(collection, new IntegerArrayIterator(attributes));
				
				fitness[i] = new DoubleFitness<>(
								new FitnessValue4Double(
									calculation.calculate(eClasses, decEClasses, attributes.length, collection.size())
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
			ClassicSequentialIDCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> collection, int... attributes
	) {
		return ClassicAttributeReductionSequentialIDAlgorithm
				.inspection(calculation, 
								sigDeviation, 
								collection, 
								new IntegerArrayIterator(attributes)
				);
	}

	@Override
	public Collection<Integer> inspection(
			ClassicSequentialIDCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> collection, Collection<Integer> attributes
	) {
		return ClassicAttributeReductionSequentialIDAlgorithm
				.inspection(
						calculation,
						sigDeviation,
						collection,
						new IntegerCollectionIterator(attributes)
				);
	}
}