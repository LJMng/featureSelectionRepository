package featureSelection.repository.support.calculation.dependency.liuRoughSet;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.liuRoughSet.LiuRoughSetAlgorithm;
import featureSelection.repository.support.calculation.alg.FeatureImportance4LiuRoughSet;
import featureSelection.repository.support.calculation.dependency.DefaultDependencyCalculation;

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
public class DependencyCalculation4LiuRoughSet
	extends DefaultDependencyCalculation
	implements FeatureImportance4LiuRoughSet<Double>
{
	private Double positive;
	@Override
	public Double getResult() {
		return positive;
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}

	@Override
	public FeatureImportance4LiuRoughSet<Double> calculate(
			Collection<Instance> instances, IntegerIterator attributes, Object...args
	) {
		// Count the current calculation
		countCalculate(attributes.size());
		// Calculate
		positive = instances==null || instances.isEmpty()?
					0: positiveRegion(instances, attributes).size() / (double) instances.size();
		return this;
	}
	
	public FeatureImportance4LiuRoughSet<Double> calculate4Incremental(
			Collection<Collection<Instance>> roughClasses, IntegerIterator attributes,
			Object...args
	){
		// Count the current calculation
		countCalculate(attributes.size());
		// Calculate
		double insSize = (int) args[0];
		positive = roughClasses==null || roughClasses.isEmpty()?
					0: incrementalPositiveRegion(roughClasses, attributes).size() / insSize;
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
			if (LiuRoughSetAlgorithm.Basic.equivalenceClass(set, new IntegerArrayIterator(0)).size()==1) {
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
	public Double difference(Double v1, Double v2) {
		return (Double) new Double(v1.doubleValue() - v2.doubleValue());
	}
}