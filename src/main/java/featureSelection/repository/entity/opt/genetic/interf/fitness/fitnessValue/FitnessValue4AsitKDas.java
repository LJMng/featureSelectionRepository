package featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue;

import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;

/**
 * An interface for Chromosome in Asit.K.Das <code>Genetic Algorithm</code>.
 * 
 * @see FitnessValue
 * 
 * @author Benjamin_L
 *
 * @param <V>
 * 		The type of fitness value. Implemented {@link Number}.
 * @param <Sig>
 * 		The type of feature importance. Implemented {@link Number}.
 */
public interface FitnessValue4AsitKDas<V extends Number, Sig extends Number> extends FitnessValue<V> {
	V getValue();
	/**
	 * Get the feature importance of attributes that the {@link Chromosome} represents.
	 * 
	 * @return {@link Sig} as feature importance.
	 */
	Sig getFeatureImportance();
}
