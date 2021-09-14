package featureSelection.repository.algorithm.opt.genetic.dependencyCalculation;

import java.util.Collection;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.algorithm.opt.genetic.GeneticAlgUtils;
import featureSelection.repository.entity.opt.genetic.GenerationRecord;
import featureSelection.repository.entity.opt.genetic.impl.fitness.DoubleFitness;
import featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.genetic.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RoughSet
@ThreadSafetyNotSecured
public abstract class DependencyBasedGA<Sig extends Number, 
										FI extends FeatureImportance<Sig>,
										Chr extends Chromosome<?>>
	implements ReductionAlgorithm<FI, Sig, Instance, Chr, FitnessValue<Double>>
{
	@NonNull private Class<Chr> chromosomeClass;
	
	@Override
	public Class<Chr> getChromosomeClass() {
		return chromosomeClass;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Fitness<Chr, FitnessValue<Double>>[] calculateFitness(
			FI calculation, Collection<Instance> collection, Chr...chromosomes
	) {
		int[] attributes;
		DoubleFitness[] fitness = new DoubleFitness[chromosomes.length];
		for (int c=0; c<chromosomes.length; c++) {
			if (chromosomes[c]==null) {
				fitness[c] = new DoubleFitness<>(new FitnessValue4Double(0.0), chromosomes[c]);
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
	public int compareMaxFitness(FitnessValue<Double> fitnessValue, GenerationRecord<Chr, FitnessValue<Double>> geneRecord) {
		return GeneticAlgUtils.ComparingMaxFitness.common(fitnessValue, geneRecord);
	}
	
	@Override
	public int compareBestFitness(Fitness<Chr, FitnessValue<Double>> fitness, GenerationRecord<Chr, FitnessValue<Double>> geneRecord) {
		return GeneticAlgUtils.ComparingBestFitness.common(fitness, geneRecord);
	}
}
