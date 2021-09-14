package featureSelection.repository.support.calculation.entropy.liangConditionEntropy.classic;

import java.util.Collection;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionHashMapAlgorithm;
import featureSelection.repository.support.calculation.alg.classic.hash.ClassicHashMapCalculation;
import featureSelection.repository.support.calculation.entropy.shannonConditionEnpropy.DefaultShannonConditionEnpropyCalculation;
import lombok.Getter;

public class LCECalculation4ClassicHashMap
	extends DefaultShannonConditionEnpropyCalculation
	implements ClassicHashMapCalculation<Double>
{
	@Getter private double entropy;
	@Override
	public Double getResult() {
		return entropy;
	}

	/**
	 * Calculated entropy result is the one <strong>without</strong> denominator
	 * part(|U|<sup>2</sup>): LCE = numerator <del>/ denominator</del>
	 */
	public LCECalculation4ClassicHashMap calculate(
			Collection<Instance> instances, IntegerIterator attributes,
			Map<Integer, Collection<Instance>> decEClasses, Object...args
	) {
		// Count the current calculation
		countCalculate(attributes.size());
		// Calculate
		entropy = instances==null || instances.isEmpty()?
					0: liangConditionalEntropy(instances, attributes, decEClasses);
		return this;
	}
	
	/**
	 * Calculated entropy result is the one <strong>without</strong> denominator
	 * part(|U|<sup>2</sup>):
	 * LCE = numerator <del><strong>/ denominator</strong></del>
	 * <p>
	 * <strong>E(D|B)</strong> = 
	 * 			&sum;<sub>i=1</sub><sup>|U/B|</sup> (
	 * 				&sum;<sub>j=1</sub><sup>|U/D|</sup> ( 
	 * 					|X<sub>i</sub> ∩ Y<sub>j</sub>|<del><strong>/|U|</strong></del> *
	 * 					|Y<sub>j</sub><sup>C</sup> - X<sub>i</sub><sup>C</sup>|<del><strong>/|U|</strong></del>
	 * 				)
	 *			)
	 * <p>
	 * which can be transformed into:
	 * <p>
	 * <strong>E(D|B)</strong> = 
	 * 			- &sum;<sub>i=1</sub><sup>|U/B|</sup> (
	 * 				&sum;<sub>j=1</sub><sup>|U/D|</sup> ( 
	 * 					|X<sub>i</sub> ∩ Y<sub>j</sub>|<del><strong>/|U|</strong></del> *
	 * 					|X<sub>i</sub> - X<sub>i</sub> ∩ Y<sub>j</sub>|<del><strong>/|U|</strong></del>
	 * 				)
	 *			)
	 * 
	 * @param Instances
	 * 		An {@link Instance} {@link Collection}: <strong>U</strong>
	 * @param attributes
	 * 		Attributes used to partition the <code>Instances</code>: <strong>B</strong>
	 * @param decEClasses
	 * 		{@link Instance}s partitioned by decision attribute: <strong>U/D</strong>.
	 * @return calculated liang conditional entropy value.
	 */
	public static double liangConditionalEntropy(
			Collection<Instance> Instances, IntegerIterator attributes,
			Map<Integer, Collection<Instance>> decEClasses
	) {
		if (attributes.size()!=0) {
			// U/P
			Map<IntArrayKey, Collection<Instance>> equClasses =
				ClassicAttributeReductionHashMapAlgorithm
					.Basic
					.equivalenceClass(Instances, attributes);
			// Loop U/P and calculate entropy
			double entropy = 0;
			for (Collection<Instance> equClass: equClasses.values()) {
				for (Collection<Instance> decEquClass: decEClasses.values()) {
					// |X<sub>i</sub> ∩ Y<sub>j</sub>|
					/*Collection<Instance> intersection =
						ClassicAttributeReductionHashMapAlgorithm
							.Basic
							.intersectionOf(equClass, decEquClass);
					// |X<sub>i</sub> - X<sub>i</sub> ∩ Y<sub>j</sub>|
					Collection<Instance> rest =
							equClass.stream()
									.filter(e->!intersection.contains(e))
									.collect(Collectors.toList());
					entropy += intersection.size() * rest.size();//*/

					// |X<sub>i</sub> ∩ Y<sub>j</sub>|
					int intersection = 
						ClassicAttributeReductionHashMapAlgorithm
							.Basic
							.intersectionOf(equClass, decEquClass)
							.size();
					// |X<sub>i</sub> ∩ Y<sub>j</sub>|*|X<sub>i</sub> - X<sub>i</sub> ∩ Y<sub>j</sub>|
					entropy += intersection * (equClass.size() - intersection);
				}
			}
			return entropy;
		}else {
			return 0;
		}
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}
}