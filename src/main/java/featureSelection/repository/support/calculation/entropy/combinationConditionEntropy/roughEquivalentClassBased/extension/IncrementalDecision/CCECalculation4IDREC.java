package featureSelection.repository.support.calculation.entropy.combinationConditionEntropy.roughEquivalentClassBased.extension.IncrementalDecision;

import common.utils.MathUtils;
import featureSelection.repository.entity.alg.rec.classSet.interf.extension.decisionMap.DecisionInfoExtendedClassSet;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation;
import featureSelection.repository.support.calculation.entropy.combinationConditionEntropy.DefaultCombinationConditionEntropyCalculation;

import java.util.Collection;

public class CCECalculation4IDREC
	extends DefaultCombinationConditionEntropyCalculation
	implements RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Double>
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
	public CCECalculation4IDREC calculate(DecisionInfoExtendedClassSet<?, ?> decisionInfo, int attributeLength, Object...args) {
		// Count the current calculation
		countCalculate(attributeLength);
		// Calculate
		if (decisionInfo!=null) {
			entropy = complementEntropy(decisionInfo) / commonDenominator((int) args[0]);
		}
		return this;
	}
	public CCECalculation4IDREC calculate(Collection<? extends DecisionInfoExtendedClassSet<?, ?>> decisionInfos, int attributeLength, Object...args) {
		// Count the current calculation
		countCalculate(attributeLength);
		// Calculate
		entropy = 0;
		if (decisionInfos!=null) {
			for (DecisionInfoExtendedClassSet<?, ?> decisionInfo: decisionInfos) {
				entropy = plus(entropy, complementEntropy(decisionInfo) / commonDenominator((int) args[0]));
			}
		}
		return this;
	}
	
	private double complementEntropy(DecisionInfoExtendedClassSet<?, ?> decisionInfo) {
		// CCE = 0
		double entropy = 0;
		// Go through E in U
		Collection<Integer> dValue;
		// dSum = U.size;
		// E.dValue
		dValue = decisionInfo.numberValues();
		// CCE = CCE+dSum*dSum*(dSum-1)/2
		entropy += decisionInfo.getInstanceSize() * MathUtils.combinatorialNumOf2(decisionInfo.getInstanceSize());
		// for i=1 to |E.dValue|
		for (int number: dValue) {
			// CCE = CCE - E.dValue[i].num * c.dValue[i].num*( c.dValue[i].num-1)/2
			entropy = entropy - number * MathUtils.combinatorialNumOf2(number);
		}
		return entropy;
	}
	
	private double commonDenominator(int instanceSize) {
//		return 1;
		return instanceSize * MathUtils.combinatorialNumOf2(instanceSize);
	}
	
	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof DecisionInfoExtendedClassSet;
	}
}