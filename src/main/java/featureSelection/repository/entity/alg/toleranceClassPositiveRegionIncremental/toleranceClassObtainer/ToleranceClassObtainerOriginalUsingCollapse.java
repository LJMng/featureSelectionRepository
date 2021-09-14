package featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.IncompleteInstance;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.toleranceClassPositiveRegionIncremental.ToleranceClassPositiveRegionAlgorithm;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.EquivalenceClass;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesCollector;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;


public class ToleranceClassObtainerOriginalUsingCollapse 
	implements ToleranceClassObtainer 
{
	
	@Override
	public Map<Instance, Collection<Instance>> obtain(
			Collection<Instance> instances, Collection<Instance> candidates,
			IntegerIterator attributes, Object...args
	) {
		if (attributes.size()==0) {
			return obtain4EmptyAttribute(instances, candidates);
		}
		
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
		// Collect complete instances for every attribute(using cache)
		InstancesCollector instanceGroups =
				args!=null && args.length>0?
					(InstancesCollector) args[0]: 
					getCacheInstanceGroups(candidates, attributes);
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
		Collection<Instance> tolerance;
		for (Instance ins: instances) {
			tolerance = toleranceOf(ins, attributes, instanceGroups, instances);
			if (tolerance!=null)	tolerances.put(ins, tolerance);
		}
		return tolerances;
	}

	@Override
	public InstancesCollector getCacheInstanceGroups(Collection<Instance> instances) {
		instances = Objects.requireNonNull(instances);
		if (instances.isEmpty()) {
			throw new IllegalStateException("instances is empty!");
		}
		
		return ToleranceClassPositiveRegionAlgorithm
				.Basic
				.attributeGroups(instances, instances.size());
	}
	@Override
	public InstancesCollector getCacheInstanceGroups(
			Collection<Instance> instances, IntegerIterator attributes
	) {
		instances = Objects.requireNonNull(instances);
		if (instances.isEmpty()) {
			throw new IllegalStateException("instances is empty!");
		}
		
		return ToleranceClassPositiveRegionAlgorithm
				.Basic
				.attributeGroups(instances, attributes);
	}

	
	private Collection<Instance> toleranceOf(
			Instance target, IntegerIterator attributes, InstancesCollector instanceGroups,
			Collection<Instance> instances
	){
		// Collect tolerant instances for each attributes.
		Collection<Instance> smallestGroup = null;
		final Collection<Collection<Instance>> attributeValueMatchGroups = new LinkedList<>();

		attributes.reset();
		for (int i=0; i<attributes.size(); i++) {
			int attr = attributes.next();
			int insAttrValue = target.getAttributeValue(attr);
			
			Collection<EquivalenceClass> group = instanceGroups.get(attr);
			
			if (insAttrValue== IncompleteInstance.MISSING_VALUE) {
				Collection<Instance> matches = new HashSet<>(instances);
				if (smallestGroup==null) {
					smallestGroup = matches;
				}
				attributeValueMatchGroups.add(matches);
			}else {
				Collection<Instance> matches = new LinkedList<>();
				
				for (EquivalenceClass equClass: group) {
					if (// missing value
						equClass.getAttributeValue()==IncompleteInstance.MISSING_VALUE ||
						// complete value
						equClass.getAttributeValue()==insAttrValue
					) {
						matches.addAll(equClass.getItems());
					}
				}

				if (smallestGroup==null || matches.size()<smallestGroup.size()) {
					smallestGroup = matches;
				}
				attributeValueMatchGroups.add(new HashSet<>(matches));
			}
		}
		
		if (smallestGroup==null) {
			return Collections.emptyList();
		}else {
			final Collection<Instance> tolerance = smallestGroup;
			
			for (Collection<Instance> group: attributeValueMatchGroups) {
				if (group!=smallestGroup) {
					tolerance.retainAll(group);
				}
			}
			return tolerance;
		}
	}//*/

	@Override
	public String name() {
		return "Original-Collapse";
	}
}