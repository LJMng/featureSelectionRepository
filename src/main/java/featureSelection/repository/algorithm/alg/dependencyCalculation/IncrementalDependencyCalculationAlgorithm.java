package featureSelection.repository.algorithm.alg.dependencyCalculation;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.alg.dependencyCalculation.IncrementalDependencyCalculationStrategy;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.basic.support.searchStrategy.HashSearchStrategy;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.incrementalDependencyCalculation.FeatureImportance4IncrementalDependencyCalculation;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Algorithm repository of IDC, which bases on the paper
 * <a href="https://www.sciencedirect.com/science/article/pii/S0020025516000785">
 * "An incremental dependency calculation technique for feature selection using rough sets"</a> 
 * by Muhammad Summair Raza, Usman Qamar.
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class IncrementalDependencyCalculationAlgorithm 
	implements HashSearchStrategy,
				IncrementalDependencyCalculationStrategy
{
	/**
	 * Obtain the core.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) importance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4IncrementalDependencyCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param globalSig
	 * 		The global dependency value.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @return A {@link List} of {@link Integer} values as core.
	 */
	public static <Sig extends Number> Collection<Integer> core(
			FeatureImportance4IncrementalDependencyCalculation<Sig> calculation, Sig sigDeviation, 
			Collection<Instance> instances, Sig globalSig, int[] attributes
	){
		Collection<Integer> core = new LinkedList<>();
		// Loop over all attributes
		Sig examSig;
		int[] examAttribute = new int[attributes.length-1];
		for (int i=0; i<examAttribute.length; i++){
			examAttribute[i] = attributes[i+1];
		}
		for (int i=0; i<attributes.length; i++) {
			// Calculate the dependency of (C - {a}).
			examSig = calculation.calculate(instances, new IntegerArrayIterator(examAttribute))
								.getResult();
			// If dep(C)-dep(C-{a}) > 0, add the current attribute into core.
			if (calculation.value1IsBetter(globalSig, examSig, sigDeviation)){
				core.add(attributes[i]);
			}
			// Next attribute
			if (i<examAttribute.length){
				examAttribute[i] = attributes[i];
			}
		}
		return core;
	}
	
	/**
	 * Obtain current least significant attribute.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) importance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4IncrementalDependencyCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param red
	 * 		Reduct attributes.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @return A {@link int} value as the most significant attribute.
	 */
	public static <Sig extends Number> int leastSignificantAttribute(
			FeatureImportance4IncrementalDependencyCalculation<Sig> calculation, Sig sigDeviation, 
			Collection<Instance> instances, Collection<Integer> red, int[] attributes
	){
		// Initiate
		int sigAttr = -1;
		Sig min = null, dependency;
		// Loop potential attributes
		int i=0;
		int[] attribute = new int[red.size()+1];	for ( int r : red) attribute[i++] = r;
		for (int attr : attributes) {
			if (!red.contains(attr)) {
				// Get dep(P ∪ {a})
				attribute[attribute.length-1] = attr;
				
				dependency = calculation.calculate(instances, new IntegerArrayIterator(attribute))
										.getResult();
				// Sig(a) = dep(P ∪ {a}) - dep(P)
				// If Sig(a)<min, update the least significant attribute
				if (min==null||calculation.value1IsBetter(min, dependency, sigDeviation)) {
					min = dependency;
					sigAttr = attr;
				}
			}
		}
		return sigAttr;
	}
	
	/**
	 * Obtain current most significant current attribute.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) importance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4IncrementalDependencyCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param red
	 * 		Reduct attributes.
	 * @param redDependency
	 * 		Reduct dependency value.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @return A {@link int} value as the most significant attribute's index.
	 */
	public static <Sig extends Number> int mostSignificantAttribute(
			FeatureImportance4IncrementalDependencyCalculation<Sig> calculation, Sig sigDeviation, 
			Collection<Instance> instances, Collection<Integer> red, Sig redDependency,
			int[] attributes
	){
		int sigAttr = -1;
		Sig max = null, sig, dependency;
		// Loop over potential attributes
		int i=0;
		int[] attribute = new int[red.size()+1];	for ( int r : red) attribute[i++] = r;
		for (int attr : attributes) {
			if (!red.contains(attr)) {
				// Get dep(P ∪ {a})
				attribute[attribute.length-1] = attr;

				dependency = calculation.calculate(instances, new IntegerArrayIterator(attribute))
										.getResult();
				// Sig(a) = dep(P ∪ {a}) - dep(P)
				sig = calculation.difference(dependency, redDependency);
				// If Sig(a)>max, update max and the most significant attribute
				if (max==null||calculation.value1IsBetter(sig, max, sigDeviation)) {
					max = sig;
					sigAttr = attr;
				}
			}
		}
		return sigAttr;
	}
	
	/**
	 * Check if meets the algorithm stopping criteria for Quick Reduct. Based on the original
	 * paper, the stopping criteria is: <code>Dep(P)==Dep(C)</code>, where <i>P</i> is a subset
	 * of <i>C</i>.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) importance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4IncrementalDependencyCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param globalSig
	 * 		The global dependency value.
	 * @param redSig
	 * 		Reduct dependency value.
	 * @return <code>true</code> if meets the stopping criteria.
	 */
	public static <Sig extends Number> boolean continueLoopQuickReduct(
			FeatureImportance<Sig> calculation, Sig sigDeviation, Sig globalSig, Sig redSig
	) {
		return !calculation.value1IsBetter(globalSig, redSig, sigDeviation);
	}
	
	/**
	 * Inspection to remove redundant attributes in reduct.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) importance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4IncrementalDependencyCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param red
	 * 		Reduct {@link Collection}.
	 */
	public static <Sig extends Number> void inspection(
			FeatureImportance4IncrementalDependencyCalculation<Sig> calculation, Sig sigDeviation, 
			Collection<Instance> instances, Collection<Integer> red
	){
		Integer[] redCopy = red.toArray(new Integer[red.size()]);
		Sig examDependency;
		Sig globalDependency = calculation.calculate(instances, new IntegerCollectionIterator(red))
											.getResult();
		for (int examAttr: redCopy) {
			red.remove(examAttr);
			examDependency = calculation.calculate(instances, new IntegerCollectionIterator(red))
										.getResult();
			if (calculation.value1IsBetter(globalDependency, examDependency, sigDeviation)){
				// Not redundant.
				red.add(examAttr);
			}
		}
	}
	
	/**
	 * Inspection to remove redundant attributes in reduct.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) importance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4IncrementalDependencyCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Consider equal when
	 * 		the difference between two sig is less than the given deviation value.
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param redArray
	 * 		Reduct array.
	 * @return Inspected reduct in {@link Collection}.
	 */
	public static <Sig extends Number> Collection<Integer> inspection(
			FeatureImportance4IncrementalDependencyCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, int...redArray
	){
		Collection<Integer> red = new HashSet<Integer>(redArray.length);
		for (int each: redArray)	red.add(each);

		Sig examDependency;
		Sig globalDependency = calculation.calculate(instances, new IntegerCollectionIterator(red))
											.getResult();
		for (int examAttr: redArray) {
			red.remove(examAttr);
			examDependency = calculation.calculate(instances, new IntegerCollectionIterator(red))
										.getResult();
			if (calculation.value1IsBetter(globalDependency, examDependency, sigDeviation)){
				// Not redundant.
				red.add(examAttr);
			}
		}
		return red;
	}
}