package featureSelection.repository.algorithm.opt.genetic.classic;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionHashMapAlgorithm;
import featureSelection.repository.entity.opt.genetic.impl.fitness.DoubleFitness;
import featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.ClassicChromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.classic.hash.ClassicHashMapCalculation;

import java.util.Collection;
import java.util.Map;

/**
 * An implementation of {@link ClassicAttributeReductionGA} with HashMap search strategy.
 * 
 * @see ClassicHashMapCalculation
 * @see ClassicAttributeReductionGA
 * 
 * @author Benjamin_L
 */
public class ClassicHashMapAttributeReductionGA<Chr extends ClassicChromosome<?>, Sig extends Number>
	extends ClassicAttributeReductionGA<Chr, ClassicHashMapCalculation<Sig>, Sig>
{
	private Map<Integer, Collection<Instance>> decEquClass;
	
	public ClassicHashMapAttributeReductionGA(Class<Chr> chromosomeClass) {
		super(chromosomeClass);
	}

	@Override
	public String shortName() {
		return "GA-Classic(HashMap)";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Fitness<Chr, FitnessValue<Double>>[] calculateFitness(
			ClassicHashMapCalculation<Sig> calculation, Collection<Instance> collection,
			Chr...chromosomes
	) {
		int[] attributes;
		DoubleFitness[] fitness = new DoubleFitness[chromosomes.length];
		
		if (decEquClass==null) {
			decEquClass = ClassicAttributeReductionHashMapAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute((Collection<Instance>) collection);
		}
		
		for (int i=0; i<fitness.length; i++) {
			if (chromosomes[i]==null) {
				fitness[i] = new DoubleFitness<>(new FitnessValue4Double(0.0), (Chromosome) chromosomes[i]);
			}else {
				attributes = chromosomes[i].getAttributes();
				fitness[i] = new DoubleFitness<>(
								new FitnessValue4Double(
									calculation.calculate(collection, new IntegerArrayIterator(attributes), decEquClass, collection.size())
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
	public FitnessValue<Double> calculateFitness(
			ClassicHashMapCalculation<Sig> calculation, Collection<Instance> collection,
			int...attributes
	) {
		if (decEquClass==null) {
			decEquClass = ClassicAttributeReductionHashMapAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute((Collection<Instance>) collection);
		}
		return new FitnessValue4Double(
				calculation.calculate(collection, new IntegerArrayIterator(attributes), decEquClass, collection.size())
						.getResult()
						.doubleValue()
		);
	}

	@Override
	public Collection<Integer> inspection(
			ClassicHashMapCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> collection, int... attributes
	) {
		return ClassicAttributeReductionHashMapAlgorithm.inspection(
				calculation, sigDeviation, collection, attributes
		);
	}

	@Override
	public Collection<Integer> inspection(
			ClassicHashMapCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> collection, Collection<Integer> attributes
	) {
		ClassicAttributeReductionHashMapAlgorithm.inspection(
				calculation, sigDeviation, collection, attributes
		);
		return attributes;
	}
}