package featureSelection.repository.algorithm.alg.liangIncrementalAlgorithm;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionHashMapAlgorithm;
import featureSelection.repository.algorithm.alg.positiveApproximationAccelerator.PositiveApproximationAcceleratorOriginalAlgorithm;
import featureSelection.repository.entity.alg.liangIncrementalAlgorithm.*;
import featureSelection.repository.entity.alg.liangIncrementalAlgorithm.classSetType.ClassSetType;
import featureSelection.repository.support.calculation.alg.FeatureImportance4LiangIncremental;
import featureSelection.repository.support.calculation.alg.PositiveApproximationAcceleratorCalculation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Algorithm repository of <strong>Liang-Incremental algorithm for Feature Selection</strong>,
 * which bases on the paper <a href="https://ieeexplore.ieee.org/document/6247431/">"A Group
 * Incremental Approach to Feature Selection Applying Rough Set Technique"</a> by Jiye Liang,
 * Feng Wang, Chuangyin Dang, Yuhua Qian.
 * 
 * @author Benjamin_L
 */
public class LiangIncrementalAlgorithm {

	/**
	 * Some basic implementations for the general algorithm.
	 */
	public static class Basic {
		/**
		 * Some mathematical implementations.
		 */
		public static class Mathematical {
			/**
			 * Obtain the intersection of 2 {@link Instance} {@link Collection}s:
			 * <code>Instance1</code> ∩ <code>Instance2</code>
			 * <p>
			 * The input <code>instance1</code> and <code>instance2</code> should be of
			 * the same {@link Collection} type.
			 *
			 * @param instance1
			 * 		A {@link Collection} of {@link Instance}.
			 * @param instance2
			 * 		Another {@link Collection} of {@link Instance}.
			 * @return The intersection result in {@link Instance} {@link Collection}.
			 */
			public static Collection<Instance> intersectionOf(
					Collection<Instance> instance1,
					Collection<Instance> instance2
			){
				// Return an empty list if any is null(empty)
				if (instance1==null || instance2==null)	return Collections.emptyList();

				Collection<Instance> sm;
				Collection<Instance> lg;
				if (instance1.size()>instance2.size()) {
					lg = instance1;
					sm = instance2;
				}else {
					lg = instance2;
					sm = instance1;
				}
				Collection<Instance> hashOfLgDiscern =
							lg instanceof Set? new HashSet<>(lg): lg;
				return sm.stream()
							.filter(each->hashOfLgDiscern.contains(each))
							.collect(Collectors.toList());
			}
		}
		
		/**
		 * Obtain the Equivalence Classes of instances induced by given attributes: <code>U/P</code>
		 * 
		 * @see ClassicAttributeReductionHashMapAlgorithm.Basic#equivalenceClass(Collection,
		 *      IntegerIterator)
		 * 
		 * @param instances
		 * 		A {@link Collection} of {@link Instance}.
		 * @param attributes
		 * 		Attributes of {@link Instance}. (Starts from 1, 0 as decision attribute)
		 * @return A {@link Map} of Equivalence Classes with {@link Instance} in {@link List}.
		 */
		public static Map<IntArrayKey, Collection<Instance>> equivalenceClass(
				Collection<Instance> instances, IntegerIterator attributes
		){
			return ClassicAttributeReductionHashMapAlgorithm
					.Basic
					.equivalenceClass(instances, attributes);
		}
		
		/**
		 * Locate the Equivalence Class with the same attribute values as the <code>instance</code>
		 * does in <code>equClasses</code>.
		 * 
		 * @param equClasses
		 * 		Equivalence Classes wrapped in a {@link Map} with equivalence values as keys and
		 * 		{@link Instance} {@link Collection} which are the correspondent equivalence
		 * 		classes as values.
		 * @param instance
		 * 		An {@link Instance}.
		 * @param attributes
		 * 		Attributes of {@link Instance}. (starts from 1, 0 as decision value)
		 * @return Collected {@link Instance} {@link Collection} as an equivalent class.
		 */
		public static Collection<Instance> locateInEquivalenceClasses(
				Map<IntArrayKey, Collection<Instance>> equClasses,
				Instance instance, IntegerIterator attributes
		){
			int[] newInstanceRedValue = Instance.attributeValuesOf(instance, attributes);
			return equClasses.get(new IntArrayKey(newInstanceRedValue));
		}
	}

	/**
	 * Implementations for incremental-related algorithms.
	 */
	public static class Incremental {
		/**
		 * Combine previous equivalence classes and new equivalence classes.
		 * <p>
		 * <strong>Notice</strong>: While a {@link Map} of combined equivalence classes is
		 * generated and returned and contains the same {@link Collection} instances in the given
		 * <code>previousEquClasses</code> and <code>newEquClasses</code>, those can not be
		 * combined are wrapped into {@link DefaultEquivalenceClass}es with
		 * {@link DefaultEquivalenceClass#getClassSetType()} being set to demonstrate the source
		 * of the equivalence class. For the original instances <code>previousEquClasses</code> and
		 * <code>newEquClasses</code>, all entries were <strong>removed</strong> in the execution.
		 * 
		 * @param previousEquClasses
		 * 		A {@link Map} with equivalence keys as keys and {@link Instance} {@link Collection}s
		 * 		as the corresponding equivalence classes as values. All {@link Instance} should be
		 * 		the previous one.
		 * @param newEquClasses
		 * 		A {@link Map} with equivalence keys as keys and {@link Instance} {@link Collection}s
		 * 		as the corresponding equivalence classes as values. All {@link Instance} should be
		 * 		the new one.
		 * @return A {@link MixedEquivalenceClassSequentialList} with the previous and new equivalence
		 *      classes combined and collected.
		 */
		public static MixedEquivalenceClassSequentialList combineEquivalenceClassesOfPreviousNNew(
			Map<IntArrayKey, Collection<Instance>> previousEquClasses,
			Map<IntArrayKey, Collection<Instance>> newEquClasses
		){
			// Initiate a new Equivalence Class list.
			Collection<EquivalenceClassInterf> equClasses = new LinkedList<>();
			// Loop over newEquClasses
			Iterator<Map.Entry<IntArrayKey, Collection<Instance>>> equClassIterator =
					newEquClasses.entrySet().iterator();
			Map.Entry<IntArrayKey, Collection<Instance>> equClass;
			while (equClassIterator.hasNext()) {
				equClass = equClassIterator.next();
				// search in previous equivalence classes.
				Collection<Instance> previousEquClass = previousEquClasses.get(equClass.getKey());
				// if exist
				if (previousEquClass!=null) {
					//	combine the previous and new equivalence classes.
					MixedEquivalenceClass mixedEquClass = new MixedEquivalenceClass();
					mixedEquClass.setPreviousInstances(previousEquClass);
					mixedEquClass.setNewInstances(equClass.getValue());
					equClasses.add(mixedEquClass);
					//	remove from previous & new equivalence classes respectively.
					previousEquClasses.remove(equClass.getKey());
					equClassIterator.remove();
				}
			}
			// Load and load previous equivalence classes that can not be combined: ones remain.
			int previousOnly = previousEquClasses.size();
			equClassIterator = previousEquClasses.entrySet().iterator();
			while (equClassIterator.hasNext()) {
				equClass = equClassIterator.next();
				// mark equivalence class contains previous equivalence class only.
				equClasses.add(new DefaultEquivalenceClass(ClassSetType.PREVIOUS, equClass.getValue()));
				equClassIterator.remove();
			}
			// Load and load new equivalence classes that can not be combined: ones remain.
			int newOnly = newEquClasses.size();
			equClassIterator = newEquClasses.entrySet().iterator();
			while (equClassIterator.hasNext()) {
				equClass = equClassIterator.next();
				// mark equivalence class contains new equivalence class only.
				equClasses.add(new DefaultEquivalenceClass(ClassSetType.NEW, equClass.getValue()));
				equClassIterator.remove();
			}
			return new MixedEquivalenceClassSequentialList(
						equClasses, 
						equClasses.size()-previousOnly-newOnly, previousOnly, newOnly
					);
		}

		/**
		 *
		 * Get the current most significant attribute.
		 *
		 * @param <Sig>
		 * 		Type of feature (subset) importance.
		 * @param previousInstances
		 *      A {@link Collection} of previous {@link Instance}s
		 * @param newInstances
		 *      A {@link Collection} of newly added {@link Instance}s
		 * @param decEquClassesCMBResult
		 *      {@link MixedEquivalenceClassSequentialList} that contains mixed result of
		 *      the previous equivalence classes and the new equivalence classes induced by
		 *      decision attribute: <strong>(U∪U<sub>X</sub>)/D</strong>
		 * @param calculation
		 *      Implemented {@link FeatureImportance4LiangIncremental} instance to calculate
		 *      feature subset significance.
		 * @param staticCalculation
		 *      Implemented {@link PositiveApproximationAcceleratorCalculation} instance used to
		 *      calculate feature subset significance of the static data.
		 * @param sigDeviation
		 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
		 * 		the difference between two sigs is less than the given deviation value.
		 * @param previousSigWithDenominator
		 *      Previous feature (subset) significance value(entropy mostly) with denominator.
		 * @param reduct
		 *      Current reduct.
		 * @param attributes
		 *      Attributes of {@link Instance}. (Starts from 1, 0 as decision attribute)
		 * @return Results wrapped in {@link MostSignificantAttributeResult}.
		 */
		public static <Sig extends Number> MostSignificantAttributeResult<Sig>
			mostSignificantAttribute(
				Collection<Instance> previousInstances, Collection<Instance> newInstances,
				MixedEquivalenceClassSequentialList decEquClassesCMBResult,
				FeatureImportance4LiangIncremental<Sig> calculation,
				PositiveApproximationAcceleratorCalculation<Sig> staticCalculation,
				Sig sigDeviation, boolean previousSigWithDenominator,
				Collection<Integer> reduct, int[] attributes
		) {
			// Loop over attributes not in reduct.
			Sig sig, previousSig, newSig, maxSig = null;
			int maxSigAttr = -1;
			MixedEquivalenceClassSequentialList mixEquClassesPack;
			Map<IntArrayKey, Collection<Instance>> previousRedEquClasses, newRedEquClasses;
			for (int attr: attributes) {
				if (reduct.contains(attr))	continue;
				reduct.add(attr);
				// obtain U/B
				previousRedEquClasses = 
					Basic.equivalenceClass(previousInstances, new IntegerCollectionIterator(reduct));
				// obtain U<sub>X</sub>/B
				newRedEquClasses = 
					Basic
						.equivalenceClass(newInstances, new IntegerCollectionIterator(reduct));
				// obtain (U∪U<sub>X</sub>)/red∪{a}
				mixEquClassesPack = 
					Incremental
						.combineEquivalenceClassesOfPreviousNNew(previousRedEquClasses, newRedEquClasses);
				// Calculate Sig(previous U)
				previousSig = staticCalculation.calculate(
								PositiveApproximationAcceleratorOriginalAlgorithm
									.Basic
									.equivalenceClass(previousInstances, new IntegerCollectionIterator(reduct)),
								reduct.size()
							).getResult();
				// Calculate Sig(new U)
				newSig = staticCalculation.calculate(
								PositiveApproximationAcceleratorOriginalAlgorithm
									.Basic
									.equivalenceClass(newInstances, new IntegerCollectionIterator(reduct)),
								reduct.size()
							).getResult();
				// Calculate outer sig
				sig = calculation.calculate(
						mixEquClassesPack, decEquClassesCMBResult, 
						previousInstances.size(), newInstances.size(),
						previousSig, newSig, 
						previousSigWithDenominator, reduct.size()
					).getResult();
				if (maxSig==null || calculation.value1IsBetter(sig, maxSig, sigDeviation)) {
					maxSig = sig;
					maxSigAttr = attr;
				}
			}
			return new MostSignificantAttributeResult<Sig>(maxSigAttr, maxSig);
		}
	}

	public static class Inspection {
		public static <Sig extends Number> void inspect(
				Collection<Instance> previousUniverse, Instance newInstance,
				Collection<Integer> reduct, Sig redSig, Sig previuosSig,
				boolean previousSigWithDenominator,
				FeatureImportance4LiangIncremental<Sig> calculation, Sig sigDeviation
		){
			//	Loop over attributes in reduct
			int[] reductArray = reduct.stream().mapToInt(v->v).toArray();
			
			IntegerIterator innerRed;
			Collection<Instance> innerSigRedEquClass;
			Map<IntArrayKey, Collection<Instance>> innerSigRedEquClasses;
			for (int attr: reductArray) {
				reduct.remove(attr);
				innerRed = new IntegerCollectionIterator(reduct);
				// Compute Sig<sub>U∪{x}</sub><pub>inner</pub>(a, B, D)
				// if Sig<sub>U∪{x}</sub><pub>inner</pub>(a, B, D) ==0, B<-B-{a}. (i.e. redundant)
				innerSigRedEquClasses = 
						Basic.equivalenceClass(previousUniverse, innerRed);
				innerSigRedEquClass =
						Basic.locateInEquivalenceClasses(
								innerSigRedEquClasses,
								newInstance,
								innerRed
						);
				
				Sig innerSig = calculation.calculate(
								previousUniverse, innerSigRedEquClass, 
								newInstance, 
								previuosSig, previousSigWithDenominator, 
								innerRed.size()
							).getResult();
				if (calculation.value1IsBetter(redSig, innerSig, sigDeviation)) {
					// Not redundant
					reduct.add(attr);
				}
			}
		}
		
		public static <Sig extends Number> void inspect(
				Collection<Instance> previousUniverse, Collection<Instance> newInstance,
				MixedEquivalenceClassSequentialList decEquClassesCMBResult,
				Collection<Integer> reduct, Sig redSig, boolean previousSigWithDenominator,
				FeatureImportance4LiangIncremental<Sig> calculation, 
				PositiveApproximationAcceleratorCalculation<Sig> staticCalculation,
				Sig sigDeviation
		){
			//	Loop over attributes in reduct
			int[] reductArray = reduct.stream().mapToInt(v->v).toArray();
			
			IntegerIterator innerRed;
			MixedEquivalenceClassSequentialList equClassesCMBResult;
			Map<IntArrayKey, Collection<Instance>> redEquClassesOfPrevious, redEquClassesOfNew;
			for (int attr: reductArray) {
				reduct.remove(attr);
				innerRed = new IntegerCollectionIterator(reduct);
				// Compute Sig<sub>U∪{x}</sub><pub>inner</pub>(a, B, D)
				//	calculate U/B
				redEquClassesOfPrevious = 
					Basic
						.equivalenceClass(previousUniverse, innerRed);
				//	calculate U<sub>X</sub>/B
				redEquClassesOfNew = 
					Basic
						.equivalenceClass(newInstance, innerRed);
				//	calculate U∪U<sub>X</sub>/B
				equClassesCMBResult = 
					Incremental.combineEquivalenceClassesOfPreviousNNew(
							redEquClassesOfPrevious, redEquClassesOfNew
						);
				//	Previous sig.
				Sig previousSig = staticCalculation.calculate(
									PositiveApproximationAcceleratorOriginalAlgorithm
										.Basic
										.equivalenceClass(previousUniverse, innerRed),
									innerRed.size()
								).getResult();
				//	New sig.
				Sig newSig = staticCalculation.calculate(
									PositiveApproximationAcceleratorOriginalAlgorithm
										.Basic
										.equivalenceClass(newInstance, innerRed),
									innerRed.size()
								).getResult();
				//	Inner sig.
				Sig innerSig = calculation.calculate(
								equClassesCMBResult, decEquClassesCMBResult, 
								previousUniverse.size(), newInstance.size(), 
								previousSig, newSig, previousSigWithDenominator, 
								innerRed.size()
							).getResult();
				// if Sig<sub>U∪{x}</sub><pub>inner</pub>(a, B, D) ==0, B<-B-{a}.
				if (calculation.value1IsBetter(redSig, innerSig, sigDeviation)) {
					reduct.add(attr);
				}
			}
		}
	}

}
