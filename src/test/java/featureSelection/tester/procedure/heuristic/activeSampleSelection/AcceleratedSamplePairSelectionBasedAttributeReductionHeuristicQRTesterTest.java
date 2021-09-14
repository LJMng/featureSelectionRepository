package featureSelection.tester.procedure.heuristic.activeSampleSelection;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.support.calculation.positiveRegion.activeSampleSelection.PositiveCalculation4ActiveSampleSelection;
import featureSelection.tester.procedure.basic.BasicTester;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;

/**
 * AcceleratedSamplePairSelectionBasedAttributeReductionHeuristicQRTester Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>08/15/2021</pre>
 */
@DisplayName("Accelerated Sample Pair Selection Based Attribute Reduction Heuristic QR Tester Test")
class AcceleratedSamplePairSelectionBasedAttributeReductionHeuristicQRTesterTest
	extends BasicTester
{
	private boolean logOn = true;

	@Test
	public void exec() throws Exception {
		int[] attributes = getAllConditionalAttributes();

		ProcedureParameters params =
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
		AcceleratedSamplePairSelectionBasedAttributeReductionHeuristicQRTester tester =
				new AcceleratedSamplePairSelectionBasedAttributeReductionHeuristicQRTester(params, logOn);

		Collection<Integer> reduct = tester.exec();

		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		System.out.println("result : "+reduct);
		System.out.println("total time : "+tester.getTime());
		System.out.println("tag time : "+tester.getTimeDetailByTags());
		System.out.println("statistics : "+ ProcedureUtils.Statistics.combineProcedureStatics(tester));
	}

}
