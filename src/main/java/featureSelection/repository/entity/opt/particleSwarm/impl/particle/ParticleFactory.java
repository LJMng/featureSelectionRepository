package featureSelection.repository.entity.opt.particleSwarm.impl.particle;

import featureSelection.repository.entity.opt.particleSwarm.impl.particle.entity.particle.HannahParticle;

public class ParticleFactory {
	@SuppressWarnings("rawtypes")
	public static HannahParticle newHannahParticle(Integer velocity, byte[] positionBits) {
		return new HannahParticle(velocity, positionBits);
	}
}
