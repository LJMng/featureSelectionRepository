package featureSelection.repository.entity.alg.rec.nestedEC;

import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * An entity to contain {@link NestedEquClasses} info.
 * 
 * @author Benjamin_L
 *
 * @param <NestedEquClasses>
 * 		Type of implemented {@link NestedEquivalenceClass}.
 */
@Data
@AllArgsConstructor
public class NestedEquivalenceClassesInfo<NestedEquClasses> {
	private NestedEquClasses nestedEquClasses;
	private boolean emptyBoundaryClass;
}
