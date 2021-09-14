package featureSelection.repository.support.shrink.compactedDecisionTable.original;

import featureSelection.basic.support.shrink.ShrinkInstance;
import featureSelection.repository.algorithm.alg.compactedDecisionTable.original.CompactedDecisionTableHashAlgorithm;
import featureSelection.repository.entity.alg.compactedDecisionTable.impl.original.compactedTable.InstanceBasedCompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.impl.original.equivalenceClass.EquivalenceClassCompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.interf.DecisionNumber;

import java.util.Collection;
import java.util.Iterator;

public class Shrink4CompactedDecisionTable<DN extends DecisionNumber>
	implements ShrinkInstance<ShrinkInput4CompactedDecisionTable<DN>, ShrinkRemoveableCriteria4CompactedDecisionTable<DN>, int[]>
{
	/**
	 * @return Positive/Negative region removed universe number: [Pos(U/C), Neg(U/C)].
	 */
	@Override
	public int[] shrink(ShrinkInput4CompactedDecisionTable<DN> in) throws Exception {
		int consistentStatus, removePos=0, removeNeg=0;
		boolean containsAllUniverses;
		EquivalenceClassCompactedTableRecord<DN> equClassRecord;
		Collection<InstanceBasedCompactedTableRecord<DN>> globalCompactedTableRecords;
		Iterator<EquivalenceClassCompactedTableRecord<DN>> equequClassIterator =
				in.getDecisionTableRecords().iterator();
		while (equequClassIterator.hasNext()) {
			equClassRecord = equequClassIterator.next();
			consistentStatus = CompactedDecisionTableHashAlgorithm
									.Basic
									.checkConsistency(equClassRecord.getDecisionNumbers());
			if (in.isFilterPositiveRegion()) {
				// if dValue of h is single. (1 criteria of positive region)
				if (consistentStatus>0) {
					// if h in global positive region. (1 criteria of positive region)
					containsAllUniverses = false;
					globalCompactedTableRecords = equClassRecord.getEquivalenceRecords();
					for (InstanceBasedCompactedTableRecord<? extends DecisionNumber> record : globalCompactedTableRecords) {
						containsAllUniverses = in.getGlobalPositiveRegion().contains(record);
						if (!containsAllUniverses)	break;
					}
					if (containsAllUniverses) {
						equequClassIterator.remove();
						removePos++;
						continue;
					}
				}
			}
			if (in.isFilterNegativeRegion()) {
				// if dValue of h is single. (1 criteria of negative region)
				if (consistentStatus<0) {
					// if h in global positive region. (1 criteria of negative region)
					containsAllUniverses = false;
					globalCompactedTableRecords = equClassRecord.getEquivalenceRecords();
					for (InstanceBasedCompactedTableRecord<? extends DecisionNumber> record : globalCompactedTableRecords) {
						containsAllUniverses = in.getGlobalNegativeRegion().contains(record);
						if (!containsAllUniverses)	break;
					}
					if (containsAllUniverses) {
						equequClassIterator.remove();
						removeNeg++;
						continue;
					}
				}
			}
		}
		return new int[] {removePos, removeNeg};
	}

	@Override
	public boolean removAble(ShrinkRemoveableCriteria4CompactedDecisionTable<DN> item) {
		if (item.getConsistentStatus()>0) {
			// if dValue of h is single. (1 criteria of positive region)
			if (item.isFilterPositiveRegion()) {
				// if h in global positive region. (1 criteria of positive region)
				boolean containsAllUniverses;
				Collection<InstanceBasedCompactedTableRecord<DN>> globalCompactedTableRecords =
						item.getItem().getEquivalenceRecords();
				for (InstanceBasedCompactedTableRecord<? extends DecisionNumber> record : globalCompactedTableRecords) {
					containsAllUniverses = item.getGlobalPositiveRegion().contains(record);
					if (!containsAllUniverses)	return false;
				}
				return true;
			}else {
				return false;
			}
		}else if (item.getConsistentStatus()<0) {
			// if dValue of h is single. (1 criteria of negative region)
			if (item.isFilterNegativeRegion()) {
				// if h in global positive region. (1 criteria of negative region)
				boolean containsAllUniverses;
				Collection<InstanceBasedCompactedTableRecord<DN>> globalCompactedTableRecords =
						item.getItem().getEquivalenceRecords();
				for (InstanceBasedCompactedTableRecord<? extends DecisionNumber> record : globalCompactedTableRecords) {
					containsAllUniverses = item.getGlobalNegativeRegion().contains(record);
					if (!containsAllUniverses)	return false;
				}
				return true;
			}else {
				return false;
			}
		}else {
			return true;
		}
	}
}
