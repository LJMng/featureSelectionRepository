package featureSelection.repository.entity.opt.artificialFishSwarm.impl.fitness.algorithm;

import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.position.ByteArrayPosition;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness.FitnessAlgorithm;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.fitness.FitnessValue;
import lombok.Getter;
import lombok.Setter;

/**
 * A {@link FitnessValue} calculation algorithm for {@link ByteArrayPosition}, which is measure by 
 * byte array. 
 * <p>
 * For example, position 1 (0, 1, 1, 1).
 * 
 * @author Benjamin_L
 *
 * @param <FI>
 *      Type of feature (subset) importance calculation.
 * @param <Sig>
 *      Type of feature (subset) importance.
 * @param <FV>
 * 		Class type of fitness's value.
 */
public abstract class PositionFitnessAlgorithm4ByteArray<FI extends FeatureImportance<Sig>,
														Sig extends Number, 
														FV extends FitnessValue<?>>
	implements FitnessAlgorithm<FI, Sig, FV, ByteArrayPosition>
{
	@Getter @Setter private double a;
	@Getter @Setter private int len;
}