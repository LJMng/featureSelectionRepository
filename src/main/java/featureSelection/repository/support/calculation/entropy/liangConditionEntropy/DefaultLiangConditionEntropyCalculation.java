package featureSelection.repository.support.calculation.entropy.liangConditionEntropy;

import featureSelection.basic.support.calculation.featureImportance.entropy.LiangConditionEntropyCalculation;
import lombok.Getter;

public abstract class DefaultLiangConditionEntropyCalculation 
	implements LiangConditionEntropyCalculation
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
	
	@Override
	public Double plus(Double v1, Double v2) {
		if (v1==null)		return v2;
		else if (v2==null)	return v1;
		else				return v1+v2;
	}
}
