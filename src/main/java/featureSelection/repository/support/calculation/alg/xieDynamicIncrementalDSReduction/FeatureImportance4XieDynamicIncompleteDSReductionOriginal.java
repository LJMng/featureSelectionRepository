package featureSelection.repository.support.calculation.alg.xieDynamicIncrementalDSReduction;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.xieDynamicIncompleteDSReduction.DynamicIncompleteDecisionSystemReductionAlgorithm;
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
 * @see FeatureImportance4XieDynamicIncompleteDSReduction
 * @see FeatureImportance4XieDynamicIncompleteDSReductionFixed
 * @see #updateToleranceClass4ObjectRelatedUpdate(Collection, Map, IntegerIterator, Map)
 * 
 * @author Benjamin_L
 *
 * @param <V>
 * 		Type of feature importance.
 */
public interface FeatureImportance4XieDynamicIncompleteDSReductionOriginal<V>
	extends FeatureImportance4XieDynamicIncompleteDSReduction<V>
{
	/**
	 * Implemented based on the algorithm in the original paper 
	 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S0888613X17302918">"A novel incremental
	 * attribute reduction approach for dynamic incomplete decision systems"</a> by Xiaojun Xie,
	 * Xiaolin Qin.
	 * <p>
	 * <strong>Notice</strong>: Using the original algorithm, an updated instance x*[i] would fail
	 * to be included in an other updated instance x*[j](i!=j) if they are tolerant with each other.
	 * In the original algorithm, it only adds possible x[k] in un-updated instance set into x*[i]'s
	 * tolerance classes, BUT seems to neglected that an updated x*[j] could also be tolerant with
	 * x*[i] and should also be added into x*[i]'s tolerance classes too.
	 * <p>
	 * To stick to the original algorithm and the original paper as well as the work of the authors,
	 * this default method does no any changes to it. However,
	 */
	default Map<Instance, Collection<Instance>> updateToleranceClass4ObjectRelatedUpdate(
			Collection<Instance> originalInstances,
			Map<Instance, Collection<Instance>> previousTolerances,
			IntegerIterator previousToleranceAttributes,
			Map<Instance, AlteredInstanceItem> alterInstanceItems
	) throws Exception {
		// Initialize T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x) =
		//		T<sub>B</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x)
		@SuppressWarnings("unchecked")
		Map<Instance, Collection<Instance>> tolerances =
			DynamicIncompleteDecisionSystemReductionAlgorithm
				.Basic
				.copyToleranceClass(previousTolerances, HashSet.class);
		
		// for each x[j] in U-U<sub>ALT</sub> do:
		for (Instance ins: originalInstances) {
			// (skip ones in U<sub>ALT</sub>)
			if (alterInstanceItems.containsKey(ins))	continue;
			// T<sub>B</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x[j]) = 
			// 	T<sub>B</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x[j]) - U<sub>ALT</sub>
			Collection<Instance> tolerancesOfIns = tolerances.get(ins);
			if (tolerancesOfIns!=null && !tolerancesOfIns.isEmpty()) {
				tolerancesOfIns.removeAll(alterInstanceItems.keySet());
			}
		}
		
		// for each x[i] in U<sub>ALT</sub> do:
		for (Instance ins: originalInstances) {
			// (only ones in U<sub>ALT</sub>)
			AlteredInstanceItem altItem = alterInstanceItems.get(ins);
			if (altItem==null)	continue;
			// T<sub>B</sub><sup>S<sub>U</sub><sup>N</sup></sup>(x[i]) ← x[i]
			Instance substance = altItem.getAlteredInstance();

			Collection<Instance> tolerancesOfIns = new LinkedList<>();
			tolerancesOfIns.add(substance);
			
			tolerances.remove(ins);
			tolerances.put(substance, tolerancesOfIns);
		}
		
		// for each x[k] in U - U<sub>ALT</sub> and x[i]* in U<sub>ALT</sub>
		Collection<Instance> tolerancesOfIns;
		for (Instance ins1: originalInstances) {
			// (skip ones in U<sub>ALT</sub>)
			if (alterInstanceItems.containsKey(ins1))	continue;
			for (Instance ins2: originalInstances) {
				// (only ones in U<sub>ALT</sub>)
				AlteredInstanceItem altItem = alterInstanceItems.get(ins2);
				if (altItem==null)	continue;
				// if (x[k], x[i]*) in T(B) then
				if (DynamicIncompleteDecisionSystemReductionAlgorithm
						.Basic
						.tolerant(ins1, altItem.getAlteredInstance(), previousToleranceAttributes)
				) {
					// T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[k]) ← 
					//	T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[k]) ∪
					//	{x[i]*}
					tolerancesOfIns = tolerances.get(ins1);
					tolerancesOfIns.add(altItem.getAlteredInstance());
					// T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[i]*) ← 
					//	T<sub>B</sub><sup>S<sub>U</sub><sup>N+1</sup></sup>(x[i]*) ∪
					//	{x[k]}
					tolerancesOfIns = tolerances.get(altItem.getAlteredInstance());
					tolerancesOfIns.add(ins1);
				}
			}
		}
		
		return tolerances;
	}
	
}
