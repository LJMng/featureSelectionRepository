package featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction;

import java.util.Collection;
import java.util.Map;

import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.basic.model.universe.instance.Instance;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@ReturnWrapper
public class DynamicUpdateResult<Sig> {
	private Sig significance;
	private Map<Instance, Collection<Instance>> toleranceClassesOfCondAttrs;
	private Map<Instance, Collection<Instance>> toleranceClassesOfCondAttrsNDecAttr;
}