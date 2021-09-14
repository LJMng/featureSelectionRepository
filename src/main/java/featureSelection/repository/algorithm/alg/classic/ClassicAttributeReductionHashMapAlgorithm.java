package featureSelection.repository.algorithm.alg.classic;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.alg.ClassicStrategy;
import featureSelection.basic.support.searchStrategy.HashSearchStrategy;
import featureSelection.repository.support.calculation.alg.classic.hash.ClassicHashMapCalculation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An implementation of {@link ClassicAttributeReductionSequentialIDAlgorithm} using {@link HashMap}
 * search strategy.
 *
 * @author Benjamin_L
 */
@ThreadSafetyNotSecured
@RoughSet
public class ClassicAttributeReductionHashMapAlgorithm
	implements ClassicStrategy,
				HashSearchStrategy
{
	public static class Basic {
		/**
		 * Calculate the Equivalence Classes by the given attributes: U/B
		 *
		 * @param instances
		 * 		A {@link Collection} of {@link Instance}
		 * @param attributes
		 * 		Attributes of {@link Instance}. (starts from 1, 0 as the decision attribute)
		 * @return A {@link Map} of Equivalence Classes with {@link Instance}s in {@link Collection}s.
		 */
		public static Map<IntArrayKey, Collection<Instance>> equivalenceClass(
				Collection<Instance> instances, IntegerIterator attributes
		){
			// Initiate a Hash Map to contain equivalence classes.
			Map<IntArrayKey, Collection<Instance>> equivalenceClasses = new HashMap<>(instances.size());
			// For each instance, key = P(x[j]), obtain equivalence classes:
			int[] attrValue;
			IntArrayKey key;
			Collection<Instance> equivalenceClass;
			for (Instance ins: instances) {
				attrValue = Instance.attributeValuesOf(ins, attributes);
				key = new IntArrayKey(attrValue);
				// if key doesn't exist, initiate.
				// else key exists, add into the correspondent equivalence class.
				equivalenceClass = equivalenceClasses.get(key);
				if (equivalenceClass==null){
					equivalenceClasses.put(key, equivalenceClass=new HashSet<>());
				}
				equivalenceClass.add(ins);
			}
			return equivalenceClasses;
		}


		/**
		 * Calculate the Equivalence Classes induced by Decision Attribute: U/D
		 *
		 * @param instances
		 * 		A {@link Collection} of {@link Instance}
		 * @return A {@link Collection} of Equivalence Classes in {@link Instance} {@link List}
		 */
		public static Map<Integer, Collection<Instance>> equivalenceClassOfDecisionAttribute(
				Collection<Instance> instances
		){
			// Initiate a Hash map H.
			Map<Integer, Collection<Instance>> equivalenceClass = new HashMap<>(instances.size());
			// For u, key = P(x[j]), execute:
			Collection<Instance> equSet;
			for (Instance ins: instances) {
				// if key doesn't exist, initiate,
				// if key exists, add into the correspondent equivalence class
				equSet = equivalenceClass.get(ins.getAttributeValue(0));
				if (equSet==null) {
					equivalenceClass.put(
							ins.getAttributeValue(0),
							equSet=new HashSet<>(instances.size())
					);
				}
				equSet.add(ins);
			}
			// return
			return equivalenceClass;
		}


		/**
		 * Check if all {@link Instance}s are in the same decision equivalent class.
		 *
		 * @param decEClassKeys
		 * 		A {@link Collection} of {@link IntArrayKey} {@link Collection} as equivalence
		 * 		classes' decision keys.
		 * @param equClassKey
		 * 		The key of an equivalence class.
		 * @return <code>true</code> if all {@link Instance} at the same decision equivalence
		 * 		class.
		 */
		public static boolean allInstancesAtTheSameDecEquClass(
				Collection<Collection<IntArrayKey>> decEClassKeys, IntArrayKey equClassKey
		) {
			int classNum = 0;
			for (Collection<IntArrayKey> decKeys : decEClassKeys) {
				if (decKeys.contains(equClassKey))	classNum++;
				if (classNum>1)						return false;
			}
			return true;
		}


		/**
		 * Get the intersection of two {@link Instance} {@link Collection}s.
		 *
		 * @param collection1
		 * 		An {@link Instance} {@link Collection}.
		 * @param collection2
		 * 		Another {@link Instance} {@link Collection}.
		 * @return intersection.
		 */
		public static Collection<Instance> intersectionOf(
			Collection<Instance> collection1, Collection<Instance> collection2
		){
			if (collection1.isEmpty() || collection2.isEmpty()){
				return Collections.emptyList();
			}

			if (collection1 instanceof List) {
				final Collection<Instance> sm, lg;
				if (collection1.size()>collection2.size()) {
					lg = new HashSet<>(collection1);
					sm = collection2;
				}else {
					lg = new HashSet<>(collection2);
					sm = collection1;
				}
				return sm.stream().filter(sp->lg.contains(sp)).collect(Collectors.toList());//*/
			}else {
				final Collection<Instance> sm, lg;
				if (collection1.size()>collection2.size()) {
					lg = collection1;
					sm = collection2;
				}else {
					lg = collection2;
					sm = collection1;
				}
				final Collection<Instance> hashOfLgDiscern = new HashSet<>(lg);
				return sm.stream().filter(sp->hashOfLgDiscern.contains(sp))
								.collect(Collectors.toSet());
			}
		}
	}

	/**
	 * Get the core.
	 *
	 * @param <Sig>
	 * 		Type of feature (subset) significance.
	 * @param calculation
	 * 		An implemented {@link ClassicHashMapCalculation} instance.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param decClasses
	 * 		A {@link Collection} of {@link Instance} {@link List} as decision attribute
	 * 		equivalence classes.
	 * @param globalSig
	 * 		The global significance.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @return A {@link List} of {@link Integer} values as core.
	 */
	public static <Sig extends Number> Collection<Integer> core(
			ClassicHashMapCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances,
			Map<Integer, Collection<Instance>> decClasses, Sig globalSig,
			int...attributes
	){
		// Initiate a Hash Set to collect core attributes.
		Collection<Integer> core = new HashSet<>();
		// Loop over all attributes
		Sig redSig;
		int[] examAttributes = new int[attributes.length-1];
		for (int i=0; i<examAttributes.length; i++)	examAttributes[i] = attributes[i+1];
		for (int i=0; i<attributes.length; i++) {
			// Calculate the significance with an attribute
			redSig = calculation.calculate(instances, new IntegerArrayIterator(examAttributes), decClasses)
								.getResult();
			// Check if the attribute is core.
			if (calculation.value1IsBetter(globalSig, redSig, sigDeviation)){
				core.add(attributes[i]);
			}
			// next attribute
			if (i!=attributes.length-1){
				examAttributes[i] = attributes[i];
			}
		}
		return core;
	}

	/**
	 * Get the least significant attribute.
	 *
	 * @param <Sig>
	 * 		Type of feature (subset) significance.
	 * @param calculation
	 * 		An implemented {@link ClassicHashMapCalculation} instance.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param decClasses
	 * 		A {@link Collection} of {@link Instance} {@link List} as decision attribute
	 * 		equivalence classes.
	 * @param red
	 *      Current reduct.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @return An attribute with least significance.
	 */
	public static <Sig extends Number> int leastSignificantAttribute(
			ClassicHashMapCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Map<Integer, Collection<Instance>> decClasses,
			Collection<Integer> red, int[] attributes
	){
		int sigAttr = -1;
		Sig subSig, min = null;
		// Loop over potential attributes
		int i=0;
		int[] attribute = new int[red.size()+1];	for (int r : red) attribute[i++] = r;
		for (int attr : attributes) {
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
	 * 		Implemented {@link ClassicHashMapCalculation} instance.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param decClasses
	 * 		A {@link Collection} of {@link Instance} {@link List} as decision attribute equivalent
	 * 		classes.
	 * @param red
	 * 		Reduct attributes.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @return A <code>int</code> value as the most significant attribute's index.
	 */
	public static <Sig extends Number> int mostSignificantAttribute(
			ClassicHashMapCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Map<Integer, Collection<Instance>> decClasses,
			Collection<Integer> red, int[] attributes
	){
		int sigAttr = -1;
		Sig subSig, max = null;
		// Loop over potential attributes
		int i=0;
		int[] attribute = new int[red.size()+1];	for (int r : red) attribute[i++] = r;
		for (int attr : attributes) {
			if (!red.contains(attr)) {
				attribute[attribute.length-1] = attr;
				subSig = calculation.calculate(instances, new IntegerArrayIterator(attribute), decClasses)
									.getResult();
				if (calculation.value1IsBetter(subSig, max, sigDeviation) || sigAttr==-1) {
					max = subSig;
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
	 * 		Implemented {@link ClassicHashMapCalculation} instance.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		The {@link Instance} Collection.
	 * @param redArray
	 * 		Reduct attributes.
	 * @return Inspected reduct.
	 */
	public static <Sig extends Number> Collection<Integer> inspection(
			ClassicHashMapCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, int...redArray
	){
		Sig examSig;
		Map<Integer, Collection<Instance>> decEqu = Basic.equivalenceClassOfDecisionAttribute(instances);
		Sig globalSig = calculation.calculate(instances, new IntegerArrayIterator(redArray), decEqu)
									.getResult();
		// Loop over attributes in reduct one by one and try to remove one each time.
		int attr;
		LinkedList<Integer> red = new LinkedList<>();	for (int r: redArray)	red.add(r);
		for (int i=redArray.length-1; i>=0; i--) {
			attr = red.removeLast();
			examSig = calculation.calculate(instances, new IntegerCollectionIterator(red), decEqu)
								.getResult();
			if (calculation.value1IsBetter(globalSig, examSig, sigDeviation)){
				// not redundant.
				red.addFirst(attr);
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
	 * 		Implemented {@link ClassicHashMapCalculation} instance.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		The {@link Instance} Collection.
	 * @param red
	 * 		Reduct attributes.
	 */
	public static <Sig extends Number> void inspection(
			ClassicHashMapCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Collection<Integer> red
	){
		Map<Integer, Collection<Instance>> decEqu = Basic.equivalenceClassOfDecisionAttribute(instances);
		Sig globalSig = calculation.calculate(instances, new IntegerCollectionIterator(red), decEqu)
									.getResult();
		// Loop over attributes in reduct one by one and try to remove one each time.
		Sig examSig;
		Integer[] redCopy = red.toArray(new Integer[red.size()]);
		for (int examAttr: redCopy) {
			red.remove(examAttr);
			examSig = calculation.calculate(instances, new IntegerCollectionIterator(red), decEqu)
								.getResult();
			if (calculation.value1IsBetter(globalSig, examSig, sigDeviation)){
				// not redundant.
				red.add(examAttr);
			}
		}
	}
}