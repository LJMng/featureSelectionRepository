package featureSelection.repository.entity.opt.genetic.impl.fitness.fitnessValue;

import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FitnessValue4Double4AsitKDas<Sig extends Number> implements FitnessValue<Double> {
	private Double value;
	private Sig featureSignificance;
}