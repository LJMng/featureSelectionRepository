package featureSelection.repository.algorithm.opt.artificialFishSwarm.classic;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.ReductionAlgorithm;

@RoughSet
public interface ClassicAttributeReductionFSA<FI extends FeatureImportance<Double>>
	extends ReductionAlgorithm<FI, Double, Instance>
{}