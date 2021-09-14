package featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue;

import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FitnessValue4Double implements FitnessValue<Double> {
	Double value;
}