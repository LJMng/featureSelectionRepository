package featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness;

import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;

/**
 * Fitness of the fish's position in Artifical Fish Swarm Algorithm.
 * 
 * @author Benjamin_L
 *
 * @param <Posi>
 * 		Class extends {@link Position}.
 * @param <Value>
 * 		Class type of fitness's value.
 */
public interface Fitness<Posi extends Position<?>, Value extends FitnessValue<?>>{
	void setFitnessValue(Value v);
	Value getFitnessValue();

	void setPosition(Posi position);
	Posi getPosition();
}
