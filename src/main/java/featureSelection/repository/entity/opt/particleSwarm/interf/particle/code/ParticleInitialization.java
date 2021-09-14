package featureSelection.repository.entity.opt.particleSwarm.interf.particle.code;

import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Particle;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;

import java.util.Random;

/**
 * Particle initialization for Particle Swarm Optimization.
 *
 * @see ParticleInitializationParameters
 *
 * @author Benjamin_L
 *
 * @param <Velocity>
 * 		Class extends {@link Number} as values of velocity
 * @param <Posi>
 * 		Class extends {@link Position}.
 * @param <FValue>
 *      Type of implemented {@link FitnessValue}
 * @param <Param>
 *     Type of {@link ParticleInitializationParameters}.
 */
public interface ParticleInitialization<Velocity, Posi extends Position<?>, FValue extends FitnessValue<?>, Param extends ParticleInitializationParameters> {
	/**
	 * Initiate a <code>Particle</code> array.
	 * 
	 * @param <CollectionItem>
	 * 		Collection items of dataset.
	 * @param initParams
	 * 		{@link ParticleInitializationParameters} withe parameters.
	 * @param random
	 * 		{@link Random}.
	 * @return An array of {@link Particle}.
	 */
	public <CollectionItem> Particle<Velocity, Posi, FValue>[] initParticles(
			Param initParams, Random random
	);
}