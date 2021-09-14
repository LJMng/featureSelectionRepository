package featureSelection.repository.algorithm.opt.particleSwarm.roughEquivalenceClassBased;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.algorithm.opt.particleSwarm.AbstractHannahPSO;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@RoughSet
@ThreadSafetyNotSecured
public abstract class RoughEquivalenceClassBasedPSO<CollectionItem, Velocity, Cal extends FeatureImportance<Sig>, Sig extends Number>
	extends AbstractHannahPSO<CollectionItem, Velocity, FitnessValue<Sig>, Cal, Sig>
{
	private int universeSize;
}
