package featureSelection.tester.procedure.opt.artificialFishSwarm.component.common.generationLoop.action.follow;

import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.artificialFishSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.fish.action.follow.FishFollowAlgorithm4ByteArray;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.position.ByteArrayPosition;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.Fish;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness.FitnessValue;

import java.util.Collection;

public class FishFollowActionFactory {
	
	@SuppressWarnings("unchecked")
	public static <FI extends FeatureImportance<Sig>, Sig extends Number, CollectionItem,
					Posi extends Position<?>, FV extends FitnessValue<?>> FishFollowActionInterf<Posi>
		newAction(
			FI calculation, Collection<CollectionItem> collectionItem,
			Fish<Posi> currentFish, Fish<Posi>[] fishGroup,
			ReductionParameters params
	) {
		if (params.getFishFollowAlgorithm() instanceof FishFollowAlgorithm4ByteArray) {
			return (FishFollowActionInterf<Posi>) 
					new FishFollowAction4ByteArray<FI, Sig, CollectionItem, FV>(
						calculation,
						collectionItem,
						(Fish<ByteArrayPosition>) currentFish,
						(Fish<ByteArrayPosition>[]) fishGroup,
						params
					);
		}else {
			return null;
		}
	}
}
