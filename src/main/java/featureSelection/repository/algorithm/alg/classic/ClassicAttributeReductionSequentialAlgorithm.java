package featureSelection.repository.algorithm.alg.classic;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.alg.ClassicStrategy;
import featureSelection.basic.support.searchStrategy.SequentialSearchStrategy;
import featureSelection.repository.support.calculation.alg.classic.sequential.ClassicSequentialCalculation;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Algorithm repository of Classic Attribute Reduction.
 *
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class ClassicAttributeReductionSequentialAlgorithm 
	implements ClassicStrategy,
				SequentialSearchStrategy
{
	public static class Basic {
		/**
		 * Obtain Equivalence Classes induced by the given attributes.
		 * 
		 * @param instances
		 * 		A {@link Collection} of {@link Instance}s.
		 * @param attributes
		 * 		Attributes of {@link Instance}. (starts from 1, 0 as decision attribute)
		 * @return A {@link Collection} of Equivalence Class in {@link Instance} {@link List}
		 */
		public static Collection<List<Instance>> equivalenceClass(
				Collection<Instance> instances, IntegerIterator attributes
		){
			return ClassicAttributeReductionSequentialIDAlgorithm
					.Basic
					.equivalenceClass(instances, attributes);
		}
		
		/**
		 * Obtain Equivalence Classes induced by decision attribute
		 * 
		 * @param instances
		 * 		A {@link Collection} of {@link Instance}
		 * @return A {@link Collection} of Equivalence Class in {@link Instance} {@link List}
		 */
		public static Collection<List<Instance>> equivalenceClassOfDecisionAttribute(
				Collection<Instance> instances
		){
			return equivalenceClass(instances, new IntegerArrayIterator(0));
		}
	
		/**
		 * Check if {@link Instance} {@link List} is part of positive region.
		 * 
		 * @param equClass
		 * 		{@link Instance} {@link List} as an equivalence class.
		 * @param decEClasses
		 * 		A {@link Collection} of {@link Instance} {@link List} as decision attribute
		 * 		equivalence classes.
		 * @param attributes
		 * 		Attributes of {@link Instance}.
		 * @return <code>true</code> if it is part of positive region.
		 */
		public static boolean isPositiveRegion(
				List<Instance> equClass, Collection<List<Instance>> decEClasses,
				IntegerIterator attributes
		) {
			// obtain the 1st instance in equivalence class
			Instance x = equClass.get(0);
			// classNum = 0 / found = false;
			boolean found = false;
			// Loop over U/D
			for (List<Instance> decEquClass: decEClasses) {
				// classNum = classNum + belong(x, records in DEClass[j]);
				if (!found) {
					found = inUniverseCollection(x, decEquClass, attributes);
				}else if (inUniverseCollection(x, decEquClass, attributes)) {
					return false;
				}
			}
			return true;
		}
		
		/**
		 * Check if {@link Instance} is in the <code>decU</code> Collection.
		 * 
		 * @param x
		 * 		An {@link Instance}
		 * @param decU
		 * 		A {@link Collection} of {@link Instance} as a decision attribute equivalence class
		 * @param attributes
		 * 		Attributes of {@link Instance} for equivalent classes.
		 * @return <code>true</code> if it is.
		 */
		public static boolean inUniverseCollection(
				Instance x, Collection<Instance> decU, IntegerIterator attributes
		) {
			// Loop over decU
			int attr;
			DecUniverseLoop:
			for (Instance u : decU) {
				// for m=1 to number of P, i.e. attributes in P.
				attributes.reset();
				while (attributes.hasNext()) {
					attr = attributes.next();
					// if (record[m] !=  records[k][m])
					if (Integer.compare(u.getAttributeValue(attr), x.getAttributeValue(attr))!=0)
						continue DecUniverseLoop;
				}
				return true;
			}
			return false;
		}
	}

	/**
	 * Get the core.
	 * 
	 * @param <Sig>
	 * 		{@link Number} implemented type as the value of Significance.
	 * @param calculation
	 * 		Implemented {@link ClassicSequentialCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link List} of {@link Instance}.
	 * @param decClasses
	 * 		A {@link Collection} of {@link Instance} {@link List} as decision attribute equivalence
	 * 		classes.
	 * @param globalSig
	 * 		The global positive region.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @return A {@link List} of {@link Integer} values as core.
	 */
	public static <Sig extends Number> List<Integer> core(
			ClassicSequentialCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Collection<List<Instance>> decClasses, Sig globalSig,
			int[] attributes
	){
		List<Integer> core = new LinkedList<>();
		// Loop over all attributes
		Sig redSig;
		int[] exAttribute = new int[attributes.length-1];
		for (int i=0; i<exAttribute.length; i++){
			exAttribute[i] = attributes[i+1];
		}
		for ( int i=0; i<attributes.length; i++ ) {
			redSig = calculation.calculate(instances, new IntegerArrayIterator(exAttribute), decClasses)
								.getResult();
			if (calculation.value1IsBetter(globalSig, redSig, sigDeviation)){
				core.add(attributes[i]);
			}
			// next attribute
			if (i<exAttribute.length){
				exAttribute[i] = attributes[i];
			}
		}
		return core;
	}
	
	/**
	 * Get the least significant attribute.
	 * 
	 * @param <Sig>
	 * 		{@link Number} implemented type as the value of Significance.
	 * @param calculation
	 * 		Implemented {@link ClassicSequentialCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link List} of {@link Instance}.
	 * @param decClasses
	 * 		A {@link Collection} of {@link Instance} {@link List} as decision attribute equivalence
	 * 		classes.
	 * @param red
	 * 		Reduct attributes.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @return An int value as the most significant attribute.
	 */
	public static <Sig extends Number> int leastSignificantAttribute(
			ClassicSequentialCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Collection<List<Instance>> decClasses,
			Collection<Integer> red, IntegerIterator attributes
	){
		int sigAttr = -1;
		Sig min = null, subSig;
		// Loop over potential attributes
		int i=0;
		int[] attribute = new int[red.size()+1];	for (int r : red) attribute[i++] = r;
		int attr;
		attributes.reset();
		while (attributes.hasNext()) {
			attr = attributes.next();
			if (!red.contains(attr)) {
				attribute[attribute.length-1] = attr;
				subSig = calculation.calculate(instances, new IntegerArrayIterator(attribute), decClasses)
									.getResult();
				if (calculation.value1IsBetter(min, subSig, sigDeviation) || sigAttr==-1) {
					min = subSig;
					sigAttr = attr;
				}
			}
		}
		return sigAttr;
	}
	
	/**
	 * Get the most significant attribute.
	 * 
	 * @param <Sig>
	 * 		{@link Number} implemented type as the value of Significance.
	 * @param calculation
	 * 		Implemented {@link ClassicSequentialCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param universes
	 * 		A {@link List} of {@link Instance}.
	 * @param decClasses
	 * 		A {@link Collection} of {@link Instance} {@link List} as decision attribute equivalence
	 * 		classes.
	 * @param red
	 * 		Reduct attributes.
	 * @param redSig
	 * 		The reduct positive region value.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @return A {@link int} value as the most significant attribute's index.
	 */
	public static <Sig extends Number> int mostSignificantAttribute(
			ClassicSequentialCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> universes, Collection<List<Instance>> decClasses,
			Collection<Integer> red, Sig redSig, IntegerIterator attributes
	){
		int sigAttr = -1;
		Sig max = null, sig, subSig;
		// Loop over potential attributes
		int i=0;
		int[] attribute = new int[red.size()+1];	for (int r : red) attribute[i++] = r;
		int attr;
		attributes.reset();
		while (attributes.hasNext()) {
			attr = attributes.next();
			if (!red.contains(attr)) {
				attribute[attribute.length-1] = attr;
				subSig = calculation.calculate(universes, new IntegerArrayIterator(attribute), decClasses)
									.getResult();
				sig = calculation.difference(subSig, redSig);
				if (calculation.value1IsBetter(sig, max, sigDeviation) || sigAttr==-1) {
					max = sig;
					sigAttr = attr;
				}
			}
		}
		return sigAttr;
	}
	
	/**
	 * Inspection of reduct: remove redundant attributes.
	 * 
	 * @param <Sig>
	 * 		{@link Number} implemented type as the value of Significance.
	 * @param calculation
	 * 		Implemented {@link ClassicSequentialCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		The {@link Instance} list.
	 * @param redArray
	 * 		Reduct attributes.
	 * @return Inspected reduct attributes.
	 */
	public static <Sig extends Number> Collection<Integer> inspection(
			ClassicSequentialCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, int...redArray
	){
		Collection<List<Instance>> decEqu = Basic.equivalenceClassOfDecisionAttribute(instances);
		Sig globalSig = calculation.calculate(instances, new IntegerArrayIterator(redArray), decEqu)
									.getResult();
		LinkedList<Integer> red = new LinkedList<>();	for (int r: redArray) red.add(r);
		Sig examPos;
		int examAttr;
		for ( int i=red.size()-1; i>=0; i-- ) {
			examAttr = red.removeLast();
			examPos = calculation.calculate(instances, new IntegerCollectionIterator(red), decEqu)
								.getResult();
			if (calculation.value1IsBetter(globalSig, examPos, sigDeviation)){
				// not redundant
				red.addFirst(examAttr);
			}
		}
		return red;
	}

	/**
	 * Inspection of reduct: remove redundant attributes.
	 * 
	 * @param <Sig>
	 * 		{@link Number} implemented type as the value of Significance.
	 * @param calculation
	 * 		Implemented {@link ClassicSequentialCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		The {@link Instance} list.
	 * @param red
	 * 		Reduct attributes.
	 */
	public static <Sig extends Number> void inspection(
			ClassicSequentialCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Collection<Integer> red
	){
		Collection<List<Instance>> decEqu = Basic.equivalenceClassOfDecisionAttribute(instances);
		Sig globalSig = calculation.calculate(instances, new IntegerCollectionIterator(red), decEqu)
									.getResult();
		Sig examSig;
		Integer[] attrCopy = red.toArray(new Integer[red.size()]);
		for (int examAttr: attrCopy) {
			red.remove(examAttr);
			examSig = calculation.calculate(instances, new IntegerCollectionIterator(red), decEqu)
								.getResult();
			if (calculation.value1IsBetter(globalSig, examSig, sigDeviation)){
				// not redundant
				red.add(examAttr);
			}
		}
	}
}