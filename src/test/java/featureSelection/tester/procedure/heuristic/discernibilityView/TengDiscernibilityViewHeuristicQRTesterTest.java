package featureSelection.tester.procedure.heuristic.discernibilityView;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.support.calculation.discernibility.tengDiscernibilityView.DiscernibilityCalculation4TengDiscernibilityView;
import featureSelection.repository.support.calculation.discernibility.tengDiscernibilityView.DiscernibilityCalculation4TengDiscernibilityView4LongValue;
import featureSelection.repository.support.shrink.discernibilityView.Shrink4TengDiscernibilityView;
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
 * TengDiscernibilityViewHeuristicQRTester Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Teng Discernibility View Heuristic QR Tester Test")
class TengDiscernibilityViewHeuristicQRTesterTest
	extends BasicTester
{
	private boolean logOn = true;

	@Test
	void test() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();

		ProcedureParameters params = new ProcedureParameters()
				// U
				.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
				// C
				.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
				// set feature (subset) importance calculation class, one of the following:
				//  DiscernibilityCalculation4TengDiscernibilityView
				//  DiscernibilityCalculation4TengDiscernibilityView4LongValue
				.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, DiscernibilityCalculation4TengDiscernibilityView.class)
				// set feature (subset) importance calculation class, one of the following:
				//  DiscernibilityCalculation4TengDiscernibilityView: int
				//  DiscernibilityCalculation4TengDiscernibilityView4LongValue: long
				.set(true, ParameterConstants.PARAMETER_SHRINK_INSTANCE_CLASS, Shrink4TengDiscernibilityView.class);

		// Create a procedure.
		TengDiscernibilityViewHeuristicQRTester<Integer> tester =
				new TengDiscernibilityViewHeuristicQRTester<>(params, logOn);
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
