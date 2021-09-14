package featureSelection.repository.entity.opt.particleSwarm.interf;

import featureSelection.basic.model.optimization.OptimizationAlgorithm;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Particle;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;

import java.util.Collection;

public interface ReductionAlgorithm<Item, Velocity, Posi extends Position<?>, FValue extends FitnessValue<?>,
									Cal extends FeatureImportance<Sig>, Sig extends Number>
	extends OptimizationAlgorithm
{
	Class<? extends Particle> getParticleClass();
	
	/**
	 * Calculate the fitness/feature significance of the given <code>attributes</code>.
	 * 
	 * @param calculation
	 * 		{@link Cal} instance.
	 * @param collection
	 * 		{@link Instance} / <code>EquivalenceClass</code> {@link Collection}.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @return Feature importance in {@link Sig}.
	 */
	FValue fitnessValue(Cal calculation, Collection<Item> collection, int[] attributes);
		
	/**
	 * Calculate the fitness of attributes with the given <code>attributeIndexes</code>.
	 * 
	 * @param calculation
	 * 		{@link Cal} instance.
	 * @param collection
	 * 		{@link Instance} / <code>EquivalenceClass</code> {@link Collection}.
	 * @param attributesSrc
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @param attributeIndexes
	 * 		The indexes of <code>attributesSrc</code> to be calculated. (Starts from 0)
	 * @return {@link Fitness}.
	 */
	Fitness<Posi, FValue> fitness(
			Cal calculation, Collection<Item> collection, int[] attributesSrc,
			int[] attributeIndexes
	);
	/**
	 * Calculate the fitness of given <code>particles</code>.
	 * 
	 * @param calculation
	 * 		{@link Cal} instance.
	 * @param collection
	 * 		{@link Instance} / <code>EquivalenceClass</code> {@link Collection}.
	 * @param attributesSrc
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @param particle
	 * 		{@link Particle}s.
	 * @return {@link Fitness[]}.
	 */
	Fitness<Posi, FValue>[] fitness(
			Cal calculation, Collection<Item> collection, int[] attributesSrc,
			@SuppressWarnings("unchecked") Particle<Velocity, Posi, FValue>...particle
	);
	
	/**
	 * Compare the given max fitness value with another fitness.
	 * 
	 * @param maxFitnessValue
	 * 		Max {@link FValue} to compare with.
	 * @param fitnessValue
	 * 		Another {@link FValue} to compare with.
	 * @return positive value if <code>maxFitnessValue</code> is greater, 0 if equals, negative value if 
	 * 			smaller.
	 */
	int compareMaxFitness(FValue maxFitnessValue, FValue fitnessValue);
	/**
	 * Compare the given max fitness value with the best Fitness in {@link GenerationRecord}.
	 * 
	 * @param maxFitnessValue
	 * 		Max {@link FValue} to compare with.
	 * @param generRecord
	 * 		A {@link GenerationRecord} instance.
	 * @return positive value if <code>maxFitnessValue</code> is greater, 0 if equals, negative value if 
	 * 			smaller.
	 */
	int compareMaxFitness(FValue maxFitnessValue, GenerationRecord<Velocity, Posi, FValue> generRecord);
	/**
	 * Compare the given fitness value with the best Fitness.
	 * 
	 * @param fitness1
	 * 		A {@link Fitness}.
	 * @param fitness2
	 * 		Another {@link Fitness}.
	 * @return positive value if <code>fitness1</code> is greater, 0 if equals, negative value if smaller.
	 */
	int compareFitness(Fitness<Posi, FValue> fitness1, Fitness<Posi, FValue> fitness2);
	
	Collection<Integer> inspection(Cal calculation, Sig sigDeviation, Collection<Item> collection, int[] positionAttr);
	Collection<Integer> inspection(Cal calculation, Sig sigDeviation, Collection<Item> collection, Collection<Integer> positionAttr);
	
	Posi toPosition(int[] attributeIndexes, int positionSize);
}