package featureSelection.tester.procedure.heuristic.compactedDecisionTable.original;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.entity.alg.compactedDecisionTable.impl.original.decisionNumber.HashMapDecisionNumber;
import featureSelection.repository.support.calculation.entropy.combinationConditionEntropy.compactedDecisionTableAlgorithm.CCECalculation4CTOriginalHash;
import featureSelection.repository.support.calculation.entropy.liangConditionEntropy.compactedDecisionTableAlgorithm.LCECalculation4CTOriginalHash;
import featureSelection.repository.support.calculation.entropy.shannonConditionEnpropy.compactedDecisionTableAlgorithm.SCECalculation4CTOriginalHash;
import featureSelection.repository.support.calculation.inConsistency.compactedDecisionTable.InConsistencyCalculation4CTOriginal;
import featureSelection.tester.procedure.basic.BasicTester;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Collection;

/**
 * CompactedDecisionTableHeuristicQRTester Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Compacted Decision Table Heuristic QR Tester Test")
class CompactedDecisionTableHeuristicQRTesterTest
		extends BasicTester
{
	private boolean execCore = false;
	private boolean logOn = true;

	private ProcedureParameters getCommonProcedureParameters(){
		double defaultSigDeviation = 10e-11;
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();

		return new ProcedureParameters()
						// U
						.setNonRoot(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
						// C
						.setNonRoot(ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
						// execute Core ?
						.setNonRoot(ParameterConstants.PARAMETER_QR_EXEC_CORE, execCore)
						// set feature (subset) importance calculation class, one of the following:
						//  SCECalculation4CTOriginalHash
						.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, SCECalculation4CTOriginalHash.class)
						// set significance deviation for calculation:
						//  SCECalculation4CTOriginalHash: double
						.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, defaultSigDeviation)
						// Class for decision number counting.
						.setNonRoot(ParameterConstants.PARAMETER_DECISION_NUMBER_CLASS, HashMapDecisionNumber.class);
	}

	private void commonExec(ProcedureParameters parameters) throws Exception{
		// Create a procedure.
		CompactedDecisionTableHeuristicQRTester<Double, HashMapDecisionNumber> tester =
				new CompactedDecisionTableHeuristicQRTester<>(parameters, logOn);

		// Execute
		Collection<Integer> red = tester.exec();
		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		System.out.println("result : "+red);
		System.out.println("total time : "+tester.getTime());
		System.out.println("tag time : "+tester.getTimeDetailByTags());
		System.out.println("statistics : "+ ProcedureUtils.Statistics.combineProcedureStatics(tester));
	}

	@Test
	void testExecSCE() throws Exception {
		// Load parameters.
		ProcedureParameters parameters = getCommonProcedureParameters();
		parameters.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, SCECalculation4CTOriginalHash.class);
		// Execute.
		commonExec(parameters);
	}

	@Test
	void testExecLCE() throws Exception {
		// Load parameters.
		ProcedureParameters parameters = getCommonProcedureParameters();
		parameters.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, LCECalculation4CTOriginalHash.class);
		// Execute.
		commonExec(parameters);
	}

	@Test
	void testExecCCE() throws Exception {
		// Load parameters.
		ProcedureParameters parameters = getCommonProcedureParameters();
		parameters.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, CCECalculation4CTOriginalHash.class);
		// Execute.
		commonExec(parameters);
	}

	@Test
	void testExecInConsistency() throws Exception {
		// Load parameters.
		ProcedureParameters parameters = getCommonProcedureParameters();
		parameters.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, InConsistencyCalculation4CTOriginal.class);
		parameters.set(true, ParameterConstants.PARAMETER_SIG_DEVIATION, 0);
		// Execute.
		commonExec(parameters);
	}
}
