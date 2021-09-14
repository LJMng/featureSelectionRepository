package featureSelection.repository.entity.opt.genetic.impl.chromosome.code.initialization.binaryGene;

import featureSelection.repository.entity.opt.genetic.impl.chromosome.code.initialization.razaGene.NumericGeneChromosomeInitialization;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.code.initlization.ChromosomeInitializationParameters;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Parameters for {@link NumericGeneChromosomeInitialization}:
 * <ul>
 * 	<li>population: <code>int</code></li>
 * 	<li>geneSize: <code>int</code></li>
 * 	<li>geneRate: <code>double</code></li>
 * 	<li>chromosomeClass: <code>Class</code></li>
 * </ul>
 * 
 * @author Benjamin_L
 *
 * @param <Chr> implemented {@link Chromosome} with <code>int[]</code> gene.
 */
@Data
@AllArgsConstructor
public class DefaultBinaryGeneChromosomeInitializationParameters<Chr extends Chromosome<byte[]>>
	implements ChromosomeInitializationParameters<Chr>
{
	private int population;
	private int geneSize;
	private int[] attributes;
	private double geneRate;
	private Class<Chr> chromosomeClass;
}