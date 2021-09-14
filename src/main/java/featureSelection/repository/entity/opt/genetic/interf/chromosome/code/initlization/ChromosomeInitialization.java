package featureSelection.repository.entity.opt.genetic.interf.chromosome.code.initlization;

import java.util.Random;

import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;

/**
 * Chromosome Initialization for Genetic Algorithm.
 * 
 * @author Benjamin_L
 *
 * @param <Chr>
 * 		Type of implemented {@link Chromosome} to be initiated.
 */
public interface ChromosomeInitialization<Chr extends Chromosome<?>,
										Param extends ChromosomeInitializationParameters<Chr>> 
{
	/**
	 * Initiate {@link Chromosome}s.
	 * 
	 * @param random
	 * 		A {@link Random} instance used to generate genes randomly.
	 * @return A {@link Chr} array.
	 */
	public Chr[] initChromosomes(Random random);
	/**
	 * Set {@link ChromosomeInitializationParameters} for initialization.
	 * 
	 * @param params
	 * 		{@link ChromosomeInitializationParameters}.
	 */
	public void setParameters(Param params);
	/**
	 * Get the initialization parameters.
	 * 
	 * @return {@link Param} instance.
	 */
	public Param getParameters();
}