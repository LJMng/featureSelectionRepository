package featureSelection.repository.entity.alg.activeSampleSelection.samplePair;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.repository.entity.alg.activeSampleSelection.EquivalenceClass;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * An entity for Sample pair selection result for incremental computations. With fields:
 * <ul>
 * 	<li><strong>{@link #updatedSamplePairFamilyMap}</strong>: {@link Map}
 * 		<p>Update Sample Pairs for incremental computations.
 * 		<p>With attributes in {@link IntArrayKey} as keys and correspondent {@link SamplePair} {@link Collection}
 * 			as values.
 * 	</li>
 * 	<li><strong>{@link #updatedEquClasses}</strong>: {@link Map}
 * 		<p>Update {@link EquivalenceClass}es for incremental computations.
 * 		<p>With attributes in {@link IntArrayKey} as keys and correspondent {@link EquivalenceClass} as values.
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Data
@AllArgsConstructor
@ReturnWrapper
public class SamplePairSelectionResult4Incremental {
	private Map<IntArrayKey, Collection<SamplePair>> updatedSamplePairFamilyMap;
	private Map<IntArrayKey, EquivalenceClass> updatedEquClasses;
	
	/**
	 * Get selected Sample Pairs in {@link Collection}.
	 * 
	 * @return A {@link Collection} of {@link SamplePair} {@link Collection}.
	 */
	public Collection<Collection<SamplePair>> getSelectedSamplePairs(){
		return updatedSamplePairFamilyMap.values();
	}
	
	/**
	 * Get selected attributes in {@link IntArrayKey} {@link Set}.
	 * 
	 * @return A {@link Set} of {@link IntArrayKey}.
	 */
	public Set<IntArrayKey> getSelectionAttributes(){
		return updatedSamplePairFamilyMap.keySet();
	}

	public Map<SamplePair, Collection<Integer>> getSamplePairMap(){
		Map<SamplePair, Collection<Integer>> map = new HashMap<>();
		
		updatedSamplePairFamilyMap.entrySet().forEach(entry->{
			entry.getValue().forEach(sp->{
				Collection<Integer> attributes = map.get(sp);
				if (attributes==null)	map.put(sp, attributes=new HashSet<>());
				for (int a: entry.getKey().key())	attributes.add(a);
			});
		});
		
		return Collections.unmodifiableMap(map);
	}
}