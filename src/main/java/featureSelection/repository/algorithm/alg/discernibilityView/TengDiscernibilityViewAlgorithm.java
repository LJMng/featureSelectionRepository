package featureSelection.repository.algorithm.alg.discernibilityView;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionHashMapAlgorithm;
import featureSelection.repository.support.calculation.alg.FeatureImportance4TengDiscernibilityView;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Algorithm repository of FAR-DV, which based on the paper
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S0020025515005605">
 * "Efficient attribute reduction from the viewpoint of discernibility"</a> by Shu-Hua Teng,
 * Min Lu, A-Feng Yang, Jun Zhang, Yongjian Nian, Mi He.
 *
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class TengDiscernibilityViewAlgorithm {

	/**
	 * Some basic algorithms of FAR-DV.
	 */
	public static class Basic {
		
		/**
		 * Obtain the equivalence classes induced by the given attributes: <code>U/P</code>.
		 * 
		 * @see ClassicAttributeReductionHashMapAlgorithm.Basic#equivalenceClass(Collection,
		 * 		IntegerIterator)
		 * 
		 * @param instances
		 * 		An {@link Instance} {@link Collection} to be partitioned.
		 * @param attributes
		 * 		Attributes of {@link Instance}. (Starts from 1)
		 * @return A {@link Map} with equivalence values as keys and {@link Instance}
		 * 		{@link Collection} as equivalence class as values.
		 */
		public static Map<IntArrayKey, Collection<Instance>> equivalenceClass(
				Collection<Instance> instances, IntegerIterator attributes
		){
			return ClassicAttributeReductionHashMapAlgorithm
					.Basic
					.equivalenceClass(instances, attributes);
		}
		
		/**
		 * Obtain the equivalence classes partitioned induced by the given attributes:
		 * <code>(U/P)/Q</code>.
		 *
		 * @param equClasses
		 * 		A {@link Collection} of {@link Instance} {@link Collection} as equivalence classes
		 * 		to be further induced: <code>U/P</code>
		 * @param attributes
		 * 		Attributes of {@link Instance}. (Starts from 1): <code>Q</code>
		 * @return A {@link Collection} of {@link Instance} {@link Collection} as equivalence
		 * 		classes.
		 */
		public static Collection<Collection<Instance>> gainEquivalenceClass(
				Collection<Collection<Instance>> equClasses, IntegerIterator attributes
		){
			Collection<Collection<Instance>> newEquClasses = new LinkedList<>();
			// Loop over equivalence class in equivalence classes.
			for (Collection<Instance> equClass: equClasses) {
				// Use the given attributes to partition instances inside and collect results.
				newEquClasses.addAll(equivalenceClass(equClass, attributes).values());
			}
			return newEquClasses;
		}
		
	}
	
	/**
	 * Check if the given Equivalence Class is a part of the given Equivalence Classes: with every
	 * {@link Instance} in <code>equClass</code> all in the same equivalence class of
	 * <code>equClasses</code>. 
	 * 
	 * @param equClasses
	 * 		A {@link Collection} of {@link Instance} {@link Collection} as equivalence classes which
	 * 		contains a lot of {@link Instance} {@link Collection}s which each is considered as an
	 * 		equivalence class.
	 * @param equClass
	 * 		An {@link Instance} {@link Collection} as an equivalence class.
	 * @return <code>true</code> if <code>equClass</code> is a sub-element of <code>equClasses</code>.
	 */
	public static boolean isSubEquivalenceClassOf(
			Collection<Collection<Instance>> equClasses, Collection<Instance> equClass
	) {
		for (Collection<Instance> instances: equClasses) {
			if (instances.containsAll(equClass)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Select the most significant attribute bases on (relative) discernibility degree based outer 
	 * significance calculation: 
	 * <ul>
	 * 	<li>select the one with max. outer significance;</li>
	 * 	<li>if two outer significances are the same, select the one with the smallest discernibility
	 * 		degree.
	 * 	</li>
	 * </ul>
	 * 
	 * @param <Sig>
	 * 		Type of attribute significance.
	 * @param insSize
	 * 		The size of {@link Instance}s in <code>redEquClasses</code>.
	 * @param attributes
	 * 		Attributes of {@link Instance}
	 * @param redundantAttributes
	 * 		Redundant attributes.
	 * @param redEquClasses
	 * 		Equivalence Classes partitioned by reduct.
	 * @param redRelativeDisDegree
	 * 		The relative discernibility degree of the reduct.
	 * @param calculation
	 * 		{@link FeatureImportance4TengDiscernibilityView} instance.
	 * @return the selected most significant attribute.
	 */
	public static <Sig extends Number> int mostSignificantAttribute(
			int insSize, int[] attributes, Collection<Integer> redundantAttributes,
			Collection<Collection<Instance>> redEquClasses, Sig redRelativeDisDegree,
			FeatureImportance4TengDiscernibilityView<Sig> calculation
	) {
		// Initiate.
		int sigAttr = -1, sigValue = 0, disValue = -1;
		
		int outerSigOfAttr;
		IntegerIterator gainedAttribute = new IntegerArrayIterator(0);
		for (int attr: attributes) {
			// skip attribute in A'(i.e. attribute not in A[j])
			if (redundantAttributes.contains(attr))	continue;
			// Calculate SIG<sup>outer</sup><sub>dis</sub>(a[t], red, D).
			outerSigOfAttr = calculation.calculateOuterSignificance(
								// U/red
								redEquClasses, 
								// |DIS(D/red)|
								redRelativeDisDegree, 
								// a[t]
								new IntegerArrayIterator(attr), 
								// D
								gainedAttribute
							).getResult().intValue();
			// Update sig.
			if (sigAttr==-1) {
				sigAttr = attr;
				sigValue = outerSigOfAttr;
				disValue = calculation.calculate(insSize, redEquClasses).getResult().intValue();
			}else {
				int cmp = Integer.compare(outerSigOfAttr, sigValue);
				if (cmp<0) {
					// do nothing ...
				}else {
					int attrDis = calculation.calculate(insSize, redEquClasses).getResult().intValue();
					if (cmp>0 || Integer.compare(attrDis, disValue)<0) {
						sigAttr = attr;
						sigValue = outerSigOfAttr;
						disValue = attrDis;
					}
				}
			}
		}
		return sigAttr;
	}
}