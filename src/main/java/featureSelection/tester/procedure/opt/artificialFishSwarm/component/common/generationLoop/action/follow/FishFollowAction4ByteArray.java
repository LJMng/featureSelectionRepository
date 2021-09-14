package featureSelection.tester.procedure.opt.artificialFishSwarm.component.common.generationLoop.action.follow;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.action.ComponentExecution;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.func.ArtificialFishSwarm;
import featureSelection.repository.entity.opt.artificialFishSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.position.ByteArrayPosition;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.Fish;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness.FitnessValue;
import lombok.Getter;

public class FishFollowAction4ByteArray<FI extends FeatureImportance<Sig>,
										Sig extends Number, 
										CollectionItem,
										FV extends FitnessValue<? extends Number>>
	implements ComponentExecution<ByteArrayPosition>,
				FishFollowActionInterf<ByteArrayPosition>
{

	private FI calculation;
	private Collection<CollectionItem> collectionItems;
	private Fish<ByteArrayPosition> fish;
	private Fish<ByteArrayPosition>[] fishGroup;
	private ReductionParameters params;
	
	@Getter private int calculationCount;
	
	public FishFollowAction4ByteArray(
			FI calculation, Collection<CollectionItem> collectionItems,
			Fish<ByteArrayPosition> fish, Fish<ByteArrayPosition>[] fishGroup,
			ReductionParameters params
	) {
		this.calculation = calculation;
		this.collectionItems = collectionItems;
		this.fish = fish;
		this.fishGroup = fishGroup;
		this.params = params;
		
		calculationCount = 0;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ByteArrayPosition exec(
			ProcedureComponent<?> component, Object... parameters
	) throws Exception {
		Collection<Fish<ByteArrayPosition>> withinVisual;
		// Collect fishes within visual for current fish in fish group.
		withinVisual = ArtificialFishSwarm.getFishWithinVisual(fish, fishGroup, params);
		if (withinVisual.isEmpty())	return null;
		// Calculate fitnesses of fishes within visual, and find the best one.
		FV fitnessCalResult;
		Fish<ByteArrayPosition> bestFish = null;
		Map<Fish<ByteArrayPosition>, FV> fitnessBuffer = new HashMap<>(withinVisual.size());
		for (Fish<ByteArrayPosition> f: withinVisual) {
			fitnessCalResult =
					(FV) params.getFitnessAlgorthm()
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
			if (bestFish==null || fitnessCalResult.compareTo(fitnessBuffer.get(bestFish))>0) {
				bestFish = f;
			}
		}
		TimerUtils.timePause((TimeCounted) component);
		calculationCount += withinVisual.size();
		TimerUtils.timeContinue((TimeCounted) component);
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
								component, 
								calculation, 
								collectionItems, 
								fitnessBuffer,
								f,
								params
						).getFitnessValue()
						.doubleValue();
		}
		// Check and return.
		TimerUtils.timePause((TimeCounted) component);
		calculationCount++;
		TimerUtils.timeContinue((TimeCounted) component);
		double currentFitnessValue = 
				(double) params.getFitnessAlgorthm()
								.calculateFitness(
									params.getReductionAlgorithm(), 
									fish.getPosition().getAttributes(),
									params.getReductionAlgorithm().dependency(calculation, collectionItems, fish.getPosition().getAttributes())
								).getFitnessValue()
								.getFitnessValue();
		return (withinVisual.isEmpty()?0:(sumFitness/withinVisual.size())) < params.getCFactor()*currentFitnessValue ?
				bestFish.getPosition().clone(): null;
	}
	
	@SuppressWarnings("unchecked")
	protected FV calculateFitnessWithBuffer(
			ProcedureComponent<?> component,
			FI calculation, Collection<CollectionItem> collectionItems, 
			Map<Fish<ByteArrayPosition>, FV> fitnessMap, Fish<ByteArrayPosition> fish,
			ReductionParameters params
	) {
		FV fitness = fitnessMap.get(fish);
		if (fitness==null) {
			TimerUtils.timePause((TimeCounted) component);
			calculationCount++;
			TimerUtils.timeContinue((TimeCounted) component);
			
			return (FV) params.getFitnessAlgorthm()
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
