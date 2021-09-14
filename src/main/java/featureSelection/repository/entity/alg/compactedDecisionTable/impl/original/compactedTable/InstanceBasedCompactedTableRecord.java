package featureSelection.repository.entity.alg.compactedDecisionTable.impl.original.compactedTable;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.compactedDecisionTable.interf.CompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.interf.DecisionNumber;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * An entity for Compacted Table record.
 * </p>
 * Uses an {@link Instance} as representative({@link #insRepresentitive}).
 * 
 * @author Benjamin_L
 *
 * @param <DN>
 * 		Type of decision number.
 */
@Getter
@ToString
@AllArgsConstructor
public class InstanceBasedCompactedTableRecord<DN extends DecisionNumber>
	implements CompactedTableRecord<DN>,
				Cloneable 
{
	private Instance insRepresentitive;
	private DN decisionNumbers;
	
	@SuppressWarnings("unchecked")
	@Override
	public InstanceBasedCompactedTableRecord<DN> clone() {
		return new InstanceBasedCompactedTableRecord<DN>(
				insRepresentitive, (DN) decisionNumbers.clone()
		);
	}
}