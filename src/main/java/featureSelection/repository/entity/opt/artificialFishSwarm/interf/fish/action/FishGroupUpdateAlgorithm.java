package featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.action;

import featureSelection.repository.entity.opt.artificialFishSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.Fish;

import java.util.Random;

/**
 * Fish group update algorithm in Artificial Fish Swarm
 * 
 * @author Benjamin_L
 *
 * @param <Posi>
 * 		Class extends {@link Position}.
 * @param <F>
 * 		Class extends {@link Fish}.
 */
public interface FishGroupUpdateAlgorithm<Posi extends Position<?>, F extends Fish<Posi>> {
	F[] generateFishGroup(
			int fishGroupNumber, int attributeLength, ReductionParameters params, Random random
	);
}
