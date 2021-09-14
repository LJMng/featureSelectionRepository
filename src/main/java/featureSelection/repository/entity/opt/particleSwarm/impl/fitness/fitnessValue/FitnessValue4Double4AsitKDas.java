package featureSelection.repository.entity.opt.particleSwarm.impl.fitness.fitnessValue;

import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue4AsitKDas;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FitnessValue4Double4AsitKDas implements FitnessValue4AsitKDas<Double, Double> {
	Double value;
	Double featureImportance;
}