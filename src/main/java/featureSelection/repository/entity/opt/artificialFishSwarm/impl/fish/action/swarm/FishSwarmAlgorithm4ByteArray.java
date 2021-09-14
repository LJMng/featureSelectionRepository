package featureSelection.repository.entity.opt.artificialFishSwarm.impl.fish.action.swarm;

import java.util.Collection;

import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.func.ArtificialFishSwarm;
import featureSelection.repository.entity.opt.artificialFishSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.position.ByteArrayPosition;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.Fish;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.action.FishSwarmAlgorithm;

/**
 * <code>byte[]</code> {@link Position} based <code>Fish Swarm Algorithm</code> <strong>SWARM</strong> 
 * action.
 * 
 * @author Benjamin_L
 *
 * @param <FI>
 * 		Implemented {@link FeatureImportance}.
 * @param <Sig>
 * 		Implemented {@link Number}. The type of feature importance.
 */
public class FishSwarmAlgorithm4ByteArray<FI extends FeatureImportance<Sig>,
										Sig extends Number>
	implements FishSwarmAlgorithm<FI, Sig, ByteArrayPosition>
{
	@SuppressWarnings("unchecked")
	@Override
	public <CollectionItem> ByteArrayPosition swarm(
			FI calculation,
			Collection<CollectionItem> collectionList,
			Fish<ByteArrayPosition> currentFish, Fish<ByteArrayPosition>[] fishGroup,
			ReductionParameters params
	) {
		Collection<Fish<ByteArrayPosition>> withinVisual = 
				ArtificialFishSwarm.getFishWithinVisual(currentFish, fishGroup, params);
		if (withinVisual.isEmpty())	return null;
		double fitnessValue = 0;
		for (Fish<ByteArrayPosition> fish: withinVisual) {

			fitnessValue += params.getFitnessAlgorthm()
									.calculateFitness(
										params.getReductionAlgorithm(), 
										fish.getPosition().getAttributes(), 
										params.getReductionAlgorithm()
											.dependency(
												calculation, 
												collectionList,
												fish.getPosition().getAttributes()
											)
									).getFitnessValue()
									.getFitnessValue()
									.doubleValue();
		}
		// 3 if ( (swarmFitness/ n ) < cFactor*fcurrentFish.fitness )
		double currentFishFitness = params.getFitnessAlgorthm()
										.calculateFitness(
												params.getReductionAlgorithm(), 
												currentFish.getPosition().getAttributes(),
												params.getReductionAlgorithm()
													.dependency(
														calculation, 
														collectionList, 
														currentFish.getPosition().getAttributes()
													)
										).getFitnessValue()
										.getFitnessValue()
										.doubleValue();
		if (fitnessValue/withinVisual.size() < params.getCFactor()*currentFishFitness) {
			// fishCenter = fishCenterCalculation(), return fishCenter.
			return (ByteArrayPosition) params.getFishCenterCalculationAlgorithm()
											.compute(withinVisual);
		// 4 else return null.
		}else {
			return null;
		}
	}
}
