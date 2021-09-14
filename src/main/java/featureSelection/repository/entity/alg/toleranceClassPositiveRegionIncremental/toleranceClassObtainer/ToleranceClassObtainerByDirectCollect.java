package featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.IncompleteInstance;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesCollector;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;


public class ToleranceClassObtainerByDirectCollect 
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
			tolerance = new LinkedList<>();
			
			CandidateLoop:
			for (Instance candidate: candidates) {
				attributes.reset();
				for (int i=0; i<attributes.size(); i++) {
					int attr = attributes.next();
					// skip missing value.
					if ((ins instanceof IncompleteInstance) &&
						((IncompleteInstance) ins).isValueMissing(attr)
					)	continue;

					if ((candidate instanceof IncompleteInstance) &&
						((IncompleteInstance) candidate).isValueMissing(attr)
					)	continue;
					
					if (candidate.getAttributeValue(attr)!=ins.getAttributeValue(attr)) {
						continue CandidateLoop;
					}
				}
				tolerance.add(candidate);
			}
			if (!tolerance.isEmpty())	tolerances.put(ins, tolerance);
		}
		return tolerances;
	}
	
	@Override
	public InstancesCollector getCacheInstanceGroups(Collection<Instance> instances) {
		return null;
	}
	
	@Override
	public InstancesCollector getCacheInstanceGroups(
		Collection<Instance> instances, IntegerIterator attributes
	) {
		return null;
	}

	
	@Override
	public String name() {
		return "Direct Collect";
	}
}
