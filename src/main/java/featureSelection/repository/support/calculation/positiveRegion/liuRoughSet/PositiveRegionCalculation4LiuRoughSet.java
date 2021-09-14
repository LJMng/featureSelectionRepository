package featureSelection.repository.support.calculation.positiveRegion.liuRoughSet;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.liuRoughSet.LiuRoughSetAlgorithm;
import featureSelection.repository.support.calculation.alg.FeatureImportance4LiuRoughSet;
import featureSelection.repository.support.calculation.positiveRegion.DefaultPositiveRegionCalculation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Positive Region Calculation for Liu-RoughSet.
 * 
 * @see LiuRoughSetAlgorithm
 * 
 * @author Benjamin_L
 */
public class PositiveRegionCalculation4LiuRoughSet
	extends DefaultPositiveRegionCalculation
	implements FeatureImportance4LiuRoughSet<Integer>
{
	private Integer positive;
	@Override
	public Integer getResult() {
		return positive;
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}

	@Override
	public FeatureImportance4LiuRoughSet<Integer> calculate(
			Collection<Instance> instances, IntegerIterator attributes,
			Object...args
	) {
		// Count the current calculation
		countCalculate(attributes.size());
		// Calculate
		positive = instances==null || instances.isEmpty()?
					0: positiveRegion(instances, attributes).size();
		return this;
	}
	
	public FeatureImportance4LiuRoughSet<Integer> calculate4Incremental(
			Collection<Collection<Instance>> roughClasses, IntegerIterator attributes,
			Object...args
	){
		// Count the current calculation
		countCalculate(attributes.size());
		// Calculate
		positive = roughClasses==null || roughClasses.isEmpty()?
					0: incrementalPositiveRegion(roughClasses, attributes).size();
		return this;
	}
	
	/**
	 * Get the positive region based on the given {@link Instance}s and given attributes.
	 * 
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @return A {@link Set} of {@link Instance} {@link Set} as Positive Region.
	 */
	public static Collection<Instance> positiveRegion(
			Collection<Instance> instances, IntegerIterator attributes
	) {
		// Get equivalent class based on the universes and given attributes
		Collection<Collection<Instance>> equClass =
				LiuRoughSetAlgorithm.Basic.equivalenceClass(instances, attributes);
		// Initiate positive
		Collection<Instance> positive = new HashSet<>();
		// Go through each class
		for (Collection<Instance> set : equClass) {
			if (LiuRoughSetAlgorithm.Basic
									.equivalenceClass(set, new IntegerArrayIterator(0))
									.size()==1
			) {
				positive.addAll(set);
			}
		}
		return positive;
	}
	
	/**
	 * Get the positive region based on the {@link Instance}s and given attributes
	 * 
	 * @param rough
	 * 		A {@link Set} of {@link Instance} {@link Set} as Rough Equivalent Class.
	 * @param attributes
	 * 		Attributes of {@link Instance}.
	 * @return A {@link Set} of {@link Instance} {@link Set} as Positive Region.
	 */
	public static Collection<Instance> incrementalPositiveRegion(
			Collection<Collection<Instance>> rough, IntegerIterator attributes
	){
		// Calculate incremental rough equivalent class
		Collection<Collection<Instance>> increment = new HashSet<>();
		for (Collection<Instance> r : rough)
			increment.addAll(LiuRoughSetAlgorithm.Basic.equivalenceClass(r, attributes));
		// Calculate positive region
		Collection<Instance> positive = new HashSet<>();
		for (Collection<Instance> set : increment) {
			if (LiuRoughSetAlgorithm.Basic.equivalenceClass(set, new IntegerArrayIterator(0)).size() == 1) {
				positive.addAll(set);
			}
		}
		return positive;
	}
	
	@Override
	public Integer difference(Integer v1, Integer v2) {
		return v1.intValue() - v2.intValue();
	}
}