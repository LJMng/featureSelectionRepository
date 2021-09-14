package featureSelection.repository.entity.opt.genetic;

import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import featureSelection.repository.entity.opt.genetic.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;

import java.util.Comparator;

/**
 * {@link Comparator} for {@link Fitness}. Fitnesses with smaller value ranked bigger. 
 */
public class ReverseFitnessComparator<Chr extends Chromosome<?>, FValue extends FitnessValue<?>>
	implements Comparator<Fitness<Chr, FValue>>
{
	@Override
	public int compare(Fitness<Chr, FValue> o1, Fitness<Chr, FValue> o2) {
		if (o1==null) {
			if (o2==null)	return 0;
			else			return 1;
		}else {
			return - o1.compareTo(o2);
		}
	}
}
