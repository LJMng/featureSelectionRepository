package featureSelection.repository.entity.alg.activeSampleSelection.incrementalAttributeReductionResult;

import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.repository.entity.alg.activeSampleSelection.samplePair.SamplePair;

import java.util.*;

/**
 * Full name: ActiveSampleSelectionResult4Static (Active Sample Selection Result 4 Static)
 * <p>
 * An entity extends {@link ActiveSampleSelectionResult}, specialise for static:
 *
 * @see ActiveSampleSelectionResult
 * 
 * @author Benjamin_L
 */
@ReturnWrapper
public class ASSResult4Static 
	extends ActiveSampleSelectionResult<Map<SamplePair, Collection<Integer>>>
{
	public ASSResult4Static(
		Collection<Integer> reduct, Map<SamplePair, Collection<Integer>> samplePairInfo
	) {
		super(reduct, samplePairInfo);
	}

	@Override
	public ActiveSampleSelectionResult<Map<SamplePair, Collection<Integer>>> clone() {
		Map<SamplePair, Collection<Integer>> samplePairInfo = new HashMap<>(this.getSamplePairInfo().size());
		
		this.getSamplePairInfo().entrySet().stream().forEach(entry->{
			samplePairInfo.put(entry.getKey(), new HashSet<>(entry.getValue()));
		});
		
		return new ASSResult4Static(new LinkedList<>(this.getReduct()), samplePairInfo);
	}
}
