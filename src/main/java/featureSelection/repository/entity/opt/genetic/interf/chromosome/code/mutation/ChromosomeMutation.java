package featureSelection.repository.entity.opt.genetic.interf.chromosome.code.mutation;

import java.util.Random;

import featureSelection.repository.entity.opt.genetic.interf.chromosome.entity.Chromosome;

/**
 * Chromosome gene mutation for Genetic4StaticData.
 * 
 * @author Benjamin_L
 *
 * @param <Chr>
 * 		Type of implemented {@link Chromosome} to be mutated.
 */
public interface ChromosomeMutation<Chr extends Chromosome<?>,
									MutateParam extends ChromosomeMutationParameters<Chr>>
{
	void mutate(Chr chromosome, Random random);
	
	public void setParameters(MutateParam param);
	public MutateParam getParameters();
}