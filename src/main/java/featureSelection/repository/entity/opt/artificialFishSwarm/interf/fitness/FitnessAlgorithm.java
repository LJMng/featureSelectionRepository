package featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness;

import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.Position;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.ReductionAlgorithm;

/**
 * Fitness calculation algorithm
 * 
 * @author Benjamin_L
 *
 * @param <FI>
 *     Type of implemented feature (subset) importance calculation.
 * @param <Sig>
 *     Type of feature (subset) importance value.
 * @param <FV>
 * 		Type of implemented {@link FitnessValue}.
 * @param <Posi>
 * 		Implemented {@link Position}.
 */
public interface FitnessAlgorithm<FI extends FeatureImportance<Sig>,
									Sig extends Number,
									FV extends FitnessValue<? extends Number>, 
									Posi extends Position<?>>
{
	
	/**
	 * Calculate the fitness by position.
	 * 
	 * @param <CollectionItem>
	 * 		Universe or Equivalent Class.
	 * @param redAlg
	 * 		Feature Selection implemented {@link ReductionAlgorithm}.
	 * @param attributes
	 * 		Attributes to be calculated.
	 * @param featureSignificance
	 * 		{@link Sig} instance.
	 * @param args
	 * 		Extra arguments.
	 * @return {@link Fitness} as Fitness.
	 */
	<CollectionItem> Fitness<Posi, FV> calculateFitness(
			ReductionAlgorithm<FI, Sig, CollectionItem> redAlg,
			int[] attributes, Sig featureSignificance, Object...args
	);
	
	/**
	 * Compare fitnesses and return the best one.
	 * 
	 * @param fitnesses
	 * 		One or an array of {@link Fitness}
	 * @return {@link Fitness} with the best value among.
	 */
	@SuppressWarnings("unchecked")
	Fitness<Posi, FV> findBestFitness(Fitness<Posi, FV>...fitnesses);
	
	int compareFitnessValue(FV fv1, FV fv2);
}