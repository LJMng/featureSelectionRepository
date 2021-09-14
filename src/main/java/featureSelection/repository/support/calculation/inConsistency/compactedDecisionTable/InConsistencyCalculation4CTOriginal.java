package featureSelection.repository.support.calculation.inConsistency.compactedDecisionTable;

import java.util.Collection;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.repository.entity.alg.compactedDecisionTable.interf.CompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.interf.DecisionNumber;
import featureSelection.repository.support.calculation.alg.CompactedDecisionTableCalculation;
import featureSelection.repository.support.calculation.inConsistency.DefaultInConsistencyCalculation;
import lombok.Getter;

public class InConsistencyCalculation4CTOriginal 
	extends DefaultInConsistencyCalculation
	implements CompactedDecisionTableCalculation<Integer>
{
	@Getter private int inConsistency;
	@Override
	public Integer getResult() {
		return inConsistency;
	}

	public InConsistencyCalculation4CTOriginal calculate(
			Collection<? extends CompactedTableRecord<? extends DecisionNumber>> records,
			int attributeLength, Object...args
	) {
		// Count the current calculation
		countCalculate(attributeLength);
		// Calculate
		inConsistency = records==null || records.isEmpty() ? 0: inConsistency(records);
		return this;
	}
	
	private static int inConsistency(
			Collection<? extends CompactedTableRecord<? extends DecisionNumber>> records
	) {
		// incon = 0
		int inCon = 0;
		// Go through c in CTHash
		int dSum, maxNumber, number;
		IntegerIterator dValue;
		for (CompactedTableRecord<? extends DecisionNumber> record : records) {
			// dSum = 0
			dSum = 0;
			// c.dValue
			maxNumber = 0;
			dValue = record.getDecisionNumbers()
							.numberValues();
			dValue.reset();
			while (dValue.hasNext()) {
				number = dValue.next();
				dSum += number;
				if (Integer.compare(number, maxNumber)>0)	maxNumber = number;
			}
			dSum -= maxNumber; 
			inCon += dSum;
		}
		return inCon;
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof CompactedTableRecord;
	}
}