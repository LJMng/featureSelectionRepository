package featureSelection.repository.entity.alg.dynamics.instance.asitKDasIncremental.fitness;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.support.calculation.asitKDasFitness.DefaultAsitKDasFitnessCalculation;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Parameters of fitness evaluation/calculation for {@link DefaultAsitKDasFitnessCalculation}
 * <p>
 * Calculation formular:
 * <pre>
 * fitness = w * ( (Sig(U[new])/|U[new]|)^k / |A1|^z ) +
 * 	(1-w) * (intersection of newReduct &amp; redB4DataArrived) / |A|
 * 
 * 	w, k, z: parameters set in <code>this</code> Class entity.
 *  U[new]: new data arrived.
 *  A: complete attributes of {@link Instance}.
 *  A1: a reduct of the data before new one arriving. 
 *  R1: a reduct of the new data arrived.
 * </pre>
 * 
 * @author Benjamin_L
 */
@Data
@AllArgsConstructor
public class FitnessEvaluationParameters {
	private int k;
	private double z;
	private double w;
}
