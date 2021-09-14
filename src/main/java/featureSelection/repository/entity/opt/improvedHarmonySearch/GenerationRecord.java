package featureSelection.repository.entity.opt.improvedHarmonySearch;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.harmony.HarmonyFactory;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;
import lombok.Getter;

@Getter
public class GenerationRecord<FValue extends FitnessValue<?>> {
	/**
	 * Current generation
	 */
	private int generation;
	/**
	 * Current convergence
	 */
	private int convergence;
	/**
	 * Global best fitness.
	 */
	private FValue bestFitness;
	/**
	 * Harmonies have global best fitness.
	 */
	private Collection<Harmony<?>> bestHarmonies;
	/**
	 * Distinct attributes of best harmonies.
	 */
	private Collection<IntArrayKey> distinctAttributes;
	
	public GenerationRecord() {
		generation = 0;
		convergence = 0;
		bestFitness = null;
		bestHarmonies = new LinkedList<>();
		distinctAttributes = new HashSet<>();
	}
	
	public void nextGeneration() {
		generation++;
	}
	
	public void countConvergence() {
		convergence++;
	}
	
	public void resetConvergence() {
		convergence = 0;
	}
	
	/**
	 * Update the {@link #bestFitness} into the given one. 
	 * <p>
	 * {@link #bestHarmonies}, {@link #distinctAttributes} would be cleared.
	 * 
	 * @param bestFitness
	 * 		The best {@link Fitness} to be updated.
	 * @param harmony
	 * 		{@link Harmony} instance with the best fitness.
	 */
	public void updateBestFitness(Fitness<?, FValue> bestFitness, Harmony<?> harmony) {
		this.bestFitness = bestFitness.getFitnessValue();
		if (!bestHarmonies.isEmpty()) {
			bestHarmonies.clear();
			distinctAttributes.clear();
		}
		addBestFitness(harmony);
	}
	
	public void addBestFitness(Harmony<?> harmony) {
		Harmony<?> copy = HarmonyFactory.copyHarmony(harmony);
		bestHarmonies.add(copy);
		distinctAttributes.add(new IntArrayKey(copy.getAttributes()));
	}

	public int getDistinctBestFitnessCount() {
		return distinctAttributes.size();
	}
	
	@Override
	public String toString() {
		return "GenerationRecord [generation=" + generation + ", convergenceCount=" + convergence
				+ ", bestFitness=" + bestFitness + ", |harmonies|=" + bestHarmonies.size() + "]";
	}
}
