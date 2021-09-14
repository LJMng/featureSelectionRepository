package featureSelection.repository.support.calculation.entropy.combinationConditionEntropy.compactedDecisionTableAlgorithm;

import common.utils.MathUtils;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.repository.entity.alg.compactedDecisionTable.interf.CompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.interf.DecisionNumber;
import featureSelection.repository.support.calculation.alg.CompactedDecisionTableCalculation;
import featureSelection.repository.support.calculation.entropy.combinationConditionEntropy.DefaultCombinationConditionEntropyCalculation;

import java.util.Collection;

public class CCECalculation4CTOriginalHash
	extends DefaultCombinationConditionEntropyCalculation
	implements CompactedDecisionTableCalculation<Double>
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
	public CCECalculation4CTOriginalHash calculate(
			Collection<? extends CompactedTableRecord<? extends DecisionNumber>> records,
			int attributeLength, Object...args
	) {
		// Count the current calculation
		countCalculate(attributeLength);
		// Calculate
		entropy = records==null || records.isEmpty()? 
					0.0: 
					complementEntropy(records, commonDenominator((int) args[0]));
		return this;
	}

	private static double complementEntropy(
		Collection<? extends CompactedTableRecord<? extends DecisionNumber>> records,
		double denominator
	) {
		// CCE = 0
		Double entropy = 0.0;
		// Go through c in CTHash
		int number;
		double dSum;
		IntegerIterator dValue;
		for (CompactedTableRecord<? extends DecisionNumber> record : records) {
			// dSum=0
			dSum = 0;
			// c.dValue
			dValue = record.getDecisionNumbers()
							.numberValues();
			// for i=1 to |c.dValue|
			dValue.reset();
			while (dValue.hasNext()) {
				number = dValue.next();
				// dSum = dSum + c.dValue[i].number;
				dSum += number;
			}
			// CCE = CCE+dSum* dSum*(dSum-1) /2
			entropy += (dSum * MathUtils.combinatorialNumOf2((int) dSum) / denominator);
			// for i=1 to |c.dValue|
			dValue.reset();
			while (dValue.hasNext()) {
				number = dValue.next();
				// CCE = CCE - c.dValue[i] * c.dValue[i]*( c.dValue[i]-1)/2
				entropy = entropy - number * MathUtils.combinatorialNumOf2(number) / denominator;
			}
		}
		// return CCE/ (|U|^2*|U|(|U|-1)/2)
		return entropy;
	}
	
	private double commonDenominator(int universeSize) {
		return universeSize * MathUtils.combinatorialNumOf2(universeSize);
	}
	
	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof CompactedTableRecord;
	}
}