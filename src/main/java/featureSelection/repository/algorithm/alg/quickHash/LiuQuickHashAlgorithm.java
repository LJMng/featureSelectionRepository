package featureSelection.repository.algorithm.alg.quickHash;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.liuQuickHash.EquivalenceClass;
import featureSelection.repository.entity.alg.liuQuickHash.RoughEquivalenceClass;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Algorithm repository of Liu-Quick Hash Algorithm, based on the paper
 * <a href="http://cjc.ict.ac.cn/quanwenjiansuo/2009-8/ly.pdf">"Quick Attribute Reduction Algorithm
 * with Hash"</a> by Liu Yong, Xiong Rong, Chu Jian.
 * 
 * @author Benjamin_L
 */
public class LiuQuickHashAlgorithm {
	
	/**
	 * Obtain the {@link EquivalenceClass} {@link Map} induced by the given <code>attributes</code>.
	 * 
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1, 0 as decision attribute)
	 * @return A {@link Collection} of {@link EquivalenceClass}es.
	 */
	public static Collection<EquivalenceClass> equivalenceClass(
			Collection<Instance> instances, IntegerIterator attributes
	) {
		// Initiate a hash map to contain equivalent keys and classes.
		Map<IntArrayKey, EquivalenceClass> equClasses = new HashMap<>(instances.size());
		// Loop over instances and partition
		int[] value;
		IntArrayKey key;
		EquivalenceClass equClass;
		for (Instance ins : instances) {
			value = Instance.attributeValuesOf(ins, attributes);
			key = new IntArrayKey(value);
			equClass = equClasses.get(key);
			if (equClass==null){
				equClasses.put(key, equClass=new EquivalenceClass());
			}
			equClass.addInstance(ins);
		}
		return equClasses.values();
	}
	
	/**
	 * Obtain the {@link RoughEquivalenceClass}es induced by given <code>attributes</code> on
	 * equivalence classes.
	 * 
	 * @param equClasses
	 * 		A {@link Collection} of {@link EquivalenceClass}es.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1, 0 as decision attribute)
	 * @return A {@link Collection} of {@link RoughEquivalenceClass}es.
	 */
	public static Collection<RoughEquivalenceClass> roughEquivalenceClass(
			Collection<EquivalenceClass> equClasses, IntegerIterator attributes
	){
		// Loop over all equivalence classes and partition
		int[] value;
		IntArrayKey key;
		RoughEquivalenceClass roughItem;
		Map<IntArrayKey, RoughEquivalenceClass> roughEquClasses = new HashMap<>();
		for (EquivalenceClass equClass : equClasses) {
			attributes.reset();
			value = new int[attributes.size()];
			for (int i=0; i<attributes.size(); i++){
				value[i] = equClass.attributeValueAt(attributes.next()-1);
			}
			key = new IntArrayKey(value);
			roughItem = roughEquClasses.get(key);
			if (roughItem==null){
				roughEquClasses.put(key, roughItem = new RoughEquivalenceClass());
			}
			roughItem.addAnEquivalenceClass(equClass);
		}
		return roughEquClasses.values();
	}
	
	/**
	 * Obtain the {@link RoughEquivalenceClass}es further induced by the given <code>attributes</code>
	 * based on the current {@link RoughEquivalenceClass}es.
	 * 
	 * @param roughEquClasses
	 * 		A {@link Collection} of {@link RoughEquivalenceClass}es.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1, 0 as decision attribute)
	 * @return A {@link Collection} of {@link RoughEquivalenceClass}es.
	 */
	public static Collection<RoughEquivalenceClass> incrementalRoughEquivalenceClass(
			Collection<RoughEquivalenceClass> roughEquClasses, IntegerIterator attributes
	){
		// Loop over rough equivalenceClass in roughEquClasses, and obtain incremental rough
		//  equivalence classes
		Collection<RoughEquivalenceClass> collector = new HashSet<>();
		for (RoughEquivalenceClass roughEquClass: roughEquClasses) {
			// Use the equivalence classes to further partition.
			// Partitioning is only performed in instances of equivalence classes in the
			//  rough equivalence class.
			// Then add all of the generated rough equivalence classes into <code>collector</code>
			collector.addAll(roughEquivalenceClass(roughEquClass.equivalenceClasses(), attributes));
		}
		return collector;
	}
	
	/**
	 * Count the size of positive region of the given {@link RoughEquivalenceClass}.
	 * 
	 * @param roughClasses
	 * 		A {@link Collection} of {@link RoughEquivalenceClass}es.
	 * @return An {@link int} value as the positive region number.
	 */
	public static int positiveRegion(Collection<RoughEquivalenceClass> roughClasses) {
		int pos = 0;
		for (RoughEquivalenceClass roughClass: roughClasses) {
			// Count if consistent.
			if (roughClass.cons())	pos += roughClass.instanceSize();
		}
		return pos;
	}
	
	/**
	 * Count the size of negative region of the given {@link RoughEquivalenceClass}.
	 *
	 * @param roughClasses
	 * 		A {@link Collection} of {@link RoughEquivalenceClass}es.
	 * @return An {@link int} value as the negative region number.
	 */
	public static int negativeRegion(Collection<RoughEquivalenceClass> roughClasses) {
		int neg = 0;
		for (RoughEquivalenceClass roughClass: roughClasses) {
			// Count if in-consistent
			if (!roughClass.cons())	neg += roughClass.instanceSize();
		}
		return neg;
	}
	
	/**
	 * Inspect reduct and remove redundant attributes.
	 * 
	 * @param equClasses
	 * 		A {@link Collection} of {@link EquivalenceClass}es that contains all {@link Instance}s.
	 * @param reduct
	 * 		Reduct attributes. (Starts from 1, 0 as decision attribute)
	 * @param globalPositiveRegion
	 * 		The global positive region.
	 * @return An inspected {@link Collection} of {@link int} values as reduct attributes.
	 */
	public static Collection<Integer> inspection(
			Collection<EquivalenceClass> equClasses, Collection<Integer> reduct,
			Collection<EquivalenceClass> globalPositiveRegion
	) {
		int leftPos, redPos = 0;
		// Count global positive region size.
		for (EquivalenceClass e : globalPositiveRegion){
			// Count if consistent.
			if (e.cons())	redPos += e.instanceSize();
		}

		Integer[] redArray = reduct.toArray(new Integer[reduct.size()]);
		Collection<RoughEquivalenceClass> roughClasses;
		for (int examAttr: redArray) {
			reduct.remove(examAttr);
			leftPos = 0;
			roughClasses = roughEquivalenceClass(equClasses, new IntegerCollectionIterator(reduct));
			for (RoughEquivalenceClass rough : roughClasses) {
				// Count if consistent.
				if (rough.cons())	leftPos += rough.instanceSize();
			}
			// if Pos(Red) != Pos(Red - {a})
			if (redPos != leftPos){
				// Not redundant.
				reduct.add(examAttr);
			}
		}
		return reduct;
	}
}