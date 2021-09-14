package featureSelection.repository.algorithm.alg.roughEquivalenceClassBased;

import common.utils.ArrayCollectionUtils;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.PlainNestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.PartitionResult;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.entity.alg.rec.nestedEC.MostSignificanceResult;
import featureSelection.repository.entity.alg.rec.nestedEC.NestedEquivalenceClassesInfo;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedIncrementalPartitionCalculation;
import featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased.PositiveRegionCalculation4IPNEC;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * Algorithm repository of Nested Equivalent Class based algorithm, evolving from 
 * {@link RoughEquivalenceClassBasedAlgorithm} and
 * {@link RoughEquivalenceClassBasedExtensionAlgorithm}
 * <p>
 * In this class, the following contents and algorithms are provided:
 * <ul>
 * 	<li>Basic ideas/implementations of <strong>Nested Equivalent Class based algorithm(NEC)
 * 		</strong></li>
 * 	<li><strong>IP-NEC</strong>
 * 		<p>Incremental Partition NEC. An re-implementation of 
 * 			{@link RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition IP-REC}.
 * 		<p>Moreover, new implementations are also provided too.
 * 		<p>Updating of NEC and reduct for incremental {@link Instance} arriving is provided.
 * 	</li>
 * 	<li><strong>I-NEC</strong>
 * 		<p>Incomplete NEC. Algorithm for incremental tolerance based attribute reduction which
 * 			is designed to be used in attribute reduction in incomplete decision systems with
 * 			incremental(/dynamic) {@link Instance} updating strategies provided.
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadSafetyNotSecured
public class NestedEquivalenceClassBasedAlgorithm {

	/**
	 * Basic algorithms for NEC.
	 *
	 * @author Benjamin_L
	 */
	public static class Basic {
		/**
		 * Obtain {@link EquivalenceClass}es of {@link Instance}s induced by given
		 * <code>attributes</code>.
		 * 
		 * @param instances
		 * 		A  {@link Collection} of {@link Instance}s.
		 * @param attributes
		 * 		Attributes of {@link Instance}. (Starts from 1).
		 * @return A {@link Collection} of {@link EquivalenceClass}es.
		 */
		public static Map<IntArrayKey, EquivalenceClass> equivalenceClass(
			Collection<Instance> instances, IntegerIterator attributes
		){
			// Initiate a key-value structure to contain equivalence classes and their keys.
			Map<IntArrayKey, EquivalenceClass> equClasses = new HashMap<>(instances.size());
			// Loop over instances and partition.
			int[] code;
			IntArrayKey key;
			EquivalenceClass equItem;
			for (Instance ins: instances) {
				code = Instance.attributeValuesOf(ins, attributes);
				key = new IntArrayKey(code);
				equItem = equClasses.get(key);
				if (equItem!=null) {
					// add the instance into the equivalence class.
					equItem.addClassItem(ins);
					//	if the decision values are not equal, update the equivalence class.
					if (ins.getAttributeValue(0)!=equItem.getDecisionValue()) {
						equItem.setUnsortable();
					}
				}else {
					// Initiate one.
					equClasses.put(key, equItem=new EquivalenceClass(ins));
				}
			};
			return equClasses;
		}
		
		/**
		 * Obtain {@link NestedEquivalenceClass}es induced by by the given <code>attributes</code>.
		 * 
		 * @param equClasses
		 * 		A {@link Collection} of {@link EquivalenceClass}es.
		 * @param attributes
		 * 		Attributes of {@link Instance}. (Starts from 1).
		 * @return {@link NestedEquivalenceClassesInfo} with
		 * 		<ul>
		 * 			<li>1. A {@link Map} whose keys are the equivalent attribute values in
		 * 		 		{@link IntArrayKey} and {@link NestedEquivalenceClass}es as values that are
		 * 		 		correspondent;
		 * 			</li>
		 * 			<li>2. significance in <code>int</code>;</li>
		 * 			<li>3. boolean value for empty boundary classes.</li>
		 * 		</ul>
		 */
		public static NestedEquivalenceClassesInfo<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>>
			nestedEquivalenceClass(
					Collection<EquivalenceClass> equClasses, IntegerIterator attributes
		) {
			Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses =
					new HashMap<>(equClasses.size());
			boolean notEmptyBoundary = false;
			// Loop over Equivalence Classes and partition.
			int[] keyArray;
			IntArrayKey key;
			NestedEquivalenceClass<EquivalenceClass> nestedEquClass;
			for (EquivalenceClass equClass: equClasses) {
				keyArray = new int[attributes.size()];
				attributes.reset();
				for (int i=0; i<keyArray.length; i++) {
					keyArray[i] = equClass.getAttributeValueAt(attributes.next() - 1);
				}
				key = new IntArrayKey(keyArray);
				// if no such key
				nestedEquClass = nestedEquClasses.get(key);
				if (nestedEquClass==null) {
					// create h, h.count=e[i].count, h.dec=e[i].dec
					// if e[i].cnst=true
					//	h.cnst=1
					// else e[i].cnst=false
					//	h.cnst=-1
					nestedEquClasses.put(key, new PlainNestedEquivalenceClass(equClass));
				// else nested equivalence class exists, update
				}else {
					nestedEquClass.addClassItem(equClass);
					switch (nestedEquClass.getType()) {
						case POSITIVE:		// 1-NEC
							// if decision values equal		        1-NEC
							if (nestedEquClass.getDec()==equClass.getDecisionValue()) {
								// do nothing
							// else							        1-NEC => 0-NEC
							}else {
								// update class set type.
								nestedEquClass.setType(ClassSetType.BOUNDARY);
								// mark 0-NEC
								notEmptyBoundary = true;
							}
							break;
						case NEGATIVE:		// -1-NEC
							// if equivalence class is consistent	-1-NEC => 0-NEC
							if (equClass.getType().isPositive()) {
								nestedEquClass.setType(ClassSetType.BOUNDARY);
								// mark 0-NEC
								notEmptyBoundary = true;
							}
							break;
						default:			// 0-NEC
							// do nothing
							break;
					}
				}
			}
			return new NestedEquivalenceClassesInfo<>(
					nestedEquClasses, 
					!notEmptyBoundary
				);
		}
	}

	/**
	 * <strong>IP-NEC</strong> for short. An re-implementation of <code>IP-REC</code> using
	 * {@link NestedEquivalenceClass}.
	 *
	 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition
	 *
	 * @author Benjamin_L
	 */
	public static class IncrementalPartition {
		/**
		 * Basic algorithms for IP-NEC.
		 *
		 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition.Basic
		 * 
		 * @author Benjamin_L
		 */
		public static class Basic {
			/**
			 * {@link ClassSetType#BOUNDARY} sensitive Nested Equivalence Classes partitioning.
			 * <p>
			 * Obtain Nested Equivalence Classes induced by the given <code>attributes</code>
			 * on equivalence classes. However, partitioning is interrupted and returns
			 * <code>null</code> if a {@link ClassSetType#BOUNDARY} Nested Equivalence Class
			 * is obtained.
			 *
			 * @param equClasses
			 * 		A {@link Collection} of {@link EquivalenceClass}es.
			 * @param attributes
			 * 		Attributes of {@link Instance}s for partitioning.(Starts from 1)
			 * @param hashKeyCapacity
			 * 		Capacity for initiation of {@link HashMap} whose keys are attribute values and
			 * 		values are {@link PlainNestedEquivalenceClass}.
			 * 		<code>capacity = sizeOf(Equivalent Class)</code> is recommended.
			 * @return <code>null</code> if the partitioned result contains
			 *          {@link ClassSetType#BOUNDARY} ones. / {@link PlainNestedEquivalenceClass}
			 *          {@link Collection}.
			 */
			public static Collection<PlainNestedEquivalenceClass>
				boundarySensitiveNestedEquivalenceClass(
					Collection<EquivalenceClass> equClasses, IntegerIterator attributes,
					int hashKeyCapacity
			){
				Map<IntArrayKey, PlainNestedEquivalenceClass> nestedEquClasses = new HashMap<>(hashKeyCapacity);
				// Loop over equivalence classes and partition
				int[] code;
				IntArrayKey key;
				PlainNestedEquivalenceClass nestedEquClass;
				for (EquivalenceClass equClass: equClasses) {
					// obtain key
					attributes.reset();
					key=new IntArrayKey(code=new int[attributes.size()]);
					for (int i=0; i<attributes.size(); i++) {
						code[i] = equClass.getAttributeValueAt(attributes.next() - 1);
					}
					nestedEquClass = nestedEquClasses.get(key);
					// If no such key
					if (nestedEquClass==null) {
						// create a nested equivalence class
						nestedEquClasses.put(key, nestedEquClass=new PlainNestedEquivalenceClass(equClass));
						nestedEquClass.setType(equClass.getType());
					// Else the nested equivalence class already exists.
					}else {
						nestedEquClass.addClassItem(equClass);
						if (// 1-NEC
							nestedEquClass.getType().isPositive() &&
							// equivalence class is consistent
							equClass.sortable() &&
							// decision values equal
							equClass.getDecisionValue()==nestedEquClass.getDec()
						) {
							// 1-NEC
							// do nothing
						}else if (
							// -1-NEC
							nestedEquClass.getType().isNegative() &&
							// equivalence class is in-consistent
							!equClass.sortable()
						) {
							// -1-NEC
							// do nothing
						}else {
							// 0-NEC
							return null;    // boundary sensitive, return!
						}
					}
				}
				return nestedEquClasses.values();
			}
		}
		
		/**
		 * Algorithms for obtaining core for IP-NEC.
		 * <p>
		 * Identical to IP-REC.
		 *
		 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition.Core
		 * 
		 * @author Benjamin_L
		 */
		@SuppressWarnings("deprecation")
		public static class Core extends RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition.Core {}
		
		/**
		 * Obtain the most significant attribute for empty reduct(for 1st round of searching).
		 * 
		 * @see IncrementalPartition#mostSignificantAttribute(Collection, int[], Collection, 
		 * 		NestedEquivalenceClassBasedIncrementalPartitionCalculation, Number)
		 * 
		 * @param <Sig>
		 * 		Type of feature subset significance.
		 * @param equClasses
		 * 		A {@link Collection} of {@link EquivalenceClass}es.
		 * @param attributes
		 * 		Attributes of {@link Instance}s. (starts from 1)
		 * @param calculation
		 * 		{@link NestedEquivalenceClassBasedIncrementalPartitionCalculation} instance.
		 * @param sigDeviation
		 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
		 * 		the difference between two sigs is less than the given deviation value.
		 * @return Wrapped {@link MostSignificanceResult}.
		 * @throws Exception if exceptions occur when calling 
		 * 			{@link NestedEquivalenceClassBasedIncrementalPartitionCalculation#calculate(
		 * 			IntegerIterator, Collection, Object...) calculate()}
		 */
		public static <Sig extends Number> MostSignificanceResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Sig>
			mostSignificantAttribute(
				Collection<EquivalenceClass> equClasses, int[] attributes,
				NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig> calculation,
				Sig sigDeviation
		) throws Exception {
			int sigAttr = -1;
			Sig maxSig = null;
			// Loop over all attributes
			int[] attrArray = new int[1];
			IntegerArrayIterator key = new IntegerArrayIterator(attrArray);
			NestedEquivalenceClassesInfo<Collection<NestedEquivalenceClass<EquivalenceClass>>> sigResult = null;
			for (int a=0; a<attributes.length; a++) {
				// red = red U a
				// NEC, sig, flag = NEC(NEC, P)
				attrArray[0] = attributes[a];
				
				calculation.calculate(key.reset(), equClasses);
				// if sig > maxSig, update.
				if (maxSig==null ||
					calculation.value1IsBetter(calculation.getResult(), maxSig, sigDeviation)
				) {
					sigAttr = attributes[a];
					maxSig = calculation.getResult();
					sigResult = calculation.getNecInfoWithCollection();
				}
			}
			return new MostSignificanceResult<>(
						// the correspondent nested equivalence classes
						sigResult.getNestedEquClasses(),
						// the sig attribute
						sigAttr,
						// the sig value
						maxSig,
						// whether contains 0-NEC?
						sigResult.isEmptyBoundaryClass()
					);
		}
		
		/**
		 * Obtain the current most significant attribute.
		 * 
		 * @see IncrementalPartition#mostSignificantAttribute(Collection, int[], 
		 * 		NestedEquivalenceClassBasedIncrementalPartitionCalculation, Number)
		 * 
		 * @param <Sig>
		 * 		Type of feature subset significance.
		 * @param nestedEquClasses
		 * 		A {@link Collection} of {@link NestedEquivalenceClass}es.
		 * @param attributes
		 * 		Attributes of {@link Instance}s. (starts from 1)
		 * @param reduct
		 * 		Current reduct. 
		 * @param calculation
		 * 		{@link NestedEquivalenceClassBasedIncrementalPartitionCalculation} instance.
		 * @param sigDeviation
		 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
		 * 		the difference between two sigs is less than the given deviation value.
		 * @return Wrapped {@link MostSignificanceResult}.
		 * @throws Exception if exceptions occur when calling 
		 * 			{@link NestedEquivalenceClassBasedIncrementalPartitionCalculation#calculate(
		 * 			IntegerIterator, Collection, Object...) calculate()}
		 */
		public static <Sig extends Number> MostSignificanceResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Sig>
			mostSignificantAttribute(
				Collection<? extends NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses,
				int[] attributes, Collection<Integer> reduct,
				NestedEquivalenceClassBasedIncrementalPartitionCalculation<Sig> calculation,
				Sig sigDeviation
		) throws Exception {
			int sigAttr = -1;
			Sig maxSig = null;
			// Loop over all potential attributes(attributes not in reduct)
			NestedEquivalenceClassesInfo<Collection<NestedEquivalenceClass<EquivalenceClass>>> sigResult = null;
			for (int a=0; a<attributes.length; a++) {
				if (reduct.contains(attributes[a]))	continue;
				// calculate the significance of (reduct U a)
				calculation.incrementalCalculate(new IntegerArrayIterator(new int[] {attributes[a]}), nestedEquClasses);
				// if sig>maxSig, update
				if (maxSig==null ||
					calculation.value1IsBetter(calculation.getResult(), maxSig, sigDeviation)
				) {
					sigAttr = attributes[a];
					maxSig = calculation.getResult();
					sigResult = calculation.getNecInfoWithCollection();
				}
			}
			return new MostSignificanceResult<>(
						// the correspondent nested equivalence classes
						sigResult.getNestedEquClasses(),
						// the sig attribute
						sigAttr,
						// the sig value
						maxSig,
						// whether contains 0-NEC?
						sigResult.isEmptyBoundaryClass()
					);
		}
		
		/**
		 * Redundancy inspection for reduct to remove redundant features.
		 * 
		 * @author Benjamin_L
		 */
		public static class Inspection {
			/** 
			 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition.Inspection
			 * 		#compute(AttrProcessStrategy4Comb, Collection)
			 */
			@SuppressWarnings("deprecation")
			public static Collection<Integer> computeEquivalenceClasses(
					AttrProcessStrategy4Comb inspectAttributeProcessStrategy,
					Collection<EquivalenceClass> equClasses
			) {
				return RoughEquivalenceClassBasedExtensionAlgorithm
						.IncrementalPartition
						.Inspection
						.compute(inspectAttributeProcessStrategy, equClasses);
			}

			/** 
			 * Self-designed inspection procedure specialised for IP-NEC.
			 * <p>
			 * Use <code>inspectAttributeProcessStrategy</code> to assign attributes for
			 * <strong>incremental</strong> partition bases on <code>nestedEquClasses</code> in
			 * inspection.
			 * <p>
			 * Actually, this method is identical to
			 * {@link RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition.Inspection
			 * #compute(AttrProcessStrategy4Comb, Collection)}
			 *
			 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition.Inspection
			 *      #compute(AttrProcessStrategy4Comb, Collection)
			 * 
			 * @param inspectAttributeProcessStrategy
			 * 		Implemented {@link AttributeProcessStrategy} instance.
			 * @param nestedEquClasses
			 * 		{@link NestedEquivalenceClass} {@link Collection}.
			 * @return Reduct inspected.
			 */
			public static Collection<Integer> computeNestedEquivalenceClasses(
					AttrProcessStrategy4Comb inspectAttributeProcessStrategy,
					Collection<NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses
			) {
				// If contains 1 attribute only, no need to inspect
				if (inspectAttributeProcessStrategy.getAllLength()==1)
					return Arrays.asList(inspectAttributeProcessStrategy.getExamingAttr());

				Collection<Integer> red = null, redundant = null;
				// Initiate pointer: begin = 0, end = m-1
				int[] attributeGroup;
				PartitionResult<Collection<Integer>, Collection<NestedEquivalenceClass<EquivalenceClass>>> partitionResult;
				// Loop over all attribute groups
				while (inspectAttributeProcessStrategy.hasNext()) {
					// Extract attributes from begin to end: B as attributeGroup
					if (redundant==null || redundant.isEmpty()) {
						// no redundant attributes yet, extract directly.
						attributeGroup = inspectAttributeProcessStrategy.getInLineAttr();
					}else {
						// need to check if redundant attributes in extracted group.
						Collection<Integer> examingLineAttrs = new LinkedList<>();
						// Loop over in-line attributes and collect ones that are not redundant.
						for (int a: inspectAttributeProcessStrategy.getInLineAttr()) {
							if (!redundant.contains(a)) examingLineAttrs.add(a);
						}
						// if |examing line attributes|==|in-line attributes|: all not redundant
						//  set attributeGroup = in-line attributes directly
						// else: has redundant attributes
						//  set attributeGroyp = in-line attributes without redundant ones.
						attributeGroup =
								examingLineAttrs.size()==inspectAttributeProcessStrategy.getInLineAttr().length?
									Arrays.copyOf(
										inspectAttributeProcessStrategy.getInLineAttr(),
										inspectAttributeProcessStrategy.getInLineAttr().length
									):
									ArrayCollectionUtils.getIntArrayByCollection(examingLineAttrs);
					}
					// If no non-redundant attributes in attributeGroup
					if (attributeGroup.length==0) {
						// Collect attributes left in examing line attributes, plus the examing
						//  attribute.
						int[] left = Arrays.copyOf(
										inspectAttributeProcessStrategy.getExamingLineAttr(), 
										inspectAttributeProcessStrategy.getExamingLineAttr().length+1
								);
						left[left.length-1] = inspectAttributeProcessStrategy.getExamingAttr();
						return computeNestedEquivalenceClasses(
									inspectAttributeProcessStrategy.initiate(
										new IntegerArrayIterator(left)
									),
									nestedEquClasses
								);
					}
					// Use attributes in attributeGroup to do 1st round of partitioning: U/(C-B),
					//  one by one.
					partitionResult = PositiveRegionCalculation4IPNEC
										.inTurnIncrementalPartition(
											nestedEquClasses, 
											new IntegerArrayIterator(attributeGroup), 
											false
										);
					// if 0-NEC is empty
					if (partitionResult.isEmptyBoundaryClassSetTypeClass()) {
						if (partitionResult.getAttributes().size()==1) {
							return partitionResult.getAttributes();
						}else {
							// The rest of the attributes are redundant, inspect the rest of the
							//  attributes by recursively compute().
							return computeNestedEquivalenceClasses(
										inspectAttributeProcessStrategy.initiate(
											// rest of the attributes
											new IntegerCollectionIterator(partitionResult.getAttributes())
										),
										nestedEquClasses
									);
						}
					}else {
						// Attributes used in partitioning are not enough.
						// Loop over the rest of attributes(that are not in the attributes used in
						//  the 1st round partitioning).
						if (red==null) {
							red = new HashSet<>(inspectAttributeProcessStrategy.getAllLength());
							red.addAll(partitionResult.getAttributes());
						}
						
						int attr;
						IntegerIterator examLine;
						AttributeLoop:
						while (inspectAttributeProcessStrategy.hasNextExamAttribute()) {
							Collection<Integer> tmp2 = redundant;
							// Select an attribute for redundancy inspection.
							attr = inspectAttributeProcessStrategy.getExamingAttr();
							if (tmp2==null || tmp2.isEmpty()) {
								examLine = new IntegerArrayIterator(
											inspectAttributeProcessStrategy.getExamingLineAttr()
										);
							}else {
								// Filter redundant attributes in examing line attributes.
								Collection<Integer> examingLineAttrs = new LinkedList<>();
								for (int each: inspectAttributeProcessStrategy.getExamingLineAttr()) {
									// filter redundant
									if (!tmp2.contains(each))	examingLineAttrs.add(each);
								}
								examLine = new IntegerCollectionIterator(examingLineAttrs);
							}
							
							if (examLine.size()!=0) {
								// Loop over attributes in examing line to partition
								for (NestedEquivalenceClass<EquivalenceClass> nestedEquClass:
										partitionResult.getRoughClasses()
								) {
									// If |0-REC|!=0, the attribute is required to eliminate
									//  the 0-REC => not redundant.
									if (Basic.boundarySensitiveNestedEquivalenceClass(
											nestedEquClass.getEquClasses().values(),
											examLine,
											nestedEquClass.getItemSize()
										)==null
									) {
										red.add(attr);
										// Inspect the next attribute in examing attribute line.
										inspectAttributeProcessStrategy.updateExamAttribute();
										continue AttributeLoop;
									}
								}
								// Used all examing line attributes and still no 0-REC occur,
								//  then the current examing attribute is redundant.
								if (redundant==null){
									redundant = new HashSet<>(inspectAttributeProcessStrategy.getAllLength());
								}
								redundant.add(attr);
							}else {
								// not redundant.
								red.add(attr);
							}
							// Inspect the next attribute in examing attribute line.
							inspectAttributeProcessStrategy.updateExamAttribute();
						}
						// Update pointers:
						//  begin=end
						//  end = i+1<g? end+m: |C|-1
						inspectAttributeProcessStrategy.updateInLineAttrs();
					}
				}
				// remove all redundant attributes from reduct.
				if (redundant!=null && !redundant.isEmpty()){
					red.removeAll(redundant);
				}
				return red;
			}
		}
	
		/**
		 * Update the {@link NestedEquivalenceClass} {@link Map} with the new reduct.
		 * <p>
		 * (For incremental dataset computation.)
		 *
		 * @param hashCapacity
		 * 		Capacity of the updated Nested Equivalence Classes {@link Map}. (If not known,
		 * 		the size of {@link EquivalenceClass}es is recommended) In {@link Map}, 16 by
		 * 		default.
		 * @param fullReductInOrder
		 * 		Full reduct that contains both previous reduct and the new one sorted in
		 * 		ascending order.
		 * @param newReduct
		 * 		The new reduct(with previous reduct attributes added).
		 * @param previousNestedEquClasses
		 * 		Previous Nested Equivalence Classes {@link Map} to be updated which is induced
		 * 		by previous reduct.
		 * @return Updated Nested Equivalence Classes {@link Map} with equivalent values as keys
		 * 		and corresponding {@link NestedEquivalenceClass}es as values.
		 */
		public static Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> updateNestedEquivalenceClassKey(
				int hashCapacity, 
				IntegerIterator fullReductInOrder, Collection<Integer> newReduct, 
				Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> previousNestedEquClasses
		) {
			Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> newNestedEquClasses =
					new HashMap<>(hashCapacity);

			// Loop over the previous nested equivalence classes and update.
			int[] equivalentIndex, key;
			NestedEquivalenceClass<EquivalenceClass> nestedEquClass;
			Map.Entry<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> previousNestedEquClass;
			Iterator<NestedEquivalenceClass<EquivalenceClass>> iterator4NestedEquClass;
			Iterator<Map.Entry<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>> iterator4Previous =
					previousNestedEquClasses.entrySet().iterator();
			Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> subNestedEquClasses;
			while (iterator4Previous.hasNext()) {
				previousNestedEquClass = iterator4Previous.next();
				if (previousNestedEquClass.getValue().getItemSize()>1) {
					// partitioning using new reduct.
					subNestedEquClasses = 
						NestedEquivalenceClassBasedAlgorithm
							.Basic
							.nestedEquivalenceClass(
								previousNestedEquClass.getValue().getEquClasses().values(), 
								new IntegerCollectionIterator(newReduct)
							).getNestedEquClasses();
					
					iterator4NestedEquClass = subNestedEquClasses.values().iterator();
					do {
						// obtain the full equivalent value.
						nestedEquClass = iterator4NestedEquClass.next();
						equivalentIndex = nestedEquClass.getEquClasses()
												.keySet()
												.iterator()
												.next()
												.key();
						// re-construct key based on the <code>fullReductInOrder</code>
						fullReductInOrder.reset();
						key = new int[fullReductInOrder.size()];
						for (int i=0; i<key.length; i++){
							key[i] = equivalentIndex[fullReductInOrder.next()-1];
						}
						// store the Nested Equivalence Class
						newNestedEquClasses.put(new IntArrayKey(key), nestedEquClass);
					} while (iterator4NestedEquClass.hasNext());
				}else {
					equivalentIndex = previousNestedEquClass.getValue().getEquClasses()
											.keySet()
											.iterator()
											.next()
											.key();
					// re-construct key based on the <code>fullReductInOrder</code>
					fullReductInOrder.reset();
					key = new int[fullReductInOrder.size()];
					for (int i=0; i<key.length; i++){
						key[i] = equivalentIndex[fullReductInOrder.next()-1];
					}
					// store the Nested Equivalence Class
					newNestedEquClasses.put(new IntArrayKey(key), previousNestedEquClass.getValue());
				}
				iterator4Previous.remove();
			}
			return newNestedEquClasses;
		}
	}

}