package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.staticData;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.support.calculation.dependency.roughEquivalentClassBased.DependencyCalculation4RSCNEC;
import featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased.PositiveRegionCalculation4RSCNEC;
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
 * NestedEquivalenceClassBasedAlgorithmSimpleRealtimeCountingHeuristicQRTester Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Nested Equivalence Class Based Algorithm Simple Realtime Counting Heuristic QR Tester Test")
class NestedEquivalenceClassBasedAlgorithmSimpleRealtimeCountingHeuristicQRTesterTest
	extends BasicTester
{
	private boolean logOn = true;

	@Test
	public void testExec() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();

		// Load parameters.
		ProcedureParameters parameters = new ProcedureParameters()
				// U
				.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
				// C
				.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
				// set feature (subset) importance calculation class, one of the following:
				//  PositiveRegionCalculation4RSCNEC
				//  DependencyRegionCalculation4RSCNEC
				.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveRegionCalculation4RSCNEC.class)
				// set significance deviation for calculation:
				//  PositiveRegionCalculation4RSCNEC: int
				//  DependencyRegionCalculation4RSCNEC: double
				.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, 0);

		// Create a procedure.
		NestedEquivalenceClassBasedAlgorithmSimpleRealtimeCountingHeuristicQRTester<Integer> tester =
				new NestedEquivalenceClassBasedAlgorithmSimpleRealtimeCountingHeuristicQRTester<>(parameters, logOn);

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
