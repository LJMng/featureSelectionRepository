package featureSelection.repository.entity.opt.artificialFishSwarm.impl.fish.action.follow;

import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.func.ArtificialFishSwarm;
import featureSelection.repository.entity.opt.artificialFishSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.position.ByteArrayPosition;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.Fish;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.action.FishFollowAlgorithm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness.FitnessValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link FishFollowAlgorithm} for {@link ByteArrayPosition}.
 * 
 * @author Benjamin_L
 *
 * @param <FI>
 * 		Implemented {@link FeatureImportance}.
 * @param <Sig>
 * 		Implemented {@link Number}.
 */
public class FishFollowAlgorithm4ByteArray<FI extends FeatureImportance<Sig>,
											Sig extends Number>
	implements FishFollowAlgorithm<FI, Sig, ByteArrayPosition>
{
	/**
	 * Follow action in {@link ArtificialFishSwarm} with the following steps:
	 * <p> 1. Collect fish within visual for current fish in fish group.
	 * <p> *. If no fish within visual, return null, else proceed.
	 * <p> 2. Count fish within visual of the fish with best fitness, and sum their fitnesses..
	 * <p> 3. Check and return.
	 * 
	 * @param calculation
	 * 		Implemented {@link FeatureImportance} instance.
	 * @param collectionItems
	 * 		Universe or Equivalent Class {@link Collection}.
	 * @param fish
	 * 		The current {@link Fish} to follow others.
	 * @param fishGroup
	 * 		{@link Fish} Group to be followed.
	 * @param params
	 * 		{@link ReductionParameters}.
	 * @return <code>null</code> if no fish within visual. / {@link ByteArrayPosition} to follow.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <CollectionItem> ByteArrayPosition follow(
			FI calculation,
			Collection<CollectionItem> collectionItems, Fish<ByteArrayPosition> fish,
			Fish<ByteArrayPosition>[] fishGroup, ReductionParameters params
	) {
		Collection<Fish<ByteArrayPosition>> withinVisual;
		// Collect fishes within visual for current fish in fish group.
		withinVisual = ArtificialFishSwarm.getFishWithinVisual(
							fish, 
							fishGroup, 
							params
						);
		if (withinVisual.isEmpty())	return null;
		// Calculate fitnesses of fishes within visual, and find the best one.
		FitnessValue<? extends Number> fitnessCalResult;
		Fish<ByteArrayPosition> bestFish = null;
		Map<Fish<ByteArrayPosition>, FitnessValue<?>> fitnessBuffer = new HashMap<>(withinVisual.size());
		for (Fish<ByteArrayPosition> f: withinVisual) {
			fitnessCalResult = 
					params.getFitnessAlgorthm()
						.calculateFitness(
							params.getReductionAlgorithm(),
							f.getPosition().getAttributes(),
							params.getReductionAlgorithm()
								.dependency(
									calculation, 
									collectionItems, 
									f.getPosition().getAttributes()
								)
						).getFitnessValue();
			fitnessBuffer.put(f, fitnessCalResult);
			if (bestFish==null || fitnessCalResult.compareTo(fitnessBuffer.get(bestFish))>0)
				bestFish = f;
		}
		// Clean JVM
		withinVisual = null;
		// Count fishes within visual of the fish with best fitness, and sum their fitnesses.
		withinVisual = ArtificialFishSwarm.getFishWithinVisual(
							bestFish, 
							fishGroup, 
							params
						);
		double sumFitness=0;
		for (Fish<ByteArrayPosition> f: withinVisual) {
			sumFitness += calculateFitnessWithBuffer(
								calculation, 
								collectionItems, 
								fitnessBuffer,
								f,
								params
						).getFitnessValue()
						.doubleValue();
		}
		// Check and return.
		double currentFitnessValue = 
				params.getFitnessAlgorthm()
					.calculateFitness(
						params.getReductionAlgorithm(), 
						fish.getPosition().getAttributes(),
						params.getReductionAlgorithm()
							.dependency(
								calculation, 
								collectionItems, 
								fish.getPosition().getAttributes()
							)
					).getFitnessValue()
					.getFitnessValue()
					.doubleValue();
		return (withinVisual.isEmpty()?0:sumFitness/withinVisual.size()) < params.getCFactor()*currentFitnessValue ?
				bestFish.getPosition().clone(): null;
	}
	
	@SuppressWarnings("unchecked")
	protected <CollectionItem> FitnessValue<? extends Number> calculateFitnessWithBuffer(
			FI calculation, Collection<CollectionItem> collectionItems, 
			Map<Fish<ByteArrayPosition>, FitnessValue<? extends Number>> fitnessMap, 
			Fish<ByteArrayPosition> fish, ReductionParameters params
	) {
		FitnessValue<? extends Number> fitness = fitnessMap.get(fish);
		if (fitness==null) {
			return params.getFitnessAlgorthm()
						.calculateFitness(
								params.getReductionAlgorithm(),
								fish.getPosition().getAttributes(),
								params.getReductionAlgorithm()
									.dependency(
										calculation,
										collectionItems,
										fish.getPosition().getAttributes()
									)
						).getFitnessValue();
		}else {
			return fitness;
		}
	}
}