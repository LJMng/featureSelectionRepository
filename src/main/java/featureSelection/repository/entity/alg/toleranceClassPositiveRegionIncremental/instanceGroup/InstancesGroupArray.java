package featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup;

import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.EquivalenceClass;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class InstancesGroupArray
	implements InstancesCollector
{

	private Collection<EquivalenceClass>[] completeInsArray;
	
	@SuppressWarnings("unchecked")
	public InstancesGroupArray(int attributeSizeOfUniverseInstance) {
		completeInsArray = new Collection[attributeSizeOfUniverseInstance];
	}
	
	public void set(int attribute, Collection<EquivalenceClass> completeInstances) {
		completeInsArray[attribute-1] = completeInstances;
	}

	@Override
	public Collection<EquivalenceClass> get(int attribute) {
		return completeInsArray[attribute-1];
	}

	@Override
	public boolean containsAttribute(int attribute) {
		return completeInsArray[attribute-1]!=null && !completeInsArray[attribute-1].isEmpty();
	}

	@Override
	public Iterator<Collection<EquivalenceClass>> iterator() {
		return Arrays.stream(completeInsArray).iterator();
	}
	
}