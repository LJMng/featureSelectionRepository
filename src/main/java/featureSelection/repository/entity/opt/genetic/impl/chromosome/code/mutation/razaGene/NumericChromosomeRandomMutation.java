package featureSelection.repository.entity.opt.genetic.impl.chromosome.code.mutation.razaGene;

import java.util.Random;
import java.util.Set;

import common.utils.RandomUtils;
import featureSelection.repository.entity.opt.genetic.impl.chromosome.chromosomeEntity.razaChromosome.RazaChromosome;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.code.mutation.ChromosomeMutation;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Mutation for {@link Chromosome}, using randomly mutate strategy. Mutation contains following
 * steps:
 * <ul>
 * 	<li>Generate a set of mutate indexes for genes, using
 * 		{@link RandomUtils#randomUniqueInts(int, int, int, Random)}.</li>
 * 	<li>For each index, if {@link RandomUtils#probability(double, Random)}>
 * 	    <code>parameters.getMutateRate()</code>,
 * 		mutate gene: if {@link RandomUtils#randomUniqueInt(int, int, Random)}==0, gene[i]=-1,
 * 		else gene[i]=random.
 * 	</li>
 * 	<li>{@link Chr#update(int[])} to update {@link Chromosome} attributes.</li>
 * </ul>
 * 
 * @author Benjamin_L
 *
 * @param <Chr>
 *     Type of implemented Chromosome.
 */
@Data
@AllArgsConstructor
public class NumericChromosomeRandomMutation<Chr extends RazaChromosome>
	implements ChromosomeMutation<Chr, RazaRandomMutationParameters<Chr>>
{
	private RazaRandomMutationParameters<Chr> parameters;

	@Override
	public void mutate(Chr chromosome, Random random) {
		int[] attributes = parameters.getAttributes();
		boolean update = false;
		int[] gene = chromosome.encodedValues();
		Set<Integer> mutateIndex = RandomUtils.randomUniqueInts(0, gene.length, parameters.getMutateSize(), random);
		for (int i : mutateIndex) {
			if (RandomUtils.probability(parameters.getMutateRate(), random)) {
				gene[i] = attributes[RandomUtils.randomUniqueInt(0, attributes.length, random)];
				if (gene[i]==0)	gene[i] = -1;
				if (!update)	update = true;
			}
		}
		if (update)	chromosome.update(gene);
	}
}
