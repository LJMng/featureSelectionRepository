package featureSelection.repository.support.calculation.entropy.mutualInformationEntropy.informationEntropy.semisupervisedRepresentative;

import java.util.Collection;
import java.util.Map;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.alg.SemisupervisedRepresentativeStrategy;
import featureSelection.repository.support.calculation.alg.semisupervisedRepresentative.FeatureImportance4SemisupervisedRepresentative;
import featureSelection.repository.support.calculation.entropy.mutualInformationEntropy.informationEntropy.DefaultInformationEntropyCalculation;
import org.apache.commons.math3.util.FastMath;

public class InformationEntropyCalculation4SemisupervisedRepresentative
	extends DefaultInformationEntropyCalculation
	implements SemisupervisedRepresentativeStrategy,
				FeatureImportance4SemisupervisedRepresentative<Double>
{
	private double entropy;
	@Override
	public Double getResult() {
		return entropy;
	}
	
	/**
	 * Calculate Info. entropy of Equivalent Classes(<code>equClasses</code>).
	 * <p>
	 * No extra arguments required in <code>args</code>.
	 */
	@Override
	public FeatureImportance4SemisupervisedRepresentative<Double> calculate(
			Collection<Collection<Instance>> equClasses, Object...args
	) {
		// Initiate
		int universeSize = (int) args[0];
		
		entropy = 0;
		double p;
		for (Collection<Instance> equClass: equClasses) {
			// H -= |u| / log(1/|u|)
			// p = |u| / |U|
			// H -= p * log(p)
			p = equClass.size() / (double) universeSize;
			entropy -= p * FastMath.log(p);
		}
		return this;
	}
	
	@Override
	public Double plus(Double v1, Double v2) throws Exception {
		throw new UnsupportedOperationException("Unimplemented method!");
	}
	
	@Override
	public <Item> boolean calculateAble(Item item) {
		if (item instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) item;
			if (map.values() instanceof Collection)
				return map.isEmpty()? true: map.values().iterator().next() instanceof Instance;
			else
				return false;
		}else {
			return false;
		}
	}
}