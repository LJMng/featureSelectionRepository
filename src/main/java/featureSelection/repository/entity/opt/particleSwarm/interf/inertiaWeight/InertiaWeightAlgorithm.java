package featureSelection.repository.entity.opt.particleSwarm.interf.inertiaWeight;

import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;

/**
 * Inertia weight algorithm for Particle Swarm Optimization.
 *
 * @see InertiaWeightAlgorithmParameters
 *
 * @author Benjamin_L
 *
 * @param <Weight>
 * 		Type of inertia weight.
 */
public interface InertiaWeightAlgorithm<Weight> {
	Weight updateInertiaWeight(
			GenerationRecord<?, ?, ?> geneRationRecord, ReductionParameters<?, ?, ?> optParams
	);
}
