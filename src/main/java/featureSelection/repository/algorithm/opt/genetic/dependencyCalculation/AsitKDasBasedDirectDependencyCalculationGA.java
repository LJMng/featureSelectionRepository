package featureSelection.repository.algorithm.opt.genetic.dependencyCalculation;

import java.util.Collection;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.dependencyCalculation.DirectDependencyCalculationAlgorithm;
import featureSelection.repository.algorithm.opt.genetic.GeneticAlgUtils;
import featureSelection.repository.entity.opt.genetic.GenerationRecord;
import featureSelection.repository.entity.opt.genetic.impl.fitness.DoubleFitness;
import featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue.FitnessValue4Double4AsitKDas;
import featureSelection.repository.entity.opt.genetic.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.DDCChromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.directDependencyCalculation.FeatureImportance4DirectDependencyCalculation;
import featureSelection.repository.support.calculation.asitKDasFitness.DefaultAsitKDasFitnessCalculation;
import featureSelection.repository.support.calculation.asitKDasFitness.dependencyCalculation.directDependencyCalculation.AsitKDasFitnessCalculation4DDC;
import lombok.Getter;

/**
 * Implementation of {@link DependencyBasedGA} Attribute Reduction with Asit.K.Das Algorithm
 * <strong>PLUS</strong> DDC algorithm. 
 * 
 * @see DefaultAsitKDasFitnessCalculation
 * @see AsitKDasFitnessCalculation4DDC
 * @see DependencyBasedGA
 * @see DirectDependencyCalculationAlgorithm
 * @see FeatureImportance4DirectDependencyCalculation
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class AsitKDasBasedDirectDependencyCalculationGA<Chr extends DDCChromosome<?>>
	implements ReductionAlgorithm<
				AsitKDasFitnessCalculation4DDC<FeatureImportance4DirectDependencyCalculation<Double>>,
				Double, Instance, Chr,
				FitnessValue4Double4AsitKDas<Double>>
{
	@Getter private Class<Chr> chromosomeClass;

	public AsitKDasBasedDirectDependencyCalculationGA(Class<Chr> chromosomeClass) {
		this.chromosomeClass = chromosomeClass;
	}

	@Override
	public String shortName() {
		return "GA-AsitKDas-DDC";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Fitness<Chr, FitnessValue4Double4AsitKDas<Double>>[] calculateFitness(
			AsitKDasFitnessCalculation4DDC<FeatureImportance4DirectDependencyCalculation<Double>> calculation, 
			Collection<Instance> collection, Chr...chromosomes
	) {
		int[] attributes;
		DoubleFitness[] fitness = new DoubleFitness[chromosomes.length];
		for (int c=0; c<chromosomes.length; c++) {
			if (chromosomes[c]==null) {
				fitness[c] = new DoubleFitness<>(new FitnessValue4Double4AsitKDas(0.0, 0.0), chromosomes[c]);
			}else {
				attributes = chromosomes[c].getAttributes();		// filtered -1 gene in Chromosome.
				fitness[c] = new DoubleFitness<>(
								calculateFitness(calculation, collection, attributes),
								chromosomes[c]
							);
			}
		}
		return fitness;
	}
	
	@Override
	public FitnessValue4Double4AsitKDas<Double> calculateFitness(
			AsitKDasFitnessCalculation4DDC<FeatureImportance4DirectDependencyCalculation<Double>> calculation, 
			Collection<Instance> collection, int...attributes
	) {
		calculation.calculate(collection, new IntegerArrayIterator(attributes));
		return new FitnessValue4Double4AsitKDas<Double>(
				calculation.getResult(),
				calculation.getFeatureImportanceValue()
			);
	}

	@Override
	public Collection<Integer> inspection(
			AsitKDasFitnessCalculation4DDC<FeatureImportance4DirectDependencyCalculation<Double>> calculation, 
			Double sigDeviation, Collection<Instance> collection, int...attributes
	) {
		return DirectDependencyCalculationAlgorithm
					.inspection(
						calculation.getFeatureImportance(), 
						sigDeviation, 
						collection, 
						attributes
					);
	}

	@Override
	public Collection<Integer> inspection(
			AsitKDasFitnessCalculation4DDC<FeatureImportance4DirectDependencyCalculation<Double>> calculation, 
			Double sigDeviation, Collection<Instance> collection, Collection<Integer> attributes
	) {
		DirectDependencyCalculationAlgorithm
			.inspection(
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