package featureSelection.repository.entity.opt.improvedHarmonySearch.impl.fitness.fValue;

import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FitnessValue4Integer implements FitnessValue<Integer> {
	Integer value;
}