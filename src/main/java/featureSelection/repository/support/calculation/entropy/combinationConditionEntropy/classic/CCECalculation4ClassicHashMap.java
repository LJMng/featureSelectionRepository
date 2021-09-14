package featureSelection.repository.support.calculation.entropy.combinationConditionEntropy.classic;

import java.util.Collection;
import java.util.Map;

import common.utils.MathUtils;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionHashMapAlgorithm;
import featureSelection.repository.support.calculation.alg.classic.hash.ClassicHashMapCalculation;
import featureSelection.repository.support.calculation.entropy.shannonConditionEnpropy.DefaultShannonConditionEnpropyCalculation;
import lombok.Getter;

public class CCECalculation4ClassicHashMap
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
	 * part(|U|<sup>2</sup> * C<sub>|U|</sub><sup>2</sup>):
	 * CCE = numerator <del>/ denominator</del>
	 * <p>
	 * where <strong>C<sub>|U|</sub><sup>2</sup></strong> is the Combinatorial number of
	 * 2 of |U|:
	 * <p>
	 * C<sub>|U|</sub><sup>2</sup> = |U| * (|U|-1)
	 */
	public CCECalculation4ClassicHashMap calculate(
			Collection<Instance> universes, IntegerIterator attributes,
			Map<Integer, Collection<Instance>> decEClasses, Object...args
	) {
		// Count the current calculation
		countCalculate(attributes.size());
		// Calculate
		entropy = universes==null || universes.isEmpty()? 
					0: combinationConditionalEntropy(universes, attributes, decEClasses);
		return this;
	}
	
	/**
	 * Calculated entropy result is the one <strong>without</strong> denominator
	 * part(|U|<sup>2</sup> * C<sub>|U|</sub><sup>2</sup>):
	 * CCE = numerator <del><strong>/ denominator</strong></del>
	 * <p>
	 * <strong>CE(D|B)</strong> = 
	 * 			&sum;<sub>i=1</sub><sup>|U/B|</sup> (
	 * 				|X<sub>i</sub>|<del><strong>/|U|</strong></del> * 
	 * 				C<sub>|X<sub>i</sub>|</sub><sup>2</sup><del><strong>/C<sub>|U|</sub><sup>2</sup></strong></del> -
	 * 				&sum;<sub>j=1</sub><sup>|U/D|</sup> ( 
	 * 					|X<sub>i</sub> ∩ Y<sub>j</sub>|<del><strong>/|U|</strong></del> * 
	 * 					C<sub>|X<sub>i</sub> ∩ Y<sub>j</sub>|</sub><sup>2</sup>
	 * 						<del><strong>/C<sub>|U|</sub><sup>2</sup></strong></del>
	 * 				)
	 *			)
	 * <p>
	 * where <strong>C<sub>|U|</sub><sup>2</sup></strong> is the Combinatorial number
	 * of 2 of |U|:
	 * <p>
	 * C<sub>|U|</sub><sup>2</sup> = |U| * (|U|-1)
	 * <p>
	 * That is:
	 * <p>
	 * <strong>CE(D|B)</strong> = 
	 * 			&sum;<sub>i=1</sub><sup>|U/B|</sup> (
	 * 				|X<sub>i</sub>| * 
	 * 				C<sub>|X<sub>i</sub>|</sub><sup>2</sup> -
	 * 				&sum;<sub>j=1</sub><sup>|U/D|</sup> ( 
	 * 					|X<sub>i</sub> ∩ Y<sub>j</sub>| * 
	 * 					C<sub>|X<sub>i</sub> ∩ Y<sub>j</sub>|</sub><sup>2</sup>
	 * 				)
	 *			)
	 * 
	 * @param Instances
	 * 		An {@link Instance} {@link Collection}: <strong>U</strong>
	 * @param attributes
	 * 		Attributes used to partition the <code>Instances</code>: <strong>B</strong>
	 * @param decEClasses
	 * 		{@link Instance}s partitioned by decision attribute: <strong>U/D</strong>.
	 * @return calculated combination conditional entropy value.
	 */
	public static double combinationConditionalEntropy(
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
				double sub = 0;
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
					// |X<sub>i</sub> ∩ Y<sub>j</sub>| * C<sub>|X<sub>i</sub> ∩ Y<sub>j</sub>|</sub><sup>2</sup>
					sub += intersection * MathUtils.combinatorialNumOf2(intersection);
				}
				// |X<sub>i</sub>| * C<sub>|X<sub>i</sub>|</sub><sup>2</sup>
				entropy += (equClass.size() * MathUtils.combinatorialNumOf2(equClass.size()) - sub);
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