package featureSelection.repository.entity.opt.genetic.impl.chromosome.code.initialization.razaGene;

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
 * 			Class.</li>
 * 	<li>Loop each {@link Chromosome} and use {@link RandomUtils#probability(double, Random)} to
 * 			set genes' value (value = -1 or attribute index)</li>
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
public class NumericGeneChromosomeInitialization<Chr extends Chromosome<int[]>>
	implements ChromosomeInitialization<Chr, NumericGeneChromosomeInitializationParameters<Chr>>
{
	private NumericGeneChromosomeInitializationParameters<Chr> parameters;
	
	@Override
	public Chr[] initChromosomes(Random random) {
		int[] gene;
		int[] attributes = parameters.getAttributes();
		@SuppressWarnings("unchecked")
		Chr[] chromosome = (Chr[]) Array.newInstance(
										parameters.getChromosomeClass(), 
										parameters.getPopulation()
							);
		for (int c=0; c<parameters.getPopulation(); c++) {
			gene = new int[parameters.getGeneSize()];
			for (int g=0; g<parameters.getGeneSize(); g++) {
				gene[g] = RandomUtils.probability(parameters.getGeneRate(), random)?
							attributes[g % attributes.length]: -1;
			}
			chromosome[c] = ChromosomeFactory.getChromosome(gene, parameters.getChromosomeClass());
		}
		return chromosome;
	}
}