package featureSelection.repository.entity.opt.artificialFishSwarm.impl.fish.centerCalculation;

import java.util.Collection;

import featureSelection.repository.entity.opt.artificialFishSwarm.impl.position.ByteArrayPosition;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.Fish;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.FishCenterCalculationAlgorithm;
import lombok.Getter;
import lombok.Setter;

/**
 * <code>byte[]</code> {@link Position} based Fish Center Calculation Algorithm.
 * <p>
 * PS: Must call <code>setPositionLength()</code> to set the Particle length before any
 * computation.
 * 
 * @author Benjamin_L
 */
public class PositionFishCenterCalculationAlgorithm4ByteArray 
	implements FishCenterCalculationAlgorithm<byte[]>
{
	@Getter @Setter private int positionLength;
	
	@SuppressWarnings("unchecked")
	@Override
	public <Posi extends Position<byte[]>> Posi compute(Fish<Posi>...fishNeighbours) {
		//return calculate(fishNeighbours);
		return calculateImproved(fishNeighbours);
	}

	public <Posi extends Position<byte[]>> Posi compute(Collection<Fish<Posi>> fishNeighbours) {
		//return calculate(fishNeighbours);
		return calculateImproved(fishNeighbours);
	}

	@SuppressWarnings({ "unused", "unchecked" })
	private <Posi extends Position<byte[]>> Posi calculate(Fish<Posi>...fishNeighbours) {
		if (positionLength<=0)
			throw new IllegalStateException("Unset value : positionLength="+positionLength);
		// center = 0, center is a vector
		int[] center = new int[positionLength];
		// for any fish in fishNeighbours
		for (Fish<Posi> fish : fishNeighbours) {
			// sum vector.
			for (int i=0; i<positionLength; i++)	center[i] += fish.getPosition().getPosition()[i];
		}
		// center = center / |fishNeighbours|
		// for any pos in center, if pos>0.5, pos=1;
		byte[] posi;
		ByteArrayPosition position = new ByteArrayPosition(posi=new byte[positionLength]);
		for (int i=0; i<positionLength; i++) {
			if (Double.compare(center[i] / (double) fishNeighbours.length, 0.5)>0)
				posi[i] = (byte) 1;
		}
		return (Posi) position;
	}

	@SuppressWarnings("unchecked")
	private <Posi extends Position<byte[]>> Posi calculateImproved(Fish<Posi>...fishNeighbours) {
		if (positionLength<=0)	throw new RuntimeException("Unset value : positionLength="+positionLength);

		// 1 center = 0, center is a vector
		int center;
		double threadshold = fishNeighbours.length * 0.5;
		// 2 for any fish in fishNeighbours
		byte[] posi;
		ByteArrayPosition position = new ByteArrayPosition(posi=new byte[positionLength]);
		for (int i=0; i<positionLength; i++) {
			center = 0;
			// sum vector.
			for (Fish<Posi> fish: fishNeighbours) {
				center += fish.getPosition().getPosition()[i];
				if (center>threadshold) {
					posi[i] = (byte) 1;
					break;
				}
			}
			// 3 center = center / |fishNeighbours|
			// 4 for any pos in center, if pos>0.5, pos=1;
		}
		return (Posi) position;
	}

	@SuppressWarnings({ "unused", "unchecked" })
	private <Posi extends Position<byte[]>> Posi calculate(Collection<Fish<Posi>> fishNeighbours) {
		if (positionLength<=0)	throw new RuntimeException("Unset value : positionLength="+positionLength);
		
		// 1 center = 0, center is a vector
		int[] center = new int[positionLength];
		// 2 for any fish in fishNeighbours
		for (Fish<Posi> fish : fishNeighbours) {
			// sum vector.
			for (int i=0; i<positionLength; i++)	center[i] += fish.getPosition().getPosition()[i];
		}
		// 3 center = center / |fishNeighbours|
		// 4 for any pos in center, if pos>0.5, pos=1;
		byte[] posi;
		double fishNeighboursSize = fishNeighbours.size();
		ByteArrayPosition position = new ByteArrayPosition(posi=new byte[positionLength]);
		for (int i=0; i<positionLength; i++) {
			if (Double.compare(center[i] / fishNeighboursSize , 0.5)>0)
				posi[i] = (byte) 1;
		}
		return (Posi) position;
	}

	@SuppressWarnings("unchecked")
	private <Posi extends Position<byte[]>> Posi calculateImproved(Collection<Fish<Posi>> fishNeighbours) {
		if (positionLength<=0)	throw new RuntimeException("Unset value : positionLength="+positionLength);

		// 1 center = 0, center is a vector
		int center;
		double threadshold = fishNeighbours.size() * 0.5;
		// 2 for any fish in fishNeighbours
		byte[] posi;
		ByteArrayPosition position = new ByteArrayPosition(posi=new byte[positionLength]);
		for (int i=0; i<positionLength; i++) {
			center = 0;
			// sum vector.
			for (Fish<Posi> fish: fishNeighbours) {
				center += fish.getPosition().getPosition()[i];
				if (center>threadshold) {
					posi[i] = (byte) 1;
					break;
				}
			}
			// 3 center = center / |fishNeighbours|
			// 4 for any pos in center, if pos>0.5, pos=1;
		}
		return (Posi) position;
	}
}