package featureSelection.repository.algorithm.opt.particleSwarm.roughEquivalenceClassBased.nestedEquivalenceClassBased;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.repository.algorithm.opt.particleSwarm.roughEquivalenceClassBased.RoughEquivalenceClassBasedPSO;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.FeatureImportance4NestedEquivalenceClassBased;

@RoughSet
public abstract class NestedEquivalenceClassBasedPSO<CollectionItem, Velocity,
													Cal extends FeatureImportance4NestedEquivalenceClassBased<Sig>,
													Sig extends Number>
	extends RoughEquivalenceClassBasedPSO<CollectionItem, Velocity, Cal, Sig>
{
	public NestedEquivalenceClassBasedPSO(int insSize) {
		super(insSize);
	}
}