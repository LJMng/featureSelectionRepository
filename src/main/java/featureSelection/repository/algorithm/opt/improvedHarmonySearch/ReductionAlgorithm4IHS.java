package featureSelection.repository.algorithm.opt.improvedHarmonySearch;

import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.improvedHarmonySearch.GenerationRecord;
import featureSelection.repository.entity.opt.improvedHarmonySearch.ReductionParameters;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.harmony.HarmonyFactory;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class ReductionAlgorithm4IHS<FI extends FeatureImportance<Sig>,
											Sig extends Number, 
											CollectionItem,
											FValue extends FitnessValue<Sig>>
	implements ReductionAlgorithm<FI, Sig, CollectionItem, FValue>
{
	protected Map<Integer, Integer> attributeIndexDictionary = null;

	@Override
	public FValue fitnessValue(
			FI calculation, Collection<CollectionItem> collection,
			int[] attributes, int[] attributeIndexes
	) {
		if (attributeIndexDictionary==null)	initAttributeIndexDictionary(attributes);
		
		return fitnessValue(calculation, collection, indexes2ActualAttributes(attributes, attributeIndexes));
	}

	@Override
	public Harmony<?> inspection(
			FI calculation, Sig sigDeviation, Harmony<?> harmony,
			int harmonySearchLength, Collection<CollectionItem> collection,
			int[] attributes
	) {
		if (attributeIndexDictionary==null)	initAttributeIndexDictionary(attributes);
		
		int[] actualAttributes = indexes2ActualAttributes(attributes, harmony.getAttributes());
		Collection<Integer> red = inspection(calculation, sigDeviation, collection, actualAttributes);
		return HarmonyFactory.newHarmony(
				harmony.getClass(), 
				harmonySearchLength, 
				actualAttributes2Indexes(red)
			);
	}
	
	/**
	 * Compare by {@link FitnessValue#getValue()}.
	 */
	@Override
	public int compareToMaxFitness(Fitness<Sig, FValue> bestFitness, ReductionParameters<Sig, Harmony<?>, FValue> params) {
		double bestFitnessV = bestFitness==null? 0: bestFitness.getFitnessValue().getValue().doubleValue();
		double maxFitnessV = params.getMaxFitness().getValue().doubleValue();
		return Double.compare(bestFitnessV, maxFitnessV);
	}
	/**
	 * Compare by {@link FitnessValue#getValue()}.
	 */
	public int compareToBestFitness(GenerationRecord<FValue> geneRecord, Fitness<Sig, FValue> fitness) {
		double bestFitnessV = geneRecord.getBestFitness()==null? 0: geneRecord.getBestFitness().getValue().doubleValue();
		double fitnessV = fitness==null? 0: fitness.getFitnessValue().getValue().doubleValue();
		return Double.compare(bestFitnessV, fitnessV);
	}

	/**
	 * Initiate a dictionary using {@link Map} with attribute values as keys and indexes of the value as
	 * values: {@link #attributeIndexDictionary}.
	 * 
	 * @param attributes
	 * 		Original attributes of {@link Instance}. (Starts from 1)
	 */
	protected void initAttributeIndexDictionary(int[] attributes) {
		Map<Integer, Integer> attributeIndexMap = new HashMap<>(attributes.length);
		for (int i=0; i<attributes.length; i++)	attributeIndexMap.put(attributes[i], i);
		attributeIndexDictionary = Collections.unmodifiableMap(attributeIndexMap);
	}

	/**
	 * Transfer actual attribute values to attribute indexes using <code>attributeIndexDictionary</code>.
	 * 
	 * @param actualAttributes
	 * 		The actual attributes of {@link Instance}. (Starts from 1)
	 * @return {@link int[]} as indexes of attributes. (Starts from 0)
	 */
	private int[] actualAttributes2Indexes(Collection<Integer> actualAttributes) {
		int[] attributeIndexes = new int[actualAttributes.size()];
		Iterator<Integer> iterator = actualAttributes.iterator();
		for (int i=0; i<attributeIndexes.length; i++)
			attributeIndexes[i] = attributeIndexDictionary.get(iterator.next());
		return attributeIndexes;
	}

	/**
	 * Get actual attributes of {@link Instance} based on the given <code>indexes</code>.
	 * 
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @param indexes
	 * 		Indexes of {@link Instance} attributes. (Starts from 0)
	 * @return An {@link int[]} as the actual attributes of {@link Instance}.
	 */
	protected int[] indexes2ActualAttributes(int[] attributes, int...indexes) {
		int[] actual = new int[indexes.length];
		for (int i=0; i<indexes.length; i++)	actual[i] = attributes[indexes[i]];
		return actual;
	}
}