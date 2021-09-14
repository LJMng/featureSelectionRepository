package featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue;

/**
 * An interface for Chromosome Fitness Value in <code>Genetic Algorithm</code>.
 * 
 * @author Benjamin_L
 *
 * @param <V>
 * 		The type of fitness value. Implemented {@link Number}.
 */
public interface FitnessValue<V extends Number> {
	
	/**
	 * Get the fitness value.
	 * 
	 * @return the fitness in {@link V}.
	 */
	V getValue();
	
}
