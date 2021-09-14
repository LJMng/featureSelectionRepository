package featureSelection.repository.entity.opt.genetic.interf.chromosome.code.initlization;

import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;

/**
 * Parameters for {@link ChromosomeInitialization}
 * 
 * @author Benjamin_L
 *
 * @param <Chr>
 * 		Type of implemented {@link Chromosome} to be initiated.
 */
public interface ChromosomeInitializationParameters<Chr extends Chromosome<?>> {
	Class<Chr> getChromosomeClass();
}