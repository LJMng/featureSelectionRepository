package featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import featureSelection.basic.model.universe.instance.Instance;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PreviousInfoPack {
	private Collection<Instance> instances;
	private Map<Instance, Collection<Instance>> tolerancesOfCondAttrs;
	private Map<Instance, Collection<Instance>> tolerancesOfCondAttrsNDecAttrs;
	
	private Collection<Integer> reduct;
	private Map<Instance, Collection<Instance>> tolerancesOfReduct;
	private Map<Instance, Collection<Instance>> tolerancesOfReductNDecAttrs;
	
	public PreviousInfoPack(
			Collection<Instance> originalInstances,
			Map<Instance, Collection<Instance>> previousTolerancesOfCondAttrs,
			Map<Instance, Collection<Instance>> previousTolerancesOfCondAttrsNDecAttrs,
			Collection<Integer> previousReduct,
			Map<Instance, Collection<Instance>> previousTolerancesOfReduct,
			Map<Instance, Collection<Instance>> previousTolerancesOfReductNDecAttrs
	) {
		this.instances = Collections.unmodifiableCollection(originalInstances);
		this.tolerancesOfCondAttrs = Collections.unmodifiableMap(previousTolerancesOfCondAttrs);
		this.tolerancesOfCondAttrsNDecAttrs = Collections.unmodifiableMap(previousTolerancesOfCondAttrsNDecAttrs);
		this.reduct = previousReduct;
		this.tolerancesOfReduct = Collections.unmodifiableMap(previousTolerancesOfReduct);
		this.tolerancesOfReductNDecAttrs = Collections.unmodifiableMap(previousTolerancesOfReductNDecAttrs);
	}
}