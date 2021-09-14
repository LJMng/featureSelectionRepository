package featureSelection.repository.entity.alg.liangIncrementalAlgorithm;

import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Data;

import featureSelection.repository.entity.alg.liangIncrementalAlgorithm.classSetType.ClassSetType;

/**
 * A mixed equivalent class sequential list with equivalence classes in the following order:
 * <ul>
 * 	<li><strong>{@link ClassSetType#MIXED}</strong></li>
 * 	<li><strong>{@link ClassSetType#PREVIOUS}</strong></li>
 * 	<li><strong>{@link ClassSetType#NEW}</strong></li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Data
@AllArgsConstructor
public class MixedEquivalenceClassSequentialList {
	private Collection<EquivalenceClassInterf> equClasses;
	private int mixed;
	private int previousOnly;
	private int newOnly;
}
