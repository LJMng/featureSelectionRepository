package featureSelection.repository.entity.opt.artificialFishSwarm.impl.fitness.algorithm;

import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.fitness.fitness.PositionFitness4ByteArray;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.fitness.fitnessValue.FitnessValue4Double;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.position.ByteArrayPosition;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness.Fitness;

/**
 * Refer to the article 
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S0950705115000337">
 * "Finding rough set reducts with fish swarm algorithm" </a> by Yumin Chen.
 * <p>
 * Fitness calculation algorithm : 
 * 		fitnes = <code>a</code> * dependency + <code>b</code> * (|C|-len)/|C|.
 * <p>
 * Where <code>a</code> is a double param between [0,1] and <code>b=1-a</code>, <code>|C|</code>
 * is the length of all attributes, <code>len</code> is the length of current attributes,
 * <code>dependency</code> is the value of measuring feature importance ("the classification
 * quality" as the article cites).
 *
 * @param <FI>
 *     Type of feature (subset) importance calculation.
 * @param <Sig>
 *     Type of feature (subset) significance.
 * 
 * @author Benjamin_L
 */
public class FitnessAlgorithm4ByteArray<FI extends FeatureImportance<Sig>, Sig extends Number>
	extends PositionFitnessAlgorithm4ByteArray<FI, Sig, FitnessValue4Double>
{
	@Override
	public int compareFitnessValue(FitnessValue4Double fv1, FitnessValue4Double fv2) {
		return Double.compare(fv1==null?0:fv1.getFitnessValue(), fv2==null?0:fv2.getFitnessValue());
	}

	@Override
	public <CollectionItem> Fitness<ByteArrayPosition, FitnessValue4Double> calculateFitness(
			ReductionAlgorithm<FI, Sig, CollectionItem> redAlg,
			int[] attributes, Sig featureSignificance, Object...args
	) {
		PositionFitness4ByteArray<FitnessValue4Double> fitness;
		if (attributes==null || attributes.length==0) {
			fitness = new PositionFitness4ByteArray<>(new FitnessValue4Double(0.0, 0.0));
			fitness.setPosition(new ByteArrayPosition(new int[0], getLen()));
			return fitness;
		}
		
		int len = attributes.length;
		double dependency = featureSignificance.doubleValue();
		fitness = new PositionFitness4ByteArray<>(
						new FitnessValue4Double(
							(getA() * dependency + (1-getA()) * (getLen()-len))
							/ (double) this.getLen(),
							dependency
						)
					);
		fitness.setPosition(new ByteArrayPosition(attributes, this.getLen()));
		return fitness;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Fitness<ByteArrayPosition, FitnessValue4Double> findBestFitness(
			Fitness<ByteArrayPosition, FitnessValue4Double>...fitnesses
	) {
		int bestIndex = 0;
		for (int i=1; i<fitnesses.length; i++) {
			if (fitnesses[i]==null)	continue;
			if (fitnesses[bestIndex]==null || 
				Double.compare(
						fitnesses[i].getFitnessValue().getFitnessValue(), 
						fitnesses[bestIndex].getFitnessValue().getFitnessValue()
				)>0
			) {
				bestIndex = i;
			}
		}
		return fitnesses[bestIndex];
	}
}