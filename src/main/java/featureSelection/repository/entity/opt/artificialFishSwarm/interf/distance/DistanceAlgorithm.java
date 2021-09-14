package featureSelection.repository.entity.opt.artificialFishSwarm.interf.distance;

import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;

/**
 * Distance calculation algorithm
 * 
 * @author Benjamin_L
 *
 * @param <Distance>
 * 		Class extends {@link Number}.
 * @param <Posi>
 * 		Class extends {@link Position}
 */
public interface DistanceAlgorithm<Distance extends Number, Posi> {
	
	/**
	 * Get the value of the distance.
	 * 
	 * @param x
	 * 		A {@link Position}.
	 * @param y
	 * 		Another {@link Position}.
	 * @return {@link Distance} measures the distance.
	 */
	Distance distance(Position<Posi> x, Position<Posi> y);
}