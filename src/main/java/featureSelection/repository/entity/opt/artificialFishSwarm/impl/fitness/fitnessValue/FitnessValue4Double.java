package featureSelection.repository.entity.opt.artificialFishSwarm.impl.fitness.fitnessValue;

import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness.FitnessValue;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FitnessValue4Double 
	implements FitnessValue<Double>
{
	private Double fitnessValue;
	private Number dependency;
	
	@Override
	public int compareTo(FitnessValue<? extends Number> fv) {
		Double d1 = fitnessValue==null?0: fitnessValue;
		Double d2 = fv==null? 0: fv.getFitnessValue()==null? 0: fv.getFitnessValue().doubleValue();
		return Double.compare(d1, d2);
	}
}
