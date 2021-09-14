package featureSelection.repository.entity.alg.positiveApproximationAccelerator;

import java.util.Collection;

import featureSelection.basic.annotation.common.ReturnWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * An entity to contain the result of obtaining the most significant attribute.
 * 
 * @author Benjamin_L
 *
 * @param <Sig>
 * 		Type of significance of attribute.
 */
@Data
@AllArgsConstructor
@ReturnWrapper
public class MostSignificantAttributeResult<Sig> {
	private int attribute;
	private Sig maxSig;
	private Collection<EquivalenceClass> equClasses;
}
