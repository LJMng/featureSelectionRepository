package featureSelection.repository.entity.alg.rec.nestedEC.impl.nestedEquivalenceClassesMerge.result;

import java.util.Map;
import java.util.Collection;

import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A reduction result for Incremental Partition Nested Equivalent Class based Feature Selection.
 * With fields:
 * <ul>
 * 	<li><strong>wrappedInstances</strong>
 * 		<p>Wrapped {@link Instance}, can be U or U/C.
 * 	</li>
 * 	<li><strong>reduct</strong>
 * 		<p>The reduct result.
 * 	</li>
 * 	<li><strong>reductSig</strong>
 * 		<p>The significance of the reduct result.
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 *
 * @param <WrappedUniverse>
 * 		Type of wrapped universe instances entity. Can be {@link Instance} {@link Collection},
 * 		{@link EquivalenceClass} {@link Collection}, {@link NestedEquivalenceClass} {@link Collection} or
 * 		{@link Map}s.
 * @param <Red>
 * 		Type of reduct.
 * @param <Sig>
 * 		Type of feature significance.
 */
@Data
@AllArgsConstructor
@ReturnWrapper
public class ReductionResult<WrappedUniverse, Red, Sig> {
	private WrappedUniverse wrappedInstances;
	private Red reduct;
	private Sig reductSig;
}