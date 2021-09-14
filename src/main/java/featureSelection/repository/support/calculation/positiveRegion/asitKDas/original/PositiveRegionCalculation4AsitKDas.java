package featureSelection.repository.support.calculation.positiveRegion.asitKDas.original;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionSequentialAlgorithm;
import featureSelection.repository.support.calculation.alg.FeatureImportance4AsitKDas;
import featureSelection.repository.support.calculation.positiveRegion.DefaultPositiveRegionCalculation;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;

public class PositiveRegionCalculation4AsitKDas
	extends DefaultPositiveRegionCalculation
	implements FeatureImportance4AsitKDas<Integer>
{
	@Getter private Integer positive;
	@Override
	public Integer getResult() {
		return positive;
	}

	@Override
	public FeatureImportance4AsitKDas<Integer> calculate(
			Collection<Instance> instances, IntegerIterator attributes, Object... args
	) {
		// Count the current calculation
		countCalculate(attributes.size());
		// Calculate
		positive = instances==null || instances.isEmpty()?
					0: consistency(instances, attributes);
		return this;
	}
	
	private static int consistency(Collection<Instance> universes, IntegerIterator attributes) {
		if (attributes.size()!=0) {
			// pos=0
			int pos = 0;
			// Calculate equivalent classes of universes
			Collection<List<Instance>> equClasses =
				ClassicAttributeReductionSequentialAlgorithm
					.Basic
					.equivalenceClass(universes, attributes);
			// Go through equivalent classes
			Iterator<Instance> universeIterator;
			EquClassLoop:
			for (Collection<Instance> equClass: equClasses) {
				// Go through universe in equivalent class
				universeIterator = equClass.iterator();
				int dec = universeIterator.next().getAttributeValue(0);
				while (universeIterator.hasNext()) {
					// if universe.dec != universe[0].dec
					//	go to step 3
					if (dec!=universeIterator.next().getAttributeValue(0)) {
						continue EquClassLoop;
					}
				}
				pos += equClass.size();	
			}
			return pos;
		}else {
			return 0;
		}
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}

	@Override
	public Integer difference(Integer v1, Integer v2) {
		return FastMath.abs(v1.intValue()-v2.intValue());
	}
}