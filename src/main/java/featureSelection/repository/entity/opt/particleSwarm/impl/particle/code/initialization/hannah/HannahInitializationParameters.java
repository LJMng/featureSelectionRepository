package featureSelection.repository.entity.opt.particleSwarm.impl.particle.code.initialization.hannah;

import featureSelection.repository.entity.opt.particleSwarm.interf.particle.code.ParticleInitializationParameters;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HannahInitializationParameters<Velocity>
	implements ParticleInitializationParameters
{
	private int population;
	private int particleLength;
	private Velocity minVelocity, maxVelocity;
	private double initAttributeRate;
}