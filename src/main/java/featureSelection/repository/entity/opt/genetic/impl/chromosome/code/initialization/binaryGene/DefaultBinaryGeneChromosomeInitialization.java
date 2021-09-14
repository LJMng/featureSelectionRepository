package featureSelection.repository.entity.opt.genetic.impl.chromosome.code.initialization.binaryGene;

import java.lang.reflect.Array;
import java.util.Random;

import common.utils.RandomUtils;
import featureSelection.repository.entity.opt.genetic.impl.chromosome.ChromosomeFactory;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.code.initlization.ChromosomeInitialization;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Numeric gene {@link Chromosome} initialization. Initialize by the following steps:
 * <ul>
 * 	<li>Initiate a {@link Chromosome} array with the size of populatin and {@link Chromosome}
 * 		Class.</li>
 * 	<li>Loop each {@link Chromosome} and use {@link RandomUtils#probability(double, Random)} to
 * 		set genes' value (value = -1 or 1)</li>
 * 	<li>Return {@link Chromosome} array</li>
 * </ul>
 * 
 * @author Benjamin_L
 *
 * @param <Chr>
 * 		Type of implemented {@link Chromosome}.
 */
@Data
@AllArgsConstructor
public class DefaultBinaryGeneChromosomeInitialization<Chr extends Chromosome<byte[]>>
	implements ChromosomeInitialization<Chr, DefaultBinaryGeneChromosomeInitializationParameters<Chr>>
{
	private DefaultBinaryGeneChromosomeInitializationParameters<Chr> parameters;
	
	@Override
	public Chr[] initChromosomes(Random random) {
		byte[] gene;
		int[] attributes = parameters.getAttributes();
		@SuppressWarnings("unchecked")
		Chr[] chromosome = (Chr[]) Array.newInstance(
										parameters.getChromosomeClass(), 
										parameters.getPopulation()
							);
		for (int c=0; c<parameters.getPopulation(); c++) {
			gene = new byte[parameters.getGeneSize()];
			for (int g=0; g<parameters.getGeneSize(); g++) {
				if (RandomUtils.probability(parameters.getGeneRate(), random))
					gene[attributes[g]-1] = (byte) 1;
			}
			chromosome[c] = ChromosomeFactory.getChromosome(gene, parameters.getChromosomeClass());
		}
		return chromosome;
	}
}