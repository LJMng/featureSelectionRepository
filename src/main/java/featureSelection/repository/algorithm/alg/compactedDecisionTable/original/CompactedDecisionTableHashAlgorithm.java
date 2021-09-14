package featureSelection.repository.algorithm.alg.compactedDecisionTable.original;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.compactedDecisionTable.impl.original.MostSignificantAttributeResult;
import featureSelection.repository.entity.alg.compactedDecisionTable.impl.original.compactedTable.InstanceBasedCompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.impl.original.equivalenceClass.EquivalenceClassCompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.interf.DecisionNumber;
import featureSelection.repository.support.calculation.alg.CompactedDecisionTableCalculation;

/**
 * Algorithm repository of CompactedDecisionTable(AttributeReduction), which bases on the paper
 * <a href="https://www.sciencedirect.com/science/article/abs/pii/S0950705115002312">
 * "Compacted decision tables based attribute reduction"</a> by Wei Wei, Junhong Wang, Jiye Liang, 
 * Xin Mi, Chuangyin Dang.
 * <p>
 * Original version, implemented based on the paper completely.
 * 
 * @author Benjamin_L
 */
public class CompactedDecisionTableHashAlgorithm {
	
	public static class Basic {
		/**
		 * Initiate a decision table.
		 * 
		 * @param instances
		 * 		{@link Instance} {@link Collection}.
		 * @return A key value {@link Map} in the format of {Decision Value : number}.
		 */
		public static Map<Integer, Integer> initDecisionTable(Collection<Instance> instances){
			// Initiate dValue=empty, in the structure of {value: num}
			Map<Integer, Integer> decisionTable = new HashMap<>();
			// Initiate an entry of dValue: dv.
			// dv.value = d value of U[1].
			// Loop over decision values of instances.
			int dValue;
			Instance instance;
			Iterator<Instance> iterator = instances.iterator();
			while (iterator.hasNext()) {
				instance = iterator.next();
				// Get the decision value.
				dValue = instance.getAttributeValue(0);
				// if decisionTable doesn't contains dValue, new a sub item.
				if (!decisionTable.containsKey(dValue)){
					decisionTable.put(dValue, 0);
				}
			}
			return decisionTable;
		}

		/**
		 * Check the consistency of the given {@link DecisionNumber}.
		 * 
		 * @param decNum
		 * 		Decision table record in {@link DecisionNumber}.
		 * @return -1 if it is <code>negative region</code>. / size of <code>positive region</code>
		 * 		if it is consistent.
		 */
		public static int checkConsistency(DecisionNumber decNum) {
			// nonZero = 0
			boolean nonZero = false;
			// dsize = 0
			int dSize = 0;
			// Loop over decisionTable
			int num;
			IntegerIterator dValueIterator = decNum.numberValues().reset();
			while (dValueIterator.hasNext()) {
				num = dValueIterator.next();
				if (num != 0) {
					// if d.num!=0
					//	nonZero++
					//	dsize = dsize + |d|
					//	if (nonZero>1)
					//		return false, dSize = 0
					if (nonZero)	return -1;
					else			nonZero = true;
					dSize = num;
				}
			}
			return dSize;
		}
		
		/**
		 * Generate a compacted table of <code>instances</code> induced by the given
		 * <code>attributes</code>.
		 * 
		 * @param <DN>
		 * 		Type of implemented {@link DecisionNumber} as Decision Number Info.
		 * @param decNumInfo
		 * 		{@link Class} of decision number.
		 * @param instances
		 * 		{@link Instance} {@link Collection}.
		 * @param attributes
		 * 		Attributes of {@link Instance}.
		 * @return {@link InstanceBasedCompactedTableRecord}s.
		 * @throws IllegalAccessException if exceptions occur when creating instance of
		 * 		{@link DecisionNumber}.
		 * @throws InstantiationException if exceptions occur when creating instance of
		 * 		{@link DecisionNumber}.
		 */
		public static <DN extends DecisionNumber> Collection<InstanceBasedCompactedTableRecord<DN>>
			instance2CompactedTable(
					Class<DN> decNumInfo, Collection<Instance> instances, int...attributes
		) throws InstantiationException, IllegalAccessException {
			// Initiate a HashMap as a compacted table : H
			Map<IntArrayKey, InstanceBasedCompactedTableRecord<DN>> compactedTable = new HashMap<>();
			// Loop over instances
			int dValue;
			int[] value;
			IntArrayKey key;
			DN decisionNumbers;
			InstanceBasedCompactedTableRecord<DN> tableRecord;
			IntegerArrayIterator attributesIterator = new IntegerArrayIterator((attributes));
			for (Instance ins : instances) {
				// Generate key
				value = Instance.attributeValuesOf(ins, attributesIterator);
				key = new IntArrayKey(value);
				// Search key in the compacted table
				tableRecord = compactedTable.get(key);
				dValue = ins.getAttributeValue(0);
				if (tableRecord==null) {
					// if compacted table doesn't contain key, create one
					decisionNumbers = decNumInfo.newInstance();
					decisionNumbers.setDecisionNumber(dValue, 1);
					compactedTable.put(key, new InstanceBasedCompactedTableRecord<>(ins, decisionNumbers));
				}else {
					// if compacted table contains key, num++
					tableRecord.getDecisionNumbers()
								.setDecisionNumber(
									dValue,
									1+ tableRecord.getDecisionNumbers().getNumberOfDecision(dValue)
								);
				}
			}
			return compactedTable.values();
		}
		
		/**
		 * Get the positive region and negative region of a global compacted table.
		 * 
		 * @param <DN>
		 * 		Type of implemented {@link DecisionNumber} as Decision Number Info.
		 * @param records
		 * 		{@link InstanceBasedCompactedTableRecord}s as a global compacted table.
		 * @return An {@link Instance} {@link Set} <code>array</code> with <code>element[0]</code>:
		 * 		Positive region and <code>element[1]</code> Negative region.
		 */
		@SuppressWarnings("unchecked")
		public static <DN extends DecisionNumber> Set<InstanceBasedCompactedTableRecord<DN>>[]
			globalPositiveNNegativeRegion(Collection<InstanceBasedCompactedTableRecord<DN>> records)
		{
			Set<InstanceBasedCompactedTableRecord<DN>> pos = new HashSet<>(records.size()),
														neg = new HashSet<>(records.size());
			for (InstanceBasedCompactedTableRecord<DN> record : records ) {
				if (checkConsistency(record.getDecisionNumbers())>= 0) {
					// positive region
					pos.add(record);
				}else {
					// negative region
					neg.add(record);
				}
			}
			return new Set[] {pos, neg};
		}
		
		/**
		 * Merge 2 Decision Compacted Tables in {@link Map}s in the format of { dValue: number }.
		 * 
		 * @param <DN>
		 * 		Type of implemented {@link DecisionNumber} as Decision Number Info.
		 * @param mainDecisionInfo
		 * 		The decision compacted table to absorb and contain both values from 2 records.
		 * @param toBeMergedRecord
		 * 		A decision compacted table provides value to be merged and absorbed.
		 */
		public static <DN extends DecisionNumber> void mergeCompactedTableRecord(
				EquivalenceClassCompactedTableRecord<DN> mainDecisionInfo,
				InstanceBasedCompactedTableRecord<DN> toBeMergedRecord
		){
			Integer number, key;
			// Loop over toBeMergedValues and merge into mainDecisionInfo.
			IntegerIterator toBeMergedValues =
					toBeMergedRecord.getDecisionNumbers()
									.decisionValues()
									.reset();
			while (toBeMergedValues.hasNext()) {
				key = toBeMergedValues.next();
				number = mainDecisionInfo.getDecisionNumbers()
										.getNumberOfDecision(key);
				number += toBeMergedRecord.getDecisionNumbers()
										.getNumberOfDecision(key);
				mainDecisionInfo.getDecisionNumbers()
								.setDecisionNumber(key, number);
			}
			// merge table records
			mainDecisionInfo.getEquivalenceRecords()
							.add(toBeMergedRecord);
		}

		/**
		 * Obtain an equivalence class compacted table induced by the given <code>attributes</code>
		 * on <code>CompactedTable</code>.
		 * 
		 * @param <DN>
		 * 		Type of implemented {@link DecisionNumber} as Decision Number Info.
		 * @param compactedTableRecords
		 * 		An {@link InstanceBasedCompactedTableRecord} {@link Collection}.
		 * @param attributes
		 * 		Attributes of {@link Instance}.
		 * @return A {@link Map} with attribute value {@link IntArrayKey} keys and
		 * 		{@link InstanceBasedCompactedTableRecord} values.
		 */
		@SuppressWarnings("unchecked")
		public static <DN extends DecisionNumber> Collection<EquivalenceClassCompactedTableRecord<DN>>
			equivalenceClassOfCompactedTable(
					Collection<InstanceBasedCompactedTableRecord<DN>> compactedTableRecords,
					IntegerIterator attributes
		){
			// If attributes is empty, all records are considered equivalent to each other.
			if (attributes==null || attributes.size()==0) {
				InstanceBasedCompactedTableRecord<DN> compactedTableRecord;
				Set<InstanceBasedCompactedTableRecord<DN>> compactedTableRecordSet =
						new HashSet<>();
				Iterator<InstanceBasedCompactedTableRecord<DN>> recordsIterator =
						compactedTableRecords.iterator();
				compactedTableRecord = recordsIterator.next();
				compactedTableRecordSet.add(compactedTableRecord);
				EquivalenceClassCompactedTableRecord<DN> equRecord =
						new EquivalenceClassCompactedTableRecord<>(
								(DN) compactedTableRecord.getDecisionNumbers().clone(), 
								compactedTableRecordSet
							);
				while (recordsIterator.hasNext()){
					mergeCompactedTableRecord(equRecord, recordsIterator.next());
				}

				Collection<EquivalenceClassCompactedTableRecord<DN>> result = new HashSet<>();
				result.add(equRecord);
				return result;
			}else {
				// Initiate a compacted table to contain equivalence classes
				Map<IntArrayKey, EquivalenceClassCompactedTableRecord<DN>> equClassTable = new HashMap<>();
				//  Loop over Compacted Table records
				int[] value;
				IntArrayKey key;
				EquivalenceClassCompactedTableRecord<DN> decisionInfo;
				Collection<InstanceBasedCompactedTableRecord<DN>> equivalentRecords;
				for (InstanceBasedCompactedTableRecord<DN> record : compactedTableRecords) {
					// key = P(x)
					value = Instance.attributeValuesOf(record.getInsRepresentitive(), attributes);
					key = new IntArrayKey(value);
					
					decisionInfo = equClassTable.get(key);
					if (decisionInfo==null) {
						// if key not in equivalence class compacted table, create one with P(x).
						equivalentRecords = new HashSet<>();
						equivalentRecords.add(record);
						equClassTable.put(key, 
											new EquivalenceClassCompactedTableRecord<>(
												(DN) record.getDecisionNumbers().clone(), 
												equivalentRecords
											)
										);
					}else {
						// else key exists in equivalence class compacted table, merge.
						mergeCompactedTableRecord(decisionInfo, record);
					}
				}
				// Go through h in H
				return equClassTable.values();
			}
		}
	}

	/**
	 * Get the core of the given {@link InstanceBasedCompactedTableRecord}s.
	 * 
	 * @param <Sig>
	 * 		{@link Number} implemented type as the value of Significance.
	 * @param <DN>
	 * 		Type of implemented {@link DecisionNumber} as Decision Number Info.
	 * @param globalCompactedTableRecords
	 * 		An {@link InstanceBasedCompactedTableRecord} {@link Collection} compacted using
	 * 		global(all) {@link Instance}s of the original decision table.
	 * @param insSize
	 * 		The number of {@link Instance}s in the original decision table.
	 * @param calculation
	 * 		Implemented {@link CompactedDecisionTableCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param globalSig
	 * 		The significance of global attributes.
	 * @param attributes
	 * 		Attributes of {@link Instance}.
	 * @return A {@link List} of {@link Integer} as core.
	 * @throws IllegalArgumentException if exceptions occur when constructing calculations of
	 * 		{@link CompactedDecisionTableCalculation}.
	 * @throws SecurityException if exceptions occur when constructing calculations of
	 * 		{@link CompactedDecisionTableCalculation}.
	 */
	public static <Sig extends Number, DN extends DecisionNumber> Set<Integer> core(
			Collection<InstanceBasedCompactedTableRecord<DN>> globalCompactedTableRecords,
			int insSize, CompactedDecisionTableCalculation<Sig> calculation, Sig sigDeviation,
			Sig globalSig, IntegerIterator attributes
	) throws IllegalArgumentException, SecurityException {
		// core = {}
		Set<Integer> core = new HashSet<>(attributes.size());
		// Loop over all attributes
		Sig innerSig;
		int[] examAttributes = new int[attributes.size()-1];
		attributes.skip(1);
		for (int i=0; i<examAttributes.length; i++)	examAttributes[i] = attributes.next();

		int examAttribute, i;
		attributes.reset();
		Collection<EquivalenceClassCompactedTableRecord<DN>> equTable;
		while (attributes.hasNext()) {
			i = attributes.currentIndex();
			examAttribute = attributes.next();
			// Calculate significance of C-{a}.
			equTable = Basic.equivalenceClassOfCompactedTable(
							globalCompactedTableRecords, 
							new IntegerArrayIterator(examAttributes)
						);
			innerSig = (Sig) calculation.calculate(equTable, examAttributes.length, insSize)
										.getResult();
			if (attributes.hasNext())	examAttributes[i] = examAttribute;
			// if a.innerSig!=C.sig
			//	core = core U {a}
			if (calculation.value1IsBetter(globalSig, innerSig, sigDeviation)) {
				core.add(examAttribute);
			}
		}
		return core;
	}

	/**
	 * Get the most significant attribute in {@link Instance} attributes beside red.
	 * 
	 * @param <Sig>
	 * 		{@link Number} implemented type as the value of Significance.
	 * @param <DN>
	 * 		Type of implemented {@link DecisionNumber} as Decision Number Info.
	 * @param tableRecords
	 * 		An {@link InstanceBasedCompactedTableRecord} {@link Collection}.
	 * @param insSize
	 * 		The number of {@link Instance}s in the original decision table.
	 * @param calculation
	 * 		Implemented {@link CompactedDecisionTableCalculation}.
	 * @param deviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @param red
	 * 		Current reduction.
	 * @param attributes
	 * 		Attributes of {@link Instance}.
	 * @return An int value as the most significant attribute.
	 * @throws IllegalArgumentException if exceptions occur when constructing calculations of
	 * 		{@link CompactedDecisionTableCalculation}.
	 * @throws SecurityException if exceptions occur when constructing calculations of
	 * 		{@link CompactedDecisionTableCalculation}.
	 */
	public static <Sig extends Number, DN extends DecisionNumber> MostSignificantAttributeResult<Sig, DN>
		mostSignificantAttribute(
				Collection<EquivalenceClassCompactedTableRecord<DN>> tableRecords,
				int insSize, CompactedDecisionTableCalculation<Sig> calculation, Sig deviation,
				Collection<Integer> red, IntegerIterator attributes
	) throws IllegalArgumentException, SecurityException {
		// sig=0; a*=0
		Sig maxSig = null, sig;
		int maxSigAttr = -1;
		// Loop over all attributes in C-red, i.e. attributes not in reduct.
		int attr;
		IntegerCollectionIterator examAttrIterator;
		Collection<EquivalenceClassCompactedTableRecord<DN>> equClassTable, sigEquClassTable = null;
		while (attributes.hasNext()) {
			attr = attributes.next();
			if (red.contains(attr))	continue;
			// Use red ∪ {a} to induce instances: compactedTable(CT, red U {a});
			red.add(attr);
			equClassTable = new HashSet<>();
			examAttrIterator = new IntegerCollectionIterator(red);
			for (EquivalenceClassCompactedTableRecord<DN> equRecords: tableRecords) {
				equClassTable.addAll(
					Basic.equivalenceClassOfCompactedTable(
							equRecords.getEquivalenceRecords(),
							examAttrIterator.reset()
					)
				);
			}
			// reset red for the next attribute to execute.
			red.remove(attr);
			// Call sig() to calculate Sig(red U {a}) to get a.outerSig
			sig = calculation.calculate(equClassTable, examAttrIterator.size(), insSize).getResult();
			// if a.outerSig > sig
			//	update the most significant attribute.
			if (maxSig==null || calculation.value1IsBetter(sig, maxSig, deviation)) {
				maxSig = sig;
				maxSigAttr = attr;
				sigEquClassTable = equClassTable;
			}
		}
		return new MostSignificantAttributeResult<Sig, DN>(maxSig, maxSigAttr, sigEquClassTable);
	}

	/**
	 * Inspect the given reduct and remove redundant ones.
	 * 
	 * @param <Sig>
	 * 		{@link Number} implemented type as the value of Significance.
	 * @param <DN>
	 * 		Type of implemented {@link DecisionNumber} as Decision Number Info.
	 * @param red
	 * 		Reduct {@link Collection}.
	 * @param globalSig
	 * 		The global positive region number.
	 * @param globalCompactedTableRecords
	 * 		An {@link InstanceBasedCompactedTableRecord} {@link Collection}.
	 * @param insSize
	 * 		The number of {@link Instance}s in the original decision table.
	 * @param calculation
	 * 		Implemented {@link CompactedDecisionTableCalculation}.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considering equal when
	 * 		the difference between two sigs is less than the given deviation value.
	 * @throws IllegalArgumentException if exceptions occur when constructing calculations of
	 * 		{@link CompactedDecisionTableCalculation}.
	 * @throws SecurityException if exceptions occur when constructing calculations of
	 * 		{@link CompactedDecisionTableCalculation}.
	 */
	public static <Sig extends Number, DN extends DecisionNumber> void inspection(
			Collection<Integer> red, Sig globalSig,
			Collection<InstanceBasedCompactedTableRecord<DN>> globalCompactedTableRecords,
			int insSize,
			CompactedDecisionTableCalculation<Sig> calculation, Sig sigDeviation
	) throws IllegalArgumentException, SecurityException {
		// Loop over attributes in reduct
		Integer[] redCopy = red.toArray(new Integer[red.size()]);
		Collection<EquivalenceClassCompactedTableRecord<DN>> equClasses;
		for (int attr: redCopy) {
			// calculate Sig(R-{a})
			red.remove(attr);
			equClasses = Basic.equivalenceClassOfCompactedTable(
								globalCompactedTableRecords, 
								new IntegerCollectionIterator(red)
							);
			// if (R-{a}.sig==C.sig)
			if (calculation.value1IsBetter(globalSig, calculation.calculate(equClasses, red.size(), insSize).getResult(), sigDeviation)) {
				// R = R-{a}: not redundant.
				red.add(attr);
			}
		}
	}
}