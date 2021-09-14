package featureSelection.repository.entity.alg.compactedDecisionTable.interf;

/**
 * An interface for record of Compacted Table.
 * 
 * @author Benjamin_L
 * 
 * @param <DN>
 * 		Type of decision numbers.
 */
public interface CompactedTableRecord<DN extends DecisionNumber> {
	DN getDecisionNumbers();
}