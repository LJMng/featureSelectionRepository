package featureSelection.repository.support.calculation.positiveRegion.activeSampleSelection;

import java.util.Collection;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.alg.activeSampleSelection.EquivalenceClass;
import featureSelection.repository.support.calculation.alg.FeatureImportance4ActiveSampleSelection;
import featureSelection.repository.support.calculation.positiveRegion.DefaultPositiveRegionCalculation;
import lombok.Getter;

public class PositiveCalculation4ActiveSampleSelection
	extends DefaultPositiveRegionCalculation
	implements FeatureImportance<Integer>,
		FeatureImportance4ActiveSampleSelection<Integer>
{
	@Getter private Integer result;

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Instance;
	}

	@Override
	public FeatureImportance4ActiveSampleSelection<Integer> calculate(
			Collection<EquivalenceClass> equClasses
	) {
		// Update result
		this.result = equClasses.stream()
								.filter(e->e.getDecision()!=null)
								.mapToInt(e->e.getUniverses().size())
								.sum();
		return this;
	}
}