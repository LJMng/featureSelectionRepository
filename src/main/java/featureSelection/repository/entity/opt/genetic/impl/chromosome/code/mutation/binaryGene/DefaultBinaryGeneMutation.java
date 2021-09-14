package featureSelection.repository.entity.opt.genetic.impl.chromosome.code.mutation.binaryGene;

import java.util.Random;
import java.util.Set;

import common.utils.RandomUtils;
import featureSelection.repository.entity.opt.genetic.impl.chromosome.chromosomeEntity.binaryGeneChromosome.BinaryGeneChromosome;
import featureSelection.repository.entity.opt.genetic.interf.chromosome.code.mutation.ChromosomeMutation;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DefaultBinaryGeneMutation<Chr extends BinaryGeneChromosome>
	implements ChromosomeMutation<Chr, DefaultBinaryGeneMutationParameters<Chr>>
{
	private DefaultBinaryGeneMutationParameters<Chr> parameters;
	
	@Override
	public void mutate(Chr chromosome, Random random) {
		boolean update = false;
		byte[] gene = chromosome.encodedValues();
		Set<Integer> mutateIndex = RandomUtils.randomUniqueInts(0, gene.length, parameters.getMutateSize(), random);
		for (int i : mutateIndex) {
			if (RandomUtils.probability(parameters.getMutateRate(), random)) {
				if (gene[i]==0)	gene[i] = 1;
				else			gene[i] = 0;
			}
		}
		if (update)	chromosome.update(gene);
	}
}
