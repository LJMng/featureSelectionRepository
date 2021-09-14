package featureSelection.repository.entity.opt.particleSwarm.interf.fitness;

import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;

/**
 * Fitness of the particle in Particle Swarm Algorithm.
 * 
 * @author Benjamin_L
 */
public interface Fitness<Posi extends Position<?>, FValue extends FitnessValue<?>>
	extends Cloneable 
{
	FValue getFitnessValue();
	void setFitnessValue(FValue value);
	
	Posi getPosition();
	void setPosition(Posi position);
	
	Fitness<Posi, FValue> clone();
}