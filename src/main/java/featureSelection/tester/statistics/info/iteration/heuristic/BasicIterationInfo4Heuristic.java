package featureSelection.tester.statistics.info.iteration.heuristic;

import java.util.Collection;

import common.utils.ArrayUtils;
import common.utils.StringUtils;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.tester.statistics.info.iteration.IterationBasicInfo;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.Setter;

/**
 * Info. of iteration for Heuristic based Feature Selections.
 * 
 * @author Benjamin_L
 *
 * @param <Sig>
 * 		Type of Feature Importance.
 * @param <AdditionalAttr>
 * 		Type of additional attribute(s) to be added into reduct in an iteration. Can be
 * 		{@link Integer} or <code>int[]</code>, {@link Integer} {@link Collection}.
 * @param <SigCalLength>
 *      Type of length(s) of attributes used in significance calculation in the iteration.
 */
@Setter
@Getter
@EqualsAndHashCode(callSuper=false)
public class BasicIterationInfo4Heuristic<Sig extends Number, AdditionalAttr, SigCalLength> 
	extends IterationBasicInfo
{
	/**
	 * Current reduct length, which is the moment entering the iteration before any action
	 * executed.
	 */
	private int currentReductLength;
	
	/**
	 * The additional attribute(s) to be added into the reduct.
	 */
	private AdditionalAttr additionalAttribute;
	/**
	 * The length(s) of attributes used in significance calculation in the iteration.
	 */
	private SigCalLength significanceCalculationAttributeLength;
	
	/**
	 * Significance of the reduct after adding the additional attribute(s).
	 */
	private Sig significance;
	
	/**
	 * The number of {@link Instance} removed in the current iteration.
	 */
	private int instanceRemoved;
	/**
	 * The number of record removed in the current iteration.
	 */
	private Integer recordRemoved;
	
	
	public BasicIterationInfo4Heuristic(int iteration, int recordNumber, Class<?> recordClass) {
		super(iteration, recordNumber, recordClass);
	}

	/**
	 * Get the string of {@link #additionalAttribute}.
	 * 
	 * @return {@link #additionalAttribute} in {@link String}.
	 */
	public String getAdditionalAttributeString() {
		if (additionalAttribute==null) {
			return null;
		}else if (additionalAttribute instanceof Collection) {
			return StringUtils.toString(
					(Collection<?>) additionalAttribute,
					20
			);
		}else if (additionalAttribute instanceof int[]) {
			return ArrayUtils.intArrayToString(
					(int[]) additionalAttribute,
					20
			);
		}else {
			return additionalAttribute.toString();
		}
	}
}