package featureSelection.repository.algorithm.alg.roughEquivalenceClassBased;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.RoughEquivalenceClass;
import featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased.PositiveRegionCalculation4RSCREC;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;


/**
 * Utilities for {@link RoughEquivalenceClassBasedAlgorithm}.
 * 
 * @author Benjamin_L
 */
@Slf4j
@UtilityClass
public class RoughEquivalenceClassBasedUtils {
	
	/**
	 * Collect {@link EquivalenceClass}es in the given {@link NestedEquivalenceClass}es.
	 * 
	 * @param nestedEquClasses
	 * 		{@link NestedEquivalenceClass} {@link Collection}.
	 * @return An {@link EquivalenceClass} {@link Collection}.
	 */
	public static Collection<EquivalenceClass> collectEquivalenceClassesIn(
			Collection<? extends NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses
	){
		Collection<EquivalenceClass> newEquClasses = new LinkedList<>();
		nestedEquClasses.stream()
						.map(nec->nec.getEquClasses().values())
						.forEach(equClasses->{	newEquClasses.addAll(equClasses);	});
		return newEquClasses;
	}
	
	
	public static <Equ extends EquivalenceClass, Rough extends RoughEquivalenceClass<Equ>> int
		countUniverseSize(Collection<Rough> roughEquClasses)
	{
		if (roughEquClasses==null)	return 0;
		int count = 0;
		for (RoughEquivalenceClass<Equ> roughEquClass : roughEquClasses){
			count += roughEquClass.getInstanceSize();
		}
		return count;
	}
	
	public static <Equ extends EquivalenceClass, Rough extends RoughEquivalenceClass<Equ>> int
		countEquivalenceClassSize(Collection<Rough> roughClasses)
	{
		if (roughClasses==null)	return 0;
		int count = 0;
		for (RoughEquivalenceClass<Equ> roughEquClass : roughClasses){
			count += roughEquClass.getItemSize();
		}
		return count;
	}

	
	public static int[] extractAttributeValuesFromEquClass(
			EquivalenceClass eClass, int...attributes
	) {
		int[] attrValue = new int[attributes.length];
		for (int i=0; i<attrValue.length; i++)	{
			attrValue[i] = eClass.getAttributeValueAt(attributes[i]);
		}
		return attrValue;
	}

	
	public static class Validation {

		public static boolean redWithBoundaries(
				Collection<Instance> instances, int[] attributes, IntegerIterator red
		) {
			Collection<EquivalenceClass> equClasses =
					NestedEquivalenceClassBasedAlgorithm
						.Basic
						.equivalenceClass(instances, new IntegerArrayIterator(attributes))
						.values();
			
			return redWithBoundaries4EquivalenceClasses(equClasses, red);
		}
		
		public static boolean redWithBoundaries4EquivalenceClasses(
				Collection<EquivalenceClass> equClasses, IntegerIterator red
		) {
			return NestedEquivalenceClassBasedAlgorithm
						.Basic
						.nestedEquivalenceClass(equClasses, red)
						.getNestedEquClasses()
						.values()
						.parallelStream()
						.filter(each-> ClassSetType.BOUNDARY.equals(each.getType()))
						.count()!=0;
		}
		
		public static boolean redWithBoundaries4NestedEquivalenceClasses(
				Collection<NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses,
				IntegerIterator red
		) {
			for (NestedEquivalenceClass<EquivalenceClass> nestedEquClass: nestedEquClasses) {
				if (NestedEquivalenceClassBasedAlgorithm
						.Basic
						.nestedEquivalenceClass(nestedEquClass.getEquClasses().values(), red)
						.getNestedEquClasses()
						.values()
						.parallelStream()
						.filter(each->ClassSetType.BOUNDARY.equals(each.getType()))
						.count()!=0	
				)	return true;
			}
			return false;
		}
		
		
		/**
		 * Check if the reduct is redundant validating by S-REC inspection.
		 * 
		 * @see RoughEquivalenceClassBasedExtensionAlgorithm.SimpleCounting.RealTimeCounting
		 *
		 * @param insSize
		 * 		The size of all {@link Instance}.
		 * @param equClasses
		 * 		{@link EquivalenceClass} {@link Collection}.
		 * @param redB4Inspect
		 * 		Reduct before inspection.
		 * @param reduct
		 * 		Reduct after inspection.
		 * @return true if redundant.
		 * @throws Exception if reduct is redundant
		 */
		public static void checkIfReductHasRedundancyValidBySREC(
				int insSize, Collection<EquivalenceClass> equClasses,
				int[] redB4Inspect, int[] reduct
		) throws Exception {
			Collection<Integer> redAfterInspect = 
				RoughEquivalenceClassBasedExtensionAlgorithm
					.SimpleCounting
					.RealTimeCounting
					.inspection(insSize, new PositiveRegionCalculation4RSCREC(), 0, equClasses, reduct);
			if (redAfterInspect.size()!=reduct.length) {
				log.error("Attribute redundant before inspection: {}", Arrays.toString(redB4Inspect));
				log.error("Attribute redundant after inspection: {}", Arrays.toString(reduct));
				log.error("Attribute inspection expected: {}", redAfterInspect);
				throw new Exception("Attribute redundant after inspection!");
			}
		}
		
		/**
		 * Check if the reduct is redundant validating by S-REC inspection.
		 * 
		 * @see RoughEquivalenceClassBasedExtensionAlgorithm.SimpleCounting.RealTimeCounting
		 * 
		 * @param insSize
		 * 		The size of all {@link Instance}.
		 * @param equClasses
		 * 		{@link EquivalenceClass} {@link Collection}.
		 * @param redB4Inspect
		 * 		Reduct before inspection.
		 * @param reduct
		 * 		Reduct after inspection.
		 * @return true if redundant.
		 * @throws Exception if reduct is redundant
		 */
		public static void checkIfReductHasRedundancyValidBySREC(
				int insSize, Collection<EquivalenceClass> equClasses,
				int[] redB4Inspect, Collection<Integer> reduct
		) throws Exception {
			Collection<Integer> copy = new ArrayList<>(reduct);
			RoughEquivalenceClassBasedExtensionAlgorithm
				.SimpleCounting
				.RealTimeCounting
				.inspection(insSize, new PositiveRegionCalculation4RSCREC(), 0, equClasses, copy);
			if (copy.size()!=reduct.size()) {
				log.error("Attribute redundant before inspection: {}", Arrays.toString(redB4Inspect));
				log.error("Attribute redundant after inspection: {}", reduct);
				log.error("Attribute inspection expected: {}", copy);
				throw new Exception("Attribute redundant after inspection!");
			}
		}
		
		@SuppressWarnings("unused")
		private void checkUniverseTotalSize(
				Collection<NestedEquivalenceClass<EquivalenceClass>> reductNestedEquClasses,
				int totalUniverseSize
		) throws Exception {
			int universeCount = 0;
			for (NestedEquivalenceClass<EquivalenceClass> nec: reductNestedEquClasses)
				universeCount+= nec.getInstanceSize();
			if (universeCount!=totalUniverseSize) {
				throw new Exception("Universe size abnormal, expect "+totalUniverseSize+", get "+universeCount);
			}
		}
	
	}
}