package featureSelection.repository.entity.opt.genetic.impl.chromosome.code.mutation.binaryGene;

import featureSelection.repository.entity.opt.genetic.impl.chromosome.chromosomeEntity.binaryGeneChromosome.BinaryGeneChromosome;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.code.mutation.ChromosomeMutationParameters;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Parameters for {@link DefaultBinaryGeneMutation}:
 * <ul>
 * 	<li>chromosomeClass: <code>Class</code></li>
 * 	<li>attributes: <code>int[]</code></li>
 * 	<li>mutateSize: <code>int</code></li>
 * </ul>
 * 
 * @author Benjamin_L
 *
 * @param <Chr> implemented {@link BinaryGeneChromosome}.
 */
@Data
@AllArgsConstructor
public class DefaultBinaryGeneMutationParameters<Chr extends BinaryGeneChromosome>
	implements ChromosomeMutationParameters<Chr>
{
	private Class<Chr> chromosomeClass;
	private int mutateSize;
	private double mutateRate;
}
