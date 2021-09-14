package featureSelection.repository.algorithm.opt.genetic.roughEquivalenceClassBased;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.algorithm.opt.genetic.GeneticAlgUtils;
import featureSelection.repository.entity.opt.genetic.GenerationRecord;
import featureSelection.repository.entity.opt.genetic.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.RECChromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@RoughSet
@ThreadSafetyNotSecured
public abstract class RoughEquivalenceClassBasedGA<Cal extends FeatureImportance<Sig>,
													Sig extends Number, 
													CollectionItem, 
													Chr extends RECChromosome<?>,
													FValue extends FitnessValue<Double>>
	implements ReductionAlgorithm<Cal, Sig, CollectionItem, Chr, FValue>
{
	private Class<Chr> chromosomeClass;
	@Getter private int insSize;
	
	@Override
	public Class<Chr> getChromosomeClass() {
		return chromosomeClass;
	}
	
	@Override
	public int compareMaxFitness(FValue fitnessValue, GenerationRecord<Chr, FValue> geneRecord) {
		return GeneticAlgUtils.ComparingMaxFitness.common(fitnessValue, geneRecord);
	}
	
	@Override
	public int compareBestFitness(Fitness<Chr, FValue> fitness, GenerationRecord<Chr, FValue> geneRecord) {
		return GeneticAlgUtils.ComparingBestFitness.common(fitness, geneRecord);
	}
}