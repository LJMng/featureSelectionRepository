package featureSelection.repository.support.calculation.entropy.mutualInformationEntropy.conditionalEntropy;

import featureSelection.basic.support.calculation.featureImportance.entropy.mutualInformation.ConditionalEntropyCalculation;
import lombok.Getter;

/**
 * Conditional Entropy Calculation:
 * <p>
 * H(A|B) = - &Sigma;<sub>f<sub>j</sub> in B</sub> &Sigma;<sub>f<sub>i</sub> &isin; 
 * A</sub> ( p(f<sub>i</sub>, f<sub>j</sub>) * log p(f<sub>i</sub> | f<sub>j</sub>) )
 * 
 * @author Benjamin_L
 */
public abstract class DefaultConditionalEntropyCalculation 
	implements ConditionalEntropyCalculation
{
	@Getter protected long calculationTimes = 0;
	@Getter protected long calculationAttributeLength = 0;
	public void countCalculate(int attrLen) {
		calculationTimes++;
		calculationAttributeLength += attrLen;
	}

	@Override
	public boolean value1IsBetter(Double v1, Double v2, Double deviation) {
		double value = (v2==null?0:v2) - (v1==null?0:v1);
		return Double.compare(value, deviation) > 0;
	}
}