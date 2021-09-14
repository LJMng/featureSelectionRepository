package featureSelection.repository.support.calculation.dependency.classic;

import java.util.Collection;
import java.util.List;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionSequentialAlgorithm;
import featureSelection.repository.support.calculation.alg.classic.sequential.ClassicSequentialCalculation;
import featureSelection.repository.support.calculation.dependency.DefaultDependencyCalculation;
import lombok.Getter;

public class DependencyCalculation4ClassicSequential
	extends DefaultDependencyCalculation
	implements ClassicSequentialCalculation<Double>
{
	@Getter private Double positive;	
	@Override
	public Double getResult() {
		return positive;
	}

	public DependencyCalculation4ClassicSequential calculate(
			Collection<Instance> instances, IntegerIterator attributes,
			Collection<List<Instance>> decEClasses, Object...args
	) {
		// Count the current calculation
		countCalculate(attributes.size());
		// Calculate
		positive = (Double) new Double(instances==null || instances.isEmpty()? 0.0:
								positiveRegion(instances, attributes, decEClasses) / (instances.size()+0.0)
					);
		return this;
	}
	
	private static int positiveRegion(
			Collection<Instance> instances, IntegerIterator attributes,
			Collection<List<Instance>> decEClasses
	) {
		Collection<List<Instance>> eClasses =
				ClassicAttributeReductionSequentialAlgorithm
						.Basic
						.equivalenceClass(instances, attributes);
		// POS = 0
		int pos = 0;
		// for i=1 to number of |PEClass|
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
	public Double difference(Double v1, Double v2) {
		return v1-v2;
	}
}