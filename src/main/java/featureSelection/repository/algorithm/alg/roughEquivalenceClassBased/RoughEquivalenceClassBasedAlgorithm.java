package featureSelection.repository.algorithm.alg.roughEquivalenceClassBased;

import common.utils.ArrayCollectionUtils;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.rec.IncrementalPackage;
import featureSelection.repository.entity.alg.rec.SignificantAttributeClassPack;
import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.RoughEquivalenceClass;
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.Shrink4RECBoundaryClassSetStays;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Algorithm repository of Rough Equivalence Class based algorithm(REC), which bases on the paper
 * <a href="http://www.sysengi.com/EN/Y2017/V37/I2/504">"Rough equivalence class bilateral-
 * decreasing based incremental core and attribute reduction computation with multiple Hashing"
 * </a> by Zhao Jie, Zhang Kaihang, Dong Zhenning, Xu Kefu
 * and
 * Pro.Zhao's other relevant researches including those on Attribute Reduction/Feature Selection.
 * <p>
 * Note that this algorithm is one of the proposed algorithms by Pro.Zhao & her students(including
 * this coding author Benjamin_L, not this one though)
 * <p>
 * However, Pro.Zhao's later proposed algorithms were all evolved and designed based on the thoughts
 * of this algorithm: <strong>Rough Equivalence Class(REC)</strong>.
 *
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class RoughEquivalenceClassBasedAlgorithm {
	
	/**
	 * Basic algorithm implementations for REC.
	 * 
	 * @author Benjamin_L
	 */
	public static class Basic {

		/**
		 * Obtain {@link EquivalenceClass}es of {@link Instance} {@link Collection} by given
		 * <code>attributes</code>.
		 * 
		 * @param instances
		 * 		A {@link Collection} of {@link Instance}.
		 * @param attributes
		 * 		Attributes of {@link Instance}. (Starts from 1).
		 * @return A {@link Collection} of {@link EquivalenceClass}.
		 */
		public static Collection<EquivalenceClass> equivalenceClass(
				Collection<Instance> instances, IntegerIterator attributes
		) {
			// Initiate a hash map to contain equivalence classes induced by all attributes.
			//  The worse case is all instances are different from each other and are considered
			//      as equivalence classes respectively.
			Map<IntArrayKey, EquivalenceClass> equClasses = new HashMap<>(instances.size());
			// Loop over all instances and partition
			int[] code;
			IntArrayKey key = null;
			EquivalenceClass equClass;
			for (Instance ins: instances) {
				// Obtain the attribute values of all (conditional) attributes as key.
				code = Instance.attributeValuesOf(ins, attributes);
				key = new IntArrayKey(code);
				equClass = equClasses.get(key);
				// If already exists an equivalence class with the same key.
				if (equClass!=null) {
					equClass.addClassItem(ins);
					//	if the decision values are not equal, update consistency
					if (ins.getAttributeValue(0)!=equClass.getDecisionValue()) {
						equClass.setUnsortable();
					}
				}else {
					// If no such key, create one, and initiate.
					equClasses.put(key, equClass=new EquivalenceClass(ins));
				}
			}
			return equClasses.values();
		}
		
		/**
		 * Obtain {@link EquivalenceClass}es of {@link Instance}s induced by <code>decision
		 * attribute</code>.
		 * 
		 * @param instances
		 * 		A {@link Collection} of {@link Instance}.
		 * @return A {@link Collection} of {@link EquivalenceClass}.
		 */
		public static Collection<EquivalenceClass> decisionEquivalenceClass(
			Collection<Instance> instances
		) {
			Map<Integer, EquivalenceClass> equClasses = new HashMap<>();
			// Loop over all instances.
			int code;
			EquivalenceClass equItem;
			for (Instance instance : instances) {
				code = instance.getAttributeValue(0);
				equItem = equClasses.get(code);
				if (equItem!=null) {
					equItem.addClassItem(instance);
				}else {
					// If no such key, create one.
					equItem = new EquivalenceClass(instance);
				}
				equClasses.put(code, equItem);
			}
			return equClasses.values();
		}
		
		/* ---------------------------------------------------------------------------------------- */

		/**
		 * [Single attribute]: Obtain the {@link EquivalenceClass}es induced by the given
		 * <code>attribute</code>.
		 * <p>
		 * Using the default capacity value in HashMap(16) as hash capacity.
		 * 
		 * @see #roughEquivalenceClass(Collection, int, int)
		 *
		 * @param equClasses
		 * 		A {@link Collection} of {@link EquivalenceClass}.
		 * @param attribute
		 * 		An attribute of the {@link Instance}.(Starts from 1)
		 * @return {@link RoughEquivalenceClass}es.
		 */
		public static Collection<RoughEquivalenceClass<EquivalenceClass>>
			roughEquivalenceClass(
					Collection<? extends EquivalenceClass> equClasses, int attribute
		) {
			// 16 is the default hash capacity in HashMap.
			return roughEquivalenceClass(equClasses, attribute, 16);
		}
		
		/**
		 * [Single attribute]: Obtain the {@link EquivalenceClass}es induced by the given
		 * <code>attribute</code>.
		 * 
		 * @param equClasses
		 * 		A {@link Collection} of {@link EquivalenceClass}es.
		 * @param attribute
		 * 		An attribute of {@link Instance}.(Starts from 1)
		 * @param hashKeyCapacity
		 *      The capacity for creating a {@link HashMap} to contain {@link RoughEquivalenceClass}es
		 *      and their keys.
		 * @return {@link RoughEquivalenceClass}es.
		 */
		public static Collection<RoughEquivalenceClass<EquivalenceClass>>
			roughEquivalenceClass(
					Collection<? extends EquivalenceClass> equClasses, int attribute,
					int hashKeyCapacity
		) {
			int key;
			RoughEquivalenceClass<EquivalenceClass> roughEquClass;
			Map<Integer, RoughEquivalenceClass<EquivalenceClass>> roughEquClasses =
					new HashMap<>(hashKeyCapacity);
			for (EquivalenceClass equClass: equClasses) {
				key = equClass.getAttributeValueAt(attribute-1);
				roughEquClass = roughEquClasses.get(key);
				// If a rough equivalence class with the same attribute value exists
				if (roughEquClass!=null) {
					// Add the equivalent class into it.
					roughEquClass.addClassItem(equClass);
				}else {
					// If no such key, create one.
					roughEquClass = new RoughEquivalenceClass<>();
					roughEquClass.addClassItem(equClass);
					roughEquClasses.put(key, roughEquClass);
				}
			}
			return roughEquClasses.values();
		}
		
		/**
		 * [Multi-attributes]: Obtain the {@link EquivalenceClass}es based on the given
		 * <code>attributes</code>.
		 * <p>
		 * Using the default capacity value in HashMap(16) as hash capacity.
		 *
		 * @see #roughEquivalenceClass(Collection, IntegerIterator, int)
		 * 
		 * @param equClasses
		 * 		A {@link Collection} of {@link EquivalenceClass}es.
		 * @param attributes
		 * 		Attributes of {@link Instance}. (Starts from 1)
		 * @return a {@link Collection} of {@link RoughEquivalenceClass}es.
		 */
		public static Collection<RoughEquivalenceClass<EquivalenceClass>>
			roughEquivalenceClass(
					Collection<? extends EquivalenceClass> equClasses, IntegerIterator attributes
		) {
			// 16 is the default hash capacity in HashMap.
			return roughEquivalenceClass(equClasses, attributes, 16);
		}
		
		/**
		 * [Multi-attributes]: Obtain the {@link EquivalenceClass} based on the given
		 * 	<code>attributes</code>.
		 *
		 * @see #roughEquivalenceClass(Collection, IntegerIterator)
		 * 
		 * @param equClasses
		 * 		A {@link Collection} of {@link EquivalenceClass}.
		 * @param attributes
		 * 		Attributes of {@link Instance}. (Starts from 1)
		 * @param hashKeyCapacity
		 *      The capacity for creating a {@link HashMap} to contain {@link RoughEquivalenceClass}es
		 *      and their keys.
		 * @return a {@link Collection} of {@link RoughEquivalenceClass}es.
		 */
		public static Collection<RoughEquivalenceClass<EquivalenceClass>>
			roughEquivalenceClass(
					Collection<? extends EquivalenceClass> equClasses, IntegerIterator attributes,
					int hashKeyCapacity
		) {
			int[] code;
			IntArrayKey key = null;
			RoughEquivalenceClass<EquivalenceClass> roughEquClass;
			Map<IntArrayKey, RoughEquivalenceClass<EquivalenceClass>> roughEquClasses =
					new HashMap<>(hashKeyCapacity);
			for (EquivalenceClass equClass : equClasses) {
				code = new int[attributes.size()];
				attributes.reset();
				for (int i=0; i<code.length; i++)
					code[attributes.currentIndex()] = equClass.getAttributeValueAt(attributes.next()-1);
				key = new IntArrayKey(code);
				roughEquClass = roughEquClasses.get(key);
				// If no such key, create one.
				if (roughEquClass==null)
					roughEquClasses.put(key, roughEquClass=new RoughEquivalenceClass<>());
				roughEquClass.addClassItem(equClass);
			}
			return roughEquClasses.values();
		}
		
		/* --------------------------------------------------------------------------------------------- */

		/**
		 * [Alternative/filtered]: Obtain the {@link EquivalenceClass} based on the given
		 * 	<code>attributes</code>. Using the one given <code>attributes</code> to further partition
		 * 	the given {@link EquivalenceClass}es, which is considered as incremental partitioning.
		 * <p>
		 * Using the default capacity value in HashMap(16) as hash capacity.
		 *
		 * @see #incrementalRoughEquivalenceClassFilteredClasses(Shrink4RECBoundaryClassSetStays,
		 *      Collection, int, int)
		 * 
		 * @param shrinkInstance
		 * 		{@link Shrink4RECBoundaryClassSetStays} instance.
		 * @param roughEquClasses
		 * 		A {@link Collection} of {@link RoughEquivalenceClass}es to be further partitioned.
		 * @param extAttr
		 * 		An extra attribute of {@link Instance}. (Starts from 1)
		 * @return A {@link Set} of {@link RoughEquivalenceClass} that has been filtered,
		 * 			remaining {@link ClassSetType#BOUNDARY} ones only.
		 */
		public static Set<RoughEquivalenceClass<EquivalenceClass>>
			incrementalRoughEquivalenceClassFilteredClasses(
				Shrink4RECBoundaryClassSetStays shrinkInstance,
				Collection<RoughEquivalenceClass<EquivalenceClass>> roughEquClasses, int extAttr
		) {
			// 16 is the default hash capacity in HashMap.
			return incrementalRoughEquivalenceClassFilteredClasses(
					shrinkInstance, roughEquClasses, extAttr, 16
			);
		}
		
		/**
		 * [Alternative/filtered]: Obtain the {@link EquivalenceClass} based on the given
		 * <code>attributes</code>. Using the given attribute to further partition the given
		 * {@link EquivalenceClass}es, which is considered as incremental partitioning.
		 * 
		 * @param shrinkInstance
		 * 		{@link Shrink4RECBoundaryClassSetStays} instance.
		 * @param roughEquClasses
		 * 		A {@link Collection} of {@link RoughEquivalenceClass}.
		 * @param extAttr
		 * 		The extra attribute indexes of the {@link Instance}. (Starts from 1)
		 * @return A {@link Set} of {@link RoughEquivalenceClass} that has been filtered,
		 * 			remaining {@link ClassSetType#BOUNDARY} ones only.
		 */
		public static Set<RoughEquivalenceClass<EquivalenceClass>>
			incrementalRoughEquivalenceClassFilteredClasses(
				Shrink4RECBoundaryClassSetStays shrinkInstance,
				Collection<RoughEquivalenceClass<EquivalenceClass>> roughEquClasses, int extAttr,
				int hashKeyCapacity
		) {
			Set<RoughEquivalenceClass<EquivalenceClass>> increment = new HashSet<>(hashKeyCapacity);
			for (RoughEquivalenceClass<EquivalenceClass> roughEquClass : roughEquClasses) {
				for (RoughEquivalenceClass<EquivalenceClass> partitioned :
						roughEquivalenceClass(roughEquClass.getItems(), extAttr)
				) {
					if (!shrinkInstance.removAble(partitioned))	increment.add(partitioned);
				}
			}
			return increment;
		}
		
		
		/**
		 * [Alternative/filtered]: Obtain the {@link EquivalenceClass}es further induced by the
		 * given <code>extAttr</code>, which is considered as incremental partitioning.
		 * <p>
		 * Using the default capacity value in HashMap(16) as hash capacity.
		 * 
		 * @param roughEquClasses
		 * 		A {@link Collection} of {@link RoughEquivalenceClass}es.
		 * @param extAttr
		 * 		Extra attributes. (Starts from 1)
		 * @return {@link IncrementalPackage} that contains the incremental partitioning results.
		 */
		public static IncrementalPackage<RoughEquivalenceClass<EquivalenceClass>>
			incrementalRoughEquivalenceClass(
				Collection<RoughEquivalenceClass<EquivalenceClass>> roughEquClasses, int extAttr
		) {
			// 16 is the default hash capacity in HashMap.
			return incrementalRoughEquivalenceClass(roughEquClasses, extAttr, 16);
		}
		
		/**
		 * [Alternative/filtered]: Obtain the {@link EquivalenceClass}es further induced by the
		 * given <code>extAttr</code>, which is considered as incremental partitioning.
		 * 
		 * @param roughEquClasses
		 * 		A {@link Collection} of {@link EquivalenceClass}.
		 * @param extAttr
		 * 		The extra attribute indexes of the {@link Instance}. (Starts from 1)
		 * @return A {@link Set} of {@link EquivalenceClass}.
		 */
		public static IncrementalPackage<RoughEquivalenceClass<EquivalenceClass>>
			incrementalRoughEquivalenceClass(
				Collection<RoughEquivalenceClass<EquivalenceClass>> roughEquClasses, int extAttr,
				int hashKeyCapacity
		) {
			int posCount = 0, negCount = 0, typeCount = 0;
			Set<RoughEquivalenceClass<EquivalenceClass>> increment = new HashSet<>(hashKeyCapacity);
			for (RoughEquivalenceClass<EquivalenceClass> roughEquClass : roughEquClasses) {
				for (RoughEquivalenceClass<EquivalenceClass> each :
						roughEquivalenceClass(roughEquClass.getItems(), extAttr)
				) {
					typeCount++;
					if (ClassSetType.BOUNDARY.equals(each.getType())) {
						increment.add(each);
					}else if(ClassSetType.POSITIVE.equals(each.getType())) {
						posCount += each.getInstanceSize();
					}else {
						negCount += each.getInstanceSize();
					}
				}
			}
			return new IncrementalPackage<>(increment, typeCount==roughEquClasses.size(), posCount, negCount);
		}
	}
	
	/* ------------------------------------------------------------------------------------------------- */
	
	/**
	 * Obtain the core.
	 * 
	 * @param shrinkInstance
	 * 		{@link Shrink4RECBoundaryClassSetStays} instance.
	 * @param equClasses
	 * 		A {@link Collection} of {@link EquivalenceClass}.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @return an {@link Integer} {@link Collection} as core.(Starts from 1)
	 */
	public static Collection<Integer> core(
			Shrink4RECBoundaryClassSetStays shrinkInstance,
			Collection<EquivalenceClass> equClasses, int...attributes
	) {
		boolean redundant;
		Collection<Integer> core = new HashSet<>();
		// Loop over all attributes and check.
		int limit = attributes.length;
		Collection<RoughEquivalenceClass<EquivalenceClass>> roughEquClasses = null;
		for (int i=0; i<limit; i++) {
			roughEquClasses = null;
			// Loop over extracted attributes except the current one
			redundant = false;
			for (int j=0; j<attributes.length; j++) {
				if (i==j)	continue;	//Skip this attribute
				// Obtain the rough equivalence classes induced by inner extracted attribute: a[j]
				if (roughEquClasses != null) {
					roughEquClasses = Basic.incrementalRoughEquivalenceClassFilteredClasses(shrinkInstance, roughEquClasses, attributes[j]);
				}else {
					roughEquClasses = Basic.roughEquivalenceClass(equClasses, attributes[j]);
					// Remain 0-REC only.
					shrinkInstance.shrink(roughEquClasses);
				}
				// If 0-RECs are empty, equivalence classes are partitioned just fine without this
				//  attribute(& the rest of the attributes), i.e. this attribute(& the rest) is
				//  redundant to core. So, add this attribute(& the rest) into redundant set and
				//  skip checking it(/them) in the future looping.
				if (roughEquClasses.size()==0) {
					redundant = true;
					// for j+1 ~ attribute.length, attributes are skipped in the future looping.
					limit = j+1;
					break;
				}
			}
			// Add the current attribute into core if it is not redundant.
			if (!redundant){
				core.add(attributes[i]);
			}
		}
		return core;
	}
	
	/* ------------------------------------------------------------------------------------------------- */

	/**
	 * [REC strategy] Obtain the current most significant attribute using the original strategy of REC.
	 * <p>
	 * Comparing to {@link #mostSignificantAttributeNormally(Collection, Collection, int[])}, this
	 * method uses {@link Basic#incrementalRoughEquivalenceClass(Collection, int)} in calculation
	 * for when adding an attribute into reduct.
	 * 
	 * @see #mostSignificantAttributeNormally(Collection, Collection, int[])
	 * 
	 * @param roughEquClasses
	 * 		A {@link Collection} of {@link RoughEquivalenceClass}es for further partitioning.
	 * @param red
	 * 		Reduct attributes.(Starts from 1)
	 * @param attributes
	 * 		Attributes of {@link Instance}.(Starts from 1)
	 * @param defineRedundant
	 * 		if <code>true</code>, check if attributes are redundant by checking if the number of
	 * 		types remains the same after further partitioning by an attribute.
	 * @return {@link SignificantAttributeClassPack} containing the attribute with the greatest
	 *      significance.
	 */
	public static SignificantAttributeClassPack<EquivalenceClass, RoughEquivalenceClass<EquivalenceClass>>
		mostSignificantAttribute(
			Collection<RoughEquivalenceClass<EquivalenceClass>> roughEquClasses,
			Collection<Integer> red, int[] attributes, boolean defineRedundant
	){
		if (roughEquClasses.size()==0)	return null;
		int maxPos = 0, sigAttr = -1;
		// Loop over attributes outside of current reduct.
		IncrementalPackage<RoughEquivalenceClass<EquivalenceClass>> increPack;
		Collection<RoughEquivalenceClass<EquivalenceClass>> sigIncrement = null;
		for (int i=0; i<attributes.length; i++) {
			if (attributes[i]==-1)								continue;
			if (red.size()!=0 && red.contains(attributes[i]))	continue;
			// Obtain incremental partitioning results.
			increPack = Basic.incrementalRoughEquivalenceClass(roughEquClasses, attributes[i]);
			if (defineRedundant && increPack.isRedundant()) {
				attributes[i] = -1;
				continue;
			}
			// Check incremental partitioning results and update with the one with max |1-REC|
			if (increPack.getPositive()>maxPos || sigAttr==-1) {
				maxPos = increPack.getPositive();
				sigAttr = attributes[i];
				sigIncrement = increPack.getFilteredClass();
			}
		}
		return new SignificantAttributeClassPack<>(sigAttr, attributes, sigIncrement, maxPos);
	}

	/**
	 * [Normally] Obtain the current most significant attribute.
	 * <p>
	 * Comparing to {@link #mostSignificantAttribute(Collection, Collection, int[], boolean)},
	 * this method uses {@link Basic#roughEquivalenceClass(Collection, int)} in calculation for
	 * when adding an attribute into reduct(i.e. reduct ∪ {a})
	 * 
	 * @see #mostSignificantAttribute(Collection, Collection, int[], boolean)
	 * 
	 * @param roughEquClasses
	 * 		A {@link Collection} of {@link RoughEquivalenceClass}es for further partitioning.
	 * @param red
	 * 		Reduct attributes.(Starts from 1)
	 * @param attributes
	 * 		Attributes of {@link Instance}.(Starts from 1)
	 * @return {@link SignificantAttributeClassPack} containing the attribute with the greatest
	 *      significance.
	 */
	public static SignificantAttributeClassPack<EquivalenceClass, RoughEquivalenceClass<EquivalenceClass>>
		mostSignificantAttributeNormally(
			Collection<RoughEquivalenceClass<EquivalenceClass>> roughEquClasses,
			Collection<Integer> red, int[] attributes
	){
		if (roughEquClasses.size()==0)	return null;
		Set<EquivalenceClass> equClass = new HashSet<>();
		for (RoughEquivalenceClass<EquivalenceClass> r : roughEquClasses)	equClass.addAll(r.getItems());
		// Initiate
		int maxPos = 0, sigAttr = -1, typeOne;
		int[] attrArray = new int[red.size()+1];	int i=0; for (int r : red)	attrArray[i++] = r; 
		// Go through examined attributes
		Collection<RoughEquivalenceClass<EquivalenceClass>> sigIncrement = null, increRough;
		for (int a=0; a<attributes.length; a++) {
			if (attributes[a]==-1)								continue;
			if (red.size()!=0 && red.contains(attributes[a]))	continue;
			// Get incremental dividings.
			attrArray[red.size()] = attributes[a];
			increRough = Basic.roughEquivalenceClass(equClass, new IntegerArrayIterator(attrArray));
			if (roughEquClasses.size()==increRough.size()) {
				attributes[a] = -1;
				continue;
			}
			// Go through incremental dividings and get the max |type 1|
			typeOne = 0;
			for (RoughEquivalenceClass<EquivalenceClass> each : increRough)
				if (ClassSetType.POSITIVE.equals(each.getType())) {	typeOne+= each.getInstanceSize();	}
			if (typeOne>maxPos || sigAttr==-1) {
				// If x(|type 1|) > maxPos, a* = a or haven't found a sig
				maxPos = typeOne;
				sigAttr = attributes[a];
				sigIncrement = increRough;
			}
		}
		// Return signifivant package
		return new SignificantAttributeClassPack<>(sigAttr, attributes, sigIncrement, maxPos);
	}
	
	/* ------------------------------------------------------------------------------------------------- */
	
	/**
	 * Inspections for reduction.
	 * <p>
	 * Attributes in <code>attribute</code> will be examined one by one in reverse order. The
	 * examination is to check whether the significance changes after removing an attribute from
	 * the <code>attribute</code>. If it does, the attribute is not redundant, else it is.
	 * 
	 * @see #inspection(Shrink4RECBoundaryClassSetStays, Collection, int[])
	 * 
	 * @param <Equ>
	 * 		{@link EquivalenceClass} type.
	 * @param shrinkInstance
	 * 		{@link Shrink4RECBoundaryClassSetStays} instance.
	 * @param equClasses
	 * 		A {@link Collection} of {@link EquivalenceClass}es.
	 * @param attributes
	 * 		Attributes to be inspected.(Starts from 1)
	 * @return A {@link Integer} {@link List} as inspected attributes.
	 */
	@SuppressWarnings("unchecked")
	public static <Equ extends EquivalenceClass> List<Integer> inspection(
			Shrink4RECBoundaryClassSetStays shrinkInstance,
			Collection<Equ> equClasses, List<Integer> attributes
	) {
		// Copy reduct for looping.
		int[] attrArray = ArrayCollectionUtils.getIntArrayByCollection(attributes);
		Collection<RoughEquivalenceClass<EquivalenceClass>> roughClass;
		for (int i=attributes.size()-1; i>=0; i--) {
			if (attrArray[i]==-1) continue;
			roughClass = null;
			// Loop over extracted attributes except the current one.
			for (int j=attributes.size()-1; j>=0; j--) {
				if (i==j || attrArray[j]==-1)	continue;	//Skip this attribute
				// Obtain rough equivalence classes induced by the inner extracted attribute
				// Remain 0-REC only.
				if (roughClass==null) {
					roughClass = Basic.roughEquivalenceClass((Collection<EquivalenceClass>) equClasses, new IntegerArrayIterator(attrArray[j]));
					shrinkInstance.shrink(roughClass);
				}else {
					roughClass = Basic.incrementalRoughEquivalenceClassFilteredClasses(shrinkInstance, roughClass, attrArray[j]);
				}
				// If 0-REC is empty, add this attribute and the rest of them into redundant set.
				if (roughClass.size()==0) {
					attrArray[i] = -1;
					for (int z = j-1; z>=0; z--) {
						// Mark redundant.
						if (attrArray[z]!=-1){
							attrArray[z] = -1;
						}
					}
					break;
				}
			}
		}
		List<Integer> red = new LinkedList<>();
		for (int each : attrArray){
			// If not redundant.
			if (each!=-1){
				red.add(each);
			}
		}
		return red;
	}
	
	/**
	 * Inspections for reduction.
	 * <p>
	 * Attributes in <code>attribute</code> will be examined one by one in reverse order. The
	 * examination is to check whether the significance changes after removing an attribute from
	 * the <code>attribute</code>. If it does, the attribute is not redundant, else it is.
	 * 
	 * @see #inspection(Shrink4RECBoundaryClassSetStays, Collection, List)
	 * 
	 * @param <Equ>
	 * 		{@link EquivalenceClass} type.
	 * @param shrinkInstance
	 * 		{@link Shrink4RECBoundaryClassSetStays} instance.
	 * @param equClasses
	 * 		A {@link Collection} of {@link EquivalenceClass}.
	 * @param attributes
	 * 		Attributes to be inspected.(Starts from 1)
	 * @return A {@link Integer} {@link List} as inspected attributes.
	 */
	@SuppressWarnings("unchecked")
	public static <Equ extends EquivalenceClass> List<Integer> inspection(
			Shrink4RECBoundaryClassSetStays shrinkInstance,
			Collection<Equ> equClasses, int[] attributes
	) {
		// Loop over attributes in reverse order.
		boolean[] redundant = new boolean[attributes.length];
		Collection<RoughEquivalenceClass<EquivalenceClass>> roughClass;
		for (int i=attributes.length-1; i>=0; i--) {
			if (redundant[i]) continue;
			roughClass = null;
			// Loop over extracted attributes except the current one.
			for (int j=attributes.length-1; j>=0; j--) {
				if ( i==j || redundant[j] )	continue;	//Skip this attribute
				// Obtain the rough equivalence classes induced by the inner extracted attribute
				// Remain 0-REC only.
				if (roughClass==null) {
					roughClass = Basic.roughEquivalenceClass((Collection<EquivalenceClass>) equClasses, new IntegerArrayIterator(attributes[j]));
					shrinkInstance.shrink(roughClass);
				}else {
					roughClass = Basic.incrementalRoughEquivalenceClassFilteredClasses(shrinkInstance, roughClass, attributes[j]);
				}
				// If 0-REC is empty, add this attributes and the rest into redundant set.
				if (roughClass.isEmpty()) {
					redundant[i] = true;
					for (int z = j-1; z>=0; z--){
						// Mark redundant.
						if (!redundant[z]){
							redundant[z] = true;
						}
					}
					break;
				}
			}
		}
		List<Integer> red = new LinkedList<>();	
		for (int i=0; i<attributes.length; i++){
			// If not redundant.
			if (!redundant[i] && attributes[i]!=-1){
				red.add(attributes[i]);
			}
		}
		return red;
	}
	
	/**
	 * Inspections for reduction.
	 * 
	 * @param equClasses
	 * 		A {@link Collection} of {@link EquivalenceClass}.
	 * @param attributes
	 * 		Attributes to be inspected.(Starts from 1)
	 * @return A {@link Integer} {@link List} as inspected attributes.
	 */
	public static List<Integer> inspectionOriginal(
		Collection<EquivalenceClass> equClasses, List<Integer> attributes
	) {
		//Initiate the attributes with a reversed order.
		int examAttr, pos, globalPos = 0;
		Collection<RoughEquivalenceClass<EquivalenceClass>> roughEquClasses;
		roughEquClasses = Basic.roughEquivalenceClass(equClasses, new IntegerCollectionIterator(attributes));
		for (RoughEquivalenceClass<EquivalenceClass> roughEquClass : roughEquClasses) {
			if (roughEquClass.getType().isPositive()) {
				globalPos += roughEquClass.getInstanceSize();
			}
		}
		for (int i=attributes.size()-1; i>=0; i--) {
			examAttr = attributes.remove(i);
			roughEquClasses = Basic.roughEquivalenceClass(equClasses, new IntegerCollectionIterator(attributes));
			pos = 0;
			for (RoughEquivalenceClass<EquivalenceClass> roughEquClass : roughEquClasses) {
				if (roughEquClass.getType().isPositive()) {
					pos += roughEquClass.getInstanceSize();
				}
			}
			if (pos!=globalPos){
				// Not redundant.
				attributes.add(i, examAttr);
			}
		}
		return attributes;
	}

	/**
	 * Inspections for reduction
	 * 
	 * @param equClasses
	 * 		A {@link Collection} of {@link EquivalenceClass}.
	 * @param redArrays
	 * 		Attributes to be inspected.(Starts from 1)
	 * @return A {@link Integer} {@link List} as inspected attributes.
	 */
	public static List<Integer> inspectionOriginal(
			Collection<EquivalenceClass> equClasses, int...redArrays
	) {
		//Initiate the attributes in reverse order.
		int examAttr, pos, globalPos = 0;
		Collection<RoughEquivalenceClass<EquivalenceClass>> roughEquClasses;
		roughEquClasses = Basic.roughEquivalenceClass(equClasses, new IntegerArrayIterator(redArrays));
		
		List<Integer> reduct = new LinkedList<>();
		for (int each: redArrays) {
			reduct.add(each);
		}
		
		for (RoughEquivalenceClass<EquivalenceClass> roughEquClass : roughEquClasses) {
			if (roughEquClass.getType().isPositive()) {
				globalPos += roughEquClass.getInstanceSize();
			}
		}
		for (int i=reduct.size()-1; i>=0; i--) {
			examAttr = reduct.remove(i);
			roughEquClasses = Basic.roughEquivalenceClass(equClasses, new IntegerCollectionIterator(reduct));
			pos = 0;
			for (RoughEquivalenceClass<EquivalenceClass> roughEquClass : roughEquClasses){
				if (roughEquClass.getType().isPositive()) {
					pos+=roughEquClass.getInstanceSize();
				}
			}
			if (pos!=globalPos){
				// Not redundant.
				reduct.add(i, examAttr);
			}
		}
		return reduct;
	}

	/* ------------------------------------------------------------------------------------------------- */
}