package featureSelection.repository.algorithm.opt.improvedHarmonySearch.roughEquivalenceClassBased.nestedEquivalenceClassBased;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.repository.algorithm.opt.improvedHarmonySearch.roughEquivalenceClassBased.RoughEquivalenceClassBasedIHS;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.FeatureImportance4NestedEquivalenceClassBased;

@RoughSet
@ThreadSafetyNotSecured
public abstract class NestedEquivalenceClassBasedIHS<Cal extends FeatureImportance4NestedEquivalenceClassBased<Sig>,
													Sig extends Number, 
													CollectionItem>
	extends RoughEquivalenceClassBasedIHS<Cal, Sig, CollectionItem>
{
	public NestedEquivalenceClassBasedIHS(int insSize) {
		super(insSize);
	}
}