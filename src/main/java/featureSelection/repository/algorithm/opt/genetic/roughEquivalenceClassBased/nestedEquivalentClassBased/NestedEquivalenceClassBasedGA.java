package featureSelection.repository.algorithm.opt.genetic.roughEquivalenceClassBased.nestedEquivalentClassBased;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.repository.algorithm.opt.genetic.roughEquivalenceClassBased.RoughEquivalenceClassBasedGA;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.RECChromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.FeatureImportance4NestedEquivalenceClassBased;

@RoughSet
@ThreadSafetyNotSecured
public abstract class NestedEquivalenceClassBasedGA<Cal extends FeatureImportance4NestedEquivalenceClassBased<Sig>,
													Sig extends Number, 
													CollectionItem, 
													Chr extends RECChromosome<?>,
													FValue extends FitnessValue<Double>>
	extends RoughEquivalenceClassBasedGA<Cal, Sig, CollectionItem, Chr, FValue>
{
	public NestedEquivalenceClassBasedGA(Class<Chr> chromosomeClass, int insSize) {
		super(chromosomeClass, insSize);
	}
}