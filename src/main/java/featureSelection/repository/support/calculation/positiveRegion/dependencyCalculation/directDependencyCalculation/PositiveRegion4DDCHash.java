package featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.directDependencyCalculation;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.searchStrategy.HashSearchStrategy;
import featureSelection.repository.entity.alg.directDependencyCalculation.GridRecord;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.directDependencyCalculation.FeatureImportance4DirectDependencyCalculation;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.DefaultDependencyCalculation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PositiveRegion4DDCHash
	extends DefaultDependencyCalculation
	implements HashSearchStrategy,
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
		Collection<GridRecord> grid = Support.updateGrid(instances, attributes)
											.values();
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
		public static Map<IntArrayKey, GridRecord> updateGrid(
				Collection<Instance> instances, IntegerIterator attributes
		) {
			Map<IntArrayKey, GridRecord> grid = new HashMap<>();
			Iterator<Instance> iterator = instances.iterator();
			// InsertGrid(X1)
			insertInGrid(grid, iterator.next(), attributes);
			// for i=2 to totalUniverseSize
			IntArrayKey key;
			Instance uPointer;
			while (iterator.hasNext()) {
				// uPointer = universes.get(i);
				uPointer = iterator.next();
				// ifAlreadyExistsInGrid(Xi)
				if (alreadyExistInGrid(grid, uPointer, attributes)) {
					// index = FindIndexInGrid(Xi)
					key = findIndexInGrid(uPointer, attributes);
					// grid(index, count) +=1
					grid.get(key).addInstanceCount();
					// if decisionClassMatched(index, i)=false
					if (!decisionClassMatched(grid, key, uPointer))
						updateUniquenessStatus(grid, key);
				// else
				}else {
					// InsertInGrid(Xi)
					insertInGrid(grid, uPointer, attributes);
				}
			}
			return grid;
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
				Map<IntArrayKey, GridRecord> grid, Instance instance,
				IntegerIterator attributes
		) {
			// for j=1 to totalAttributeInX, i.e. C
			int[] attributeValues = new int[attributes.size()];
			attributes.reset();
			for (int j=0; j<attributeValues.length; j++) {
				//	Grid(NextRow,j) = Xi(j), 把Xi中的属性放入Grid对应的列中
				attributeValues[j] = instance.getAttributeValue(attributes.next());
			}
			// Grid(NextRow, D)=Di; Grid(NextRow, count)=1; Grid(NextRow, cons)=0;
			grid.put(new IntArrayKey(attributeValues), new GridRecord(null, instance.getAttributeValue(0)));
		}
		
		/**
		 * Get if the given {@link Instance} already exists in the {@link GridRecord}
		 * 	{@link List}, i.e. <code>grid</code>.
		 * 
		 * @param grid
		 * 		A {@link List} of {@link GridRecord} as <code>grid</code>.
		 * @param instance
		 * 		A {@link List} of {@link Instance}.
		 * @param attributes
		 * 		Attributes of {@link Instance}.
		 * @return <code>true</code> if the given {@link Instance} exists by given
		 * 		<code>attributes</code>.
		 */
		public static boolean alreadyExistInGrid(
				Map<IntArrayKey, GridRecord> grid, Instance instance,
				IntegerIterator attributes
		) {
			int[] attrValues = new int[attributes.size()];
			attributes.reset();
			for (int i=0; i<attrValues.length; i++)
				attrValues[i] = instance.getAttributeValue(attributes.next());
			return grid.containsKey(new IntArrayKey(attrValues));
		}
		
		/**
		 * Find the {@link Instance} index that attribute values matched in
		 * <code>grid</code>.
		 *
		 * @param instance
		 * 		An {@link Instance}.
		 * @param attributes
		 * 		Attributes of {@link Instance}.
		 * @return An {@link int} value as index in grid. (Starts from 0, -1 as no matched)
		 */
		public static IntArrayKey findIndexInGrid(
				Instance instance, IntegerIterator attributes
		) {
			int[] attrValue = new int[attributes.size()];
			attributes.reset();
			for (int a=0; a<attrValue.length; a++)
				attrValue[a] = instance.getAttributeValue(attributes.next());
			return new IntArrayKey(attrValue);
		}
		
		/**
		 * Get if {@link Instance}'s decision value equals to the one in <code>grid</code>
		 * 	[<code>key</code>].
		 * 
		 * @param grid
		 * 		A {@link List} of {@link GridRecord} as grid.
		 * @param key
		 * 		key in <code>grid</code>.
		 * @param instance
		 * 		An {@link Instance}.
		 * @return true if the 2 values matches.
		 */
		public static boolean decisionClassMatched(
				Map<IntArrayKey, GridRecord> grid, IntArrayKey key, Instance instance
		) {
			return Integer.compare(
					grid.get(key).getDecisionClass(),
					instance.getAttributeValue(0)
			)==0;
		}
		
		/**
		 * Update the unique status of <code>grid</code>[<code>key</code>}] from unique to
		 * non-unique.
		 * 
		 * @param grid
		 * 		A {@link List} of {@link GridRecord} as grid.
		 * @param key
		 * 		The key to search in {@link GridRecord}.
		 */
		public static void updateUniquenessStatus(
				Map<IntArrayKey, GridRecord> grid, IntArrayKey key
		) {
			grid.get(key).setClassStatus(false);
		}
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}
}