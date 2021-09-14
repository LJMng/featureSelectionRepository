package featureSelection.repository.entity.opt.particleSwarm.impl.inertiaWeight;

import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.particleSwarm.interf.inertiaWeight.InertiaWeightAlgorithm;

public class IterationBasedDoubleInertiaWeightAlgorithm
	implements InertiaWeightAlgorithm<Double>
{
	@Override
	public Double updateInertiaWeight(
			GenerationRecord<?, ?, ?> geneRationRecord, ReductionParameters<?, ?, ?> optParams
	) {
		double maxW = (Double) optParams.getInertiaWeightAlgorithmParameters()
										.getMaxInertiaWeight();
		double minW = (Double) optParams.getInertiaWeightAlgorithmParameters()
										.getMinInertiaWeight();
		double minus = maxW-minW;
		return maxW - minus / optParams.getIteration() * geneRationRecord.getGeneration();
	}
}