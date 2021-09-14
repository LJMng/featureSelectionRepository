package featureSelection.repository.entity.opt.genetic.interf.fitness;

import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;

/**
 * Fitness of {@link Chromosome} in Generic Algorithm. 
 * Note that {@link #clone()}, {@link Object#equals(Object)}, {@link Object#hashCode()} needs to
 * be override.
 * <p>
 * {@link Chromosome} entity and its fitness values is stored.
 * 
 * @author Benjamin_L
 *
 * @param <Chr>
 * 		Type of implements {@link Chromosome}.
 */
public interface Fitness<Chr extends Chromosome, FValue extends FitnessValue<?>>
	extends Comparable<Fitness<Chr, FValue>>, 
			Cloneable
{
	FValue getFitnessValue();
	Chr getChromosome();
	
	Fitness<Chr, FValue> clone();
}