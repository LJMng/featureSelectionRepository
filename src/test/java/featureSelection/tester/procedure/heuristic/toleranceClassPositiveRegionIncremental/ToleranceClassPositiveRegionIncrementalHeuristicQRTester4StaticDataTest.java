package featureSelection.tester.procedure.heuristic.toleranceClassPositiveRegionIncremental;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainerByDirectCollectNCache;
import featureSelection.repository.support.calculation.positiveRegion.toleranceClassPositiveRegionIncremental.PositiveCalculation4TCPR;
import featureSelection.tester.procedure.basic.IncompleteDataTester;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;

/**
 * ToleranceClassPositiveRegionIncrementalHeuristicQRTester4StaticData Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Tolerance Class Positive Region Incremental Heuristic QR Tester 4 Static Data Test")
class ToleranceClassPositiveRegionIncrementalHeuristicQRTester4StaticDataTest
	extends IncompleteDataTester
{
	private boolean logOn = true;

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
				// set feature (subset) importance calculation class, one of the following:
				//  PositiveCalculation4TCPR
				.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveCalculation4TCPR.class)
				// set significance deviation for calculation
				.set(true, ParameterConstants.PARAMETER_SIG_DEVIATION, 0)
				// set tolerance class obtainer class, one of the following:
				//  ToleranceClassObtainerOriginal
				//  ToleranceClassObtainerOriginalNCache
				//  ToleranceClassObtainerOriginalUsingCollapse
				//  ToleranceClassObtainerByDirectCollect
				//  ToleranceClassObtainerByDirectCollectNCache
				.set(true, "toleranceClassObtainerClass", ToleranceClassObtainerByDirectCollectNCache.class);

		// Create a procedure.
		ToleranceClassPositiveRegionIncrementalHeuristicQRTester4StaticData tester =
				new ToleranceClassPositiveRegionIncrementalHeuristicQRTester4StaticData(parameters, logOn);

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
