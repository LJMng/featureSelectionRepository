package featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.heuristicDependencyCalculation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.searchStrategy.HashSearchStrategy;
import featureSelection.repository.entity.alg.heuristicDependencyCalculation.HashMapValue;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.heuristicDependencyCalculation.FeatureImportance4HeuristicDependencyCalculation;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.DefaultDependencyCalculation;

public class PositiveRegion4HDCHash
	extends DefaultDependencyCalculation
	implements HashSearchStrategy,
		FeatureImportance4HeuristicDependencyCalculation<Integer>
{
	private int dependency;
	@Override
	public Integer getResult() {
		return dependency;
	}
	
	@Override
	public FeatureImportance4HeuristicDependencyCalculation<Integer> calculate(
			Collection<Instance> instances, Collection<Integer> desisionValues,
			IntegerIterator attribute, Object... args
	) {
		// Count the current calculation
		countCalculate(attribute.size());
		// Calculate
		dependency = instances==null || instances.isEmpty()?
						0: dependency(instances, desisionValues, attribute);
		return this;
	}
	
	private int dependency(
			Collection<Instance> instances, Collection<Integer> desisionValues,
			IntegerIterator attributes
	) {
		// TotalCRecords = 0
		int totalCRecords = 0;
		// Go through DecisionClasses
		for (int dValue : desisionValues)
			// TotalCRecords += calculateConsistentRecords(DecisionClassValue)
			totalCRecords += calculateConsistentRecords(instances, attributes, dValue);
		return totalCRecords;
	}
	
	/**
	 * Calculate the consistent records.
	 * 
	 * @param instances
	 * 		A {@link Collection} of {@link Instance}.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @param dValue
	 * 		Decision value the {@link Instance}.
	 * @return The number of consistent records.
	 */
	public static int calculateConsistentRecords(
			Collection<Instance> instances, IntegerIterator attributes, int dValue
	) {
		// RecordsCount = 0
		int recordsCount = 0;
		// Initiate a HashMap
		Map<IntArrayKey, HashMapValue> H = new HashMap<>();
		// Go through xj in U
		int[] attrValue;
		IntArrayKey key;
		HashMapValue hashMapValue;
		for (Instance xj : instances) {
			// if DecisionClass(xj)==dValue
			if (Integer.compare(xj.getAttributeValue(0), dValue)==0) {
				// key = P(xj)
				attributes.reset();
				attrValue = new int[attributes.size()];
				for (int i=0; i<attrValue.length; i++)
					attrValue[i] = xj.getAttributeValue(attributes.next());
				key = new IntArrayKey(attrValue);
						
				hashMapValue = H.get(key);
				if (hashMapValue==null) {
					// if no such key, create hk, hk.count=1, hk.cnst=true
					hashMapValue = new HashMapValue(true);
					H.put(key, hashMapValue);
				}else {
					// if key exists in H, hk.count++
					hashMapValue.add();
				}
			}
		}
		// Go through xj in U
		for (Instance xj : instances) {
			// if D(xi)!=DValue
			if (Integer.compare(xj.getAttributeValue(0), dValue)!=0) {
				// key = P(xj)
				attrValue = new int[attributes.size()];
				attributes.reset();
				for (int i=0; i<attrValue.length; i++)	attrValue[i] = xj.getAttributeValue(attributes.next());
				key = new IntArrayKey(attrValue);
				// if key exists in H as hk
				hashMapValue = H.get(key);
				if (hashMapValue!=null) {
					// hk.cnst = false
					hashMapValue.setCnst(false);
				}
			}
		}
		// Go through hi in H
		for (HashMapValue hi : H.values())
			// if hi.cnst = true, RecoredsCount++
			if (hi.cnst())	recordsCount+=hi.count();
		return recordsCount;
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}
}