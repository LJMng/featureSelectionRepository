package featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup;

import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.EquivalenceClass;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class InstancesGroup4Empty implements InstancesCollector {

	@Override
	public Iterator<Collection<EquivalenceClass>> iterator() {
		return Collections.emptyIterator();
	}

	@Override
	public boolean containsAttribute(int attribute) {
		return false;
	}

	@Override
	public void set(int attribute, Collection<EquivalenceClass> completeInstances) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<EquivalenceClass> get(int attribute) {
		return null;
	}

}
