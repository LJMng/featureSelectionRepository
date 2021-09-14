package featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue;

import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class FitnessValue4Double implements FitnessValue<Double> {

	@Getter private Double value;
}
