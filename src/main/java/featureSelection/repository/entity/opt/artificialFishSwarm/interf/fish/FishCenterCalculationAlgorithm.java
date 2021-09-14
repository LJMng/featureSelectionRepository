package featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish;

import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;

import java.util.Collection;

/**
 * Fish group center calculation algorithm in ArtificialFishSwarm.
 * 
 * @author Benjamin_L
 *
 * @param <PosiValue>
 * 		Class type of {@link Position} value.
 */
public interface FishCenterCalculationAlgorithm<PosiValue> {
	<Posi extends Position<PosiValue>> Posi compute(@SuppressWarnings("unchecked") Fish<Posi>...fishNeighbours);
	<Posi extends Position<PosiValue>> Posi compute(Collection<Fish<Posi>> fishNeighbours);
}