package featureSelection.repository.support.calculation.inConsistency.roughEquivalentClassBased;

import java.util.Collection;

import featureSelection.repository.entity.alg.rec.classSet.interf.extension.decisionMap.DecisionInfoExtendedClassSet;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation;
import featureSelection.repository.support.calculation.inConsistency.DefaultInConsistencyCalculation;
import lombok.Getter;

public class InConsistencyCalculation4IDREC 
	extends DefaultInConsistencyCalculation
	implements RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Integer>
{
	@Getter private int inConsistency = 0;
	@Override
	public Integer getResult() {
		return inConsistency;
	}

	public InConsistencyCalculation4IDREC calculate(
			Collection<? extends DecisionInfoExtendedClassSet<?, ?>> decisionInfos,
			int attributeLength, Object...args
	) {
		// Count the current calculation
		countCalculate(attributeLength);
		// Calculate
		inConsistency = 0;
		if (decisionInfos!=null) {
			for (DecisionInfoExtendedClassSet<?, ?> info: decisionInfos) {
				inConsistency = plus(inConsistency, inConsistency(info));
			}
		}
		return this;
	}
	
	public InConsistencyCalculation4IDREC calculate(
			DecisionInfoExtendedClassSet<?, ?> decisionInfo, int attributeLength,
			Object...args
	) {
		// Count the current calculation
		countCalculate(attributeLength);
		// Calculate
		inConsistency = inConsistency(decisionInfo);
		return this;
	}
	
	private int inConsistency(DecisionInfoExtendedClassSet<?, ?> decisionInfo) {
		if (decisionInfo==null)	return 0;
		int maxNum = 0;
		for (int number : decisionInfo.numberValues())	if (maxNum<number)	maxNum = number;
		return decisionInfo.getInstanceSize() - maxNum;
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof DecisionInfoExtendedClassSet<?, ?>;
	}
}
