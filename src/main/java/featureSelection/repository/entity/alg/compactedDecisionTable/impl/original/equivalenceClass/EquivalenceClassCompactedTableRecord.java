package featureSelection.repository.entity.alg.compactedDecisionTable.impl.original.equivalenceClass;

import featureSelection.repository.entity.alg.compactedDecisionTable.impl.original.compactedTable.InstanceBasedCompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.interf.CompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.interf.DecisionNumber;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

/**
 * An entity for Equivalence Class in a Compacted Table as a record.
 * <p>
 * To served as an equivalence class induced by P: e. This equivalence class is a record in the
 * Compacted Table induced by P: e ∈ U/P = {e[0], e[1], ...}.
 *
 * @author Benjamin_L
 *
 * @param <DN>
 * 		Type of decision number.
 */
@Getter
@AllArgsConstructor
public class EquivalenceClassCompactedTableRecord<DN extends DecisionNumber>
	implements CompactedTableRecord<DN>
{
	private DN decisionNumbers;
	private Collection<InstanceBasedCompactedTableRecord<DN>> equivalenceRecords;
}