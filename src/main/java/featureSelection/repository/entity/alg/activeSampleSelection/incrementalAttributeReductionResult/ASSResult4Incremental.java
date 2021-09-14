package featureSelection.repository.entity.alg.activeSampleSelection.incrementalAttributeReductionResult;

import java.util.*;

import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.activeSampleSelection.samplePair.SamplePair;
import lombok.Getter;
import lombok.Setter;

/**
 * Active Sample Selection result for incremental computing.
 * <p>
 * An entity extends {@link ActiveSampleSelectionResult}, specialised for incremental computing:
 * <ul>
 *     <li>{@link #updatedUniverse}</li>
 * </ul>
 * 
 * @see ActiveSampleSelectionResult
 * 
 * @author Benjamin_L
 */
@ReturnWrapper
public class ASSResult4Incremental 
	extends ActiveSampleSelectionResult<Map<IntArrayKey, Collection<SamplePair>>>
{
	@Setter @Getter private Collection<Instance> updatedUniverse;
	
	public ASSResult4Incremental(
		Collection<Integer> reduct, Map<IntArrayKey, Collection<SamplePair>> samplePairInfo
	) {
		super(reduct, samplePairInfo);
	}

	/**
	 * Get sample pair in {@link Map}:
	 * <pre>{sample pair: attributes that can discern the sample pair}</pre>
	 *
	 * @return Sample pair - attribute {@link Collection} {@link Map}.
	 */
	public Map<SamplePair, Collection<Integer>> getSamplePairMap(){
		Map<SamplePair, Collection<Integer>> map = new HashMap<>();
		getSamplePairInfo().entrySet().stream().forEach(entry->{
			int[] attributes = entry.getKey().key();
			entry.getValue().forEach(samplePair->{
				Collection<Integer> minimalElements = map.get(samplePair);
				if (minimalElements==null)	map.put(samplePair, minimalElements=new HashSet<>());
				for (int attr: attributes)	minimalElements.add(attr);
			});
		});
		return map;
	}

	@Override
	public ActiveSampleSelectionResult<Map<IntArrayKey, Collection<SamplePair>>> clone() {
		Map<IntArrayKey, Collection<SamplePair>> samplePairInfo = new HashMap<>(this.getSamplePairInfo().size());
		this.getSamplePairInfo().entrySet().stream().forEach(entry->{
			samplePairInfo.put(entry.getKey(), new LinkedList<>(entry.getValue()));
		});
		
		ASSResult4Incremental clone = 
			new ASSResult4Incremental(
				new LinkedList<>(getReduct()), 
				samplePairInfo
			);
		clone.setUpdatedUniverse(updatedUniverse);
		return clone;
	}
}