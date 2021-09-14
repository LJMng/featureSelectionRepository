package featureSelection.tester.procedure.heuristic.positiveApproximationAccelerator.original;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.support.calculation.alg.PositiveApproximationAcceleratorCalculation;
import featureSelection.repository.support.calculation.entropy.combinationConditionEntropy.positiveApproximationAccelerator.CCECalculation4ACCOriginal;
import featureSelection.repository.support.calculation.entropy.liangConditionEntropy.positiveApproximationAccelerator.LCECalculation4ACCOriginal;
import featureSelection.repository.support.calculation.entropy.shannonConditionEnpropy.positiveApproximationAccelerator.SCECalculation4ACCOriginal;
import featureSelection.tester.procedure.basic.BasicTester;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Collection;

/**
 * PositiveApproximationAcceleratorAlgorithmHeuristicQRTester Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Positive Approximation Accelerator Algorithm Heuristic QR Tester Test")
class PositiveApproximationAcceleratorAlgorithmHeuristicQRTesterTest
	extends BasicTester
{
	private boolean execCore = true;
	private boolean logOn = true;
	private double defaultSigDeviation = 1E-13;

	@Test
	public void testSCE() throws Exception {
		commonTest(SCECalculation4ACCOriginal.class);
	}

	@Test
	public void testLCE() throws Exception {
		commonTest(LCECalculation4ACCOriginal.class);
	}

	@Test
	public void testCCE() throws Exception {
		commonTest(CCECalculation4ACCOriginal.class);
	}

	private void commonTest(
			Class<? extends PositiveApproximationAcceleratorCalculation> calculationClass
	) throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();

		// Load parameters.
		ProcedureParameters parameters = new ProcedureParameters()
				// U
				.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
				// C
				.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
				// execute Core ?
				.setNonRoot(ParameterConstants.PARAMETER_QR_EXEC_CORE, execCore)
				// set feature (subset) importance calculation class, one of the following:
				//  SCECalculation4ACCOriginal
				.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, calculationClass)
				// set significance deviation for calculation:
				//  PositiveCalculation4ActiveSampleSelection: double
				.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, defaultSigDeviation);

		// Create a procedure.
		PositiveApproximationAcceleratorAlgorithmHeuristicQRTester<Double> tester =
				new PositiveApproximationAcceleratorAlgorithmHeuristicQRTester<>(parameters, logOn);

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
