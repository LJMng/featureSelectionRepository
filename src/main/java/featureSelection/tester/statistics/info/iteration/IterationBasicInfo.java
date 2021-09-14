package featureSelection.tester.statistics.info.iteration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Info. of iteration.
 * <p>
 * Basic info./fields in this class:
 * <ul>
 * 	<li>{@link #iteration}</li>
 * 	<li>{@link #recordNumber}</li>
 * 	<li>{@link #recordClass}</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Getter
@AllArgsConstructor
public class IterationBasicInfo {
	/**
	 * Current iteration.
	 */
	private int iteration;
	
	/**
	 * The number of record the moment entering the iteration before any action executed.
	 */
	private int recordNumber;
	/**
	 * The {@link Class} of records;
	 */
	private Class<?> recordClass;
}