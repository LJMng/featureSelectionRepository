package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalDecision;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.support.calculation.entropy.combinationConditionEntropy.roughEquivalentClassBased.extension.IncrementalDecision.CCECalculation4IDREC;
import featureSelection.repository.support.calculation.entropy.liangConditionEntropy.roughEquivalentClassBased.extension.IncrementalDecision.LCECalculation4IDREC;
import featureSelection.repository.support.calculation.entropy.shannonConditionEnpropy.roughEquivalentClassBased.extension.IncrementalDecision.SCECalculation4IDREC;
import featureSelection.repository.support.calculation.inConsistency.roughEquivalentClassBased.InConsistencyCalculation4IDREC;
import featureSelection.tester.procedure.basic.BasicTester;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalDecision.procedure.core.ClassicImprovedCoreProcedureContainer;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalDecision.procedure.inspect.ClassicImprovedReductInspectionProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

/**
 * RoughEquivalenceClassBasedAlgorithmIncrementalDecisionHeuristicQRTester Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Rough Equivalence Class Based Algorithm Incremental Decision Heuristic QR Tester Test")
class RoughEquivalenceClassBasedAlgorithmIncrementalDecisionHeuristicQRTesterTest
	extends BasicTester
{
	private boolean execCore = true;
	private boolean logOn = true;
	private double defaultDeviation = 1E-10;

	@Test
	void testExecSCEDefault() throws Exception {
		// Load parameters.
		ProcedureParameters parameters = loadCommonParameters();
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, SCECalculation4IDREC.class);
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, defaultDeviation);

		// Execute
		commonExecDefault(parameters);
	}

	@Test
	void testExecSCEImproved() throws Exception {
		// Load parameters.
		ProcedureParameters parameters = loadCommonParameters();
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, SCECalculation4IDREC.class);
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, defaultDeviation);

		// Execute.
		commonExecImproved(parameters);
	}


	@Test
	void testExecLCEDefault() throws Exception {
		// Load parameters.
		ProcedureParameters parameters = loadCommonParameters();
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, LCECalculation4IDREC.class);
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, defaultDeviation);

		// Execute
		commonExecDefault(parameters);
	}

	@Test
	void testExecLCEImproved() throws Exception {
		// Load parameters.
		ProcedureParameters parameters = loadCommonParameters();
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, LCECalculation4IDREC.class);
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, defaultDeviation);

		// Execute.
		commonExecImproved(parameters);
	}


	@Test
	void testExecCCEDefault() throws Exception {
		// Load parameters.
		ProcedureParameters parameters = loadCommonParameters();
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, CCECalculation4IDREC.class);
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, defaultDeviation);

		// Execute.
		commonExecDefault(parameters);
	}

	@Test
	void testExecCCEImproved() throws Exception {
		// Load parameters.
		ProcedureParameters parameters = loadCommonParameters();
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, CCECalculation4IDREC.class);
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, defaultDeviation);

		// Execute.
		commonExecImproved(parameters);
	}


	@Test
	void testExecInConsistencyDefault() throws Exception {
		// Load parameters.
		ProcedureParameters parameters = loadCommonParameters();
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, InConsistencyCalculation4IDREC.class);
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, 0);

		// Execute.
		commonExecDefault(parameters);
	}

	@Test
	void testExecInConsistencyImproved() throws Exception {
		// Load parameters.
		ProcedureParameters parameters = loadCommonParameters();
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, InConsistencyCalculation4IDREC.class);
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, 0);

		// Execute.
		commonExecImproved(parameters);
	}


	private ProcedureParameters loadCommonParameters(){
		int[] attributes = getAllConditionalAttributes();

		ProcedureParameters parameters = new ProcedureParameters()
				// U
				.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
				// C
				.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
				// execute Core ?
				.setNonRoot(ParameterConstants.PARAMETER_QR_EXEC_CORE, execCore);

		return parameters;
	}

	private void commonExecDefault(ProcedureParameters parameters)
			throws Exception
	{
		// Create a procedure.
		RoughEquivalenceClassBasedAlgorithmIncrementalDecisionHeuristicQRTester<Double> tester =
				new RoughEquivalenceClassBasedAlgorithmIncrementalDecisionHeuristicQRTester<>(parameters, logOn);

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

	private void commonExecImproved(ProcedureParameters parameters) throws Exception {
		// Create a procedure.
		RoughEquivalenceClassBasedAlgorithmIncrementalDecisionHeuristicQRTester<Double> tester =
				new RoughEquivalenceClassBasedAlgorithmIncrementalDecisionHeuristicQRTester<>(parameters, logOn);

		// set core procedure with improved algorithm.
		tester.getComponent("Obtain Core")
				.setSubProcedureContainer(
						"CoreProcedureContainer",
						// available:
						//  (default) ClassicCoreProcedureContainer
						//  ClassicImprovedCoreProcedureContainer
						new ClassicImprovedCoreProcedureContainer<>(tester.getParameters(), logOn)
				);
		// set inspect procedure with improved algorithm.
		tester.getComponent("Reduct Inspection")
				.setSubProcedureContainer(
						"ReductInspectionProcedureContainer",
						// available:
						//  (default) ClassicReductInspectionProcedureContainer
						//  ClassicImprovedReductInspectionProcedureContainer
						new ClassicImprovedReductInspectionProcedureContainer<>(tester.getParameters(), logOn)
				);

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
}
