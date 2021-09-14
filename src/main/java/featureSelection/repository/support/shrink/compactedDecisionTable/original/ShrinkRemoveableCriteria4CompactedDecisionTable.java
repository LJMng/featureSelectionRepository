package featureSelection.repository.support.shrink.compactedDecisionTable.original;

import java.util.Set;

import featureSelection.repository.entity.alg.compactedDecisionTable.impl.original.compactedTable.InstanceBasedCompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.impl.original.equivalenceClass.EquivalenceClassCompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.interf.DecisionNumber;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShrinkRemoveableCriteria4CompactedDecisionTable<DN extends DecisionNumber> {
	private int consistentStatus;
	private EquivalenceClassCompactedTableRecord<DN> item;
	
	private boolean filterPositiveRegion;
	private Set<InstanceBasedCompactedTableRecord<DN>> globalPositiveRegion;
	private boolean filterNegativeRegion;
	private Set<InstanceBasedCompactedTableRecord<DN>> globalNegativeRegion;
}
