package featureSelection.repository.algorithm.alg.positiveApproximationAccelerator;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.positiveApproximationAccelerator.EquivalenceClass;
import featureSelection.repository.entity.alg.positiveApproximationAccelerator.MostSignificantAttributeResult;
import featureSelection.repository.support.calculation.alg.PositiveApproximationAcceleratorCalculation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 * Algorithm repository for ACC, which is based on the paper
 * <a href="https://www.sciencedirect.com/science/article/pii/S0004370210000548">
 * "Positive approximation: An accelerator for attribute reduction in rough set theory"
 * </a> by Yuhua Qian, Jiye Liang, etc..
 * 
 * @author Benjamin_L
 */
public class PositiveApproximationAcceleratorOriginalAlgorithm {

	public static class Basic {
		/**
		 * Obtain {@link EquivalenceClass}es induced by the given attributes.
		 * 
		 * @param instances
		 * 		An {@link Instance} {@link Collection}.
		 * @param attributes
		 * 		Attributes of {@link Instance}. (Starts from 1, 0 as decision attribute)
		 * @return An {@link EquivalenceClass} {@link Collection}
		 */
		public static Collection<EquivalenceClass> equivalenceClass(
				Collection<Instance> instances, IntegerIterator attributes
		){
			// Create a Hash for equivalence classes
			Map<IntArrayKey, EquivalenceClass> equClasses = new HashMap<>();
			// Loop over all instances.
			int index;
			int[] keyArray;
			IntArrayKey key;
			EquivalenceClass equClass;
			for (Instance instance: instances) {
				// key = P(e)
				keyArray = Instance.attributeValuesOf(instance, attributes);
				key = new IntArrayKey(keyArray);
				equClass = equClasses.get(key);
				if (equClass==null) {
					// if equivalence class doesn't exist
					//	create one
					equClasses.put(key, equClass=new EquivalenceClass());
					equClass.addUniverse(instance);
					equClass.setDecisionValue(instance.getAttributeValue(0));
				}else {
					// if equivalence class exists
					equClass.addUniverse(instance);
					//	if D(x)!=h.dec
					//		h.dec='/'
					if (Integer.compare(equClass.getDecisionValue(), instance.getAttributeValue(0))!=0)
						equClass.setDecisionValue(-1);
				}
			}
			return equClasses.values();
		}
		
		/**
		 * Filter {@link EquivalenceClass} with dec='/'(value=-1), i.e. to remove positive regions.
		 * 
		 * @param equClasses
		 * 		An {@link EquivalenceClass} {@link Collection}.
		 * @return A {@link Collection} of {@link Instance}.
		 */
		public static Collection<Instance> filteredPositiveRegionInstances(
				Collection<EquivalenceClass> equClasses
		){
			Collection<Instance> instances = new LinkedList<>();
			for (EquivalenceClass equClass: equClasses)
				if (equClass.getDecisionValue()==-1)
					instances.addAll(equClass.getInstances());
			return instances;
		}
	}

	/**
	 * Obtain the core.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) significance.
	 * @param instances
	 * 		An {@link Instance} {@link Collection}.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1, 0 as decision attribute)
	 * @param calculation
	 * 		{@link PositiveApproximationAcceleratorCalculation} instance.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @return A {@link Collection} of {@link Integer} as core.
	 * @throws IllegalArgumentException if exceptions occur in calculations of
	 * 		{@link PositiveApproximationAcceleratorCalculation}.
	 * @throws SecurityException if exceptions occur in calculations of
	 * 		{@link PositiveApproximationAcceleratorCalculation}.
	 */
	public static <Sig extends Number> Collection<Integer> core(
			Collection<Instance> instances, int[] attributes, Sig globalSig,
			PositiveApproximationAcceleratorCalculation<Sig> calculation, Sig sigDeviation
	) throws IllegalArgumentException, SecurityException {
		// core = {}
		Collection<Integer> core = new HashSet<>();
		// Loop over all attributes and check
		Sig sig;
		int[] examAttributes = new int[attributes.length-1];
		for (int i=0; i<examAttributes.length; i++)	examAttributes[i] = attributes[i+1];
		Collection<EquivalenceClass> roughEquClasses;
		for (int i=0; i<attributes.length; i++) {
			// Calculate significance of C-{a}: a.innerSig.
			roughEquClasses = Basic.equivalenceClass(instances, new IntegerArrayIterator(examAttributes));
			sig = calculation.calculate(roughEquClasses, examAttributes.length, instances.size())
							.getResult();
			// if a.innrSig!=C.sig
			if (calculation.value1IsBetter(globalSig, sig, sigDeviation)) {
				// add into core
				core.add(attributes[i]);
			}

			// Next attribute
			if (i<examAttributes.length)	examAttributes[i] = attributes[i];
		}
		return core;
	}

	/**
	 * Obtain the current most significant attributes in attributes outside of reduct.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) significance.
	 * @param equClasses
	 * 		{@link EquivalenceClass} {@link Collection}.
	 * @param red
	 * 		Reduct {@link Collection}.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1, 0 as decision attribute)
	 * @param calculation
	 * 		{@link PositiveApproximationAcceleratorCalculation} instance.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @return An int value as the most significant attribute.
	 */
	public static <Sig extends Number> MostSignificantAttributeResult<Sig>
		mostSignificantAttribute(
			Collection<EquivalenceClass> equClasses, Collection<Integer> red, int[] attributes,
			int insSize, PositiveApproximationAcceleratorCalculation<Sig> calculation,
			Sig sigDeviation
	) {
		// sig = 0; a*=0
		Sig maxSig=null;
		int sigAttr=-1;
		// Loop over C-reduct
		Sig sig;
		Collection<EquivalenceClass> subEquClasses, sigEquClasses = null;
		for (int attr: attributes) {
			if (red.contains(attr))	continue;
			// U/(Red ∪ {a}) = equivalenceClass(U/Red, Red U {a})
			red.add(attr);
			subEquClasses = new HashSet<>();
			for (EquivalenceClass equ: equClasses) {
				subEquClasses.addAll(
						Basic.equivalenceClass(
								equ.getInstances(),
								new IntegerCollectionIterator(red)
						)
				);
			}
			red.remove(attr);
			// Calculate sig : a.outerSig
			sig = calculation.calculate(subEquClasses, red.size(), insSize)
							.getResult();
			// if a.outerSig>sig
			if (maxSig==null || calculation.value1IsBetter(sig, maxSig, sigDeviation)) {
				sigAttr = attr;
				maxSig = sig;
				sigEquClasses = subEquClasses;
			}
		}
		return new MostSignificantAttributeResult<Sig>(sigAttr, maxSig, sigEquClasses);
	}

	/**
	 * Inspect the given reduct and remove redundant attributes.
	 * 
	 * @param <Sig>
	 * 		Type of feature (subset) significance.
	 * @param red
	 * 		Reduct.
	 * @param globalSig
	 * 		The global positive region number.
	 * @param instances
	 * 		{@link Instance} {@link Collection}.
	 * @param calculation
	 * 		{@link PositiveApproximationAcceleratorCalculation} instance.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @throws IllegalArgumentException if exceptions occur in calculations of
	 * 		{@link PositiveApproximationAcceleratorCalculation} instance.
	 * @throws SecurityException if exceptions occur in calculations of
	 * 		{@link PositiveApproximationAcceleratorCalculation} instance.
	 */
	public static <Sig extends Number> void inspection(
			Collection<Integer> red, Sig globalSig, Collection<Instance> instances,
			PositiveApproximationAcceleratorCalculation<Sig> calculation, Sig sigDeviation
	) throws IllegalArgumentException, SecurityException {
		// Loop over attributes in reduct
		Sig examSig;
		Collection<EquivalenceClass> equClasses;
		Integer[] redCopy = red.toArray(new Integer[red.size()]);
		for (int attr: redCopy) {
			// calculate Sig(R-{a}).
			red.remove(attr);
			equClasses = Basic.equivalenceClass(instances, new IntegerCollectionIterator(red));
			examSig = calculation.calculate(equClasses, red.size(), instances.size()).getResult();
			// if (R-{a}.sig==C.sig)
			if (calculation.value1IsBetter(globalSig, examSig, sigDeviation)) {
				// Not redundant
				red.add(attr);
			}
		}
	}
}