package featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.heuristicDependencyCalculation;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.searchStrategy.SequentialSearchStrategy;
import featureSelection.repository.entity.alg.heuristicDependencyCalculation.GridRecord;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.heuristicDependencyCalculation.FeatureImportance4HeuristicDependencyCalculation;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.DefaultDependencyCalculation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PositiveRegion4HDCSequential
	extends DefaultDependencyCalculation
	implements SequentialSearchStrategy,
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
		dependency = instances ==null || instances.isEmpty()? 0: dependency(instances, desisionValues, attribute);
		return this;
	}
	
	private int dependency(Collection<Instance> universes, Collection<Integer> desisionValues,
								IntegerIterator attributes
	) {
		// 1 TotalCRecords = 0
		int totalCRecords = 0;
		// 2 Go through DecisionClasses
		for (int dValue : desisionValues)
			// 2.1 TotalCRecords += calculateConsistentRecords(DecisionClassValue)
			totalCRecords += calculateConsistentRecords(universes, attributes, dValue);
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
		// Initiate a grid
		List<GridRecord> grid = new LinkedList<>();
		// Go through xj in U
		int[] attrValues;
		GridRecord gridRecord;
		for (Instance xj : instances) {
			// if DecisionClass(xj)==dValue
			if (Integer.compare(xj.getAttributeValue(0), dValue)==0) {
				// insertInGrid.
				attrValues = new int[attributes.size()];
				attributes.reset();
				for (int i=0; i<attributes.size(); i++)	attrValues[i] = xj.getAttributeValue(attributes.next());
				gridRecord = new GridRecord(attrValues, recordsCount);
				grid.add(gridRecord);
			}
		}
		// Go through xj in U
		for (Instance xj : instances) {
			// if D(xi)!=DValue
			if (Integer.compare(xj.getAttributeValue(0), dValue) !=0) {
				// recordExistsIngrid
				GridLoop:
				for (GridRecord each : grid) {
					attrValues = each.getConditionalAttributes();
					attributes.reset();
					for (int i=0; i<attributes.size(); i++) {
						if (Integer.compare(attrValues[i], xj.getAttributeValue(attributes.next()))!=0)
							continue GridLoop;
					}
					each.setClassStatus(false);
				}
			}
		}
		// Go through hi in H
		for (GridRecord each : grid) {
			// if hi.cnst = true, RecoredsCount++
			if (each.cnst())	recordsCount++;
		}
		return recordsCount;
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}
}