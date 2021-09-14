package featureSelection.repository.entity.alg.rec.nestedEC;

import featureSelection.basic.annotation.common.ReturnWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * An entity to contain result of IP-NEC most significant attribute searching.
 *
 * @param <NestedEquClasses>
 *     Type of Nested Equivalence Classes.
 * @param <Sig>
 *     Type of feature subset significance.
 */
@Data
@AllArgsConstructor
@ReturnWrapper
public class MostSignificanceResult<NestedEquClasses, Sig extends Number> {
	private NestedEquClasses nestedEquClasses;
	private int significantAttribute;
	private Sig significance;
	private boolean emptyBoundary;
}
