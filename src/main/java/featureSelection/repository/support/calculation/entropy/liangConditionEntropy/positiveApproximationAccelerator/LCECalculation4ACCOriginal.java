package featureSelection.repository.support.calculation.entropy.liangConditionEntropy.positiveApproximationAccelerator;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.algorithm.alg.positiveApproximationAccelerator.PositiveApproximationAcceleratorOriginalAlgorithm;
import featureSelection.repository.entity.alg.positiveApproximationAccelerator.EquivalenceClass;
import featureSelection.repository.support.calculation.alg.PositiveApproximationAcceleratorCalculation;
import featureSelection.repository.support.calculation.entropy.liangConditionEntropy.DefaultLiangConditionEntropyCalculation;

import java.util.Collection;

public class LCECalculation4ACCOriginal
	extends DefaultLiangConditionEntropyCalculation
	implements PositiveApproximationAcceleratorCalculation<Double>
{
	private double entropy;
	@Override
	public Double getResult() {
		return entropy;
	}
	
	/**
	 * Calculated entropy result is the one <strong>without</strong> denominator part(|U|): 
	 * LCE = numerator <del>/ denominator</del>
	 */
	public LCECalculation4ACCOriginal calculate(Collection<EquivalenceClass> equClasses, int attributeLength, Object...args) {
		// Count the current calculation
		countCalculate(attributeLength);
		// Calculate
		entropy = equClasses==null||equClasses.isEmpty()? 0: liangConditionalEntropy(equClasses);
		return this;
	}

	private double liangConditionalEntropy(Collection<EquivalenceClass> equClasses) {
		// LCE = 0
		double entropy = 0;
		// Loop over E in U
		Collection<EquivalenceClass> decEquClasses;
		for (EquivalenceClass equ: equClasses) {
			// e.member
			// H = equivalentClass(U, D)
			decEquClasses = PositiveApproximationAcceleratorOriginalAlgorithm
								.Basic
								.equivalenceClass(equ.getInstances(), new IntegerArrayIterator(0));
			
			for (EquivalenceClass decEqu : decEquClasses) {
				// LCE = LCE + |X[i] ∩ Y[j]||X[i]- X[i] ∩ Y[j]|
				entropy += decEqu.getInstances().size() * (equ.getInstances().size() - decEqu.getInstances().size());
			}//*/
		}
		// return LCE
		return entropy;
	}
	
	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof EquivalenceClass;
	}
}
