package featureSelection.repository.support.calculation.alg.xieDynamicIncrementalDSReduction;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesCollector;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainer;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.objectRelatedUpdate.AlteredInstanceItem;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;


/**
 * An interface for Feature Importance calculation for Xie's Dynamic Incomplete Decision System Reduction
 * (DIDSR) bases on the paper 
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S0888613X17302918">"A novel incremental attribute 
 * reduction approach for dynamic incomplete decision systems"</a> by Xiaojun Xie, Xiaolin Qin.
 * 
 * @see FeatureImportance4XieDynamicIncompleteDSReductionFixed
 * @see FeatureImportance4XieDynamicIncompleteDSReductionOriginal
 * 
 * @author Benjamin_L
 *
 * @param <V>
 * 		Type of feature importance.
 */
@RoughSet
public interface FeatureImportance4XieDynamicIncompleteDSReduction<V>
	extends FeatureImportance<V>
{
	/**
	 * Calculate the given features' importance.
	 * 
	 * @param universeInstances
	 * 		{@link Instance}s.
	 * @param attributes
	 * 		Attributes of {@link Instance}.
	 * @param completeData4Attributes
	 * 		{@link InstancesCollector} that contains complete data (in <strong>
	 * 		<code>universeInstances</code></strong>) for every attributes(in <code>attributes</code>).
	 * @param args
	 * 		Extra arguments.
	 * @return <code>this</code>.
	 */
	FeatureImportance4XieDynamicIncompleteDSReduction<V> calculate(
			Collection<Instance> universeInstances, IntegerIterator attributes,
			ToleranceClassObtainer toleranceClassObtainer,
			InstancesCollector completeData4Attributes,
			Object...args
	);
	
	/**
	 * Calculate the given features' importance based on T<sub>B</sub> and T<sub>B∪D</sub>.
	 * 
	 * @param tolerancesOfConditionalAttrs
	 * 		Tolerance classes of {@link Instance}s using (partial) conditional attributes to
	 * 		partition.
	 * @param tolerancesOfConditionalAttrsNDecAttr
	 * 		Tolerance classes of {@link Instance}s using (partial) conditional attributes and
	 * 		decision attribute to partition.
	 * @param args
	 * 		Extra arguments.
	 * @return <code>this</code>.
	 */
	FeatureImportance4XieDynamicIncompleteDSReduction<V> calculate(
			Map<Instance, Collection<Instance>> tolerancesOfConditionalAttrs,
			Map<Instance, Collection<Instance>> tolerancesOfConditionalAttrsNDecAttr,
			Object...args
	);

	
	Map<Instance, Collection<Instance>> updateToleranceClass4ObjectRelatedUpdate(
			Collection<Instance> originalInstances,
			Map<Instance, Collection<Instance>> previousTolerances,
			IntegerIterator previousToleranceAttributes,
			Map<Instance, AlteredInstanceItem> alterInstanceItems
	) throws Exception;
	

}