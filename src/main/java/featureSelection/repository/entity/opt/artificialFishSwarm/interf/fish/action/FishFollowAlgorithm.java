package featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.action;

import java.util.Collection;

import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.artificialFishSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.Fish;

/**
 * Fish follow algorithm in Artificial Fish Swarm.
 * 
 * @author Benjamin_L
 *
 * @param <Posi>
 * 		Class extends {@link Position}.
 */
public interface FishFollowAlgorithm<FI extends FeatureImportance<Sig>,
									Sig extends Number,
									Posi extends Position<?>>
{
	<CollectionItem> Posi follow(
			FI calculation, Collection<CollectionItem> collectionItems, Fish<Posi> fish,
			Fish<Posi>[] fishGroup, ReductionParameters params
	);
}
