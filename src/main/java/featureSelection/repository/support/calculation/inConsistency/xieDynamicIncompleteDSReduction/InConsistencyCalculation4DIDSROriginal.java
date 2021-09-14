package featureSelection.repository.support.calculation.inConsistency.xieDynamicIncompleteDSReduction;

import featureSelection.repository.support.calculation.alg.xieDynamicIncrementalDSReduction.FeatureImportance4XieDynamicIncompleteDSReductionOriginal;

/**
 * An implementation for Inconsistency Degree calculation for Xie's Dynamic Incomplete Decision System 
 * Reduction(DIDSR) bases on the paper 
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S0888613X17302918">"A novel incremental
 * attribute reduction approach for dynamic incomplete decision systems"</a> by Xiaojun Xie, Xiaolin Qin.
 * 
 * @author Benjamin_L
 */
public class InConsistencyCalculation4DIDSROriginal 
	extends InConsistencyCalculation4DIDSRDefault
	implements FeatureImportance4XieDynamicIncompleteDSReductionOriginal<Integer>
{
}