package featureSelection.repository.entity.opt.artificialFishSwarm.impl.distance;

import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.distance.HammingDistanceAlgorithm;

/**
 * <code>Hamming Distance</code> implementation for <code>byte[]</code> {@link Position}.
 * <p>
 * Let X; Y be binary bit strings that represent positions of two fish.
 * For binary bit stings X and Y, their Hamming distance is equal to 
 * the number of ones in X XOR Y. In information theory, the 
 * Hamming distance between two strings of equal length is the 
 * number of positions at which the corresponding symbols are different.
 * <p>
 * Example : The hamming distance between x(0,1,1,0) and y(1,0,1,0) is 2.
 * 
 * @author Benjamin_L
 */
public class HammingDistanceAlgorithm4ByteArray 
	implements HammingDistanceAlgorithm<Integer, byte[]>
{

	/**
	 * Let X; Y be binary bit strings that represent positions of two fish.
	 * For binary bit stings X and Y, their Hamming distance is equal to 
	 * the number of ones in X XOR Y. In information theory, the 
	 * Hamming distance between two strings of equal length is the 
	 * number of positions at which the corresponding symbols are different.
	 * <p>
	 * Example : The hamming distance between x(0,1,1,0) and y(1,0,1,0) is 2.
	 */
	@Override
	public Integer distance(Position<byte[]> x, Position<byte[]> y) {
		int xory = 0;
		for (int i=0; i<x.getPosition().length; i++)
			xory += x.getPosition()[i] ^ y.getPosition()[i];
		return xory;
	}
}