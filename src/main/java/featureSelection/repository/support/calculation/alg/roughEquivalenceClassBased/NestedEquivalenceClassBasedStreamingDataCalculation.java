package featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased;

import java.util.Map;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.nestedEC.NestedEquivalenceClassesInfo;
import featureSelection.repository.entity.alg.rec.nestedEC.interf.nestedEquivalenceClassesMerge.NestedEquivalenceClassesMergerParameters;
import featureSelection.repository.entity.alg.rec.nestedEC.interf.nestedEquivalenceClassesMerge.NestedEquivalenceClassesMerger;
import lombok.AllArgsConstructor;
import lombok.Data;

@RoughSet
public interface NestedEquivalenceClassBasedStreamingDataCalculation<V extends Number,
																	MergeParams extends NestedEquivalenceClassesMergerParameters>
	extends FeatureImportance4NestedEquivalenceClassBased<V>
{
	NestedEquivalenceClassesInfo<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>> getNecInfoWithMap();
	
	/**
	 * Update {@link NestedEquivalenceClass}es by merging one with less elements into the other
	 * one with more elements.
	 * 
	 * @param inputs
	 * 		{@link NestedEquivalenceClassesMergerParameters}.
	 * @param args
	 * 		Extra arguments.
	 * @return <code>this</code> {@link FeatureImportance4NestedEquivalenceClassBased} instance.
	 */
	FeatureImportance4NestedEquivalenceClassBased<V> update4Arrived(
			Update4ArrivedInputs<V, MergeParams> inputs, Object...args
	);
	
	/**
	 * Parameters for {@link NestedEquivalenceClassBasedStreamingDataCalculation#update4Arrived(
	 * Update4ArrivedInputs, Object...)}
	 * <p>
	 * Parameters: 
	 * <ul>
	 * 	<li><strong>attributes</strong>:
	 * 			{@link Instance}'s attributes.
	 * 	</li>
	 * 	<li><strong>previousEquClasses</strong>:
	 * 			Previous {@link EquivalenceClass}.
	 * 	</li>
	 * 	<li><strong>previousReduct</strong>:
	 * 			Previous redust.
	 * 	</li>
	 * 	<li><strong>previousSig</strong>:
	 * 			The significance of previous redust.
	 * 	</li>
	 * 	<li><strong>arrivedEquivalenceClasses</strong>:
	 * 			{@link EquivalenceClass}es of arrived data.
	 * 	</li>
	 * 	<li><strong>necMerger</strong>:
	 * 			Implemented {@link NestedEquivalenceClassesMerger} instance.
	 * 	</li>
	 * </ul>
	 * 
	 * @see NestedEquivalenceClassesMerger
	 * @see NestedEquivalenceClassBasedStreamingDataCalculation#update4Arrived(Update4ArrivedInputs, Object...)
	 * 
	 * @author Benjamin_L
	 *
	 * @param <Sig>
	 * 		Type of feature significance that implements {@link Number}.
	 * @param <MergeParams>
	 * 		Type of implemented {@link NestedEquivalenceClassesMergerParameters}
	 */
	@Data
	@AllArgsConstructor
	public static class Update4ArrivedInputs<Sig extends Number,
											MergeParams extends NestedEquivalenceClassesMergerParameters>
	{
		private int[] attributes;
		
		private Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> previousReductNestedEquClasses;
		private IntegerIterator previousReduct;
		private Sig previousSig;
		
		private Map<IntArrayKey, EquivalenceClass> arrivedEquivalenceClasses;
		
		private NestedEquivalenceClassesMerger<MergeParams, NestedEquivalenceClass<EquivalenceClass>> necMerger;
	}
}