package featureSelection.repository.entity.opt.genetic.interf.chromosome;

import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.genetic.ReductionParameters;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;

import java.util.Collection;
import java.util.Random;

/**
 * Chromosome Cross Algorithm for <code>Genetic Algorithm</code>.
 * 
 * @author Benjamin_L
 *
 * @param <Chr>
 * 		Type of implemented {@link Chromosome}.
 */
public interface ChromosomeCrossAlgorithm<Chr extends Chromosome<?>, FValue extends FitnessValue<?>> {
	public Chr[] crossGenes(Chr c1, Chr c2, Collection<Integer> crossIndex);
	public Chr[] crossChromosomes(Fitness<Chr, FValue>[] fitness,
								  ReductionParameters<? extends FeatureImportance<?>, ? extends Number, ?, Chr, FValue> params,
								  Random random
								);
}