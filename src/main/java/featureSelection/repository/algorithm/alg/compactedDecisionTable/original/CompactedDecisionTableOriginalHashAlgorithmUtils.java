package featureSelection.repository.algorithm.alg.compactedDecisionTable.original;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.repository.entity.alg.compactedDecisionTable.impl.original.equivalenceClass.EquivalenceClassCompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.interf.CompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.interf.DecisionNumber;
import lombok.experimental.UtilityClass;

import java.util.Collection;

/**
 * Utilities for CompactedDecisionTableHashAlgorithm.
 * 
 * @author Benjamin_L
 */
@UtilityClass
public class CompactedDecisionTableOriginalHashAlgorithmUtils {
	
	public static int instanceSizeOfCompactedTableRecords(
			Collection<? extends CompactedTableRecord<? extends DecisionNumber>> tableRecords
	){
		int sum = 0;
		IntegerIterator decisionValues;
		for (CompactedTableRecord<? extends DecisionNumber> record: tableRecords) {
			decisionValues = record.getDecisionNumbers()
									.numberValues()
									.reset();
			while (decisionValues.hasNext())	sum += decisionValues.next();
		}
		return sum;
	}

	public static <DN extends DecisionNumber> int compactedRecordSizeOfEquivalenceClassRecords(
			Collection<EquivalenceClassCompactedTableRecord<DN>> equClasses
	){
		int sum = 0;
		for (EquivalenceClassCompactedTableRecord<? extends DecisionNumber> equClass: equClasses)
			sum += equClass.getEquivalenceRecords().size();
		return sum;
	}

}