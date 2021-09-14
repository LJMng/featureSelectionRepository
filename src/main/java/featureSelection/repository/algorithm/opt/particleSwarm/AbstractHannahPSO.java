package featureSelection.repository.algorithm.opt.particleSwarm;

import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.impl.fitness.HannahFitness;
import featureSelection.repository.entity.opt.particleSwarm.impl.particle.entity.particle.HannahParticle;
import featureSelection.repository.entity.opt.particleSwarm.impl.particle.entity.position.HannahPosition;
import featureSelection.repository.entity.opt.particleSwarm.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Particle;

import java.util.Collection;

/**
 * Implementation of Particle Swarm Algorithm Attribute Reduction algorithm. Based on the paper
 * <a href="http://link.springer.com/article/10.1007/s00521-015-1840-0">"A novel hybrid feature
 * selection method based on rough set and improved harmony search"</a> by H.Hannah Inbarani,
 * M.Bagyamathi.
 * 
 * @author Benjamin_L
 */
public abstract class AbstractHannahPSO<Item, Velocity, FValue extends FitnessValue<?>, Cal extends FeatureImportance<Sig>, Sig extends Number>
	implements ReductionAlgorithm<Item, Velocity, HannahPosition, FValue, Cal, Sig>
{
	@Override
	public abstract FValue fitnessValue(Cal calculation, Collection<Item> collection, int[] attributes);
	
	/**
	 * If |<code>attributeIndexes</code>|=|<code>attributesSrc</code>|, it is considered that all values in
	 * <code>attributesSrc</code> are used in fitness calculations. Otherwise, only values in 
	 * <code>attributesSrc</code> with the given indexes in <code>attributeIndexes</code> are used in 
	 * fitness calculations.
	 *
	 * @see #fitnessValue(FeatureImportance, Collection, int[])
	 * @see #toPosition(int[], int)
	 */
	@Override
	public HannahFitness<FValue> fitness(
			Cal calculation, Collection<Item> collection, int[] attributesSrc,
			int[] attributeIndexes
	) {
		int[] attrValues;
		if (attributesSrc.length==attributeIndexes.length) {
			attrValues = attributesSrc;
		}else {
			attrValues = new int[attributeIndexes.length];
			for (int i=0; i<attrValues.length; i++)
				attrValues[i] = attributesSrc[attributeIndexes[i]];
		}
		return new HannahFitness<>(
				fitnessValue(calculation, collection, attrValues),
				toPosition(attributeIndexes, attributesSrc.length)
			);
	}
	
	/**
	 * Loop over the given <code>particle</code>s and calculate their fitnesses respectively.
	 *
	 * @see #fitness(FeatureImportance, Collection, int[], int[])
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public Fitness<HannahPosition, FValue>[] fitness(
			Cal calculation, Collection<Item> collection, int[] attributesSrc, 
			Particle<Velocity, HannahPosition, FValue>...particle
	) {
		@SuppressWarnings("rawtypes")
		HannahFitness[] fitness = new HannahFitness[particle.length];
		for (int i=0; i<fitness.length; i++) {
			fitness[i] = fitness(calculation, 
								collection, 
								attributesSrc,
								particle[i].getPosition().getAttributes()
						);
		}
		return fitness;
	}

	@Override
	public int compareMaxFitness(FValue maxFitnessValue, FValue fitnessValue) {
		return Double.compare(
				maxFitnessValue==null? 0: maxFitnessValue.getValue().doubleValue(), 
				fitnessValue==null? 0: fitnessValue.getValue().doubleValue()
			);
	}
	@Override
	public int compareMaxFitness(
		FValue maxFitnessValue, GenerationRecord<Velocity, HannahPosition, FValue> generRecord
	) {
		return Double.compare(
				maxFitnessValue==null? 0: maxFitnessValue.getValue().doubleValue(), 
				generRecord.globalBestFitnessValue().doubleValue()
			);
	}
	@Override
	public int compareFitness(Fitness<HannahPosition, FValue> fitness, Fitness<HannahPosition, FValue> particleBestFitness) {
		return Double.compare(
				fitness==null?
						0: fitness.getFitnessValue().getValue().doubleValue(),
				particleBestFitness==null?
						0: particleBestFitness.getFitnessValue().getValue().doubleValue()
			);
	}
	
	/**
	 * Generate a new {@link HannahPosition} instance with the given <code>attributeIndexes
	 * </code> and <code>positionSize</code> as basics of {@link byte[]} position.
	 */
	@Override
	public HannahPosition toPosition(int[] attributeIndexes, int positionSize) {
		return new HannahPosition(attributeIndexes, positionSize);
	}
	/**
	 * Use the given <code>positionCoding</code> to generate a new {@link HannahPosition}
	 * instance.
	 * 
	 * @param positionCoding
	 * 		Position coding in {@link byte[]}.
	 * @return new {@link HannahPosition} instance.
	 */
	public HannahPosition toPosition(byte[] positionCoding) {
		return new HannahPosition(positionCoding);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Particle> getParticleClass() {
		return HannahParticle.class;
	}
}