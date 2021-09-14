package featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesCollector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface ToleranceClassObtainer {
	
	String name();

	Map<Instance, Collection<Instance>> obtain(
			Collection<Instance> instances, Collection<Instance> candidates,
			IntegerIterator attributes, Object...args
	);

	InstancesCollector getCacheInstanceGroups(Collection<Instance> instances);
	InstancesCollector getCacheInstanceGroups(Collection<Instance> instances, IntegerIterator attributes);

	default Map<Instance, Collection<Instance>> obtain4EmptyAttribute(
		Collection<Instance> instances, Collection<Instance> candidates
	){
		Map<Instance, Collection<Instance>> tolerances =
				instances.stream().collect(
					Collectors.toMap(Function.identity(), ins->new HashSet<>(candidates))
				);
		return tolerances;
	}
}