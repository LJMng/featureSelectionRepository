package featureSelection.tester.procedure.heuristic.liuRoughSet;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.support.calculation.positiveRegion.liuRoughSet.PositiveRegionCalculation4LiuRoughSet;
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
 * LiuRoughSetHeuristicQRTester Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Liu Rough Set Heuristic QR Tester Test")
class LiuRoughSetHeuristicQRTesterTest
	extends BasicTester
{
	private boolean logOn = true;

	@Test
	void testExec() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();

		ProcedureParameters params = new ProcedureParameters()
				// U
				.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
				// C
				.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
				// set feature (subset) importance calculation class, one of the following:
				//  PositiveRegionCalculation4LiuRoughSet
				.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveRegionCalculation4LiuRoughSet.class)
				// set feature (subset) importance calculation class, one of the following:
				//  PositiveRegionCalculation4LiuRoughSet: int
				.set(true, ParameterConstants.PARAMETER_SIG_DEVIATION, 0);

		// Create a procedure.
		LiuRoughSetHeuristicQRTester<Double> tester = new LiuRoughSetHeuristicQRTester<>(params, logOn);
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
