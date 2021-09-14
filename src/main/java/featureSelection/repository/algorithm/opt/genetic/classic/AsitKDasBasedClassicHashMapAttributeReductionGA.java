package featureSelection.repository.algorithm.opt.genetic.classic;

import java.util.Collection;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionHashMapAlgorithm;
import featureSelection.repository.algorithm.opt.genetic.GeneticAlgUtils;
import featureSelection.repository.entity.opt.genetic.GenerationRecord;
import featureSelection.repository.entity.opt.genetic.impl.fitness.DoubleFitness;
import featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue.FitnessValue4Double4AsitKDas;
import featureSelection.repository.entity.opt.genetic.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.ClassicChromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.support.calculation.alg.classic.hash.ClassicHashMapCalculation;
import featureSelection.repository.support.calculation.asitKDasFitness.classic.AsitKDasFitnessCalculation4ClassicHashMap;
import lombok.Getter;

/**
 * An implementation of {@link ClassicAttributeReductionGA} with HashMap search strategy.
 * 
 * @see ClassicHashMapCalculation
 * @see ClassicAttributeReductionGA
 * 
 * @author Benjamin_L
 */
public class AsitKDasBasedClassicHashMapAttributeReductionGA<Chr extends ClassicChromosome<?>>
	implements ReductionAlgorithm<
					AsitKDasFitnessCalculation4ClassicHashMap<ClassicHashMapCalculation<Double>>,
					Double, Instance, Chr,
					FitnessValue4Double4AsitKDas<Double>>
{
	private Map<Integer, Collection<Instance>> decEquClass;
	@Getter private Class<Chr> chromosomeClass;
	
	public AsitKDasBasedClassicHashMapAttributeReductionGA(Class<Chr> chromosomeClass) {
		this.chromosomeClass=chromosomeClass;
	}

	@Override
	public String shortName() {
		return "GA-AsitKDas-Classic(HashMap)";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Fitness<Chr, FitnessValue4Double4AsitKDas<Double>>[] calculateFitness(
			AsitKDasFitnessCalculation4ClassicHashMap<ClassicHashMapCalculation<Double>> calculation, 
			Collection<Instance> collection, Chr...chromosomes
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
				fitness[i] = new DoubleFitness<>(
								new FitnessValue4Double4AsitKDas<Double>(0.0, 0.0), 
								(Chromosome) chromosomes[i]
							);
			}else {
				attributes = chromosomes[i].getAttributes();
				if (attributes.length==0) {
					fitness[i] = new DoubleFitness<>(
									new FitnessValue4Double4AsitKDas<Double>(0.0, 0.0), 
									chromosomes[i]
								);
				}else {
					calculation.calculate(collection, new IntegerArrayIterator(attributes), decEquClass, collection.size());
					fitness[i] = new DoubleFitness<>(
									new FitnessValue4Double4AsitKDas<Double>(
											calculation.getResult(), 
											calculation.getFeatureImportanceValue()
									),
									chromosomes[i]
								);
				}
			}
		}
		return fitness;
	}

	@Override
	public FitnessValue4Double4AsitKDas<Double> calculateFitness(
			AsitKDasFitnessCalculation4ClassicHashMap<ClassicHashMapCalculation<Double>> calculation,
			Collection<Instance> collection, int... attributes
	) {
		if (decEquClass==null) {
			decEquClass = ClassicAttributeReductionHashMapAlgorithm
							.Basic
							.equivalenceClassOfDecisionAttribute((Collection<Instance>) collection);
		}
		calculation.calculate(collection, new IntegerArrayIterator(attributes), decEquClass, collection.size());
		return new FitnessValue4Double4AsitKDas<Double>(
				calculation.getResult(),
				calculation.getFeatureImportanceValue()
			);
	}

	@Override
	public Collection<Integer> inspection(
			AsitKDasFitnessCalculation4ClassicHashMap<ClassicHashMapCalculation<Double>> calculation, 
			Double sigDeviation, Collection<Instance> collection, int... attributes
	) {
		return ClassicAttributeReductionHashMapAlgorithm.inspection(
					calculation.getFeatureImportance(), 
					sigDeviation, 
					collection, 
					attributes
				);
	}

	@Override
	public Collection<Integer> inspection(
			AsitKDasFitnessCalculation4ClassicHashMap<ClassicHashMapCalculation<Double>> calculation, 
			Double sigDeviation, Collection<Instance> collection, Collection<Integer> attributes
	) {
		ClassicAttributeReductionHashMapAlgorithm.inspection(
				calculation.getFeatureImportance(), 
				sigDeviation, 
				collection, 
				attributes
		);
		return attributes;
	}

	@Override
	public int compareMaxFitness(
			FitnessValue4Double4AsitKDas<Double> fitnessValue,
			GenerationRecord<Chr, FitnessValue4Double4AsitKDas<Double>> geneRecord
	) {
		return GeneticAlgUtils.ComparingMaxFitness.asitKDas(fitnessValue, geneRecord);
	}
	
	@Override
	public int compareBestFitness(
			Fitness<Chr, FitnessValue4Double4AsitKDas<Double>> fitness,
			GenerationRecord<Chr, FitnessValue4Double4AsitKDas<Double>> geneRecord
	) {
		return GeneticAlgUtils.ComparingBestFitness.common(fitness, geneRecord);
	}
}