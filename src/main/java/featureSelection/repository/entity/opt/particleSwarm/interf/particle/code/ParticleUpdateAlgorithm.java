package featureSelection.repository.entity.opt.particleSwarm.interf.particle.code;

import java.util.Random;

import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Particle;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;

/**
 * Particle Update for Particle Swarm Optimization.
 * 
 * @author Benjamin_L
 *
 * @param <Velocity>
 * 		Type of velocity
 * @param <Posi>
 * 		Type of {@link Position}.
 * @param <FValue>
 * 		Type of {@link FitnessValue}
 */
public interface ParticleUpdateAlgorithm<Velocity, Posi extends Position<?>, FValue extends FitnessValue<?>> {
	/**
	 * Update velocity and position of particle.
	 * 
	 * @param particlePosition
	 * 		The particle position.
	 * @param particleVelocity
	 * 		Current velocity of the particle.
	 * @param w
	 * 		Inertia factor.
	 * @param xg
	 * 		The number of differences between global best position and particle position.
	 * @param params
	 * 		{@link ReductionParameters}.
	 * @param random
	 * 		{@link Random}
	 * @return Updated {@link Velocity} within the velocity limited range.
	 */
	Velocity updateVelocity(
			Posi particlePosition, Velocity particleVelocity, Double w, 
			int xg, ReductionParameters<Velocity, Posi, FValue> params,
			Random random
	);
	
	/**
	 * Update velocity and position of particle.
	 * 
	 * @param particlePosition
	 * 		The particle position.
	 * @param globalBestPosition
	 * 		The global best position.
	 * @param velocity
	 * 		The velocity of the particle
	 * @param xg
	 * 		The number of differences between global best position and particle position.
	 * @param random
	 * 		{@link Random}
	 * @return {@link Posi}
	 */
	Posi updatePosition(
			Posi particlePosition, Posi globalBestPosition, Velocity velocity, 
			int xg, Random random
	);
	
	/**
	 * Update velocity and position of particle.
	 * 
	 * @param particle
	 * 		The particle instance.
	 * @param generRecord
	 * 		{@link GenerationRecord}.
	 * @param params
	 * 		{@link ReductionParameters}
	 * @param random
	 * 		{@link Random}
	 * @return {@link Particle}
	 */
	Particle<Velocity, Posi, FValue> updateVelocityNPosition(
			Particle<Velocity, Posi, FValue> particle, GenerationRecord<Velocity, Posi, FValue> generRecord,
			ReductionParameters<Velocity, Posi, FValue> params, Random random
	);
}