package featureSelection.repository.entity.opt.particleSwarm.impl.inertiaWeight;

import featureSelection.repository.entity.opt.particleSwarm.interf.inertiaWeight.InertiaWeightAlgorithmParameters;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DefaultInertiaWeightAlgorithmParameters
	implements InertiaWeightAlgorithmParameters<Double>
{
	@Getter private Double minInertiaWeight;
	@Getter private Double maxInertiaWeight;
}