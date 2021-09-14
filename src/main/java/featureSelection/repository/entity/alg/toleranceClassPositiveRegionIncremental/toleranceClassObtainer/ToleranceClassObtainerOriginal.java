package featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.IncompleteInstance;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.toleranceClassPositiveRegionIncremental.ToleranceClassPositiveRegionAlgorithm;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.EquivalenceClass;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesCollector;

public class ToleranceClassObtainerOriginal 
	implements ToleranceClassObtainer 
{
	
	@Override
	public Map<Instance, Collection<Instance>> obtain(
			Collection<Instance> instances, Collection<Instance> candidates,
			IntegerIterator attributes, Object... args
	) {
		if (attributes.size()==0) {
			return obtain4EmptyAttribute(instances, candidates);
		}
		
		checkExtraArguments(args);
		
		// Initialize S<sub>B</sub>(x) = [U, U, ..., U] (length=|U|)
		// (S<sub>B</sub>(x) is tolerance classes for all x in U)
		Map<Instance, Collection<Instance>> tolerances = new HashMap<>(instances.size());
		// ======================================================================================
		// | Special case:																		|
		// |   Directly handle the situation when attributes is empty, i.e. all candidates are	|
		// |   tolerant.																		|
		// ======================================================================================
		if (attributes.size()==0) {
			for (Instance ins: instances)	tolerances.put(ins, new HashSet<>(candidates));
			return tolerances;
		}
		// Collect complete instances for every attributes(using cache)
		InstancesCollector completes = (InstancesCollector) args[0];
		// For each object x in U do
		//	For each U<sub>ij</sub>(1<=j<=s)
		//		if b<sub>i</sub>(x)!=v // v is the feature value of object set U<sub>ij</sub>
		//			S<sub>B</sub>(x) = U - &Sigma <sub>i=1<sub><sup>m</sup> U<sub>ij</sub>;
		// ======================================================================================
		// | i.e.: To collect tolerances for every instance in U.			                    |
		// --------------------------------------------------------------------------------------
		// | Initiate S<sub>B</sub>(x) = { U[1]=U, U[2]=U, ..., U[|U|]=U	}			        |
		// | for each in S<sub>B</sub>(x), remove x that fits "b<sub>i</sub>(x)!=v" 	        |
		// ======================================================================================
//		long start, end;
//		long t1 = 0L, t2 = 0L;
				
		Collection<Instance> tolerance;
		for (Instance ins: instances) {
			final Collection<Instance> toBRemoved = new HashSet<>(candidates.size());
			attributes.reset();
					
			for (int i=0; i<attributes.size(); i++) {
				int attr = attributes.next();
				// skip if equClasses[i] is null
				if (!completes.containsAttribute(attr))	continue;
				// skip missing value.
				if ((ins instanceof IncompleteInstance) &&
					((IncompleteInstance) ins).isValueMissing(attr)
				)	continue;
				// Loop over equivalent classes(with no missing value)
				for (EquivalenceClass equClass: completes.get(attr)) {
					if (equClass.getAttributeValue()!=ins.getAttributeValue(attr)) {
						toBRemoved.addAll(equClass.getItems());
					}
				}
			}//
					
			// --------------------------------------------------------------------------------------
			// | Remove "toBRemoved" from candidates as tolerance of "ins".							|
			// --------------------------------------------------------------------------------------
			if (toBRemoved.isEmpty()) {
				tolerances.put(ins, new HashSet<>(candidates));
			}else if (toBRemoved.size()!=candidates.size()) {
				tolerance = candidates.stream()
										.filter(can->!toBRemoved.contains(can))
										.collect(Collectors.toList());
				tolerances.put(ins, tolerance);
			}else {
				// "ins" has no tolerance class.
			}
		}
		return tolerances;
	}

	private void checkExtraArguments(Object...args) {
		if (args==null || args.length!=1 || args[0]==null) {
			throw new IllegalStateException("CompleteInstancesCollector is required!");
		}
	}

	@Override
	public InstancesCollector getCacheInstanceGroups(Collection<Instance> instances) {
		instances = Objects.requireNonNull(instances);
		if (instances.isEmpty()) {
			throw new IllegalStateException("instances is empty!");
		}
		
		return ToleranceClassPositiveRegionAlgorithm
				.Basic
				.extractOnesNotMissing(instances, instances.iterator().next().getAttributeValues().length-1);
	}

	
	@Override
	public InstancesCollector getCacheInstanceGroups(
		Collection<Instance> instances, IntegerIterator attributes
	) {
		return ToleranceClassPositiveRegionAlgorithm
				.Basic
				.extractOnesNotMissing(instances, attributes);
	}

	
	@Override
	public String name() {
		return "Original";
	}
}
