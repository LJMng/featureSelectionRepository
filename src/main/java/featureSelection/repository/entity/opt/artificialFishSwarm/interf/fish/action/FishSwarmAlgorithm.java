package featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.action;

import java.util.Collection;

import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.artificialFishSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.Fish;

/**
 * Fish swarm algorithm in Artificial Fish Swarm.
 * 
 * @author Benjamin_L
 *
 * @param <Sig>
 *      Type of feature (subset) importance.
 * @param <FI>
 *      Type of implemented feature (subset) importance calculation.
 * @param <Posi>
 * 		Class extends {@link Position}.
 */
public interface FishSwarmAlgorithm<FI extends FeatureImportance<Sig>,
									Sig extends Number, 
									Posi extends Position<?>>
{
	<CollectionItem> Posi swarm(
			FI calculation,
			Collection<CollectionItem> collectionItems, Fish<Posi> fish,
			Fish<Posi>[] fishGroup, ReductionParameters params
	);
}
