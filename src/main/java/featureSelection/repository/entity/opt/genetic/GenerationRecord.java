package featureSelection.repository.entity.opt.genetic;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import lombok.Getter;
import lombok.ToString;

/**
 * Generation record for <strong>Genetic Algorithm</strong> with the following fields:
 * <ul>
 * 	<li>{@link #generation}</li>
 * 	<li>{@link #convergenceCount}</li>
 * 	<li>{@link #bestFitness}</li>
 * 	<li>{@link #fitness}</li>
 * 	<li>{@link #distinctAttributes}</li>
 * </ul>
 * 
 * @author Benjamin_L
 *
 * @param <Chr>
 * 		Type of implements {@link Chromosome}.
 */
@Getter
@ToString
@SuppressWarnings("rawtypes")
public class GenerationRecord<Chr extends Chromosome, FValue extends FitnessValue<?>> {
	/**
	 * Current generation.
	 */
	private int generation;
	/**
	 * Current convergence.
	 * <p>
	 * Count the time that best fitness stays unchanged continuously.
	 */
	private int convergenceCount;
	/**
	 * Current best fitness
	 */
	private FValue bestFitness;
	/**
	 * Correspondent {@link Fitness} {@link Collection} of {@link #bestFitness}.
	 */
	private Collection<Fitness<Chr, FValue>> fitness;	// fitness list.
	/**
	 * Correspondent distinct attribute sets of {@link #fitness}.
	 */
	private Collection<IntArrayKey> distinctAttributes; // attributes set
	
	public GenerationRecord() {
		generation = 0;
		convergenceCount = 0;
		bestFitness = null;
		fitness = new LinkedList<>();
		distinctAttributes = new HashSet<>();
	}

	/**
	 * Aggregate {@link #generation}
	 */
	public void nextGeneration() {
		generation++;
	}

	/**
	 * Aggregate {@link #convergenceCount}
	 */
	public void countConvergence() {
		convergenceCount++;
	}

	/**
	 * Resetting {@link #convergenceCount} into 0.
	 */
	public void resetConvergence() {
		convergenceCount = 0;
	}

	/**
	 * Update {@link #bestFitness} by the given one, as well as {@link #distinctAttributes}.
	 *
	 * @see #addBestFitness(Fitness)
	 * 
	 * @param bestFitness
	 *      A {@link Fitness} instance to update.
	 */
	public void updateBestFitness(Fitness<Chr, FValue> bestFitness) {
		this.bestFitness = bestFitness.getFitnessValue();
		if (!fitness.isEmpty())				fitness.clear();
		if (!distinctAttributes.isEmpty())	distinctAttributes.clear();
		
		addBestFitness(bestFitness);
	}

	/**
	 * Add a clone of the given {@link Fitness} into {@link #fitness} as well as into
	 * {@link #distinctAttributes}.
	 *
	 * @param f
	 *      A {@link Fitness} instance to add.
	 */
	public void addBestFitness(Fitness<Chr, FValue> f) {
		Fitness<Chr, FValue> clone = f.clone();
		Arrays.sort(clone.getChromosome().getAttributes());
		fitness.add(clone);
		distinctAttributes.add(new IntArrayKey(clone.getChromosome().getAttributes()));
	}

	/**
	 * Get the size of {@link #distinctAttributes}.
	 *
	 * @return the size of {@link #distinctAttributes}
	 */
	public int getDistinctBestFitnessCount() {
		return distinctAttributes.size();
	}
}