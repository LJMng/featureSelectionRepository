package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.simpleCounting.realtime;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased.PositiveRegionCalculation4RSCREC;
import featureSelection.tester.procedure.basic.BasicTester;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.simpleCounting.realtime.procedure.core.ClassicCoreProcedureContainer;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.simpleCounting.realtime.procedure.inspect.ClassicReductInspectionProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Collection;

/**
 * RECBasedAlgorithmSimpleRealtimeCountingHeuristicQRTester Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Rec Based Algorithm Simple Realtime Counting Heuristic QR Tester Test")
class RECBasedAlgorithmSimpleRealtimeCountingHeuristicQRTesterTest
	extends BasicTester
{
	private boolean logOn = true;
	private boolean execCore = true;

	@Test
	void testExec() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();

		// Load parameters.
		ProcedureParameters parameters = new ProcedureParameters()
				// U
				.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
				// C
				.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
				// execute Core ?
				.set(true, ParameterConstants.PARAMETER_QR_EXEC_CORE, execCore)
				// set feature (subset) importance calculation class, one of the following:
				//  PositiveRegionCalculation4RSCREC
				//  DependencyRegionCalculation4RSCEEC
				.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveRegionCalculation4RSCREC.class)
				// set significance deviation for calculation:
				//  PositiveRegionCalculation4RSCREC: int
				//  DependencyRegionCalculation4RSCEEC: double
				.set(true, ParameterConstants.PARAMETER_SIG_DEVIATION, 0);

		// Create a procedure.
		RECBasedAlgorithmSimpleRealtimeCountingHeuristicQRTester<Integer> tester =
				new RECBasedAlgorithmSimpleRealtimeCountingHeuristicQRTester<>(parameters, logOn);

		// Execute
		Collection<Integer> red = tester.exec();
		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		System.out.println("result : " + red);
		System.out.println("total time : " + tester.getTime());
		System.out.println("tag time : " + tester.getTimeDetailByTags());
		System.out.println("statistics : " + ProcedureUtils.Statistics.combineProcedureStatics(tester));

	}

}
