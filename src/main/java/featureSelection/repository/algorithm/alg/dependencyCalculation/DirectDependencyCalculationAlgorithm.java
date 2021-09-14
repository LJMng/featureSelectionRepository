package featureSelection.repository.algorithm.alg.dependencyCalculation;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.alg.dependencyCalculation.DirectDependencyCalculationStrategy;
import featureSelection.basic.support.searchStrategy.HashSearchStrategy;
import featureSelection.basic.support.searchStrategy.SequentialSearchStrategy;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.directDependencyCalculation.FeatureImportance4DirectDependencyCalculation;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.directDependencyCalculation.PositiveRegion4DDCHash;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.directDependencyCalculation.PositiveRegion4DDCSequential;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Algorithm repository of DDC, which based on the Article 
 * <a href="https://www.sciencedirect.com/science/article/abs/pii/S0888613X17300178">
 * "Feature selection using rough set-based direct dependency calculation by avoiding the positive
 * region"</a> by Muhammad Summair Raza, Usman Qamar.
 * <p>
 * For dependency calculation, use {@link PositiveRegion4DDCHash} for <code>Hash</code> search
 * strategy and {@link PositiveRegion4DDCSequential} for <code>Sequential</code> search
 * strategy.
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class DirectDependencyCalculationAlgorithm
	implements HashSearchStrategy, SequentialSearchStrategy,
				DirectDependencyCalculationStrategy
{
	/**
	 * Obtain the core.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) importance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4DirectDependencyCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param globalSig
	 * 		The global dependency value
	 * @param attributes
	 * 		All attributes. (Starts from 1)
	 * @return A {@link List} of {@link int} values as core.
	 */
	public static <Sig extends Number> Collection<Integer> core(
			FeatureImportance4DirectDependencyCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Sig globalSig, int[] attributes
	) {
		Collection<Integer> core = new HashSet<>(attributes.length);
		// Loop over attributes
		Sig dependency;
		int[] exAttribute = new int[attributes.length-1];
		for (int i=0; i<exAttribute.length; i++) {
			exAttribute[i] = attributes[i+1];
		}
		for (int i=0; i<attributes.length; i++) {
			// Calculate the dependency of C-{a}.
			dependency = calculation.calculate(instances, new IntegerArrayIterator(exAttribute))
									.getResult();
			// If dep(C)-dep(C-{a}) > 0, add the current attribute into core.
			if (calculation.value1IsBetter(globalSig, dependency, sigDeviation)) {
				core.add(attributes[i]);
			}
			// Next attribute
			if (i<exAttribute.length){
				exAttribute[i] = attributes[i];
			}
		}
		return core;
	}
	
	/**
	 * Get the current most significant attribute.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) importance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4DirectDependencyCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param red
	 * 		Reduct attributes.
	 * @param globalSig
	 * 		The global dependency value.
	 * @param attributes
	 * 		All attributes. (Starts from 1)
	 * @return A integer value.
	 */
	public static <Sig extends Number> int mostSignificantAttribute(
			FeatureImportance4DirectDependencyCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Collection<Integer> red, Sig globalSig, int[] attributes
	){
		// Initiate
		int sigAttr = -1;
		Sig max = null, sig, subDependency;
		// Loop over potential attributes
		int i=0;
		int[] attribute = new int[red.size()+1];	for (int r : red) attribute[i++] = r;
		for (int attr : attributes) {
			if (!red.contains(attr)) {
				// Calculate dep(P ∪ {a})
				attribute[attribute.length-1] = attr;
				subDependency = calculation.calculate(instances, new IntegerArrayIterator(attribute))
											.getResult();
				// Sig(a) = dep(P ∪ {a}) - dep(core)
				sig=calculation.difference(subDependency, globalSig);
				// If Sig(a)>max, update max and the most significant attribute
				if (max==null || calculation.value1IsBetter(sig, max, sigDeviation)) {
					max = sig;
					sigAttr = attr;
				}
			}
		}
		return sigAttr;
	}
	
	/**
	 * Get the current least significant current attribute.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) importance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4DirectDependencyCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param red
	 * 		Reduct attributes.
	 * @param attributes
	 * 		All attributes. (Starts from 1)
	 * @return A integer value.
	 */
	public static <Sig extends Number> int leastSignificantAttribute(
			FeatureImportance4DirectDependencyCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Collection<Integer> red, int[] attributes
	){
		int sigAttr = -1;
		Sig min = null, subDependency;
		// Loop over potential attributes
		int i=0;
		int[] attribute = new int[red.size()+1];	for (int r : red) attribute[i++] = r;
		for (int attr : attributes) {
			if (!red.contains(attr)) {
				// Get dep(P ∪ {a})
				attribute[attribute.length-1] = attr;
				subDependency = calculation.calculate(instances, new IntegerArrayIterator(attribute))
											.getResult();
				// Sig(a) = dep(P ∪ {a}) - dep(core)
				// If Sig(a)>max, update max and the most significant attribute
				if (min==null || calculation.value1IsBetter(min, subDependency, sigDeviation)) {
					min = subDependency;
					sigAttr = attr;
				}
			}
		}
		return sigAttr;
	}
	
	/**
	 * Execute inspection to remove redundant attributes in reduct.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) importance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4DirectDependencyCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link List} of {@link Instance}s.
	 * @param redArray
	 * 		An array of attributes.
	 * @return A list of inspected attributes.
	 */
	public static <Sig extends Number> Collection<Integer> inspection(
			FeatureImportance4DirectDependencyCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, int...redArray
	){
		Sig examSig;
		Sig globalDependency = calculation.calculate(instances, new IntegerArrayIterator(redArray))
										.getResult();
		
		int attr;
		LinkedList<Integer> red = new LinkedList<>();	for (int r: redArray)	red.add(r);
		for (int i=redArray.length-1; i>=0; i--) {
			attr = red.removeLast();
			examSig = calculation.calculate(instances, new IntegerCollectionIterator(red))
								.getResult();
			if (calculation.value1IsBetter(globalDependency, examSig, sigDeviation)){
				// Not redundant
				red.addFirst(attr);
			}
		}
		return red;
	}
	
	/**
	 * Execute inspection to remove redundant attributes in reduct.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) importance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4DirectDependencyCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Consider equal when
	 * 		the difference between two sig is less than the given deviation value.
	 * @param instances
	 * 		A {@link List} of {@link Instance}.
	 * @param red
	 * 		A list of attributes.
	 * @return A list of inspected attributes.
	 */
	public static <Sig extends Number> Collection<Integer> inspection(
			FeatureImportance4DirectDependencyCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Collection<Integer> red
	){
		Sig examSig;
		Sig globalSig = calculation.calculate(instances, new IntegerCollectionIterator(red))
									.getResult();
		Integer[] redArray = red.toArray(new Integer[red.size()]);
		for (int attr: redArray) {
			red.remove(attr);
			examSig = calculation.calculate(instances, new IntegerCollectionIterator(red))
								.getResult();
			if (calculation.value1IsBetter(globalSig, examSig, sigDeviation)){
				// Not redundant.
				red.add(attr);
			}
		}
		return red;
	}
}