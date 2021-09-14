package featureSelection.repository.support.calculation.entropy.mutualInformationEntropy.mutualInformationEntropy;

import featureSelection.basic.support.calculation.featureImportance.entropy.mutualInformation.MutualInformationEntropyCalculation;
import lombok.Getter;

/**
 * Mutual Information Entropy.
 * <p>
 * <strong>I(F, C)</strong> = <strong>H(C) - H(C|F)</strong> = H(F) - H(F|C).
 * 
 * @author Benjamin_L
 */
public abstract class DefaultMutualInformationEntropy 
	implements MutualInformationEntropyCalculation
{
	@Getter protected long calculationTimes = 0;

	@Override
	public long getCalculationAttributeLength() {
		return 0;
	}
	
	@Override
	public boolean value1IsBetter(Double v1, Double v2, Double deviation) {
		double value = (v2==null?0:v2) - (v1==null?0:v1);
		return Double.compare(value, deviation) > 0;
	}
	
	@Getter private Double result;
	
	/**
	 * <strong>I(F, C)</strong> = <strong>H(C) - H(C|F)</strong> = H(F) - H(F|C).
	 * 
	 * @param infoEntropy
	 * 		Information Entropy value: <strong>H(C)</strong>.
	 * @param condEntropy
	 * 		Conditional Entropy value: <strong>H(C|F)</strong>.
	 * @return I(F, C) as mutual information entropy.
	 */
	public DefaultMutualInformationEntropy calculate(double infoEntropy, double condEntropy) {
		calculationTimes++;
		
		result = infoEntropy - condEntropy;
		return this;
	}
}