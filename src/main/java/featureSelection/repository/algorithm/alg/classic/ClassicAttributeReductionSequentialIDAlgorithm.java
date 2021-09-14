package featureSelection.repository.algorithm.alg.classic;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.support.calculation.alg.classic.sequential.ClassicSequentialIDCalculation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Algorithm repository of <strong>Classic Attribute Reduction</strong>.
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class ClassicAttributeReductionSequentialIDAlgorithm {
	public static class Basic {
		/**
		 * Obtain the Equivalence Classes induced by the given attributes.
		 * 
		 * @param instances
		 * 		A {@link Collection} of {@link Instance}
		 * @param attributes
		 * 		Attributes of {@link Instance}. (starts from 1, 0 as decision attribute)
		 * @return A {@link Collection} of Equivalence Class in {@link Instance} {@link List}
		 */
		public static Collection<List<Instance>> equivalenceClass(
				Collection<Instance> instances, IntegerIterator attributes
		){
			Instance uPointer;
			List<Instance> equClass;
			Set<List<Instance>> equSet = new HashSet<>();
			Iterator<Instance> iterator = instances.iterator();
			
			// Initiate an equivalence class using the 1st instance.
			uPointer = iterator.next();
			equClass = new ArrayList<>();
			equClass.add(uPointer);
			equSet.add(equClass);

			// Loop over the rest of instances and partition.
			int attr;
			boolean newClass;
			Instance equRepresentitive;
			while (iterator.hasNext()) {
				// next instance
				uPointer = iterator.next();
				// newClass = true. flag
				newClass = true;
				// Loop over existing equivalence classes
				equSetLoop:
				for (List<Instance> e : equSet) {
					equRepresentitive = e.get(0);
					// check attribute values
					attributes.reset();
					while (attributes.hasNext()) {
						attr = attributes.next();
						// if ak(xi) != ak(x). x是 EClass[j]中的第一条记录
						if (uPointer.getAttributeValue(attr) !=
								equRepresentitive.getAttributeValue(attr)
						) {
							// 若有一个属性不相等，去计算xi是否属下一个类别
							continue equSetLoop;
						}
					}
					// uPointer belongs to an existing equivalence class
					e.add(uPointer);
					// newClassFlag = false;
					newClass = false;
				}
				// uPointer doesn't belong to any existing equivalence class
				if (newClass) {
					// create a new one.
					equClass = new LinkedList<>();
					equClass.add(uPointer);
					equSet.add(equClass);
				}
			}
			return equSet;
		}
		
		/**
		 * Obtain the Equivalence Class induced by Decision Attribute
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
		 * Get the position
		 * 
		 * @param dataset
		 * 		A {@link List} of {@link Instance}
		 * @param beginPos
		 * 		The begin index of the position.
		 * @param recordID
		 * 		Index of the the record.
		 * @return An index in {@link int} as position.
		 */
		public static int getPosition(List<Instance> dataset, int beginPos, int recordID) {
			// if(record.ID < dataset[beginPos].ID or record.ID > dataset[lastRecord].ID)
			if (recordID < dataset.get(beginPos).getNum() ||
				recordID > dataset.get(dataset.size()-1).getNum()
			) {
				// return -1;
				return -1;
			}else {
				// for i=1 to |dataset|
				for (int i=0; i<dataset.size(); i++) {
					// if (record.ID = dataset[i].ID
					if (recordID==dataset.get(i).getNum()) {
						// return i
						return i;
					}
				}
			}
			return -1;
		}
	}
	
	/**
	 * Get the core.
	 * 
	 * @param <Sig>
	 * 		{@link Number} implemented type as the value of Significance.
	 * @param calculation
	 * 		Implemented {@link ClassicSequentialIDCalculation}.
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
	public static <Sig extends Number> Collection<Integer> core(
			ClassicSequentialIDCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Collection<List<Instance>> decClasses, Sig globalSig,
			int...attributes
	){
		Collection<Integer> core = new HashSet<>(attributes.length);
		// Loop over attributes
		Sig redSig;
		int[] exAttribute = new int[attributes.length-1];
		for (int i=0; i<exAttribute.length; i++)	exAttribute[i] = attributes[i+1];
		for ( int i=0; i<attributes.length; i++ ) {
			redSig = calculation.calculate(
								Basic.equivalenceClass(instances, new IntegerArrayIterator(exAttribute)),
								decClasses,
								exAttribute.length
					).getResult();
			if (calculation.value1IsBetter(globalSig, redSig, sigDeviation)){
				core.add(attributes[i]);
			}
			// next
			if (i<exAttribute.length){
				exAttribute[i] = attributes[i];
			}
		}
		return core;
	}
	
	/**
	 * Get the least significant current attribute.
	 * 
	 * @param <Sig>
	 * 		{@link Number} implemented type as the value of Significance.
	 * @param calculation
	 * 		Implemented {@link ClassicSequentialIDCalculation}.
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
	 * @return A {@link int} value as the most significant attribute.
	 */
	public static <Sig extends Number> int leastSignificantAttribute(
			ClassicSequentialIDCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Collection<List<Instance>> decClasses,
			Collection<Integer> red, IntegerIterator attributes
	){
		// Initiate
		int sigAttr = -1;
		Sig min = null, subSig;
		// Go through potential attributes
		int i=0;	int[] attribute = new int[red.size()+1];	for (int r : red) attribute[i++] = r;
		int attr;
		attributes.reset();
		while (attributes.hasNext()) {
			attr = attributes.next();
			if (!red.contains(attr)) {
				attribute[attribute.length-1] = attr;
				subSig = calculation.calculate(
							Basic.equivalenceClass(instances, new IntegerArrayIterator(attribute)),
							decClasses,
							attribute.length
						).getResult();
				if (calculation.value1IsBetter(min, subSig, sigDeviation) || sigAttr==-1) {
					min = subSig;
					sigAttr = attr;
				}
			}
		}
		return sigAttr;
	}
	
	/**
	 * Get the most significant current attribute.
	 * 
	 * @param <Sig>
	 * 		{@link Number} implemented type as the value of Significance.
	 * @param calculation
	 * 		Implemented {@link ClassicSequentialIDCalculation}.
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
	 * @param redSig
	 * 		The reduct positive region value.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @return A {@link int} value as the most significant attribute.
	 */
	public static <Sig extends Number> int mostSignificantAttribute(
			ClassicSequentialIDCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, Collection<List<Instance>> decClasses,
			Collection<Integer> red, Sig redSig, int[] attributes
	){
		int sigAttr = -1;
		Sig max = null, sig, subSig;
		// Loop over potential attributes
		int i=0;
		int[] attribute = new int[red.size()+1];	for ( int r : red) attribute[i++] = r;
		for (int attr : attributes) {
			if (!red.contains(attr)) {
				attribute[attribute.length-1] = attr;
				subSig = calculation.calculate(
								Basic.equivalenceClass(instances, new IntegerArrayIterator(attribute)),
								decClasses,
								attribute.length
						).getResult();
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
	 * 		Implemented {@link ClassicSequentialIDCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param instances
	 * 		The {@link Instance} list.
	 * @param redArray
	 * 		Reduct attributes.
	 * @return Inspected reduct attributes.
	 */
	public static <Sig extends Number> List<Integer> inspection(
			ClassicSequentialIDCalculation<Sig> calculation, Sig sigDeviation,
			Collection<Instance> instances, IntegerIterator redArray
	){
		Collection<List<Instance>> equ = Basic.equivalenceClass(instances, redArray);
		Collection<List<Instance>> decEqu = Basic.equivalenceClassOfDecisionAttribute(instances);
		Sig globalSig = calculation.calculate(equ, decEqu, redArray.size())
									.getResult();
		List<Integer> red = new LinkedList<>();	
		redArray.reset();	while (redArray.hasNext())	red.add(redArray.next());

		Collection<List<Instance>> examEqu;
		Sig examSig;
		int examAttr;
		for (int i=red.size()-1; i>=0; i--) {
			examAttr = red.remove(i);
			examEqu = Basic.equivalenceClass(instances, new IntegerCollectionIterator(red));
			examSig = calculation.calculate(examEqu, decEqu, red.size())
								.getResult();
			if (calculation.value1IsBetter(globalSig, examSig, sigDeviation)){
				// not redundant.
				red.add(i, examAttr);
			}
		}
		return red;
	}
}