package featureSelection.repository.entity.alg.liangIncrementalAlgorithm;

import featureSelection.basic.annotation.common.ReturnWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * An entity for most significant attribute result.
 * 
 * @author Benjamin_L
 *
 * @param <Sig>
 * 		Type of significant attribute.
 */
@Data
@AllArgsConstructor
@ReturnWrapper
public class MostSignificantAttributeResult<Sig extends Number> {
	private int attribute;
	private Sig significance;
}
