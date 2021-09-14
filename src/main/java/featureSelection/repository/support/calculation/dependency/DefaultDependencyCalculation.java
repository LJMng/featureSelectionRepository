package featureSelection.repository.support.calculation.dependency;

import featureSelection.basic.support.calculation.featureImportance.DependencyCalculation;
import lombok.Getter;

public abstract class DefaultDependencyCalculation 
	implements DependencyCalculation
{
	@Getter protected long calculationTimes = 0;
	@Getter protected long calculationAttributeLength = 0;
	public void countCalculate(int attrLen) {
		calculationTimes++;
		calculationAttributeLength += attrLen;
	}

	
	@Override
	public Double plus(Double v1, Double v2) {
		throw new UnsupportedOperationException("Unimplemented method!");
	}
	
	@Override
	public boolean value1IsBetter(Double v1, Double v2, Double deviation) {
		return Double.compare((v1==null?0:v1) - (v2==null?0:v2), deviation)>0;
	}
}