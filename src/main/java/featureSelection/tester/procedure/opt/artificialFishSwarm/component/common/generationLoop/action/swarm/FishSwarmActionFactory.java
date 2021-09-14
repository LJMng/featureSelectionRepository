package featureSelection.tester.procedure.opt.artificialFishSwarm.component.common.generationLoop.action.swarm;

import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.artificialFishSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.fish.action.swarm.FishSwarmAlgorithm4ByteArray;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.position.ByteArrayPosition;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.Fish;

import java.util.Collection;

public class FishSwarmActionFactory {
	
	@SuppressWarnings("unchecked")
	public static <FI extends FeatureImportance<Sig>, Sig extends Number, CollectionItem,
					Posi extends Position<?>> FishSwarmActionInterf<Posi>
		newAction(
			FI calculation, Collection<CollectionItem> collectionItem,
			Fish<Posi> currentFish, Fish<Posi>[] fishGroup,
			ReductionParameters params
	) {
		if (params.getFishSwarmAlgorithm() instanceof FishSwarmAlgorithm4ByteArray) {
			return (FishSwarmActionInterf<Posi>) 
					new FishSwarmAction4ByteArray<FI, Sig, CollectionItem>(
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
