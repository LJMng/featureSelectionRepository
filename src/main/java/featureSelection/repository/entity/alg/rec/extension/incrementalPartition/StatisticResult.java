package featureSelection.repository.entity.alg.rec.extension.incrementalPartition;

import featureSelection.basic.annotation.common.ReturnWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * An entity to contain Info. of Rough Equivalence Class positive region calculation statistics result.
 * With the following fields:
 * <ul>
 * 	<li><strong>{@link #positiveRegion}</strong>
 * 		<p>The size of the positive region of the current Rough Equivalence Classes
 * 	</li>
 * 	<li><strong>{@link #emptyBoundaryClassSet}</strong>
 * 		<p>Whether there is no 0-REC in Rough Equivalence Classes or not.
 * 	</li>
 * 	<li><strong>{@link #record}</strong>
 * 		<p>Records of the partition result.
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 *
 * @param <RecordItems>
 * 		Type of record items like Equivalence Class or Rough Equivalence Class.
 */
@Getter
@AllArgsConstructor
@ReturnWrapper
public class StatisticResult<RecordItems> {
	private int positiveRegion;
	private boolean emptyBoundaryClassSet;
	private RecordItems record;
}