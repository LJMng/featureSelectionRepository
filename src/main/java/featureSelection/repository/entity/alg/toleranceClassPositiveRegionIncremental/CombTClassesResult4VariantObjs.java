package featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental;

import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.basic.model.universe.instance.Instance;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;
import java.util.Map;

/**
 * Full name: CombinedToleranceClassesResult4VariantObjects (Combined Tolerance Classes Result 4 Variant 
 * Objects)
 * <p>
 * An entity for the result of combined tolerance classes in variant objects process.
 * 
 * @author Benjamin_L
 */
@Data
@AllArgsConstructor
@ReturnWrapper
public class CombTClassesResult4VariantObjs {
	
	private Collection<Instance> invariancesUpdated;
	private Collection<Instance> variancesUpdated;
	private Map<Instance, Collection<Instance>> tolerancesOfInvariances;
	private Map<Instance, Collection<Instance>> tolerancesOfVariances;
	private Map<Instance, Collection<Instance>> tolerancesOfCombined;

	public boolean anyUpdate() {
		return invariancesUpdated!=null && variancesUpdated!=null &&
				(!invariancesUpdated.isEmpty() || !variancesUpdated.isEmpty());
	}
}