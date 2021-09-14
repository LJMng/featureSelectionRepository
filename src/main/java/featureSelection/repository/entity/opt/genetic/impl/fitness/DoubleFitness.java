package featureSelection.repository.entity.opt.genetic.impl.fitness;

import java.util.Arrays;

import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Double value {@link Fitness}. 
 * <p>
 * {@link #clone()}, {@link #equals(Object)}, {@link #hashCode()} overrided.
 * 
 * @author Benjamin_L
 *
 * @param <Chr>
 * 		Type of implemented {@link Chromosome}.
 */
@Slf4j
@Getter
@AllArgsConstructor
@SuppressWarnings("rawtypes")
public class DoubleFitness<Chr extends Chromosome>
	implements Fitness<Chr, FitnessValue<Double>>
{
	private FitnessValue<Double> fitnessValue;
	@Setter private Chr chromosome;

	@Override
	public int compareTo(Fitness<Chr, FitnessValue<Double>> o) {
		return o!=null ? 
				Double.compare(fitnessValue==null?0: fitnessValue.getValue(), o.getFitnessValue().getValue()) : 1;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(chromosome.getAttributes());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DoubleFitness) {
			DoubleFitness doubleFitness = (DoubleFitness) obj;
			return Double.compare(doubleFitness==null? 
						0.0: 
						doubleFitness.fitnessValue.getValue().doubleValue(), this.fitnessValue.getValue())==0 && 
					Arrays.equals(doubleFitness.chromosome.getAttributes(), chromosome.getAttributes());
		}else {
			return false;
		}
	}

	@Override
	public String toString() {
		return String.format("DoubleFitness [fitness=%8.4f, chromosome=%s]", 
				fitnessValue==null? 0.0: fitnessValue.getValue().doubleValue(), 
				chromosome
			);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public DoubleFitness<Chr> clone() {
		DoubleFitness<Chr> clone = null;
		try {
			clone = (DoubleFitness<Chr>)super.clone();
			clone.setChromosome((Chr) this.chromosome.clone());
		} catch (CloneNotSupportedException e) {
			log.error("", e);
		}
		return (DoubleFitness<Chr>) clone;
	}

}
