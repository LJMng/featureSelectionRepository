package featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation;

import featureSelection.repository.support.calculation.positiveRegion.DefaultPositiveRegionCalculation;

public abstract class DefaultDependencyCalculation
	extends DefaultPositiveRegionCalculation
{
	@Override
	public Integer plus(Integer v1, Integer v2) {
		if (v1==null)		return v2;
		else if (v2==null)	return v1;
		else				return v1.intValue()+v2.intValue();
	}
	
	@Override
	public boolean value1IsBetter(Integer v1, Integer v2, Integer deviation) {
		return Integer.compare(v1, v2)>0;
	}

	public Integer difference(Integer v1, Integer v2) {
		return v1-v2;
	}
}