package featureSelection.tester.procedure.opt.artificialFishSwarm.component.common.generationLoop.action.swarm;

import java.util.Collection;

import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.action.ComponentExecution;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.func.ArtificialFishSwarm;
import featureSelection.repository.entity.opt.artificialFishSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.fish.action.swarm.FishSwarmAlgorithm4ByteArray;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.position.ByteArrayPosition;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fish.Fish;
import lombok.Getter;

/**
 * A {@link ComponentExecution} for {@link FishSwarmAlgorithm4ByteArray} <strong>SWARM</strong> action.
 *
 * @author Benjamin_L
 * 
 * @see FishSwarmAlgorithm4ByteArray
 * 
 */
public class FishSwarmAction4ByteArray<FI extends FeatureImportance<Sig>,
										Sig extends Number, 
										CollectionItem>
	implements ComponentExecution<ByteArrayPosition>,
				FishSwarmActionInterf<ByteArrayPosition>
{
	private FI calculation;
	private Collection<CollectionItem> collectionItem;
	private Fish<ByteArrayPosition> currentFish;
	private Fish<ByteArrayPosition>[] fishGroup;
	private ReductionParameters params;

	@Getter private int calculationCount;
	
	public FishSwarmAction4ByteArray(
			FI calculation, Collection<CollectionItem> collectionItem, 
			Fish<ByteArrayPosition> currentFish, Fish<ByteArrayPosition>[] fishGroup,
			ReductionParameters params
	) {
		this.calculation = calculation;
		this.collectionItem = collectionItem;
		this.currentFish = currentFish;
		this.fishGroup = fishGroup;
		this.params = params;
		
		calculationCount = 0;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ByteArrayPosition exec(ProcedureComponent<?> component, Object...parameters) throws Exception {
		// n=0, nextPos, swarmFitness=0, swarm=null
		double fitnessValue = 0;
		// For any fish in fishGroup, loop.
		Collection<Fish<ByteArrayPosition>> withinVisual = 
				ArtificialFishSwarm.getFishWithinVisual(currentFish, fishGroup, params);
		if (withinVisual.isEmpty())	return null;
		for (Fish<ByteArrayPosition> fish: withinVisual) {
			fitnessValue +=	
				params.getFitnessAlgorthm()
					.calculateFitness(
						params.getReductionAlgorithm(), 
						fish.getPosition().getAttributes(), 
						params.getReductionAlgorithm()
							.dependency(
								calculation,
								collectionItem, 
								fish.getPosition().getAttributes()
							)
					).getFitnessValue()
					.getFitnessValue()
					.doubleValue();
		}
		// if ( (swarmFitness/ n ) < cFactor*fcurrentFish.fitness )
		double currentFishFitness = 
				params.getFitnessAlgorthm()
					.calculateFitness(
						params.getReductionAlgorithm(), 
						currentFish.getPosition().getAttributes(), 
						params.getReductionAlgorithm()
							.dependency(
								calculation, 
								collectionItem, 
								currentFish.getPosition().getAttributes()
							)
					).getFitnessValue()
					.getFitnessValue()
					.doubleValue();
		
		TimerUtils.timePause((TimeCounted) component);
		calculationCount+=(withinVisual.size()+1);
		TimerUtils.timeContinue((TimeCounted) component);

		if (fitnessValue/withinVisual.size() < params.getCFactor()*currentFishFitness) {
			// fishCenter = fishCenterCalculation(), return fishCenter.
			return (ByteArrayPosition) params.getFishCenterCalculationAlgorithm()
											.compute(withinVisual);
		// else return null.
		}else {
			return null;
		}
	}
}
