package featureSelection.repository.support.calculation.positiveRegion.classic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionHashMapAlgorithm;
import featureSelection.repository.support.calculation.alg.classic.hash.ClassicHashMapCalculation;
import featureSelection.repository.support.calculation.positiveRegion.DefaultPositiveRegionCalculation;
import lombok.Getter;

public class PositiveRegionCalculation4ClassicHashMap
	extends DefaultPositiveRegionCalculation
	implements ClassicHashMapCalculation<Integer>
{
	@Getter private Integer positive;
	@Override
	public Integer getResult() {
		return positive;
	}

	public PositiveRegionCalculation4ClassicHashMap calculate(Collection<Instance> universes,
			IntegerIterator attributes, Map<Integer, Collection<Instance>> decEClasses,
			Object...args
	) {
		// Count the current calculation
		countCalculate(attributes.size());
		// Calculate
		positive = universes==null || universes.isEmpty()? 
					0: positiveRegion(universes, attributes, decEClasses);
		return this;
	}
	
	private static int positiveRegion(
			Collection<Instance> universes, IntegerIterator attributes,
			Map<Integer, Collection<Instance>> decEClasses
	) {
		if (attributes.size()!=0) {
			// pos=0
			int pos = 0;
			// Obtain decEClass keys induced by P.
			Collection<Collection<IntArrayKey>> decEClassKeys = new ArrayList<>(decEClasses.size());
			for (Collection<Instance> decU: decEClasses.values()) {
				decEClassKeys.add(
					ClassicAttributeReductionHashMapAlgorithm
						.Basic
						.equivalenceClass(decU, attributes)
						.keySet()
				);
			}

			Map<IntArrayKey, Collection<Instance>> eClasses =
					ClassicAttributeReductionHashMapAlgorithm
						.Basic
						.equivalenceClass(universes, attributes);
			for (Map.Entry<IntArrayKey, Collection<Instance>> e : eClasses.entrySet()) {
				// pos += conPOSCalForOneEquivalenceClass
				if (ClassicAttributeReductionHashMapAlgorithm
						.Basic
						.allInstancesAtTheSameDecEquClass(decEClassKeys, e.getKey())
				) {
					pos += e.getValue().size();
				}
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
}