package featureSelection.repository.entity.opt.particleSwarm.impl.particle.code.update.hannah;

import java.util.Collection;
import java.util.Random;
import java.util.Set;

import common.utils.RandomUtils;
import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.particleSwarm.impl.particle.entity.position.HannahPosition;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.code.ParticleUpdateAlgorithm;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Particle;

/**
 * Implementation of {@link ParticleUpdateAlgorithm} using particle update strategies specified in   
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S0169260713003477">
 * "Supervised hybrid feature selection based on PSO and rough sets for medical diagnosis"</a> 
 * by H. Hannah. 
 * <p>Using an equation to update velocity and rules to update particle's position.
 * <p>velocity = w * p.velocity[i] + c1 * r1 * (p.best–p.pos) + c2 * r2 * (gBest.pos–p.pos)
 * 
 * @author Benjamin_L
 */
public class HannahParticleUpdateAlgorithm<FValue extends FitnessValue<?>>
	implements ParticleUpdateAlgorithm<Integer, HannahPosition, FValue>
{
	/**
	 * <pre>individualBestPosi = particle.getFitness().getPosition().encodedValues();</pre>
	 * 
	 * @see Particle#getFitness()
	 */
	private byte[] individualBestPosi;

	/**
	 * Update the velocity of particle, using the equation in Hannah's paper:
	 * <p>
	 * velocity = w * p.velocity[i] + c1 * r1 * (p.best–p.pos) + c2 * r2 * (gBest.pos–p.pos)
	 * 
	 * @see #individualBestPosi
	 */
	@Override
	public Integer updateVelocity(
			HannahPosition particlePosition, Integer particleVelocity, Double w, 
			int xg, ReductionParameters<Integer, HannahPosition, FValue> params,
			Random random
	) {
		double r1 = params.getR1(), r2 = params.getR2();
		// Auto-set: r1, r2 = random.value.
		if (r1<0)	r1 = random.nextDouble();
		if (r2<0)	r2 = random.nextDouble();
		// velocity = w*p.velocity[i] + c1 * r1 * (p.best – p.pos ) + c2 * r2 * (gBest.pos – p.pos)
		Integer velocity = 
				velocityCheck(
						params.getVelocityMin().intValue(), 
						params.getVelocityMax().intValue(), 
						(int) (w * particleVelocity + 
								params.getC1() * r1 * positionDifference(individualBestPosi, particlePosition.encodedValues()) +
								params.getC2() * r2 * xg)
				);
		return velocity;
	}
	
	@Override
	public HannahPosition updatePosition(
			HannahPosition globalBestPosition, HannahPosition particlePosition,  
			Integer velocity, int xg, Random random
	) {
		if (velocity.compareTo(xg) < 0) {
			// Change |velocity| position values which are different from the one in gBest randomly. 
			byte[] globalPosiValue = globalBestPosition.encodedValues(),
					individualPosiValue = particlePosition.encodedValues(); 
			Set<Integer> indexs = RandomUtils.randomUniqueInts(0, xg, velocity, random);
			for (int i=0, diff=-1; i<particlePosition.encodedValuesLength(); i++) {
				if (Byte.compare(globalPosiValue[i], individualPosiValue[i])!=0) {
					diff++;
					if (indexs.contains(diff))	updatePositionValue(particlePosition.encodedValues(), i);
					if (diff==xg-1)				break;
				}
			}
			particlePosition.refreshAttributes();
		}else {
			// 1. Change |velocity| position values which are different from the one in gBest randomly. 
			// 2. Change (|velocity|-xg) position values randomly.
			for (int i=0; i<particlePosition.encodedValues().length; i++)
				if (particlePosition.encodedValues()[i]!=globalBestPosition.encodedValues()[i])
					updatePositionValue(particlePosition.encodedValues(), i); 
			Set<Integer> indexs = RandomUtils.randomUniqueInts(0, particlePosition.encodedValues().length, 
									velocity - xg, random);
			updatePositionValue(particlePosition.encodedValues(), indexs);
			particlePosition.refreshAttributes();
		}
		return particlePosition;
	}
	
	/**
	 * Update {@link Particle#getVelocity()}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Particle<Integer, HannahPosition, FValue> updateVelocityNPosition(
			Particle<Integer, HannahPosition, FValue> particle, 
			GenerationRecord<Integer, HannahPosition, FValue> generRecord,
			ReductionParameters<Integer, HannahPosition, FValue> params, 
			Random random
	) {
		HannahPosition globalBestPosi = generRecord.getGlobalBestFitness().getPosition();
		// Update w.
		Double weight = (Double) params.getInertiaWeightAlgorithm()
										.updateInertiaWeight(generRecord, params);
		// xg = (gBest.pos – p.pos), and count the number of value 1 and -1 (the number of different values).
		int xg = positionDifference(globalBestPosi.encodedValues(), particle.getPosition().encodedValues());
		// update velocity
		individualBestPosi = particle.getFitness().getPosition().encodedValues();
		particle.setVelocity(
				updateVelocity(
					particle.getPosition(), particle.getVelocity(),
					weight, xg, 
					params, random
				)
		);
		// update position
		particle.setPosition(
				updatePosition(
						globalBestPosi, 
						particle.getPosition(), 
						particle.getVelocity(),
						xg, 
						random
				)
		);
		return particle;
	}
	
	
	/**
	 * Count the difference between 2 positions. For example, the difference between 
	 * [1, 1, 0] and [0, 1, 1] would be [1, 0, -1], i.e. difference = 2.
	 * 
	 * @param posi1
	 * 		A position in byte array.
	 * @param posi2
	 * 		Another position in byte array.
	 * @return the number of different value between 2 positions
	 */
	private static int positionDifference(byte[] posi1, byte[] posi2) {
		int count = 0; 	for (int i=0; i<posi1.length; i++)	if (posi1[i]!=posi2[i])	count++;
		return count;
	}
	
	/**
	 * Limit the velocity between <code>minV</code> (included) and <code>maxV</code> (included).
	 * 
	 * @param minV
	 * 		The minimum velocity value.
	 * @param maxV
	 * 		The maximum velocity value.
	 * @param velocity
	 * 		The velocity value.
	 * @return An <code>Integer</code> value as velocity
	 */
	private static Integer velocityCheck(int minV, int maxV, int velocity) {
		if (velocity < minV) {
			velocity = minV;
			return velocity;
		}else if (velocity > maxV) {
			velocity = maxV;
			return velocity;
		}else {
			return velocity;
		}
	}
	
	/**
	 * Update the position at the given index.
	 * 
	 * @param position
	 * 		A byte array as position.
	 * @param index
	 * 		The index of the position to be updated.
	 * @return The updated position in byte array.
	 */
	private static byte[] updatePositionValue(byte[] position, int index) {
		position[index] = position[index]==(byte) 0? (byte) 1:(byte) 0;
		return position;
	}
	
	/**
	 * Update the position at the given index.
	 * 
	 * @param position
	 * 		A byte array as position.
	 * @param index
	 * 		A collection of indexes of the position to be updated.
	 * @return The updated position in byte array.
	 */
	private static byte[] updatePositionValue(byte[] position, Collection<Integer> index) {
		for (int i : index)	position[i] = (position[i]==(byte) 0? (byte) 1:(byte) 0);
		return position;
	}
}