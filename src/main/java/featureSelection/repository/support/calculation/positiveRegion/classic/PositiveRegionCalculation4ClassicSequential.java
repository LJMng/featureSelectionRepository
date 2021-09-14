package featureSelection.repository.support.calculation.positiveRegion.classic;

import java.util.Collection;
import java.util.List;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionSequentialAlgorithm;
import featureSelection.repository.support.calculation.alg.classic.sequential.ClassicSequentialCalculation;
import featureSelection.repository.support.calculation.positiveRegion.DefaultPositiveRegionCalculation;
import lombok.Getter;

public class PositiveRegionCalculation4ClassicSequential
	extends DefaultPositiveRegionCalculation
	implements ClassicSequentialCalculation<Integer>
{
	@Getter private Integer positive;	
	@Override
	public Integer getResult() {
		return positive;
	}

	public PositiveRegionCalculation4ClassicSequential calculate(
			Collection<Instance> instances, IntegerIterator attributes,
			Collection<List<Instance>> decEClasses, Object...args
	) {
		// Count the current calculation
		countCalculate(attributes.size());
		// Calculate
		positive = instances==null || instances.isEmpty()? 0:
					positiveRegion(instances, attributes, decEClasses);
		return this;
	}
	
	private static int positiveRegion(
			Collection<Instance> instances, IntegerIterator attributes,
			Collection<List<Instance>> decEClasses
	) {
		Collection<List<Instance>> eClasses = ClassicAttributeReductionSequentialAlgorithm.Basic.equivalenceClass(instances, attributes);
		// POS = 0
		int pos = 0;
		for (List<Instance> e: eClasses) {
			if (ClassicAttributeReductionSequentialAlgorithm
					.Basic
					.isPositiveRegion(e, decEClasses, attributes)
			) {
				pos += e.size();
			}
		}
		return pos;
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}

	@Override
	public Integer difference(Integer v1, Integer v2) {
		return v1-v2;
	}
}