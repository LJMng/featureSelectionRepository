package featureSelection.tester.procedure.heuristic.activeSampleSelection;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.entity.alg.activeSampleSelection.incrementalAttributeReductionResult.ASSResult4Incremental;
import featureSelection.repository.support.calculation.positiveRegion.activeSampleSelection.PositiveCalculation4ActiveSampleSelection;
import featureSelection.tester.procedure.basic.BasicTester;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * SamplePairSelectionBasedAttributeReductionHeuristicQRTester Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Sample Pair Selection Based Attribute Reduction Heuristic QR Tester Test")
class SamplePairSelectionBasedAttributeReductionHeuristicQRTesterTest
	extends BasicTester
{
	private boolean logOn = true;

	@Test
	public void exec() throws Exception{
		int[] attributes = getAllConditionalAttributes();

		ProcedureParameters parameters =
				new ProcedureParameters()
						// U
						.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
						// C
						.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
						// set feature (subset) importance calculation class, one of the following:
						//  PositiveCalculation4ActiveSampleSelection
						.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveCalculation4ActiveSampleSelection.class)
						// set significance deviation for calculation:
						//  PositiveCalculation4ActiveSampleSelection: int
						.set(true, ParameterConstants.PARAMETER_SIG_DEVIATION, 0);

		// Create a procedure.
		SamplePairSelectionBasedAttributeReductionHeuristicQRTester tester =
				new SamplePairSelectionBasedAttributeReductionHeuristicQRTester(parameters, logOn);

		// Execute
		ASSResult4Incremental results = tester.exec();
		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		System.out.println("result : "+results.getReduct());
		System.out.println("total time : "+tester.getTime());
		System.out.println("tag time : "+tester.getTimeDetailByTags());
		System.out.println("statistics : "+ ProcedureUtils.Statistics.combineProcedureStatics(tester));
	}
}
