package featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadUnsafe;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedAlgorithm;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.PlainNestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.incrementalPartition.RoughEquivalenceClassDummy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.PartitionResult;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.StatisticResult;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.entity.alg.rec.nestedEC.NestedEquivalenceClassesInfo;
import featureSelection.repository.entity.alg.rec.nestedEC.impl.nestedEquivalenceClassesMerge.params.DefaultNestedEquivalenceClassesMergerParams;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.FeatureImportance4NestedEquivalenceClassBased;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedIncrementalPartitionCalculation;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedStreamingDataCalculation;
import featureSelection.repository.support.calculation.positiveRegion.DefaultPositiveRegionCalculation;

import org.apache.commons.math3.util.FastMath;

import lombok.Getter;

/**
 * Positive Region Calculation for Incremental Partition NEC.
 * 
 * <p>IP-NEC: Incremental Partition - NEC.
 * 
 * @see NestedEquivalenceClassBasedAlgorithm
 * @see NestedEquivalenceClassBasedAlgorithm.IncrementalPartition
 * 
 * @author Benjamin_L
 */
@RoughSet
@ThreadUnsafe
public class PositiveRegionCalculation4IPNEC
	extends DefaultPositiveRegionCalculation
	implements FeatureImportance4NestedEquivalenceClassBased<Integer>,
				NestedEquivalenceClassBasedIncrementalPartitionCalculation<Integer>,
				NestedEquivalenceClassBasedStreamingDataCalculation<Integer,
																	DefaultNestedEquivalenceClassesMergerParams<Integer>>
{
	/* -------------------------------------------------------------------------------------------------- */
	
	@Getter private NestedEquivalenceClassesInfo<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>> necInfoWithMap;
	@Getter private Integer result;

	/* -------------------------------------------------------------------------------------------------- */
	
	/**
	 * Update {@link NestedEquivalenceClass}es as well as the global positive region(
	 * updated previous 1-{@link EquivalenceClass} included).
	 * <p>
	 * <strong>Notice</strong>: 
	 * <p>
	 * Previous Nested Equivalent Classes will be updated in the merging. So, after the
	 * update and mergings, the Nested Equivalent Classes in
	 * {@link DefaultNestedEquivalenceClassesMergerParams#getPreviousReductNestedEquClasses()}
	 * is actually the latest Nested Equivalent Classes.
	 * <p>
	 * However, class sets in {@link DefaultNestedEquivalenceClassesMergerParams#
	 * getPreviousReductNestedEquClasses()} are ones whose type are not
	 * {@link ClassSetType#BOUNDARY}. To access {@link ClassSetType#BOUNDARY}
	 * {@link NestedEquivalenceClass}es, please call {@link #getNecInfoWithMap()}.
	 * <p>
	 * <strong>PS: </strong>
	 * <p>
	 * Please call {@link #getNecInfoWithMap()} to get {@link NestedEquivalenceClassesInfo}
	 * and <code>getResult()</code> to get the calculation result(i.e. updated positive
	 * region).
	 *
	 * @see NestedEquivalenceClassBasedStreamingDataCalculation#update4Arrived(Update4ArrivedInputs, Object...)
	 */
	@Override
	public FeatureImportance4NestedEquivalenceClassBased<Integer> update4Arrived(
			Update4ArrivedInputs<Integer, DefaultNestedEquivalenceClassesMergerParams<Integer>> inputs,
			Object... args
	) {
		// Use equivalent classes to compress new adding data.
		Map<IntArrayKey, EquivalenceClass> arrivedEquivalenceClasses = inputs.getArrivedEquivalenceClasses();
		// Use current reduct to partition adding equivalent classes
		Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses4NewData =
				PositiveRegionCalculation4RSCNEC.nestedEquivalenceClass(
					arrivedEquivalenceClasses.values(),
					inputs.getPreviousReduct()
				).getNestedEquivalenceClassesInfo()
				.getNestedEquClasses();
		
		// Initiate an S to store 0-NEC
		int pos = inputs.getPreviousSig().intValue();
		Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> boundaries =
			new HashMap<>(
				// possible maximum capacity = |new NEC|+|old NEC|
				FastMath.max(
					16, 
					nestedEquClasses4NewData.size()+inputs.getPreviousReductNestedEquClasses().size()
				)
			);
		
		// Go through adding equivalent classes
		NestedEquivalenceClass<EquivalenceClass> previousNestedEquClass;
		for (Entry<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> newData: nestedEquClasses4NewData.entrySet()) {
			// key = red(E[x])
			// If key doesn't exist in S.
			previousNestedEquClass = inputs.getPreviousReductNestedEquClasses().get(newData.getKey());
			if (previousNestedEquClass==null) {
				// Check ClassSetType.
				switch(newData.getValue().getType()) {
					// if E[x].cnst=0
					case BOUNDARY:
						// Add E[x] into boundaries
						boundaries.put(newData.getKey(), newData.getValue());
						break;
					// else if E[x].cnst=1
					case POSITIVE:
						// sig = sig + count(E[x])
						pos += newData.getValue().getInstanceSize();
						// add the nested equivalent class into NEC.
						inputs.getPreviousReductNestedEquClasses().put(newData.getKey(), newData.getValue());
						break;
					default:
						// add the nested equivalent class into NEC.
						inputs.getPreviousReductNestedEquClasses().put(newData.getKey(), newData.getValue());
						break;
				}
			// else key exists in H.
			}else {
				// Get s in S
				// newS, flag, sig = merge(s, E[x], sig)
				NestedEquivalenceClass<EquivalenceClass> mergedNestedEquClass =
						inputs.getNecMerger()
								.merge(
									new DefaultNestedEquivalenceClassesMergerParams<Integer>(
										inputs.getAttributes().length,
										inputs.getPreviousReductNestedEquClasses(),
										inputs.getPreviousSig(),
										previousNestedEquClass,
										newData.getValue()
									)
								);
				
				// if merged.cnst==1
				if (ClassSetType.POSITIVE.equals(mergedNestedEquClass.getType())) {
					// sig = sig + E[2].count
					pos += newData.getValue().getInstanceSize();
				// else if E[1].cnst==1 && E[1].cnst!=merged.cnst
				}else if (
					ClassSetType.POSITIVE.equals(previousNestedEquClass.getType()) &&
					!ClassSetType.POSITIVE.equals(mergedNestedEquClass.getType())
				) {
					// sig = sig - E[1].count
					pos -= previousNestedEquClass.getInstanceSize();
				}
				// if flag==true
				//	add newS into nH
				if (ClassSetType.BOUNDARY.equals(mergedNestedEquClass.getType())) {
					boundaries.put(newData.getKey(), mergedNestedEquClass);
					inputs.getPreviousReductNestedEquClasses()
							.remove(newData.getKey());
				// else
				//	add newS into H.
				}else {
					inputs.getPreviousReductNestedEquClasses()
							.put(newData.getKey(), mergedNestedEquClass);
				}
			}
		}

		necInfoWithMap = new NestedEquivalenceClassesInfo<>(
					boundaries,
					boundaries.isEmpty()
				);
		result = pos;
		return this;
	}
	
	/* -------------------------------------------------------------------------------------------------- */
	
	@Getter private Collection<Integer> partitionAttributes;
	@Getter private NestedEquivalenceClassesInfo<Collection<NestedEquivalenceClass<EquivalenceClass>>> necInfoWithCollection;
	
	/**
	 * Compute and calculate the positive region using dynamic group number in
	 * <code>IP-NEC</code>.
	 * <p>
	 * After calculation, the following fields are accessible for calculation
	 * result/feedback besides {@link #result}:
	 * <ul>
	 *     <li><strong>{@link #partitionAttributes}</strong></li>
	 * </ul>
	 *
	 * @see NestedEquivalenceClassBasedAlgorithm.IncrementalPartition
	 * @see PositiveRegionCalculation4IPREC
	 * 
	 * @param attributeProcessStrategy
	 * 		{@link AttributeProcessStrategy} instance.
	 * @param equClasses
	 * 		{@link EquivalenceClass} {@link Collection}.
	 * @param args
	 * 		Extra arguments.
	 * @return <code>this</code> instance.
	 */
	@Override
	public NestedEquivalenceClassBasedIncrementalPartitionCalculation<Integer> calculate(
			AttributeProcessStrategy attributeProcessStrategy, Collection<EquivalenceClass> equClasses,
			Object... args
	) {
		@SuppressWarnings("deprecation")
		PartitionResult<Collection<Integer>, Collection<RoughEquivalenceClassDummy>> partitionResult =
				RoughEquivalenceClassBasedExtensionAlgorithm
					.IncrementalPartition
					.Basic
					.dynamicIncrementalPartition(attributeProcessStrategy, equClasses);
		
		countCalculate(partitionResult.getAttributes().size());
		
		result = partitionResult.getPositive();
		partitionAttributes = partitionResult.getAttributes();
		return this;
	}
	
	/**
	 * Using dynamic attribute group process strategy with <code>Incremental Partition
	 * Strategy</code> to perform {@link NestedEquivalenceClass} based calculation.
	 * <p>
	 * After calculation, the following fields are accessible using <code>getter</code>
	 * for calculation result/feedback besides {@link #result}:
	 * <li><strong>{@link #partitionAttributes}</strong></li>
	 * <li><strong>{@link #necInfoWithCollection}</strong></li>
	 * 
	 * @see NestedEquivalenceClassBasedAlgorithm.IncrementalPartition
	 * @see PositiveRegionCalculation4IPREC#calculate(AttributeProcessStrategy, Collection, Object...)
	 * 
	 * @param attributeProcessStrategy
	 * 		{@link AttributeProcessStrategy} instance.
	 * @param nestedEquClasses
	 * 		{@link NestedEquivalenceClass} {@link Collection}.
	 * @return int value as positive region.
	 */
	@Override
	public NestedEquivalenceClassBasedIncrementalPartitionCalculation<Integer> incrementalCalculate(
			AttributeProcessStrategy attributeProcessStrategy, 
			Collection<? extends NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses,
			Object...args
	) {
		PartitionResult<Collection<Integer>, Collection<NestedEquivalenceClass<EquivalenceClass>>> partitionResult =
				incrementalDynamicIncrementalPartition(attributeProcessStrategy, nestedEquClasses);
		
		countCalculate(partitionResult.getAttributes().size());
		
		result = partitionResult.getPositive();
		partitionAttributes = partitionResult.getAttributes();
		necInfoWithCollection = new NestedEquivalenceClassesInfo<>(
										partitionResult.getRoughClasses(),
										partitionResult.isEmptyBoundaryClassSetTypeClass()
									);
		return this;
	}
	
	/**
	 * Using the given {@link AttributeProcessStrategy} to extract partial attributes to 
	 * further partition the given <code>roughClasses</code> incrementally.
	 * 
	 * @param attributeProcessStrategy
	 * 		Implemented {@link AttributeProcessStrategy} instance.
	 * @param nestEquClasses
	 * 		{@link NestedEquivalenceClass} {@link Collection}.
	 * @return {@link PartitionResult} instance, results includes:
	 * 		<ul>
	 * 			<li><code>positive region number</code>, </li>
	 * 			<li><code>whether positive region has reached global one</code>, </li>
	 * 			<li><code>attributes used in actual partition</code>, </li>
	 * 			<li><code>partitioned {@link NestedEquivalenceClass} {@link Collection}</code></li>
	 * 		</ul>
	 */
	public static PartitionResult<Collection<Integer>, Collection<NestedEquivalenceClass<EquivalenceClass>>>
		incrementalDynamicIncrementalPartition(
			AttributeProcessStrategy attributeProcessStrategy, 
			Collection<? extends NestedEquivalenceClass<EquivalenceClass>> nestEquClasses
	){
		// Record used attributes.
		int attributesSize = attributeProcessStrategy.attributeLength();
		Collection<Integer> usedAttributes = new HashSet<>(attributesSize);

		int pos = 0;
		int[] partitionAttribute;
		StatisticResult<Collection<NestedEquivalenceClass<EquivalenceClass>>> statisticResult;
		Collection<NestedEquivalenceClass<EquivalenceClass>> boundaries, result;
		do {
			// Initiate partition attributes.
			partitionAttribute = attributeProcessStrategy.next();
			boundaries = new LinkedList<>();
			for (NestedEquivalenceClass<EquivalenceClass> nectedEquClass: nestEquClasses) {
				// if 0-NEC
				if (ClassSetType.BOUNDARY.equals(nectedEquClass.getType())) {
					// Partition.
					statisticResult = 
						calculateEquivalenceClassPosPartition(
							nectedEquClass.getEquClasses().values(), 
							new IntegerArrayIterator(partitionAttribute)
						);
					// Calculate positive region.
					pos += statisticResult.getPositiveRegion();
					// if 0-REC is not empty after partition.
					if (!statisticResult.isEmptyBoundaryClassSet())
						boundaries.addAll(statisticResult.getRecord());
				}
			};
			// Record used attributes.
			if (partitionAttribute!=null)	for (int a: partitionAttribute)	usedAttributes.add(a);
			
			result = boundaries;
		// If no 0-REC left, partition finish.
		}while (!result.isEmpty() && usedAttributes.size()<attributesSize);
		return new PartitionResult<Collection<Integer>, Collection<NestedEquivalenceClass<EquivalenceClass>>>(
				pos, 
				result.isEmpty(),
				usedAttributes,
				result
			);
	}

	/**
	 * Calculate positive region of {@link EquivalenceClass} {@link Collection} for extra
	 * attributes partition.
	 * <p>
	 * Comparing to the method with the parameter <code>hashKeyCapacity</code>, this
	 * function initiates <code>HashMap</code> with its default <code>HashMap</code>
	 * capacity which is <strong>the size of {@link EquivalenceClass}</strong> during
	 * computation.
	 *  
	 * @param equClasses
	 * 		A {@link Collection} of {@link EquivalenceClass} {@link Collection}s of a
	 * 		Nested Equivalence Class.
	 * @param attributes
	 * 		Extra attributes involved in the partition.
	 * @return {@link StatisticResult} instance with current positive region number and 
	 * 		statistic record Collection as nested Equivalent Classes.
	 */
	public static StatisticResult<Collection<NestedEquivalenceClass<EquivalenceClass>>> calculateEquivalenceClassPosPartition(
			Collection<EquivalenceClass> equClasses, IntegerIterator attributes
	) {
		// Initiate an Nested Equivalent Classes map
		Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> incNestedClassesMap = new HashMap<>(equClasses.size());
		
		int pos=0;
		int[] code;
		IntArrayKey key;
		boolean emptyBoundary = true;
		NestedEquivalenceClass<EquivalenceClass> record;
		for (EquivalenceClass eClass: equClasses) {
			// key = attrValues(e[i])
			key = new IntArrayKey(code=new int[attributes.size()]);
			attributes.reset();
			for (int i=0; i<attributes.size(); i++)
				code[i] = eClass.getAttributeValueAt(attributes.next()-1);
			record = incNestedClassesMap.get(key);
			// if nested equivalent classes doesn't contains key.
			if (record==null) {
				// Initiate and add the nested equivalent class
				incNestedClassesMap.put(key, record=new PlainNestedEquivalenceClass(eClass));
				// Update positive region if 1-NEC.
				if (eClass.sortable())	pos += eClass.getItemSize();
			// else contains key.
			}else {
				switch (record.getType()) {
					case POSITIVE:	// 1-NEC
						// if decision values are not equal, 1-NEC -> 0-NEC
						if (Integer.compare(eClass.getDecisionValue(), record.getDec())!=0) {
							if (emptyBoundary)	emptyBoundary = false;
							record.setType(ClassSetType.BOUNDARY);
							record.setDec(-1);
							pos -= record.getInstanceSize();
						// else update positive region
						}else {
							pos += eClass.getItemSize();
						}
						break;
					case NEGATIVE:	// -1-NEC
						// if types are different, -1-NEC -> 0-NEC
						if (eClass.sortable()) {
							if (emptyBoundary)	emptyBoundary = false;
							record.setType(ClassSetType.BOUNDARY);
						}else {
							// do nothing
						}
						break;
					default: 		// 0-NEC
						// do nothing
						break;
				}
				// Add the equivalent class into the nested equivalent class.
				record.addClassItem(eClass);
			}
		}
		// return
		return new StatisticResult<>(pos, emptyBoundary, incNestedClassesMap.values());
	}
	
	/* -------------------------------------------------------------------------------------------------- */
	
	/**
	 * Compute and calculate the positive region using <strong>in-turn</strong> attribute
	 * process strategy in <code>IP-NEC</code>.
	 * <p>
	 * After calculation, the following fields are accessible using <code>getter</code>
	 * for calculation result/feedback besides {@link #result}:
	 * <ul>
	 *		<li><strong>{@link #partitionAttributes}</strong></li>
	 * </ul>
	 *
	 * 
	 * @see NestedEquivalenceClassBasedAlgorithm.IncrementalPartition
	 * 
	 * @param attributes
	 * 		Attributes to be used in partition.
	 * @param equClasses
	 * 		{@link EquivalenceClass} {@link Collection}.
	 * @param args
	 * 		Extra arguments.
	 * @return <code>this</code> instance.
	 */
	@Override
	public NestedEquivalenceClassBasedIncrementalPartitionCalculation<Integer> calculate(
			IntegerIterator attributes, Collection<EquivalenceClass> equClasses,
			Object... args
	) {
		if (attributes.size()!=0) {
			PartitionResult<Collection<Integer>, Collection<NestedEquivalenceClass<EquivalenceClass>>>
				partitionResult = inTurnPartition(equClasses, attributes);
			
			countCalculate(partitionResult.getAttributes().size());
			
			result = partitionResult.getPositive();
			partitionAttributes = partitionResult.getAttributes();
			necInfoWithCollection = new NestedEquivalenceClassesInfo<Collection<NestedEquivalenceClass<EquivalenceClass>>>(
											partitionResult.getRoughClasses(),
											partitionResult.isEmptyBoundaryClassSetTypeClass()
										);
		}else {
			Iterator<EquivalenceClass> equClassesIterator = equClasses.iterator();
			PlainNestedEquivalenceClass nestedEquClass = new PlainNestedEquivalenceClass(equClassesIterator.next());
			while (equClassesIterator.hasNext())	nestedEquClass.addClassItem(equClassesIterator.next());
			
			result = ClassSetType.POSITIVE.equals(nestedEquClass.getType())? nestedEquClass.getInstanceSize(): 0;
			partitionAttributes = new ArrayList<>(0);
			necInfoWithCollection = new NestedEquivalenceClassesInfo<Collection<NestedEquivalenceClass<EquivalenceClass>>>(
												Arrays.asList(nestedEquClass),
												!ClassSetType.BOUNDARY.equals(nestedEquClass.getType())
											);
		}
		return this;
	}

	/**
	 * Using dynamic attribute group process strategy with <code>Incremental Partition
	 * Strategy</code> to perform {@link NestedEquivalenceClass} based calculation.
	 * <p>
	 * After calculation, the following fields are accessible using <code>getter</code>
	 * for calculation result/feedback besides {@link #result}:
	 * <ul>
	 * 	<li><strong>{@link #partitionAttributes}</strong></li>
	 * 	<li><strong>{@link #necInfoWithCollection}</strong></li>
	 * </ul>
	 * 
	 * @see NestedEquivalenceClassBasedAlgorithm.IncrementalPartition
	 * @see PositiveRegionCalculation4IPREC#calculate(Collection, IntegerIterator, Object...)
	 * 
	 * @param attributes
	 * 		Attributes of {@link Instance}.
	 * @param nestedEquClasses
	 * 		{@link NestedEquivalenceClass} {@link Collection}.
	 * @param args
	 * 		No extra arguments needed.
	 * @return int value as positive region.
	 */
	@Override
	public NestedEquivalenceClassBasedIncrementalPartitionCalculation<Integer> incrementalCalculate(
			IntegerIterator attributes, Collection<? extends NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses,
			Object...args
	) {
		PartitionResult<Collection<Integer>, Collection<NestedEquivalenceClass<EquivalenceClass>>>
			partitionResult = inTurnIncrementalPartition(nestedEquClasses, attributes, true);
		
		countCalculate(partitionResult.getAttributes().size());
		
		result = partitionResult.getPositive();
		partitionAttributes = partitionResult.getAttributes();
		necInfoWithCollection = new NestedEquivalenceClassesInfo<>(
										partitionResult.getRoughClasses(),
										partitionResult.isEmptyBoundaryClassSetTypeClass()
									);
		return this;
	}
	
	
	/**
	 * Partition the given {@link EquivalenceClass}es incrementally use 1 attribute at
	 * each partition.
	 * 
	 * @see #inTurnIncrementalPartition(Collection, IntegerIterator, boolean)
	 * 
	 * @param equClasses
	 * 		{@link EquivalenceClass} {@link Collection}.
	 * @param attributes
	 * 		Attributes to be used in partition in order.
	 * @return {@link PartitionResult} instance, results includes:
	 * 		<ul>
	 * 			<li></lu><code>positive region number</code>,</li>
	 * 			<li><code>whether positive region has reached global one</code>,</li>
	 * 			<li><code>attributes used in actual partition</code>,</li>
	 * 			<li><code>partitioned {@link NestedEquivalenceClass} {@link Collection}</code></li>
	 * 		</ul>
	 */
	public static PartitionResult<Collection<Integer>, Collection<NestedEquivalenceClass<EquivalenceClass>>>
		inTurnPartition(
			Collection<EquivalenceClass> equClasses, IntegerIterator attributes
	) {
		// Initiate a list to contain used attributes.
		Collection<Integer> usedAttributes = new LinkedList<>();
		// Equivalent Classes -- attributes[x,y] --> Rough EquivalenceClasses
		//	(Using the 1st attribute in attributes.)
		attributes.reset();
		int targetAttribute = attributes.next();
		StatisticResult<Map<Integer, NestedEquivalenceClass<EquivalenceClass>>> statisticResult =
				calculateEquivalenceClassPosPartition(
					equClasses, 
					targetAttribute
				);
		int pos = statisticResult.getPositiveRegion();
		usedAttributes.add(targetAttribute);
		
		// if empty 0-NEC or all attributes have been used in partitions
		if (statisticResult.isEmptyBoundaryClassSet() || usedAttributes.size()==attributes.size()) {
			// calculation finished, return.
			return new PartitionResult<Collection<Integer>, Collection<NestedEquivalenceClass<EquivalenceClass>>>(
					pos, 
					statisticResult.isEmptyBoundaryClassSet(),
					usedAttributes,
					statisticResult.getRecord().values()
				);
		// else
		}else {
			// do incremental partitioning using the attributes
			PartitionResult<Collection<Integer>, Collection<NestedEquivalenceClass<EquivalenceClass>>>
				sub = inTurnIncrementalPartition(
						statisticResult.getRecord().values(), 
						attributes, 
						false				// doesn't require resetting, continue to partition.
					);
			// Set sub.attribute(used in partitioning)
			if (usedAttributes.size()>sub.getAttributes().size()) {
				usedAttributes.addAll(sub.getAttributes());
				sub.setAttributes(usedAttributes);
			}else {
				sub.getAttributes().addAll(usedAttributes);
			}
			// Update positive region size
			sub.setPositive(sub.getPositive()+pos);
			return sub;
		}
	}
	
	/**
	 * Partition the given {@link NestedEquivalenceClass}es incrementally use 1 attribute
	 * at each partition.
	 * 
	 * @param nestedEquClasses
	 * 		{@link NestedEquivalenceClass} {@link Collection}.
	 * @param attributes
	 * 		Attributes to be used in partition in order.
	 * @param resetAttributesB4Executions
	 * 		True to call {@link IntegerIterator#reset()} of <code>attributes<code> to
	 * 		reset before partitions.
	 * @return {@link PartitionResult} instance, results includes:
	 * 		<ul>
	 * 			<li><code>positive region number</code>,</li>
	 * 			<li><code>whether positive region has reached global one</code>,</li>
	 * 			<li><code>attributes used in actual partition</code>,</li>
	 * 			<li><code>partitioned {@link NestedEquivalenceClass} {@link Collection}</code></li>
	 * 		</ul>
	 */
	public static PartitionResult<Collection<Integer>, Collection<NestedEquivalenceClass<EquivalenceClass>>>
		inTurnIncrementalPartition(
			Collection<? extends NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses,
			IntegerIterator attributes, boolean resetAttributesB4Executions
	) {
		if (attributes.size()==1) {
			PartitionResult<Integer, Collection<NestedEquivalenceClass<EquivalenceClass>>>
				result = inTurnIncrementalPartition(nestedEquClasses, attributes.reset().next());
			return new PartitionResult<>(
						result.getPositive(), result.isEmptyBoundaryClassSetTypeClass(),
						Arrays.asList(result.getAttributes()), result.getRoughClasses()
					);
		}else {
			int pos = 0, targetAttribute;
			Collection<Integer> usedAttributes = new LinkedList<>();
			StatisticResult<Map<Integer, NestedEquivalenceClass<EquivalenceClass>>> statisticResult;
			// Rough EquivalenceClasses incremental partition.
			if (resetAttributesB4Executions)	attributes.reset();
			boolean loopBoundary;
			Collection<NestedEquivalenceClass<EquivalenceClass>> roughClasses;
			Collection<? extends NestedEquivalenceClass<EquivalenceClass>> roughClassesInLine = nestedEquClasses;
			do {
				loopBoundary = false;
				roughClasses = new LinkedList<>();
				targetAttribute = attributes.next();
				for (NestedEquivalenceClass<EquivalenceClass> roughClass: roughClassesInLine) {
					switch(roughClass.getType()) {
						case BOUNDARY: 
							if (!loopBoundary)	loopBoundary=true;
							// Partition.
							statisticResult = 
								calculateEquivalenceClassPosPartition(
									roughClass.getEquClasses().values(), 
									targetAttribute
								);
							// Calculate positive region.
							pos += statisticResult.getPositiveRegion();
							// if 0-REC is not empty after partition.
							if (!statisticResult.isEmptyBoundaryClassSet())
								roughClasses.addAll(statisticResult.getRecord().values());
							break;
						default:
							break;
					}
				}
				// Record used attributes.
				if (loopBoundary)	usedAttributes.add(targetAttribute);
				else				break;
				roughClassesInLine = roughClasses;
				// If no 0-REC left, partition finish.
			} while (!roughClasses.isEmpty() && attributes.hasNext());
			return new PartitionResult<Collection<Integer>, Collection<NestedEquivalenceClass<EquivalenceClass>>>(
					pos, 
					roughClasses.isEmpty(),
					usedAttributes,
					roughClasses
				);
		}
	}
	
	/**
	 * Partition the given {@link NestedEquivalenceClass}es incrementally use 1 attribute
	 * at each partition.
	 * 
	 * @param nestedEquClasses
	 * 		{@link NestedEquivalenceClass} {@link Collection}.
	 * @param attribute
	 * 		Attribute to be used in partition in order.
	 * @return {@link PartitionResult} instance, results includes:
	 * 		<ul>
	 * 			<li><code>positive region number</code>,</li>
	 * 			<li><code>whether positive region has reached global one</code>,</li>
	 * 			<li><code>attributes used in actual partition</code>,</li>
	 * 			<li><code>partitioned {@link NestedEquivalenceClass} {@link Collection}</code></li>
	 * 		</ul>
	 */
	public static PartitionResult<Integer, Collection<NestedEquivalenceClass<EquivalenceClass>>>
		inTurnIncrementalPartition(
			Collection<? extends NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses,
			int attribute
	) {
		int pos = 0;
		StatisticResult<Map<Integer, NestedEquivalenceClass<EquivalenceClass>>> statisticResult;
		// Rough EquivalenceClasses incremental partition.
		Collection<NestedEquivalenceClass<EquivalenceClass>> roughClasses = new LinkedList<>();
		for (NestedEquivalenceClass<EquivalenceClass> roughClass: nestedEquClasses) {
			switch(roughClass.getType()) {
				case BOUNDARY: 
					// Partition.
					statisticResult = 
						calculateEquivalenceClassPosPartition(
							roughClass.getEquClasses().values(), 
							attribute
						);
					// Calculate positive region.
					pos += statisticResult.getPositiveRegion();
					// if 0-REC is not empty after partition.
					if (!statisticResult.isEmptyBoundaryClassSet())
						roughClasses.addAll(statisticResult.getRecord().values());
					break;
				default:
					break;
			}
		}//*/
		return new PartitionResult<Integer, Collection<NestedEquivalenceClass<EquivalenceClass>>>(
				pos, 
				roughClasses.isEmpty(),
				attribute,
				roughClasses
			);
	}
	
	
	/**
	 * Calculate positive region of {@link EquivalenceClass} {@link Collection} for 1
	 * extra attribute partition.
	 * <p>
	 * This function initiates <code>HashMap</code> with the size of
	 * {@link EquivalenceClass}</strong> during computation.
	 * 
	 * @param equClasses
	 * 		A {@link Collection} of {@link EquivalenceClass} {@link Collection}s as Rough
	 * 		Equivalence Classes.
	 * @param attribute
	 * 		Extra attribute involved in the partition.
	 * @return {@link StatisticResult} instance with current positive region number and
	 * 		record Collection as nested Equivalent Classes.
	 */
	public static StatisticResult<Map<Integer, NestedEquivalenceClass<EquivalenceClass>>>
		calculateEquivalenceClassPosPartition(
			Collection<EquivalenceClass> equClasses, int attribute
	) {
		Map<Integer, NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses = new HashMap<>(equClasses.size());
		
		int pos=0, key;
		boolean emptyBoundary = true;
		NestedEquivalenceClass<EquivalenceClass> record;
		for (EquivalenceClass eClass: equClasses) {
			// key = attrValues(e[i])
			key = eClass.getAttributeValueAt(attribute-1);
			record = nestedEquClasses.get(key);
			// if doesn't contains key.
			if (record==null) {
				nestedEquClasses.put(key, record=new PlainNestedEquivalenceClass(eClass, equClasses.size()));
				if (eClass.sortable())	pos += eClass.getItemSize();
			}else {
				switch (record.getType()) {
					case POSITIVE:
						if (eClass.getDecisionValue() != record.getDec()) {
							if (emptyBoundary)	emptyBoundary = false;
							record.setType(ClassSetType.BOUNDARY);
							record.setDec(-1);
							pos -= record.getInstanceSize();
						}else {
							pos += eClass.getItemSize();
						}
						break;
					case NEGATIVE:
						if (eClass.sortable()) {
							if (emptyBoundary)	emptyBoundary = false;
							record.setType(ClassSetType.BOUNDARY);
						}
						break;
					default: 
						break;
				}
				record.addClassItem(eClass);
			}
		}
		// return
		return new StatisticResult<>(pos, emptyBoundary, nestedEquClasses);
	}
	
	/* -------------------------------------------------------------------------------------------------- */
	
	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}

}