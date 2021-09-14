package featureSelection.repository.entity.alg.activeSampleSelection;

import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.repository.entity.alg.activeSampleSelection.samplePair.SamplePair;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

/**
 * An entity for Discern Intersection result. With the following fields:
 * <li><code>intersection</code>: 
 * <p>A collection of {@link SamplePair}.
 * </li>
 * <li><code>selectionAttributes</code>: 
 * <p>A collection of attribute values.
 * </li>
 * 
 * @author Benjamin_L
 */
@Data
@AllArgsConstructor
@ReturnWrapper
public class DiscernIntersectionResult {
	private Collection<SamplePair> intersection;
	private Collection<Integer> selectionAttributes;
}
