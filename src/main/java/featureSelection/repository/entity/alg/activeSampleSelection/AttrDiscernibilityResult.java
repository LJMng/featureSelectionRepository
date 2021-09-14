package featureSelection.repository.entity.alg.activeSampleSelection;

import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.activeSampleSelection.samplePair.SamplePair;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;
import java.util.Map;

/**
 * Full name: AttributeDiscernibilityResult (Attribute Discernibility Result)
 * <p>
 * An entity for Attribute Discernibility result. With fields: 
 * <li><strong>discernibilities</strong>: Collection<SamplePair>[]
 * 		<p>Collections of {@link SamplePair} corresponding to each attribute.
 * </li>
 * <li><strong>samplePairNumber</strong>: int
 * 		<p>The number of {@link SamplePair}s.
 * </li>
 * <li><strong>discernibilityMatrix</strong>: Map
 * 		<p>The correspondent discernibility matrix. A map with {@link Instance} as keys and
 * 			{@link Map}s which contains another {@link Instance} and a {@link Collection}
 * 			of attribute indexes that discerns the two {@link Instance}s as values.
 * </li>
 * 
 * @author Benjamin_L
 */
@Data
@AllArgsConstructor
@ReturnWrapper
public class AttrDiscernibilityResult {
	private Collection<SamplePair>[] discernibilities;
	private int samplePairNumber;
	
	// <Universe instance with smaller ID <Universe instance with larger ID, attributes with
	// 	discernibility>>
	private Map<Instance, Map<Instance, Collection<Integer>>> discernibilityMatrix;
}
