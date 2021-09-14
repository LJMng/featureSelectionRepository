package featureSelection.repository.support.calculation.entropy.shannonConditionEnpropy.positiveApproximationAccelerator;

import java.util.Collection;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.algorithm.alg.positiveApproximationAccelerator.PositiveApproximationAcceleratorOriginalAlgorithm;
import featureSelection.repository.entity.alg.positiveApproximationAccelerator.EquivalenceClass;
import featureSelection.repository.support.calculation.alg.PositiveApproximationAcceleratorCalculation;
import featureSelection.repository.support.calculation.entropy.shannonConditionEnpropy.DefaultShannonConditionEnpropyCalculation;
import org.apache.commons.math3.util.FastMath;

public class SCECalculation4ACCOriginal
	extends DefaultShannonConditionEnpropyCalculation
	implements PositiveApproximationAcceleratorCalculation<Double>
{
	private double entropy;
	@Override
	public Double getResult() {
		return entropy;
	}
	
	/**
	 * Calculated entropy result is the one <strong>without</strong> denominator part(|U|): 
	 * SCE = numerator <del>/ denominator</del>
	 */
	public SCECalculation4ACCOriginal calculate(
			Collection<EquivalenceClass> equClasses, int attributeLength, Object...args
	) {
		// Count the current calculation
		countCalculate(attributeLength);
		// Calculate
		entropy = equClasses==null||equClasses.isEmpty()? 0: shannonConditionalEntropy(equClasses);
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
	 * @param equClasses
	 * 		An {@link EquivalenceClass} {@link Collection}: <strong>U/B</strong>
	 * @return calculated shannon conditional entropy value.
	 */
	public double shannonConditionalEntropy(Collection<EquivalenceClass> equClasses) {
		// SCE = 0
		double entropy = 0;
		// Go through E in U
		double dSum, tmp;
		Collection<EquivalenceClass> decEquClasses;
		for (EquivalenceClass equ: equClasses) {
			// e.member
			// H = EquivalenceClass(U, D)
			decEquClasses = PositiveApproximationAcceleratorOriginalAlgorithm
								.Basic
								.equivalenceClass(equ.getInstances(), new IntegerArrayIterator(0));
			// dSum = 0
			dSum = 0;
			// for i=1 to |H|
			for (EquivalenceClass decEqu : decEquClasses) {
				// dSum = dSum + d
				dSum += decEqu.getInstances().size();
			}
			// for i=1 to |H|
			for (EquivalenceClass decEqu : decEquClasses) {
				tmp = decEqu.getInstances().size() / dSum;
				// SCE = SCE + dSum * ( (d/dSum) * log(d/dSum) )
				entropy -= dSum * tmp * FastMath.log(tmp);
			}
		}
		// return SCE
		return entropy;
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof EquivalenceClass;
	}
}