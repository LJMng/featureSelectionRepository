package featureSelection.repository.support.calculation.alg;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.alg.compactedDecisionTable.interf.CompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.interf.DecisionNumber;

import java.util.Collection;

/**
 * An interface for Feature Importance calculation using Rough Set Theory dependency(Positive
 * region based) calculation based on Compacted Decision table.
 * <p>
 * Implementations should base on the original article
 * <a href="https://www.sciencedirect.com/science/article/abs/pii/S0950705115002312">
 * "Compacted decision tables based attribute reduction"</a> by Wei Wei, Junhong Wang, Jiye Liang, 
 * Xin Mi, Chuangyin Dang.
 * 
 * @author Benjamin_L
 *
 * @param <V>
 * 		Type of feature importance.
 */
@RoughSet
public interface CompactedDecisionTableCalculation<V> extends FeatureImportance<V> {
	/**
	 * Calculate.
	 * 
	 * @param records
	 * 		Records of a <code>Compacted Decision Table</code> in a {@link Collection}.
	 * @param attributeLength
	 * 		The length of the attribute participated in the calculation.
	 * @param args
	 * 		Extra arguments.
	 * @return <code>this</code>
	 */
	CompactedDecisionTableCalculation<V> calculate(
			Collection<? extends CompactedTableRecord<? extends DecisionNumber>> records,
			int attributeLength, Object...args
	);
}