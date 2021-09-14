package featureSelection.tester.statistics.info.iteration.optimization.optimizationEntity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import common.utils.ArrayUtils;
import lombok.Data;

/**
 * Basic info. of an optimization entity.
 * 
 * @author Benjamin_L
 *
 * @param <FitnessValue>
 * 		Type of fitness value of a specific Optimization algorithm.
 */
@Data
public class OptEntityBasicInfo<FitnessValue> {
	/**
	 * Length of <strong>original</strong> attributes transfered from an optimization
	 * entity/solution whose codings are managed by the specific optimization(i.e.
	 * created/modified by the specific optimization and <strong>has nothing to do with
	 * Feature Selection Algorithms</strong>, strategies or calculations)
	 * <p>
	 * <i>For example</i>, for Opt-P-NEC, the entity coding might be streamlined after
	 * calculating the fitness of an entity for some redundant attributes being deleted from
	 * the original entity coding.
	 * For this field, the length of an original entity coding before the streamlining should
	 * be set here.
	 * 
	 * @see #finalAttributes
	 */
	private int entityAttributeLength;
	/**
	 * Current attributes transfered from a optimization entity/solution whose coding is
	 * managed by the specific optimization <strong>finalised</strong> and is the basis of
	 * the fitness calculation. BTD, the attributes here could be finalised by some Feature
	 * Selection algorithms or strategies to improve the quality of the entity.
	 * <p>
	 * <i>For example</i>, for Opt-P-NEC, the entity coding might be streamlined after
	 * calculating the fitness of an entity for some redundant attributes being deleted from
	 * the original entity coding. For this field, the entity coding after the streamlining(
	 * with some redundant attributes filtered) should be set here.
	 * 
	 * @see #entityAttributeLength
	 */
	private int[] finalAttributes;
	/**
	 * FitnessValue of the reduct.
	 */
	private FitnessValue currentFitnessValue;
	
	/**
	 * {@link SupremeMark}.
	 */
	private SupremeMark supremeMark;
	
	/**
	 * The reduct after inspection.
	 */
	private Collection<Integer> inspectedReduct;
	/**
	 * Whether this entity is selected as (one of) the final solution(s).
	 */
	private boolean isSolution = false;
	
	
	@Override
	public String toString() {
		return String.format("%s | coding=%d, final=%d, minus=%d | Fitness=%.4f, %s", 
				isSolution? "√": " ",
				// coding, final, minus
				entityAttributeLength, finalAttributes.length, entityAttributeLength - finalAttributes.length,
				// Fitness
				((Number) currentFitnessValue).doubleValue(),
				supremeMark==null? 
					SupremeMarkType.NONE.toString():
					String.format("%s[%d] %s -> %s", 
							supremeMark.getSupremeMarkType().toString(), supremeMark.getRank(), 
							ArrayUtils.intArrayToString(finalAttributes, 20),
							inspectedReduct
					)
			);
	}

	public boolean differ(
			OptEntityBasicInfo<FitnessValue> entityInfo,
			Comparator<FitnessValue> fitnessValueComparator
	) {
		if (entityInfo==this)												return false;
		
		if (entityInfo.isSolution!=isSolution)								return true;
		if (entityInfo.entityAttributeLength!=entityAttributeLength)		return true;
		if (nullableDiffer(entityInfo.finalAttributes, finalAttributes, (a1, a2)->a1.length-a2.length))
																			return true;
		if (nullableDiffer(entityInfo.inspectedReduct, inspectedReduct, (c1, c2)->c1.size()-c2.size()))
																			return true;
		if (fitnessValueComparator.compare(entityInfo.currentFitnessValue, currentFitnessValue)!=0)	
																			return true;
		if (nullableDiffer(entityInfo.supremeMark, supremeMark, (m1, m2)->entityInfo.supremeMark!=null && entityInfo.supremeMark.differ(supremeMark)? 1: 0))
																			return true;
		if (!Arrays.equals(entityInfo.finalAttributes, finalAttributes))	return true;
		if (entityInfo.inspectedReduct!=null && inspectedReduct!=null &&
			(entityInfo.inspectedReduct instanceof Set? entityInfo.inspectedReduct: new HashSet<>(entityInfo.inspectedReduct))
				.containsAll(inspectedReduct)
		)																	return true;
		return false;
	}
	
	private <T> boolean nullableDiffer(T obj1, T obj2, Comparator<T> cmp) {
		if (obj1==obj2)					return false;
		if (obj1==null && obj2==null)	return false;
		return cmp.compare(obj1, obj2)!=0;
	}
}