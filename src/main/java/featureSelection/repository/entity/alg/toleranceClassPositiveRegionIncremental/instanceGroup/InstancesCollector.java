package featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup;

import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.EquivalenceClass;

import java.util.Collection;

public interface InstancesCollector extends Iterable<Collection<EquivalenceClass>> {
	
	boolean containsAttribute(int attribute);
	
	void set(int attribute, Collection<EquivalenceClass> completeInstances);
	
	Collection<EquivalenceClass> get(int attribute);
	
}
