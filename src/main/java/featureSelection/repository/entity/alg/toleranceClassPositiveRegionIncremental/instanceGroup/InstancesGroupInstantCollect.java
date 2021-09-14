package featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.toleranceClassPositiveRegionIncremental.ToleranceClassPositiveRegionAlgorithm;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.EquivalenceClass;

import java.util.Collection;
import java.util.Iterator;

public class InstancesGroupInstantCollect implements InstancesCollector {

	private Collection<Instance> instances;
	
	public InstancesGroupInstantCollect(Collection<Instance> universeInstances) {
		this.instances = universeInstances;
	}
	
	public void set(int attribute, Collection<EquivalenceClass> completeInstances) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<EquivalenceClass> get(int attribute) {
		return ToleranceClassPositiveRegionAlgorithm
				.Basic
				.extractOnesNotMissing(instances, attribute)
				.get(attribute);
	}

	@Override
	public boolean containsAttribute(int attribute) {
		return true;
	}

	
	@Override
	public Iterator<Collection<EquivalenceClass>> iterator() {
		throw new UnsupportedOperationException();
	}
}