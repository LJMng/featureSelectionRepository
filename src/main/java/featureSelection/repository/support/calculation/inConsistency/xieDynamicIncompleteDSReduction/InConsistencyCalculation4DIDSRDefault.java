package featureSelection.repository.support.calculation.inConsistency.xieDynamicIncompleteDSReduction;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.xieDynamicIncompleteDSReduction.DynamicIncompleteDecisionSystemReductionAlgorithm;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesCollector;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainer;
import featureSelection.repository.support.calculation.alg.xieDynamicIncrementalDSReduction.FeatureImportance4XieDynamicIncompleteDSReduction;
import featureSelection.repository.support.calculation.inConsistency.DefaultInConsistencyCalculation;
import lombok.Getter;

/**
 * An implementation for Inconsistency Degree calculation for Xie's Dynamic Incomplete Decision System 
 * Reduction(DIDSR) bases on the paper 
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S0888613X17302918">"A novel incremental attribute 
 * reduction approach for dynamic incomplete decision systems"</a> by Xiaojun Xie, Xiaolin Qin.
 * 
 * @author Benjamin_L
 */
public abstract class InConsistencyCalculation4DIDSRDefault 
	extends DefaultInConsistencyCalculation
	implements FeatureImportance4XieDynamicIncompleteDSReduction<Integer>
{
	@Getter private int inConsistency = 0;
	@Override
	public Integer getResult() {
		return inConsistency;
	}
	
	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}
	
	/**
	 * Calculate the <strong>Inconsistency Degree</strong> of the given attributes:
	 * <p>
	 * &#8706;(B) = |R<sub>B</sub>| = (
	 * 	&Sigma;<sub>x<sub>i</sub>&in;U</sub>
	 * 		|T<sub>B</sub>(x<sub>i</sub>) - T<sub>B∪D</sub>(x<sub>i</sub>)| 
	 * 	) / 2
	 */
	@Override
	public FeatureImportance4XieDynamicIncompleteDSReduction<Integer> calculate(
			Collection<Instance> universeInstances, IntegerIterator attributes,
			ToleranceClassObtainer toleranceClassObtainer,
			InstancesCollector completeData4Attributes,
			Object... args
	) {
		// T<sub>B</sub>(x), for x in U.
		Map<Instance, Collection<Instance>> tolerancesOfAttributes =
			DynamicIncompleteDecisionSystemReductionAlgorithm
				.Basic
				.toleranceClass(
					universeInstances, attributes, 
					toleranceClassObtainer, completeData4Attributes
				);
		// T<sub>B∪D</sub>(x), for x in U.
		Map<Instance, Collection<Instance>> tolerancesOfAttributesWithDecValue =
			DynamicIncompleteDecisionSystemReductionAlgorithm
				.Basic
				.toleranceClassConsideringDecisionValue(tolerancesOfAttributes);
		
		this.calculate(tolerancesOfAttributes, tolerancesOfAttributesWithDecValue, args);
		
		return this;
	}
	
	/**
	 * Calculate the <strong>Inconsistency Degree</strong> of the given tolerance classes(
	 * T<sub>B</sub>(x<sub>i</sub>) and T<sub>B∪D</sub>(x<sub>i</sub>) ):
	 * <p>
	 * &#8706;(B) = |R<sub>B</sub>| = (
	 * 	&Sigma;<sub>x<sub>i</sub>&in;U</sub>
	 * 		|T<sub>B</sub>(x<sub>i</sub>) - T<sub>B∪D</sub>(x<sub>i</sub>)| 
	 * 	) / 2
	 * 
	 * @param tolerancesOfConditionalAttrs
	 * 		T<sub>B</sub>(x<sub>i</sub>)
	 * @param tolerancesOfConditionalAttrsNDecAttr
	 * 		T<sub>B∪D</sub>(x<sub>i</sub>)
	 */
	@Override
	public FeatureImportance4XieDynamicIncompleteDSReduction<Integer> calculate(
			Map<Instance, Collection<Instance>> tolerancesOfConditionalAttrs,
			Map<Instance, Collection<Instance>> tolerancesOfConditionalAttrsNDecAttr,
			Object... args
	) {
		Collection<Instance> universeInstances = tolerancesOfConditionalAttrs.keySet();
		// count in-consistency
		int inConsistency = 0;
		Collection<Instance> toleranceOfAttributes, toleranceOfAttributesWithDecisionV;
		for (Instance ins: universeInstances) {
			toleranceOfAttributes = tolerancesOfConditionalAttrs.get(ins);
			if (toleranceOfAttributes==null)	
				continue;
			else
				toleranceOfAttributes = new HashSet<>(toleranceOfAttributes);

			toleranceOfAttributesWithDecisionV = tolerancesOfConditionalAttrsNDecAttr.get(ins);
			if (toleranceOfAttributesWithDecisionV==null)
				toleranceOfAttributesWithDecisionV = Collections.emptySet();

			// in-consistency = in-consistency + | T<sub>B</sub>(x[i])- T<sub>B∪D</sub>(x[i])|
			if (!toleranceOfAttributesWithDecisionV.isEmpty())
				toleranceOfAttributes.removeAll(toleranceOfAttributesWithDecisionV);
			inConsistency += toleranceOfAttributes.size();
		}
		// Each element of the inconsistent object set has been added twice. 
		inConsistency = inConsistency / 2;
		
		this.inConsistency = inConsistency;
		return this;
	}

	
	protected static Map<Instance, Collection<Instance>> tolerancesOfAttributesWithDecisionValue(
			Collection<Instance> universeInstances, IntegerIterator attributes,
			ToleranceClassObtainer toleranceClassObtainer,
			InstancesCollector completeData4Attributes
	){
		int[] attributesWithDecisionV = new int[attributes.size()+1];
		attributes.reset();
		for (int i=0; i<attributes.size(); i++)	attributesWithDecisionV[i] = attributes.next();
		//	Skip set attributes[-1]=0 for it has been initialised as 0.
		Map<Instance, Collection<Instance>> tolerancesOfAttributesWithDecisionV =
			DynamicIncompleteDecisionSystemReductionAlgorithm
				.Basic
				.toleranceClass(
					universeInstances, new IntegerArrayIterator(attributesWithDecisionV),
					toleranceClassObtainer, 
					toleranceClassObtainer.getCacheInstanceGroups(
						universeInstances, new IntegerArrayIterator(attributesWithDecisionV)
					)
				);
		return tolerancesOfAttributesWithDecisionV;
	}//*/
}