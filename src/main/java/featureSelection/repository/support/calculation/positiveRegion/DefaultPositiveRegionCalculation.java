package featureSelection.repository.support.calculation.positiveRegion;

import featureSelection.basic.support.calculation.featureImportance.PositiveRegionCalculation;
import lombok.Getter;

public abstract class DefaultPositiveRegionCalculation
	implements PositiveRegionCalculation<Integer>
{
	@Getter protected long calculationTimes = 0;
	@Getter protected long calculationAttributeLength = 0;
	public void countCalculate(int attrLen) {
		calculationTimes++;
		calculationAttributeLength += attrLen;
	}
	
	@Override
	public Integer plus(Integer v1, Integer v2) {
		if (v1==null) {
			return v2;
		}else if (v2==null) {
			return v1;
		}else {
			return (Integer) new Integer(v1.intValue() + v2.intValue());
		}
	}
	
	@Override
	public boolean value1IsBetter(Integer v1, Integer v2, Integer deviation) {
		return Integer.compare((v1==null?0: v1.intValue()) - (v2==null?0: v2.intValue()), 
								deviation.intValue())>0;
	}
}