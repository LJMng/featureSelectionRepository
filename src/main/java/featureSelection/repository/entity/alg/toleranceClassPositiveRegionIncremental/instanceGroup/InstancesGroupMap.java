package featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup;

import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.EquivalenceClass;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class InstancesGroupMap implements InstancesCollector {

	private Map<Integer, Collection<EquivalenceClass>> completeInsMap;
	
	public InstancesGroupMap(int attributeSize) {
		this.completeInsMap = new HashMap<>(attributeSize);
	}
	
	public void set(int attribute, Collection<EquivalenceClass> completeInstances) {
		completeInsMap.put(attribute, completeInstances);
	}

	@Override
	public Collection<EquivalenceClass> get(int attribute) {
		return completeInsMap.get(attribute);
	}

	@Override
	public boolean containsAttribute(int attribute) {
		return completeInsMap.containsKey(attribute);
	}

	@Override
	public Iterator<Collection<EquivalenceClass>> iterator() {
		return completeInsMap.values().iterator();
	}
}