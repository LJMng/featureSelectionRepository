package featureSelection.repository.entity.opt.particleSwarm.impl.particle.code.initialization.hannah;

import java.util.Random;

import common.utils.RandomUtils;
import featureSelection.repository.entity.opt.particleSwarm.impl.particle.ParticleFactory;
import featureSelection.repository.entity.opt.particleSwarm.impl.particle.entity.particle.HannahParticle;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.code.ParticleInitialization;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Particle;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;

/**
 * H. Hannah Inbarania's Particle initialization implementation. Check out the original paper 
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S0169260713003477">
 * "Supervised hybrid feature selection based on PSO and rough sets for medical diagnosis"
 * </a>
 * <p>
 * This initialization initiates {@link HannahParticle}s with velocity set using a random
 * {@link int} value whose range is set in {@link HannahInitializationParameters#getMinVelocity()}
 * and {@link HannahInitializationParameters#getMaxVelocity()} as well as {@link byte[]}
 * {@link Position} set.
 * <p>
 * <strong>Notice: </strong>
 * However, <strong>fitnesses</strong> of {@link Particle}s are not set in this initialization.
 * 
 * @author Benjamin_L
 *
 * @param <FValue>
 * 		Type of {@link FitnessValue}.
 */
public class HannahParticleInitialization<FValue extends FitnessValue<?>>
	implements ParticleInitialization<Integer, Position<byte[]>, FValue, HannahInitializationParameters<Integer>>
{
	@SuppressWarnings("unchecked")
	@Override
	public <CollectionItem> Particle<Integer, Position<byte[]>, FValue>[] initParticles(
			HannahInitializationParameters<Integer> params, Random random
	) {
		int velocity;
		byte[] position;
		Particle<Integer, Position<byte[]>, FValue>[] particles = new Particle[params.getPopulation()];
		for (int i=0; i<particles.length; i++) {
			// initiate velocity = random().int
			velocity = RandomUtils.randomUniqueInt(
							params.getMinVelocity(), 
							params.getMaxVelocity()-params.getMinVelocity()+1, 
							random
						);
			// initiate position = [random().byte, ...]
			position = new byte[params.getParticleLength()];
			for (int p=0; p<position.length; p++)
				position[p] = (byte) (RandomUtils.probability(params.getInitAttributeRate(), random)? 1 : 0);
			// initiate particle
			particles[i] = ParticleFactory.newHannahParticle(velocity, position);
		}
		return particles;
	}
}