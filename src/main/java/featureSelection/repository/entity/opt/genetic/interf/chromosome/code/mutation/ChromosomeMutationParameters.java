package featureSelection.repository.entity.opt.genetic.interf.chromosome.code.mutation;

import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;

/**
 * Parameters for {@link ChromosomeMutation}.
 * 
 * @author Benjamin_L
 *
 * @param <Chr>
 * 		Type of implemented {@link Chromosome} to be mutated.
 */
public interface ChromosomeMutationParameters<Chr extends Chromosome<?>> {
	Class<Chr> getChromosomeClass();
}