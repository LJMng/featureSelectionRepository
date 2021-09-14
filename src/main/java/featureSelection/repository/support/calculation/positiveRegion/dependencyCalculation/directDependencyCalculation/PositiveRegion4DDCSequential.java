package featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.directDependencyCalculation;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.searchStrategy.SequentialSearchStrategy;
import featureSelection.repository.entity.alg.directDependencyCalculation.GridRecord;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.directDependencyCalculation.FeatureImportance4DirectDependencyCalculation;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.DefaultDependencyCalculation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class PositiveRegion4DDCSequential
	extends DefaultDependencyCalculation
	implements SequentialSearchStrategy,
				FeatureImportance4DirectDependencyCalculation<Integer>
{
	private int dependency;
	@Override
	public Integer getResult() {
		return dependency;
	}
	
	@Override
	public FeatureImportance4DirectDependencyCalculation<Integer> calculate(
			Collection<Instance> instances, IntegerIterator attribute, Object... args
	) {
		// Count the current calculation
		countCalculate(attribute.size());
		// Calculate
		dependency = instances==null || instances.isEmpty()?
						0: dependency(instances, attribute);
		return this;
	}
	
	private int dependency(Collection<Instance> instances, IntegerIterator attributes) {
		Collection<GridRecord> grid = Support.updateGrid(instances, attributes);
		// dep=0
		int dep = 0;
		// for i=1 to totalRecoredsInGrid
		for (GridRecord gridRecord : grid) {
			// if grid(i, classStatus)=0
			if (gridRecord.unique()) {
				// dep = dep + grid(i,count)
				dep += gridRecord.getInstanceCount();
			}
		}
		// return dep / TotalRecoreds
		return dep;
	}
	
	public static class Support {
		/**
		 * Generate a {@link List} of {@link GridRecord} based on the given {@link Instance}
		 * {@link List}.
		 * 
		 * @param instances
		 * 		A {@link List} of {@link Instance}.
		 * @param attributes
		 * 		Attributes of {@link Instance}.
		 * @return A {@link List} of {@link GridRecord} 
		 */
		public static Collection<GridRecord> updateGrid(
				Collection<Instance> instances, IntegerIterator attributes
		) {
			List<GridRecord> grid = new ArrayList<>();
			// InsertGrid(X1)
			Iterator<Instance> insIterator = instances.iterator();
			insertInGrid(grid, insIterator.next(), attributes);
			// for i=2 to totalUniverseSize
			int index;
			Instance insPointer;
			while (insIterator.hasNext()) {
				insPointer = insIterator.next();
				// ifAlreadyExistsInGrid(Xi)
				if (alreadyExistInGrid(grid, insPointer, attributes)) {
					// index = FindIndexInGrid(Xi)
					index = findIndexInGrid(grid, insPointer, attributes);
					// grid(index, count) +=1
					grid.get(index)
						.addInstanceCount();
					// if decisionClassMatched(index, i)=false
					if (!decisionClassMatched(grid, index, insPointer))
						updateUniquenessStatus(grid, index);
				// else
				}else {
					// InsertInGrid(Xi)
					insertInGrid(grid, insPointer, attributes);
				}
			}
			return grid;
		}
		
		/**
		 *  Get if the given {@link Instance} already exists in the {@link GridRecord}
		 * 	{@link List}, i.e. <code>grid</code>.
		 * 
		 * @param grid
		 * 		A {@link List} of {@link GridRecord} as <code>grid</code>.
		 * @param instance
		 * 		A {@link List} of {@link Instance}.
		 * @param attributes
		 * 		Attributes of {@link Instance}.
		 * @return <code>true</code> if the given {@link Instance} exists by given <code>attributes</code>.
		 */
		public static boolean alreadyExistInGrid(
				Collection<GridRecord> grid, Instance instance, IntegerIterator attributes
		) {
			// for i=1 to totalRecordsInGrid
			GridLoop:
			for (GridRecord gridRecord : grid) {
				// for j=1 to totalAttributeInX
				attributes.reset();
				for (int j=0; j<attributes.size(); j++) {
					// if grid(i, j) <> X[j], i.e. grid(i, j) != universe.attributes.value[j]
					if (Integer.compare(gridRecord.getConditionalAttributes().key()[j], 
										instance.getAttributeValue(attributes.next())) != 0
					) {
						// continue to check next grid record.
						continue GridLoop;
					}
				}
				return true;
			}
			return false;
		}
		
		/**
		 * Find the {@link Instance} index that attribute values matched in <code>grid</code>.
		 * 
		 * @param grid
		 * 		A {@link List} of {@link GridRecord}.
		 * @param instance
		 * 		An {@link Instance}.
		 * @param attributes
		 * 		Attributes of {@link Instance}.
		 * @return An {@link int} value as index in grid. (Starts from 0, -1 as no matched)
		 */
		public static int findIndexInGrid(
				Collection<GridRecord> grid, Instance instance, IntegerIterator attributes
		) {
			int[] attrValue;
			// for i=1 to totalRecordsInGrid. 顺序查找Xi.
			int i=0;
			GridLoop:
			for (GridRecord gridRecord: grid) {
				// init recordMatched.
				// for j=1 to totalAttributeInX
				attrValue = gridRecord.getConditionalAttributes().key();
				attributes.reset();
				for (int j=0; j<attrValue.length; j++) {
					// if grid(i, j) != Xi[j]
					if (Integer.compare(
							attrValue[j],
							instance.getAttributeValue(attributes.next())
						)!=0
					) {
						// recordMatched = false
						i++;
						continue GridLoop;
					}
				}
				// if recordMatched is true. Return i.
				return i;
			}
			// return true.
			return -1;
		}

		/**
		 * Get if {@link Instance}'s decision value equals to the one in <code>grid</code>
		 * 	[<code>index</code>].
		 * 
		 * @param grid
		 * 		A {@link List} of {@link GridRecord} as grid.
		 * @param index
		 * 		The index of {@link GridRecord}.
		 * @param instance
		 * 		An {@link Instance}.
		 * @return true if the 2 values matches.
		 */
		public static boolean decisionClassMatched(
				List<GridRecord> grid, int index, Instance instance
		) {
			return Integer.compare(
					grid.get(index).getDecisionClass(),
					instance.getAttributeValue(0)
			)==0;
		}

		/**
		 * Update the unique status of <code>grid</code>[<code>index</code>}] from unique to
		 * non-unique.
		 * 
		 * @param grid
		 * 		A {@link List} of {@link GridRecord} as grid.
		 * @param index
		 * 		The index of {@link GridRecord}.
		 */
		public static void updateUniquenessStatus(List<GridRecord> grid, int index) {
			grid.get(index).setClassStatus(false);
		}
		
		/**
		 * Insert a new record into <code>grid</code>.
		 *
		 * @param grid
		 * 		A {@link List} of {@link GridRecord} as <code>grid</code>.
		 * @param instance
		 * 		A {@link Instance} to be inserted.
		 * @param attributes
		 * 		Attributes of {@link Instance}.
		 */
		public static void insertInGrid(
				Collection<GridRecord> grid, Instance instance, IntegerIterator attributes
		) {
			// for j=1 to totalAttributeInX, i.e. C
			int[] attributeValues = new int[attributes.size()];
			attributes.reset();
			for (int j=0; j<attributes.size(); j++) {
				//	Grid(NextRow,j) = Xi(j), 把Xi中的属性放入Grid对应的列中
				attributeValues[j] = instance.getAttributeValue(attributes.next());
			}
			// Grid(NextRow, D)=Di; Grid(NextRow, count)=1; Grid(NextRow, cons)=0;
			grid.add(new GridRecord(attributeValues, instance.getAttributeValue(0)));
		}
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}
}