package featureSelection.repository.entity.opt.particleSwarm.interf.particle.code;

/**
 * Interface for {@link ParticleInitialization}'s parameters.
 *
 * @see ParticleInitialization
 */
public interface ParticleInitializationParameters{
	int getParticleLength();
	void setParticleLength(int len);
}