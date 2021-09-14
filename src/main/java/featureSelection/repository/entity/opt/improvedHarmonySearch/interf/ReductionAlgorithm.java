package featureSelection.repository.entity.opt.improvedHarmonySearch.interf;

import featureSelection.basic.model.optimization.OptimizationAlgorithm;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.opt.improvedHarmonySearch.GenerationRecord;
import featureSelection.repository.entity.opt.improvedHarmonySearch.ReductionParameters;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;

import java.util.Collection;

public interface ReductionAlgorithm<FI extends FeatureImportance<Sig>, Sig extends Number, CollectionItem,
									FValue extends FitnessValue<Sig>>
	extends OptimizationAlgorithm
{
	/**
	 * Calculate the fitness/feature significance of the given <code>attributes</code>.
	 * 
	 * @param calculation
	 * 		{@link FI} instance.
	 * @param collection
	 * 		{@link Instance} / {@link EquivalenceClass} {@link Collection}.
	 * @param attributes
	 * 		Original attributes of {@link Instance}. (Starts from 1)
	 * @return Feature importance in {@link Sig}.
	 */
	FValue fitnessValue(FI calculation, Collection<CollectionItem> collection, int[] attributes);
	/**
	 * Calculate the fitness/feature significance of the given <code>attributes</code>.
	 * 
	 * @param calculation
	 * 		{@link FI} instance.
	 * @param collection
	 * 		{@link Instance} / {@link EquivalenceClass} {@link Collection}.
	 * @param attributes
	 * 		Original attributes of {@link Instance}. (Starts from 1)
	 * @param attributeIndexes
	 * 		Indexes of {@link Instance} attributes. (Starts from 0)
	 * @return Feature importance in {@link Sig}.
	 */
	FValue fitnessValue(FI calculation, Collection<CollectionItem> collection, int[] attributes, int[] attributeIndexes);
	Fitness<Sig, FValue> fitness(FI calculation, Harmony<?> harmony, Collection<CollectionItem> collection, int[] attributes);
	
	/**
	 * Inspect redundancy of the given <code>harmony</code>.
	 * 
	 * @param calculation
	 * 		{@link FI} instance.
	 * @param sigDeviation
	 * 		Acceptable deviation when calculating significance of attributes. Considered equal
	 * 		when the difference between two sig is less than the given deviation value.
	 * @param harmony
	 * 		The {{@link Harmony} instance to be inspected.
	 * @param harmonySearchLength
	 * 		The searching length of harmony.
	 * @param collection
	 * 		{@link Instance} / {@link EquivalenceClass} {@link Collection}.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @return
	 */
	Harmony<?> inspection(
			FI calculation, Sig sigDeviation, Harmony<?> harmony,
			int harmonySearchLength, Collection<CollectionItem> collection,
			int[] attributes);

	/**
	 * Inspections for the given <code>attributes</code> which are actual attributes of {@link Instance}.
	 * 
	 * @param calculation
	 * 		{@link FI} instance.
	 * @param sigDeviation
	 * 		Significance deviation.
	 * @param collection
	 * 		{@link Instance} or {@link EquivalenceClass} {@link Collection}.
	 * @param attributes
	 * 		Attributes of {@link Instance}. (Starts from 1)
	 * @return Inspected attributes of {@link Instance}.  (Starts from 1)
	 */
	Collection<Integer> inspection(FI calculation, Sig sigDeviation, Collection<CollectionItem> collection, 
									int[] attributes);

	/**
	 * Comparing current <code>best fitness</code> to {@link ReductionParameters#getMaxFitness()}.
	 * 
	 * @param bestFitness
	 * 		{@link Fitness} instance with the best fitness value.
	 * @param params
	 * 		{@link ReductionParameters} instance.
	 * @return <strong>positive value</strong> if <code>best fitness</code> &gt; <code>max fitness</code>;
	 * 			<strong>negative value</strong> if <code>best fitness</code> &lt; <code>max fitness</code>;
	 * 			<strong>0</strong> if <code>best fitness</code> = <code>max fitness</code>.
	 */
	int compareToMaxFitness(Fitness<Sig, FValue> bestFitness, ReductionParameters<Sig, Harmony<?>, FValue> params);
	/**
	 * Comparing the given <code>fitness</code> to {@link GenerationRecord#getBestFitness()}.
	 * 
	 * @param geneRecord
	 * 		{@link GenerationRecord} instance with the best fitness recorded.
	 * @param fitness
	 * 		{@link Fitness} instance to compare.
	 * @return <strong>positive value</strong> if <code>best fitness</code> &gt; <code>given fitness</code>;
	 * 			<strong>negative value</strong> if <code>best fitness</code> &lt; <code>given fitness</code>;
	 * 			<strong>0</strong> if <code>best fitness</code> = <code>given fitness</code>.
	 */
	int compareToBestFitness(GenerationRecord<FValue> geneRecord, Fitness<Sig, FValue> fitness);
}