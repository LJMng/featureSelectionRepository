package featureSelection.repository.entity.alg.rec.extension.incrementalPartition;

import java.util.Collection;

import featureSelection.basic.annotation.common.ReturnWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * An entity for <i>Incremental Partition Nested Equivalent Class based</i> (<strong>IP-NEC</strong>) 
 * partition result. Including the following fields:
 * <ul>
 * 	<li><strong>positive</strong>: int</li>
 * 	<li><strong>emptyBoundaryClassSetTypeClass</strong>: boolean</li>
 * 	<li><strong>attributes</strong>: AttributeSet</li>
 * 	<li><strong>roughClasses</strong>: RoughClasses</li>
 * </ul>
 * 
 * @author Benjamin_L
 *
 * @param <AttributeSet>
 * 		Type of attributes set. Could be <code>int[]</code>, or {@link Integer} {@link Collection} etc.
 * @param <RoughEquClasses>
 * 		Type of Rough Equivalence Class: (U/C)/P
 */
@Data
@AllArgsConstructor
@ReturnWrapper
public class PartitionResult<AttributeSet, RoughEquClasses> {
	private int positive;
	private boolean emptyBoundaryClassSetTypeClass;
	private AttributeSet attributes;
	private RoughEquClasses roughClasses;
}