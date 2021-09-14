package featureSelection.repository.support.calculation.entropy.shannonConditionEnpropy.classic;

import java.util.Collection;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionHashMapAlgorithm;
import featureSelection.repository.support.calculation.alg.classic.hash.ClassicHashMapCalculation;
import featureSelection.repository.support.calculation.entropy.shannonConditionEnpropy.DefaultShannonConditionEnpropyCalculation;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;

public class SCECalculation4ClassicHashMap
	extends DefaultShannonConditionEnpropyCalculation
	implements ClassicHashMapCalculation<Double>
{
	@Getter private double entropy;
	@Override
	public Double getResult() {
		return entropy;
	}

	/**
	 * Calculated entropy result is the one <strong>without</strong> denominator part(|U|): 
	 * SCE = numerator <del>/ denominator</del>
	 */
	public SCECalculation4ClassicHashMap calculate(Collection<Instance> universes,
			IntegerIterator attributes, Map<Integer, Collection<Instance>> decEClasses,
			Object...args
	) {
		// Count the current calculation
		countCalculate(attributes.size());
		// Calculate
		entropy = universes==null || universes.isEmpty()? 
					0: shannonConditionalEntropy(universes, attributes, decEClasses);
		return this;
	}
	
	/**
	 * Calculated entropy result is the one <strong>without</strong> denominator part(|U|): 
	 * SCE = numerator <del><strong>/ denominator</strong></del>
	 * <p>
	 * <strong>H(D|B)</strong> = 
	 * 			- &sum;<sub>i=1</sub><sup>|U/B|</sup> (
	 * 				|X<sub>i</sub>| <del><strong>/ |U|</strong></del> *
	 * 				&sum;<sub>j=1</sub><sup>|U/D|</sup> ( 
	 * 					|X<sub>i</sub> ∩ Y<sub>j</sub>|/|X<sub>i</sub>| *
	 * 					log(|X<sub>i</sub> ∩ Y<sub>j</sub>|/|X<sub>i</sub>|) 
	 * 				)
	 *			)
	 * 
	 * @param Instances
	 * 		An {@link Instance} {@link Collection}: <strong>U</strong>
	 * @param attributes
	 * 		Attributes used to partition the <code>Instances</code>: <strong>B</strong>
	 * @param decEClasses
	 * 		{@link Instance}s partitioned by decision attribute: <strong>U/D</strong>.
	 * @return calculated shannon conditional entropy value.
	 */
	public static double shannonConditionalEntropy(
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
					// |X<xub>i</sub> ∩ Y<sub>j</sub>|
					double intersection = ClassicAttributeReductionHashMapAlgorithm
											.Basic
											.intersectionOf(equClass, decEquClass)
											.size();
					// |X<xub>i</sub> ∩ Y<sub>j</sub>| / |X<xub>i</sub>|
					double tmp = intersection / equClass.size();
					sub += tmp * FastMath.log(tmp);
				}
				entropy -= equClass.size() * sub;
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