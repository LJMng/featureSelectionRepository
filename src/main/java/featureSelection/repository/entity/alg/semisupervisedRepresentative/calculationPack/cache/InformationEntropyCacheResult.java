package featureSelection.repository.entity.alg.semisupervisedRepresentative.calculationPack.cache;

import java.util.Collection;

import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.basic.model.universe.instance.Instance;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * An entity to contain info. entropy calculation results for cache.
 */
@Data
@AllArgsConstructor
@ReturnWrapper
public class InformationEntropyCacheResult {
	private double entropyValue;
	private Collection<Collection<Instance>> equivalenceClass;
}
