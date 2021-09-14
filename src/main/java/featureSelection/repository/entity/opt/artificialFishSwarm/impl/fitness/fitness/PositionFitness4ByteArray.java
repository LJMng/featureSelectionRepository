package featureSelection.repository.entity.opt.artificialFishSwarm.impl.fitness.fitness;

import featureSelection.repository.entity.opt.artificialFishSwarm.impl.position.ByteArrayPosition;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness.FitnessValue;
import lombok.Data;

@Data
public class PositionFitness4ByteArray<FV extends FitnessValue<?>>
	implements Fitness<ByteArrayPosition, FV>
{
	private FV fitnessValue;
	private ByteArrayPosition position;
	
	public PositionFitness4ByteArray(FV fitnessValue) {
		this.fitnessValue = fitnessValue;
	}

}