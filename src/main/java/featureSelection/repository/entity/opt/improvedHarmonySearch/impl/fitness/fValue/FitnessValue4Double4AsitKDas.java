package featureSelection.repository.entity.opt.improvedHarmonySearch.impl.fitness.fValue;

import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue4AsitKDas;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FitnessValue4Double4AsitKDas implements FitnessValue4AsitKDas<Double, Double> {
	Double value;
	Double featureImportance;
}