package featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased;

import featureSelection.repository.entity.alg.rec.classSet.interf.extension.decisionMap.DecisionInfoExtendedClassSet;

import java.util.Collection;

public interface RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<V>
	extends FeatureImportance4RoughEquivalenceClassBased<V>
{
	RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<V> calculate(
			Collection<? extends DecisionInfoExtendedClassSet<?, ?>> decisionInfos,
			int attributeLength, 
			Object...args
	);
	RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<V> calculate(
			DecisionInfoExtendedClassSet<?, ?> decisionInfo, 
			int attributeLength, 
			Object...args
	);
}