package featureSelection.repository.entity.opt.particleSwarm.interf.inertiaWeight;

/**
 * Interface for {@link InertiaWeightAlgorithm}'s parameters.
 *
 * @see InertiaWeightAlgorithm
 *
 * @param <Weight>
 * 		Class type of inertia weight.
 */
public interface InertiaWeightAlgorithmParameters<Weight> {
	Weight getMinInertiaWeight();
	Weight getMaxInertiaWeight();
}