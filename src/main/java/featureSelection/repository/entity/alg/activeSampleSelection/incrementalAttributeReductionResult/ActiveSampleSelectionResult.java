package featureSelection.repository.entity.alg.activeSampleSelection.incrementalAttributeReductionResult;

import featureSelection.basic.annotation.common.ReturnWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

/**
 * An entity to contain the result of Active Sample Selection:
 * <ul>
 *     <li>{@link #reduct}</li>
 *     <li>{@link #samplePairInfo}</li>
 * </ul>
 * 
 * @author Benjamin_L
 *
 * @param <SamplePairInfo>
 * 		Type of sample pair info.
 */
@Data
@AllArgsConstructor
@ReturnWrapper
public abstract class ActiveSampleSelectionResult<SamplePairInfo> implements Cloneable {
	private Collection<Integer> reduct;
	private SamplePairInfo samplePairInfo;
	
	public abstract ActiveSampleSelectionResult<SamplePairInfo> clone();
}