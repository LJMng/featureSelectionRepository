package featureSelection.repository.algorithm.opt.artificialFishSwarm.roughEquivalentClassBased;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.ReductionAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@RoughSet
@ThreadSafetyNotSecured
public abstract class RoughEquivalenceClassBasedFSA<FI extends FeatureImportance<Sig>, Sig extends Number, CollectionItem>
	implements ReductionAlgorithm<FI, Sig, CollectionItem>
{
	private int instanceSize;
}
