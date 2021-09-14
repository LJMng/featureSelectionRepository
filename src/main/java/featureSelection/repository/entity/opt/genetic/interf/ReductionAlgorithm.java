package featureSelection.repository.entity.opt.genetic.interf;

import featureSelection.basic.model.optimization.OptimizationAlgorithm;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.opt.genetic.GenerationRecord;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;

import java.util.Collection;

/**
 * An interface for <code>Feature Selection</code> using <code>Genetic Algorithm</code>.
 * 
 * @author Benjamin_L
 *
 * @param <Cal>
 * 		Implemented {@link FeatureImportance}
 * @param <Sig>
 * 		The type of feature importance.
 * @param <Item>
 * 		The type of items in {@link Collection} of the dataset. 
 * 		{@link Instance}, {@link EquivalenceClass}, etc.
 * @param <Chr>
 * 		Implemented {@link Chromosome}.
 * @param <FValue>
 * 		Implemented {@link FitnessValue}.
 */
public interface ReductionAlgorithm<Cal extends FeatureImportance<?>,
									Sig extends Number, 
									Item, 
									Chr extends Chromosome<?>,
									FValue extends FitnessValue<?>>
	extends OptimizationAlgorithm
{
	/**
	 * Calculate the fitness using attributes.
	 * 
	 * @param calculation
	 * 		Implemented {@link Cal} instance.
	 * @param collection
	 * 		{@link Instance} / {@link EquivalenceClass} {@link Collection}.
	 * @param attributes
	 * 		Attributes to be evaluated. (Starts from 1)
	 * @return {@link FValue} instance.
	 */
	FValue calculateFitness(Cal calculation, Collection<Item> collection, int...attributes);

	/**
	 * Calculate fitnesses of given {@link Chromosome}s.
	 * 
	 * @param calculation
	 * 		Implemented {@link Cal} instance.
	 * @param collection
	 * 		{@link Instance} / {@link EquivalenceClass} {@link Collection}.
	 * @param chromosomes
	 *		{@link Chromosome}s to be evaluated. 
	 * @return {@link FValue} array correspondent to <code>chromosomes</code>.
	 */
	Fitness<Chr, FValue>[] calculateFitness(Cal calculation, Collection<Item> collection,
											@SuppressWarnings("unchecked") Chr...chromosomes);
	
	/**
	 * Get the {@link Chromosome} Class used in algorithm.
	 * 
	 * @return {@link Chromosome} {@link Class}.
	 */
	Class<Chr> getChromosomeClass();

	int compareMaxFitness(FValue fitnessValue, GenerationRecord<Chr, FValue> geneRecord);
	int compareBestFitness(Fitness<Chr, FValue> fitnessValue, GenerationRecord<Chr, FValue> geneRecord);

	Collection<Integer> inspection(Cal calculation, Sig sigDeviation, Collection<Item> collection, int...attributes);
	Collection<Integer> inspection(Cal calculation, Sig sigDeviation, Collection<Item> collection, Collection<Integer> attributes);
	
	String shortName();
}