package featureSelection.repository.support.calculation.entropy.liangConditionEntropy.roughEquivalentClassBased.extension.IncrementalDecision;

import featureSelection.repository.entity.alg.rec.classSet.interf.extension.decisionMap.DecisionInfoExtendedClassSet;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation;
import featureSelection.repository.support.calculation.entropy.liangConditionEntropy.DefaultLiangConditionEntropyCalculation;

import java.util.Collection;

public class LCECalculation4IDREC
	extends DefaultLiangConditionEntropyCalculation
	implements RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Double>
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
	public LCECalculation4IDREC calculate(DecisionInfoExtendedClassSet<?, ?> decisionInfo, int attributeLength, Object...args) {
		// Count the current calculation
		countCalculate(attributeLength);
		// Calculate
		entropy = decisionInfo==null? 0: liangConditionalEntropy(decisionInfo);
		return this;
	}
	public LCECalculation4IDREC calculate(Collection<? extends DecisionInfoExtendedClassSet<?, ?>> decisionInfos, int attributeLength, Object...args) {
		// Count the current calculation
		countCalculate(attributeLength);
		// Calculate
		entropy = 0;
		if (decisionInfos!=null) {
			for (DecisionInfoExtendedClassSet<?, ?> decisionInfo: decisionInfos) {
				entropy = this.plus(entropy, liangConditionalEntropy(decisionInfo));
			}
		}
		return this;
	}
	
	private double liangConditionalEntropy(DecisionInfoExtendedClassSet<?, ?> decisionInfo) {
		// LCE = 0
		double entropy = 0;
		// Go through E in U
		Collection<Integer> dValues;
		// dSum = E.universeSize;
		// E.dValue
		dValues = decisionInfo.numberValues();
		// for i=1 to |E.dValue|
		for (int number: dValues) {
			// LCE = LCE + E.dValue[i].num * (dSum - c.dValue[i].num)
			entropy += number * ((long) decisionInfo.getInstanceSize() - (long) number);
		}
		return entropy;
	}
	
	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof DecisionInfoExtendedClassSet;
	}
}
