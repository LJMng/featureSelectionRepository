package featureSelection.repository.algorithm.alg.dependencyCalculation;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.alg.dependencyCalculation.HeuristicDependencyCalculationStrategy;
import featureSelection.basic.support.searchStrategy.HashSearchStrategy;
import featureSelection.basic.support.searchStrategy.SequentialSearchStrategy;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.heuristicDependencyCalculation.FeatureImportance4HeuristicDependencyCalculation;
import featureSelection.repository.support.calculation.dependency.dependencyCalculation.heuristicDependencyCalculation.DependencyCalculation4HDCHash;
import featureSelection.repository.support.calculation.dependency.dependencyCalculation.heuristicDependencyCalculation.DependencyCalculation4HDCSequential;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Algorithm repository of HDC, which based on the paper
 * <a href="https://www.sciencedirect.com/science/article/abs/pii/S0031320318301432">
 * "A heuristic based dependency calculation technique for rough set theory"</a> 
 * by Muhammad Summair Raza, Usman Qamar.
 * <p>
 * For dependency calculation, use {@link DependencyCalculation4HDCHash} for <code>Hash</code>
 * search strategy and {@link DependencyCalculation4HDCSequential} for <code>Sequential</code>
 * search strategy.
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class HeuristicDependencyCalculationAlgorithm
	implements HashSearchStrategy, SequentialSearchStrategy,
				HeuristicDependencyCalculationStrategy
{
	public static class Basic {
		/**
		 * Collect all decision values of the given {@link Instance} {@link Collection} using
		 * {@link HashSet}.
		 * 
		 * @see #decisionValuesByJavaStream(Collection)
		 * 
		 * @param instances
		 * 		A {@link Collection} of {@link Instance}.
		 * @return A {@link Collection} of {@link Integer} values as decision values.
		 */
		public static Collection<Integer> decisionValues(Collection<Instance> instances){
			Set<Integer> decisionValues = new HashSet<>();
			for (Instance ins: instances) {
				int decisionValue = ins.getAttributeValue(0);
				decisionValues.add(decisionValue);
			}
			return decisionValues;//*/
		}

		/**
		 * Collect all decision values of the given {@link Instance} {@link Collection} using
		 * <code>Java 8 Stream</code>.
		 * 
		 * @see #decisionValues(Collection)
		 *
		 * @param instances
		 * 		A {@link Collection} of {@link Instance}.
		 * @return A {@link Collection} of {@link Integer} values as decision values.
		 */
		public static Collection<Integer> decisionValuesByJavaStream(Collection<Instance> instances){
			return instances.stream()
							.map(ins->ins.getAttributeValue(0))
							.collect(Collectors.toSet());
		}
	}
	
	/**
	 * Obtain the core.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) importance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4HeuristicDependencyCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param globalSig
	 * 		The global dependency value.
	 * @param attributes
	 * 		All attributes. (Starts from 1)
	 * @param decisionValues
	 * 		The decision values of the universe instances.
	 * @return A {@link List} of {@link Integer} value as core.
	 */
	public static <Sig extends Number> Collection<Integer> core(
			FeatureImportance4HeuristicDependencyCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Sig globalSig, int[] attributes,
			Collection<Integer> decisionValues
	){
		// Initiate core using HashSet for later quick searching in reduct.
		Collection<Integer> core = new HashSet<>(attributes.length);
		// Loop over attributes
		Sig subSig;
		int[] exeAttribute = new int[attributes.length-1];
		for (int i=0; i<exeAttribute.length; i++){
			exeAttribute[i] = attributes[i+1];
		}
		for (int i=0; i<attributes.length; i++) {
			//Get dependency of C-{a}.
			subSig = calculation.calculate(instances, decisionValues, new IntegerArrayIterator(exeAttribute))
								.getResult();
			// If dep(C)-dep(C-{a}) > 0, add the current attribute into core.
			if (calculation.value1IsBetter(globalSig, subSig, sigDeviation))	core.add(attributes[i]);
			// Next attribute
			if (i<exeAttribute.length){
				exeAttribute[i] = attributes[i];
			}
		}
		return core;
	}
	
	/**
	 * Obtain the current least significant attribute
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) importance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4HeuristicDependencyCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param red
	 * 		Reduct attributes. (Starts from 1)
	 * @param attributes
	 * 		All attributes. (Starts from 1)
	 * @return An int value as attribute (Starts from 1).
	 */
	public static <Sig extends Number> int leastSignificantAttribute(
			FeatureImportance4HeuristicDependencyCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Collection<Integer> red,
			int[] attributes, Collection<Integer> decisionValues
	){
		int sigAttr = -1;
		Sig min = null, sig;
		// Loop over potential attributes
		int i=0;
		int[] attribute = new int[red.size()+1];	for (int r : red) attribute[i++] = r;
		for (int attr : attributes) {
			if (!red.contains(attr)) {
				//Get dep(P ∪ {a})
				attribute[attribute.length-1] = attr;
				sig = calculation.calculate(instances, decisionValues, new IntegerArrayIterator(attribute))
									.getResult();
				// Sig(a) = dep(P ∪ {a}) - dep(core)
				// If Sig(a)>max, update max and the most significant attribute
				if (min==null || calculation.value1IsBetter(min, sig, sigDeviation)) {
					min = sig;
					sigAttr = attr;
				}
			}
		}
		return sigAttr;
	}
	
	/**
	 * Obtain the current most significant current attribute.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) importance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4HeuristicDependencyCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param red
	 * 		Reduct attributes. (Starts from 1)
	 * @param globalSig
	 * 		The current dependency value.
	 * @param attributes
	 * 		All attributes. (Starts from 1)
	 * @return An int value as attribute (Starts from 1).
	 */
	public static <Sig extends Number> int mostSignificantAttribute(
			FeatureImportance4HeuristicDependencyCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Collection<Integer> red, Sig globalSig,
			int[] attributes, Collection<Integer> decisionValues
	){
		int sigAttr = -1;
		Sig max = null, sig, subSig;
		// Loop over potential attributes
		int i=0;
		int[] attribute = new int[red.size()+1];	for (int r : red) attribute[i++] = r;
		for (int attr : attributes) {
			if (!red.contains(attr)) {
				//Get dep(P ∪ {a})
				attribute[attribute.length-1] = attr;
				subSig = calculation.calculate(instances, decisionValues, new IntegerArrayIterator(attribute))
									.getResult();
				// Sig(a) = dep(P ∪ {a}) - dep(core)
				sig = calculation.difference(subSig, globalSig);
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
	 * Execute inspection for redundant attributes in reduct. This method will obtain decision
	 * values of the given universe instances first, then return {@link #inspection(
	 * FeatureImportance4HeuristicDependencyCalculation, Number, Collection, Collection, int...
	 * )}.
	 * 
	 * @see Basic#decisionValues(Collection)
	 * @see #inspection(FeatureImportance4HeuristicDependencyCalculation, Number, Collection,
	 *      Collection, int...)
	 *
	 * @param <Sig>
	 * 		Type of feature (subset) importance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4HeuristicDependencyCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link List} of {@link Instance}.
	 * @param redArray
	 * 		An array of <code>int</code> attributes.
	 * @return A {@link List} of inspected {@link int} attributes.
	 */
	public static <Sig extends Number> Collection<Integer> inspection(
			FeatureImportance4HeuristicDependencyCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, int...redArray
	){
		return inspection(calculation, sigDeviation, instances, Basic.decisionValues(instances),
							redArray);
	}
	
	/**
	 * Execute inspection for redundant attributes in reduct.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) importance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4HeuristicDependencyCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link List} of {@link Instance}.
	 * @param decisionValues
	 * 		A {@link Collection} of decision values of the given universe instances.
	 * @param redArray
	 * 		An array of <code>int</code> attributes.
	 * @return A {@link List} of inspected {@link int} attributes.
	 */
	public static <Sig extends Number> Collection<Integer> inspection(
			FeatureImportance4HeuristicDependencyCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Collection<Integer> decisionValues, int...redArray
	){
		Sig examSig;
		Sig globalSig = calculation.calculate(instances, decisionValues, new IntegerArrayIterator(redArray))
									.getResult();
		LinkedList<Integer> red = new LinkedList<>();
		for (int attr: redArray)	red.add(attr);

		int examAttr;
		for (int i=redArray.length-1; i>=0; i--) {
			examAttr = red.removeLast();
			examSig = calculation.calculate(instances, decisionValues, new IntegerCollectionIterator(red))
								.getResult();
			if (calculation.value1IsBetter(globalSig, examSig, sigDeviation)){
				// not redundant
				red.addFirst(examAttr);
			}
		}
		return red;
	}

	
	/**
	 * Execute inspection for redundant attributes in red. This method will calculate decision 
	 * values of the given universes first, then call <code>inspection(universes, decisionValues, 
	 * red)</code> and return.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) importance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4HeuristicDependencyCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link List} of {@link Instance}.
	 * @param red
	 * 		Reduct attributes.
	 * @return A {@link List} of inspected {@link int} attributes.
	 */
	public static <Sig extends Number> Collection<Integer> inspection(
			FeatureImportance4HeuristicDependencyCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Collection<Integer> red
	){
		return inspection(calculation, sigDeviation, instances, Basic.decisionValues(instances), red);
	}
	
	/**
	 * Execute inspection for redundant attributes in red. This method will calculate based on 
	 * the given <code>universes</code> and <code>decisionValues</code>.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) importance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4HeuristicDependencyCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param decisionValues
	 * 		A collection of decision values of the given universe instances.
	 * @param red
	 * 		Reduct attributes.
	 * @return A {@link List} of inspected {@link int} attributes.
	 */
	public static <Sig extends Number> Collection<Integer> inspection(
			FeatureImportance4HeuristicDependencyCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Collection<Integer> decisionValues,
			Collection<Integer> red
	){
		Sig examSig;
		Sig globalSig = calculation.calculate(instances, decisionValues, new IntegerCollectionIterator(red))
									.getResult();
		int attr;
		LinkedList<Integer> redList = new LinkedList<>(red);
		for (int i=0; i<red.size(); i++) {
			attr = redList.removeLast();
			examSig = calculation.calculate(instances, decisionValues, new IntegerCollectionIterator(redList))
								.getResult();
			if (calculation.value1IsBetter(globalSig, examSig, sigDeviation)){
				// not redundant
				redList.addFirst(attr);
			}
		}
		return red;
	}
}