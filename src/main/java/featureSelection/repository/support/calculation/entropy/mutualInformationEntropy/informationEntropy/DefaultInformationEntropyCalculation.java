package featureSelection.repository.support.calculation.entropy.mutualInformationEntropy.informationEntropy;

import featureSelection.basic.support.calculation.featureImportance.entropy.mutualInformation.InformationEntropyCalculation;
import lombok.Getter;

public abstract class DefaultInformationEntropyCalculation 
	implements InformationEntropyCalculation
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