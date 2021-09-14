package featureSelection.repository.algorithm.opt.genetic.classic;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.algorithm.opt.genetic.GeneticAlgUtils;
import featureSelection.repository.entity.opt.genetic.GenerationRecord;
import featureSelection.repository.entity.opt.genetic.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.ClassicChromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RoughSet
@ThreadSafetyNotSecured
public abstract class ClassicAttributeReductionGA<Chr extends ClassicChromosome<?>, FI extends FeatureImportance<Sig>, Sig extends Number>
	implements ReductionAlgorithm<FI, Sig, Instance, Chr, FitnessValue<Double>>
{
	@NonNull @Getter private Class<Chr> chromosomeClass;

	@Override
	public int compareMaxFitness(
			FitnessValue<Double> fitnessValue,
			GenerationRecord<Chr, FitnessValue<Double>> geneRecord
	) {
		return GeneticAlgUtils.ComparingMaxFitness.common(fitnessValue, geneRecord);
	}
	
	@Override
	public int compareBestFitness(
			Fitness<Chr, FitnessValue<Double>> fitness,
			GenerationRecord<Chr, FitnessValue<Double>> geneRecord
	) {
		return GeneticAlgUtils.ComparingBestFitness.common(fitness, geneRecord);
	}
}