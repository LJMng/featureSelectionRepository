package featureSelection.repository.support.calculation.inConsistency.positiveApproximationAccelerator;

import java.util.Collection;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.repository.algorithm.alg.positiveApproximationAccelerator.PositiveApproximationAcceleratorOriginalAlgorithm;
import featureSelection.repository.entity.alg.positiveApproximationAccelerator.EquivalenceClass;
import featureSelection.repository.support.calculation.alg.PositiveApproximationAcceleratorCalculation;
import featureSelection.repository.support.calculation.inConsistency.DefaultInConsistencyCalculation;
import lombok.Getter;

public class InConsistencyCalculation4ACCOriginal 
	extends DefaultInConsistencyCalculation
	implements PositiveApproximationAcceleratorCalculation<Integer>
{
	@Getter private int inConsistency;
	@Override
	public Integer getResult() {
		return inConsistency;
	}
	
	public InConsistencyCalculation4ACCOriginal calculate(Collection<EquivalenceClass> records, int attributeLength, Object...args) {
		// Count the current calculation
		countCalculate(attributeLength);
		// Calculate
		inConsistency = records==null || records.isEmpty() ? 0: inConsistency(records);
		return this;
	}
	
	private static int inConsistency(Collection<EquivalenceClass> records) {
		// incon = 0
		int inCon = 0;
		// Go through e in U
		int dSum, maxNum = 0;
		Collection<EquivalenceClass> decEquClasses;
		for (EquivalenceClass equClass : records) {
			// e.member
			// H = equivalenceClass(equClass, D)
			decEquClasses = PositiveApproximationAcceleratorOriginalAlgorithm
								.Basic
								.equivalenceClass(equClass.getInstances(), new IntegerArrayIterator(0));
			// dSum = 0
			dSum = 0;
			// Go through d in H
			maxNum = 0;
			for (EquivalenceClass decEquClass: decEquClasses) {
				dSum += decEquClass.getInstances().size();
				if (decEquClass.getInstances().size()>maxNum)
					maxNum = decEquClass.getInstances().size();
			}
			inCon += dSum - maxNum;
		}
		return inCon;
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof EquivalenceClass;
	}
}