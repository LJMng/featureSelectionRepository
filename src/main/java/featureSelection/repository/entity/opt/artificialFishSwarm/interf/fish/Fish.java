package featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish;

import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;

/**
 * Fish entity in Artificial Fish Swarm.
 * 
 * @author Benjamin_L
 *
 * @param <Posi>
 * 		Class extends {@link Position}.
 */
public interface Fish<Posi extends Position<?>> {
	void setPosition(Posi posi);
	Posi getPosition();
	
	Fish<Posi> exit();
	boolean isExited();
}