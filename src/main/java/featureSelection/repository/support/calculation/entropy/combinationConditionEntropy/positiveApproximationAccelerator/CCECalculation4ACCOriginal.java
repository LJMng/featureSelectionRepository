package featureSelection.repository.support.calculation.entropy.combinationConditionEntropy.positiveApproximationAccelerator;

import common.utils.MathUtils;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.algorithm.alg.positiveApproximationAccelerator.PositiveApproximationAcceleratorOriginalAlgorithm;
import featureSelection.repository.entity.alg.positiveApproximationAccelerator.EquivalenceClass;
import featureSelection.repository.support.calculation.alg.PositiveApproximationAcceleratorCalculation;
import featureSelection.repository.support.calculation.entropy.combinationConditionEntropy.DefaultCombinationConditionEntropyCalculation;

import java.util.Collection;

public class CCECalculation4ACCOriginal
	extends DefaultCombinationConditionEntropyCalculation
	implements PositiveApproximationAcceleratorCalculation<Double>
{
	private double entropy;
	@Override
	public Double getResult() {
		return entropy;
	}
	
	/**
	 * Calculated entropy result is the one with denominator part calculated: 
	 * CCE = numerator / <strong>denominator</strong>
	 */
	public CCECalculation4ACCOriginal calculate(
			Collection<EquivalenceClass> equClasses, int attributeLength, Object...args
	) {
		// Count the current calculation
		countCalculate(attributeLength);
		// Calculate
		entropy = equClasses==null || equClasses.isEmpty()? 
					0: complementEntropy(equClasses, commonDenominator((int) args[0]));
		return this;
	}

	private double complementEntropy(
			Collection<EquivalenceClass> equClasses, double denominator
	) {
		// CCE = 0
		double entropy = 0;
		// Loop over E in U
		int dSum, tmp;
		Collection<EquivalenceClass> decEquClasses;
		for (EquivalenceClass equ: equClasses) {
			// e.member
			// H = equivalenceClass(U, D)
			decEquClasses = PositiveApproximationAcceleratorOriginalAlgorithm
								.Basic
								.equivalenceClass(
										equ.getInstances(),
										new IntegerArrayIterator(0)
								);
			// dSum = 0
			dSum = 0;
			// for i=1 to |H|
			for (EquivalenceClass decEqu : decEquClasses) {
				// dSum = dSum + d
				dSum += decEqu.getInstances().size();
			}
			// CCE = CCE+dSum* dSum*(dSum-1) /2
			entropy += dSum * MathUtils.combinatorialNumOf2(dSum) / denominator;
			// for i=1 to |H|
			for (EquivalenceClass decEqu : decEquClasses) {
				tmp = decEqu.getInstances().size();
				// CCE = CCE - d*d*(d -1)/2
				entropy = entropy - tmp * MathUtils.combinatorialNumOf2(tmp) / denominator;
			}
		}
		// return CCE
		return entropy;
	}
	
	private double commonDenominator(int universeSize) {
		return universeSize * MathUtils.combinatorialNumOf2(universeSize);
	}
	
	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof EquivalenceClass;
	}
}
