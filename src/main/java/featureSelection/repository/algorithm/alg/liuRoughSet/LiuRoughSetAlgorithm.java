package featureSelection.repository.algorithm.alg.liuRoughSet;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.liuRoughSet.InstanceComparator;
import featureSelection.repository.support.calculation.alg.FeatureImportance4LiuRoughSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Algorithm repository of Liu-RoughSetAlgorithm, which based on the paper
 * <a href="http://cjc.ict.ac.cn/quanwenjiansuo/2003-05.pdf">"Research on Efficient Algorithms
 * for Rough Set Methods"</a>(<a href="http://cjc.ict.ac.cn/eng/qwjse/view.asp?id=1248">Spare
 * link</a>) by Liu Shaohui, Sheng Qiujian, Wu Bin, Shi Zhongzhi, Hu Fei.
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class LiuRoughSetAlgorithm {

	public static class Basic {
		/**
		 * Obtain the Equivalence Classes induced by the given attributes.
		 * 
		 * @param instances
		 * 		A {@link Collection} of {@link Instance}.
		 * @param attributes
		 * 		Attributes of {@link Instance}. (Starts from 1, 0 as decision attribute)
		 * 		or <code>null</code> if using all attributes.
		 * @return A {@link Collection} of {@link Instance} {@link Collection} as Equivalence Classes.
		 */
		public static Collection<Collection<Instance>> equivalenceClass(
				Collection<Instance> instances, IntegerIterator attributes
		){
			if (attributes.size()==0) {
				// All instances are considered as equivalent.
				Collection<Collection<Instance>> equClass = new HashSet<>(1);
				Collection<Instance> sub = new HashSet<>(instances.size());
				sub.addAll(instances);
				equClass.add(sub);
				return equClass;
			}else {
				// Partition instances using attributes.
				List<Instance> instanceList =
						instances instanceof ArrayList?
								(List<Instance>) instances:
								new ArrayList<>(instances);
				Collections.sort(instanceList, new InstanceComparator(attributes));
				// Initiate
				int cmp = 0;    // index of the representative instance of the current equivalence class.
				Collection<Collection<Instance>> equClasses = new HashSet<>();
				// Loop over instances and sort into Equivalence Classes
				int attr;
				Collection<Instance> equClass = new HashSet<>();
				equClass.add(instanceList.get(cmp));
				for (int i=1; i<instanceList.size(); i++) {
					// Check all attribute values of the instance if equivalent.
					attributes.reset();
					for (int a=0; a<attributes.size(); a++) {
						attr = attributes.next();
						// if attribute value not equals to the one of equClass,
						//  create an other equivalence class for the instance.
						if (Integer.compare(
								instanceList.get(i).getAttributeValue(attr),
								instanceList.get(cmp).getAttributeValue(attr)
							) != 0
						) {
							equClasses.add(equClass);
							equClass = new HashSet<>();
							cmp = i;
							break;
						}else{
							// equivalent, pass
						}
					}
					// Confirm the instance is inside the current equivalence class.
					equClass.add(instanceList.get(i));
				}
				equClasses.add(equClass);//*/
				return equClasses;
			}
		}
	}
	
	/**
	 * Obtain the core attributes Of {@link Instance}s.
	 * 
	 * @param <Sig>
	 * 		{@link Number} implemented type as the value of Significance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4LiuRoughSet} instance.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}s.
	 * @param globalPos
	 * 		The size of global positive region.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @return a {@link Collection} of {@link Integer} as core.(Starts from 1)
	 */
	public static <Sig extends Number> Collection<Integer> core(
			FeatureImportance4LiuRoughSet<Sig> calculation, Sig sigDeviation, 
			Collection<Instance> instances, Sig globalPos, int[] attributes
	) {
		// Initiate a hashset to contain core.
		Collection<Integer> core = new HashSet<>(attributes.length);
		// Loop over attributes, remove one to check if it is a core attribute.
		Sig redSig;
		int[] examine = new int[attributes.length-1];
		for (int i=0; i<examine.length; i++){
			examine[i] = attributes[i+1];
		}
		for (int i=0; i<attributes.length; i++) {
			// Calculate sig(C-{a})
			redSig = calculation.calculate(instances, new IntegerArrayIterator(examine))
								.getResult();
			if (calculation.value1IsBetter(globalPos, redSig, sigDeviation)){
				// attribute is inside core
				core.add(attributes[i]);
			}
			// Next attribute
			if (i<examine.length){
				examine[i] = attributes[i];
			}
		}
		return core;
	}
	
	/**
	 * <code>Sig(a<sup>*</sup>,C,D,U)</code> where a<sup>*</sup> in C.
	 * <p>
	 * Obtain the most significant current attribute.
	 * 
	 * @param <Sig>
	 * 		Type of {@link Sig}.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4LiuRoughSet} instance.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param roughClasses
	 * 		A {@link Set} of {@link Instance} {@link Set} as Rough Equivalence Classes.
	 * @param red
	 * 		Reduct attributes.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1, 0 as the decision attribute)
	 * @param insSize
	 * 		The number of {@link Instance}s.
	 * @param redPos
	 * 		The size of the reduct positive region.
	 * @return An {@link int} value as the most significant attribute.
	 */
	public static <Sig extends Number> int mostSignificantAttribute(
			FeatureImportance4LiuRoughSet<Sig> calculation, Sig sigDeviation, 
			Collection<Collection<Instance>> roughClasses, Collection<Integer> red,
			int[] attributes, int insSize, Sig redPos
	) {
		int index = -1;
		Sig positive, significance, max = null;
		int[] examine = new int[red.size()+1];
		int i=0;	for (int attr: red)		examine[i++] = attr;
		// Loop over non-reduct attributes
		for (int attr : attributes) {
			if (red.contains(attr))	continue;
			// Calculate Sig(red ∪ {a})
			examine[examine.length-1] = attr;
			positive = calculation.calculate4Incremental(roughClasses, new IntegerArrayIterator(examine), insSize)
								.getResult();
			// Calculate the difference between the above significance and the one of reduct.
			significance = calculation.difference(positive, redPos);
			// Update max significance and attribute
			if (index==-1 || calculation.value1IsBetter(significance, max, sigDeviation)) {
				max = significance;
				index = attr;
			}
		}
		return index;
	}
	
	/**
	 * Execute inspection to remove redundant attributes in reduct.
	 * 
	 * @param <Sig>
	 * 		{@link Number} implemented type as the value of Significance.
	 * @param calculation
	 * 		Implemented {@link FeatureImportance4LiuRoughSet} instance.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		A {@link List} of {@link Instance}.
	 * @param red
	 * 		The reduct attributes.
	 * @param core
	 * 		The core {@link List}.
	 * @param globalPos
	 * 		The global positive region's size.
	 */
	public static <Sig extends Number> void inspectReduct(
			FeatureImportance4LiuRoughSet<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, List<Integer> red,
			Collection<Integer> core, Sig globalPos
	){
		// Loop over reduct in reverse order.
		Collections.reverse(red);
		Integer[] redArray = red.toArray(new Integer[red.size()]);
		for (int attr: redArray) {
			// Return if the attribute is a core attribute.
			if (core.contains(attr)) {
				return;
			}else {
				red.remove(new Integer(attr));
				// Calculate sig(Red-{a})
				Sig redPos = calculation.calculate(instances, new IntegerCollectionIterator(red))
										.getResult();
				if (calculation.value1IsBetter(globalPos, redPos, sigDeviation)) {
					// Not redundant
					red.add(new Integer(attr));
				}
			}
		}
	}
}