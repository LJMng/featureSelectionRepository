package featureSelection.repository.entity.alg.rec.nestedEC.reductionResult;

import java.util.Collection;

import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * An entity to store the return value of IP-NEC static data execution. With the following fields:
 * <ul>
 * 	<li><strong>nestedEquClasses</strong> <code>{@link NestedEquivalenceClass} {@link Collection}
 * 		</code>:
 * 		<p>A {@link Collection} of {@link NestedEquivalenceClass} induced by reduct.
 * 	</li>
 * 	<li><strong>reduct</strong> <code>{@link Reduct}</code>:
 * 		<p>reduct.
 * 	</li>
 * 	<li><strong>reductSig</strong> <code>{@link Sig}</code>:
 * 		<p>Significance of the updated reduct.
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 *
 * @param <Reduct>
 * 		Type of reduct returns.
 * @param <Sig>
 * 		Type of feature significance
 */
@Data
@AllArgsConstructor
@ReturnWrapper
public class ReductionResult4Static<Reduct, Sig extends Number>{
	private Collection<NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses;
	private Reduct reduct;
	private Sig reductSig;
}