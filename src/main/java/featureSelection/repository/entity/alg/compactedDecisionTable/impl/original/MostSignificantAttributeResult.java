package featureSelection.repository.entity.alg.compactedDecisionTable.impl.original;

import java.util.Collection;

import featureSelection.basic.annotation.common.ReturnWrapper;
import featureSelection.repository.entity.alg.compactedDecisionTable.impl.original.equivalenceClass.EquivalenceClassCompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.interf.DecisionNumber;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * An entity for Most significant attribute search result.
 * 
 * @author Benjamin_L
 *
 * @param <Sig>
 * 		Type of significance of attribute.
 * @param <DN>
 * 		Type of decision numbers.
 */
@Getter
@AllArgsConstructor
@ReturnWrapper
public class MostSignificantAttributeResult<Sig extends Number, DN extends DecisionNumber> {
	private Sig significance;
	private int attribute;
	private Collection<EquivalenceClassCompactedTableRecord<DN>> equClasses;
}
