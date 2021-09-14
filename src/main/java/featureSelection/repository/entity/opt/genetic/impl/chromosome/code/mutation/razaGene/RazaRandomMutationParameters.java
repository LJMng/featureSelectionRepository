package featureSelection.repository.entity.opt.genetic.impl.chromosome.code.mutation.razaGene;

import featureSelection.repository.entity.opt.genetic.impl.chromosome.chromosomeEntity.razaChromosome.RazaChromosome;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.code.mutation.ChromosomeMutationParameters;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Parameters for {@link NumericChromosomeRandomMutation}:
 * <ul>
 * 	<li>chromosomeClass: <code>Class</code></li>
 * 	<li>mutateSize: <code>int</code></li>
 * 	<li>mutateRate: <code>double</code></li>
 * </ul>
 * 
 * @author Benjamin_L
 *
 * @param <Chr>
 *     Type of implemented {@link RazaChromosome}.
 */
@Data
@AllArgsConstructor
public class RazaRandomMutationParameters<Chr extends RazaChromosome>
	implements ChromosomeMutationParameters<Chr>
{
	private Class<Chr> chromosomeClass;
	private int mutateSize;
	private double mutateRate;
	private int[] attributes;
}
