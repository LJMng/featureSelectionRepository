package featureSelection.repository.entity.opt.improvedHarmonySearch.impl.fitness;

import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DefaultFitness<Sig extends Number, FValue extends FitnessValue<Sig>>
		implements Fitness<Sig, FValue>
{
	private FValue fitnessValue;

	@Override
	public int compareToFitness(Fitness<Sig, FValue> fitness) {
		return fitness==null? 
				1: 
				Double.compare(
					fitnessValue.getValue().doubleValue(), 
					fitness.getFitnessValue().getValue().doubleValue()
				);
	}
}
