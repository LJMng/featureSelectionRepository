package featureSelection.repository.entity.opt.particleSwarm;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;
import lombok.Getter;
import lombok.Setter;

/**
 * Generation record for Particle Swarm Optimization.
 * 
 * @author Benjamin_L
 *
 * @param <Velocity> 
 * 		Type of implemented {@link Number} as values of velocity.
 * @param <Posi> 
 * 		Type of implemented {@link Position}.
 * @param <FValue>
 * 		Type of implemented {@link FitnessValue}.
 */
public class GenerationRecord<Velocity, Posi extends Position<?>, FValue extends FitnessValue<?>> {
	@Getter private int generation;							// eNum 叠代次数/第x代
	@Getter private int convergenceCount;					// conNumber 连续fitness最优次数
	
	@Getter private Fitness<Posi, FValue> globalBestFitness;
	@Setter @Getter private Collection<Fitness<Posi, FValue>> globalBestFitnessCollection;
	@Getter private Collection<IntArrayKey> distinctAttributes;
	
	public GenerationRecord() {
		generation = 0;
		convergenceCount = 0;
		globalBestFitnessCollection = new LinkedList<>();
		distinctAttributes = new HashSet<>();
	}
	
	public void nextGeneration() {
		generation++;
	}
	
	public void countConvergence() {
		convergenceCount++;
	}
	
	public void resetConvergence() {
		convergenceCount = 0;
	}

	public Number globalBestFitnessValue() {
		return globalBestFitness==null? 0:globalBestFitness.getFitnessValue().getValue();
	}

	public Integer getGlobalBestAttributeLength() {
		return globalBestFitness==null ||
				globalBestFitness.getPosition()==null?
				null: globalBestFitness.getPosition().getAttributes().length;
	}

	/**
	 * Update {@link #globalBestFitness}:
	 * <ul>
	 * 	<li>Clear {@link #globalBestFitnessCollection}, {@link #distinctAttributes}</li>
	 * 	<li>{@link #addGlobalBestFitness(Fitness)}</li>
	 * 	<li>Set field {@link #globalBestFitness}</li>
	 * </ul>
	 * 
	 * @see #addGlobalBestFitness(Fitness, boolean)
	 * 
	 * @param globalBestFitness
	 * 		The {@link Fitness} to be set.
	 */
	public void updateGlobalBestFitness(Fitness<Posi, FValue> globalBestFitness) {
		if (!globalBestFitnessCollection.isEmpty()) {
			globalBestFitnessCollection.clear();
			distinctAttributes.clear();
		}
		addGlobalBestFitness(globalBestFitness, true);
	}
	
	/**
	 * Execute {@link #addGlobalBestFitness(Fitness, boolean)} with the 2nd parameter 
	 * <i>"updateGlobalBest"</i> set <strong>false</strong>: 
	 * <pre>
	 * {@code addGlobalBestFitness(fitness, false);}
	 * </pre>
	 * 
	 * @see #addGlobalBestFitness(Fitness, boolean)
	 * 
	 * @param fitness
	 * 		The {@link Fitness} instance to be added.
	 */
	public void addGlobalBestFitness(Fitness<Posi, FValue> fitness) {
		addGlobalBestFitness(fitness, false);
	}
	/**
	 * Add a {@link Fitness} instance whose {@link FitnessValue} equals to
	 * {@link #globalBestFitness} into {@link #globalBestFitnessCollection}.
	 * <p>
	 * The following fields will be maintained during the calling of the method:
	 * <ul>
	 * 	<li>{@link #globalBestFitness}</li>
	 * 	<li>{@link #distinctAttributes}</li>
	 * </ul>
	 * 
	 * @see Fitness#clone()
	 * 
	 * @param fitness
	 * 		The {@link Fitness} instance to be added.
	 * @param updateGlobalBest
	 * 		True to replace {@link #globalBestFitness} with the clone of the given
	 * 		<code>fitness</code>.
	 */
	public void addGlobalBestFitness(Fitness<Posi, FValue> fitness, boolean updateGlobalBest) {
		Fitness<Posi, FValue> fitnessClone = fitness.clone();
		
		globalBestFitnessCollection.add(fitnessClone);
		distinctAttributes.add(new IntArrayKey(fitness.getPosition().getAttributes()));
		
		if (updateGlobalBest ||
			globalBestFitness==null || 
			globalBestFitness.getPosition().getAttributes().length>
			fitness.getPosition().getAttributes().length
		) {
			globalBestFitness = fitnessClone;	
		}
	}

	/**
	 * Count the number of current distinct attributes reduct. (i.e.
	 * <code>distinctAttributes.size()</code>)
	 * 
	 * @see #distinctAttributes
	 * 
	 * @return The number of distinct attributes reduct.
	 */
	public int countDistinctBestFitness() {
		return distinctAttributes.size();
	}
}