package featureSelection.repository.support.calculation.dependency.classic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionHashMapAlgorithm;
import featureSelection.repository.support.calculation.alg.classic.hash.ClassicHashMapCalculation;
import featureSelection.repository.support.calculation.dependency.DefaultDependencyCalculation;
import lombok.Getter;

public class DependencyCalculation4ClassicHashMap
	extends DefaultDependencyCalculation
	implements ClassicHashMapCalculation<Double>
{
	@Getter private Double positive;
	@Override
	public Double getResult() {
		return positive;
	}

	public DependencyCalculation4ClassicHashMap calculate(Collection<Instance> universes,
			IntegerIterator attributes, Map<Integer, Collection<Instance>> decEClasses,
			Object...args
	) {
		// Count the current calculation
		countCalculate(attributes.size());
		// Calculate
		int universeSize = universes.size();
		positive = (Double) new Double(universes==null || universes.isEmpty()? 
							0: positiveRegion(universes, attributes, decEClasses) / (double) universeSize
					);
		return this;
	}
	
	private static int positiveRegion(
			Collection<Instance> universes, IntegerIterator attributes,
			Map<Integer, Collection<Instance>> decEClasses
	) {
		if (attributes.size()!=0) {
			// 1 pos=0
			int pos = 0;
			// Generate decEClass keys partited by P.
			Collection<Collection<IntArrayKey>> decEClassKeys = new ArrayList<>(decEClasses.size());
			for (Collection<Instance> decU: decEClasses.values()) {
				decEClassKeys.add(
					ClassicAttributeReductionHashMapAlgorithm
						.Basic
						.equivalenceClass(decU, attributes)
						.keySet()
				);
			}
			// 2 for j=1 to numebr of |PEClass|
			Map<IntArrayKey, Collection<Instance>> eClasses =
					ClassicAttributeReductionHashMapAlgorithm
							.Basic
							.equivalenceClass(universes, attributes);
			for (Map.Entry<IntArrayKey, Collection<Instance>> e : eClasses.entrySet()) {
				// 3 pos += conPOSCalForOneEquivalenceClass
				if (ClassicAttributeReductionHashMapAlgorithm
						.Basic
						.allInstancesAtTheSameDecEquClass(decEClassKeys, e.getKey())
				) {
					pos += e.getValue().size();
				}
			}
			// 4 return pos
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