package featureSelection.repository.entity.opt.particleSwarm.impl.fitness;

import featureSelection.repository.entity.opt.particleSwarm.impl.particle.entity.position.HannahPosition;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HannahFitness<FValue extends FitnessValue<?>>
	implements Fitness<HannahPosition, FValue>
{
	private FValue fitnessValue;
	private HannahPosition position;
	
	@Override
	public HannahFitness<FValue> clone() {
		return new HannahFitness<>(fitnessValue, position.clone());
	}
}